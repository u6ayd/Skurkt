package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Coffee
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DotGridColor
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.NeoCoral
import com.example.ui.theme.NeoMintGreen
import com.example.ui.theme.NeoYellow
import com.example.ui.theme.PaperCanvas
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.sdp
import com.example.ui.theme.ssp

/**
 * Subtle papery small dot-grid background.
 */
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
            .drawBehind {
                val stepPx = 18.dp.toPx()
                val radiusPx = 0.95.dp.toPx()
                var x = stepPx / 2f
                while (x < size.width) {
                    var y = stepPx / 2f
                    while (y < size.height) {
                        drawCircle(
                            color = dotColor,
                            radius = radiusPx,
                            center = Offset(x, y)
                        )
                        y += stepPx
                    }
                    x += stepPx
                }
            }
            .then(modifier),
        content = content
    )
}

/**
 * Smooth color-dimming press modifier without a boxy rectangular ripple.
 */
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

/**
 * Clean Learnify-style Card: Crisp 1.5.dp normal border lines, symmetrical 20.dp curves, ZERO drop shadow.
 */
@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = PureWhiteSurface,
    borderColor: Color = InkBlack,
    cornerRadius: Dp = 20.dp,
    shadowOffset: Dp = 0.dp,
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
    tagLabel: String = "",
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
            // Top Row: Learnify-style internal pill badge (e.g. PHY 12) + Percentage badge
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
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
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
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        color = InkBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Single Folder Title (SemiBold 600)
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                color = InkBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Learnify-style Progress Header Row (Regular 400 metadata)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
                    color = InkCharcoal
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
                    color = InkBlack
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            NeoProgressBar(progress = progressPercent / 100f)
            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Action Row: Clean status on left (Medium 500) + "Continue" pill on right (SemiBold 600)
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
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
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
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
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

/**
 * Clean Pill Button with normal 1.5.dp border and zero drop shadow.
 */
@Composable
fun NeoPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    selected: Boolean = false,
    activeColor: Color = NeoYellow,
    inactiveColor: Color = PureWhiteSurface,
    shadowOffset: Dp = 0.dp,
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

/**
 * High-Contrast Progress Bar: crisp Pure White (#FFFFFF) unfilled track with a 1.2.dp solid dark border (#1A1A1A),
 * and solid dark (#1A1A1A) or vibrant coral (#FF5E45) filled progress indicator.
 */
@Composable
fun NeoProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    fillColor: Color = InkBlack,
    trackColor: Color = PureWhiteSurface,
    height: Dp = 8.sdp
) {
    val clamped = progress.coerceIn(0f, 1f)
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(trackColor, shape)
            .border(1.2.dp, InkBlack, shape)
    ) {
        if (clamped > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(clamped)
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
    val shape = RoundedCornerShape(8.sdp)
    Box(
        modifier = modifier
            .size(24.sdp)
            .smoothPressClickable(role = Role.Checkbox, onClick = onCheckedChange)
            .clip(shape)
            .background(bgColor, shape)
            .border(1.5.dp, InkBlack, shape),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                tint = InkBlack,
                modifier = Modifier.size(15.sdp)
            )
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
    Box(
        modifier = modifier
            .size(28.sdp)
            .smoothPressClickable(role = Role.Checkbox, onClick = onCheckedChange)
            .clip(CircleShape)
            .background(bgColor, CircleShape)
            .border(1.8.dp, if (checked) InkBlack else accentColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                tint = InkBlack,
                modifier = Modifier.size(16.sdp)
            )
        }
    }
}

@Composable
fun NeoDashedDivider(
    modifier: Modifier = Modifier,
    color: Color = InkBlack.copy(alpha = 0.14f)
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        )
    }
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
