package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Learnify × Structured High-Contrast Warm Paper & Ink Tokens
val PaperCanvas = Color(0xFFFAF8F5)
val PureWhiteSurface = Color(0xFFFFFFFF)
val SoftSheetSurface = Color(0xFFF3F1EC)
val DotGridColor = Color(0xFFDDD8D0)
val InkBlack = Color(0xFF1A1A1A)
val InkCharcoal = Color(0xFF27272A)
val InkMuted = Color(0xFF71717A)
val DividerStroke = Color(0xFFE4E2DD)
val NavRailDark = Color(0xFF1A1A1A)

// Vibrant Soft Neo-Brutalist Pastel & Accent Palette
val NeoYellow = Color(0xFFFFE156)       // 1. Sunny Yellow (#FFE156)
val NeoSkyBlue = Color(0xFF99D6FF)      // 2. Vibrant Sky Blue (#99D6FF)
val NeoMintGreen = Color(0xFF7BF1A8)    // 3. Fresh Mint Green (#7BF1A8)
val NeoSage = Color(0xFF7BF1A8)         // 3. Fresh Mint Green (#7BF1A8)
val NeoCoral = Color(0xFFFF5E45)        // 4. Energetic Coral / Orange (#FF5E45)
val NeoTangerine = Color(0xFFFFC48C)    // 5. Warm Peach (#FFC48C)
val NeoRosePink = Color(0xFFFFADD6)     // 6. Bright Pastel Pink (#FFADD6)
val NeoLavender = Color(0xFFD6B8FF)     // Vibrant Pastel Lavender

data class NeoColorOption(
    val key: String,
    val label: String,
    val fill: Color,
    val softFill: Color
)

val NeoPalette = listOf(
    NeoColorOption("YELLOW", "Sunny Gold", NeoYellow, Color(0xFFFFFBEB)),
    NeoColorOption("LAVENDER", "Lavender", NeoLavender, Color(0xFFF6F0FF)),
    NeoColorOption("SKY", "Sky Blue", NeoSkyBlue, Color(0xFFEBF6FF)),
    NeoColorOption("CORAL", "Coral Orange", NeoCoral, Color(0xFFFFEFEA)),
    NeoColorOption("MINT", "Mint Green", NeoMintGreen, Color(0xFFE8FCF0)),
    NeoColorOption("SAGE", "Soft Green", NeoSage, Color(0xFFE8FCF0)),
    NeoColorOption("PINK", "Soft Rose", NeoRosePink, Color(0xFFFFEBF5)),
    NeoColorOption("ORANGE", "Warm Peach", NeoTangerine, Color(0xFFFFF4E6))
)

fun neoColorForKey(key: String): Color {
    return NeoPalette.firstOrNull { it.key.equals(key, ignoreCase = true) }?.fill ?: NeoYellow
}

fun neoSoftColorForKey(key: String): Color {
    return NeoPalette.firstOrNull { it.key.equals(key, ignoreCase = true) }?.softFill ?: Color(0xFFFFFBEB)
}
