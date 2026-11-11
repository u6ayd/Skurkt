package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.ui.effectiveTaskStepNumber
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
private val SHORT_WEEKDAY_LABELS = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

// Shared geometry constants so the timeline spine sits shifted toward the left edge
private val TIME_COLUMN_WIDTH = 38.dp
private val TIME_TO_CAPSULE_GAP = 6.dp
private val CAPSULE_COLUMN_WIDTH = 44.dp

/**
 * Ultra-Clean Header & Swipeable Week Calendar:
 * - Shows ONLY the Month name ("November") in the Outfit font.
 * - Horizontally swipeable 7-day week strip with cached weekday labels and smooth press dimming.
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

    val monthName = remember(selectedDate.month) {
        selectedDate.month.getDisplayName(JavaTextStyle.FULL, Locale.ENGLISH)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = monthName,
            style = MaterialTheme.typography.displayMedium,
            color = InkBlack,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

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
                    val dayLabel = SHORT_WEEKDAY_LABELS[offset.toInt()]
                    val dotColors = weekDotColorsByEpochDay[epoch].orEmpty()

                    val badgeColor by animateColorAsState(
                        targetValue = when {
                            isSelected -> NeoCoral
                            isCurrentToday -> NeoYellow
                            else -> Color.Transparent
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "day_badge_color"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .smoothPressClickable(role = Role.Tab) { onSelectDate(epoch) }
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
                                .background(badgeColor, CircleShape)
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
 * Top Anytime Daily Quick-Bubbles (e.g., "Structure Tomorrow", "Water Plants", "Call Mum"):
 * - Single tap on the circular icon smoothly dims/animates the circle color and toggles Done/Undone.
 * - Long-press on the circular icon (or tap on its text label below) opens a clean quick-action popup
 *   with Delete, Edit, and Toggle Done options!
 */
