package com.example.ui.components

import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.data.SubtaskEntity
import com.example.data.TaskEntity
import com.example.data.TaskWithSubtasks
import com.example.ui.FocusTimerSnapshot
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkMuted
import com.example.ui.theme.NeoCoral
import com.example.ui.theme.NeoLavender
import com.example.ui.theme.NeoMintGreen
import com.example.ui.theme.NeoSkyBlue
import com.example.ui.theme.NeoYellow
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.SoftSheetSurface
import com.example.ui.theme.neoColorForKey
import com.example.ui.theme.sdp
import com.example.ui.theme.ssp
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt

private enum class ExpandedScheduleSetting {
    NONE,
    START_OF_DAY,
    END_OF_DAY
}

@Composable
fun StruktSettingsScreen(
    use24HourFormat: Boolean,
    startOfDayMinutes: Int,
    endOfDayMinutes: Int,
    onUpdateSettings: (use24Hour: Boolean, startOfDayMin: Int, endOfDayMin: Int) -> Unit,
    onOpenCleanupExport: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedRow by remember { mutableStateOf(ExpandedScheduleSetting.NONE) }
    val activeWindowHours = remember(startOfDayMinutes, endOfDayMinutes) {
        val diffMins = (endOfDayMinutes - startOfDayMinutes).coerceAtLeast(0)
        formatDurationBadge(diffMins)
    }

    DotGridBackground(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(42.sdp)
                        .clip(CircleShape)
                        .background(PureWhiteSurface, CircleShape)
                        .border(1.5.dp, InkBlack, CircleShape)
                        .smoothPressClickable(role = Role.Button, onClick = onDismiss)
                        .testTag("settings_back_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = InkBlack,
                        modifier = Modifier.size(20.sdp)
                    )
                }

                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = InkBlack
                )
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = InkBlack.copy(alpha = 0.08f)
            )

            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                SettingsSectionGroup(title = "DISPLAY & TIME") {
                    SettingsPreferenceRow(
                        icon = Icons.Default.Schedule,
                        iconBgColor = NeoYellow,
                        title = "24-Hour Clock",
                        subtitle = if (use24HourFormat) {
                            "Showing times as 14:30"
                        } else {
                            "Showing times as 2:30 PM"
                        },
                        onClick = {
                            onUpdateSettings(!use24HourFormat, startOfDayMinutes, endOfDayMinutes)
                        },
                        modifier = Modifier.testTag("settings_24h_toggle"),
                        trailingContent = {
                            NeoSettingsSwitch(
                                checked = use24HourFormat,
                                onCheckedChange = { enabled ->
                                    onUpdateSettings(enabled, startOfDayMinutes, endOfDayMinutes)
                                }
                            )
                        }
                    )
                }

                SettingsSectionGroup(
                    title = "DAILY SCHEDULE",
                    trailingHint = "Active window: $activeWindowHours"
                ) {
                    val isStartExpanded = expandedRow == ExpandedScheduleSetting.START_OF_DAY
                    val startChevronRotation by animateFloatAsState(
                        targetValue = if (isStartExpanded) 180f else 0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "start_chevron_rot"
                    )

                    SettingsPreferenceRow(
                        icon = Icons.Default.WbSunny,
                        iconBgColor = NeoMintGreen,
                        title = "Start of Day",
                        subtitle = "Wake-up boundary on timeline",
                        onClick = {
                            expandedRow = if (isStartExpanded) {
                                ExpandedScheduleSetting.NONE
                            } else {
                                ExpandedScheduleSetting.START_OF_DAY
                            }
                        },
                        modifier = Modifier.testTag("settings_start_of_day_row"),
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SettingsTimeValueBadge(
                                    text = formatMinutesAsClock(startOfDayMinutes, use24HourFormat)
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isStartExpanded) "Collapse" else "Expand",
                                    tint = InkMuted,
                                    modifier = Modifier
                                        .size(20.sdp)
                                        .rotate(startChevronRotation)
                                )
                            }
                        }
                    )

                    AnimatedVisibility(
                        visible = isStartExpanded,
                        enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                        exit = shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
                    ) {
                        SettingsTimeAdjustDrawer(
                            currentMinutes = startOfDayMinutes,
                            minMinutes = 4 * 60,
                            maxMinutes = (endOfDayMinutes - 60).coerceIn(5 * 60, 14 * 60),
                            use24HourFormat = use24HourFormat,
                            onChangeMinutes = { nextStart ->
                                val adjustedEnd = if (endOfDayMinutes <= nextStart + 60) {
                                    (nextStart + 12 * 60).coerceAtMost(23 * 60 + 45)
                                } else {
                                    endOfDayMinutes
                                }
                                onUpdateSettings(use24HourFormat, nextStart, adjustedEnd)
                            }
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 66.dp),
                        thickness = 1.dp,
                        color = InkBlack.copy(alpha = 0.09f)
                    )

                    val isEndExpanded = expandedRow == ExpandedScheduleSetting.END_OF_DAY
                    val endChevronRotation by animateFloatAsState(
                        targetValue = if (isEndExpanded) 180f else 0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "end_chevron_rot"
                    )

                    SettingsPreferenceRow(
                        icon = Icons.Default.NightsStay,
                        iconBgColor = NeoLavender,
                        title = "End of Day",
                        subtitle = "Wind-down cutoff for timed blocks",
                        onClick = {
                            expandedRow = if (isEndExpanded) {
                                ExpandedScheduleSetting.NONE
                            } else {
                                ExpandedScheduleSetting.END_OF_DAY
                            }
                        },
                        modifier = Modifier.testTag("settings_end_of_day_row"),
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SettingsTimeValueBadge(
                                    text = formatMinutesAsClock(endOfDayMinutes, use24HourFormat)
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isEndExpanded) "Collapse" else "Expand",
                                    tint = InkMuted,
                                    modifier = Modifier
                                        .size(20.sdp)
                                        .rotate(endChevronRotation)
                                )
                            }
                        }
                    )

                    AnimatedVisibility(
                        visible = isEndExpanded,
                        enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                        exit = shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
                    ) {
                        SettingsTimeAdjustDrawer(
                            currentMinutes = endOfDayMinutes,
                            minMinutes = (startOfDayMinutes + 60).coerceAtLeast(16 * 60),
                            maxMinutes = 23 * 60 + 59,
                            use24HourFormat = use24HourFormat,
                            onChangeMinutes = { nextEnd ->
                                onUpdateSettings(use24HourFormat, startOfDayMinutes, nextEnd)
                            }
                        )
                    }
                }

                SettingsSectionGroup(title = "DATA & STORAGE") {
                    SettingsPreferenceRow(
                        icon = Icons.Default.Description,
                        iconBgColor = NeoSkyBlue,
                        title = "Cleanup & Export",
                        subtitle = "Export PDF / CSV reports or purge done tasks",
                        onClick = onOpenCleanupExport,
                        modifier = Modifier.testTag("settings_open_cleanup_export_button"),
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Open Cleanup and Export",
                                tint = InkMuted,
                                modifier = Modifier.size(22.sdp)
                            )
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 66.dp),
                        thickness = 1.dp,
                        color = InkBlack.copy(alpha = 0.09f)
                    )

                    SettingsPreferenceRow(
                        icon = Icons.Default.Lock,
                        iconBgColor = SoftSheetSurface,
                        title = "On-Device Storage",
                        subtitle = "100% offline SQLite • Your data stays private",
                        onClick = null,
                        trailingContent = {}
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Strukt • Visual Day & Sequence Planner",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkMuted,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SettingsSectionGroup(
    title: String,
    trailingHint: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 6.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.9.sp
                ),
                color = InkMuted
            )
            if (trailingHint != null) {
                Text(
                    text = trailingHint,
                    style = MaterialTheme.typography.labelSmall,
                    color = InkMuted
                )
            }
        }

        NeoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = PureWhiteSurface,
            cornerRadius = 18.dp,
            borderWidth = 1.5.dp,
            contentPadding = PaddingValues(0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
        }
    }
}

