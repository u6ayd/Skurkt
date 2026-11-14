package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shower
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas as ComposeCanvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import com.example.data.SubtaskEntity
import com.example.ui.theme.DotGridColor
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.NeoCoral
import com.example.ui.theme.NeoMintGreen
import com.example.ui.theme.NeoSkyBlue
import com.example.ui.theme.NeoYellow
import com.example.ui.theme.PaperCanvas
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.SoftSheetSurface
import com.example.ui.theme.sdp
import com.example.ui.theme.ssp

@Composable
fun DotGridBackground(
    modifier: Modifier = Modifier,
    backgroundColor: Color = PaperCanvas,
    dotColor: Color = DotGridColor,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .drawWithCache {
                val stepPx = 18.dp.toPx().roundToInt().coerceAtLeast(8)
                val radiusPx = 0.95.dp.toPx()
                val tileBitmap = ImageBitmap(stepPx, stepPx)
                val tileCanvas = ComposeCanvas(tileBitmap)
                val dotPaint = Paint().apply {
                    color = dotColor
                    isAntiAlias = true
                }
                val center = stepPx / 2f
                tileCanvas.drawCircle(Offset(center, center), radiusPx, dotPaint)
                val tiledBrush = ShaderBrush(
                    ImageShader(tileBitmap, TileMode.Repeated, TileMode.Repeated)
                )
                onDrawBehind {
                    drawRect(brush = tiledBrush)
                }
            }
            .then(modifier),
        content = content
    )
}

fun Modifier.smoothPressClickable(
    role: Role? = Role.Button,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.72f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "press_alpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "press_scale"
    )
    this
        .graphicsLayer {
            this.alpha = alpha
            this.scaleX = scale
            this.scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            role = role,
            onClick = onClick
        )
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.smoothCombinedClickable(
    role: Role? = Role.Button,
    onLongClick: () -> Unit,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.72f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "combined_press_alpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "combined_press_scale"
    )
    this
        .graphicsLayer {
            this.alpha = alpha
            this.scaleX = scale
            this.scaleY = scale
        }
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            role = role,
            onLongClick = onLongClick,
            onClick = onClick
        )
}

@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = PureWhiteSurface,
    borderColor: Color = InkBlack,
    cornerRadius: Dp = 20.dp,
    borderWidth: Dp = 1.5.dp,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.smoothPressClickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .background(backgroundColor, shape)
            .border(BorderStroke(borderWidth, borderColor), shape)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun NeoFolderCard(
    tabCode: String,
    title: String,
    subtitle: String,
    progressPercent: Int,
    fillColor: Color,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NeoCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = fillColor,
        cornerRadius = 22.dp,
        borderWidth = 1.5.dp,
        onClick = onCardClick,
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(InkBlack)
                        .padding(horizontal = 11.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = tabCode.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 11.4.ssp,
                            lineHeight = 15.5.ssp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = PureWhiteSurface,
                        maxLines = 1
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(PureWhiteSurface)
                        .border(1.5.dp, InkBlack, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$progressPercent%",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 12.4.ssp,
                            lineHeight = 16.5.ssp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = InkBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 18.5.ssp,
                    lineHeight = 24.ssp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = InkBlack,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 11.4.ssp,
                        lineHeight = 15.5.ssp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = InkCharcoal
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 11.4.ssp,
                        lineHeight = 15.5.ssp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = InkBlack
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            NeoProgressBar(progress = progressPercent / 100f)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.sdp)
                            .clip(CircleShape)
                            .background(InkBlack)
                    )
                    Text(
                        text = if (progressPercent >= 100) "Completed" else "Active Folder",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 11.4.ssp,
                            lineHeight = 15.5.ssp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = InkCharcoal
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(11.sdp))
                        .background(NeoCoral)
                        .border(1.5.dp, InkBlack, RoundedCornerShape(11.sdp))
                        .padding(horizontal = 14.sdp, vertical = 7.sdp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 12.4.ssp,
                            lineHeight = 16.5.ssp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = PureWhiteSurface
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Folder",
                        tint = PureWhiteSurface,
                        modifier = Modifier.size(14.sdp)
                    )
                }
            }
        }
    }
}

