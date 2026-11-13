package com.example.ui.components

import android.os.SystemClock
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.data.FolderEntity
import com.example.data.RecurrenceRule
import com.example.data.SubtaskEntity
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
import com.example.ui.theme.sdp
import com.example.ui.theme.ssp
import kotlinx.coroutines.delay
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

/**
 * Single-Window Global Settings Popup Modal:
 * - Global Time Format: 24-Hour (14:30) vs 12-Hour (2:30 PM) applied across all timeline labels, pickers, and logs.
 * - Start-of-Day (Wake-Up) & End-of-Day (Wind-Down) thresholds enforced when scheduling tasks.
 */
@Composable
fun GlobalSettingsPopupModal(
    use24HourFormat: Boolean,
    startOfDayMinutes: Int,
    endOfDayMinutes: Int,
    onUpdateSettings: (use24Hour: Boolean, startOfDayMin: Int, endOfDayMin: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var localUse24h by remember(use24HourFormat) { mutableStateOf(use24HourFormat) }
    var localStartMin by remember(startOfDayMinutes) { mutableIntStateOf(startOfDayMinutes) }
    var localEndMin by remember(endOfDayMinutes) { mutableIntStateOf(endOfDayMinutes) }

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
            cornerRadius = 24.dp,
            contentPadding = PaddingValues(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(NeoYellow, CircleShape)
                                .border(1.5.dp, InkBlack, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = InkBlack,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Planner Settings",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = InkBlack
                        )
                    }

                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SoftSheetSurface, CircleShape)
                            .border(1.5.dp, InkBlack, CircleShape)
                            .smoothPressClickable(role = Role.Button, onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Settings",
                            tint = InkBlack,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // 1. Global Clock Format (24-Hour vs 12-Hour AM/PM)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SoftSheetSurface)
                        .border(1.5.dp, InkBlack, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Global Time Format",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = InkBlack
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NeoPillButton(
                            text = "24-Hour (14:30)",
                            selected = localUse24h,
                            activeColor = NeoYellow,
                            onClick = { localUse24h = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_24h_button")
                        )
                        NeoPillButton(
                            text = "12-Hour (2:30 PM)",
                            selected = !localUse24h,
                            activeColor = NeoYellow,
                            onClick = { localUse24h = false },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_12h_button")
                        )
                    }
                }

                // 2. Start of Day (Wake-Up Threshold)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SoftSheetSurface)
                        .border(1.5.dp, InkBlack, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Start of Day (Wake-Up)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = InkBlack
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(NeoMintGreen)
                                .border(1.5.dp, InkBlack, RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = formatMinutesAsClock(localStartMin, localUse24h),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = InkBlack
                            )
                        }
                    }

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(5 * 60, 6 * 60, 7 * 60, 8 * 60, 9 * 60, 10 * 60).forEach { presetMin ->
                            NeoPillButton(
                                text = formatMinutesAsClock(presetMin, localUse24h),
                                selected = localStartMin == presetMin,
                                activeColor = NeoMintGreen,
                                onClick = {
                                    localStartMin = presetMin
                                    if (localEndMin <= localStartMin + 60) {
                                        localEndMin = (localStartMin + 12 * 60).coerceAtMost(23 * 60 + 45)
                                    }
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NeoPillButton(
                            text = "-15m",
                            onClick = { localStartMin = (localStartMin - 15).coerceAtLeast(0) }
                        )
                        Text(
                            text = "Wake-Up Threshold",
                            style = MaterialTheme.typography.labelMedium,
                            color = InkMuted
                        )
                        NeoPillButton(
                            text = "+15m",
                            onClick = {
                                localStartMin = (localStartMin + 15).coerceAtMost((localEndMin - 60).coerceAtLeast(0))
                            }
                        )
                    }
                }

                // 3. End of Day (Wind-Down Threshold)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SoftSheetSurface)
                        .border(1.5.dp, InkBlack, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "End of Day (Wind-Down)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = InkBlack
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(NeoCoral)
                                .border(1.5.dp, InkBlack, RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = formatMinutesAsClock(localEndMin, localUse24h),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = PureWhiteSurface
                            )
                        }
                    }

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(20 * 60, 21 * 60, 22 * 60, 23 * 60, 23 * 60 + 30, 23 * 60 + 59).forEach { presetMin ->
                            NeoPillButton(
                                text = formatMinutesAsClock(presetMin, localUse24h),
                                selected = localEndMin == presetMin,
                                activeColor = NeoCoral,
                                onClick = {
                                    if (presetMin > localStartMin + 30) {
                                        localEndMin = presetMin
                                    }
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NeoPillButton(
                            text = "-15m",
                            onClick = {
                                localEndMin = (localEndMin - 15).coerceAtLeast(localStartMin + 60)
                            }
                        )
                        Text(
                            text = "Blocks tasks past End of Day",
                            style = MaterialTheme.typography.labelMedium,
                            color = InkMuted
                        )
                        NeoPillButton(
                            text = "+15m",
                            onClick = {
                                localEndMin = (localEndMin + 15).coerceAtMost(23 * 60 + 59)
                            }
                        )
                    }
                }

                NeoPillButton(
                    text = "Save Settings",
                    selected = true,
                    activeColor = NeoMintGreen,
                    onClick = {
                        onUpdateSettings(localUse24h, localStartMin, localEndMin)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_settings_button")
                )
            }
        }
    }
}

