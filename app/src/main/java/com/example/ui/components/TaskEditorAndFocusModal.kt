package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.FolderEntity
import com.example.data.RecurrenceRule
import com.example.data.TaskEntity
import com.example.data.TaskMode
import com.example.data.TaskWithSubtasks
import com.example.data.formatOrdinalDayOfMonth
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class EditorSubPopup {
    NONE,
    DURATION_TIME,
    COLOR_ICON,
    FOLDER_SECTION,
    RECURRENCE,
    SUBTASKS,
    DELETE_CONFIRM
}

private enum class TimedSelectorTab {
    START,
    END,
    DURATION
}


@Composable
private fun NeoCompactMinutesRow(
    durationMinutes: Int,
    minAllowedDuration: Int,
    onDurationChange: (Int) -> Unit
) {
    var textValue by remember(durationMinutes) {
        mutableStateOf(durationMinutes.coerceAtLeast(minAllowedDuration).toString())
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PureWhiteSurface)
                .border(1.5.dp, InkBlack, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Minutes",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = InkMuted,
                maxLines = 1
            )
            BasicTextField(
                value = textValue,
                onValueChange = { raw ->
                    val filtered = raw.filter { it.isDigit() }.take(3)
                    textValue = filtered
                    val parsed = filtered.toIntOrNull()
                    if (parsed != null) {
                        onDurationChange(parsed.coerceIn(minAllowedDuration, 720))
                    } else if (filtered.isEmpty() && minAllowedDuration == 0) {
                        onDurationChange(0)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = InkBlack,
                    textAlign = TextAlign.End
                ),
                cursorBrush = SolidColor(InkBlack),
                modifier = Modifier
                    .width(64.dp)
                    .testTag("custom_minutes_input")
            )
        }

        NeoPillButton(
            text = "-5m",
            onClick = {
                val next = (durationMinutes - 5).coerceAtLeast(minAllowedDuration)
                onDurationChange(next)
                textValue = next.toString()
            }
        )
        NeoPillButton(
            text = "+5m",
            onClick = {
                val next = (durationMinutes + 5).coerceAtMost(720)
                onDurationChange(next)
                textValue = next.toString()
            }
        )
    }
}

