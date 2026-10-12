package com.example.ui.theme

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.model.ThemeOption

val LocalAuraColors = staticCompositionLocalOf {
    ThemeOption.AURA_GREEN.toAuraColors()
}

object AuraTheme {
    val colors: AuraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAuraColors.current
}

@Composable
fun AuraAppTheme(
    selectedTheme: ThemeOption = ThemeOption.AURA_GREEN,
    content: @Composable () -> Unit
) {
    val auraColors = selectedTheme.toAuraColors()

    val materialColorScheme = darkColorScheme(
        primary = auraColors.primary,
        onPrimary = auraColors.onPrimary,
        primaryContainer = auraColors.surfaceVariant,
        onPrimaryContainer = auraColors.primary,
        secondary = auraColors.accent,
        onSecondary = Color.Black,
        background = auraColors.background,
        onBackground = auraColors.onBackground,
        surface = auraColors.surface,
        onSurface = auraColors.onSurface,
        surfaceVariant = auraColors.surfaceVariant,
        onSurfaceVariant = auraColors.onSurfaceVariant,
        outline = auraColors.badgeBorder,
        outlineVariant = auraColors.divider
    )

    CompositionLocalProvider(LocalAuraColors provides auraColors) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * SharedPreferences storage for theme persistence.
 */
object ThemePreferences {
    private const val PREFS_NAME = "aura_settings"
    private const val KEY_SELECTED_THEME = "key_selected_theme"

    fun getSelectedTheme(context: Context): ThemeOption {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_SELECTED_THEME, ThemeOption.AURA_GREEN.name)
        return ThemeOption.fromName(name)
    }

    fun setSelectedTheme(context: Context, theme: ThemeOption) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SELECTED_THEME, theme.name).apply()
    }
}
