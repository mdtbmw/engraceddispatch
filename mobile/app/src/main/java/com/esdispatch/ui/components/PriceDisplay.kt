package com.esdispatch.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.esdispatch.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

enum class PriceVariant {
    COMPACT,        // ₦2,500 (no decimals)
    STANDARD,       // ₦2,500.00
    DISPLAY_METRIC, // 28sp heavy amount
    MUTED           // Subtle secondary fee
}

/**
 * Standardized PriceDisplay component for Nigerian Naira (₦).
 * Section 12.1 & Section 5: Tabular figures with clear hierarchy.
 */
@Composable
fun PriceDisplay(
    amount: Double,
    modifier: Modifier = Modifier,
    variant: PriceVariant = PriceVariant.COMPACT,
    customColor: Color? = null,
    showSign: Boolean = false
) {
    val dark = isDarkTheme

    val formattedNumber = when (variant) {
        PriceVariant.COMPACT, PriceVariant.DISPLAY_METRIC -> {
            val formatter = NumberFormat.getNumberInstance(Locale.US)
            formatter.maximumFractionDigits = 0
            formatter.format(amount)
        }
        PriceVariant.STANDARD, PriceVariant.MUTED -> {
            val formatter = NumberFormat.getNumberInstance(Locale.US)
            formatter.minimumFractionDigits = 2
            formatter.maximumFractionDigits = 2
            formatter.format(amount)
        }
    }

    val (fontSize, fontWeight, fontFamily, color) = when (variant) {
        PriceVariant.COMPACT -> Tuple4(
            15.sp,
            FontWeight.Bold,
            Poppins,
            customColor ?: AppTextColor
        )
        PriceVariant.STANDARD -> Tuple4(
            16.sp,
            FontWeight.Bold,
            Poppins,
            customColor ?: AppTextColor
        )
        PriceVariant.DISPLAY_METRIC -> Tuple4(
            28.sp,
            FontWeight.Black,
            Poppins,
            customColor ?: (if (dark) Gold else Obsidian)
        )
        PriceVariant.MUTED -> Tuple4(
            12.sp,
            FontWeight.Medium,
            Poppins,
            customColor ?: TextGray
        )
    }

    val signPrefix = if (showSign && amount > 0) "+" else ""

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${signPrefix}₦$formattedNumber",
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            color = color,
            letterSpacing = (-0.3).sp,
            maxLines = 1
        )
    }
}

private data class Tuple4<A, B, C, D>(
    val a: A,
    val b: B,
    val c: C,
    val d: D
)
