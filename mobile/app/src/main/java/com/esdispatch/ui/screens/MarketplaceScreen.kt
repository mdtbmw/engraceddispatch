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
    val isSystemDark = MaterialTheme.colorScheme.background == BackgroundDark
    
    // State variables
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showItemDetails by remember { mutableStateOf<MarketplaceItem?>(null) }
    val cartItems by viewModel.cartItems.collectAsState()
    var showCheckoutSheet by remember { mutableStateOf(false) }
    
    val marketplaceItems by viewModel.marketplaceProducts.collectAsState()

    val categories = listOf("All", "Packaging", "Merchandise", "Gear", "Accessories")
    
    val filteredItems = marketplaceItems.filter { item ->
        val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
        val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) || 
                          item.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
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
                title = "Marketplace",
                onBack = { onNavigate("Dashboard") }
            )

            RoundedSheet(
                modifier = Modifier.weight(1f)
            ) {

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            placeholder = { Text("Search products...", color = TextGray) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = TextGray) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gold,
                unfocusedBorderColor = if (isSystemDark) BorderDark else Slate,
                focusedContainerColor = if (isSystemDark) Charcoal else GoldenWhite,
                unfocusedContainerColor = if (isSystemDark) Charcoal else GoldenWhite
            ),
            singleLine = true
        )
        
        // Categories
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Gold else (if (isSystemDark) Charcoal else GoldenWhite),
                    border = if (!isSelected) BorderStroke(1.dp, if (isSystemDark) BorderDark else Slate.copy(alpha = 0.5f)) else null,
                    modifier = Modifier.clickable { selectedCategory = category }
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) Obsidian else (if (isSystemDark) Color.White else Obsidian),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Products Grid List
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Store,
                        contentDescription = null,
                        tint = TextGray.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No items found",
                        color = TextGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredItems) { item ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSystemDark) Charcoal else GoldenWhite,
                        border = BorderStroke(1.dp, if (isSystemDark) BorderDark else Slate.copy(alpha = 0.5f)),
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
                            Image(
                                painter = rememberAsyncImagePainter(item.imageUrl),
                                contentDescription = item.title,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = item.title,
                                    color = if (isSystemDark) Color.White else Obsidian,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.description,
                                    color = TextGray,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    lineHeight = 14.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = Gold,
                                        modifier = Modifier.size(12.dp)
                                    )
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
                                    color = if (isSystemDark) Gold else Obsidian,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
            }
        }
    }
    
    // Purchase dialog / details sheets
    val item = showItemDetails
    if (item != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showItemDetails = null },
            sheetState = sheetState,
            containerColor = if (isSystemDark) Charcoal else GoldenWhite,
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
                Image(
                    painter = rememberAsyncImagePainter(item.imageUrl),
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = if (isSystemDark) Color.White else Obsidian
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                    Text(text = "${item.rating} (${item.reviewsCount} reviews)", color = TextGray, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = item.description,
                    fontSize = 14.sp,
                    color = if (isSystemDark) TextGray else Obsidian,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                
                // Price & Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Price", fontSize = 12.sp, color = TextGray)
                        Text(
                            text = "₦${String.format("%,.2f", item.price)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSystemDark) Gold else Obsidian
                        )
                    }
                    Button(
                        onClick = {
                            viewModel.addToCart(item)
                            Toast.makeText(context, "${item.title} added to cart", Toast.LENGTH_SHORT).show()
                            showItemDetails = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(50.dp).padding(start = 16.dp)
                    ) {
                        Text("Add to Cart", fontWeight = FontWeight.Black)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    if (cartItems.isNotEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { showCheckoutSheet = true },
                containerColor = Gold,
                contentColor = Obsidian,
                modifier = Modifier
                    .padding(end = 24.dp, bottom = 100.dp)
                    .size(64.dp)
            ) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ) {
                            val count = cartItems.sumOf { it.quantity }
                            Text(count.toString())
                        }
                    }
                ) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart", modifier = Modifier.size(32.dp))
                }
            }
        }
    }
    
    if (showCheckoutSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var address by remember { mutableStateOf("") }
        ModalBottomSheet(
            onDismissRequest = { showCheckoutSheet = false },
            sheetState = sheetState,
            containerColor = if (isSystemDark) Charcoal else GoldenWhite,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier.fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text("Checkout", fontSize = 24.sp, fontWeight = FontWeight.Black, color = if (isSystemDark) Gold else Obsidian)
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(cartItems) { cartItem ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(cartItem.item.title, modifier = Modifier.weight(1f), color = if (isSystemDark) Color.White else Obsidian)
                            Text("x${cartItem.quantity}", color = TextGray)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("₦${String.format("%,.0f", cartItem.item.price * cartItem.quantity)}", fontWeight = FontWeight.Bold, color = if (isSystemDark) Gold else Obsidian)
                        }
                    }
                }
                
                HorizontalDivider(color = if (isSystemDark) BorderDark else Slate)
                Spacer(modifier = Modifier.height(16.dp))
                
                val total = cartItems.sumOf { it.item.price * it.quantity }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextGray)
                    Text("₦${String.format("%,.0f", total)}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = if (isSystemDark) Gold else Obsidian)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Delivery Address") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = if (isSystemDark) BorderDark else Slate
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        if (address.isBlank()) {
                            Toast.makeText(context, "Please enter an address", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.checkoutMarketplaceCart(address) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (success) {
                                showCheckoutSheet = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Pay with Wallet", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
}
