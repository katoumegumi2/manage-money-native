package com.taufik.managemoney.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Palet v3: permukaan terang bersih, aksen emerald tunggal, dua kartu gelap sebagai jangkar.
private val LightColors = lightColorScheme(
    primary = Emerald,
    onPrimary = Color.White,
    primaryContainer = EmeraldSoftBg,
    onPrimaryContainer = EmeraldSoftText,
    secondary = Emerald,
    onSecondary = Color.White,
    secondaryContainer = EmeraldSoftBg,
    onSecondaryContainer = EmeraldSoftText,
    background = Bg,
    onBackground = Ink,
    surface = CardLight,
    onSurface = Ink,
    surfaceVariant = Surface1,
    onSurfaceVariant = Muted,
    surfaceContainerLowest = CardLight,
    surfaceContainerLow = Surface2,
    surfaceContainer = Surface2,
    surfaceContainerHigh = Field,
    surfaceContainerHighest = Field,
    outline = Color(0xFFE4E7EC),
    outlineVariant = Hairline,
    error = Danger,
    onError = Color.White,
    errorContainer = DangerSoftBg,
    onErrorContainer = Danger,
    inverseSurface = Ink,
    inverseOnSurface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Emerald,
    onPrimary = Color.White,
    primaryContainer = EmeraldSoftBgDark,
    onPrimaryContainer = EmeraldSoftTextDark,
    secondary = EmeraldBright,
    onSecondary = Ink,
    secondaryContainer = EmeraldSoftBgDark,
    onSecondaryContainer = EmeraldSoftTextDark,
    background = DarkBg,
    onBackground = OnDark,
    surface = CardDark,
    onSurface = OnDark,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = MutedDark,
    surfaceContainerLowest = DarkBg,
    surfaceContainerLow = CardDark,
    surfaceContainer = CardDark,
    surfaceContainerHigh = DarkField,
    surfaceContainerHighest = DarkField,
    outline = LineDark,
    outlineVariant = LineDark,
    error = Danger,
    onError = Color.White,
    errorContainer = DangerSoftBgDark,
    onErrorContainer = ExpenseOnDark,
    inverseSurface = OnDark,
    inverseOnSurface = Ink,
)

@Composable
fun ManageMoneyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
