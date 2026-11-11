package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.RecurrenceRule
import com.example.data.SubtaskEntity
import com.example.data.TaskEntity
import com.example.data.TaskMode
import com.example.data.TaskWithSubtasks
import com.example.ui.FlowFilterMode
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.NeoCoral
import com.example.ui.theme.NeoMintGreen
import com.example.ui.theme.NeoYellow
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.SoftSheetSurface
import com.example.ui.theme.neoColorForKey
import com.example.ui.theme.neoSoftColorForKey
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

fun formatMinutesAsClock(minutes: Int): String {
    val hrs24 = (minutes / 60).coerceIn(0, 23)
    val mins = (minutes % 60).coerceIn(0, 59)
    return String.format(Locale.US, "%02d:%02d", hrs24, mins)
}

fun formatDurationBadge(minutes: Int): String {
    return when {
        minutes >= 60 && minutes % 60 == 0 -> "${minutes / 60}h"
        minutes > 60 -> "${minutes / 60}h ${minutes % 60}m"
        else -> "${minutes}m"
    }
}

/**
 * Clean Month-Only Header & 7-Day Week Strip (Removed "FLOW OS", removed day/year clutter,
 * removed Reset button, and added a 1-tap Rotate/Split-View button for easy tablet previewing).
 */
@Composable
fun DateAndWeekHeader(
    selectedDate: LocalDate,
    weekDotColorsByEpochDay: Map<Long, List<String>>,
    onSelectDate: (Long) -> Unit,
    onShiftWeek: (Long) -> Unit,
    onJumpToToday: () -> Unit,
    onToggleOrientationMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthName = selectedDate.month.getDisplayName(JavaTextStyle.FULL, Locale.ENGLISH)
    val isToday = selectedDate == LocalDate.now()
    val startOfWeek = selectedDate.minusDays((selectedDate.dayOfWeek.value - 1).toLong())

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Only the Month name per user feedback
            Text(
                text = monthName,
                style = MaterialTheme.typography.displayMedium,
                color = InkBlack,
                maxLines = 1
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!isToday) {
                    NeoPillButton(
                        text = "Today",
                        onClick = onJumpToToday,
                        selected = true,
                        activeColor = NeoYellow,
                        modifier = Modifier.testTag("jump_today_button")
                    )
                }
                IconButton(
                    onClick = { onShiftWeek(-1L) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PureWhiteSurface)
                        .border(1.5.dp, InkBlack, CircleShape)
                        .testTag("prev_week_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Week",
                        tint = InkBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { onShiftWeek(1L) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PureWhiteSurface)
                        .border(1.5.dp, InkBlack, CircleShape)
                        .testTag("next_week_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Week",
                        tint = InkBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onToggleOrientationMode,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PureWhiteSurface)
                        .border(1.5.dp, InkBlack, CircleShape)
                        .testTag("rotate_layout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = "Rotate Tablet / Handheld View",
                        tint = InkBlack,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 7-Day Week Strip (Matches Structured Screenshots 5 & 6)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (offset in 0L..6L) {
                val day = startOfWeek.plusDays(offset)
                val epoch = day.toEpochDay()
                val isSelected = day == selectedDate
                val isCurrentToday = day == LocalDate.now()
                val dayLabel = day.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, Locale.ENGLISH)
                val dotColors = weekDotColorsByEpochDay[epoch].orEmpty()

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(role = Role.Tab) { onSelectDate(epoch) }
                        .padding(vertical = 4.dp)
                        .testTag("week_day_${day.dayOfWeek.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) InkBlack else InkMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> NeoCoral
                                    isCurrentToday -> NeoYellow.copy(alpha = 0.5f)
                                    else -> Color.Transparent
                                }
                            )
                            .then(
                                if (isSelected) Modifier.border(1.8.dp, InkBlack, CircleShape)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = InkBlack
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(6.dp)
                    ) {
                        dotColors.take(4).forEach { colorKey ->
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(neoColorForKey(colorKey))
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Clean Top Circular Routine Bubbles (No morphing drop shadow on small bubbles, and no "+ Add Bubble" button).
 */
@Composable
fun TopAnytimeBubblesRow(
    bubbles: List<TaskWithSubtasks>,
    onBubbleClick: (TaskWithSubtasks) -> Unit,
    modifier: Modifier = Modifier
) {
    if (bubbles.isEmpty()) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.Top
    ) {
        bubbles.forEach { item ->
            val task = item.task
            val color = neoColorForKey(task.colorKey)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(74.dp)
                    .clickable(role = Role.Button) { onBubbleClick(item) }
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (task.isCompleted) SoftSheetSurface else color)
                        .border(1.8.dp, InkBlack, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.Check else iconForKey(task.iconKey),
                        contentDescription = task.title,
                        tint = InkBlack,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (task.isCompleted) InkMuted else InkBlack,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Minimal Filter Bar (All / Timeless / Timed).
 */
@Composable
fun FlowStructureFilterBar(
    currentFilter: FlowFilterMode,
    onSelectFilter: (FlowFilterMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FlowFilterMode.entries.forEach { mode ->
            NeoPillButton(
                text = mode.label,
                selected = currentFilter == mode,
                activeColor = NeoYellow,
                onClick = { onSelectFilter(mode) }
            )
        }
    }
}

/**
 * Clean Structured-style Free-Time Gap Indicator ("30m of free time?").
 */
@Composable
fun FreeTimeGapIndicator(
    gapMinutes: Int,
    onFillGapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(46.dp))
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
            ) {
                drawLine(
                    color = InkMuted.copy(alpha = 0.45f),
                    start = Offset(size.width / 2f, 0f),
                    end = Offset(size.width / 2f, size.height),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable(onClick = onFillGapClick)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = NeoCoral,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${formatDurationBadge(gapMinutes)} of free time?",
                style = MaterialTheme.typography.labelMedium.copy(color = NeoCoral)
            )
        }
    }
}

/**
 * Clean Structured × Neo-Brutalist Timeline Row:
 * - Left: Time or Step #
 * - Center: Capsule Icon with crisp offset shadow (kept specifically per user feedback!)
 * - Right: Minimal subtitle + Title + tiny subtask badge + Circle Checkbox.
 * - Includes Dotted "Continued" connector when a task timer was paused and resumed!
 * - Tapping the row opens the clean Task & Subtasks Detail Modal.
 */
@Composable
fun HybridSpineTaskRow(
    item: TaskWithSubtasks,
    sequenceNumber: Int,
    isUpNext: Boolean,
    onToggleComplete: () -> Unit,
    onOpenTaskDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val task = item.task
    val accentColor = neoColorForKey(task.colorKey)
    val isTimed = task.modeEnum == TaskMode.TIMED_BLOCK || task.startTimeMinutes != null
    val startMin = task.startTimeMinutes ?: 0
    val endMin = (startMin + task.durationMinutes).coerceAtMost(23 * 60 + 59)
    val segments = task.parsedSessionSegments

    val capsuleHeight = when {
        task.durationMinutes >= 60 -> 82.dp
        task.durationMinutes >= 45 -> 68.dp
        task.durationMinutes >= 30 -> 58.dp
        else -> 48.dp
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clickable(onClick = onOpenTaskDetail)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Left Column: Start/End Clock OR Step #
            Column(
                modifier = Modifier.width(46.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (isTimed) {
                    Text(
                        text = formatMinutesAsClock(startMin),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isUpNext) FontWeight.Bold else FontWeight.Medium,
                            color = if (isUpNext) InkBlack else InkMuted
                        )
                    )
                    Spacer(modifier = Modifier.height((capsuleHeight - 28.dp).coerceAtLeast(8.dp)))
                    Text(
                        text = formatMinutesAsClock(endMin),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                        color = InkMuted
                    )
                } else {
                    Text(
                        text = "#$sequenceNumber",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isUpNext) InkBlack else InkMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // 2. Center Spine: Vertical Line + Capsule Icon WITH Crisp Shadow
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(InkBlack.copy(alpha = 0.2f))
                )

                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .height(capsuleHeight)
                        .drawBehind {
                            drawRoundRect(
                                color = InkBlack,
                                topLeft = Offset(2.5.dp.toPx(), 2.5.dp.toPx()),
                                size = size,
                                cornerRadius = CornerRadius(23.dp.toPx(), 23.dp.toPx())
                            )
                        }
                        .clip(RoundedCornerShape(23.dp))
                        .background(if (task.isCompleted) SoftSheetSurface else accentColor)
                        .border(1.8.dp, InkBlack, RoundedCornerShape(23.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconForKey(task.iconKey),
                        contentDescription = task.title,
                        tint = InkBlack,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // 3. Right Content: Clean Structured-style Title + Minimal Subtitle + Circle Checkbox
            Row(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (isUpNext && !task.isCompleted) {
                            Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(neoSoftColorForKey(task.colorKey))
                                .border(1.5.dp, InkBlack, RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        } else {
                            Modifier.padding(vertical = 6.dp)
                        }
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = when {
                                task.activeSessionStartMin != null ->
                                    "Running since ${formatMinutesAsClock(task.activeSessionStartMin)}"
                                isTimed ->
                                    "${formatMinutesAsClock(startMin)}–${formatMinutesAsClock(endMin)} (${formatDurationBadge(task.durationMinutes)})"
                                isUpNext && !task.isCompleted ->
                                    "Up Next • ${formatDurationBadge(task.durationMinutes)}"
                                else ->
                                    formatDurationBadge(task.durationMinutes)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = InkMuted
                        )
                        if (task.recurrenceEnum != RecurrenceRule.NONE) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = task.recurrenceEnum.label,
                                tint = InkMuted,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (task.isCompleted) InkMuted else InkBlack,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (item.subtasks.isNotEmpty()) {
                        val done = item.subtasks.count { it.isCompleted }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = null,
                                tint = InkMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "$done/${item.subtasks.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = InkMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                NeoCircleCheckbox(
                    checked = task.isCompleted,
                    accentColor = accentColor,
                    onCheckedChange = onToggleComplete,
                    modifier = Modifier.testTag("toggle_task_${task.id}")
                )
            }
        }

        // 4. Dotted "Continued" Session Line when a task was paused and resumed!
        if (segments.size > 1 || (segments.size == 1 && task.activeSessionStartMin != null)) {
            val allPoints = buildList {
                segments.forEach { add(it.first to it.second) }
                task.activeSessionStartMin?.let { add(it to -1) }
            }
            for (i in 1 until allPoints.size) {
                val prevEnd = allPoints[i - 1].second
                val resumedAt = allPoints[i].first
                val resumedEnd = allPoints[i].second
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenTaskDetail)
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatMinutesAsClock(resumedAt),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                        color = InkMuted,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(46.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier
                                .width(2.dp)
                                .fillMaxHeight()
                        ) {
                            drawLine(
                                color = InkBlack,
                                start = Offset(size.width / 2f, 0f),
                                end = Offset(size.width / 2f, size.height),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = if (resumedEnd > 0) {
                            "Continued ${formatMinutesAsClock(resumedAt)}–${formatMinutesAsClock(resumedEnd)} (paused at ${formatMinutesAsClock(prevEnd)})"
                        } else {
                            "Continued at ${formatMinutesAsClock(resumedAt)} (paused at ${formatMinutesAsClock(prevEnd)})"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(color = InkCharcoal)
                    )
                }
            }
        }
    }
}

/**
 * Dedicated Task & Subtasks Detail Window when a task is clicked on the timeline.
 * Keeps subtasks, session segments, and notes in a clean popup rather than cramming the timeline!
 */
@Composable
fun TaskDetailPopupModal(
    item: TaskWithSubtasks,
    onDismiss: () -> Unit,
    onToggleComplete: () -> Unit,
    onToggleSubtask: (SubtaskEntity) -> Unit,
    onAddSubtask: (String) -> Unit,
    onStartFocus: () -> Unit,
    onEditTask: () -> Unit
) {
    val task = item.task
    val accent = neoColorForKey(task.colorKey)
    var newSubtask by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        NeoCard(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth(),
            backgroundColor = PureWhiteSurface,
            cornerRadius = 22.dp,
            shadowOffset = 4.dp,
            contentPadding = PaddingValues(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(accent)
                                .border(1.8.dp, InkBlack, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconForKey(task.iconKey),
                                contentDescription = null,
                                tint = InkBlack,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "${task.folderName} • ${task.sectionName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = InkMuted
                            )
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = InkBlack
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onEditTask,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SoftSheetSurface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Task",
                                tint = InkBlack,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SoftSheetSurface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = InkBlack,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                if (task.notes.isNotBlank()) {
                    Text(
                        text = task.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkCharcoal
                    )
                }

                // Recorded Session Segments (if any)
                if (task.parsedSessionSegments.isNotEmpty() || task.activeSessionStartMin != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SoftSheetSurface)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Recorded Timeline Sessions",
                            style = MaterialTheme.typography.labelLarge,
                            color = InkBlack
                        )
                        task.parsedSessionSegments.forEachIndexed { idx, (s, e) ->
                            val prefix = if (idx == 0) "Started" else "Continued"
                            Text(
                                text = "$prefix: ${formatMinutesAsClock(s)} – ${formatMinutesAsClock(e)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = InkCharcoal
                            )
                        }
                        task.activeSessionStartMin?.let { activeStart ->
                            Text(
                                text = "Active now since ${formatMinutesAsClock(activeStart)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = NeoCoral
                            )
                        }
                    }
                }

                // Subtasks List inside Popup
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Subtasks (${item.subtasks.count { it.isCompleted }}/${item.subtasks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = InkBlack
                    )
                    item.sortedSubtasks.forEach { sub ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleSubtask(sub) }
                                .padding(vertical = 2.dp),
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
                                    accentColor = accent
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = sub.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    color = if (sub.isCompleted) InkMuted else InkBlack
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newSubtask,
                            onValueChange = { newSubtask = it },
                            placeholder = { Text("Add subtask...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        NeoPillButton(
                            text = "Add",
                            selected = true,
                            activeColor = accent,
                            onClick = {
                                if (newSubtask.isNotBlank()) {
                                    onAddSubtask(newSubtask)
                                    newSubtask = ""
                                }
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeoPillButton(
                        text = "Focus Timer",
                        icon = Icons.Default.PlayArrow,
                        selected = true,
                        activeColor = NeoYellow,
                        onClick = onStartFocus,
                        modifier = Modifier.weight(1f)
                    )
                    NeoPillButton(
                        text = if (task.isCompleted) "Completed" else "Mark Done",
                        icon = Icons.Default.Check,
                        selected = true,
                        activeColor = NeoMintGreen,
                        onClick = {
                            onToggleComplete()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
