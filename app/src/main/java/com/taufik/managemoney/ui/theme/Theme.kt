package com.taufik.managemoney.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Gold,
    onPrimary = Color.White,
    secondary = Transfer,
    background = Paper,
    onBackground = Ink,
    surface = CardLight,
    onSurface = Ink,
    surfaceVariant = Paper,
    onSurfaceVariant = Muted,
    outline = Line,
    error = Expense,
)

private val DarkColors = darkColorScheme(
    primary = Gold,
    onPrimary = Ink,
    secondary = Transfer,
    background = InkDark,
    onBackground = OnDark,
    surface = CardDark,
    onSurface = OnDark,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = MutedDark,
    outline = LineDark,
    error = Expense,
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
