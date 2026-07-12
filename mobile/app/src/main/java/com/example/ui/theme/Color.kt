package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color

// Premium Gold & Light Palette (Elegant light theme)
val Gold = Color(0xFFFFB800)
val GoldDark = Color(0xFFD4AF37)
val GoldLight = Color(0xFFFFF3D6)
val GoldContainer = Color(0xFFFFE082)

// Subtle golden-white palette
val GoldenWhite = Color(0xFFFAF7EC) // Smooth warm golden-white background
val GoldenWhiteLight = Color(0xFFFFFDF5) // Radiant warm golden-white for cards/surfaces

// Obsidian is used for dark text and major headings
val Obsidian = Color(0xFF121212)

// Charcoal is white for cards/surfaces in light mode, dark gray in dark mode
val Charcoal: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.background == BackgroundDark) Color(0xFF1E1E1E) else GoldenWhiteLight

// Slate is a light grayish color for borders/dividers - updated to a warm gold-tinted gray
val Slate = Color(0xFFE4DFD0)

// LuxuryBlack is a soft off-white for page backgrounds
val LuxuryBlack: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.background == BackgroundDark) BackgroundDark else GoldenWhite

// BackgroundGray is light gray for sheets and containers - updated to GoldenWhite
val BackgroundGray = GoldenWhite

val BackgroundLight = GoldenWhite
val BackgroundDark = Color(0xFF121212) // Clean, deep black

val OnSurfaceDark = Color(0xFFFFFFFF) // White text in dark mode
val OnSurfaceLight = Color(0xFF1C1C1E)
val TextGray = Color(0xFF6B7280) // Darker gray for better contrast

val AppTextColor: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.background == BackgroundDark) Color.White else Obsidian

val HeaderBgColor: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.background == BackgroundDark) Gold else Obsidian

// Map Canvas Theme Tokens
val MapSatelliteBg = Color(0xFF0F151B)
val MapSatelliteGrid = Color(0xFF1B2A36)
val MapStandardBg = Color(0xFF161616)
val MapBlock = Color(0xFF1C1C1C)
val MapPark = Color(0xFF1E2822)
val MapRoad = Color(0xFF262626)
val MapRoadSatellite = Color(0xFF1C2D3A)
val MapSecondaryRoad = Color(0xFF1E1E1E)
val MapSecondaryRoadSatellite = Color(0xFF15222E)
val SuccessGreen = Color(0xFF34C759)
val ErrorRed = Color(0xFFFF3B30)
val TrafficYellow = Color(0xFFFFCC00)
val WarningOrange = Color(0xFFFF9800)

// Named Border and Divider Tokens
val BorderDark = Color(0xFF222222)
val BorderLight = Color(0xFFE5E7EB)

val BorderColor: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.background == BackgroundDark) Color(0xFF222222) else Slate

val DividerColor: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.background == BackgroundDark) Color(0xFF2E2E2E) else Color(0xFFE5E7EB)




