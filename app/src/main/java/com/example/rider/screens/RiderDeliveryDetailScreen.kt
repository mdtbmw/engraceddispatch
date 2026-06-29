package com.example.rider.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rider.RiderViewModel
import com.example.rider.components.*
import com.example.rider.navigation.RiderView
import com.example.ui.theme.*
import com.example.ui.components.ScreenHeader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderDeliveryDetailScreen(viewModel: RiderViewModel, trackingNumber: String) {
    val haptic = LocalHapticFeedback.current
    val delivery by viewModel.currentDelivery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var otpInput by remember { mutableStateOf("") }
    var showOtpSheet by remember { mutableStateOf(false) }
    var otpResult by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(trackingNumber) { viewModel.loadDeliveryByTracking(trackingNumber) }

    val d = delivery

    Box(Modifier.fillMaxSize().background(LuxuryBlack)) {
        Column(Modifier.fillMaxSize().background(BrandGradient)) {
            ScreenHeader(title = "DELIVERY DETAIL", onBack = { viewModel.navigateBack() })

            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundGray),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                if (d == null && isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BiroBlue)
                    }
                } else if (d == null) {
                    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = TextGray, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Delivery not found", color = TextGray, fontSize = 15.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.navigateBack() }, colors = ButtonDefaults.buttonColors(containerColor = BiroBlue), shape = RoundedCornerShape(12.dp)) { Text("Back") }
                    }
                } else {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 120.dp)) {
                        // Status & Tracking
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = BiroBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) { Text(d.trackingNumber, color = BiroBlue, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) }
                            RiderStatusBadge(d.status)
                        }

                        Spacer(Modifier.height(20.dp))

                        // Status Timeline
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Delivery Progress", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(12.dp))
                                val steps = listOf("ASSIGNED" to "Assigned", "PICKED_UP" to "Picked Up", "OUT_FOR_DELIVERY" to "Out for Delivery", "DELIVERED" to "Delivered")
                                val currentIdx = steps.indexOfFirst { it.first == d.status }.coerceAtLeast(0)
                                steps.forEachIndexed { i, (key, label) ->
                                    val isDone = i <= currentIdx
                                    val isActive = i == currentIdx
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(28.dp).clip(CircleShape).background(if (isDone) SuccessGreen else Color(0xFFE2E8F0)), contentAlignment = Alignment.Center) {
                                            if (isDone) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Text(label, color = if (isActive) TextMain else if (isDone) SuccessGreen else TextGray, fontSize = 13.sp, fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium)
                                    }
                                    if (i < steps.lastIndex) {
                                        Spacer(Modifier.height(4.dp))
                                        Box(Modifier.width(1.dp).height(20.dp).padding(start = 13.5.dp).background(if (i < currentIdx) SuccessGreen else CardBorderGray))
                                        Spacer(Modifier.height(4.dp))
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Customer Info
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Customer Information", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(44.dp).clip(CircleShape).background(BiroBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(22.dp)) }
                                    Spacer(Modifier.width(12.dp))
                                    Column { Text(d.customerName, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold); Text(d.customerPhone, color = BiroBlue, fontSize = 12.sp) }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Route
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Route", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(10.dp).clip(CircleShape).background(BiroBlue)); Spacer(Modifier.width(10.dp)); Column { Text("Pickup", color = TextGray, fontSize = 10.sp); Text(d.pickupAddress, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) } }
                                Spacer(Modifier.height(4.dp))
                                Box(Modifier.width(1.dp).height(16.dp).padding(start = 4.5.dp).background(CardBorderGray))
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFF97316))); Spacer(Modifier.width(10.dp)); Column { Text("Drop-off", color = TextGray, fontSize = 10.sp); Text(d.deliveryAddress, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) } }
                                Spacer(Modifier.height(8.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Surface(color = Color(0xFFEEF2F6), shape = RoundedCornerShape(8.dp)) { Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Phone, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(12.dp)); Spacer(Modifier.width(4.dp)); Text("Call", color = BiroBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold) } }
                                    Surface(color = Color(0xFFEEF2F6), shape = RoundedCornerShape(8.dp)) { Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.NearMe, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(12.dp)); Spacer(Modifier.width(4.dp)); Text("Navigate", color = BiroBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold) } }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Package Info
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Package Details", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(12.dp))
                                Row(Modifier.fillMaxWidth()) { Text("Item", color = TextGray, fontSize = 12.sp, modifier = Modifier.weight(1f)); Text(d.itemName, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                                Spacer(Modifier.height(6.dp))
                                Row(Modifier.fillMaxWidth()) { Text("Weight", color = TextGray, fontSize = 12.sp, modifier = Modifier.weight(1f)); Text("${d.itemWeight} KG", color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                                Spacer(Modifier.height(6.dp))
                                Row(Modifier.fillMaxWidth()) { Text("Distance", color = TextGray, fontSize = 12.sp, modifier = Modifier.weight(1f)); Text(d.distance, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                                Spacer(Modifier.height(6.dp))
                                Row(Modifier.fillMaxWidth()) { Text("Schedule", color = TextGray, fontSize = 12.sp, modifier = Modifier.weight(1f)); Text(d.scheduledAt, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // OTP & Photo Proof
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Verification", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(12.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Lock, contentDescription = null, tint = TextGray, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(6.dp)); Text("OTP Code:", color = TextGray, fontSize = 12.sp); Spacer(Modifier.width(8.dp)); Text(d.otpCode, color = BiroBlue, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp) }
                                    if (d.otpVerified) Surface(color = SuccessGreen.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) { Text("Verified", color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)) }
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Action buttons based on status
                        when (d.status) {
                            "ASSIGNED" -> {
                                Button(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.updateDeliveryStatus(d.trackingNumber, "PICKED_UP"); viewModel.navigateBack() }, colors = ButtonDefaults.buttonColors(containerColor = BiroBlue), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(52.dp)) { Icon(Icons.Default.Inventory, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Mark as Picked Up", fontWeight = FontWeight.Bold) }
                            }
                            "PICKED_UP" -> {
                                Button(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.updateDeliveryStatus(d.trackingNumber, "OUT_FOR_DELIVERY") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(52.dp)) { Icon(Icons.Default.NearMe, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Mark as Out for Delivery", fontWeight = FontWeight.Bold) }
                            }
                            "OUT_FOR_DELIVERY" -> {
                                Button(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); showOtpSheet = true }, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(52.dp)) { Icon(Icons.Default.Verified, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Verify & Complete Delivery", fontWeight = FontWeight.Bold) }
                            }
                            "DELIVERED" -> {
                                Surface(color = SuccessGreen.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                                    Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Delivery Completed", color = SuccessGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                if (d.photoProofUri == null) {
                                    OutlinedButton(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.uploadPhoto(d.trackingNumber, "photo_${d.trackingNumber}.jpg") }, border = BorderStroke(1.dp, BiroBlue), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) { Icon(Icons.Default.CameraAlt, contentDescription = null, tint = BiroBlue); Spacer(Modifier.width(8.dp)); Text("Add Delivery Photo", color = BiroBlue, fontWeight = FontWeight.Bold) }
                                } else {
                                    Surface(color = SuccessGreen.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, SuccessGreen), modifier = Modifier.fillMaxWidth()) {
                                        Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Image, contentDescription = null, tint = SuccessGreen); Spacer(Modifier.width(8.dp)); Text("Photo proof attached", color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.navigateBack() }, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Back to Dashboard", color = TextGray, fontWeight = FontWeight.Bold) }

                        otpResult?.let {
                            Spacer(Modifier.height(12.dp))
                            Surface(color = if (it == "verified") SuccessGreen.copy(alpha = 0.1f) else DangerRed.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
                                Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(if (it == "verified") Icons.Default.CheckCircle else Icons.Default.Cancel, contentDescription = null, tint = if (it == "verified") SuccessGreen else DangerRed)
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (it == "verified") "OTP Verified Successfully!" else "Incorrect OTP. Try again.", color = if (it == "verified") SuccessGreen else DangerRed, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showOtpSheet) {
        OtpVerificationSheet(
            otpCode = d?.otpCode ?: "",
            onVerify = { inputOtp ->
                if (d != null && viewModel.verifyOtp(d.trackingNumber, inputOtp)) {
                    otpResult = "verified"
                    showOtpSheet = false
                } else {
                    otpResult = "failed"
                }
            },
            onDismiss = { showOtpSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OtpVerificationSheet(otpCode: String, onVerify: (String) -> Unit, onDismiss: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var inputOtp by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = { scope.launch { sheetState.hide(); onDismiss() } }, sheetState = sheetState, containerColor = Color.White, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(64.dp).background(BiroBlue.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Lock, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(32.dp)) }
            Spacer(Modifier.height(16.dp))
            Text("Verify Delivery", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
            Text("Enter the 4-digit OTP from the customer", color = TextGray, fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(otpCode, color = BiroBlue, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 8.sp)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = inputOtp, onValueChange = { if (it.length <= 4) inputOtp = it }, placeholder = { Text("Enter OTP") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedBorderColor = BiroBlue, unfocusedBorderColor = CardBorderGray), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(20.dp))
            Button(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onVerify(inputOtp) }, colors = ButtonDefaults.buttonColors(containerColor = BiroBlue), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(52.dp), enabled = inputOtp.length == 4) { Text("Verify OTP", fontWeight = FontWeight.Bold, color = Color.White) }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); scope.launch { sheetState.hide(); onDismiss() } }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp), border = BorderStroke(1.dp, CardBorderGray)) { Text("Cancel", color = TextGray, fontWeight = FontWeight.Bold) }
        }
    }
}
