package com.esdispatch.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esdispatch.data.DeliveryStatusDisplay
import com.esdispatch.data.ParcelStatus
import com.esdispatch.ui.theme.Poppins

enum class StatusBadgeSize {
    COMPACT,
    DEFAULT,
    LARGE
}

/**
 * Standardized StatusBadge component for ESDispatch.
 * Conforms to ESDispatch Design System Section 4.2 & Section 12.1.
 */
@Composable
fun StatusBadge(
    modifier: Modifier = Modifier,
    status: ParcelStatus? = null,
    statusString: String? = null,
    size: StatusBadgeSize = StatusBadgeSize.DEFAULT,
    useAdminLabel: Boolean = false,
    showLivePulse: Boolean = true
) {
    val display = when {
        status != null -> DeliveryStatusDisplay.fromParcelStatus(status)
        !statusString.isNullOrBlank() -> DeliveryStatusDisplay.fromString(statusString)
        else -> DeliveryStatusDisplay.fromParcelStatus(ParcelStatus.PENDING)
    }

    val label = if (useAdminLabel) display.adminLabel else display.customerLabel

    val (horizontalPad, verticalPad, fontSize, cornerRadius, dotSize) = when (size) {
        StatusBadgeSize.COMPACT -> Tuple5(6.dp, 2.dp, 11.sp, 6.dp, 5.dp)
        StatusBadgeSize.DEFAULT -> Tuple5(9.dp, 4.dp, 12.sp, 8.dp, 6.dp)
        StatusBadgeSize.LARGE -> Tuple5(12.dp, 6.dp, 13.sp, 10.dp, 7.dp)
    }

    val containerColor = display.containerColor()
    val contentColor = display.contentColor()
    val borderColor = display.borderColor()

    // Breathing beacon animation for live states
    val infiniteTransition = rememberInfiniteTransition(label = "badgePulse")
    val pulseAlpha by if (display.isLive && showLivePulse) {
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "badgePulseAlpha"
        )
    } else {
        rememberUpdatedState(1.0f)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = horizontalPad, vertical = verticalPad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (display.isLive && showLivePulse) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(contentColor.copy(alpha = pulseAlpha))
                )
                Spacer(modifier = Modifier.width(5.dp))
            }

            Text(
                text = label,
                fontSize = fontSize,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                letterSpacing = 0.2.sp,
                maxLines = 1
            )
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E
)
