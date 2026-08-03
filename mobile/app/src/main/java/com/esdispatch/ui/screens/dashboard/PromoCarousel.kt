package com.esdispatch.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import com.esdispatch.ui.theme.*

data class PromoMock(
    val title: String,
    val code: String,
    val discount: String,
    val desc: String
)

@Composable
fun PromoCarousel(
    promotions: List<com.esdispatch.data.PromoCode>,
    onApplyPromo: (String) -> Unit = {}
) {
    if (promotions.isEmpty()) return

    val context = LocalContext.current
    val isDark = isDarkTheme
    
    val promoMockups = promotions.map { p ->
        PromoMock(
            title = when (p.code) { "FIRSTFREE" -> "First Free"; "EID2026" -> "Festive Save"; "WEEKEND30" -> "Weekend Rush"; else -> "Special Offer" },
            code = p.code,
            discount = if (p.discountPercent >= 100) "₦2,500.00" else "${p.discountPercent}% OFF",
            desc = p.description
        )
    }

    var currentIndex by remember { mutableStateOf(0) }
    val swipeOffsetAnim = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    var isPromoCarouselIdle by remember { mutableStateOf(true) }

    LaunchedEffect(currentIndex, isPromoCarouselIdle) {
        if (!isPromoCarouselIdle) return@LaunchedEffect
        kotlinx.coroutines.delay(5000L)
        if (!isPromoCarouselIdle) return@LaunchedEffect
        swipeOffsetAnim.animateTo(-800f, animationSpec = tween(350))
        currentIndex = (currentIndex + 1) % promoMockups.size
        swipeOffsetAnim.snapTo(800f)
        swipeOffsetAnim.animateTo(0f, animationSpec = spring(dampingRatio = 0.82f, stiffness = 300f))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(215.dp)
            .padding(horizontal = 24.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        if (isPromoCarouselIdle) isPromoCarouselIdle = false
                        coroutineScope.launch {
                            swipeOffsetAnim.snapTo(swipeOffsetAnim.value + dragAmount)
                        }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            val offset = swipeOffsetAnim.value
                            if (offset > 200f) {
                                swipeOffsetAnim.animateTo(800f, animationSpec = tween(250))
                                currentIndex = (currentIndex - 1 + promoMockups.size) % promoMockups.size
                                swipeOffsetAnim.snapTo(-800f)
                                swipeOffsetAnim.animateTo(0f, animationSpec = spring(dampingRatio = 0.82f, stiffness = 300f))
                            } else if (offset < -200f) {
                                swipeOffsetAnim.animateTo(-800f, animationSpec = tween(250))
                                currentIndex = (currentIndex + 1) % promoMockups.size
                                swipeOffsetAnim.snapTo(800f)
                                swipeOffsetAnim.animateTo(0f, animationSpec = spring(dampingRatio = 0.82f, stiffness = 300f))
                            } else {
                                swipeOffsetAnim.animateTo(0f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f))
                            }
                            kotlinx.coroutines.delay(5000)
                            isPromoCarouselIdle = true
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            swipeOffsetAnim.snapTo(0f)
                            kotlinx.coroutines.delay(5000)
                            isPromoCarouselIdle = true
                        }
                    }
                )
            },
        contentAlignment = Alignment.TopCenter
    ) {
        val promoCount = promoMockups.size
        val sortedIndices = promoMockups.indices.toList().sortedByDescending { idx ->
            (idx - currentIndex + promoCount) % promoCount
        }

        sortedIndices.forEach { index ->
            val relativeIndex = (index - currentIndex + promoCount) % promoCount
            val promo = promoMockups[index]
            val swipeProgress = (kotlin.math.abs(swipeOffsetAnim.value) / 800f).coerceIn(0f, 1f)

            val scale = when (relativeIndex) {
                0 -> 1.0f
                1 -> 0.90f + 0.10f * swipeProgress
                else -> 0.80f + 0.10f * swipeProgress
            }
            val translationY = when (relativeIndex) {
                0 -> 0.dp
                1 -> (18f - 18f * swipeProgress).dp
                else -> (36f - 18f * swipeProgress).dp
            }
            val baseColor = if (isDark) Charcoal else Color.White
            val cardBgColor = if (relativeIndex == 0) {
                baseColor
            } else if (relativeIndex == 1) {
                androidx.compose.ui.graphics.lerp(Gold, baseColor, swipeProgress)
            } else {
                Gold
            }
            val contentAlpha = if (relativeIndex == 0) {
                1.0f - swipeProgress
            } else if (relativeIndex == 1) {
                swipeProgress
            } else {
                0f
            }
            val zIndexVal = when (relativeIndex) {
                0 -> 3f
                1 -> 2f
                else -> 1f
            }
            val rotationZ = if (relativeIndex == 0) (swipeOffsetAnim.value / 40f) else 0f
            val translationX = if (relativeIndex == 0) swipeOffsetAnim.value else 0f

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = cardBgColor,
                border = BorderStroke(1.dp, if (relativeIndex == 0) BorderLight else Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(165.dp)
                    .graphicsLayer {
                        this.scaleX = scale
                        this.scaleY = scale
                        this.translationY = translationY.toPx()
                        this.translationX = translationX
                        this.rotationZ = rotationZ
                    },
                shadowElevation = if (relativeIndex == 0) 4.dp else 1.dp
            ) {
                if (relativeIndex == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(contentAlpha)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = promo.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Obsidian
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Obsidian
                                    ) {
                                        Text(
                                            text = promo.discount,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Gold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = promo.desc,
                                    fontSize = 11.sp,
                                    color = Color(0xFF424242),
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 14.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Obsidian, RoundedCornerShape(12.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "CODE: ${promo.code}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                onApplyPromo(promo.code)
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Apply",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Gold
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(0.8f)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.Canvas(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    val canvasWidth = size.width
                                    val canvasHeight = size.height
                                    drawCircle(
                                        color = Gold.copy(alpha = 0.12f),
                                        radius = canvasWidth * 0.42f,
                                        center = Offset(canvasWidth * 0.65f, canvasHeight * 0.5f)
                                    )
                                    val speedY1 = canvasHeight * 0.35f
                                    val speedY2 = canvasHeight * 0.52f
                                    val speedY3 = canvasHeight * 0.68f
                                    drawLine(
                                        color = Gold.copy(alpha = 0.3f),
                                        start = Offset(canvasWidth * 0.1f, speedY1),
                                        end = Offset(canvasWidth * 0.45f, speedY1),
                                        strokeWidth = 3f,
                                        cap = StrokeCap.Round
                                    )
                                    drawLine(
                                        color = Gold,
                                        start = Offset(canvasWidth * 0.2f, speedY2),
                                        end = Offset(canvasWidth * 0.55f, speedY2),
                                        strokeWidth = 4f,
                                        cap = StrokeCap.Round
                                    )
                                    drawLine(
                                        color = Gold.copy(alpha = 0.3f),
                                        start = Offset(canvasWidth * 0.15f, speedY3),
                                        end = Offset(canvasWidth * 0.42f, speedY3),
                                        strokeWidth = 3f,
                                        cap = StrokeCap.Round
                                    )
                                    val boxSize = canvasWidth * 0.28f
                                    val boxX = canvasWidth * 0.42f
                                    val boxY = canvasHeight * 0.28f
                                    val pathFrontLeft = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(boxX, boxY + boxSize * 0.35f)
                                        lineTo(boxX + boxSize * 0.5f, boxY + boxSize * 0.65f)
                                        lineTo(boxX + boxSize * 0.5f, boxY + boxSize * 1.25f)
                                        lineTo(boxX, boxY + boxSize * 0.95f)
                                        close()
                                    }
                                    drawPath(pathFrontLeft, color = Obsidian)
                                    val pathFrontRight = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(boxX + boxSize * 0.5f, boxY + boxSize * 0.65f)
                                        lineTo(boxX + boxSize, boxY + boxSize * 0.35f)
                                        lineTo(boxX + boxSize, boxY + boxSize * 0.95f)
                                        lineTo(boxX + boxSize * 0.5f, boxY + boxSize * 1.25f)
                                        close()
                                    }
                                    drawPath(pathFrontRight, color = Obsidian.copy(alpha = 0.85f))
                                    val pathTopFace = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(boxX, boxY + boxSize * 0.35f)
                                        lineTo(boxX + boxSize * 0.5f, boxY)
                                        lineTo(boxX + boxSize, boxY + boxSize * 0.35f)
                                        lineTo(boxX + boxSize * 0.5f, boxY + boxSize * 0.7f)
                                        close()
                                    }
                                    drawPath(pathTopFace, color = Gold)
                                    val pathTapeAccent = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(boxX + boxSize * 0.22f, boxY + boxSize * 0.48f)
                                        lineTo(boxX + boxSize * 0.5f, boxY + boxSize * 0.62f)
                                        lineTo(boxX + boxSize * 0.78f, boxY + boxSize * 0.48f)
                                        lineTo(boxX + boxSize * 0.5f, boxY + boxSize * 0.34f)
                                        close()
                                    }
                                    drawPath(pathTapeAccent, color = Color.White.copy(alpha = 0.65f))
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 3) {
                val isSelected = i == currentIndex
                val w = if (isSelected) 24.dp else 8.dp
                Box(
                    modifier = Modifier
                        .size(width = w, height = 8.dp)
                        .background(
                            color = if (isSelected) Gold else (if (isDark) Color(0xFF333333) else Color(0xFFE0E0E0)),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}
