package com.example.rider.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.RowScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun RiderScreenHeader(
    title: String,
    onBack: () -> Unit,
    rightContent: @Composable () -> Unit = { Spacer(Modifier.size(48.dp)) }
) {
    Box(Modifier.fillMaxWidth().statusBarsPadding().background(BrandGradient)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
            Text(title.uppercase(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
            rightContent()
        }
    }
}

@Composable
fun RiderStatusBadge(status: String) {
    val (bg, fg) = when (status) {
        "ASSIGNED" -> Color(0xFFDBEAFE) to Color(0xFF2563EB)
        "PICKED_UP" -> Color(0xFFE0E7FF) to Color(0xFF4F46E5)
        "OUT_FOR_DELIVERY" -> Color(0xFFFEF3C7) to Color(0xFFD97706)
        "DELIVERED" -> Color(0xFFD1FAE5) to Color(0xFF059669)
        "CANCELLED" -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
        else -> Color(0xFFF1F5F9) to TextGray
    }
    Surface(color = bg, shape = RoundedCornerShape(6.dp)) {
        Text(status.replace("_", " "), color = fg, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

@Composable
fun OnlineToggleButton(isOnline: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bgColor by animateColorAsState(targetValue = if (isOnline) SuccessGreen else Color(0xFF94A3B8), animationSpec = tween(300), label = "online_bg")
    val pulseScale by rememberInfiniteTransition(label = "pulse").animateFloat(1f, 1.08f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "pulse")
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(48.dp).clip(RoundedCornerShape(16.dp)).background(bgColor)
    ) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(Color.White).graphicsLayer {
            scaleX = pulseScale; scaleY = pulseScale
        })
        Spacer(Modifier.width(8.dp))
        Text(if (isOnline) "ONLINE" else "OFFLINE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
    }
}

@Composable
fun RiderScaffold(
    title: String,
    onBack: () -> Unit,
    onlineStatus: Boolean = false,
    onToggleOnline: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Box(Modifier.fillMaxSize().background(LuxuryBlack)) {
        Column(Modifier.fillMaxSize().background(BrandGradient)) {
            RiderScreenHeader(title = title, onBack = onBack, rightContent = {
                if (onlineStatus) OnlineToggleButton(onlineStatus, onToggleOnline)
            })
            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundGray),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 100.dp), content = content)
            }
        }
    }
}

@Composable
fun RowScope.RiderStatCard(value: String, label: String, icon: ImageVector, iconColor: Color = BiroBlue) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.weight(1f)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(32.dp).clip(CircleShape).background(iconColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp)) }
            Spacer(Modifier.height(6.dp)); Text(value, color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            Text(label, color = TextMuted, fontSize = 9.sp, textAlign = TextAlign.Center, lineHeight = 12.sp)
        }
    }
}