@Composable
fun TopAnytimeBubblesRow(
    bubbles: List<TaskWithSubtasks>,
    onToggleBubbleDone: (TaskWithSubtasks) -> Unit,
    onEditBubble: (TaskWithSubtasks) -> Unit,
    onDeleteBubble: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (bubbles.isEmpty()) return
    var actionBubble by remember { mutableStateOf<TaskWithSubtasks?>(null) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.Top
    ) {
        bubbles.forEach { item ->
            val task = item.task
            val baseColor = neoColorForKey(task.colorKey)
            val animatedBubbleFill by animateColorAsState(
                targetValue = if (task.isCompleted) SoftSheetSurface else baseColor,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "top_bubble_fill_${task.id}"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(74.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .smoothCombinedClickable(
                            role = Role.Checkbox,
                            onLongClick = { actionBubble = item },
                            onClick = { onToggleBubbleDone(item) }
                        )
                        .clip(CircleShape)
                        .background(animatedBubbleFill, CircleShape)
                        .border(1.5.dp, InkBlack, CircleShape)
                        .testTag("top_bubble_${task.id}"),
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
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.smoothPressClickable(role = Role.Button) {
                        actionBubble = item
                    }
                )
            }
        }
    }

    actionBubble?.let { selectedItem ->
        val bubbleTask = selectedItem.task
        val bubbleColor = neoColorForKey(bubbleTask.colorKey)
        Dialog(onDismissRequest = { actionBubble = null }) {
            NeoCard(
                modifier = Modifier
                    .widthIn(max = 330.dp)
                    .fillMaxWidth(),
                backgroundColor = PureWhiteSurface,
                cornerRadius = 22.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(bubbleColor, CircleShape)
                                    .border(1.5.dp, InkBlack, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconForKey(bubbleTask.iconKey),
                                    contentDescription = null,
                                    tint = InkBlack,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "All-Day Bubble",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = InkMuted
                                )
                                Text(
                                    text = bubbleTask.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = InkBlack,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        IconButton(
                            onClick = { actionBubble = null },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SoftSheetSurface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = InkBlack,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    NeoPillButton(
                        text = if (bubbleTask.isCompleted) "Mark Undone" else "Mark Done",
                        icon = Icons.Default.Check,
                        selected = true,
                        activeColor = NeoMintGreen,
                        onClick = {
                            onToggleBubbleDone(selectedItem)
                            actionBubble = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NeoPillButton(
                            text = "Edit",
                            icon = Icons.Default.Edit,
                            selected = true,
                            activeColor = NeoYellow,
                            onClick = {
                                val target = selectedItem
                                actionBubble = null
                                onEditBubble(target)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        NeoPillButton(
                            text = "Delete",
                            icon = Icons.Default.Delete,
                            selected = true,
                            activeColor = NeoCoral,
                            onClick = {
                                val targetId = bubbleTask.id
                                actionBubble = null
                                onDeleteBubble(targetId)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("delete_bubble_button")
                        )
                    }
                }
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
 * - Aligned with the left-shifted capsule column (TIME_COLUMN_WIDTH + TIME_TO_CAPSULE_GAP).
 * - Solid vertical line when tasks flow continuously back-to-back.
 * - Dotted vertical break line when there is a time gap between tasks.
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
        Spacer(modifier = Modifier.width(TIME_COLUMN_WIDTH + TIME_TO_CAPSULE_GAP))
        Box(
            modifier = Modifier
                .width(CAPSULE_COLUMN_WIDTH)
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
 * Smooth Aesthetic Roller-Coaster Dotted Loop Canvas:
 * - Starts directly at (cx, 0f) touching the bottom of the top half-oval (zero gap!).
 * - Flows straight down briefly, sweeps smoothly out to the right, loops gracefully over the top,
 *   and flows straight down into (cx, height) touching the top of the bottom half-oval!
 */
@Composable
fun DottedBreakLoopCanvas(
    modifier: Modifier = Modifier,
    height: Dp = 54.dp,
    color: Color = InkBlack
) {
    Canvas(
        modifier = modifier
            .width(CAPSULE_COLUMN_WIDTH)
            .height(height)
    ) {
        val cx = size.width / 2f
        val h = size.height
        val loopExtentRight = cx + 16.dp.toPx()

        val path = Path().apply {
            // 1. Start flush against the top half-oval bottom edge and glide straight down
            moveTo(cx, 0f)
            lineTo(cx, h * 0.14f)

            // 2. Smoothly sweep down and out to the right into the bottom of the loop
            cubicTo(
                cx, h * 0.56f,
                loopExtentRight, h * 0.78f,
                loopExtentRight, h * 0.46f
            )

            // 3. Gracefully loop over the top crest and curve back to the center spine
            cubicTo(
                loopExtentRight, h * 0.14f,
                cx, h * 0.28f,
                cx, h * 0.84f
            )

            // 4. Glide straight down flush into the top edge of the bottom half-oval
            lineTo(cx, h)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.5.dp.toPx(), 4.5.dp.toPx()), 0f)
            )
        )
    }
}

/**
 * Shifted-Left Timeline Task Row with 100% Connected Spine, Oval Bubble Capsules,
 * and Seamless Halved Oval + Smooth Dotted Loop + Task Icon in Both Halves on Break:
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
    val capsuleFill by animateColorAsState(
        targetValue = if (task.isCompleted) SoftSheetSurface else accentColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "capsule_fill_${task.id}"
    )

    val isTimed = task.modeEnum == TaskMode.TIMED_BLOCK || task.startTimeMinutes != null
    val startMin = task.startTimeMinutes ?: (9 * 60)
    val endMin = (startMin + task.durationMinutes).coerceAtMost(23 * 60 + 59)
    val segments = task.parsedSessionSegments
    val hasPauseBreak = segments.size > 1 || (segments.size == 1 && task.activeSessionStartMin != null)

    if (!hasPauseBreak) {
        // =========================================================================
        // CASE A: Normal Unbroken Task -> Smooth Vertical Oval Bubble Capsule
        // Always >= 68.dp tall so the oval is the tallest element in the Row and
        // touches incoming/outgoing spine lines with ZERO vertical gap!
        // =========================================================================
        val ovalHeight = when {
            task.durationMinutes >= 60 -> 84.dp
            task.durationMinutes >= 45 -> 76.dp
            task.durationMinutes >= 30 -> 70.dp
            else -> 68.dp
        }
        val ovalShape = RoundedCornerShape(percent = 50)

        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(ovalHeight)
                .smoothPressClickable(role = Role.Button, onClick = onOpenTaskDetail),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Left-shifted Time Column
            Column(
                modifier = Modifier
                    .width(TIME_COLUMN_WIDTH)
                    .fillMaxHeight()
                    .padding(top = 6.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (isTimed) {
                    Text(
                        text = formatMinutesAsClock(startMin),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 10.5.sp,
                            fontWeight = if (isUpNext) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isUpNext) InkBlack else InkMuted
                        )
                    )
                    Text(
                        text = formatMinutesAsClock(endMin),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                        color = InkMuted
                    )
                } else {
                    val displayStep = maxOf(sequenceNumber, effectiveTaskStepNumber(task))
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "#$displayStep",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isUpNext) InkBlack else InkMuted
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.width(TIME_TO_CAPSULE_GAP))

            // 2. Smooth Vertical Oval Bubble Capsule (fills full row height -> zero gap to spine lines)
            Box(
                modifier = Modifier
                    .width(CAPSULE_COLUMN_WIDTH)
                    .fillMaxHeight()
                    .clip(ovalShape)
                    .background(capsuleFill, ovalShape)
                    .border(1.5.dp, InkBlack, ovalShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconForKey(task.iconKey),
                    contentDescription = task.title,
                    tint = InkBlack,
                    modifier = Modifier.size(21.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 3. Right Content: Task Title + Checkbox
            TaskRightTitleAndCheckRow(
                item = item,
                isUpNext = isUpNext,
                accentColor = accentColor,
                onToggleComplete = onToggleComplete,
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        // =========================================================================
        // CASE B: Task WITH a Break -> Single Unified Row where the Center Spine Column
        // stacks Top Half-Oval + Smooth Dotted Loop + Bottom Half-Oval with ZERO gaps!
        // =========================================================================
        val firstSegStart = segments.firstOrNull()?.first ?: startMin
        val firstSegEnd = segments.firstOrNull()?.second ?: (startMin + task.durationMinutes / 2)
        val secondSegStart = task.activeSessionStartMin ?: segments.lastOrNull()?.first ?: (firstSegEnd + 10)
        val secondSegEnd = segments.lastOrNull()?.second ?: endMin

        val topHalfHeight = 52.dp
        val loopHeight = 54.dp
        val bottomHalfHeight = 52.dp
        val totalSplitHeight = topHalfHeight + loopHeight + bottomHalfHeight

        // Top half of the oval: rounded oval top, abrupt flat cut at bottom
        val topHalfOvalShape = RoundedCornerShape(
            topStart = 22.dp,
            topEnd = 22.dp,
            bottomStart = 4.dp,
            bottomEnd = 4.dp
        )
        // Bottom half of the oval: abrupt flat cut at top, rounded oval at bottom
        val bottomHalfOvalShape = RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 4.dp,
            bottomStart = 22.dp,
            bottomEnd = 22.dp
        )

        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(totalSplitHeight)
                .smoothPressClickable(role = Role.Button, onClick = onOpenTaskDetail),
            verticalAlignment = Alignment.Top
        ) {
            // 1. Left Time Column aligned with Top Half-Oval and Bottom Half-Oval
            Column(
                modifier = Modifier
                    .width(TIME_COLUMN_WIDTH)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(topHalfHeight)
                        .padding(top = 4.dp, bottom = 2.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatMinutesAsClock(firstSegStart),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 10.5.sp,
                            fontWeight = if (isUpNext) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isUpNext) InkBlack else InkMuted
                        )
                    )
                    Text(
                        text = formatMinutesAsClock(firstSegEnd),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.5.sp),
                        color = InkMuted
                    )
                }

                Spacer(modifier = Modifier.height(loopHeight))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(bottomHalfHeight)
                        .padding(top = 2.dp, bottom = 4.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatMinutesAsClock(secondSegStart),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.5.sp),
                        color = InkMuted
                    )
                    Text(
                        text = formatMinutesAsClock(secondSegEnd),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                        color = InkMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(TIME_TO_CAPSULE_GAP))

            // 2. Uninterrupted Center Spine Column (0px gap between incoming line, top half, loop, bottom half, and outgoing line!)
            Column(
                modifier = Modifier
                    .width(CAPSULE_COLUMN_WIDTH)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Half-Oval Tube with Task Icon
                Box(
                    modifier = Modifier
                        .width(CAPSULE_COLUMN_WIDTH)
                        .height(topHalfHeight)
                        .clip(topHalfOvalShape)
                        .background(capsuleFill, topHalfOvalShape)
                        .border(1.5.dp, InkBlack, topHalfOvalShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconForKey(task.iconKey),
                        contentDescription = task.title,
                        tint = InkBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Smooth Flowing Dotted Loop directly connecting Top Half and Bottom Half
                DottedBreakLoopCanvas(
                    height = loopHeight,
                    color = InkBlack
                )

                // Bottom Half-Oval Tube with the SAME Task Icon (not a dot!)
                Box(
                    modifier = Modifier
                        .width(CAPSULE_COLUMN_WIDTH)
                        .height(bottomHalfHeight)
                        .clip(bottomHalfOvalShape)
                        .background(capsuleFill, bottomHalfOvalShape)
                        .border(1.5.dp, InkBlack, bottomHalfOvalShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconForKey(task.iconKey),
                        contentDescription = "${task.title} Continued",
                        tint = InkBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 3. Right Content Column aligned with Top Half-Oval (no redundant '(Continued)' text at bottom)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(topHalfHeight + 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                TaskRightTitleAndCheckRow(
                    item = item,
                    isUpNext = isUpNext,
                    accentColor = accentColor,
                    onToggleComplete = onToggleComplete,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TaskRightTitleAndCheckRow(
    item: TaskWithSubtasks,
    isUpNext: Boolean,
    accentColor: Color,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val task = item.task
    Row(
        modifier = modifier
            .then(
                if (isUpNext && !task.isCompleted) {
                    Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(neoSoftColorForKey(task.colorKey))
                        .border(1.5.dp, InkBlack, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 9.dp)
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
                Spacer(modifier = Modifier.height(2.dp))
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

/**
 * Dedicated Task & Subtasks Detail Window when a task is clicked on the timeline.
 * Includes a 1-tap "Split with Break Loop" / "Merge Capsule" button alongside Focus & Subtasks.
 */
@Composable
fun TaskDetailPopupModal(
    item: TaskWithSubtasks,
    currentStepNumber: Int = 1,
    onUpdateStepNumber: (Int) -> Unit = {},
    onDismiss: () -> Unit,
    onToggleComplete: () -> Unit,
    onToggleBreakLoop: () -> Unit,
    onToggleSubtask: (SubtaskEntity) -> Unit,
    onAddSubtask: (String) -> Unit,
    onStartFocus: () -> Unit,
    onEditTask: () -> Unit
) {
    val task = item.task
    val accent = neoColorForKey(task.colorKey)
    var newSubtask by remember { mutableStateOf("") }
    var stepVal by remember(task.id, task.sortOrder, currentStepNumber) {
        mutableStateOf(maxOf(currentStepNumber, effectiveTaskStepNumber(task)))
    }
    val hasBreakLoop = task.parsedSessionSegments.size > 1 ||
        (task.parsedSessionSegments.size == 1 && task.activeSessionStartMin != null)

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

                if (task.modeEnum != TaskMode.TOP_BUBBLE) {
                    // Sequence Position (#) Step Selector for Timeless / Spine tasks (e.g. change #4 -> #6)
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sequence Step (#)",
                                style = MaterialTheme.typography.titleMedium,
                                color = InkBlack
                            )
                            Text(
                                text = if (task.modeEnum == TaskMode.TIMELESS_STEP) {
                                    "Adjust timeline position (#1, #4, #6...)"
                                } else {
                                    "Timed tasks sort by clock (${formatMinutesAsClock(task.startTimeMinutes ?: 480)})"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = InkMuted
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .smoothPressClickable(role = Role.Button) {
                                        val next = (stepVal - 1).coerceAtLeast(1)
                                        stepVal = next
                                        onUpdateStepNumber(next)
                                    }
                                    .clip(CircleShape)
                                    .background(PureWhiteSurface)
                                    .border(1.5.dp, InkBlack, CircleShape)
                                    .testTag("step_number_minus"),
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
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "#$stepVal",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = InkBlack
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .smoothPressClickable(role = Role.Button) {
                                        val next = (stepVal + 1).coerceAtMost(99)
                                        stepVal = next
                                        onUpdateStepNumber(next)
                                    }
                                    .clip(CircleShape)
                                    .background(PureWhiteSurface)
                                    .border(1.5.dp, InkBlack, CircleShape)
                                    .testTag("step_number_plus"),
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

                // 1-Tap Break Loop Split Toggle + Recorded Session Log
                NeoPillButton(
                    text = if (hasBreakLoop) "Merge Halved Capsule (Clear Break)" else "Split Capsule with Dotted Break Loop",
                    icon = Icons.Default.Pause,
                    selected = hasBreakLoop,
                    activeColor = NeoSkyBlue,
                    onClick = onToggleBreakLoop,
                    modifier = Modifier.fillMaxWidth()
                )

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
                                    text = "Started ${formatMinutesAsClock(s)} • Break at ${formatMinutesAsClock(e)}",
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
                                .smoothPressClickable { onToggleSubtask(sub) }
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
