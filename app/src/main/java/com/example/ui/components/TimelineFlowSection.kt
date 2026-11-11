package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.RecurrenceRule
import com.example.data.SubtaskEntity
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
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.time.temporal.ChronoUnit
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

private const val WEEK_PAGER_CENTER = 500
private const val WEEK_PAGER_COUNT = 1000

/**
 * Section 2 Ultra-Clean Header & Swipeable Week Calendar:
 * - Shows ONLY the Month name ("November") in the Outfit font.
 * - Removed < > arrows, Today button, and Rotate button so the header never overlaps or shifts down.
 * - Horizontally swipeable 7-day week strip (slide left/right to view any past or future week).
 * - Today is highlighted with NeoYellow; the selected day is highlighted with NeoCoral.
 */
@Composable
fun DateAndWeekHeader(
    selectedDate: LocalDate,
    weekDotColorsByEpochDay: Map<Long, List<String>>,
    onSelectDate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val baseMonday = remember { today.minusDays((today.dayOfWeek.value - 1).toLong()) }
    val selectedMonday = selectedDate.minusDays((selectedDate.dayOfWeek.value - 1).toLong())
    val targetPage = remember(selectedMonday) {
        val weeksDiff = ChronoUnit.WEEKS.between(baseMonday, selectedMonday).toInt()
        (WEEK_PAGER_CENTER + weeksDiff).coerceIn(0, WEEK_PAGER_COUNT - 1)
    }

    val pagerState = rememberPagerState(
        initialPage = targetPage,
        pageCount = { WEEK_PAGER_COUNT }
    )

    // When user swipes to a new week page, move selectedDate to the same weekday in that week
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val weekOffset = (page - WEEK_PAGER_CENTER).toLong()
                val currentDayOfWeekOffset = (selectedDate.dayOfWeek.value - 1).toLong()
                val newDate = baseMonday.plusWeeks(weekOffset).plusDays(currentDayOfWeekOffset)
                if (newDate != selectedDate) {
                    onSelectDate(newDate.toEpochDay())
                }
            }
    }

    val monthName = selectedDate.month.getDisplayName(JavaTextStyle.FULL, Locale.ENGLISH)

    Column(modifier = modifier.fillMaxWidth()) {
        // Month-only title in clean Outfit font
        Text(
            text = monthName,
            style = MaterialTheme.typography.displayMedium,
            color = InkBlack,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontally Swipeable 7-Day Week Strip
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("swipeable_week_pager")
        ) { page ->
            val weekOffset = (page - WEEK_PAGER_CENTER).toLong()
            val startOfWeek = baseMonday.plusWeeks(weekOffset)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (offset in 0L..6L) {
                    val day = startOfWeek.plusDays(offset)
                    val epoch = day.toEpochDay()
                    val isSelected = day == selectedDate
                    val isCurrentToday = day == today
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
                            color = if (isSelected || isCurrentToday) InkBlack else InkMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> NeoCoral
                                        isCurrentToday -> NeoYellow
                                        else -> Color.Transparent
                                    }
                                )
                                .then(
                                    if (isSelected || isCurrentToday) {
                                        Modifier.border(1.5.dp, InkBlack, CircleShape)
                                    } else {
                                        Modifier
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.dayOfMonth.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSelected) PureWhiteSurface else InkBlack
                            )
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
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
}

/**
 * Clean Top Circular Routine Bubbles.
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
                        .border(1.5.dp, InkBlack, CircleShape),
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
 * Spine Connector Line between consecutive tasks:
 * - Solid vertical line when tasks flow continuously back-to-back.
 * - Dotted vertical break line when there is an unscheduled gap or break between tasks (no noisy text!).
 */
@Composable
fun SpineConnectorSegment(
    isDottedBreak: Boolean,
    height: Dp = if (isDottedBreak) 26.dp else 18.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(56.dp))
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(height),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
            ) {
                drawLine(
                    color = if (isDottedBreak) InkMuted else InkBlack,
                    start = Offset(size.width / 2f, 0f),
                    end = Offset(size.width / 2f, size.height),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = if (isDottedBreak) {
                        PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    } else {
                        null
                    }
                )
            }
        }
    }
}

