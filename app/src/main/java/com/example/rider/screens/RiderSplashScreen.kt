package com.example.rider.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.flow.first
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rider.RiderViewModel
import com.example.rider.navigation.RiderView
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun RiderSplashScreen(viewModel: RiderViewModel) {
    var phase by remember { mutableIntStateOf(0) }
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (viewModel.preferences.isLoggedIn.first() && viewModel.preferences.isRider.first()) {
            viewModel.loadRiderData()
            viewModel.navigateToRoot(RiderView.Dashboard)
            return@LaunchedEffect
        }
        delay(2200)
        showSplash = false
    }

    if (showSplash) {
        Box(Modifier.fillMaxSize().background(LuxuryBlack), contentAlignment = Alignment.Center) {
            val infiniteTransition = rememberInfiniteTransition(label = "rider_splash")
            val orb1X by infiniteTransition.animateFloat(0f, 1f, infiniteRepeatable(tween(3000), RepeatMode.Reverse), label = "orb1")
            val orb2X by infiniteTransition.animateFloat(1f, 0f, infiniteRepeatable(tween(4000), RepeatMode.Reverse), label = "orb2")

            Canvas(Modifier.fillMaxSize()) {
                drawCircle(Color(0xFF5C58FF).copy(alpha = 0.08f), radius = size.width * 0.6f, center = Offset(size.width * orb1X, size.height * 0.3f))
                drawCircle(Color(0xFF10B981).copy(alpha = 0.06f), radius = size.width * 0.5f, center = Offset(size.width * orb2X, size.height * 0.7f))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val logoScale by animateFloatAsState(targetValue = 1f, animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessLow), label = "logo")
                Box(Modifier.size(120.dp).scale(logoScale).background(BrandGradient, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = Color.White, modifier = Modifier.size(56.dp))
                }
                Spacer(Modifier.height(20.dp))
                Text("ENGRACED", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = 6.sp)
                Text("RIDER HUB", color = BiroBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(3) { i ->
                        val p by animateFloatAsState(targetValue = if (i == 0) 1f else 0.3f, animationSpec = infiniteRepeatable(tween(600 + i * 200)), label = "dot")
                        Box(Modifier.size(8.dp).clip(CircleShape).background(BiroBlue.copy(alpha = p)))
                    }
                }
            }
        }
    } else {
        RiderOnboarding(viewModel)
    }
}

@Composable
private fun RiderOnboarding(viewModel: RiderViewModel) {
    var page by remember { mutableIntStateOf(0) }

    Box(Modifier.fillMaxSize().background(LuxuryBlack), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            val items = listOf(
                Triple(Icons.Default.TwoWheeler, "Assigned Deliveries", "Get real-time delivery assignments straight from dispatch. No more waiting."),
                Triple(Icons.Default.NearMe, "Live Navigation", "GPS-powered turn-by-turn directions to pickup and drop-off points."),
                Triple(Icons.Default.Verified, "OTP Verification", "Secure delivery completion with customer OTP and photo proof.")
            )
            val (icon, title, desc) = items[page]

            Box(Modifier.size(120.dp).background(BrandGradient, RoundedCornerShape(30.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(56.dp))
            }
            Spacer(Modifier.height(32.dp))
            Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(desc, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, textAlign = TextAlign.Center)

            Spacer(Modifier.height(48.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { i ->
                    Box(Modifier.size(if (i == page) 28.dp else 8.dp).height(8.dp).clip(RoundedCornerShape(4.dp)).background(if (i == page) BiroBlue else Color.White.copy(alpha = 0.2f)))
                }
            }

            Spacer(Modifier.height(40.dp))
            Button(
                onClick = { if (page < 2) page++ else viewModel.navigateTo(RiderView.Login) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp).background(BrandGradient, RoundedCornerShape(16.dp))
            ) { Text(if (page < 2) "Next" else "Get Started", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold) }

            if (page < 2) {
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = { viewModel.navigateTo(RiderView.Login) }) { Text("Skip", color = Color.White.copy(alpha = 0.5f)) }
            }
        }
    }
}

private fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer(scaleX = scale, scaleY = scale, alpha = scale.coerceIn(0.5f, 1f))
)
