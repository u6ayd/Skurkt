package com.example.ui.components

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.data.SubtaskEntity
import com.example.data.TaskEntity
import com.example.data.TaskWithSubtasks
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkMuted
import com.example.ui.theme.NeoCoral
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

@Composable
fun GlobalSettingsPopupModal(
    use24HourFormat: Boolean,
    startOfDayMinutes: Int,
    endOfDayMinutes: Int,
    onUpdateSettings: (use24Hour: Boolean, startOfDayMin: Int, endOfDayMin: Int) -> Unit,
    onOpenCleanupExport: () -> Unit = {},
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
                    text = "Cleanup & Export (PDF / CSV)",
                    icon = Icons.Default.Description,
                    selected = false,
                    onClick = onOpenCleanupExport,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_open_cleanup_export_button")
                )

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
    accent: Color,
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
