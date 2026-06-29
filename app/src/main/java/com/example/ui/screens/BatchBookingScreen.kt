package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DeliveryViewModel
import com.example.ui.navigation.AppView
import com.example.ui.theme.*
import com.example.ui.components.*

private data class DestinationInfo(
    val id: Long,
    var name: String = "",
    var phone: String = "",
    var address: String = "",
    var itemName: String = "",
    var category: String = "Documents",
    var weightStr: String = ""
)

@Composable
fun BatchBookingScreen(viewModel: DeliveryViewModel) {
    val activeType = "Batch"
    val isLoading by viewModel.isLoading.collectAsState()
    val walletBalance by viewModel.walletBalance.collectAsState()
    val error by viewModel.error.collectAsState()

    var pickup by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("Immediate") }

    var nextDestId by remember { mutableLongStateOf(2L) }
    val destinations = remember {
        mutableStateListOf(
            DestinationInfo(1L)
        )
    }

    val categories = listOf("Documents", "Electronics", "Fashion", "Food", "Fragile", "Others")

    Box(Modifier.fillMaxSize().background(LuxuryBlack)) {
        Column(Modifier.fillMaxSize().background(BrandGradient)) {
            Box(Modifier.fillMaxWidth().statusBarsPadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.clearError(); viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text("BATCH DISPATCH (MULTI-DROP)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                    Box(Modifier.size(48.dp))
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundGray),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 120.dp)) {
                    error?.let {
                        Card(colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.1f)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(it, color = DangerRed, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                IconButton(onClick = { viewModel.clearError() }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = DangerRed, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    // Wallet check card
                    val additionalStopsFee = (destinations.size - 1) * 800.0
                    val totalCost = viewModel.getBasePrice(activeType) + additionalStopsFee
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text("Wallet Balance", color = TextGray, fontSize = 11.sp)
                                    Text("₦${String.format("%,.2f", walletBalance)}", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            val canAfford = walletBalance >= totalCost
                            Surface(color = if (canAfford) SuccessGreen.copy(alpha = 0.1f) else DangerRed.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    if (canAfford) "Sufficient" else "Insufficient",
                                    color = if (canAfford) SuccessGreen else DangerRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Timeline Schedule Picker
                    Text("Select Schedule Date", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Immediate", "Today", "Tomorrow", "Next 3 Days", "Weekend").forEach { date ->
                            val isSel = selectedDate == date
                            Card(
                                colors = CardDefaults.cardColors(containerColor = if (isSel) BiroBlue else Color.White),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { selectedDate = date },
                                border = if (isSel) null else BorderStroke(1.dp, CardBorderGray)
                            ) {
                                Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(date, color = if (isSel) Color.White else TextMain, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text("Pickup Information", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pickup,
                        onValueChange = { pickup = it },
                        label = { Text("Pickup Location") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = BiroBlue) },
                        singleLine = true,
                        colors = fieldColors(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(20.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Destinations (${destinations.size})", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        if (destinations.size < 5) {
                            TextButton(onClick = {
                                destinations.add(DestinationInfo(nextDestId))
                                nextDestId++
                            }) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add Destination", color = BiroBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))

                    destinations.forEachIndexed { index, dest ->
                        var nameVal by remember(dest.id) { mutableStateOf(dest.name) }
                        var phoneVal by remember(dest.id) { mutableStateOf(dest.phone) }
                        var addressVal by remember(dest.id) { mutableStateOf(dest.address) }
                        var itemVal by remember(dest.id) { mutableStateOf(dest.itemName) }
                        var catVal by remember(dest.id) { mutableStateOf(dest.category) }
                        var weightVal by remember(dest.id) { mutableStateOf(dest.weightStr) }

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, CardBorderGray),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Destination #${index + 1}", color = BiroBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    if (destinations.size > 1) {
                                        IconButton(onClick = { destinations.removeAt(index) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = nameVal,
                                    onValueChange = { nameVal = it; dest.name = it },
                                    label = { Text("Recipient Name") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextGray) },
                                    singleLine = true,
                                    colors = fieldColors(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = phoneVal,
                                    onValueChange = { phoneVal = it; dest.phone = it },
                                    label = { Text("Recipient Phone Number") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TextGray) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    colors = fieldColors(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = addressVal,
                                    onValueChange = { addressVal = it; dest.address = it },
                                    label = { Text("Drop-off Address") },
                                    leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null, tint = SuccessGreen) },
                                    singleLine = true,
                                    colors = fieldColors(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = itemVal,
                                    onValueChange = { itemVal = it; dest.itemName = it },
                                    label = { Text("Item Name") },
                                    leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null, tint = TextGray) },
                                    singleLine = true,
                                    colors = fieldColors(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(10.dp))
                                Text("Item Category", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(4.dp))
                                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    categories.forEach { cat ->
                                        val isSel = catVal == cat
                                        Surface(
                                            color = if (isSel) BiroBlue.copy(alpha = 0.1f) else Color.Transparent,
                                            border = BorderStroke(1.dp, if (isSel) BiroBlue else CardBorderGray),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable { catVal = cat; dest.category = cat }
                                        ) {
                                            Text(
                                                cat,
                                                color = if (isSel) BiroBlue else TextMain,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = weightVal,
                                    onValueChange = { weightVal = it; dest.weightStr = it },
                                    label = { Text("Weight (KG)") },
                                    leadingIcon = { Icon(Icons.Default.Scale, contentDescription = null, tint = TextGray) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    colors = fieldColors(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    // Price Breakdown
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Price Breakdown", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(10.dp))
                            InfoRow("Base Price (Batch Pickup)", "₦${viewModel.getBasePrice(activeType).toInt()}")
                            if (destinations.size > 1) {
                                InfoRow("Additional Stops (${destinations.size - 1} stops)", "₦${additionalStopsFee.toInt()}")
                            }
                            InfoRow("Transit Safety Cover", "₦0.00", SuccessGreen)
                            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = CardBorderGray)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Total Estimate", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                                Text("₦${totalCost.toInt()}", color = BiroBlue, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    val isFormValid = pickup.isNotBlank() && destinations.all {
                        it.name.isNotBlank() && it.address.isNotBlank() && it.itemName.isNotBlank() && it.weightStr.toDoubleOrNull() != null
                    }
                    val canBook = isFormValid && walletBalance >= totalCost

                    PremiumGradientButton(
                        text = if (isLoading) "Processing..." else "Confirm Batch Booking",
                        icon = Icons.Default.Check,
                        onClick = {
                            if (!isFormValid) return@PremiumGradientButton
                            // Loop and book
                            destinations.forEachIndexed { i, dest ->
                                val w = dest.weightStr.toDoubleOrNull() ?: 1.0
                                val stopLabel = "Stop #${i + 1}: ${dest.name} (${dest.phone})"
                                viewModel.createBooking(
                                    pickup = pickup,
                                    delivery = dest.address,
                                    itemName = "${dest.itemName} (${dest.category}) [Batch to $stopLabel]",
                                    itemWeight = w,
                                    date = selectedDate,
                                    time = "Immediate"
                                ) {}
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !isLoading && canBook
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextMain, unfocusedTextColor = TextMain,
    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
    focusedBorderColor = BiroBlue, unfocusedBorderColor = CardBorderGray,
    focusedLabelColor = BiroBlue, unfocusedLabelColor = TextGray, cursorColor = BiroBlue
)
