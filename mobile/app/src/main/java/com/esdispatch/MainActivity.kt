package com.esdispatch

// Font scale locked to 1.0 via attachBaseContext
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.esdispatch.viewmodel.ToastData
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
import com.esdispatch.ui.screens.*
import com.esdispatch.ui.theme.MyApplicationTheme
import com.esdispatch.ui.theme.Gold
import com.esdispatch.ui.theme.Obsidian
import com.esdispatch.ui.theme.GoldenWhite
import com.esdispatch.ui.theme.GoldenWhiteLight
import com.esdispatch.ui.theme.GoldenWhiteSurface
import com.esdispatch.ui.theme.TextGray
import com.esdispatch.ui.theme.Hugeicons
import com.esdispatch.ui.theme.AnimatedHugeIcon
import com.esdispatch.viewmodel.DeliveryViewModel
import com.esdispatch.viewmodel.ToastType
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
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {
    private lateinit var viewModel: DeliveryViewModel

    override fun attachBaseContext(newBase: android.content.Context?) {
        if (newBase == null) {
            super.attachBaseContext(newBase)
            return
        }
        super.attachBaseContext(newBase)
    }

    private fun showPendingCrashReport() {
        try {
            val prefs = getSharedPreferences(com.esdispatch.DispatchApplication.PREFS, android.content.Context.MODE_PRIVATE)
            val crash = prefs.getString(com.esdispatch.DispatchApplication.KEY_LAST_CRASH, null)
            if (crash.isNullOrBlank()) return
            prefs.edit().remove(com.esdispatch.DispatchApplication.KEY_LAST_CRASH).apply()
            android.app.AlertDialog.Builder(this)
                .setTitle("ESDispatch Crash Report")
                .setMessage("The app encountered an unexpected error. Details:\n\n" + crash.take(3000))
                .setPositiveButton("OK") { d, _ -> d.dismiss() }
                .setNeutralButton("Copy") { d, _ ->
                    try {
                        val clip = android.content.ClipData.newPlainText("esdispatch_crash", crash)
                        (getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager)
                            ?.setPrimaryClip(clip)
                    } catch (e: Throwable) {
                        android.util.Log.w("MainActivity", "Clipboard copy failed: ${e.message}")
                    }
                    d.dismiss()
                }
                .setCancelable(false)
                .show()
        } catch (e: Throwable) {
            android.util.Log.w("MainActivity", "Crash report dialog failed: ${e.message}")
        }
    }

    private var lastBackgroundTime = 0L

    override fun onPause() {
        super.onPause()
        lastBackgroundTime = System.currentTimeMillis()
        checkAndLockApp()
    }

    override fun onStop() {
        super.onStop()
        checkAndLockApp()
    }

    private fun checkAndLockApp() {
        if (!::viewModel.isInitialized) return
        if (viewModel.isGoogleAuthInProgress.value) return

        // STRICT: An active rider shift session is NOT subject to customer PIN lock!
        // When user is a rider and is currently online/working, do NOT lock the app on screen off/minimize.
        val isRiderWorking = (viewModel.userRole.value == "rider" || viewModel.activeViewMode.value == "rider") && viewModel.isOnline.value
        if (isRiderWorking) {
            return
        }

        val prefs = getSharedPreferences("esdispatch_prefs", android.content.Context.MODE_PRIVATE)
        val hasLocalUser = !prefs.getString("local_uid", "").isNullOrEmpty() ||
                viewModel.firebaseUserId.value?.isNotBlank() == true ||
                viewModel.userEmail.value.isNotBlank()
        val hasFirebaseUser = com.esdispatch.data.FirebaseManager.auth?.currentUser != null

        val hasPin = viewModel.hasConfiguredPin()
        val hasBiometric = viewModel.biometricEnabled.value && viewModel.biometricRegistered.value

        if ((hasLocalUser || hasFirebaseUser) && (hasPin || hasBiometric)) {
            viewModel.lockApp()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: android.content.Intent?) {
        val shortcutRoute = intent?.getStringExtra("shortcut_route")
        if (shortcutRoute != null) {
            viewModel.setPendingShortcutRoute(shortcutRoute)
        }
        val action = intent?.getStringExtra("action")
        val notifParcelId = intent?.getStringExtra("parcelId")
        val notifType = intent?.getStringExtra("type")
        val isRiderAlert = action == "rider_manifest" || notifType == "NEW_DISPATCH" || notifParcelId == "DISPATCH"

        if (isRiderAlert) {
            if (!notifParcelId.isNullOrBlank() && notifParcelId != "DISPATCH") {
                viewModel.selectDispatchForRider(notifParcelId)
            }
            viewModel.setPendingShortcutRoute("RiderDeliveries")
        } else if (!notifParcelId.isNullOrBlank() && notifParcelId != "GIFT" && notifParcelId != "OTP") {
            viewModel.selectParcelForTracking(notifParcelId)
            viewModel.setPendingShortcutRoute("Tracking")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        viewModel = androidx.lifecycle.ViewModelProvider(this)[DeliveryViewModel::class.java]
        setupShortcuts()

        showPendingCrashReport()

        handleIncomingIntent(intent)

        val permsToRequest = mutableListOf<String>()
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            permsToRequest.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
            permsToRequest.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            permsToRequest.add(android.Manifest.permission.CAMERA)
        }
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permsToRequest.add(permission)
            }
        }
        if (permsToRequest.isNotEmpty()) {
            androidx.core.app.ActivityCompat.requestPermissions(this, permsToRequest.distinct().toTypedArray(), 101)
        }

        enableEdgeToEdge()
        com.esdispatch.util.SoundManager.initialize(this)
        com.esdispatch.data.MyFirebaseMessagingService.createNotificationChannels(this)
        setContent {
            val context = androidx.compose.ui.platform.LocalContext.current
            val soundEnabled by viewModel.soundEffectsEnabled.collectAsState()
            val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
            LaunchedEffect(soundEnabled, hapticsEnabled) {
                com.esdispatch.util.SoundManager.setPreferences(soundEnabled, hapticsEnabled)
            }
            LaunchedEffect(Unit) {
                viewModel.initializeDatabase(context)
            }
            val currentAuthUid by viewModel.firebaseUserId.collectAsState()
            LaunchedEffect(currentAuthUid) {
                val uid = currentAuthUid
                if (!uid.isNullOrBlank() && !uid.startsWith("local_user_")) {
                    try {
                        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                            .addOnSuccessListener { token ->
                                if (token.isNotBlank()) {
                                    com.esdispatch.data.FirebaseManager.updateFcmTokenInFirestore(uid, token)
                                }
                            }
                    } catch (e: Exception) {
                        android.util.Log.w("MainActivity", "FCM initial token fetch failed: ${e.message}")
                    }
                }
            }
            LaunchedEffect(Unit) {
                com.esdispatch.data.FirebaseManager.fcmNotifications.collect { pair ->
                    val title = pair.first
                    val message = pair.second
                    viewModel.addNotification(title, message)
                    viewModel.showInAppNotification(title, message)
                }
            }
            val darkModeEnabled by viewModel.darkModeEnabled.collectAsState()
            val view = androidx.compose.ui.platform.LocalView.current
            LaunchedEffect(darkModeEnabled) {
                val window = (context as? android.app.Activity)?.window
                if (window != null) {
                    // Light mode headers are Obsidian (#0D0D11) -> icons must be white (isAppearanceLightStatusBars = false)
                    // Dark mode headers are Gold (#FFB800) -> icons must be dark (isAppearanceLightStatusBars = true)
                    androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkModeEnabled
                    androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkModeEnabled
                }
            }

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
                val customToastData by viewModel.customToastData.collectAsState()
                val marketplaceEnabled by viewModel.marketplaceEnabled.collectAsState()
                val networkOnline by viewModel.networkOnline.collectAsState()

                val handleNavigation: (String) -> Unit = { route ->
                    val effectiveRoute = if (!marketplaceEnabled && (route == "Marketplace" || route.startsWith("VendorStorefront") || route == "VendorPortal" || route.startsWith("VendorProfile"))) {
                        "Dashboard"
                    } else {
                        route
                    }
                    try {
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        when (effectiveRoute) {
                            "BACK" -> {
                                if (currentRoute == "Dashboard" || currentRoute == "RiderDashboard") {
                                    // Root dashboard level: BackHandler on the screen handles double-back exit confirmation.
                                    // Never pop backstack here to prevent popping into auth/login screens!
                                } else if (currentRoute in listOf("OrderLogs", "Marketplace", "Profile", "Wallet")) {
                                    navController.navigate("Dashboard") {
                                        popUpTo("Dashboard") { inclusive = false }
                                        launchSingleTop = true
                                    }
                                } else {
                                    val popped = navController.popBackStack()
                                    if (!popped) {
                                        navController.navigate("Dashboard") {
                                            popUpTo(0) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            }
                            "Dashboard" -> {
                                val isComingFromAuth = currentRoute in listOf(
                                    "Splash", "Preloader", "Onboarding", "Login", "SignUp", "CompleteProfile", "AppLock"
                                ) || currentRoute?.startsWith("Preloader") == true

                                if (isComingFromAuth) {
                                    navController.navigate("Dashboard") {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else {
                                    navController.navigate("Dashboard") {
                                        popUpTo("Dashboard") {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            }
                            "RiderDashboard", "RiderDeliveries" -> {
                                navController.navigate("RiderDashboard") {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                            "Login" -> {
                                navController.navigate("Login") {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                            "Onboarding" -> {
                                navController.navigate("Onboarding") {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                            "Preloader" -> {
                                navController.navigate("Preloader") {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                            "OrderLogs", "Marketplace", "Profile", "Wallet" -> {
                                navController.navigate(effectiveRoute) {
                                    popUpTo("Dashboard") {
                                        saveState = true
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            "SendParcel" -> {
                                navController.navigate("SendParcel") {
                                    popUpTo("Dashboard") {
                                        saveState = true
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            }
                            "PaymentSuccess" -> {
                                navController.navigate("PaymentSuccess") {
                                    popUpTo("Dashboard") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                            "Tracking", "ActiveTracking" -> {
                                navController.navigate("ActiveTracking") {
                                    popUpTo("Dashboard") {
                                        saveState = true
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            }
                            else -> {
                                navController.navigate(effectiveRoute) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    } catch (e: Throwable) {
                        android.util.Log.e("MainActivity", "Navigation failure for route '$route': ${e.message}")
                    }
                }

                LaunchedEffect(marketplaceEnabled) {
                    if (!marketplaceEnabled) {
                        val currentDest = navController.currentDestination?.route
                        if (currentDest == "Marketplace" || currentDest?.startsWith("Vendor") == true) {
                            navController.navigate("Dashboard") {
                                popUpTo("Dashboard") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    }
                }

                val pendingShortcutRoute by viewModel.pendingShortcutRoute.collectAsState()
                LaunchedEffect(pendingShortcutRoute) {
                    pendingShortcutRoute?.let { route ->
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        if (currentRoute != "Splash" && currentRoute != "Onboarding" && currentRoute != "Login" && currentRoute != "SignUp") {
                            handleNavigation(route)
                            viewModel.clearPendingShortcutRoute()
                        }
                    }
                }

                val isFirebaseConfigured by viewModel.isFirebaseConfigured.collectAsState()
                val maintenanceMode by viewModel.maintenanceMode.collectAsState()
                val isAppLocked by viewModel.isAppLocked.collectAsState()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
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
                        SplashScreen(viewModel = viewModel, onNavigate = { target ->
                            navController.navigate(target) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        })
                    }
                    composable("AppLock") {
                        AppLockScreen(
                            viewModel = viewModel,
                            onUnlocked = {
                                navController.navigate("Preloader/Dashboard") {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onSignOut = {
                                viewModel.logout()
                                navController.navigate("Login") {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable("Onboarding") {
                        OnboardingScreen(viewModel = viewModel, onNavigate = { target ->
                            navController.navigate(target) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        })
                    }
                    composable("Login") {
                        LoginScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("SignUp") {
                        SignUpScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("CompleteProfile") {
                        CompleteProfileScreen(viewModel = viewModel, onNavigate = { target ->
                            navController.navigate(target) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        })
                    }
                    composable("Preloader") {
                        PreloaderScreen(viewModel = viewModel, onNavigate = { target ->
                            navController.navigate(target) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }, nextRoute = "Dashboard")
                    }
                    composable("Preloader/{nextRoute}") { backStackEntry ->
                        val nextRoute = backStackEntry.arguments?.getString("nextRoute") ?: "Onboarding"
                        PreloaderScreen(viewModel = viewModel, onNavigate = { target ->
                            navController.navigate(target) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }, nextRoute = nextRoute)
                    }

                    // Main App Shell
                    composable("Dashboard") {
                        DashboardScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("Marketplace") {
                        MarketplaceScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("VendorPortal") {
                        VendorPortalScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("VendorStorefront/{vendorId}") { backStackEntry ->
                        val vendorId = backStackEntry.arguments?.getString("vendorId") ?: ""
                        VendorStorefrontScreen(viewModel = viewModel, vendorId = vendorId, onNavigate = handleNavigation)
                    }
                    composable("VendorProfile/{vendorId}") { backStackEntry ->
                        val vendorId = backStackEntry.arguments?.getString("vendorId") ?: ""
                        VendorProfileScreen(vendorId = vendorId, viewModel = viewModel, onNavigate = handleNavigation, onBack = { handleNavigation("BACK") })
                    }
                    composable("OrderLogs") {
                        OrderLogsScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("Profile") {
                        ProfileScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }

                    // Booking Flow — 4 Canonical Services Only
                    composable("SendParcel") {
                        ServiceSelectionScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("SendParcelDetails") {
                        ExpressBookingScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("ExpressBooking") {
                        ExpressBookingScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("EconomyBooking") {
                        EconomyBookingScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("BatchBooking") {
                        BatchBookingScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("MultiBooking") {
                        MultiBookingScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("BookingForm") {
                        ExpressBookingScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("BookingDetails") {
                        ExpressBookingScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("BookingSelection") {
                        ServiceSelectionScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("PaymentSuccess") {
                        PaymentSuccessScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }

                    // Extras & Tools
                    composable("ActiveTracking") {
                        ActiveTrackingScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("Tracking") {
                        ActiveTrackingScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("Scanner") {
                        ScannerScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("ProofOfDelivery/{parcelId}") { backStackEntry ->
                        val parcelId = backStackEntry.arguments?.getString("parcelId") ?: ""
                        com.esdispatch.ui.screens.ProofOfDeliveryScreen(navController = navController, viewModel = viewModel, parcelId = parcelId)
                    }
                    composable("CustomerAssistant") {
                        CustomerAssistantScreen(viewModel = viewModel, onBack = { handleNavigation("BACK") })
                    }
                    composable("AIDispatchManager") {
                        AIDispatchManagerScreen(viewModel = viewModel, onBack = { handleNavigation("BACK") })
                    }

                    // Profile settings, reviews & options
                    composable("Wallet") {
                        WalletScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("Settings") {
                        SettingsScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("NotificationSettings") {
                        NotificationSettingsScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("RiderReview") {
                        RiderReviewScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("AddressBook") {
                        AddressBookScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("Notifications") {
                        NotificationsScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("Promotions") {
                        PromotionsScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("Referral") {
                        ReferralScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("RiderDashboard") {
                        RiderDashboardScreen(viewModel = viewModel, onNavigate = handleNavigation)
                    }
                    composable("RiderDeliveries") {
                        RiderDashboardScreen(viewModel = viewModel, onNavigate = handleNavigation)
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

                // Custom Luxury Toast Notification Pill Overlay
                var activeToastData by remember { mutableStateOf<ToastData?>(null) }
                val toastScope = rememberCoroutineScope()
                var dismissJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

                LaunchedEffect(Unit) {
                    com.esdispatch.util.CustomToastBridge.toastFlow.collect { data ->
                        activeToastData = data
                        dismissJob?.cancel()
                        dismissJob = toastScope.launch {
                            delay(3200)
                            if (activeToastData?.id == data.id) {
                                activeToastData = null
                            }
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    viewModel.customToastData.collect { data ->
                        if (data != null) {
                            activeToastData = data
                            dismissJob?.cancel()
                            dismissJob = toastScope.launch {
                                delay(3200)
                                if (activeToastData?.id == data.id) {
                                    activeToastData = null
                                }
                            }
                        }
                    }
                }

                // Offline Connectivity Status Banner (Subtle, non-disruptive, auto-reconnecting, debounced)
                var showOfflineBanner by remember { mutableStateOf(false) }
                LaunchedEffect(networkOnline) {
                    if (!networkOnline) {
                        delay(2500)
                        showOfflineBanner = true
                    } else {
                        showOfflineBanner = false
                    }
                }

                AnimatedVisibility(
                    visible = showOfflineBanner,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 8.dp)
                        .zIndex(9995f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (darkModeEnabled) Color(0xFF1E1C14) else Color(0xFFFFF8E7),
                        border = BorderStroke(1.dp, if (darkModeEnabled) Gold.copy(alpha = 0.6f) else Obsidian.copy(alpha = 0.25f)),
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(Color(0xFFFFB800), shape = CircleShape)
                            )
                            Text(
                                text = "Offline Mode • Actions will sync when reconnected",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (darkModeEnabled) Gold else Obsidian
                            )
                        }
                    }
                }

                val currentToast = activeToastData
                AnimatedVisibility(
                    visible = currentToast != null,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 16.dp)
                        .padding(horizontal = 20.dp)
                        .zIndex(9999f)
                ) {
                    if (currentToast != null) {
                        val toastType = currentToast.type
                        val accentColor = when (toastType) {
                            ToastType.SUCCESS -> if (darkModeEnabled) Gold else Color(0xFF10B981) // Emerald in light, Gold in dark
                            ToastType.ERROR -> Color(0xFFFF5252)
                            ToastType.WARNING -> Color(0xFFFFB800)
                            ToastType.INFO -> if (darkModeEnabled) Gold else Obsidian
                        }
                        val toastIcon = when (toastType) {
                            ToastType.SUCCESS -> Hugeicons.Solid.CheckCircle
                            ToastType.ERROR -> Hugeicons.Solid.AlertTriangle
                            ToastType.WARNING -> Hugeicons.Solid.AlertTriangle
                            ToastType.INFO -> Hugeicons.Solid.Bell
                        }

                        Surface(
                            shape = RoundedCornerShape(26.dp),
                            color = if (darkModeEnabled) Color(0xFF161618) else GoldenWhiteSurface,
                            border = BorderStroke(1.5.dp, accentColor),
                            shadowElevation = 8.dp,
                            modifier = Modifier.clickable { 
                                viewModel.dismissCustomToast()
                                dismissJob?.cancel()
                                activeToastData = null
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AnimatedHugeIcon(
                                    icon = toastIcon,
                                    contentDescription = null,
                                    tint = accentColor,
                                    size = 20.dp
                                )
                                Text(
                                    text = currentToast.message,
                                    fontSize = 13.sp,
                                    color = if (darkModeEnabled) Color.White else Obsidian,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (maintenanceMode) {
                    ConfigurationErrorScreen(isDark = darkModeEnabled)
                    return@Box
                }

                if (isAppLocked) {
                    AppLockScreen(
                        viewModel = viewModel,
                        onUnlocked = {
                            viewModel.unlockApp()
                        },
                        onSignOut = {
                            viewModel.unlockApp()
                            viewModel.logout()
                            navController.navigate("Login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                    return@Box
                }
        }
    }
}
}
}

    private fun setupShortcuts() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            try {
                val context = this
                val sendParcelShortcut = androidx.core.content.pm.ShortcutInfoCompat.Builder(context, "shortcut_send_parcel")
                    .setShortLabel("Send Parcel")
                    .setLongLabel("Premium Courier Booking")
                    .setIcon(androidx.core.graphics.drawable.IconCompat.createWithResource(context, R.drawable.ic_shortcut_send))
                    .setIntent(
                        android.content.Intent(context, MainActivity::class.java).apply {
                            action = "com.esdispatch.ACTION_SHORTCUT"
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
                            action = "com.esdispatch.ACTION_SHORTCUT"
                            putExtra("shortcut_route", "ActiveTracking")
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
                            action = "com.esdispatch.ACTION_SHORTCUT"
                            putExtra("shortcut_route", "Wallet")
                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                    .build()

                androidx.core.content.pm.ShortcutManagerCompat.setDynamicShortcuts(context, listOf(sendParcelShortcut, trackParcelShortcut, walletShortcut))
            } catch (e: Throwable) {
                android.util.Log.w("MainActivity", "Launcher shortcut registration skipped: ${e.message}")
            }
        }
    }
}

@Composable
fun ConfigurationErrorScreen(isDark: Boolean) {
    val backgroundColor = if (isDark) Obsidian else GoldenWhite
    val textColor = if (isDark) Color.White else Obsidian
    val surfaceColor = if (isDark) Color(0xFF1E1E1E) else GoldenWhiteLight
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
            contentDescription = "ESDispatch Logo",
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Gold),
            modifier = Modifier.size(54.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "ESDISPATCH",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = if (isDark) Gold else Obsidian,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "PREMIUM LOGISTICS & DISPATCH",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) TextGray else Color.Gray,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = surfaceColor,
            border = BorderStroke(1.dp, cardBorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
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
                    text = "ESDispatch is currently undergoing secure maintenance upgrades. We will be back to serve your logistics needs in a moment!",
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.8f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
