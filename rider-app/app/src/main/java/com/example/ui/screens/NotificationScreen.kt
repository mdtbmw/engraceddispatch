package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.NotificationItem
import com.example.ui.DeliveryViewModel
import com.example.ui.theme.*
import com.example.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationScreen(viewModel: DeliveryViewModel) {
    val notificationList by viewModel.notifications.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadNotifications() }

    Box(Modifier.fillMaxSize().background(LuxuryBlack)) {
        Column(Modifier.fillMaxSize().background(BrandGradient)) {
            ScreenScaffold(
                title = "NOTIFICATIONS",
                onBack = { viewModel.navigateBack() },
                rightContent = {
                    val unread = notificationList.count { !it.isRead }
                    if (unread > 0) {
                        Surface(color = DangerRed, shape = CircleShape) {
                            Text("$unread", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    } else {
                        Spacer(Modifier.size(48.dp))
                    }
                }
            ) {
                if (notificationList.isEmpty()) {
                    EmptyState(Icons.Default.NotificationsNone, "No notifications", "You're all caught up!")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                        items(notificationList, key = { it.id }) { item ->
                            StaggeredItem(index = notificationList.indexOf(item)) {
                                NotificationCard(item) { viewModel.markNotificationRead(item.id) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(item: NotificationItem, onClick: () -> Unit) {
    val bgColor = if (item.isRead) Color.White else BiroBlue.copy(alpha = 0.04f)
    val iconColor = when (item.type) {
        "tracking" -> BiroBlue; "wallet" -> SuccessGreen; "promo" -> Color(0xFFF97316); "referral" -> Color(0xFF8B5CF6)
        else -> TextGray
    }
    val icon = when (item.type) {
        "tracking" -> Icons.Default.LocalShipping; "wallet" -> Icons.Default.AccountBalanceWallet; "promo" -> Icons.Default.CardGiftcard; "referral" -> Icons.Default.People
        else -> Icons.Default.Notifications
    }

    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, if (item.isRead) CardBorderGray else BiroBlue.copy(alpha = 0.3f))
    ) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(iconColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.title, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    if (!item.isRead) Box(Modifier.size(8.dp).clip(CircleShape).background(BiroBlue))
                }
                Spacer(Modifier.height(4.dp))
                Text(item.message, color = TextGray, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Text(SimpleDateFormat("d MMM, h:mm a", Locale.getDefault()).format(Date(item.createdAt)), color = TextMuted, fontSize = 10.sp)
            }
        }
    }
}
