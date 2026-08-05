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
    val displayItems = if (filteredItems.isNotEmpty()) filteredItems else rawItems
    
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
}
