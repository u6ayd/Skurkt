package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.draw.drawWithCache
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
import androidx.compose.ui.window.DialogProperties
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
import com.example.ui.theme.sdp
import com.example.ui.theme.ssp
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

object GlobalClockFormat {
    var use24Hour by mutableStateOf(true)
    var startOfDayMinutes by mutableStateOf(8 * 60)
    var endOfDayMinutes by mutableStateOf(22 * 60)
}

fun formatMinutesAsClock(
    minutes: Int,
    use24Hour: Boolean = GlobalClockFormat.use24Hour
): String {
    val normalized = ((minutes % 1440) + 1440) % 1440
    val hrs24 = (normalized / 60).coerceIn(0, 23)
    val mins = (normalized % 60).coerceIn(0, 59)
    return if (use24Hour) {
        String.format(Locale.US, "%02d:%02d", hrs24, mins)
    } else {
        val period = if (hrs24 < 12) "AM" else "PM"
        val hrs12 = when {
            hrs24 == 0 -> 12
            hrs24 > 12 -> hrs24 - 12
            else -> hrs24
        }
        String.format(Locale.US, "%d:%02d %s", hrs12, mins, period)
    }
}

fun formatDurationBadge(minutes: Int): String {
    return when {
        minutes <= 0 -> "No duration"
        minutes >= 60 && minutes % 60 == 0 -> "${minutes / 60}h"
        minutes > 60 -> "${minutes / 60}h ${minutes % 60}m"
        else -> "${minutes}m"
    }
}

fun formatFreeGapLabel(gapMinutes: Int): String {
    val safe = gapMinutes.coerceAtLeast(0)
    return when {
        safe >= 60 && safe % 60 == 0 -> "${safe / 60}h free"
        safe > 60 -> "${safe / 60}h ${safe % 60}m free"
        else -> "${safe}m free"
    }
}

private const val WEEK_PAGER_CENTER = 500
private const val WEEK_PAGER_COUNT = 1000
private val SHORT_WEEKDAY_LABELS = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

// Shared geometry constants so the timeline spine sits shifted toward the left edge and scales proportionally on tablet
private val TIME_COLUMN_WIDTH: Dp
    @Composable get() = if (GlobalClockFormat.use24Hour) 38.sdp else 52.sdp
private val TIME_TO_CAPSULE_GAP: Dp
    @Composable get() = 6.sdp
