package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Clean Structured-Inspired Warm Canvas & Crisp Ink Tokens
val PaperCanvas = Color(0xFFFBF8F6)
val PureWhiteSurface = Color(0xFFFFFFFF)
val SoftSheetSurface = Color(0xFFF3F1ED)
val DotGridColor = Color(0xFFE5E0DC)
val InkBlack = Color(0xFF141824)
val InkCharcoal = Color(0xFF334155)
val InkMuted = Color(0xFF78849B)
val DividerStroke = Color(0xFFE8E5E0)

// Vibrant Structured × Neo-Brutalist Candy Palette
val NeoCoral = Color(0xFFF69A8E)
val NeoSkyBlue = Color(0xFF6EC6F5)
val NeoMintGreen = Color(0xFF7BE3A6)
val NeoYellow = Color(0xFFFCD34D)
val NeoRosePink = Color(0xFFF7A3C9)
val NeoTangerine = Color(0xFFFBAD6B)
val NeoLavender = Color(0xFFB9A6FA)
val NeoSage = Color(0xFF8CC572)

data class NeoColorOption(
    val key: String,
    val label: String,
    val fill: Color,
    val softFill: Color
)

val NeoPalette = listOf(
    NeoColorOption("CORAL", "Coral", NeoCoral, Color(0xFFFFF0ED)),
    NeoColorOption("SKY", "Sky Blue", NeoSkyBlue, Color(0xFFEAF6FE)),
    NeoColorOption("MINT", "Mint Green", NeoMintGreen, Color(0xFFEAFBF1)),
    NeoColorOption("SAGE", "Structured Green", NeoSage, Color(0xFFEEF7E8)),
    NeoColorOption("YELLOW", "Sunny Gold", NeoYellow, Color(0xFFFEF9E6)),
    NeoColorOption("PINK", "Rose Pink", NeoRosePink, Color(0xFFFDF0F6)),
    NeoColorOption("ORANGE", "Tangerine", NeoTangerine, Color(0xFFFFF3E8)),
    NeoColorOption("LAVENDER", "Lavender", NeoLavender, Color(0xFFF3F0FF))
)

fun neoColorForKey(key: String): Color {
    return NeoPalette.firstOrNull { it.key.equals(key, ignoreCase = true) }?.fill ?: NeoCoral
}

fun neoSoftColorForKey(key: String): Color {
    return NeoPalette.firstOrNull { it.key.equals(key, ignoreCase = true) }?.softFill ?: Color(0xFFFFF0ED)
}