@Composable
private fun NeoCompactClockSelector(
    clockMinutes: Int,
    onClockChange: (Int) -> Unit
) {
    val currentHour = (clockMinutes / 60).coerceIn(0, 23)
    val currentMinute = (clockMinutes % 60).coerceIn(0, 59)

    var hourText by remember(currentHour) {
        mutableStateOf(String.format(Locale.US, "%02d", currentHour))
    }
    var minText by remember(currentMinute) {
        mutableStateOf(String.format(Locale.US, "%02d", currentMinute))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(PureWhiteSurface)
                .border(1.5.dp, InkBlack, RoundedCornerShape(14.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(SoftSheetSurface, CircleShape)
                    .border(1.2.dp, InkBlack, CircleShape)
                    .clickable {
                        val prevHr = if (currentHour == 0) 23 else currentHour - 1
                        onClockChange(prevHr * 60 + currentMinute)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Previous Hour",
                    tint = InkBlack,
                    modifier = Modifier.size(15.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "HR",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = InkMuted,
                    maxLines = 1
                )
                BasicTextField(
                    value = hourText,
                    onValueChange = { raw ->
                        val filtered = raw.filter { it.isDigit() }.take(2)
                        hourText = filtered
                        val h = filtered.toIntOrNull()
                        if (h != null && h in 0..23) {
                            onClockChange(h * 60 + currentMinute)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = InkBlack,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(InkBlack),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(SoftSheetSurface, CircleShape)
                    .border(1.2.dp, InkBlack, CircleShape)
                    .clickable {
                        val nextHr = (currentHour + 1) % 24
                        onClockChange(nextHr * 60 + currentMinute)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Next Hour",
                    tint = InkBlack,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Text(
            text = ":",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = InkBlack
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(PureWhiteSurface)
                .border(1.5.dp, InkBlack, RoundedCornerShape(14.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(SoftSheetSurface, CircleShape)
                    .border(1.2.dp, InkBlack, CircleShape)
                    .clickable {
                        onClockChange((clockMinutes - 5).coerceAtLeast(0))
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease 5 Minutes",
                    tint = InkBlack,
                    modifier = Modifier.size(15.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "MIN",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = InkMuted,
                    maxLines = 1
                )
                BasicTextField(
                    value = minText,
                    onValueChange = { raw ->
                        val filtered = raw.filter { it.isDigit() }.take(2)
                        minText = filtered
                        val m = filtered.toIntOrNull()
                        if (m != null && m in 0..59) {
                            onClockChange(currentHour * 60 + m)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = InkBlack,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(InkBlack),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(SoftSheetSurface, CircleShape)
                    .border(1.2.dp, InkBlack, CircleShape)
                    .clickable {
                        onClockChange((clockMinutes + 5).coerceAtMost(23 * 60 + 59))
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase 5 Minutes",
                    tint = InkBlack,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UnifiedTimeAndDurationPickerSection(
    toInbox: Boolean,
    taskMode: TaskMode,
    startTimeMinutes: Int,
    durationMinutes: Int,
    showBubbleOption: Boolean = true,
    onModeChange: (TaskMode) -> Unit,
    onStartTimeChange: (Int) -> Unit,
    onDurationChange: (Int) -> Unit
) {
    val startOfDayMin = GlobalClockFormat.startOfDayMinutes
    val endOfDayMin = GlobalClockFormat.endOfDayMinutes
    val use24h = GlobalClockFormat.use24Hour

    var activeTimedTab by remember { mutableStateOf(TimedSelectorTab.START) }

    val timelessPresetValues = remember { setOf(0, 15, 30, 45, 60) }
    var showCustomTimelessInput by remember {
        mutableStateOf(durationMinutes > 0 && durationMinutes !in timelessPresetValues)
    }

    val effectiveTimedDuration = if (taskMode == TaskMode.TIMED_BLOCK) {
        if (durationMinutes <= 0) 60 else durationMinutes.coerceAtLeast(5)
    } else {
        durationMinutes.coerceAtLeast(0)
    }
    val calculatedEndMinutes = (startTimeMinutes + effectiveTimedDuration).coerceAtMost(23 * 60 + 59)

    val selectedClockMinutes = if (activeTimedTab == TimedSelectorTab.END) {
        calculatedEndMinutes
    } else {
        startTimeMinutes
    }

    val applyClockUpdate: (Int) -> Unit = { newClockMin ->
        val clampedClock = newClockMin.coerceIn(0, 23 * 60 + 59)
        if (activeTimedTab == TimedSelectorTab.END) {
            val newEnd = clampedClock.coerceAtLeast(startTimeMinutes + 5)
            val newDur = (newEnd - startTimeMinutes).coerceIn(5, 720)
            onDurationChange(newDur)
        } else {
            onStartTimeChange(clampedClock)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!toInbox) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NeoPillButton(
                    text = "Timeless",
                    selected = taskMode == TaskMode.TIMELESS_STEP,
                    activeColor = NeoYellow,
                    onClick = {
                        if (taskMode == TaskMode.TIMED_BLOCK) {
                            onDurationChange(0)
                            showCustomTimelessInput = false
                        }
                        onModeChange(TaskMode.TIMELESS_STEP)
                    },
                    modifier = Modifier.weight(1f)
                )
                NeoPillButton(
                    text = "Timed",
                    selected = taskMode == TaskMode.TIMED_BLOCK,
                    activeColor = NeoCoral,
                    onClick = {
                        if (durationMinutes <= 0) {
                            onDurationChange(60)
                        }
                        onModeChange(TaskMode.TIMED_BLOCK)
                    },
                    modifier = Modifier.weight(1f)
                )
                if (showBubbleOption) {
                    NeoPillButton(
                        text = "Bubble",
                        selected = taskMode == TaskMode.TOP_BUBBLE,
                        activeColor = NeoSkyBlue,
                        onClick = {
                            if (taskMode == TaskMode.TIMED_BLOCK) {
                                onDurationChange(0)
                                showCustomTimelessInput = false
                            }
                            onModeChange(TaskMode.TOP_BUBBLE)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (!toInbox && taskMode == TaskMode.TIMED_BLOCK) {
            val isBeforeStartOfDay = startTimeMinutes < startOfDayMin
            val isPastEndOfDay = (startTimeMinutes + effectiveTimedDuration) > endOfDayMin

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SoftSheetSurface)
                    .border(1.5.dp, InkBlack, RoundedCornerShape(18.dp))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val startSelected = activeTimedTab == TimedSelectorTab.START
                    val endSelected = activeTimedTab == TimedSelectorTab.END
                    val durSelected = activeTimedTab == TimedSelectorTab.DURATION

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (startSelected) NeoCoral else PureWhiteSurface)
                            .border(if (startSelected) 2.dp else 1.5.dp, InkBlack, RoundedCornerShape(12.dp))
                            .smoothPressClickable { activeTimedTab = TimedSelectorTab.START }
                            .padding(horizontal = 6.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "START",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (startSelected) PureWhiteSurface else InkMuted,
                            maxLines = 1
                        )
                        Text(
                            text = formatMinutesAsClock(startTimeMinutes, use24h),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (startSelected) PureWhiteSurface else InkBlack,
                            maxLines = 1
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (endSelected) NeoCoral else PureWhiteSurface)
                            .border(if (endSelected) 2.dp else 1.5.dp, InkBlack, RoundedCornerShape(12.dp))
                            .smoothPressClickable { activeTimedTab = TimedSelectorTab.END }
                            .padding(horizontal = 6.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "END",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (endSelected) PureWhiteSurface else InkMuted,
                            maxLines = 1
                        )
                        Text(
                            text = formatMinutesAsClock(calculatedEndMinutes, use24h),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (endSelected) PureWhiteSurface else InkBlack,
                            maxLines = 1
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (durSelected) NeoYellow else PureWhiteSurface)
                            .border(if (durSelected) 2.dp else 1.5.dp, InkBlack, RoundedCornerShape(12.dp))
                            .smoothPressClickable { activeTimedTab = TimedSelectorTab.DURATION }
                            .padding(horizontal = 6.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DURATION",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (durSelected) InkBlack else InkMuted,
                            maxLines = 1
                        )
                        Text(
                            text = formatDurationBadge(effectiveTimedDuration),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = InkBlack,
                            maxLines = 1
                        )
                    }
                }

                if (activeTimedTab == TimedSelectorTab.START || activeTimedTab == TimedSelectorTab.END) {
                    NeoCompactClockSelector(
                        clockMinutes = selectedClockMinutes,
                        onClockChange = applyClockUpdate
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            -15 to "-15m",
                            -5 to "-5m",
                            5 to "+5m",
                            15 to "+15m"
                        ).forEach { (delta, label) ->
                            NeoPillButton(
                                text = label,
                                onClick = { applyClockUpdate(selectedClockMinutes + delta) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    val timedPresets = remember {
                        listOf(
                            15 to "15m",
                            30 to "30m",
                            45 to "45m",
                            60 to "1h",
                            90 to "1.5h",
                            120 to "2h"
                        )
                    }
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        timedPresets.forEach { (mins, label) ->
                            NeoPillButton(
                                text = label,
                                selected = effectiveTimedDuration == mins,
                                activeColor = NeoYellow,
                                onClick = { onDurationChange(mins) }
                            )
                        }
                    }

                    NeoCompactMinutesRow(
                        durationMinutes = effectiveTimedDuration,
                        minAllowedDuration = 5,
                        onDurationChange = onDurationChange
                    )
                }

                if (isBeforeStartOfDay || isPastEndOfDay) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeoYellow)
                            .border(1.5.dp, InkBlack, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isPastEndOfDay) {
                                "Exceeds End of Day (${formatMinutesAsClock(endOfDayMin, use24h)})"
                            } else {
                                "Before Start of Day (${formatMinutesAsClock(startOfDayMin, use24h)})"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = InkBlack
                        )
                        NeoPillButton(
                            text = "Fit Within Day (${formatMinutesAsClock(startOfDayMin, use24h)}–${formatMinutesAsClock(endOfDayMin, use24h)})",
                            selected = true,
                            activeColor = PureWhiteSurface,
                            onClick = {
                                val safeStart = startTimeMinutes.coerceIn(startOfDayMin, (endOfDayMin - 15).coerceAtLeast(startOfDayMin))
                                val maxDur = (endOfDayMin - safeStart).coerceAtLeast(5)
                                onStartTimeChange(safeStart)
                                onDurationChange(effectiveTimedDuration.coerceAtMost(maxDur))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            val timelessPresets = remember {
                listOf(
                    0 to "None (0m)",
                    15 to "15m",
                    30 to "30m",
                    45 to "45m",
                    60 to "1h"
                )
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                timelessPresets.forEach { (mins, label) ->
                    val isSelected = !showCustomTimelessInput && durationMinutes == mins
                    NeoPillButton(
                        text = label,
                        selected = isSelected,
                        activeColor = NeoYellow,
                        onClick = {
                            showCustomTimelessInput = false
                            onDurationChange(mins)
                        }
                    )
                }

                NeoPillButton(
                    text = if (showCustomTimelessInput && durationMinutes > 0) {
                        "Custom (${formatDurationBadge(durationMinutes)})"
                    } else {
                        "Custom"
                    },
                    selected = showCustomTimelessInput,
                    activeColor = NeoCoral,
                    onClick = {
                        showCustomTimelessInput = true
                        if (durationMinutes == 0) {
                            onDurationChange(25)
                        }
                    }
                )
            }

            if (showCustomTimelessInput) {
                NeoCompactMinutesRow(
                    durationMinutes = durationMinutes,
                    minAllowedDuration = 0,
                    onDurationChange = onDurationChange
                )
            }
        }
    }
}

@Composable
fun StructuredTaskEditorDialog(
    selectedDate: LocalDate,
    folders: List<FolderEntity>,
    editingItem: TaskWithSubtasks?,
    defaultMode: TaskMode,
    defaultToInbox: Boolean,
    preselectedFolderName: String? = null,
    preselectedSectionName: String? = null,
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
        recurrenceIntervalDays: Int,
        subtasks: List<Pair<String, String>>,
        sortOrder: Int
    ) -> Unit,
    onMoveToInbox: ((TaskEntity) -> Unit)? = null,
    onDeleteTodayOnly: ((TaskEntity) -> Unit)? = null,
    onDelete: ((Long) -> Unit)? = null
) {
    val existingTask = editingItem?.task
    val matchedPreselectedFolder = remember(folders, preselectedFolderName) {
        preselectedFolderName?.let { target ->
            folders.firstOrNull { it.name.equals(target, ignoreCase = true) }
        }
    }
    var title by remember(existingTask) { mutableStateOf(existingTask?.title ?: "") }
    var notes by remember(existingTask) { mutableStateOf(existingTask?.notes ?: "") }
    var toInbox by remember(existingTask, defaultToInbox) {
        mutableStateOf(if (existingTask != null) existingTask.dateEpochDay == null else defaultToInbox)
    }
    var taskMode by remember(existingTask, defaultMode) {
        mutableStateOf(existingTask?.modeEnum ?: defaultMode)
    }
    var startTimeMinutes by remember(existingTask) {
        val defaultStart = (GlobalClockFormat.startOfDayMinutes + 60).coerceIn(0, 22 * 60)
        mutableIntStateOf(existingTask?.startTimeMinutes ?: defaultStart)
    }
    var durationMinutes by remember(existingTask, defaultMode) {
        val initialDur = when {
            existingTask != null -> existingTask.durationMinutes
            defaultMode == TaskMode.TIMED_BLOCK -> 60
            else -> 0
        }
        mutableIntStateOf(initialDur)
    }
    var colorKey by remember(existingTask, matchedPreselectedFolder) {
        mutableStateOf(existingTask?.colorKey ?: matchedPreselectedFolder?.colorKey ?: "SAGE")
    }
    var iconKey by remember(existingTask, matchedPreselectedFolder) {
        mutableStateOf(existingTask?.iconKey ?: matchedPreselectedFolder?.iconKey ?: "BOOK")
    }
    var folderName by remember(existingTask, folders, preselectedFolderName) {
        mutableStateOf(
            existingTask?.folderName
                ?: preselectedFolderName
                ?: (folders.firstOrNull()?.name ?: "Physics")
        )
    }
    var sectionName by remember(existingTask, folders, preselectedFolderName, preselectedSectionName) {
        val defaultSec = preselectedSectionName
            ?: folders.firstOrNull {
                it.name.equals(
                    existingTask?.folderName ?: preselectedFolderName ?: folders.firstOrNull()?.name,
                    ignoreCase = true
                )
            }?.sections?.firstOrNull()
            ?: "Core"
        mutableStateOf(existingTask?.sectionName ?: defaultSec)
    }
    var recurrence by remember(existingTask) {
        mutableStateOf(existingTask?.recurrenceEnum ?: RecurrenceRule.NONE)
    }
    var recurrenceDayOfMonth by remember(existingTask) {
        mutableIntStateOf(existingTask?.recurrenceDayOfMonth ?: selectedDate.dayOfMonth)
    }
    var recurrenceIntervalDays by remember(existingTask) {
        mutableIntStateOf(existingTask?.recurrenceIntervalDays ?: 2)
    }
    var stepOrder by remember(existingTask) {
        mutableIntStateOf(existingTask?.let { effectiveTaskStepNumber(it) } ?: 1)
    }

    val subtasksList = remember(editingItem) {
        mutableStateListOf<Pair<String, String>>().apply {
            editingItem?.sortedSubtasks?.forEach { add(it.title to it.stageTag) }
        }
    }

    var activeSubPopup by remember { mutableStateOf(EditorSubPopup.NONE) }
    var endOfDayErrorText by remember { mutableStateOf<String?>(null) }

    val headerColor = neoColorForKey(colorKey)
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE dd. MMM yyyy", Locale.ENGLISH) }

    Dialog(
        onDismissRequest = {
            if (activeSubPopup != EditorSubPopup.NONE) {
                activeSubPopup = EditorSubPopup.NONE
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        BackHandler(enabled = activeSubPopup != EditorSubPopup.NONE) {
            activeSubPopup = EditorSubPopup.NONE
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            NeoCard(
                modifier = Modifier
                    .widthIn(max = 460.dp)
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {},
                backgroundColor = PaperCanvas,
                cornerRadius = 28.dp,
                contentPadding = PaddingValues(0.dp)
            ) {
                AnimatedContent(
                    targetState = activeSubPopup,
                    transitionSpec = {
                        if (targetState != EditorSubPopup.NONE) {
                            (slideInHorizontally { it / 3 } + fadeIn()) togetherWith
                                (slideOutHorizontally { -it / 3 } + fadeOut())
                        } else {
                            (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith
                                (slideOutHorizontally { it / 3 } + fadeOut())
                        }
                    },
                    label = "single_window_editor_transition"
                ) { subScreen ->
                    when (subScreen) {
                        EditorSubPopup.NONE -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
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
                                                .minimumInteractiveComponentSize()
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(PureWhiteSurface.copy(alpha = 0.9f), CircleShape)
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

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (existingTask != null && existingTask.dateEpochDay != null && onMoveToInbox != null) {
                                                NeoPillButton(
                                                    text = "To Inbox",
                                                    icon = Icons.Default.Inbox,
                                                    selected = false,
                                                    onClick = { onMoveToInbox(existingTask) }
                                                )
                                            }
                                            if (existingTask != null && onDelete != null) {
                                                IconButton(
                                                    onClick = {
                                                        if (existingTask.recurrenceEnum != RecurrenceRule.NONE && onDeleteTodayOnly != null) {
                                                            activeSubPopup = EditorSubPopup.DELETE_CONFIRM
                                                        } else {
                                                            onDelete(existingTask.id)
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .minimumInteractiveComponentSize()
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(NeoCoral, CircleShape)
                                                        .border(1.5.dp, InkBlack, CircleShape)
                                                        .testTag("delete_task_button")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete Task",
                                                        tint = PureWhiteSurface,
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
                                        Box(
                                            modifier = Modifier
                                                .size(62.dp)
                                                .clip(CircleShape)
                                                .background(PureWhiteSurface, CircleShape)
                                                .border(2.dp, InkBlack, CircleShape)
                                                .smoothPressClickable { activeSubPopup = EditorSubPopup.COLOR_ICON },
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
                                                    toInbox -> "Inbox • ${formatDurationBadge(durationMinutes)}"
                                                    taskMode == TaskMode.TIMED_BLOCK -> {
                                                        val safeDur = durationMinutes.coerceAtLeast(5)
                                                        "${formatMinutesAsClock(startTimeMinutes)}–${formatMinutesAsClock(startTimeMinutes + safeDur)} (${formatDurationBadge(safeDur)})"
                                                    }
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

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
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

                                    EditorSummaryRow(
                                        icon = Icons.Default.Schedule,
                                        label = "Time & Duration",
                                        valueText = when {
                                            toInbox -> formatDurationBadge(durationMinutes)
                                            taskMode == TaskMode.TIMED_BLOCK -> {
                                                val safeDur = durationMinutes.coerceAtLeast(5)
                                                "${formatMinutesAsClock(startTimeMinutes)}–${formatMinutesAsClock(startTimeMinutes + safeDur)} • ${formatDurationBadge(safeDur)}"
                                            }
                                            taskMode == TaskMode.TOP_BUBBLE ->
                                                "Top Bubble • ${formatDurationBadge(durationMinutes)}"
                                            else ->
                                                "Timeless (#$stepOrder) • ${formatDurationBadge(durationMinutes)}"
                                        },
                                        onClick = {
                                            endOfDayErrorText = null
                                            activeSubPopup = EditorSubPopup.DURATION_TIME
                                        }
                                    )

                                    EditorSummaryRow(
                                        icon = Icons.Default.Palette,
                                        label = "Color & Icon",
                                        valueText = NeoPalette.firstOrNull { it.key == colorKey }?.label ?: "Color",
                                        onClick = { activeSubPopup = EditorSubPopup.COLOR_ICON }
                                    )

                                    EditorSummaryRow(
                                        icon = Icons.Default.Folder,
                                        label = "Folder",
                                        valueText = "$folderName • $sectionName",
                                        onClick = { activeSubPopup = EditorSubPopup.FOLDER_SECTION }
                                    )

                                    EditorSummaryRow(
                                        icon = Icons.Default.Repeat,
                                        label = "Repeat",
                                        valueText = when (recurrence) {
                                            RecurrenceRule.MONTHLY -> "Monthly (${formatOrdinalDayOfMonth(recurrenceDayOfMonth)})"
                                            RecurrenceRule.CUSTOM_DAYS -> "Every ${recurrenceIntervalDays.coerceAtLeast(1)} days"
                                            else -> recurrence.label
                                        },
                                        onClick = { activeSubPopup = EditorSubPopup.RECURRENCE }
                                    )

                                    EditorSummaryRow(
                                        icon = Icons.Default.Checklist,
                                        label = "Subtasks",
                                        valueText = if (subtasksList.isEmpty()) "None" else "${subtasksList.size} steps",
                                        onClick = { activeSubPopup = EditorSubPopup.SUBTASKS }
                                    )

                                    endOfDayErrorText?.let { errMsg ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(NeoYellow)
                                                .border(1.5.dp, InkBlack, RoundedCornerShape(12.dp))
                                                .padding(10.dp)
                                        ) {
                                            Text(
                                                text = errMsg,
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                                color = InkBlack
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(NeoCoral)
                                            .border(2.dp, InkBlack, RoundedCornerShape(50))
                                            .clickable {
                                                val finalDur = if (taskMode == TaskMode.TIMED_BLOCK) {
                                                    durationMinutes.coerceIn(5, 720)
                                                } else {
                                                    durationMinutes.coerceIn(0, 720)
                                                }
                                                if (!toInbox && taskMode == TaskMode.TIMED_BLOCK) {
                                                    val endMin = startTimeMinutes + finalDur
                                                    if (endMin > GlobalClockFormat.endOfDayMinutes) {
                                                        endOfDayErrorText =
                                                            "Task ends at ${formatMinutesAsClock(endMin)}, past End of Day (${formatMinutesAsClock(GlobalClockFormat.endOfDayMinutes)}). Adjust time or End-of-Day in Settings."
                                                        return@clickable
                                                    }
                                                }
                                                val finalTitle = title.ifBlank { "Task" }
                                                onSave(
                                                    existingTask?.id ?: 0L,
                                                    finalTitle,
                                                    notes,
                                                    toInbox,
                                                    taskMode,
                                                    if (taskMode == TaskMode.TIMED_BLOCK) startTimeMinutes else null,
                                                    finalDur,
                                                    colorKey,
                                                    iconKey,
                                                    folderName,
                                                    sectionName,
                                                    recurrence,
                                                    recurrenceDayOfMonth,
                                                    recurrenceIntervalDays,
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

                        EditorSubPopup.DURATION_TIME -> {
                            InPlaceSubScreenContainer(
                                title = "Time & Duration",
                                onBack = { activeSubPopup = EditorSubPopup.NONE }
                            ) {
                                UnifiedTimeAndDurationPickerSection(
                                    toInbox = toInbox,
                                    taskMode = taskMode,
                                    startTimeMinutes = startTimeMinutes,
                                    durationMinutes = durationMinutes,
                                    showBubbleOption = true,
                                    onModeChange = { taskMode = it },
                                    onStartTimeChange = { startTimeMinutes = it },
                                    onDurationChange = { durationMinutes = it }
                                )
                            }
                        }

                        EditorSubPopup.COLOR_ICON -> {
                            InPlaceSubScreenContainer(
                                title = "Color & Icon",
                                onBack = { activeSubPopup = EditorSubPopup.NONE }
                            ) {
                                ColorAndIconInPlaceContent(
                                    selectedColorKey = colorKey,
                                    selectedIconKey = iconKey,
                                    onSelectColor = { colorKey = it },
                                    onSelectIcon = { iconKey = it }
                                )
                            }
                        }

                        EditorSubPopup.FOLDER_SECTION -> {
                            InPlaceSubScreenContainer(
                                title = "Folder & Section",
                                onBack = { activeSubPopup = EditorSubPopup.NONE }
                            ) {
                                FolderAndSectionInPlaceContent(
                                    folders = folders,
                                    selectedFolder = folderName,
                                    selectedSection = sectionName,
                                    onSelectFolderAndSection = { f, s ->
                                        folderName = f
                                        sectionName = s
                                    }
                                )
                            }
                        }

                        EditorSubPopup.RECURRENCE -> {
                            InPlaceSubScreenContainer(
                                title = "Repeat",
                                onBack = { activeSubPopup = EditorSubPopup.NONE }
                            ) {
                                RecurrenceInPlaceContent(
                                    selectedRule = recurrence,
                                    dayOfMonth = recurrenceDayOfMonth,
                                    intervalDays = recurrenceIntervalDays,
                                    onSelectRule = { recurrence = it },
                                    onSelectDayOfMonth = { recurrenceDayOfMonth = it },
                                    onSelectIntervalDays = { recurrenceIntervalDays = it }
                                )
                            }
                        }

                        EditorSubPopup.SUBTASKS -> {
                            var draftSubtask by remember { mutableStateOf("") }
                            InPlaceSubScreenContainer(
                                title = "Subtasks & Notes",
                                onBack = {
                                    if (draftSubtask.isNotBlank()) {
                                        subtasksList.add(draftSubtask.trim() to "STEP")
                                    }
                                    activeSubPopup = EditorSubPopup.NONE
                                }
                            ) {
                                SubtasksInPlaceContent(
                                    subtasks = subtasksList,
                                    notes = notes,
                                    draftSubtask = draftSubtask,
                                    onDraftSubtaskChange = { draftSubtask = it },
                                    onNotesChange = { notes = it }
                                )
                            }
                        }

                        EditorSubPopup.DELETE_CONFIRM -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PureWhiteSurface)
                                    .padding(22.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = "Delete Repeating Task?",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = InkBlack
                                )
                                Text(
                                    text = "Would you like to remove \"${existingTask?.title.orEmpty()}\" from today only, or delete all future occurrences?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = InkCharcoal
                                )
                                NeoPillButton(
                                    text = "Delete Today Only",
                                    selected = true,
                                    activeColor = NeoYellow,
                                    onClick = {
                                        if (existingTask != null && onDeleteTodayOnly != null) {
                                            onDeleteTodayOnly(existingTask)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                NeoPillButton(
                                    text = "Delete All Future Tasks",
                                    icon = Icons.Default.Delete,
                                    selected = true,
                                    activeColor = NeoCoral,
                                    onClick = {
                                        if (existingTask != null && onDelete != null) {
                                            onDelete(existingTask.id)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                NeoPillButton(
                                    text = "Cancel",
                                    selected = false,
                                    onClick = { activeSubPopup = EditorSubPopup.NONE },
                                    modifier = Modifier.fillMaxWidth()
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
private fun InPlaceSubScreenContainer(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PureWhiteSurface)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeoPillButton(
                text = "Back",
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                onClick = onBack
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = InkBlack
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        content()

        NeoPillButton(
            text = "Done",
            selected = true,
            activeColor = NeoMintGreen,
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        )
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
                color = InkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorAndIconInPlaceContent(
    selectedColorKey: String,
    selectedIconKey: String,
    onSelectColor: (String) -> Unit,
    onSelectIcon: (String) -> Unit
) {
    val activeColor = neoColorForKey(selectedColorKey)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NeoPalette.forEach { option ->
                val selected = selectedColorKey == option.key
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(option.fill, CircleShape)
                        .border(if (selected) 2.5.dp else 1.dp, InkBlack, CircleShape)
                        .smoothPressClickable { onSelectColor(option.key) },
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
                                .smoothPressClickable { onSelectIcon(item.key) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) activeColor else SoftSheetSurface, CircleShape)
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
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FolderAndSectionInPlaceContent(
    folders: List<FolderEntity>,
    selectedFolder: String,
    selectedSection: String,
    onSelectFolderAndSection: (String, String) -> Unit
) {
    val activeFolderObj = folders.firstOrNull { it.name.equals(selectedFolder, ignoreCase = true) }
        ?: folders.firstOrNull()
    val sections = activeFolderObj?.sections?.ifEmpty { listOf("Core") } ?: listOf("Core")

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecurrenceInPlaceContent(
    selectedRule: RecurrenceRule,
    dayOfMonth: Int,
    intervalDays: Int,
    onSelectRule: (RecurrenceRule) -> Unit,
    onSelectDayOfMonth: (Int) -> Unit,
    onSelectIntervalDays: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RecurrenceRule.entries.forEach { rule ->
            val selected = selectedRule == rule
            val displayLabel = when (rule) {
                RecurrenceRule.MONTHLY -> "Monthly (${formatOrdinalDayOfMonth(dayOfMonth)})"
                RecurrenceRule.CUSTOM_DAYS -> "Every ${intervalDays.coerceAtLeast(1)} days"
                else -> rule.label
            }
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
                    text = displayLabel,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SoftSheetSurface)
                    .border(1.5.dp, InkBlack, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatOrdinalDayOfMonth(dayOfMonth),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
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
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = InkBlack
                    )
                    NeoPillButton(
                        text = "+",
                        onClick = { onSelectDayOfMonth((dayOfMonth + 1).coerceAtMost(31)) }
                    )
                }
            }
        }

        if (selectedRule == RecurrenceRule.CUSTOM_DAYS) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SoftSheetSurface)
                    .border(1.5.dp, InkBlack, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Repeat Interval",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = InkBlack
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NeoPillButton(
                            text = "-",
                            onClick = { onSelectIntervalDays((intervalDays - 1).coerceAtLeast(2)) }
                        )
                        Text(
                            text = "Every $intervalDays days",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = InkBlack
                        )
                        NeoPillButton(
                            text = "+",
                            onClick = { onSelectIntervalDays((intervalDays + 1).coerceAtMost(90)) }
                        )
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(2, 3, 4, 5, 10, 14, 30).forEach { days ->
                        NeoPillButton(
                            text = "Every ${days}d",
                            selected = intervalDays == days,
                            activeColor = NeoYellow,
                            onClick = { onSelectIntervalDays(days) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubtasksInPlaceContent(
    subtasks: MutableList<Pair<String, String>>,
    notes: String,
    draftSubtask: String,
    onDraftSubtaskChange: (String) -> Unit,
    onNotesChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        .minimumInteractiveComponentSize()
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
                onValueChange = onDraftSubtaskChange,
                placeholder = { Text("Subtask") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    if (draftSubtask.isNotBlank()) {
                        subtasks.add(draftSubtask.trim() to "STEP")
                        onDraftSubtaskChange("")
                    }
                },
                modifier = Modifier
                    .minimumInteractiveComponentSize()
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
    }
}

