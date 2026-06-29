package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.DeliveryViewModel
import com.example.ui.DeliveryViewModelFactory
import com.example.ui.navigation.AppView
import com.example.ui.theme.*
import com.example.ui.screens.*

class NotchedNavShape(private val notchRadius: androidx.compose.ui.unit.Dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val notchRadiusPx = with(density) { notchRadius.toPx() }
        val path = Path().apply {
            moveTo(0f, 0f)
            val centerX = size.width / 2f
            lineTo(centerX - notchRadiusPx, 0f)
            arcTo(rect = Rect(left = centerX - notchRadiusPx, top = -notchRadiusPx, right = centerX + notchRadiusPx, bottom = notchRadiusPx), startAngleDegrees = 180f, sweepAngleDegrees = -180f, forceMoveTo = false)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val tempVm: DeliveryViewModel = viewModel(factory = DeliveryViewModelFactory(context.applicationContext as android.app.Application))
            val darkMode by tempVm.preferences.darkMode.collectAsState(initial = false)

            MyApplicationTheme(darkTheme = darkMode) {
                MainLayout()
            }
        }
    }
}

@Composable
fun MainLayout() {
    val context = LocalContext.current
    val vm: DeliveryViewModel = viewModel(factory = DeliveryViewModelFactory(context.applicationContext as android.app.Application))
    val currentView by vm.currentView.collectAsStateWithLifecycle()
    val showBottomBar = currentView is AppView.Dashboard || currentView is AppView.OrderLogs || currentView is AppView.Wallet || currentView is AppView.Profile

    BackHandler {
        if (currentView !is AppView.Dashboard && currentView !is AppView.Splash && currentView !is AppView.Login) {
            vm.navigateBack()
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize().background(BackgroundGray), containerColor = BackgroundGray) {
        Box(Modifier.fillMaxSize().background(BackgroundGray)) {
            Box(Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = currentView,
                    transitionSpec = {
                        (slideInHorizontally(initialOffsetX = { w -> w / 3 }, animationSpec = tween(400, easing = FastOutSlowInEasing)) + fadeIn(tween(400)))
                            .togetherWith(slideOutHorizontally(targetOffsetX = { w -> -w / 3 }, animationSpec = tween(400, easing = FastOutSlowInEasing)) + fadeOut(tween(400)))
                    },
                    label = "nav"
                ) { targetView -> Box(Modifier.fillMaxSize()) { InteractiveAppFlow(vm, targetView) } }
            }
            if (showBottomBar) {
                Box(Modifier.fillMaxWidth().align(Alignment.BottomCenter).background(Color.Transparent)) {
                    Surface(
                        color = Color.White,
                        shape = NotchedNavShape(38.dp),
                        modifier = Modifier.fillMaxWidth().height(82.dp)
                            .shadow(16.dp, NotchedNavShape(38.dp), clip = false, ambientColor = Color(0x0A000000), spotColor = Color(0x1F000000))
                            .align(Alignment.BottomCenter)
                    ) {
                        Row(Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            NavItem(Icons.Default.Home, "Home", currentView is AppView.Dashboard) { vm.navigateToRoot(AppView.Dashboard) }
                            NavItem(Icons.Default.Inventory2, "Deliveries", currentView is AppView.OrderLogs) { vm.navigateTo(AppView.OrderLogs) }
                            Spacer(Modifier.width(64.dp))
                            NavItem(Icons.Default.Work, "Shift", currentView is AppView.Wallet) { vm.navigateTo(AppView.Wallet) }
                            NavItem(Icons.Default.Person, "Profile", currentView is AppView.Profile) { vm.navigateTo(AppView.Profile) }
                        }
                    }
                    Box(Modifier.align(Alignment.TopCenter).offset(y = (-28).dp)) {
                        val haptic = LocalHapticFeedback.current
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                vm.navigateTo(AppView.Scanner)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = CircleShape, contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(60.dp)
                                .shadow(8.dp, CircleShape, ambientColor = Color(0x145C58FF), spotColor = Color(0x3D5C58FF))
                                .background(BrandGradient, CircleShape)
                        ) { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = Color.White, modifier = Modifier.size(28.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun NavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val tint by animateColorAsState(targetValue = if (isSelected) BiroBlue else Color(0xFF94A3B8), animationSpec = tween(300), label = "tint")
    Column(Modifier.width(52.dp).clip(RoundedCornerShape(16.dp)).clickable { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onClick() }.padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.height(26.dp)) { Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp)) }
        Spacer(Modifier.height(2.dp))
        Text(label, color = tint, fontSize = 9.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium, maxLines = 1)
        Spacer(Modifier.height(2.dp))
        Box(Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) BiroBlue else Color.Transparent))
    }
}

@Composable
fun InteractiveAppFlow(vm: DeliveryViewModel, v: AppView) {
    when (v) {
        is AppView.Splash -> SplashOnboardingScreen(vm)
        is AppView.Login -> LoginScreen(vm)
        is AppView.Dashboard -> DashboardScreen(vm)
        is AppView.ActiveTracking -> ActiveTrackingScreen(vm, v.trackingNumber)
        is AppView.OrderLogs -> OrderLogsScreen(vm)
        is AppView.Wallet -> WalletScreen(vm)
        is AppView.Profile -> ProfileScreen(vm)
        is AppView.Settings -> SettingsScreen(vm)
        is AppView.Notifications -> NotificationScreen(vm)
        is AppView.Scanner -> ScannerScreen(vm)
        is AppView.MapNavigation -> MapNavigationScreen(vm, v.trackingNumber)
    }
}
