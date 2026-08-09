package com.esdispatch.ui.screens.dashboard

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.esdispatch.data.ParcelStatus
import com.esdispatch.ui.components.BottomNav
import com.esdispatch.ui.components.ConfettiEffect
import com.esdispatch.ui.screens.AnimatedParcelIllustration
import com.esdispatch.ui.screens.DashboardActionBtn
import com.esdispatch.ui.screens.ParcelCard
import com.esdispatch.ui.screens.SwipeToArchiveBox
import com.esdispatch.ui.theme.*
import com.esdispatch.viewmodel.DeliveryViewModel

@Composable
fun V2DashboardScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val activeViewMode by viewModel.activeViewMode.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val isDark by viewModel.darkModeEnabled.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val photoUrl by viewModel.photoUrl.collectAsState()
    val loyaltyPoints by viewModel.loyaltyPoints.collectAsState()
    val deliveryCount by viewModel.deliveryCount.collectAsState()
    val referralCode by viewModel.referralCode.collectAsState()
    val parcels by viewModel.parcels.collectAsState()
    val archivedParcelIds by viewModel.archivedParcelIds.collectAsState()
    val promotions by viewModel.promotions.collectAsState()
    val sections by viewModel.dashboardSectionsEnabled.collectAsState()
    val dashboardVariant by viewModel.dashboardVariant.collectAsState()

    val firstName = remember(userName) { userName.trim().split(" ").firstOrNull() ?: userName }

    val unarchivedParcels = remember(parcels, archivedParcelIds) {
        parcels.filter { it.id !in archivedParcelIds }
    }
    var selectedFilter by remember { mutableStateOf("In Transit") }
    val filteredParcels = remember(unarchivedParcels, selectedFilter) {
        when (selectedFilter) {
            "In Transit" -> unarchivedParcels.filter { it.status == ParcelStatus.TRANSIT || it.status == ParcelStatus.OUT_FOR_DELIVERY }
            else -> unarchivedParcels.filter { it.status != ParcelStatus.DELIVERED && it.status != ParcelStatus.CANCELLED }
        }
    }
    val completedParcels = remember(parcels, archivedParcelIds) {
        parcels.filter { it.status == ParcelStatus.DELIVERED && it.id !in archivedParcelIds }
    }
    val activeCount = remember(unarchivedParcels) {
        unarchivedParcels.count { it.status == ParcelStatus.TRANSIT || it.status == ParcelStatus.OUT_FOR_DELIVERY }
    }

    var triggerConfetti by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        containerColor = LuxuryBlack,
        bottomBar = { BottomNav(currentScreen = "Dashboard", onNavigate = onNavigate, activeViewMode = activeViewMode, userRole = userRole) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            ConfettiEffect(trigger = triggerConfetti, onFinished = { triggerConfetti = false })

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 140.dp)
            ) {
                item {
                    V2DashboardHeader(
                        firstName = firstName,
                        photoUrl = photoUrl,
                        loyaltyPoints = loyaltyPoints,
                        deliveryCount = deliveryCount,
                        isDark = isDark,
                        variant = dashboardVariant,
                        onVariantChange = { viewModel.setDashboardVariant(it) },
                        onProfile = { onNavigate("Profile") },
                        onNotifications = { onNavigate("Notifications") }
                    )
                }

                if (sections["promo_banner"] != false) {
                    item {
                        Spacer(modifier = Modifier.height(22.dp))
                        HeroCarousel()
                    }
                }

                if (sections["quick_actions"] != false) {
                    item {
                        Spacer(modifier = Modifier.height(26.dp))
                        V2QuickActions(onNavigate = onNavigate)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(26.dp))
                    StatsGrid(
                        activeCount = activeCount,
                        completedCount = completedParcels.size,
                        deliveryCount = deliveryCount,
                        loyaltyPoints = loyaltyPoints
                    )
                }

                if (promotions.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(26.dp))
                        PromoCarousel(
                            promotions = promotions,
                            onApplyPromo = { code ->
                                viewModel.applyPromoCode(code) { success, msg ->
                                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }

                if (sections["loyalty_rewards"] != false) {
                    item {
                        Spacer(modifier = Modifier.height(26.dp))
                        LoyaltyRewardsCard(
                            viewModel = viewModel,
                            loyaltyPoints = loyaltyPoints,
                            deliveryCount = deliveryCount,
                            referralCode = referralCode,
                            onTriggerConfetti = { triggerConfetti = true }
                        )
                    }
                }

                if (sections["active_shipments"] != false) {
                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedFilter == "In Transit") "Active Deliveries" else "All Deliveries",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppTextColor
                            )
                            Text(
                                text = "View All",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Obsidian,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Gold)
                                    .clickable { onNavigate("OrderLogs") }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                                .background(
                                    color = if (isDark) Charcoal else GoldenWhiteLight,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val filters = listOf("All", "In Transit")
                            filters.forEach { filterOption ->
                                val isSelected = selectedFilter == filterOption
                                val tabBg = if (isSelected) {
                                    if (isDark) Gold else Obsidian
                                } else {
                                    Color.Transparent
                                }
                                val tabTextColor = if (isSelected) {
                                    if (isDark) Obsidian else Color.White
                                } else {
                                    TextGray
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(tabBg)
                                        .clickable { selectedFilter = filterOption }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = filterOption,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = tabTextColor
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (filteredParcels.isEmpty()) {
                        item {
                            val emptyBorderColor = if (isDark) Gold.copy(alpha = 0.3f) else Slate
                            val blurryGoldBrush = Brush.linearGradient(
                                colors = listOf(Charcoal, Gold.copy(alpha = 0.08f))
                            )
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                                    .background(blurryGoldBrush, RoundedCornerShape(28.dp)),
                                shape = RoundedCornerShape(28.dp),
                                color = Color.Transparent,
                                border = BorderStroke(1.2.dp, emptyBorderColor)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AnimatedParcelIllustration()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = if (selectedFilter == "In Transit") "No Active Shipments" else "No Shipments Found",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppTextColor
                                    )
                                    Text(
                                        text = if (selectedFilter == "In Transit") {
                                            "You don't have any shipments on the road right now."
                                        } else {
                                            "Your shipment list is currently empty."
                                        },
                                        fontSize = 12.sp,
                                        color = TextGray,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                                    )
                                    Button(
                                        onClick = { onNavigate("SendParcel") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                                        shape = RoundedCornerShape(18.dp)
                                    ) {
                                        Text("Ship Something Now", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Obsidian)
                                    }
                                }
                            }
                        }
                    } else {
                        items(filteredParcels, key = { it.id }) { parcel ->
                            SwipeToArchiveBox(
                                key = parcel.id,
                                onArchive = {
                                    viewModel.archiveParcel(parcel.id)
                                    Toast.makeText(context, "Parcel ${parcel.id} archived", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                ParcelCard(
                                    parcel = parcel,
                                    onClick = {
                                        viewModel.selectParcelForTracking(parcel.id)
                                        onNavigate("ActiveTracking")
                                    },
                                    onQuickView = { },
                                    onCopyTrackingId = { id -> viewModel.showCustomToast("Tracking ID copied: $id") }
                                )
                            }
                        }
                    }
                }

                val recentParcels = completedParcels.take(3)
                if (recentParcels.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(28.dp))
                        Text(
                            text = "Recent History",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppTextColor,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    items(recentParcels) { parcel ->
                        val recentImageBgColor = if (isDark) Color(0xFF1D1D1D) else GoldenWhiteLight
                        val recentPriceTextColor = if (isDark) Gold else Obsidian
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = AppSurface,
                            shadowElevation = 0.dp,
                            border = BorderStroke(1.dp, if (isDark) BorderDark else Slate)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(recentImageBgColor)
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(parcel.imageUrl),
                                            contentDescription = parcel.itemName,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(parcel.itemName, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = AppTextColor)
                                        Text("ID: ${parcel.id} • ${parcel.dateString}", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Medium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${parcel.pickupAddress.substringBefore(",")} ➔ ${parcel.deliveryAddress.substringBefore(",")}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextGray
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("₦${String.format("%,.2f", parcel.price)}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = recentPriceTextColor)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0x154CAF50)
                                        ) {
                                            Text(
                                                text = "Delivered",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF4CAF50),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun V2DashboardHeader(
    firstName: String,
    photoUrl: String,
    loyaltyPoints: Int,
    deliveryCount: Int,
    isDark: Boolean,
    variant: String,
    onVariantChange: (String) -> Unit,
    onProfile: () -> Unit,
    onNotifications: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(if (isDark) Gold else Obsidian)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(2.dp, Gold, CircleShape)
                        .clickable { onProfile() }
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            if (photoUrl.isNotEmpty()) photoUrl
                            else "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&h=100&fit=crop"
                        ),
                        contentDescription = "Profile Pic",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    V2HeaderChip(text = "$loyaltyPoints Pts", icon = Icons.Filled.CardGiftcard, isDark = isDark)
                    V2HeaderChip(text = "$deliveryCount Sent", icon = Icons.Filled.LocalShipping, isDark = isDark)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Obsidian.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.1f))
                            .clickable { onNotifications() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = if (isDark) Obsidian else Gold,
                            modifier = Modifier.size(20.dp)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-2).dp, y = 2.dp)
                                .size(9.dp)
                                .background(if (isDark) Obsidian else Gold, shape = CircleShape)
                                .border(1.5.dp, if (isDark) Gold else Obsidian, CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Hello,",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Obsidian.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = "$firstName!",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Obsidian else Color.White
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isDark) Charcoal.copy(alpha = 0.6f) else GoldenWhiteLight.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                V2VariantChip(
                    label = "Lite",
                    selected = variant == "v2",
                    isDark = isDark,
                    onClick = { onVariantChange("v2") },
                    modifier = Modifier.weight(1f)
                )
                V2VariantChip(
                    label = "Full",
                    selected = variant == "full",
                    isDark = isDark,
                    onClick = { onVariantChange("full") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun V2HeaderChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDark: Boolean = false
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDark) Obsidian.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (isDark) Obsidian else Gold, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isDark) Obsidian else Color.White)
    }
}

@Composable
private fun V2VariantChip(
    label: String,
    selected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Gold else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = if (selected) Obsidian else TextGray
        )
    }
}

@Composable
private fun V2QuickActions(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Quick Actions",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AppTextColor
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardActionBtn(
                title = "Send Parcel",
                icon = Icons.Filled.Send,
                onClick = { onNavigate("SendParcel") },
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp)
            )
            DashboardActionBtn(
                title = "Track",
                icon = Icons.Filled.Place,
                onClick = { onNavigate("ActiveTracking") },
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardActionBtn(
                title = "Multi-Booking",
                icon = Icons.Filled.LocalShipping,
                onClick = { onNavigate("MultiBooking") },
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp)
            )
            DashboardActionBtn(
                title = "Wallet",
                icon = Icons.Filled.AccountBalanceWallet,
                onClick = { onNavigate("Wallet") },
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp)
            )
        }
    }
}