@Composable
fun NeoPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    selected: Boolean = false,
    activeColor: Color = NeoYellow,
    inactiveColor: Color = PureWhiteSurface,
    textFontWeight: FontWeight = FontWeight.SemiBold
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pill_bg"
    )
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .height(40.sdp)
            .smoothPressClickable(role = Role.Button, onClick = onClick)
            .clip(shape)
            .background(bgColor, shape)
            .border(1.5.dp, InkBlack, shape)
            .padding(horizontal = 14.sdp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = InkBlack,
                    modifier = Modifier.size(16.sdp)
                )
                if (text.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
            if (text.isNotEmpty()) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 13.ssp,
                        fontWeight = textFontWeight
                    ),
                    color = InkBlack,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun NeoProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    fillColor: Color = InkBlack,
    trackColor: Color = PureWhiteSurface,
    height: Dp = 8.sdp
) {
    val clamped = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = clamped,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "neo_progress_width"
    )
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(trackColor, shape)
            .border(1.2.dp, InkBlack, shape)
    ) {
        if (animatedProgress > 0.001f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(fillColor, shape)
            )
        }
    }
}

@Composable
fun NeoSquareCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = NeoMintGreen
) {
    val bgColor by animateColorAsState(
        targetValue = if (checked) accentColor else PureWhiteSurface,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "square_check_bg"
    )
    val checkScale by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "square_check_scale"
    )
    val checkAlpha by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "square_check_alpha"
    )
    val shape = RoundedCornerShape(8.sdp)
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .smoothPressClickable(role = Role.Checkbox, onClick = onCheckedChange),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(24.sdp)
                .clip(shape)
                .background(bgColor, shape)
                .border(1.5.dp, InkBlack, shape),
            contentAlignment = Alignment.Center
        ) {
            if (checkAlpha > 0.01f) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = InkBlack,
                    modifier = Modifier
                        .size(15.sdp)
                        .graphicsLayer {
                            scaleX = checkScale
                            scaleY = checkScale
                            alpha = checkAlpha
                        }
                )
            }
        }
    }
}

@Composable
fun NeoCircleCheckbox(
    checked: Boolean,
    accentColor: Color,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (checked) accentColor else PureWhiteSurface,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "circle_check_bg"
    )
    val checkScale by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "circle_check_scale"
    )
    val checkAlpha by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "circle_check_alpha"
    )
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .smoothPressClickable(role = Role.Checkbox, onClick = onCheckedChange),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(28.sdp)
                .clip(CircleShape)
                .background(bgColor, CircleShape)
                .border(1.8.dp, if (checked) InkBlack else accentColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (checkAlpha > 0.01f) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = InkBlack,
                    modifier = Modifier
                        .size(16.sdp)
                        .graphicsLayer {
                            scaleX = checkScale
                            scaleY = checkScale
                            alpha = checkAlpha
                        }
                )
            }
        }
    }
}

@Composable
fun NeoDashedDivider(
    modifier: Modifier = Modifier,
    color: Color = InkBlack.copy(alpha = 0.14f)
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .drawWithCache {
                val strokePx = 1.5.dp.toPx()
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                onDrawBehind {
                    drawLine(
                        color = color,
                        start = Offset(0f, size.height / 2f),
                        end = Offset(size.width, size.height / 2f),
                        strokeWidth = strokePx,
                        pathEffect = dashEffect
                    )
                }
            }
    )
}

data class TaskIconOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

val TaskIconCatalog = listOf(
    TaskIconOption("ALARM", "Wake", Icons.Default.AccessAlarm),
    TaskIconOption("WORKOUT", "Yoga", Icons.Default.SelfImprovement),
    TaskIconOption("FITNESS", "Sport", Icons.Default.FitnessCenter),
    TaskIconOption("BIKE", "Bike", Icons.AutoMirrored.Filled.DirectionsBike),
    TaskIconOption("BOOK", "Book", Icons.AutoMirrored.Filled.MenuBook),
    TaskIconOption("SCIENCE", "Physics", Icons.Default.Science),
    TaskIconOption("CODE", "Code", Icons.Default.Code),
    TaskIconOption("MEETING", "Team", Icons.Default.Groups),
    TaskIconOption("COFFEE", "Coffee", Icons.Default.Coffee),
    TaskIconOption("PLANT", "Plants", Icons.Default.Eco),
    TaskIconOption("CALL", "Call", Icons.Default.Call),
    TaskIconOption("LAUNDRY", "Laundry", Icons.Default.LocalLaundryService),
    TaskIconOption("SHOWER", "Shower", Icons.Default.Shower),
    TaskIconOption("TAX", "Finance", Icons.Default.AccountBalance),
    TaskIconOption("CHECKLIST", "Routine", Icons.Default.Checklist),
    TaskIconOption("BOLT", "Focus", Icons.Default.Bolt),
    TaskIconOption("TASK", "Task", Icons.Default.TaskAlt)
)