@Composable
private fun SettingsPreferenceRow(
    icon: ImageVector,
    iconBgColor: Color,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    trailingContent: @Composable () -> Unit
) {
    val rowModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .smoothPressClickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    } else {
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.sdp)
                .clip(RoundedCornerShape(11.dp))
                .background(iconBgColor)
                .border(1.2.dp, InkBlack, RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = InkBlack,
                modifier = Modifier.size(19.sdp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = InkBlack
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted
            )
        }

        trailingContent()
    }
}

@Composable
private fun SettingsTimeValueBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SoftSheetSurface)
            .border(1.dp, InkBlack.copy(alpha = 0.22f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum"
            ),
            color = InkBlack
        )
    }
}

@Composable
private fun NeoSettingsSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) NeoMintGreen else SoftSheetSurface,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "switch_track_color"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 2.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "switch_thumb_offset"
    )

    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .smoothPressClickable(role = Role.Switch) { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(50))
                .background(trackColor, RoundedCornerShape(50))
                .border(1.5.dp, InkBlack, RoundedCornerShape(50)),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(PureWhiteSurface, CircleShape)
                    .border(1.5.dp, InkBlack, CircleShape)
            )
        }
    }
}

@Composable
private fun SettingsTimeAdjustDrawer(
    currentMinutes: Int,
    minMinutes: Int,
    maxMinutes: Int,
    use24HourFormat: Boolean,
    onChangeMinutes: (Int) -> Unit
) {
    val sliderMin = minMinutes.toFloat()
    val sliderMax = maxMinutes.coerceAtLeast(minMinutes + 15).toFloat()
    val sliderVal = currentMinutes.toFloat().coerceIn(sliderMin, sliderMax)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SoftSheetSurface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .height(36.sdp)
                    .clip(RoundedCornerShape(50))
                    .background(PureWhiteSurface, RoundedCornerShape(50))
                    .border(1.2.dp, InkBlack, RoundedCornerShape(50))
                    .smoothPressClickable(role = Role.Button) {
                        val next = (currentMinutes - 15).coerceAtLeast(minMinutes)
                        onChangeMinutes(next)
                    }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease 15 minutes",
                        tint = InkBlack,
                        modifier = Modifier.size(14.sdp)
                    )
                    Text(
                        text = "15m",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = InkBlack
                    )
                }
            }

            Text(
                text = formatMinutesAsClock(currentMinutes, use24HourFormat),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum"
                ),
                color = InkBlack
            )

            Box(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .height(36.sdp)
                    .clip(RoundedCornerShape(50))
                    .background(PureWhiteSurface, RoundedCornerShape(50))
                    .border(1.2.dp, InkBlack, RoundedCornerShape(50))
                    .smoothPressClickable(role = Role.Button) {
                        val next = (currentMinutes + 15).coerceAtMost(maxMinutes)
                        onChangeMinutes(next)
                    }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase 15 minutes",
                        tint = InkBlack,
                        modifier = Modifier.size(14.sdp)
                    )
                    Text(
                        text = "15m",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = InkBlack
                    )
                }
            }
        }

        Slider(
            value = sliderVal,
            onValueChange = { raw ->
                val snapped = ((raw / 15f).roundToInt() * 15).coerceIn(minMinutes, maxMinutes)
                if (snapped != currentMinutes) {
                    onChangeMinutes(snapped)
                }
            },
            valueRange = sliderMin..sliderMax,
            colors = SliderDefaults.colors(
                thumbColor = InkBlack,
                activeTrackColor = InkBlack,
                inactiveTrackColor = InkBlack.copy(alpha = 0.16f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatMinutesAsClock(minMinutes, use24HourFormat),
                style = MaterialTheme.typography.labelSmall,
                color = InkMuted
            )
            Text(
                text = formatMinutesAsClock(maxMinutes, use24HourFormat),
                style = MaterialTheme.typography.labelSmall,
                color = InkMuted
            )
        }
    }
}

