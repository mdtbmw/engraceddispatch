package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.DeliveryViewModel
import com.example.ui.navigation.AppView
import com.example.ui.theme.*
import com.example.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: DeliveryViewModel) {
    val user by viewModel.currentUser.collectAsState()
    var showEditSheet by remember { mutableStateOf(false) }
    var showPaymentSheet by remember { mutableStateOf(false) }
    var showHelpSheet by remember { mutableStateOf(false) }
    var showAboutSheet by remember { mutableStateOf(false) }
    val deliveries by viewModel.allDeliveries.collectAsState()
    val deliveryCount = if (user.totalDeliveries > 0) user.totalDeliveries else deliveries.size

    Box(Modifier.fillMaxSize().background(LuxuryBlack)) {
        Column(Modifier.fillMaxSize().background(BrandGradient)) {
            ScreenHeader(title = "MY PROFILE", onBack = { viewModel.navigateBack() })

            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundGray),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(
                    Modifier.fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 120.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(16.dp))

                    Box(
                        Modifier.size(100.dp)
                            .clip(CircleShape)
                            .border(3.dp, BiroBlue, CircleShape)
                            .background(Color(0xFFE2E8F0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user.photoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = user.photoUrl,
                                contentDescription = "Profile Photo",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = BiroBlue,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        user.fullName.ifEmpty { "New Member" },
                        color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(user.email.ifEmpty { "No email set" }, color = TextGray, fontSize = 13.sp)
                    if (user.phone.isNotEmpty()) {
                        Text(user.phone, color = TextGray, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(12.dp))
                    Surface(color = SuccessGreen.copy(alpha = 0.12f), shape = RoundedCornerShape(20.dp)) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("ENGRACED VERIFIED MEMBER", color = SuccessGreen, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val earned = if (user.totalEarned > 0) "\u20A6${String.format("%.1fk", user.totalEarned / 1000.0)}" else "\u20A60"
                        val memberSince = user.memberSince.ifEmpty { "New" }
                        ProfileStatItem(deliveryCount.toString(), "Deliveries", Modifier.weight(1f))
                        ProfileStatItem(String.format("%.1f", user.rating), "Rating", Modifier.weight(1f))
                        ProfileStatItem(earned, "Earned", Modifier.weight(1f))
                        ProfileStatItem(memberSince, "Member", Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(24.dp))

                    StaggeredItem(0) { ProfileMenuRow(Icons.Default.Person, "Edit Profile", "Manage account & personal details") { showEditSheet = true } }
                    StaggeredItem(1) { ProfileMenuRow(Icons.Default.CreditCard, "Payment Methods", "Linked cards & banking") { showPaymentSheet = true } }
                    StaggeredItem(2) { ProfileMenuRow(Icons.Default.Settings, "App Settings", "Preferences & configuration") { viewModel.navigateTo(AppView.Settings) } }
                    StaggeredItem(3) { ProfileMenuRow(Icons.Default.HeadsetMic, "Help & Support", "FAQ & live support") { showHelpSheet = true } }
                    StaggeredItem(4) { ProfileMenuRow(Icons.Default.Info, "About Engraced Smile", "Version 1.0.0") { showAboutSheet = true } }

                    Spacer(Modifier.height(24.dp))

                    PremiumGradientButton(
                        "Logout",
                        icon = Icons.Default.Logout,
                        onClick = { viewModel.logout() },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    if (showEditSheet) ProfileEditSheet(viewModel) { showEditSheet = false }
    if (showPaymentSheet) PaymentMethodsSheet(user) { showPaymentSheet = false }
    if (showHelpSheet) HelpSupportSheet(LocalContext.current) { showHelpSheet = false }
    if (showAboutSheet) AboutProfileSheet { showAboutSheet = false }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileEditSheet(viewModel: DeliveryViewModel, onDismiss: () -> Unit) {
    val user by viewModel.currentUser.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var editName by remember { mutableStateOf(user.fullName) }
    var editEmail by remember { mutableStateOf(user.email) }
    var editPhone by remember { mutableStateOf(user.phone) }
    val isLoading by viewModel.isLoading.collectAsState()
    val dismiss: () -> Unit = { scope.launch { sheetState.hide(); onDismiss() } }

    ModalBottomSheet(
        onDismissRequest = dismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Text("Edit Profile", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = profileSheetFieldColors())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = editEmail, onValueChange = { editEmail = it }, label = { Text("Email Address") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = profileSheetFieldColors())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Phone Number") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = profileSheetFieldColors(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = dismiss, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f).height(48.dp), border = BorderStroke(1.dp, CardBorderGray)) {
                    Text("Cancel", color = TextGray, fontWeight = FontWeight.Bold)
                }
                Button(onClick = { viewModel.updateProfile(editName, editEmail, editPhone); dismiss() }, colors = ButtonDefaults.buttonColors(containerColor = BiroBlue), enabled = !isLoading, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f).height(48.dp)) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentMethodsSheet(user: com.example.data.models.User, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dismiss: () -> Unit = { scope.launch { sheetState.hide(); onDismiss() } }

    ModalBottomSheet(onDismissRequest = dismiss, sheetState = sheetState, containerColor = Color.White, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Text("Payment Methods", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
            Spacer(Modifier.height(8.dp))
            Text("Manage your cards and bank accounts", color = TextGray, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(BiroBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.CreditCard, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(20.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text("Visa ending in 8241", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold); Text("Expires 12/29", color = TextGray, fontSize = 11.sp) }
                    Surface(color = SuccessGreen.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) { Text("Default", color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(SuccessGreen.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text("GTBank Savings", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold); Text("****7892", color = TextGray, fontSize = 11.sp) }
                }
            }

            Spacer(Modifier.height(16.dp))
            PremiumGradientButton("Add New Payment Method", icon = Icons.Default.Add, onClick = dismiss, modifier = Modifier.fillMaxWidth().height(48.dp))
            Spacer(Modifier.height(12.dp))
            Button(onClick = dismiss, colors = ButtonDefaults.buttonColors(containerColor = BiroBlue), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Close", fontWeight = FontWeight.Bold, color = Color.White) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HelpSupportSheet(context: android.content.Context, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dismiss: () -> Unit = { scope.launch { sheetState.hide(); onDismiss() } }

    ModalBottomSheet(onDismissRequest = dismiss, sheetState = sheetState, containerColor = Color.White, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Text("Help & Support", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
            Spacer(Modifier.height(8.dp))
            Text("We're here to help you", color = TextGray, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))

            SupportOption(Icons.Default.Phone, "Call Support", "24/7 customer service line", Color(0xFFEFF6FF), Color(0xFF2563EB)) {
                context.startActivity(Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:+2348001234567") }); dismiss()
            }
            SupportOption(Icons.Default.Chat, "Live Chat", "Chat with a support agent", Color(0xFFECFDF5), Color(0xFF059669)) { dismiss() }
            SupportOption(Icons.Default.Email, "Email Support", "support@engraceddispatch.com", Color(0xFFFFF7ED), Color(0xFFEA580C)) {
                context.startActivity(Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:support@engraceddispatch.com") }); dismiss()
            }
            SupportOption(Icons.Default.Description, "FAQ", "Frequently asked questions", Color(0xFFF3E8FF), Color(0xFF9333EA)) { dismiss() }

            Spacer(Modifier.height(16.dp))
            Button(onClick = dismiss, colors = ButtonDefaults.buttonColors(containerColor = BiroBlue), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Close", fontWeight = FontWeight.Bold, color = Color.White) }
        }
    }
}

@Composable
private fun SupportOption(icon: ImageVector, title: String, subtitle: String, bg: Color, iconColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorderGray)
    ) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text(title, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold); Text(subtitle, color = TextGray, fontSize = 11.sp) }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutProfileSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dismiss: () -> Unit = { scope.launch { sheetState.hide(); onDismiss() } }

    ModalBottomSheet(onDismissRequest = dismiss, sheetState = sheetState, containerColor = Color.White, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(72.dp).background(BiroBlue.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(36.dp)) }
            Spacer(Modifier.height(16.dp))
            Text("Engraced Smile Dispatch", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
            Text("Premium Logistics Platform", color = TextGray, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))
            AboutProfileRow("Version", "1.0.0")
            AboutProfileRow("Build", "2026.06.28")
            AboutProfileRow("Platform", "Android")
            AboutProfileRow("Package", "com.aistudio.engraceddispatch.kxmpzq")
            Spacer(Modifier.height(20.dp))
            Button(onClick = dismiss, colors = ButtonDefaults.buttonColors(containerColor = BiroBlue), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Close", fontWeight = FontWeight.Bold, color = Color.White) }
        }
    }
}

@Composable
private fun AboutProfileRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextGray, fontSize = 13.sp)
        Text(value, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ProfileStatItem(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorderGray)
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(2.dp))
            Text(label, color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ProfileMenuRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorderGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(BiroBlue.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, color = TextGray, fontSize = 11.sp)
                }
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun profileSheetFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextMain, unfocusedTextColor = TextMain,
    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
    focusedBorderColor = BiroBlue, unfocusedBorderColor = CardBorderGray,
    focusedLabelColor = BiroBlue, unfocusedLabelColor = TextGray, cursorColor = BiroBlue
)
