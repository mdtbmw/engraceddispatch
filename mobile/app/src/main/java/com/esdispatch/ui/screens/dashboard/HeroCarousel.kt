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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.esdispatch.ui.theme.*
import com.esdispatch.viewmodel.MarketplaceItem
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HeroCarousel(
    products: List<MarketplaceItem> = emptyList(),
    onAddToCart: (MarketplaceItem) -> Unit = {},
    onProductClick: (MarketplaceItem) -> Unit = {}
) {
    val fallbackItems = remember {
        listOf(
            MarketplaceItem(
                id = "feat_1",
                title = "Heavy-Duty Courier Box 65L",
                category = "Packaging",
                price = 18500.0,
                rating = 4.9,
                reviewsCount = 42,
                imageUrl = "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?q=80&w=800&auto=format&fit=crop",
                description = "Waterproof reinforced thermal delivery box for bikes and motorbikes.",
                stock = 25,
                vendorStore = "ESDispatch Official Gear"
            ),
            MarketplaceItem(
                id = "feat_2",
                title = "Reflective Rider Safety Vest",
                category = "Safety",
                price = 6500.0,
                rating = 4.8,
                reviewsCount = 56,
                imageUrl = "https://images.unsplash.com/photo-1512418491527-6f55e1112fb1?q=80&w=800&auto=format&fit=crop",
                description = "High-visibility dual-stripe weather-resistant reflective safety vest.",
                stock = 40,
                vendorStore = "Apex Courier Supplies"
            ),
            MarketplaceItem(
                id = "feat_3",
                title = "Thermal Insulated Food Bag",
                category = "Packaging",
                price = 12000.0,
                rating = 5.0,
                reviewsCount = 31,
                imageUrl = "https://images.unsplash.com/photo-1516541196182-6bdd0514013b?q=80&w=800&auto=format&fit=crop",
                description = "Triple-layer insulation keeps meals and packages at optimal temperature.",
                stock = 18,
                vendorStore = "Swift Logistics Gear"
            )
        )
    }

    val displayItems = remember(products) {
        if (products.size >= 3) {
            products.take(5)
        } else if (products.isNotEmpty()) {
            (products + fallbackItems).distinctBy { it.id }.take(3)
        } else {
            fallbackItems
        }
    }

    var currentHeroPage by remember { mutableStateOf(0) }
    var isHeroCarouselIdle by remember { mutableStateOf(true) }

    LaunchedEffect(isHeroCarouselIdle, displayItems.size) {
        if (!isHeroCarouselIdle) return@LaunchedEffect
        while (true) {
            kotlinx.coroutines.delay(4500)
            if (!isHeroCarouselIdle) break
            currentHeroPage = (currentHeroPage + 1) % displayItems.size
        }
    }

    var dragOffset by remember { mutableFloatStateOf(0f) }
    val animatedDragOffset by animateFloatAsState(
        targetValue = dragOffset,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dragOffsetAnimation"
    )
    val coroutineScope = rememberCoroutineScope()

    val currencyFormatter = remember {
        NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .pointerInput(displayItems.size) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragOffset > 80f) {
                            currentHeroPage = (currentHeroPage - 1 + displayItems.size) % displayItems.size
                        } else if (dragOffset < -80f) {
                            currentHeroPage = (currentHeroPage + 1) % displayItems.size
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
        val sortedIndices = displayItems.indices.toList().sortedByDescending { idx ->
            (idx - currentHeroPage + displayItems.size) % displayItems.size
        }

        sortedIndices.forEach { index ->
            val relativeIndex = (index - currentHeroPage + displayItems.size) % displayItems.size
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
                    .height(215.dp)
                    .zIndex(zIndexVal)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                        translationX = xShift.toPx()
                        translationY = animatedYShift.toPx()
                        alpha = animatedAlpha
                    },
                shape = RoundedCornerShape(26.dp),
                color = if (relativeIndex == 0) Obsidian else Gold,
                border = BorderStroke(
                    width = 1.2.dp,
                    color = if (relativeIndex == 0) BorderDark else Gold.copy(alpha = 0.5f)
                ),
                shadowElevation = if (relativeIndex == 0) 8.dp else 2.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (relativeIndex == 0) {
                        val product = displayItems[index]
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = product.imageUrl.ifBlank { "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?q=80&w=800" }
                            ),
                            contentDescription = product.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(26.dp))
                                .clickable { onProductClick(product) }
                        )

                        // Rich dark luxury vignette
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Black.copy(alpha = 0.35f),
                                            Color.Black.copy(alpha = 0.88f)
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top Row: Featured store badge & Rating
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Gold,
                                    shadowElevation = 2.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Storefront,
                                            contentDescription = null,
                                            tint = Obsidian,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = product.vendorStore.take(22).uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Obsidian,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.6f),
                                    border = BorderStroke(0.8.dp, Gold.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = Gold,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${product.rating}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // Bottom Section: Product Title, Price & Add To Cart Button
                            Column {
                                Text(
                                    text = product.title,
                                    fontSize = 16.sp,
                                    lineHeight = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = product.description.ifBlank { "High-demand dispatch & logistics equipment." },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = TextGray.copy(alpha = 0.9f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "PRICE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Gold,
                                            letterSpacing = 0.8.sp
                                        )
                                        Text(
                                            text = "₦${currencyFormatter.format(product.price)}",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }

                                    Button(
                                        onClick = { onAddToCart(product) },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Gold,
                                            contentColor = Obsidian
                                        ),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                        modifier = Modifier.height(38.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AddShoppingCart,
                                            contentDescription = null,
                                            tint = Obsidian,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Add to Cart",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Gold),
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

        // Indicator dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 230.dp)
                .wrapContentWidth(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in displayItems.indices.take(5)) {
                val isSelected = i == currentHeroPage
                val width = if (isSelected) 24.dp else 8.dp
                Box(
                    modifier = Modifier
                        .size(width = width, height = 8.dp)
                        .background(
                            color = if (isSelected) Gold else Charcoal,
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}