private val CAPSULE_COLUMN_WIDTH: Dp
    @Composable get() = 48.sdp

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
    onOpenSettings: () -> Unit = {},
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthName,
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                color = InkBlack,
                maxLines = 1
            )

            Box(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(40.sdp)
                    .clip(CircleShape)
                    .background(PureWhiteSurface, CircleShape)
                    .border(1.5.dp, InkBlack, CircleShape)
                    .smoothPressClickable(role = Role.Button, onClick = onOpenSettings)
                    .testTag("open_settings_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = InkBlack,
                    modifier = Modifier.size(20.sdp)
                )
            }
        }

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
                                .size(36.sdp)
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
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                                color = if (isSelected) PureWhiteSurface else InkBlack
                            )
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(6.sdp)
                        ) {
                            dotColors.take(4).forEach { colorKey ->
                                Box(
                                    modifier = Modifier
                                        .size(5.sdp)
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
    onInspectBubble: (TaskWithSubtasks) -> Unit,
    onEditBubble: (TaskWithSubtasks) -> Unit = {},
    onDeleteBubble: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (bubbles.isEmpty()) return

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
                modifier = Modifier.width(74.sdp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.sdp)
                        .smoothCombinedClickable(
                            role = Role.Checkbox,
                            onLongClick = { onInspectBubble(item) },
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
                        modifier = Modifier.size(22.sdp)
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 11.ssp,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (task.isCompleted) InkMuted else InkBlack,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.smoothPressClickable(role = Role.Button) {
                        onInspectBubble(item)
                    }
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
 * - Aligned with the left-shifted capsule column (TIME_COLUMN_WIDTH + TIME_TO_CAPSULE_GAP).
 * - Solid vertical line when tasks flow continuously back-to-back.
 * - Dotted vertical break line when there is a time gap between tasks.
 */
@Composable
fun SpineConnectorSegment(
    isDottedBreak: Boolean,
    freeTimeLabel: String? = null,
    height: Dp = if (isDottedBreak) 34.sdp else 18.sdp,
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
            Spacer(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .drawWithCache {
                        val strokePx = 2.dp.toPx()
                        val dashEffect = if (isDottedBreak) {
                            PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        } else {
                            null
                        }
                        val lineColor = if (isDottedBreak) InkMuted else InkBlack
                        onDrawBehind {
                            drawLine(
                                color = lineColor,
                                start = Offset(size.width / 2f, 0f),
                                end = Offset(size.width / 2f, size.height),
                                strokeWidth = strokePx,
                                pathEffect = dashEffect
                            )
                        }
                    }
            )
        }
        if (!freeTimeLabel.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(SoftSheetSurface)
                    .border(1.dp, InkBlack.copy(alpha = 0.35f), RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = freeTimeLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.ssp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = InkMuted
                )
            }
        }
    }
}

/**
 * Smooth Aesthetic Roller-Coaster Dotted Loop Canvas (cached via drawWithCache for 60fps scrolling):
 * - Starts directly at (cx, 0f) touching the bottom of the top half-oval (zero gap!).
 * - Flows straight down briefly, sweeps smoothly out to the right, loops gracefully over the top,
 *   and flows straight down into (cx, height) touching the top of the bottom half-oval!
 */
@Composable
fun DottedBreakLoopCanvas(
    modifier: Modifier = Modifier,
    height: Dp = 54.sdp,
    color: Color = InkBlack
) {
    val loopRightOffset = 16.sdp
    Spacer(
        modifier = modifier
            .width(CAPSULE_COLUMN_WIDTH)
            .height(height)
            .drawWithCache {
                val cx = size.width / 2f
                val h = size.height
                val loopExtentRight = cx + loopRightOffset.toPx()

                val cachedPath = Path().apply {
                    moveTo(cx, 0f)
                    lineTo(cx, h * 0.14f)
                    cubicTo(
                        cx, h * 0.56f,
                        loopExtentRight, h * 0.78f,
                        loopExtentRight, h * 0.46f
                    )
                    cubicTo(
                        loopExtentRight, h * 0.14f,
                        cx, h * 0.28f,
                        cx, h * 0.84f
                    )
                    lineTo(cx, h)
                }

                val dashPx = 4.5.dp.toPx()
                val cachedStroke = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashPx, dashPx), 0f)
                )

                onDrawBehind {
                    drawPath(
                        path = cachedPath,
                        color = color,
                        style = cachedStroke
                    )
                }
            }
    )
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
    isSimultaneous: Boolean = false,
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

    val isTimed = task.modeEnum == TaskMode.TIMED_BLOCK
    val startMin = task.startTimeMinutes ?: (9 * 60)
    val endMin = (startMin + task.durationMinutes.coerceAtLeast(5)).coerceAtMost(23 * 60 + 59)
    val segments = task.parsedSessionSegments
    val hasPauseBreak = segments.size > 1 || (segments.size == 1 && task.activeSessionStartMin != null)

    if (!hasPauseBreak) {
        val ovalHeight = when {
            task.durationMinutes >= 90 -> 92.sdp
            task.durationMinutes >= 60 -> 88.sdp
            task.durationMinutes >= 45 -> 80.sdp
            task.durationMinutes >= 30 -> 74.sdp
            else -> 70.sdp
        }
        val ovalShape = RoundedCornerShape(percent = 50)

        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(ovalHeight)
                .smoothCombinedClickable(
                    role = Role.Button,
                    onLongClick = onOpenTaskDetail,
                    onClick = onOpenTaskDetail
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Left-shifted Time / Sequence (#1, #2, #3...) Column
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
                            fontSize = 10.5.ssp,
                            fontWeight = FontWeight.Normal,
                            color = if (isUpNext) InkBlack else InkMuted
                        )
                    )
                    Text(
                        text = formatMinutesAsClock(endMin),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 10.ssp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = InkMuted
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "#$sequenceNumber",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
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
                    modifier = Modifier.size(22.sdp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 3. Right Content: Task Title + Checkbox
            TaskRightTitleAndCheckRow(
                item = item,
                isUpNext = isUpNext,
                isSimultaneous = isSimultaneous || (isTimed && task.allowSimultaneous),
                accentColor = accentColor,
                onToggleComplete = onToggleComplete,
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        // =========================================================================
        // CASE B: Task WITH 1 or More Breaks -> Condenses onto 1 Unified Split Break Loop
        // on the Center Spine Column (Top Half-Oval + Dotted Loop + Bottom Half-Oval)
        // and extends Timed task end time by totalBreakMinutes!
        // =========================================================================
        val firstSegStart = segments.firstOrNull()?.first ?: startMin
        val firstSegEnd = segments.firstOrNull()?.second ?: (startMin + task.durationMinutes / 2)
        val secondSegStart = task.activeSessionStartMin ?: segments.lastOrNull()?.first ?: (firstSegEnd + 10)
        val extendedTimedEnd = maxOf(
            segments.lastOrNull()?.second ?: 0,
            startMin + task.durationMinutes.coerceAtLeast(5) + task.totalBreakMinutes
        ).coerceAtMost(23 * 60 + 59)
        val secondSegEnd = if (isTimed) extendedTimedEnd else (segments.lastOrNull()?.second ?: endMin)

        val topHalfHeight = 52.sdp
        val loopHeight = 54.sdp
        val bottomHalfHeight = 52.sdp
        val totalSplitHeight = topHalfHeight + loopHeight + bottomHalfHeight

        // Top half of the oval: rounded oval top, abrupt flat cut at bottom
        val topHalfOvalShape = RoundedCornerShape(
            topStart = 22.sdp,
            topEnd = 22.sdp,
            bottomStart = 4.sdp,
            bottomEnd = 4.sdp
        )
        // Bottom half of the oval: abrupt flat cut at top, rounded oval at bottom
        val bottomHalfOvalShape = RoundedCornerShape(
            topStart = 4.sdp,
            topEnd = 4.sdp,
            bottomStart = 22.sdp,
            bottomEnd = 22.sdp
        )

        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(totalSplitHeight)
                .smoothCombinedClickable(
                    role = Role.Button,
                    onLongClick = onOpenTaskDetail,
                    onClick = onOpenTaskDetail
                ),
            verticalAlignment = Alignment.Top
        ) {
            // 1. Left Time / Sequence Column aligned with Top Half-Oval and Bottom Half-Oval
            Column(
                modifier = Modifier
                    .width(TIME_COLUMN_WIDTH)
                    .fillMaxHeight()
            ) {
                if (isTimed) {
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
                                fontSize = 10.5.ssp,
                                fontWeight = FontWeight.Normal,
                                color = if (isUpNext) InkBlack else InkMuted
                            )
                        )
                        Text(
                            text = formatMinutesAsClock(firstSegEnd),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 9.5.ssp,
                                fontWeight = FontWeight.Normal
                            ),
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
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 9.5.ssp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = InkMuted
                        )
                        Text(
                            text = formatMinutesAsClock(secondSegEnd),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 10.ssp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = InkMuted
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(topHalfHeight),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "#$sequenceNumber",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isUpNext) InkBlack else InkMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(loopHeight))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(bottomHalfHeight),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Cont.",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 10.ssp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = InkMuted
                        )
                    }
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
                        modifier = Modifier.size(20.sdp)
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
                        modifier = Modifier.size(20.sdp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 3. Right Content Column aligned with Top Half-Oval (allows 2-line task titles cleanly)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = topHalfHeight + 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                TaskRightTitleAndCheckRow(
                    item = item,
                    isUpNext = isUpNext,
                    isSimultaneous = isSimultaneous || (isTimed && task.allowSimultaneous),
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
    isSimultaneous: Boolean = false,
    accentColor: Color,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val task = item.task
    val hasPauseBreak = task.parsedSessionSegments.size > 1 ||
        (task.parsedSessionSegments.size == 1 && task.activeSessionStartMin != null)

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
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 20.ssp,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (task.isCompleted) InkMuted else InkBlack,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (task.recurrenceEnum != RecurrenceRule.NONE) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = task.recurrenceSummaryLabel(),
                        tint = InkMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            val hasDurationBadge = task.durationMinutes > 0
            val hasSubtasks = item.subtasks.isNotEmpty()
            if (hasDurationBadge || hasSubtasks || isSimultaneous || hasPauseBreak) {
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (hasDurationBadge) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = InkMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = formatDurationBadge(task.durationMinutes),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
                                color = InkMuted
                            )
                        }
                    }

                    if (hasSubtasks) {
                        val done = item.completedSubtasksCount
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
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
                                color = InkMuted
                            )
                        }
                    }

                    if (isSimultaneous) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(NeoYellow)
                                .border(1.dp, InkBlack, RoundedCornerShape(50))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Simultaneous",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.5.ssp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = InkBlack
                            )
                        }
                    }
                }

                if (hasPauseBreak) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val workedMin = task.totalWorkedMinutes.coerceAtLeast(1)
                    val breakMin = task.totalBreakMinutes.coerceAtLeast(1)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(PureWhiteSurface)
                            .border(1.dp, InkBlack.copy(alpha = 0.6f), RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Worked ${formatDurationBadge(workedMin)} • Break ${formatDurationBadge(breakMin)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.ssp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = InkBlack
                        )
                    }
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
 * Unified, Cohesive Task Action & Detail Modal:
 * - Used for BOTH Top Bubbles (on long-click / label tap) and Spine Tasks (on click / long-click).
 * - Decluttered layout:
 *   1. Compact 1-Row Sequence (#) & Break Loop Strip for Spine Tasks.
 *   2. Subtasks Checklist (collapses the input field into a neat '+ Add Subtask' chip when 0 subtasks exist).
 *   3. Cohesive Action Buttons: Focus Timer, Mark Done, Edit (Pencil), and a dedicated Delete Popup!
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
    onEditTask: () -> Unit,
    onDeleteTodayOnly: (() -> Unit)? = null,
    onDeleteTask: () -> Unit = {}
) {
    val task = item.task
    val accent = neoColorForKey(task.colorKey)
    var newSubtask by remember { mutableStateOf("") }
    var showAddSubtaskField by remember(task.id) { mutableStateOf(item.subtasks.isNotEmpty()) }
    var showDeleteConfirmPopup by remember(task.id) { mutableStateOf(false) }
    var stepVal by remember(task.id, currentStepNumber) {
        mutableStateOf(currentStepNumber.coerceIn(1, 99))
    }
    val hasBreakLoop = task.parsedSessionSegments.size > 1 ||
        (task.parsedSessionSegments.size == 1 && task.activeSessionStartMin != null)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        NeoCard(
            modifier = Modifier
                .widthIn(max = 430.dp)
                .fillMaxWidth(),
            backgroundColor = PureWhiteSurface,
            cornerRadius = 22.dp,
            contentPadding = PaddingValues(18.dp)
        ) {
            AnimatedContent(
                targetState = showDeleteConfirmPopup,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "task_detail_delete_switch"
            ) { isDeleting ->
                if (isDeleting) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(NeoCoral)
                                    .border(1.5.dp, InkBlack, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = PureWhiteSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (task.recurrenceEnum != RecurrenceRule.NONE) "Delete Repeating Task?" else "Delete Task?",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = InkBlack
                                )
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 18.ssp
                                    ),
                                    color = InkMuted,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (task.recurrenceEnum != RecurrenceRule.NONE && onDeleteTodayOnly != null) {
                            NeoPillButton(
                                text = "Delete Today Only",
                                selected = true,
                                activeColor = NeoYellow,
                                onClick = {
                                    showDeleteConfirmPopup = false
                                    onDeleteTodayOnly()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("delete_today_only_button")
                            )
                            NeoPillButton(
                                text = "Delete All Future Tasks",
                                icon = Icons.Default.Delete,
                                selected = true,
                                activeColor = NeoCoral,
                                onClick = {
                                    showDeleteConfirmPopup = false
                                    onDeleteTask()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("confirm_delete_task_button")
                            )
                            NeoPillButton(
                                text = "Cancel",
                                selected = false,
                                onClick = { showDeleteConfirmPopup = false },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                NeoPillButton(
                                    text = "Cancel",
                                    selected = false,
                                    onClick = { showDeleteConfirmPopup = false },
                                    modifier = Modifier.weight(1f)
                                )
                                NeoPillButton(
                                    text = "Delete",
                                    icon = Icons.Default.Delete,
                                    selected = true,
                                    activeColor = NeoCoral,
                                    onClick = {
                                        showDeleteConfirmPopup = false
                                        onDeleteTask()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("confirm_delete_task_button")
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Clean Header Row: Icon + Metadata + Title on Left, Close (X) on Right
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = buildString {
                                    append("${task.folderName} • ${task.sectionName} • ${formatDurationBadge(task.durationMinutes)}")
                                    task.startTimeMinutes?.let {
                                        append(" • ${formatMinutesAsClock(it)}")
                                    }
                                },
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
                                color = InkMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 20.ssp
                                ),
                                color = InkBlack,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .smoothPressClickable(role = Role.Button, onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.sdp)
                                .clip(CircleShape)
                                .background(SoftSheetSurface)
                                .border(1.5.dp, InkBlack, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = InkBlack,
                                modifier = Modifier.size(16.sdp)
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

                // 2. Sleek 1-Row Structure Bar for Spine Tasks (Sequence Step # + Break Loop side-by-side!)
                if (task.modeEnum != TaskMode.TOP_BUBBLE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Compact Step # Stepper
                        Row(
                            modifier = Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(50))
                                .background(SoftSheetSurface)
                                .border(1.5.dp, InkBlack, RoundedCornerShape(50))
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .smoothPressClickable(role = Role.Button) {
                                        val next = (stepVal - 1).coerceAtLeast(1)
                                        stepVal = next
                                        onUpdateStepNumber(next)
                                    }
                                    .clip(CircleShape)
                                    .background(PureWhiteSurface)
                                    .border(1.2.dp, InkBlack, CircleShape)
                                    .testTag("step_number_minus"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease Step #",
                                    tint = InkBlack,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(NeoYellow)
                                    .border(1.2.dp, InkBlack, RoundedCornerShape(50))
                                    .padding(horizontal = 10.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "#$stepVal",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = InkBlack
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .smoothPressClickable(role = Role.Button) {
                                        val next = (stepVal + 1).coerceAtMost(99)
                                        stepVal = next
                                        onUpdateStepNumber(next)
                                    }
                                    .clip(CircleShape)
                                    .background(PureWhiteSurface)
                                    .border(1.2.dp, InkBlack, CircleShape)
                                    .testTag("step_number_plus"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase Step #",
                                    tint = InkBlack,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        // Right: 1-Tap Break Loop Split / Merge Pill
                        NeoPillButton(
                            text = if (hasBreakLoop) "Merge Loop" else "Split Break Loop",
                            icon = Icons.Default.Pause,
                            selected = hasBreakLoop,
                            activeColor = NeoSkyBlue,
                            onClick = onToggleBreakLoop,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 3. Subtasks Section (Smart: full checklist when subtasks exist, compact '+ Subtask' pill when 0 subtasks)
                if (item.subtasks.isNotEmpty() || showAddSubtaskField) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SoftSheetSurface)
                            .border(1.5.dp, InkBlack, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Subtasks (${item.completedSubtasksCount}/${item.subtasks.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = InkBlack
                            )
                            if ( !showAddSubtaskField ) {
                                Text(
                                    text = "+ Add",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = InkBlack,
                                    modifier = Modifier.smoothPressClickable { showAddSubtaskField = true }
                                )
                            }
                        }

                        item.sortedSubtasks.forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PureWhiteSurface)
                                    .smoothPressClickable { onToggleSubtask(sub) }
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
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

                        if (showAddSubtaskField) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newSubtask,
                                    onValueChange = { newSubtask = it },
                                    placeholder = {
                                        Text(
                                            "Add subtask...",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                                            color = InkMuted
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(PureWhiteSurface, RoundedCornerShape(12.dp))
                                )
                                NeoPillButton(
                                    text = "Add",
                                    selected = true,
                                    activeColor = NeoSkyBlue,
                                    onClick = {
                                        if (newSubtask.isNotBlank()) {
                                            onAddSubtask(newSubtask)
                                            newSubtask = ""
                                        }
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // When 0 subtasks exist, show a clean single-tap '+ Add Subtask' chip instead of an empty text box
                    NeoPillButton(
                        text = "+ Add Subtask",
                        icon = Icons.Default.Checklist,
                        selected = false,
                        onClick = { showAddSubtaskField = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 4. Primary Workflow Row: Focus Timer + Mark Done
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

                // 5. Cohesive Edit (Pencil) & Dedicated Delete (Trash) Row (Same architecture across all bubbles & tasks!)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeoPillButton(
                        text = "Edit",
                        icon = Icons.Default.Edit,
                        selected = true,
                        activeColor = NeoYellow,
                        onClick = onEditTask,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("popup_edit_task_button")
                    )
                    NeoPillButton(
                        text = "Delete",
                        icon = Icons.Default.Delete,
                        selected = true,
                        activeColor = NeoCoral,
                        onClick = { showDeleteConfirmPopup = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("popup_delete_task_button")
                    )
                }
            }
                }
            }
        }
    }
}

@Composable
fun DayThresholdMarkerRow(
    timeMinutes: Int,
    label: String,
    isStartOfDay: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .smoothPressClickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatMinutesAsClock(timeMinutes),
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 10.5.ssp,
                fontWeight = FontWeight.Medium
            ),
            color = InkMuted,
            modifier = Modifier.width(TIME_COLUMN_WIDTH)
        )
        Spacer(modifier = Modifier.width(TIME_TO_CAPSULE_GAP))
        Box(
            modifier = Modifier.width(CAPSULE_COLUMN_WIDTH),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.sdp)
                    .clip(CircleShape)
                    .background(if (isStartOfDay) NeoYellow else SoftSheetSurface, CircleShape)
                    .border(1.5.dp, InkBlack, CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 11.ssp,
                fontWeight = FontWeight.Medium
            ),
            color = InkMuted
        )
    }
}

