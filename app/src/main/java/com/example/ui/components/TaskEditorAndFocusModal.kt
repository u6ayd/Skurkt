package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.RecurrenceRule
import com.example.data.SubtaskEntity
import com.example.data.TaskEntity
import com.example.data.TaskMode
import com.example.data.TaskWithSubtasks
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.NeoCoral
import com.example.ui.theme.NeoMintGreen
import com.example.ui.theme.NeoPalette
import com.example.ui.theme.NeoSkyBlue
import com.example.ui.theme.NeoYellow
import com.example.ui.theme.PaperCanvas
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.SoftSheetSurface
import com.example.ui.theme.neoColorForKey
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Structured-Inspired Task Creation & Edit Sheet (Directly inspired by Screenshot 7:
 * Full-bleed colored top header with large circular icon badge, Time selector, Duration pills
 * [15m, 30m, 45m, 1h, 1.5h], and full-width Continue button, hybridized with Neo-Brutalist 2.dp ink borders).
 */
@Composable
fun StructuredTaskEditorDialog(
    selectedDate: LocalDate,
    editingItem: TaskWithSubtasks?,
    defaultMode: TaskMode,
    defaultToInbox: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        existingId: Long,
        title: String,
        notes: String,
        toInbox: Boolean,
        taskMode: TaskMode,
        startTimeMinutes: Int?,
        durationMinutes: Int,
        colorKey: String,
        iconKey: String,
        subjectTag: String,
        recurrence: RecurrenceRule,
        subtasks: List<Pair<String, String>>,
        sortOrder: Int
    ) -> Unit,
    onMoveToInbox: ((TaskEntity) -> Unit)? = null,
    onDelete: ((Long) -> Unit)? = null
) {
    val existingTask = editingItem?.task
    var title by remember(existingTask) { mutableStateOf(existingTask?.title ?: "") }
    var notes by remember(existingTask) { mutableStateOf(existingTask?.notes ?: "") }
    var subjectTag by remember(existingTask) { mutableStateOf(existingTask?.subjectTag ?: "FLOW") }
    var toInbox by remember(existingTask, defaultToInbox) {
        mutableStateOf(if (existingTask != null) existingTask.dateEpochDay == null else defaultToInbox)
    }
    var taskMode by remember(existingTask, defaultMode) {
        mutableStateOf(existingTask?.modeEnum ?: defaultMode)
    }
    var startTimeMinutes by remember(existingTask) {
        mutableIntStateOf(existingTask?.startTimeMinutes ?: (9 * 60))
    }
    var durationMinutes by remember(existingTask) {
        mutableIntStateOf(existingTask?.durationMinutes ?: 45)
    }
    var colorKey by remember(existingTask) { mutableStateOf(existingTask?.colorKey ?: "MINT") }
    var iconKey by remember(existingTask) { mutableStateOf(existingTask?.iconKey ?: "BOOK") }
    var recurrence by remember(existingTask) {
        mutableStateOf(existingTask?.recurrenceEnum ?: RecurrenceRule.NONE)
    }

    val subtasksList = remember(editingItem) {
        mutableStateListOf<Pair<String, String>>().apply {
            editingItem?.sortedSubtasks?.forEach { add(it.title to it.stageTag) }
        }
    }
    var draftSubtaskTitle by remember { mutableStateOf("") }
    var draftSubtaskStage by remember { mutableStateOf("STEP") }

    val headerColor = neoColorForKey(colorKey)
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE dd. MMM yyyy", Locale.ENGLISH) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            NeoCard(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth(),
                backgroundColor = PaperCanvas,
                cornerRadius = 26.dp,
                shadowOffset = 6.dp,
                contentPadding = PaddingValues(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // 1. Full-Bleed Colored Top Banner (Directly inspired by Screenshot 7's green top header)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(headerColor)
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(PureWhiteSurface)
                                    .border(2.dp, InkBlack, CircleShape)
                                    .testTag("close_editor_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = InkBlack
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (existingTask != null && existingTask.dateEpochDay != null && onMoveToInbox != null) {
                                    NeoPillButton(
                                        text = "To Inbox",
                                        icon = Icons.Default.Inbox,
                                        onClick = { onMoveToInbox(existingTask) },
                                        shadowOffset = 2.dp
                                    )
                                }
                                if (existingTask != null && onDelete != null) {
                                    IconButton(
                                        onClick = { onDelete(existingTask.id) },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(NeoCoral)
                                            .border(2.dp, InkBlack, CircleShape)
                                            .testTag("delete_task_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Task",
                                            tint = InkBlack
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Large Circular Icon Badge + Title Input (Matches Screenshot 7)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(PureWhiteSurface)
                                    .border(2.5.dp, InkBlack, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconForKey(iconKey),
                                    contentDescription = null,
                                    tint = InkBlack,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when {
                                        toInbox -> "BRAIN-DUMP INBOX"
                                        taskMode == TaskMode.TIMED_BLOCK ->
                                            "${formatMinutesAsClock(startTimeMinutes)}–${formatMinutesAsClock(startTimeMinutes + durationMinutes)} (${formatDurationBadge(durationMinutes)})"
                                        taskMode == TaskMode.TOP_BUBBLE -> "TOP ROUTINE BUBBLE • ${formatDurationBadge(durationMinutes)}"
                                        else -> "TIMELESS FLOW STEP • ${formatDurationBadge(durationMinutes)}"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = InkCharcoal
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    placeholder = {
                                        Text(
                                            "Task title (e.g. Read a Book)...",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = InkCharcoal.copy(alpha = 0.6f)
                                        )
                                    },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(PureWhiteSurface, RoundedCornerShape(12.dp))
                                        .testTag("editor_title_input")
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(InkBlack)
                    )

                    // 2. Editor Body Controls
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Destination Pill Bar: Selected Date Flow vs Inbox
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(PureWhiteSurface)
                                .border(2.dp, InkBlack, RoundedCornerShape(14.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (toInbox) "Unscheduled Inbox" else selectedDate.format(dateFormatter),
                                style = MaterialTheme.typography.titleMedium,
                                color = InkBlack
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                NeoPillButton(
                                    text = "Day Flow",
                                    selected = !toInbox,
                                    activeColor = NeoYellow,
                                    shadowOffset = 1.5.dp,
                                    onClick = { toInbox = false }
                                )
                                NeoPillButton(
                                    text = "Inbox",
                                    selected = toInbox,
                                    activeColor = NeoCoral,
                                    shadowOffset = 1.5.dp,
                                    onClick = { toInbox = true }
                                )
                            }
                        }

                        // Structure Mode Selector (Timeless Step vs Timed Slot vs Top Bubble)
                        if (!toInbox) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Structure Mode (With or Without Clock Time)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = InkBlack
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    NeoPillButton(
                                        text = "Timeless Step (No Clock)",
                                        selected = taskMode == TaskMode.TIMELESS_STEP,
                                        activeColor = NeoYellow,
                                        onClick = { taskMode = TaskMode.TIMELESS_STEP },
                                        modifier = Modifier.testTag("mode_timeless_button")
                                    )
                                    NeoPillButton(
                                        text = "Timed Slot (Clock)",
                                        selected = taskMode == TaskMode.TIMED_BLOCK,
                                        activeColor = NeoCoral,
                                        onClick = { taskMode = TaskMode.TIMED_BLOCK },
                                        modifier = Modifier.testTag("mode_timed_button")
                                    )
                                    NeoPillButton(
                                        text = "Top Bubble",
                                        selected = taskMode == TaskMode.TOP_BUBBLE,
                                        activeColor = NeoSkyBlue,
                                        onClick = { taskMode = TaskMode.TOP_BUBBLE },
                                        modifier = Modifier.testTag("mode_bubble_button")
                                    )
                                }
                            }
                        }

                        // Time Selector Card (Directly inspired by Screenshot 7's "Time 18:00-18:45" picker)
                        if (!toInbox && taskMode == TaskMode.TIMED_BLOCK) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Time",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = InkBlack
                                )
                                NeoCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    backgroundColor = PureWhiteSurface,
                                    cornerRadius = 18.dp,
                                    shadowOffset = 3.dp,
                                    contentPadding = PaddingValues(14.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = formatMinutesAsClock((startTimeMinutes - 15).coerceAtLeast(0)),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = InkMuted
                                        )

                                        // Highlighted Coral Time Pill (Matches Screenshot 7)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            NeoPillButton(
                                                text = "-30m",
                                                onClick = {
                                                    startTimeMinutes = (startTimeMinutes - 30).coerceAtLeast(0)
                                                },
                                                shadowOffset = 1.5.dp
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .background(NeoCoral)
                                                    .border(2.dp, InkBlack, RoundedCornerShape(50))
                                                    .padding(horizontal = 24.dp, vertical = 10.dp)
                                            ) {
                                                Text(
                                                    text = "${formatMinutesAsClock(startTimeMinutes)}–${formatMinutesAsClock((startTimeMinutes + durationMinutes).coerceAtMost(23 * 60 + 59))}",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = InkBlack
                                                )
                                            }
                                            NeoPillButton(
                                                text = "+30m",
                                                onClick = {
                                                    startTimeMinutes = (startTimeMinutes + 30).coerceAtMost(23 * 60)
                                                },
                                                shadowOffset = 1.5.dp
                                            )
                                        }

                                        Text(
                                            text = formatMinutesAsClock((startTimeMinutes + 15).coerceAtMost(23 * 60 + 45)),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = InkMuted
                                        )

                                        // Quick Preset Clock Anchors
                                        Row(
                                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf(
                                                "08:00" to 8 * 60,
                                                "09:30" to 9 * 60 + 30,
                                                "12:00" to 12 * 60,
                                                "14:30" to 14 * 60 + 30,
                                                "18:00" to 18 * 60,
                                                "20:30" to 20 * 60 + 30
                                            ).forEach { (lbl, mins) ->
                                                NeoPillButton(
                                                    text = lbl,
                                                    selected = startTimeMinutes == mins,
                                                    activeColor = NeoYellow,
                                                    shadowOffset = 1.dp,
                                                    onClick = { startTimeMinutes = mins }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Duration Pill Bar (Directly inspired by Screenshot 7: 15, 30, 45m, 1h, 1.5h)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Duration",
                                style = MaterialTheme.typography.titleLarge,
                                color = InkBlack
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(50))
                                    .background(PureWhiteSurface)
                                    .border(2.dp, InkBlack, RoundedCornerShape(50))
                                    .padding(6.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    10 to "10m",
                                    15 to "15m",
                                    30 to "30m",
                                    45 to "45m",
                                    60 to "1h",
                                    90 to "1.5h",
                                    120 to "2h"
                                ).forEach { (mins, label) ->
                                    val isSelected = durationMinutes == mins
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(if (isSelected) NeoCoral else SoftSheetSurface)
                                            .then(
                                                if (isSelected) Modifier.border(2.dp, InkBlack, RoundedCornerShape(50))
                                                else Modifier
                                            )
                                            .clickable { durationMinutes = mins }
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = InkBlack
                                        )
                                    }
                                }
                            }
                        }

                        // Color Palette & Category Icon Pickers
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Color & Category Icon",
                                style = MaterialTheme.typography.titleMedium,
                                color = InkBlack
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                NeoPalette.forEach { option ->
                                    val selected = colorKey == option.key
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(option.fill)
                                            .border(if (selected) 3.dp else 1.5.dp, InkBlack, CircleShape)
                                            .clickable { colorKey = option.key },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (selected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = option.label,
                                                tint = InkBlack,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TaskIconCatalog.forEach { item ->
                                    val selected = iconKey == item.key
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (selected) headerColor else PureWhiteSurface)
                                            .border(2.dp, InkBlack, RoundedCornerShape(12.dp))
                                            .clickable { iconKey = item.key },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            tint = InkBlack,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Folder Tag & Recurrence Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = subjectTag,
                                onValueChange = { subjectTag = it },
                                label = { Text("Folder / Tag (e.g. PHY 12)") },
                                singleLine = true,
                                modifier = Modifier.width(170.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                RecurrenceRule.entries.forEach { rule ->
                                    NeoPillButton(
                                        text = rule.label,
                                        selected = recurrence == rule,
                                        activeColor = NeoYellow,
                                        shadowOffset = 1.5.dp,
                                        onClick = { recurrence = rule }
                                    )
                                }
                            }
                        }

                        // Notes & Subtasks / Milestones Builder
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes / Context") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Subtasks & Milestones (${subtasksList.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = InkBlack
                            )
                            subtasksList.forEachIndexed { index, (subTitle, stage) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(PureWhiteSurface)
                                        .border(1.5.dp, InkBlack, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}. $subTitle ($stage)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = InkBlack,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove Subtask",
                                        tint = InkBlack,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable { subtasksList.removeAt(index) }
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = draftSubtaskTitle,
                                    onValueChange = { draftSubtaskTitle = it },
                                    placeholder = { Text("Add milestone (e.g. Lecture, Anki, MCQs)...") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = draftSubtaskStage,
                                    onValueChange = { draftSubtaskStage = it },
                                    placeholder = { Text("Tag") },
                                    singleLine = true,
                                    modifier = Modifier.width(86.dp)
                                )
                                IconButton(
                                    onClick = {
                                        if (draftSubtaskTitle.isNotBlank()) {
                                            subtasksList.add(draftSubtaskTitle.trim() to draftSubtaskStage.trim())
                                            draftSubtaskTitle = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(NeoMintGreen)
                                        .border(2.dp, InkBlack, RoundedCornerShape(12.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Milestone",
                                        tint = InkBlack
                                    )
                                }
                            }
                        }

                        // Full-Width "Continue / Save Structure" Button (Matches Screenshot 7's bottom Coral button)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .drawBehind {
                                    drawRoundRect(
                                        color = InkBlack,
                                        topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                                        size = size,
                                        cornerRadius = CornerRadius(27.dp.toPx(), 27.dp.toPx())
                                    )
                                }
                                .clip(RoundedCornerShape(50))
                                .background(NeoCoral)
                                .border(2.dp, InkBlack, RoundedCornerShape(50))
                                .clickable {
                                    val finalTitle = title.ifBlank { "Untitled Step" }
                                    if (draftSubtaskTitle.isNotBlank()) {
                                        subtasksList.add(draftSubtaskTitle.trim() to draftSubtaskStage.trim())
                                    }
                                    onSave(
                                        existingTask?.id ?: 0L,
                                        finalTitle,
                                        notes,
                                        toInbox,
                                        taskMode,
                                        startTimeMinutes,
                                        durationMinutes,
                                        colorKey,
                                        iconKey,
                                        subjectTag,
                                        recurrence,
                                        subtasksList.toList(),
                                        existingTask?.sortOrder ?: 0
                                    )
                                }
                                .testTag("save_task_continue_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (existingTask == null) "Continue • Add to Structure" else "Save Changes",
                                style = MaterialTheme.typography.titleLarge,
                                color = InkBlack
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full-Screen Anti-Paralysis Focus View:
 * Isolates the single current task so the user never feels overwhelmed by the rest of the day.
 * Uses a battery-efficient timestamp calculation that only ticks while visible and unpaused.
 */
@Composable
fun AntiParalysisFocusScreen(
    focusItem: TaskWithSubtasks?,
    onBackToFlow: () -> Unit,
    onToggleSubtask: (SubtaskEntity) -> Unit,
    onAddQuickSubtask: (Long, String) -> Unit,
    onCompleteAndAdvance: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (focusItem == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            NeoCard(
                modifier = Modifier.widthIn(max = 480.dp),
                backgroundColor = NeoMintGreen,
                cornerRadius = 22.dp,
                shadowOffset = 5.dp,
                contentPadding = PaddingValues(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "All Steps Complete!",
                        style = MaterialTheme.typography.displayMedium,
                        color = InkBlack
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You have cleared every task in today's flow. Add a new step or enjoy your free time.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = InkCharcoal
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    NeoPillButton(
                        text = "Back to Day Flow",
                        selected = true,
                        activeColor = PureWhiteSurface,
                        onClick = onBackToFlow
                    )
                }
            }
        }
        return
    }

    val task = focusItem.task
    val accent = neoColorForKey(task.colorKey)
    val targetSeconds = (task.durationMinutes * 60).coerceAtLeast(60)
    var isRunning by remember(task.id) { mutableStateOf(true) }
    var elapsedSeconds by remember(task.id) { mutableLongStateOf(0L) }
    var newStepTitle by remember { mutableStateOf("") }

    LaunchedEffect(task.id, isRunning) {
        while (isRunning) {
            delay(1000L)
            elapsedSeconds += 1L
        }
    }

    val remainingSeconds = (targetSeconds - elapsedSeconds).coerceAtLeast(0L)
    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val progress = (elapsedSeconds.toFloat() / targetSeconds.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeoPillButton(
                text = "Back to Day Flow",
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                onClick = onBackToFlow,
                shadowOffset = 2.dp
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(NeoYellow)
                    .border(2.dp, InkBlack, RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "ANTI-PARALYSIS FOCUS MODE",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkBlack
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        NeoCard(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth(),
            backgroundColor = accent,
            cornerRadius = 24.dp,
            shadowOffset = 6.dp,
            contentPadding = PaddingValues(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PureWhiteSurface)
                        .border(2.dp, InkBlack, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${task.subjectTag} • DO ONLY THIS RIGHT NOW",
                        style = MaterialTheme.typography.labelLarge,
                        color = InkBlack
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(PureWhiteSurface)
                        .border(2.5.dp, InkBlack, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconForKey(task.iconKey),
                        contentDescription = null,
                        tint = InkBlack,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.displayMedium,
                    color = InkBlack
                )
                if (task.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.notes,
                        style = MaterialTheme.typography.bodyLarge,
                        color = InkCharcoal
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Low-Power Monospace Countdown / Pacing Card
                NeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PureWhiteSurface,
                    cornerRadius = 18.dp,
                    shadowOffset = 3.dp,
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "OPTIONAL PACING TIMER (ZERO CLOCK PRESSURE)",
                            style = MaterialTheme.typography.labelMedium,
                            color = InkMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = String.format(Locale.US, "%02d:%02d", mins, secs),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 46.sp),
                            color = InkBlack
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        NeoProgressBar(progress = progress, fillColor = accent, height = 12.dp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            NeoPillButton(
                                text = if (isRunning) "Pause Timer" else "Resume Timer",
                                icon = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                selected = isRunning,
                                activeColor = NeoYellow,
                                onClick = { isRunning = !isRunning }
                            )
                            NeoPillButton(
                                text = "Reset",
                                icon = Icons.Default.Refresh,
                                onClick = { elapsedSeconds = 0L }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Interactive Step-by-Step Checklist
                NeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PureWhiteSurface,
                    cornerRadius = 18.dp,
                    shadowOffset = 3.dp,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Step-by-Step Milestones (${focusItem.subtasks.count { it.isCompleted }}/${focusItem.subtasks.size})",
                            style = MaterialTheme.typography.titleLarge,
                            color = InkBlack
                        )
                        if (focusItem.sortedSubtasks.isEmpty()) {
                            Text(
                                text = "Break this task into tiny micro-steps below if you feel stuck:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkMuted
                            )
                        }
                        focusItem.sortedSubtasks.forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleSubtask(sub) },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    NeoSquareCheckbox(
                                        checked = sub.isCompleted,
                                        onCheckedChange = { onToggleSubtask(sub) },
                                        accentColor = NeoMintGreen
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = sub.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                        ),
                                        color = if (sub.isCompleted) InkMuted else InkBlack
                                    )
                                }
                                Text(
                                    text = sub.stageTag,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = InkMuted
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newStepTitle,
                                onValueChange = { newStepTitle = it },
                                placeholder = { Text("Add micro-step...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            NeoPillButton(
                                text = "Add",
                                selected = true,
                                activeColor = NeoSkyBlue,
                                onClick = {
                                    if (newStepTitle.isNotBlank()) {
                                        onAddQuickSubtask(task.id, newStepTitle)
                                        newStepTitle = ""
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Complete & Advance to Next Step CTA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .drawBehind {
                            drawRoundRect(
                                color = InkBlack,
                                topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                                size = size,
                                cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx())
                            )
                        }
                        .clip(RoundedCornerShape(50))
                        .background(NeoMintGreen)
                        .border(2.5.dp, InkBlack, RoundedCornerShape(50))
                        .clickable(onClick = onCompleteAndAdvance)
                        .testTag("complete_and_advance_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = InkBlack
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DONE — ADVANCE TO NEXT STEP",
                            style = MaterialTheme.typography.titleLarge,
                            color = InkBlack
                        )
                    }
                }
            }
        }
    }
}
