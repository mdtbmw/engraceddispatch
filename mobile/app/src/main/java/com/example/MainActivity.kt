package com.example

// Font scale locked to 1.0 via attachBaseContext
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Gold
import com.example.ui.theme.Obsidian
import com.example.ui.theme.GoldenWhiteLight
import com.example.viewmodel.DeliveryViewModel
import androidx.compose.foundation.Image
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.Alignment
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import com.example.data.TelemetrySyncWorker
import java.util.concurrent.TimeUnit
class MainActivity : ComponentActivity() {
    private lateinit var viewModel: DeliveryViewModel

    override fun attachBaseContext(newBase: android.content.Context) {
        val config = android.content.res.Configuration(newBase.resources.configuration)
        config.fontScale = 1.0f
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        viewModel = androidx.lifecycle.ViewModelProvider(this)[DeliveryViewModel::class.java]
        setupShortcuts()

        val shortcutRoute = intent?.getStringExtra("shortcut_route")
        if (shortcutRoute != null) {
            viewModel.setPendingShortcutRoute(shortcutRoute)
        }
        val parcelId = intent?.getStringExtra("parcelId")
        if (parcelId != null) {
            viewModel.selectParcelForTracking(parcelId)
            viewModel.setPendingShortcutRoute("ActiveTracking")
        }

        // Schedule WorkManager background sync
        val syncConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val syncWorkRequest = PeriodicWorkRequestBuilder<TelemetrySyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(syncConstraints)
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "TelemetrySyncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )

        if (android.os.Build.VERSION.SDK_INT >= 33) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(permission), 101)
            }
        }

        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = androidx.activity.SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            val context = androidx.compose.ui.platform.LocalContext.current
            LaunchedEffect(Unit) {
                viewModel.initializeDatabase(context)
            }
            LaunchedEffect(Unit) {
                com.example.data.FirebaseManager.fcmNotifications.collect { pair ->
                    val title = pair.first
                    val message = pair.second
                    viewModel.addNotification(title, message)
                    viewModel.showInAppNotification(title, message)
                }
            }
            val darkModeEnabled by viewModel.darkModeEnabled.collectAsState()

            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(
                    density = androidx.compose.ui.platform.LocalDensity.current.density,
                    fontScale = 1.0f
                )
            ) {
                MyApplicationTheme(darkTheme = darkModeEnabled) {
                val navController = rememberNavController()
                val activeNotification by viewModel.activeInAppNotification.collectAsState()
                val customToast by viewModel.customToast.collectAsState()

                val pendingShortcutRoute by viewModel.pendingShortcutRoute.collectAsState()
                LaunchedEffect(pendingShortcutRoute) {
                    pendingShortcutRoute?.let { route ->
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        if (currentRoute != "Splash" && currentRoute != "Onboarding" && currentRoute != "Login" && currentRoute != "SignUp") {
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                            viewModel.clearPendingShortcutRoute()
                        }
                    }
                }

                val isFirebaseConfigured by viewModel.isFirebaseConfigured.collectAsState()
                val maintenanceMode by viewModel.maintenanceMode.collectAsState()

                if (!isFirebaseConfigured || maintenanceMode) {
                    ConfigurationErrorScreen(isDark = darkModeEnabled)
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        NavHost(
                            navController = navController,
                    startDestination = "Splash",
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = {
                        scaleIn(
                            initialScale = 0.95f,
                            animationSpec = tween(400, easing = EaseInOutQuart)
                        ) + fadeIn(animationSpec = tween(400))
                    },
                    exitTransition = {
                        scaleOut(
                            targetScale = 1.05f,
                            animationSpec = tween(400, easing = EaseInOutQuart)
                        ) + fadeOut(animationSpec = tween(400))
                    },
                    popEnterTransition = {
                        scaleIn(
                            initialScale = 1.05f,
                            animationSpec = tween(400, easing = EaseInOutQuart)
                        ) + fadeIn(animationSpec = tween(400))
                    },
                    popExitTransition = {
                        scaleOut(
                            targetScale = 0.95f,
                            animationSpec = tween(400, easing = EaseInOutQuart)
                        ) + fadeOut(animationSpec = tween(400))
                    }
                ) {
                    // Onboarding flow
                    composable("Splash") {
                        SplashScreen(viewModel = viewModel, onNavigate = {
                            navController.navigate(it) {
                                popUpTo("Splash") { inclusive = true }
                            }
                        })
                    }
                    composable("Onboarding") {
                        OnboardingScreen(viewModel = viewModel, onNavigate = {
                            navController.navigate(it) {
                                popUpTo("Onboarding") { inclusive = true }
                            }
                        })
                    }
                    composable("Login") {
                        LoginScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                    composable("SignUp") {
                        SignUpScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                    composable("CompleteProfile") {
                        CompleteProfileScreen(viewModel = viewModel, onNavigate = {
                            navController.navigate(it) {
                                popUpTo("CompleteProfile") { inclusive = true }
                            }
                        })
                    }
                    composable("Preloader") {
                        PreloaderScreen(viewModel = viewModel, onNavigate = {
                            navController.navigate(it) {
                                popUpTo("Preloader") { inclusive = true }
                            }
                        }, nextRoute = "Dashboard")
                    }
                    composable("Preloader/{nextRoute}") { backStackEntry ->
                        val nextRoute = backStackEntry.arguments?.getString("nextRoute") ?: "Onboarding"
                        PreloaderScreen(viewModel = viewModel, onNavigate = {
                            navController.navigate(it) {
                                popUpTo("Preloader/{nextRoute}") { inclusive = true }
                            }
                        }, nextRoute = nextRoute)
                    }

                    // Main App Shell
                    composable("Dashboard") {
                        DashboardScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                    composable("OrderLogs") {
                        OrderLogsScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                    composable("Profile") {
                        ProfileScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }

                    // Booking Flow
                    composable("SendParcel") {
                        ServiceSelectionScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                    composable("SendParcelDetails") {
                        SendParcelScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                    composable("ExpressBooking") {
                        ExpressBookingScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                    composable("EconomyBooking") {
                        EconomyBookingScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                    composable("BatchBooking") {
                        BatchBookingScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                    composable("MultiBooking") {
                        MultiBookingScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                    composable("BookingForm") {
                        BookingFormScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                    composable("BookingDetails") {
                        BookingDetails(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                    composable("BookingSelection") {
                        BookingSelectionScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                    composable("PaymentSuccess") {
                        PaymentSuccessScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }

                    // Extras & Tools
                    composable("ActiveTracking") {
                        ActiveTrackingScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                    composable("Scanner") {
                        ScannerScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                    composable("CustomerAssistant") {
                        CustomerAssistantScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }
                    composable("AIDispatchManager") {
                        AIDispatchManagerScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }

                    // Profile settings, reviews & options
                    composable("Wallet") {
                        WalletScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                    composable("Settings") {
                        SettingsScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                    composable("NotificationSettings") {
                        NotificationSettingsScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                    composable("RiderReview") {
                        RiderReviewScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                    composable("AddressBook") {
                        AddressBookScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                    composable("Notifications") {
                        NotificationsScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                    composable("Promotions") {
                        PromotionsScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                    composable("Referral") {
                        ReferralScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
                    }
                }

                // Foreground notification Toast banner UI
                AnimatedVisibility(
                    visible = activeNotification != null,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp)
                        .padding(horizontal = 16.dp)
                        .zIndex(99f)
                ) {
                    activeNotification?.let { (title, msg) ->
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Obsidian,
                            border = BorderStroke(1.5.dp, Gold),
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.dismissInAppNotification() }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.NotificationsActive,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = msg,
                                        fontSize = 12.sp,
                                        color = GoldenWhiteLight,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { viewModel.dismissInAppNotification() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Dismiss",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Custom Toast Notification Overlay (Obsidian-Gold Theme)
                AnimatedVisibility(
                    visible = customToast != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 96.dp) // shift upwards to clear bottom floating dock easily
                        .padding(horizontal = 24.dp)
                        .zIndex(200f)
                ) {
                    customToast?.let { toastMsg ->
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Obsidian,
                            border = BorderStroke(1.5.dp, Gold),
                            shadowElevation = 8.dp,
                            modifier = Modifier.clickable { viewModel.dismissCustomToast() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = toastMsg,
                                    fontSize = 13.sp,
                                    color = Gold,
                                    fontWeight = FontWeight.Bold
                                )
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

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val shortcutRoute = intent.getStringExtra("shortcut_route")
        if (shortcutRoute != null) {
            viewModel.setPendingShortcutRoute(shortcutRoute)
        }
        val parcelId = intent.getStringExtra("parcelId")
        if (parcelId != null) {
            viewModel.selectParcelForTracking(parcelId)
            viewModel.setPendingShortcutRoute("ActiveTracking")
        }
    }

    private fun setupShortcuts() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            val context = this
            val sendParcelShortcut = androidx.core.content.pm.ShortcutInfoCompat.Builder(context, "shortcut_send_parcel")
                .setShortLabel("Send Parcel")
                .setLongLabel("Premium Courier Booking")
                .setIcon(androidx.core.graphics.drawable.IconCompat.createWithResource(context, R.drawable.ic_shortcut_send))
                .setIntent(
                    android.content.Intent(context, MainActivity::class.java).apply {
                        action = "com.example.ACTION_SHORTCUT"
                        putExtra("shortcut_route", "SendParcel")
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )
                .build()

            val trackParcelShortcut = androidx.core.content.pm.ShortcutInfoCompat.Builder(context, "shortcut_track_parcel")
                .setShortLabel("Track Status")
                .setLongLabel("Real-time Courier Tracking")
                .setIcon(androidx.core.graphics.drawable.IconCompat.createWithResource(context, R.drawable.ic_shortcut_track))
                .setIntent(
                    android.content.Intent(context, MainActivity::class.java).apply {
                        action = "com.example.ACTION_SHORTCUT"
                        putExtra("shortcut_route", "ActiveTracking")
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )
                .build()

            val aiDispatchShortcut = androidx.core.content.pm.ShortcutInfoCompat.Builder(context, "shortcut_ai_dispatch")
                .setShortLabel("AI Dispatch")
                .setLongLabel("AI Dispatch & Logistics Assistant")
                .setIcon(androidx.core.graphics.drawable.IconCompat.createWithResource(context, R.drawable.ic_shortcut_ai))
                .setIntent(
                    android.content.Intent(context, MainActivity::class.java).apply {
                        action = "com.example.ACTION_SHORTCUT"
                        putExtra("shortcut_route", "CustomerAssistant")
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )
                .build()

            val walletShortcut = androidx.core.content.pm.ShortcutInfoCompat.Builder(context, "shortcut_wallet")
                .setShortLabel("Premium Wallet")
                .setLongLabel("Manage Fund & Wallet Balance")
                .setIcon(androidx.core.graphics.drawable.IconCompat.createWithResource(context, R.drawable.ic_shortcut_wallet))
                .setIntent(
                    android.content.Intent(context, MainActivity::class.java).apply {
                        action = "com.example.ACTION_SHORTCUT"
                        putExtra("shortcut_route", "Wallet")
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )
                .build()

            androidx.core.content.pm.ShortcutManagerCompat.setDynamicShortcuts(context, listOf(sendParcelShortcut, trackParcelShortcut, aiDispatchShortcut, walletShortcut))
        }
    }
}

@Composable
fun ConfigurationErrorScreen(isDark: Boolean) {
    val backgroundColor = if (isDark) Obsidian else Color.White
    val textColor = if (isDark) Color.White else Obsidian
    val surfaceColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF9F9F9)
    val cardBorderColor = if (isDark) Gold else Obsidian.copy(alpha = 0.1f)

    // Animated rotation for a friendly waving hand 👋
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveRotation by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveRot"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo
        Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_logo),
            contentDescription = "Engraced Dispatch Logo",
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Gold),
            modifier = Modifier
                .size(100.dp)
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ENGRACED DISPATCH",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isDark) Gold else Obsidian,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "PREMIUM LOGISTICS & DISPATCH",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White.copy(alpha = 0.6f) else Obsidian.copy(alpha = 0.6f),
            letterSpacing = 2.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            border = BorderStroke(1.5.dp, cardBorderColor)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Friendly Wave icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background((if (isDark) Gold else Obsidian).copy(alpha = 0.1f), shape = CircleShape)
                        .graphicsLayer {
                            rotationZ = waveRotation
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👋",
                        fontSize = 38.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "UNDER MAINTENANCE",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDark) Gold else Obsidian,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Engraced Dispatch is currently undergoing secure maintenance upgrades. We will be back to serve your logistics needs in a moment!",
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.8f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
