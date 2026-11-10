package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RecurrenceRule
import com.example.data.SubtaskEntity
import com.example.data.TaskEntity
import com.example.data.TaskMode
import com.example.data.TaskWithSubtasks
import com.example.ui.FlowFilterMode
import com.example.ui.theme.DividerStroke
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.NeoCoral
import com.example.ui.theme.NeoMintGreen
import com.example.ui.theme.NeoSkyBlue
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
        minutes >= 60 && minutes % 60 == 0 -> "${minutes / 60} hr"
        minutes > 60 -> "${minutes / 60}h ${minutes % 60}m"
        else -> "$minutes min"
    }
}

/**
 * Hybrid Header combining Trackerly's Playfair Display Bold Italic + STUDY/FLOW OS badge
 * with Structured's "7. October 2026 >" & 7-day Week Strip with colored dot clusters.
 */
@Composable
fun DateAndWeekHeader(
    selectedDate: LocalDate,
    weekDotColorsByEpochDay: Map<Long, List<String>>,
    onSelectDate: (Long) -> Unit,
    onShiftWeek: (Long) -> Unit,
    onJumpToToday: () -> Unit,
    onResetDay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthName = selectedDate.month.getDisplayName(JavaTextStyle.FULL, Locale.ENGLISH)
    val isToday = selectedDate == LocalDate.now()
    val startOfWeek = selectedDate.minusDays((selectedDate.dayOfWeek.value - 1).toLong())

    Column(modifier = modifier.fillMaxWidth()) {
        // Top Row: Date Title + Flow OS Badge + Quick Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${selectedDate.dayOfMonth}. $monthName ",
                    style = MaterialTheme.typography.displayMedium,
                    color = InkBlack
                )
                Text(
                    text = "${selectedDate.year}",
                    style = MaterialTheme.typography.displayMedium,
                    color = NeoCoral
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeoYellow)
                        .border(1.5.dp, InkBlack, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "FLOW OS",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                        color = InkBlack
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!isToday) {
                    NeoPillButton(
                        text = "Today",
                        onClick = onJumpToToday,
                        selected = true,
                        activeColor = NeoMintGreen,
                        shadowOffset = 2.dp,
                        modifier = Modifier.testTag("jump_today_button")
                    )
                }
                IconButton(
                    onClick = { onShiftWeek(-1L) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(PureWhiteSurface)
                        .border(2.dp, InkBlack, CircleShape)
                        .testTag("prev_week_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Week",
                        tint = InkBlack
                    )
                }
                IconButton(
                    onClick = { onShiftWeek(1L) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(PureWhiteSurface)
                        .border(2.dp, InkBlack, CircleShape)
                        .testTag("next_week_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Week",
                        tint = InkBlack
                    )
                }
                IconButton(
                    onClick = onResetDay,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(PureWhiteSurface)
                        .border(2.dp, InkBlack, CircleShape)
                        .testTag("reset_day_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Day Progress",
                        tint = InkBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 7-Day Week Strip (Inspired by Structured Screenshots 5 & 6)
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
                        .padding(vertical = 6.dp)
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
                            .size(36.dp)
                            .drawBehind {
                                if (isSelected) {
                                    drawCircle(
                                        color = InkBlack,
                                        radius = size.minDimension / 2f,
                                        center = Offset(center.x + 2.dp.toPx(), center.y + 2.dp.toPx())
                                    )
                                }
                            }
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
                                    Modifier.border(2.dp, InkBlack, CircleShape)
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = InkBlack
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    // Mini Colored Task Dots Row (Matches Structured Screenshots 5 & 6)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(8.dp)
                    ) {
                        dotColors.take(4).forEach { colorKey ->
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(neoColorForKey(colorKey))
                                    .border(1.dp, InkBlack, CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Anti-Paralysis Pinned "UP NEXT" Banner at the top of the flow.
 * Makes it immediately obvious what single step to do right now without scanning the whole day.
 */
@Composable
fun PinnedUpNextBanner(
    upNext: TaskWithSubtasks,
    onEnterFocus: () -> Unit,
    onCompleteNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val task = upNext.task
    val accent = neoColorForKey(task.colorKey)
    val doneSubs = upNext.subtasks.count { it.isCompleted }
    val totalSubs = upNext.subtasks.size

    NeoCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pinned_up_next_card"),
        backgroundColor = accent,
        cornerRadius = 18.dp,
        shadowOffset = 4.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(InkBlack)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "DO THIS NEXT • NO PARALYSIS",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                            color = PureWhiteSurface
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(PureWhiteSurface)
                            .border(1.5.dp, InkBlack, RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (task.modeEnum == TaskMode.TIMED_BLOCK && task.startTimeMinutes != null) {
                                "${formatMinutesAsClock(task.startTimeMinutes)} • ${formatDurationBadge(task.durationMinutes)}"
                            } else {
                                "TIMELESS STEP • ${formatDurationBadge(task.durationMinutes)}"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                            color = InkBlack
                        )
                    }
                }

                if (totalSubs > 0) {
                    Text(
                        text = "$doneSubs/$totalSubs steps",
                        style = MaterialTheme.typography.labelLarge,
                        color = InkBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PureWhiteSurface)
                            .border(2.dp, InkBlack, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconForKey(task.iconKey),
                            contentDescription = null,
                            tint = InkBlack,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = InkBlack,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val nextSub = upNext.sortedSubtasks.firstOrNull { !it.isCompleted }
                        Text(
                            text = when {
                                nextSub != null -> "Next micro-step: ${nextSub.title}"
                                task.notes.isNotBlank() -> task.notes
                                else -> "Tap Focus to lock in or mark done to advance"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkCharcoal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NeoPillButton(
                        text = "Focus",
                        icon = Icons.Default.PlayArrow,
                        onClick = onEnterFocus,
                        selected = true,
                        activeColor = PureWhiteSurface,
                        shadowOffset = 2.dp,
                        modifier = Modifier.testTag("start_focus_button")
                    )
                    IconButton(
                        onClick = onCompleteNow,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(NeoMintGreen)
                            .border(2.dp, InkBlack, CircleShape)
                            .testTag("complete_up_next_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Complete & Advance",
                            tint = InkBlack
                        )
                    }
                }
            }
        }
    }
}

/**
 * Top Circular "Anytime" Routine Bubbles Row (Inspired by Structured Screenshots 5 & 6:
 * "Structure Tomorrow", "Water Plants", "Call Mum").
 */
@Composable
fun TopAnytimeBubblesRow(
    bubbles: List<TaskWithSubtasks>,
    onToggleBubble: (TaskEntity) -> Unit,
    onEditBubble: (TaskWithSubtasks) -> Unit,
    onAddBubble: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.Top
    ) {
        bubbles.forEach { item ->
            val task = item.task
            val color = neoColorForKey(task.colorKey)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(78.dp)
                    .clickable(role = Role.Button) { onToggleBubble(task) }
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .drawBehind {
                            drawCircle(
                                color = InkBlack,
                                radius = size.minDimension / 2f,
                                center = Offset(center.x + 2.5.dp.toPx(), center.y + 2.5.dp.toPx())
                            )
                        }
                        .clip(CircleShape)
                        .background(if (task.isCompleted) SoftSheetSurface else color)
                        .border(2.dp, InkBlack, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.Check else iconForKey(task.iconKey),
                        contentDescription = task.title,
                        tint = InkBlack,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (task.isCompleted) InkMuted else InkBlack,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { onEditBubble(item) }
                )
            }
        }

        // Quick "+ Bubble" button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(72.dp)
                .clickable(role = Role.Button, onClick = onAddBubble)
                .testTag("add_top_bubble_button")
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(SoftSheetSurface)
                    .border(2.dp, InkBlack, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Routine Bubble",
                    tint = InkBlack,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Add Bubble",
                style = MaterialTheme.typography.labelMedium.copy(color = InkCharcoal),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Filter strip inside the Day Flow sheet allowing instant toggle between
 * All Structure, Timeless Steps Only, and Timed Slots Only.
 */
@Composable
fun FlowStructureFilterBar(
    currentFilter: FlowFilterMode,
    onSelectFilter: (FlowFilterMode) -> Unit,
    onAddTimelessStep: () -> Unit,
    onAddTimedBlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FlowFilterMode.entries.forEach { mode ->
            NeoPillButton(
                text = mode.label,
                selected = currentFilter == mode,
                activeColor = NeoYellow,
                shadowOffset = if (currentFilter == mode) 3.dp else 1.dp,
                onClick = { onSelectFilter(mode) }
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        NeoPillButton(
            text = "+ Timeless Step",
            selected = false,
            inactiveColor = NeoSkyBlue.copy(alpha = 0.35f),
            shadowOffset = 2.dp,
            onClick = onAddTimelessStep,
            modifier = Modifier.testTag("add_timeless_step_button")
        )
        NeoPillButton(
            text = "+ Timed Slot",
            selected = false,
            inactiveColor = NeoCoral.copy(alpha = 0.35f),
            shadowOffset = 2.dp,
            onClick = onAddTimedBlock,
            modifier = Modifier.testTag("add_timed_slot_button")
        )
    }
}

/**
 * Free-time gap indicator between timed tasks (Inspired by Structured Screenshot 5: "30min of free time?").
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
        Spacer(modifier = Modifier.width(56.dp))
        Box(
            modifier = Modifier
                .width(52.dp)
                .height(34.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(InkBlack.copy(alpha = 0.3f))
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(SoftSheetSurface)
                .border(1.5.dp, InkBlack.copy(alpha = 0.35f), RoundedCornerShape(50))
                .clickable(onClick = onFillGapClick)
                .padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = NeoCoral,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${formatDurationBadge(gapMinutes)} of free breathing room — keep open or tap to slot",
                style = MaterialTheme.typography.labelMedium.copy(color = InkCharcoal)
            )
        }
    }
}

/**
 * Hybrid Structured × Neo-Brutalist Vertical Spine Node:
 * - Supports both TIMELESS_STEP (numbered sequence #1, #2 without clock anxiety)
 *   and TIMED_BLOCK (start/end timestamps + stretched vertical pill capsule).
 * - Includes inline expandable Subtask Milestone Checklist (Screenshot 3 style).
 */
@Composable
fun HybridSpineTaskRow(
    item: TaskWithSubtasks,
    sequenceNumber: Int,
    isUpNext: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onToggleComplete: () -> Unit,
    onToggleSubtask: (SubtaskEntity) -> Unit,
    onAddQuickSubtask: (String) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEditTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    val task = item.task
    val accentColor = neoColorForKey(task.colorKey)
    val softAccent = neoSoftColorForKey(task.colorKey)
    val isTimed = task.modeEnum == TaskMode.TIMED_BLOCK && task.startTimeMinutes != null
    val startMin = task.startTimeMinutes ?: 0
    val endMin = (startMin + task.durationMinutes).coerceAtMost(23 * 60 + 59)

    // Capsule height scales with duration just like Structured's stretched pills in Screenshots 5 & 6
    val capsuleHeight = when {
        task.durationMinutes >= 60 -> 88.dp
        task.durationMinutes >= 45 -> 74.dp
        task.durationMinutes >= 30 -> 62.dp
        else -> 52.dp
    }

    var subtasksExpanded by remember(task.id, isUpNext) {
        mutableStateOf(isUpNext && item.subtasks.isNotEmpty())
    }
    var newSubtaskText by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 1. Left Column: Clock Timestamps OR Timeless Step Sequence Tag
        Column(
            modifier = Modifier
                .width(56.dp)
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.End
        ) {
            if (isTimed) {
                Text(
                    text = formatMinutesAsClock(startMin),
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                    color = if (isUpNext) InkBlack else InkMuted
                )
                Spacer(modifier = Modifier.height(capsuleHeight - 32.dp))
                Text(
                    text = formatMinutesAsClock(endMin),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                    color = InkMuted
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isUpNext) NeoYellow else SoftSheetSurface)
                        .border(1.5.dp, InkBlack, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "#${sequenceNumber.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                        color = InkBlack
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "FLOW",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.sp),
                    color = InkMuted
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // 2. Center Visual Spine: Vertical Ink Line + Stretched Colored Pill Capsule with Icon
        Box(
            modifier = Modifier
                .width(54.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            // Continuous Vertical Spine Line
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .fillMaxHeight()
                    .background(InkBlack)
            )

            // Stretched Capsule Node (Structured × Neo-Brutalist)
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .width(48.dp)
                    .height(capsuleHeight)
                    .drawBehind {
                        drawRoundRect(
                            color = InkBlack,
                            topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                            size = size,
                            cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                        )
                    }
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (task.isCompleted) SoftSheetSurface else accentColor)
                    .border(2.dp, InkBlack, RoundedCornerShape(24.dp))
                    .clickable(onClick = onEditTask),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconForKey(task.iconKey),
                    contentDescription = task.title,
                    tint = InkBlack,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 3. Right Task Card & Expandable Subtasks Matrix
        val cardBg = when {
            isUpNext -> softAccent
            task.isCompleted -> SoftSheetSurface.copy(alpha = 0.6f)
            else -> PureWhiteSurface
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp)
                .drawBehind {
                    val offsetPx = if (isUpNext) 4.dp.toPx() else 2.5.dp.toPx()
                    drawRoundRect(
                        color = InkBlack,
                        topLeft = Offset(offsetPx, offsetPx),
                        size = size,
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                    )
                }
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg, RoundedCornerShape(16.dp))
                .border(
                    BorderStroke(if (isUpNext) 2.5.dp else 2.dp, InkBlack),
                    RoundedCornerShape(16.dp)
                )
                .clickable(onClick = onEditTask)
                .padding(14.dp)
        ) {
            // Top Meta Row: Time/Sequence subtitle + Subject Tag + Reorder Arrows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor)
                            .border(1.5.dp, InkBlack, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.subjectTag.uppercase(),
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                            color = InkBlack
                        )
                    }

                    Text(
                        text = if (isTimed) {
                            "${formatMinutesAsClock(startMin)}–${formatMinutesAsClock(endMin)} (${formatDurationBadge(task.durationMinutes)})"
                        } else {
                            "Timeless Step • ${formatDurationBadge(task.durationMinutes)}"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = InkCharcoal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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

                // Up / Down Sequence Reorder Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (canMoveUp) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Move Step Earlier",
                            tint = InkBlack,
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onMoveUp)
                        )
                    }
                    if (canMoveDown) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move Step Later",
                            tint = InkBlack,
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onMoveDown)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Title + Completion Checkbox Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (task.isCompleted) InkMuted else InkBlack
                    )
                    if (task.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = task.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkMuted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
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

            // Subtask Pill & Expandable Neo-Brutalist Checklist (Screenshots 3 & 5)
            val totalSubs = item.subtasks.size
            val doneSubs = item.subtasks.count { it.isCompleted }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PureWhiteSurface)
                        .border(1.5.dp, InkBlack, RoundedCornerShape(8.dp))
                        .clickable { subtasksExpanded = !subtasksExpanded }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Checklist,
                        contentDescription = null,
                        tint = InkBlack,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (totalSubs > 0) "$doneSubs/$totalSubs Milestones" else "+ Add Subtasks",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                        color = InkBlack
                    )
                }

                if (isUpNext && !task.isCompleted) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeoYellow)
                            .border(1.5.dp, InkBlack, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ACTIVE STEP",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                            color = InkBlack
                        )
                    }
                }
            }

            AnimatedVisibility(visible = subtasksExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PureWhiteSurface)
                        .border(1.5.dp, InkBlack, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item.sortedSubtasks.forEach { sub ->
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
                                    accentColor = accentColor
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = sub.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    color = if (sub.isCompleted) InkMuted else InkBlack
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SoftSheetSurface)
                                    .border(1.dp, InkBlack, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = sub.stageTag.uppercase(),
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.sp),
                                    color = InkBlack
                                )
                            }
                        }
                    }

                    // Inline Quick-Add Subtask Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newSubtaskText,
                            onValueChange = { newSubtaskText = it },
                            placeholder = {
                                Text(
                                    "Add micro-step...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = InkMuted
                                )
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        NeoPillButton(
                            text = "Add",
                            onClick = {
                                if (newSubtaskText.isNotBlank()) {
                                    onAddQuickSubtask(newSubtaskText)
                                    newSubtaskText = ""
                                }
                            },
                            selected = true,
                            activeColor = accentColor,
                            shadowOffset = 2.dp
                        )
                    }
                }
            }
        }
    }
}
