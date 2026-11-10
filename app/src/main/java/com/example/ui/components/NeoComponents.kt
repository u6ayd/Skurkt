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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.ui.theme.NeoMintGreen
import com.example.ui.theme.NeoYellow
import com.example.ui.theme.PaperCanvas
import com.example.ui.theme.PureWhiteSurface

/**
 * Single-pass Canvas dot-grid background matching the Trackerly Neo-Brutalist screenshots.
 * Zero recomposition overhead and battery-friendly.
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
                val stepPx = 22.dp.toPx()
                val radiusPx = 1.35.dp.toPx()
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
 * Neo-Brutalist Card with crisp 2.dp ink stroke and hard solid offset shadow.
 */
@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = PureWhiteSurface,
    borderColor: Color = InkBlack,
    cornerRadius: Dp = 18.dp,
    shadowOffset: Dp = 4.dp,
    borderWidth: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val shadowPx = shadowOffset
    Box(
        modifier = modifier
            .drawBehind {
                if (shadowPx > 0.dp) {
                    val offsetPx = shadowPx.toPx()
                    val radiusPx = cornerRadius.toPx()
                    drawRoundRect(
                        color = borderColor,
                        topLeft = Offset(offsetPx, offsetPx),
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(radiusPx, radiusPx)
                    )
                }
            }
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

/**
 * Folder-tabbed Neo-Brutalist card inspired directly by Screenshot 2 ("PHY 12", "CHEM 12").
 */
@Composable
fun NeoFolderCard(
    tabCode: String,
    title: String,
    subtitle: String,
    progressPercent: Int,
    tagLabel: String,
    fillColor: Color,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Folder top-left protruding tab
        Box(
            modifier = Modifier
                .width(136.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(fillColor)
                .border(
                    BorderStroke(2.dp, InkBlack),
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tabCode.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = InkBlack,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(InkBlack)
                )
            }
        }

        // Main Folder Body overlapping the tab bottom border
        NeoCard(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-2).dp),
            backgroundColor = fillColor,
            cornerRadius = 18.dp,
            shadowOffset = 4.dp,
            onClick = onCardClick,
            contentPadding = PaddingValues(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.displaySmall,
                            color = InkBlack,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = InkCharcoal
                        )
                    }
                    // Percentage Pill Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(PureWhiteSurface)
                            .border(2.dp, InkBlack, RoundedCornerShape(50))
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
                NeoProgressBar(progress = progressPercent / 100f)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bottom left white pill tag
                    Box(
                        modifier = Modifier
                            .drawBehind {
                                drawRoundRect(
                                    color = InkBlack,
                                    topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                                    size = size,
                                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                                )
                            }
                            .clip(RoundedCornerShape(10.dp))
                            .background(PureWhiteSurface)
                            .border(2.dp, InkBlack, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = (actionLabel ?: tagLabel).uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = InkBlack
                        )
                    }

                    // Bottom right circular arrow button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(InkBlack)
                            .border(2.dp, InkBlack, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Apply or Open",
                            tint = PureWhiteSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * High-contrast Neo-Brutalist pill button with 2.dp ink border and hard offset shadow.
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
    shadowOffset: Dp = 3.dp
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pill_bg"
    )
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .height(42.dp)
            .drawBehind {
                if (shadowOffset > 0.dp) {
                    val offsetPx = shadowOffset.toPx()
                    drawRoundRect(
                        color = InkBlack,
                        topLeft = Offset(offsetPx, offsetPx),
                        size = size,
                        cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                    )
                }
            }
            .clip(shape)
            .background(bgColor, shape)
            .border(2.dp, InkBlack, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
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
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp),
                color = InkBlack,
                maxLines = 1
            )
        }
    }
}

/**
 * Neo-Brutalist Progress Bar with white track and 1.5.dp ink outline (Screenshots 1, 2 & 4).
 */
@Composable
fun NeoProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    fillColor: Color = InkBlack,
    trackColor: Color = PureWhiteSurface,
    height: Dp = 10.dp
) {
    val clamped = progress.coerceIn(0f, 1f)
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(trackColor, shape)
            .border(1.5.dp, InkBlack, shape)
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

/**
 * Neo-Brutalist Square Checkbox (inspired by Screenshot 3's milestone matrix).
 */
@Composable
fun NeoSquareCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = NeoMintGreen
) {
    val shape = RoundedCornerShape(7.dp)
    Box(
        modifier = modifier
            .size(26.dp)
            .clip(shape)
            .background(if (checked) accentColor else PureWhiteSurface, shape)
            .border(2.dp, InkBlack, shape)
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

/**
 * Hybrid Structured × Neo-Brutalist circular completion checkbox (Screenshots 5 & 6).
 */
@Composable
fun NeoCircleCheckbox(
    checked: Boolean,
    accentColor: Color,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(if (checked) accentColor else PureWhiteSurface, CircleShape)
            .border(2.dp, if (checked) InkBlack else accentColor, CircleShape)
            .clickable(role = Role.Checkbox, onClick = onCheckedChange),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                tint = InkBlack,
                modifier = Modifier.size(17.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, InkBlack.copy(alpha = 0.25f), CircleShape)
            )
        }
    }
}

/**
 * Dotted divider line matching Screenshot 1 ("Academic Master Tracker" header divider).
 */
@Composable
fun NeoDashedDivider(
    modifier: Modifier = Modifier,
    color: Color = InkBlack.copy(alpha = 0.2f)
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
    TaskIconOption("ALARM", "Wake / Alarm", Icons.Default.AccessAlarm),
    TaskIconOption("WORKOUT", "Workout", Icons.Default.SelfImprovement),
    TaskIconOption("FITNESS", "Gym / Run", Icons.Default.FitnessCenter),
    TaskIconOption("BIKE", "Commute", Icons.AutoMirrored.Filled.DirectionsBike),
    TaskIconOption("BOOK", "Study / Read", Icons.AutoMirrored.Filled.MenuBook),
    TaskIconOption("SCIENCE", "Lab / Physics", Icons.Default.Science),
    TaskIconOption("CODE", "Deep Work", Icons.Default.Code),
    TaskIconOption("MEETING", "Team / Call", Icons.Default.Groups),
    TaskIconOption("COFFEE", "Break / Coffee", Icons.Default.Coffee),
    TaskIconOption("PLANT", "Chores / Home", Icons.Default.Eco),
    TaskIconOption("CALL", "Phone Call", Icons.Default.Call),
    TaskIconOption("LAUNDRY", "Laundry", Icons.Default.LocalLaundryService),
    TaskIconOption("SHOWER", "Refresh", Icons.Default.Shower),
    TaskIconOption("CHECKLIST", "Routine", Icons.Default.Checklist),
    TaskIconOption("BOLT", "Quick Sprint", Icons.Default.Bolt),
    TaskIconOption("TASK", "Task", Icons.Default.TaskAlt)
)

fun iconForKey(key: String): ImageVector {
    return TaskIconCatalog.firstOrNull { it.key.equals(key, ignoreCase = true) }?.icon
        ?: Icons.Default.TaskAlt
}
