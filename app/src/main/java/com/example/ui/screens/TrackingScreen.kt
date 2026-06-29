package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Delivery
import com.example.ui.DeliveryViewModel
import com.example.ui.navigation.AppView
import com.example.ui.theme.*
import com.example.ui.components.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveTrackingScreen(viewModel: DeliveryViewModel, trackingNumber: String) {
    val delivery by viewModel.currentTrackingDelivery.collectAsState()
    val context = LocalContext.current
    var showVerificationSheet by remember { mutableStateOf(false) }
    var verificationResult by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(trackingNumber) {
        if (delivery == null || delivery?.trackingNumber != trackingNumber) {
            viewModel.loadDeliveryByTracking(trackingNumber)
        }
    }

    Box(Modifier.fillMaxSize().background(LuxuryBlack)) {
        if (delivery == null) {
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Spacer(Modifier.height(24.dp))
                ShimmerBox(height = 100.dp, corners = 20.dp)
                ShimmerBox(height = 200.dp, corners = 20.dp)
                ShimmerBox(height = 160.dp, corners = 20.dp)
                ShimmerBox(height = 100.dp, corners = 20.dp)
                OutlinedGradientButton("Back to Home", onClick = { viewModel.navigateBack() })
            }
        } else {
            Column(Modifier.fillMaxSize().background(BrandGradient)) {
                ScreenHeader("TRACKING: ${delivery!!.trackingNumber}", onBack = { viewModel.navigateBack() })
                RoundedSheet {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        RiderInfoCard(delivery!!)
                        Spacer(Modifier.height(16.dp))

                        // Live map showing pickup ↔ delivery route
                        DeliveryMapView(
                            pickupLat = delivery!!.pickupLatitude ?: 6.5244,
                            pickupLng = delivery!!.pickupLongitude ?: 3.3792,
                            deliveryLat = delivery!!.deliveryLatitude ?: 6.4643,
                            deliveryLng = delivery!!.deliveryLongitude ?: 3.3942,
                            riderLat = 6.5044,
                            riderLng = 3.3692,
                            modifier = Modifier.fillMaxWidth().height(240.dp)
                        )
                        Spacer(Modifier.height(16.dp))

                        StatusTimelineCard(delivery!!)
                        Spacer(Modifier.height(16.dp))

                        DeliveryDetailsCard(delivery!!)
                        Spacer(Modifier.height(16.dp))

                        OtpVerificationCard(delivery!!, viewModel) { showVerificationSheet = true }
                        Spacer(Modifier.height(16.dp))

                        if (delivery!!.photoProofUri != null || delivery!!.otpVerified) {
                            PhotoProofCard(delivery!!)
                            Spacer(Modifier.height(16.dp))
                        }

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (delivery!!.status != "DELIVERED") {
                                PremiumGradientButton(
                                    text = "Advance Status",
                                    icon = Icons.Default.PlayArrow,
                                    onClick = { viewModel.advanceDeliveryStatus() },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (delivery!!.status == "DELIVERED" && !delivery!!.otpVerified) {
                                OutlinedGradientButton(
                                    text = "Verify & Complete",
                                    icon = Icons.Default.Check,
                                    onClick = { showVerificationSheet = true },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (delivery!!.status == "DELIVERED" && delivery!!.otpVerified) {
                                OutlinedGradientButton(
                                    text = "Back to Dashboard",
                                    icon = Icons.Default.Home,
                                    onClick = { viewModel.navigateToRoot(AppView.Dashboard) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    if (showVerificationSheet) {
        OtpVerificationSheet(
            currentOtp = delivery?.otpCode ?: "",
            onVerify = { inputOtp ->
                delivery?.let { d ->
                    viewModel.updateOtpInput(inputOtp)
                }
                val result = viewModel.verifyOtp()
                verificationResult = if (result) "OTP Verified Successfully!" else "Invalid OTP. Please try again."
                showVerificationSheet = false
            },
            onDismiss = { showVerificationSheet = false }
        )
    }

    verificationResult?.let { msg ->
        SuccessBottomSheet(message = msg) { verificationResult = null }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OtpVerificationSheet(currentOtp: String, onVerify: (String) -> Unit, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var otpInput by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val dismiss: () -> Unit = { scope.launch { sheetState.hide(); onDismiss() } }

    ModalBottomSheet(
        onDismissRequest = dismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Text("Verify Delivery OTP", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
            Spacer(Modifier.height(8.dp))
            Text("Enter the 4-digit code to confirm delivery", color = TextGray, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))

            Surface(color = BiroBlue.copy(alpha = 0.08f), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your OTP Code", color = TextGray, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(currentOtp, color = BiroBlue, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 8.sp)
                }
            }
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = otpInput,
                onValueChange = { if (it.length <= 4) { otpInput = it; error = null } },
                placeholder = { Text("Enter 4-digit OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                isError = error != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextMain, unfocusedTextColor = TextMain,
                    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                    focusedBorderColor = BiroBlue, unfocusedBorderColor = CardBorderGray,
                    cursorColor = BiroBlue
                ),
                modifier = Modifier.fillMaxWidth()
            )
            error?.let { Text(it, color = DangerRed, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp)) }

            Spacer(Modifier.height(24.dp))
            PremiumGradientButton(
                text = "Verify OTP",
                onClick = {
                    if (otpInput.length != 4) error = "Enter a valid 4-digit code"
                    else onVerify(otpInput)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = dismiss, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp), border = BorderStroke(1.dp, CardBorderGray)) {
                Text("Cancel", color = TextGray, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RiderInfoCard(delivery: Delivery) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CardBorderGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(CircleShape).background(BiroBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(delivery.riderName, color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Motorcycle: ${delivery.riderBikeNumber}", color = TextGray, fontSize = 12.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(String.format("%.1f", delivery.riderRating), color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = CardBorderGray)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("ESTIMATED ARRIVAL", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Text("${delivery.etaMinutes} Mins", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun StatusTimelineCard(delivery: Delivery) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CardBorderGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Delivery Progress", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(16.dp))
            TimelineStep("Order Placed", delivery.pickupAddress.split(",").first(), isCompleted = true, isActive = delivery.status == "PENDING")
            TimelineStep("Rider Dispatched", "Rider assigned to pickup", isCompleted = delivery.status in listOf("ASSIGNED", "PICKED_UP", "OUT_FOR_DELIVERY", "DELIVERED"), isActive = delivery.status == "ASSIGNED")
            TimelineStep("Package Picked Up", "In transit to destination", isCompleted = delivery.status in listOf("PICKED_UP", "OUT_FOR_DELIVERY", "DELIVERED"), isActive = delivery.status == "PICKED_UP")
            TimelineStep("Out for Delivery", "Approaching destination", isCompleted = delivery.status in listOf("OUT_FOR_DELIVERY", "DELIVERED"), isActive = delivery.status == "OUT_FOR_DELIVERY")
            TimelineStep("Delivered", delivery.deliveryAddress.split(",").first(), isCompleted = delivery.status == "DELIVERED", isActive = delivery.status == "DELIVERED", isLast = true)
        }
    }
}

@Composable
private fun TimelineStep(title: String, subtitle: String, isCompleted: Boolean, isActive: Boolean, isLast: Boolean = false) {
    Row(Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
            val color = if (isCompleted) SuccessGreen else if (isActive) BiroBlue else Color.LightGray
            Box(Modifier.size(20.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
                if (isCompleted) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                else if (isActive) Box(Modifier.size(8.dp).clip(CircleShape).background(Color.White))
            }
            if (!isLast) Box(Modifier.width(2.dp).height(40.dp).background(if (isCompleted) SuccessGreen else Color(0xFFE2E8F0)))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.padding(bottom = if (isLast) 0.dp else 16.dp)) {
            Text(title, color = if (isActive || isCompleted) TextMain else TextGray, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextGray.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun DeliveryDetailsCard(delivery: Delivery) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CardBorderGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Delivery Details", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(BiroBlue))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Pickup", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Text(delivery.pickupAddress, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF97316)))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Destination", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Text(delivery.deliveryAddress, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = CardBorderGray)
            Spacer(Modifier.height(12.dp))
            InfoRow("Item", "${delivery.itemName} (${delivery.itemWeight}kg)")
            InfoRow("Type", delivery.deliveryType)
            InfoRow("Amount", "\u20A6${delivery.totalAmount.toInt()}", valueColor = BiroBlue)
            InfoRow("Schedule", delivery.scheduledAt)
        }
    }
}

@Composable
private fun OtpVerificationCard(delivery: Delivery, viewModel: DeliveryViewModel, onOpenSheet: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CardBorderGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Delivery Verification", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Your OTP Code", color = TextGray, fontSize = 11.sp)
                    Text(delivery.otpCode, color = BiroBlue, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
                }
                StatusBadge(if (delivery.otpVerified) "VERIFIED" else "PENDING")
            }
            if (!delivery.otpVerified) {
                Spacer(Modifier.height(16.dp))
                PremiumGradientButton(
                    text = "Verify Delivery",
                    icon = Icons.Default.Verified,
                    onClick = onOpenSheet,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                )
            }
        }
    }
}

@Composable
private fun PhotoProofCard(delivery: Delivery) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CardBorderGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Delivery Proof", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(12.dp))
            if (delivery.photoProofUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(delivery.photoProofUri).crossfade(true).build(),
                    contentDescription = "Photo Proof",
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp))
                )
                Spacer(Modifier.height(8.dp))
            }
            if (delivery.otpVerified) {
                Text("Verified handover recorded successfully.", color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
