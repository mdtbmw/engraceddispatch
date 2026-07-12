package com.example.ui.screens

import com.example.BuildConfig
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.South
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.zIndex
import android.widget.Toast
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Delete
import com.example.R
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.filled.Lock
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import coil.compose.rememberAsyncImagePainter
import com.example.ui.components.Box3D
import com.example.ui.components.MapCanvas
import com.example.ui.components.QuiltedBackground
import com.example.ui.components.RoundedSheet
import com.example.ui.components.ScreenHeader
import com.example.ui.components.SupportButton
import com.example.ui.components.SupportDialog
import com.example.ui.theme.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import com.example.data.*
import com.example.viewmodel.DeliveryViewModel
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.input.KeyboardType

// ====================================================================================================
// STRICT DESIGN PRESERVATION & BACKEND INTEGRATION CONTRACT (READ CAREFULLY!)
// ====================================================================================================
// This screen has been crafted following the premium Material 3 Dark Luxury theme standards of 
// Engraced Smile. Under NO circumstances should any AI agent or developer change, alter, or remove:
// 1. Color Palette: BackgroundDark, LuxuryBlack, AppSurface, Charcoal, Gold, Obsidian.
// 2. Corner Radii: Card shapes must stay RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp) for sheet depth.
// 3. Contrast Rule: Never put White icons/text on Gold backgrounds; always use Obsidian (black) text/icons.
// 4. Fixed Dock Layout: The Courier/Driver Agent card is pinned fixedly at the absolute bottom. It must NOT 
//    be placed inside scrollable rows or floating elements that can be hidden or moved upward on any screen.
// ====================================================================================================

/**
 * Path Coordinate Interpolator for the Map Route.
 * Computes exact pixel/dp Offset along the multi-segment route polyline (Segment 1 -> Midpoint 1 -> Midpoint 2 -> Segment 3)
 * ensuring the courier avatar tracks exactly along the street highways drawn on the MapCanvas.
 */
private fun getRouteOffset(width: Float, height: Float, progress: Float): Offset {
    val startX = width * 0.3f
    val startY = height * 0.3f
    val mid1Y = height * 0.55f
    val mid2X = width * 0.7f
    val endY = height * 0.8f

    val seg1Len = mid1Y - startY
    val seg2Len = mid2X - startX
    val seg3Len = endY - mid1Y
    val totalLen = seg1Len + seg2Len + seg3Len

    if (totalLen <= 0f) return Offset(startX, startY)

    val p1 = seg1Len / totalLen
    val p2 = (seg1Len + seg2Len) / totalLen

    return when {
        progress <= p1 -> {
            val ratio = progress / p1
            Offset(startX, startY + (mid1Y - startY) * ratio)
        }
        progress <= p2 -> {
            val ratio = (progress - p1) / (p2 - p1)
            Offset(startX + (mid2X - startX) * ratio, mid1Y)
        }
        else -> {
            val ratio = (progress - p2) / (1f - p2)
            Offset(mid2X, mid1Y + (endY - mid1Y) * ratio)
        }
    }
}

fun generateQRCodeBitmap(text: String, size: Int): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE)
        }
    }
    return bitmap
}

@Composable
fun QRCodeImage(text: String, sizeDp: Dp) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val sizePx = with(density) { sizeDp.roundToPx() }
    
    val imageBitmap = remember(text, sizePx) {
        try {
            val bitmap = generateQRCodeBitmap(text, sizePx)
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
    
    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = "QR Code for $text",
            modifier = Modifier.size(sizeDp),
            contentScale = ContentScale.Fit
        )
    } else {
        Box(
            modifier = Modifier
                .size(sizeDp)
                .background(TextGray, RoundedCornerShape(12.dp))
        )
    }
}

enum class DrawerState {
    CLOSED,
    COLLAPSED,
    EXPANDED
}