/**
 * Clean Neo-Brutalist Single-Line Custom Minutes Row:
 * - Replaces the tall Material OutlinedTextField and its 2-line wrapping floating label.
 * - Renders a compact Neo-Brutalist bordered box with a short single-line "Minutes" label + numeric input,
 *   flanked by `-5m` and `+5m` buttons on the same single line.
 */
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

/**
 * Clean, Compact Centered `Hour : Minute` Selector using Neo-Brutalist bordered boxes (zero OutlinedTextFields).
 */
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
        // Hour Neo-Brutalist Box (50% width via weight(1f) so HR and MIN are always symmetrical)
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

        // Minute Neo-Brutalist Box (50% width via weight(1f) so MIN never clips)
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

/**
 * Decluttered, Streamlined Time & Duration Picker Section:
 * 1. Timeless / Inbox / Bubble Mode:
 *    - Shows ONLY a single row of duration pills: `None (0m)` | `15m` | `30m` | `45m` | `1h` | `Custom`.
 *    - Defaults to `None (0m)` for timeless tasks.
 *    - Hides the custom minutes input field and `-5m / +5m` buttons by default; ONLY reveals a compact
 *      single-line `"Minutes"` row if the user taps `Custom`.
 * 2. Timed Mode:
 *    - Displays 3 compact selector cards in a single row at the top of the Timed box:
 *      `[ START: 09:00 ]`   `[ END: 10:00 ]`   `[ DURATION: 1h ]`
 *    - Shows ONLY the controls for the currently selected card (never stacks Start Time and Duration pickers):
 *      - When `START` or `END` is active: shows ONLY a clean centered `Hour : Minute` Neo-Brutalist selector
 *        plus one row of quick nudge pills (`-15m`, `-5m`, `+5m`, `+15m`).
 *      - When `DURATION` is active: shows ONLY the preset duration pills (`15m`, `30m`, `45m`, `1h`, `1.5h`, `2h`)
 *        and a single compact row with the `"Minutes"` input box + `-5m` / `+5m` buttons.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UnifiedTimeAndDurationPickerSection(
    toInbox: Boolean,
    taskMode: TaskMode,
    stepOrder: Int,
    startTimeMinutes: Int,
    durationMinutes: Int,
    showBubbleOption: Boolean = true,
    onModeChange: (TaskMode) -> Unit,
    onStepOrderChange: (Int) -> Unit = {},
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
            // 2. SIMPLIFIED "TIMED" MODE: 3 Compact Selector Cards in a Single Row
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
                // Single Row of 3 Compact Selector Cards: [ START: 09:00 ] [ END: 10:00 ] [ DURATION: 1h ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val startSelected = activeTimedTab == TimedSelectorTab.START
                    val endSelected = activeTimedTab == TimedSelectorTab.END
                    val durSelected = activeTimedTab == TimedSelectorTab.DURATION

                    // Card 1: START
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

                    // Card 2: END
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

                    // Card 3: DURATION
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

                // Controls ONLY for the currently active tab (never stacked)
                if (activeTimedTab == TimedSelectorTab.START || activeTimedTab == TimedSelectorTab.END) {
                    // Clean, compact centered Hour : Minute selector
                    NeoCompactClockSelector(
                        clockMinutes = selectedClockMinutes,
                        onClockChange = applyClockUpdate
                    )

                    // Single row of quick nudge pills (-15m, -5m, +5m, +15m)
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
                    // When DURATION is active: preset duration pills + single compact Minutes row
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

                // Start-of-Day / End-of-Day Threshold Inline Notice & 1-Tap Clamp
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
            // 1. SIMPLIFIED "TIMELESS" MODE (No Wall of Boxes):
            // Single row of duration pills: None (0m) | 15m | 30m | 45m | 1h | Custom
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

            // ONLY reveal the compact single-line custom minutes row if the user tapped Custom
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

/**
 * Single-Window Structured Task Editor Dialog:
 * - Zero nested sub-dialogs ("popup within a popup" eliminated): tapping Time & Duration, Color & Icon,
 *   Folder, Repeat, or Subtasks transitions smoothly in-place inside the same NeoCard via AnimatedContent!
 * - Clicking outside the dialog card dismisses it immediately.
 * - Top-right Move to Inbox & Delete buttons have generous spacing and zero overlap.
 */
