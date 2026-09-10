package com.esdispatch.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ESDispatch Design System Spacing Scale
 * Defined in section 6 of ESDispatch Design System Recommendations.
 */
object SpacingTokens {
    /** 4dp - Micro gaps between icon and text */
    val xs: Dp = 4.dp
    /** 8dp - Compact inner spacing, chip padding */
    val sm: Dp = 8.dp
    /** 12dp - Related card item spacing, card inner group spacing */
    val md: Dp = 12.dp
    /** 16dp - Default component and inner card padding */
    val lg: Dp = 16.dp
    /** 20dp - Roomy card padding, section internal spacing */
    val xl: Dp = 20.dp
    /** 24dp - Screen horizontal margins and major card padding */
    val `2xl`: Dp = 24.dp
    /** 32dp - Section separation */
    val `3xl`: Dp = 32.dp
    /** 40dp - Hero and header separation */
    val `4xl`: Dp = 40.dp
}

/**
 * ESDispatch Design System Radius Scale
 * Defined in section 7 of ESDispatch Design System Recommendations.
 */
object RadiusTokens {
    /** 8dp - Tiny status badges */
    val sm: Dp = 8.dp
    /** 12dp - Chips, small controls, inputs */
    val md: Dp = 12.dp
    /** 16dp - Buttons, compact cards */
    val lg: Dp = 16.dp
    /** 20dp - Secondary/compact cards */
    val xl: Dp = 20.dp
    /** 24dp - Primary product and shipment cards */
    val `2xl`: Dp = 24.dp
    /** 32dp - Bottom sheets, hero surfaces */
    val `3xl`: Dp = 32.dp
    /** Custom 28dp - Floating capsule dock */
    val nav: Dp = 28.dp
}

/**
 * ESDispatch Elevation Scale
 * Defined in section 8 of ESDispatch Design System Recommendations.
 * Note: Shadows are kept flat/0dp on standard cards to prevent muddy drop shadows.
 */
object ElevationTokens {
    val none: Dp = 0.dp
    val bordered: Dp = 0.dp
    val soft: Dp = 4.dp
    val active: Dp = 8.dp
}

/**
 * ESDispatch Semantic Typography Scale
 * Aligned with section 5.1 of ESDispatch Design System Recommendations.
 */
object ESTypography {
    val display = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Black,
        fontSize = 28.sp,
        lineHeight = 34.sp
    )

    val screenTitle = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    )

    val sectionTitle = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    )

    val cardTitle = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    )

    val body = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )

    val bodySmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )

    val caption = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 15.sp
    )

    val micro = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp
    )
}