@Composable
fun ActiveTrackingScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val selectedParcel by viewModel.selectedParcel.collectAsState()
    val riders by viewModel.aiRiders.collectAsState()
    val isDark by viewModel.darkModeEnabled.collectAsState()
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val context = LocalContext.current
    val recentSearches by viewModel.recentSearches.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var searchQueryError by remember { mutableStateOf<String?>(null) }
    var showGeminiSummary by remember { mutableStateOf(false) }
    var isHistoryUnlocked by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }

    val userAvatar by viewModel.photoUrl.collectAsState()
    var userCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    val scope = rememberCoroutineScope()

    val locationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions: Map<String, Boolean> ->
        scope.launch {
            val detected = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                detectUserLocationCoords(context)
            }
            userCoords = detected
        }
    }

    LaunchedEffect(Unit) {
        val fineGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val coarseGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            val detected = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                detectUserLocationCoords(context)
            }
            userCoords = detected
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(selectedParcel?.id) {
        selectedParcel?.id?.let { id ->
            viewModel.startRealTimeTrackingListener(id)
        }
    }

    val userParcels by viewModel.parcels.collectAsState()
    val activeParcel = selectedParcel ?: userParcels.firstOrNull { 
        it.status == ParcelStatus.TRANSIT || 
        it.status == ParcelStatus.PENDING || 
        it.status == ParcelStatus.ASSIGNED || 
        it.status == ParcelStatus.OUT_FOR_DELIVERY 
    }

    val dummyParcel = remember {
        Parcel(
            id = "DEMO-777",
            itemName = "Premium Express Document",
            imageUrl = "",
            status = ParcelStatus.TRANSIT,
            pickupAddress = "Murtala Muhammed Rd, Ikeja, Lagos",
            deliveryAddress = "Herbert Macaulay Way, Yaba, Lagos",
            senderName = "Elite Member",
            senderPhone = "+234 803 111 2222",
            receiverName = "Olusola Coker",
            receiverPhone = "+234 812 345 6789",
            price = 3500.0,
            courierName = "Richard Dheo",
            courierPhone = "+234 803 111 2222",
            progress = 0.45f,
            courierLatitude = 6.5244,
            courierLongitude = 3.3792
        )
    }

    val hasNoBooking = activeParcel == null
    val parcel = activeParcel ?: dummyParcel

    LaunchedEffect(hasNoBooking, parcel.id, parcel.status, parcel.progress) {
        val statusText = when (parcel.status) {
            ParcelStatus.PENDING -> "Pending Dispatch"
            ParcelStatus.ASSIGNED -> "Courier Assigned"
            ParcelStatus.TRANSIT -> "In Transit to destination"
            ParcelStatus.OUT_FOR_DELIVERY -> "Out for delivery now!"
            ParcelStatus.DELIVERED -> "Delivered safely!"
            ParcelStatus.CANCELLED -> "Cancelled"
        }
        com.example.data.TrackingAppWidget.updateWidgetData(
            context = context,
            parcelId = if (hasNoBooking) null else parcel.id,
            statusText = statusText,
            progressPercent = (parcel.progress * 100).toInt()
        )
    }

    var isLocalLoading by remember(parcel.id) { mutableStateOf(false) }
    var showChatSheet by remember { mutableStateOf(false) }

    if (showFeedbackDialog) {
        DeliveryFeedbackDialog(
            parcel = parcel,
            isDark = isDark,
            onDismiss = { showFeedbackDialog = false },
            onSubmit = { rating, tip ->
                viewModel.rateAndTipRider(
                    parcelId = parcel.id,
                    riderId = parcel.riderId,
                    rating = rating,
                    tipAmount = tip,
                    onComplete = { success, error ->
                        if (success) {
                            Toast.makeText(context, "Feedback and Tip submitted successfully!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Error: ${error ?: "Submission failed"}", Toast.LENGTH_LONG).show()
                        }
                        showFeedbackDialog = false
                    }
                )
            }
        )
    }

    if (showChatSheet) {
        ParcelChatDialog(
            parcelId = parcel.id,
            senderRole = "customer",
            viewModel = viewModel,
            onDismiss = { showChatSheet = false }
        )
    }
    LaunchedEffect(parcel.id) {
        val validationResult = com.example.util.Zod.string(parcel.id)
            .min(7, "Tracking ID must be at least 7 characters.")
            .max(12, "Tracking ID must not exceed 12 characters.")
            .regex("^[a-zA-Z0-9\\s-]+$", "Only letters, numbers, and hyphens allowed.")
            .safeParse()

        if (validationResult is com.example.util.ZodResult.Success) {
            viewModel.checkRouteTrafficViaMapbox(parcel.pickupAddress, parcel.deliveryAddress)
        } else {
            android.util.Log.e("TrackingScreen", "Mapbox request aborted: Invalid parcel ID format: ${parcel.id}")
        }
        isLocalLoading = true
        kotlinx.coroutines.delay(1200)
        isLocalLoading = false
    }

    var drawerState by remember(hasNoBooking) {
        mutableStateOf(DrawerState.COLLAPSED)
    }
    var isGoingUp by remember { mutableStateOf(true) }

    val bottomCardHeight by animateDpAsState(
        targetValue = when (drawerState) {
            DrawerState.CLOSED -> if (hasNoBooking) 20.dp else 120.dp
            DrawerState.COLLAPSED -> if (hasNoBooking) 132.dp else 340.dp
            DrawerState.EXPANDED -> if (hasNoBooking) 132.dp else 520.dp
        },
        label = "bottomCardHeight"
    )

    // Dynamic Weather state and AI Mode (Points 11 & 12)
    var currentWeather by remember { mutableStateOf("Rainy 🌧️") }
    var isAiEtaActive by remember { mutableStateOf(true) }

    fun calculateEta(prog: Float, weather: String, aiActive: Boolean): Int {
        val baseSeconds = ((1f - prog) * 1200).toInt().coerceAtLeast(10)
        val weatherMultiplier = when {
            weather.contains("Rainy") -> 1.35
            weather.contains("Stormy") -> 1.75
            weather.contains("Foggy") -> 1.25
            else -> 1.0
        }
        val aiOffset = if (aiActive) -45 else 0 // AI model optimization offset
        return ((baseSeconds * weatherMultiplier) + aiOffset).toInt().coerceAtLeast(5)
    }

    // Dynamic 'Estimated Time of Arrival' countdown ticking in real-time
    var tickingSeconds by remember(parcel.progress, currentWeather, isAiEtaActive) {
        mutableStateOf(calculateEta(parcel.progress, currentWeather, isAiEtaActive))
    }

    LaunchedEffect(parcel.progress, currentWeather, isAiEtaActive) {
        while (tickingSeconds > 0) {
            delay(1000L)
            tickingSeconds--
        }
    }

    // 1-Mile Proximity Notification / Alert Simulation
    var hasNotifiedWithinOneMile by remember { mutableStateOf(false) }
    var showInAppNotificationBanner by remember { mutableStateOf(false) }

    LaunchedEffect(parcel.progress) {
        if (parcel.progress >= 0.85f && parcel.progress < 0.98f && !hasNotifiedWithinOneMile) {
            hasNotifiedWithinOneMile = true
            showInAppNotificationBanner = true
            Toast.makeText(context, "🔔 Delivery Notice: Courier is within 1 mile of your location!", Toast.LENGTH_LONG).show()
        }
        if (parcel.progress < 0.85f) {
            hasNotifiedWithinOneMile = false
            showInAppNotificationBanner = false
        }
    }

    // Auto-dismiss the in-app notification banner after 6 seconds
    LaunchedEffect(showInAppNotificationBanner) {
        if (showInAppNotificationBanner) {
            delay(6000L)
            showInAppNotificationBanner = false
        }
    }

    // ------------------------------------------------------------------------------------------------
    // Mapbox Interactive Configurations (Simulated GL Engine Controls)
    // ------------------------------------------------------------------------------------------------
    var isSatelliteMode by remember { mutableStateOf(false) }
    var showTraffic by remember { mutableStateOf(true) }
    var mapZoom by remember { mutableFloatStateOf(14.5f) }
    var dismissedTrafficAlert by remember { mutableStateOf(false) }

    // Dynamic infinite animation for real-time courier path gliding
    val infiniteTransition = rememberInfiniteTransition(label = "tracking")
    val progressOffset by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserPos"
    )

    val accentIconColor = if (isLight) Obsidian else Gold
    val accentTextColor = if (isLight) Obsidian else Gold

    val aiTrafficCongested by viewModel.aiTrafficCongested.collectAsState()

    val headerBgColor = if (isDark) Gold else Obsidian
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(headerBgColor)
    ) {
        ScreenHeader(
            title = "Track Shipment",
            onBack = { onNavigate("Dashboard") },
            rightContent = {
                SupportButton(onClick = { showSupportDialog = true })
            }
        )

        if (showSupportDialog) {
            SupportDialog(onDismiss = { showSupportDialog = false })
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(if (isDark) BackgroundDark else BackgroundLight)
        ) {
            val routeColor = if (aiTrafficCongested) "#FF3B30" else if (showTraffic) "#FF9500" else "#D4AF37"

            // 1. FULL SCREEN MAP BACKGROUND (Uber-like experience)
            if (isLocalLoading) {
                SkeletonBox(
                    modifier = Modifier.fillMaxSize(),
                    isLight = isLight,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
            } else {
                LiveMapView(
                    modifier = Modifier.fillMaxSize(),
                    pickupAddress = parcel.pickupAddress,
                    deliveryAddress = parcel.deliveryAddress,
                    progress = parcel.progress,
                    isSatellite = isSatelliteMode,
                    showTraffic = showTraffic,
                    zoom = mapZoom,
                    courierAvatar = parcel.courierAvatar,
                    routeColor = routeColor,
                    onMapTypeToggled = { isSat ->
                        isSatelliteMode = isSat
                    },
                    courierLatitude = parcel.courierLatitude,
                    courierLongitude = parcel.courierLongitude,
                    userAvatar = userAvatar,
                    hasNoBooking = hasNoBooking,
                    userCoords = userCoords
                )
            }

            // 2. FLOATING TOP NOTIFICATIONS (Stacked neatly inside the map area)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .zIndex(10f)
            ) {

            // FLOATING AI TRAFFIC BANNER
            androidx.compose.animation.AnimatedVisibility(
                visible = aiTrafficCongested && !dismissedTrafficAlert,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Surface(
                    color = if (isLight) Obsidian else Gold,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (isLight) Obsidian else BorderDark),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dismissedTrafficAlert = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isLight) Color.White.copy(alpha = 0.15f) else Obsidian.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "AI Alert",
                                tint = if (isLight) Color.White else Obsidian,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI TRAFFIC REROUTING ACTIVE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isLight) Color.White else Obsidian
                            )
                            Text(
                                text = "Gridlock detected on Express Route. Richard Dheo was automatically rerouted to bypass congestion.",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLight) Color.White.copy(alpha = 0.8f) else Obsidian.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(onClick = { dismissedTrafficAlert = true }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss Traffic Banner",
                                tint = if (isLight) Color.White else Obsidian,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // FLOATING BANNER (1-Mile Proximity Simulation Overlay)
            androidx.compose.animation.AnimatedVisibility(
                visible = showInAppNotificationBanner,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Gold),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Notification",
                            tint = Obsidian,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Courier Is Near! 🚴",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Obsidian
                            )
                            Text(
                                text = "${parcel.courierName} is within 1 mile of your location. Preparing to receive.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Obsidian.copy(alpha = 0.85f)
                            )
                        }
                        IconButton(onClick = { showInAppNotificationBanner = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Banner",
                                tint = Obsidian,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // FLOATING RIDER APPROACHING BANNER (< 500m)
            if (!hasNoBooking && parcel.progress >= 0.95f && parcel.status != ParcelStatus.DELIVERED) {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Gold),
                    border = BorderStroke(1.5.dp, Obsidian),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DirectionsBike,
                            contentDescription = "Approaching",
                            tint = Obsidian,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Rider Approaching! (< 500m) 🚴💨",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Obsidian
                            )
                            val mins = tickingSeconds / 60
                            val secs = tickingSeconds % 60
                            Text(
                                text = "Arriving in ${mins}m ${secs}s • Watch map for live approach",
                                fontSize = 11.sp,
                                color = Obsidian.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // 3. WEATHER & AI ETA CONTROLS (Floating Center-Left for thumb comfort and zero overlays)
        androidx.compose.animation.AnimatedVisibility(
            visible = drawerState != DrawerState.EXPANDED,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .zIndex(5f)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val weatherEmoji = when (currentWeather) {
                    "Sunny ☀️" -> "☀️"
                    "Rainy 🌧️" -> "🌧️"
                    "Stormy ⛈️" -> "⛈️"
                    else -> "☀️"
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Obsidian.copy(alpha = 0.85f))
                        .border(1.dp, BorderDark, CircleShape)
                        .clickable {
                            currentWeather = when (currentWeather) {
                                "Sunny ☀️" -> "Rainy 🌧️"
                                "Rainy 🌧️" -> "Stormy ⛈️"
                                else -> "Sunny ☀️"
                            }
                            tickingSeconds = calculateEta(parcel.progress, currentWeather, isAiEtaActive)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = weatherEmoji,
                        fontSize = 18.sp
                    )
                }

                MapControlButton(
                    icon = Icons.Filled.Schedule,
                    description = "Toggle AI ETA Estimator",
                    isActive = isAiEtaActive
                ) {
                    isAiEtaActive = !isAiEtaActive
                    tickingSeconds = calculateEta(parcel.progress, currentWeather, isAiEtaActive)
                }
            }
        }

        // 4. MAPBOX CONTROLS (Floating Center-Right for thumb comfort and zero overlays)
        androidx.compose.animation.AnimatedVisibility(
            visible = drawerState != DrawerState.EXPANDED,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .zIndex(5f)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MapControlButton(icon = Icons.Default.Add, description = "Zoom In") {
                    if (mapZoom < 20f) mapZoom += 0.5f
                }
                MapControlButton(icon = Icons.Default.Remove, description = "Zoom Out") {
                    if (mapZoom > 2f) mapZoom -= 0.5f
                }
                MapControlButton(
                    icon = Icons.Default.Traffic,
                    description = "Traffic Overlay",
                    isActive = showTraffic
                ) {
                    showTraffic = !showTraffic
                }
                MapControlButton(
                    icon = Icons.Default.Layers,
                    description = "Satellite View",
                    isActive = isSatelliteMode
                ) {
                    isSatelliteMode = !isSatelliteMode
                }
            }
        }

                    // --------------------------------------------------------------------------------------------
                    // LOWER PORTION: COLLAPSIBLE DRWAVER (overlapping map, extremely pretty!)
                    // --------------------------------------------------------------------------------------------
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(bottomCardHeight)
                            .zIndex(20f)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isDark) Obsidian else Color.White),
                            border = BorderStroke(1.5.dp, if (isDark) Gold else BorderLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                val overrideStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = Poppins,
                                    color = if (isDark) GoldLight else Obsidian
                                )
                                androidx.compose.runtime.CompositionLocalProvider(
                                    androidx.compose.material3.LocalTextStyle provides overrideStyle,
                                    androidx.compose.material3.LocalContentColor provides (if (isDark) GoldLight else Obsidian)
                                ) {
                                // Upper Scrollable Content (only shown when not closed)
                                if (drawerState != DrawerState.CLOSED) {
                                    if (hasNoBooking) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Button(
                                                onClick = { onNavigate("SendParcel") },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .fillMaxHeight()
                                                    .testTag("book_new_dispatch_button"),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Gold,
                                                    contentColor = Obsidian
                                                ),
                                                shape = RoundedCornerShape(16.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.LocalShipping,
                                                        contentDescription = null,
                                                        tint = Obsidian,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Text(
                                                        text = "BOOK A NEW DISPATCH",
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 16.sp,
                                                        letterSpacing = 1.sp
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .align(Alignment.TopCenter)
                                                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 100.dp)
                                                .verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                        // AI Route Optimization Indicator
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isLight) GoldenWhiteLight else Charcoal.copy(alpha = 0.4f),
                                            border = BorderStroke(1.dp, if (isLight) Slate else Gold.copy(alpha = 0.15f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.NotificationsActive,
                                                    contentDescription = "AI Match",
                                                    tint = if (isLight) Obsidian else Gold,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "AI Optimized Delivery Match (98.6% precision)",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isLight) Obsidian.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.7f)
                                                )
                                            }
                                        }

                                        // 1. Shipment Meta Info (ID, Item Name, and Status Badge)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (isLocalLoading) {
                                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    SkeletonBox(
                                                        modifier = Modifier.width(100.dp).height(20.dp),
                                                        isLight = isLight
                                                    )
                                                    SkeletonBox(
                                                        modifier = Modifier.width(160.dp).height(14.dp),
                                                        isLight = isLight
                                                    )
                                                }
                                            } else {
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = "#${parcel.id}",
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 17.sp,
                                                            color = AppOnSurface
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        AnimatedStatusBadge(
                                                            status = parcel.status,
                                                            isDark = isDark,
                                                            fontSize = 10.sp,
                                                            paddingHorizontal = 8.dp,
                                                            paddingVertical = 3.dp
                                                        )
                                                    }
                                                    Text(
                                                        text = parcel.itemName,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TextGray
                                                    )
                                                }
                                            }

                                            // ETA Indicator (Luxury theme accent) - UPDATED IN REAL-TIME
                                            val etaText = when (parcel.status) {
                                                ParcelStatus.DELIVERED -> "ARRIVED"
                                                ParcelStatus.CANCELLED -> "CANCELLED"
                                                else -> {
                                                    val mins = tickingSeconds / 60
                                                    val secs = tickingSeconds % 60
                                                    "${mins}m ${secs}s"
                                                }
                                            }
                                            val etaSubText = when (parcel.status) {
                                                ParcelStatus.DELIVERED -> "Completed"
                                                ParcelStatus.CANCELLED -> "No status"
                                                else -> "Est. Arrival"
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = etaText,
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = accentTextColor
                                                )
                                                Text(
                                                    text = etaSubText,
                                                    fontSize = 10.sp,
                                                    color = TextGray
                                                )
                                            }
                                        }

                                        // 2. Beautiful Horizontal Stepper Progress Bar
                                        ShippingJourneyProgressBar(
                                            status = parcel.status,
                                            progress = parcel.progress,
                                            isDark = isDark,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                        if (false) Column(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(text = "Departed Hub", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Bold)
                                                Text(text = "En Route", fontSize = 11.sp, color = accentTextColor, fontWeight = FontWeight.Bold)
                                                Text(text = "Delivered", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(CircleShape)
                                                    .background(TextGray.copy(alpha = 0.2f))
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .fillMaxWidth(parcel.progress)
                                                        .background(Gold)
                                                )
                                            }
                                        }

                                        // Dynamic Delivery Estimation Component
                                        DeliveryEstimationCard(
                                            status = parcel.status,
                                            progress = parcel.progress,
                                            isDark = isDark
                                        )

                                        // 3. Sender to Receiver addresses overview panel
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (isLocalLoading) {
                                                Column(modifier = Modifier.weight(1.0f)) {
                                                    Text("PICKUP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                                    SkeletonBox(
                                                        modifier = Modifier.width(120.dp).height(16.dp),
                                                        isLight = isLight
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.Navigation,
                                                    contentDescription = null,
                                                    tint = accentIconColor,
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .padding(horizontal = 4.dp)
                                                )
                                                Column(
                                                    modifier = Modifier.weight(1.0f),
                                                    horizontalAlignment = Alignment.End
                                                ) {
                                                    Text("DESTINATION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                                    SkeletonBox(
                                                        modifier = Modifier.width(120.dp).height(16.dp),
                                                        isLight = isLight
                                                    )
                                                }
                                            } else {
                                                Column(modifier = Modifier.weight(1.0f)) {
                                                    Text("PICKUP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                                    Text(
                                                        text = parcel.pickupAddress.substringBefore(","),
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = AppOnSurface,
                                                        maxLines = 1
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.Navigation,
                                                    contentDescription = null,
                                                    tint = accentIconColor,
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .padding(horizontal = 4.dp)
                                                )
                                                Column(
                                                    modifier = Modifier.weight(1.0f),
                                                    horizontalAlignment = Alignment.End
                                                ) {
                                                    Text("DESTINATION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                                    Text(
                                                        text = parcel.deliveryAddress.substringBefore(","),
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = AppOnSurface,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }

                                        // Handover, Tipping & Sharing Interface Panel
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = if (isDark) Charcoal.copy(alpha = 0.5f) else Color(0xFFF9FAFB)),
                                            shape = RoundedCornerShape(16.dp),
                                            border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.15f) else Color.Transparent),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                // Battery optimization & tracking status row
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.CheckCircle,
                                                        contentDescription = "Battery Optimized",
                                                        tint = Color(0xFF4CAF50),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Location tracking optimized (2.1% / hr battery impact)",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF4CAF50)
                                                    )
                                                }

                                                HorizontalDivider(color = if (isDark) BorderDark else BorderLight)

                                                // QR Code Parcel Handover Verification
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "QR HANDOVER VERIFICATION",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Black,
                                                        letterSpacing = 1.sp,
                                                        color = if (isDark) GoldLight else Obsidian
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    
                                                    // Beautiful Dynamic QR Code Drawing
                                                    Box(
                                                        modifier = Modifier
                                                            .size(120.dp)
                                                            .background(Color.White, RoundedCornerShape(12.dp))
                                                            .padding(10.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        QRCodeImage(
                                                            text = selectedParcel?.id ?: "ENGRACED_DISPATCH_MOCK_ID",
                                                            sizeDp = 100.dp
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = "Show this QR to courier to verify parcel handover.",
                                                        fontSize = 10.sp,
                                                        color = TextGray,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }

                                                HorizontalDivider(color = if (isDark) BorderDark else BorderLight)

                                                // Share Live Tracking & Tip Rider Row
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    // Share Live Tracking Link
                                                    Button(
                                                        onClick = {
                                                            val sendIntent: Intent = Intent().apply {
                                                                action = Intent.ACTION_SEND
                                                                putExtra(Intent.EXTRA_TEXT, "Track my Engraced Dispatch package live: https://engraced.dispatch.com/track/${parcel.id}")
                                                                type = "text/plain"
                                                            }
                                                            val shareIntent = Intent.createChooser(sendIntent, "Share Tracking Link")
                                                            context.startActivity(shareIntent)
                                                        },
                                                        modifier = Modifier.weight(1f).height(40.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Obsidian else Color(0xFFEEEEEE)),
                                                        shape = RoundedCornerShape(12.dp),
                                                        border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.3f) else Color.Transparent)
                                                    ) {
                                                        Text("Share Link", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isDark) Gold else Obsidian)
                                                    }

                                                    // Feedback & Tip Rider After Delivery
                                                    if (parcel.isRated) {
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .height(40.dp)
                                                                .background(
                                                                    if (isDark) Charcoal else Color(0xFFEEEEEE),
                                                                    RoundedCornerShape(12.dp)
                                                                )
                                                                .border(BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.2f) else Color.Transparent), RoundedCornerShape(12.dp)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Filled.Star,
                                                                    contentDescription = null,
                                                                    tint = Gold,
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                val tipFormatted = if (parcel.tipAmount > 0.0) " • ₦${String.format("%,.0f", parcel.tipAmount)}" else ""
                                                                Text(
                                                                    text = "${parcel.customerRating.toInt()} ★$tipFormatted",
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (isDark) Gold else Obsidian
                                                                )
                                                            }
                                                        }
                                                    } else {
                                                        Button(
                                                            onClick = { showFeedbackDialog = true },
                                                            modifier = Modifier.weight(1f).height(40.dp),
                                                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                                                            shape = RoundedCornerShape(12.dp)
                                                        ) {
                                                            Text("Feedback & Tip", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Obsidian)
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // NEW DETAILED EXPANDABLE TIMELINE
                                        val isTimelineExpanded = drawerState == DrawerState.EXPANDED
                                        AnimatedVisibility(
                                            visible = isTimelineExpanded,
                                            enter = expandVertically() + fadeIn(),
                                            exit = shrinkVertically() + fadeOut()
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(if (isDark) Charcoal.copy(alpha = 0.3f) else Obsidian.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                                                    .padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(14.dp)
                                            ) {
                                                val timelineSteps = listOf(
                                                    Triple("Picked up", "Courier has collected your parcel.", parcel.progress >= 0.15f),
                                                    Triple("In Transit", "Your package is on its way.", parcel.progress >= 0.45f),
                                                    Triple("Out for Delivery", "Courier is arriving shortly.", parcel.progress >= 0.8f),
                                                    Triple("Delivered", "Package safely received.", parcel.progress >= 1.0f)
                                                )
                                                
                                                timelineSteps.forEachIndexed { index, (title, desc, isCompleted) ->
                                                    val itemAlpha by animateFloatAsState(
                                                        targetValue = if (isTimelineExpanded) 1f else 0f,
                                                        animationSpec = tween(durationMillis = 400, delayMillis = index * 100, easing = EaseInOutQuart),
                                                        label = "itemAlpha_$index"
                                                    )
                                                    val itemTranslationX by animateFloatAsState(
                                                        targetValue = if (isTimelineExpanded) 0f else -25f,
                                                        animationSpec = tween(durationMillis = 400, delayMillis = index * 100, easing = EaseInOutQuart),
                                                        label = "itemTranslationX_$index"
                                                    )
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .graphicsLayer {
                                                                alpha = itemAlpha
                                                                translationX = itemTranslationX
                                                            },
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        // Circle dot with connecting line
                                                        Column(
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            modifier = Modifier.width(24.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(12.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isCompleted) Gold else TextGray.copy(alpha = 0.4f))
                                                                    .border(
                                                                        width = 2.dp,
                                                                        color = if (isCompleted) Gold else TextGray,
                                                                        shape = CircleShape
                                                                    )
                                                            )
                                                            if (index < timelineSteps.size - 1) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .width(2.dp)
                                                                        .height(24.dp)
                                                                        .background(if (timelineSteps[index + 1].third) Gold else TextGray.copy(alpha = 0.4f))
                                                                )
                                                            }
                                                        }
                                                        
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = title,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isCompleted) AppOnSurface else TextGray
                                                            )
                                                            Text(
                                                                text = desc,
                                                                fontSize = 10.sp,
                                                                color = TextGray
                                                            )
                                                        }
                                                        
                                                        // Realistic status timestamp
                                                        val timestampText = when (index) {
                                                            0 -> "12:30 PM"
                                                            1 -> "12:45 PM"
                                                            2 -> if (parcel.progress >= 0.8f) "1:05 PM" else "--:--"
                                                            3 -> if (parcel.progress >= 1.0f) "1:15 PM" else "--:--"
                                                            else -> ""
                                                        }
                                                        Text(
                                                            text = timestampText,
                                                            fontSize = 10.sp,
                                                            color = TextGray,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // 3.5. Search / Track Another Shipment Section
                                        Card(
                                             colors = CardDefaults.cardColors(containerColor = if (isDark) Charcoal.copy(alpha = 0.5f) else Color(0xFFF9FAFB)),
                                             shape = RoundedCornerShape(16.dp),
                                             border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.15f) else Color.Transparent),
                                             modifier = Modifier.fillMaxWidth()
                                        ) {
                                             Column(
                                                 modifier = Modifier.padding(16.dp),
                                                 verticalArrangement = Arrangement.spacedBy(12.dp)
                                             ) {
                                                 Text(
                                                     text = "Track Another Shipment",
                                                     fontWeight = FontWeight.Bold,
                                                     fontSize = 14.sp,
                                                     color = AppOnSurface
                                                 )

                                                 var inlineSearchQuery by remember { mutableStateOf("") }
                                                 var inlineSearchQueryError by remember { mutableStateOf<String?>(null) }
                                                 OutlinedTextField(
                                                     value = inlineSearchQuery,
                                                     onValueChange = {
                                                         inlineSearchQuery = com.example.util.FormatUtils.formatTrackingId(it)
                                                         inlineSearchQueryError = null
                                                     },
                                                     placeholder = { Text("Enter Tracking Number", fontSize = 12.sp) },
                                                      shape = RoundedCornerShape(16.dp),
                                                     isError = inlineSearchQueryError != null,
                                                     supportingText = inlineSearchQueryError?.let { { Text(it, color = androidx.compose.ui.graphics.Color.Red, fontSize = 10.sp) } },
                                                     modifier = Modifier
                                                         .fillMaxWidth()
                                                         .testTag("track_parcel_inline_input"),
                                                     singleLine = true,
                                                     colors = OutlinedTextFieldDefaults.colors(
                                                         focusedBorderColor = if (isDark) Gold else Obsidian,
                                                         focusedLabelColor = if (isDark) Gold else Obsidian
                                                     )
                                                 )

                                                 Button(
                                                     onClick = {
                                                         val validationResult = com.example.util.Zod.string(inlineSearchQuery)
                                                             .min(7, "Tracking ID must be at least 7 characters.")
                                                             .max(12, "Tracking ID must not exceed 12 characters.")
                                                             .regex("^[a-zA-Z0-9\\s-]+$", "Only letters, numbers, and hyphens allowed.")
                                                             .safeParse()

                                                         when (validationResult) {
                                                             is com.example.util.ZodResult.Error -> {
                                                                 inlineSearchQueryError = validationResult.message
                                                             }
                                                             is com.example.util.ZodResult.Success -> {
                                                                 inlineSearchQueryError = null
                                                                 viewModel.searchAndTrackParcel(
                                                                     context = context,
                                                                     trackingNumber = inlineSearchQuery,
                                                                     onSuccess = {
                                                                         inlineSearchQuery = ""
                                                                     },
                                                                     onError = { msg ->
                                                                         Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                                                     }
                                                                 )
                                                             }
                                                         }
                                                     },
                                                     modifier = Modifier
                                                         .fillMaxWidth()
                                                         .height(44.dp)
                                                         .testTag("track_parcel_inline_button"),
                                                     colors = ButtonDefaults.buttonColors(
                                                         containerColor = if (isDark) Gold else Obsidian,
                                                         contentColor = if (isDark) Obsidian else Color.White
                                                     ),
                                                     shape = RoundedCornerShape(8.dp)
                                                 ) {
                                                     Text("Search ID", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                 }

                                                 // Recent Searches
                                                 if (recentSearches.isNotEmpty()) {
                                                     Row(
                                                         modifier = Modifier.fillMaxWidth(),
                                                         horizontalArrangement = Arrangement.SpaceBetween,
                                                         verticalAlignment = Alignment.CenterVertically
                                                     ) {
                                                         Text(
                                                             text = "Recent Searches",
                                                             fontWeight = FontWeight.Bold,
                                                             fontSize = 11.sp,
                                                             color = TextGray
                                                         )
                                                         androidx.compose.material3.TextButton(
                                                             onClick = { viewModel.clearSearchHistory(context) },
                                                             modifier = Modifier.height(24.dp).testTag("clear_history_inline_button"),
                                                             contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                                                             colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                                                 contentColor = if (isDark) Gold else Obsidian
                                                             )
                                                         ) {
                                                             Text("Clear", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                         }
                                                     }
                                                     recentSearches.take(3).forEach { searchId ->
                                                         Row(
                                                             modifier = Modifier
                                                                 .fillMaxWidth()
                                                                 .clickable {
                                                                     viewModel.searchAndTrackParcel(
                                                                         context = context,
                                                                         trackingNumber = searchId,
                                                                         onSuccess = {},
                                                                         onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                                                                     )
                                                                 }
                                                                 .padding(vertical = 4.dp),
                                                             verticalAlignment = Alignment.CenterVertically,
                                                             horizontalArrangement = Arrangement.SpaceBetween
                                                         ) {
                                                             Row(
                                                                 verticalAlignment = Alignment.CenterVertically,
                                                                 modifier = Modifier.weight(1f)
                                                             ) {
                                                                 Icon(
                                                                     imageVector = Icons.Default.History,
                                                                     contentDescription = null,
                                                                     tint = if (isDark) Gold else Obsidian,
                                                                     modifier = Modifier.size(14.dp)
                                                                 )
                                                                 Spacer(modifier = Modifier.width(8.dp))
                                                                 Text(
                                                                     text = searchId,
                                                                     fontSize = 12.sp,
                                                                     color = AppOnSurface,
                                                                     fontWeight = FontWeight.Medium
                                                                 )
                                                                 
                                                                 val parcelForId = viewModel.parcels.collectAsState().value.find { it.id.equals(searchId, ignoreCase = true) }
                                                                 if (parcelForId != null) {
                                                                     val badgeText = when (parcelForId.status) {
                                                                         ParcelStatus.PENDING -> "Pending"
                                                                         ParcelStatus.ASSIGNED -> "Assigned"
                                                                         ParcelStatus.TRANSIT -> "In Transit"
                                                                         ParcelStatus.OUT_FOR_DELIVERY -> "Out for Delivery"
                                                                         ParcelStatus.DELIVERED -> "Delivered"
                                                                         ParcelStatus.CANCELLED -> "Cancelled"
                                                                     }
                                                                     val badgeBgColor = when (parcelForId.status) {
                                                                         ParcelStatus.PENDING -> Color(0x202196F3)
                                                                         ParcelStatus.ASSIGNED -> Color(0x209C27B0)
                                                                         ParcelStatus.DELIVERED -> Color(0x204CAF50)
                                                                         ParcelStatus.OUT_FOR_DELIVERY -> Color(0x20FF9800)
                                                                         ParcelStatus.CANCELLED -> Color(0x20F44336)
                                                                         ParcelStatus.TRANSIT -> if (isDark) Gold.copy(alpha = 0.15f) else Color(0x100E0E10)
                                                                     }
                                                                     val badgeTextColor = when (parcelForId.status) {
                                                                         ParcelStatus.PENDING -> Color(0xFF2196F3)
                                                                         ParcelStatus.ASSIGNED -> Color(0xFF9C27B0)
                                                                         ParcelStatus.DELIVERED -> Color(0xFF4CAF50)
                                                                         ParcelStatus.OUT_FOR_DELIVERY -> Color(0xFFFF9800)
                                                                         ParcelStatus.CANCELLED -> Color(0xFFF44336)
                                                                         ParcelStatus.TRANSIT -> if (isDark) Gold else Obsidian
                                                                     }
                                                                     Surface(
                                                                         color = badgeBgColor,
                                                                         shape = RoundedCornerShape(6.dp),
                                                                         modifier = Modifier.padding(start = 6.dp)
                                                                     ) {
                                                                         Text(
                                                                             text = badgeText,
                                                                             fontSize = 9.sp,
                                                                             fontWeight = FontWeight.Bold,
                                                                             color = badgeTextColor,
                                                                             modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                                         )
                                                                     }
                                                                 }
                                                             }
                                                             Icon(
                                                                 imageVector = Icons.Default.ArrowForward,
                                                                 contentDescription = null,
                                                                 tint = TextGray,
                                                                 modifier = Modifier.size(12.dp)
                                                             )
                                                         }
                                                     }
                                                 } else {
                                                     Column(
                                                         modifier = Modifier
                                                             .fillMaxWidth()
                                                             .padding(vertical = 12.dp),
                                                         horizontalAlignment = Alignment.CenterHorizontally
                                                     ) {
                                                         AnimatedSearchIllustration()
                                                         Spacer(modifier = Modifier.height(8.dp))
                                                         Text(
                                                             text = "No recent searches yet",
                                                             fontSize = 11.sp,
                                                             color = TextGray,
                                                             fontWeight = FontWeight.Bold
                                                         )
                                                     }
                                                 }
                                             }
                                        }
                                    }
                                }

                                if (drawerState == DrawerState.CLOSED) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp, vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Button(
                                            onClick = { onNavigate("SendParcel") },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .fillMaxHeight()
                                                .testTag("book_new_dispatch_button"),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Gold,
                                                contentColor = Obsidian
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.LocalShipping,
                                                    contentDescription = null,
                                                    tint = Obsidian,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Book a New Dispatch",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                }

                                // 4. THE COURIER / DRIVER AGENT CARD (FIXED AT THE ABSOLUTE BOTTOM)
                                if (!hasNoBooking && drawerState != DrawerState.CLOSED) {
                                    Surface(
                                        shape = RoundedCornerShape(24.dp),
                                        color = if (isDark) Charcoal else Obsidian,
                                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.2f)),
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        QuiltedBackground(modifier = Modifier.matchParentSize()) {}

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (isLocalLoading) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(50.dp)
                                                            .border(2.dp, Gold, CircleShape)
                                                            .clip(CircleShape)
                                                    ) {
                                                        SkeletonBox(
                                                            modifier = Modifier.fillMaxSize(),
                                                            isLight = isLight,
                                                            shape = CircleShape
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        SkeletonBox(
                                                            modifier = Modifier.width(140.dp).height(16.dp),
                                                            isLight = isLight
                                                        )
                                                        SkeletonBox(
                                                            modifier = Modifier.width(80.dp).height(12.dp),
                                                            isLight = isLight
                                                        )
                                                    }
                                                }
                                            } else {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(50.dp)
                                                            .border(2.dp, Gold, CircleShape)
                                                            .clip(CircleShape)
                                                    ) {
                                                        Image(
                                                            painter = rememberAsyncImagePainter(parcel.courierAvatar),
                                                            contentDescription = "Courier Profile",
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Text(
                                                            text = parcel.courierName,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            fontSize = 16.sp,
                                                            color = Color.White
                                                        )
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                imageVector = Icons.Default.Star,
                                                                contentDescription = null,
                                                                tint = Gold,
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            val activeRider = riders.find { it.id == parcel.riderId }
                                                            val riderRatingStr = activeRider?.let { String.format("%.2f", it.rating) } ?: "4.95"
                                                            Text(
                                                                text = "$riderRatingStr" + if (parcel.riderBikeNumber.isNotEmpty()) " • Bike: ${parcel.riderBikeNumber}" else " (VIP Rider)",
                                                                fontSize = 11.sp,
                                                                color = TextGray,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // High Contrast Contact Actions (NO White on Gold!)
                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                // Call Trigger (Obsidian icon on White circle)
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White)
                                                        .clickable {
                                                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${parcel.courierPhone}"))
                                                            try {
                                                                context.startActivity(dialIntent)
                                                            } catch (e: Exception) {
                                                                Toast.makeText(context, "Call not supported on this device", Toast.LENGTH_SHORT).show()
                                                            }
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Filled.Call, null, tint = Obsidian, modifier = Modifier.size(18.dp))
                                                }

                                                // Chat Trigger (Obsidian icon on Gold circle)
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(CircleShape)
                                                        .background(Gold)
                                                        .clickable { showChatSheet = true },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Filled.Chat, null, tint = Obsidian, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                                }
                            }
                        }
                    }

                        // Collapsible drawer arrow button overlapping the top center edge
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-18).dp)
                                .border(1.dp, if (isDark) Gold.copy(alpha = 0.3f) else Slate, RoundedCornerShape(12.dp))
                                .clickable {
                                    if (hasNoBooking) {
                                        // Only two states: CLOSED and COLLAPSED
                                        drawerState = if (drawerState == DrawerState.CLOSED) {
                                            DrawerState.COLLAPSED
                                        } else {
                                            DrawerState.CLOSED
                                        }
                                    } else {
                                        when (drawerState) {
                                            DrawerState.CLOSED -> {
                                                drawerState = DrawerState.COLLAPSED
                                                isGoingUp = true
                                            }
                                            DrawerState.COLLAPSED -> {
                                                if (isGoingUp) {
                                                    drawerState = DrawerState.EXPANDED
                                                } else {
                                                    drawerState = DrawerState.CLOSED
                                                }
                                            }
                                            DrawerState.EXPANDED -> {
                                                drawerState = DrawerState.COLLAPSED
                                                isGoingUp = false
                                            }
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = AppSurface,
                            border = BorderStroke(1.dp, if (isLight) Slate else Gold.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (hasNoBooking) {
                                        if (drawerState == DrawerState.CLOSED) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
                                    } else {
                                        when (drawerState) {
                                            DrawerState.CLOSED -> Icons.Default.KeyboardArrowUp
                                            DrawerState.COLLAPSED -> if (isGoingUp) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
                                            DrawerState.EXPANDED -> Icons.Default.KeyboardArrowDown
                                        }
                                    },
                                    contentDescription = "Toggle Drawer",
                                    tint = Gold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

/**
 * Floating Map Control buttons supporting custom zoom, traffic, or map mode triggers.
 */
@Composable
private fun MapControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (isActive) Gold else Obsidian.copy(alpha = 0.85f))
            .border(1.dp, if (isActive) Gold else BorderDark, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = if (isActive) Obsidian else Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

class LeafletJavascriptInterface(
    private val context: android.content.Context,
    private val onMapClickCallback: (Double, Double) -> Unit,
    private val onMarkerPlacedCallback: (String, Double, Double) -> Unit,
    private val onTrackingUpdatedCallback: (Double, Double) -> Unit,
    private val onMapTypeToggledCallback: (Boolean) -> Unit
) {
    @android.webkit.JavascriptInterface
    fun onMapClick(lat: Double, lng: Double) {
        (context as? android.app.Activity)?.runOnUiThread {
            onMapClickCallback(lat, lng)
        }
    }

    @android.webkit.JavascriptInterface
    fun onMarkerPlaced(label: String, lat: Double, lng: Double) {
        (context as? android.app.Activity)?.runOnUiThread {
            onMarkerPlacedCallback(label, lat, lng)
        }
    }

    @android.webkit.JavascriptInterface
    fun onTrackingUpdated(lat: Double, lng: Double) {
        (context as? android.app.Activity)?.runOnUiThread {
            onTrackingUpdatedCallback(lat, lng)
        }
    }

    @android.webkit.JavascriptInterface
    fun onMapTypeToggled(isSatellite: Boolean) {
        (context as? android.app.Activity)?.runOnUiThread {
            onMapTypeToggledCallback(isSatellite)
        }
    }
}

private fun geocodeAddressToLatLng(context: android.content.Context, address: String): Pair<Double, Double> {
    val lower = address.lowercase()
    try {
        if (android.location.Geocoder.isPresent()) {
            val geocoder = android.location.Geocoder(context)
            val addresses = geocoder.getFromLocationName(address, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                return Pair(addr.latitude, addr.longitude)
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("Geocoder", "System Geocoder failed: ${e.message}")
    }
    return when {
        lower.contains("city mall") || lower.contains("ikeja city") -> Pair(6.6018, 3.3515)
        lower.contains("airport") || lower.contains("murtala") -> Pair(6.5244, 3.3792)
        lower.contains("conservation") || lower.contains("lcc") -> Pair(6.4281, 3.4219)
        lower.contains("theatre") || lower.contains("iganmu") -> Pair(6.4633, 3.3672)
        lower.contains("unilag") || lower.contains("university of lagos") || lower.contains("akoka") || lower.contains("yaba") -> Pair(6.5158, 3.3897)
        lower.contains("lekki phase 1") || lower.contains("admiralty") || lower.contains("admirality") -> Pair(6.4265, 3.4300)
        lower.contains("chevron") -> Pair(6.4446, 3.4912)
        lower.contains("ikoyi club") || lower.contains("ikoyi") || lower.contains("kingsway") -> Pair(6.4549, 3.4244)
        lower.contains("nike") || lower.contains("gallery") || lower.contains("elegushi") -> Pair(6.4474, 3.4735)
        lower.contains("island") || lower.contains("marina") -> Pair(6.4501, 3.3958)
        lower.contains("computer") || lower.contains("village") || lower.contains("isaac") -> Pair(6.6250, 3.3421)
        lower.contains("ozumba") || lower.contains("mbadiwe") || lower.contains("victoria") || lower.contains("eko") -> Pair(6.4350, 3.4270)
        lower.contains("surulere") || lower.contains("stadium") -> Pair(6.5000, 3.3500)
        lower.contains("mainland") -> Pair(6.5244, 3.3792)
        else -> {
            val hash = address.hashCode().toLong()
            val latOffset = (Math.abs(hash) % 100) / 1000.0
            val lngOffset = (Math.abs(hash / 100) % 100) / 1000.0
            Pair(6.5244 + latOffset - 0.05, 3.3792 + lngOffset - 0.05)
        }
    }
}

@Composable
fun LiveMapView(
    modifier: Modifier = Modifier,
    pickupAddress: String,
    deliveryAddress: String,
    progress: Float,
    isSatellite: Boolean,
    showTraffic: Boolean,
    zoom: Float,
    courierAvatar: String,
    routeColor: String,
    onMapTypeToggled: (Boolean) -> Unit,
    courierLatitude: Double? = null,
    courierLongitude: Double? = null,
    userAvatar: String = "",
    hasNoBooking: Boolean = false,
    userCoords: Pair<Double, Double>? = null
) {
    val context = LocalContext.current
    val pickupCoords = remember(pickupAddress, hasNoBooking, userCoords) {
        if (hasNoBooking && userCoords != null) {
            userCoords
        } else {
            geocodeAddressToLatLng(context, pickupAddress)
        }
    }
    val deliveryCoords = remember(deliveryAddress, hasNoBooking, userCoords) {
        if (hasNoBooking && userCoords != null) {
            userCoords
        } else {
            geocodeAddressToLatLng(context, deliveryAddress)
        }
    }
    val isDarkTheme = MaterialTheme.colorScheme.background == BackgroundDark
    val tileUrl = if (isDarkTheme) {
        "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png"
    } else {
        "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
    }
    val defaultMapLabel = if (isDarkTheme) "DARK" else "STREET"
    val mapboxToken = BuildConfig.MAPBOX_ACCESS_TOKEN
    var isPageLoaded by remember { mutableStateOf(false) }
    val webView = remember(context) {
        WebView(context).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            try {
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            } catch (e: Throwable) {}
            
            try {
                WebView.setWebContentsDebuggingEnabled(true)
            } catch (e: Throwable) {}

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    isPageLoaded = true
                    view?.evaluateJavascript("if (typeof map !== 'undefined' && map !== null) { map.invalidateSize(); }", null)
                }

                override fun onReceivedSslError(
                    view: WebView?,
                    handler: android.webkit.SslErrorHandler?,
                    error: android.net.http.SslError?
                ) {
                    handler?.proceed() // bypass SSL clock mismatches in virtual/emulator environments
                }
            }

            webChromeClient = object : android.webkit.WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                    android.util.Log.d("MapWebViewConsole", "[${consoleMessage?.messageLevel()}] ${consoleMessage?.message()} (${consoleMessage?.sourceId()}:${consoleMessage?.lineNumber()})")
                    return true
                }
            }

            addJavascriptInterface(
                LeafletJavascriptInterface(
                    context = context,
                    onMapClickCallback = { lat, lng ->
                        reverseGeocodeAddress(context, lat, lng) { address ->
                            Toast.makeText(context, "Location Selected on Map: $address", Toast.LENGTH_LONG).show()
                        }
                    },
                    onMarkerPlacedCallback = { label, lat, lng ->
                        reverseGeocodeAddress(context, lat, lng) { address ->
                            Toast.makeText(context, "$label Address Marker: $address", Toast.LENGTH_LONG).show()
                        }
                    },
                    onTrackingUpdatedCallback = { lat, lng ->
                        // Leaflet reported coordinates update
                    },
                    onMapTypeToggledCallback = { isSatellite ->
                        onMapTypeToggled(isSatellite)
                    }
                ),
                "AndroidMap"
            )
        }
    }

    /*
     * BACKEND DEVELOPER & DRIVER APP INTEGRATION GUIDE (MANDATORY READ):
     * ===================================================================================================
     * 1. COORDINATE TELEMETRY (Driver App):
     *    The Driver Application should periodically (every 2-3 seconds) POST its current GPS latitude 
     *    and longitude coordinates to the backend database:
     *    ENDPOINT: POST /api/v1/deliveries/{deliveryId}/telemetry
     *    PAYLOAD: { "latitude": 6.4281, "longitude": 3.4219, "speed": 12.5, "bearing": 180.0 }
     *
     * 2. REAL-TIME COORDINATE FEED (This Client App):
     *    This tracking screen uses a reactive progress value simulating route coverage. To connect 
     *    this map to real-time driver streams, implement a WebSocket or Server-Sent Events (SSE) listener:
     *    ENDPOINT: WS /api/v1/deliveries/{deliveryId}/tracking-stream
     *    On receiving message:
     *    { "courierLatitude": 6.5102, "courierLongitude": 3.3912, "progress": 0.62 }
     *    Update the ViewModel state Flow, which will recompose this Composable and trigger
     *    updateCourierProgress(progress) inside Leaflet automatically with fluid CSS animations.
     * ===================================================================================================
     */
    val htmlContent = remember(pickupAddress, deliveryAddress, courierAvatar, routeColor, pickupCoords, deliveryCoords, userAvatar, hasNoBooking, userCoords) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes" />
            <!-- Include both Mapbox and Leaflet for dynamic hybrid loading -->
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <link href="https://api.mapbox.com/mapbox-gl-js/v3.1.2/mapbox-gl.css" rel="stylesheet" />
            <script src="https://api.mapbox.com/mapbox-gl-js/v3.1.2/mapbox-gl.js"></script>
            <style>
                html, body, #map {
                    margin: 0;
                    padding: 0;
                    width: 100%;
                    height: 100%;
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    background: #0E0E10;
                    overflow: hidden;
                    z-index: 1;
                }
                @keyframes icon-pulse {
                    0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(212, 175, 55, 0.7); }
                    70% { transform: scale(1.03); box-shadow: 0 0 0 14px rgba(212, 175, 55, 0); }
                    100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(212, 175, 55, 0); }
                }
                .pulsing-courier {
                    animation: icon-pulse 2s infinite ease-in-out;
                    border-radius: 50%;
                }
                .mapbox-pulsing-courier {
                    width: 36px;
                    height: 36px;
                    border-radius: 50%;
                    border: 2.5px solid #D4AF37;
                    background-size: cover;
                    background-position: center;
                    box-shadow: 0 0 12px rgba(212,175,55,0.7);
                    animation: icon-pulse 2s infinite ease-in-out;
                }
                .map-controls {
                    position: absolute;
                    top: 16px;
                    left: 16px;
                    z-index: 1000;
                    background: rgba(21, 21, 24, 0.95);
                    border: 1.5px solid #D4AF37;
                    border-radius: 24px;
                    padding: 4px;
                    display: flex;
                    gap: 4px;
                }
                .control-btn {
                    background: transparent;
                    border: none;
                    color: #A0AEC0;
                    padding: 6px 14px;
                    font-size: 10px;
                    font-weight: bold;
                    border-radius: 20px;
                    cursor: pointer;
                    transition: all 0.2s ease;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                }
                .control-btn.active {
                    background: #D4AF37;
                    color: #151518;
                }
                
                /* Premium Logistics Address Popups & Tooltips styling */
                .map-tooltip {
                    background: rgba(21, 21, 24, 0.95) !important;
                    border: 1.5px solid #D4AF37 !important;
                    color: #FFFFFF !important;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif !important;
                    font-size: 11px !important;
                    font-weight: 600 !important;
                    border-radius: 8px !important;
                    padding: 6px 10px !important;
                    box-shadow: 0 4px 12px rgba(0,0,0,0.5) !important;
                    white-space: normal !important;
                    max-width: 200px !important;
                    text-align: center !important;
                }
                .leaflet-tooltip-top:before, .map-tooltip:before {
                    border-top-color: #D4AF37 !important;
                }
                .mapboxgl-popup-content {
                    background: rgba(21, 21, 24, 0.95) !important;
                    border: 1.5px solid #D4AF37 !important;
                    color: #FFFFFF !important;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif !important;
                    font-size: 11px !important;
                    font-weight: 600 !important;
                    border-radius: 8px !important;
                    padding: 8px 12px !important;
                    box-shadow: 0 4px 12px rgba(0,0,0,0.5) !important;
                    max-width: 220px !important;
                    text-align: center !important;
                }
                .mapboxgl-popup-anchor-top .mapboxgl-popup-tip {
                    border-bottom-color: #D4AF37 !important;
                }
                .mapboxgl-popup-anchor-bottom .mapboxgl-popup-tip {
                    border-top-color: #D4AF37 !important;
                }
                .mapboxgl-popup-anchor-left .mapboxgl-popup-tip {
                    border-right-color: #D4AF37 !important;
                }
                .mapboxgl-popup-anchor-right .mapboxgl-popup-tip {
                    border-left-color: #D4AF37 !important;
                }
                
                /* Advanced Modern User Location Pointer */
                .user-pointer-container {
                    position: relative;
                    width: 60px;
                    height: 75px;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                }
                .user-pointer-pulse {
                    position: absolute;
                    bottom: 2px;
                    left: 50%;
                    transform: translateX(-50%);
                    width: 24px;
                    height: 10px;
                    background: rgba(212, 175, 55, 0.5);
                    border-radius: 50%;
                    z-index: 1;
                    animation: user-ripple 1.8s infinite ease-out;
                }
                @keyframes user-ripple {
                    0% {
                        transform: translateX(-50%) scale(0.5);
                        opacity: 1;
                    }
                    100% {
                        transform: translateX(-50%) scale(2.8);
                        opacity: 0;
                    }
                }
                .user-pointer-pin {
                    position: absolute;
                    top: 4px;
                    width: 44px;
                    height: 44px;
                    border-radius: 50% 50% 50% 0;
                    background: #151518;
                    border: 3px solid #D4AF37;
                    transform: rotate(-45deg);
                    box-shadow: 0 4px 12px rgba(0,0,0,0.5);
                    z-index: 2;
                    overflow: hidden;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }
                .user-pointer-avatar {
                    width: 38px;
                    height: 38px;
                    border-radius: 50%;
                    transform: rotate(45deg);
                    background-size: cover;
                    background-position: center;
                }
                .user-pointer-dot {
                    position: absolute;
                    bottom: 8px;
                    width: 8px;
                    height: 8px;
                    background: #D4AF37;
                    border-radius: 50%;
                    z-index: 3;
                    border: 1.5px solid #151518;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <div class="map-controls">
                <button class="control-btn active" id="streetBtn" onclick="onToggleClick(false)">$defaultMapLabel</button>
                <button class="control-btn" id="satelliteBtn" onclick="onToggleClick(true)">SATELLITE</button>
            </div>
            <script>
                var pickupLoc = [${pickupCoords.first}, ${pickupCoords.second}]; // [lat, lng]
                var deliveryLoc = [${deliveryCoords.first}, ${deliveryCoords.second}]; // [lat, lng]
                var pickupAddress = "${pickupAddress.replace('"', '\'').replace('\n', ' ')}";
                var deliveryAddress = "${deliveryAddress.replace('"', '\'').replace('\n', ' ')}";
                var isDarkTheme = $isDarkTheme;
                var mapboxToken = '$mapboxToken';
                var hasNoBooking = $hasNoBooking;
                var userAvatar = "$userAvatar";
                var hasUserLoc = ${userCoords != null};
                var userLoc = [${userCoords?.first ?: 0.0}, ${userCoords?.second ?: 0.0}];

                var map = null;
                var isMapboxActive = false;
                var routeLine = null;
                var courierMarker = null;
                var pickupMarker = null;
                var deliveryMarker = null;
                var routeGeometryCoordinates = []; // stores [lat, lng] array for continuous interpolation

                // Fallback tile layers for Leaflet
                var darkTiles = null;
                var satelliteTiles = null;
                var satelliteLabels = null;

                function initMapContainer() {
                    var container = document.getElementById('map');
                    if (!container) return;
                    if (container.offsetWidth === 0 || container.offsetHeight === 0) {
                        requestAnimationFrame(initMapContainer);
                        return;
                    }
                    if (map) return;

                    var hasMapbox = typeof mapboxgl !== 'undefined' && mapboxToken && !mapboxToken.includes('placeholder') && mapboxToken.startsWith('pk.');

                    if (hasMapbox) {
                        try {
                            mapboxgl.accessToken = mapboxToken;
                            var styleMode = '$isSatellite' === 'true' ? 'mapbox://styles/mapbox/satellite-streets-v12' : 
                                            (isDarkTheme ? 'mapbox://styles/mapbox/navigation-night-v1' : 'mapbox://styles/mapbox/navigation-day-v1');
                            
                            var initialCenter = hasUserLoc ? [userLoc[1], userLoc[0]] : [pickupLoc[1], pickupLoc[0]];
                            map = new mapboxgl.Map({
                                container: 'map',
                                style: styleMode,
                                center: initialCenter, // [lng, lat] for Mapbox
                                zoom: $zoom,
                                attributionControl: false
                            });

                            // Setup Mapbox Load Timeout with Leaflet Fallback
                            var mapboxFailed = false;
                            var loadTimeout = setTimeout(function() {
                                if (!map.isStyleLoaded()) {
                                    console.warn("Mapbox style failed to load in time. Falling back to Leaflet.");
                                    fallbackToLeaflet();
                                }
                            }, 3500);

                            map.on('error', function(e) {
                                console.error("Mapbox error encountered:", e);
                                // Trigger Leaflet fallback on authorization failure, rate limit, or style load issue
                                if (!mapboxFailed && e && e.error && (e.error.status === 401 || e.error.status === 403 || e.error.status === 404)) {
                                    mapboxFailed = true;
                                    clearTimeout(loadTimeout);
                                    fallbackToLeaflet();
                                }
                            });

                            map.on('style.load', function() {
                                clearTimeout(loadTimeout);
                            });

                            isMapboxActive = true;
                            loadMapboxFeatures();
                            return;
                        } catch (e) {
                            console.warn("Mapbox initialization failed. Falling back to Leaflet:", e);
                        }
                    }

                    // Leaflet fallback
                    loadLeafletFeatures();
                }

                function fallbackToLeaflet() {
                    if (isMapboxActive) {
                        console.log("Safely falling back to Leaflet tile layer...");
                        isMapboxActive = false;
                        if (map) {
                            try {
                                map.remove();
                            } catch(err) {}
                            map = null;
                        }
                        var container = document.getElementById('map');
                        if (container) {
                            container.innerHTML = ''; // reset Mapbox canvas container
                        }
                        loadLeafletFeatures();
                    }
                }

                // ------------------ MAPBOX IMPLEMENTATION ------------------
                function loadMapboxFeatures() {
                    var pickupEl = document.createElement('div');
                    pickupEl.style.width = '16px';
                    pickupEl.style.height = '16px';
                    pickupEl.style.borderRadius = '50%';
                    pickupEl.style.backgroundColor = '#D4AF37';
                    pickupEl.style.border = '2px solid #000';
                    pickupEl.style.boxShadow = '0 0 8px #D4AF37';

                    var deliveryEl = document.createElement('div');
                    deliveryEl.style.width = '16px';
                    deliveryEl.style.height = '16px';
                    deliveryEl.style.borderRadius = '50%';
                    deliveryEl.style.backgroundColor = '#0E0E10';
                    deliveryEl.style.border = '2px solid #D4AF37';
                    deliveryEl.style.boxShadow = '0 0 8px #D4AF37';

                    var courierEl = document.createElement('div');
                    courierEl.className = 'mapbox-pulsing-courier';
                    courierEl.style.backgroundImage = 'url("$courierAvatar")';

                    if (hasNoBooking) {
                        var userEl = document.createElement('div');
                        userEl.className = 'user-pointer-container';
                        userEl.innerHTML = `
                            <div class="user-pointer-pulse"></div>
                            <div class="user-pointer-pin">
                                <div class="user-pointer-avatar" style="background-image: url('${userAvatar}');"></div>
                            </div>
                            <div class="user-pointer-dot"></div>
                        `;
                        
                        var targetLoc = hasUserLoc ? userLoc : pickupLoc;
                        courierMarker = new mapboxgl.Marker({
                            element: userEl,
                            anchor: 'bottom'
                        }).setLngLat([targetLoc[1], targetLoc[0]]).addTo(map);
                        map.setCenter([targetLoc[1], targetLoc[0]]);
                        map.setZoom(15);
                    } else {
                        // Create and bind gold premium popup overlays for addresses
                        var pickupPopup = new mapboxgl.Popup({ offset: 25, closeButton: false, closeOnClick: false })
                            .setHTML("<div class='map-tooltip-content'><b>Pickup:</b> " + pickupAddress + "</div>");
                        pickupMarker = new mapboxgl.Marker(pickupEl).setLngLat([pickupLoc[1], pickupLoc[0]]).setPopup(pickupPopup).addTo(map);
                        pickupPopup.addTo(map);

                        var deliveryPopup = new mapboxgl.Popup({ offset: 25, closeButton: false, closeOnClick: false })
                            .setHTML("<div class='map-tooltip-content'><b>Delivery:</b> " + deliveryAddress + "</div>");
                        deliveryMarker = new mapboxgl.Marker(deliveryEl).setLngLat([deliveryLoc[1], deliveryLoc[0]]).setPopup(deliveryPopup).addTo(map);
                        deliveryPopup.addTo(map);

                        courierMarker = new mapboxgl.Marker(courierEl).setLngLat([pickupLoc[1], pickupLoc[0]]).addTo(map);

                        if (window.AndroidMap) {
                            window.AndroidMap.onMarkerPlaced("Pickup", pickupLoc[0], pickupLoc[1]);
                            window.AndroidMap.onMarkerPlaced("Delivery", deliveryLoc[0], deliveryLoc[1]);
                        }

                        if (hasUserLoc) {
                            var userPinEl = document.createElement('div');
                            userPinEl.className = 'user-pointer-container';
                            userPinEl.innerHTML = `
                                <div class="user-pointer-pulse"></div>
                                <div class="user-pointer-pin">
                                    <div class="user-pointer-avatar" style="background-image: url('${userAvatar}');"></div>
                                </div>
                                <div class="user-pointer-dot"></div>
                            `;
                            
                            new mapboxgl.Marker({
                                element: userPinEl,
                                anchor: 'bottom'
                            }).setLngLat([userLoc[1], userLoc[0]]).addTo(map);
                        }
                    }

                    map.on('click', function(e) {
                        if (window.AndroidMap) {
                            window.AndroidMap.onMapClick(e.lngLat.lat, e.lngLat.lng);
                        }
                    });

                    map.on('load', function() {
                        if (typeof map.resize === 'function') {
                            map.resize();
                        }
                        var finalCenter = hasUserLoc ? [userLoc[1], userLoc[0]] : [pickupLoc[1], pickupLoc[0]];
                        map.setCenter(finalCenter);
                        
                        setTimeout(function() {
                            if (typeof map.resize === 'function') {
                                map.resize();
                            }
                            map.setCenter(finalCenter);
                        }, 400);

                        if (!hasNoBooking) {
                            fetchOSRMRoute();
                        }
                    });
                }

                function fetchOSRMRoute() {
                    var dirUrl = 'https://router.project-osrm.org/route/v1/driving/' + pickupLoc[1] + ',' + pickupLoc[0] + ';' + deliveryLoc[1] + ',' + deliveryLoc[0] + '?geometries=geojson';
                    fetch(dirUrl)
                        .then(res => res.json())
                        .then(data => {
                            if (data.routes && data.routes.length > 0) {
                                var geojson = data.routes[0].geometry;
                                routeGeometryCoordinates = geojson.coordinates.map(function(coord) {
                                    return [coord[1], coord[0]]; // [lat, lng]
                                });

                                if (isMapboxActive) {
                                    map.addSource('route', {
                                        'type': 'geojson',
                                        'data': {
                                            'type': 'Feature',
                                            'properties': {},
                                            'geometry': geojson
                                        }
                                    });
                                    map.addLayer({
                                        'id': 'route',
                                        'type': 'line',
                                        'source': 'route',
                                        'layout': {
                                            'line-join': 'round',
                                            'line-cap': 'round'
                                        },
                                        'paint': {
                                            'line-color': '$routeColor',
                                            'line-width': 6,
                                            'line-opacity': 0.95
                                        }
                                    });

                                    var bounds = geojson.coordinates.reduce(function(bounds, coord) {
                                        return bounds.extend(coord);
                                    }, new mapboxgl.LngLatBounds(geojson.coordinates[0], geojson.coordinates[0]));
                                    map.fitBounds(bounds, { padding: 50 });
                                } else {
                                    // Leaflet route rendering
                                    if (routeLine) {
                                        routeLine.setLatLngs(routeGeometryCoordinates);
                                    } else {
                                        routeLine = L.polyline(routeGeometryCoordinates, {
                                            color: '$routeColor',
                                            weight: 6,
                                            opacity: 0.95
                                        }).addTo(map);
                                    }
                                    var bounds = L.latLngBounds(routeGeometryCoordinates);
                                    map.fitBounds(bounds, { padding: [50, 50] });
                                }
                            }
                        })
                        .catch(err => {
                            console.error("OSRM directions fetch error:", err);
                        });
                }

                // ------------------ LEAFLET FALLBACK IMPLEMENTATION ------------------
                function loadLeafletFeatures() {
                    var initialCenter = hasUserLoc ? userLoc : pickupLoc;
                    map = L.map('map', {
                        center: initialCenter,
                        zoom: 12,
                        zoomControl: false,
                        attributionControl: false
                    });

                    darkTiles = L.tileLayer('$tileUrl', { maxZoom: 20 });
                    satelliteTiles = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', { maxZoom: 19 });
                    satelliteLabels = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/Reference/World_Boundaries_and_Places/MapServer/tile/{z}/{y}/{x}', { maxZoom: 19 });

                    var isSat = '$isSatellite' === 'true';
                    if (isSat) {
                        satelliteTiles.addTo(map);
                        satelliteLabels.addTo(map); // add boundaries & places label overlay on top of raw satellite tiles!
                        document.getElementById('satelliteBtn').classList.add('active');
                        document.getElementById('streetBtn').classList.remove('active');
                    } else {
                        darkTiles.addTo(map);
                        document.getElementById('streetBtn').classList.add('active');
                        document.getElementById('satelliteBtn').classList.remove('active');
                    }

                    map.on('click', function(e) {
                        if (window.AndroidMap) {
                            window.AndroidMap.onMapClick(e.latlng.lat, e.latlng.lng);
                        }
                    });

                    var goldCircleIcon = L.divIcon({
                        className: 'custom-div-icon',
                        html: "<div style='width: 16px; height: 16px; border-radius: 50%; background-color: #D4AF37; border: 2px solid #000; box-shadow: 0 0 8px #D4AF37;'></div>",
                        iconSize: [16, 16],
                        iconAnchor: [8, 8]
                    });

                    var darkCircleIcon = L.divIcon({
                        className: 'custom-div-icon',
                        html: "<div style='width: 16px; height: 16px; border-radius: 50%; background-color: #0E0E10; border: 2px solid #D4AF37; box-shadow: 0 0 8px #D4AF37;'></div>",
                        iconSize: [16, 16],
                        iconAnchor: [8, 8]
                    });

                    var courierIcon = L.divIcon({
                        className: 'pulsing-courier',
                        html: "<div style='width: 36px; height: 36px; border-radius: 50%; border: 2.5px solid #D4AF37; background-image: url(\"$courierAvatar\"); background-size: cover; box-shadow: 0 0 12px rgba(212,175,55,0.7);'></div>",
                        iconSize: [36, 36],
                        iconAnchor: [18, 18]
                    });

                    if (hasNoBooking) {
                        var userIcon = L.divIcon({
                            className: 'user-leaflet-pointer',
                            html: `
                                <div class="user-pointer-container">
                                    <div class="user-pointer-pulse"></div>
                                    <div class="user-pointer-pin">
                                        <div class="user-pointer-avatar" style="background-image: url('${userAvatar}');"></div>
                                    </div>
                                    <div class="user-pointer-dot"></div>
                                </div>
                            `,
                            iconSize: [60, 75],
                            iconAnchor: [30, 63]
                        });
                        var targetLoc = hasUserLoc ? userLoc : pickupLoc;
                        courierMarker = L.marker(targetLoc, { icon: userIcon }).addTo(map);
                        map.setView(targetLoc, 15);
                    } else {
                        // Create and bind tooltips to display beautiful text bubbles with exact address details
                        pickupMarker = L.marker(pickupLoc, { icon: goldCircleIcon }).addTo(map);
                        pickupMarker.bindTooltip("<b>Pickup Location</b><br>" + pickupAddress, { permanent: true, direction: 'top', className: 'map-tooltip' });

                        deliveryMarker = L.marker(deliveryLoc, { icon: darkCircleIcon }).addTo(map);
                        deliveryMarker.bindTooltip("<b>Delivery Location</b><br>" + deliveryAddress, { permanent: true, direction: 'top', className: 'map-tooltip' });

                        courierMarker = L.marker(pickupLoc, { icon: courierIcon }).addTo(map);

                        if (window.AndroidMap) {
                            window.AndroidMap.onMarkerPlaced("Pickup", pickupLoc[0], pickupLoc[1]);
                            window.AndroidMap.onMarkerPlaced("Delivery", deliveryLoc[0], deliveryLoc[1]);
                        }

                        fetchOSRMRoute();

                        if (hasUserLoc) {
                            var userPinIcon = L.divIcon({
                                className: 'user-leaflet-pointer',
                                html: `
                                    <div class="user-pointer-container">
                                        <div class="user-pointer-pulse"></div>
                                        <div class="user-pointer-pin">
                                            <div class="user-pointer-avatar" style="background-image: url('${userAvatar}');"></div>
                                        </div>
                                        <div class="user-pointer-dot"></div>
                                    </div>
                                `,
                                iconSize: [60, 75],
                                iconAnchor: [30, 63]
                            });
                            L.marker(userLoc, { icon: userPinIcon }).addTo(map);
                        }
                    }

                    setTimeout(function() {
                        map.invalidateSize();
                        var finalCenter = hasUserLoc ? userLoc : pickupLoc;
                        map.setView(finalCenter, 15);
                    }, 400);

                    window.addEventListener('resize', function() {
                        if (map) map.invalidateSize();
                    });
                }

                // ------------------ SHARED LOGIC ------------------
                function updateMapType(isSatellite) {
                    if (!map) return;
                    if (isMapboxActive) {
                        var style = isSatellite ? 'mapbox://styles/mapbox/satellite-streets-v12' : 
                                    (isDarkTheme ? 'mapbox://styles/mapbox/navigation-night-v1' : 'mapbox://styles/mapbox/navigation-day-v1');
                        map.setStyle(style);
                    } else {
                        if (isSatellite) {
                            map.removeLayer(darkTiles);
                            satelliteTiles.addTo(map);
                            satelliteLabels.addTo(map); // add labels layer over satellite tiles
                        } else {
                            try { map.removeLayer(satelliteTiles); } catch(e){}
                            try { map.removeLayer(satelliteLabels); } catch(e){}
                            darkTiles.addTo(map);
                        }
                    }

                    if (isSatellite) {
                        document.getElementById('satelliteBtn').classList.add('active');
                        document.getElementById('streetBtn').classList.remove('active');
                    } else {
                        document.getElementById('streetBtn').classList.add('active');
                        document.getElementById('satelliteBtn').classList.remove('active');
                    }
                }

                function onToggleClick(isSat) {
                    updateMapType(isSat);
                    if (window.AndroidMap) {
                        window.AndroidMap.onMapTypeToggled(isSat);
                    }
                }

                function updateTraffic(showTraffic) {
                    console.log("Traffic toggled: " + showTraffic);
                }

                // Path Coordinate Interpolator along actual OSRM routed road path
                function getCoordinateAlongRoute(coords, fraction) {
                    if (!coords || coords.length === 0) return null;
                    if (fraction <= 0) return coords[0];
                    if (fraction >= 1) return coords[coords.length - 1];
                    
                    var totalDistance = 0;
                    var segmentDistances = [];
                    for (var i = 0; i < coords.length - 1; i++) {
                        var d = distanceBetween(coords[i], coords[i+1]);
                        segmentDistances.push(d);
                        totalDistance += d;
                    }
                    
                    if (totalDistance === 0) return coords[0];
                    
                    var targetDistance = fraction * totalDistance;
                    var accumulatedDistance = 0;
                    for (var i = 0; i < segmentDistances.length; i++) {
                        if (accumulatedDistance + segmentDistances[i] >= targetDistance) {
                            var segFraction = (targetDistance - accumulatedDistance) / segmentDistances[i];
                            var p1 = coords[i];
                            var p2 = coords[i+1];
                            return [
                                p1[0] + (p2[0] - p1[0]) * segFraction,
                                p1[1] + (p2[1] - p1[1]) * segFraction
                            ];
                        }
                        accumulatedDistance += segmentDistances[i];
                    }
                    return coords[coords.length - 1];
                }

                function distanceBetween(p1, p2) {
                    var dy = p1[0] - p2[0];
                    var dx = p1[1] - p2[1];
                    return Math.sqrt(dx * dx + dy * dy);
                }

                function updateCourierProgress(progressVal) {
                    var lat, lng;
                    if (routeGeometryCoordinates && routeGeometryCoordinates.length > 0) {
                        var coord = getCoordinateAlongRoute(routeGeometryCoordinates, progressVal);
                        lat = coord[0];
                        lng = coord[1];
                    } else {
                        // linear straight-line fallback
                        lat = pickupLoc[0] + (deliveryLoc[0] - pickupLoc[0]) * progressVal;
                        lng = pickupLoc[1] + (deliveryLoc[1] - pickupLoc[1]) * progressVal;
                    }

                    var hasRealCoords = ${courierLatitude != null && courierLongitude != null};
                    if (hasRealCoords) {
                        lat = ${courierLatitude ?: 6.5244};
                        lng = ${courierLongitude ?: 3.3792};
                    }

                    if (courierMarker) {
                        if (isMapboxActive) {
                            courierMarker.setLngLat([lng, lat]);
                        } else {
                            courierMarker.setLatLng([lat, lng]);
                        }
                    }
                    if (window.AndroidMap) {
                        window.AndroidMap.onTrackingUpdated(lat, lng);
                    }
                }

                function updateCourierCoordinates(latVal, lngVal) {
                    if (courierMarker) {
                        if (isMapboxActive) {
                            courierMarker.setLngLat([lngVal, latVal]);
                            if (map) map.panTo([lngVal, latVal]);
                        } else {
                            courierMarker.setLatLng([latVal, lngVal]);
                            if (map) map.panTo([latVal, lngVal]);
                        }
                        if (window.AndroidMap) {
                            window.AndroidMap.onTrackingUpdated(latVal, lngVal);
                        }
                    }
                }

                window.addEventListener('DOMContentLoaded', initMapContainer);
                setTimeout(initMapContainer, 300);
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    LaunchedEffect(htmlContent) {
        isPageLoaded = false
        webView.loadDataWithBaseURL("https://localhost", htmlContent, "text/html", "UTF-8", null)
    }

    LaunchedEffect(isSatellite, isPageLoaded) {
        if (isPageLoaded) {
            webView.evaluateJavascript("updateMapType($isSatellite)", null)
        }
    }

    LaunchedEffect(showTraffic, isPageLoaded) {
        if (isPageLoaded) {
            webView.evaluateJavascript("updateTraffic($showTraffic)", null)
        }
    }

    LaunchedEffect(zoom, isPageLoaded) {
        if (isPageLoaded) {
            webView.evaluateJavascript("map.setZoom($zoom)", null)
        }
    }

    LaunchedEffect(progress, isPageLoaded) {
        if (isPageLoaded) {
            webView.evaluateJavascript("updateCourierProgress($progress)", null)
        }
    }

    LaunchedEffect(courierLatitude, courierLongitude, isPageLoaded) {
        if (isPageLoaded && courierLatitude != null && courierLongitude != null) {
            webView.evaluateJavascript("updateCourierCoordinates($courierLatitude, $courierLongitude)", null)
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier
    )
}

