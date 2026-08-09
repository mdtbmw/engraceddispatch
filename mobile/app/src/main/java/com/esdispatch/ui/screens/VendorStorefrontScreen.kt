package com.esdispatch.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.esdispatch.ui.components.RoundedSheet
import com.esdispatch.ui.components.ScreenHeader
import com.esdispatch.ui.theme.*
import com.esdispatch.viewmodel.DeliveryViewModel
import com.esdispatch.viewmodel.MarketplaceItem
import com.esdispatch.viewmodel.MarketplaceStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorStorefrontScreen(
    viewModel: DeliveryViewModel,
    vendorId: String,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark

    val stores by viewModel.marketplaceStores.collectAsState()
    val products by viewModel.marketplaceProducts.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()

    val store = stores.firstOrNull { it.id == vendorId }
    val storeProducts = products.filter { it.vendorId == vendorId }
    val relatedStores = stores.filter {
        it.id != vendorId && it.isVerified && it.category.equals(store?.category ?: "", ignoreCase = true)
    }.distinctBy { it.id }.take(8)

    var quickViewItem by remember { mutableStateOf<MarketplaceItem?>(null) }

    val cartCount = cartItems.sumOf { it.quantity }

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
                title = store?.storeName ?: "Vendor Store",
                onBack = { onNavigate("Dashboard") },
                rightContent = {
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        IconButton(onClick = { onNavigate("Marketplace") }) {
                            Icon(
                                Icons.Filled.ShoppingCart,
                                contentDescription = "Cart",
                                tint = Gold
                            )
                        }
                        if (cartCount > 0) {
                            Badge(
                                containerColor = Color(0xFFEF4444),
                                contentColor = Color.White,
                                modifier = Modifier.align(Alignment.TopEnd).offset(x = (-4).dp)
                            ) {
                                Text("$cartCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            )

            if (store == null) {
                RoundedSheet(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Gold, strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Loading store…", color = TextGray, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                RoundedSheet(modifier = Modifier.weight(1f)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 0.dp, end = 0.dp, top = 20.dp, bottom = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            StorefrontHero(store = store, isDark = isDark, products = storeProducts)
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            StorefrontStats(store = store, productCount = storeProducts.size, isDark = isDark)
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            StorefrontAbout(store = store, isDark = isDark)
                        }
                        if (relatedStores.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                RelatedVendorsRow(
                                    stores = relatedStores,
                                    isDark = isDark,
                                    onStoreTap = { onNavigate("VendorStorefront/${it.id}") }
                                )
                            }
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "SHOP THIS STORE",
                                    fontSize = 12.sp, fontWeight = FontWeight.Black, color = Gold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    "${storeProducts.size} items",
                                    fontSize = 11.sp, color = TextGray
                                )
                            }
                        }
                        if (storeProducts.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = if (isDark) Charcoal else GoldenWhite,
                                    border = BorderStroke(1.dp, if (isDark) BorderDark else Slate.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Filled.Storefront, null, tint = Gold, modifier = Modifier.size(40.dp))
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            "This store hasn't listed any products yet.",
                                            fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTextColor,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(storeProducts, key = { it.id }) { product ->
                                StorefrontProductTile(
                                    item = product,
                                    isDark = isDark,
                                    onTap = { quickViewItem = product },
                                    onAddToCart = {
                                        viewModel.addToCart(product)
                                        Toast.makeText(context, "${product.title} added to cart", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Floating Cart Bar ─────────────────────────────────────────────
        if (cartCount > 0) {
            Card(
                onClick = { onNavigate("Marketplace") },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Gold),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Badge(containerColor = Obsidian, contentColor = Color.White) {
                            Text("$cartCount", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("View Cart & Checkout", fontWeight = FontWeight.Bold, color = Obsidian, fontSize = 14.sp)
                    }
                    Icon(Icons.Filled.ChevronRight, null, tint = Obsidian)
                }
            }
        }
    }

    // ── Quick View Sheet ──────────────────────────────────────────────────
    val quickView = quickViewItem
    if (quickView != null) {
        ModalBottomSheet(
            onDismissRequest = { quickViewItem = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = if (isDark) Charcoal else GoldenWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.82f)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top
            ) {
                val painter = rememberAsyncImagePainter(quickView.imageUrl)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDark) Color(0xFF222222) else Color(0xFFEAEAEA)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painter,
                        contentDescription = quickView.title,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )
                    if (painter.state is AsyncImagePainter.State.Error || quickView.imageUrl.isBlank()) {
                        Icon(Icons.Filled.ShoppingBag, null, tint = Gold, modifier = Modifier.size(56.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (quickView.stock <= 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.15f)
                    ) {
                        Text(
                            "OUT OF STOCK",
                            fontSize = 10.sp, fontWeight = FontWeight.Black,
                            color = Color(0xFFEF4444),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(quickView.title, fontSize = 20.sp, fontWeight = FontWeight.Black, color = AppTextColor)
                Text(quickView.vendorStore, fontSize = 12.sp, color = Gold, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { i ->
                        Icon(
                            if (i < quickView.rating.toInt()) Icons.Filled.Star else Icons.Filled.StarBorder,
                            null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${quickView.rating} (${quickView.reviewsCount} reviews)",
                        fontSize = 11.sp, color = TextGray
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "₦${String.format("%,.0f", quickView.price)}",
                    fontSize = 24.sp, fontWeight = FontWeight.Black, color = Gold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(quickView.description, fontSize = 13.sp, color = AppTextColor.copy(alpha = 0.75f), lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (quickView.stock > 0) {
                            viewModel.addToCart(quickView)
                            Toast.makeText(context, "Added to cart!", Toast.LENGTH_SHORT).show()
                            quickViewItem = null
                        }
                    },
                    enabled = quickView.stock > 0,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.AddShoppingCart, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (quickView.stock > 0) "Add to Cart" else "Out of Stock",
                        fontWeight = FontWeight.Black, fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ── Storefront Sections ─────────────────────────────────────────────────────

@Composable
private fun storeInitials(name: String): String {
    val words = name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    return when {
        words.isEmpty() -> "ES"
        words.size == 1 -> words[0].take(2).uppercase()
        else -> (words[0].firstOrNull()?.toString() ?: "") + (words[1].firstOrNull()?.toString() ?: "").uppercase()
    }
}

@Composable
private fun StorefrontHero(store: MarketplaceStore, isDark: Boolean, products: List<MarketplaceItem>) {
    val coverUrl = products.firstOrNull { it.imageUrl.isNotBlank() }?.imageUrl ?: ""
    val initials = storeInitials(store.storeName)

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = if (isDark) Charcoal else GoldenWhite,
        border = BorderStroke(1.dp, if (isDark) BorderDark else Slate.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Cover area — gradient with product cover art when available
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(GoldDark, Gold, Gold.copy(alpha = 0.65f))
                        )
                    )
            ) {
                if (coverUrl.isNotBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(coverUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f))))
                    )
                }
                if (store.isVerified) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Obsidian.copy(alpha = 0.85f),
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Verified, null, tint = Gold, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("VERIFIED", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Gold)
                        }
                    }
                }
            }

            // Store identity row
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo avatar
                if (store.logoUrl.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isDark) Color(0xFF222222) else Color(0xFFEAEAEA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(store.logoUrl),
                            contentDescription = store.storeName,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Gold),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initials, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Obsidian)
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            store.storeName,
                            fontSize = 17.sp, fontWeight = FontWeight.Black, color = AppTextColor,
                            maxLines = 2, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (store.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Filled.Verified,
                                contentDescription = "Verified Store",
                                tint = Gold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            String.format("%.1f", store.rating),
                            fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppTextColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Gold.copy(alpha = 0.15f)
                        ) {
                            Text(
                                store.category.uppercase(),
                                fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Gold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StorefrontStats(store: MarketplaceStore, productCount: Int, isDark: Boolean) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isDark) Charcoal else GoldenWhite,
        border = BorderStroke(1.dp, if (isDark) BorderDark else Slate.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCell(label = "ITEMS", value = "$productCount", isDark = isDark)
            StatDivider(isDark = isDark)
            StatCell(label = "RATING", value = String.format("%.1f", store.rating), isDark = isDark)
            StatDivider(isDark = isDark)
            StatCell(label = "SALES", value = "${store.totalSales}", isDark = isDark)
        }
    }
}

@Composable
private fun StatCell(label: String, value: String, isDark: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.Black, color = Gold)
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp)
    }
}

