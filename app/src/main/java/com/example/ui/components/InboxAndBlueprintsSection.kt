package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.FolderEntity
import com.example.data.TaskEntity
import com.example.data.TaskMode
import com.example.data.TaskWithSubtasks
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.NeoCoral
import com.example.ui.theme.NeoMintGreen
import com.example.ui.theme.NeoPalette
import com.example.ui.theme.NeoRosePink
import com.example.ui.theme.NeoSkyBlue
import com.example.ui.theme.NeoYellow
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.SoftSheetSurface
import com.example.ui.theme.neoColorForKey
import com.example.ui.theme.sdp
import com.example.ui.theme.ssp

@Composable
fun NeoInboxPane(
    inboxTasks: List<TaskWithSubtasks>,
    dayReadinessPercent: Int,
    completedCount: Int,
    totalCount: Int,
    onQuickAddInbox: (String) -> Unit,
    onOpenSchedulePopup: (TaskEntity) -> Unit,
    onEditInboxTask: (TaskWithSubtasks) -> Unit,
    isMultiSelectActive: Boolean = false,
    selectedTaskIds: Set<Long> = emptySet(),
    onToggleMultiSelectMode: () -> Unit = {},
    onToggleSelectTask: (Long) -> Unit = {},
    onEnterMultiSelectWithTask: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var brainDumpText by remember { mutableStateOf("") }
    val uniformCorner = RoundedCornerShape(18.dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(NeoCoral)
                    .border(1.5.dp, InkBlack, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Inbox,
                    contentDescription = null,
                    tint = PureWhiteSurface,
                    modifier = Modifier.size(18.sdp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Inbox",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PureWhiteSurface
                )
            }

            if (inboxTasks.isNotEmpty() || isMultiSelectActive) {
                NeoPillButton(
                    text = if (isMultiSelectActive) "Done" else "Select",
                    icon = Icons.Default.Checklist,
                    selected = isMultiSelectActive,
                    activeColor = NeoYellow,
                    onClick = onToggleMultiSelectMode,
                    modifier = Modifier.testTag("inbox_multi_select_toggle")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NeoStatCard(
                label = "DAY FLOW",
                primaryValue = "$dayReadinessPercent%",
                backgroundColor = NeoYellow,
                progress = dayReadinessPercent / 100f,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            NeoStatCard(
                label = "STEPS DONE",
                primaryValue = "$completedCount / $totalCount",
                backgroundColor = NeoSkyBlue,
                progress = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.sdp)
                    .clip(uniformCorner)
                    .background(PureWhiteSurface, uniformCorner)
                    .border(1.5.dp, InkBlack, uniformCorner)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                OutlinedTextField(
                    value = brainDumpText,
                    onValueChange = { brainDumpText = it },
                    placeholder = {
                        Text(
                            "Brain dump",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                            color = InkMuted
                        )
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    shape = uniformCorner,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inbox_quick_input")
                )
            }

            Box(
                modifier = Modifier
                    .size(52.sdp)
                    .clip(uniformCorner)
                    .background(NeoMintGreen, uniformCorner)
                    .border(1.5.dp, InkBlack, uniformCorner)
                    .smoothPressClickable(role = Role.Button) {
                        if (brainDumpText.isNotBlank()) {
                            onQuickAddInbox(brainDumpText)
                            brainDumpText = ""
                        }
                    }
                    .testTag("inbox_quick_submit_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = InkBlack,
                    modifier = Modifier.size(24.sdp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(
                items = inboxTasks,
                key = { it.task.id },
                contentType = { "inbox_task" }
            ) { item ->
                val task = item.task
                val color = neoColorForKey(task.colorKey)
                val isSelected = task.id in selectedTaskIds
                NeoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .smoothCombinedClickable(
                            role = Role.Button,
                            onLongClick = { onEnterMultiSelectWithTask(task.id) },
                            onClick = {
                                if (isMultiSelectActive) {
                                    onToggleSelectTask(task.id)
                                } else {
                                    onEditInboxTask(item)
                                }
                            }
                        ),
                    backgroundColor = if (isSelected) NeoYellow else PureWhiteSurface,
                    cornerRadius = 18.dp,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                ) {
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
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) PureWhiteSurface else color)
                                    .border(1.5.dp, InkBlack, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Check else iconForKey(task.iconKey),
                                    contentDescription = null,
                                    tint = InkBlack,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = formatDurationBadge(task.durationMinutes),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
                                    color = InkMuted
                                )
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = 19.ssp
                                    ),
                                    color = InkBlack,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (isMultiSelectActive) {
                            NeoCircleCheckbox(
                                checked = isSelected,
                                accentColor = NeoMintGreen,
                                onCheckedChange = { onToggleSelectTask(task.id) }
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SoftSheetSurface)
                                    .border(1.5.dp, InkBlack, RoundedCornerShape(12.dp))
                                    .smoothPressClickable(role = Role.Button) { onOpenSchedulePopup(task) }
                                    .testTag("schedule_inbox_task_${task.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Schedule into Flow",
                                    tint = InkBlack,
                                    modifier = Modifier.size(20.dp)
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
fun ScheduleInboxMiniPopup(
    task: TaskEntity,
    onDismiss: () -> Unit,
    onConfirmSchedule: (asTimed: Boolean, startTimeMinutes: Int?, durationMinutes: Int) -> Unit
) {
    val clockConfig = LocalClockFormat.current
    var selectedMode by remember(task.id) { mutableStateOf(TaskMode.TIMELESS_STEP) }
    var startTimeMin by remember(task.id) {
        val defaultStart = (clockConfig.startOfDayMinutes + 60).coerceIn(0, 22 * 60)
        mutableIntStateOf(task.startTimeMinutes ?: defaultStart)
    }
    var durationMin by remember(task.id) {
        mutableIntStateOf(0)
    }
    var errorBanner by remember { mutableStateOf<String?>(null) }

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
            contentPadding = PaddingValues(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Schedule into Flow",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = InkBlack
                        )
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    NeoCircleCloseButton(onClick = onDismiss)
                }

                UnifiedTimeAndDurationPickerSection(
                    toInbox = false,
                    taskMode = selectedMode,
                    startTimeMinutes = startTimeMin,
                    durationMinutes = durationMin,
                    showBubbleOption = false,
                    onModeChange = { newMode ->
                        errorBanner = null
                        selectedMode = newMode
                        if (newMode == TaskMode.TIMELESS_STEP) {
                            durationMin = 0
                        } else if (newMode == TaskMode.TIMED_BLOCK && durationMin <= 0) {
                            durationMin = 60
                        }
                    },
                    onStartTimeChange = {
                        errorBanner = null
                        startTimeMin = it
                    },
                    onDurationChange = {
                        errorBanner = null
                        durationMin = it
                    }
                )

                errorBanner?.let { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeoYellow)
                            .border(1.5.dp, InkBlack, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = InkBlack
                        )
                    }
                }

                NeoPillButton(
                    text = "Schedule into Day Flow",
                    selected = true,
                    activeColor = NeoMintGreen,
                    onClick = {
                        val isTimed = selectedMode == TaskMode.TIMED_BLOCK
                        val finalDur = if (isTimed) durationMin.coerceIn(5, 720) else durationMin.coerceIn(0, 720)
                        if (isTimed && (startTimeMin + finalDur) > clockConfig.endOfDayMinutes) {
                            errorBanner =
                                "Task ends past End of Day (${formatMinutesAsClock(clockConfig.endOfDayMinutes, clockConfig.use24Hour)}). Adjust time or Settings."
                            return@NeoPillButton
                        }
                        onConfirmSchedule(
                            isTimed,
                            if (isTimed) startTimeMin else null,
                            finalDur
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

