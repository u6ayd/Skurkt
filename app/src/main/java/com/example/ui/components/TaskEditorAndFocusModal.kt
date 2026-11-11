package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.FolderEntity
import com.example.data.RecurrenceRule
import com.example.data.SubtaskEntity
import com.example.data.TaskEntity
import com.example.data.TaskMode
import com.example.data.TaskWithSubtasks
import com.example.ui.effectiveTaskStepNumber
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
import com.example.ui.theme.sdp
import com.example.ui.theme.ssp
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class EditorSubPopup {
    NONE,
    DURATION_TIME,
    COLOR_ICON,
    FOLDER_SECTION,
    RECURRENCE,
    SUBTASKS
}

/**
 * De-Cramped Structured Task Editor (Inspired by Screenshot 7):
 * - Input placeholder is simply "Task".
 * - Tapping Duration, Color & Icon, Folder, Repeat, or Subtasks opens a dedicated clean Sub-Popup!
 */
@Composable
fun StructuredTaskEditorDialog(
    selectedDate: LocalDate,
    folders: List<FolderEntity>,
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
        folderName: String,
        sectionName: String,
        recurrence: RecurrenceRule,
        recurrenceDayOfMonth: Int,
        subtasks: List<Pair<String, String>>,
        sortOrder: Int
    ) -> Unit,
    onMoveToInbox: ((TaskEntity) -> Unit)? = null,
    onDelete: ((Long) -> Unit)? = null
) {
    val existingTask = editingItem?.task
    var title by remember(existingTask) { mutableStateOf(existingTask?.title ?: "") }
    var notes by remember(existingTask) { mutableStateOf(existingTask?.notes ?: "") }
    var toInbox by remember(existingTask, defaultToInbox) {
        mutableStateOf(if (existingTask != null) existingTask.dateEpochDay == null else defaultToInbox)
    }
    var taskMode by remember(existingTask, defaultMode) {
        mutableStateOf(existingTask?.modeEnum ?: defaultMode)
    }
    var startTimeMinutes by remember(existingTask) {
        mutableIntStateOf(existingTask?.startTimeMinutes ?: (18 * 60))
    }
    var durationMinutes by remember(existingTask) {
        mutableIntStateOf(existingTask?.durationMinutes ?: 45)
    }
    var colorKey by remember(existingTask) { mutableStateOf(existingTask?.colorKey ?: "SAGE") }
    var iconKey by remember(existingTask) { mutableStateOf(existingTask?.iconKey ?: "BOOK") }
    var folderName by remember(existingTask, folders) {
        mutableStateOf(existingTask?.folderName ?: (folders.firstOrNull()?.name ?: "Physics"))
    }
    var sectionName by remember(existingTask, folders) {
        val defaultSec = folders.firstOrNull { it.name == (existingTask?.folderName ?: folders.firstOrNull()?.name) }
            ?.sections?.firstOrNull() ?: "Core"
        mutableStateOf(existingTask?.sectionName ?: defaultSec)
    }
    var recurrence by remember(existingTask) {
        mutableStateOf(existingTask?.recurrenceEnum ?: RecurrenceRule.NONE)
    }
    var recurrenceDayOfMonth by remember(existingTask) {
        mutableIntStateOf(existingTask?.recurrenceDayOfMonth ?: selectedDate.dayOfMonth)
    }
    var stepOrder by remember(existingTask) {
        mutableIntStateOf(existingTask?.let { effectiveTaskStepNumber(it) } ?: 4)
    }

    val subtasksList = remember(editingItem) {
        mutableStateListOf<Pair<String, String>>().apply {
            editingItem?.sortedSubtasks?.forEach { add(it.title to it.stageTag) }
        }
    }

    var activeSubPopup by remember { mutableStateOf(EditorSubPopup.NONE) }

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
                    .widthIn(max = 460.dp)
                    .fillMaxWidth(),
                backgroundColor = PaperCanvas,
                cornerRadius = 28.dp,
                shadowOffset = 5.dp,
                contentPadding = PaddingValues(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // 1. Clean Colored Top Header (Matches Screenshot 7: green banner, circular icon, "Task" input)
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PureWhiteSurface.copy(alpha = 0.85f))
                                    .border(1.5.dp, InkBlack, CircleShape)
                                    .testTag("close_editor_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = InkBlack,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (existingTask != null && existingTask.dateEpochDay != null && onMoveToInbox != null) {
                                    IconButton(
                                        onClick = { onMoveToInbox(existingTask) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(PureWhiteSurface.copy(alpha = 0.85f))
                                            .border(1.5.dp, InkBlack, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Inbox,
                                            contentDescription = "Move to Inbox",
                                            tint = InkBlack,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                if (existingTask != null && onDelete != null) {
                                    IconButton(
                                        onClick = { onDelete(existingTask.id) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(PureWhiteSurface.copy(alpha = 0.85f))
                                            .border(1.5.dp, InkBlack, CircleShape)
                                            .testTag("delete_task_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Task",
                                            tint = InkBlack,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Tapping the large circular icon opens the Color & Icon popup
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(PureWhiteSurface)
                                    .border(2.dp, InkBlack, CircleShape)
                                    .clickable { activeSubPopup = EditorSubPopup.COLOR_ICON },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconForKey(iconKey),
                                    contentDescription = "Change Icon & Color",
                                    tint = InkBlack,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when {
                                        toInbox -> "Inbox"
                                        taskMode == TaskMode.TIMED_BLOCK ->
                                            "${formatMinutesAsClock(startTimeMinutes)}–${formatMinutesAsClock(startTimeMinutes + durationMinutes)} (${formatDurationBadge(durationMinutes)})"
                                        else -> formatDurationBadge(durationMinutes)
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
                                    color = InkCharcoal
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    placeholder = {
                                        Text(
                                            "Task",
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
                                            color = InkMuted
                                        )
                                    },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(PureWhiteSurface, RoundedCornerShape(14.dp))
                                        .testTag("editor_title_input")
                                )
                            }
                        }
                    }

                    // 2. Clean Summary Rows (Each opens a dedicated Sub-Popup so nothing is cramped!)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Destination Toggle Row (Date Flow vs Inbox)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(PureWhiteSurface)
                                .border(1.5.dp, InkBlack, RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (toInbox) "Inbox" else selectedDate.format(dateFormatter),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = InkBlack
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                NeoPillButton(
                                    text = "Timeline",
                                    selected = !toInbox,
                                    activeColor = NeoYellow,
                                    onClick = { toInbox = false }
                                )
                                NeoPillButton(
                                    text = "Inbox",
                                    selected = toInbox,
                                    activeColor = NeoCoral,
                                    onClick = { toInbox = true }
                                )
                            }
                        }

                        // Row 1: Time & Duration Popup Trigger
                        EditorSummaryRow(
                            icon = Icons.Default.Schedule,
                            label = "Time & Duration",
                            valueText = when {
                                toInbox -> formatDurationBadge(durationMinutes)
                                taskMode == TaskMode.TIMED_BLOCK ->
                                    "${formatMinutesAsClock(startTimeMinutes)} • ${formatDurationBadge(durationMinutes)}"
                                taskMode == TaskMode.TOP_BUBBLE ->
                                    "Top Bubble • ${formatDurationBadge(durationMinutes)}"
                                else ->
                                    "Timeless (#$stepOrder) • ${formatDurationBadge(durationMinutes)}"
                            },
                            onClick = { activeSubPopup = EditorSubPopup.DURATION_TIME }
                        )

                        // Row 2: Color & Category Icon Popup Trigger
                        EditorSummaryRow(
                            icon = Icons.Default.Palette,
                            label = "Color & Icon",
                            valueText = NeoPalette.firstOrNull { it.key == colorKey }?.label ?: "Color",
                            onClick = { activeSubPopup = EditorSubPopup.COLOR_ICON }
                        )

                        // Row 3: Folder & Section Popup Trigger
                        EditorSummaryRow(
                            icon = Icons.Default.Folder,
                            label = "Folder",
                            valueText = "$folderName • $sectionName",
                            onClick = { activeSubPopup = EditorSubPopup.FOLDER_SECTION }
                        )

                        // Row 4: Repeat / Recurrence Popup Trigger
                        EditorSummaryRow(
                            icon = Icons.Default.Repeat,
                            label = "Repeat",
                            valueText = if (recurrence == RecurrenceRule.MONTHLY) {
                                "Monthly (Day $recurrenceDayOfMonth)"
                            } else {
                                recurrence.label
                            },
                            onClick = { activeSubPopup = EditorSubPopup.RECURRENCE }
                        )

                        // Row 5: Subtasks Popup Trigger
                        EditorSummaryRow(
                            icon = Icons.Default.Checklist,
                            label = "Subtasks",
                            valueText = if (subtasksList.isEmpty()) "None" else "${subtasksList.size} steps",
                            onClick = { activeSubPopup = EditorSubPopup.SUBTASKS }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Bottom Full-Width Coral "Continue" Button (Matches Screenshot 7)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(50))
                                .background(NeoCoral)
                                .border(2.dp, InkBlack, RoundedCornerShape(50))
                                .clickable {
                                    val finalTitle = title.ifBlank { "Task" }
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
                                        folderName,
                                        sectionName,
                                        recurrence,
                                        recurrenceDayOfMonth,
                                        subtasksList.toList(),
                                        if (taskMode == TaskMode.TIMELESS_STEP) stepOrder else (existingTask?.sortOrder ?: 0)
                                    )
                                }
                                .testTag("save_task_continue_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Continue",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = PureWhiteSurface
                            )
                        }
                    }
                }
            }
        }
    }

    // Dedicated Sub-Popups so each setting has generous breathing room!
    when (activeSubPopup) {
        EditorSubPopup.DURATION_TIME -> {
            TimeAndDurationSubPopup(
                toInbox = toInbox,
                taskMode = taskMode,
                stepOrder = stepOrder,
                startTimeMinutes = startTimeMinutes,
                durationMinutes = durationMinutes,
                onModeChange = { taskMode = it },
                onStepOrderChange = { stepOrder = it },
                onStartTimeChange = { startTimeMinutes = it },
                onDurationChange = { durationMinutes = it },
                onDismiss = { activeSubPopup = EditorSubPopup.NONE }
            )
        }

        EditorSubPopup.COLOR_ICON -> {
            ColorAndIconSubPopup(
                selectedColorKey = colorKey,
                selectedIconKey = iconKey,
                onSelectColor = { colorKey = it },
                onSelectIcon = { iconKey = it },
                onDismiss = { activeSubPopup = EditorSubPopup.NONE }
            )
        }

        EditorSubPopup.FOLDER_SECTION -> {
            FolderAndSectionSubPopup(
                folders = folders,
                selectedFolder = folderName,
                selectedSection = sectionName,
                onSelectFolderAndSection = { f, s ->
                    folderName = f
                    sectionName = s
                },
                onDismiss = { activeSubPopup = EditorSubPopup.NONE }
            )
        }

        EditorSubPopup.RECURRENCE -> {
            RecurrenceSubPopup(
                selectedRule = recurrence,
                dayOfMonth = recurrenceDayOfMonth,
                onSelectRule = { recurrence = it },
                onSelectDayOfMonth = { recurrenceDayOfMonth = it },
                onDismiss = { activeSubPopup = EditorSubPopup.NONE }
            )
        }

        EditorSubPopup.SUBTASKS -> {
            SubtasksSubPopup(
                subtasks = subtasksList,
                notes = notes,
                onNotesChange = { notes = it },
                onDismiss = { activeSubPopup = EditorSubPopup.NONE }
            )
        }

        EditorSubPopup.NONE -> Unit
    }
}

