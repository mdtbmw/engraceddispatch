package com.esdispatch.ui.maps

import android.animation.ValueAnimator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.esdispatch.data.ParcelStatus
import com.esdispatch.ui.theme.Gold
import com.esdispatch.ui.theme.Obsidian
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/** Road geometry, location and camera remain separate: camera gestures never interrupt GPS. */
@Composable
fun NativeGoogleMapView(
    modifier: Modifier = Modifier,
    pickupAddress: String = "",
    deliveryAddress: String = "",
    progress: Float = 0f,
    isSatellite: Boolean = false,
    showTraffic: Boolean = true,
    isDarkTheme: Boolean = false,
    courierLatitude: Double? = null,
    courierLongitude: Double? = null,
    courierBearing: Float = 0f,
    hasNoBooking: Boolean = false,
    userCoords: Pair<Double, Double>? = null,
    isRider: Boolean = false,
    parcelStatus: ParcelStatus = ParcelStatus.PENDING,
    parcelPickupLat: Double? = null,
    parcelPickupLng: Double? = null,
    parcelDeliveryLat: Double? = null,
    parcelDeliveryLng: Double? = null,
    routePoints: List<LatLng> = emptyList(),
    zoom: Float = 14.5f,
    followUser: Boolean = false,
    is3D: Boolean = false,
    courierLastUpdated: Long = 0L,
    routeRetry: Int = 0,
    bottomInset: Dp = 120.dp,
    recenterTrigger: Int = 0,
    onPannedChanged: (Boolean) -> Unit = {},
    onGuidance: (RouteGuidance) -> Unit = {},
    onMapClick: ((LatLng) -> Unit)? = null
) {
    val context = LocalContext.current
    val reducedMotion = remember {
        if (android.os.Build.VERSION.SDK_INT >= 26) !ValueAnimator.areAnimatorsEnabled()
        else android.provider.Settings.Global.getFloat(context.contentResolver,
            android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    val pickup = remember(parcelPickupLat, parcelPickupLng, pickupAddress) {
        RoadWaypoint(validMapPosition(parcelPickupLat, parcelPickupLng), pickupAddress)
    }
    val delivery = remember(parcelDeliveryLat, parcelDeliveryLng, deliveryAddress) {
        RoadWaypoint(validMapPosition(parcelDeliveryLat, parcelDeliveryLng), deliveryAddress)
    }
    val phase = when {
        hasNoBooking -> "none"
        parcelStatus in listOf(ParcelStatus.DELIVERED, ParcelStatus.CANCELLED, ParcelStatus.RETURNED) -> "none"
        parcelStatus in listOf(ParcelStatus.ASSIGNED, ParcelStatus.ARRIVED_PICKUP) -> "pickup"
        parcelStatus == ParcelStatus.RETURN_TO_SENDER -> "return"
        parcelStatus in listOf(ParcelStatus.PICKED_UP, ParcelStatus.TRANSIT, ParcelStatus.OUT_FOR_DELIVERY,
            ParcelStatus.ARRIVED, ParcelStatus.HANDOVER_VERIFIED, ParcelStatus.RECIPIENT_UNAVAILABLE,
            ParcelStatus.FAILED_DELIVERY) -> "delivery"
        else -> "planned"
    }
    val courier = validMapPosition(courierLatitude, courierLongitude)
        .takeIf { phase in listOf("pickup", "delivery", "return") }
    val user = userCoords?.let { validMapPosition(it.first, it.second) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(courierLastUpdated) {
        while (isActive) {
            now = System.currentTimeMillis()
            delay(5000)
        }
    }
    val fresh = courier != null && courierLastUpdated > 0 && now - courierLastUpdated in -5000L..60000L
    val road = rememberRoadRoute(context, pickup, delivery, phase, courier, fresh, routeRetry)
    val activeLeg = road.route?.legs?.firstOrNull()
    val nextLeg = road.route?.legs?.getOrNull(1)
    val pickupPosition = pickup.position ?: when (phase) {
        "pickup" -> activeLeg?.points?.lastOrNull()
        "planned" -> activeLeg?.points?.firstOrNull()
        "return" -> activeLeg?.points?.lastOrNull()
        else -> null
    }
    val deliveryPosition = delivery.position ?: when (phase) {
        "pickup" -> nextLeg?.points?.lastOrNull()
        "return" -> null
        else -> activeLeg?.points?.lastOrNull()
    }
    val targetName = if (phase == "pickup" || phase == "return") "pickup" else "delivery"
    val missingAddress = when (phase) {
        "delivery" -> !delivery.available
        "return" -> !pickup.available
        else -> !pickup.available || !delivery.available
    }
    val atStop = parcelStatus in listOf(ParcelStatus.ARRIVED_PICKUP, ParcelStatus.ARRIVED, ParcelStatus.HANDOVER_VERIFIED)
    val stale = courier != null && !fresh
    val guidance = when {
        phase == "none" -> RouteGuidance(title = "Delivery complete", detail = "")
        missingAddress -> RouteGuidance("Address needed", "Contact support to confirm the pickup and delivery locations.")
        phase != "planned" && courier == null -> RouteGuidance("Waiting for location",
            if (isRider) "Enable location services to get directions." else "The rider’s location will appear when an update arrives.")
        stale -> RouteGuidance("Location update delayed", if (courierLastUpdated > 0)
            "Last update ${((now - courierLastUpdated) / 60000).coerceAtLeast(1)} min ago. Showing the last known position."
            else "Waiting for a fresh location update. Showing the last known position.")
        road.failed -> RouteGuidance("Directions unavailable", "Check your connection and retry, or open navigation.", canRetry = true)
        activeLeg == null -> RouteGuidance(loading = true)
        else -> RouteGuidance(
            title = when {
                phase == "planned" -> "Planned delivery route"
                parcelStatus == ParcelStatus.ARRIVED_PICKUP -> "At pickup"
                parcelStatus in listOf(ParcelStatus.ARRIVED, ParcelStatus.HANDOVER_VERIFIED) -> "At delivery"
                isRider -> "Directions to $targetName"
                phase == "return" -> "Rider returning to sender"
                else -> "Rider heading to $targetName"
            },
            detail = when {
                parcelStatus == ParcelStatus.ARRIVED_PICKUP -> if (isRider) "Collect the parcel and confirm pickup before leaving." else "Your rider is collecting the parcel."
                parcelStatus in listOf(ParcelStatus.ARRIVED, ParcelStatus.HANDOVER_VERIFIED) -> if (isRider) "Complete the handover with the recipient." else "Your rider is at the delivery address."
                isRider && phase != "planned" -> activeLeg.nextInstruction(courier)
                phase == "planned" -> "Live tracking starts when your rider begins this delivery."
                road.loading -> "Updating the road route…"
                else -> "Following the rider’s latest location"
            },
            etaSeconds = activeLeg.seconds.takeIf { fresh && phase != "planned" && !atStop },
            distanceMeters = activeLeg.meters,
            loading = road.loading,
            congested = activeLeg.congested
        )
    }.copy(destination = if (targetName == "pickup") pickupPosition else deliveryPosition)
    val latestGuidance by rememberUpdatedState(onGuidance)
    LaunchedEffect(guidance) { latestGuidance(guidance) }

    val defaultCenter = remember { LatLng(6.3350, 5.6037) }
    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(courier ?: pickupPosition ?: user ?: defaultCenter, zoom)
    }
    var loaded by remember { mutableStateOf(false) }
    var previousZoom by remember { mutableFloatStateOf(zoom) }
    var framed by remember { mutableStateOf(false) }
    var panned by remember { mutableStateOf(false) }
    LaunchedEffect(panned) {
        onPannedChanged(panned)
    }
    LaunchedEffect(recenterTrigger) {
        if (recenterTrigger > 0) {
            panned = false
        }
    }
    LaunchedEffect(camera.isMoving) {
        if (camera.isMoving && camera.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) panned = true
    }
    LaunchedEffect(followUser, is3D) { panned = false }
    // Depend on received fixes, never on each animation frame. Customer overview keeps the destination visible.
    LaunchedEffect(loaded, courier, user.takeIf { followUser || hasNoBooking }, road.route, followUser, is3D, recenterTrigger) {
        if (!loaded || panned) return@LaunchedEffect
        val target = if (followUser || hasNoBooking) user ?: courier else courier ?: pickupPosition ?: user
        val boundsPoints = (activeLeg?.points.orEmpty() + nextLeg?.points.orEmpty() + listOfNotNull(courier, pickupPosition, deliveryPosition))
        val update = if (!isRider && !followUser && !hasNoBooking && boundsPoints.distinct().size > 1) {
            CameraUpdateFactory.newLatLngBounds(LatLngBounds.builder().also { b -> boundsPoints.forEach { b.include(it) } }.build(), 70)
        } else if (hasNoBooking && user != null) {
            CameraUpdateFactory.newCameraPosition(CameraPosition.Builder()
                .target(user)
                .zoom(15.5f)
                .tilt(if (is3D) 45f else 0f)
                .bearing(0f)
                .build())
        } else if (isRider && courier != null) {
            // Smarter rider camera: position slightly below center so more road ahead is visible
            // Dynamic zoom: Zoom in near upcoming turns (< 80m), zoom out on straightaways
            val nextTurnMeters = activeLeg?.distanceToNextTurn(courier) ?: 500
            val targetZoom = when {
                nextTurnMeters < 80 -> 17.2f
                nextTurnMeters < 180 -> 16.5f
                else -> 15.8f
            }
            // Project camera target slightly ahead along bearing so rider is in the lower 35% of viewport
            val lookAheadMeters = 35.0
            val targetCoord = if (courierBearing != 0f) {
                SphericalUtil.computeOffset(courier, lookAheadMeters, courierBearing.toDouble())
            } else courier
            CameraUpdateFactory.newCameraPosition(CameraPosition.Builder()
                .target(targetCoord)
                .zoom(if (!framed) targetZoom else camera.position.zoom.coerceIn(14f, 18.5f))
                .tilt(if (is3D) 55f else 0f)
                .bearing(if (is3D) courierBearing else 0f)
                .build())
        } else {
            CameraUpdateFactory.newCameraPosition(CameraPosition.Builder()
                .target(target ?: defaultCenter).zoom(if (!framed && isRider && courier != null) 16.5f else if (hasNoBooking && user != null) 15.5f else camera.position.zoom)
                .tilt(if (is3D) 50f else 0f).bearing(if (isRider && is3D) courierBearing else 0f).build())
        }
        framed = true
        if (reducedMotion) camera.move(update) else camera.animate(update, 800)
    }
    LaunchedEffect(zoom) {
        val delta = zoom - previousZoom
        previousZoom = zoom
        if (loaded && delta != 0f) {
            panned = true
            val update = CameraUpdateFactory.zoomBy(delta)
            if (reducedMotion) camera.move(update) else camera.animate(update, 250)
        }
    }
    val smooth = rememberSmoothCourierState(courier?.latitude, courier?.longitude, courierBearing,
        animate = fresh && !reducedMotion, roadPoints = activeLeg?.points.orEmpty())
    val courierState = remember { MarkerState() }
    val pickupState = remember { MarkerState() }
    val deliveryState = remember { MarkerState() }
    SideEffect {
        smooth.currentPosition?.let { courierState.position = it }
        pickupPosition?.let { pickupState.position = it }
        deliveryPosition?.let { deliveryState.position = it }
    }
    var courierIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var pickupIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var deliveryIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var userIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    val userState = remember { MarkerState() }
    LaunchedEffect(Unit) {
        MapsInitializer.initialize(context)
        courierIcon = MapMarkerFactory.getCourierMarkerIcon(context)
        pickupIcon = MapMarkerFactory.getPickupMarkerIcon(context)
        deliveryIcon = MapMarkerFactory.getDeliveryMarkerIcon(context)
        userIcon = MapMarkerFactory.getUserLocationMarkerIcon(context)
    }
    SideEffect {
        user?.let { userState.position = it }
    }
    val pulse = rememberInfiniteTransition(label = "Live location")
    val pulseAlpha by pulse.animateFloat(0.20f, 0.05f,
        infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "Live beacon")
    BoxWithConstraints(modifier) {
        val visibleBottomInset = bottomInset.coerceAtMost(maxHeight * 0.55f)
        GoogleMap(
            modifier = Modifier.fillMaxSize(), cameraPositionState = camera,
            properties = MapProperties(
                mapType = if (isSatellite) MapType.HYBRID else MapType.NORMAL,
                isTrafficEnabled = showTraffic,
                isBuildingEnabled = true,
                mapStyleOptions = if (isSatellite) null else if (isDarkTheme) BoltMapStyle.Dark else null
            ),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false,
                compassEnabled = true, mapToolbarEnabled = false),
            contentPadding = PaddingValues(top = if (hasNoBooking) 16.dp else 150.dp.coerceAtMost(maxHeight * 0.25f), bottom = visibleBottomInset),
            onMapLoaded = { loaded = true },
            onMapClick = { if (hasNoBooking) onMapClick?.invoke(it) }
        ) {
            if (!hasNoBooking) {
                pickupPosition?.let { Marker(state = pickupState, title = "Pickup", snippet = pickupAddress,
                    icon = pickupIcon, anchor = Offset(0.5f, 0.5f), zIndex = 3f) }
                deliveryPosition?.let { Marker(state = deliveryState, title = "Delivery", snippet = deliveryAddress,
                    icon = deliveryIcon, anchor = Offset(0.5f, 0.5f), zIndex = 3f) }
            }
            if (courier != null && smooth.currentPosition != null) {
                if (fresh) Circle(center = courierState.position, radius = 12.0,
                    fillColor = Gold.copy(alpha = if (reducedMotion) 0.12f else pulseAlpha), strokeWidth = 0f)
                Marker(state = courierState, title = if (isRider) "Your location" else "Rider",
                    snippet = if (fresh) "Latest location" else "Last known location",
                    icon = courierIcon, rotation = smooth.currentBearing, flat = true,
                    anchor = Offset(0.5f, 0.5f), zIndex = 5f, alpha = if (fresh) 1f else 0.6f)
            }
            if (user != null) {
                Circle(
                    center = user,
                    radius = 24.0,
                    fillColor = androidx.compose.ui.graphics.Color(0x261A73E8),
                    strokeColor = androidx.compose.ui.graphics.Color(0x661A73E8),
                    strokeWidth = 1.5f
                )
                Marker(
                    state = userState,
                    title = "Your Location",
                    snippet = "You are here",
                    icon = userIcon,
                    anchor = Offset(0.5f, 0.5f),
                    zIndex = 4f
                )
            }
            val geometry = activeLeg?.points ?: routePoints
            if (geometry.size >= 2 && !hasNoBooking) {
                nextLeg?.let { Polyline(points = it.points, color = if (isDarkTheme)
                    androidx.compose.ui.graphics.Color.LightGray else androidx.compose.ui.graphics.Color.DarkGray,
                    width = 6f, pattern = listOf(Dash(18f), Gap(10f)), zIndex = 0f) }

                // Split active route into traveled (greyed-out) and remaining (crisp Gold)
                val courierPos = smooth.currentPosition ?: courier
                val split = splitRouteAtCourier(geometry, courierPos)

                // Traveled section (greyed-out trail showing how far the rider has traveled)
                if (split.traveledPoints.size >= 2) {
                    Polyline(
                        points = split.traveledPoints,
                        color = androidx.compose.ui.graphics.Color(0xFF71717A).copy(alpha = 0.55f),
                        width = 6.5f,
                        jointType = JointType.ROUND,
                        startCap = RoundCap(),
                        endCap = RoundCap(),
                        zIndex = 1f
                    )
                }

                // Remaining section (crisp brand Gold + Obsidian casing connected directly to rider)
                val remaining = if (split.remainingPoints.size >= 2) split.remainingPoints else geometry
                Polyline(
                    points = remaining,
                    color = Obsidian.copy(alpha = 0.90f),
                    width = 13.5f,
                    jointType = JointType.ROUND,
                    startCap = RoundCap(),
                    endCap = RoundCap(),
                    zIndex = 2f
                )
                Polyline(
                    points = remaining,
                    color = Gold.copy(alpha = if (stale) 0.5f else 1f),
                    width = 8f,
                    jointType = JointType.ROUND,
                    startCap = RoundCap(),
                    endCap = RoundCap(),
                    zIndex = 3f
                )
            }
        }
    }
}