fun iconForKey(key: String): ImageVector {
    return TaskIconCatalog.firstOrNull { it.key.equals(key, ignoreCase = true) }?.icon
        ?: Icons.Default.TaskAlt
}

@Composable
fun NeoStatCard(
    label: String,
    primaryValue: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    progress: Float? = null,
    cornerRadius: Dp = 18.dp,
    contentPadding: PaddingValues = PaddingValues(14.dp)
) {
    NeoCard(
        modifier = modifier,
        backgroundColor = backgroundColor,
        cornerRadius = cornerRadius,
        contentPadding = contentPadding
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = InkBlack
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = primaryValue,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = InkBlack
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = InkCharcoal
                )
            }
            if (progress != null) {
                Spacer(modifier = Modifier.height(6.dp))
                NeoProgressBar(progress = progress, height = 6.dp)
            }
        }
    }
}

@Composable
fun NeoCircleCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Close",
    size: Dp = 34.sdp,
    iconSize: Dp = 16.sdp,
    backgroundColor: Color = SoftSheetSurface
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .smoothPressClickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(backgroundColor, CircleShape)
                .border(1.5.dp, InkBlack, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = contentDescription,
                tint = InkBlack,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
fun NeoDeleteTaskConfirmView(
    taskTitle: String,
    isRecurring: Boolean,
    onDeleteTodayOnly: (() -> Unit)?,
    onDeleteAll: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(NeoCoral)
                    .border(1.5.dp, InkBlack, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = PureWhiteSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isRecurring) "Delete Repeating Task?" else "Delete Task?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = InkBlack
                )
                Text(
                    text = taskTitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.ssp
                    ),
                    color = InkMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (isRecurring && onDeleteTodayOnly != null) {
            NeoPillButton(
                text = "Delete Today Only",
                selected = true,
                activeColor = NeoYellow,
                onClick = onDeleteTodayOnly,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delete_today_only_button")
            )
            NeoPillButton(
                text = "Delete All Future Tasks",
                icon = Icons.Default.Delete,
                selected = true,
                activeColor = NeoCoral,
                onClick = onDeleteAll,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confirm_delete_task_button")
            )
            NeoPillButton(
                text = "Cancel",
                selected = false,
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeoPillButton(
                    text = "Cancel",
                    selected = false,
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                )
                NeoPillButton(
                    text = "Delete",
                    icon = Icons.Default.Delete,
                    selected = true,
                    activeColor = NeoCoral,
                    onClick = onDeleteAll,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("confirm_delete_task_button")
                )
            }
        }
    }
}

@Composable
fun NeoSubtaskChecklistSection(
    subtasks: List<SubtaskEntity>,
    completedCount: Int,
    onToggleSubtask: (SubtaskEntity) -> Unit,
    onAddSubtask: (String) -> Unit,
    modifier: Modifier = Modifier,
    alwaysExpanded: Boolean = false
) {
    var showAddSubtaskField by remember { mutableStateOf(alwaysExpanded) }
    var newSubtask by remember { mutableStateOf("") }

    if (subtasks.isNotEmpty() || showAddSubtaskField || alwaysExpanded) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SoftSheetSurface)
                .border(1.5.dp, InkBlack, RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Subtasks ($completedCount/${subtasks.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = InkBlack
                )
                if (!showAddSubtaskField && !alwaysExpanded) {
                    Text(
                        text = "+ Add",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = InkBlack,
                        modifier = Modifier.smoothPressClickable { showAddSubtaskField = true }
                    )
                }
            }

            subtasks.forEach { sub ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(PureWhiteSurface)
                        .smoothPressClickable { onToggleSubtask(sub) }
                        .padding(horizontal = 10.dp, vertical = 7.dp),
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

            if (showAddSubtaskField || alwaysExpanded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newSubtask,
                        onValueChange = { newSubtask = it },
                        placeholder = {
                            Text(
                                "Add subtask...",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                                color = InkMuted
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .background(PureWhiteSurface, RoundedCornerShape(12.dp))
                    )
                    NeoPillButton(
                        text = "Add",
                        selected = true,
                        activeColor = NeoSkyBlue,
                        onClick = {
                            if (newSubtask.isNotBlank()) {
                                onAddSubtask(newSubtask)
                                newSubtask = ""
                            }
                        }
                    )
                }
            }
        }
    } else {
        NeoPillButton(
            text = "+ Add Subtask",
            icon = Icons.Default.Checklist,
            selected = false,
            onClick = { showAddSubtaskField = true },
            modifier = modifier.fillMaxWidth()
        )
    }
}

