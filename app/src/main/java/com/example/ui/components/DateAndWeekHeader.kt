package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.TaskWithSubtasks
import com.example.ui.FlowFilterMode
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkMuted
import com.example.ui.theme.NeoCoral
import com.example.ui.theme.NeoYellow
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.SoftSheetSurface
import com.example.ui.theme.neoColorForKey
import com.example.ui.theme.sdp
import com.example.ui.theme.ssp
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@Immutable
data class ClockFormatConfig(
    val use24Hour: Boolean = true,
    val startOfDayMinutes: Int = 8 * 60,
    val endOfDayMinutes: Int = 22 * 60
)

val LocalClockFormat = compositionLocalOf { ClockFormatConfig() }

fun formatMinutesAsClock(
    minutes: Int,
    use24Hour: Boolean = true
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

@Composable
fun TopAnytimeBubblesRow(
    bubbles: List<TaskWithSubtasks>,
    onToggleBubbleDone: (TaskWithSubtasks) -> Unit,
    onInspectBubble: (TaskWithSubtasks) -> Unit,
    isMultiSelectActive: Boolean = false,
    selectedTaskIds: Set<Long> = emptySet(),
    onToggleSelectTask: (Long) -> Unit = {},
    onEnterMultiSelectWithTask: (Long) -> Unit = {},
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
            val isSelected = task.id in selectedTaskIds
            val baseColor = neoColorForKey(task.colorKey)
            val animatedBubbleFill by animateColorAsState(
                targetValue = when {
                    isSelected -> NeoYellow
                    task.isCompleted -> SoftSheetSurface
                    else -> baseColor
                },
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
                            onLongClick = { onEnterMultiSelectWithTask(task.id) },
                            onClick = {
                                if (isMultiSelectActive) {
                                    onToggleSelectTask(task.id)
                                } else {
                                    onToggleBubbleDone(item)
                                }
                            }
                        )
                        .clip(CircleShape)
                        .background(animatedBubbleFill, CircleShape)
                        .border(if (isSelected) 2.2.dp else 1.5.dp, InkBlack, CircleShape)
                        .testTag("top_bubble_${task.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSelected || task.isCompleted) Icons.Default.Check else iconForKey(task.iconKey),
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
                        if (isMultiSelectActive) {
                            onToggleSelectTask(task.id)
                        } else {
                            onInspectBubble(item)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FlowStructureFilterBar(
    currentFilter: FlowFilterMode,
    onSelectFilter: (FlowFilterMode) -> Unit,
    isMultiSelectActive: Boolean = false,
    onToggleMultiSelectMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
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

        NeoPillButton(
            text = if (isMultiSelectActive) "Done" else "Select",
            icon = Icons.Default.Checklist,
            selected = isMultiSelectActive,
            activeColor = NeoYellow,
            onClick = onToggleMultiSelectMode,
            modifier = Modifier.testTag("timeline_multi_select_toggle")
        )
    }
}