@Composable
fun StructuredTaskEditorDialog(
    selectedDate: LocalDate,
    folders: List<FolderEntity>,
    editingItem: TaskWithSubtasks?,
    defaultMode: TaskMode,
    defaultToInbox: Boolean,
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
    var colorKey by remember(existingTask) { mutableStateOf(existingTask?.colorKey ?: "SAGE") }
    var iconKey by remember(existingTask) { mutableStateOf(existingTask?.iconKey ?: "BOOK") }
    var folderName by remember(existingTask, folders) {
        mutableStateOf(existingTask?.folderName ?: (folders.firstOrNull()?.name ?: "Physics"))
    }
    var sectionName by remember(existingTask, folders) {
        val defaultSec = folders.firstOrNull { it.name == (existingTask?.folderName ?: folders.firstOrNull()?.name) }
            ?.sections?.firstOrNull() ?: "Core"
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

        // Full-screen backdrop: clicking outside the NeoCard dismisses the dialog immediately
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
                    ) { /* Consume inside clicks so backdrop doesn't dismiss */ },
                backgroundColor = PaperCanvas,
                cornerRadius = 28.dp,
                shadowOffset = 5.dp,
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
                                // 1. Colored Top Header with non-overlapping Close, Move to Inbox, and Delete controls
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
                                        // Tapping the large circular icon opens the in-place Color & Icon view
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

                                // 2. Clean Summary Rows (Each transitions in-place inside this same window!)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Destination Toggle Row (Date Flow vs Inbox)
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

                                    // Row 1: Time & Duration In-Place Trigger
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

                                    // Row 2: Color & Category Icon In-Place Trigger
                                    EditorSummaryRow(
                                        icon = Icons.Default.Palette,
                                        label = "Color & Icon",
                                        valueText = NeoPalette.firstOrNull { it.key == colorKey }?.label ?: "Color",
                                        onClick = { activeSubPopup = EditorSubPopup.COLOR_ICON }
                                    )

                                    // Row 3: Folder & Section In-Place Trigger
                                    EditorSummaryRow(
                                        icon = Icons.Default.Folder,
                                        label = "Folder",
                                        valueText = "$folderName • $sectionName",
                                        onClick = { activeSubPopup = EditorSubPopup.FOLDER_SECTION }
                                    )

                                    // Row 4: Repeat / Recurrence In-Place Trigger
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

                                    // Row 5: Subtasks In-Place Trigger
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

                                    // Bottom Full-Width Coral "Continue" Button
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
                                    stepOrder = stepOrder,
                                    startTimeMinutes = startTimeMinutes,
                                    durationMinutes = durationMinutes,
                                    showBubbleOption = true,
                                    onModeChange = { taskMode = it },
                                    onStepOrderChange = { stepOrder = it },
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

/**
 * Overlapping / Simultaneous Timed Task Conflict Prompt Modal:
 * - Shown when scheduling or saving a Timed task that overlaps an existing Timed task on the same date.
 * - Offers two clear choices:
 *   1. Auto-Shift: Moves the new task's start time to immediately follow the existing task's end time.
 *   2. Allow Simultaneous: Saves the task at the overlapping time and displays a compact "Simultaneous" badge on both tasks.
 */
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
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SoftSheetSurface, CircleShape)
                            .border(1.5.dp, InkBlack, CircleShape)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = InkBlack,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SoftSheetSurface)
                        .border(1.5.dp, InkBlack, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "\"$newTaskTitle\" (${formatMinutesAsClock(requestedStartMin)}–${formatMinutesAsClock(requestedEndMin)}) overlaps with \"$conflictingTaskTitle\".",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = InkBlack
                    )
                }

                NeoPillButton(
                    text = "Auto-Shift (${formatMinutesAsClock(suggestedShiftStartMin)}–${formatMinutesAsClock(suggestedShiftEndMin)})",
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


/**
 * Clean Focus Screen with Live Session Recording, Count-Up Stopwatch (for 0m tasks),
 * Countdown Timer with +1m / +5m live extensions, and Detailed Session Interval Log:
 */
@Composable
fun AntiParalysisFocusScreen(
    focusItem: TaskWithSubtasks?,
    totalDayTasksCount: Int = 0,
    completedDayTasksCount: Int = 0,
    onBackToFlow: () -> Unit,
    onAddTaskToFlow: () -> Unit = {},
    onStartOrResumeSession: (TaskEntity) -> Unit,
    onPauseSession: (TaskEntity) -> Unit,
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
    var newStepTitle by remember { mutableStateOf("") }

    val recordedIntervals = remember(task.parsedSessionSegments, task.activeSessionStartMin) {
        buildList {
            val segs = task.parsedSessionSegments
            segs.forEachIndexed { idx, (s, e) ->
                if (idx > 0) {
                    val prevEnd = segs[idx - 1].second
                    val breakMins = (s - prevEnd).coerceAtLeast(1)
                    add("Break • ${formatDurationBadge(breakMins)} (${formatMinutesAsClock(prevEnd)} – ${formatMinutesAsClock(s)})")
                }
                val workMins = (e - s).coerceAtLeast(1)
                val label = if (idx == 0) "Started" else "Resumed"
                add("$label  ${formatMinutesAsClock(s)} – ${formatMinutesAsClock(e)} (${formatDurationBadge(workMins)})")
            }
            task.activeSessionStartMin?.let { activeStart ->
                if (segs.isNotEmpty()) {
                    val prevEnd = segs.last().second
                    val breakMins = (activeStart - prevEnd).coerceAtLeast(1)
                    add("Break • ${formatDurationBadge(breakMins)} (${formatMinutesAsClock(prevEnd)} – ${formatMinutesAsClock(activeStart)})")
                }
                val label = if (segs.isEmpty()) "Started" else "Resumed"
                add("$label  ${formatMinutesAsClock(activeStart)} – Active")
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
            // Top Bar: Back to Timeline + Folder/Section Badge
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

            // Single-Column Focus Workspace centered within max 680.dp
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
                    onTogglePlayPause = {
                        if (isRunning) {
                            isRunning = false
                            onPauseSession(task)
                        } else {
                            isRunning = true
                            onStartOrResumeSession(task)
                        }
                    },
                    onExtendDurationMinutes = { deltaMins ->
                        onExtendDurationMinutes(task, deltaMins)
                    },
                    onCompleteAndAdvance = onCompleteAndAdvance
                )

                FocusSubtasksAndSessionCard(
                    focusItem = focusItem,
                    recordedIntervals = recordedIntervals,
                    newStepTitle = newStepTitle,
                    onNewStepTitleChange = { newStepTitle = it },
                    onToggleSubtask = onToggleSubtask,
                    onAddQuickSubtask = {
                        if (newStepTitle.isNotBlank()) {
                            onAddQuickSubtask(task.id, newStepTitle)
                            newStepTitle = ""
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FocusHeroAndTimerCard(
    task: TaskEntity,
    accent: androidx.compose.ui.graphics.Color,
    isStopwatch: Boolean,
    targetSeconds: Long,
    isRunning: Boolean,
    onTogglePlayPause: () -> Unit,
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

            // Isolated Leaf Composable for 1-second Timer Ticking (0 parent recompositions, 0 background CPU)
            FocusLiveTimerReadoutCard(
                task = task,
                isStopwatch = isStopwatch,
                targetSeconds = targetSeconds,
                isRunning = isRunning,
                onTogglePlayPause = onTogglePlayPause,
                onExtendDurationMinutes = onExtendDurationMinutes
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.sdp)
                    .clip(RoundedCornerShape(16.sdp))
                    .background(NeoMintGreen)
                    .border(1.5.dp, InkBlack, RoundedCornerShape(16.sdp))
                    .clickable(onClick = onCompleteAndAdvance)
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

/**
 * Dedicated leaf composable that isolates the 1-second Focus Timer state:
 * - Only this inner card recomposes once per second when the timer is running.
 * - Supports Count-Up Stopwatch mode (when durationMinutes == 0) and Countdown mode (durationMinutes > 0)
 *   with Google Clock-style +1m and +5m live extension buttons.
 * - Uses `SystemClock.elapsedRealtime()` delta timestamps + `repeatOnLifecycle(Lifecycle.State.STARTED)`
 *   so 0 coroutine ticks occur when the app is minimized or the screen is off (<1% background battery),
 *   and elapsed time is immediately recalculated upon returning to the foreground.
 */
@Composable
private fun FocusLiveTimerReadoutCard(
    task: TaskEntity,
    isStopwatch: Boolean,
    targetSeconds: Long,
    isRunning: Boolean,
    onTogglePlayPause: () -> Unit,
    onExtendDurationMinutes: (Int) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var accumulatedElapsedSeconds by remember(task.id) { mutableLongStateOf(0L) }
    var activeRunAnchorRealtimeMs by remember(task.id) {
        mutableLongStateOf(if (isRunning) SystemClock.elapsedRealtime() else 0L)
    }
    var displayElapsedSeconds by remember(task.id) { mutableLongStateOf(0L) }

    LaunchedEffect(task.id, isRunning, lifecycleOwner) {
        if (isRunning) {
            if (activeRunAnchorRealtimeMs == 0L) {
                activeRunAnchorRealtimeMs = SystemClock.elapsedRealtime()
            }
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    val deltaSec = ((SystemClock.elapsedRealtime() - activeRunAnchorRealtimeMs) / 1000L).coerceAtLeast(0L)
                    displayElapsedSeconds = accumulatedElapsedSeconds + deltaSec
                    delay(1000L)
                }
            }
        } else {
            if (activeRunAnchorRealtimeMs != 0L) {
                val deltaSec = ((SystemClock.elapsedRealtime() - activeRunAnchorRealtimeMs) / 1000L).coerceAtLeast(0L)
                accumulatedElapsedSeconds += deltaSec
                activeRunAnchorRealtimeMs = 0L
            }
            displayElapsedSeconds = accumulatedElapsedSeconds
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
                        task.parsedSessionSegments.isNotEmpty() -> "Resume"
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
                        accumulatedElapsedSeconds = 0L
                        activeRunAnchorRealtimeMs = if (isRunning) SystemClock.elapsedRealtime() else 0L
                        displayElapsedSeconds = 0L
                    }
                )
            }
        }
    }
}

@Composable
private fun FocusSubtasksAndSessionCard(
    focusItem: TaskWithSubtasks,
    recordedIntervals: List<String>,
    newStepTitle: String,
    onNewStepTitleChange: (String) -> Unit,
    onToggleSubtask: (SubtaskEntity) -> Unit,
    onAddQuickSubtask: () -> Unit
) {
    val task = focusItem.task
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Subtasks Card
        NeoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = PureWhiteSurface,
            cornerRadius = 22.dp,
            contentPadding = PaddingValues(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Subtasks (${focusItem.completedSubtasksCount}/${focusItem.subtasks.size})",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = InkBlack
                )

                focusItem.sortedSubtasks.forEach { sub ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SoftSheetSurface)
                            .clickable { onToggleSubtask(sub) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newStepTitle,
                        onValueChange = onNewStepTitleChange,
                        placeholder = {
                            Text(
                                "Add subtask",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                                color = InkMuted
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                    NeoPillButton(
                        text = "Add",
                        selected = true,
                        activeColor = NeoSkyBlue,
                        onClick = onAddQuickSubtask
                    )
                }
            }
        }

        // Live Session Interval Log Card (preserves every individual work and break interval)
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
                                    color = if (isBreakRow) InkBlack.copy(alpha = 0.3f) else androidx.compose.ui.graphics.Color.Transparent,
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
