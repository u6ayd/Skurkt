package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.data.FolderEntity
import com.example.data.TaskWithSubtasks
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.NavRailDark
import com.example.ui.theme.NeoCoral
import com.example.ui.theme.NeoMintGreen
import com.example.ui.theme.NeoSkyBlue
import com.example.ui.theme.NeoYellow
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.SoftSheetSurface
import com.example.ui.theme.sdp
import com.example.ui.theme.ssp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class ExportTimeframeFilter(val label: String) {
    LAST_MONTH("Last Month"),
    OLDER_THAN_30_DAYS("Older than 30 Days"),
    ALL_COMPLETED("All Completed Tasks")
}

fun filterCompletedTasksForExport(
    allTasks: List<TaskWithSubtasks>,
    filter: ExportTimeframeFilter,
    today: LocalDate = LocalDate.now()
): List<TaskWithSubtasks> {
    val completed = allTasks.filter { it.task.isCompleted }
    val todayEpoch = today.toEpochDay()
    val lastMonth = YearMonth.from(today).minusMonths(1)
    val lastMonthStartEpoch = lastMonth.atDay(1).toEpochDay()
    val lastMonthEndEpoch = lastMonth.atEndOfMonth().toEpochDay()

    return when (filter) {
        ExportTimeframeFilter.LAST_MONTH -> {
            val strictLastMonth = completed.filter { item ->
                val epoch = item.task.dateEpochDay ?: return@filter false
                epoch in lastMonthStartEpoch..lastMonthEndEpoch
            }
            if (strictLastMonth.isNotEmpty()) {
                strictLastMonth
            } else {
                completed.filter { item ->
                    val epoch = item.task.dateEpochDay ?: todayEpoch
                    epoch in (todayEpoch - 30L)..todayEpoch
                }
            }
        }

        ExportTimeframeFilter.OLDER_THAN_30_DAYS -> {
            completed.filter { item ->
                val epoch = item.task.dateEpochDay ?: return@filter false
                epoch <= (todayEpoch - 30L)
            }
        }

        ExportTimeframeFilter.ALL_COMPLETED -> completed
    }
}