/**
 * Section 2 De-Duplicated Timeline Task Row:
 * - Left of capsule: Start/End time (08:00 / 08:15) or #1, #2.
 * - Center: Capsule icon with clean 1.5.dp normal line (no heavy offset drop shadow).
 * - Right of capsule: ONLY the Task Title (+ small recurring icon and subtask count if present) — NO duplicate time text!
 * - If the task was paused and resumed in Focus Mode, renders a clean visual split capsule with a dotted break line in between,
 *   while keeping the detailed start/pause/resume timestamps inside the popup when tapped!
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
    val hasPauseBreak = segments.size > 1 || (segments.size == 1 && task.activeSessionStartMin != null)

    val capsuleHeight = when {
        task.durationMinutes >= 60 -> 78.dp
        task.durationMinutes >= 45 -> 66.dp
        task.durationMinutes >= 30 -> 56.dp
        else -> 48.dp
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenTaskDetail),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Left Column: Start/End Time OR Step #
            Column(
                modifier = Modifier.width(46.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (isTimed) {
                    Text(
                        text = formatMinutesAsClock(startMin),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isUpNext) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isUpNext) InkBlack else InkMuted
                        )
                    )
                    Spacer(modifier = Modifier.height((capsuleHeight - 30.dp).coerceAtLeast(8.dp)))
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

            // 2. Center Capsule Icon (Clean 1.5.dp normal line, no offset shadow)
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(capsuleHeight),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .height(capsuleHeight)
                        .clip(RoundedCornerShape(23.dp))
                        .background(if (task.isCompleted) SoftSheetSurface else accentColor)
                        .border(1.5.dp, InkBlack, RoundedCornerShape(23.dp)),
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

            // 3. Right Content: ONLY Task Title + small icons + Circle Checkbox (No duplicate time text!)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (isUpNext && !task.isCompleted) {
                            Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(neoSoftColorForKey(task.colorKey))
                                .border(1.5.dp, InkBlack, RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        } else {
                            Modifier.padding(vertical = 8.dp)
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
                            text = task.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (task.isCompleted) InkMuted else InkBlack,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (task.recurrenceEnum != RecurrenceRule.NONE) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = task.recurrenceEnum.label,
                                tint = InkMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

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

        // 4. Clean visual dotted break connector when a task was paused & resumed (details live inside the popup on tap!)
        if (hasPauseBreak) {
            val resumedMin = task.activeSessionStartMin ?: segments.lastOrNull()?.first ?: endMin
            SpineConnectorSegment(isDottedBreak = true, height = 22.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenTaskDetail),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatMinutesAsClock(resumedMin),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                    color = InkMuted,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(46.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier.width(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (task.isCompleted) SoftSheetSurface else accentColor)
                            .border(1.5.dp, InkBlack, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconForKey(task.iconKey),
                            contentDescription = "Continued",
                            tint = InkBlack,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "${task.title} (Continued)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkCharcoal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Dedicated Task & Subtasks Detail Window when a task is clicked on the timeline.
 * Shows full time/duration, pause/resume session history, and subtasks cleanly on tap.
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
                                .border(1.5.dp, InkBlack, CircleShape),
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
                                text = buildString {
                                    append("${task.folderName} • ${task.sectionName} • ${formatDurationBadge(task.durationMinutes)}")
                                    task.startTimeMinutes?.let {
                                        append(" • ${formatMinutesAsClock(it)}")
                                    }
                                },
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

                // Recorded Session Segments (Started / Paused / Continued details shown on tap!)
                if (task.parsedSessionSegments.isNotEmpty() || task.activeSessionStartMin != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SoftSheetSurface)
                            .border(1.5.dp, InkBlack.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Timeline Session Log",
                            style = MaterialTheme.typography.labelLarge,
                            color = InkBlack
                        )
                        val segments = task.parsedSessionSegments
                        segments.forEachIndexed { idx, (s, e) ->
                            if (idx == 0) {
                                Text(
                                    text = "Started ${formatMinutesAsClock(s)} • Paused at ${formatMinutesAsClock(e)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = InkCharcoal
                                )
                            } else {
                                Text(
                                    text = "Continued ${formatMinutesAsClock(s)} – ${formatMinutesAsClock(e)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = InkCharcoal
                                )
                            }
                        }
                        task.activeSessionStartMin?.let { activeStart ->
                            val label = if (segments.isEmpty()) "Started at" else "Continued at"
                            Text(
                                text = "$label ${formatMinutesAsClock(activeStart)} (Running)",
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
