package com.esdispatch.util

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.esdispatch.ui.theme.Gold
import com.esdispatch.ui.theme.Charcoal
import com.esdispatch.ui.theme.GoldenWhiteLight

/**
 * Standard luxury image painter with smooth 300ms crossfade transitions.
 * Eliminates jarring 0ms image popping across product cards, avatars, and hero carousels.
 */
@Composable
fun rememberCrossfadePainter(
    url: String?,
    crossfadeDurationMs: Int = 300
): Painter {
    val context = LocalContext.current
    return rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(url?.takeIf { it.isNotBlank() })
            .crossfade(crossfadeDurationMs)
            .build()
    )
}

/**
 * Luxury shimmer skeleton placeholder for downloading images and loading cards.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    isDark: Boolean = true
) {
    val shimmerColors = if (isDark) {
        listOf(
            Color(0xFF1E1E24),
            Color(0xFF2C2C34),
            Color(0xFF1E1E24)
        )
    } else {
        listOf(
            Color(0xFFEBE3D0),
            Color(0xFFF7F2E7),
            Color(0xFFEBE3D0)
        )
    }

    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 300f, translateAnim + 300f)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}
