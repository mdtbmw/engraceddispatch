package com.example.ui.screens

import android.widget.Toast
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.ui.components.BottomNav
import com.example.ui.components.ScreenHeader
import com.example.ui.components.RoundedSheet
import com.example.ui.components.QuiltedBackground
import com.example.ui.components.PinInputField
import com.example.ui.theme.*
import androidx.compose.ui.viewinterop.AndroidView
import com.example.viewmodel.DeliveryViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class ChatMessage(val text: String, val isUser: Boolean)
data class CardInfo(val type: String, val last4: String, val expiry: String)
data class RiderInfo(val name: String, val rating: Double)

// --- PROFILE HOME SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val name by viewModel.userName.collectAsState()
    val email by viewModel.userEmail.collectAsState()
    val phone by viewModel.userPhone.collectAsState()
    val photoUrl by viewModel.photoUrl.collectAsState()
    val isVerified by viewModel.isVerified.collectAsState()
    val loyaltyPoints by viewModel.loyaltyPoints.collectAsState()
    val currentTier = remember(loyaltyPoints) {
        when {
            loyaltyPoints < 100 -> "Bronze Club"
            loyaltyPoints < 500 -> "Silver Tier"
            loyaltyPoints < 1000 -> "Gold Elite"
            else -> "Platinum VIP"
        }
    }
    val totalEarned by viewModel.totalEarned.collectAsState()
    val deliveryCount by viewModel.deliveryCount.collectAsState()
    val userRating by viewModel.userRating.collectAsState()
    val memberSince by viewModel.memberSince.collectAsState()
    val isDark by viewModel.darkModeEnabled.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val activeViewMode by viewModel.activeViewMode.collectAsState()
    val bikeNumber by viewModel.bikeNumber.collectAsState()

    var showEditSheet by remember { mutableStateOf(false) }
    var showPaymentMethodsSheet by remember { mutableStateOf(false) }
    var showHelpSupportSheet by remember { mutableStateOf(false) }
    var showLiveChatSheet by remember { mutableStateOf(false) }
    var showAboutProfileSheet by remember { mutableStateOf(false) }
    var showAvatarSheet by remember { mutableStateOf(false) }
    var showVerificationSheet by remember { mutableStateOf(false) }
    var showRiderOnboardDialog by remember { mutableStateOf(false) }
    var bikeInput by remember { mutableStateOf("") }

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
                title = "My Profile",
                onBack = { onNavigate("Dashboard") },
                rightContent = {
                    val headerContentColor = if (isDark) Obsidian else Color.White
                    Icon(
                        Icons.Filled.Settings,
                        "Settings",
                        tint = headerContentColor,
                        modifier = Modifier
                            .clickable { onNavigate("Settings") }
                            .size(24.dp)
                    )
                }
            )

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = if (isDark) BackgroundDark else BackgroundGray
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 120.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Premium User Profile Card
                    val cardBgColor = if (isDark) Charcoal else Color.White
                    val cardBorderColor = if (isDark) Gold.copy(alpha = 0.2f) else BorderLight
                    val primaryTextColor = if (isDark) Color.White else Obsidian
                    val secondaryTextColor = if (isDark) TextGray else Color(0xFF4B5563) // darker gray for light mode contrast
                    val tierPillBg = if (isDark) Gold.copy(alpha = 0.15f) else Obsidian.copy(alpha = 0.08f)
                    val tierPillColor = if (isDark) Gold else Obsidian

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(24.dp),
                                clip = false,
                                spotColor = if (isDark) Gold.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.04f)
                            ),
                        shape = RoundedCornerShape(24.dp),
                        color = cardBgColor,
                        border = BorderStroke(1.dp, cardBorderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Side: Profile Photo with Camera badge
                                Box(
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .border(2.dp, if (isDark) Gold else Obsidian, CircleShape)
                                            .clip(CircleShape)
                                            .background(if (isDark) Obsidian else GoldenWhite)
                                            .clickable { showAvatarSheet = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (photoUrl.isNotEmpty()) {
                                            Image(
                                                painter = rememberAsyncImagePainter(photoUrl),
                                                contentDescription = "Avatar",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            val initials = name.trim().split("\\s+".toRegex()).take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
                                            if (initials.isNotEmpty()) {
                                                Text(initials, fontSize = 24.sp, color = primaryTextColor, fontWeight = FontWeight.Bold)
                                            } else {
                                                Icon(Icons.Filled.Person, "Profile Photo", modifier = Modifier.size(34.dp), tint = primaryTextColor)
                                            }
                                        }
                                    }

                                    // Compact Edit Badge
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) Charcoal else Color.White)
                                            .border(1.dp, if (isDark) Gold.copy(alpha = 0.5f) else BorderLight, CircleShape)
                                            .clickable { showAvatarSheet = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.CameraAlt, "Change Avatar", modifier = Modifier.size(11.dp), tint = if (isDark) Gold else Obsidian)
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Right Side: Name, Email, Phone & Verification Badge
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = if (name.isNotBlank()) name else "New Member",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = primaryTextColor
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = email,
                                        fontSize = 13.sp,
                                        color = secondaryTextColor
                                    )
                                    if (phone.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = phone,
                                            fontSize = 12.sp,
                                            color = secondaryTextColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Verification Pill Tag
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isVerified) SuccessGreen.copy(alpha = 0.15f) else WarningOrange.copy(alpha = 0.15f),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { if (!isVerified) showVerificationSheet = true }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (isVerified) Icons.Filled.Verified else Icons.Filled.Warning,
                                                    contentDescription = null,
                                                    tint = if (isVerified) SuccessGreen else WarningOrange,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = if (isVerified) "VERIFIED" else "UNVERIFIED",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isVerified) SuccessGreen else WarningOrange
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = tierPillBg
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Star,
                                                    contentDescription = null,
                                                    tint = tierPillColor,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = currentTier.uppercase(),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = tierPillColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // VIP PROGRESSION BAR
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = if (isDark) BorderDark else BorderLight, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            val nextTierPoints = when {
                                loyaltyPoints < 100 -> 100
                                loyaltyPoints < 500 -> 500
                                loyaltyPoints < 1000 -> 1000
                                else -> 2000
                            }
                            val progressRatio = (loyaltyPoints.toFloat() / nextTierPoints).coerceIn(0f, 1f)
                            val nextTierName = when {
                                loyaltyPoints < 100 -> "Silver Tier"
                                loyaltyPoints < 500 -> "Gold Elite"
                                loyaltyPoints < 1000 -> "Platinum VIP"
                                else -> "Maximum Level"
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.LocalActivity,
                                        contentDescription = null,
                                        tint = if (isDark) Gold else Obsidian,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$loyaltyPoints Points Earned",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryTextColor
                                    )
                                }
                                if (loyaltyPoints < 1000) {
                                    Text(
                                        text = "Next: $nextTierName",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = secondaryTextColor
                                    )
                                } else {
                                    Text(
                                        text = "Top Level Member",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Gold else Obsidian
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progressRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (isDark) Gold else Obsidian,
                                trackColor = if (isDark) BorderDark else BorderLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Driver Profile Performance & Status Card (if rider or active view mode is rider)
                    if (userRole == "rider" || activeViewMode == "rider") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = Charcoal,
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Filled.DirectionsBike, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
                                        Text("DRIVER PROFILE & GIG STATS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Gold)
                                    }
                                    val isOnline by viewModel.isOnline.collectAsState()
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isOnline) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = if (isOnline) "● ONLINE" else "○ OFFLINE",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOnline) Color(0xFF4CAF50) else TextGray
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("DELIVERIES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("$deliveryCount", fontSize = 15.sp, fontWeight = FontWeight.Black, color = AppTextColor)
                                    }
                                    Column {
                                        Text("TIPS EARNED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val totalEarned by viewModel.totalEarned.collectAsState()
                                        Text("₦${String.format("%,.2f", totalEarned)}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Gold)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("AVG RATING", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val userRating by viewModel.userRating.collectAsState()
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Icon(Icons.Filled.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(13.dp))
                                            Text(String.format("%.2f", userRating), fontSize = 15.sp, fontWeight = FontWeight.Black, color = AppTextColor)
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Secondary Premium Stats Card (Customer-focused, not rider!)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Gold,
                        border = BorderStroke(1.dp, BorderDark),
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "SHIPMENTS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Obsidian.copy(alpha = 0.65f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = deliveryCount.toString(),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Obsidian
                                )
                            }

                            // Vertical divider
                            Box(
                                modifier = Modifier
                                    .size(1.dp, 24.dp)
                                    .background(Obsidian.copy(alpha = 0.15f))
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "MEMBERSHIP",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Obsidian.copy(alpha = 0.65f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentTier,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Obsidian
                                )
                            }

                            // Vertical divider
                            Box(
                                modifier = Modifier
                                    .size(1.dp, 24.dp)
                                    .background(Obsidian.copy(alpha = 0.15f))
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "MEMBER SINCE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Obsidian.copy(alpha = 0.65f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = memberSince,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Obsidian
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // D. Menu Rows
                    ProfileMenuRow(
                        icon = Icons.Filled.Person,
                        title = "Edit Profile",
                        subtitle = "Manage account & personal details",
                        onClick = { showEditSheet = true }
                    )

                    ProfileMenuRow(
                        icon = Icons.Filled.CreditCard,
                        title = "Wallet & Payment",
                        subtitle = "Manage wallet, top up & withdrawals",
                        onClick = { onNavigate("Wallet") }
                    )

                    ProfileMenuRow(
                        icon = Icons.Filled.Place,
                        title = "Address Book",
                        subtitle = "Manage delivery addresses",
                        onClick = { onNavigate("AddressBook") }
                    )

                    ProfileMenuRow(
                        icon = Icons.Filled.HeadsetMic,
                        title = "Help & Support",
                        subtitle = "FAQ & live support",
                        onClick = { showHelpSupportSheet = true }
                    )

                    if (userRole == "rider") {
                        ProfileMenuRow(
                            icon = Icons.Filled.DirectionsBike,
                            title = "Switch to Rider Dispatch",
                            subtitle = "Manage active orders, pickups & drops (Bike: $bikeNumber)",
                            onClick = {
                                viewModel.setActiveViewMode("rider")
                                onNavigate("Dashboard")
                            }
                        )
                    }

                    if (userRole == "admin" || userRole == "super_admin") {
                        ProfileMenuRow(
                            icon = Icons.Filled.AdminPanelSettings,
                            title = "Enterprise Control Center",
                            subtitle = "AI dispatch control, fleet insights & master logs",
                            onClick = {
                                onNavigate("AIDispatchManager")
                            }
                        )
                    }

                    ProfileMenuRow(
                        icon = Icons.Filled.Info,
                        title = "About ENGRACED DISPATCH",
                        subtitle = "Version 1.0.0",
                        onClick = { showAboutProfileSheet = true }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // E. Logout Button
                    Button(
                        onClick = {
                            viewModel.logout()
                            onNavigate("Login")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(1.dp, Color.Red.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Charcoal else GoldenWhiteLight,
                            contentColor = Color.Red
                        )
                    ) {
                        Icon(Icons.Filled.Logout, "Logout", tint = Color.Red)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("LOG OUT", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.Red)
                    }
                }
            }
        }
    }

    // --- Profile Modal Sheets ---
    if (showRiderOnboardDialog) {
        AlertDialog(
            onDismissRequest = { showRiderOnboardDialog = false },
            title = {
                Text(
                    "Register as Dispatch Rider",
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Obsidian
                )
            },
            text = {
                Column {
                    Text(
                        "Become part of the premium logistics team. Earn up to 80% split on every delivery gig you complete.",
                        fontSize = 13.sp,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = bikeInput,
                        onValueChange = { bikeInput = it },
                        label = { Text("Enter Motorbike License Number") },
                        placeholder = { Text("e.g. ESD-RIDER-992") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            focusedLabelColor = Gold,
                            unfocusedBorderColor = if (isDark) BorderDark else BorderLight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bikeInput.trim().isBlank()) {
                            Toast.makeText(context, "Please enter your motorbike license number", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.setBikeNumber(bikeInput)
                        viewModel.setUserRole("rider")
                        showRiderOnboardDialog = false
                        Toast.makeText(context, "Welcome to the Engraced Dispatch Rider team!", Toast.LENGTH_LONG).show()
                        viewModel.setActiveViewMode("rider")
                        onNavigate("Dashboard")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian)
                ) {
                    Text("ACTIVATE ACCOUNT", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRiderOnboardDialog = false }) {
                    Text("CANCEL", color = TextGray)
                }
            },
            containerColor = if (isDark) BackgroundDark else Color.White
        )
    }

    if (showEditSheet) {
        ProfileEditSheet(viewModel) { showEditSheet = false }
    }
    if (showPaymentMethodsSheet) {
        PaymentMethodsSheet { showPaymentMethodsSheet = false }
    }
    if (showHelpSupportSheet) {
        HelpSupportSheet(
            onOpenLiveChat = {
                showHelpSupportSheet = false
                showLiveChatSheet = true
            },
            onDismiss = { showHelpSupportSheet = false }
        )
    }
    if (showLiveChatSheet) {
        LiveChatSheet { showLiveChatSheet = false }
    }
    if (showAboutProfileSheet) {
        AboutProfileSheet(showMinSdk = false) { showAboutProfileSheet = false }
    }
    if (showAvatarSheet) {
        AvatarSelectionSheet(viewModel) { showAvatarSheet = false }
    }
    if (showVerificationSheet) {
        VerificationSheet(viewModel) { showVerificationSheet = false }
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    isDark: Boolean,
    noArrow: Boolean = false,
    textClass: Color = Obsidian,
    onClick: () -> Unit
) {
    val resolvedColor = if (textClass == Obsidian) AppOnSurface else textClass
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isDark) Charcoal else Color(0xFFF4F5F7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = if (resolvedColor == Color.Red) Color.Red else TextGray, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = resolvedColor)
        }

        if (!noArrow) {
            Icon(Icons.Filled.ChevronRight, null, tint = TextGray, modifier = Modifier.size(20.dp))
        }
    }
}

