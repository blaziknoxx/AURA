package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.model.ThemeOption

@Immutable
data class AuraColors(
    val primary: Color,
    val onPrimary: Color = Color.Black,
    val background: Color,
    val onBackground: Color = Color(0xFFEDEDED),
    val surface: Color,
    val onSurface: Color = Color(0xFFEDEDED),
    val surfaceVariant: Color,
    val onSurfaceVariant: Color = Color(0xFFA1A1AA),
    val accent: Color,
    val badgeBorder: Color,
    val gold: Color = Color(0xFFD4AF37),
    val hiResGlow: Color = Color(0xFFF59E0B),
    val cdQualityDisc: Color = Color(0xFF38BDF8),
    val divider: Color = Color(0xFF27272A),
    val cardBackground: Color
)

fun ThemeOption.toAuraColors(): AuraColors {
    return AuraColors(
        primary = this.primaryColor,
        onPrimary = if (this == ThemeOption.OBSIDIAN || this == ThemeOption.ARCTIC_SILVER) Color.Black else Color.White,
        background = this.backgroundColor,
        surface = this.surfaceColor,
        surfaceVariant = this.surfaceVariantColor,
        accent = this.accentColor,
        badgeBorder = this.badgeBorderColor,
        gold = this.goldColor,
        cardBackground = this.surfaceColor
    )
}
