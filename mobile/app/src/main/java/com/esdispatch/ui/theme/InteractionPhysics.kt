package com.esdispatch.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * ESDispatch Standardized Spring Dynamics & Interaction Physics Tokens.
 * These springs provide a weighted, soft, luxury physical tactile feel.
 */
object SpringPhysics {
    // Snappy, immediate feedback for touch down-press and button engagement
    val TouchPress = spring<Float>(
        dampingRatio = 0.70f,
        stiffness = 400f
    )

    // Soft elastic settle for cards, drawers, and modal expansions
    val SoftElastic = spring<Float>(
        dampingRatio = 0.75f,
        stiffness = 280f
    )

    // Spatial sliding pill for floating capsule dock indicator with zero overshoot
    val SnappyPill = spring<Float>(
        dampingRatio = 0.85f,
        stiffness = 450f
    )

    // Elastic boundary resistance for overscroll and rubber-banding
    val RubberBand = spring<Float>(
        dampingRatio = 0.60f,
        stiffness = 220f
    )

    // Resonant signature moment pulse for dispatch send, arrival alert, and escrow release
    val SignatureMoment = spring<Float>(
        dampingRatio = 0.65f,
        stiffness = 180f
    )
}

/**
 * Tactile touch response modifier.
 * Applies a physical 3D scale compression upon touch down with haptic feedback,
 * and releases smoothly with spring physics on touch up.
 */
fun Modifier.tactilePress(
    scaleDown: Float = 0.96f,
    haptic: Boolean = true,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    if (!enabled) return@composed this

    val hapticFeedback = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1.0f,
        animationSpec = SpringPhysics.TouchPress,
        label = "tactileScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(enabled) {
            while (true) {
                awaitPointerEventScope {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    if (haptic) {
                        try {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        } catch (_: Exception) {}
                    }
                    val upOrCancel = waitForUpOrCancellation()
                    isPressed = false
                    if (upOrCancel != null && onClick != null) {
                        onClick()
                    }
                }
            }
        }
}

/**
 * Subtle breathing pulse modifier for live telemetry markers and active status beacons.
 * Recreates high-accuracy GPS heartbeat without layout recalculations.
 */
fun Modifier.breathingPulse(
    active: Boolean = true,
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f,
    durationMs: Int = 1800
): Modifier = composed {
    if (!active) return@composed this

    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Ambient Gold glow aura modifier for verified stores, arrived status, and signature seals.
 */
fun Modifier.signatureGlow(
    color: Color = Gold,
    active: Boolean = true,
    radiusRatio: Float = 1.25f
): Modifier = composed {
    if (!active) return@composed this

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    this.drawBehind {
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = size.maxDimension * 0.5f * radiusRatio,
            center = Offset(size.width / 2f, size.height / 2f)
        )
    }
}