@Composable
private fun StatDivider(isDark: Boolean) {
    Box(
        modifier = Modifier
            .height(30.dp)
            .width(1.dp)
            .background(if (isDark) BorderDark else Slate.copy(alpha = 0.6f))
    )
}

@Composable
private fun StorefrontAbout(store: MarketplaceStore, isDark: Boolean) {
    val hasDescription = store.description.isNotBlank()
    val hasContact = store.address.isNotBlank() || store.phone.isNotBlank() || store.email.isNotBlank()

    if (!hasDescription && !hasContact) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "ABOUT THIS VENDOR",
            fontSize = 12.sp, fontWeight = FontWeight.Black, color = Gold, letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (isDark) Charcoal else GoldenWhite,
            border = BorderStroke(1.dp, if (isDark) BorderDark else Slate.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (hasDescription) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Info, null, tint = Gold, modifier = Modifier.size(16.dp).padding(top = 1.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            store.description,
                            fontSize = 12.sp, color = AppTextColor.copy(alpha = 0.8f), lineHeight = 18.sp
                        )
                    }
                }
                if (store.ownerName.isNotBlank() && store.ownerName != "Store Owner") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Person, null, tint = Gold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Owner: ${store.ownerName}", fontSize = 12.sp, color = AppTextColor)
                    }
                }
                if (store.address.isNotBlank()) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Place, null, tint = Gold, modifier = Modifier.size(16.dp).padding(top = 1.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            store.address,
                            fontSize = 12.sp, color = AppTextColor.copy(alpha = 0.8f), lineHeight = 17.sp
                        )
                    }
                }
                if (store.phone.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Phone, null, tint = Gold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(store.phone, fontSize = 12.sp, color = AppTextColor)
                    }
                }
                if (store.email.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Email, null, tint = Gold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(store.email, fontSize = 12.sp, color = AppTextColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun RelatedVendorsRow(
    stores: List<MarketplaceStore>,
    isDark: Boolean,
    onStoreTap: (MarketplaceStore) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "RELATED VENDORS",
            fontSize = 12.sp, fontWeight = FontWeight.Black, color = Gold, letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = 4.dp)
        ) {
            items(stores, key = { it.id }) { related ->
                RelatedVendorCard(store = related, isDark = isDark, onTap = { onStoreTap(related) })
            }
        }
    }
}

