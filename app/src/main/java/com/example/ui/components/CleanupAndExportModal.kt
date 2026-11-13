package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.data.FolderEntity
import com.example.data.TaskWithSubtasks
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.NeoCoral
import com.example.ui.theme.NeoMintGreen
import com.example.ui.theme.NeoSkyBlue
import com.example.ui.theme.NeoYellow
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.SoftSheetSurface
import com.example.ui.theme.sdp
import com.example.ui.theme.ssp
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CleanupAndExportModal(
    allTasks: List<TaskWithSubtasks>,
    folders: List<FolderEntity>,
    onPurgeTasks: (List<Long>) -> Unit,
    onExportStatusMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val today = remember { LocalDate.now() }
    var selectedFilter by remember { mutableStateOf(ExportTimeframeFilter.LAST_MONTH) }
    var confirmPurge by remember { mutableStateOf(false) }

    val matchedTasks = remember(allTasks, selectedFilter, today) {
        filterCompletedTasksForExport(allTasks, selectedFilter, today)
    }

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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cleanup & Progress Export",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = InkBlack
                        )
                        Text(
                            text = "Export PDF/CSV report & purge completed tasks",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SoftSheetSurface, CircleShape)
                            .border(1.5.dp, InkBlack, CircleShape)
                            .smoothPressClickable(role = Role.Button, onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = InkBlack,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SoftSheetSurface)
                        .border(1.5.dp, InkBlack, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Timeframe Filter",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = InkBlack
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExportTimeframeFilter.entries.forEach { filter ->
                            val count = filterCompletedTasksForExport(allTasks, filter, today).size
                            NeoPillButton(
                                text = "${filter.label} ($count)",
                                selected = selectedFilter == filter,
                                activeColor = NeoYellow,
                                onClick = {
                                    selectedFilter = filter
                                    confirmPurge = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeoCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = NeoMintGreen,
                        cornerRadius = 16.dp,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "COMPLETED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = InkBlack
                            )
                            Text(
                                text = "${matchedTasks.size} tasks",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = InkBlack
                            )
                            Text(
                                text = "$totalSubtasksDone milestones",
                                style = MaterialTheme.typography.labelSmall,
                                color = InkCharcoal
                            )
                        }
                    }

                    NeoCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = NeoSkyBlue,
                        cornerRadius = 16.dp,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "FOCUSED TIME",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = InkBlack
                            )
                            Text(
                                text = formatDurationBadge(totalWorkedMinutes),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = InkBlack
                            )
                            Text(
                                text = "Breaks: ${formatDurationBadge(totalBreakMinutes)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = InkCharcoal
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeoPillButton(
                        text = "Export PDF",
                        icon = Icons.Default.Description,
                        selected = true,
                        activeColor = NeoYellow,
                        onClick = {
                            val file = generateProgressPdfFile(
                                context = context,
                                filter = selectedFilter,
                                matchedTasks = matchedTasks,
                                allTasks = allTasks,
                                folders = folders,
                                totalWorkedMinutes = totalWorkedMinutes,
                                totalBreakMinutes = totalBreakMinutes
                            )
                            if (file != null) {
                                shareExportedFile(context, file, "application/pdf")
                                onExportStatusMessage("PDF exported: ${file.name}")
                            } else {
                                onExportStatusMessage("Failed to generate PDF")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_pdf_button")
                    )

                    NeoPillButton(
                        text = "Export CSV",
                        icon = Icons.Default.TableChart,
                        selected = true,
                        activeColor = NeoSkyBlue,
                        onClick = {
                            val file = generateProgressCsvFile(
                                context = context,
                                filter = selectedFilter,
                                matchedTasks = matchedTasks
                            )
                            if (file != null) {
                                shareExportedFile(context, file, "text/csv")
                                onExportStatusMessage("CSV exported: ${file.name}")
                            } else {
                                onExportStatusMessage("Failed to generate CSV")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_csv_button")
                    )
                }

                if (matchedTasks.isNotEmpty()) {
                    if (!confirmPurge) {
                        NeoPillButton(
                            text = "Purge ${matchedTasks.size} Completed Tasks",
                            icon = Icons.Default.Delete,
                            selected = true,
                            activeColor = NeoCoral,
                            onClick = { confirmPurge = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("purge_completed_tasks_button")
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SoftSheetSurface)
                                .border(1.5.dp, InkBlack, RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Permanently remove ${matchedTasks.size} completed tasks (${selectedFilter.label}) from database?",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = InkBlack
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                NeoPillButton(
                                    text = "Cancel",
                                    onClick = { confirmPurge = false },
                                    modifier = Modifier.weight(1f)
                                )
                                NeoPillButton(
                                    text = "Confirm Purge",
                                    icon = Icons.Default.Delete,
                                    selected = true,
                                    activeColor = NeoCoral,
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
fun UniversalBulkSelectBar(
    selectedCount: Int,
    totalVisibleCount: Int,
    onSelectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allSelected = totalVisibleCount > 0 && selectedCount >= totalVisibleCount
    NeoCard(
        modifier = modifier
            .widthIn(max = 640.dp)
            .fillMaxWidth()
            .testTag("universal_bulk_select_bar"),
        backgroundColor = PureWhiteSurface,
        cornerRadius = 20.dp,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NeoPillButton(
                    text = if (allSelected) "Deselect All" else "Select All",
                    icon = if (allSelected) Icons.Default.Check else Icons.Default.SelectAll,
                    selected = allSelected,
                    activeColor = NeoYellow,
                    onClick = onSelectAll,
                    modifier = Modifier.testTag("bulk_select_all_button")
                )
                Text(
                    text = "$selectedCount selected",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 13.5.ssp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = InkBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedCount > 0) {
                    NeoPillButton(
                        text = "Delete ($selectedCount)",
                        icon = Icons.Default.Delete,
                        selected = true,
                        activeColor = NeoCoral,
                        onClick = onDeleteSelected,
                        modifier = Modifier.testTag("bulk_delete_selected_button")
                    )
                }

                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(36.sdp)
                        .clip(CircleShape)
                        .background(SoftSheetSurface, CircleShape)
                        .border(1.5.dp, InkBlack, CircleShape)
                        .clickable(onClick = onCancel)
                        .testTag("bulk_cancel_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit Multi-Select",
                        tint = InkBlack,
                        modifier = Modifier.size(18.sdp)
                    )
                }
            }
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
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

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
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(26, 26, 26)
            strokeWidth = 1.5f
        }

        var y = 48f
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
        folders.forEach { folder ->
            if (y > 780f) return@forEach
            val folderTasks = allTasks.filter { it.task.folderName.equals(folder.name, ignoreCase = true) }
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

        y += 14f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 20f
        canvas.drawText("COMPLETED TASKS LOG (${matchedTasks.size})", 40f, y, sectionPaint)
        y += 18f

        matchedTasks.take(28).forEachIndexed { idx, item ->
            if (y > 800f) return@forEachIndexed
            val t = item.task
            val dateLabel = t.dateEpochDay?.let { LocalDate.ofEpochDay(it).toString() } ?: "Inbox"
            val durLabel = formatDurationBadge(if (t.totalWorkedMinutes > 0) t.totalWorkedMinutes else t.durationMinutes)
            canvas.drawText(
                "${idx + 1}. [$dateLabel] ${t.title} (${t.folderName} • ${t.sectionName}) — $durLabel",
                48f,
                y,
                bodyPaint
            )
            y += 15f
        }

        document.finishPage(page)
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