@Composable
fun OverlapConflictModal(
    newTaskTitle: String,
    conflictingTaskTitle: String,
    requestedStartMin: Int,
    requestedEndMin: Int,
    suggestedShiftStartMin: Int,
    suggestedShiftEndMin: Int,
    onAutoShift: () -> Unit,
    onAllowSimultaneous: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        NeoCard(
            modifier = Modifier
                .widthIn(max = 420.dp)
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
                    Text(
                        text = "Time Slot Overlap",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = InkBlack
                    )
                    NeoCircleCloseButton(
                        onClick = onDismiss,
                        size = 32.dp
                    )
                }

                val use24h = LocalClockFormat.current.use24Hour
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SoftSheetSurface)
                        .border(1.5.dp, InkBlack, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "\"$newTaskTitle\" (${formatMinutesAsClock(requestedStartMin, use24h)}–${formatMinutesAsClock(requestedEndMin, use24h)}) overlaps with \"$conflictingTaskTitle\".",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = InkBlack
                    )
                }

                NeoPillButton(
                    text = "Auto-Shift (${formatMinutesAsClock(suggestedShiftStartMin, use24h)}–${formatMinutesAsClock(suggestedShiftEndMin, use24h)})",
                    selected = true,
                    activeColor = NeoMintGreen,
                    onClick = onAutoShift,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("overlap_auto_shift_button")
                )

                NeoPillButton(
                    text = "Allow Simultaneous",
                    selected = true,
                    activeColor = NeoYellow,
                    onClick = onAllowSimultaneous,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("overlap_allow_simultaneous_button")
                )

                NeoPillButton(
                    text = "Cancel",
                    selected = false,
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun AntiParalysisFocusScreen(
    focusItem: TaskWithSubtasks?,
    timerSnapshot: FocusTimerSnapshot = FocusTimerSnapshot(),
    totalDayTasksCount: Int = 0,
    completedDayTasksCount: Int = 0,
    onBackToFlow: () -> Unit,
    onAddTaskToFlow: () -> Unit = {},
    onStartOrResumeSession: (TaskEntity) -> Unit,
    onPauseSession: (TaskEntity) -> Unit,
    onResetTimer: (TaskEntity) -> Unit = {},
    onExtendDurationMinutes: (TaskEntity, Int) -> Unit = { _, _ -> },
    onToggleSubtask: (SubtaskEntity) -> Unit,
    onAddQuickSubtask: (Long, String) -> Unit,
    onCompleteAndAdvance: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (focusItem == null) {
        val hasNoTasksAtAll = totalDayTasksCount == 0
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            NeoCard(
                modifier = Modifier.widthIn(max = 420.dp),
                backgroundColor = if (hasNoTasksAtAll) NeoYellow else NeoMintGreen,
                cornerRadius = 22.dp,
                contentPadding = PaddingValues(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (hasNoTasksAtAll) "No Tasks Scheduled" else "All Done",
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                        color = InkBlack
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (hasNoTasksAtAll) {
                            "Schedule a task on the timeline to begin focusing."
                        } else {
                            "$completedDayTasksCount of $totalDayTasksCount tasks completed for this day."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkBlack.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (hasNoTasksAtAll) {
                            NeoPillButton(
                                text = "+ Add Task",
                                selected = true,
                                activeColor = NeoCoral,
                                onClick = onAddTaskToFlow
                            )
                        }
                        NeoPillButton(
                            text = "Timeline",
                            selected = true,
                            activeColor = PureWhiteSurface,
                            onClick = onBackToFlow
                        )
                    }
                }
            }
        }
        return
    }

    val task = focusItem.task
    val accent = neoColorForKey(task.colorKey)
    val isStopwatch = task.durationMinutes <= 0
    val targetSeconds = (task.durationMinutes * 60L).coerceAtLeast(60L)
    var isRunning by remember(task.id, task.activeSessionStartMin) {
        mutableStateOf(task.activeSessionStartMin != null)
    }

    val use24h = LocalClockFormat.current.use24Hour
    val recordedIntervals = remember(task.parsedSessionSegments, task.activeSessionStartMin, use24h) {
        buildList {
            val segs = task.parsedSessionSegments
            segs.forEachIndexed { idx, (s, e) ->
                if (idx > 0) {
                    val prevEnd = segs[idx - 1].second
                    val breakMins = (s - prevEnd).coerceAtLeast(1)
                    add("Break • ${formatDurationBadge(breakMins)} (${formatMinutesAsClock(prevEnd, use24h)} – ${formatMinutesAsClock(s, use24h)})")
                }
                val workMins = (e - s).coerceAtLeast(1)
                val label = if (idx == 0) "Started" else "Resumed"
                add("$label  ${formatMinutesAsClock(s, use24h)} – ${formatMinutesAsClock(e, use24h)} (${formatDurationBadge(workMins)})")
            }
            task.activeSessionStartMin?.let { activeStart ->
                if (segs.isNotEmpty()) {
                    val prevEnd = segs.last().second
                    val breakMins = (activeStart - prevEnd).coerceAtLeast(1)
                    add("Break • ${formatDurationBadge(breakMins)} (${formatMinutesAsClock(prevEnd, use24h)} – ${formatMinutesAsClock(activeStart, use24h)})")
                }
                val label = if (segs.isEmpty()) "Started" else "Resumed"
                add("$label  ${formatMinutesAsClock(activeStart, use24h)} – Active")
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 12.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

            Column(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FocusHeroAndTimerCard(
                    task = task,
                    accent = accent,
                    isStopwatch = isStopwatch,
                    targetSeconds = targetSeconds,
                    isRunning = isRunning,
                    timerSnapshot = timerSnapshot,
                    onTogglePlayPause = {
                        if (isRunning) {
                            isRunning = false
                            onPauseSession(task)
                        } else {
                            isRunning = true
                            onStartOrResumeSession(task)
                        }
                    },
                    onResetTimer = { onResetTimer(task) },
                    onExtendDurationMinutes = { deltaMins ->
                        onExtendDurationMinutes(task, deltaMins)
                    },
                    onCompleteAndAdvance = onCompleteAndAdvance
                )

                FocusSubtasksAndSessionCard(
                    focusItem = focusItem,
                    recordedIntervals = recordedIntervals,
                    onToggleSubtask = onToggleSubtask,
                    onAddQuickSubtask = { title ->
                        onAddQuickSubtask(task.id, title)
                    }
                )
            }
        }
    }
}

@Composable
private fun FocusHeroAndTimerCard(
    task: TaskEntity,
    accent: Color,
    isStopwatch: Boolean,
    targetSeconds: Long,
    isRunning: Boolean,
    timerSnapshot: FocusTimerSnapshot,
    onTogglePlayPause: () -> Unit,
    onResetTimer: () -> Unit,
    onExtendDurationMinutes: (Int) -> Unit,
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
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 31.ssp
                ),
                color = InkBlack,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            FocusLiveTimerReadoutCard(
                task = task,
                isStopwatch = isStopwatch,
                targetSeconds = targetSeconds,
                isRunning = isRunning,
                timerSnapshot = timerSnapshot,
                onTogglePlayPause = onTogglePlayPause,
                onResetTimer = onResetTimer,
                onExtendDurationMinutes = onExtendDurationMinutes
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.sdp)
                    .smoothPressClickable(role = Role.Button, onClick = onCompleteAndAdvance)
                    .clip(RoundedCornerShape(16.sdp))
                    .background(NeoMintGreen)
                    .border(1.5.dp, InkBlack, RoundedCornerShape(16.sdp))
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
private fun FocusLiveTimerReadoutCard(
    task: TaskEntity,
    isStopwatch: Boolean,
    targetSeconds: Long,
    isRunning: Boolean,
    timerSnapshot: FocusTimerSnapshot,
    onTogglePlayPause: () -> Unit,
    onResetTimer: () -> Unit,
    onExtendDurationMinutes: (Int) -> Unit
) {
    var localResetGeneration by remember(task.id) { mutableIntStateOf(0) }

    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = PureWhiteSurface,
        cornerRadius = 20.dp,
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(SoftSheetSurface)
                    .border(1.dp, InkBlack.copy(alpha = 0.25f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (isStopwatch) {
                        "STOPWATCH • COUNTING UP"
                    } else {
                        "COUNTDOWN • TARGET ${formatDurationBadge(task.durationMinutes).uppercase()}"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = InkMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            FocusLiveClockAndProgressLeaf(
                taskId = task.id,
                isStopwatch = isStopwatch,
                targetSeconds = targetSeconds,
                isRunning = isRunning,
                timerSnapshot = timerSnapshot,
                localResetGeneration = localResetGeneration
            )

            if (!isStopwatch) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NeoPillButton(
                        text = "+1m",
                        selected = false,
                        onClick = { onExtendDurationMinutes(1) },
                        modifier = Modifier.testTag("focus_extend_1m_button")
                    )
                    NeoPillButton(
                        text = "+5m",
                        selected = false,
                        onClick = { onExtendDurationMinutes(5) },
                        modifier = Modifier.testTag("focus_extend_5m_button")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NeoPillButton(
                    text = when {
                        isRunning -> "Pause (Break)"
                        task.parsedSessionSegments.isNotEmpty() || timerSnapshot.accumulatedSeconds > 0L -> "Resume"
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
                    onClick = {
                        localResetGeneration++
                        onResetTimer()
                    }
                )
            }
        }
    }
}

@Composable
private fun FocusLiveClockAndProgressLeaf(
    taskId: Long,
    isStopwatch: Boolean,
    targetSeconds: Long,
    isRunning: Boolean,
    timerSnapshot: FocusTimerSnapshot,
    localResetGeneration: Int
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var fallbackAccumulatedSeconds by remember(taskId, localResetGeneration) {
        mutableLongStateOf(timerSnapshot.accumulatedSeconds)
    }
    var fallbackAnchorRealtimeMs by remember(taskId, localResetGeneration) {
        mutableLongStateOf(
            when {
                timerSnapshot.activeRunAnchorRealtimeMs != 0L -> timerSnapshot.activeRunAnchorRealtimeMs
                isRunning -> SystemClock.elapsedRealtime()
                else -> 0L
            }
        )
    }

    val effectiveAccumulatedSeconds = if (timerSnapshot.accumulatedSeconds > 0L || localResetGeneration > 0) {
        timerSnapshot.accumulatedSeconds
    } else {
        fallbackAccumulatedSeconds
    }

    val effectiveAnchorMs = when {
        !isRunning -> 0L
        timerSnapshot.activeRunAnchorRealtimeMs != 0L -> timerSnapshot.activeRunAnchorRealtimeMs
        fallbackAnchorRealtimeMs != 0L -> fallbackAnchorRealtimeMs
        else -> SystemClock.elapsedRealtime()
    }

    var displayElapsedSeconds by remember(taskId, localResetGeneration) {
        val initialDelta = if (isRunning && effectiveAnchorMs != 0L) {
            ((SystemClock.elapsedRealtime() - effectiveAnchorMs) / 1000L).coerceAtLeast(0L)
        } else {
            0L
        }
        mutableLongStateOf(effectiveAccumulatedSeconds + initialDelta)
    }

    LaunchedEffect(
        taskId,
        isRunning,
        effectiveAccumulatedSeconds,
        effectiveAnchorMs,
        localResetGeneration,
        lifecycleOwner
    ) {
        if (isRunning) {
            val anchor = if (effectiveAnchorMs != 0L) effectiveAnchorMs else SystemClock.elapsedRealtime()
            fallbackAnchorRealtimeMs = anchor
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    val deltaSec = ((SystemClock.elapsedRealtime() - anchor) / 1000L).coerceAtLeast(0L)
                    displayElapsedSeconds = effectiveAccumulatedSeconds + deltaSec
                    delay(1000L)
                }
            }
        } else {
            if (fallbackAnchorRealtimeMs != 0L && timerSnapshot.accumulatedSeconds == 0L) {
                val deltaSec = ((SystemClock.elapsedRealtime() - fallbackAnchorRealtimeMs) / 1000L).coerceAtLeast(0L)
                fallbackAccumulatedSeconds += deltaSec
                fallbackAnchorRealtimeMs = 0L
            }
            displayElapsedSeconds = if (timerSnapshot.accumulatedSeconds > 0L) {
                timerSnapshot.accumulatedSeconds
            } else {
                fallbackAccumulatedSeconds
            }
        }
    }

    val shownSeconds = if (isStopwatch) {
        displayElapsedSeconds
    } else {
        (targetSeconds - displayElapsedSeconds).coerceAtLeast(0L)
    }
    val mins = shownSeconds / 60L
    val secs = shownSeconds % 60L
    val progress = if (isStopwatch) {
        ((displayElapsedSeconds % 1500L).toFloat() / 1500f).coerceIn(0f, 1f)
    } else {
        (displayElapsedSeconds.toFloat() / targetSeconds.toFloat()).coerceIn(0f, 1f)
    }

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
}

@Composable
private fun FocusSubtasksAndSessionCard(
    focusItem: TaskWithSubtasks,
    recordedIntervals: List<String>,
    onToggleSubtask: (SubtaskEntity) -> Unit,
    onAddQuickSubtask: (String) -> Unit
) {
    val task = focusItem.task
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        NeoSubtaskChecklistSection(
            subtasks = focusItem.sortedSubtasks,
            completedCount = focusItem.completedSubtasksCount,
            onToggleSubtask = onToggleSubtask,
            onAddSubtask = onAddQuickSubtask,
            alwaysExpanded = true
        )

        if (recordedIntervals.isNotEmpty()) {
            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = PureWhiteSurface,
                cornerRadius = 22.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Session Interval Log",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = InkBlack
                        )
                        if (task.parsedSessionSegments.isNotEmpty()) {
                            val workedLabel = formatDurationBadge(task.totalWorkedMinutes.coerceAtLeast(1))
                            val breakLabel = formatDurationBadge(task.totalBreakMinutes)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(NeoYellow)
                                    .border(1.dp, InkBlack, RoundedCornerShape(50))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (task.totalBreakMinutes > 0) {
                                        "Worked $workedLabel • Break $breakLabel"
                                    } else {
                                        "Worked $workedLabel"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = InkBlack
                                )
                            }
                        }
                    }
                    recordedIntervals.forEachIndexed { idx, intervalText ->
                        val isBreakRow = intervalText.startsWith("Break")
                        if (idx > 0 && !isBreakRow) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 10.dp)
                            ) {
                                SpineConnectorSegment(
                                    isDottedBreak = true,
                                    height = 14.dp
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isBreakRow) NeoYellow.copy(alpha = 0.35f) else SoftSheetSurface)
                                .border(
                                    width = if (isBreakRow) 1.dp else 0.dp,
                                    color = if (isBreakRow) InkBlack.copy(alpha = 0.3f) else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = intervalText,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (isBreakRow) FontWeight.SemiBold else FontWeight.Medium
                                ),
                                color = InkBlack
                            )
                        }
                    }
                }
            }
        }
    }
}
