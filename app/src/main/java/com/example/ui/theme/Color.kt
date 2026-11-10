package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Neo-Brutalist Canvas & Ink Tokens (Inspired by Trackerly + Structured screenshots)
val PaperCanvas = Color(0xFFFAF9F5)
val PureWhiteSurface = Color(0xFFFFFFFF)
val SoftSheetSurface = Color(0xFFF4F3EE)
val DotGridColor = Color(0xFFD6DCE5)
val InkBlack = Color(0xFF0F172A)
val InkCharcoal = Color(0xFF1E293B)
val InkMuted = Color(0xFF64748B)
val DividerStroke = Color(0xFFE2E8F0)

// High-Contrast Candy Pastel Neo-Brutalist Palette
val NeoYellow = Color(0xFFFDE047)
val NeoSkyBlue = Color(0xFF7DD3FC)
val NeoMintGreen = Color(0xFF77F2A1)
val NeoRosePink = Color(0xFFF9A8D4)
val NeoTangerine = Color(0xFFFDBA74)
val NeoCoral = Color(0xFFFF9B85)
val NeoLavender = Color(0xFFC4B5FD)
val NeoSage = Color(0xFFA7F3D0)

data class NeoColorOption(
    val key: String,
    val label: String,
    val fill: Color,
    val softFill: Color
)

val NeoPalette = listOf(
    NeoColorOption("SKY", "Sky Cyan", NeoSkyBlue, Color(0xFFE0F2FE)),
    NeoColorOption("YELLOW", "Butter Gold", NeoYellow, Color(0xFFFEF9C3)),
    NeoColorOption("MINT", "Mint Emerald", NeoMintGreen, Color(0xFFD1FAE5)),
    NeoColorOption("PINK", "Bubblegum Rose", NeoRosePink, Color(0xFFFCE7F3)),
    NeoColorOption("ORANGE", "Tangerine", NeoTangerine, Color(0xFFFFEDD5)),
    NeoColorOption("CORAL", "Structured Coral", NeoCoral, Color(0xFFFFE4E6)),
    NeoColorOption("LAVENDER", "Lavender Pop", NeoLavender, Color(0xFFEDE9FE))
)

fun neoColorForKey(key: String): Color {
    return NeoPalette.firstOrNull { it.key.equals(key, ignoreCase = true) }?.fill ?: NeoSkyBlue
}

fun neoSoftColorForKey(key: String): Color {
    return NeoPalette.firstOrNull { it.key.equals(key, ignoreCase = true) }?.softFill ?: Color(0xFFE0F2FE)
}
