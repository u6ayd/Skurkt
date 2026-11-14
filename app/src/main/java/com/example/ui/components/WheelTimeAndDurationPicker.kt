package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.TaskMode
import com.example.ui.theme.InkBlack
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
internal fun NeoCompactClockSelector(
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
                    .smoothPressClickable(role = Role.Button) {
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
                    .smoothPressClickable(role = Role.Button) {
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
                    .smoothPressClickable(role = Role.Button) {
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
                    .smoothPressClickable(role = Role.Button) {
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
