package com.esdispatch.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.esdispatch.ui.components.AddressAutocompleteField
import com.esdispatch.ui.components.RoundedSheet
import com.esdispatch.ui.components.ScreenHeader
import com.esdispatch.ui.theme.*
import com.esdispatch.viewmodel.DeliveryViewModel
import com.esdispatch.viewmodel.MarketplaceItem
import com.esdispatch.viewmodel.MarketplaceStore

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MarketplaceScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    // Correct dark-mode check — isDarkTheme doesn't exist as a top-level symbol
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark

    // Live Firestore state
    val marketplaceItems by viewModel.marketplaceProducts.collectAsState()
    val stores by viewModel.marketplaceStores.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val walletBalance by viewModel.walletBalance.collectAsState()
    val marketplaceStores by viewModel.marketplaceStores.collectAsState()

    // UI state
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showItemDetails by remember { mutableStateOf<MarketplaceItem?>(null) }
    var showCheckoutSheet by remember { mutableStateOf(false) }

    // Aligned with Vendor Portal product categories so every vendor item is reachable
    val categories = listOf("All", "Delivery Gear", "Apparel", "Lubricants", "Accessories", "Safety", "Packaging", "Electronics", "General", "Food & Beverages", "Services")

    val rawItems = marketplaceItems
    val filteredItems = rawItems.filter { item ->
        val matchesCategory = selectedCategory == "All" ||
                item.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true) ||
                item.vendorStore.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }
    val displayItems = filteredItems

    val cartCount = cartItems.sumOf { it.quantity }
    val cartTotal = cartItems.sumOf { it.item.price * it.quantity }

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
            // Header with cart badge
            ScreenHeader(
                title = "Marketplace Catalog",
                onBack = { onNavigate("Dashboard") },
                rightContent = {
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        val badgeScale by animateFloatAsState(
                            targetValue = if (cartCount > 0) 1.0f else 0.0f,
                            animationSpec = SpringPhysics.TouchPress,
                            label = "cartBadgeScale"
                        )
                        IconButton(
                            onClick = { showCheckoutSheet = true },
                            modifier = Modifier.tactilePress(scaleDown = 0.90f) { showCheckoutSheet = true }
                        ) {
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
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp)
                                    .graphicsLayer {
                                        scaleX = badgeScale
                                        scaleY = badgeScale
                                    }
                            ) {
                                Text("$cartCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            )

            RoundedSheet(modifier = Modifier.weight(1f)) {
                val verifiedStores = remember(marketplaceStores, stores) {
                    (marketplaceStores + stores).filter { it.isVerified }.distinctBy { it.id }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 160.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ── 1. Hero & Verified Stores (Scrolls Away Naturally) ──
                    item(key = "vendor_hub_banner") {
                        Card(
                            onClick = { onNavigate("VendorPortal") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Gold.copy(alpha = 0.12f)),
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.35f)),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Storefront, null, tint = Gold, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Vendor Control Hub", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                                        Text("Manage store, products & payouts", fontSize = 10.sp, color = AppTextColor.copy(alpha = 0.6f))
                                    }
                                }
                                Icon(Icons.Filled.ChevronRight, null, tint = Gold)
                            }
                        }
                    }

                    if (verifiedStores.isNotEmpty()) {
                        item(key = "verified_stores_section") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Featured Verified Stores",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AppTextColor
                                    )
                                    Text(
                                        text = "${verifiedStores.size} Stores",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Gold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(verifiedStores, key = { it.id }) { store ->
                                        VendorStoreCard(
                                            store = store,
                                            isDark = isDark,
                                            onTap = { onNavigate("VendorStorefront/${store.id}") }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── 2. Sticky Search Bar & Category Filter Header ──
                    stickyHeader(key = "sticky_filter_bar") {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(22.dp),
                            color = if (isDark) Charcoal else Color.White,
                            border = BorderStroke(1.dp, if (isDark) BorderDark else BorderLight),
                            shadowElevation = 3.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Search catalog, equipment, parts…", color = TextGray, fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Filled.Search, null, tint = if (isDark) Gold else Obsidian, modifier = Modifier.size(20.dp)) },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(Icons.Filled.Clear, null, tint = TextGray, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Gold,
                                        unfocusedBorderColor = if (isDark) BorderDark else BorderLight,
                                        focusedContainerColor = if (isDark) Obsidian else GoldenWhite,
                                        unfocusedContainerColor = if (isDark) Obsidian else GoldenWhite,
                                        focusedTextColor = if (isDark) Color.White else Obsidian,
                                        unfocusedTextColor = if (isDark) Color.White else Obsidian
                                    ),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(categories) { cat ->
                                        val isSelected = cat == selectedCategory
                                        Surface(
                                            onClick = { selectedCategory = cat },
                                            shape = RoundedCornerShape(20.dp),
                                            color = if (isSelected) Gold else if (isDark) Obsidian else GoldenWhite,
                                            border = if (!isSelected) BorderStroke(1.dp, if (isDark) BorderDark else BorderLight) else null
                                        ) {
                                            Text(
                                                cat,
                                                color = if (isSelected) Obsidian else (if (isDark) Color.White else Obsidian),
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── 3. Products List ──
                    if (displayItems.isEmpty()) {
                        item(key = "empty_state") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Filled.Inventory2,
                                        contentDescription = null,
                                        tint = Gold.copy(alpha = 0.4f),
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "No products found",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppTextColor
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Try adjusting your search or category filter",
                                        fontSize = 12.sp,
                                        color = TextGray
                                    )
                                }
                            }
                        }
                    } else {
                        items(displayItems, key = { it.id }) { item ->
                            MarketplaceProductCard(
                                item = item,
                                isDark = isDark,
                                onTap = { showItemDetails = item },
                                onAddToCart = {
                                    viewModel.addToCart(item)
                                    Toast.makeText(context, "${item.title} added to cart", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }

        // ── Floating Cart Bar ──────────────────────────────────────────────────
        // navigationBarsPadding() keeps it above the system nav gesture handle
        if (cartCount > 0) {
            Card(
                onClick = { showCheckoutSheet = true },
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
                    Text(
                        "₦${String.format("%,.0f", cartTotal)}",
                        fontWeight = FontWeight.Black, color = Obsidian, fontSize = 16.sp
                    )
                }
            }
        }
    }

    // ── Item Detail Sheet ──────────────────────────────────────────────────────
    val detailItem = showItemDetails
    if (detailItem != null) {
        ModalBottomSheet(
            onDismissRequest = { showItemDetails = null },
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
                val painter = rememberAsyncImagePainter(detailItem.imageUrl)
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
                        contentDescription = detailItem.title,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )
                    if (painter.state is AsyncImagePainter.State.Error || detailItem.imageUrl.isBlank()) {
                        Icon(Icons.Filled.ShoppingBag, null, tint = Gold, modifier = Modifier.size(56.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Out of stock chip
                if (detailItem.stock <= 0) {
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
                Text(detailItem.title, fontSize = 20.sp, fontWeight = FontWeight.Black, color = AppTextColor)
                Text(detailItem.vendorStore, fontSize = 12.sp, color = Gold, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { i ->
                        Icon(
                            if (i < detailItem.rating.toInt()) Icons.Filled.Star else Icons.Filled.StarBorder,
                            null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${detailItem.rating} (${detailItem.reviewsCount} reviews)",
                        fontSize = 11.sp, color = TextGray
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "₦${String.format("%,.0f", detailItem.price)}",
                    fontSize = 24.sp, fontWeight = FontWeight.Black, color = Gold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(detailItem.description, fontSize = 13.sp, color = AppTextColor.copy(alpha = 0.75f), lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (detailItem.stock > 0) {
                            viewModel.addToCart(detailItem)
                            Toast.makeText(context, "Added to cart!", Toast.LENGTH_SHORT).show()
                            showItemDetails = null
                        }
                    },
                    enabled = detailItem.stock > 0,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.AddShoppingCart, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (detailItem.stock > 0) "Add to Cart" else "Out of Stock",
                        fontWeight = FontWeight.Black, fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ── Checkout Sheet ─────────────────────────────────────────────────────────
    if (showCheckoutSheet && cartItems.isNotEmpty()) {
        var shippingAddress by remember { mutableStateOf("") }
        var selectedPaymentMethod by remember { mutableStateOf("Wallet") }
        var showPaystackSheet by remember { mutableStateOf(false) }
        var isSubmitting by remember { mutableStateOf(false) }
        var redeemPoints by remember { mutableStateOf(false) }

        val subtotal = cartItems.sumOf { it.item.price * it.quantity }
        val deliveryFee = 1500.0
        val loyaltyPoints by viewModel.loyaltyPoints.collectAsState()
        val pointsDiscount = if (redeemPoints && loyaltyPoints >= 500) 1000.0 else 0.0
        val grandTotal = (subtotal + deliveryFee - pointsDiscount).coerceAtLeast(0.0)

        ModalBottomSheet(
            onDismissRequest = { showCheckoutSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = if (isDark) Charcoal else GoldenWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.94f)
                    .imePadding()               // keyboard won't cover Pay button
                    .navigationBarsPadding()    // system nav bar clear
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ShoppingCart, null, tint = Gold, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Cart (${cartItems.size} ${if (cartItems.size == 1) "item" else "items"})",
                            fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = AppTextColor
                        )
                    }
                    IconButton(onClick = { showCheckoutSheet = false }) {
                        Icon(Icons.Filled.Close, null, tint = AppTextColor)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cart Items
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(cartItems, key = { it.item.id }) { cartItem ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isDark) LuxuryBlack else Color.White,
                            border = BorderStroke(1.dp, if (isDark) BorderDark else Slate.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Product image
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isDark) Charcoal else Color(0xFFEAEAEA)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (cartItem.item.imageUrl.isNotBlank()) {
                                        Image(
                                            painter = rememberAsyncImagePainter(cartItem.item.imageUrl),
                                            contentDescription = cartItem.item.title,
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Filled.ShoppingBag, null, tint = Gold, modifier = Modifier.size(24.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        cartItem.item.title,
                                        fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                        color = AppTextColor, maxLines = 1
                                    )
                                    Text(cartItem.item.vendorStore, fontSize = 10.sp, color = Gold)
                                    Text(
                                        "₦${String.format("%,.0f", cartItem.item.price)} × ${cartItem.quantity} = ₦${String.format("%,.0f", cartItem.item.price * cartItem.quantity)}",
                                        fontSize = 11.sp, color = AppTextColor.copy(alpha = 0.6f)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                // Quantity stepper — custom circular Box controls with exact centering
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) Charcoal else Color(0xFFEEEEEE))
                                            .clickable { viewModel.updateCartQuantity(cartItem.item.id, -1) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Remove, null,
                                            tint = AppTextColor, modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        "${cartItem.quantity}",
                                        fontWeight = FontWeight.Black, fontSize = 14.sp,
                                        color = AppTextColor,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.widthIn(min = 24.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Gold)
                                            .clickable { viewModel.updateCartQuantity(cartItem.item.id, 1) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Add, null,
                                            tint = Obsidian, modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                            .clickable { viewModel.removeFromCart(cartItem.item.id) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.DeleteOutline, null,
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                AddressAutocompleteField(
                    value = shippingAddress,
                    onValueChange = { shippingAddress = it },
                    onAddressSelected = { item ->
                        shippingAddress = item.displayInput
                    },
                    isDark = isDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Method
                Text(
                    "PAYMENT METHOD",
                    fontSize = 10.sp, fontWeight = FontWeight.Black, color = Gold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(
                        "Wallet" to "Bal: ₦${String.format("%,.0f", walletBalance)}",
                        "Paystack Card" to "Card · Bank Transfer"
                    ).forEach { (method, sub) ->
                        val sel = method == selectedPaymentMethod
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedPaymentMethod = method },
                            shape = RoundedCornerShape(14.dp),
                            color = if (sel) Gold.copy(alpha = 0.18f) else if (isDark) LuxuryBlack else Color.White,
                            border = BorderStroke(1.5.dp, if (sel) Gold else if (isDark) BorderDark else Slate)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    method, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                    color = if (sel) Gold else AppTextColor
                                )
                                Text(sub, fontSize = 10.sp, color = TextGray)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Loyalty Points Discount Option
                if (loyaltyPoints >= 500) {
                    Surface(
                        onClick = { redeemPoints = !redeemPoints },
                        shape = RoundedCornerShape(16.dp),
                        color = if (redeemPoints) Gold.copy(alpha = 0.15f) else if (isDark) LuxuryBlack else Color.White,
                        border = BorderStroke(1.dp, if (redeemPoints) Gold else if (isDark) BorderDark else Slate)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CardGiftcard, null, tint = Gold, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Redeem 500 Loyalty Pts", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                                    Text("Get ₦1,000 instant discount on order", fontSize = 10.sp, color = TextGray)
                                }
                            }
                            Switch(
                                checked = redeemPoints,
                                onCheckedChange = { redeemPoints = it },
                                colors = SwitchDefaults.colors(checkedTrackColor = Gold)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Price Summary
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) LuxuryBlack else Color.White,
                    border = BorderStroke(1.dp, if (isDark) BorderDark else Slate.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PriceLine("Items Subtotal", "₦${String.format("%,.0f", subtotal)}")
                        PriceLine("Dispatch Shipping", "₦${String.format("%,.0f", deliveryFee)}")
                        if (pointsDiscount > 0) {
                            PriceLine("Loyalty Pts Discount", "-₦${String.format("%,.0f", pointsDiscount)}")
                        }
                        HorizontalDivider(color = if (isDark) BorderDark else Slate, thickness = 0.8.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", fontSize = 14.sp, fontWeight = FontWeight.Black, color = AppTextColor)
                            Text(
                                "₦${String.format("%,.0f", grandTotal)}",
                                fontSize = 16.sp, fontWeight = FontWeight.Black, color = Gold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (shippingAddress.isBlank()) {
                            Toast.makeText(context, "Please enter a delivery address", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (selectedPaymentMethod == "Paystack Card") {
                            showPaystackSheet = true
                        } else {
                            isSubmitting = true
                            viewModel.checkoutMarketplaceCart(shippingAddress, "Wallet", redeemPoints) { ok, msg ->
                                isSubmitting = false
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                if (ok) {
                                    showCheckoutSheet = false
                                }
                            }
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Obsidian, strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(
                            if (selectedPaymentMethod == "Wallet") Icons.Filled.AccountBalanceWallet
                            else Icons.Filled.CreditCard,
                            null, modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "PAY ₦${String.format("%,.0f", grandTotal)}",
                            fontWeight = FontWeight.Black, fontSize = 15.sp
                        )
                    }
                }

                // Paystack WebView checkout — reuses ProfileScreens.kt composable
                if (showPaystackSheet) {
                    PaystackCheckoutSheet(
                        amount = grandTotal,
                        onPaymentComplete = { reference ->
                            showPaystackSheet = false
                            isSubmitting = true
                            viewModel.checkoutMarketplaceCart(shippingAddress, "Paystack:$reference", redeemPoints) { ok, msg ->
                                isSubmitting = false
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                if (ok) showCheckoutSheet = false
                            }
                        },
                        onDismiss = { showPaystackSheet = false }
                    )
                }
            }
        }
    }
}

// ── Private helpers ───────────────────────────────────────────────────────────

@Composable
private fun MarketplaceProductCard(
    item: MarketplaceItem,
    isDark: Boolean,
    onTap: () -> Unit,
    onAddToCart: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isDark) Charcoal else GoldenWhite,
        border = BorderStroke(1.dp, if (isDark) BorderDark else Slate.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .tactilePress(scaleDown = 0.97f) { onTap() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val painter = rememberAsyncImagePainter(item.imageUrl)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
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
                    Icon(Icons.Filled.ShoppingBag, null, tint = Gold, modifier = Modifier.size(28.dp))
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

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    color = if (isDark) Color.White else Obsidian,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Text(
                    item.vendorStore,
                    color = Gold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        "${item.rating} (${item.reviewsCount})",
                        color = TextGray, fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "₦${String.format("%,.0f", item.price)}",
                    color = if (isDark) Gold else Obsidian,
                    fontSize = 14.sp, fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                    IconButton(
                        onClick = onAddToCart,
                        enabled = item.stock > 0,
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                if (item.stock > 0) Gold else Gold.copy(alpha = 0.3f),
                                RoundedCornerShape(10.dp)
                            )
                            .tactilePress(scaleDown = 0.88f) { if (item.stock > 0) onAddToCart() }
                    ) {
                        Icon(
                            Icons.Filled.AddShoppingCart, null,
                            tint = Obsidian, modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = TextGray)
        Text(value, fontSize = 11.sp, color = AppTextColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun VendorStoreCard(
    store: MarketplaceStore,
    isDark: Boolean,
    onTap: () -> Unit
) {
    val initials = store.storeName.trim().split("\\s+".toRegex())
        .filter { it.isNotBlank() }
        .let { words ->
            when {
                words.isEmpty() -> "ES"
                words.size == 1 -> words[0].take(2).uppercase()
                else -> (words[0].firstOrNull()?.toString() ?: "") + (words[1].firstOrNull()?.toString() ?: "").uppercase()
            }
        }

    Surface(
        onClick = onTap,
        shape = RoundedCornerShape(18.dp),
        color = if (isDark) Charcoal else GoldenWhite,
        border = BorderStroke(1.dp, if (isDark) BorderDark else Slate.copy(alpha = 0.5f)),
        modifier = Modifier.width(158.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (store.logoUrl.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0xFF222222) else Color(0xFFEAEAEA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(store.logoUrl),
                            contentDescription = store.storeName,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Gold),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initials, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Obsidian)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        store.storeName,
                        fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTextColor,
                        maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Verified, null, tint = Gold, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Verified", fontSize = 9.sp, color = TextGray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                store.category,
                fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Gold,
                maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(11.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    String.format("%.1f", store.rating),
                    fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AppTextColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "${store.itemCount} items",
                    fontSize = 10.sp, color = TextGray
                )
            }
        }
    }
}
