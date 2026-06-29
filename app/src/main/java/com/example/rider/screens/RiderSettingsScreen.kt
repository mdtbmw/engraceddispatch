package com.example.rider.screens

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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rider.RiderViewModel
import com.example.rider.components.RiderScaffold
import com.example.rider.navigation.RiderView
import com.example.ui.theme.*
import com.example.ui.components.SectionTitle

@Composable
fun RiderSettingsScreen(viewModel: RiderViewModel) {
    val haptic = LocalHapticFeedback.current
    val isOnline by viewModel.isOnline.collectAsState()
    val profile by viewModel.riderProfile.collectAsState()

    RiderScaffold(title = "SETTINGS", onBack = { viewModel.navigateBack() }) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // Account Section
            SectionTitle("Account")
            SettingsRow(Icons.Default.Person, "Rider Profile", subtitle = profile.fullName.ifEmpty { "Set up profile" }) { }
            SettingsRow(Icons.Default.TwoWheeler, "Vehicle Info", subtitle = "${profile.bikeNumber} - ${profile.bikeModel}") { }
            SettingsRow(Icons.Default.Lock, "Change Password") { }

            Spacer(Modifier.height(20.dp))

            // Preferences Section
            SectionTitle("Preferences")
            ToggleRow(Icons.Default.Notifications, "Push Notifications", true) { }
            val darkMode by viewModel.preferences.darkMode.collectAsState(initial = false)
            ToggleRow(Icons.Default.DarkMode, "Dark Mode", darkMode) { viewModel.updateDarkMode(!darkMode) }
            val biometric by viewModel.preferences.biometricEnabled.collectAsState(initial = false)
            ToggleRow(Icons.Default.Fingerprint, "Biometric Login", biometric) { viewModel.updateBiometric(!biometric) }

            Spacer(Modifier.height(20.dp))

            // Delivery Section
            SectionTitle("Delivery")
            SettingsRow(Icons.Default.Map, "Coverage Zone", subtitle = profile.currentZone) { }
            SettingsRow(Icons.Default.CalendarMonth, "Delivery History") { viewModel.navigateTo(RiderView.DeliveryHistory) }
            SettingsRow(Icons.Default.Star, "My Ratings", subtitle = "${profile.rating} stars") { }

            Spacer(Modifier.height(20.dp))

            // Support Section
            SectionTitle("Support")
            SettingsRow(Icons.Default.HelpOutline, "Help & FAQ") { }
            SettingsRow(Icons.Default.Forum, "Live Chat with Dispatch") { }
            SettingsRow(Icons.Default.Description, "Terms of Service") { }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.logout() },
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White); Spacer(Modifier.width(8.dp)); Text("Logout of Account", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String = "", onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorderGray)
    ) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(BiroBlue.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(18.dp)) }
                Spacer(Modifier.width(12.dp))
                Column { Text(title, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold); if (subtitle.isNotEmpty()) Text(subtitle, color = TextGray, fontSize = 11.sp) }
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ToggleRow(icon: ImageVector, title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorderGray)
    ) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(BiroBlue.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(18.dp)) }
                Spacer(Modifier.width(12.dp))
                Text(title, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BiroBlue, uncheckedThumbColor = Color.White, uncheckedTrackColor = Color(0xFFCBD5E1)))
        }
    }
}
