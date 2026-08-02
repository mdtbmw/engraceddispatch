package com.esdispatch.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.esdispatch.ui.components.RoundedSheet
import com.esdispatch.ui.components.ScreenHeader
import com.esdispatch.ui.theme.*
import com.esdispatch.viewmodel.DeliveryViewModel

data class MarketplaceItem(
    val id: String,
    val title: String,
    val category: String,
    val price: Double,
    val rating: Double,
    val reviewsCount: Int,
    val imageUrl: String,
    val description: String
)

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
    var showCartDialog by remember { mutableStateOf<MarketplaceItem?>(null) }
    
    // Sample items list
    val marketplaceItems = remember {
        listOf(
            MarketplaceItem(
                id = "box_10",
                title = "Premium Shipping Box (10x)",
                category = "Packaging",
                price = 4500.0,
                rating = 4.8,
                reviewsCount = 92,
                imageUrl = "https://images.unsplash.com/photo-1589939705384-5185137a7f0f?auto=format&fit=crop&w=400&q=80",
                description = "High-quality double-walled corrugated cardboard boxes. Ideal for shipping fragile items and heavy packages securely."
            ),
            MarketplaceItem(
                id = "bubble_50",
                title = "Bubble Wrap Roll (50m)",
                category = "Packaging",
                price = 3500.0,
                rating = 4.9,
                reviewsCount = 145,
                imageUrl = "https://images.unsplash.com/photo-1521913626209-0fbf68f4c4b1?auto=format&fit=crop&w=400&q=80",
                description = "Lightweight shock-absorbent cushioning wrap. Perfect for protecting glassware, electronics, and delicate logistics valuables."
            ),
            MarketplaceItem(
                id = "tape_fragile",
                title = "Fragile Warning Tape (3x)",
                category = "Packaging",
                price = 1800.0,
                rating = 4.7,
                reviewsCount = 74,
                imageUrl = "https://images.unsplash.com/photo-1563245372-f21724e3856d?auto=format&fit=crop&w=400&q=80",
                description = "Heavy-duty adhesive packing tape printed with 'FRAGILE' text to alert handlers. Highly visible bright background."
            ),
            MarketplaceItem(
                id = "rider_bag",
                title = "Premium Dispatch Rider Bag",
                category = "Merchandise",
                price = 12500.0,
                rating = 4.9,
                reviewsCount = 205,
                imageUrl = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=400&q=80",
                description = "Insulated, waterproof, heavy-duty backpack with security reflectors. Keeps hot orders warm and protects items from Lagos rain."
            ),
            MarketplaceItem(
                id = "raincoat_shield",
                title = "Waterproof Rider Raincoat",
                category = "Security",
                price = 6500.0,
                rating = 4.6,
                reviewsCount = 58,
                imageUrl = "https://images.unsplash.com/photo-1548883354-7622d03aca27?auto=format&fit=crop&w=400&q=80",
                description = "Heavy duty double-layered waterproof rainsuit with high-visibility reflective neon patches. Essential for all-season dispatch riders."
            ),
            MarketplaceItem(
                id = "courier_pack_50",
                title = "Waterproof Mailing Flyer (50x)",
                category = "Packaging",
                price = 2800.0,
                rating = 4.7,
                reviewsCount = 112,
                imageUrl = "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=400&q=80",
                description = "Self-adhesive, tamper-proof courier flyer bags. Puncture resistant, moisture proof, and highly durable for shipping documents and clothes."
            ),
            MarketplaceItem(
                id = "rider_jacket",
                title = "ESDispatch Branded Vest",
                category = "Merchandise",
                price = 7500.0,
                rating = 4.9,
                reviewsCount = 310,
                imageUrl = "https://images.unsplash.com/photo-1578587018452-892bacefd3f2?auto=format&fit=crop&w=400&q=80",
                description = "Official premium branded reflective cargo utility vest. Features heavy-duty zippers, pocket compartments, and high-visibility branding."
            )
        )
    }
    
    // Filter logic
    val filteredItems = remember(searchQuery, selectedCategory) {
        marketplaceItems.filter { item ->
            val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) || 
                                item.description.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
            matchesSearch && matchesCategory
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
                title = "Logistics Marketplace",
                onBack = { onNavigate("Dashboard") }
            )
            
            RoundedSheet(
                modifier = Modifier.weight(1f),
                containerColor = if (isSystemDark) BackgroundDark else BackgroundLight
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp)
                ) {
                    // Search Bar Section (Padded)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .height(56.dp)
                            .background(
                                if (isSystemDark) Charcoal else GoldenWhiteLight,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .border(
                                1.dp,
                                if (isSystemDark) Gold.copy(alpha = 0.3f) else Slate,
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = if (isSystemDark) Gold else Obsidian,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = if (isSystemDark) Color.White else Obsidian,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            cursorBrush = SolidColor(if (isSystemDark) Gold else Obsidian),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search packaging, gear...",
                                            color = TextGray,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                    
                    // Categories horizontal slider
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val categories = listOf("All", "Packaging", "Merchandise", "Security")
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            val catBgColor = if (isSelected) Gold else (if (isSystemDark) Charcoal else GoldenWhite)
                            val catTextColor = if (isSelected) Obsidian else (if (isSystemDark) Color.White else Obsidian)
                            val catBorderColor = if (isSelected) Gold else (if (isSystemDark) BorderDark else Slate.copy(alpha = 0.5f))
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(catBgColor)
                                    .border(1.dp, catBorderColor, RoundedCornerShape(12.dp))
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat,
                                    color = catTextColor, // STRICT LOCK: Obsidian text on Gold background
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
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
                                    text = "No items found in this category",
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
                                    tonalElevation = 0.dp,
                                    shadowElevation = 0.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showCartDialog = item }
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
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Gold,
                                                modifier = Modifier.clickable { showCartDialog = item }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Add,
                                                    contentDescription = "Buy Now",
                                                    tint = Obsidian, // STRICT LOCK: Obsidian on Gold
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .padding(6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Purchase dialog / details sheets
    showCartDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showCartDialog = null },
            title = {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = if (isSystemDark) Color.White else Obsidian
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Image(
                        painter = rememberAsyncImagePainter(item.imageUrl),
                        contentDescription = item.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = item.description,
                        fontSize = 12.sp,
                        color = if (isSystemDark) TextGray else Obsidian,
                        lineHeight = 16.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PRICE:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGray
                        )
                        Text(
                            text = "₦${String.format("%,.2f", item.price)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSystemDark) Gold else Obsidian
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Order placed! Logistics center will deliver materials soon.", Toast.LENGTH_LONG).show()
                        showCartDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Order Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCartDialog = null }) {
                    Text("Cancel", color = if (isSystemDark) Color.White else Obsidian)
                }
            },
            containerColor = if (isSystemDark) Charcoal else GoldenWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }
}
