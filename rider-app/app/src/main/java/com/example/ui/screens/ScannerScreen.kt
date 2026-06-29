package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.components.PremiumGradientButton
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DeliveryViewModel
import com.example.ui.navigation.AppView
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(viewModel: DeliveryViewModel) {
    val deliveries by viewModel.allDeliveries.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    // Laser scanning animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserYOffset by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserY"
    )

    var scanningState by remember { mutableStateOf("SCANNING") } // SCANNING | SUCCESS
    var scannedTrackingNumber by remember { mutableStateOf("") }

    val scannablePool = deliveries.filter { 
        it.status == "PENDING" && it.riderName.isEmpty() 
    }

    Box(Modifier.fillMaxSize().background(LuxuryBlack)) {
        if (scanningState == "SCANNING") {
            // Viewfinder layout
            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    Modifier.fillMaxWidth().height(64.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)).clickable { viewModel.navigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Text("PARCEL SCANNER", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    Box(Modifier.size(40.dp))
                }

                Spacer(Modifier.height(40.dp))

                // Scanner viewport box
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .border(2.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Frame corners (neon green style)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                val borderLen = 30.dp.toPx()
                                val strokeW = 4.dp.toPx()
                                val neon = SuccessGreen
                                // Top-Left
                                drawLine(neon, Offset(0f, 0f), Offset(borderLen, 0f), strokeW)
                                drawLine(neon, Offset(0f, 0f), Offset(0f, borderLen), strokeW)
                                // Top-Right
                                drawLine(neon, Offset(size.width - borderLen, 0f), Offset(size.width, 0f), strokeW)
                                drawLine(neon, Offset(size.width, 0f), Offset(size.width, borderLen), strokeW)
                                // Bottom-Left
                                drawLine(neon, Offset(0f, size.height), Offset(borderLen, size.height), strokeW)
                                drawLine(neon, Offset(0f, size.height - borderLen), Offset(0f, size.height), strokeW)
                                // Bottom-Right
                                drawLine(neon, Offset(size.width - borderLen, size.height), Offset(size.width, size.height), strokeW)
                                drawLine(neon, Offset(size.width, size.height - borderLen), Offset(size.width, size.height), strokeW)
                            }
                    )

                    // Pulse laser line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.015f)
                            .align(Alignment.TopCenter)
                            .graphicsLayer {
                                this.translationY = this.size.height * laserYOffset
                            }
                            .background(SuccessGreen)
                    )

                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.size(100.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))
                Text("Align barcode / label inside the frame", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Simulates device camera package scanning", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))

                Spacer(Modifier.height(40.dp))

                // Available packages near
                Text("Simulated Parcels Nearby", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Left)
                Spacer(Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (scannablePool.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("No pending parcel pool packages found. Please simulate bookings on the admin dashboard first.", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        items(scannablePool) { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    scannedTrackingNumber = item.trackingNumber
                                    scanningState = "SUCCESS"
                                    
                                    // Assign dispatch to rider
                                    viewModel.assignRiderToDelivery(item.trackingNumber, currentUser.fullName)
                                }
                            ) {
                                Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(item.trackingNumber, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                                        Text("${item.itemName} (${item.itemWeight}kg)", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Tap to Scan", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.width(6.dp))
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Success layout
            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(72.dp).clip(CircleShape).background(SuccessGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(40.dp))
                }
                Spacer(Modifier.height(20.dp))
                Text("SCAN SUCCESSFUL", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    "Parcel $scannedTrackingNumber has been successfully scanned and added to your active dispatch manifests.",
                    color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp,
                    textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp)
                )

                Spacer(Modifier.height(40.dp))

                PremiumGradientButton(
                    text = "View Route Timeline",
                    onClick = {
                        viewModel.navigateBack()
                        viewModel.navigateTo(AppView.ActiveTracking(scannedTrackingNumber))
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                )
            }
        }
    }
}