@Composable
fun CleanupAndExportModal(
    allTasks: List<TaskWithSubtasks>,
    folders: List<FolderEntity>,
    onPurgeTasks: (List<Long>) -> Unit,
    onExportStatusMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val today = remember { LocalDate.now() }
    var selectedFilter by remember { mutableStateOf(ExportTimeframeFilter.LAST_MONTH) }
    var confirmPurge by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }

    val tasksByFilter = remember(allTasks, today) {
        ExportTimeframeFilter.entries.associateWith { filter ->
            filterCompletedTasksForExport(allTasks, filter, today)
        }
    }

    val matchedTasks = tasksByFilter[selectedFilter].orEmpty()

    val totalWorkedMinutes = remember(matchedTasks) {
        matchedTasks.sumOf { item ->
            val t = item.task
            if (t.totalWorkedMinutes > 0) t.totalWorkedMinutes else t.durationMinutes
        }
    }

    val totalBreakMinutes = remember(matchedTasks) {
        matchedTasks.sumOf { it.task.totalBreakMinutes }
    }

    val totalSubtasksDone = remember(matchedTasks) {
        matchedTasks.sumOf { it.completedSubtasksCount }
    }

    var pendingPdfFile by remember { mutableStateOf<File?>(null) }
    var pendingCsvFile by remember { mutableStateOf<File?>(null) }

    val createPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        val src = pendingPdfFile
        pendingPdfFile = null
        if (uri != null && src != null) {
            coroutineScope.launch {
                val saved = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            src.inputStream().use { input -> input.copyTo(out) }
                        } != null
                    }.getOrDefault(false)
                }
                onExportStatusMessage(if (saved) "Saved PDF: ${src.name}" else "Failed to save PDF")
            }
        }
    }

    val createCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val src = pendingCsvFile
        pendingCsvFile = null
        if (uri != null && src != null) {
            coroutineScope.launch {
                val saved = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            src.inputStream().use { input -> input.copyTo(out) }
                        } != null
                    }.getOrDefault(false)
                }
                onExportStatusMessage(if (saved) "Saved CSV: ${src.name}" else "Failed to save CSV")
            }
        }
    }

    val completedLabel = if (matchedTasks.size == 1) "1 task" else "${matchedTasks.size} tasks"
    val milestoneLabel = if (totalSubtasksDone == 1) "1 milestone" else "$totalSubtasksDone milestones"
    val focusedValue = if (totalWorkedMinutes > 0) formatDurationBadge(totalWorkedMinutes) else "0m"
    val breaksSubtitle = if (totalBreakMinutes > 0) "Breaks: ${formatDurationBadge(totalBreakMinutes)}" else "No breaks"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        NeoCard(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth(),
            backgroundColor = PureWhiteSurface,
            cornerRadius = 24.dp,
            contentPadding = PaddingValues(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Cleanup & Export",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = InkBlack
                        )
                        Text(
                            text = "Export progress reports or purge completed tasks",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SoftSheetSurface, CircleShape)
                            .smoothPressClickable(role = Role.Button, onClick = onDismiss)
                            .testTag("close_cleanup_modal_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = InkBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Grouped timeframe selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "TIMEFRAME",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = InkMuted,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )

                    val groupShape = RoundedCornerShape(16.dp)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(groupShape)
                            .background(SoftSheetSurface)
                            .border(1.dp, InkBlack.copy(alpha = 0.08f), groupShape)
                    ) {
                        val filters = ExportTimeframeFilter.entries
                        filters.forEachIndexed { index, filter ->
                            val count = tasksByFilter[filter]?.size ?: 0
                            val isSelected = selectedFilter == filter
                            TimeframeRadioRow(
                                label = filter.label,
                                count = count,
                                selected = isSelected,
                                onClick = {
                                    selectedFilter = filter
                                    confirmPurge = false
                                }
                            )
                            if (index < filters.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    thickness = 1.dp,
                                    color = InkBlack.copy(alpha = 0.07f)
                                )
                            }
                        }
                    }
                }

                // Calm 2-column summary strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExportSummaryTile(
                        caption = "COMPLETED",
                        value = completedLabel,
                        subtitle = milestoneLabel,
                        tint = NeoMintGreen.copy(alpha = 0.28f),
                        modifier = Modifier.weight(1f)
                    )
                    ExportSummaryTile(
                        caption = "FOCUSED TIME",
                        value = focusedValue,
                        subtitle = breaksSubtitle,
                        tint = NeoSkyBlue.copy(alpha = 0.28f),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Export actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExportActionButton(
                        label = if (isExporting) "Generating..." else "Export PDF",
                        icon = Icons.Default.Description,
                        backgroundColor = NeoYellow,
                        onClick = {
                            if (isExporting) return@ExportActionButton
                            isExporting = true
                            coroutineScope.launch {
                                val file = withContext(Dispatchers.IO) {
                                    generateProgressPdfFile(
                                        context = context,
                                        filter = selectedFilter,
                                        matchedTasks = matchedTasks,
                                        allTasks = allTasks,
                                        folders = folders,
                                        totalWorkedMinutes = totalWorkedMinutes,
                                        totalBreakMinutes = totalBreakMinutes
                                    )
                                }
                                isExporting = false
                                if (file != null) {
                                    pendingPdfFile = file
                                    val launched = runCatching {
                                        createPdfLauncher.launch(file.name)
                                    }.isSuccess
                                    if (!launched) {
                                        shareExportedFile(context, file, "application/pdf")
                                        onExportStatusMessage("PDF exported: ${file.name}")
                                    }
                                } else {
                                    onExportStatusMessage("Failed to generate PDF")
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_pdf_button")
                    )

                    ExportActionButton(
                        label = if (isExporting) "Generating..." else "Export CSV",
                        icon = Icons.Default.TableChart,
                        backgroundColor = NeoSkyBlue,
                        onClick = {
                            if (isExporting) return@ExportActionButton
                            isExporting = true
                            coroutineScope.launch {
                                val file = withContext(Dispatchers.IO) {
                                    generateProgressCsvFile(
                                        context = context,
                                        filter = selectedFilter,
                                        matchedTasks = matchedTasks
                                    )
                                }
                                isExporting = false
                                if (file != null) {
                                    pendingCsvFile = file
                                    val launched = runCatching {
                                        createCsvLauncher.launch(file.name)
                                    }.isSuccess
                                    if (!launched) {
                                        shareExportedFile(context, file, "text/csv")
                                        onExportStatusMessage("CSV exported: ${file.name}")
                                    }
                                } else {
                                    onExportStatusMessage("Failed to generate CSV")
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_csv_button")
                    )
                }

                // Destructive purge control (only shown when tasks match the selected timeframe)
                AnimatedVisibility(
                    visible = matchedTasks.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    if (!confirmPurge) {
                        ExportActionButton(
                            label = "Purge $completedLabel",
                            icon = Icons.Default.Delete,
                            backgroundColor = NeoCoral,
                            contentColor = PureWhiteSurface,
                            onClick = { confirmPurge = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("purge_completed_tasks_button")
                        )
                    } else {
                        val confirmShape = RoundedCornerShape(14.dp)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(confirmShape)
                                .background(NeoCoral.copy(alpha = 0.12f))
                                .border(1.dp, NeoCoral.copy(alpha = 0.45f), confirmShape)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Permanently remove $completedLabel (${selectedFilter.label})?",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = InkBlack
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(PureWhiteSurface)
                                        .border(1.dp, InkBlack.copy(alpha = 0.2f), RoundedCornerShape(50))
                                        .smoothPressClickable(role = Role.Button) { confirmPurge = false },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Cancel",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = InkCharcoal
                                    )
                                }
                                ExportActionButton(
                                    label = "Confirm Purge",
                                    icon = Icons.Default.Delete,
                                    backgroundColor = NeoCoral,
                                    contentColor = PureWhiteSurface,
                                    heightDp = 42,
                                    onClick = {
                                        val ids = matchedTasks.map { it.task.id }
                                        onPurgeTasks(ids)
                                        onDismiss()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("confirm_purge_button")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeframeRadioRow(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val rowBg by animateColorAsState(
        targetValue = if (selected) PureWhiteSurface else Color.Transparent,
        label = "timeframe_row_bg"
    )
    val badgeBg by animateColorAsState(
        targetValue = if (selected) NeoYellow else PureWhiteSurface.copy(alpha = 0.85f),
        label = "timeframe_badge_bg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .smoothPressClickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (selected) NeoYellow else Color.Transparent, CircleShape)
                    .border(
                        width = 1.5.dp,
                        color = if (selected) InkBlack else InkMuted.copy(alpha = 0.5f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(InkBlack, CircleShape)
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (selected) InkBlack else InkCharcoal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(badgeBg)
                .border(
                    width = 1.dp,
                    color = if (selected) InkBlack else InkBlack.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(50)
                )
                .padding(horizontal = 10.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                ),
                color = if (selected) InkBlack else InkMuted
            )
        }
    }
}

@Composable
private fun ExportSummaryTile(
    caption: String,
    value: String,
    subtitle: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(tint)
            .border(1.dp, InkBlack.copy(alpha = 0.10f), shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = caption,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            ),
            color = InkCharcoal.copy(alpha = 0.7f),
            maxLines = 1
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = InkBlack,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = InkMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ExportActionButton(
    label: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color = InkBlack,
    heightDp: Int = 46,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .height(heightDp.dp)
            .clip(shape)
            .background(backgroundColor)
            .border(1.5.dp, InkBlack, shape)
            .smoothPressClickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(17.dp)
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BulkDeleteRecurringModal(
    selectedDateLabel: String?,
    oneTimeCount: Int,
    recurringCount: Int,
    onDeleteThisDayOnly: (() -> Unit)?,
    onDeleteAllOccurrences: () -> Unit,
    onDismiss: () -> Unit
) {
    val totalCount = oneTimeCount + recurringCount
    val recurringLabel = if (recurringCount == 1) "1 recurring task" else "$recurringCount recurring tasks"
    val oneTimeSuffix = when (oneTimeCount) {
        0 -> ""
        1 -> " and 1 one-time task"
        else -> " and $oneTimeCount one-time tasks"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        NeoCard(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(),
            backgroundColor = PureWhiteSurface,
            cornerRadius = 24.dp,
            contentPadding = PaddingValues(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Delete $totalCount Selected",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = InkBlack
                        )
                        Text(
                            text = "Includes $recurringLabel$oneTimeSuffix.",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SoftSheetSurface, CircleShape)
                            .smoothPressClickable(role = Role.Button, onClick = onDismiss)
                            .testTag("bulk_delete_close_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = InkBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = if (onDeleteThisDayOnly != null && !selectedDateLabel.isNullOrBlank()) {
                        "Choose whether to remove recurring tasks only from $selectedDateLabel (keeping future days intact) or delete every occurrence."
                    } else {
                        "Deleting recurring tasks here will remove all future and past occurrences."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkCharcoal
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (onDeleteThisDayOnly != null && !selectedDateLabel.isNullOrBlank()) {
                        ExportActionButton(
                            label = "This Day Only ($selectedDateLabel)",
                            icon = Icons.Default.Check,
                            backgroundColor = NeoYellow,
                            contentColor = InkBlack,
                            onClick = onDeleteThisDayOnly,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bulk_delete_this_day_button")
                        )
                    }

                    ExportActionButton(
                        label = "Delete All Occurrences",
                        icon = Icons.Default.Delete,
                        backgroundColor = NeoCoral,
                        contentColor = PureWhiteSurface,
                        onClick = onDeleteAllOccurrences,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bulk_delete_all_occurrences_button")
                    )

                    val cancelShape = RoundedCornerShape(50)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(cancelShape)
                            .background(SoftSheetSurface)
                            .border(1.dp, InkBlack.copy(alpha = 0.14f), cancelShape)
                            .smoothPressClickable(role = Role.Button, onClick = onDismiss)
                            .testTag("bulk_delete_cancel_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = InkCharcoal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UniversalBulkSelectBar(
    selectedCount: Int,
    totalVisibleCount: Int,
    onSelectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onCancel: () -> Unit,
    docked: Boolean = false,
    modifier: Modifier = Modifier
) {
    val allSelected = totalVisibleCount > 0 && selectedCount >= totalVisibleCount
    val pillShape = RoundedCornerShape(50)

    val barContent: @Composable (Modifier) -> Unit = { rowModifier ->
        Row(
            modifier = rowModifier,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Row(
                    modifier = Modifier
                        .height(38.sdp)
                        .clip(pillShape)
                        .background(if (allSelected) NeoYellow else PureWhiteSurface.copy(alpha = 0.14f))
                        .then(
                            if (allSelected) Modifier.border(1.2.dp, InkBlack, pillShape)
                            else Modifier.border(1.dp, PureWhiteSurface.copy(alpha = 0.22f), pillShape)
                        )
                        .smoothPressClickable(role = Role.Button, onClick = onSelectAll)
                        .padding(horizontal = 12.sdp)
                        .testTag("bulk_select_all_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (allSelected) Icons.Default.Check else Icons.Default.SelectAll,
                        contentDescription = null,
                        tint = if (allSelected) InkBlack else PureWhiteSurface,
                        modifier = Modifier.size(16.sdp)
                    )
                    Text(
                        text = if (allSelected) "Deselect All" else "Select All",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 12.5.ssp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (allSelected) InkBlack else PureWhiteSurface,
                        maxLines = 1
                    )
                }

                Text(
                    text = "$selectedCount selected",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 13.ssp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = PureWhiteSurface.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedCount > 0) {
                    Row(
                        modifier = Modifier
                            .height(38.sdp)
                            .clip(pillShape)
                            .background(NeoCoral)
                            .smoothPressClickable(role = Role.Button, onClick = onDeleteSelected)
                            .padding(horizontal = 13.sdp)
                            .testTag("bulk_delete_selected_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = PureWhiteSurface,
                            modifier = Modifier.size(16.sdp)
                        )
                        Text(
                            text = "Delete ($selectedCount)",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 12.5.ssp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = PureWhiteSurface,
                            maxLines = 1
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(38.sdp)
                        .clip(CircleShape)
                        .background(PureWhiteSurface.copy(alpha = 0.14f), CircleShape)
                        .smoothPressClickable(role = Role.Button, onClick = onCancel)
                        .testTag("bulk_cancel_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit Multi-Select",
                        tint = PureWhiteSurface,
                        modifier = Modifier.size(18.sdp)
                    )
                }
            }
        }
    }

    if (docked) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(NavRailDark)
                .navigationBarsPadding()
                .testTag("universal_bulk_select_bar"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(InkBlack)
            )
            barContent(
                Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxWidth()
                    .height(66.sdp)
                    .padding(horizontal = 14.dp)
            )
        }
    } else {
        val capsuleShape = RoundedCornerShape(50)
        Box(
            modifier = modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .shadow(10.dp, capsuleShape, clip = false)
                .clip(capsuleShape)
                .background(NavRailDark, capsuleShape)
                .border(1.dp, PureWhiteSurface.copy(alpha = 0.16f), capsuleShape)
                .testTag("universal_bulk_select_bar")
        ) {
            barContent(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

private fun generateProgressPdfFile(
    context: Context,
    filter: ExportTimeframeFilter,
    matchedTasks: List<TaskWithSubtasks>,
    allTasks: List<TaskWithSubtasks>,
    folders: List<FolderEntity>,
    totalWorkedMinutes: Int,
    totalBreakMinutes: Int
): File? {
    return runCatching {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "strukt_progress_${filter.name.lowercase(Locale.US)}.pdf")
        val document = PdfDocument()

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(26, 26, 26)
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(26, 26, 26)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(55, 55, 55)
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(110, 110, 110)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(26, 26, 26)
            strokeWidth = 1.5f
        }

        val pageWidth = 595
        val pageHeight = 842
        val maxContentY = 790f

        var pageNumber = 1
        var currentPage = document.startPage(
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        )
        var canvas: Canvas = currentPage.canvas
        var y = 48f

        fun startNextPageIfNeeded(requiredSpace: Float = 18f) {
            if (y + requiredSpace > maxContentY) {
                canvas.drawText("Strukt Progress Report • Page $pageNumber", 40f, 818f, footerPaint)
                document.finishPage(currentPage)
                pageNumber++
                currentPage = document.startPage(
                    PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                )
                canvas = currentPage.canvas
                y = 44f
                canvas.drawText(
                    "STRUKT • PROGRESS & CLEANUP REPORT (CONT.)",
                    40f,
                    y,
                    sectionPaint
                )
                y += 10f
                canvas.drawLine(40f, y, 555f, y, linePaint)
                y += 20f
            }
        }

        canvas.drawText("STRUKT • PROGRESS & CLEANUP REPORT", 40f, y, titlePaint)
        y += 20f
        val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))
        canvas.drawText(
            "Filter: ${filter.label}  •  Generated: $dateStr  •  Completed Tasks: ${matchedTasks.size}",
            40f,
            y,
            bodyPaint
        )
        y += 10f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 22f

        canvas.drawText(
            "Total Focused Work: ${formatDurationBadge(totalWorkedMinutes)}   |   Total Break Time: ${formatDurationBadge(totalBreakMinutes)}",
            40f,
            y,
            sectionPaint
        )
        y += 24f

        canvas.drawText("FOLDER READINESS SUMMARY", 40f, y, sectionPaint)
        y += 16f
        val tasksByFolderLower = allTasks.groupBy { it.task.folderName.trim().lowercase(Locale.US) }
        folders.forEach { folder ->
            startNextPageIfNeeded(18f)
            val folderTasks = tasksByFolderLower[folder.name.trim().lowercase(Locale.US)].orEmpty()
            val doneCount = folderTasks.count { it.task.isCompleted }
            val pct = if (folderTasks.isEmpty()) 0 else ((doneCount * 100f) / folderTasks.size).toInt()
            canvas.drawText(
                "[${folder.codeTag}] ${folder.name}: $doneCount / ${folderTasks.size} tasks completed ($pct%)",
                48f,
                y,
                bodyPaint
            )
            y += 15f
        }

        startNextPageIfNeeded(48f)
        y += 14f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 20f
        canvas.drawText("COMPLETED TASKS LOG (${matchedTasks.size})", 40f, y, sectionPaint)
        y += 18f

        matchedTasks.forEachIndexed { idx, item ->
            startNextPageIfNeeded(18f)
            val t = item.task
            val dateLabel = t.dateEpochDay?.let { LocalDate.ofEpochDay(it).toString() } ?: "Inbox"
            val durLabel = formatDurationBadge(
                if (t.totalWorkedMinutes > 0) t.totalWorkedMinutes else t.durationMinutes
            )
            canvas.drawText(
                "${idx + 1}. [$dateLabel] ${t.title} (${t.folderName} • ${t.sectionName}) — $durLabel",
                48f,
                y,
                bodyPaint
            )
            y += 15f
        }

        canvas.drawText("Strukt Progress Report • Page $pageNumber", 40f, 818f, footerPaint)
        document.finishPage(currentPage)
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        file
    }.getOrNull()
}

private fun generateProgressCsvFile(
    context: Context,
    filter: ExportTimeframeFilter,
    matchedTasks: List<TaskWithSubtasks>
): File? {
    return runCatching {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "strukt_progress_${filter.name.lowercase(Locale.US)}.csv")
        val sb = StringBuilder()
        sb.appendLine("Task ID,Title,Date,Mode,Folder,Section,Duration Minutes,Worked Minutes,Break Minutes,Subtasks Done,Total Subtasks")
        for (item in matchedTasks) {
            val t = item.task
            val safeTitle = "\"" + t.title.replace("\"", "\"\"") + "\""
            val safeFolder = "\"" + t.folderName.replace("\"", "\"\"") + "\""
            val safeSection = "\"" + t.sectionName.replace("\"", "\"\"") + "\""
            val dateStr = t.dateEpochDay?.let { LocalDate.ofEpochDay(it).toString() } ?: "Inbox"
            sb.appendLine(
                "${t.id},$safeTitle,$dateStr,${t.taskMode},$safeFolder,$safeSection,${t.durationMinutes},${t.totalWorkedMinutes},${t.totalBreakMinutes},${item.completedSubtasksCount},${item.subtasks.size}"
            )
        }
        file.writeText(sb.toString())
        file
    }.getOrNull()
}

private fun shareExportedFile(context: Context, file: File, mimeType: String) {
    runCatching {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Strukt Progress Export (${file.name})")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Export Progress Report").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
