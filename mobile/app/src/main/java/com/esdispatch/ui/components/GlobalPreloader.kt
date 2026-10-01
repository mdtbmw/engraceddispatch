package com.esdispatch.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.esdispatch.R
import com.esdispatch.ui.theme.Gold
import com.esdispatch.ui.theme.GoldLight
import com.esdispatch.viewmodel.GlobalPreloaderState

@Composable
fun GlobalPreloaderOverlay(
    state: GlobalPreloaderState,
    modifier: Modifier = Modifier
) {
    GlobalPreloaderOverlay(
        isVisible = state.isVisible,
        message = state.message,
        modifier = modifier
    )
}

@Composable
fun GlobalPreloaderOverlay(
    isVisible: Boolean,
    message: String = "",
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(150))
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White) // Solid white background per explicit user direction (strictly non-transparent)
                .zIndex(9999999f)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        while (true) {
                            val event = awaitPointerEvent()
                            event.changes.forEach { it.consume() }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "preloaderBreath")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.90f,
                targetValue = 1.10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(850, easing = EaseInOutQuad),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.85f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(850, easing = EaseInOutQuad),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                // Raw brand logo in Obsidian on solid white background (AGENTS.md strict contrast lock: No Gold on White)
                Icon(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "Loading",
                    tint = com.esdispatch.ui.theme.Obsidian,
                    modifier = Modifier
                        .size(58.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                )

                if (message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = message,
                        color = com.esdispatch.ui.theme.Obsidian,
                        fontSize = 14.sp,
                        fontFamily = com.esdispatch.ui.theme.SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "PREMIUM LOGISTICS & DISPATCH",
                        color = com.esdispatch.ui.theme.TextGray,
                        fontSize = 10.sp,
                        fontFamily = com.esdispatch.ui.theme.SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
