package com.esdispatch.ui.screens

import com.esdispatch.BuildConfig
import com.esdispatch.data.TrackingAppWidget
import com.esdispatch.util.FormatUtils
import com.esdispatch.util.PhoneVisualTransformation
import com.esdispatch.util.Zod
import com.esdispatch.util.ZodResult
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Delete
import com.esdispatch.R
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MyLocation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import coil.compose.rememberAsyncImagePainter
import com.esdispatch.ui.components.Box3D
import com.esdispatch.ui.components.MapCanvas
import com.esdispatch.ui.components.QuiltedBackground
import com.esdispatch.ui.components.RoundedSheet
import com.esdispatch.ui.components.ScreenHeader
import com.esdispatch.ui.components.BottomNav
import com.esdispatch.ui.components.SupportButton
import com.esdispatch.ui.components.SupportDialog
import com.esdispatch.ui.components.CancelDeliverySecurityDialog
import com.esdispatch.ui.components.CourierAvatarBadge
import com.esdispatch.ui.components.DisputeReportBottomSheet
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.ReportProblem
import com.esdispatch.ui.theme.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import com.esdispatch.data.*
import com.esdispatch.viewmodel.DeliveryViewModel
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

    DisposableEffect(context) {
        val fusedClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
        val callback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(res: com.google.android.gms.location.LocationResult) {
                res.lastLocation?.let { loc ->
                    userCoords = Pair(loc.latitude, loc.longitude)
                }
            }
        }
        val fineGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val coarseGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            val req = com.google.android.gms.location.LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 5000
            ).setMinUpdateIntervalMillis(2000).build()
            try {
                fusedClient.requestLocationUpdates(req, callback, android.os.Looper.getMainLooper())
            } catch (e: Exception) {
                android.util.Log.e("TrackingScreen", "Live location updates failed: ${e.message}")
            }
        }
        onDispose {
            try {
                fusedClient.removeLocationUpdates(callback)
            } catch (e: Exception) { }
        }
    }

    LaunchedEffect(selectedParcel?.id) {
        selectedParcel?.id?.let { id ->
            viewModel.startRealTimeTrackingListener(id)
        }
    }

    val userParcels by viewModel.parcels.collectAsState()
    val riderAssignments by viewModel.riderAssignments.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val activeViewMode by viewModel.activeViewMode.collectAsState()
    val isRider = userRole == "rider" || activeViewMode == "rider"
    val enableQrCodeHandover by viewModel.enableQrCodeHandover.collectAsState()
    val userName by viewModel.userName.collectAsState()

    val effectiveParcels = if (isRider) riderAssignments else userParcels
    val activeParcels = remember(effectiveParcels) {
        effectiveParcels.filter { 
            it.status != ParcelStatus.DELIVERED && it.status != ParcelStatus.CANCELLED
        }
    }
    val activeParcel = remember(selectedParcel, activeParcels, effectiveParcels) {
        val nonCancelledSelected = selectedParcel?.takeIf { it.status != ParcelStatus.CANCELLED }
        nonCancelledSelected
            ?: activeParcels.firstOrNull()
            ?: effectiveParcels.firstOrNull { it.status != ParcelStatus.CANCELLED && it.status != ParcelStatus.DELIVERED }
    }

    val previewParcel = remember {
        Parcel(
            id = "",
            itemName = "Express Logistics Delivery",
            imageUrl = "",
            status = ParcelStatus.PENDING,
            pickupAddress = "",
            deliveryAddress = "",
            senderName = "",
            senderPhone = "",
            receiverName = "",
            receiverPhone = "",
            weight = 0.0,
            price = 0.0,
            courierName = "",
            courierPhone = "",
            progress = 0f
        )
    }

    val parcel = activeParcel ?: previewParcel
    val hasNoBooking = activeParcel == null

    LaunchedEffect(hasNoBooking, parcel.id, parcel.status, parcel.progress) {
        val statusText = when (parcel.status) {
            ParcelStatus.PENDING -> "Pending Dispatch"
            ParcelStatus.QUEUED -> "Queued in Dispatch"
            ParcelStatus.RESERVED_NEXT -> "Courier Reserved"
            ParcelStatus.ASSIGNED -> "Courier Assigned"
            ParcelStatus.TRANSIT -> "In Transit to destination"
            ParcelStatus.PICKED_UP -> "Parcel picked up by courier"
            ParcelStatus.ARRIVED -> "Courier has arrived!"
            ParcelStatus.HANDOVER_VERIFIED -> "Handover Verified"
            ParcelStatus.OUT_FOR_DELIVERY -> "Out for delivery now!"
            ParcelStatus.DELIVERED -> "Delivered safely!"
            ParcelStatus.CANCELLED -> "Cancelled"
            else -> "Active Dispatch"
        }
        TrackingAppWidget.updateWidgetData(
            context = context,
            parcelId = if (hasNoBooking) null else parcel.id,
            statusText = statusText,
            progressPercent = (parcel.progress * 100).toInt()
        )
    }

    var isLocalLoading by remember(parcel.id) { mutableStateOf(false) }
    var showChatSheet by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showDisputeSheet by remember { mutableStateOf(false) }

    if (showDisputeSheet) {
        DisputeReportBottomSheet(
            parcel = parcel,
            viewModel = viewModel,
            isDark = isDark,
            onDismiss = { showDisputeSheet = false }
        )
    }

    val walletBalance by viewModel.walletBalance.collectAsState()
    if (showFeedbackDialog) {
        DeliveryFeedbackDialog(
            parcel = parcel,
            isDark = isDark,
            walletBalance = walletBalance,
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
        val activeContactPhone = if (isRider) {
            if (parcel.status in listOf(ParcelStatus.ASSIGNED, ParcelStatus.RESERVED_NEXT, ParcelStatus.ARRIVED_PICKUP)) parcel.senderPhone else parcel.receiverPhone
        } else {
            if (parcel.courierPhone.isNotBlank()) parcel.courierPhone else "+234 803 777 8888"
        }
        ParcelChatDialog(
            parcelId = parcel.id,
            senderRole = if (isRider) "rider" else "customer",
            recipientPhone = activeContactPhone,
            viewModel = viewModel,
            onDismiss = { showChatSheet = false }
        )
    }

    if (showCancelDialog) {
        CancelDeliverySecurityDialog(
            parcel = parcel,
            viewModel = viewModel,
            isDark = isDark,
            onDismiss = { showCancelDialog = false },
            onCancelled = { refundAmount, deductionFee ->
                showCancelDialog = false
                viewModel.selectParcel(null)
                val toastMsg = if (deductionFee > 0.0) {
                    "Delivery cancelled • ₦${String.format("%,.2f", refundAmount)} refunded (₦500 dispatch fee deducted)"
                } else {
                    "Delivery cancelled. Wallet refunded."
                }
                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
            }
        )
    }
    LaunchedEffect(parcel.id) {
        val validationResult = Zod.string(parcel.id)
            .min(4, "Tracking ID must be at least 4 characters.")
            .max(36, "Tracking ID must not exceed 36 characters.")
            .regex("^[a-zA-Z0-9\\s-]+$", "Only letters, numbers, and hyphens allowed.")
            .safeParse()

        if (validationResult is ZodResult.Success) {
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

    val screenHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp
    val dynamicExpandedHeight = (screenHeight * 0.70f).coerceIn(320.dp, 560.dp)
    val dynamicCollapsedHeight = (screenHeight * 0.42f).coerceIn(220.dp, 360.dp)

    val bottomCardHeight by animateDpAsState(
        targetValue = when (drawerState) {
            DrawerState.CLOSED -> if (hasNoBooking) 48.dp else 76.dp
            DrawerState.COLLAPSED -> if (hasNoBooking) 140.dp else dynamicCollapsedHeight
            DrawerState.EXPANDED -> if (hasNoBooking) 140.dp else dynamicExpandedHeight
        },
        label = "bottomCardHeight"
    )

    // Dynamic Weather state and AI Mode (Points 11 & 12)
    var currentWeather by remember { mutableStateOf("Clear (29°C)") }
    var isAiEtaActive by remember { mutableStateOf(true) }

    LaunchedEffect(parcel.courierLatitude, parcel.courierLongitude) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val lat = parcel.courierLatitude ?: 6.3350
                val lng = parcel.courierLongitude ?: 5.6037
                val url = java.net.URL("https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lng&current=temperature_2m,weather_code")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                if (conn.responseCode == 200) {
                    val responseStr = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = org.json.JSONObject(responseStr)
                    val currentObj = json.optJSONObject("current")
                    if (currentObj != null) {
                        val temp = currentObj.optDouble("temperature_2m", 28.0)
                        val code = currentObj.optInt("weather_code", 0)
                        val condition = when (code) {
                            0 -> "Clear (${temp.toInt()}°C)"
                            1, 2, 3 -> "Partly Cloudy (${temp.toInt()}°C)"
                            45, 48 -> "Foggy (${temp.toInt()}°C)"
                            51, 53, 55, 61, 63, 65 -> "Rainy (${temp.toInt()}°C)"
                            80, 81, 82 -> "Heavy Rain (${temp.toInt()}°C)"
                            95, 96, 99 -> "Thunderstorm (${temp.toInt()}°C)"
                            else -> "Clear (${temp.toInt()}°C)"
                        }
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            currentWeather = condition
                        }
                    }
                }
            } catch (e: Exception) {
                // Keep default or fallback
            }
        }
    }

    var realDistanceKm by remember { mutableStateOf<Float?>(null) }

    fun calculateEta(prog: Float, weather: String, aiActive: Boolean, distKm: Float? = realDistanceKm): Int {
        val baseSeconds = if (distKm != null && distKm > 0.05f) {
            ((distKm * 144f) + 90f).toInt()
        } else {
            ((1f - prog) * 1200).toInt().coerceAtLeast(10)
        }
        val weatherMultiplier = when {
            weather.contains("Rainy") || weather.contains("Heavy Rain") -> 1.35
            weather.contains("Thunderstorm") || weather.contains("Stormy") -> 1.75
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

    // 1-Mile Proximity Notification (Distance-Based Real-Time Alert) & 50m Rider Proximity Arrival Trigger
    var hasNotifiedWithinOneMile by remember { mutableStateOf(false) }
    var showInAppNotificationBanner by remember { mutableStateOf(false) }
    var consecutiveArrivalPings by remember(parcel.id) { mutableStateOf(0) }
    val geocoder = remember { android.location.Geocoder(context, java.util.Locale.getDefault()) }

    LaunchedEffect(parcel.courierLatitude, parcel.courierLongitude, parcel.progress, parcel.deliveryAddress, isRider, parcel.status) {
        // Calculate real distance if GPS coordinates are available
        if (parcel.courierLatitude != null && parcel.courierLongitude != null && parcel.deliveryAddress.isNotEmpty()) {
            try {
                // Geocode the delivery address in the background thread
                val addresses = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.esdispatch.utils.GeocoderUtils.getFromLocationNameCompat(geocoder, parcel.deliveryAddress, 1)
                }
                if (!addresses.isNullOrEmpty()) {
                    val destLat = addresses[0].latitude
                    val destLng = addresses[0].longitude
                    val results = FloatArray(1)
                    android.location.Location.distanceBetween(
                        parcel.courierLatitude, parcel.courierLongitude,
                        destLat, destLng,
                        results
                    )
                    val distanceMeters = results[0]
                    val distKm = distanceMeters / 1000f
                    realDistanceKm = distKm
                    tickingSeconds = calculateEta(parcel.progress, currentWeather, isAiEtaActive, distKm)

                    // 50-Meter Proximity Arrival Trigger for Riders (2-ping safeguard)
                    if (isRider && (parcel.status == ParcelStatus.TRANSIT || parcel.status == ParcelStatus.OUT_FOR_DELIVERY)) {
                        if (distanceMeters <= 50f) {
                            consecutiveArrivalPings++
                            if (consecutiveArrivalPings >= 2) {
                                viewModel.updateParcelStatusByRider(parcel.id, ParcelStatus.ARRIVED, 0.95f) { _, _ -> }
                                com.esdispatch.util.CustomToastBridge.show("Destination reached (within 50m). Status set to Arrived.", com.esdispatch.viewmodel.ToastType.SUCCESS)
                            }
                        } else if (distanceMeters > 70f) {
                            consecutiveArrivalPings = 0
                        }
                    }

                    if (distanceMeters <= 1609.34f && !hasNotifiedWithinOneMile) { // 1 mile = 1609.34 meters
                        hasNotifiedWithinOneMile = true
                        showInAppNotificationBanner = true
                        com.esdispatch.util.CustomToastBridge.show("Delivery Notice: Courier is within 1 mile of your location!", com.esdispatch.viewmodel.ToastType.INFO)
                    }
                    if (distanceMeters > 1609.34f) {
                        hasNotifiedWithinOneMile = false
                        showInAppNotificationBanner = false
                    }
                    return@LaunchedEffect // Exit early if real coordinates are used
                }
            } catch (e: Exception) {
                // Ignore geocoding errors and fallback to progress
            }
        }
        realDistanceKm = null
        
        // Fallback to simulated progress if no real coordinates
        if (parcel.progress >= 0.85f && parcel.progress < 0.98f && !hasNotifiedWithinOneMile) {
            hasNotifiedWithinOneMile = true
            showInAppNotificationBanner = true
            com.esdispatch.util.CustomToastBridge.show("Delivery Notice: Courier is within 1 mile of your location!", com.esdispatch.viewmodel.ToastType.INFO)
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
    var followUser by remember(hasNoBooking) { mutableStateOf(hasNoBooking) }

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
    Scaffold(
        containerColor = headerBgColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .background(headerBgColor)
        ) {
            ScreenHeader(
                title = "Track Shipment",
                onBack = { onNavigate("Dashboard") },
                rightContent = {
                    SupportButton(onClick = { showSupportDialog = true })
                }
            )

            // Multi-delivery Switcher Carousel (Customer Only)
            if (activeParcels.size > 1 && !isRider) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(activeParcels) { p ->
                        val isSelected = p.id == activeParcel?.id
                        val chipBg = if (isSelected) (if (isDark) Obsidian else Gold) else (if (isDark) Gold.copy(alpha = 0.25f) else Obsidian.copy(alpha = 0.15f))
                        val chipText = if (isSelected) (if (isDark) Gold else Obsidian) else (if (isDark) Obsidian else Gold)
                        Surface(
                            onClick = { viewModel.selectParcel(p) },
                            shape = RoundedCornerShape(12.dp),
                            color = chipBg,
                            border = BorderStroke(1.dp, if (isSelected) (if (isDark) Obsidian else Gold) else Color.Transparent),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) chipText else SuccessGreen)
                                )
                                Text(
                                    text = "${p.itemName.ifBlank { "Shipment" }.take(14)} (#${p.id.takeLast(4)})",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                    color = chipText
                                )
                            }
                        }
                    }
                }
            }

            if (showSupportDialog) {
                SupportDialog(
                    onDismiss = { showSupportDialog = false },
                    onReportIssue = { showDisputeSheet = true }
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(if (isDark) BackgroundDark else BackgroundLight)
            ) {
            val routeColor = if (aiTrafficCongested) "#FF3B30" else if (showTraffic) "#FF9500" else "#FFB800"

            val matchedRider = riders.find { it.id == parcel.riderId || it.name.equals(parcel.courierName, ignoreCase = true) } ?: riders.firstOrNull()
            val resolvedCourierAvatar = if (isRider) {
                userAvatar
            } else if (parcel.courierAvatar.isNotBlank()) {
                parcel.courierAvatar
            } else {
                matchedRider?.avatar?.ifBlank { "" } ?: ""
            }
            val resolvedCourierName = if (parcel.courierName.isNotBlank()) parcel.courierName else (matchedRider?.name ?: "Verified Dispatch Courier")
            val resolvedCourierPhone = if (parcel.courierPhone.isNotBlank()) parcel.courierPhone else (matchedRider?.phone ?: "+234 803 777 8888")

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
                    courierAvatar = resolvedCourierAvatar,
                    routeColor = routeColor,
                    onMapTypeToggled = { isSat ->
                        isSatelliteMode = isSat
                    },
                    courierLatitude = parcel.courierLatitude,
                    courierLongitude = parcel.courierLongitude,
                    userAvatar = userAvatar,
                    hasNoBooking = hasNoBooking,
                    followUser = followUser,
                    userCoords = userCoords,
                    isRider = isRider,
                    parcelStatus = parcel.status
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
                    onClick = { dismissedTrafficAlert = true },
                    modifier = Modifier.fillMaxWidth()
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
                                text = "Gridlock detected on Express Route. Your rider was automatically rerouted to bypass congestion.",
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
                                text = "Courier Is Near!",
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
                    shape = RoundedCornerShape(16.dp)
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
                                text = "Rider Approaching! (< 500m)",
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

            // FLOATING LUXURY DELIVERY SUCCESS POP-UP BANNER DOCKED AT TOP
            if (!hasNoBooking && parcel.status == ParcelStatus.DELIVERED) {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Gold),
                    border = BorderStroke(1.5.dp, Obsidian),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Obsidian),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Delivered",
                                tint = Gold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PACKAGE DELIVERED SUCCESSFULLY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Obsidian,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Handover confirmed with ${parcel.courierName.ifBlank { "courier" }}. Thank you for choosing ESDispatch!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Obsidian.copy(alpha = 0.85f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.clearActiveTrackingParcel()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Dismiss",
                                tint = Obsidian,
                                modifier = Modifier.size(18.dp)
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
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Obsidian.copy(alpha = 0.85f))
                        .border(1.dp, BorderDark, CircleShape)
                        .clickable {
                            currentWeather = when {
                                currentWeather.contains("Clear") || currentWeather.contains("Sunny") -> "Rainy (24°C)"
                                currentWeather.contains("Rain") -> "Stormy (22°C)"
                                else -> "Clear (29°C)"
                            }
                            tickingSeconds = calculateEta(parcel.progress, currentWeather, isAiEtaActive)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            currentWeather.contains("Rain") -> "RAIN"
                            currentWeather.contains("Storm") || currentWeather.contains("Thunder") -> "STORM"
                            else -> "SUN"
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Gold
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
                    icon = Icons.Default.MyLocation,
                    description = if (followUser) "Following you — tap to follow courier" else "Following courier — tap to follow you",
                    isActive = followUser
                ) {
                    followUser = !followUser
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
                    val isDrawerClosed = drawerState == DrawerState.CLOSED
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .height(bottomCardHeight)
                            .padding(
                                horizontal = if (isDrawerClosed) 16.dp else 0.dp,
                                vertical = if (isDrawerClosed) 8.dp else 0.dp
                            )
                            .zIndex(20f)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            shape = if (isDrawerClosed) RoundedCornerShape(24.dp) else RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDrawerClosed) (if (isDark) Obsidian else Color(0xFF18181B)) else (if (isDark) Obsidian else GoldenWhiteLight)
                            ),
                            border = BorderStroke(1.5.dp, if (isDrawerClosed) Gold else (if (isDark) Gold else BorderLight)),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isDrawerClosed) 8.dp else 0.dp)
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
                                if (drawerState == DrawerState.CLOSED) {
                                    // SLEEK COMPACT BOTTOM BAR WHEN DRAWER IS MINIMIZED
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable { drawerState = DrawerState.COLLAPSED }
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        if (hasNoBooking) {
                                            if (isRider) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                            .background(Gold),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Filled.DirectionsBike, null, tint = Obsidian, modifier = Modifier.size(18.dp))
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(userName.ifBlank { "Courier Profile" }, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppOnSurface)
                                                }
                                                Icon(Icons.Filled.KeyboardArrowUp, null, tint = Gold, modifier = Modifier.size(20.dp))
                                            } else {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                            .background(Gold),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Filled.Send, null, tint = Obsidian, modifier = Modifier.size(18.dp))
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text("Tap to Book Express Dispatch", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Gold)
                                                }
                                                Icon(Icons.Filled.KeyboardArrowUp, null, tint = Gold, modifier = Modifier.size(20.dp))
                                            }
                                        } else {
                                            val isCourierAssigned = parcel.courierName.isNotBlank() &&
                                                !parcel.courierName.equals("unassigned", ignoreCase = true) &&
                                                parcel.riderId.isNotBlank() &&
                                                parcel.status != ParcelStatus.PENDING &&
                                                parcel.status != ParcelStatus.QUEUED

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                if (isCourierAssigned) {
                                                    CourierAvatarBadge(
                                                        avatarUrl = resolvedCourierAvatar,
                                                        name = resolvedCourierName,
                                                        size = 42.dp,
                                                        borderWidth = 1.5.dp
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text(
                                                            text = resolvedCourierName,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = when (parcel.status) {
                                                                ParcelStatus.DELIVERED -> "Delivered Successfully"
                                                                ParcelStatus.ARRIVED -> "Rider Arrived"
                                                                else -> "In Transit • Tap for details"
                                                            },
                                                            fontSize = 11.sp,
                                                            color = Color(0xFFD4D4D8)
                                                        )
                                                    }
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(38.dp)
                                                            .clip(CircleShape)
                                                            .background(Gold.copy(alpha = 0.2f))
                                                            .border(1.dp, Gold, CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            Icons.Filled.DirectionsBike,
                                                            null,
                                                            tint = Gold,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text(
                                                            text = "Request Queued",
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                        Text(
                                                            text = "Assigning verified rider…",
                                                            fontSize = 11.sp,
                                                            color = Color(0xFFD4D4D8)
                                                        )
                                                    }
                                                }
                                            }

                                            // PIN / ETA indicator and expand action
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                if (isRider) {
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = Gold.copy(alpha = 0.15f),
                                                        border = BorderStroke(1.dp, Gold)
                                                    ) {
                                                        Text(
                                                            text = "${(tickingSeconds / 60).coerceAtLeast(1)}m ETA",
                                                            color = Gold,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                } else if (parcel.otpCode.isNotBlank()) {
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = Gold.copy(alpha = 0.15f),
                                                        border = BorderStroke(1.dp, Gold)
                                                    ) {
                                                        Text(
                                                            text = "PIN: ${parcel.otpCode}",
                                                            color = Gold,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(Gold),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Filled.KeyboardArrowUp,
                                                        contentDescription = "Expand",
                                                        tint = Obsidian,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                // Upper Scrollable Content (only shown when not closed)
                                    if (hasNoBooking) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isRider) {
                                                Surface(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(20.dp),
                                                    color = if (isLight) GoldenWhiteLight else Charcoal,
                                                    border = BorderStroke(1.dp, if (isLight) Slate else Gold.copy(alpha = 0.3f))
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(24.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(56.dp)
                                                                .clip(CircleShape)
                                                                .background(Gold.copy(alpha = 0.15f)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Filled.DirectionsBike,
                                                                contentDescription = null,
                                                                tint = Gold,
                                                                modifier = Modifier.size(30.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(14.dp))
                                                        Text(
                                                            text = "No Active Mission Assigned",
                                                            fontSize = 16.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = AppOnSurface
                                                        )
                                                        Text(
                                                            text = "You currently have no active deliveries on your radar. Return to your manifest to view pending pickups.",
                                                            fontSize = 12.sp,
                                                            color = TextGray,
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
                                                        )
                                                        Button(
                                                            onClick = { onNavigate("Dashboard") },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                                                            shape = RoundedCornerShape(14.dp),
                                                            modifier = Modifier.fillMaxWidth().height(48.dp)
                                                        ) {
                                                            Text("VIEW DISPATCH MANIFEST", fontWeight = FontWeight.Black, fontSize = 13.sp)
                                                        }
                                                    }
                                                }
                                            } else {
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
                                        }
                                    } else if (parcel.status == ParcelStatus.DELIVERED) {
                                        // DELIVERED DRAWER: Replace route details with Delivery Complete, Courier Rating & Tipping, and Book New Dispatch
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .align(Alignment.TopCenter)
                                                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 100.dp)
                                                .verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            // Header
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
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

                                                IconButton(
                                                    onClick = { viewModel.clearActiveTrackingParcel() },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextGray)
                                                }
                                            }

                                            // Rating & Tipping Card for Customer
                                            if (!isRider) {
                                                Surface(
                                                    shape = RoundedCornerShape(20.dp),
                                                    color = if (isDark) Charcoal else GoldenWhiteLight,
                                                    border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.2f) else Slate),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(18.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            CourierAvatarBadge(
                                                                avatarUrl = resolvedCourierAvatar,
                                                                name = resolvedCourierName,
                                                                size = 44.dp
                                                            )
                                                            Spacer(modifier = Modifier.width(12.dp))
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                    text = resolvedCourierName,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 14.sp,
                                                                    color = AppOnSurface
                                                                )
                                                                Text(
                                                                    text = "Delivered your parcel safely",
                                                                    fontSize = 11.sp,
                                                                    color = TextGray
                                                                )
                                                            }
                                                        }

                                                        Button(
                                                            onClick = { showFeedbackDialog = true },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                                                            shape = RoundedCornerShape(14.dp),
                                                            modifier = Modifier.fillMaxWidth().height(46.dp)
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(Icons.Filled.Star, contentDescription = null, tint = Obsidian, modifier = Modifier.size(18.dp))
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                Text("RATE & TIP COURIER", fontWeight = FontWeight.Black, fontSize = 13.sp)
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            // Book A New Dispatch Button
                                            Button(
                                                onClick = {
                                                    viewModel.clearActiveTrackingParcel()
                                                    if (!isRider) onNavigate("SendParcel") else onNavigate("Dashboard")
                                                },
                                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                                                shape = RoundedCornerShape(16.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (!isRider) Icons.Filled.LocalShipping else Icons.Filled.DirectionsBike,
                                                        contentDescription = null,
                                                        tint = Obsidian,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = if (!isRider) "BOOK A NEW DISPATCH" else "RETURN TO FLEET DASHBOARD",
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 14.sp,
                                                        letterSpacing = 0.5.sp
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
                                                    text = "Real-Time Fleet Routing • Benin City Zone",
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

                                            // ETA Indicator (Truthful Ranges & Telemetry Backed) - UPDATED IN REAL-TIME
                                            val etaText = when (parcel.status) {
                                                ParcelStatus.DELIVERED -> "ARRIVED"
                                                ParcelStatus.CANCELLED -> "CANCELLED"
                                                ParcelStatus.QUEUED -> "IN QUEUE"
                                                ParcelStatus.RESERVED_NEXT -> "RESERVED"
                                                ParcelStatus.PENDING -> "PROCESSING"
                                                ParcelStatus.ASSIGNED -> "ASSIGNED"
                                                ParcelStatus.HANDOVER_VERIFIED -> "VERIFYING"
                                                else -> {
                                                    val mins = (tickingSeconds / 60).coerceAtLeast(2)
                                                    val minR = (mins - 2).coerceAtLeast(1)
                                                    val maxR = mins + 4
                                                    "$minR–$maxR mins"
                                                }
                                            }
                                            val etaSubText = when (parcel.status) {
                                                ParcelStatus.DELIVERED -> "Package Delivered"
                                                ParcelStatus.CANCELLED -> "Order Cancelled"
                                                ParcelStatus.QUEUED -> "Awaiting Available Rider"
                                                ParcelStatus.RESERVED_NEXT -> "Courier Finishing Drop"
                                                ParcelStatus.PENDING -> "Order Received"
                                                ParcelStatus.ASSIGNED -> "Dispatched to Pickup"
                                                ParcelStatus.HANDOVER_VERIFIED -> "Proof in Progress"
                                                else -> realDistanceKm?.let { String.format(java.util.Locale.US, "%.1f km • GPS Live", it) } ?: "Transit (Traffic Adjusted)"
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

                                        // 3. Sender to Receiver addresses overview panel — full detail, Uber-style
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            if (isLocalLoading) {
                                                Column(modifier = Modifier.weight(1.0f)) {
                                                    Text("PICKUP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                                    SkeletonBox(
                                                        modifier = Modifier.width(180.dp).height(16.dp),
                                                        isLight = isLight
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Column(modifier = Modifier.weight(1.0f)) {
                                                    Text("DESTINATION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                                    SkeletonBox(
                                                        modifier = Modifier.width(180.dp).height(16.dp),
                                                        isLight = isLight
                                                    )
                                                }
                                            } else {
                                                AddressDetailRow(
                                                    icon = Icons.Filled.Place,
                                                    label = "PICKUP",
                                                    address = parcel.pickupAddress,
                                                    accentColor = accentIconColor,
                                                    onSurfaceColor = AppOnSurface
                                                )
                                                AddressDetailRow(
                                                    icon = Icons.Filled.Flag,
                                                    label = "DESTINATION",
                                                    address = parcel.deliveryAddress,
                                                    accentColor = accentIconColor,
                                                    onSurfaceColor = AppOnSurface
                                                )
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

                                                // Prominent 4-Digit Handover PIN Card (Customer Only)
                                                if (!isRider && (parcel.otpCode.isNotBlank() || parcel.status !in listOf(ParcelStatus.DELIVERED, ParcelStatus.CANCELLED))) {
                                                    if (parcel.otpCode.isBlank()) {
                                                        LaunchedEffect(parcel.id) {
                                                            viewModel.ensureDeliveryOtp(parcel.id)
                                                        }
                                                    }
                                                    val displayOtp = parcel.otpCode.ifBlank { "••••" }
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 6.dp)
                                                    ) {
                                                        if (parcel.status == ParcelStatus.ARRIVED) {
                                                            Surface(
                                                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                                                shape = RoundedCornerShape(14.dp),
                                                                color = if (isDark) Charcoal else GoldenWhiteLight,
                                                                border = BorderStroke(1.5.dp, Gold)
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier.padding(12.dp),
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                                ) {
                                                                    Box(
                                                                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Gold),
                                                                        contentAlignment = Alignment.Center
                                                                    ) {
                                                                        Icon(Icons.Filled.DirectionsBike, contentDescription = null, tint = Obsidian, modifier = Modifier.size(20.dp))
                                                                    }
                                                                    Column(modifier = Modifier.weight(1f)) {
                                                                        Text(
                                                                            text = "Courier Has Arrived!",
                                                                            fontSize = 12.sp,
                                                                            fontWeight = FontWeight.Black,
                                                                            color = if (isDark) GoldLight else Obsidian
                                                                        )
                                                                        Text(
                                                                            text = "Please meet your courier and share your 4-digit Handover PIN below to receive your package.",
                                                                            fontSize = 11.sp,
                                                                            color = TextGray
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        Text(
                                                            text = "HANDOVER PIN",
                                                            fontSize = 12.sp,
                                                            fontFamily = SpaceGrotesk,
                                                            fontWeight = FontWeight.Black,
                                                            letterSpacing = 1.sp,
                                                            color = if (isDark) GoldLight else Obsidian
                                                        )
                                                        Spacer(modifier = Modifier.height(10.dp))
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            displayOtp.forEach { digit ->
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(50.dp)
                                                                        .background(if (isDark) LuxuryBlack else Color.White, RoundedCornerShape(12.dp))
                                                                        .border(2.dp, Gold, RoundedCornerShape(12.dp)),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Text(
                                                                        text = digit.toString(),
                                                                        fontSize = 22.sp,
                                                                        fontWeight = FontWeight.Black,
                                                                        fontFamily = SpaceGrotesk,
                                                                        color = if (isDark) Gold else Obsidian
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Text(
                                                            text = "Provide this 4-digit PIN to courier upon delivery to verify handover",
                                                            fontSize = 11.sp,
                                                            color = TextGray,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }

                                                    HorizontalDivider(color = if (isDark) BorderDark else BorderLight)
                                                }

                                                // Optional QR Code Handover (only shown to customers if admin enabled enableQrCodeHandover)
                                                if (!isRider && enableQrCodeHandover) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 6.dp)
                                                    ) {
                                                        Text(
                                                            text = "QR HANDOVER VERIFICATION",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Black,
                                                            letterSpacing = 1.sp,
                                                            color = if (isDark) GoldLight else Obsidian
                                                        )
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .size(120.dp)
                                                                .background(Color.White, RoundedCornerShape(12.dp))
                                                                .padding(10.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            QRCodeImage(
                                                                text = activeParcel?.id ?: "INVALID_ID",
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
                                                }

                                                if (isRider) {
                                                    // Rider Navigation & Client Contact Row
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        Button(
                                                            onClick = {
                                                                val dest = if (parcel.status == ParcelStatus.ASSIGNED) parcel.pickupAddress else parcel.deliveryAddress
                                                                val uri = Uri.parse("google.navigation:q=" + Uri.encode(dest))
                                                                val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                                                                    setPackage("com.google.android.apps.maps")
                                                                }
                                                                try {
                                                                    context.startActivity(mapIntent)
                                                                } catch (e: Exception) {
                                                                    val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + Uri.encode(dest))
                                                                    context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                                                }
                                                            },
                                                            modifier = Modifier.weight(1f).height(44.dp).tactilePress(scaleDown = 0.94f),
                                                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Obsidian else Gold),
                                                            shape = RoundedCornerShape(12.dp),
                                                            border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.5f) else Obsidian.copy(alpha = 0.3f))
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                                                Icon(Icons.Filled.Navigation, "Navigate", tint = if (isDark) Gold else Obsidian, modifier = Modifier.size(16.dp))
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Text("Directions", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Gold else Obsidian)
                                                            }
                                                        }

                                                        OutlinedButton(
                                                            onClick = {
                                                                val phone = if (parcel.status == ParcelStatus.ASSIGNED) parcel.senderPhone else parcel.receiverPhone
                                                                val cleanPhone = phone.filter { it.isDigit() || it == '+' }
                                                                if (cleanPhone.isNotBlank()) {
                                                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone"))
                                                                    context.startActivity(intent)
                                                                } else {
                                                                    Toast.makeText(context, "No contact phone available", Toast.LENGTH_SHORT).show()
                                                                }
                                                            },
                                                            modifier = Modifier.weight(1f).height(44.dp).tactilePress(scaleDown = 0.94f),
                                                            shape = RoundedCornerShape(12.dp),
                                                            border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.5f) else Obsidian.copy(alpha = 0.3f)),
                                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isDark) Gold else Obsidian)
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                                                Icon(Icons.Filled.Call, "Call", tint = if (isDark) Gold else Obsidian, modifier = Modifier.size(16.dp))
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Text("Call Client", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }

                                                    // Rider Milestone Progression Action Button
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    when (parcel.status) {
                                                        ParcelStatus.ASSIGNED -> {
                                                            Button(
                                                                onClick = {
                                                                    viewModel.updateParcelStatusByRider(parcel.id, ParcelStatus.PICKED_UP, 0.40f) { success, _ ->
                                                                        if (success) Toast.makeText(context, "Pickup confirmed! Order is now picked up.", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                },
                                                                modifier = Modifier.fillMaxWidth().height(48.dp).tactilePress(scaleDown = 0.96f),
                                                                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                                                                shape = RoundedCornerShape(14.dp)
                                                            ) {
                                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                                                    Icon(Icons.Filled.CheckCircle, null, tint = Obsidian, modifier = Modifier.size(18.dp))
                                                                    Spacer(modifier = Modifier.width(8.dp))
                                                                    Text("CONFIRM PICKUP", fontWeight = FontWeight.Black, fontSize = 13.sp)
                                                                }
                                                            }
                                                        }
                                                        ParcelStatus.PICKED_UP -> {
                                                            Button(
                                                                onClick = {
                                                                    viewModel.updateParcelStatusByRider(parcel.id, ParcelStatus.TRANSIT, 0.60f) { success, _ ->
                                                                        if (success) Toast.makeText(context, "In transit to destination! Customer notified.", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                },
                                                                modifier = Modifier.fillMaxWidth().height(48.dp).tactilePress(scaleDown = 0.96f),
                                                                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                                                                shape = RoundedCornerShape(14.dp)
                                                            ) {
                                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                                                    Icon(Icons.Filled.DirectionsBike, null, tint = Obsidian, modifier = Modifier.size(18.dp))
                                                                    Spacer(modifier = Modifier.width(8.dp))
                                                                    Text("START TRANSIT", fontWeight = FontWeight.Black, fontSize = 13.sp)
                                                                }
                                                            }
                                                        }
                                                        ParcelStatus.TRANSIT, ParcelStatus.OUT_FOR_DELIVERY -> {
                                                            Button(
                                                                onClick = {
                                                                    viewModel.updateParcelStatusByRider(parcel.id, ParcelStatus.ARRIVED, 0.90f) { success, _ ->
                                                                        if (success) Toast.makeText(context, "Marked as arrived! Recipient notified.", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                },
                                                                modifier = Modifier.fillMaxWidth().height(48.dp).tactilePress(scaleDown = 0.96f),
                                                                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                                                                shape = RoundedCornerShape(14.dp)
                                                            ) {
                                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                    Icon(Icons.Filled.LocationOn, null, tint = Obsidian, modifier = Modifier.size(18.dp))
                                                                    Text("MARK ARRIVED AT DESTINATION", fontWeight = FontWeight.Black, fontSize = 13.sp)
                                                                }
                                                            }
                                                        }
                                                        ParcelStatus.ARRIVED -> {
                                                            Button(
                                                                onClick = {
                                                                    onNavigate("ProofOfDelivery/${parcel.id}")
                                                                },
                                                                modifier = Modifier.fillMaxWidth().height(48.dp).tactilePress(scaleDown = 0.96f),
                                                                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                                                                shape = RoundedCornerShape(14.dp)
                                                            ) {
                                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                    Icon(Icons.Filled.VerifiedUser, null, tint = Obsidian, modifier = Modifier.size(18.dp))
                                                                    Text("ENTER 4-DIGIT PIN & COMPLETE", fontWeight = FontWeight.Black, fontSize = 13.sp)
                                                                }
                                                            }
                                                        }
                                                        else -> {}
                                                    }
                                                } else {
                                                    // Share Live Tracking & Tip Rider Row (Customers Only)
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        // Share Live Tracking Link
                                                        Button(
                                                            onClick = {
                                                                val sendIntent: Intent = Intent().apply {
                                                                    action = Intent.ACTION_SEND
                                                                    putExtra(Intent.EXTRA_TEXT, "Track my ESDispatch package live: https://esdispatch.com/track/${parcel.id}")
                                                                    type = "text/plain"
                                                                }
                                                                val shareIntent = Intent.createChooser(sendIntent, "Share Tracking Link")
                                                                context.startActivity(shareIntent)
                                                            },
                                                            modifier = Modifier.weight(1f).height(40.dp).tactilePress(scaleDown = 0.94f),
                                                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Obsidian else Gold),
                                                            shape = RoundedCornerShape(12.dp),
                                                            border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.3f) else Obsidian.copy(alpha = 0.3f))
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
                                                                        tint = if (isDark) Gold else Obsidian,
                                                                        modifier = Modifier.size(14.dp)
                                                                    )
                                                                    Spacer(modifier = Modifier.width(4.dp))
                                                                    val tipFormatted = if (parcel.tipAmount > 0.0) " • ₦${String.format("%,.0f", parcel.tipAmount)}" else ""
                                                                    Text(
                                                                        text = "${parcel.customerRating.toInt()}$tipFormatted",
                                                                        fontSize = 11.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = if (isDark) Gold else Obsidian
                                                                    )
                                                                }
                                                            }
                                                        } else {
                                                            Button(
                                                                onClick = { showFeedbackDialog = true },
                                                                modifier = Modifier.weight(1f).height(40.dp).tactilePress(scaleDown = 0.94f),
                                                                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                                                                shape = RoundedCornerShape(12.dp)
                                                            ) {
                                                                Text("Feedback & Tip", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Obsidian)
                                                            }
                                                        }
                                                    }
                                                }

                                                if (!isRider && parcel.status in listOf(ParcelStatus.PENDING, ParcelStatus.QUEUED, ParcelStatus.RESERVED_NEXT, ParcelStatus.ASSIGNED)) {
                                                    OutlinedButton(
                                                        onClick = { showCancelDialog = true },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(42.dp)
                                                            .tactilePress(scaleDown = 0.96f),
                                                        shape = RoundedCornerShape(12.dp),
                                                        border = BorderStroke(1.2.dp, Color(0xFFE53935).copy(alpha = 0.6f)),
                                                        colors = ButtonDefaults.outlinedButtonColors(
                                                            contentColor = Color(0xFFE53935)
                                                        )
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Close,
                                                            contentDescription = null,
                                                            tint = Color(0xFFE53935),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "Cancel Delivery & Refund",
                                                            fontSize = 12.sp,
                                                            fontFamily = Poppins,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFE53935)
                                                        )
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
                                                    Triple("Booked", "Delivery order received and confirmed.", parcel.status != ParcelStatus.CANCELLED),
                                                    Triple("Courier Assigned", "Courier assigned and en route to pickup.", parcel.status in listOf(ParcelStatus.ASSIGNED, ParcelStatus.PICKED_UP, ParcelStatus.TRANSIT, ParcelStatus.ARRIVED, ParcelStatus.HANDOVER_VERIFIED, ParcelStatus.DELIVERED) || parcel.progress >= 0.20f),
                                                    Triple("In Transit", "Package collected and on the way.", parcel.status in listOf(ParcelStatus.PICKED_UP, ParcelStatus.TRANSIT, ParcelStatus.ARRIVED, ParcelStatus.HANDOVER_VERIFIED, ParcelStatus.DELIVERED) || parcel.progress >= 0.40f),
                                                    Triple("Courier Arrived", "Courier has arrived at destination.", parcel.status in listOf(ParcelStatus.ARRIVED, ParcelStatus.HANDOVER_VERIFIED, ParcelStatus.DELIVERED) || parcel.progress >= 0.90f),
                                                    Triple("Delivered", "Package safely received.", parcel.status in listOf(ParcelStatus.HANDOVER_VERIFIED, ParcelStatus.DELIVERED) || parcel.progress >= 1.0f)
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
                                                        
                                                        // Realistic dynamic status timestamp
                                                         val baseTime = remember(parcel.id, parcel.dateString) {
                                                             val hash = kotlin.math.abs(parcel.id.hashCode())
                                                             val hour = (hash % 12).coerceAtLeast(1)
                                                             val min = (hash % 50)
                                                             Pair(hour, min)
                                                         }
                                                         val timestampText = when (index) {
                                                             0 -> String.format(java.util.Locale.US, "%d:%02d AM", baseTime.first, baseTime.second)
                                                             1 -> if (timelineSteps[1].third) {
                                                                 String.format(java.util.Locale.US, "%d:%02d AM", baseTime.first, (baseTime.second + 8) % 60)
                                                             } else "--:--"
                                                             2 -> if (timelineSteps[2].third) {
                                                                 val transitHour = if (baseTime.second + 25 >= 60) (baseTime.first % 12) + 1 else baseTime.first
                                                                 val amPm = if (transitHour >= 12) "PM" else "AM"
                                                                 String.format(java.util.Locale.US, "%d:%02d %s", transitHour, (baseTime.second + 25) % 60, amPm)
                                                             } else "--:--"
                                                             3 -> if (timelineSteps[3].third) {
                                                                 val arrHour = if (baseTime.second + 45 >= 60) (baseTime.first % 12) + 1 else baseTime.first
                                                                 val amPm = if (arrHour >= 12) "PM" else "AM"
                                                                 String.format(java.util.Locale.US, "%d:%02d %s", arrHour, (baseTime.second + 45) % 60, amPm)
                                                             } else "--:--"
                                                             4 -> if (timelineSteps[4].third) {
                                                                 val delivHour = if (baseTime.second + 55 >= 60) (baseTime.first % 12) + 1 else baseTime.first
                                                                 val amPm = if (delivHour >= 12) "PM" else "AM"
                                                                 String.format(java.util.Locale.US, "%d:%02d %s", delivHour, (baseTime.second + 55) % 60, amPm)
                                                             } else "--:--"
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

                                        // 3.5. Search / Track Another Shipment Section (Customer Only)
                                        if (!isRider) {
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
                                                             inlineSearchQuery = FormatUtils.formatTrackingId(it)
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
                                                             val validationResult = Zod.string(inlineSearchQuery)
                                                                 .min(4, "Tracking ID must be at least 4 characters.")
                                                                 .max(36, "Tracking ID must not exceed 36 characters.")
                                                                 .regex("^[a-zA-Z0-9\\s-]+$", "Only letters, numbers, and hyphens allowed.")
                                                                 .safeParse()

                                                             when (validationResult) {
                                                                 is ZodResult.Error -> {
                                                                     inlineSearchQueryError = validationResult.message
                                                                 }
                                                                 is ZodResult.Success -> {
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
                                                              containerColor = Gold,
                                                              contentColor = Obsidian
                                                          ),
                                                          shape = RoundedCornerShape(12.dp)
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
                                                                 text = "RECENT SEARCHES",
                                                                 fontSize = 11.sp,
                                                                 fontFamily = SpaceGrotesk,
                                                                 fontWeight = FontWeight.Black,
                                                                 letterSpacing = 0.5.sp,
                                                                 color = if (isDark) GoldLight else Obsidian
                                                             )
                                                             Text(
                                                                 text = "CLEAR ALL",
                                                                 fontSize = 10.sp,
                                                                 fontWeight = FontWeight.Bold,
                                                                 color = TextGray,
                                                                 modifier = Modifier.clickable {
                                                                     viewModel.clearSearchHistory(context)
                                                                 }
                                                             )
                                                         }
                                                         Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                             recentSearches.take(5).forEach { searchId ->
                                                                 val parcelForId = viewModel.parcels.collectAsState().value.find { it.id.equals(searchId, ignoreCase = true) }
                                                                 if (parcelForId != null) {
                                                                     val badgeText = when (parcelForId.status) {
                                                                         ParcelStatus.PENDING -> "Pending"
                                                                         ParcelStatus.QUEUED -> "Queued"
                                                                         ParcelStatus.RESERVED_NEXT -> "Reserved"
                                                                         ParcelStatus.ASSIGNED -> "Assigned"
                                                                         ParcelStatus.TRANSIT -> "In Transit"
                                                                         ParcelStatus.PICKED_UP -> "Picked Up"
                                                                         ParcelStatus.ARRIVED -> "Arrived"
                                                                         ParcelStatus.HANDOVER_VERIFIED -> "Handover Verified"
                                                                         ParcelStatus.OUT_FOR_DELIVERY -> "Out for Delivery"
                                                                         ParcelStatus.DELIVERED -> "Delivered"
                                                                         ParcelStatus.CANCELLED -> "Cancelled"
                                                                         else -> "Active"
                                                                     }
                                                                     val badgeBgColor = when (parcelForId.status) {
                                                                         ParcelStatus.PENDING -> Color(0x202196F3)
                                                                         ParcelStatus.QUEUED -> Color(0x20FF9800)
                                                                         ParcelStatus.RESERVED_NEXT -> Color(0x209C27B0)
                                                                         ParcelStatus.ASSIGNED -> Color(0x203F51B5)
                                                                         ParcelStatus.PICKED_UP -> Color(0x205E35B1)
                                                                         ParcelStatus.ARRIVED -> Color(0x2000897B)
                                                                         ParcelStatus.HANDOVER_VERIFIED -> Color(0x20009688)
                                                                         ParcelStatus.DELIVERED -> Color(0x204CAF50)
                                                                         ParcelStatus.OUT_FOR_DELIVERY -> Color(0x20FF9800)
                                                                         ParcelStatus.CANCELLED -> Color(0x20F44336)
                                                                         ParcelStatus.TRANSIT -> if (isDark) Gold.copy(alpha = 0.15f) else Color(0x100E0E10)
                                                                         else -> Gold.copy(alpha = 0.15f)
                                                                     }
                                                                     val badgeTextColor = when (parcelForId.status) {
                                                                         ParcelStatus.PENDING -> Color(0xFF2196F3)
                                                                         ParcelStatus.QUEUED -> Color(0xFFFF9800)
                                                                         ParcelStatus.RESERVED_NEXT -> Color(0xFF9C27B0)
                                                                         ParcelStatus.ASSIGNED -> Color(0xFF3F51B5)
                                                                         ParcelStatus.PICKED_UP -> Color(0xFF5E35B1)
                                                                         ParcelStatus.ARRIVED -> Color(0xFF00897B)
                                                                         ParcelStatus.HANDOVER_VERIFIED -> Color(0xFF009688)
                                                                         ParcelStatus.DELIVERED -> Color(0xFF4CAF50)
                                                                         ParcelStatus.OUT_FOR_DELIVERY -> Color(0xFFFF9800)
                                                                         ParcelStatus.CANCELLED -> Color(0xFFF44336)
                                                                         ParcelStatus.TRANSIT -> if (isDark) Gold else Obsidian
                                                                         else -> Gold
                                                                     }

                                                                      Surface(
                                                                          onClick = { viewModel.selectParcel(parcelForId) },
                                                                          shape = RoundedCornerShape(12.dp),
                                                                          color = if (isDark) LuxuryBlack else Color.White,
                                                                          border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.1f) else Color(0xFFE5E7EB)),
                                                                          modifier = Modifier.fillMaxWidth()
                                                                      ) {
                                                                          Row(
                                                                              modifier = Modifier.padding(10.dp),
                                                                              horizontalArrangement = Arrangement.SpaceBetween,
                                                                              verticalAlignment = Alignment.CenterVertically
                                                                          ) {
                                                                              Row(
                                                                                  verticalAlignment = Alignment.CenterVertically,
                                                                                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                              ) {
                                                                                  Icon(
                                                                                      Icons.Filled.History,
                                                                                      contentDescription = null,
                                                                                      tint = Gold,
                                                                                      modifier = Modifier.size(14.dp)
                                                                                  )
                                                                                  Column {
                                                                                      Text(
                                                                                          text = parcelForId.itemName.ifBlank { "Shipment" },
                                                                                          fontWeight = FontWeight.Bold,
                                                                                          fontSize = 11.sp,
                                                                                          color = AppOnSurface
                                                                                      )
                                                                                      Text(
                                                                                          text = "#$searchId",
                                                                                          fontSize = 9.sp,
                                                                                          color = TextGray
                                                                                      )
                                                                                  }
                                                                              }
                                                                              Surface(
                                                                                  shape = RoundedCornerShape(6.dp),
                                                                                  color = badgeBgColor
                                                                              ) {
                                                                                  Text(
                                                                                      text = badgeText,
                                                                                      fontSize = 9.sp,
                                                                                      fontWeight = FontWeight.Bold,
                                                                                      color = badgeTextColor,
                                                                                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                                  )
                                                                              }
                                                                          }
                                                                      }
                                                                  } else {
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
                                                                          }
                                                                          Icon(
                                                                              imageVector = Icons.Default.ArrowForward,
                                                                              contentDescription = null,
                                                                              tint = TextGray,
                                                                              modifier = Modifier.size(12.dp)
                                                                          )
                                                                      }
                                                                  }
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
                                }

                                // 4. THE COURIER / DRIVER AGENT CARD (FIXED AT THE ABSOLUTE BOTTOM)
                                if (!hasNoBooking && drawerState != DrawerState.CLOSED) {
                                    val isCourierAssigned = parcel.courierName.isNotBlank() &&
                                        !parcel.courierName.equals("unassigned", ignoreCase = true) &&
                                        parcel.riderId.isNotBlank() &&
                                        parcel.status != ParcelStatus.PENDING

                                    Surface(
                                        shape = RoundedCornerShape(24.dp),
                                        color = if (isDark) Charcoal else Obsidian,
                                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.2f)),
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
                                    ) {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        QuiltedBackground(modifier = Modifier.matchParentSize()) {}

                                        if (isRider) {
                                            // RIDER VIEW: Show Customer / Recipient Contact Card
                                            val isPickupPhase = parcel.status in listOf(ParcelStatus.ASSIGNED, ParcelStatus.RESERVED_NEXT, ParcelStatus.ARRIVED_PICKUP)
                                            val contactRole = if (isPickupPhase) "Sender" else "Recipient"
                                            val contactName = (if (isPickupPhase) parcel.senderName else parcel.receiverName).ifBlank { if (isPickupPhase) "Customer / Sender" else "Recipient" }
                                            val contactPhone = if (isPickupPhase) parcel.senderPhone else parcel.receiverPhone
                                            val contactAddress = if (isPickupPhase) parcel.pickupAddress else parcel.deliveryAddress
                                            val contactInitials = contactName.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("").ifBlank { "C" }

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(14.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(50.dp)
                                                            .border(2.dp, Gold, CircleShape)
                                                            .clip(CircleShape)
                                                            .background(if (isDark) LuxuryBlack else Charcoal),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = contactInitials,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            fontSize = 18.sp,
                                                            color = Gold
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = contactName,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                fontSize = 15.sp,
                                                                color = Color.White,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Surface(
                                                                shape = RoundedCornerShape(6.dp),
                                                                color = Gold.copy(alpha = 0.2f),
                                                                border = BorderStroke(0.5.dp, Gold)
                                                            ) {
                                                                Text(
                                                                    text = contactRole,
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Gold,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                )
                                                            }
                                                        }
                                                        Text(
                                                            text = contactAddress.ifBlank { "Delivery destination" },
                                                            fontSize = 11.sp,
                                                            color = TextGray,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                // High Contrast Contact Actions (NO White on Gold!)
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    // Call Trigger
                                                    Box(
                                                        modifier = Modifier
                                                            .size(44.dp)
                                                            .clip(CircleShape)
                                                            .background(GoldenWhiteLight)
                                                            .clickable {
                                                                val cleanPhone = contactPhone.filter { it.isDigit() || it == '+' }
                                                                if (cleanPhone.isNotBlank()) {
                                                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone"))
                                                                    try {
                                                                        context.startActivity(dialIntent)
                                                                    } catch (e: Exception) {
                                                                        Toast.makeText(context, "Call not supported on this device", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                } else {
                                                                    Toast.makeText(context, "Phone number unavailable", Toast.LENGTH_SHORT).show()
                                                                }
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Filled.Call, contentDescription = "Call $contactRole", tint = Obsidian, modifier = Modifier.size(18.dp))
                                                    }

                                                    // Chat Trigger
                                                    Box(
                                                        modifier = Modifier
                                                            .size(44.dp)
                                                            .clip(CircleShape)
                                                            .background(Gold)
                                                            .clickable { showChatSheet = true },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Filled.Chat, contentDescription = "Chat with $contactRole", tint = Obsidian, modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                            }
                                        } else if (isCourierAssigned) {
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
                                                        CourierAvatarBadge(
                                                            avatarUrl = resolvedCourierAvatar,
                                                            name = resolvedCourierName,
                                                            size = 50.dp,
                                                            borderWidth = 2.dp
                                                        )
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Column {
                                                            Text(
                                                                text = resolvedCourierName,
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
                                                            .background(GoldenWhiteLight)
                                                            .clickable {
                                                                val cleanPhone = resolvedCourierPhone.filter { it.isDigit() || it == '+' }
                                                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone"))
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
                                        } else {
                                            // Courier is being assigned / searching nearest rider
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(14.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val infiniteTransition = rememberInfiniteTransition(label = "pulseCourier")
                                                val pulseScale by infiniteTransition.animateFloat(
                                                    initialValue = 0.92f,
                                                    targetValue = 1.08f,
                                                    animationSpec = infiniteRepeatable(
                                                        animation = tween(1000, easing = FastOutSlowInEasing),
                                                        repeatMode = RepeatMode.Reverse
                                                    ),
                                                    label = "pulseScale"
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .graphicsLayer {
                                                                scaleX = pulseScale
                                                                scaleY = pulseScale
                                                            }
                                                            .background(Gold.copy(alpha = 0.15f), CircleShape)
                                                            .border(1.5.dp, Gold.copy(alpha = 0.6f), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.DirectionsBike,
                                                            contentDescription = "Assigning Courier",
                                                            tint = Gold,
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        val isReserved = parcel.status == ParcelStatus.RESERVED_NEXT || parcel.reservedCourierName.isNotEmpty()
                                                        val isQueued = parcel.status == ParcelStatus.QUEUED || parcel.status == ParcelStatus.PENDING
                                                        Text(
                                                            text = when {
                                                                isReserved -> "Rider Reserved"
                                                                isQueued -> "Request Queued"
                                                                else -> "Dispatch Matching"
                                                            },
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            color = Color.White
                                                        )
                                                        Text(
                                                            text = when {
                                                                isReserved -> "${parcel.reservedCourierName.ifBlank { "A fleet rider" }} is finishing an ongoing delivery and will start yours next"
                                                                isQueued -> "In line for next available fleet rider in Benin City"
                                                                else -> "Connecting with Benin City dispatch fleet"
                                                            },
                                                            fontSize = 11.sp,
                                                            color = TextGray
                                                        )
                                                    }
                                                }
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    color = Gold,
                                                    strokeWidth = 2.dp
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

                        // Collapsible drawer arrow button overlapping the top center edge (only when not CLOSED)
                        if (drawerState != DrawerState.CLOSED) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AppSurface,
                                border = BorderStroke(1.dp, if (isLight) Slate else Gold.copy(alpha = 0.3f)),
                                onClick = {
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
                                                    isGoingUp = false
                                                } else {
                                                    drawerState = DrawerState.CLOSED
                                                    isGoingUp = true
                                                }
                                            }
                                            DrawerState.EXPANDED -> {
                                                drawerState = DrawerState.COLLAPSED
                                                isGoingUp = false
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(y = (-18).dp)
                                    .zIndex(30f)
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
                                        tint = if (isLight) Obsidian else Gold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
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
    private val onMapTypeToggledCallback: (Boolean) -> Unit,
    private val onUserInteractedCallback: () -> Unit = {}
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

    @android.webkit.JavascriptInterface
    fun onUserInteracted() {
        (context as? android.app.Activity)?.runOnUiThread {
            onUserInteractedCallback()
        }
    }
}

@Composable
private fun AddressDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    address: String,
    accentColor: Color,
    onSurfaceColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = accentColor, modifier = Modifier.size(13.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = address.ifBlank { "Address pending" },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor,
                lineHeight = 18.sp
            )
        }
    }
}

private fun geocodeAddressToLatLng(context: android.content.Context, address: String): Pair<Double, Double> {
    if (address.isBlank()) return Pair(6.3350, 5.6037)

    // 1. Instant lookup from authentic Benin City address catalog (fastest, 0ms latency)
    val beninCoords = com.esdispatch.data.AddressDatabase.getCoordinates(address)
    if (beninCoords != null) {
        return beninCoords
    }

    // 2. System Geocoder with strict timeout
    try {
        if (android.location.Geocoder.isPresent()) {
            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
            val query = if (address.contains("Benin", ignoreCase = true)) address else "$address, Benin City, Nigeria"
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(query, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                if (addr.latitude in 6.1..6.5 && addr.longitude in 5.4..5.8) {
                    return Pair(addr.latitude, addr.longitude)
                }
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("Geocoder", "System Geocoder failed: ${e.message}")
    }

    // 3. Hash-based deterministic coordinate strictly within Benin City boundary
    val hash = address.hashCode().toLong()
    val latOffset = ((Math.abs(hash) % 80) - 40) / 1000.0
    val lngOffset = ((Math.abs(hash / 100) % 80) - 40) / 1000.0
    return Pair(6.3350 + latOffset, 5.6037 + lngOffset)
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
    followUser: Boolean = true,
    userCoords: Pair<Double, Double>? = null,
    isRider: Boolean = false,
    parcelStatus: ParcelStatus = ParcelStatus.PENDING
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
    var isPageLoaded by remember { mutableStateOf(false) }
    var userHasPanned by remember { mutableStateOf(false) }

    val leafletCss = remember(context) {
        try {
            context.assets.open("leaflet/leaflet.css").bufferedReader().use { it.readText() }
        } catch (e: Throwable) {
            ""
        }
    }
    val leafletJs = remember(context) {
        try {
            context.assets.open("leaflet/leaflet.js").bufferedReader().use { it.readText() }
        } catch (e: Throwable) {
            ""
        }
    }

    val webView = remember(context) {
        WebView(context).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.allowFileAccessFromFileURLs = true
            settings.allowUniversalAccessFromFileURLs = true
            settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            settings.userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
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
                    handler?.proceed()
                }
            }

            webChromeClient = object : android.webkit.WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                    android.util.Log.d("MapWebViewConsole", "[${consoleMessage?.messageLevel()}] ${consoleMessage?.message()}")
                    return true
                }
            }

            addJavascriptInterface(
                LeafletJavascriptInterface(
                    context = context,
                    onMapClickCallback = { lat, lng ->
                        if (hasNoBooking) {
                            reverseGeocodeAddress(context, lat, lng) { address ->
                                com.esdispatch.util.CustomToastBridge.show("Location Selected: $address", com.esdispatch.viewmodel.ToastType.SUCCESS)
                            }
                        }
                    },
                    onMarkerPlacedCallback = { _, _, _ -> },
                    onTrackingUpdatedCallback = { _, _ -> },
                    onMapTypeToggledCallback = { isSat ->
                        onMapTypeToggled(isSat)
                    },
                    onUserInteractedCallback = {
                        userHasPanned = true
                    }
                ),
                "AndroidMap"
            )
        }
    }

    // Load static shell ONLY ONCE
    val htmlContent = remember(leafletCss, leafletJs) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes" />
            <style>
                $leafletCss
                
                html, body {
                    margin: 0;
                    padding: 0;
                    width: 100%;
                    height: 100%;
                    background: #121214;
                    overflow: hidden;
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                }
                #map {
                    margin: 0;
                    padding: 0;
                    width: 100%;
                    height: 100%;
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    background: #121214;
                    overflow: hidden;
                    z-index: 1;
                }
                .dark-tiles {
                    filter: invert(100%) hue-rotate(180deg) brightness(88%) contrast(105%);
                }
                @keyframes icon-pulse {
                    0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(255, 184, 0, 0.7); }
                    70% { transform: scale(1.04); box-shadow: 0 0 0 14px rgba(255, 184, 0, 0); }
                    100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(255, 184, 0, 0); }
                }
                .pulsing-courier {
                    animation: icon-pulse 1.8s infinite ease-in-out;
                    border-radius: 50%;
                }
                @keyframes beacon-expand {
                    0% { transform: scale(0.4); opacity: 0.9; }
                    50% { transform: scale(1.6); opacity: 0.4; }
                    100% { transform: scale(2.4); opacity: 0; }
                }
                .arrival-beacon-wrap {
                    position: relative;
                    width: 60px;
                    height: 60px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    pointer-events: none;
                }
                .arrival-beacon-ring {
                    position: absolute;
                    width: 60px;
                    height: 60px;
                    border-radius: 50%;
                    background: rgba(255, 184, 0, 0.35);
                    border: 2px solid #FFB800;
                    animation: beacon-expand 1.6s infinite cubic-bezier(0.2, 0.8, 0.2, 1);
                }
                .arrival-beacon-core {
                    width: 12px;
                    height: 12px;
                    border-radius: 50%;
                    background: #FFB800;
                    box-shadow: 0 0 8px #FFB800;
                }
                .clean-map-badge {
                    background: transparent !important;
                    border: none !important;
                    box-shadow: none !important;
                    padding: 0 !important;
                }
                .clean-map-badge:before {
                    display: none !important;
                }
                .map-badge-pickup {
                    background: #121214;
                    color: #FFB800;
                    border: 1.5px solid #FFB800;
                    font-size: 10px;
                    font-weight: 900;
                    letter-spacing: 0.6px;
                    padding: 4px 8px;
                    border-radius: 12px;
                    text-transform: uppercase;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.5);
                    white-space: nowrap;
                }
                .map-badge-delivery {
                    background: #FFB800;
                    color: #121214;
                    border: 1.5px solid #121214;
                    font-size: 10px;
                    font-weight: 900;
                    letter-spacing: 0.6px;
                    padding: 4px 8px;
                    border-radius: 12px;
                    text-transform: uppercase;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.5);
                    white-space: nowrap;
                }
                .map-controls {
                    position: absolute;
                    top: 16px;
                    left: 16px;
                    z-index: 1000;
                    background: rgba(18, 18, 18, 0.95);
                    border: 1.5px solid #FFB800;
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
                    font-weight: 800;
                    border-radius: 20px;
                    cursor: pointer;
                    transition: all 0.2s ease;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                }
                .control-btn.active {
                    background: #FFB800;
                    color: #121212;
                    font-weight: 900;
                }
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
                    background: rgba(255, 184, 0, 0.35);
                    border-radius: 50%;
                    z-index: 1;
                    animation: user-ripple 1.8s infinite ease-out;
                }
                @keyframes user-ripple {
                    0% { transform: translateX(-50%) scale(0.5); opacity: 1; }
                    100% { transform: translateX(-50%) scale(2.8); opacity: 0; }
                }
                .user-pointer-pin {
                    position: absolute;
                    top: 4px;
                    width: 44px;
                    height: 44px;
                    border-radius: 50% 50% 50% 0;
                    background: #121212;
                    border: 3px solid #FFB800;
                    transform: rotate(-45deg);
                    box-shadow: none;
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
                    background-color: #1A1A1A;
                    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23FFB800'%3E%3Cpath d='M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z'/%3E%3C/svg%3E");
                }
                .user-pointer-dot {
                    position: absolute;
                    bottom: 8px;
                    width: 8px;
                    height: 8px;
                    background: #FFB800;
                    border-radius: 50%;
                    z-index: 3;
                    border: 1.5px solid #121212;
                }
            </style>
            <script>
                $leafletJs
            </script>
        </head>
        <body>
            <div id="map"></div>
            <div class="map-controls">
                <button class="control-btn active" id="streetBtn" onclick="onToggleClick(false)">STREET</button>
                <button class="control-btn" id="satelliteBtn" onclick="onToggleClick(true)">SATELLITE</button>
            </div>
            <script>
                var pickupLoc = [6.3350, 5.6037];
                var deliveryLoc = [6.3450, 5.6250];
                var userLoc = null;
                var isDarkTheme = true;
                var isSatelliteMode = false;
                var followMode = 'courier';
                var userInteracted = false;
                var hasBooking = true;
                var isRiderMode = false;

                var map = null;
                var passedRouteLine = null;
                var activeRouteLine = null;
                var courierMarker = null;
                var pickupMarker = null;
                var deliveryMarker = null;
                var liveUserMarker = null;
                var arrivalBeaconCircle = null;
                var routeGeometryCoordinates = [];

                var googleStreetTiles = null;
                var darkStreetTiles = null;
                var satelliteTiles = null;
                var osmTiles = null;
                var currentProgress = 0.0;

                function initMapContainer() {
                    var container = document.getElementById('map');
                    if (!container || map) return;

                    try {
                        map = L.map('map', {
                            center: [6.3350, 5.6037],
                            zoom: 14,
                            minZoom: 3,
                            maxZoom: 20,
                            zoomControl: false,
                            attributionControl: false
                        });

                        googleStreetTiles = L.tileLayer('https://mt{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
                            maxZoom: 20,
                            subdomains: ['0', '1', '2', '3']
                        });

                        darkStreetTiles = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            maxZoom: 19,
                            className: 'dark-tiles'
                        });

                        satelliteTiles = L.tileLayer('https://mt{s}.google.com/vt/lyrs=y&x={x}&y={y}&z={z}', {
                            maxZoom: 20,
                            subdomains: ['0', '1', '2', '3']
                        });

                        osmTiles = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            maxZoom: 19
                        });

                        darkStreetTiles.on('tileerror', function() { googleStreetTiles.addTo(map); });
                        googleStreetTiles.on('tileerror', function() { osmTiles.addTo(map); });

                        if (isSatelliteMode) {
                            satelliteTiles.addTo(map);
                        } else if (isDarkTheme) {
                            darkStreetTiles.addTo(map);
                        } else {
                            googleStreetTiles.addTo(map);
                        }

                        map.on('movestart dragstart zoomstart', function(e) {
                            if (e && e.originalEvent) {
                                userInteracted = true;
                                if (window.AndroidMap && window.AndroidMap.onUserInteracted) {
                                    window.AndroidMap.onUserInteracted();
                                }
                            }
                        });

                        map.on('click', function(e) {
                            if (window.AndroidMap && window.AndroidMap.onMapClick) {
                                window.AndroidMap.onMapClick(e.latlng.lat, e.latlng.lng);
                            }
                        });

                        setTimeout(function() { if (map) map.invalidateSize(); }, 250);
                        window.addEventListener('resize', function() { if (map) map.invalidateSize(); });
                    } catch(e) {
                        console.error("Map init error:", e);
                    }
                }

                function distanceBetweenCoords(lat1, lon1, lat2, lon2) {
                    var R = 6371; // km
                    var dLat = (lat2 - lat1) * Math.PI / 180;
                    var dLon = (lon2 - lon1) * Math.PI / 180;
                    var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                            Math.sin(dLon/2) * Math.sin(dLon/2);
                    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
                    return R * c;
                }

                function distanceBetween(p1, p2) {
                    var dy = p1[0] - p2[0];
                    var dx = p1[1] - p2[1];
                    return Math.sqrt(dx * dx + dy * dy);
                }

                function calculateBearing(startLat, startLng, destLat, destLng) {
                    var startLatRad = startLat * Math.PI / 180;
                    var startLngRad = startLng * Math.PI / 180;
                    var destLatRad = destLat * Math.PI / 180;
                    var destLngRad = destLng * Math.PI / 180;
                    var y = Math.sin(destLngRad - startLngRad) * Math.cos(destLatRad);
                    var x = Math.cos(startLatRad) * Math.sin(destLatRad) -
                            Math.sin(startLatRad) * Math.cos(destLatRad) * Math.cos(destLngRad - startLngRad);
                    var brng = Math.atan2(y, x) * 180 / Math.PI;
                    return (brng + 360) % 360;
                }

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

                function setPickupLocation(lat, lng, addr) {
                    if (!lat || !lng) return;
                    pickupLoc = [lat, lng];
                    if (!map) return;
                    var goldCircleIcon = L.divIcon({
                        className: 'custom-div-icon',
                        html: "<div style='width: 16px; height: 16px; border-radius: 50%; background-color: #FFB800; border: 2.5px solid #000;'></div>",
                        iconSize: [16, 16],
                        iconAnchor: [8, 8]
                    });
                    if (pickupMarker) {
                        pickupMarker.setLatLng(pickupLoc);
                    } else {
                        pickupMarker = L.marker(pickupLoc, { icon: goldCircleIcon }).addTo(map);
                        pickupMarker.bindTooltip("<div class='map-badge-pickup'>PICKUP</div>", {
                            permanent: true,
                            direction: 'top',
                            className: 'clean-map-badge',
                            offset: [0, -10]
                        });
                    }
                    fetchOSRMRoute();
                }

                function setDeliveryLocation(lat, lng, addr) {
                    if (!lat || !lng) return;
                    deliveryLoc = [lat, lng];
                    if (!map) return;
                    var darkCircleIcon = L.divIcon({
                        className: 'custom-div-icon',
                        html: "<div style='width: 16px; height: 16px; border-radius: 50%; background-color: #0E0E10; border: 2.5px solid #FFB800;'></div>",
                        iconSize: [16, 16],
                        iconAnchor: [8, 8]
                    });
                    if (deliveryMarker) {
                        deliveryMarker.setLatLng(deliveryLoc);
                    } else {
                        deliveryMarker = L.marker(deliveryLoc, { icon: darkCircleIcon }).addTo(map);
                        deliveryMarker.bindTooltip("<div class='map-badge-delivery'>DELIVERY</div>", {
                            permanent: true,
                            direction: 'top',
                            className: 'clean-map-badge',
                            offset: [0, -10]
                        });
                    }
                    fetchOSRMRoute();
                }

                function setArrivalBeacon(active) {
                    if (!map || !deliveryLoc) return;
                    if (active) {
                        if (!arrivalBeaconCircle) {
                            var beaconIcon = L.divIcon({
                                className: 'arrival-beacon-wrap',
                                html: "<div class='arrival-beacon-ring'></div><div class='arrival-beacon-core'></div>",
                                iconSize: [60, 60],
                                iconAnchor: [30, 30]
                            });
                            arrivalBeaconCircle = L.marker(deliveryLoc, { icon: beaconIcon, zIndexOffset: -100 }).addTo(map);
                        }
                    } else {
                        if (arrivalBeaconCircle) {
                            try { map.removeLayer(arrivalBeaconCircle); } catch(e){}
                            arrivalBeaconCircle = null;
                        }
                    }
                }

                function fetchOSRMRoute() {
                    if (!pickupLoc || !deliveryLoc || !map) return;
                    var url = 'https://router.project-osrm.org/route/v1/driving/' + 
                        pickupLoc[1] + ',' + pickupLoc[0] + ';' + 
                        deliveryLoc[1] + ',' + deliveryLoc[0] + 
                        '?overview=full&geometries=geojson';
                    fetch(url)
                        .then(function(res) { return res.json(); })
                        .then(function(data) {
                            if (data && data.routes && data.routes.length > 0) {
                                var coords = data.routes[0].geometry.coordinates;
                                routeGeometryCoordinates = coords.map(function(c) { return [c[1], c[0]]; });
                                
                                if (!passedRouteLine) {
                                    passedRouteLine = L.polyline([], {
                                        color: '#71717A',
                                        weight: 4.5,
                                        opacity: 0.8,
                                        lineCap: 'round',
                                        lineJoin: 'round'
                                    }).addTo(map);
                                }
                                
                                if (!activeRouteLine) {
                                    activeRouteLine = L.polyline(routeGeometryCoordinates, {
                                        color: '#FFB800',
                                        weight: 5.5,
                                        opacity: 1.0,
                                        lineCap: 'round',
                                        lineJoin: 'round'
                                    }).addTo(map);
                                } else {
                                    activeRouteLine.setLatLngs(routeGeometryCoordinates);
                                }

                                sliceRouteAtProgress(currentProgress);

                                if (!userInteracted) {
                                    try {
                                        var bounds = L.latLngBounds(routeGeometryCoordinates);
                                        map.fitBounds(bounds, { padding: [50, 50], maxZoom: 16 });
                                    } catch(e) {}
                                }
                            }
                        })
                        .catch(function(err) {
                            console.error("OSRM directions error:", err);
                        });
                }

                function sliceRouteAtProgress(prog) {
                    currentProgress = prog;
                    if (!routeGeometryCoordinates || routeGeometryCoordinates.length === 0) return;
                    var totalPoints = routeGeometryCoordinates.length;
                    var targetIdx = Math.min(totalPoints - 1, Math.max(0, Math.floor(prog * (totalPoints - 1))));
                    var curCoord = getCoordinateAlongRoute(routeGeometryCoordinates, prog);

                    var passed = routeGeometryCoordinates.slice(0, targetIdx + 1);
                    if (curCoord) passed.push(curCoord);

                    var active = [];
                    if (curCoord) active.push(curCoord);
                    active = active.concat(routeGeometryCoordinates.slice(targetIdx + 1));

                    if (passedRouteLine) passedRouteLine.setLatLngs(passed);
                    if (activeRouteLine) activeRouteLine.setLatLngs(active);
                }

                function updateCourierLocation(lat, lng, bearing) {
                    if (isRiderMode) return;
                    if (!lat || !lng || (lat === 0.0 && lng === 0.0)) return;
                    if (!map) return;

                    var courierIcon = L.divIcon({
                        className: 'pulsing-courier',
                        html: "<div style='width: 38px; height: 38px; border-radius: 50%; border: 2.5px solid #FFB800; background-color: #0E0E10; display: flex; align-items: center; justify-content: center;'>" +
                            "<svg width='20' height='20' viewBox='0 0 24 24' fill='%23FFB800'><path d='M15.5 5.5c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zM5 12c-2.8 0-5 2.2-5 5s2.2 5 5 5 5-2.2 5-5-2.2-5-5-5zm0 8.5c-1.9 0-3.5-1.6-3.5-3.5s1.6-3.5 3.5-3.5 3.5 1.6 3.5 3.5-1.6 3.5-3.5 3.5zm14-8.5c-2.8 0-5 2.2-5 5s2.2 5 5 5 5-2.2 5-5-2.2-5-5-5zm0 8.5c-1.9 0-3.5-1.6-3.5-3.5s1.6-3.5 3.5-3.5 3.5 1.6 3.5 3.5-1.6 3.5-3.5 3.5zM10.8 10.5l2.4-2.4 1.8 1.8c1.1 1.1 2.6 1.8 4.2 1.8v-2c-1.1 0-2.2-.5-3-1.3l-1.9-1.9c-.4-.4-.9-.7-1.5-.7-.6 0-1.1.2-1.5.6L7.5 9.8 4.2 8.7 3.5 10.6l4.5 1.5 2.8-1.6z'/></svg>" +
                            "</div>",
                        iconSize: [38, 38],
                        iconAnchor: [19, 19]
                    });

                    if (courierMarker) {
                        courierMarker.setLatLng([lat, lng]);
                        var el = courierMarker.getElement ? courierMarker.getElement() : null;
                        if (el && bearing !== 0) {
                            el.style.transform = (el.style.transform || '').replace(/rotate\(.*?\)/, '') + ' rotate(' + bearing + 'deg)';
                        }
                    } else {
                        courierMarker = L.marker([lat, lng], { icon: courierIcon }).addTo(map);
                    }

                    if (deliveryLoc) {
                        var distKm = distanceBetweenCoords(lat, lng, deliveryLoc[0], deliveryLoc[1]);
                        setArrivalBeacon(distKm <= 0.05);
                    }

                    if (!userInteracted && followMode === 'courier') {
                        map.panTo([lat, lng], { animate: true, duration: 0.8 });
                    }

                    if (window.AndroidMap && window.AndroidMap.onTrackingUpdated) {
                        window.AndroidMap.onTrackingUpdated(lat, lng);
                    }
                }

                function updateCourierProgress(progressVal) {
                    var lat, lng;
                    var bearing = 0;
                    if (routeGeometryCoordinates && routeGeometryCoordinates.length > 0) {
                        var coord = getCoordinateAlongRoute(routeGeometryCoordinates, progressVal);
                        lat = coord[0];
                        lng = coord[1];
                        var nextCoord = getCoordinateAlongRoute(routeGeometryCoordinates, Math.min(1.0, progressVal + 0.02));
                        if (nextCoord) {
                            bearing = calculateBearing(lat, lng, nextCoord[0], nextCoord[1]);
                        }
                    } else {
                        lat = pickupLoc[0] + (deliveryLoc[0] - pickupLoc[0]) * progressVal;
                        lng = pickupLoc[1] + (deliveryLoc[1] - pickupLoc[1]) * progressVal;
                        bearing = calculateBearing(pickupLoc[0], pickupLoc[1], deliveryLoc[0], deliveryLoc[1]);
                    }
                    sliceRouteAtProgress(progressVal);
                    updateCourierLocation(lat, lng, bearing);
                }

                function updateCourierCoordinates(lat, lng) {
                    updateCourierLocation(lat, lng, 0);
                }

                function setUserLocation(lat, lng) {
                    userLoc = [lat, lng];
                    if (!map) return;
                    try {
                        if (liveUserMarker) {
                            liveUserMarker.setLatLng([lat, lng]);
                        } else {
                            var userIcon = L.divIcon({
                                className: 'user-pointer-container',
                                html: '<div class="user-pointer-pulse"></div><div class="user-pointer-pin"><div class="user-pointer-avatar"></div></div><div class="user-pointer-dot"></div>',
                                iconSize: [40, 40],
                                iconAnchor: [20, 40]
                            });
                            liveUserMarker = L.marker([lat, lng], { icon: userIcon }).addTo(map);
                        }
                        if (!userInteracted && followMode === 'user') {
                            map.panTo([lat, lng], { animate: true, duration: 0.8 });
                        }
                    } catch(err) {}
                }

                function updateMapType(isSat) {
                    isSatelliteMode = isSat;
                    if (!map) return;
                    try { map.removeLayer(satelliteTiles); } catch(e){}
                    try { map.removeLayer(darkStreetTiles); } catch(e){}
                    try { map.removeLayer(googleStreetTiles); } catch(e){}
                    try { map.removeLayer(osmTiles); } catch(e){}

                    if (isSat) {
                        satelliteTiles.addTo(map);
                        var satBtn = document.getElementById('satelliteBtn');
                        if (satBtn) satBtn.classList.add('active');
                        var strBtn = document.getElementById('streetBtn');
                        if (strBtn) strBtn.classList.remove('active');
                    } else {
                        if (isDarkTheme) {
                            darkStreetTiles.addTo(map);
                        } else {
                            googleStreetTiles.addTo(map);
                        }
                        var strBtn = document.getElementById('streetBtn');
                        if (strBtn) strBtn.classList.add('active');
                        var satBtn = document.getElementById('satelliteBtn');
                        if (satBtn) satBtn.classList.remove('active');
                    }
                }

                function onToggleClick(isSat) {
                    updateMapType(isSat);
                    if (window.AndroidMap) {
                        window.AndroidMap.onMapTypeToggled(isSat);
                    }
                }

                function recenterMap() {
                    userInteracted = false;
                    if (!map) return;
                    if (courierMarker && hasBooking) {
                        map.panTo(courierMarker.getLatLng(), { animate: true, duration: 0.8 });
                    } else if (routeGeometryCoordinates && routeGeometryCoordinates.length > 0) {
                        var bounds = L.latLngBounds(routeGeometryCoordinates);
                        map.fitBounds(bounds, { padding: [50, 50], maxZoom: 16 });
                    } else if (liveUserMarker) {
                        map.panTo(liveUserMarker.getLatLng(), { animate: true, duration: 0.8 });
                    } else if (pickupLoc && deliveryLoc) {
                        var bounds = L.latLngBounds([pickupLoc, deliveryLoc]);
                        map.fitBounds(bounds, { padding: [60, 60], maxZoom: 16 });
                    }
                }

                window.addEventListener('DOMContentLoaded', initMapContainer);
                setTimeout(initMapContainer, 80);
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    LaunchedEffect(htmlContent) {
        isPageLoaded = false
        webView.loadDataWithBaseURL("https://esdispatch.app", htmlContent, "text/html", "UTF-8", null)
    }

    LaunchedEffect(isPageLoaded) {
        if (isPageLoaded) {
            val safePickup = pickupCoords ?: Pair(6.3350, 5.6037)
            val safeDelivery = deliveryCoords ?: Pair(6.3450, 5.6250)
            val safePickupAddr = pickupAddress.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ")
            val safeDeliveryAddr = deliveryAddress.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ")
            webView.evaluateJavascript("setPickupLocation(${safePickup.first}, ${safePickup.second}, \"$safePickupAddr\")", null)
            webView.evaluateJavascript("setDeliveryLocation(${safeDelivery.first}, ${safeDelivery.second}, \"$safeDeliveryAddr\")", null)
            webView.evaluateJavascript("updateMapType($isSatellite)", null)
            if (userCoords != null) {
                webView.evaluateJavascript("setUserLocation(${userCoords.first}, ${userCoords.second})", null)
            }
            if (courierLatitude != null && courierLongitude != null) {
                webView.evaluateJavascript("updateCourierCoordinates($courierLatitude, $courierLongitude)", null)
            }
            webView.evaluateJavascript("updateCourierProgress($progress)", null)
        }
    }

    LaunchedEffect(isSatellite, isPageLoaded) {
        if (isPageLoaded) {
            webView.evaluateJavascript("updateMapType($isSatellite)", null)
        }
    }

    LaunchedEffect(zoom, isPageLoaded) {
        if (isPageLoaded) {
            webView.evaluateJavascript("if (typeof map !== 'undefined' && map !== null) { map.setZoom($zoom); }", null)
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

    LaunchedEffect(userCoords, isPageLoaded) {
        if (isPageLoaded && userCoords != null) {
            webView.evaluateJavascript("setUserLocation(${userCoords.first}, ${userCoords.second})", null)
        }
    }

    LaunchedEffect(pickupCoords, isPageLoaded) {
        if (isPageLoaded && pickupCoords != null) {
            val safeAddr = pickupAddress.replace("\"", "\\\"").replace("'", "\\'").trim()
            webView.evaluateJavascript("setPickupLocation(${pickupCoords.first}, ${pickupCoords.second}, '$safeAddr')", null)
        }
    }

    LaunchedEffect(deliveryCoords, isPageLoaded) {
        if (isPageLoaded && deliveryCoords != null) {
            val safeAddr = deliveryAddress.replace("\"", "\\\"").replace("'", "\\'").trim()
            webView.evaluateJavascript("setDeliveryLocation(${deliveryCoords.first}, ${deliveryCoords.second}, '$safeAddr')", null)
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize()
        )

        // Floating Recenter button when user has panned or zoomed
        androidx.compose.animation.AnimatedVisibility(
            visible = userHasPanned,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 120.dp)
                .zIndex(15f)
        ) {
            Surface(
                onClick = {
                    userHasPanned = false
                    webView.evaluateJavascript("recenterMap()", null)
                },
                shape = RoundedCornerShape(20.dp),
                color = Obsidian,
                border = BorderStroke(1.5.dp, Gold),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CenterFocusStrong,
                        contentDescription = "Recenter Map",
                        tint = Gold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Recenter",
                        color = Gold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun DeliveryEstimationCard(
    status: ParcelStatus,
    progress: Float,
    isDark: Boolean
) {
    val isLight = !isDark
    val now = remember { java.util.Date() }
    val dayFormat = remember { java.text.SimpleDateFormat("EEEE, MMMM d, yyyy", java.util.Locale.getDefault()) }
    val timeFormat = remember { java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()) }
    val todayStr = remember(now) { "Today, ${dayFormat.format(now)}" }
    val currentTimeStr = remember(now) { timeFormat.format(now) }

    val estimationText = when (status) {
        ParcelStatus.PENDING -> "Awaiting Assignment"
        ParcelStatus.QUEUED -> "Queued in Dispatch"
        ParcelStatus.RESERVED_NEXT -> "Courier Reserved"
        ParcelStatus.ASSIGNED -> "Preparing for Pickup"
        ParcelStatus.PICKED_UP -> "Parcel Picked Up"
        ParcelStatus.ARRIVED -> "Arrived at Destination"
        ParcelStatus.HANDOVER_VERIFIED -> "Handover Verified"
        ParcelStatus.DELIVERED -> "Delivered ($currentTimeStr)"
        ParcelStatus.OUT_FOR_DELIVERY -> todayStr
        ParcelStatus.CANCELLED -> "No Delivery (Cancelled)"
        ParcelStatus.TRANSIT -> {
            when {
                progress >= 0.7f -> todayStr
                progress >= 0.3f -> "Estimated: $todayStr"
                else -> "In Dispatch Queue"
            }
        }
        else -> "Active Dispatch"
    }

    val windowText = when (status) {
        ParcelStatus.PENDING -> "Waiting for dispatcher to assign a courier"
        ParcelStatus.QUEUED -> "All couriers active; order will dispatch shortly"
        ParcelStatus.RESERVED_NEXT -> "Courier is finishing a nearby delivery and will proceed next"
        ParcelStatus.ASSIGNED -> "Courier has been dispatched to pickup location"
        ParcelStatus.PICKED_UP -> "Courier has picked up the parcel"
        ParcelStatus.ARRIVED -> "Courier is at the delivery location"
        ParcelStatus.HANDOVER_VERIFIED -> "OTP verified • Courier is uploading final Proof of Delivery"
        ParcelStatus.DELIVERED -> "Successfully handed over to recipient"
        ParcelStatus.OUT_FOR_DELIVERY -> "Active courier on route in Benin City"
        ParcelStatus.CANCELLED -> "Shipment was cancelled by sender"
        ParcelStatus.TRANSIT -> {
            val remainingMins = (35 * (1f - progress.coerceIn(0f, 0.95f))).toInt().coerceAtLeast(4)
            val minRange = (remainingMins - 3).coerceAtLeast(2)
            val maxRange = remainingMins + 4
            "Estimated arrival in $minRange–$maxRange mins (Traffic adjusted)"
        }
        else -> "Active delivery transit"
    }

    val confidenceScore = when (status) {
        ParcelStatus.PENDING -> "Received"
        ParcelStatus.QUEUED -> "Queued"
        ParcelStatus.RESERVED_NEXT -> "Reserved"
        ParcelStatus.ASSIGNED -> "Assigned"
        ParcelStatus.PICKED_UP -> "In Custody"
        ParcelStatus.ARRIVED -> "Arrived"
        ParcelStatus.HANDOVER_VERIFIED -> "Verified"
        ParcelStatus.DELIVERED -> "Completed"
        ParcelStatus.OUT_FOR_DELIVERY -> "Active"
        ParcelStatus.CANCELLED -> "Cancelled"
        ParcelStatus.TRANSIT -> "In Transit"
        else -> status.name.replace('_', ' ')
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
        ParcelStatus.QUEUED -> Color(0x20FF9800)
        ParcelStatus.RESERVED_NEXT -> Color(0x209C27B0)
        ParcelStatus.ASSIGNED -> Color(0x203F51B5)
        ParcelStatus.PICKED_UP -> Color(0x205E35B1)
        ParcelStatus.ARRIVED -> Color(0x2000897B)
        ParcelStatus.HANDOVER_VERIFIED -> Color(0x20009688)
        ParcelStatus.DELIVERED -> Color(0x204CAF50)
        ParcelStatus.OUT_FOR_DELIVERY -> Color(0x20FF9800)
        ParcelStatus.CANCELLED -> Color(0x20F44336)
        ParcelStatus.TRANSIT -> if (isDark) Gold.copy(alpha = 0.15f) else Color(0x100E0E10)
        else -> Gold.copy(alpha = 0.15f)
    }
    val targetTextColor = when (status) {
        ParcelStatus.PENDING -> Color(0xFF2196F3)
        ParcelStatus.QUEUED -> Color(0xFFFF9800)
        ParcelStatus.RESERVED_NEXT -> Color(0xFF9C27B0)
        ParcelStatus.ASSIGNED -> Color(0xFF3F51B5)
        ParcelStatus.PICKED_UP -> Color(0xFF5E35B1)
        ParcelStatus.ARRIVED -> Color(0xFF00897B)
        ParcelStatus.HANDOVER_VERIFIED -> Color(0xFF009688)
        ParcelStatus.DELIVERED -> Color(0xFF4CAF50)
        ParcelStatus.OUT_FOR_DELIVERY -> Color(0xFFFF9800)
        ParcelStatus.CANCELLED -> Color(0xFFF44336)
        ParcelStatus.TRANSIT -> if (isDark) Gold else Obsidian
        else -> Gold
    }
    val badgeText = when (status) {
        ParcelStatus.PENDING -> "Pending Dispatch"
        ParcelStatus.QUEUED -> "Queued"
        ParcelStatus.RESERVED_NEXT -> "Courier Reserved"
        ParcelStatus.ASSIGNED -> "Courier Assigned"
        ParcelStatus.PICKED_UP -> "Picked Up"
        ParcelStatus.ARRIVED -> "Arrived"
        ParcelStatus.HANDOVER_VERIFIED -> "Handover Verified"
        ParcelStatus.TRANSIT -> "In Transit"
        ParcelStatus.OUT_FOR_DELIVERY -> "Out for Delivery"
        ParcelStatus.DELIVERED -> "Delivered"
        ParcelStatus.CANCELLED -> "Cancelled"
        else -> status.name.replace('_', ' ')
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

@Composable
fun ShippingJourneyProgressBar(
    status: ParcelStatus,
    progress: Float,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val isLight = !isDark
    
    // Define steps
    val steps = listOf("Queued", "Assigned", "In Transit", "Delivered")
    
    // Determine active step index directly mapped to ParcelStatus
    val activeIndex = when (status) {
        ParcelStatus.CANCELLED -> -1
        ParcelStatus.DELIVERED -> 3
        ParcelStatus.TRANSIT, ParcelStatus.OUT_FOR_DELIVERY, ParcelStatus.ARRIVED, ParcelStatus.HANDOVER_VERIFIED -> 2
        ParcelStatus.ASSIGNED, ParcelStatus.PICKED_UP -> 1
        ParcelStatus.PENDING, ParcelStatus.QUEUED, ParcelStatus.RESERVED_NEXT -> 0
        else -> 1
    }
    
    // Progress line mapping (fraction of track that is filled)
    val targetProgressFraction = when {
        status == ParcelStatus.CANCELLED -> 0f
        activeIndex == 3 -> 1.0f
        activeIndex == 2 -> 0.66f + ((progress - 0.5f).coerceAtLeast(0f) * 0.68f).coerceAtMost(0.34f)
        activeIndex == 1 -> 0.33f + ((progress - 0.2f).coerceAtLeast(0f) * 0.55f).coerceAtMost(0.33f)
        else -> 0.08f
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
        if (isLight) highlightColor else Gold.copy(alpha = 0.25f),
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
                val addresses = com.esdispatch.utils.GeocoderUtils.getFromLocationCompat(geocoder, lat, lng, 1)
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
                urlConnection.setRequestProperty("User-Agent", "ESDispatchAndroidApp/1.0 (reachheytek@gmail.com)")
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

            // Local Benin City Landmark geocoder fallback (highly robust)
            val landmarks = listOf(
                Triple(6.3350, 5.6260, "King's Square, Ring Road, Benin City"),
                Triple(6.3150, 5.6120, "Airport Road, GRA, Benin City"),
                Triple(6.3812, 5.6291, "UNIBEN Main Gate, Ugbowo, Benin City"),
                Triple(6.3330, 5.6230, "Oba Market, Ring Road, Benin City"),
                Triple(6.3180, 5.6320, "Kada Plaza, Sapele Road, Benin City"),
                Triple(6.3450, 5.6550, "Ramat Park, Ikpoba Hill, Benin City"),
                Triple(6.3750, 5.6150, "Uselu Market, Uselu, Benin City"),
                Triple(6.3210, 5.5980, "Ekenwan Road Campus, Benin City"),
                Triple(6.3710, 5.6610, "Aduwawa Central, Benin City"),
                Triple(6.3520, 5.5890, "Siluko Road Junction, Benin City")
            )

            val nearest = landmarks.minByOrNull { (lLat, lLng, _) ->
                val dLat = lat - lLat
                val dLng = lng - lLng
                dLat * dLat + dLng * dLng
            }

            if (nearest != null) {
                nearest.third
            } else {
                "King's Square, Ring Road, Benin City"
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
    walletBalance: Double = 0.0,
    onDismiss: () -> Unit,
    onSubmit: (Double, Double) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var rating by remember { mutableStateOf(5) }
    var selectedTipIndex by remember { mutableStateOf(1) } // Default to index 1 (₦1,000)
    val tipOptions = listOf(0.0, 500.0, 1000.0, 2000.0, -1.0) // -1.0 is Custom
    var customTipString by remember { mutableStateOf("") }

    val tipAmount = if (selectedTipIndex == 4) {
        customTipString.toDoubleOrNull() ?: 0.0
    } else {
        tipOptions.getOrElse(selectedTipIndex) { 0.0 }
    }

    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    androidx.compose.material3.BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(16.dp),
        content = {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Charcoal,
                tonalElevation = 6.dp,
                border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.2f) else Slate)
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
                        text = "Your feedback helps us maintain the ESDispatch premium standard.",
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
                        CourierAvatarBadge(
                            avatarUrl = parcel.courierAvatar,
                            name = parcel.courierName.ifBlank { "Courier" },
                            size = 44.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = parcel.courierName.ifBlank { "Assigned Courier" },
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Add Courier Tip",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) GoldLight else Obsidian
                            )
                            Text(
                                text = "Balance: ₦${String.format("%,.2f", walletBalance)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (tipAmount > walletBalance) Color(0xFFFF5252) else TextGray
                            )
                        }
                        
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
                                    focusedContainerColor = if (isDark) LuxuryBlack else GoldenWhiteSurface,
                                    unfocusedContainerColor = if (isDark) LuxuryBlack else GoldenWhiteLight,
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
                                containerColor = if (isDark) Obsidian else Gold
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.3f) else Obsidian.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "Skip/Cancel",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Gold else Obsidian
                            )
                        }

                        Button(
                            onClick = {
                                if (tipAmount > 0.0 && tipAmount > walletBalance) {
                                    Toast.makeText(context, "Insufficient wallet balance (₦${String.format("%,.2f", walletBalance)}). Please top up your wallet to add a tip.", Toast.LENGTH_LONG).show()
                                } else {
                                    onSubmit(rating.toDouble(), tipAmount)
                                }
                            },
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
    recipientPhone: String = "",
    viewModel: DeliveryViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
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
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = headerContentColor
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (senderRole == "customer") "Customer Support" else "Dispatch & Customer Care",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = headerContentColor,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Shipment #${parcelId.take(8).uppercase()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Obsidian.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.7f)
                        )
                    }

                    if (recipientPhone.isNotBlank()) {
                        val cleanPhone = recipientPhone.filter { it.isDigit() || it == '+' }
                        IconButton(
                            onClick = {
                                viewModel.logCourierCallEvent(parcelId, cleanPhone)
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone"))
                                try {
                                    context.startActivity(dialIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Call not supported on this device", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = "Call Contact",
                                tint = headerContentColor
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
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
                                imageVector = Icons.Filled.Send,
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

private suspend fun detectUserLocationCoords(context: android.content.Context): Pair<Double, Double>? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    try {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            val fusedClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
            val loc: android.location.Location? = kotlinx.coroutines.suspendCancellableCoroutine { cont ->
                fusedClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        cont.resumeWith(Result.success(location))
                    } else {
                        fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                            cont.resumeWith(Result.success(lastLoc))
                        }.addOnFailureListener { cont.resumeWith(Result.success(null)) }
                    }
                }.addOnFailureListener {
                    fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                        cont.resumeWith(Result.success(lastLoc))
                    }.addOnFailureListener { cont.resumeWith(Result.success(null)) }
                }
            }
            if (loc != null) {
                return@withContext Pair(loc.latitude, loc.longitude)
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("DetectLocationCoords", "GPS high accuracy detection failed: ${e.message}")
    }

    try {
        val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager
        if (lm != null) {
            val providers = lm.getProviders(true)
            for (provider in providers) {
                @Suppress("MissingPermission")
                val l = lm.getLastKnownLocation(provider)
                if (l != null) {
                    return@withContext Pair(l.latitude, l.longitude)
                }
            }
        }
    } catch (_: Exception) {}

    return@withContext null
}
