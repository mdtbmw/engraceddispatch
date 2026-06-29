package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

private data class PickupInfo(
    val id: Long,
    var senderName: String = "",
    var senderPhone: String = "",
    var address: String = "",
    var itemName: String = "",
    var category: String = "Fashion",
    var weightStr: String = ""
)

@Composable
fun MultiBookingScreen(viewModel: DeliveryViewModel) {
    val activeType = "Multi-Pickup"
    val isLoading by viewModel.isLoading.collectAsState()
    val walletBalance by viewModel.walletBalance.collectAsState()
    val error by viewModel.error.collectAsState()

    var delivery by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("Immediate") }

    var nextPickupId by remember { mutableLongStateOf(2L) }
    val pickups = remember {
        mutableStateListOf(
            PickupInfo(1L)
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
                    Text("MULTI-PICKUP DISPATCH", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
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
                    val additionalPickupsFee = (pickups.size - 1) * 1000.0
                    val totalCost = viewModel.getBasePrice(activeType) + additionalPickupsFee
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
                    Text("Delivery Destination", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = delivery,
                        onValueChange = { delivery = it },
                        label = { Text("Delivery Destination") },
                        leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null, tint = SuccessGreen) },
                        singleLine = true,
                        colors = fieldColors(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(20.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Pickup Vendors (${pickups.size})", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        if (pickups.size < 5) {
                            TextButton(onClick = {
                                pickups.add(PickupInfo(nextPickupId))
                                nextPickupId++
                            }) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add Vendor", color = BiroBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))

                    pickups.forEachIndexed { index, pick ->
                        var nameVal by remember(pick.id) { mutableStateOf(pick.senderName) }
                        var phoneVal by remember(pick.id) { mutableStateOf(pick.senderPhone) }
                        var addressVal by remember(pick.id) { mutableStateOf(pick.address) }
                        var itemVal by remember(pick.id) { mutableStateOf(pick.itemName) }
                        var catVal by remember(pick.id) { mutableStateOf(pick.category) }
                        var weightVal by remember(pick.id) { mutableStateOf(pick.weightStr) }

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, CardBorderGray),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Vendor #${index + 1}", color = BiroBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    if (pickups.size > 1) {
                                        IconButton(onClick = { pickups.removeAt(index) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = nameVal,
                                    onValueChange = { nameVal = it; pick.senderName = it },
                                    label = { Text("Vendor Name") },
                                    leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = TextGray) },
                                    singleLine = true,
                                    colors = fieldColors(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = phoneVal,
                                    onValueChange = { phoneVal = it; pick.senderPhone = it },
                                    label = { Text("Vendor Phone Number") },
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
                                    onValueChange = { addressVal = it; pick.address = it },
                                    label = { Text("Pickup Address") },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = BiroBlue) },
                                    singleLine = true,
                                    colors = fieldColors(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = itemVal,
                                    onValueChange = { itemVal = it; pick.itemName = it },
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
                                            modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable { catVal = cat; pick.category = cat }
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
                                    onValueChange = { weightVal = it; pick.weightStr = it },
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
                            InfoRow("Base Price (Multi-Pickup Drop)", "₦${viewModel.getBasePrice(activeType).toInt()}")
                            if (pickups.size > 1) {
                                InfoRow("Additional Pickups (${pickups.size - 1} stops)", "₦${additionalPickupsFee.toInt()}")
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

                    val isFormValid = delivery.isNotBlank() && pickups.all {
                        it.senderName.isNotBlank() && it.address.isNotBlank() && it.itemName.isNotBlank() && it.weightStr.toDoubleOrNull() != null
                    }
                    val canBook = isFormValid && walletBalance >= totalCost

                    PremiumGradientButton(
                        text = if (isLoading) "Processing..." else "Confirm Multi-Pickup Booking",
                        icon = Icons.Default.Check,
                        onClick = {
                            if (!isFormValid) return@PremiumGradientButton
                            // Loop and book each
                            pickups.forEachIndexed { i, pick ->
                                val w = pick.weightStr.toDoubleOrNull() ?: 1.0
                                val vendorLabel = "Vendor #${i + 1}: ${pick.senderName} (${pick.senderPhone})"
                                viewModel.createBooking(
                                    pickup = pick.address,
                                    delivery = delivery,
                                    itemName = "${pick.itemName} (${pick.category}) [Pickup from $vendorLabel]",
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