@Composable
fun DeliveryEstimationCard(
    status: ParcelStatus,
    progress: Float,
    isDark: Boolean
) {
    val isLight = !isDark
    val estimationText = when (status) {
        ParcelStatus.PENDING -> "Awaiting Assignment"
        ParcelStatus.ASSIGNED -> "Preparing for Pickup"
        ParcelStatus.DELIVERED -> "Delivered"
        ParcelStatus.OUT_FOR_DELIVERY -> "Today, July 7, 2026"
        ParcelStatus.CANCELLED -> "No Delivery (Cancelled)"
        ParcelStatus.TRANSIT -> {
            when {
                progress >= 0.8f -> "Today, July 7, 2026"
                progress >= 0.45f -> "Tomorrow, July 8, 2026"
                else -> "Thursday, July 9, 2026"
            }
        }
    }

    val windowText = when (status) {
        ParcelStatus.PENDING -> "Waiting for dispatcher to assign a courier"
        ParcelStatus.ASSIGNED -> "Courier has been dispatched to pickup location"
        ParcelStatus.DELIVERED -> "Delivered at 1:15 PM"
        ParcelStatus.OUT_FOR_DELIVERY -> "Expected between 2:00 PM - 6:00 PM"
        ParcelStatus.CANCELLED -> "Shipment was cancelled by sender"
        ParcelStatus.TRANSIT -> {
            when {
                progress >= 0.8f -> "Expected between 3:00 PM - 7:00 PM"
                progress >= 0.45f -> "Expected between 9:00 AM - 1:00 PM"
                else -> "Expected between 10:00 AM - 5:00 PM"
            }
        }
    }

    val confidenceScore = when (status) {
        ParcelStatus.PENDING -> "N/A"
        ParcelStatus.ASSIGNED -> "96.5% Precision"
        ParcelStatus.DELIVERED -> "100% Verified"
        ParcelStatus.OUT_FOR_DELIVERY -> "99.2% Accurate"
        ParcelStatus.CANCELLED -> "N/A"
        ParcelStatus.TRANSIT -> {
            when {
                progress >= 0.8f -> "98.6% Precision"
                progress >= 0.45f -> "95.4% Precision"
                else -> "92.1% Precision"
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("delivery_estimation_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLight) GoldenWhiteLight else Charcoal.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, if (isLight) Slate else Gold.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isLight) Obsidian.copy(alpha = 0.05f) else Gold.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (status == ParcelStatus.DELIVERED) Icons.Default.CheckCircle else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (status == ParcelStatus.DELIVERED) Color(0xFF4CAF50) else (if (isDark) Gold else Obsidian),
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ESTIMATED ARRIVAL",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    letterSpacing = 1.sp
                )
                Text(
                    text = estimationText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppOnSurface
                )
                Text(
                    text = windowText,
                    fontSize = 11.sp,
                    color = TextGray
                )
            }
            if (status != ParcelStatus.CANCELLED) {
                Surface(
                    color = if (isLight) Obsidian.copy(alpha = 0.08f) else Gold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = confidenceScore,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLight) Obsidian else Gold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedStatusBadge(
    status: ParcelStatus,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 10.sp,
    paddingHorizontal: androidx.compose.ui.unit.Dp = 8.dp,
    paddingVertical: androidx.compose.ui.unit.Dp = 3.dp
) {
    val targetBgColor = when (status) {
        ParcelStatus.PENDING -> Color(0x202196F3)
        ParcelStatus.ASSIGNED -> Color(0x209C27B0)
        ParcelStatus.DELIVERED -> Color(0x204CAF50)
        ParcelStatus.OUT_FOR_DELIVERY -> Color(0x20FF9800)
        ParcelStatus.CANCELLED -> Color(0x20F44336)
        ParcelStatus.TRANSIT -> if (isDark) Gold.copy(alpha = 0.15f) else Color(0x100E0E10)
    }
    val targetTextColor = when (status) {
        ParcelStatus.PENDING -> Color(0xFF2196F3)
        ParcelStatus.ASSIGNED -> Color(0xFF9C27B0)
        ParcelStatus.DELIVERED -> Color(0xFF4CAF50)
        ParcelStatus.OUT_FOR_DELIVERY -> Color(0xFFFF9800)
        ParcelStatus.CANCELLED -> Color(0xFFF44336)
        ParcelStatus.TRANSIT -> if (isDark) Gold else Obsidian
    }
    val badgeText = when (status) {
        ParcelStatus.PENDING -> "Pending Dispatch"
        ParcelStatus.ASSIGNED -> "Courier Assigned"
        ParcelStatus.TRANSIT -> "In Transit"
        ParcelStatus.OUT_FOR_DELIVERY -> "Out for Delivery"
        ParcelStatus.DELIVERED -> "Delivered"
        ParcelStatus.CANCELLED -> "Cancelled"
    }

    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
        label = "badgeBgColorAnim"
    )
    val animatedTextColor by animateColorAsState(
        targetValue = targetTextColor,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
        label = "badgeTextColorAnim"
    )

    Surface(
        color = animatedBgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        AnimatedContent(
            targetState = badgeText,
            transitionSpec = {
                (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300)))
                    .togetherWith(fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.95f, animationSpec = tween(300)))
            },
            label = "badgeTextAnim"
        ) { text ->
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = animatedTextColor,
                modifier = Modifier.padding(horizontal = paddingHorizontal, vertical = paddingVertical)
            )
        }
    }
}

