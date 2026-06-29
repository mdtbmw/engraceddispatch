package com.example.rider.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rider.RiderViewModel
import com.example.rider.components.*
import com.example.rider.models.RiderDelivery
import com.example.rider.navigation.RiderView
import com.example.ui.theme.*
import com.example.ui.components.AnimatedCounter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RiderDashboardScreen(viewModel: RiderViewModel) {
    val profile by viewModel.riderProfile.collectAsState()
    val deliveries by viewModel.riderDeliveries.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val stats by viewModel.riderStats.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadRiderData() }

    val activeDeliveries = deliveries.filter { it.status != "DELIVERED" && it.status != "CANCELLED" }
    val todayCompleted = deliveries.count { it.status == "DELIVERED" }
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    val greeting = when { hour < 12 -> "Good Morning"; hour < 17 -> "Good Afternoon"; else -> "Good Evening" }
    val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
    val todayDate = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date())

    val isPeakHour = hour in 7..9 || hour in 17..19
    val dispatchMsg = if (isPeakHour) "Peak hours active - Priority dispatch running" else "Normal dispatch operations - ${activeDeliveries.size} jobs pending"
    val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
    val shiftBonus = if (isPeakHour) "Peak bonus: +15% per job" else if (isWeekend) "Weekend rate: +10% per job" else "Standard rate active"

    Box(Modifier.fillMaxSize().background(BackgroundGray)) {
        Column(Modifier.fillMaxSize()) {
            // ===== HEADER =====
            Box(Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)).background(BrandGradient, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))) {
                Column(Modifier.statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(46.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(greeting, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(profile.fullName.ifEmpty { "Rider" }, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        OnlineToggleButton(isOnline, onClick = { viewModel.toggleOnline() })
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(profile.bikeNumber.ifEmpty { "LAG-0000-XX" }, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(todayDate, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(currentTime, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(profile.currentZone, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Surface(color = if (isOnline) SuccessGreen.copy(alpha = 0.2f) else Color(0xFF94A3B3).copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                            Text(shiftBonus, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                }
            }

            // ===== CONTENT =====
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Quick Actions Dock
                item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickActionDock(Icons.Default.QrCodeScanner, "Scan QR", BiroBlue) { }
                        QuickActionDock(Icons.Default.NearMe, "Navigate", SuccessGreen) { }
                        QuickActionDock(Icons.Default.Forum, "Dispatch Chat", Color(0xFF8B5CF6)) { }
                        QuickActionDock(Icons.Default.ReportProblem, "Report Issue", Color(0xFFF97316)) { }
                        QuickActionDock(Icons.Default.Settings, "Tools", TextGray) { viewModel.navigateTo(RiderView.Settings) }
                    }
                }

                // Stats Dock
                item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        RiderStatCard("${activeDeliveries.size}", "Dispatch Jobs", Icons.Default.LocalShipping, BiroBlue)
                        RiderStatCard("$todayCompleted", "Completed", Icons.Default.CheckCircle, SuccessGreen)
                        RiderStatCard("${stats.rating}", "Rider Score", Icons.Default.Star, Color(0xFFF59E0B))
                        RiderStatCard("${stats.onTimeRate}%", "On-Time", Icons.Default.Schedule, Color(0xFF8B5CF6))
                    }
                }

                // Today's Shift Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, CardBorderGray),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                DockLabel("SHIFT", BrandGradient)
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Today's Dispatch Shift", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                                if (isOnline) {
                                    Surface(color = SuccessGreen.copy(alpha = 0.12f), shape = RoundedCornerShape(20.dp)) {
                                        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(Modifier.size(6.dp).clip(CircleShape).background(SuccessGreen))
                                            Spacer(Modifier.width(4.dp))
                                            Text("On Duty", color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                ShiftTimeBlock("Start", "06:00 AM", "Dispatch login")
                                ShiftTimeBlock("Break 1", "10:30 AM", "Rest period")
                                ShiftTimeBlock("Break 2", "01:30 PM", "Lunch break")
                                ShiftTimeBlock("End", "05:00 PM", "Shift close")
                            }
                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider(color = CardBorderGray)
                            Spacer(Modifier.height(10.dp))
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Assigned Zone: ${profile.currentZone}", color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Surface(color = if (activeDeliveries.isNotEmpty()) BiroBlue.copy(alpha = 0.12f) else SuccessGreen.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                                    Text("${deliveries.size} today | ${todayCompleted} done", color = if (activeDeliveries.isNotEmpty()) BiroBlue else SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                                }
                            }
                        }
                    }
                }

                // Vehicle Status Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, CardBorderGray),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                DockLabel("FLEET", Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFFA78BFA))))
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Vehicle Status", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                Spacer(Modifier.weight(1f))
                                Surface(color = SuccessGreen.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                                    Row(Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(6.dp).clip(CircleShape).background(SuccessGreen))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Roadworthy", color = SuccessGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF7C3AED).copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(24.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(profile.bikeNumber.ifEmpty { "LAG-0000-XX" }, color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                    Text(profile.bikeModel.ifEmpty { "Honda CBZ 150cc" }, color = TextGray, fontSize = 12.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Next Service", color = TextGray, fontSize = 9.sp)
                                    Text("12,450 km", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                VehicleStat("Fuel", "\u26FD Full", SuccessGreen)
                                VehicleStat("Tires", "\u26A1 Good", Color(0xFFF59E0B))
                                VehicleStat("Insurance", "\u2705 Active", SuccessGreen)
                                VehicleStat("License", "\u2705 Valid", SuccessGreen)
                            }
                        }
                    }
                }

                // Active Dispatch Jobs Section
                item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Active Dispatch Jobs", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        DockLabel("${activeDeliveries.size}", BrandGradient)
                    }
                }
                if (activeDeliveries.isEmpty()) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                            Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(Modifier.size(72.dp).background(SuccessGreen.copy(alpha = 0.08f), CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(40.dp))
                                }
                                Spacer(Modifier.height(16.dp))
                                Text("All Clear - No Pending Jobs!", color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                                Text("Stay online to receive new dispatch assignments from the control room.", color = TextGray, fontSize = 13.sp, textAlign = TextAlign.Center)
                                Spacer(Modifier.height(20.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    val haptic = LocalHapticFeedback.current
                                    OutlinedButton(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.toggleOnline() }, border = BorderStroke(1.dp, if (isOnline) DangerRed else SuccessGreen), shape = RoundedCornerShape(12.dp)) {
                                        Text(if (isOnline) "Go Offline" else "Go Online", color = if (isOnline) DangerRed else SuccessGreen, fontWeight = FontWeight.Bold)
                                    }
                                    Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = BiroBlue), shape = RoundedCornerShape(12.dp)) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Refresh", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(activeDeliveries, key = { it.trackingNumber }) { delivery ->
                        RiderJobCard(delivery, viewModel)
                    }
                }

                // Suggested Next Job
                if (activeDeliveries.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    DockLabel("ROUTE", Brush.horizontalGradient(listOf(Color(0xFFF97316), Color(0xFFF59E0B))))
                                    Spacer(Modifier.width(8.dp))
                                    Icon(Icons.Default.Route, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Optimized Route", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                                }
                                Spacer(Modifier.height(12.dp))
                                val firstJob = activeDeliveries.first()
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(8.dp).clip(CircleShape).background(BiroBlue))
                                    Spacer(Modifier.width(8.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text("START: ${firstJob.pickupAddress.split(",").first()}", color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("2.3 km away \u2022 Est. 8 min ride", color = TextGray, fontSize = 10.sp)
                                    }
                                    Surface(color = BiroBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                                        Text("Navigate", color = BiroBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp).clickable { })
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF97316)))
                                    Spacer(Modifier.width(8.dp))
                                    Text("END: ${firstJob.deliveryAddress.split(",").first()}", color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Fleet Standings / Leaderboard
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, CardBorderGray),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                DockLabel("FLEET", Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), BiroBlue)))
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Fleet Standings", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                Spacer(Modifier.weight(1f))
                                Text("Top 5 today", color = TextGray, fontSize = 10.sp)
                            }
                            Spacer(Modifier.height(14.dp))
                            FleetStandingRow(1, "Sani Ibrahim", 12, 100, true)
                            Spacer(Modifier.height(6.dp))
                            FleetStandingRow(2, "Chukwuemeka Obi", 10, 95, false)
                            Spacer(Modifier.height(6.dp))
                            FleetStandingRow(3, "You", todayCompleted, stats.onTimeRate, false, isCurrentUser = true, highlight = true)
                            Spacer(Modifier.height(6.dp))
                            FleetStandingRow(4, "Tunde Bakare", 7, 88, false)
                            Spacer(Modifier.height(6.dp))
                            FleetStandingRow(5, "Amara Okafor", 6, 92, false)
                        }
                    }
                }

                // Dispatch Announcements
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, CardBorderGray),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                DockLabel("OPS", Brush.horizontalGradient(listOf(Color(0xFFF97316), Color(0xFFEA580C))))
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Dispatch Ops Center", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Spacer(Modifier.height(12.dp))
                            AnnouncementDock("URGENT", "Ikeja axis: High volume, extra riders needed", "Now", DangerRed)
                            Spacer(Modifier.height(8.dp))
                            AnnouncementDock("PEAK", "Peak hour bonus +15% until 10:00 AM", "Active", Color(0xFFF97316))
                            Spacer(Modifier.height(8.dp))
                            AnnouncementDock("INFO", "Fuel station 101 has diesel at \u20A61,200/L", "1h ago", BiroBlue)
                            Spacer(Modifier.height(8.dp))
                            AnnouncementDock("ROAD", "Lekki - Eti-Osa road: Temporary diversion", "2h ago", Color(0xFF8B5CF6))
                        }
                    }
                }

                // Performance & Milestones
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, CardBorderGray),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                DockLabel("STATS", BrandGradient)
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Performance Metrics", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Spacer(Modifier.height(14.dp))
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("On-Time Dispatch Rate", color = TextGray, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Text("${stats.onTimeRate}%", color = SuccessGreen, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(progress = { stats.onTimeRate / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = SuccessGreen, trackColor = CardBorderGray)
                            Spacer(Modifier.height(14.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                MilestoneChip(Icons.Default.LocalShipping, "Total Jobs: ${profile.totalDeliveries}", BiroBlue)
                                MilestoneChip(Icons.Default.Star, "Score: ${stats.rating}", Color(0xFFF59E0B))
                                MilestoneChip(Icons.Default.MilitaryTech, "Level ${(profile.totalDeliveries / 10) + 1} Rider", SuccessGreen)
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TextGray, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Joined ${profile.memberSince.ifEmpty { "Jan 2025" }}", color = TextGray, fontSize = 10.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = TextGray, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Avg response: 4.2 min", color = TextGray, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                // Weekly Summary
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, CardBorderGray),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                DockLabel("WEEK", Brush.horizontalGradient(listOf(SuccessGreen, Color(0xFF059669))))
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("This Week's Summary", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Spacer(Modifier.height(14.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                WeeklyStat("Mon", 4, isToday = dayOfWeek == Calendar.MONDAY)
                                WeeklyStat("Tue", 6, isToday = dayOfWeek == Calendar.TUESDAY)
                                WeeklyStat("Wed", 3, isToday = dayOfWeek == Calendar.WEDNESDAY)
                                WeeklyStat("Thu", 5, isToday = dayOfWeek == Calendar.THURSDAY)
                                WeeklyStat("Fri", todayCompleted, isToday = dayOfWeek == Calendar.FRIDAY)
                                WeeklyStat("Sat", 0, isToday = dayOfWeek == Calendar.SATURDAY)
                                WeeklyStat("Sun", 0, isToday = dayOfWeek == Calendar.SUNDAY)
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Weekly total: 18 jobs", color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Avg: 3.6/day", color = TextGray, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Quick Links Dock
                item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickLinkCard(Icons.Default.Person, "Rider Profile", BrandGradient) { viewModel.navigateTo(RiderView.Profile) }
                        QuickLinkCard(Icons.Default.Tune, "Settings Panel", Brush.horizontalGradient(listOf(BiroBlue, DarkGradientBlue))) { viewModel.navigateTo(RiderView.Settings) }
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

// ===== DOCK LABEL - macOS-style pill badge for section headers =====
@Composable
private fun DockLabel(text: String, gradient: Brush) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.background(gradient, RoundedCornerShape(6.dp))
    ) {
        Text(text.uppercase(), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp, modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
    }
}

// ===== QUICK ACTION DOCK =====
@Composable
private fun RowScope.QuickActionDock(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorderGray),
        modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).clickable(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onClick() })
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(34.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(17.dp))
            }
            Spacer(Modifier.height(4.dp))
            Text(label, color = TextMain, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ===== SHIFT TIME BLOCK =====
@Composable
private fun ShiftTimeBlock(label: String, time: String, sub: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(3.dp))
        Text(time, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
        Text(sub, color = TextMuted, fontSize = 8.sp)
    }
}

// ===== VEHICLE STAT =====
@Composable
private fun RowScope.VehicleStat(label: String, value: String, valueColor: Color) {
    Surface(color = valueColor.copy(alpha = 0.06f), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) {
        Column(Modifier.padding(vertical = 6.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = TextGray, fontSize = 9.sp)
            Text(value, color = valueColor, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

// ===== ANNOUNCEMENT DOCK =====
@Composable
private fun AnnouncementDock(tag: String, message: String, time: String, tagColor: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(color = tagColor.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp)) {
            Text(tag, color = tagColor, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(message, color = TextMain, fontSize = 11.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(time, color = TextMuted, fontSize = 9.sp)
    }
}

// ===== FLEET STANDING ROW =====
@Composable
private fun FleetStandingRow(rank: Int, name: String, jobs: Int, onTime: Int, isYou: Boolean, isCurrentUser: Boolean = false, highlight: Boolean = false) {
    Surface(
        color = if (highlight) BiroBlue.copy(alpha = 0.04f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        border = if (highlight) BorderStroke(1.dp, BiroBlue.copy(alpha = 0.2f)) else null
    ) {
        Row(Modifier.padding(vertical = 6.dp, horizontal = 8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(24.dp).clip(CircleShape).background(if (rank <= 3) Color(0xFFF59E0B).copy(alpha = 0.15f) else Color(0xFFE2E8F0)), contentAlignment = Alignment.Center) {
                Text("$rank", color = if (rank <= 3) Color(0xFFF59E0B) else TextGray, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(name, color = if (isCurrentUser) BiroBlue else TextMain, fontSize = 12.sp, fontWeight = if (isCurrentUser) FontWeight.ExtraBold else FontWeight.SemiBold)
                    if (isYou) { Spacer(Modifier.width(4.dp)); Text("(You)", color = BiroBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$jobs jobs", color = TextGray, fontSize = 10.sp)
                Spacer(Modifier.width(8.dp))
                Surface(color = if (onTime >= 90) SuccessGreen.copy(alpha = 0.1f) else Color(0xFFF97316).copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                    Text("$onTime%", color = if (onTime >= 90) SuccessGreen else Color(0xFFF97316), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            if (rank == 1) { Spacer(Modifier.width(4.dp)); Text("\uD83C\uDFC6", fontSize = 14.sp) }
        }
    }
}

// ===== WEEKLY STAT =====
@Composable
private fun WeeklyStat(day: String, count: Int, isToday: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(day.take(3), color = if (isToday) BiroBlue else TextGray, fontSize = 9.sp, fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Box(Modifier.size(28.dp).clip(CircleShape).background(if (count > 0) if (isToday) BiroBlue.copy(alpha = 0.15f) else SuccessGreen.copy(alpha = 0.1f) else Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
            Text("$count", color = if (count > 0) if (isToday) BiroBlue else SuccessGreen else TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ===== MILESTONE CHIP =====
@Composable
private fun MilestoneChip(icon: ImageVector, text: String, color: Color) {
    Surface(color = color.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp)) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text(text, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ===== QUICK LINK CARD =====
@Composable
private fun RowScope.QuickLinkCard(icon: ImageVector, label: String, gradient: Brush, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).background(gradient, RoundedCornerShape(16.dp))
    ) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ===== RIDER JOB CARD =====
@Composable
private fun RiderJobCard(delivery: RiderDelivery, viewModel: RiderViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CardBorderGray),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(20.dp)).clickable { viewModel.loadDeliveryByTracking(delivery.trackingNumber); viewModel.navigateTo(RiderView.DeliveryDetail(delivery.trackingNumber)) }
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = BiroBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text(delivery.trackingNumber, color = BiroBlue, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(delivery.deliveryType, color = TextMuted, fontSize = 10.sp)
                }
                RiderStatusBadge(delivery.status)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(BiroBlue))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("PICKUP POINT", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(delivery.pickupAddress, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.height(2.dp))
            Box(Modifier.width(1.dp).height(14.dp).padding(start = 4.5.dp).background(CardBorderGray))
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFF97316)))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("DROPOFF POINT", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(delivery.deliveryAddress, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = CardBorderGray)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TextGray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(delivery.customerName, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NearMe, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(delivery.distance, color = BiroBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(" \u2022 ", color = TextGray, fontSize = 11.sp)
                    Text("${delivery.etaMinutes} min", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            if (delivery.notes.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Surface(color = Color(0xFFFFF3CD), shape = RoundedCornerShape(8.dp)) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 5.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF856404), modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(delivery.notes, color = Color(0xFF856404), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
