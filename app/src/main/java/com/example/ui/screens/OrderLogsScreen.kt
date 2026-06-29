package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Delivery
import com.example.ui.DeliveryViewModel
import com.example.ui.navigation.AppView
import com.example.ui.theme.*
import com.example.ui.components.*

@Composable
fun OrderLogsScreen(viewModel: DeliveryViewModel) {
    val deliveries by viewModel.allDeliveries.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var initialLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(700)
        initialLoading = false
    }

    ScreenScaffold(
        title = "DISPATCH LOGS",
        onBack = { viewModel.navigateBack() },
        rightContent = {
            Surface(color = Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp)) {
                Text("${deliveries.size}", color = BiroBlue, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
            }
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it },
                placeholder = { Text("Search by tracking, item name...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedBorderColor = BiroBlue, unfocusedBorderColor = CardBorderGray),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            val filtered = if (searchQuery.isEmpty()) deliveries
            else deliveries.filter {
                it.trackingNumber.contains(searchQuery, ignoreCase = true) ||
                it.itemName.contains(searchQuery, ignoreCase = true) ||
                it.deliveryType.contains(searchQuery, ignoreCase = true)
            }

            if (initialLoading) {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(4) {
                        ShimmerBox(height = 120.dp, corners = 24.dp)
                    }
                }
            } else if (filtered.isEmpty()) {
                EmptyState(Icons.Default.SearchOff, "No matching dispatch logs found", "Try a different search term")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    itemsIndexed(filtered, key = { _, item -> item.id }) { index, log ->
                        StaggeredItem(index) {
                            DeliveryLogCard(log) {
                                viewModel.setTrackingDelivery(log)
                                viewModel.navigateTo(AppView.ActiveTracking(log.trackingNumber))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliveryLogCard(delivery: Delivery, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, CardBorderGray),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val typeColor = when (delivery.deliveryType) {
                        "Express" -> BiroBlue; "Economy" -> SuccessGreen; "Batch" -> BiroBlue; else -> Color(0xFFF97316)
                    }
                    Box(Modifier.size(32.dp).clip(CircleShape).background(typeColor.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Icon(
                            when (delivery.deliveryType) { "Express" -> Icons.Default.TwoWheeler; "Economy" -> Icons.Default.Share; else -> Icons.Default.Inventory2 },
                            contentDescription = null, tint = typeColor, modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(delivery.trackingNumber, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Schedule: ${delivery.scheduledAt}", color = TextGray, fontSize = 11.sp)
                    }
                }
                StatusBadge(delivery.status)
            }
            Spacer(Modifier.height(10.dp))
            Text("Route: ${delivery.pickupAddress.split(",").first()} \u2192 ${delivery.deliveryAddress.split(",").first()}", color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Item: ${delivery.itemName} (${delivery.itemWeight}kg)", color = TextGray, fontSize = 11.sp)
                Text("\u20A6${delivery.totalAmount.toInt()}", color = BiroBlue, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