fun authenticateBiometric(
    activity: android.app.Activity,
    title: String,
    subtitle: String,
    description: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
        val executor = activity.mainExecutor
        val biometricPrompt = android.hardware.biometrics.BiometricPrompt.Builder(activity)
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButton("Cancel", executor, { _, _ ->
                onError("Cancelled")
            })
            .build()

        val cancellationSignal = android.os.CancellationSignal()
        biometricPrompt.authenticate(
            cancellationSignal,
            executor,
            object : android.hardware.biometrics.BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: android.hardware.biometrics.BiometricPrompt.AuthenticationResult?) {
                    super.onAuthenticationSucceeded(result)
                    activity.runOnUiThread {
                        onSuccess()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    activity.runOnUiThread {
                        onError("Failed")
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    super.onAuthenticationError(errorCode, errString)
                    activity.runOnUiThread {
                        onError(errString?.toString() ?: "Error")
                    }
                }
            }
        )
    } else {
        onSuccess()
    }
}

@Composable
fun ShippingJourneyProgressBar(
    status: ParcelStatus,
    progress: Float,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val isLight = !isDark
    
    // Define steps
    val steps = listOf("Ordered", "Shipped", "In Transit", "Delivered")
    
    // Determine active step index
    val activeIndex = when {
        status == ParcelStatus.CANCELLED -> -1
        status == ParcelStatus.DELIVERED || progress >= 1.0f -> 3
        status == ParcelStatus.OUT_FOR_DELIVERY || progress >= 0.7f -> 2
        progress >= 0.35f -> 1
        else -> 0
    }
    
    // Progress line mapping (fraction of track that is filled)
    val targetProgressFraction = when {
        status == ParcelStatus.CANCELLED -> 0f
        activeIndex == 3 -> 1.0f
        activeIndex == 2 -> 0.66f + ((progress - 0.7f).coerceAtLeast(0f) * 1.03f).coerceAtMost(0.34f)
        activeIndex == 1 -> 0.33f + ((progress - 0.35f).coerceAtLeast(0f) * 0.94f).coerceAtMost(0.33f)
        else -> 0.0f + (progress * 0.94f).coerceAtMost(0.33f)
    }.coerceIn(0f, 1f)
    
    val animatedProgressFraction by animateFloatAsState(
        targetValue = targetProgressFraction,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "animatedProgressFraction"
    )
    
    // Active step infinite pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutQuart),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutQuart),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Horizontal Track Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Background line (unfilled track)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(if (isLight) BorderLight else Charcoal)
            )
            
            // Foreground line (animated filled track)
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgressFraction)
                    .padding(horizontal = 24.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Gold)
            )
            
            // Nodes (Ordered, Shipped, In Transit, Delivered)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, title ->
                    val isCompleted = index <= activeIndex && status != ParcelStatus.CANCELLED
                    val isActive = index == activeIndex && status != ParcelStatus.CANCELLED
                    
                    // Animated Node Scale on activation
                    val nodeScale by animateFloatAsState(
                        targetValue = if (isActive) 1.2f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                        label = "nodeScale"
                    )
                    
                    // Animated Node Color
                    val animatedNodeBgColor by animateColorAsState(
                        targetValue = when {
                            isActive -> Gold
                            isCompleted -> Gold
                            else -> if (isLight) BorderLight else Charcoal
                        },
                        animationSpec = tween(500),
                        label = "nodeBgColor"
                    )
                    
                    val animatedNodeContentColor by animateColorAsState(
                        targetValue = when {
                            isActive || isCompleted -> Obsidian
                            else -> if (isLight) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                        },
                        animationSpec = tween(500),
                        label = "nodeContentColor"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .graphicsLayer {
                                scaleX = nodeScale
                                scaleY = nodeScale
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Pulse overlay for the active step
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .graphicsLayer {
                                        scaleX = pulseScale
                                        scaleY = pulseScale
                                        alpha = pulseAlpha
                                    }
                                    .background(Gold)
                            )
                        }
                        
                        // Main node circle
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(animatedNodeBgColor)
                                .border(
                                    width = if (isActive) 2.dp else 1.dp,
                                    color = if (isActive) (if (isLight) Obsidian else Color.White) else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val icon = when (index) {
                                0 -> Icons.Default.ReceiptLong
                                1 -> Icons.Default.Storefront
                                2 -> Icons.Default.LocalShipping
                                else -> Icons.Default.CheckCircle
                            }
                            
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = animatedNodeContentColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Labels row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, title ->
                val isCompleted = index <= activeIndex && status != ParcelStatus.CANCELLED
                val isActive = index == activeIndex && status != ParcelStatus.CANCELLED
                
                val labelColor by animateColorAsState(
                    targetValue = when {
                        isActive -> if (isLight) Obsidian else Gold
                        isCompleted -> AppOnSurface
                        else -> TextGray
                    },
                    animationSpec = tween(500),
                    label = "labelColor"
                )
                
                val labelWeight = if (isActive || isCompleted) FontWeight.ExtraBold else FontWeight.Medium
                
                Box(
                    modifier = Modifier.width(76.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = labelWeight,
                        color = labelColor,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedSearchIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "searchState")
    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "scale"
    )
    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "rotate"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .graphicsLayer {
                scaleX = scaleFactor
                scaleY = scaleFactor
                rotationZ = rotateAngle
            },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2
            val cy = h / 2
            val goldColor = Color(0xFFE5A93B)
            
            // Draw a Radar scan circle
            drawCircle(
                color = goldColor.copy(alpha = 0.08f),
                radius = 35.dp.toPx(),
                center = Offset(cx, cy)
            )
            drawCircle(
                color = goldColor.copy(alpha = 0.15f),
                radius = 25.dp.toPx(),
                center = Offset(cx, cy),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
            )

            // Draw a simple modern magnifying glass
            val lensRadius = 12.dp.toPx()
            val lensCx = cx - 4.dp.toPx()
            val lensCy = cy - 4.dp.toPx()
            
            // Handle of magnifying glass
            drawLine(
                color = goldColor,
                start = Offset(lensCx + 8.dp.toPx(), lensCy + 8.dp.toPx()),
                end = Offset(cx + 20.dp.toPx(), cy + 20.dp.toPx()),
                strokeWidth = 3.5f.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            // Lens frame
            drawCircle(
                color = goldColor,
                radius = lensRadius,
                center = Offset(lensCx, lensCy),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
            )
            // Lens glass reflection
            drawCircle(
                color = goldColor.copy(alpha = 0.15f),
                radius = lensRadius - 1.5f.dp.toPx(),
                center = Offset(lensCx, lensCy)
            )
        }
    }
}

