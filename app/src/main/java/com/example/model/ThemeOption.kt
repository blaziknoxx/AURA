package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * Six luxury themes required for AURA:
 * 1. AURA Green
 * 2. Obsidian
 * 3. Midnight Blue
 * 4. Royal Violet
 * 5. Champagne Gold
 * 6. Arctic Silver
 */
enum class ThemeOption(
    val title: String,
    val subtitle: String,
    val primaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val surfaceVariantColor: Color,
    val accentColor: Color,
    val badgeBorderColor: Color,
    val goldColor: Color = Color(0xFFD4AF37)
) {
    AURA_GREEN(
        title = "AURA Green",
        subtitle = "Spotify-inspired deep emerald with charcoal surfaces and rich black",
        primaryColor = Color(0xFF1DB954),
        backgroundColor = Color(0xFF09090B),
        surfaceColor = Color(0xFF121216),
        surfaceVariantColor = Color(0xFF1A1A22),
        accentColor = Color(0xFF10B981),
        badgeBorderColor = Color(0xFF1DB954)
    ),

    OBSIDIAN(
        title = "Obsidian",
        subtitle = "Pure black depth, graphite brushed surfaces and platinum accents",
        primaryColor = Color(0xFFE5E5EA),
        backgroundColor = Color(0xFF000000),
        surfaceColor = Color(0xFF121212),
        surfaceVariantColor = Color(0xFF1E1E1E),
        accentColor = Color(0xFFFFFFFF),
        badgeBorderColor = Color(0xFF8E8E93)
    ),

    MIDNIGHT_BLUE(
        title = "Midnight Blue",
        subtitle = "Deep oceanic navy backgrounds with vivid electric blue luminescence",
        primaryColor = Color(0xFF38BDF8),
        backgroundColor = Color(0xFF060B14),
        surfaceColor = Color(0xFF0C1524),
        surfaceVariantColor = Color(0xFF132238),
        accentColor = Color(0xFF60A5FA),
        badgeBorderColor = Color(0xFF38BDF8)
    ),

    ROYAL_VIOLET(
        title = "Royal Violet",
        subtitle = "Dark velvet plum surfaces and sophisticated royal violet radiance",
        primaryColor = Color(0xFFC084FC),
        backgroundColor = Color(0xFF0A0512),
        surfaceColor = Color(0xFF150B24),
        surfaceVariantColor = Color(0xFF22113A),
        accentColor = Color(0xFFA855F7),
        badgeBorderColor = Color(0xFFC084FC)
    ),

    CHAMPAGNE_GOLD(
        title = "Champagne Gold",
        subtitle = "Near-black espresso slate with opulent champagne gold luxury details",
        primaryColor = Color(0xFFE2C474),
        backgroundColor = Color(0xFF0D0C09),
        surfaceColor = Color(0xFF181611),
        surfaceVariantColor = Color(0xFF24211A),
        accentColor = Color(0xFFD4AF37),
        badgeBorderColor = Color(0xFFD4AF37)
    ),

    ARCTIC_SILVER(
        title = "Arctic Silver",
        subtitle = "Cool brushed slate surfaces, titanium accents and crisp white typography",
        primaryColor = Color(0xFF94A3B8),
        backgroundColor = Color(0xFF0B0E11),
        surfaceColor = Color(0xFF13171C),
        surfaceVariantColor = Color(0xFF1E242B),
        accentColor = Color(0xFFE2E8F0),
        badgeBorderColor = Color(0xFF94A3B8)
    );

    companion object {
        fun fromName(name: String?): ThemeOption {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: AURA_GREEN
        }
    }
}
