package com.esdispatch.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.esdispatch.ui.theme.*
import com.esdispatch.viewmodel.DeliveryViewModel
import com.esdispatch.viewmodel.MarketplaceItem
import com.esdispatch.ui.components.ScreenHeader
import com.esdispatch.ui.components.RoundedSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val isDark = isDarkTheme
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showItemDetails by remember { mutableStateOf<MarketplaceItem?>(null) }
    val cartItems by viewModel.cartItems.collectAsState()
    var showCheckoutSheet by remember { mutableStateOf(false) }
    
    val marketplaceItems by viewModel.marketplaceProducts.collectAsState()

    val categories = listOf("All", "Delivery Gear", "Apparel", "Lubricants", "Accessories", "Safety")
    
    val rawItems = if (marketplaceItems.isNotEmpty()) marketplaceItems else viewModel.defaultSampleProducts
    val filteredItems = rawItems.filter { item ->
        val matchesCategory = selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() || 
                          item.title.contains(searchQuery, ignoreCase = true) || 
                          item.description.contains(searchQuery, ignoreCase = true) ||
                          item.vendorStore.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }
    val displayItems = if (filteredItems.isNotEmpty()) filteredItems else (if (rawItems.isNotEmpty()) rawItems else viewModel.defaultSampleProducts)
    
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
                title = "Marketplace Catalog",
                onBack = { onNavigate("Dashboard") }
            )

            RoundedSheet(
                modifier = Modifier.weight(1f)
            ) {
                // Vendor Portal Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { onNavigate("VendorPortal") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Gold.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Storefront, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Vendor Control Hub", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                                Text("Manage store products, sales & payouts", fontSize = 10.sp, color = AppTextColor.copy(alpha = 0.6f))
                            }
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Gold)
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    placeholder = { Text("Search catalog items...", color = TextGray) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = TextGray) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = if (isDark) BorderDark else Slate,
                        focusedContainerColor = if (isDark) Charcoal else GoldenWhite,
                        unfocusedContainerColor = if (isDark) Charcoal else GoldenWhite
                    ),
                    singleLine = true
                )
                
                // Categories Bar
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = category == selectedCategory
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Gold else (if (isDark) Charcoal else GoldenWhite),
                            border = if (!isSelected) BorderStroke(1.dp, if (isDark) BorderDark else Slate.copy(alpha = 0.5f)) else null,
                            modifier = Modifier.clickable { selectedCategory = category }
                        ) {
                            Text(
                                text = category,
                                color = if (isSelected) Obsidian else (if (isDark) Color.White else Obsidian),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
                
                // Products List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayItems) { item ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isDark) Charcoal else GoldenWhite,
                            border = BorderStroke(1.dp, if (isDark) BorderDark else Slate.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showItemDetails = item }
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
                                        Icon(Icons.Filled.ShoppingBag, contentDescription = null, tint = Gold, modifier = Modifier.size(28.dp))
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(14.dp))
                                
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = item.title,
                                        color = if (isDark) Color.White else Obsidian,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.vendorStore,
                                        color = Gold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = item.description,
                                        color = TextGray,
                                        fontSize = 10.sp,
                                        maxLines = 2,
                                        lineHeight = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Filled.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(11.dp))
                                        Text(
                                            text = "${item.rating} (${item.reviewsCount})",
                                            color = TextGray,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "₦${String.format("%,.0f", item.price)}",
                                        color = if (isDark) Gold else Obsidian,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    IconButton(
                                        onClick = {
                                            viewModel.addToCart(item)
                                            Toast.makeText(context, "Added ${item.title} to Cart", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(Gold, RoundedCornerShape(10.dp))
                                    ) {
                                        Icon(Icons.Filled.AddShoppingCart, contentDescription = "Add", tint = Obsidian, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Cart Summary Bar
        if (cartItems.isNotEmpty()) {
            val totalCartPrice = cartItems.sumOf { it.item.price * it.quantity }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .clickable { showCheckoutSheet = true },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Gold),
                elevation = CardDefaults.cardElevation(8.dp)
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
                            Text("${cartItems.sumOf { it.quantity }}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("View Cart Checkout", fontWeight = FontWeight.Bold, color = Obsidian, fontSize = 14.sp)
                    }
                    Text("₦${String.format("%,.0f", totalCartPrice)}", fontWeight = FontWeight.Black, color = Obsidian, fontSize = 16.sp)
                }
            }
        }
    }
    
    // Purchase dialog / details sheet
    val item = showItemDetails
    if (item != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showItemDetails = null },
            sheetState = sheetState,
            containerColor = if (isDark) Charcoal else GoldenWhite,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier.fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.Top
            ) {
                val painter = rememberAsyncImagePainter(item.imageUrl)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
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
                        Icon(Icons.Filled.ShoppingBag, contentDescription = null, tint = Gold, modifier = Modifier.size(56.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(item.title, fontSize = 20.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Obsidian)
                Text(item.vendorStore, fontSize = 12.sp, color = Gold, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("₦${String.format("%,.0f", item.price)}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = if (isDark) Gold else Obsidian)
                Spacer(modifier = Modifier.height(12.dp))
                Text(item.description, fontSize = 13.sp, color = TextGray, lineHeight = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        viewModel.addToCart(item)
                        Toast.makeText(context, "Added to cart!", Toast.LENGTH_SHORT).show()
                        showItemDetails = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Add Product to Cart", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }

    // Real Marketplace Cart & Checkout Bottom Sheet
    if (showCheckoutSheet && cartItems.isNotEmpty()) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val walletBalance by viewModel.walletBalance.collectAsState()
        var shippingAddress by remember { mutableStateOf("12 Ikeja City Mall Way, Lagos") }
        var selectedPaymentMethod by remember { mutableStateOf("Wallet") }
        var isSubmitting by remember { mutableStateOf(false) }

        val subtotal = cartItems.sumOf { it.item.price * it.quantity }
        val deliveryFee = 1500.0
        val grandTotal = subtotal + deliveryFee

        ModalBottomSheet(
            onDismissRequest = { showCheckoutSheet = false },
            sheetState = sheetState,
            containerColor = if (isDark) Charcoal else GoldenWhite,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier.fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = null, tint = Gold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Cart Checkout (${cartItems.size})", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = AppTextColor)
                    }
                    IconButton(onClick = { showCheckoutSheet = false }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = AppTextColor)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cart Items List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(cartItems) { cartItem ->
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
                                Image(
                                    painter = rememberAsyncImagePainter(cartItem.item.imageUrl),
                                    contentDescription = cartItem.item.title,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cartItem.item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTextColor, maxLines = 1)
                                    Text(cartItem.item.vendorStore, fontSize = 10.sp, color = Gold, fontWeight = FontWeight.SemiBold)
                                    Text("₦${String.format("%,.0f", cartItem.item.price)} each", fontSize = 11.sp, color = TextGray)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.updateCartQuantity(cartItem.item.id, -1) },
                                        modifier = Modifier.size(28.dp).background(if (isDark) Charcoal else GoldenWhite, CircleShape)
                                    ) {
                                        Icon(Icons.Filled.Remove, contentDescription = "Decrease", tint = AppTextColor, modifier = Modifier.size(14.dp))
                                    }
                                    Text("${cartItem.quantity}", modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppTextColor)
                                    IconButton(
                                        onClick = { viewModel.updateCartQuantity(cartItem.item.id, 1) },
                                        modifier = Modifier.size(28.dp).background(Gold, CircleShape)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Increase", tint = Obsidian, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Shipping Address Input
                Text("SHIPPING DESTINATION", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Gold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = shippingAddress,
                    onValueChange = { shippingAddress = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = if (isDark) BorderDark else Slate,
                        focusedContainerColor = if (isDark) LuxuryBlack else Color.White,
                        unfocusedContainerColor = if (isDark) LuxuryBlack else Color.White
                    ),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AppTextColor)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Method Options
                Text("SELECT PAYMENT METHOD", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Gold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPaymentMethod = "Wallet" },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selectedPaymentMethod == "Wallet") Gold.copy(alpha = 0.2f) else (if (isDark) LuxuryBlack else Color.White),
                        border = BorderStroke(1.5.dp, if (selectedPaymentMethod == "Wallet") Gold else BorderDark)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("ESDispatch Wallet", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                            Text("Bal: ₦${String.format("%,.0f", walletBalance)}", fontSize = 10.sp, color = Gold, fontWeight = FontWeight.Black)
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPaymentMethod = "Paystack Card" },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selectedPaymentMethod == "Paystack Card") Gold.copy(alpha = 0.2f) else (if (isDark) LuxuryBlack else Color.White),
                        border = BorderStroke(1.5.dp, if (selectedPaymentMethod == "Paystack Card") Gold else BorderDark)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Debit Card / Transfer", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                            Text("Instant Paystack Gateway", fontSize = 10.sp, color = TextGray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Price Summary Breakdown
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDark) LuxuryBlack else Color.White, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Items Subtotal", fontSize = 11.sp, color = TextGray)
                        Text("₦${String.format("%,.0f", subtotal)}", fontSize = 11.sp, color = AppTextColor, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Dispatch Express Shipping", fontSize = 11.sp, color = TextGray)
                        Text("₦${String.format("%,.0f", deliveryFee)}", fontSize = 11.sp, color = AppTextColor, fontWeight = FontWeight.Bold)
                    }
                    Divider(color = if (isDark) BorderDark else Slate, thickness = 0.8.dp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grand Total Amount", fontSize = 13.sp, fontWeight = FontWeight.Black, color = AppTextColor)
                        Text("₦${String.format("%,.0f", grandTotal)}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Gold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        isSubmitting = true
                        viewModel.checkoutMarketplaceCart(shippingAddress, selectedPaymentMethod) { success, msg ->
                            isSubmitting = false
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (success) {
                                showCheckoutSheet = false
                                onNavigate("OrderLogs")
                            }
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Obsidian, strokeWidth = 2.5.dp)
                    } else {
                        Text("PAY ₦${String.format("%,.0f", grandTotal)} & PLACE ORDER", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            }
        }
    }

}