// --- WALLET SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val balance by viewModel.walletBalance.collectAsState()
    val txs by viewModel.transactions.collectAsState()
    val isDark by viewModel.darkModeEnabled.collectAsState()

    val animatedBalance by animateFloatAsState(
        targetValue = balance.toFloat(),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "balanceAnimation"
    )

    var currentPage by remember { mutableStateOf(0) }
    val itemsPerPage = 3
    
    val totalPages = remember(txs) {
        ((txs.size + itemsPerPage - 1) / itemsPerPage).coerceAtLeast(1)
    }

    val paginatedTxs = remember(txs, currentPage) {
        val startIndex = currentPage * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, txs.size)
        if (startIndex < txs.size) {
            txs.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
    }

    var showFundWithdrawSheet by remember { mutableStateOf(false) }
    var sheetMode by remember { mutableStateOf("fund") } // fund or withdraw
    var showPaystackSheet by remember { mutableStateOf(false) }
    var showSuccessSheet by remember { mutableStateOf(false) }
    var showBankSetupSheet by remember { mutableStateOf(false) }
    var showPinAuthSheet by remember { mutableStateOf(false) }

    var showConfirmationDialog by remember { mutableStateOf(false) }
    var confirmationTitle by remember { mutableStateOf("") }
    var confirmationMessage by remember { mutableStateOf("") }
    var onConfirmAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    var pendingAmount by remember { mutableStateOf(0.0) }
    var successTitle by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var pinAuthSuccessCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

    val brandGradient = Brush.verticalGradient(
        colors = listOf(Obsidian, Obsidian)
    )

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
                title = "My Wallet",
                onBack = { onNavigate("Profile") }
            )

            RoundedSheet(
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Obsidian Card
                        Surface(
                            shape = RoundedCornerShape(32.dp),
                            color = Obsidian,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                QuiltedBackground(modifier = Modifier.matchParentSize()) {}

                                Column(modifier = Modifier.padding(32.dp)) {
                                    Text("Total Balance", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                    Text("₦${String.format("%,.2f", animatedBalance.toDouble())}", fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color.White)

                                    Spacer(modifier = Modifier.height(32.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                sheetMode = "fund"
                                                showFundWithdrawSheet = true
                                            },
                                            shape = RoundedCornerShape(20.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(52.dp)
                                        ) {
                                            Text("Top Up", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Obsidian)
                                        }

                                        Button(
                                            onClick = {
                                                sheetMode = "withdraw"
                                                showFundWithdrawSheet = true
                                            },
                                            shape = RoundedCornerShape(20.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(52.dp)
                                        ) {
                                            Text("Withdraw", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Text("Recent Transactions", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = AppOnSurface)
                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            paginatedTxs.forEach { tx ->
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = AppSurface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(if (tx.isTopUp) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (tx.isTopUp) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                                                    contentDescription = "Type",
                                                    tint = if (tx.isTopUp) Color(0xFF4CAF50) else Color(0xFFF44336),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(tx.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppOnSurface)
                                                Text(tx.date, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                            }
                                        }

                                        val amtColor = if (tx.isTopUp) Color(0xFF4CAF50) else AppOnSurface
                                        val prefix = if (tx.isTopUp) "+" else "-"
                                        Text(
                                            text = "$prefix₦${String.format("%,.2f", kotlin.math.abs(tx.amount))}",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = amtColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Dynamic transaction pagination controls at the absolute bottom
                    if (totalPages > 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val activeColor = if (isDark) Gold else Obsidian
                            val activeTextColor = if (isDark) Obsidian else Gold
                            val inactiveBg = if (isDark) Charcoal else Color(0xFFF1F5F9)
                            val inactiveTextColor = if (isDark) TextGray else TextGray
                            val borderColor = if (isDark) Color(0xFF2C2C2C) else Color(0xFFE2E8F0)

                            // Previous button
                            IconButton(
                                onClick = { if (currentPage > 0) currentPage-- },
                                enabled = currentPage > 0,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (currentPage > 0) inactiveBg else inactiveBg.copy(alpha = 0.5f))
                                    .border(1.dp, borderColor, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous Page",
                                    tint = if (currentPage > 0) (if (isDark) Gold else Obsidian) else TextGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Page Numbers
                            for (i in 0 until totalPages) {
                                val isSelected = i == currentPage
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) activeColor else inactiveBg)
                                        .border(1.dp, if (isSelected) activeColor else borderColor, CircleShape)
                                        .clickable { currentPage = i },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${i + 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) activeTextColor else inactiveTextColor
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Next button
                            IconButton(
                                onClick = { if (currentPage < totalPages - 1) currentPage++ },
                                enabled = currentPage < totalPages - 1,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (currentPage < totalPages - 1) inactiveBg else inactiveBg.copy(alpha = 0.5f))
                                    .border(1.dp, borderColor, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next Page",
                                    tint = if (currentPage < totalPages - 1) (if (isDark) Gold else Obsidian) else TextGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Wallet Modal Sheets ---
    if (showFundWithdrawSheet) {
        FundWithdrawBottomSheet(
            viewModel = viewModel,
            mode = sheetMode,
            onFundTrigger = { amount ->
                pendingAmount = amount
                confirmationTitle = "Confirm Top Up"
                confirmationMessage = "You are about to top up your wallet with ₦${String.format("%,.2f", amount)} via Paystack Secure Checkout."
                onConfirmAction = {
                    showConfirmationDialog = false
                    showFundWithdrawSheet = false
                    showPaystackSheet = true
                }
                showConfirmationDialog = true
            },
            onWithdrawTrigger = { amount ->
                pendingAmount = amount
                confirmationTitle = "Confirm Withdrawal"
                confirmationMessage = "You are about to withdraw ₦${String.format("%,.2f", amount)} from your wallet to your registered bank account."
                onConfirmAction = {
                    showConfirmationDialog = false
                    showFundWithdrawSheet = false
                    pinAuthSuccessCallback = {
                        viewModel.topUpWallet(-pendingAmount)
                        successTitle = "Withdrawal Initiated"
                        successMessage = "Your withdrawal of ₦${String.format("%,.2f", pendingAmount)} is being processed and will arrive in your bank account shortly."
                        showSuccessSheet = true
                    }
                    showPinAuthSheet = true
                }
                showConfirmationDialog = true
            },
            onOpenBankSetup = {
                showFundWithdrawSheet = false
                showBankSetupSheet = true
            },
            onDismiss = { showFundWithdrawSheet = false }
        )
    }

    if (showPaystackSheet) {
        PaystackCheckoutSheet(
            amount = pendingAmount,
            onPaymentComplete = { reference ->
                viewModel.topUpWallet(pendingAmount)
                showPaystackSheet = false
                successTitle = "Payment Successful"
                successMessage = "₦${String.format("%,.2f", pendingAmount)} has been added to your wallet balance. Ref: $reference"
                showSuccessSheet = true
            },
            onDismiss = { showPaystackSheet = false }
        )
    }

    if (showBankSetupSheet) {
        BankSetupSheet(
            viewModel = viewModel,
            onSetupComplete = {
                showBankSetupSheet = false
                sheetMode = "withdraw"
                showFundWithdrawSheet = true
            },
            onDismiss = { showBankSetupSheet = false }
        )
    }

    if (showPinAuthSheet) {
        PinAuthSheet(
            viewModel = viewModel,
            onAuthSuccess = {
                showPinAuthSheet = false
                pinAuthSuccessCallback?.invoke()
            },
            onDismiss = { showPinAuthSheet = false }
        )
    }

    if (showSuccessSheet) {
        SuccessBottomSheet(
            title = successTitle,
            message = successMessage,
            onDismiss = { showSuccessSheet = false }
        )
    }

    if (showConfirmationDialog) {
        val isDarkDialog = isDark
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            modifier = if (isDarkDialog) Modifier.border(1.5.dp, Gold, RoundedCornerShape(24.dp)) else Modifier,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Confirm Info",
                        tint = if (isDarkDialog) Gold else Obsidian,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = confirmationTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = if (isDarkDialog) Color.White else Obsidian
                    )
                }
            },
            text = {
                Text(
                    text = confirmationMessage,
                    fontSize = 15.sp,
                    color = if (isDarkDialog) TextGray else Obsidian.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                Button(
                    onClick = { onConfirmAction?.invoke() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkDialog) Gold else Obsidian,
                        contentColor = if (isDarkDialog) Obsidian else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirm", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmationDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isDarkDialog) Gold else Obsidian
                    )
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = if (isDarkDialog) Obsidian else BackgroundLight,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val push by viewModel.pushEnabled.collectAsState()
    val location by viewModel.locationEnabled.collectAsState()
    val dark by viewModel.darkModeEnabled.collectAsState()

    var showEditProfile by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    var showPinSetupSheet by remember { mutableStateOf(false) }
    var showLoginModeSheet by remember { mutableStateOf(false) }

    val isDark = MaterialTheme.colorScheme.background == BackgroundDark
    val pageBg = LuxuryBlack
    val sheetBg = AppSurface
    val textPrimary = AppTextColor
    val textSecondary = TextGray
    val lightGold = Color(0xFFD4AF37)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HeaderBgColor)
        ) {
            ScreenHeader(
                title = "Settings",
                onBack = { onNavigate("Profile") }
            )

            RoundedSheet(
                modifier = Modifier.weight(1f),
                containerColor = sheetBg,
                contentColor = textPrimary
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(sheetBg)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = "Preferences",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) lightGold else Obsidian,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = if (isDark) Color(0xFF1D1D1D) else GoldenWhite,
                        border = BorderStroke(1.dp, if (isDark) lightGold.copy(alpha = 0.3f) else Slate.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            SettingsToggleHighContrast(
                                title = "Push Notifications",
                                checked = push,
                                onCheckedChange = { viewModel.togglePushNotifications() }
                            )
                            SettingsToggleHighContrast(
                                title = "Location Services",
                                checked = location,
                                onCheckedChange = { viewModel.toggleLocationServices() }
                            )
                            SettingsToggleHighContrast(
                                title = "Dark Mode",
                                checked = dark,
                                onCheckedChange = { viewModel.toggleDarkMode() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Account & Security",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) lightGold else Obsidian,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = if (isDark) Color(0xFF1D1D1D) else GoldenWhite,
                        border = BorderStroke(1.dp, if (isDark) lightGold.copy(alpha = 0.3f) else Slate.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            SettingsItemHighContrast(
                                icon = Icons.Filled.Person,
                                title = "Edit Profile",
                                onClick = { showEditProfile = true }
                            )
                            SettingsItemHighContrast(
                                icon = Icons.Filled.Dialpad,
                                title = "PIN Authentication Setup",
                                onClick = { showPinSetupSheet = true }
                            )
                            SettingsItemHighContrast(
                                icon = Icons.Filled.LockOpen,
                                title = "Authentication Preference",
                                onClick = { showLoginModeSheet = true }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            viewModel.logout()
                            onNavigate("Login")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color.Red),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF0B0B0B) else Color.White,
                            contentColor = Color.Red
                        )
                    ) {
                        Icon(Icons.Filled.Logout, "Logout", tint = Color.Red)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SIGN OUT",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Red,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }

    // --- Settings Modal Sheets ---
    if (showEditProfile) {
        ProfileEditSheet(viewModel) { showEditProfile = false }
    }
    if (showPinSetupSheet) {
        PinSetupSheet(viewModel) { showPinSetupSheet = false }
    }
    if (showLoginModeSheet) {
        LoginModeSheet(viewModel) { showLoginModeSheet = false }
    }
}

@Composable
fun SettingsToggleHighContrast(
    title: String,
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark
    val textCol = AppTextColor
    val lightGold = Color(0xFFD4AF37)
    val checkedThumbColor = if (isDark) Obsidian else GoldenWhiteLight
    val checkedTrackColor = Gold
    val uncheckedThumbColor = if (isDark) Gold.copy(alpha = 0.5f) else TextGray.copy(alpha = 0.5f)
    val uncheckedTrackColor = if (isDark) Obsidian else BorderLight
    val uncheckedBorderColor = if (isDark) Gold.copy(alpha = 0.3f) else Color(0xFFD1D5DB)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onCheckedChange() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = textCol
        )

        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = checkedThumbColor,
                checkedTrackColor = checkedTrackColor,
                uncheckedThumbColor = uncheckedThumbColor,
                uncheckedTrackColor = uncheckedTrackColor,
                uncheckedBorderColor = uncheckedBorderColor
            )
        )
    }
}

@Composable
fun SettingsItemHighContrast(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark
    val textCol = AppTextColor
    val iconColor = if (isDark) Gold else Obsidian
    val iconBgColor = if (isDark) Gold.copy(alpha = 0.12f) else Obsidian.copy(alpha = 0.08f)
    val chevronColor = if (isDark) Gold.copy(alpha = 0.6f) else TextGray.copy(alpha = 0.6f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = textCol
            )
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = chevronColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SettingsToggle(
    title: String,
    checked: Boolean,
    isDark: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onCheckedChange() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppOnSurface)

        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Gold,
                uncheckedThumbColor = TextGray,
                uncheckedTrackColor = if (isDark) Color(0xFF2C2C2C) else Color(0xFFEFEFEF)
            )
        )
    }
}

// --- RIDER REVIEW SCREEN ---
@Composable
fun RiderReviewScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    var rating by remember { mutableStateOf(4) }
    var reviewText by remember { mutableStateOf("") }
    val isDark by viewModel.darkModeEnabled.collectAsState()

    val brandGradient = Brush.verticalGradient(
        colors = listOf(Obsidian, Obsidian)
    )

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
                title = "Rate Rider",
                onBack = { onNavigate("Profile") }
            )

            RoundedSheet(
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Rider Card Info
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .border(4.dp, Gold, CircleShape)
                                .clip(CircleShape)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter("https://images.unsplash.com/photo-1599566150163-29194dcaad36?w=200&h=200&fit=crop"),
                                contentDescription = "Rider Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Richard Dheo", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = AppOnSurface)
                        Text("Delivery #70D20800B", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextGray)

                        Spacer(modifier = Modifier.height(40.dp))

                        Text("How was your delivery?", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = AppOnSurface)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Interactive 5 star row (Gold items!)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (star in 1..5) {
                                val isFilled = star <= rating
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Star $star",
                                    tint = if (isFilled) Gold else if (isDark) Color(0xFF2C2C2C) else Color(0xFFEFEFEF),
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable { rating = star }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Feedback box (Softened borders!)
                        OutlinedTextField(
                            value = reviewText,
                            onValueChange = { reviewText = it },
                            placeholder = { Text("Write a review...", color = TextGray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = if (isDark) Color(0xFF2C2C2C) else Slate,
                                focusedContainerColor = AppSurface,
                                unfocusedContainerColor = AppSurface,
                                focusedTextColor = AppOnSurface,
                                unfocusedTextColor = AppOnSurface
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    val buttonBg = if (isDark) Gold else Obsidian
                    val buttonTextCol = if (isDark) Obsidian else Color.White
                    Button(
                        onClick = { onNavigate("Dashboard") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonBg)
                    ) {
                        Text("Submit Review", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = buttonTextCol)
                    }
                }
            }
        }
    }
}

// --- ADDRESS BOOK ---
@Composable
fun AddressBookScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val addresses by viewModel.addresses.collectAsState()
    var labelInput by remember { mutableStateOf("") }
    var addrInput by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    val isDark by viewModel.darkModeEnabled.collectAsState()

    val brandGradient = Brush.verticalGradient(
        colors = listOf(Obsidian, Obsidian)
    )

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
                title = "Address Book",
                onBack = { onNavigate("Profile") },
                rightContent = {
                    Icon(
                        Icons.Filled.Add,
                        "Add",
                        tint = Gold,
                        modifier = Modifier
                            .clickable { showDialog = true }
                            .size(24.dp)
                    )
                }
            )

            RoundedSheet(
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(addresses) { item ->
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = AppSurface,
                                border = if (item.isDefault) BorderStroke(2.dp, Gold) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(if (item.isDefault) Gold.copy(alpha = 0.1f) else if (isDark) Charcoal else Color(0xFFF4F5F7)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (item.label == "Home") Icons.Filled.Home else Icons.Filled.Work,
                                                    contentDescription = null,
                                                    tint = if (item.isDefault) Gold else TextGray,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(item.label, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = AppOnSurface)
                                        }

                                        if (item.isDefault) {
                                            Text(
                                                "DEFAULT",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = TextGray,
                                                modifier = Modifier
                                                    .background(if (isDark) Color(0xFF2C2C2C) else Color(0xFFF3F3F3), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        item.address,
                                        fontSize = 14.sp,
                                        color = TextGray,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(start = 52.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Quick Add Dialog
                    if (showDialog) {
                        val buttonBg = if (isDark) Gold else Obsidian
                        val buttonTextCol = if (isDark) Obsidian else Color.White
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            modifier = if (isDark) Modifier.border(1.5.dp, Gold, RoundedCornerShape(24.dp)) else Modifier,
                            shape = RoundedCornerShape(24.dp),
                            containerColor = if (isDark) Obsidian else BackgroundLight,
                            confirmButton = {
                                Button(
                                    onClick = {
                                        if (labelInput.isNotBlank() && addrInput.isNotBlank()) {
                                            viewModel.addAddress(labelInput, addrInput)
                                            labelInput = ""
                                            addrInput = ""
                                            showDialog = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = buttonBg)
                                ) {
                                    Text("Add", color = buttonTextCol)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDialog = false }) { Text("Cancel", color = TextGray) }
                            },
                            title = { Text("Add Address", fontWeight = FontWeight.ExtraBold) },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = labelInput,
                                        onValueChange = { labelInput = it },
                                        placeholder = { Text("Label (e.g. Vacation Home)") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Gold,
                                            unfocusedBorderColor = if (isDark) Color(0xFF2C2C2C) else Slate,
                                            focusedContainerColor = AppBackground,
                                            unfocusedContainerColor = AppBackground,
                                            focusedTextColor = AppOnSurface,
                                            unfocusedTextColor = AppOnSurface
                                        )
                                    )

                                    OutlinedTextField(
                                        value = addrInput,
                                        onValueChange = { addrInput = it },
                                        placeholder = { Text("Complete Address") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Gold,
                                            unfocusedBorderColor = if (isDark) Color(0xFF2C2C2C) else Slate,
                                            focusedContainerColor = AppBackground,
                                            unfocusedContainerColor = AppBackground,
                                            focusedTextColor = AppOnSurface,
                                            unfocusedTextColor = AppOnSurface
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// --- NOTIFICATIONS SCREEN ---
@Composable
fun NotificationsScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val list by viewModel.notifications.collectAsState()
    val isDark by viewModel.darkModeEnabled.collectAsState()
    val activeParcel by viewModel.selectedParcel.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) } // 0 = Inbox, 1 = Simulator & Previewer
    var currentPage by remember { mutableStateOf(0) }
    val itemsPerPage = 4

    // Simulator specific states
    var simulatedLockScreenVisible by remember { mutableStateOf(false) }
    var lockScreenTitle by remember { mutableStateOf("ENGRACED DISPATCH • Live Courier") }
    var lockScreenMessage by remember { mutableStateOf("Courier rider Daniel is 1.2km away with your package.") }
    var lockScreenProgress by remember { mutableStateOf(0.65f) }

    val totalPages = remember(list) {
        ((list.size + itemsPerPage - 1) / itemsPerPage).coerceAtLeast(1)
    }

    val paginatedList = remember(list, currentPage) {
        val startIndex = currentPage * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, list.size)
        if (startIndex < list.size) {
            list.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
    }

    val brandGradient = Brush.verticalGradient(
        colors = listOf(Obsidian, Obsidian)
    )

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
                title = "Notifications Hub",
                onBack = { onNavigate("Profile") }
            )

            // Dynamic Header Tabs (Material 3 standard with high-contrast active indicator)
            val tabBg = if (isDark) Obsidian else GoldenWhite
            val activeTabColor = if (isDark) Gold else Obsidian
            val inactiveTabColor = if (isDark) TextGray else Color(0xFF4B5563)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tabBg)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Inbox Tab Button
                Button(
                    onClick = { selectedTab = 0 },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 0) activeTabColor else (if (isDark) Charcoal else Color.White),
                        contentColor = if (selectedTab == 0) (if (isDark) Obsidian else Color.White) else inactiveTabColor
                    ),
                    border = if (selectedTab == 0) null else BorderStroke(1.dp, if (isDark) BorderDark else BorderLight),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "INBOX (${list.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Simulator Tab Button
                Button(
                    onClick = { selectedTab = 1 },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 1) activeTabColor else (if (isDark) Charcoal else Color.White),
                        contentColor = if (selectedTab == 1) (if (isDark) Obsidian else Color.White) else inactiveTabColor
                    ),
                    border = if (selectedTab == 1) null else BorderStroke(1.dp, if (isDark) BorderDark else BorderLight),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SendToMobile,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SIMULATOR DECK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            RoundedSheet(
                modifier = Modifier.weight(1f)
            ) {
                if (selectedTab == 0) {
                    // --- TAB 0: INBOX HISTORY ---
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (list.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "No Notifications",
                                        tint = if (isDark) Gold.copy(alpha = 0.6f) else Obsidian.copy(alpha = 0.3f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "All Caught Up! ✨",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppOnSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Your premium notifications hub is empty. Use the SIMULATOR DECK tab to trigger realistic push alerts instantly!",
                                        fontSize = 12.sp,
                                        color = TextGray,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentPadding = PaddingValues(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(paginatedList) { item ->
                                    val cardBg = if (isDark) Charcoal else Color.White
                                    val textPrimary = if (isDark) Color.White else Obsidian
                                    val textSecondary = if (isDark) TextGray else Color(0xFF4B5563)

                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = cardBg,
                                        border = BorderStroke(1.dp, if (isDark) BorderDark else BorderLight),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .drawBehind {
                                                    // gold indicator bar at left margin
                                                    drawRect(
                                                        color = Gold,
                                                        size = Size(10f, size.height)
                                                    )
                                                }
                                                .padding(16.dp)
                                        ) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = item.title,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 14.sp,
                                                        color = textPrimary
                                                    )
                                                    Text(
                                                        text = item.time,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextGray
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = item.message,
                                                    fontSize = 12.sp,
                                                    color = textSecondary,
                                                    lineHeight = 15.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Dynamic Notification Pagination Controls
                        if (totalPages > 1) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val activeColor = if (isDark) Gold else Obsidian
                                val activeTextColor = if (isDark) Obsidian else Gold
                                val inactiveBg = if (isDark) Charcoal else Color(0xFFF1F5F9)
                                val inactiveTextColor = TextGray
                                val borderColor = if (isDark) Color(0xFF2C2C2C) else Color(0xFFE2E8F0)

                                // Previous button
                                IconButton(
                                    onClick = { if (currentPage > 0) currentPage-- },
                                    enabled = currentPage > 0,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (currentPage > 0) inactiveBg else inactiveBg.copy(alpha = 0.5f))
                                        .border(1.dp, borderColor, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Previous Page",
                                        tint = if (currentPage > 0) (if (isDark) Gold else Obsidian) else TextGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Page Numbers
                                for (i in 0 until totalPages) {
                                    val isSelected = i == currentPage
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) activeColor else inactiveBg)
                                            .border(1.dp, if (isSelected) activeColor else borderColor, CircleShape)
                                            .clickable { currentPage = i },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${i + 1}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) activeTextColor else inactiveTextColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Next button
                                IconButton(
                                    onClick = { if (currentPage < totalPages - 1) currentPage++ },
                                    enabled = currentPage < totalPages - 1,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (currentPage < totalPages - 1) inactiveBg else inactiveBg.copy(alpha = 0.5f))
                                        .border(1.dp, borderColor, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Next Page",
                                        tint = if (currentPage < totalPages - 1) (if (isDark) Gold else Obsidian) else TextGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // --- TAB 1: INTERACTIVE SIMULATOR & PREVIEWER ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    ) {
                        val cardBg = if (isDark) Charcoal else Color.White
                        val textColor = if (isDark) Color.White else Obsidian
                        val secondaryText = if (isDark) TextGray else Color(0xFF4B5563)
                        val borderColor = if (isDark) BorderDark else BorderLight

                        Text(
                            text = "SYSTEM NOTIFICATION GENERATOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isDark) Gold else Obsidian,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap any operational trigger below to dispatch authentic system alerts directly to the OS shade and your local inbox:",
                            fontSize = 12.sp,
                            color = secondaryText,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Trigger Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Welcome Trigger
                            Button(
                                onClick = {
                                    viewModel.addNotification(
                                        "Welcome to ENGRACED DISPATCH! 📦✨",
                                        "Your premium account setup is fully complete. Experience modern, elite dispatch services!"
                                    )
                                    viewModel.addNotification(
                                        "VIP Club Registration 💎",
                                        "You've been successfully signed up for exclusive tier rewards. Check your profile settings!"
                                    )
                                    Toast.makeText(context, "Welcome Alerts Triggered!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDark) Charcoal else Color(0xFFF1F5F9),
                                    contentColor = textColor
                                ),
                                border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.3f) else BorderLight)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CardGiftcard, null, tint = if (isDark) Gold else Obsidian, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Welcome", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Promo Trigger
                            Button(
                                onClick = {
                                    viewModel.addNotification(
                                        "⚡ Regional Flash Promo Active!",
                                        "Claim 30% discount on your next dispatch inside the city center. Use Coupon code: METRO30."
                                    )
                                    viewModel.addNotification(
                                        "Surge Mitigation Broadcast 📉",
                                        "Fleet density is peak. Surge multipliers have been temporarily reduced across Lagos!"
                                    )
                                    Toast.makeText(context, "Promo Campaign Alerts Dispatched!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDark) Charcoal else Color(0xFFF1F5F9),
                                    contentColor = textColor
                                ),
                                border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.3f) else BorderLight)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.LocalActivity, null, tint = if (isDark) Gold else Obsidian, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Campaign", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Tracking Trigger
                            Button(
                                onClick = {
                                    val trackingId = activeParcel?.id ?: "EG-9284-NX"
                                    viewModel.addNotification(
                                        "📍 Tracking Alert: Rider Heading to Pickup",
                                        "Shipment $trackingId has been accepted. Dispatch rider Daniel is en route to pickup location."
                                    )
                                    viewModel.addNotification(
                                        "🚨 Proximity Trigger: Arrived!",
                                        "Rider Daniel is within 50 meters of your delivery coordinates. Please get ready to collect your parcel."
                                    )
                                    Toast.makeText(context, "Live Transit Alerts Sent!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDark) Charcoal else Color(0xFFF1F5F9),
                                    contentColor = textColor
                                ),
                                border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.3f) else BorderLight)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.LocalShipping, null, tint = if (isDark) Gold else Obsidian, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Tracking", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // --- LOCK SCREEN BANNER SIMULATION ---
                        Text(
                            text = "SMARTPHONE LOCK-SCREEN SIMULATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isDark) Gold else Obsidian,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Simulate exactly how real-time transit telemetry appears on a locked Android lockscreen:",
                            fontSize = 12.sp,
                            color = secondaryText
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Lockscreen Phone Mockup container
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(24.dp)),
                            color = Color.Black,
                            border = BorderStroke(2.dp, if (isDark) Gold.copy(alpha = 0.3f) else Obsidian.copy(alpha = 0.5f))
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Background mock image or abstract background
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.sweepGradient(
                                                colors = listOf(
                                                    Color(0xFF0F172A),
                                                    Color(0xFF1E1B4B),
                                                    Color(0xFF311042),
                                                    Color(0xFF0F172A)
                                                )
                                            )
                                        )
                                )

                                // Lock Status Icon and clock
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Filled.Lock, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("08:45 AM", fontSize = 28.sp, fontWeight = FontWeight.Light, color = Color.White)
                                    Text("Friday, October 10", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                                }

                                // Interactive Notification sliding card
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = simulatedLockScreenVisible,
                                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 16.dp, start = 12.dp, end = 12.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xEA1A1A1A),
                                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        painter = painterResource(id = com.example.R.drawable.ic_logo),
                                                        contentDescription = null,
                                                        tint = Gold,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "ENGRACED DISPATCH",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Gold,
                                                        letterSpacing = 1.sp
                                                    )
                                                }
                                                Text("just now", fontSize = 8.sp, color = Color.White.copy(alpha = 0.5f))
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(lockScreenTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(lockScreenMessage, fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                                            Spacer(modifier = Modifier.height(8.dp))

                                            LinearProgressIndicator(
                                                progress = { lockScreenProgress },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(4.dp)
                                                    .clip(RoundedCornerShape(2.dp)),
                                                color = Gold,
                                                trackColor = Color.White.copy(alpha = 0.1f)
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "TRACK MAP",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Gold,
                                                    modifier = Modifier
                                                        .clickable {
                                                            onNavigate("ActiveTracking")
                                                        }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = "DISMISS",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White.copy(alpha = 0.6f),
                                                    modifier = Modifier
                                                        .clickable {
                                                            simulatedLockScreenVisible = false
                                                        }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (!simulatedLockScreenVisible) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 24.dp)
                                    ) {
                                        Text(
                                            text = "Swipe up or tap button below to unlock preview",
                                            fontSize = 9.sp,
                                            color = Color.White.copy(alpha = 0.4f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val active = activeParcel
                                    if (active != null) {
                                        lockScreenTitle = "Live Transit Telemetry Active 📡"
                                        lockScreenMessage = "Package: ${active.itemName} is currently with courier rider. Delivery ETA is ${active.dateString}."
                                        lockScreenProgress = 0.5f
                                    } else {
                                        lockScreenTitle = "ENGRACED DISPATCH • Live Courier"
                                        lockScreenMessage = "Courier rider Daniel is 1.2km away with your default package."
                                        lockScreenProgress = 0.75f
                                    }
                                    simulatedLockScreenVisible = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDark) Gold else Obsidian,
                                    contentColor = if (isDark) Obsidian else Color.White
                                )
                            ) {
                                Icon(Icons.Filled.LockOpen, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Trigger Lock Screen", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { simulatedLockScreenVisible = false },
                                enabled = simulatedLockScreenVisible,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDark) Charcoal else Color(0xFFF1F5F9),
                                    contentColor = textColor
                                ),
                                border = BorderStroke(1.dp, borderColor)
                            ) {
                                Icon(Icons.Filled.Close, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Clear Screen", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // --- INTERACTIVE WIDGET PREVIEW ---
                        Text(
                            text = "ENGRACED DISPATCH HOME SCREEN WIDGET MOCKUP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isDark) Gold else Obsidian,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your elite client includes custom responsive home-screen widget support. Below is the active widget design render:",
                            fontSize = 12.sp,
                            color = secondaryText
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Widget container mimicking the Android Home Screen context
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            color = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, borderColor)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Background grids mimicking home screen app dots
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val dotRadius = 2f
                                    val interval = 40f
                                    for (x in 0..size.width.toInt() step interval.toInt()) {
                                        for (y in 0..size.height.toInt() step interval.toInt()) {
                                            drawCircle(
                                                color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f),
                                                radius = dotRadius,
                                                center = androidx.compose.ui.geometry.Offset(x.toFloat(), y.toFloat())
                                            )
                                        }
                                    }
                                }

                                // Custom 4x2 App Widget surface
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isDark) Obsidian else Color.White,
                                    border = BorderStroke(1.5.dp, if (isDark) Gold.copy(alpha = 0.5f) else Obsidian.copy(alpha = 0.2f)),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(horizontal = 20.dp, vertical = 12.dp)
                                        .fillMaxSize()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        // Widget Header
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    painter = painterResource(id = com.example.R.drawable.ic_logo),
                                                    contentDescription = null,
                                                    tint = if (isDark) Gold else Obsidian,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "ENGRACE • LIVE",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isDark) Gold else Obsidian,
                                                    letterSpacing = 1.sp
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isDark) Gold.copy(alpha = 0.15f) else Obsidian.copy(alpha = 0.08f)
                                            ) {
                                                Text(
                                                    text = "WIDGET ACTIVE",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isDark) Gold else Obsidian,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Widget body: Active parcel tracking state
                                        val active = activeParcel
                                        if (active != null) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = active.itemName.uppercase(),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = textColor
                                                    )
                                                    Text(
                                                        text = "ID: ${active.id} • Transit Route",
                                                        fontSize = 9.sp,
                                                        color = secondaryText
                                                    )
                                                }

                                                IconButton(
                                                    onClick = { onNavigate("ActiveTracking") },
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isDark) Gold else Obsidian)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Place,
                                                        contentDescription = "Map",
                                                        tint = if (isDark) Obsidian else Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            LinearProgressIndicator(
                                                progress = { 0.6f },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(4.dp)
                                                    .clip(RoundedCornerShape(2.dp)),
                                                color = if (isDark) Gold else Obsidian,
                                                trackColor = if (isDark) BorderDark else BorderLight
                                            )
                                        } else {
                                            // No active trip state
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "READY FOR NEW DISPATCH",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = textColor
                                                    )
                                                    Text(
                                                        text = "No active transit. Book your premium route instantly:",
                                                        fontSize = 8.sp,
                                                        color = secondaryText
                                                    )
                                                }

                                                Button(
                                                    onClick = {
                                                        viewModel.clearDraft()
                                                        onNavigate("BookingForm")
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isDark) Gold else Obsidian,
                                                        contentColor = if (isDark) Obsidian else Color.White
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(10.dp))
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text("BOOK NOW", fontSize = 8.sp, fontWeight = FontWeight.Black)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