@Composable
private fun RelatedVendorCard(store: MarketplaceStore, isDark: Boolean, onTap: () -> Unit) {
    Surface(
        onClick = onTap,
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Charcoal else GoldenWhite,
        border = BorderStroke(1.dp, if (isDark) BorderDark else Slate.copy(alpha = 0.5f)),
        modifier = Modifier.width(150.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Gold),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        storeInitials(store.storeName),
                        fontSize = 11.sp, fontWeight = FontWeight.Black, color = Obsidian
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        store.storeName,
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppTextColor,
                        maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            String.format("%.1f", store.rating),
                            fontSize = 9.sp, color = TextGray, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                store.category,
                fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Gold,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            if (store.isVerified) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Verified, null, tint = Gold, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Verified Store", fontSize = 9.sp, color = TextGray)
                }
            }
        }
    }
}

@Composable
private fun StorefrontProductTile(
    item: MarketplaceItem,
    isDark: Boolean,
    onTap: () -> Unit,
    onAddToCart: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isDark) Charcoal else GoldenWhite,
        border = BorderStroke(1.dp, if (isDark) BorderDark else Slate.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onTap)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            val painter = rememberAsyncImagePainter(item.imageUrl)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(if (isDark) Color(0xFF222222) else Color(0xFFEAEAEA)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painter,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (painter.state is AsyncImagePainter.State.Error || item.imageUrl.isBlank()) {
                    Icon(Icons.Filled.ShoppingBag, null, tint = Gold, modifier = Modifier.size(30.dp))
                }
                if (item.stock <= 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("SOLD OUT", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    item.title,
                    fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTextColor,
                    maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        "${item.rating}",
                        fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "₦${String.format("%,.0f", item.price)}",
                        fontSize = 14.sp, fontWeight = FontWeight.Black,
                        color = if (isDark) Gold else Obsidian
                    )
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                        IconButton(
                            onClick = onAddToCart,
                            enabled = item.stock > 0,
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (item.stock > 0) Gold else Gold.copy(alpha = 0.3f),
                                    RoundedCornerShape(9.dp)
                                )
                        ) {
                            Icon(
                                Icons.Filled.AddShoppingCart, null,
                                tint = Obsidian, modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
