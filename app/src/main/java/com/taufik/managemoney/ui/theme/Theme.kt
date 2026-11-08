package com.taufik.managemoney.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.taufik.managemoney.data.settings.FlowColorMode

/** colorScheme M3 diturunkan dari [AppColors] supaya komponen bawaan (TextField, Switch, dst.) ikut aksen. */
private fun schemeOf(c: AppColors): ColorScheme {
    val base = if (c.isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = c.accent, onPrimary = c.onAccent,
        primaryContainer = c.accentSoft, onPrimaryContainer = c.accentText,
        secondary = c.accent, onSecondary = c.onAccent,
        secondaryContainer = c.accentSoft, onSecondaryContainer = c.accentText,
        tertiary = c.accent, onTertiary = c.onAccent,
        background = c.bg, onBackground = c.ink,
        surface = c.card, onSurface = c.ink,
        surfaceVariant = c.field, onSurfaceVariant = c.muted,
        surfaceTint = Color.Transparent,
        surfaceContainerLowest = c.card, surfaceContainerLow = c.card, surfaceContainer = c.card,
        surfaceContainerHigh = if (c.isDark) Color(0xFF1C2029) else Color(0xFFF1F3F7),
        surfaceContainerHighest = if (c.isDark) Color(0xFF232733) else Color(0xFFEDF0F4),
        outline = if (c.isDark) Color(0x24FFFFFF) else Color(0xFFDDE2E9), outlineVariant = c.line,
        error = c.danger, onError = Color.White,
        errorContainer = c.danger.copy(alpha = 0.14f), onErrorContainer = c.danger,
        inverseSurface = c.ink, inverseOnSurface = c.bg,
        scrim = Color(0x99000000),
    )
}

@Composable
fun ManageMoneyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accent: Color = DefaultAccent,
    flowColor: FlowColorMode = FlowColorMode.FOLLOW_ACCENT,
    content: @Composable () -> Unit,
) {
    val colors = remember(darkTheme, accent, flowColor) { appColors(accent, darkTheme, flowColor) }
    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = schemeOf(colors),
            typography = AppTypography,
            content = content,
        )
    }
}
