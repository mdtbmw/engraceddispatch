package com.esdispatch.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.esdispatch.ui.components.ScreenHeader
import com.esdispatch.ui.theme.*
import com.esdispatch.viewmodel.DeliveryViewModel
import com.esdispatch.viewmodel.MarketplaceItem
import com.esdispatch.viewmodel.MarketplaceStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorProfileScreen(
    vendorId: String,
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark

    val stores by viewModel.marketplaceStores.collectAsState()
    val products by viewModel.marketplaceProducts.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()

    val store = remember(vendorId, stores) {
        stores.find { it.id == vendorId } ?: MarketplaceStore(
            id = vendorId,
            storeName = "Verified ESDispatch Vendor",
            category = "VIP Executive Store",
            description = "Official verified partner storefront on ESDispatch Marketplace. Premium quality guaranteed.",
            ownerName = "Executive Vendor",
            address = "No. 12 Victoria Island Admiralty Way, Ikeja, Lagos",
            rating = 4.9,
            totalSales = 48,
            itemCount = products.count { it.vendorId == vendorId },
            isVerified = true
        )
    }

    val storeProducts = remember(vendorId, products) {
        products.filter { it.vendorId == vendorId || vendorId.isBlank() || it.vendorStore.equals(store.storeName, ignoreCase = true) }
    }

    val otherStores = remember(vendorId, stores) {
        stores.filter { it.id != vendorId }
    }

    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var quickViewItem by remember { mutableStateOf<MarketplaceItem?>(null) }

    val categories = remember(storeProducts) {
        listOf("All") + storeProducts.map { it.category }.distinct()
    }

    val filteredProducts = remember(storeProducts, selectedCategory, searchQuery) {
        storeProducts.filter { item ->
            (selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)) &&
                    (searchQuery.isBlank() || item.title.contains(searchQuery, ignoreCase = true) || item.description.contains(searchQuery, ignoreCase = true))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HeaderBgColor)
        ) {
            ScreenHeader(
                title = store.storeName,
                onBack = onBack
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppBackground),
                contentPadding = PaddingValues(bottom = 100.dp, top = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // STORE HERO HEADER
                item(span = { GridItemSpan(2) }) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Store Cover & Logo Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    if (store.logoUrl.isNotBlank()) store.logoUrl else "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=800&fit=crop"
                                ),
                                contentDescription = "Store Cover",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .background(
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                        )
                                    )
                            )

                            // Floating Store Avatar
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(start = 20.dp)
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(AppSurface)
                                    .border(2.dp, Gold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (store.logoUrl.isNotBlank()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(store.logoUrl),
                                        contentDescription = "Logo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Storefront,
                                        contentDescription = null,
                                        tint = Gold,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }

                        // Store Info Details
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = store.storeName,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AppTextColor
                                )
                                if (store.isVerified) {
                                    Icon(
                                        imageVector = Icons.Filled.Verified,
                                        contentDescription = "Verified",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = store.category,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${String.format("%.1f", store.rating)} (48+ reviews)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppTextColor
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Schedule, null, tint = TextGray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "15-25 min delivery",
                                        fontSize = 12.sp,
                                        color = TextGray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocationOn, null, tint = Gold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = store.address.ifBlank { "No. 12 Admiralty Way, Lekki Phase 1, Lagos" },
                                    fontSize = 12.sp,
                                    color = AppTextColor.copy(alpha = 0.8f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (store.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = store.description,
                                    fontSize = 12.sp,
                                    color = TextGray,
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Search Bar
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search in ${store.storeName}...", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Filled.Search, null, tint = TextGray) },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Gold,
                                    unfocusedBorderColor = if (isDark) BorderDark else Slate,
                                    focusedTextColor = AppTextColor,
                                    unfocusedTextColor = AppTextColor
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Category Chips
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(categories) { cat ->
                                    val isSel = selectedCategory == cat
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { selectedCategory = cat },
                                        label = { Text(cat, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Gold,
                                            selectedLabelColor = Obsidian,
                                            containerColor = if (isDark) Charcoal else GoldenWhite,
                                            labelColor = AppTextColor
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // PRODUCTS LISTING GRID
                if (filteredProducts.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No products found in this store",
                                fontSize = 13.sp,
                                color = TextGray
                            )
                        }
                    }
                } else {
                    items(filteredProducts) { item ->
                        VendorProductCard(
                            item = item,
                            isDark = isDark,
                            onQuickView = { quickViewItem = item },
                            onAddToCart = {
                                viewModel.addToCart(item, 1)
                                Toast.makeText(context, "${item.title} added to cart!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                // RELATED VERIFIED VENDORS CAROUSEL
                if (otherStores.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            Text(
                                text = "Explore Other Verified Stores",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppTextColor
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(otherStores) { other ->
                                    RelatedVendorCard(
                                        store = other,
                                        isDark = isDark,
                                        onClick = { onNavigate("VendorProfile/${other.id}") }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Cart Navigation Bar
        if (cartItems.isNotEmpty()) {
            val totalCartPrice = cartItems.sumOf { it.item.price * it.quantity }
            val totalCartQty = cartItems.sumOf { it.quantity }

            Surface(
                onClick = { onNavigate("Marketplace") },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                color = Gold,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Obsidian),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$totalCartQty", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Gold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("View Cart • ₦${String.format("%,.0f", totalCartPrice)}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Obsidian)
                    }

                    Icon(Icons.Filled.ArrowForward, "Cart", tint = Obsidian, modifier = Modifier.size(20.dp))
                }
            }
        }
    }

    // Quick View Product Dialog Sheet
    quickViewItem?.let { item ->
        QuickViewProductModal(
            item = item,
            isDark = isDark,
            onDismiss = { quickViewItem = null },
            onAddToCart = { qty ->
                viewModel.addToCart(item, qty)
                Toast.makeText(context, "Added $qty x ${item.title} to cart!", Toast.LENGTH_SHORT).show()
                quickViewItem = null
            }
        )
    }
}

@Composable
private fun VendorProductCard(
    item: MarketplaceItem,
    isDark: Boolean,
    onQuickView: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Charcoal else GoldenWhite
        ),
        border = BorderStroke(1.dp, if (isDark) BorderDark else Slate)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clickable { onQuickView() }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        if (item.imageUrl.isNotBlank()) item.imageUrl else "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&fit=crop"
                    ),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Stock Chip
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (item.stock > 0) Color(0xFF10B981) else Color.Red)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (item.stock > 0) "In Stock" else "Sold Out",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "₦${String.format("%,.0f", item.price)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Gold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onQuickView,
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) LuxuryBlack else Color.White, contentColor = AppTextColor),
                        border = BorderStroke(1.dp, if (isDark) BorderDark else Slate),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("+ Add", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun RelatedVendorCard(
    store: MarketplaceStore,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(200.dp)
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Charcoal else GoldenWhite
        ),
        border = BorderStroke(1.dp, if (isDark) BorderDark else Slate)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AppSurface)
                    .border(1.dp, Gold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (store.logoUrl.isNotBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(store.logoUrl),
                        contentDescription = "Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Filled.Storefront, null, tint = Gold, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = store.storeName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (store.isVerified) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(Icons.Filled.Verified, null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                    }
                }
                Text(
                    text = store.category,
                    fontSize = 10.sp,
                    color = TextGray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickViewProductModal(
    item: MarketplaceItem,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onAddToCart: (Int) -> Unit
) {
    var quantity by remember { mutableStateOf(1) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Charcoal else GoldenWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        if (item.imageUrl.isNotBlank()) item.imageUrl else "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&fit=crop"
                    ),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(item.title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = AppTextColor)
            Text(item.vendorStore, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Gold)

            Spacer(modifier = Modifier.height(8.dp))

            Text("₦${String.format("%,.0f", item.price)}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Gold)

            if (item.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(item.description, fontSize = 13.sp, color = TextGray, lineHeight = 18.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Select Quantity", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppTextColor)

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isDark) LuxuryBlack else Color.White)
                            .clickable { if (quantity > 1) quantity-- },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                    }

                    Text("$quantity", fontSize = 16.sp, fontWeight = FontWeight.Black, color = AppTextColor)

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isDark) LuxuryBlack else Color.White)
                            .clickable { quantity++ },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onAddToCart(quantity) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian)
            ) {
                Text("Add $quantity to Cart • ₦${String.format("%,.0f", item.price * quantity)}", fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
