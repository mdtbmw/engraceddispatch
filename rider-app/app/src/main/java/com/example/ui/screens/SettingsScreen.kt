package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DeliveryViewModel
import com.example.ui.navigation.AppView
import com.example.ui.theme.*
import com.example.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: DeliveryViewModel) {
    val context = LocalContext.current
    val notificationsEnabled by viewModel.preferences.notificationsEnabled.collectAsState(initial = true)
    val darkMode by viewModel.preferences.darkMode.collectAsState(initial = false)
    val biometricEnabled by viewModel.preferences.biometricEnabled.collectAsState(initial = false)
    val user by viewModel.currentUser.collectAsState()

    var showSecuritySheet by remember { mutableStateOf(false) }
    var showPasswordSheet by remember { mutableStateOf(false) }
    var showAddressSheet by remember { mutableStateOf(false) }
    var showAboutSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showPaymentSheet by remember { mutableStateOf(false) }
    var showRidersSheet by remember { mutableStateOf(false) }
    var showLiveChatSheet by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(LuxuryBlack)) {
        Column(Modifier.fillMaxSize().background(BrandGradient)) {
            ScreenHeader(title = "SETTINGS", onBack = { viewModel.navigateBack() })

            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundGray),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(
                    Modifier.fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 120.dp)
                ) {
                    Text("Account", color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(12.dp))
                    SettingsMenuRow(Icons.Default.Person, "Edit Profile", "Name, email, phone number") {
                        viewModel.navigateTo(AppView.Profile)
                    }
                    SettingsMenuRow(Icons.Default.Lock, "Change Password", "Update your password regularly") { showPasswordSheet = true }
                    SettingsMenuRow(Icons.Default.Shield, "Security", "2FA, biometrics, sessions") { showSecuritySheet = true }

                    Spacer(Modifier.height(20.dp))
                    Text("Preferences", color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(12.dp))

                    SettingsToggleRow(Icons.Default.Notifications, "Push Notifications", "Delivery alerts & updates", notificationsEnabled) { viewModel.updateNotificationPrefs(it) }
                    SettingsToggleRow(Icons.Default.DarkMode, "Dark Mode", "Use dark color theme", darkMode) { viewModel.updateDarkMode(it) }
                    SettingsToggleRow(Icons.Default.Fingerprint, "Biometric Login", "Fingerprint sign-in", biometricEnabled) { viewModel.updateBiometric(it) }
                    SettingsMenuRow(Icons.Default.Language, "Language", "English (US)") { showLanguageSheet = true }

                    Spacer(Modifier.height(20.dp))
                    Text("Fleet & Shift", color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(12.dp))
                    SettingsMenuRow(Icons.Default.LocationCity, "Assigned Hubs", "View default dispatcher hubs") { showPaymentSheet = true }
                    SettingsMenuRow(Icons.Default.TwoWheeler, "Motorcycle Profile", "View assigned vehicle stats") { showAddressSheet = true }
                    SettingsMenuRow(Icons.Default.Receipt, "Delivery History", "View all past deliveries") { viewModel.navigateToRoot(AppView.OrderLogs) }

                    Spacer(Modifier.height(20.dp))
                    Text("Support", color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(12.dp))
                    SettingsMenuRow(Icons.Default.HeadsetMic, "Contact Support", "24/7 customer service") {
                        context.startActivity(Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:+2348001234567") })
                    }
                    SettingsMenuRow(Icons.Default.Chat, "Live Chat", "Chat with support agent") { showLiveChatSheet = true }
                    SettingsMenuRow(Icons.Default.Description, "Terms of Service", "Platform terms & conditions") {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://engraceddispatch.com/terms")))
                    }
                    SettingsMenuRow(Icons.Default.Policy, "Privacy Policy", "How we handle your data") {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://engraceddispatch.com/privacy")))
                    }
                    SettingsMenuRow(Icons.Default.Info, "About & Version", "v1.0.0 Build 2026") { showAboutSheet = true }

                    Spacer(Modifier.height(24.dp))

                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.3f))) {
                        Column(Modifier.padding(14.dp)) {
                            Text("Account Management", color = DangerRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { viewModel.logout() },
                                border = BorderStroke(1.dp, DangerRed),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Logout of Account", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    if (showSecuritySheet) SecuritySheet(user.fullName, user.email) { showSecuritySheet = false }
    if (showPasswordSheet) ChangePasswordSheet(viewModel) { showPasswordSheet = false }
    if (showAddressSheet) DefaultAddressSheet(viewModel) { showAddressSheet = false }
    if (showAboutSheet) AboutSheet { showAboutSheet = false }
    if (showLanguageSheet) LanguageSheet(viewModel) { showLanguageSheet = false }
    if (showPaymentSheet) SettingsPaymentSheet { showPaymentSheet = false }
    if (showRidersSheet) PreferredRidersSheet { showRidersSheet = false }
    if (showLiveChatSheet) LiveChatSheet { showLiveChatSheet = false }
}

@Composable
private fun SettingsMenuRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
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
private fun SettingsToggleRow(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onCheck: (Boolean) -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(BiroBlue.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(18.dp)) }
                Spacer(Modifier.width(12.dp))
                Column { Text(title, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold); Text(subtitle, color = TextGray, fontSize = 11.sp) }
            }
            Switch(checked = checked, onCheckedChange = onCheck, colors = SwitchDefaults.colors(checkedThumbColor = BiroBlue, checkedTrackColor = BiroBlue.copy(alpha = 0.3f)))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecuritySheet(userName: String, userEmail: String, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dismiss: () -> Unit = { scope.launch { sheetState.hide(); onDismiss() } }

    ModalBottomSheet(onDismissRequest = dismiss, sheetState = sheetState, containerColor = Color.White, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Text("Security Settings", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
            Spacer(Modifier.height(20.dp))

            SecurityItem(Icons.Default.VerifiedUser, "Two-Factor Auth", "Add extra layer of security", true)
            Spacer(Modifier.height(10.dp))
            SecurityItem(Icons.Default.Devices, "Active Sessions", "2 devices active", false)
            Spacer(Modifier.height(10.dp))
            SecurityItem(Icons.Default.History, "Login History", "Last login: Today, Lagos", false)
            Spacer(Modifier.height(10.dp))
            SecurityItem(Icons.Default.LocationOn, "Login Locations", "Lagos, Nigeria", false)

            Spacer(Modifier.height(24.dp))
            Button(onClick = dismiss, colors = ButtonDefaults.buttonColors(containerColor = BiroBlue), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("Close", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun SecurityItem(icon: ImageVector, title: String, subtitle: String, isEnabled: Boolean) {
    Surface(shape = RoundedCornerShape(12.dp), color = Color.White, border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(BiroBlue.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, color = TextGray, fontSize = 11.sp)
                }
            }
            Surface(color = if (isEnabled) SuccessGreen.copy(alpha = 0.12f) else Color(0xFFF1F5F9), shape = RoundedCornerShape(8.dp)) {
                Text(
                    if (isEnabled) "Active" else "Off",
                    color = if (isEnabled) SuccessGreen else TextGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePasswordSheet(viewModel: DeliveryViewModel, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dismiss: () -> Unit = { scope.launch { sheetState.hide(); onDismiss() } }
    var current by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = dismiss, sheetState = sheetState, containerColor = Color.White, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Text("Change Password", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
            Spacer(Modifier.height(8.dp))
            Text("Enter your current and new password", color = TextGray, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(value = current, onValueChange = { current = it; error = null }, label = { Text("Current Password") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = sheetFieldColors())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = newPass, onValueChange = { newPass = it; error = null }, label = { Text("New Password") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = sheetFieldColors())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = confirm, onValueChange = { confirm = it; error = null }, label = { Text("Confirm New Password") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = sheetFieldColors())
            error?.let { Text(it, color = DangerRed, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp)) }
            Spacer(Modifier.height(24.dp))
            PremiumGradientButton(
                "Update Password",
                onClick = {
                    when {
                        current.isBlank() -> error = "Enter current password"
                        newPass.length < 6 -> error = "Password must be at least 6 characters"
                        newPass != confirm -> error = "Passwords do not match"
                        else -> {
                            viewModel.changePassword(
                                current = current,
                                newPass = newPass,
                                onSuccess = { dismiss() },
                                onError = { error = it }
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultAddressSheet(viewModel: DeliveryViewModel, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dismiss: () -> Unit = { scope.launch { sheetState.hide(); onDismiss() } }

    ModalBottomSheet(onDismissRequest = dismiss, sheetState = sheetState, containerColor = Color.White, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Text("Motorcycle Profile", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
            Spacer(Modifier.height(8.dp))
            Text("Assigned company vehicle specifications", color = TextGray, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(value = "LAG-5832-BK", onValueChange = {}, label = { Text("License Plate Number") }, leadingIcon = { Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = BiroBlue) }, readOnly = true, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = sheetFieldColors())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = "TVS HLX 150 (2025)", onValueChange = {}, label = { Text("Motorcycle Model") }, leadingIcon = { Icon(Icons.Default.Build, contentDescription = null, tint = BiroBlue) }, readOnly = true, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = sheetFieldColors())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = "Maryland Logistics Hub", onValueChange = {}, label = { Text("Assigned Fleet Location") }, leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = BiroBlue) }, readOnly = true, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = sheetFieldColors())
            Spacer(Modifier.height(24.dp))
            Button(onClick = dismiss, colors = ButtonDefaults.buttonColors(containerColor = BiroBlue), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("Close", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dismiss: () -> Unit = { scope.launch { sheetState.hide(); onDismiss() } }

    ModalBottomSheet(onDismissRequest = dismiss, sheetState = sheetState, containerColor = Color.White, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(72.dp).background(BiroBlue.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(36.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Engraced Smile Dispatch", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
            Text("Premium Logistics Platform", color = TextGray, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))
            AboutRow("Version", "1.0.0")
            AboutRow("Build", "2026.06.28")
            AboutRow("Platform", "Android")
            AboutRow("Min SDK", "Android 7.0 (API 24)")
            AboutRow("Package", "com.aistudio.engraceddispatch.kxmpzq")
            Spacer(Modifier.height(20.dp))
            Button(onClick = dismiss, colors = ButtonDefaults.buttonColors(containerColor = BiroBlue), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("Close", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextGray, fontSize = 13.sp)
        Text(value, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSheet(viewModel: DeliveryViewModel, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dismiss: () -> Unit = { scope.launch { sheetState.hide(); onDismiss() } }
    val selected by viewModel.selectedLanguage.collectAsState()
    val languages = listOf("English" to "en_US", "French" to "fr_FR", "Yoruba" to "yo_NG", "Hausa" to "ha_NG", "Igbo" to "ig_NG")

    ModalBottomSheet(onDismissRequest = dismiss, sheetState = sheetState, containerColor = Color.White, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Text("Language", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
            Spacer(Modifier.height(8.dp))
            Text("Select your preferred language", color = TextGray, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))
            languages.forEach { (name, _) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(12.dp)).clickable { viewModel.updateLanguage(name); dismiss() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (selected == name) BiroBlue.copy(alpha = 0.08f) else Color.White),
                    border = BorderStroke(1.dp, if (selected == name) BiroBlue else CardBorderGray)
                ) {
                    Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        if (selected == name) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(name, color = if (selected == name) BiroBlue else TextMain, fontSize = 14.sp, fontWeight = if (selected == name) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsPaymentSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dismiss: () -> Unit = { scope.launch { sheetState.hide(); onDismiss() } }

    ModalBottomSheet(onDismissRequest = dismiss, sheetState = sheetState, containerColor = Color.White, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Text("Assigned Dispatch Hubs", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
            Spacer(Modifier.height(8.dp))
            Text("Assigned logistics centers for rider check-in", color = TextGray, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(BiroBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.LocationCity, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(20.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text("Maryland Logistics Hub", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold); Text("Maryland, Lagos", color = TextGray, fontSize = 11.sp) }
                    Surface(color = SuccessGreen.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) { Text("Primary", color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(SuccessGreen.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.LocationOn, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text("Lekki Dispatch Center", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold); Text("Lekki Phase 1, Lagos", color = TextGray, fontSize = 11.sp) }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = dismiss, colors = ButtonDefaults.buttonColors(containerColor = BiroBlue), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Close", fontWeight = FontWeight.Bold, color = Color.White) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreferredRidersSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dismiss: () -> Unit = { scope.launch { sheetState.hide(); onDismiss() } }
    val riders = listOf("Sani Ibrahim" to "4.9", "Chukwuemeka Obi" to "4.8", "Tunde Bakare" to "4.7")
    var selectedRider by remember { mutableStateOf("Sani Ibrahim") }

    ModalBottomSheet(onDismissRequest = dismiss, sheetState = sheetState, containerColor = Color.White, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Text("Preferred Riders", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
            Spacer(Modifier.height(8.dp))
            Text("Select your preferred riders for deliveries", color = TextGray, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))

            riders.forEach { (name, rating) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).clip(RoundedCornerShape(14.dp)).clickable { selectedRider = name; dismiss() },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = if (selectedRider == name) BiroBlue.copy(alpha = 0.08f) else Color.White),
                    border = BorderStroke(1.dp, if (selectedRider == name) BiroBlue else CardBorderGray)
                ) {
                    Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(CircleShape).background(BiroBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(20.dp)) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) { Text(name, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold); Text("Rating: $rating", color = TextGray, fontSize = 11.sp) }
                        if (selectedRider == name) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Button(onClick = dismiss, colors = ButtonDefaults.buttonColors(containerColor = BiroBlue), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Done", fontWeight = FontWeight.Bold, color = Color.White) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LiveChatSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dismiss: () -> Unit = { scope.launch { sheetState.hide(); onDismiss() } }
    var message by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = dismiss, sheetState = sheetState, containerColor = Color.White, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Text("Live Chat", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
            Spacer(Modifier.height(8.dp))
            Text("Chat with a support agent", color = TextGray, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))

            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFF8FAFC), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(32.dp).clip(CircleShape).background(SuccessGreen.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp)) }
                        Spacer(Modifier.width(8.dp))
                        Column { Text("Support Agent", color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold); Text("Online", color = SuccessGreen, fontSize = 10.sp) }
                    }
                    Spacer(Modifier.height(12.dp))
                    Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
                        Text("Hello! How can I help you today?", color = TextMain, fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                placeholder = { Text("Type your message...") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = sheetFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            PremiumGradientButton("Send Message", icon = Icons.Default.Send, onClick = dismiss, modifier = Modifier.fillMaxWidth().height(48.dp))
        }
    }
}

@Composable
private fun sheetFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextMain, unfocusedTextColor = TextMain,
    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
    focusedBorderColor = BiroBlue, unfocusedBorderColor = CardBorderGray,
    focusedLabelColor = BiroBlue, unfocusedLabelColor = TextGray, cursorColor = BiroBlue
)