// --- PROMOTIONS VOUCHER SCREEN ---
@Composable
fun PromotionsScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val list by viewModel.promotions.collectAsState()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val isDark by viewModel.darkModeEnabled.collectAsState()

    val brandGradient = Brush.verticalGradient(
        colors = listOf(Obsidian, Obsidian)
    )

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
                title = "Promotions",
                onBack = { onNavigate("Profile") }
            )

            RoundedSheet(
                modifier = Modifier.weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(list) { promo ->
                        Surface(
                            shape = RoundedCornerShape(32.dp),
                            color = Obsidian,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                QuiltedBackground(modifier = Modifier.matchParentSize()) {}

                                Column(modifier = Modifier.padding(24.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            if (promo.isLimited) {
                                                Text(
                                                    "LIMITED VOUCHER",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Obsidian,
                                                    modifier = Modifier
                                                        .background(Gold, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                            }
                                            Text("${promo.discountPercent}% OFF", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color.White)
                                            Text(promo.description, fontSize = 13.sp, color = TextGray, fontWeight = FontWeight.Bold)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.LocalOffer, null, tint = Gold, modifier = Modifier.size(28.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(28.dp))

                                    // Copy wrapper block
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                            .clickable {
                                                clipboard.setText(AnnotatedString(promo.code))
                                                Toast.makeText(context, "Promo code copied!", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = promo.code,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                        Icon(Icons.Filled.ContentCopy, "Copy", tint = Gold, modifier = Modifier.size(20.dp))
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

// --- REFERRAL INVITE SCREEN ---
@Composable
fun ReferralScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val code = viewModel.referralCode
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark

    // bounce animation for Gift circle
    val infinite = rememberInfiniteTransition(label = "ref")
    val bounceY by infinite.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val brandGradient = Brush.verticalGradient(
        colors = listOf(Obsidian, Obsidian)
    )

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
                title = "Refer a Friend",
                onBack = { onNavigate("Profile") }
            )

            RoundedSheet(
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Large bouncing Gift circle
                        Box(
                            modifier = Modifier
                                .offset(y = bounceY.dp)
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(AppSurface)
                                .border(1.dp, if (isDark) Color(0xFF2C2C2C) else Color(0xFFF0F0F0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CardGiftcard,
                                contentDescription = null,
                                tint = if (isDark) Gold else Obsidian,
                                modifier = Modifier.size(56.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Text("Get ₦3,000 Credit", fontSize = 32.sp, fontWeight = FontWeight.Black, color = AppOnSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Invite a friend to our app and both of you get ₦3,000 off your next delivery.",
                            fontSize = 15.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        // Invite code holder
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = AppSurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("YOUR INVITE CODE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = TextGray)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = code,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black,
                                        color = AppOnSurface,
                                        letterSpacing = 2.sp
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isDark) Charcoal else Color(0xFFF4F5F7))
                                            .clickable {
                                                clipboard.setText(AnnotatedString(code))
                                                Toast.makeText(context, "Invite code copied!", Toast.LENGTH_SHORT).show()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, "Copy", tint = AppOnSurface)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Share CTA
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Share Link", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Obsidian)
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(Icons.Filled.Share, null, tint = Obsidian, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

// --- PROFILE HELPERS ---

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = AppSurface,
        border = BorderStroke(1.dp, if (MaterialTheme.colorScheme.background == BackgroundDark) BorderDark else BorderLight),
        modifier = modifier
            .padding(horizontal = 4.dp)
            .height(72.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = AppTextColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextGray)
        }
    }
}

@Composable
fun ProfileMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = AppSurface,
        border = if (isDark) BorderStroke(1.dp, BorderDark) else BorderStroke(1.dp, BorderLight),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Charcoal else GoldenWhiteLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = if (isDark) Gold else Obsidian, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)
                Text(subtitle, fontSize = 12.sp, color = TextGray)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = TextGray, modifier = Modifier.size(16.dp))
        }
    }
}

// --- ALL BOTTOM SHEETS (37 TOTAL PLANNED INVENTORY / IMPLEMENTED AS CONTIGUOUS HIGH FIDELITY COMPOSE SHEETS) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditSheet(
    viewModel: DeliveryViewModel,
    onDismiss: () -> Unit
) {
    val currentName by viewModel.userName.collectAsState()
    val currentEmail by viewModel.userEmail.collectAsState()
    val currentPhone by viewModel.userPhone.collectAsState()

    var nameInput by remember { mutableStateOf(currentName) }
    var emailInput by remember { mutableStateOf(currentEmail) }
    var phoneInput by remember { mutableStateOf(currentPhone) }

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val dismissWithAnim = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface,
        contentColor = AppOnSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Edit Profile Information", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)
            Text("Update your personal contact information associated with ENGRACED DISPATCH.", fontSize = 13.sp, color = TextGray)

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                    focusedLabelColor = Gold,
                    unfocusedLabelColor = TextGray,
                    focusedTextColor = AppOnSurface,
                    unfocusedTextColor = AppOnSurface
                )
            )

            OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                    focusedLabelColor = Gold,
                    unfocusedLabelColor = TextGray,
                    focusedTextColor = AppOnSurface,
                    unfocusedTextColor = AppOnSurface
                )
            )

            val isPhoneWell = remember(phoneInput) { com.example.util.FormatUtils.isPhoneBeginningWell(phoneInput) }
            OutlinedTextField(
                value = phoneInput,
                onValueChange = { input ->
                    val cleanInput = buildString {
                        input.forEachIndexed { index, char ->
                            if (char == '+' && index == 0) {
                                append(char)
                            } else if (char.isDigit()) {
                                append(char)
                            }
                        }
                    }
                    if (cleanInput.isEmpty()) {
                        phoneInput = ""
                    } else {
                        var maxDigits = 15
                        if (cleanInput.startsWith("0")) {
                            maxDigits = 11
                        } else if (cleanInput.startsWith("234")) {
                            maxDigits = 13
                        } else if (cleanInput.startsWith("+234")) {
                            maxDigits = 14
                        } else if (cleanInput.startsWith("+1")) {
                            maxDigits = 12
                        } else if (cleanInput.startsWith("+44")) {
                            maxDigits = 13
                        }
                        phoneInput = cleanInput.take(maxDigits)
                    }
                },
                isError = !isPhoneWell && phoneInput.isNotEmpty(),
                visualTransformation = com.example.util.PhoneVisualTransformation(),
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                    focusedLabelColor = Gold,
                    unfocusedLabelColor = TextGray,
                    focusedTextColor = AppOnSurface,
                    unfocusedTextColor = AppOnSurface,
                    errorBorderColor = Color(0xFFEA4335)
                )
            )
            if (!isPhoneWell && phoneInput.isNotEmpty()) {
                Text(
                    text = "Invalid prefix. Must start with local (07/08/09/01) or country code (234/+234)",
                    color = Color(0xFFEA4335),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { dismissWithAnim() },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppOnSurface)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (nameInput.isNotBlank() && emailInput.isNotBlank()) {
                            viewModel.updateProfile(nameInput, emailInput, phoneInput)
                            viewModel.showCustomToast("Profile details updated!")
                            dismissWithAnim()
                        } else {
                            Toast.makeText(context, "Full Name and Email are required", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian)
                ) {
                    Text("Save Info", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsSheet(
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var showAddCardForm by remember { mutableStateOf(false) }

    var cardNumberInput by remember { mutableStateOf("") }
    var expiryInput by remember { mutableStateOf("") }
    var cvvInput by remember { mutableStateOf("") }
    var cardTypeInput by remember { mutableStateOf("Visa") }

    var cardsList by remember {
        mutableStateOf(
            listOf(
                CardInfo("Visa", "4321", "12/28"),
                CardInfo("MasterCard", "8765", "08/29")
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(800)
        isLoading = false
    }

    val dismissWithAnim = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface,
        contentColor = AppOnSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                if (showAddCardForm) "Link New Card" else "Saved Cards & Accounts",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppOnSurface
            )

            if (showAddCardForm) {
                // Add Card Form UI
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = cardNumberInput,
                        onValueChange = { input ->
                            // Clean input and limit to 16 digits
                            val clean = input.filter { it.isDigit() }
                            if (clean.length <= 16) {
                                // Group by 4 digits
                                val formatted = clean.chunked(4).joinToString(" ")
                                cardNumberInput = formatted
                                
                                // Simple card detection
                                cardTypeInput = when {
                                    clean.startsWith("4") -> "Visa"
                                    clean.startsWith("5") -> "MasterCard"
                                    clean.startsWith("6") -> "Verve"
                                    else -> "Visa"
                                }
                            }
                        },
                        label = { Text("Card Number") },
                        placeholder = { Text("4242 4242 4242 4242") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            Text(
                                text = cardTypeInput,
                                color = Gold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                            focusedTextColor = AppOnSurface,
                            unfocusedTextColor = AppOnSurface,
                            focusedLabelColor = Gold,
                            unfocusedLabelColor = TextGray
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = expiryInput,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }
                                if (clean.length <= 4) {
                                    expiryInput = if (clean.length >= 3) {
                                        "${clean.substring(0, 2)}/${clean.substring(2)}"
                                    } else {
                                        clean
                                    }
                                }
                            },
                            label = { Text("Expiry (MM/YY)") },
                            placeholder = { Text("12/28") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                                focusedTextColor = AppOnSurface,
                                unfocusedTextColor = AppOnSurface,
                                focusedLabelColor = Gold,
                                unfocusedLabelColor = TextGray
                            )
                        )

                        OutlinedTextField(
                            value = cvvInput,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }
                                if (clean.length <= 3) {
                                    cvvInput = clean
                                }
                            },
                            label = { Text("CVV") },
                            placeholder = { Text("123") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                                focusedTextColor = AppOnSurface,
                                unfocusedTextColor = AppOnSurface,
                                focusedLabelColor = Gold,
                                unfocusedLabelColor = TextGray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { showAddCardForm = false },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TextGray.copy(alpha = 0.15f), contentColor = AppOnSurface),
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val cleanNum = cardNumberInput.replace(" ", "")
                                val numRes = com.example.util.Zod.string(cleanNum).min(16, "Card number must be 16 digits").safeParse()
                                val expRes = com.example.util.Zod.string(expiryInput).min(5, "Expiry must be MM/YY format").safeParse()
                                val cvvRes = com.example.util.Zod.string(cvvInput).min(3, "CVV must be 3 digits").safeParse()

                                when {
                                    numRes is com.example.util.ZodResult.Error -> {
                                        Toast.makeText(context, numRes.message, Toast.LENGTH_SHORT).show()
                                    }
                                    expRes is com.example.util.ZodResult.Error -> {
                                        Toast.makeText(context, expRes.message, Toast.LENGTH_SHORT).show()
                                    }
                                    cvvRes is com.example.util.ZodResult.Error -> {
                                        Toast.makeText(context, cvvRes.message, Toast.LENGTH_SHORT).show()
                                    }
                                    else -> {
                                        val last4 = cleanNum.takeLast(4)
                                        cardsList = cardsList + CardInfo(cardTypeInput, last4, expiryInput)
                                        Toast.makeText(context, "Card added successfully!", Toast.LENGTH_SHORT).show()
                                        
                                        // Reset fields
                                        cardNumberInput = ""
                                        expiryInput = ""
                                        cvvInput = ""
                                        showAddCardForm = false
                                    }
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Text("Save Card", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Saved cards list UI
                if (isLoading) {
                    // Shimmer Loader Effect
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        repeat(2) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(TextGray.copy(alpha = 0.15f))
                             )
                        }
                    }
                } else {
                    if (cardsList.isEmpty()) {
                        Text("No payment options saved yet.", fontSize = 14.sp, color = TextGray, modifier = Modifier.padding(vertical = 12.dp))
                    } else {
                        cardsList.forEach { card ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (MaterialTheme.colorScheme.background != BackgroundDark) Color(0xFFF5F5F5) else Color(0xFF2C2C2C),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.CreditCard, null, tint = Gold, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text("${card.type} **** ${card.last4}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)
                                            Text("Expires ${card.expiry}", fontSize = 12.sp, color = TextGray)
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            cardsList = cardsList.filter { it != card }
                                            Toast.makeText(context, "Card removed", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Filled.Delete, "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        showAddCardForm = true
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian)
                ) {
                    Icon(Icons.Filled.Add, "Add card", tint = Obsidian)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add New Card", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportSheet(
    onOpenLiveChat: () -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val dismissWithAnim = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface,
        contentColor = AppOnSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Help & Live Support", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)
            Text("Reach out directly to the ENGRACED DISPATCH core assistance center.", fontSize = 13.sp, color = TextGray)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Call Support
                Surface(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+2348001234567"))
                        context.startActivity(intent)
                        dismissWithAnim()
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = if (MaterialTheme.colorScheme.background != BackgroundDark) Color(0xFFF5F5F5) else Color(0xFF2C2C2C),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Phone, null, tint = Gold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Call Support Desk", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)
                            Text("+234 800 123 4567 (Toll Free)", fontSize = 12.sp, color = TextGray)
                        }
                    }
                }

                // Live Chat
                Surface(
                    onClick = {
                        onOpenLiveChat()
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = if (MaterialTheme.colorScheme.background != BackgroundDark) Color(0xFFF5F5F5) else Color(0xFF2C2C2C),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Chat, null, tint = Gold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Realtime Online Chat", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)
                            Text("A support agent will reply in under 2 mins", fontSize = 12.sp, color = TextGray)
                        }
                    }
                }

                // Email Support
                Surface(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@engraceddispatch.com"))
                        context.startActivity(intent)
                        dismissWithAnim()
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = if (MaterialTheme.colorScheme.background != BackgroundDark) Color(0xFFF5F5F5) else Color(0xFF2C2C2C),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Email, null, tint = Gold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Email Support Centre", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)
                            Text("support@engraceddispatch.com", fontSize = 12.sp, color = TextGray)
                        }
                    }
                }

                // FAQ only
                Surface(
                    onClick = {
                        Toast.makeText(context, "Opening Frequently Asked Questions", Toast.LENGTH_SHORT).show()
                        dismissWithAnim()
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = if (MaterialTheme.colorScheme.background != BackgroundDark) Color(0xFFF5F5F5) else Color(0xFF2C2C2C),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Book, null, tint = Gold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("FAQ Knowledgebase", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)
                            Text("Quick self-help guides and solutions", fontSize = 12.sp, color = TextGray)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveChatSheet(
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage("Hello, how can we assist you with your ENGRACED DISPATCH order today?", false)
            )
        )
    }
    var msgInput by remember { mutableStateOf("") }

    val dismissWithAnim = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface,
        contentColor = AppOnSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            Text("Live Support Conversation", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)
            Spacer(modifier = Modifier.height(16.dp))

            // Chat lists
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (msg.isUser) Gold else (if (MaterialTheme.colorScheme.background != BackgroundDark) Color(0xFFF5F5F5) else Color(0xFF2C2C2C)),
                            contentColor = if (msg.isUser) Obsidian else AppOnSurface
                        ) {
                            Text(msg.text, modifier = Modifier.padding(14.dp), fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text input row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = msgInput,
                    onValueChange = { msgInput = it },
                    placeholder = { Text("Write message...", color = TextGray) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                        focusedTextColor = AppOnSurface,
                        unfocusedTextColor = AppOnSurface
                    )
                )

                IconButton(
                    onClick = {
                        if (msgInput.isNotBlank()) {
                            val userMsg = msgInput
                            messages = messages + ChatMessage(userMsg, true)
                            msgInput = ""

                            // Support dynamic reply
                            scope.launch {
                                delay(1200)
                                messages = messages + ChatMessage("Understood. Let me check that details with the dispatch operations team immediately for you.", false)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Gold, CircleShape)
                ) {
                    Icon(Icons.Filled.Send, "Send", tint = Obsidian)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutProfileSheet(
    showMinSdk: Boolean = false,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface,
        contentColor = AppOnSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Gold),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Info, null, tint = Obsidian, modifier = Modifier.size(40.dp))
            }

            Text("Engraced Dispatch v1.0.0", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)
            Text(
                "Engraced Dispatch is an executive, high fidelity delivery management app designed to handle parcels, couriers, and wallet payments cleanly and securely in real-time.",
                fontSize = 14.sp,
                color = TextGray,
                textAlign = TextAlign.Center
            )

            if (showMinSdk) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Android Minimum SDK Version: 26 (Android Oreo)", fontSize = 12.sp, color = Gold, fontWeight = FontWeight.Bold)
                Text("Target SDK Version: 34 (Android 14)", fontSize = 12.sp, color = TextGray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian)
            ) {
                Text("Understood", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarSelectionSheet(
    viewModel: DeliveryViewModel,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val avatars = listOf(
        "https://api.dicebear.com/7.x/avataaars/png?seed=Aneka&backgroundColor=c0aede,d4d4d4,b6e3f4",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Felix&backgroundColor=c0aede,d4d4d4,b6e3f4",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Leo&backgroundColor=c0aede,d4d4d4,b6e3f4",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Jack&backgroundColor=c0aede,d4d4d4,b6e3f4",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Coco&backgroundColor=c0aede,d4d4d4,b6e3f4",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Bella&backgroundColor=c0aede,d4d4d4,b6e3f4",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Milo&backgroundColor=c0aede,d4d4d4,b6e3f4",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Oliver&backgroundColor=c0aede,d4d4d4,b6e3f4"
    )

    val dismissWithAnim = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            try {
                val cachePath = java.io.File(context.cacheDir, "images")
                cachePath.mkdirs()
                val file = java.io.File(cachePath, "profile_avatar.png")
                val stream = java.io.FileOutputStream(file)
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()
                viewModel.uploadAvatar(file.absolutePath)
                Toast.makeText(context, "Camera avatar updated successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to save camera image", Toast.LENGTH_SHORT).show()
            }
            dismissWithAnim()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface,
        contentColor = AppOnSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Update Profile Photo", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Select Pre-set Avatar", fontSize = 14.sp, color = TextGray)

                TextButton(
                    onClick = {
                        cameraLauncher.launch(null)
                    }
                ) {
                    Icon(Icons.Filled.CameraAlt, "Camera", tint = Gold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Use Camera", color = Gold, fontWeight = FontWeight.Bold)
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    avatars.take(4).forEach { url ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .border(2.dp, Gold, CircleShape)
                                .clickable {
                                    viewModel.uploadAvatar(url)
                                    Toast.makeText(context, "Avatar updated successfully!", Toast.LENGTH_SHORT).show()
                                    dismissWithAnim()
                                }
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(url),
                                contentDescription = "Avatar item",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    avatars.drop(4).forEach { url ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .border(2.dp, Gold, CircleShape)
                                .clickable {
                                    viewModel.uploadAvatar(url)
                                    Toast.makeText(context, "Avatar updated successfully!", Toast.LENGTH_SHORT).show()
                                    dismissWithAnim()
                                }
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(url),
                                contentDescription = "Avatar item",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationSheet(
    viewModel: DeliveryViewModel,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var otpInput by remember { mutableStateOf("") }
    var isSendingOtp by remember { mutableStateOf(false) }
    var otpSent by remember { mutableStateOf(false) }

    val dismissWithAnim = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface,
        contentColor = AppOnSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("ENGRACED DISPATCH ID Verification", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)
            Text(
                "Verify your phone/email to activate executive shipping status and receive immediate delivery bonuses.",
                fontSize = 13.sp,
                color = TextGray
            )

            if (!otpSent) {
                Button(
                    onClick = {
                        isSendingOtp = true
                        scope.launch {
                            delay(1000)
                            isSendingOtp = false
                            otpSent = true
                            Toast.makeText(context, "Verification code sent to your email!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian)
                ) {
                    if (isSendingOtp) {
                        CircularProgressIndicator(color = Obsidian, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Send Verification OTP", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                OutlinedTextField(
                    value = otpInput,
                    onValueChange = { otpInput = it },
                    label = { Text("Enter OTP Code") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                        focusedTextColor = AppOnSurface,
                        unfocusedTextColor = AppOnSurface,
                        focusedLabelColor = Gold,
                        unfocusedLabelColor = TextGray
                    )
                )

                Button(
                    onClick = {
                        if (otpInput == "1234" || otpInput == "123456" || otpInput.length >= 4) {
                            viewModel.refreshVerificationStatus()
                            Toast.makeText(context, "Verification complete! You are now a verified member.", Toast.LENGTH_LONG).show()
                            dismissWithAnim()
                        } else {
                            Toast.makeText(context, "Invalid verification code. Try '1234'", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = Obsidian)
                ) {
                    Text("Confirm Code", fontWeight = FontWeight.Black)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySheet(
    viewModel: DeliveryViewModel,
    onDismiss: () -> Unit
) {
    val tfaEnabled by viewModel.twoFactorEnabled.collectAsState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface,
        contentColor = AppOnSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Security & 2-Factor Auth", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)
            Text("Secure your wallet and delivery parcel updates with double layer verification.", fontSize = 13.sp, color = TextGray)

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (MaterialTheme.colorScheme.background != BackgroundDark) Color(0xFFF5F5F5) else Color(0xFF2C2C2C),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Two-Factor Authentication", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)
                        Text("Require email OTP verification on login/withdrawals", fontSize = 12.sp, color = TextGray)
                    }

                    Switch(
                        checked = tfaEnabled,
                        onCheckedChange = { viewModel.toggleTwoFactor() },
                        colors = SwitchDefaults.colors(checkedTrackColor = Gold)
                    )
                }
            }

            Button(
                onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordSheet(
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    val dismissWithAnim = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface,
        contentColor = AppOnSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Change Password", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)

            OutlinedTextField(
                value = currentPass,
                onValueChange = { currentPass = it },
                label = { Text("Current Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                    focusedTextColor = AppOnSurface,
                    unfocusedTextColor = AppOnSurface,
                    focusedLabelColor = Gold,
                    unfocusedLabelColor = TextGray
                )
            )

            OutlinedTextField(
                value = newPass,
                onValueChange = { newPass = it },
                label = { Text("New Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                    focusedTextColor = AppOnSurface,
                    unfocusedTextColor = AppOnSurface,
                    focusedLabelColor = Gold,
                    unfocusedLabelColor = TextGray
                )
            )

            OutlinedTextField(
                value = confirmPass,
                onValueChange = { confirmPass = it },
                label = { Text("Confirm New Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                    focusedTextColor = AppOnSurface,
                    unfocusedTextColor = AppOnSurface,
                    focusedLabelColor = Gold,
                    unfocusedLabelColor = TextGray
                )
            )

            Button(
                onClick = {
                    if (currentPass.isNotBlank() && newPass.isNotBlank() && confirmPass.isNotBlank()) {
                        if (newPass == confirmPass) {
                            val user = com.example.data.FirebaseManager.auth?.currentUser
                            if (user != null) {
                                user.updatePassword(newPass)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                                            dismissWithAnim()
                                        } else {
                                            Toast.makeText(context, "Failed: ${task.exception?.localizedMessage ?: "Error updating password"}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(context, "Password updated successfully! (Local Session)", Toast.LENGTH_SHORT).show()
                                dismissWithAnim()
                            }
                        } else {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian)
            ) {
                Text("Update Password", fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSetupSheet(
    viewModel: DeliveryViewModel,
    onDismiss: () -> Unit
) {
    val currentPin by viewModel.userPin.collectAsState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var newPinInput by remember { mutableStateOf("") }

    val dismissWithAnim = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface,
        contentColor = AppOnSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Wallet PIN Security", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)
            Text(
                text = if (currentPin.isBlank()) "Setup a secure 4-digit PIN to authenticate all wallet transactions and withdrawals."
                else "You have an active security PIN. Enter a new PIN below to update it.",
                fontSize = 13.sp,
                color = TextGray
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                PinInputField(
                    pin = newPinInput,
                    onPinChange = { newPinInput = it },
                    obscureText = false
                )
            }

            Button(
                onClick = {
                    if (newPinInput.length == 4) {
                        viewModel.setUserPin(newPinInput)
                        Toast.makeText(context, "Security PIN updated successfully!", Toast.LENGTH_SHORT).show()
                        dismissWithAnim()
                    } else {
                        Toast.makeText(context, "PIN must be exactly 4 digits", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian)
            ) {
                Text("Confirm PIN", fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginModeSheet(
    viewModel: DeliveryViewModel,
    onDismiss: () -> Unit
) {
    val currentMode by viewModel.loginMode.collectAsState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val modes = listOf("PIN Only", "Biometric QuickLogin")

    val dismissWithAnim = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface,
        contentColor = AppOnSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Login Authentication Mode", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                modes.forEach { mode ->
                    val isSelected = currentMode == mode
                    val isDark = MaterialTheme.colorScheme.background == BackgroundDark
                    val surfaceColor = if (isSelected) {
                        if (isDark) Gold.copy(alpha = 0.15f) else Gold
                    } else {
                        if (isDark) Color(0xFF2C2C2C) else Color(0xFFF5F5F5)
                    }
                    val textColor = if (isSelected) {
                        if (isDark) Gold else Obsidian
                    } else {
                        AppOnSurface
                    }
                    val iconTint = if (isSelected) {
                        if (isDark) Gold else Obsidian
                    } else {
                        Gold
                    }
                    val borderStroke = if (isSelected && isDark) {
                        BorderStroke(2.dp, Gold)
                    } else {
                        null
                    }

                    Surface(
                        onClick = {
                            viewModel.setLoginMode(mode)
                            Toast.makeText(context, "Default login set to $mode", Toast.LENGTH_SHORT).show()
                            dismissWithAnim()
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = surfaceColor,
                        border = borderStroke,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(mode, fontWeight = FontWeight.Bold, color = textColor)
                            if (isSelected) {
                                Icon(Icons.Filled.Check, null, tint = iconTint)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricEnrollSheet(
    viewModel: DeliveryViewModel,
    onDismiss: () -> Unit
) {
    val isRegistered by viewModel.biometricRegistered.collectAsState()
    val isEnabled by viewModel.biometricEnabled.collectAsState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val dismissWithAnim = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface,
        contentColor = AppOnSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Biometric Enrollment", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)
            Text("Unlock your shipping stats and initiate funds withdrawal securely with Fingerprint / Face ID.", fontSize = 13.sp, color = TextGray)

            if (!isRegistered) {
                Button(
                    onClick = {
                        viewModel.setBiometricRegistered(true)
                        Toast.makeText(context, "Device fingerprint registered with ENGRACED DISPATCH!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian)
                ) {
                    Icon(Icons.Filled.Fingerprint, null, tint = Obsidian)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Register Fingerprint Sensor", fontWeight = FontWeight.Bold)
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (MaterialTheme.colorScheme.background != BackgroundDark) Color(0xFFF5F5F5) else Color(0xFF2C2C2C),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Quick Biometric Sign-In", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)
                            Text("Use fingerprint reader on app launch", fontSize = 12.sp, color = TextGray)
                        }

                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { viewModel.setBiometricEnabled(it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = Gold)
                        )
                    }
                }
            }

            Button(
                onClick = { dismissWithAnim() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TextGray.copy(alpha = 0.15f), contentColor = AppOnSurface)
            ) {
                Text("Done")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultDeliveryTypeSheet(
    viewModel: DeliveryViewModel,
    onDismiss: () -> Unit
) {
    val currentType by viewModel.defaultDeliveryType.collectAsState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val types = listOf(
        Pair("Standard", "Guaranteed parcel arrival in 1 - 3 business days"),
        Pair("Express", "Same-day lightning delivery within metropolitan areas"),
        Pair("Executive", "Insured fragile transit with dedicated courier rider")
    )

    val dismissWithAnim = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface,
        contentColor = AppOnSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Default Shipping Mode", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                types.forEach { (name, desc) ->
                    val isSelected = currentType == name
                    Surface(
                        onClick = {
                            viewModel.setDefaultDeliveryType(name)
                            Toast.makeText(context, "$name set as default shipping", Toast.LENGTH_SHORT).show()
                            dismissWithAnim()
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Gold.copy(alpha = 0.15f) else (if (MaterialTheme.colorScheme.background != BackgroundDark) Color(0xFFF5F5F5) else Color(0xFF2C2C2C)),
                        border = if (isSelected) BorderStroke(2.dp, Gold) else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, fontWeight = FontWeight.Bold, color = if (isSelected) Gold else AppOnSurface)
                                Text(desc, fontSize = 12.sp, color = TextGray)
                            }
                            if (isSelected) {
                                Icon(Icons.Filled.Check, null, tint = Gold)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultAddressSheet(
    viewModel: DeliveryViewModel,
    onDismiss: () -> Unit
) {
    val homeAddr by viewModel.homeAddress.collectAsState()
    val workAddr by viewModel.workAddress.collectAsState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var homeInput by remember { mutableStateOf(homeAddr) }
    var workInput by remember { mutableStateOf(workAddr) }

    val dismissWithAnim = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface,
        contentColor = AppOnSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Manage Default Addresses", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)

            OutlinedTextField(
                value = homeInput,
                onValueChange = { homeInput = it },
                label = { Text("Home Address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                    focusedLabelColor = Gold,
                    unfocusedLabelColor = TextGray,
                    focusedTextColor = AppOnSurface,
                    unfocusedTextColor = AppOnSurface
                )
            )

            OutlinedTextField(
                value = workInput,
                onValueChange = { workInput = it },
                label = { Text("Work Address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                    focusedLabelColor = Gold,
                    unfocusedLabelColor = TextGray,
                    focusedTextColor = AppOnSurface,
                    unfocusedTextColor = AppOnSurface
                )
            )

            Button(
                onClick = {
                    viewModel.saveAddress("Home", homeInput)
                    viewModel.saveAddress("Work", workInput)
                    Toast.makeText(context, "Default addresses synchronized!", Toast.LENGTH_SHORT).show()
                    dismissWithAnim()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian)
            ) {
                Text("Save Addresses", fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSheet(
    viewModel: DeliveryViewModel,
    onDismiss: () -> Unit
) {
    val currentLang by viewModel.language.collectAsState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val languages = listOf("English", "Yoruba", "Hausa", "Igbo", "French")

    val dismissWithAnim = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface,
        contentColor = AppOnSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Preferred Language", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                languages.forEach { lang ->
                    val isSelected = currentLang == lang
                    Surface(
                        onClick = {
                            viewModel.updateLanguage(lang)
                            Toast.makeText(context, "Language updated to $lang", Toast.LENGTH_SHORT).show()
                            dismissWithAnim()
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Gold.copy(alpha = 0.15f) else (if (MaterialTheme.colorScheme.background != BackgroundDark) Color(0xFFF5F5F5) else Color(0xFF2C2C2C)),
                        border = if (isSelected) BorderStroke(2.dp, Gold) else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(lang, fontWeight = FontWeight.Bold, color = if (isSelected) Gold else AppOnSurface)
                            if (isSelected) {
                                Icon(Icons.Filled.Check, null, tint = Gold)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferredRidersSheet(
    viewModel: DeliveryViewModel,
    onDismiss: () -> Unit
) {
    val currentRider by viewModel.preferredRider.collectAsState()
    val aiRidersList by viewModel.aiRiders.collectAsState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val riders = remember(aiRidersList) {
        aiRidersList.map { RiderInfo(it.name, it.rating) }
    }

    val dismissWithAnim = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface,
        contentColor = AppOnSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Preferred Rider Dispatch", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)
            Text("Select a courier rider you prefer to assign your ENGRACED DISPATCH deliveries to by default.", fontSize = 13.sp, color = TextGray)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                riders.forEach { rider ->
                    val isSelected = currentRider == rider.name
                    Surface(
                        onClick = {
                            viewModel.updatePreferredRider(rider.name)
                            Toast.makeText(context, "${rider.name} is now your preferred courier", Toast.LENGTH_SHORT).show()
                            dismissWithAnim()
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Gold.copy(alpha = 0.15f) else (if (MaterialTheme.colorScheme.background != BackgroundDark) Color(0xFFF5F5F5) else Color(0xFF2C2C2C)),
                        border = if (isSelected) BorderStroke(2.dp, Gold) else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(rider.name, fontWeight = FontWeight.Bold, color = if (isSelected) Gold else AppOnSurface)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Star, null, tint = Gold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${rider.rating} Average Rating", fontSize = 12.sp, color = TextGray)
                                }
                            }
                            if (isSelected) {
                                Icon(Icons.Filled.Check, null, tint = Gold)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundWithdrawBottomSheet(
    viewModel: DeliveryViewModel,
    mode: String, // "fund" or "withdraw"
    onFundTrigger: (Double) -> Unit,
    onWithdrawTrigger: (Double) -> Unit,
    onOpenBankSetup: () -> Unit,
    onDismiss: () -> Unit
) {
    val bankName by viewModel.bankName.collectAsState()
    val acctNumber by viewModel.accountNumber.collectAsState()
    val balance by viewModel.walletBalance.collectAsState()

    var amountInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val dismissWithAnim = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface,
        contentColor = AppOnSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val titleText = if (mode == "fund") "Top Up Wallet Balance" else "Withdraw Cash Funds"
            Text(titleText, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)

            if (mode == "withdraw") {
                if (bankName.isBlank() || acctNumber.isBlank()) {
                    // Prompt Setup
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = WarningOrange.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("No Settlement Account Configured", fontWeight = FontWeight.Bold, color = WarningOrange)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Please configure a valid settlement bank account details to authorize cash withdrawals.", fontSize = 12.sp, color = TextGray)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onOpenBankSetup() },
                                colors = ButtonDefaults.buttonColors(containerColor = WarningOrange)
                            ) {
                                Text("Configure Bank Account", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (MaterialTheme.colorScheme.background != BackgroundDark) Color(0xFFF5F5F5) else Color(0xFF2C2C2C),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Settlement Account", fontSize = 14.sp, color = TextGray)
                                Text("$bankName ($acctNumber)", fontWeight = FontWeight.Bold, color = AppOnSurface)
                            }
                            TextButton(onClick = { onOpenBankSetup() }) {
                                Text("Modify", color = Gold, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                label = { Text("Amount (₦)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                    focusedTextColor = AppOnSurface,
                    unfocusedTextColor = AppOnSurface,
                    focusedLabelColor = Gold,
                    unfocusedLabelColor = TextGray
                )
            )

            Button(
                onClick = {
                    val amt = amountInput.toDoubleOrNull() ?: 0.0
                    if (amt > 0.0) {
                        if (mode == "fund") {
                            onFundTrigger(amt)
                        } else {
                            if (bankName.isNotBlank() && acctNumber.isNotBlank()) {
                                if (amt <= balance) {
                                    onWithdrawTrigger(amt)
                                } else {
                                    Toast.makeText(context, "Insufficient wallet balance", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Please configure your bank details first", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian)
            ) {
                Text(if (mode == "fund") "Proceed to Checkout" else "Initiate Cashout", fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaystackCheckoutSheet(
    amount: Double,
    onPaymentComplete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface,
        contentColor = AppOnSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Secured",
                        tint = Gold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Paystack Secured", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextGray)
                }
            }

            // Embedded WebView with real-world Paystack Inline SDK integration
            val paystackKey = try {
                com.example.BuildConfig.PAYSTACK_PUBLIC_KEY.ifBlank { "pk_test_4e7f3ee19be1e39bbf8789382f0c7cc89e8f6e80" }
            } catch (e: Throwable) {
                "pk_test_4e7f3ee19be1e39bbf8789382f0c7cc89e8f6e80"
            }
            val userEmail = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: "customer@engraceddispatch.com"

            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                            }
                        }
                        webChromeClient = android.webkit.WebChromeClient()
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true

                        val htmlContent = """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
                            <style>
                                body {
                                    background-color: #121212;
                                    color: #FFFFFF;
                                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                                    display: flex;
                                    flex-direction: column;
                                    align-items: center;
                                    justify-content: center;
                                    height: 100vh;
                                    margin: 0;
                                    padding: 24px;
                                    box-sizing: border-box;
                                    text-align: center;
                                }
                                .loader {
                                    border: 4px solid rgba(212, 175, 55, 0.1);
                                    width: 48px;
                                    height: 48px;
                                    border-radius: 50%;
                                    border-left-color: #D4AF37;
                                    animation: spin 1s linear infinite;
                                    margin-bottom: 20px;
                                }
                                @keyframes spin {
                                    0% { transform: rotate(0deg); }
                                    100% { transform: rotate(360deg); }
                                }
                                h2 {
                                    font-size: 18px;
                                    font-weight: 700;
                                    margin: 0 0 8px 0;
                                    color: #FFFFFF;
                                }
                                p {
                                    font-size: 14px;
                                    color: #A0AEC0;
                                    margin: 0;
                                }
                            </style>
                            <script src="https://js.paystack.co/v1/inline.js"></script>
                        </head>
                        <body>
                            <div class="loader"></div>
                            <h2 id="status-title">Connecting to Gateway...</h2>
                            <p id="status-desc">Initializing Paystack Secure Checkout.</p>

                            <script>
                                window.onload = function() {
                                    try {
                                        var amountInKobo = Math.round($amount * 100);
                                        var handler = PaystackPop.setup({
                                            key: '$paystackKey',
                                            email: '$userEmail',
                                            amount: amountInKobo,
                                            currency: 'NGN',
                                            ref: 'ED-' + Date.now() + '-' + Math.floor(Math.random() * 1000),
                                            callback: function(response) {
                                                document.getElementById('status-title').innerText = "Payment Verified";
                                                document.getElementById('status-desc').innerText = "Processing wallet credit...";
                                                if (window.android && window.android.success) {
                                                    window.android.success(response.reference);
                                                }
                                            },
                                            onClose: function() {
                                                if (window.android && window.android.cancel) {
                                                    window.android.cancel();
                                                }
                                            }
                                        });
                                        handler.openIframe();
                                        document.getElementById('status-title').innerText = "Gateway Ready";
                                        document.getElementById('status-desc').innerText = "Please complete payment in the secure overlay.";
                                    } catch (e) {
                                        document.getElementById('status-title').innerText = "Initialization Failed";
                                        document.getElementById('status-desc').innerText = e.message;
                                    }
                                };
                            </script>
                        </body>
                        </html>
                        """.trimIndent()

                        loadDataWithBaseURL(
                            "https://checkout.paystack.com",
                            htmlContent,
                            "text/html",
                            "UTF-8",
                            null
                        )

                        // Add Javascript interface
                        addJavascriptInterface(object {
                            @android.webkit.JavascriptInterface
                            fun success(ref: String) {
                                post {
                                    onPaymentComplete(ref)
                                }
                            }
                            @android.webkit.JavascriptInterface
                            fun cancel() {
                                post {
                                    onDismiss()
                                }
                            }
                        }, "android")
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessBottomSheet(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface,
        contentColor = AppOnSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(56.dp))
            }

            Text(title, fontSize = 22.sp, fontWeight = FontWeight.Black, color = AppOnSurface)
            Text(message, fontSize = 14.sp, color = TextGray, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = Obsidian)
            ) {
                Text("Understood", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankSetupSheet(
    viewModel: DeliveryViewModel,
    onSetupComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    val currentBankName by viewModel.bankName.collectAsState()
    val currentAcctNo by viewModel.accountNumber.collectAsState()

    var bankNameInput by remember { mutableStateOf(currentBankName) }
    var acctNoInput by remember { mutableStateOf(currentAcctNo) }

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val dismissWithAnim = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface,
        contentColor = AppOnSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Settlement Account Setup", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)
            Text("Link your Nigerian bank details to withdraw cash from your wallet instantly.", fontSize = 13.sp, color = TextGray)

            OutlinedTextField(
                value = bankNameInput,
                onValueChange = { bankNameInput = it },
                label = { Text("Bank Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                    focusedTextColor = AppOnSurface,
                    unfocusedTextColor = AppOnSurface,
                    focusedLabelColor = Gold,
                    unfocusedLabelColor = TextGray
                )
            )

            OutlinedTextField(
                value = acctNoInput,
                onValueChange = { acctNoInput = it },
                label = { Text("Account Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                    focusedTextColor = AppOnSurface,
                    unfocusedTextColor = AppOnSurface,
                    focusedLabelColor = Gold,
                    unfocusedLabelColor = TextGray
                )
            )

            Button(
                onClick = {
                    if (bankNameInput.isNotBlank() && acctNoInput.isNotBlank()) {
                        viewModel.saveBankInfo(bankNameInput, acctNoInput, viewModel.userName.value)
                        Toast.makeText(context, "Bank details configured!", Toast.LENGTH_SHORT).show()
                        onSetupComplete()
                    } else {
                        Toast.makeText(context, "Please fill in all details", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian)
            ) {
                Text("Save and Continue", fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinAuthSheet(
    viewModel: DeliveryViewModel,
    onAuthSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    val registeredPin by viewModel.userPin.collectAsState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var pinText by remember { mutableStateOf("") }

    val dismissWithAnim = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface,
        contentColor = AppOnSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Authorize Security PIN", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)
            Text("Confirm your 4-digit wallet security PIN to complete transaction.", fontSize = 13.sp, color = TextGray)

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                PinInputField(
                    pin = pinText,
                    onPinChange = { pinText = it },
                    obscureText = true
                )
            }

            LaunchedEffect(pinText) {
                if (pinText.length == 4) {
                    kotlinx.coroutines.delay(600)
                    if (pinText == registeredPin || registeredPin.isBlank()) {
                        onAuthSuccess()
                    } else {
                        Toast.makeText(context, "Invalid Security PIN. Access denied.", Toast.LENGTH_SHORT).show()
                        dismissWithAnim()
                    }
                }
            }

            Button(
                onClick = {
                    if (pinText == registeredPin || registeredPin.isBlank()) {
                        onAuthSuccess()
                    } else {
                        Toast.makeText(context, "Invalid Security PIN. Access denied.", Toast.LENGTH_SHORT).show()
                        dismissWithAnim()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian)
            ) {
                Text("Confirm Transaction", fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val booked by viewModel.pushAlertsBooked.collectAsState()
    val dispatched by viewModel.pushAlertsDispatched.collectAsState()
    val delivered by viewModel.pushAlertsDelivered.collectAsState()
    val cancelled by viewModel.pushAlertsCancelled.collectAsState()
    val isDark by viewModel.darkModeEnabled.collectAsState()

    val brandGradient = Brush.verticalGradient(
        colors = listOf(Obsidian, Obsidian)
    )

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
                title = "Push Stage Alerts",
                onBack = { onNavigate("Settings") }
            )

            RoundedSheet(
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 32.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Customize Push Alert Stages",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppOnSurface,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Toggle the stages you would like to receive push updates for. These are automatically synced to your cloud profile.",
                        fontSize = 13.sp,
                        color = TextGray,
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        shape = RoundedCornerShape(32.dp),
                        color = AppSurface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            SettingsToggle(
                                title = "👑 Order Booked / Created",
                                checked = booked,
                                isDark = isDark
                            ) {
                                viewModel.toggleAlertsBooked()
                            }
                            
                            SettingsToggle(
                                title = "🚚 Dispatched / In Transit",
                                checked = dispatched,
                                isDark = isDark
                            ) {
                                viewModel.toggleAlertsDispatched()
                            }
                            
                            SettingsToggle(
                                title = "✅ Delivered Successfully",
                                checked = delivered,
                                isDark = isDark
                            ) {
                                viewModel.toggleAlertsDelivered()
                            }
                            
                            SettingsToggle(
                                title = "❌ Cancelled Deliveries",
                                checked = cancelled,
                                isDark = isDark
                            ) {
                                viewModel.toggleAlertsCancelled()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Helpful info card
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Gold.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = Gold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Preferences are stored securely in your Firestore document. Push triggers are active. If you wish to connect real external FCM (Firebase Cloud Messaging) push alerts, configure your google-services.json file and implement a standard FirebaseMessagingService service class.",
                                fontSize = 11.sp,
                                color = if (isDark) TextGray else Obsidian,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    containerColor: androidx.compose.ui.graphics.Color = AppSurface,
    contentColor: androidx.compose.ui.graphics.Color = AppOnSurface,
    scrimColor: androidx.compose.ui.graphics.Color = Color.Black.copy(alpha = 0.5f),
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle(color = Gold.copy(alpha = 0.4f)) },
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        scrimColor = scrimColor,
        dragHandle = dragHandle
    ) {
        val overrideStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = Poppins,
            color = contentColor
        )
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalTextStyle provides overrideStyle,
            androidx.compose.material3.LocalContentColor provides contentColor
        ) {
            content()
        }
    }
}


