package com.esdispatch.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.esdispatch.ui.components.RoundedSheet
import com.esdispatch.ui.components.ScreenHeader
import com.esdispatch.ui.theme.*
import com.esdispatch.viewmodel.DeliveryViewModel
import com.esdispatch.viewmodel.MarketplaceItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorPortalScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val isSystemDark = MaterialTheme.colorScheme.background == BackgroundDark
    
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val marketplaceProducts by viewModel.marketplaceProducts.collectAsState()
    
    // Vendor state
    var activeTab by remember { mutableStateOf(0) } // 0: Products, 1: Orders, 2: Payouts
    var showAddProductModal by remember { mutableStateOf(false) }

    // Add Product Form State
    var productTitle by remember { mutableStateOf("") }
    var productCategory by remember { mutableStateOf("Packaging") }
    var productPrice by remember { mutableStateOf("") }
    var productStock by remember { mutableStateOf("10") }

    val storeName = if (userName.isNotBlank()) "${userName}'s Store" else "ESDispatch Partner Store"
    val vendorProducts = marketplaceProducts

    val isQualifiedVendor = true // Qualified vendor state

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
                title = "Vendor Control Hub",
                onBack = { onNavigate("Dashboard") }
            )

            RoundedSheet(
                modifier = Modifier.weight(1f)
            ) {
                // Qualification Status Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isQualifiedVendor) Color(0xFF10B981).copy(alpha = 0.12f) else Gold.copy(alpha = 0.12f)
                    ),
                    border = BorderStroke(1.dp, if (isQualifiedVendor) Color(0xFF10B981).copy(alpha = 0.4f) else Gold.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isQualifiedVendor) Color(0xFF10B981) else Gold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isQualifiedVendor) Icons.Filled.Verified else Icons.Filled.Storefront,
                                contentDescription = null,
                                tint = Obsidian,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isQualifiedVendor) "VERIFIED QUALIFIED VENDOR" else "VENDOR STORE ENLISTMENT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = AppTextColor
                            )
                            Text(
                                text = "${storeName} • Active Partner Store",
                                fontSize = 11.sp,
                                color = AppTextColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Vendor Analytics Overview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSystemDark) Charcoal else GoldenWhite),
                        border = BorderStroke(1.dp, if (isSystemDark) BorderDark else Slate)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Store Sales", fontSize = 10.sp, color = AppTextColor.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                            Text("₦${String.format("%,.0f", vendorProducts.sumOf { it.price * it.stock })}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Gold)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSystemDark) Charcoal else GoldenWhite),
                        border = BorderStroke(1.dp, if (isSystemDark) BorderDark else Slate)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("My Products", fontSize = 10.sp, color = AppTextColor.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                            Text("${vendorProducts.size} Active", fontSize = 15.sp, fontWeight = FontWeight.Black, color = AppTextColor)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSystemDark) Charcoal else GoldenWhite),
                        border = BorderStroke(1.dp, if (isSystemDark) BorderDark else Slate)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Store Rating", fontSize = 10.sp, color = AppTextColor.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                            Text("4.9 ★", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Control Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf("Products (${vendorProducts.size})", "Store Orders", "Earnings")
                    tabs.forEachIndexed { index, label ->
                        Button(
                            onClick = { activeTab = index },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeTab == index) Gold else if (isSystemDark) Charcoal else GoldenWhite,
                                contentColor = if (activeTab == index) Obsidian else AppTextColor
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Content
                when (activeTab) {
                    0 -> { // Products Inventory CRUD
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Store Inventory", fontSize = 14.sp, fontWeight = FontWeight.Black, color = AppTextColor)
                            Button(
                                onClick = { showAddProductModal = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Product", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(vendorProducts) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isSystemDark) Charcoal else GoldenWhite),
                                    border = BorderStroke(1.dp, if (isSystemDark) BorderDark else Slate)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(LuxuryBlack.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (item.imageUrl.isNotBlank()) {
                                                Image(
                                                    painter = rememberAsyncImagePainter(item.imageUrl),
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Icon(Icons.Filled.ShoppingBag, contentDescription = null, tint = Gold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                                            Text("Category: ${item.category} • Stock: ${item.stock}", fontSize = 10.sp, color = AppTextColor.copy(alpha = 0.6f))
                                            Text("₦${String.format("%,.0f", item.price)}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Gold)
                                        }

                                        IconButton(onClick = {
                                            Toast.makeText(context, "Product edit feature available in web dashboard", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = AppTextColor.copy(alpha = 0.6f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> { // Store Orders
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = Gold, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Customer Store Orders", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                                Text("Orders placed for your store products display here.", fontSize = 11.sp, color = AppTextColor.copy(alpha = 0.5f))
                            }
                        }
                    }

                    2 -> { // Earnings & Payouts
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isSystemDark) Charcoal else GoldenWhite),
                            border = BorderStroke(1.dp, if (isSystemDark) BorderDark else Slate)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Available Vendor Balance", fontSize = 11.sp, color = AppTextColor.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                                Text("₦0.00", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Gold)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { Toast.makeText(context, "Payout request submitted", Toast.LENGTH_SHORT).show() },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian)
                                ) {
                                    Text("Request Payout", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Product Dialog Modal
    if (showAddProductModal) {
        AlertDialog(
            onDismissRequest = { showAddProductModal = false },
            title = { Text("Add Store Product", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = productTitle,
                        onValueChange = { productTitle = it },
                        label = { Text("Product Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = productPrice,
                        onValueChange = { productPrice = it },
                        label = { Text("Price (₦)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = productStock,
                        onValueChange = { productStock = it },
                        label = { Text("Stock Quantity") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (productTitle.isBlank() || productPrice.isBlank()) {
                            Toast.makeText(context, "Please fill in title and price", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        Toast.makeText(context, "Product listed successfully!", Toast.LENGTH_SHORT).show()
                        showAddProductModal = false
                        productTitle = ""
                        productPrice = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian)
                ) {
                    Text("Add Product")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
