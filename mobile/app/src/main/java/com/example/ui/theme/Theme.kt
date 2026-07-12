package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Gold,
    secondary = GoldDark,
    tertiary = GoldLight,
    background = BackgroundDark,
    surface = Color(0xFF1E1E1E),
    onPrimary = Obsidian,
    onSecondary = OnSurfaceDark,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Gold,
    secondary = GoldDark,
    tertiary = GoldLight,
    background = BackgroundLight,
    surface = GoldenWhiteLight,
    onPrimary = Obsidian,
    onSecondary = OnSurfaceLight,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  // Override dynamicColor to false by default to preserve the gorgeous Gold visual theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography) {
    val defaultStyle = androidx.compose.ui.text.TextStyle(
      fontFamily = Poppins
    )
    val currentDensity = androidx.compose.ui.platform.LocalDensity.current
    androidx.compose.runtime.CompositionLocalProvider(
      androidx.compose.material3.LocalTextStyle provides defaultStyle,
      androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(
        density = currentDensity.density,
        fontScale = 1f
      ),
      content = content
    )
  }
}

val AppBackground: Color
    @Composable
    get() = MaterialTheme.colorScheme.background

val AppSurface: Color
    @Composable
    get() = MaterialTheme.colorScheme.surface

val AppOnSurface: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurface

