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
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
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
    androidx.activity.compose.BackHandler {
        onNavigate("BACK")
    }
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
    var isGuidanceDismissed by remember { mutableStateOf(false) }
    var isActionSubmitting by remember { mutableStateOf(false) }

    val userAvatar by viewModel.photoUrl.collectAsState()
    var userCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var userLocationTime by remember { mutableLongStateOf(0L) }
    var userLocationBearing by remember { mutableFloatStateOf(0f) }
    var userLocationAccuracy by remember { mutableFloatStateOf(Float.MAX_VALUE) }
    val scope = rememberCoroutineScope()
    var locationPermissionRevision by remember { mutableIntStateOf(0) }

    val locationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions: Map<String, Boolean> ->
        locationPermissionRevision++
        scope.launch {
            val detected = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                detectUserLocationCoords(context)
            }
            if (userLocationTime == 0L) userCoords = detected
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
            if (detected != null) {
                userCoords = detected
                userLocationTime = System.currentTimeMillis()
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    DisposableEffect(context, locationPermissionRevision) {
        val fusedClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
        val callback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(res: com.google.android.gms.location.LocationResult) {
                res.lastLocation?.let { loc ->
                    if (!loc.hasAccuracy() || loc.accuracy > 100f ||
                        com.esdispatch.ui.maps.validMapPosition(loc.latitude, loc.longitude) == null) return
                    userCoords = Pair(loc.latitude, loc.longitude)
                    userLocationTime = loc.time
                    if (loc.hasBearing()) userLocationBearing = loc.bearing
                    userLocationAccuracy = loc.accuracy
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
            try {
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null && com.esdispatch.ui.maps.validMapPosition(loc.latitude, loc.longitude) != null) {
                        if (userCoords == null) {
                            userCoords = Pair(loc.latitude, loc.longitude)
                            userLocationTime = loc.time
                            if (loc.hasBearing()) userLocationBearing = loc.bearing
                            userLocationAccuracy = loc.accuracy
                        }
                    }
                }
            } catch (_: Exception) {}

            val req = com.google.android.gms.location.LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 4000
            ).setMinUpdateIntervalMillis(1500).build()
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
    val activeParcel = remember(selectedParcel, activeParcels) {
        // Keep the selected shipment visible through completion; do not jump to another order.
        selectedParcel ?: activeParcels.firstOrNull()
    }

    val trackingTargetId = activeParcel?.id ?: selectedParcel?.id
    LaunchedEffect(trackingTargetId) {
        trackingTargetId?.takeIf { it.isNotBlank() }?.let { id ->
            viewModel.startRealTimeTrackingListener(id)
        }
    }

    val serviceLiveLocation by com.esdispatch.util.LocationService.liveLocationFlow.collectAsState()
    LaunchedEffect(serviceLiveLocation) {
        serviceLiveLocation?.let { loc ->
            if (loc.time >= userLocationTime) {
                userCoords = Pair(loc.latitude, loc.longitude)
                userLocationTime = loc.time
                if (loc.hasBearing()) userLocationBearing = loc.bearing
                userLocationAccuracy = loc.accuracy
            }
        }
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

    val resolvedPickupLat = remember(parcel.pickupLat, parcel.pickupAddress) {
        parcel.pickupLat?.takeIf { it != 0.0 }
            ?: com.esdispatch.data.AddressDatabase.searchItems(parcel.pickupAddress).firstOrNull()?.lat
    }
    val resolvedPickupLng = remember(parcel.pickupLng, parcel.pickupAddress) {
        parcel.pickupLng?.takeIf { it != 0.0 }
            ?: com.esdispatch.data.AddressDatabase.searchItems(parcel.pickupAddress).firstOrNull()?.lng
    }
    val resolvedDeliveryLat = remember(parcel.deliveryLat, parcel.deliveryAddress) {
        parcel.deliveryLat?.takeIf { it != 0.0 }
            ?: com.esdispatch.data.AddressDatabase.searchItems(parcel.deliveryAddress).firstOrNull()?.lat
    }
    val resolvedDeliveryLng = remember(parcel.deliveryLng, parcel.deliveryAddress) {
        parcel.deliveryLng?.takeIf { it != 0.0 }
            ?: com.esdispatch.data.AddressDatabase.searchItems(parcel.deliveryAddress).firstOrNull()?.lng
    }

    LaunchedEffect(isRider, activeParcel?.id, activeParcel?.status, locationPermissionRevision, resolvedPickupLat, resolvedDeliveryLat) {
        if (isRider && activeParcel?.status !in listOf(ParcelStatus.DELIVERED, ParcelStatus.CANCELLED, ParcelStatus.RETURNED)) {
            val fineGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (fineGranted) {
                val isPickup = activeParcel?.status in listOf(ParcelStatus.ASSIGNED, ParcelStatus.ARRIVED_PICKUP)
                val targetLat = if (isPickup) resolvedPickupLat else resolvedDeliveryLat
                val targetLng = if (isPickup) resolvedPickupLng else resolvedDeliveryLng
                val targetType = if (isPickup) "PICKUP" else "DELIVERY"

                com.esdispatch.util.LocationService.start(
                    context = context,
                    parcelId = activeParcel?.id,
                    batchId = activeParcel?.batchId,
                    stopLat = targetLat,
                    stopLng = targetLng,
                    stopType = targetType
                )
            }
        }
    }

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

    val trackingPrefs = remember(context) { context.getSharedPreferences("esdispatch_prefs", android.content.Context.MODE_PRIVATE) }
    val isFeedbackDismissed = remember(parcel.id, parcel.feedbackDismissed) {
        parcel.feedbackDismissed || trackingPrefs.getBoolean("feedback_dismissed_${parcel.id}", false)
    }

    // Automatically and intelligently pop up Feedback & Tip dialog once when delivery is completed
    LaunchedEffect(parcel.status, parcel.isRated, isRider, isFeedbackDismissed, parcel.id) {
        if (!isRider && parcel.status == ParcelStatus.DELIVERED && !parcel.isRated && parcel.id.isNotBlank() && !isFeedbackDismissed) {
            showFeedbackDialog = true
        }
    }

    if (showFeedbackDialog) {
        DeliveryFeedbackDialog(
            parcel = parcel,
            isDark = isDark,
            walletBalance = walletBalance,
            onDismiss = {
                showFeedbackDialog = false
                trackingPrefs.edit().putBoolean("feedback_dismissed_${parcel.id}", true).apply()
                viewModel.dismissFeedback(parcel.id)
            },
            onSubmit = { rating, tip ->
                trackingPrefs.edit().putBoolean("feedback_dismissed_${parcel.id}", true).apply()
                viewModel.dismissFeedback(parcel.id)
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


    var isMapPanned by remember { mutableStateOf(false) }
    var recenterTrigger by remember { mutableIntStateOf(0) }

    var drawerState by remember(hasNoBooking) {
        mutableStateOf(DrawerState.COLLAPSED)
    }
    var isGoingUp by remember { mutableStateOf(true) }

    val density = androidx.compose.ui.platform.LocalDensity.current
    val screenHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp
    val dynamicExpandedHeight = (screenHeight * 0.72f).coerceIn(340.dp, 580.dp)
    val dynamicCollapsedHeight = (screenHeight * 0.42f).coerceIn(220.dp, 360.dp)
    val dynamicClosedHeight = 0.dp

    var dragOffsetDp by remember { mutableStateOf(0.dp) }

    val baseTargetHeight = when (drawerState) {
        DrawerState.CLOSED -> 0.dp
        DrawerState.COLLAPSED -> if (hasNoBooking) 290.dp else dynamicCollapsedHeight
        DrawerState.EXPANDED -> if (hasNoBooking) 360.dp else dynamicExpandedHeight
    }

    val bottomCardHeight by animateDpAsState(
        targetValue = if (drawerState == DrawerState.CLOSED) 0.dp else (baseTargetHeight + dragOffsetDp).coerceIn(
            160.dp,
            if (hasNoBooking) 360.dp else dynamicExpandedHeight
        ),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bottomCardHeight"
    )

    val drawerDragModifier = Modifier.pointerInput(hasNoBooking, baseTargetHeight, dynamicCollapsedHeight, dynamicExpandedHeight) {
        detectVerticalDragGestures(
            onDragStart = {
                dragOffsetDp = 0.dp
            },
            onVerticalDrag = { change, dragAmount ->
                change.consume()
                val deltaDp = -dragAmount / density.density
                dragOffsetDp = (dragOffsetDp + deltaDp.dp).coerceIn(
                    -(baseTargetHeight - 120.dp),
                    (dynamicExpandedHeight - baseTargetHeight)
                )
            },
            onDragEnd = {
                val currentEffectiveHeight = baseTargetHeight + dragOffsetDp
                dragOffsetDp = 0.dp
                if (hasNoBooking) {
                    drawerState = if (currentEffectiveHeight > 180.dp) DrawerState.COLLAPSED else DrawerState.CLOSED
                } else {
                    val midExpanded = (dynamicCollapsedHeight + dynamicExpandedHeight) / 2
                    val midCollapsed = dynamicCollapsedHeight / 2
                    drawerState = when {
                        currentEffectiveHeight >= midExpanded -> {
                            isGoingUp = false
                            DrawerState.EXPANDED
                        }
                        currentEffectiveHeight >= midCollapsed -> {
                            isGoingUp = true
                            DrawerState.COLLAPSED
                        }
                        else -> {
                            isGoingUp = true
                            DrawerState.CLOSED
                        }
                    }
                }
            },
            onDragCancel = {
                dragOffsetDp = 0.dp
            }
        )
    }

    var weatherLabel by remember { mutableStateOf("Weather unavailable") }
    LaunchedEffect(Unit) {
        weatherLabel = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val lat = parcel.pickupLat ?: parcel.courierLatitude ?: 6.3350
            val lng = parcel.pickupLng ?: parcel.courierLongitude ?: 5.6037
            val connection = java.net.URL("https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lng&current=temperature_2m,weather_code")
                .openConnection() as java.net.HttpURLConnection
            try {
                connection.connectTimeout = 4000
                connection.readTimeout = 4000
                val weather = org.json.JSONObject(connection.inputStream.bufferedReader().use { it.readText() }).getJSONObject("current")
                val condition = when (weather.getInt("weather_code")) {
                    0 -> "Clear"
                    1, 2, 3 -> "Cloudy"
                    45, 48 -> "Fog"
                    in 51..82 -> "Rain"
                    in 95..99 -> "Storm"
                    else -> "Weather"
                }
                "$condition ${weather.getDouble("temperature_2m").toInt()}°C"
            } catch (_: Exception) { "Weather unavailable" } finally { connection.disconnect() }
        }
    }

    var roadGuidance by remember(parcel.id, parcel.status) {
        mutableStateOf(com.esdispatch.ui.maps.RouteGuidance())
    }
    var routeRetry by remember(parcel.id) { mutableIntStateOf(0) }
    var isSatelliteMode by remember { mutableStateOf(false) }
    var showTraffic by remember { mutableStateOf(true) }
    var mapZoom by remember { mutableFloatStateOf(14.5f) }
    var followUser by remember(hasNoBooking) { mutableStateOf(hasNoBooking) }
    var is3D by remember { mutableStateOf(false) }
    var isNavigating by remember(parcel.id) { mutableStateOf(false) }
    var isVoiceMuted by remember { mutableStateOf(false) }
    val realDistanceKm = roadGuidance.distanceMeters?.div(1000)?.toFloat()
    val pickupPhase = parcel.status in listOf(ParcelStatus.ASSIGNED, ParcelStatus.ARRIVED_PICKUP)
    val navigationAddress = if (pickupPhase || parcel.status == ParcelStatus.RETURN_TO_SENDER) parcel.pickupAddress else parcel.deliveryAddress

    fun toggleInAppNavigation() {
        isNavigating = !isNavigating
        if (isNavigating) {
            is3D = true
            followUser = true
            isMapPanned = false
            recenterTrigger++
            mapZoom = 17.2f
            val targetName = if (pickupPhase) "pickup" else "destination"
            com.esdispatch.util.VoiceGuidanceManager.speak(
                "Starting driving guidance to $targetName. Follow the road route.",
                isUrgent = true
            )
        } else {
            com.esdispatch.util.VoiceGuidanceManager.stop()
        }
    }

    fun openExternalGoogleMaps() {
        val destination = roadGuidance.destination?.let { "${it.latitude},${it.longitude}" }
            ?: navigationAddress.takeIf { it.isNotBlank() }
        if (destination == null) {
            Toast.makeText(context, "Destination address is not available for external maps.", Toast.LENGTH_LONG).show()
            return
        }
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=${Uri.encode(destination)}&mode=d"))
                .setPackage("com.google.android.apps.maps"))
        } catch (_: android.content.ActivityNotFoundException) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${Uri.encode(destination)}&travelmode=driving")))
            } catch (_: android.content.ActivityNotFoundException) {
                Toast.makeText(context, "No external navigation app is available on this device.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Voice announcement when guidance changes during active in-app navigation
    LaunchedEffect(roadGuidance.detail, isNavigating) {
        if (isNavigating && !isVoiceMuted && roadGuidance.detail.isNotBlank() && !roadGuidance.loading) {
            com.esdispatch.util.VoiceGuidanceManager.speak(roadGuidance.detail)
        }
    }

    var hasNotifiedWithinOneMile by remember(parcel.id) { mutableStateOf(false) }
    var showInAppNotificationBanner by remember(parcel.id) { mutableStateOf(false) }
    var consecutiveArrivalPings by remember(parcel.id, parcel.status) { mutableIntStateOf(0) }
    var showArrivalPrompt by remember(parcel.id, parcel.status) { mutableStateOf(false) }
    var arrivalPromptDismissed by remember(parcel.id, parcel.status) { mutableStateOf(false) }
    var isPickupArrival by remember(parcel.id, parcel.status) { mutableStateOf(false) }

    val locationTime = if (isRider) userLocationTime else parcel.courierLastUpdated
    LaunchedEffect(locationTime, parcel.status, userCoords) {
        val isPickup = parcel.status in listOf(ParcelStatus.ASSIGNED, ParcelStatus.ARRIVED_PICKUP)
        val isDelivery = parcel.status in listOf(ParcelStatus.PICKED_UP, ParcelStatus.TRANSIT, ParcelStatus.OUT_FOR_DELIVERY)
        val recent = locationTime > 0 && System.currentTimeMillis() - locationTime in -5000L..60000L
        if ((!isPickup && !isDelivery) || !recent) {
            showInAppNotificationBanner = false
            consecutiveArrivalPings = 0
            return@LaunchedEffect
        }
        val position = if (isRider) userCoords else parcel.courierLatitude?.let { lat -> parcel.courierLongitude?.let { lat to it } }
        val target = if (isPickup) {
            com.esdispatch.ui.maps.validMapPosition(resolvedPickupLat, resolvedPickupLng)
        } else {
            com.esdispatch.ui.maps.validMapPosition(resolvedDeliveryLat, resolvedDeliveryLng) ?: roadGuidance.destination
        }
        if (position != null && target != null) {
            val meters = FloatArray(1)
            android.location.Location.distanceBetween(position.first, position.second, target.latitude, target.longitude, meters)
            val threshold = if (userLocationAccuracy > 0f) (50f + (userLocationAccuracy * 0.5f)).coerceIn(50f, 85f) else 50f
            if (isRider && meters[0] <= threshold) {
                consecutiveArrivalPings++
                if (consecutiveArrivalPings >= 2 && !arrivalPromptDismissed) {
                    if (isPickup && parcel.status == ParcelStatus.ASSIGNED) {
                        isPickupArrival = true
                        showArrivalPrompt = true
                        com.esdispatch.util.VoiceGuidanceManager.speak(
                            "You have arrived at the pickup location. Please collect the parcel from the sender.",
                            isUrgent = true
                        )
                    } else if (isDelivery) {
                        isPickupArrival = false
                        showArrivalPrompt = true
                        com.esdispatch.util.VoiceGuidanceManager.speak(
                            "You have arrived at the delivery destination. Please verify the recipient's handover PIN.",
                            isUrgent = true
                        )
                    }
                }
            } else {
                consecutiveArrivalPings = 0
            }
            if (!isRider && meters[0] <= 1609.34f && !hasNotifiedWithinOneMile) {
                hasNotifiedWithinOneMile = true
                showInAppNotificationBanner = true
            }
        }
    }

    if (showArrivalPrompt) {
        val isPickup = isPickupArrival
        AlertDialog(
            onDismissRequest = { showArrivalPrompt = false; arrivalPromptDismissed = true },
            title = { Text(if (isPickup) "At Pickup Location?" else "At Delivery Address?") },
            text = {
                Text(
                    if (isPickup) "You are near the sender's pickup address. Confirm arrival to notify the customer."
                    else "You are near the destination. Confirm arrival to proceed with handover PIN verification."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showArrivalPrompt = false
                    arrivalPromptDismissed = true
                    val targetStatus = if (isPickup) ParcelStatus.ARRIVED_PICKUP else ParcelStatus.ARRIVED
                    val targetProgress = if (isPickup) 0.40f else 0.95f
                    viewModel.updateParcelStatusByRider(parcel.id, targetStatus, targetProgress) { ok, _ ->
                        if (!ok) {
                            arrivalPromptDismissed = false
                            Toast.makeText(context, "Could not update arrival. Please try again.", Toast.LENGTH_LONG).show()
                        }
                    }
                }) { Text(if (isPickup) "I've Arrived at Pickup" else "I've Arrived") }
            },
            dismissButton = {
                TextButton(onClick = { showArrivalPrompt = false; arrivalPromptDismissed = true }) { Text("Not yet") }
            }
        )
    }


    // Auto-dismiss the in-app notification banner after 6 seconds
    LaunchedEffect(showInAppNotificationBanner) {
        if (showInAppNotificationBanner) {
            delay(6000L)
            showInAppNotificationBanner = false
        }
    }

    // ------------------------------------------------------------------------------------------------
    // Map view configurations driven by live GPS telemetry
    val accentIconColor = if (isLight) Obsidian else Gold
    val accentTextColor = if (isLight) Obsidian else Gold


    val headerBgColor = if (isDark) Gold else Obsidian
    Scaffold(
        containerColor = headerBgColor
    ) { innerPadding ->
        androidx.activity.compose.BackHandler {
            onNavigate("Dashboard")
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .background(headerBgColor)
        ) {
            ScreenHeader(
                title = if (isRider) "Delivery Navigation" else "Track Shipment",
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
                                    text = "${p.itemName.ifBlank { "Shipment" }.take(14)} (${com.esdispatch.util.FormatUtils.formatDisplayTrackingId(p.id)})",
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
            val routeColor = "#FFB800"

            val matchedRider = riders.find { it.id.isNotBlank() && it.id == parcel.riderId.ifEmpty { parcel.driverId } }
            val resolvedCourierAvatar = if (isRider) {
                userAvatar
            } else if (parcel.courierAvatar.isNotBlank()) {
                parcel.courierAvatar
            } else {
                matchedRider?.avatar?.ifBlank { "" } ?: ""
            }
            val resolvedCourierName = if (parcel.courierName.isNotBlank()) parcel.courierName else (matchedRider?.name ?: "Verified Dispatch Courier")
            val resolvedCourierPhone = if (parcel.courierPhone.isNotBlank()) parcel.courierPhone else (matchedRider?.phone ?: "")

            // 1. FULL SCREEN MAP BACKGROUND (Uber-like experience)
            if (isLocalLoading) {
                SkeletonBox(
                    modifier = Modifier.fillMaxSize(),
                    isLight = isLight,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
            } else {
                key(parcel.id) { LiveMapView(
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
                    courierLatitude = if (isRider) (userCoords?.first ?: parcel.courierLatitude) else parcel.courierLatitude,
                    courierLongitude = if (isRider) (userCoords?.second ?: parcel.courierLongitude) else parcel.courierLongitude,
                    courierBearing = if (isRider) userLocationBearing else parcel.courierBearing,
                    userAvatar = userAvatar,
                    hasNoBooking = hasNoBooking,
                    followUser = followUser,
                    userCoords = userCoords,
                    isRider = isRider,
                    parcelStatus = parcel.status,
                    parcelPickupLat = resolvedPickupLat,
                    parcelPickupLng = resolvedPickupLng,
                    parcelDeliveryLat = resolvedDeliveryLat,
                    parcelDeliveryLng = resolvedDeliveryLng,
                    courierName = resolvedCourierName,
                    courierPhone = resolvedCourierPhone,
                    isDarkTheme = isDark,
                    is3D = is3D,
                    courierLastUpdated = locationTime,
                    routeRetry = routeRetry,
                    bottomInset = if (drawerState == DrawerState.CLOSED) 72.dp else bottomCardHeight + 16.dp,
                    recenterTrigger = recenterTrigger,
                    onPannedChanged = { isMapPanned = it },
                    onGuidance = { roadGuidance = it }
                ) }
            }

            // 2. FLOATING TOP NOTIFICATIONS (Stacked neatly inside the map area)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .zIndex(10f)
            ) {

            if (!hasNoBooking && drawerState != DrawerState.EXPANDED && parcel.status !in listOf(ParcelStatus.DELIVERED, ParcelStatus.CANCELLED, ParcelStatus.RETURNED)) {
                if (!isGuidanceDismissed) {
                    com.esdispatch.ui.maps.RouteGuidanceCard(
                        guidance = roadGuidance,
                        onRetry = { routeRetry++ },
                        onNavigate = if (isRider && parcel.status in listOf(ParcelStatus.ASSIGNED, ParcelStatus.ARRIVED_PICKUP,
                            ParcelStatus.PICKED_UP, ParcelStatus.TRANSIT, ParcelStatus.OUT_FOR_DELIVERY, ParcelStatus.RETURN_TO_SENDER)) ({ toggleInAppNavigation() }) else null,
                        isRider = isRider,
                        isVoiceMuted = isVoiceMuted,
                        onToggleVoice = if (isRider && isNavigating) ({ isVoiceMuted = com.esdispatch.util.VoiceGuidanceManager.toggleMuted() }) else null,
                        onOpenExternalMaps = if (isRider) ({ openExternalGoogleMaps() }) else null,
                        onDismiss = { isGuidanceDismissed = true },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Obsidian,
                        border = BorderStroke(1.dp, Gold),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 8.dp)
                            .clickable { isGuidanceDismissed = false }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Navigation,
                                contentDescription = null,
                                tint = Gold,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = roadGuidance.etaSeconds?.let { "${kotlin.math.ceil(it / 60).toInt()} min • View HUD" } ?: "View Navigation",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold
                            )
                        }
                    }
                }
            }

            // Customer proximity notification from fresh delivery telemetry only.
            androidx.compose.animation.AnimatedVisibility(
                visible = showInAppNotificationBanner && !isRider,
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

        // Keep the existing left-hand control position for navigation perspective.
        if (drawerState != DrawerState.EXPANDED) {
            Column(modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp).zIndex(5f),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(12.dp), color = Charcoal, modifier = Modifier.widthIn(max = 112.dp)) {
                    Text(weatherLabel, color = AppTextColor, fontSize = 11.sp, modifier = Modifier.padding(10.dp))
                }
                if (isRider && !hasNoBooking) {
                    MapControlButton(icon = Icons.Filled.Explore, description = "Toggle 3D navigation view", isActive = is3D) { is3D = !is3D }
                    if (parcel.status !in listOf(ParcelStatus.PENDING, ParcelStatus.QUEUED, ParcelStatus.OFFERED, ParcelStatus.RESERVED_NEXT, ParcelStatus.DELIVERED, ParcelStatus.CANCELLED, ParcelStatus.RETURNED)) {
                        MapControlButton(icon = Icons.Filled.Navigation, description = "Toggle in-app navigation", isActive = isNavigating) { toggleInAppNavigation() }
                    }
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
                if (isMapPanned) {
                    MapControlButton(
                        icon = if (isRider) Icons.Default.Navigation else Icons.Default.CenterFocusStrong,
                        description = if (isRider) "Resume navigation" else "Recenter Map",
                        isActive = true
                    ) {
                        recenterTrigger++
                        isMapPanned = false
                    }
                }
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
                    // LOWER PORTION: COLLAPSIBLE LUXURY DRAWER OR FLOATING CIRCLE
                    // --------------------------------------------------------------------------------------------
                    if (drawerState == DrawerState.CLOSED) {
                        // Floating 56dp luxury circle on bottom-left edge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .navigationBarsPadding()
                                .padding(start = 18.dp, bottom = 20.dp)
                                .zIndex(25f)
                        ) {
                            Surface(
                                onClick = { drawerState = DrawerState.COLLAPSED },
                                shape = CircleShape,
                                color = Gold,
                                shadowElevation = 10.dp,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (hasNoBooking) {
                                            if (isRider) Icons.Filled.DirectionsBike else Icons.Filled.LocalShipping
                                        } else {
                                            Icons.Filled.DirectionsBike
                                        },
                                        contentDescription = "Expand Delivery Drawer",
                                        tint = Obsidian,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Open Drawer Card (Collapsed or Expanded)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .height(bottomCardHeight)
                                .zIndex(20f)
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(drawerDragModifier),
                                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Obsidian else GoldenWhiteLight
                                ),
                                border = BorderStroke(1.5.dp, if (isDark) Gold else BorderLight),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            // Integrated top drag bar & minimize button
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Spacer(modifier = Modifier.size(32.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .width(42.dp)
                                                        .height(4.5.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isLight) Slate else Gold.copy(alpha = 0.6f))
                                                        .clickable { drawerState = DrawerState.CLOSED }
                                                )
                                                IconButton(
                                                    onClick = { drawerState = DrawerState.CLOSED },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.KeyboardArrowDown,
                                                        contentDescription = "Minimize drawer",
                                                        tint = if (isDark) Gold else Obsidian,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }

                                            // Drawer Scrollable Content
                                            Box(modifier = Modifier.fillMaxSize()) {
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
                                                                imageVector = Icons.Filled.LocalShipping,
                                                                contentDescription = null,
                                                                tint = Gold,
                                                                modifier = Modifier.size(30.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(14.dp))
                                                        Text(
                                                            text = "Express Fleet On Standby",
                                                            fontSize = 16.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = AppOnSurface
                                                        )
                                                        Text(
                                                            text = "No active deliveries in transit. Book an instant dispatch courier or browse verified shops in the marketplace.",
                                                            fontSize = 12.sp,
                                                            color = TextGray,
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                                                        )
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                        ) {
                                                            Button(
                                                                onClick = { onNavigate("SendParcel") },
                                                                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                                                                shape = RoundedCornerShape(14.dp),
                                                                modifier = Modifier.weight(1f).height(46.dp)
                                                            ) {
                                                                Text("BOOK DISPATCH", fontWeight = FontWeight.Black, fontSize = 12.sp)
                                                            }
                                                            OutlinedButton(
                                                                onClick = { onNavigate("Marketplace") },
                                                                colors = ButtonDefaults.outlinedButtonColors(
                                                                    contentColor = if (isDark) Gold else Obsidian
                                                                ),
                                                                border = BorderStroke(1.5.dp, Gold),
                                                                shape = RoundedCornerShape(14.dp),
                                                                modifier = Modifier.weight(1f).height(46.dp)
                                                            ) {
                                                                Text("MARKETPLACE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                            }
                                                        }
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
                                                            text = com.esdispatch.util.FormatUtils.formatDisplayTrackingId(parcel.id),
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
                                                            text = com.esdispatch.util.FormatUtils.formatDisplayTrackingId(parcel.id),
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
                                            val etaText = if (isRider) {
                                                when (parcel.status) {
                                                    ParcelStatus.DELIVERED -> "Completed"
                                                    ParcelStatus.CANCELLED -> "Cancelled"
                                                    ParcelStatus.PENDING, ParcelStatus.QUEUED -> "Pending Dispatch"
                                                    ParcelStatus.RESERVED_NEXT -> "Next delivery reserved"
                                                    ParcelStatus.ASSIGNED -> "Go to Pickup"
                                                    ParcelStatus.ARRIVED_PICKUP -> "At Sender Point"
                                                    ParcelStatus.PICKED_UP -> "Heading to Drop-off"
                                                    ParcelStatus.ARRIVED -> "At Drop-off Point"
                                                    ParcelStatus.HANDOVER_VERIFIED -> "Verifying Handover"
                                                    ParcelStatus.TRANSIT, ParcelStatus.OUT_FOR_DELIVERY -> {
                                                        roadGuidance.etaSeconds?.let { "About ${kotlin.math.ceil(it / 60).toInt().coerceAtLeast(1)} min" } ?: "ETA unavailable"
                                                    }
                                                    else -> "In Transit"
                                                }
                                            } else {
                                                when (parcel.status) {
                                                    ParcelStatus.DELIVERED -> "Delivered"
                                                    ParcelStatus.CANCELLED -> "Cancelled"
                                                    ParcelStatus.PENDING, ParcelStatus.QUEUED -> "Dispatching Order"
                                                    ParcelStatus.RESERVED_NEXT -> "Rider reserved"
                                                    ParcelStatus.ASSIGNED -> "Courier Assigned"
                                                    ParcelStatus.ARRIVED_PICKUP -> "Collecting Package"
                                                    ParcelStatus.PICKED_UP -> "En Route"
                                                    ParcelStatus.ARRIVED -> "Courier is Here"
                                                    ParcelStatus.HANDOVER_VERIFIED -> "Verifying Handover"
                                                    ParcelStatus.TRANSIT, ParcelStatus.OUT_FOR_DELIVERY -> {
                                                        roadGuidance.etaSeconds?.let { "About ${kotlin.math.ceil(it / 60).toInt().coerceAtLeast(1)} min" } ?: "ETA unavailable"
                                                    }
                                                    else -> "In Transit"
                                                }
                                            }

                                            val etaSubText = if (isRider) {
                                                when (parcel.status) {
                                                    ParcelStatus.DELIVERED -> "Payout credited"
                                                    ParcelStatus.CANCELLED -> "Order closed"
                                                    ParcelStatus.PENDING, ParcelStatus.QUEUED -> "Awaiting assignment"
                                                    ParcelStatus.RESERVED_NEXT -> "Finish your current delivery first"
                                                    ParcelStatus.ASSIGNED -> "Navigate to pickup address"
                                                    ParcelStatus.ARRIVED_PICKUP -> "Verify parcel & confirm pickup"
                                                    ParcelStatus.PICKED_UP -> "Follow route to delivery address"
                                                    ParcelStatus.ARRIVED -> "Request 4-digit PIN from receiver"
                                                    ParcelStatus.HANDOVER_VERIFIED -> "Capture delivery photo proof"
                                                    ParcelStatus.TRANSIT, ParcelStatus.OUT_FOR_DELIVERY -> {
                                                        realDistanceKm?.let { String.format(java.util.Locale.US, "%.1f km to drop-off", it) } ?: "Waiting for location update"
                                                    }
                                                    else -> "Active mission"
                                                }
                                            } else {
                                                when (parcel.status) {
                                                    ParcelStatus.DELIVERED -> "Handover complete • Thank you!"
                                                    ParcelStatus.CANCELLED -> "Trip cancelled"
                                                    ParcelStatus.PENDING, ParcelStatus.QUEUED -> "Matching with nearest courier..."
                                                    ParcelStatus.RESERVED_NEXT -> "Waiting for the rider’s current delivery"
                                                    ParcelStatus.ASSIGNED -> "Heading to pickup location"
                                                    ParcelStatus.ARRIVED_PICKUP -> "Courier at pickup point"
                                                    ParcelStatus.PICKED_UP -> "Package secured • On the way"
                                                    ParcelStatus.ARRIVED -> "Present 4-digit PIN to receive parcel"
                                                    ParcelStatus.HANDOVER_VERIFIED -> "Photo verification in progress"
                                                    ParcelStatus.TRANSIT, ParcelStatus.OUT_FOR_DELIVERY -> {
                                                        realDistanceKm?.let { String.format(java.util.Locale.US, "%.1f km remaining", it) } ?: "Waiting for location update"
                                                    }
                                                    else -> "On schedule"
                                                }
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

                                        // Dynamic Delivery Estimation Component (In Transit Only)
                                        if (parcel.status !in listOf(ParcelStatus.ARRIVED, ParcelStatus.HANDOVER_VERIFIED, ParcelStatus.DELIVERED, ParcelStatus.CANCELLED)) {
                                            DeliveryEstimationCard(
                                                status = parcel.status,
                                                progress = parcel.progress,
                                                isDark = isDark,
                                                isRider = isRider,
                                                routeEtaSeconds = roadGuidance.etaSeconds
                                            )
                                        }

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
                                                        imageVector = Icons.Filled.GpsFixed,
                                                        contentDescription = "Live Telemetry",
                                                        tint = Color(0xFF4CAF50),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Live dispatch GPS telemetry active",
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
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = Gold.copy(alpha = 0.15f),
                                                            border = BorderStroke(0.5.dp, Gold)
                                                        ) {
                                                            Text(
                                                                text = "CONFIDENTIAL HANDOVER PIN",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Black,
                                                                letterSpacing = 1.sp,
                                                                color = if (isDark) GoldLight else Obsidian,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                            )
                                                        }
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
                                                        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                                                        var pinCopied by remember { mutableStateOf(false) }
                                                        OutlinedButton(
                                                            onClick = {
                                                                if (parcel.otpCode.isNotBlank()) {
                                                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(parcel.otpCode))
                                                                    pinCopied = true
                                                                }
                                                            },
                                                            colors = ButtonDefaults.outlinedButtonColors(
                                                                contentColor = if (isDark) GoldLight else Obsidian
                                                            ),
                                                            border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.5f) else Obsidian.copy(alpha = 0.3f)),
                                                            shape = RoundedCornerShape(10.dp),
                                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                                            modifier = Modifier.height(34.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = if (pinCopied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                                                                contentDescription = "Copy PIN",
                                                                tint = if (isDark) GoldLight else Obsidian,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(
                                                                text = if (pinCopied) "PIN COPIED" else "COPY PIN",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Text(
                                                            text = "Confidential • Only release this PIN after physically inspecting and receiving your package.",
                                                            fontSize = 11.sp,
                                                            color = TextGray,
                                                            textAlign = TextAlign.Center
                                                        )
                                                        if (parcel.declaredValue > 0.0) {
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(
                                                                text = "Declared Value: ₦${String.format(java.util.Locale.US, "%,.0f", parcel.declaredValue)}",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = if (isDark) GoldLight.copy(alpha = 0.8f) else Obsidian.copy(alpha = 0.7f)
                                                            )
                                                        }
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
                                                    // Rider Milestone Progression Action Button
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    when (parcel.status) {
                                                        ParcelStatus.ASSIGNED -> {
                                                            Button(
                                                                onClick = {
                                                                    if (!isActionSubmitting) {
                                                                        isActionSubmitting = true
                                                                        com.esdispatch.util.SoundManager.playClick()
                                                                        viewModel.updateParcelStatusByRider(parcel.id, ParcelStatus.PICKED_UP, 0.40f) { success, _ ->
                                                                            isActionSubmitting = false
                                                                            if (success) Toast.makeText(context, "Pickup confirmed! Order is now picked up.", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                    }
                                                                },
                                                                enabled = !isActionSubmitting,
                                                                modifier = Modifier.fillMaxWidth().height(48.dp).tactilePress(scaleDown = 0.96f),
                                                                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                                                                shape = RoundedCornerShape(14.dp)
                                                            ) {
                                                                if (isActionSubmitting) {
                                                                    CircularProgressIndicator(strokeWidth = 2.5.dp, color = Obsidian, modifier = Modifier.size(20.dp))
                                                                } else {
                                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                                                        Icon(Icons.Filled.CheckCircle, null, tint = Obsidian, modifier = Modifier.size(18.dp))
                                                                        Spacer(modifier = Modifier.width(8.dp))
                                                                        Text("CONFIRM PICKUP", fontWeight = FontWeight.Black, fontSize = 13.sp)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        ParcelStatus.PICKED_UP -> {
                                                            Button(
                                                                onClick = {
                                                                    if (!isActionSubmitting) {
                                                                        isActionSubmitting = true
                                                                        com.esdispatch.util.SoundManager.playClick()
                                                                        viewModel.updateParcelStatusByRider(parcel.id, ParcelStatus.TRANSIT, 0.60f) { success, _ ->
                                                                            isActionSubmitting = false
                                                                            if (success) Toast.makeText(context, "In transit to destination! Customer notified.", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                    }
                                                                },
                                                                enabled = !isActionSubmitting,
                                                                modifier = Modifier.fillMaxWidth().height(48.dp).tactilePress(scaleDown = 0.96f),
                                                                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                                                                shape = RoundedCornerShape(14.dp)
                                                            ) {
                                                                if (isActionSubmitting) {
                                                                    CircularProgressIndicator(strokeWidth = 2.5.dp, color = Obsidian, modifier = Modifier.size(20.dp))
                                                                } else {
                                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                                                        Icon(Icons.Filled.DirectionsBike, null, tint = Obsidian, modifier = Modifier.size(18.dp))
                                                                        Spacer(modifier = Modifier.width(8.dp))
                                                                        Text("START TRANSIT", fontWeight = FontWeight.Black, fontSize = 13.sp)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        ParcelStatus.TRANSIT, ParcelStatus.OUT_FOR_DELIVERY -> {
                                                            Button(
                                                                onClick = {
                                                                    if (!isActionSubmitting) {
                                                                        isActionSubmitting = true
                                                                        com.esdispatch.util.SoundManager.playClick()
                                                                        viewModel.updateParcelStatusByRider(parcel.id, ParcelStatus.ARRIVED, 0.90f) { success, _ ->
                                                                            isActionSubmitting = false
                                                                            if (success) Toast.makeText(context, "Marked as arrived! Recipient notified.", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                    }
                                                                },
                                                                enabled = !isActionSubmitting,
                                                                modifier = Modifier.fillMaxWidth().height(48.dp).tactilePress(scaleDown = 0.96f),
                                                                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                                                                shape = RoundedCornerShape(14.dp)
                                                            ) {
                                                                if (isActionSubmitting) {
                                                                    CircularProgressIndicator(strokeWidth = 2.5.dp, color = Obsidian, modifier = Modifier.size(20.dp))
                                                                } else {
                                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                        Icon(Icons.Filled.LocationOn, null, tint = Obsidian, modifier = Modifier.size(18.dp))
                                                                        Text("MARK ARRIVED AT DESTINATION", fontWeight = FontWeight.Black, fontSize = 13.sp)
                                                                    }
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
                                                    if (parcel.status == ParcelStatus.DELIVERED) {
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
                                                    } else {
                                                        // Active delivery: Share Tracking Link only (Feedback & Tip hidden until delivery is complete)
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
                                                            modifier = Modifier.fillMaxWidth().height(40.dp).tactilePress(scaleDown = 0.94f),
                                                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Obsidian else Gold),
                                                            shape = RoundedCornerShape(12.dp),
                                                            border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.3f) else Obsidian.copy(alpha = 0.3f))
                                                        ) {
                                                            Text("Share Tracking Link", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Gold else Obsidian)
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
                                                        
                                                         val timeFormat = remember { java.text.SimpleDateFormat("h:mm a", java.util.Locale.US) }
                                                         val timestampText = when (index) {
                                                             0 -> if (parcel.createdAt > 0L) timeFormat.format(java.util.Date(parcel.createdAt)) else "--:--"
                                                             1 -> if (timelineSteps[1].third && parcel.createdAt > 0L) timeFormat.format(java.util.Date(parcel.createdAt)) else "--:--"
                                                             2 -> if (timelineSteps[2].third) {
                                                                 val ts = if (parcel.transitTimestamp > 0L) parcel.transitTimestamp else parcel.pickupTimestamp
                                                                 if (ts > 0L) timeFormat.format(java.util.Date(ts)) else "--:--"
                                                             } else "--:--"
                                                             3 -> if (timelineSteps[3].third) {
                                                                 val ts = if (parcel.estimatedArrivalAt > 0L) parcel.estimatedArrivalAt else parcel.deliveryTimestamp
                                                                 if (ts > 0L) timeFormat.format(java.util.Date(ts)) else "--:--"
                                                             } else "--:--"
                                                             4 -> if (timelineSteps[4].third && parcel.deliveryTimestamp > 0L) timeFormat.format(java.util.Date(parcel.deliveryTimestamp)) else "--:--"
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
                                                    Column(modifier = Modifier.weight(1f, fill = false)) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Text(
                                                                text = contactName,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                fontSize = 15.sp,
                                                                color = Color.White,
                                                                maxLines = 1,
                                                                modifier = Modifier
                                                                    .weight(1f, fill = false)
                                                                    .basicMarquee()
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
                                                        Column(modifier = Modifier.weight(1f, fill = false)) {
                                                            Text(
                                                                text = resolvedCourierName,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                fontSize = 16.sp,
                                                                color = Color.White,
                                                                maxLines = 1,
                                                                modifier = Modifier.basicMarquee()
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
                                                                if (cleanPhone.isBlank()) {
                                                                    Toast.makeText(context, "Courier contact details are not available yet.", Toast.LENGTH_SHORT).show()
                                                                } else {
                                                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone"))
                                                                    try {
                                                                        context.startActivity(dialIntent)
                                                                    } catch (e: Exception) {
                                                                        Toast.makeText(context, "Call not supported on this device", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                }
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Filled.Call, "Call rider", tint = Obsidian, modifier = Modifier.size(18.dp))
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
            .size(44.dp)
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

    // 3. Reliable central Benin City fallback (Ring Road / King's Square)
    return Pair(6.3350, 5.6037)
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
    courierBearing: Float = 0f,
    userAvatar: String = "",
    hasNoBooking: Boolean = false,
    followUser: Boolean = true,
    userCoords: Pair<Double, Double>? = null,
    isRider: Boolean = false,
    parcelStatus: ParcelStatus = ParcelStatus.PENDING,
    parcelPickupLat: Double? = null,
    parcelPickupLng: Double? = null,
    parcelDeliveryLat: Double? = null,
    parcelDeliveryLng: Double? = null,
    courierName: String = "Courier",
    courierPhone: String = "",
    isDarkTheme: Boolean = false,
    onRouteTelemetry: (Double, Double) -> Unit = { _, _ -> },
    is3D: Boolean = false,
    courierLastUpdated: Long = 0L,
    routeRetry: Int = 0,
    bottomInset: androidx.compose.ui.unit.Dp = 120.dp,
    recenterTrigger: Int = 0,
    onPannedChanged: (Boolean) -> Unit = {},
    onGuidance: (com.esdispatch.ui.maps.RouteGuidance) -> Unit = {}
) {
    val context = LocalContext.current

    com.esdispatch.ui.maps.NativeGoogleMapView(
        modifier = modifier,
        pickupAddress = pickupAddress,
        deliveryAddress = deliveryAddress,
        progress = progress,
        isSatellite = isSatellite,
        showTraffic = showTraffic,
        isDarkTheme = isDarkTheme,
        courierLatitude = courierLatitude,
        courierLongitude = courierLongitude,
        courierBearing = courierBearing,
        hasNoBooking = hasNoBooking,
        userCoords = userCoords,
        isRider = isRider,
        parcelStatus = parcelStatus,
        parcelPickupLat = parcelPickupLat,
        parcelPickupLng = parcelPickupLng,
        parcelDeliveryLat = parcelDeliveryLat,
        parcelDeliveryLng = parcelDeliveryLng,
        zoom = zoom,
        followUser = followUser,
        is3D = is3D,
        courierLastUpdated = courierLastUpdated,
        routeRetry = routeRetry,
        bottomInset = bottomInset,
        recenterTrigger = recenterTrigger,
        onPannedChanged = onPannedChanged,
        onGuidance = { guidance ->
            onGuidance(guidance)
            if (guidance.distanceMeters != null && guidance.etaSeconds != null) onRouteTelemetry(guidance.distanceMeters, guidance.etaSeconds)
        },
        onMapClick = { latLng ->
            if (hasNoBooking) {
                reverseGeocodeAddress(context, latLng.latitude, latLng.longitude) { address ->
                    com.esdispatch.util.CustomToastBridge.show("Location Selected: $address", com.esdispatch.viewmodel.ToastType.SUCCESS)
                }
            }
        }
    )
}

@Composable
fun DeliveryEstimationCard(
    status: ParcelStatus,
    progress: Float,
    isDark: Boolean,
    isRider: Boolean = false,
    routeEtaSeconds: Double? = null
) {
    val isLight = !isDark
    val now = remember { java.util.Date() }
    val dayFormat = remember { java.text.SimpleDateFormat("EEEE, MMMM d, yyyy", java.util.Locale.getDefault()) }
    val timeFormat = remember { java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()) }
    val todayStr = remember(now) { "Today, ${dayFormat.format(now)}" }
    val currentTimeStr = remember(now) { timeFormat.format(now) }

    val estimationText = when (status) {
        ParcelStatus.PENDING, ParcelStatus.QUEUED -> if (isRider) "Available for pickup" else "Waiting for rider"
        ParcelStatus.RESERVED_NEXT -> "Rider reserved"
        ParcelStatus.ASSIGNED -> routeEtaSeconds?.let { "${kotlin.math.ceil(it / 60).toInt().coerceAtLeast(1)} min to pickup" } ?: if (isRider) "Navigate to pickup" else "Rider assigned"
        ParcelStatus.ARRIVED_PICKUP -> if (isRider) "At pickup location" else "Preparing for pickup"
        ParcelStatus.PICKED_UP -> if (isRider) "Heading to drop-off" else "In transit"
        ParcelStatus.TRANSIT, ParcelStatus.OUT_FOR_DELIVERY -> {
            routeEtaSeconds?.let { "${kotlin.math.ceil(it / 60).toInt().coerceAtLeast(1)} min to delivery" }
                ?: "Waiting for an updated arrival estimate"
        }
        ParcelStatus.ARRIVED -> if (isRider) "At delivery location" else "Courier has arrived!"
        ParcelStatus.HANDOVER_VERIFIED -> if (isRider) "Take delivery photo" else "Verifying delivery"
        ParcelStatus.DELIVERED -> "Delivered"
        ParcelStatus.CANCELLED -> "Cancelled"
        else -> if (isRider) "Active Delivery" else "Active Dispatch"
    }

    val windowText = when (status) {
        ParcelStatus.PENDING, ParcelStatus.QUEUED -> if (isRider) "Order ready in pool" else "Dispatching order..."
        ParcelStatus.RESERVED_NEXT -> "Waiting for the rider to finish their current delivery"
        ParcelStatus.ASSIGNED -> if (isRider) "Head to sender address" else "Heading to pickup"
        ParcelStatus.ARRIVED_PICKUP -> if (isRider) "Collect package & confirm pickup" else "Courier at pickup location"
        ParcelStatus.PICKED_UP -> if (isRider) "Package collected • Proceed to drop-off" else "Package collected • On route"
        ParcelStatus.TRANSIT, ParcelStatus.OUT_FOR_DELIVERY -> if (isRider) "Navigate along highlighted route" else "Heading to delivery"
        ParcelStatus.ARRIVED -> if (isRider) "Recipient PIN required for handover" else "Courier arrived at destination"
        ParcelStatus.HANDOVER_VERIFIED -> if (isRider) "Snap photo to finalize delivery" else "Photo proof in progress"
        ParcelStatus.DELIVERED -> "Delivery completed"
        ParcelStatus.CANCELLED -> "Order was cancelled"
        else -> "Active delivery"
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
