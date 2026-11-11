package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
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
        modifier = modifier
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
            },
        content = content
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
            .clip(shape)
            .background(backgroundColor, shape)
            .border(BorderStroke(borderWidth, borderColor), shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                }
            )
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
                        style = MaterialTheme.typography.labelMedium,
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
                        style = MaterialTheme.typography.labelLarge,
                        color = InkBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Single Folder Title (no duplicate folder tag at the bottom)
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = InkBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Learnify-style Progress Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkCharcoal
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = InkBlack
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            NeoProgressBar(progress = progressPercent / 100f)
            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Action Row: Clean status on left + "Continue" pill on right
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
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(InkBlack)
                    )
                    Text(
                        text = if (progressPercent >= 100) "Completed" else "Active Folder",
                        style = MaterialTheme.typography.labelMedium,
                        color = InkCharcoal
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(11.dp))
                        .background(NeoCoral)
                        .border(1.5.dp, InkBlack, RoundedCornerShape(11.dp))
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.labelLarge,
                        color = PureWhiteSurface
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Folder",
                        tint = PureWhiteSurface,
                        modifier = Modifier.size(14.dp)
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
    shadowOffset: Dp = 0.dp
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pill_bg"
    )
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .background(bgColor, shape)
            .border(1.5.dp, InkBlack, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
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
                    modifier = Modifier.size(16.dp)
                )
                if (text.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
            if (text.isNotEmpty()) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp),
                    color = InkBlack,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Learnify-style Progress Bar: solid dark fill over a subtle track.
 */
@Composable
fun NeoProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    fillColor: Color = InkBlack,
    trackColor: Color = InkBlack.copy(alpha = 0.14f),
    height: Dp = 7.dp
) {
    val clamped = progress.coerceIn(0f, 1f)
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(trackColor, shape)
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
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(shape)
            .background(if (checked) accentColor else PureWhiteSurface, shape)
            .border(1.5.dp, InkBlack, shape)
            .clickable(role = Role.Checkbox, onClick = onCheckedChange),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                tint = InkBlack,
                modifier = Modifier.size(15.dp)
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
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (checked) accentColor else PureWhiteSurface, CircleShape)
            .border(1.8.dp, if (checked) InkBlack else accentColor, CircleShape)
            .clickable(role = Role.Checkbox, onClick = onCheckedChange),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                tint = InkBlack,
                modifier = Modifier.size(16.dp)
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
