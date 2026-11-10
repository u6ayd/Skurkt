package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val StruktColorScheme = lightColorScheme(
    primary = InkBlack,
    onPrimary = PureWhiteSurface,
    primaryContainer = NeoYellow,
    onPrimaryContainer = InkBlack,
    secondary = NeoSkyBlue,
    onSecondary = InkBlack,
    secondaryContainer = NeoMintGreen,
    onSecondaryContainer = InkBlack,
    tertiary = NeoCoral,
    onTertiary = InkBlack,
    background = PaperCanvas,
    onBackground = InkBlack,
    surface = PureWhiteSurface,
    onSurface = InkBlack,
    surfaceVariant = SoftSheetSurface,
    onSurfaceVariant = InkCharcoal,
    outline = InkBlack
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StruktColorScheme,
        typography = Typography,
        content = content
    )
}
