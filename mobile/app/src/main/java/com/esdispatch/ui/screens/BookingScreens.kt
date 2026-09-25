package com.esdispatch.ui.screens

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esdispatch.ui.components.ConfettiEffect
import com.esdispatch.util.SoundManager
import com.esdispatch.ui.components.ScreenHeader
import com.esdispatch.ui.components.RoundedSheet
import com.esdispatch.ui.components.WalletCheckoutSheet
import com.esdispatch.ui.components.QuiltedBackground
import com.esdispatch.ui.theme.*
import com.esdispatch.viewmodel.DeliveryViewModel
import com.esdispatch.viewmodel.PendingQuote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

/**
 * Canonical Service Mapping:
 * All parcel dispatches flow through the 4 canonical booking screens:
 * 1. ExpressBookingScreen
 * 2. EconomyBookingScreen
 * 3. BatchBookingScreen
 * 4. MultiBookingScreen
 */
@Composable
fun SendParcelScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    ExpressBookingScreen(viewModel = viewModel, onNavigate = onNavigate)
}

@Composable
fun BookingSelectionScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    ServiceSelectionScreen(viewModel = viewModel, onNavigate = onNavigate)
}

@Composable
fun PaymentSuccessScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val scale = remember { Animatable(0f) }
    var triggerConfetti by remember { mutableStateOf(false) }
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val isDark = !isLight
    val context = LocalContext.current
    val parcels by viewModel.parcels.collectAsState()
    val selectedParcel by viewModel.selectedParcel.collectAsState()
    val draft by viewModel.parcelDraft.collectAsState()
    val latestParcel = selectedParcel ?: parcels.firstOrNull()
    val currentUserName by viewModel.userName.collectAsState()
    val currentUserPhone by viewModel.userPhone.collectAsState()

    val displayTrackingId = latestParcel?.id?.ifBlank { null } ?: "ENG-DISPATCH"
    val displayPrice = latestParcel?.price ?: if (draft.price > 0) draft.price else 0.0
    val displaySenderName = latestParcel?.senderName?.ifBlank { null } ?: currentUserName.ifBlank { draft.senderName.ifBlank { "Customer" } }
    val displaySenderPhone = latestParcel?.senderPhone?.ifBlank { null } ?: currentUserPhone.ifBlank { draft.senderPhone.ifBlank { "N/A" } }
    val displayReceiverName = latestParcel?.receiverName?.ifBlank { null } ?: draft.receiverName.ifBlank { "Valued Customer" }
    val displayReceiverPhone = latestParcel?.receiverPhone?.ifBlank { null } ?: draft.receiverPhone.ifBlank { "N/A" }
    val displayPickup = latestParcel?.pickupAddress?.ifBlank { null } ?: draft.pickupAddress.ifBlank { "Scheduled Pickup" }
    val displayDelivery = latestParcel?.deliveryAddress?.ifBlank { null } ?: draft.deliveryAddress.ifBlank { "Scheduled Delivery Destination" }
    val displayItem = latestParcel?.itemName?.ifBlank { null } ?: "Premium Package Dispatch"

    LaunchedEffect(Unit) {
        SoundManager.playCelebrationFanfare()
        triggerConfetti = true
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    androidx.activity.compose.BackHandler {
        onNavigate("Dashboard")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryBlack)
    ) {
        ConfettiEffect(
            trigger = triggerConfetti,
            onFinished = { triggerConfetti = false }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HeaderBgColor)
        ) {
            ScreenHeader(
                title = "Payment Success",
                onBack = { onNavigate("Dashboard") }
            )

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = if (isDark) BackgroundDark else BackgroundLight
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Celebration circle card
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(scale.value),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .background(Gold.copy(alpha = 0.15f), CircleShape)
                        )
                        Surface(
                            modifier = Modifier.size(110.dp),
                            shape = CircleShape,
                            color = Charcoal,
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f)),
                            shadowElevation = 0.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Success",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Payment Successful", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = AppTextColor)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("PREMIUM DISPATCH CONFIRMED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 1.5.sp)
                    Spacer(modifier = Modifier.height(24.dp))

                    // Scalloped / Premium Ticket Receipt Card
                    Surface(
                        color = Charcoal,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, if (isDark) BorderDark else Slate),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("ESDISPATCH", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Gold, fontFamily = SpaceGrotesk)
                                    Text("PREMIUM LOGISTICS & DISPATCH", fontSize = 8.sp, fontWeight = FontWeight.Medium, color = TextGray, letterSpacing = 1.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(Gold.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("PAID", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Gold)
                                }
                            }

                            // Thin Divider line
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderDark))

                            // Key Fields: Tracking ID & Amount Paid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("TRACKING ID", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .background(if (isDark) BackgroundDark else BackgroundLight, RoundedCornerShape(8.dp))
                                            .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = displayTrackingId,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = AppTextColor,
                                            fontFamily = SpaceGrotesk
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("AMOUNT PAID", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                    Text(
                                        text = "₦${String.format("%,.2f", displayPrice)}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Gold,
                                        fontFamily = SpaceGrotesk
                                    )
                                }
                            }

                            // Thin Divider line
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderDark))

                            // Sender & Receiver Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("SENDER", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                    Text(displaySenderName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                                    Text(displaySenderPhone, fontSize = 10.sp, color = TextGray)
                                }
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text("RECIPIENT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                    Text(displayReceiverName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                                    Text(displayReceiverPhone, fontSize = 10.sp, color = TextGray)
                                }
                            }

                            // Pickup & Delivery Address
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("PICKUP ADDRESS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                Text(displayPickup, fontSize = 11.sp, color = TextGray)
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("DELIVERY ADDRESS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                Text(displayDelivery, fontSize = 11.sp, color = TextGray)
                            }

                            // Item Description
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("ITEM DESCRIPTION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                Text(displayItem, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppTextColor)
                            }

                            // Thin Divider line
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderDark))

                            // Footer Note
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Saved to your permanent Booking History",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Streamlined Navigation Flow Actions
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Primary Highlight Action: Track Shipment (Gold on Obsidian background / Black text on Gold)
                        Button(
                            onClick = { onNavigate("ActiveTracking") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Gold,
                                contentColor = Obsidian
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = Obsidian, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Track Live Shipment", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        // 2. Secondary Row Action: Share Details & Back to Home
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Share Button
                            Button(
                                onClick = {
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_TEXT, "Track my ESDispatch shipment! ID: ${latestParcel?.id ?: "ENG-824-LGS"}")
                                    }
                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Shipment Details"))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Charcoal else GoldenWhite),
                                border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share details", tint = Gold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gold)
                                }
                            }

                            // Back Home Button
                            OutlinedButton(
                                onClick = { onNavigate("Dashboard") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.5.dp, Color(0xFF333333)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold)
                            ) {
                                Text("Back to Home", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ServiceSelectionScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val draft by viewModel.parcelDraft.collectAsState()
    var selectedService by remember { mutableStateOf("Express") }

    androidx.activity.compose.BackHandler {
        onNavigate("Dashboard")
    }

    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val scrollState = rememberScrollState()

    val accentBorderColor = if (isLight) Obsidian else Gold
    val accentIconColor = if (isLight) Obsidian else Gold
    val accentTextColor = if (isLight) Obsidian else Gold

    val isDark = !isLight

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
                title = "Select Service",
                onBack = { onNavigate("Dashboard") }
            )

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = if (isDark) BackgroundDark else BackgroundLight
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                        .padding(bottom = 120.dp)
                ) {
                Text(
                    text = "Choose Your Service",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentTextColor
                )
                Text(
                    text = "Select the option that best fits your schedule and parcel type.",
                    fontSize = 14.sp,
                    color = TextGray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                val services = listOf(
                    ServiceOption(
                        id = "Express",
                        name = "Express Delivery",
                        desc = "Immediate dispatch, fastest delivery in 30-45 mins.",
                        price = "",
                        badge = "Fastest",
                        badgeColor = Gold,
                        icon = Icons.Filled.Bolt
                    ),
                    ServiceOption(
                        id = "Economy",
                        name = "Economy Delivery",
                        desc = "High efficiency, standard delivery in 2-3 hours.",
                        price = "",
                        badge = "Best Price",
                        badgeColor = Color(0xFF4CAF50),
                        icon = Icons.Filled.LocalShipping
                    ),
                    ServiceOption(
                        id = "Batch",
                        name = "Batch Delivery",
                        desc = "Optimized multi-stop bulk package delivery.",
                        price = "",
                        badge = "Eco Saver",
                        badgeColor = Color(0xFF00BCD4),
                        icon = Icons.Filled.Layers
                    ),
                    ServiceOption(
                        id = "Multi",
                        name = "Multi-Stop Delivery",
                        desc = "Deliver to up to 5 destinations in a single run.",
                        price = "",
                        badge = "Business",
                        badgeColor = Gold,
                        icon = Icons.Filled.Share
                    )
                )

                services.forEach { service ->
                    val isSelected = selectedService == service.id
                    val borderStroke = if (isSelected) {
                        BorderStroke(2.dp, accentBorderColor)
                    } else {
                        if (isLight) BorderStroke(1.dp, BorderLight) else BorderStroke(1.dp, Color(0xFF333333))
                    }
                    val containerColor = if (isLight) {
                        if (isSelected) Gold.copy(alpha = 0.08f) else GoldenWhiteLight
                    } else {
                        if (isSelected) Charcoal else Obsidian
                    }

                    Surface(
                        onClick = { selectedService = service.id },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = containerColor,
                        border = borderStroke,
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (isSelected) Gold.copy(alpha = 0.15f) else (if (isLight) GoldenWhiteLight else Obsidian)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = service.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) accentIconColor else TextGray,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = service.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AppTextColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = service.badgeColor.copy(alpha = 0.15f),
                                    ) {
                                        Text(
                                            text = service.badge,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isLight) Obsidian else service.badgeColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = service.desc,
                                    fontSize = 12.sp,
                                    color = TextGray,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                if (service.price.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = service.price,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = accentTextColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Charcoal,
            tonalElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.finalizeDraftPrice(selectedService)
                        when (selectedService) {
                            "Express" -> onNavigate("ExpressBooking")
                            "Economy" -> onNavigate("EconomyBooking")
                            "Batch" -> onNavigate("BatchBooking")
                            "Multi" -> onNavigate("MultiBooking")
                            else -> onNavigate("SendParcelDetails")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Obsidian),
                    border = BorderStroke(1.2.dp, Gold)
                ) {
                    Text(
                        text = "Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Gold
                    )
                }
            }
        }
    }
}

private data class ServiceOption(
    val id: String,
    val name: String,
    val desc: String,
    val price: String,
    val badge: String,
    val badgeColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
