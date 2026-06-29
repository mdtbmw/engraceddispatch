package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import java.util.Calendar
import androidx.compose.ui.text.style.TextOverflow
import com.example.data.Delivery
import com.example.ui.DeliveryViewModel
import com.example.ui.navigation.AppView
import com.example.ui.theme.*
import com.example.ui.components.*
import com.example.ui.components.DeliveryMapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DeliveryViewModel) {
    val deliveries by viewModel.allDeliveries.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val listState = rememberLazyListState()
    var isOnline by remember { mutableStateOf(true) }

    var overscrollProgress by remember { mutableFloatStateOf(0f) }
    var isRefreshing by remember { mutableStateOf(false) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.y > 0f && overscrollProgress >= 0f) {
                    val pull = (overscrollProgress + available.y / 400f).coerceIn(0f, 1.2f)
                    overscrollProgress = pull
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (overscrollProgress > 0f) {
                    val consumedY = available.y.coerceIn(-overscrollProgress * 200f, 0f)
                    overscrollProgress = (overscrollProgress + consumedY / 200f).coerceAtLeast(0f)
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (overscrollProgress >= 1f) {
                    isRefreshing = true
                    viewModel.refreshAllData {
                        isRefreshing = false
                    }
                }
                overscrollProgress = 0f
                return Velocity.Zero
            }
        }
    }

    val density = LocalDensity.current.density

    val headerHeight by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 115.dp
            else { (220f - (listState.firstVisibleItemScrollOffset / density).coerceIn(0f, 105f)).dp }
        }
    }
    val collapseProgress by remember {
        derivedStateOf { ((220f - headerHeight.value) / 105f).coerceIn(0f, 1f) }
    }

    val activeJobs = deliveries.filter { 
        (it.riderName == currentUser.fullName || it.riderName.isEmpty()) && 
        it.status in listOf("PENDING", "ASSIGNED", "PICKED_UP", "OUT_FOR_DELIVERY") 
    }
    
    val myActiveJob = deliveries.firstOrNull { 
        it.riderName == currentUser.fullName && 
        it.status in listOf("ASSIGNED", "PICKED_UP", "OUT_FOR_DELIVERY") 
    }

    val completedCount = deliveries.count { it.status == "DELIVERED" && it.riderName == currentUser.fullName }
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when { hour < 12 -> "Good morning"; hour < 17 -> "Good afternoon"; else -> "Good evening" }

    val headerCorner = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
    val pullExtra = (overscrollProgress * 30f).dp

    val displayName = currentUser.fullName.split(" ").firstOrNull()?.uppercase() ?: "RIDER"

    // OTP Modal
    var showOtpDialog by remember { mutableStateOf(false) }
    var otpInputValue by remember { mutableStateOf("") }
    var otpErrorText by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize().background(BackgroundGray)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection),
            contentPadding = PaddingValues(top = 236.dp, bottom = 120.dp)
        ) {
            // Online/Offline status card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, CardBorderGray),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                ) {
                    Row(
                        Modifier.padding(20.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Duty Status", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            Text(
                                if (isOnline) "You are online and receiving jobs" else "You are offline",
                                color = TextMuted, fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = isOnline,
                            onCheckedChange = { isOnline = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SuccessGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.LightGray
                            )
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Active Accepted Job
            item {
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    Text("Your Active Dispatch", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(10.dp))
                    if (myActiveJob == null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, CardBorderGray),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(40.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("No Active Deliveries", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Select and claim an available job pool order below", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, CardBorderGray),
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).clickable {
                                viewModel.setTrackingDelivery(myActiveJob)
                                viewModel.navigateTo(AppView.ActiveTracking(myActiveJob.trackingNumber))
                            }
                        ) {
                            Column(Modifier.padding(20.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Surface(color = BiroBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                                        Text(myActiveJob.trackingNumber, color = BiroBlue, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }
                                    Text(myActiveJob.deliveryType, color = BiroBlue, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                }
                                Spacer(Modifier.height(14.dp))
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(Modifier.size(8.dp).clip(CircleShape).background(BiroBlue))
                                            Spacer(Modifier.width(10.dp))
                                            Text(myActiveJob.pickupAddress.split(",").first(), color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF97316)))
                                            Spacer(Modifier.width(10.dp))
                                            Text(myActiveJob.deliveryAddress.split(",").first(), color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                    Box(Modifier.size(48.dp).clip(CircleShape).background(BiroBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(24.dp))
                                    }
                                }
                                
                                Spacer(Modifier.height(16.dp))
                                HorizontalDivider(color = CardBorderGray)
                                Spacer(Modifier.height(16.dp))
                                
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedGradientButton(
                                        text = "Navigate",
                                        icon = Icons.Default.Navigation,
                                        onClick = { viewModel.navigateTo(AppView.MapNavigation(myActiveJob.trackingNumber)) },
                                        modifier = Modifier.weight(1f).height(44.dp)
                                    )

                                    when (myActiveJob.status) {
                                        "ASSIGNED" -> {
                                            PremiumGradientButton(
                                                text = "Pick Up",
                                                onClick = { viewModel.updateDeliveryStatus(myActiveJob.trackingNumber, "PICKED_UP") },
                                                modifier = Modifier.weight(1f).height(44.dp)
                                            )
                                        }
                                        "PICKED_UP" -> {
                                            PremiumGradientButton(
                                                text = "Transit",
                                                onClick = { viewModel.updateDeliveryStatus(myActiveJob.trackingNumber, "OUT_FOR_DELIVERY") },
                                                modifier = Modifier.weight(1f).height(44.dp)
                                            )
                                        }
                                        "OUT_FOR_DELIVERY" -> {
                                            PremiumGradientButton(
                                                text = "Verify OTP",
                                                onClick = {
                                                    otpInputValue = ""
                                                    otpErrorText = ""
                                                    showOtpDialog = true
                                                },
                                                modifier = Modifier.weight(1f).height(44.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Rider stats grid
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val activeJobCount = if (myActiveJob != null) 1 else 0
                    StatTile("$activeJobCount", "My active", Icons.Default.DirectionsRun, Color(0xFFEFF6FF), Color(0xFF2563EB)) {}
                    StatTile("$completedCount", "My Done", Icons.Default.CheckCircle, Color(0xFFECFDF5), Color(0xFF10B981)) {}
                    StatTile("98%", "On-Time", Icons.Default.Schedule, Color(0xFFEEEDFF), BiroBlue) {}
                    StatTile("4.9", "Rating", Icons.Default.Star, Color(0xFFFFFAEC), Color(0xFFF59E0B)) {}
                }
                Spacer(Modifier.height(20.dp))
            }

            // Mini-map showing nearby jobs
            item {
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    Text("Nearby Area", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(10.dp))
                    DeliveryMapView(
                        pickupLat = 6.5244,
                        pickupLng = 3.3792,
                        deliveryLat = 6.4643,
                        deliveryLng = 3.3942,
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        interactive = false
                    )
                }
                Spacer(Modifier.height(20.dp))
            }

            // Available Job pool
            item {
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    Text("Available Dispatch Pool", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(10.dp))
                    val poolJobs = deliveries.filter { it.status == "PENDING" && it.riderName.isEmpty() }
                    if (poolJobs.isEmpty() || !isOnline) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, CardBorderGray),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = TextMuted, modifier = Modifier.size(32.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    if (!isOnline) "You are Offline" else "No Available Jobs",
                                    color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (!isOnline) "Toggle duty switch online to view pool" else "Waiting for new customer bookings...",
                                    color = TextMuted, fontSize = 11.sp
                                )
                            }
                        }
                    } else {
                        poolJobs.forEach { job ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, CardBorderGray),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(job.trackingNumber, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                                        Surface(color = BiroBlue.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp)) {
                                            Text(job.deliveryType, color = BiroBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(job.itemName, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Spacer(Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(6.dp).clip(CircleShape).background(BiroBlue))
                                        Spacer(Modifier.width(8.dp))
                                        Text("From: ${job.pickupAddress.split(",").first()}", color = TextGray, fontSize = 11.sp)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFF97316)))
                                        Spacer(Modifier.width(8.dp))
                                        Text("To: ${job.deliveryAddress.split(",").first()}", color = TextGray, fontSize = 11.sp)
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    
                                    // Accept job action
                                    PremiumGradientButton(
                                        text = "Claim Job - \u20A6${job.totalAmount.toInt()}",
                                        onClick = {
                                            // Assign order locally
                                            viewModel.assignRiderToDelivery(job.trackingNumber, currentUser.fullName)
                                        },
                                        modifier = Modifier.fillMaxWidth().height(38.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Header Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight + pullExtra)
                .shadow(8.dp, headerCorner, clip = false)
                .background(BrandGradient, headerCorner)
        ) {
            if (collapseProgress > 0.6f) CollapsedHeader(greeting, displayName, viewModel)
            else ExpandedHeader(greeting, displayName, completedCount, collapseProgress, viewModel)
        }

        if (overscrollProgress > 0.03f && !isRefreshing) {
            Box(Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = headerHeight + pullExtra + 8.dp), contentAlignment = Alignment.Center) {
                Surface(shape = RoundedCornerShape(20.dp), color = BiroBlue, shadowElevation = 4.dp) {
                    Text(
                        if (overscrollProgress >= 1f) "\u2193 Release to refresh" else "\u2193 Pull to refresh",
                        color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (isRefreshing) {
            Box(Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = headerHeight + pullExtra + 8.dp), contentAlignment = Alignment.Center) {
                Surface(shape = RoundedCornerShape(20.dp), color = BiroBlue, shadowElevation = 4.dp) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp)); Text("Refreshing...", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // OTP Verify Dialog
    if (showOtpDialog && myActiveJob != null) {
        AlertDialog(
            onDismissRequest = { showOtpDialog = false },
            title = { Text("Verify Handover OTP", fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    Text("Ask the customer for their 4-digit security code.", color = TextMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Demo Assist - Code is: ${myActiveJob.otpCode}", color = BiroBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = otpInputValue,
                        onValueChange = { otpInputValue = it },
                        placeholder = { Text("Enter 4-digit code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (otpErrorText.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(otpErrorText, color = Color.Red, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (otpInputValue == myActiveJob.otpCode) {
                            viewModel.verifyDeliveryOtp(myActiveJob.trackingNumber, otpInputValue) { success ->
                                if (success) {
                                    showOtpDialog = false
                                } else {
                                    otpErrorText = "Network error. Please try again."
                                }
                            }
                        } else {
                            otpErrorText = "Invalid 4-digit verification code"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BiroBlue)
                ) {
                    Text("Verify & Complete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOtpDialog = false }) {
                    Text("Cancel", color = TextMain)
                }
            }
        )
    }
}

@Composable
private fun CollapsedHeader(greeting: String, displayName: String, viewModel: DeliveryViewModel) {
    val user by viewModel.currentUser.collectAsState()
    Row(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = user.photoUrl, contentDescription = "Profile", modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)).clickable { viewModel.navigateTo(AppView.Profile) })
            Spacer(Modifier.width(10.dp))
            Column { Text(greeting, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp); Text(displayName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold) }
        }
        Box(Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)).clickable { viewModel.navigateTo(AppView.Notifications) }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)) }
    }
}

@Composable
private fun ExpandedHeader(greeting: String, displayName: String, completedCount: Int, progress: Float, viewModel: DeliveryViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val alpha = (1f - progress * 1.5f).coerceIn(0f, 1f)
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    AsyncImage(model = user.photoUrl, contentDescription = "Profile", modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)).clickable { viewModel.navigateTo(AppView.Profile) })
                    Spacer(Modifier.width(12.dp))
                    Column { Text(greeting, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold); Text(displayName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold) }
                }
                Box(Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f)).clickable { viewModel.navigateTo(AppView.Notifications) }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.height(10.dp))
            Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
                Text("ENGRACED VERIFIED RIDER", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
            }
        }
        Row(Modifier.fillMaxWidth().graphicsLayer { this.alpha = alpha }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Column {
                Text("TODAY'S DELIVERIES", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("$completedCount", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.16f)), shape = RoundedCornerShape(14.dp), modifier = Modifier.clickable { viewModel.navigateTo(AppView.OrderLogs) }) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("View History", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RowScope.StatTile(value: String, label: String, icon: ImageVector, bg: Color, iconColor: Color, onClick: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(32.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp)) }
            Spacer(Modifier.height(6.dp)); Text(value, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(2.dp)); Text(label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, lineHeight = 12.sp)
        }
    }
}
