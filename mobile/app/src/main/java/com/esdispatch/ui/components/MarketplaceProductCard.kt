package com.esdispatch.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.esdispatch.ui.theme.*
import com.esdispatch.viewmodel.MarketplaceItem
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Standardized MarketplaceProductCard adhering to Section 18 of ESDispatch Design System.
 * Guarantees 44dp minimum touch targets, high contrast, and structured stock badges.
 */
@Composable
fun MarketplaceProductCard(
    item: MarketplaceItem,
    isFavorite: Boolean,
    onTap: () -> Unit,
    onAddToCart: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isDarkTheme
    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
    ) {
        // Background swipe action indicators
        Row(
            modifier = Modifier
                .matchParentSize()
                .background(if (dark) Color(0xFF18181A) else Color(0xFFE8E8E8)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Action: Revealed when dragged RIGHT -> Favorite
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(if (isFavorite) Color(0xFFDC2626) else Color(0xFFEF4444).copy(alpha = 0.9f))
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedHugeIcon(
                    icon = Hugeicons.Solid.Heart,
                    tint = Color.White,
                    size = 20.dp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isFavorite) "Saved" else "Favorite",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            }

            // Right Action: Revealed when dragged LEFT -> Add to Cart
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(Gold)
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add to Cart",
                    color = Obsidian, // Strict lock: NO white on gold
                    fontSize = 11.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(6.dp))
                AnimatedHugeIcon(
                    icon = Hugeicons.Solid.Cart,
                    tint = Obsidian,
                    size = 20.dp
                )
            }
        }

        // Foreground Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (dark) Charcoal else GoldenWhiteSurface,
            border = BorderStroke(1.dp, if (dark) BorderDark else Slate.copy(alpha = 0.5f)),
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(item.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                val currentOffset = offsetX.value
                                if (currentOffset > 70f) {
                                    onToggleFavorite()
                                } else if (currentOffset < -70f) {
                                    if (item.stock > 0) {
                                        onAddToCart()
                                    }
                                }
                                offsetX.animateTo(0f, animationSpec = spring(stiffness = 400f, dampingRatio = 0.75f))
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                val target = offsetX.value + dragAmount * 0.75f
                                offsetX.snapTo(target.coerceIn(-160f, 160f))
                            }
                        }
                    )
                }
                .clickable { onTap() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product Image Container with Fallback
                val painter = rememberAsyncImagePainter(item.imageUrl)
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (dark) Color(0xFF1E1E22) else Color(0xFFEFE8D8)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painter,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (painter.state is AsyncImagePainter.State.Error || item.imageUrl.isBlank()) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingBag,
                            contentDescription = null,
                            tint = if (dark) Gold else Obsidian,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    if (item.stock <= 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SOLD OUT",
                                fontSize = 9.sp,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Middle Details Column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        color = AppTextColor,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Vendor Store Name (Strict contrast: NO gold text on light surfaces)
                    Text(
                        text = item.vendorStore,
                        color = if (dark) Gold else Color(0xFF78350F),
                        fontFamily = Poppins,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Rating & Stock Badge Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.reviewsCount > 0) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${item.rating} (${item.reviewsCount})",
                                color = TextGray,
                                fontFamily = Poppins,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "New",
                                color = TextGray,
                                fontFamily = Poppins,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Stock State Badge (Section 18)
                        val (stockBg, stockFg, stockLabel) = when {
                            item.stock <= 0 -> Triple(
                                if (dark) Color(0x33EF4444) else Color(0xFFFEE2E2),
                                if (dark) Color(0xFFF87171) else Color(0xFF991B1B),
                                "Out of stock"
                            )
                            item.stock <= 3 -> Triple(
                                if (dark) Color(0x33F59E0B) else Color(0xFFFEF3C7),
                                if (dark) Color(0xFFFBBF24) else Color(0xFF92400E),
                                "Low stock"
                            )
                            else -> Triple(
                                if (dark) Color(0x3310B981) else Color(0xFFD1FAE5),
                                if (dark) Color(0xFF34D399) else Color(0xFF065F46),
                                "In stock"
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(stockBg)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = stockLabel,
                                fontSize = 9.sp,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                color = stockFg
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right Actions Column
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    PriceDisplay(
                        amount = item.price,
                        variant = PriceVariant.COMPACT,
                        customColor = if (dark) Gold else Obsidian
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Favorite Icon Action (Minimum 44dp hit area)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .tactilePress(scaleDown = 0.90f) { onToggleFavorite() },
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedHugeIcon(
                                icon = Hugeicons.Solid.Heart,
                                tint = if (isFavorite) Color(0xFFEF4444) else TextGray.copy(alpha = 0.6f),
                                size = 19.dp
                            )
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        // Add to Cart Button (Minimum 44dp hit area)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (item.stock > 0) Gold else Gold.copy(alpha = 0.35f)
                                )
                                .tactilePress(scaleDown = 0.90f) {
                                    if (item.stock > 0) onAddToCart()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedHugeIcon(
                                icon = Hugeicons.Solid.Cart,
                                tint = Obsidian, // Strict rule: NO white on gold
                                size = 19.dp
                            )
                        }
                    }
                }
            }
        }
    }
}
