package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.RecurrenceRule
import com.example.data.SubtaskEntity
import com.example.data.TaskMode
import com.example.data.TaskWithSubtasks
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
import com.example.ui.theme.ssp

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
    var showDeleteConfirmPopup by remember(task.id) { mutableStateOf(false) }
    var stepVal by remember(task.id, currentStepNumber) {
        mutableStateOf(currentStepNumber.coerceIn(1, 99))
    }
    val hasBreakLoop = task.hasBreakLoop

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
                    NeoDeleteTaskConfirmView(
                        taskTitle = task.title,
                        isRecurring = task.recurrenceEnum != RecurrenceRule.NONE,
                        onDeleteTodayOnly = onDeleteTodayOnly?.let { action ->
                            {
                                showDeleteConfirmPopup = false
                                action()
                            }
                        },
                        onDeleteAll = {
                            showDeleteConfirmPopup = false
                            onDeleteTask()
                        },
                        onCancel = { showDeleteConfirmPopup = false }
                    )
                } else {
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
                                val use24Hour = LocalClockFormat.current.use24Hour
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = buildString {
                                            append("${task.folderName} • ${task.sectionName} • ${formatDurationBadge(task.durationMinutes)}")
                                            task.startTimeMinutes?.let {
                                                append(" • ${formatMinutesAsClock(it, use24Hour)}")
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

                            NeoCircleCloseButton(onClick = onDismiss)
                        }

                        if (task.notes.isNotBlank()) {
                            Text(
                                text = task.notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkCharcoal
                            )
                        }

                        if (task.modeEnum != TaskMode.TOP_BUBBLE) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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

                        NeoSubtaskChecklistSection(
                            subtasks = item.sortedSubtasks,
                            completedCount = item.completedSubtasksCount,
                            onToggleSubtask = onToggleSubtask,
                            onAddSubtask = onAddSubtask
                        )

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
