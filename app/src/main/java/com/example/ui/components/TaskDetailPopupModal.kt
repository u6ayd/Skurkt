package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import com.example.ui.theme.sdp
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
            cornerRadius = 24.dp,
            contentPadding = PaddingValues(20.dp)
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                                        modifier = Modifier.size(21.dp)
                                    )
                                }
                                val use24Hour = LocalClockFormat.current.use24Hour
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = buildString {
                                            append("${task.folderName} · ${task.sectionName} · ${formatDurationBadge(task.durationMinutes)}")
                                            task.startTimeMinutes?.let {
                                                append(" · ${formatMinutesAsClock(it, use24Hour)}")
                                            }
                                        },
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 11.5.ssp
                                        ),
                                        color = InkMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 21.ssp
                                        ),
                                        color = InkBlack,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Box(
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(SoftSheetSurface, CircleShape)
                                    .border(1.dp, InkBlack.copy(alpha = 0.14f), CircleShape)
                                    .smoothPressClickable(role = Role.Button, onClick = onDismiss),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = InkCharcoal,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Notes & optional break telemetry (plain clean typography, no noisy border pill)
                        if (task.notes.isNotBlank() || hasBreakLoop) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (task.notes.isNotBlank()) {
                                    Text(
                                        text = task.notes,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            lineHeight = 19.ssp
                                        ),
                                        color = InkCharcoal
                                    )
                                }
                                if (hasBreakLoop) {
                                    val workedMin = task.totalWorkedMinutes.coerceAtLeast(1)
                                    val breakMin = task.totalBreakMinutes.coerceAtLeast(1)
                                    Text(
                                        text = "${formatDurationBadge(workedMin)} work · ${formatDurationBadge(breakMin)} break",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.5.ssp
                                        ),
                                        color = InkMuted
                                    )
                                }
                            }
                        }

                        // Calm soft-paper controls strip (Order + Break Loop)
                        if (task.modeEnum != TaskMode.TOP_BUBBLE) {
                            val pillShape = RoundedCornerShape(50)
                            val breakBg by animateColorAsState(
                                targetValue = if (hasBreakLoop) NeoSkyBlue.copy(alpha = 0.36f) else SoftSheetSurface,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                label = "detail_break_bg"
                            )
                            val breakBorder = if (hasBreakLoop) InkBlack else InkBlack.copy(alpha = 0.14f)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .height(40.sdp)
                                        .clip(pillShape)
                                        .background(SoftSheetSurface, pillShape)
                                        .border(1.dp, InkBlack.copy(alpha = 0.14f), pillShape)
                                        .padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.sdp)
                                            .clip(CircleShape)
                                            .smoothPressClickable(role = Role.Button) {
                                                val next = (stepVal - 1).coerceAtLeast(1)
                                                stepVal = next
                                                onUpdateStepNumber(next)
                                            }
                                            .testTag("step_number_minus"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Decrease Step #",
                                            tint = InkBlack,
                                            modifier = Modifier.size(15.sdp)
                                        )
                                    }

                                    Text(
                                        text = "#$stepVal",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 13.ssp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = InkBlack,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(32.sdp)
                                            .clip(CircleShape)
                                            .smoothPressClickable(role = Role.Button) {
                                                val next = (stepVal + 1).coerceAtMost(99)
                                                stepVal = next
                                                onUpdateStepNumber(next)
                                            }
                                            .testTag("step_number_plus"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Increase Step #",
                                            tint = InkBlack,
                                            modifier = Modifier.size(15.sdp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.sdp)
                                        .clip(pillShape)
                                        .background(breakBg, pillShape)
                                        .border(
                                            width = if (hasBreakLoop) 1.2.dp else 1.dp,
                                            color = breakBorder,
                                            shape = pillShape
                                        )
                                        .smoothPressClickable(role = Role.Button, onClick = onToggleBreakLoop)
                                        .padding(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = null,
                                        tint = InkBlack,
                                        modifier = Modifier.size(15.sdp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (hasBreakLoop) "Merge Loop" else "Split Break Loop",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 12.5.ssp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = InkBlack,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Subtasks section
                        NeoSubtaskChecklistSection(
                            subtasks = item.sortedSubtasks,
                            completedCount = item.completedSubtasksCount,
                            onToggleSubtask = onToggleSubtask,
                            onAddSubtask = onAddSubtask
                        )

                        // Primary actions (signature Neo-Brutalist emphasis)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                    text = if (task.isCompleted) "Mark Undone" else "Mark Done",
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

                            // Secondary quiet utility actions
                            val secondaryShape = RoundedCornerShape(50)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.sdp)
                                        .clip(secondaryShape)
                                        .background(SoftSheetSurface, secondaryShape)
                                        .border(1.dp, InkBlack.copy(alpha = 0.16f), secondaryShape)
                                        .smoothPressClickable(role = Role.Button, onClick = onEditTask)
                                        .testTag("popup_edit_task_button"),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = InkCharcoal,
                                        modifier = Modifier.size(15.sdp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Edit",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 13.ssp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = InkCharcoal,
                                        maxLines = 1
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.sdp)
                                        .clip(secondaryShape)
                                        .background(NeoCoral.copy(alpha = 0.12f), secondaryShape)
                                        .border(1.dp, NeoCoral.copy(alpha = 0.42f), secondaryShape)
                                        .smoothPressClickable(role = Role.Button) { showDeleteConfirmPopup = true }
                                        .testTag("popup_delete_task_button"),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = NeoCoral,
                                        modifier = Modifier.size(15.sdp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Delete",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 13.ssp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = NeoCoral,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
