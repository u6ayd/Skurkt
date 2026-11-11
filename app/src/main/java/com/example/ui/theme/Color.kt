package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Learnify × Structured High-Contrast Warm Paper & Ink Tokens
val PaperCanvas = Color(0xFFFAF8F5)
val PureWhiteSurface = Color(0xFFFFFFFF)
val SoftSheetSurface = Color(0xFFF3F1EC)
val DotGridColor = Color(0xFFDDD8D0)
val InkBlack = Color(0xFF18181B)
val InkCharcoal = Color(0xFF27272A)
val InkMuted = Color(0xFF71717A)
val DividerStroke = Color(0xFFE4E2DD)
val NavRailDark = Color(0xFF222225)

// High-Contrast Learnify Pastel & Accent Palette
val NeoCoral = Color(0xFFF26444)
val NeoSkyBlue = Color(0xFFC5EAFF)
val NeoMintGreen = Color(0xFFA7F3D0)
val NeoYellow = Color(0xFFFCE073)
val NeoRosePink = Color(0xFFFBCFE8)
val NeoTangerine = Color(0xFFFED7AA)
val NeoLavender = Color(0xFFDEC7FF)
val NeoSage = Color(0xFFBBF7D0)

data class NeoColorOption(
    val key: String,
    val label: String,
    val fill: Color,
    val softFill: Color
)

val NeoPalette = listOf(
    NeoColorOption("YELLOW", "Sunny Gold", NeoYellow, Color(0xFFFEF9E7)),
    NeoColorOption("LAVENDER", "Lavender", NeoLavender, Color(0xFFF6F0FF)),
    NeoColorOption("SKY", "Sky Blue", NeoSkyBlue, Color(0xFFEFF8FF)),
    NeoColorOption("CORAL", "Coral Orange", NeoCoral, Color(0xFFFFF1EE)),
    NeoColorOption("MINT", "Mint Green", NeoMintGreen, Color(0xFFECFDF5)),
    NeoColorOption("SAGE", "Soft Green", NeoSage, Color(0xFFF0FDF4)),
    NeoColorOption("PINK", "Soft Rose", NeoRosePink, Color(0xFFFDF2F8)),
    NeoColorOption("ORANGE", "Warm Peach", NeoTangerine, Color(0xFFFFF7ED))
)

fun neoColorForKey(key: String): Color {
    return NeoPalette.firstOrNull { it.key.equals(key, ignoreCase = true) }?.fill ?: NeoYellow
}

fun neoSoftColorForKey(key: String): Color {
    return NeoPalette.firstOrNull { it.key.equals(key, ignoreCase = true) }?.softFill ?: Color(0xFFFEF9E7)
}
