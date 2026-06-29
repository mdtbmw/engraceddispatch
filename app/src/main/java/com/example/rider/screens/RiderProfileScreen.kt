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

@Composable
fun RiderProfileScreen(viewModel: RiderViewModel) {
    val profile by viewModel.riderProfile.collectAsState()
    val stats by viewModel.riderStats.collectAsState()
    val haptic = LocalHapticFeedback.current

    RiderScaffold(title = "MY PROFILE", onBack = { viewModel.navigateBack() }) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // Avatar & Name
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(100.dp).clip(CircleShape).background(BrandGradient), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(50.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text(profile.fullName.ifEmpty { "Rider" }, color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text(profile.email, color = TextGray, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Surface(color = BiroBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text("ENGRACED RIDER", color = BiroBlue, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // Vehicle Info Card
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Vehicle Information", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(48.dp).clip(CircleShape).background(BiroBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(24.dp)) }
                        Spacer(Modifier.width(12.dp))
                        Column { Text(profile.bikeNumber.ifEmpty { "LAG-0000-XX" }, color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(profile.bikeModel.ifEmpty { "Honda CBZ" }, color = TextGray, fontSize = 12.sp) }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Stats Row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileStatCard("${stats.totalDeliveries}", "Deliveries", Icons.Default.LocalShipping)
                ProfileStatCard("${stats.rating}", "Rating", Icons.Default.Star)
                ProfileStatCard(profile.currentZone, "Zone", Icons.Default.LocationOn)
            }

            Spacer(Modifier.height(16.dp))

            // Menu items
            MenuRow(Icons.Default.Notifications, "Notification Preferences") { }
            MenuRow(Icons.Default.HelpOutline, "Help & Support") { }
            MenuRow(Icons.Default.Info, "About Engraced Rider") { }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.logout() },
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White); Spacer(Modifier.width(8.dp)); Text("Logout", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun RowScope.ProfileStatCard(value: String, label: String, icon: ImageVector) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.weight(1f)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
            Text(label, color = TextGray, fontSize = 10.sp)
        }
    }
}

@Composable
private fun MenuRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
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
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
        }
    }
}