@Composable
private fun EditorSummaryRow(
    icon: ImageVector,
    label: String,
    valueText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PureWhiteSurface)
            .border(1.5.dp, InkBlack, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = InkBlack,
                modifier = Modifier.size(19.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = InkBlack
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = InkMuted
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = InkMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Sub-Popup 1: Time & Duration (Matches Screenshot 7's Time & Duration pickers!).
 */
@Composable
private fun TimeAndDurationSubPopup(
    toInbox: Boolean,
    taskMode: TaskMode,
    stepOrder: Int,
    startTimeMinutes: Int,
    durationMinutes: Int,
    onModeChange: (TaskMode) -> Unit,
    onStepOrderChange: (Int) -> Unit,
    onStartTimeChange: (Int) -> Unit,
    onDurationChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var customHourText by remember(startTimeMinutes) {
        mutableStateOf(String.format(Locale.US, "%02d", (startTimeMinutes / 60).coerceIn(0, 23)))
    }
    var customMinText by remember(startTimeMinutes) {
        mutableStateOf(String.format(Locale.US, "%02d", (startTimeMinutes % 60).coerceIn(0, 59)))
    }
    var customDurationText by remember(durationMinutes) {
        mutableStateOf(durationMinutes.toString())
    }

    Dialog(onDismissRequest = onDismiss) {
        NeoCard(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth(),
            backgroundColor = PureWhiteSurface,
            cornerRadius = 24.dp,
            contentPadding = PaddingValues(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Time & Duration",
                    style = MaterialTheme.typography.titleLarge,
                    color = InkBlack
                )

                if (!toInbox) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NeoPillButton(
                            text = "Timeless",
                            selected = taskMode == TaskMode.TIMELESS_STEP,
                            activeColor = NeoYellow,
                            onClick = { onModeChange(TaskMode.TIMELESS_STEP) },
                            modifier = Modifier.weight(1f)
                        )
                        NeoPillButton(
                            text = "Timed",
                            selected = taskMode == TaskMode.TIMED_BLOCK,
                            activeColor = NeoCoral,
                            onClick = { onModeChange(TaskMode.TIMED_BLOCK) },
                            modifier = Modifier.weight(1f)
                        )
                        NeoPillButton(
                            text = "Bubble",
                            selected = taskMode == TaskMode.TOP_BUBBLE,
                            activeColor = NeoSkyBlue,
                            onClick = { onModeChange(TaskMode.TOP_BUBBLE) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (!toInbox && taskMode == TaskMode.TIMELESS_STEP) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SoftSheetSurface)
                            .border(1.5.dp, InkBlack, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sequence Step (#)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = InkBlack
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(PureWhiteSurface)
                                    .border(1.5.dp, InkBlack, CircleShape)
                                    .clickable { onStepOrderChange((stepOrder - 1).coerceAtLeast(1)) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease Step #",
                                    tint = InkBlack,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NeoYellow)
                                    .border(1.5.dp, InkBlack, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "#$stepOrder",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = InkBlack
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(PureWhiteSurface)
                                    .border(1.5.dp, InkBlack, CircleShape)
                                    .clickable { onStepOrderChange((stepOrder + 1).coerceAtMost(99)) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase Step #",
                                    tint = InkBlack,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                if (!toInbox && taskMode == TaskMode.TIMED_BLOCK) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(SoftSheetSurface)
                            .border(1.5.dp, InkBlack, RoundedCornerShape(18.dp))
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(NeoCoral)
                                .border(1.5.dp, InkBlack, RoundedCornerShape(50))
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${formatMinutesAsClock(startTimeMinutes)} – ${formatMinutesAsClock((startTimeMinutes + durationMinutes).coerceAtMost(23 * 60 + 59))}",
                                style = MaterialTheme.typography.titleMedium,
                                color = PureWhiteSurface
                            )
                        }

                        // Custom Hour : Minute direct inputs + fine steppers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customHourText,
                                onValueChange = { txt ->
                                    val filtered = txt.filter { it.isDigit() }.take(2)
                                    customHourText = filtered
                                    val h = filtered.toIntOrNull()?.coerceIn(0, 23) ?: 0
                                    val m = (startTimeMinutes % 60).coerceIn(0, 59)
                                    onStartTimeChange(h * 60 + m)
                                },
                                label = { Text("Hour (0-23)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = ":",
                                style = MaterialTheme.typography.displaySmall,
                                color = InkBlack
                            )
                            OutlinedTextField(
                                value = customMinText,
                                onValueChange = { txt ->
                                    val filtered = txt.filter { it.isDigit() }.take(2)
                                    customMinText = filtered
                                    val h = (startTimeMinutes / 60).coerceIn(0, 23)
                                    val m = filtered.toIntOrNull()?.coerceIn(0, 59) ?: 0
                                    onStartTimeChange(h * 60 + m)
                                },
                                label = { Text("Min (0-59)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            NeoPillButton(
                                text = "-15m",
                                onClick = { onStartTimeChange((startTimeMinutes - 15).coerceAtLeast(0)) }
                            )
                            NeoPillButton(
                                text = "-1m",
                                onClick = { onStartTimeChange((startTimeMinutes - 1).coerceAtLeast(0)) }
                            )
                            NeoPillButton(
                                text = "+1m",
                                onClick = { onStartTimeChange((startTimeMinutes + 1).coerceAtMost(23 * 60 + 59)) }
                            )
                            NeoPillButton(
                                text = "+15m",
                                onClick = { onStartTimeChange((startTimeMinutes + 15).coerceAtMost(23 * 60 + 59)) }
                            )
                        }
                    }
                }

                // Duration Presets + Custom Minutes Input (any duration from 1m to 720m / 12h)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Duration",
                        style = MaterialTheme.typography.titleMedium,
                        color = InkBlack
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(SoftSheetSurface)
                            .border(1.5.dp, InkBlack, RoundedCornerShape(50))
                            .padding(6.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            15 to "15m",
                            30 to "30m",
                            45 to "45m",
                            60 to "1h",
                            90 to "1.5h",
                            120 to "2h",
                            180 to "3h",
                            240 to "4h"
                        ).forEach { (mins, label) ->
                            val selected = durationMinutes == mins
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (selected) NeoCoral else PureWhiteSurface)
                                    .clickable {
                                        onDurationChange(mins)
                                        customDurationText = mins.toString()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (selected) PureWhiteSurface else InkBlack
                                )
                            }
                        }
                    }

                    // Custom Duration Field (Minutes + Quick -5m / +5m / +30m)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customDurationText,
                            onValueChange = { txt ->
                                val filtered = txt.filter { it.isDigit() }.take(3)
                                customDurationText = filtered
                                val parsed = filtered.toIntOrNull()
                                if (parsed != null && parsed >= 1) {
                                    onDurationChange(parsed.coerceIn(1, 720))
                                }
                            },
                            label = { Text("Custom minutes (e.g. 25, 150)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        NeoPillButton(
                            text = "-5m",
                            onClick = {
                                val next = (durationMinutes - 5).coerceAtLeast(1)
                                onDurationChange(next)
                                customDurationText = next.toString()
                            }
                        )
                        NeoPillButton(
                            text = "+5m",
                            onClick = {
                                val next = (durationMinutes + 5).coerceAtMost(720)
                                onDurationChange(next)
                                customDurationText = next.toString()
                            }
                        )
                    }
                }

                NeoPillButton(
                    text = "Done",
                    selected = true,
                    activeColor = NeoMintGreen,
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Sub-Popup 2: Color & Category Icon Picker.
 */
@Composable
private fun ColorAndIconSubPopup(
    selectedColorKey: String,
    selectedIconKey: String,
    onSelectColor: (String) -> Unit,
    onSelectIcon: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val activeColor = neoColorForKey(selectedColorKey)
    Dialog(onDismissRequest = onDismiss) {
        NeoCard(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth(),
            backgroundColor = PureWhiteSurface,
            cornerRadius = 24.dp,
            shadowOffset = 4.dp,
            contentPadding = PaddingValues(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Color & Icon",
                    style = MaterialTheme.typography.titleLarge,
                    color = InkBlack
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeoPalette.forEach { option ->
                        val selected = selectedColorKey == option.key
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(option.fill)
                                .border(if (selected) 2.5.dp else 1.dp, InkBlack, CircleShape)
                                .clickable { onSelectColor(option.key) },
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

                // Clean 4-column rows of icons
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TaskIconCatalog.chunked(4).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            rowItems.forEach { item ->
                                val selected = selectedIconKey == item.key
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(68.dp)
                                        .clickable { onSelectIcon(item.key) }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(if (selected) activeColor else SoftSheetSurface)
                                            .border(if (selected) 2.dp else 1.dp, InkBlack, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            tint = InkBlack,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = InkBlack
                                    )
                                }
                            }
                        }
                    }
                }

                NeoPillButton(
                    text = "Done",
                    selected = true,
                    activeColor = NeoMintGreen,
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Sub-Popup 3: Folder & Section Selector.
 */
@Composable
private fun FolderAndSectionSubPopup(
    folders: List<FolderEntity>,
    selectedFolder: String,
    selectedSection: String,
    onSelectFolderAndSection: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val activeFolderObj = folders.firstOrNull { it.name.equals(selectedFolder, ignoreCase = true) }
        ?: folders.firstOrNull()
    val sections = activeFolderObj?.sections?.ifEmpty { listOf("Core") } ?: listOf("Core")

    Dialog(onDismissRequest = onDismiss) {
        NeoCard(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth(),
            backgroundColor = PureWhiteSurface,
            cornerRadius = 24.dp,
            shadowOffset = 4.dp,
            contentPadding = PaddingValues(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Folder & Section",
                    style = MaterialTheme.typography.titleLarge,
                    color = InkBlack
                )

                Text(
                    text = "Folder",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkMuted
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    folders.forEach { folder ->
                        val isSelected = folder.name.equals(selectedFolder, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) neoColorForKey(folder.colorKey) else SoftSheetSurface)
                                .border(if (isSelected) 1.8.dp else 1.dp, InkBlack, RoundedCornerShape(12.dp))
                                .clickable {
                                    val firstSec = folder.sections.firstOrNull() ?: "Core"
                                    onSelectFolderAndSection(folder.name, firstSec)
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = folder.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = InkBlack
                            )
                            Text(
                                text = folder.codeTag,
                                style = MaterialTheme.typography.labelMedium,
                                color = InkCharcoal
                            )
                        }
                    }
                }

                Text(
                    text = "Section",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkMuted
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sections.forEach { sec ->
                        NeoPillButton(
                            text = sec,
                            selected = sec.equals(selectedSection, ignoreCase = true),
                            activeColor = NeoYellow,
                            onClick = {
                                onSelectFolderAndSection(activeFolderObj?.name ?: selectedFolder, sec)
                            }
                        )
                    }
                }

                NeoPillButton(
                    text = "Done",
                    selected = true,
                    activeColor = NeoMintGreen,
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Sub-Popup 4: Repeat / Recurrence Picker (Supports Daily, Weekdays, Weekly, and Monthly on Day 1..31).
 */
@Composable
private fun RecurrenceSubPopup(
    selectedRule: RecurrenceRule,
    dayOfMonth: Int,
    onSelectRule: (RecurrenceRule) -> Unit,
    onSelectDayOfMonth: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        NeoCard(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth(),
            backgroundColor = PureWhiteSurface,
            cornerRadius = 24.dp,
            shadowOffset = 4.dp,
            contentPadding = PaddingValues(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Repeat",
                    style = MaterialTheme.typography.titleLarge,
                    color = InkBlack
                )

                RecurrenceRule.entries.forEach { rule ->
                    val selected = selectedRule == rule
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) NeoYellow else SoftSheetSurface)
                            .border(if (selected) 1.8.dp else 1.dp, InkBlack, RoundedCornerShape(12.dp))
                            .clickable { onSelectRule(rule) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = rule.label,
                            style = MaterialTheme.typography.titleMedium,
                            color = InkBlack
                        )
                        if (selected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = InkBlack,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (selectedRule == RecurrenceRule.MONTHLY) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Day of Month",
                            style = MaterialTheme.typography.titleMedium,
                            color = InkBlack
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NeoPillButton(
                                text = "-",
                                onClick = { onSelectDayOfMonth((dayOfMonth - 1).coerceAtLeast(1)) }
                            )
                            Text(
                                text = "$dayOfMonth",
                                style = MaterialTheme.typography.titleLarge,
                                color = InkBlack
                            )
                            NeoPillButton(
                                text = "+",
                                onClick = { onSelectDayOfMonth((dayOfMonth + 1).coerceAtMost(31)) }
                            )
                        }
                    }
                }

                NeoPillButton(
                    text = "Done",
                    selected = true,
                    activeColor = NeoMintGreen,
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Sub-Popup 5: Subtasks & Notes Builder.
 */
@Composable
private fun SubtasksSubPopup(
    subtasks: MutableList<Pair<String, String>>,
    notes: String,
    onNotesChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var draftSubtask by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        NeoCard(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth(),
            backgroundColor = PureWhiteSurface,
            cornerRadius = 24.dp,
            shadowOffset = 4.dp,
            contentPadding = PaddingValues(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Subtasks & Notes",
                    style = MaterialTheme.typography.titleLarge,
                    color = InkBlack
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    placeholder = { Text("Notes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                subtasks.forEachIndexed { idx, (subTitle, _) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SoftSheetSurface)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${idx + 1}. $subTitle",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = InkBlack,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = InkBlack,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { subtasks.removeAt(idx) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = draftSubtask,
                        onValueChange = { draftSubtask = it },
                        placeholder = { Text("Subtask") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            if (draftSubtask.isNotBlank()) {
                                subtasks.add(draftSubtask.trim() to "STEP")
                                draftSubtask = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeoMintGreen)
                            .border(1.8.dp, InkBlack, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = InkBlack
                        )
                    }
                }

                NeoPillButton(
                    text = "Done",
                    selected = true,
                    activeColor = NeoMintGreen,
                    onClick = {
                        if (draftSubtask.isNotBlank()) {
                            subtasks.add(draftSubtask.trim() to "STEP")
                        }
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Clean Focus Screen with Live Session Recording:
 * - Play notes the current device time and feeds it to the timeline.
 * - Pause notes a break segment ("14:00-14:37") without creating a separate Break task.
 * - Resume starts a new segment ("14:47-...") and draws a dotted "Continued" line on the timeline!
 */
@Composable
fun AntiParalysisFocusScreen(
    focusItem: TaskWithSubtasks?,
    onBackToFlow: () -> Unit,
    onStartOrResumeSession: (TaskEntity) -> Unit,
    onPauseSession: (TaskEntity) -> Unit,
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
                modifier = Modifier.widthIn(max = 420.dp),
                backgroundColor = NeoMintGreen,
                cornerRadius = 22.dp,
                contentPadding = PaddingValues(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "All Done",
                        style = MaterialTheme.typography.displayMedium,
                        color = InkBlack
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    NeoPillButton(
                        text = "Timeline",
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
    var isRunning by remember(task.id) { mutableStateOf(task.activeSessionStartMin != null) }
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

    val recordedIntervals = remember(task.parsedSessionSegments, task.activeSessionStartMin) {
        buildList {
            task.parsedSessionSegments.forEachIndexed { idx, (s, e) ->
                val label = if (idx == 0) "Started" else "Resumed"
                add("$label  ${formatMinutesAsClock(s)} – ${formatMinutesAsClock(e)}")
            }
            task.activeSessionStartMin?.let { activeStart ->
                val label = if (task.parsedSessionSegments.isEmpty()) "Started" else "Resumed"
                add("$label  ${formatMinutesAsClock(activeStart)} – Active")
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Keep the single-column Focus card layout intact and centered within 680.dp across devices
        val isWideFocusLayout = false

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 12.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Back to Timeline + Folder/Section Badge
            Row(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeoPillButton(
                    text = "Timeline",
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    textFontWeight = FontWeight.Medium,
                    onClick = onBackToFlow
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(InkBlack)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${task.folderName.uppercase()} • ${task.sectionName.uppercase()}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = PureWhiteSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isWideFocusLayout) {
                // Redmi Pad Pro 5G Two-Column Focus Workspace
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Left Column: Task Hero + Timer + Complete CTA
                    Column(
                        modifier = Modifier.weight(1.05f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FocusHeroAndTimerCard(
                            task = task,
                            accent = accent,
                            mins = mins,
                            secs = secs,
                            progress = progress,
                            isRunning = isRunning,
                            onTogglePlayPause = {
                                if (isRunning) {
                                    isRunning = false
                                    onPauseSession(task)
                                } else {
                                    isRunning = true
                                    onStartOrResumeSession(task)
                                }
                            },
                            onResetTimer = { elapsedSeconds = 0L },
                            onCompleteAndAdvance = onCompleteAndAdvance
                        )
                    }

                    // Right Column: Subtasks Checklist + Live Session Break Log
                    Column(
                        modifier = Modifier.weight(0.95f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FocusSubtasksAndSessionCard(
                            focusItem = focusItem,
                            recordedIntervals = recordedIntervals,
                            newStepTitle = newStepTitle,
                            onNewStepTitleChange = { newStepTitle = it },
                            onToggleSubtask = onToggleSubtask,
                            onAddQuickSubtask = {
                                if (newStepTitle.isNotBlank()) {
                                    onAddQuickSubtask(task.id, newStepTitle)
                                    newStepTitle = ""
                                }
                            }
                        )
                    }
                }
            } else {
                // Single-Column Focus Workspace centered within max 680.dp
                Column(
                    modifier = Modifier
                        .widthIn(max = 680.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FocusHeroAndTimerCard(
                        task = task,
                        accent = accent,
                        mins = mins,
                        secs = secs,
                        progress = progress,
                        isRunning = isRunning,
                        onTogglePlayPause = {
                            if (isRunning) {
                                isRunning = false
                                onPauseSession(task)
                            } else {
                                isRunning = true
                                onStartOrResumeSession(task)
                            }
                        },
                        onResetTimer = { elapsedSeconds = 0L },
                        onCompleteAndAdvance = onCompleteAndAdvance
                    )

                    FocusSubtasksAndSessionCard(
                        focusItem = focusItem,
                        recordedIntervals = recordedIntervals,
                        newStepTitle = newStepTitle,
                        onNewStepTitleChange = { newStepTitle = it },
                        onToggleSubtask = onToggleSubtask,
                        onAddQuickSubtask = {
                            if (newStepTitle.isNotBlank()) {
                                onAddQuickSubtask(task.id, newStepTitle)
                                newStepTitle = ""
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusHeroAndTimerCard(
    task: TaskEntity,
    accent: androidx.compose.ui.graphics.Color,
    mins: Long,
    secs: Long,
    progress: Float,
    isRunning: Boolean,
    onTogglePlayPause: () -> Unit,
    onResetTimer: () -> Unit,
    onCompleteAndAdvance: () -> Unit
) {
    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = accent,
        cornerRadius = 22.dp,
        contentPadding = PaddingValues(22.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(62.sdp)
                    .clip(CircleShape)
                    .background(PureWhiteSurface)
                    .border(1.5.dp, InkBlack, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconForKey(task.iconKey),
                    contentDescription = null,
                    tint = InkBlack,
                    modifier = Modifier.size(28.sdp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = task.title,
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                color = InkBlack
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Symmetrical Timer Card
            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = PureWhiteSurface,
                cornerRadius = 20.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format(Locale.US, "%02d:%02d", mins, secs),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 46.ssp,
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum"
                        ),
                        color = InkBlack
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    NeoProgressBar(progress = progress, fillColor = InkBlack, height = 10.sdp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        NeoPillButton(
                            text = when {
                                isRunning -> "Pause (Break)"
                                task.parsedSessionSegments.isNotEmpty() -> "Resume"
                                else -> "Play Timer"
                            },
                            icon = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            selected = true,
                            activeColor = if (isRunning) NeoYellow else NeoMintGreen,
                            onClick = onTogglePlayPause,
                            modifier = Modifier.testTag("focus_play_pause_button")
                        )
                        NeoPillButton(
                            text = "Reset",
                            icon = Icons.Default.Refresh,
                            onClick = onResetTimer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.sdp)
                    .clip(RoundedCornerShape(16.sdp))
                    .background(NeoMintGreen)
                    .border(1.5.dp, InkBlack, RoundedCornerShape(16.sdp))
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
                        text = "Done • Next Task",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = InkBlack
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusSubtasksAndSessionCard(
    focusItem: TaskWithSubtasks,
    recordedIntervals: List<String>,
    newStepTitle: String,
    onNewStepTitleChange: (String) -> Unit,
    onToggleSubtask: (SubtaskEntity) -> Unit,
    onAddQuickSubtask: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Subtasks Card
        NeoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = PureWhiteSurface,
            cornerRadius = 22.dp,
            contentPadding = PaddingValues(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Subtasks (${focusItem.subtasks.count { it.isCompleted }}/${focusItem.subtasks.size})",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = InkBlack
                )

                focusItem.sortedSubtasks.forEach { sub ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SoftSheetSurface)
                            .clickable { onToggleSubtask(sub) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NeoSquareCheckbox(
                            checked = sub.isCompleted,
                            onCheckedChange = { onToggleSubtask(sub) },
                            accentColor = NeoMintGreen
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = sub.title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (sub.isCompleted) InkMuted else InkBlack
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
                        onValueChange = onNewStepTitleChange,
                        placeholder = {
                            Text(
                                "Add subtask",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                                color = InkMuted
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                    NeoPillButton(
                        text = "Add",
                        selected = true,
                        activeColor = NeoSkyBlue,
                        onClick = onAddQuickSubtask
                    )
                }
            }
        }

        // Live Session Activity Log Card (with visual dotted break line when paused/resumed)
        if (recordedIntervals.isNotEmpty()) {
            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = PureWhiteSurface,
                cornerRadius = 22.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Session Timeline",
                        style = MaterialTheme.typography.titleMedium,
                        color = InkBlack
                    )
                    recordedIntervals.forEachIndexed { idx, intervalText ->
                        if (idx > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 10.dp)
                            ) {
                                SpineConnectorSegment(
                                    isDottedBreak = true,
                                    height = 18.dp
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoftSheetSurface)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = intervalText,
                                style = MaterialTheme.typography.titleMedium,
                                color = InkBlack
                            )
                        }
                    }
                }
            }
        }
    }
}
