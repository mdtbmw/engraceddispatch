package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BiroBlue,
    onPrimary = Color.White,
    secondary = Color(0xFF5C58FF),
    background = BackgroundGray,
    surface = CardBackgroundLight,
    onBackground = TextMain,
    onSurface = TextMain,
    error = DangerRed,
    outline = CardBorderGray
)

private val DarkColorScheme = darkColorScheme(
    primary = BiroBlue,
    onPrimary = Color.White,
    secondary = Color(0xFF5C58FF),
    background = Color(0xFF0F1118),
    surface = Color(0xFF1A1D2E),
    onBackground = Color(0xFFE8E9ED),
    onSurface = Color(0xFFE8E9ED),
    error = DangerRed,
    outline = Color(0xFF2E3140),
    surfaceVariant = Color(0xFF252838),
    onSurfaceVariant = Color(0xFFA0A3B0)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