@androidx.compose.runtime.Composable
fun SkeletonBox(
    modifier: Modifier,
    isLight: Boolean = false,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(4.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .shimmer(isLight = isLight)
    )
}

fun Modifier.shimmer(
    isLight: Boolean = false,
    durationMillis: Int = 1200
): Modifier = composed {
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(durationMillis, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    val baseColor = if (isLight) Color(0xFFE0E0E0) else Charcoal
    val highlightColor = if (isLight) Color(0xFFF5F5F5) else Color(0xFF2D2D2D)
    val shimmerColors = listOf(
        baseColor,
        if (isLight) highlightColor else Color(0xFFD4AF37).copy(alpha = 0.25f),
        baseColor
    )

    this.drawBehind {
        val brush = androidx.compose.ui.graphics.Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnim.value - 300f, 0f),
            end = Offset(translateAnim.value, 300f)
        )
        drawRect(brush = brush)
    }
}

fun reverseGeocodeAddress(context: android.content.Context, lat: Double, lng: Double, onResult: (String) -> Unit) {
    val coroutineScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
    coroutineScope.launch {
        val addressText = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addrLine = addresses[0].getAddressLine(0)
                    if (!addrLine.isNullOrBlank()) return@withContext addrLine
                }
            } catch (e: Exception) {
                android.util.Log.e("TrackingGeocoder", "System Geocoder failed: ${e.message}")
            }

            try {
                val url = java.net.URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lng&addressdetails=1")
                val urlConnection = url.openConnection() as java.net.HttpURLConnection
                urlConnection.setRequestProperty("User-Agent", "EngracedDispatchAndroidApp/1.0 (reachheytek@gmail.com)")
                urlConnection.connectTimeout = 3000
                urlConnection.readTimeout = 3000
                val response = urlConnection.inputStream.bufferedReader().use { it.readText() }
                val json = org.json.JSONObject(response)
                val displayName = json.optString("display_name")
                if (!displayName.isNullOrBlank()) {
                    return@withContext displayName
                }
            } catch (e: Exception) {
                android.util.Log.e("TrackingGeocoder", "OSM Nominatim failed: ${e.message}")
            }

            // Local Lagos Landmark geocoder fallback (highly robust)
            val landmarks = listOf(
                Triple(6.6018, 3.3515, "Ikeja City Mall, Obafemi Awolowo Way, Ikeja, Lagos"),
                Triple(6.5244, 3.3792, "Murtala Muhammed International Airport (LOS), Airport Road, Ikeja, Lagos"),
                Triple(6.4281, 3.4219, "Lekki Conservation Centre, Lekki-Epe Expressway, Lagos"),
                Triple(6.4633, 3.3672, "National Theatre, Iganmu, Surulere, Lagos"),
                Triple(6.5158, 3.3897, "University of Lagos, Akoka, Yaba, Lagos"),
                Triple(6.4265, 3.4300, "Lekki Phase 1, Victoria Island, Lagos"),
                Triple(6.4446, 3.4912, "Chevron Drive, Lekki, Lagos"),
                Triple(6.4549, 3.4244, "Ikoyi Club 1938, Ikoyi, Lagos"),
                Triple(6.4474, 3.4735, "Nike Art Gallery, Elegushi, Lekki, Lagos"),
                Triple(6.4501, 3.3958, "Lagos Island, Marina, Lagos")
            )

            val nearest = landmarks.minByOrNull { (lLat, lLng, _) ->
                val dLat = lat - lLat
                val dLng = lng - lLng
                dLat * dLat + dLng * dLng
            }

            if (nearest != null) {
                nearest.third
            } else {
                "Admiralty Way, Lekki Phase 1, Lagos"
            }
        }
        onResult(addressText)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryFeedbackDialog(
    parcel: Parcel,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Double, Double) -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var selectedTipIndex by remember { mutableStateOf(1) } // Default to index 1 (₦1,000)
    val tipOptions = listOf(0.0, 500.0, 1000.0, 2000.0, -1.0) // -1.0 is Custom
    var customTipString by remember { mutableStateOf("") }

    val tipAmount = if (selectedTipIndex == 4) {
        customTipString.toDoubleOrNull() ?: 0.0
    } else {
        tipOptions.getOrElse(selectedTipIndex) { 0.0 }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(16.dp),
        content = {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = if (isDark) Charcoal else Color.White,
                tonalElevation = 6.dp,
                border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.2f) else Color(0xFFE5E7EB))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Title Header (Luxury Gold Background in Dark Mode, Obsidian in Light Mode)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(
                                if (isDark) Gold else Obsidian,
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "RATE & TIP COURIER",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            color = if (isDark) Obsidian else Gold
                        )
                    }

                    Text(
                        text = "Your feedback helps us maintain the Engraced Dispatch premium standard.",
                        fontSize = 11.sp,
                        color = if (isDark) TextGray else Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Courier Information Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isDark) LuxuryBlack.copy(alpha = 0.5f) else Color(0xFFF3F4F6),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rider avatar
                        Image(
                            painter = rememberAsyncImagePainter(parcel.courierAvatar.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&h=150&fit=crop" }),
                            contentDescription = "Courier Avatar",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = parcel.courierName.ifEmpty { "Adebayo Richard" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Obsidian
                            )
                            Text(
                                text = "Premium Courier • ${parcel.riderBikeNumber.ifEmpty { "LA-329-DIS" }}",
                                fontSize = 10.sp,
                                color = TextGray
                            )
                        }
                    }

                    // Interactive Star Rating Selector
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Rate Delivery Service",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) GoldLight else Obsidian
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = { rating = star },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "$star Stars",
                                        tint = if (star <= rating) Gold else if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.15f),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Tip Presets Grid
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Add Courier Tip",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) GoldLight else Obsidian
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val listLabels = listOf("No Tip", "₦500", "₦1K", "₦2K", "Custom")
                            listLabels.forEachIndexed { idx, label ->
                                Button(
                                    onClick = { selectedTipIndex = idx },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedTipIndex == idx) Gold else (if (isDark) LuxuryBlack else Color(0xFFF3F4F6))
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (selectedTipIndex == idx) Gold else (if (isDark) Gold.copy(alpha = 0.2f) else Color.Transparent)
                                    )
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTipIndex == idx) Obsidian else (if (isDark) Color.White else Obsidian)
                                    )
                                }
                            }
                        }

                        if (selectedTipIndex == 4) {
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = customTipString,
                                onValueChange = { customTipString = it.filter { char -> char.isDigit() } },
                                label = { Text("Custom Tip Amount (₦)", fontSize = 11.sp, color = if (isDark) GoldLight else Obsidian) },
                                leadingIcon = { Text("₦", fontSize = 13.sp, color = if (isDark) Gold else Obsidian, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Gold,
                                    unfocusedBorderColor = if (isDark) Gold.copy(alpha = 0.3f) else Color.LightGray,
                                    focusedLabelColor = Gold,
                                    unfocusedLabelColor = if (isDark) GoldLight else Obsidian,
                                    focusedContainerColor = if (isDark) LuxuryBlack else Color.White,
                                    unfocusedContainerColor = if (isDark) LuxuryBlack else Color.White,
                                    focusedTextColor = if (isDark) Color.White else Obsidian,
                                    unfocusedTextColor = if (isDark) Color.White else Obsidian
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = if (isDark) Color.White else Obsidian),
                                maxLines = 1,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Dialog Actions (Submit or Cancel)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Obsidian else Color(0xFFEEEEEE)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.3f) else Color.Transparent)
                        ) {
                            Text(
                                text = "Skip/Cancel",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Gold else Obsidian
                            )
                        }

                        Button(
                            onClick = { onSubmit(rating.toDouble(), tipAmount) },
                            modifier = Modifier
                                .weight(1.8f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Gold
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Submit Feedback",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Obsidian
                            )
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcelChatDialog(
    parcelId: String,
    senderRole: String, // "customer" or "rider"
    viewModel: DeliveryViewModel,
    onDismiss: () -> Unit
) {
    val isDark = viewModel.darkModeEnabled.collectAsState().value
    val chatMessages by viewModel.activeParcelChats.collectAsState()
    val scope = rememberCoroutineScope()
    var messageInput by remember { mutableStateOf("") }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(parcelId) {
        viewModel.startListeningToParcelChats(parcelId)
    }

    DisposableEffect(parcelId) {
        onDispose {
            viewModel.stopListeningToParcelChats()
        }
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp), // status bar spacer
            color = LuxuryBlack
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header (matching Dark Mode / Light Mode header rules)
                val headerBg = if (isDark) Gold else Obsidian
                val headerContentColor = if (isDark) Obsidian else Color.White

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBg)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = headerContentColor
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ENGRACED DISPATCH CHAT",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = headerContentColor,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (senderRole == "customer") "Active Courier Support" else "Recipient Direct Contact",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Obsidian.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(if (isDark) Obsidian.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "ID: #${parcelId.take(6).uppercase()}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = headerContentColor
                        )
                    }
                }

                // Chat Messages List
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(LuxuryBlack)
                        .padding(horizontal = 16.dp)
                ) {
                    if (chatMessages.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "No Chats",
                                tint = TextGray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Start the Conversation",
                                color = AppTextColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Send a secure real-time message to coordinate delivery routing or special instructions.",
                                color = TextGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(chatMessages) { chat ->
                                val isMe = chat.senderRole == senderRole
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                                ) {
                                    Column(
                                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                                    ) {
                                        // Sender tag
                                        Text(
                                            text = if (isMe) "You" else chat.senderName,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextGray,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )

                                        // Message bubble
                                        val bubbleColor = if (isMe) Gold else Charcoal
                                        val textColor = if (isMe) Obsidian else Color.White // STRICT CONTRAST: Obsidian on Gold
                                        val bubbleShape = if (isMe) {
                                            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
                                        } else {
                                            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
                                        }

                                        Surface(
                                            shape = bubbleShape,
                                            color = bubbleColor,
                                            modifier = Modifier.widthIn(max = 280.dp),
                                            border = if (!isMe) BorderStroke(1.dp, BorderDark) else null
                                        ) {
                                            Text(
                                                text = chat.messageText,
                                                color = textColor,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Input Row
                Surface(
                    color = Charcoal,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, BorderDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageInput,
                            onValueChange = { messageInput = it },
                            placeholder = { Text("Enter premium dispatch instruction...", fontSize = 13.sp, color = TextGray) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(max = 100.dp),
                            shape = RoundedCornerShape(20.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = LuxuryBlack,
                                unfocusedContainerColor = LuxuryBlack,
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = BorderColor,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = Color.White)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        IconButton(
                            onClick = {
                                if (messageInput.trim().isNotEmpty()) {
                                    val txt = messageInput.trim()
                                    messageInput = ""
                                    viewModel.sendParcelChatMessage(parcelId, senderRole, txt) { success, _ ->
                                        if (!success) {
                                            // Handle error
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (messageInput.trim().isNotEmpty()) Gold else BorderColor),
                            enabled = messageInput.trim().isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (messageInput.trim().isNotEmpty()) Obsidian else TextGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private suspend fun detectUserLocationCoords(context: android.content.Context): Pair<Double, Double> {
    try {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager
            val providers = locationManager?.getProviders(true)
            var bestLocation: android.location.Location? = null
            if (providers != null) {
                for (provider in providers) {
                    val loc = locationManager.getLastKnownLocation(provider) ?: continue
                    if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                        bestLocation = loc
                    }
                }
            }
            if (bestLocation != null) {
                return Pair(bestLocation.latitude, bestLocation.longitude)
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("DetectLocationCoords", "GPS detection failed: ${e.message}")
    }

    try {
        val url = java.net.URL("https://ipapi.co/json/")
        val urlConnection = url.openConnection() as java.net.HttpURLConnection
        urlConnection.setRequestProperty("User-Agent", "EngracedDispatchAndroidApp/1.0")
        urlConnection.connectTimeout = 3000
        urlConnection.readTimeout = 3000
        val response = urlConnection.inputStream.bufferedReader().use { it.readText() }
        val json = org.json.JSONObject(response)
        val lat = json.optDouble("latitude", Double.NaN)
        val lng = json.optDouble("longitude", Double.NaN)
        if (!lat.isNaN() && !lng.isNaN()) {
            return Pair(lat, lng)
        }
    } catch (e: Exception) {
        android.util.Log.e("DetectLocationCoords", "GeoIP fallback failed: ${e.message}")
    }

    return Pair(6.4281, 3.4219)
}


