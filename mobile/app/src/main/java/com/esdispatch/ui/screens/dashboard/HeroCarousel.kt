package com.esdispatch.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.esdispatch.ui.theme.*
import kotlinx.coroutines.launch

data class CarouselSlide(
    val title: String,
    val desc: String,
    val imageUrl: String
)

@Composable
fun HeroCarousel() {
    var currentHeroPage by remember { mutableStateOf(0) }
    var isHeroCarouselIdle by remember { mutableStateOf(true) }
    
    val carouselSlides = listOf(
        CarouselSlide(
            title = "Move Anything,\nAnywhere",
            desc = "Premium, instant delivery at your doorstep.",
            imageUrl = "https://images.unsplash.com/photo-1512418491527-6f55e1112fb1?q=80&w=800&auto=format&fit=crop"
        ),
        CarouselSlide(
            title = "Supercharged\nExpress Network",
            desc = "City-wide delivery in under 45 minutes.",
            imageUrl = "https://images.unsplash.com/photo-1516541196182-6bdd0514013b?q=80&w=800&auto=format&fit=crop"
        ),
        CarouselSlide(
            title = "Fully Protected\nIn-Transit Guarantee",
            desc = "Live map tracking and automatic insurance.",
            imageUrl = "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?q=80&w=800&auto=format&fit=crop"
        )
    )

    LaunchedEffect(isHeroCarouselIdle) {
        if (!isHeroCarouselIdle) return@LaunchedEffect
        while (true) {
            kotlinx.coroutines.delay(4000)
            if (!isHeroCarouselIdle) break
            currentHeroPage = (currentHeroPage + 1) % carouselSlides.size
        }
    }

    var dragOffset by remember { mutableFloatStateOf(0f) }
    val animatedDragOffset by animateFloatAsState(
        targetValue = dragOffset,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dragOffsetAnimation"
    )
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragOffset > 100f) {
                            currentHeroPage = (currentHeroPage - 1 + carouselSlides.size) % carouselSlides.size
                        } else if (dragOffset < -100f) {
                            currentHeroPage = (currentHeroPage + 1) % carouselSlides.size
                        }
                        dragOffset = 0f
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(5000)
                            isHeroCarouselIdle = true
                        }
                    },
                    onDragCancel = {
                        dragOffset = 0f
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(5000)
                            isHeroCarouselIdle = true
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        if (isHeroCarouselIdle) isHeroCarouselIdle = false
                        dragOffset += dragAmount
                    }
                )
            },
        contentAlignment = Alignment.TopCenter
    ) {
        val sortedIndices = carouselSlides.indices.toList().sortedByDescending { idx ->
            (idx - currentHeroPage + carouselSlides.size) % carouselSlides.size
        }

        sortedIndices.forEach { index ->
            val relativeIndex = (index - currentHeroPage + carouselSlides.size) % carouselSlides.size
            val scaleFactor = when (relativeIndex) {
                0 -> 1.0f
                1 -> 0.92f
                else -> 0.84f
            }
            val yShift = when (relativeIndex) {
                0 -> 0.dp
                1 -> 18.dp
                else -> 36.dp
            }
            val zIndexVal = when (relativeIndex) {
                0 -> 3f
                1 -> 2f
                else -> 1f
            }
            val opacityVal = when (relativeIndex) {
                0 -> 1.0f
                1 -> 0.9f
                else -> 0.8f
            }
            val xShift = if (relativeIndex == 0) animatedDragOffset.dp else 0.dp
            val animatedScale by animateFloatAsState(targetValue = scaleFactor, label = "scale_$index")
            val animatedYShift by animateDpAsState(targetValue = yShift, label = "yShift_$index")
            val animatedAlpha by animateFloatAsState(targetValue = opacityVal, label = "alpha_$index")

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(210.dp)
                    .zIndex(zIndexVal)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                        translationX = xShift.toPx()
                        translationY = animatedYShift.toPx()
                        alpha = animatedAlpha
                    },
                shape = RoundedCornerShape(24.dp),
                color = if (relativeIndex == 0) Obsidian else Gold,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (relativeIndex == 0) (if (isDarkTheme) BorderDark else Slate) else Gold.copy(alpha = 0.5f)
                ),
                shadowElevation = if (relativeIndex == 0) 6.dp else 1.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (relativeIndex == 0) {
                        val slide = carouselSlides[index]
                        Image(
                            painter = rememberAsyncImagePainter(slide.imageUrl),
                            contentDescription = slide.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))
                        )
                        Box(
                            modifier = Modifier.fillMaxSize().background(
                                Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.85f)))
                            )
                        )
                        Column(
                            modifier = Modifier.fillMaxSize().padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Gold,
                                modifier = Modifier.align(Alignment.Start)
                            ) {
                                Text(
                                    text = "ES",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Obsidian,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = slide.title,
                                    fontSize = 18.sp,
                                    lineHeight = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = slide.desc,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextGray.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Gold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = com.esdispatch.R.drawable.ic_logo),
                                contentDescription = "Inactive slide logo",
                                tint = Obsidian.copy(alpha = 0.15f),
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    }
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
                val isSelected = i == currentHeroPage
                val width = if (isSelected) 24.dp else 8.dp
                Box(
                    modifier = Modifier
                        .size(width = width, height = 8.dp)
                        .background(
                            color = if (isSelected) Gold else (if (isDarkTheme) Color(0xFF333333) else Color(0xFFE0E0E0)),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}
