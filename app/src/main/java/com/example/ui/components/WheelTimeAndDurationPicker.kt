package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskMode
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.NeoCoral
import com.example.ui.theme.NeoSkyBlue
import com.example.ui.theme.NeoYellow
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.SoftSheetSurface
import java.util.Locale

private enum class TimedSelectorTab {
    START,
    END,
    DURATION
}

@Composable
internal fun NeoCompactMinutesRow(
    durationMinutes: Int,
    minAllowedDuration: Int,
    onDurationChange: (Int) -> Unit,
    containerColor: Color = PureWhiteSurface
) {
    var textValue by remember(durationMinutes) {
        mutableStateOf(durationMinutes.coerceAtLeast(minAllowedDuration).toString())
    }

    val shape = RoundedCornerShape(14.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .background(containerColor)
            .border(1.dp, InkBlack.copy(alpha = 0.12f), shape)
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepperIconCircle(
            icon = Icons.Default.Remove,
            contentDescription = "Decrease 5 Minutes",
            onClick = {
                val next = (durationMinutes - 5).coerceAtLeast(minAllowedDuration)
                onDurationChange(next)
                textValue = next.toString()
            },
            backgroundColor = if (containerColor == SoftSheetSurface) PureWhiteSurface else SoftSheetSurface
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
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
                    .width(52.dp)
                    .testTag("custom_minutes_input")
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "min",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = InkMuted,
                maxLines = 1
            )
        }

        StepperIconCircle(
            icon = Icons.Default.Add,
            contentDescription = "Increase 5 Minutes",
            onClick = {
                val next = (durationMinutes + 5).coerceAtMost(720)
                onDurationChange(next)
                textValue = next.toString()
            },
            backgroundColor = if (containerColor == SoftSheetSurface) PureWhiteSurface else SoftSheetSurface
        )
    }
}

