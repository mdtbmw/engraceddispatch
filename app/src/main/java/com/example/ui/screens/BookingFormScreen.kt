package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

@Composable
fun BookingFormScreen(viewModel: DeliveryViewModel) {
    val activeType by viewModel.selectedDeliveryType.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val walletBalance by viewModel.walletBalance.collectAsState()
    val error by viewModel.error.collectAsState()
    var pickup by remember { mutableStateOf("") }
    var delivery by remember { mutableStateOf("") }
    var itemName by remember { mutableStateOf("") }
    var itemWeightStr by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("Immediate") }

    Box(Modifier.fillMaxSize().background(LuxuryBlack)) {
        Column(Modifier.fillMaxSize().background(BrandGradient)) {
            Box(Modifier.fillMaxWidth().statusBarsPadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.clearError(); viewModel.navigateBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                    Text("NEW ${activeType.uppercase()} DISPATCH", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                    Box(Modifier.size(48.dp))
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundGray),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                if (isLoading) {
                    Column(Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        ShimmerBox(height = 70.dp)
                        ShimmerBox(height = 40.dp)
                        ShimmerBox(height = 90.dp)
                        ShimmerBox(height = 60.dp)
                        ShimmerBox(height = 60.dp)
                        ShimmerBox(height = 150.dp)
                        ShimmerBox(height = 56.dp)
                    }
                } else {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 120.dp)) {
                    error?.let {
                        Card(colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.1f)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(it, color = DangerRed, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                IconButton(onClick = { viewModel.clearError() }, modifier = Modifier.size(20.dp)) { Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = DangerRed, modifier = Modifier.size(14.dp)) }
                            }
                        }
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text("Wallet Balance", color = TextGray, fontSize = 11.sp)
                                    Text("\u20A6${String.format("%,.2f", walletBalance)}", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            val totalCost = viewModel.getBasePrice(activeType) + viewModel.getSurgeAmount(activeType)
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
                    Text("Routing Addresses", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    BookingField(value = pickup, onValueChange = { pickup = it }, label = "Pickup Location", icon = Icons.Default.LocationOn, tint = BiroBlue)
                    Spacer(Modifier.height(12.dp))
                    BookingField(value = delivery, onValueChange = { delivery = it }, label = "Delivery Destination", icon = Icons.Default.Flag, tint = SuccessGreen)
                    Spacer(Modifier.height(20.dp))
                    Text("Package Information", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    BookingField(value = itemName, onValueChange = { itemName = it }, label = "Item Name & Category", icon = Icons.Default.Inventory, tint = TextGray)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = itemWeightStr, onValueChange = { itemWeightStr = it }, label = { Text("Weight (KG)") }, leadingIcon = { Icon(Icons.Default.Scale, contentDescription = null, tint = TextGray) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, colors = fieldColors(), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(24.dp))
                    PriceBreakdown(activeType, viewModel)
                    Spacer(Modifier.height(24.dp))

                    val isFormValid = pickup.isNotBlank() && delivery.isNotBlank() && itemName.isNotBlank() && itemWeightStr.toDoubleOrNull() != null
                    val totalCost = viewModel.getBasePrice(activeType) + viewModel.getSurgeAmount(activeType)
                    val canBook = isFormValid && walletBalance >= totalCost

                    PremiumGradientButton(
                        text = if (isLoading) "Processing..." else "Confirm Booking",
                        icon = Icons.Default.Check,
                        onClick = {
                            if (!isFormValid) return@PremiumGradientButton
                            val w = itemWeightStr.toDoubleOrNull() ?: 1.0
                            viewModel.createBooking(pickup, delivery, itemName, w, selectedDate, "Immediate") {}
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
}

@Composable
private fun BookingField(value: String, onValueChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) }, leadingIcon = { Icon(icon, contentDescription = null, tint = tint) }, singleLine = true, colors = fieldColors(), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
}

@Composable
private fun PriceBreakdown(type: String, viewModel: DeliveryViewModel) {
    val base = viewModel.getBasePrice(type); val surge = viewModel.getSurgeAmount(type); val total = base.toInt() + surge
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Price Breakdown", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            InfoRow("Base Price ($type)", "\u20A6${base.toInt()}")
            if (surge > 0) { Spacer(Modifier.height(6.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(12.dp)); Spacer(Modifier.width(4.dp)); Text("Peak Surcharge (1.35x)", color = Color(0xFFF97316), fontSize = 12.sp, fontWeight = FontWeight.Bold) }; Text("+\u20A6$surge", color = Color(0xFFF97316), fontSize = 12.sp, fontWeight = FontWeight.Bold) } }
            Spacer(Modifier.height(6.dp)); InfoRow("Priority Rider fee", "FREE", SuccessGreen); Spacer(Modifier.height(6.dp)); InfoRow("Transit Safety Cover", "\u20A60.00", SuccessGreen)
            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = CardBorderGray)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Total Estimate", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold); Text("\u20A6$total", color = BiroBlue, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold) }
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
