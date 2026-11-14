package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.RecurrenceRule
import com.example.data.TaskMode
import com.example.data.TaskWithSubtasks
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkMuted
import com.example.ui.theme.NeoMintGreen
import com.example.ui.theme.NeoYellow
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.SoftSheetSurface
import com.example.ui.theme.neoColorForKey
import com.example.ui.theme.neoSoftColorForKey
import com.example.ui.theme.sdp
import com.example.ui.theme.ssp

private val TIME_COLUMN_WIDTH: Dp
    @Composable get() = if (LocalClockFormat.current.use24Hour) 38.sdp else 52.sdp
private val TIME_TO_CAPSULE_GAP: Dp
    @Composable get() = 6.sdp
private val CAPSULE_COLUMN_WIDTH: Dp
    @Composable get() = 48.sdp

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

@Composable
fun HybridSpineTaskRow(
    item: TaskWithSubtasks,
    sequenceNumber: Int,
    isUpNext: Boolean,
    isSimultaneous: Boolean = false,
    isMultiSelectActive: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onLongPressSelect: () -> Unit = {},
    onToggleComplete: () -> Unit,
    onOpenTaskDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val task = item.task
    val accentColor = neoColorForKey(task.colorKey)
    val capsuleFill by animateColorAsState(
        targetValue = when {
            isSelected -> NeoYellow
            task.isCompleted -> SoftSheetSurface
            else -> accentColor
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "capsule_fill_${task.id}"
    )

    val isTimed = task.modeEnum == TaskMode.TIMED_BLOCK
    val use24Hour = LocalClockFormat.current.use24Hour
    val startMin = task.startTimeMinutes ?: (9 * 60)
    val endMin = (startMin + task.durationMinutes.coerceAtLeast(5)).coerceAtMost(23 * 60 + 59)
    val segments = task.parsedSessionSegments
    val hasPauseBreak = task.hasBreakLoop

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
                    onLongClick = onLongPressSelect,
                    onClick = {
                        if (isMultiSelectActive) {
                            onToggleSelect()
                        } else {
                            onOpenTaskDetail()
                        }
                    }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                        text = formatMinutesAsClock(startMin, use24Hour),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 10.5.ssp,
                            fontWeight = FontWeight.Normal,
                            color = if (isUpNext) InkBlack else InkMuted
                        )
                    )
                    Text(
                        text = formatMinutesAsClock(endMin, use24Hour),
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

            Box(
                modifier = Modifier
                    .width(CAPSULE_COLUMN_WIDTH)
                    .fillMaxHeight()
                    .clip(ovalShape)
                    .background(capsuleFill, ovalShape)
                    .border(if (isSelected) 2.2.dp else 1.5.dp, InkBlack, ovalShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.Check else iconForKey(task.iconKey),
                    contentDescription = task.title,
                    tint = InkBlack,
                    modifier = Modifier.size(22.sdp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            TaskRightTitleAndCheckRow(
                item = item,
                isUpNext = isUpNext,
                isSimultaneous = isSimultaneous || (isTimed && task.allowSimultaneous),
                isMultiSelectActive = isMultiSelectActive,
                isSelected = isSelected,
                accentColor = accentColor,
                onToggleComplete = {
                    if (isMultiSelectActive) {
                        onToggleSelect()
                    } else {
                        onToggleComplete()
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    } else {
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

        val topHalfOvalShape = RoundedCornerShape(
            topStart = 22.sdp,
            topEnd = 22.sdp,
            bottomStart = 4.sdp,
            bottomEnd = 4.sdp
        )
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
                    onLongClick = onLongPressSelect,
                    onClick = {
                        if (isMultiSelectActive) {
                            onToggleSelect()
                        } else {
                            onOpenTaskDetail()
                        }
                    }
                ),
            verticalAlignment = Alignment.Top
        ) {
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
                            text = formatMinutesAsClock(firstSegStart, use24Hour),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 10.5.ssp,
                                fontWeight = FontWeight.Normal,
                                color = if (isUpNext) InkBlack else InkMuted
                            )
                        )
                        Text(
                            text = formatMinutesAsClock(firstSegEnd, use24Hour),
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
                            text = formatMinutesAsClock(secondSegStart, use24Hour),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 9.5.ssp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = InkMuted
                        )
                        Text(
                            text = formatMinutesAsClock(secondSegEnd, use24Hour),
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

            Column(
                modifier = Modifier
                    .width(CAPSULE_COLUMN_WIDTH)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(CAPSULE_COLUMN_WIDTH)
                        .height(topHalfHeight)
                        .clip(topHalfOvalShape)
                        .background(capsuleFill, topHalfOvalShape)
                        .border(if (isSelected) 2.2.dp else 1.5.dp, InkBlack, topHalfOvalShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Check else iconForKey(task.iconKey),
                        contentDescription = task.title,
                        tint = InkBlack,
                        modifier = Modifier.size(20.sdp)
                    )
                }

                DottedBreakLoopCanvas(
                    height = loopHeight,
                    color = InkBlack
                )

                Box(
                    modifier = Modifier
                        .width(CAPSULE_COLUMN_WIDTH)
                        .height(bottomHalfHeight)
                        .clip(bottomHalfOvalShape)
                        .background(capsuleFill, bottomHalfOvalShape)
                        .border(if (isSelected) 2.2.dp else 1.5.dp, InkBlack, bottomHalfOvalShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Check else iconForKey(task.iconKey),
                        contentDescription = "${task.title} Continued",
                        tint = InkBlack,
                        modifier = Modifier.size(20.sdp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

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
                    isMultiSelectActive = isMultiSelectActive,
                    isSelected = isSelected,
                    accentColor = accentColor,
                    onToggleComplete = {
                        if (isMultiSelectActive) {
                            onToggleSelect()
                        } else {
                            onToggleComplete()
                        }
                    },
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
    isMultiSelectActive: Boolean = false,
    isSelected: Boolean = false,
    accentColor: Color,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val task = item.task
    val hasPauseBreak = task.hasBreakLoop

    Row(
        modifier = modifier
            .then(
                when {
                    isSelected -> {
                        Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(NeoYellow)
                            .border(1.5.dp, InkBlack, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    }
                    isUpNext && !task.isCompleted -> {
                        Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(neoSoftColorForKey(task.colorKey))
                            .border(1.5.dp, InkBlack, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    }
                    else -> {
                        Modifier.padding(vertical = 6.dp)
                    }
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
            checked = if (isMultiSelectActive) isSelected else task.isCompleted,
            accentColor = if (isMultiSelectActive) NeoMintGreen else accentColor,
            onCheckedChange = onToggleComplete,
            modifier = Modifier.testTag("toggle_task_${task.id}")
        )
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
    val use24Hour = LocalClockFormat.current.use24Hour
    Row(
        modifier = modifier
            .fillMaxWidth()
            .smoothPressClickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatMinutesAsClock(timeMinutes, use24Hour),
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