@Composable
internal fun NeoCompactClockSelector(
    clockMinutes: Int,
    onClockChange: (Int) -> Unit
) {
    val use24h = LocalClockFormat.current.use24Hour
    val currentHour24 = (clockMinutes / 60).coerceIn(0, 23)
    val currentMinute = (clockMinutes % 60).coerceIn(0, 59)
    val isPm = currentHour24 >= 12
    val displayHour = if (use24h) {
        currentHour24
    } else {
        when (val h = currentHour24 % 12) {
            0 -> 12
            else -> h
        }
    }

    var hourText by remember(displayHour, use24h) {
        mutableStateOf(
            if (use24h) String.format(Locale.US, "%02d", displayHour)
            else displayHour.toString()
        )
    }
    var minText by remember(currentMinute) {
        mutableStateOf(String.format(Locale.US, "%02d", currentMinute))
    }

    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(PureWhiteSurface)
            .border(1.dp, InkBlack.copy(alpha = 0.08f), cardShape)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hour stepper
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StepperIconCircle(
                    icon = Icons.Default.Remove,
                    contentDescription = "Previous Hour",
                    onClick = {
                        val prevHr = if (currentHour24 == 0) 23 else currentHour24 - 1
                        onClockChange(prevHr * 60 + currentMinute)
                    }
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "HR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.6.sp
                        ),
                        color = InkMuted,
                        maxLines = 1
                    )
                    BasicTextField(
                        value = hourText,
                        onValueChange = { raw ->
                            val filtered = raw.filter { it.isDigit() }.take(2)
                            hourText = filtered
                            val h = filtered.toIntOrNull()
                            if (h != null) {
                                if (use24h && h in 0..23) {
                                    onClockChange(h * 60 + currentMinute)
                                } else if (!use24h && h in 1..12) {
                                    val h24 = when {
                                        isPm && h == 12 -> 12
                                        isPm -> h + 12
                                        h == 12 -> 0
                                        else -> h
                                    }
                                    onClockChange(h24 * 60 + currentMinute)
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = InkBlack,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(InkBlack),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                StepperIconCircle(
                    icon = Icons.Default.Add,
                    contentDescription = "Next Hour",
                    onClick = {
                        val nextHr = (currentHour24 + 1) % 24
                        onClockChange(nextHr * 60 + currentMinute)
                    }
                )
            }

            Text(
                text = ":",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = InkMuted,
                modifier = Modifier.padding(top = 12.dp)
            )

            // Minute stepper
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StepperIconCircle(
                    icon = Icons.Default.Remove,
                    contentDescription = "Decrease 5 Minutes",
                    onClick = {
                        onClockChange((clockMinutes - 5).coerceAtLeast(0))
                    }
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "MIN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.6.sp
                        ),
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
                                onClockChange(currentHour24 * 60 + m)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = InkBlack,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(InkBlack),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                StepperIconCircle(
                    icon = Icons.Default.Add,
                    contentDescription = "Increase 5 Minutes",
                    onClick = {
                        onClockChange((clockMinutes + 5).coerceAtMost(23 * 60 + 59))
                    }
                )
            }
        }

        if (!use24h) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SoftSheetSurface)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(false to "AM", true to "PM").forEach { (targetPm, label) ->
                    val selected = isPm == targetPm
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) PureWhiteSurface else Color.Transparent)
                            .then(
                                if (selected) Modifier.border(1.dp, InkBlack.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                else Modifier
                            )
                            .smoothPressClickable(role = Role.RadioButton) {
                                if (isPm != targetPm) {
                                    val toggledHour = if (targetPm) {
                                        (currentHour24 + 12).coerceAtMost(23)
                                    } else {
                                        (currentHour24 - 12).coerceAtLeast(0)
                                    }
                                    onClockChange(toggledHour * 60 + currentMinute)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selected) InkBlack else InkMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepperIconCircle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    backgroundColor: Color = SoftSheetSurface
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(backgroundColor, CircleShape)
            .smoothPressClickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = InkBlack,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun ModeSegmentItem(
    label: String,
    selected: Boolean,
    activeColor: Color,
    activeContentColor: Color = InkBlack,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) activeColor else Color.Transparent,
        label = "mode_seg_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) activeContentColor else InkMuted,
        label = "mode_seg_fg"
    )
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .background(bgColor)
            .then(
                if (selected) Modifier.border(1.5.dp, InkBlack, shape)
                else Modifier
            )
            .smoothPressClickable(role = Role.Tab, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TimedSummaryTab(
    caption: String,
    value: String,
    selected: Boolean,
    activeColor: Color,
    activeContentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) activeColor else PureWhiteSurface,
        label = "timed_tab_bg"
    )
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) InkBlack else InkBlack.copy(alpha = 0.08f),
                shape = shape
            )
            .smoothPressClickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = caption,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ),
            color = if (selected) activeContentColor.copy(alpha = 0.85f) else InkMuted,
            maxLines = 1
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (selected) activeContentColor else InkBlack,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PresetGridChip(
    label: String,
    selected: Boolean,
    activeColor: Color = NeoYellow,
    activeContentColor: Color = InkBlack,
    unselectedColor: Color = SoftSheetSurface,
    unselectedBorderAlpha: Float = 0f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) activeColor else unselectedColor,
        label = "preset_chip_bg"
    )
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .background(bgColor)
            .then(
                when {
                    selected -> Modifier.border(1.5.dp, InkBlack, shape)
                    unselectedBorderAlpha > 0f -> Modifier.border(
                        1.dp,
                        InkBlack.copy(alpha = unselectedBorderAlpha),
                        shape
                    )
                    else -> Modifier
                }
            )
            .smoothPressClickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (selected) activeContentColor else InkCharcoal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

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
    val clockConfig = LocalClockFormat.current
    val startOfDayMin = clockConfig.startOfDayMinutes
    val endOfDayMin = clockConfig.endOfDayMinutes
    val use24h = clockConfig.use24Hour

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

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Unified segmented mode switcher
        if (!toInbox) {
            val trackShape = RoundedCornerShape(16.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(trackShape)
                    .background(SoftSheetSurface)
                    .border(1.dp, InkBlack.copy(alpha = 0.08f), trackShape)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ModeSegmentItem(
                    label = "Timeless",
                    selected = taskMode == TaskMode.TIMELESS_STEP,
                    activeColor = NeoYellow,
                    activeContentColor = InkBlack,
                    testTag = "mode_segment_timeless",
                    onClick = {
                        if (taskMode == TaskMode.TIMED_BLOCK) {
                            onDurationChange(0)
                            showCustomTimelessInput = false
                        }
                        onModeChange(TaskMode.TIMELESS_STEP)
                    },
                    modifier = Modifier.weight(1f)
                )
                ModeSegmentItem(
                    label = "Timed",
                    selected = taskMode == TaskMode.TIMED_BLOCK,
                    activeColor = NeoCoral,
                    activeContentColor = PureWhiteSurface,
                    testTag = "mode_segment_timed",
                    onClick = {
                        if (durationMinutes <= 0) {
                            onDurationChange(60)
                        }
                        onModeChange(TaskMode.TIMED_BLOCK)
                    },
                    modifier = Modifier.weight(1f)
                )
                if (showBubbleOption) {
                    ModeSegmentItem(
                        label = "Bubble",
                        selected = taskMode == TaskMode.TOP_BUBBLE,
                        activeColor = NeoSkyBlue,
                        activeContentColor = InkBlack,
                        testTag = "mode_segment_bubble",
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
            val panelShape = RoundedCornerShape(18.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(panelShape)
                    .background(SoftSheetSurface)
                    .border(1.dp, InkBlack.copy(alpha = 0.08f), panelShape)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Summary selector strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimedSummaryTab(
                        caption = "START",
                        value = formatMinutesAsClock(startTimeMinutes, use24h),
                        selected = activeTimedTab == TimedSelectorTab.START,
                        activeColor = NeoCoral,
                        activeContentColor = PureWhiteSurface,
                        onClick = { activeTimedTab = TimedSelectorTab.START },
                        modifier = Modifier.weight(1f)
                    )
                    TimedSummaryTab(
                        caption = "END",
                        value = formatMinutesAsClock(calculatedEndMinutes, use24h),
                        selected = activeTimedTab == TimedSelectorTab.END,
                        activeColor = NeoCoral,
                        activeContentColor = PureWhiteSurface,
                        onClick = { activeTimedTab = TimedSelectorTab.END },
                        modifier = Modifier.weight(1f)
                    )
                    TimedSummaryTab(
                        caption = "DURATION",
                        value = formatDurationBadge(effectiveTimedDuration),
                        selected = activeTimedTab == TimedSelectorTab.DURATION,
                        activeColor = NeoYellow,
                        activeContentColor = InkBlack,
                        onClick = { activeTimedTab = TimedSelectorTab.DURATION },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (activeTimedTab == TimedSelectorTab.START || activeTimedTab == TimedSelectorTab.END) {
                    NeoCompactClockSelector(
                        clockMinutes = selectedClockMinutes,
                        onClockChange = applyClockUpdate
                    )
                } else {
                    val timedRow1 = remember { listOf(15 to "15m", 30 to "30m", 45 to "45m") }
                    val timedRow2 = remember { listOf(60 to "1h", 90 to "1.5h", 120 to "2h") }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            timedRow1.forEach { (mins, label) ->
                                PresetGridChip(
                                    label = label,
                                    selected = effectiveTimedDuration == mins,
                                    unselectedColor = PureWhiteSurface,
                                    unselectedBorderAlpha = 0.08f,
                                    onClick = { onDurationChange(mins) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            timedRow2.forEach { (mins, label) ->
                                PresetGridChip(
                                    label = label,
                                    selected = effectiveTimedDuration == mins,
                                    unselectedColor = PureWhiteSurface,
                                    unselectedBorderAlpha = 0.08f,
                                    onClick = { onDurationChange(mins) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    NeoCompactMinutesRow(
                        durationMinutes = effectiveTimedDuration,
                        minAllowedDuration = 5,
                        onDurationChange = onDurationChange,
                        containerColor = PureWhiteSurface
                    )
                }

                if (isBeforeStartOfDay || isPastEndOfDay) {
                    val noticeShape = RoundedCornerShape(12.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(noticeShape)
                            .background(NeoYellow.copy(alpha = 0.35f))
                            .border(1.dp, InkBlack.copy(alpha = 0.2f), noticeShape)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPastEndOfDay) {
                                "Past End of Day (${formatMinutesAsClock(endOfDayMin, use24h)})"
                            } else {
                                "Before Start (${formatMinutesAsClock(startOfDayMin, use24h)})"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = InkBlack,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(PureWhiteSurface)
                                .border(1.dp, InkBlack, RoundedCornerShape(50))
                                .smoothPressClickable(role = Role.Button) {
                                    val safeStart = startTimeMinutes.coerceIn(
                                        startOfDayMin,
                                        (endOfDayMin - 15).coerceAtLeast(startOfDayMin)
                                    )
                                    val maxDur = (endOfDayMin - safeStart).coerceAtLeast(5)
                                    onStartTimeChange(safeStart)
                                    onDurationChange(effectiveTimedDuration.coerceAtMost(maxDur))
                                }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "Fit",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = InkBlack
                            )
                        }
                    }
                }
            }
        } else {
            // Timeless / Inbox optional duration selector in a balanced 3x2 grid
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DURATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = InkMuted
                    )
                    Text(
                        text = if (durationMinutes > 0) formatDurationBadge(durationMinutes) else "Optional",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (durationMinutes > 0) InkBlack else InkMuted
                    )
                }

                val row1 = remember { listOf(0 to "None", 15 to "15m", 30 to "30m") }
                val row2Presets = remember { listOf(45 to "45m", 60 to "1h") }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row1.forEach { (mins, label) ->
                            val isSelected = !showCustomTimelessInput && durationMinutes == mins
                            PresetGridChip(
                                label = label,
                                selected = isSelected,
                                onClick = {
                                    showCustomTimelessInput = false
                                    onDurationChange(mins)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row2Presets.forEach { (mins, label) ->
                            val isSelected = !showCustomTimelessInput && durationMinutes == mins
                            PresetGridChip(
                                label = label,
                                selected = isSelected,
                                onClick = {
                                    showCustomTimelessInput = false
                                    onDurationChange(mins)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        PresetGridChip(
                            label = if (showCustomTimelessInput && durationMinutes > 0) {
                                formatDurationBadge(durationMinutes)
                            } else {
                                "Custom"
                            },
                            selected = showCustomTimelessInput,
                            activeColor = NeoCoral,
                            activeContentColor = PureWhiteSurface,
                            onClick = {
                                showCustomTimelessInput = true
                                if (durationMinutes == 0) {
                                    onDurationChange(25)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showCustomTimelessInput,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    NeoCompactMinutesRow(
                        durationMinutes = durationMinutes,
                        minAllowedDuration = 0,
                        onDurationChange = onDurationChange,
                        containerColor = SoftSheetSurface
                    )
                }
            }
        }
    }
}
