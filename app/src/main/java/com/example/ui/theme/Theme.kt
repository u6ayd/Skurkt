package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

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
    val configuration = LocalConfiguration.current
    val shortestWidthDp = minOf(configuration.screenWidthDp, configuration.screenHeightDp)
    val uiScale = if (shortestWidthDp >= 600) 1.24f else 1.0f
    val responsiveTypography = remember(uiScale) { scaledTypography(uiScale) }

    CompositionLocalProvider(LocalUiScale provides uiScale) {
        MaterialTheme(
            colorScheme = StruktColorScheme,
            typography = responsiveTypography,
            content = content
        )
    }
}
