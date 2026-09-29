package com.esdispatch.ui.maps

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.esdispatch.data.ParcelStatus
import com.esdispatch.ui.theme.Gold
import com.esdispatch.ui.theme.Obsidian
import com.esdispatch.utils.GeocoderUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

/**
 * Bolt-Grade Native Hardware-Accelerated Google Map View.
 * 
 * Replaces the legacy embedded WebView + Leaflet.js raster tiles with a 60-120 FPS
 * native vector map pipeline featuring:
 * - Minimalist clean styling (zero commercial/tourist POI clutter).
 * - Smooth bearing rotation & dead reckoning interpolation for couriers.
 * - Double-cased polyline with Gold core and dark casing.
 * - Pulsing radar beacon around active couriers.
 * - Intelligent camera framing and decouple-on-pan with floating Recenter action.
 */
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
    onMapClick: ((LatLng) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Initialize Google Maps context & usage attribution
    LaunchedEffect(Unit) {
        try {
            MapsInitializer.initialize(context)
            // Add internal usage attribution per Google Maps Platform guidelines
            try {
                val settingsClass = Class.forName("com.google.android.gms.maps.MapsApiSettings")
                val method = settingsClass.getMethod("addInternalUsageAttributionId", Context::class.java, String::class.java)
                method.invoke(null, context, "gmp_git_agentskills_v1")
            } catch (_: Throwable) {}
        } catch (e: Exception) {
            android.util.Log.w("NativeGoogleMapView", "MapsInitializer error: ${e.message}")
        }
    }

    // Resolve Pickup Coordinates
    val pickupLatLng by remember(pickupAddress, parcelPickupLat, parcelPickupLng, hasNoBooking) {
        derivedStateOf {
            if (hasNoBooking) null
            else if (parcelPickupLat != null && parcelPickupLng != null && parcelPickupLat != 0.0 && parcelPickupLng != 0.0) {
                LatLng(parcelPickupLat, parcelPickupLng)
            } else if (pickupAddress.isNotBlank()) {
                val coords = com.esdispatch.data.AddressDatabase.getCoordinates(pickupAddress)
                coords?.let { LatLng(it.first, it.second) }
            } else null
        }
    }

    // Resolve Delivery Coordinates
    val deliveryLatLng by remember(deliveryAddress, parcelDeliveryLat, parcelDeliveryLng, hasNoBooking) {
        derivedStateOf {
            if (hasNoBooking) null
            else if (parcelDeliveryLat != null && parcelDeliveryLng != null && parcelDeliveryLat != 0.0 && parcelDeliveryLng != 0.0) {
                LatLng(parcelDeliveryLat, parcelDeliveryLng)
            } else if (deliveryAddress.isNotBlank()) {
                val coords = com.esdispatch.data.AddressDatabase.getCoordinates(deliveryAddress)
                coords?.let { LatLng(it.first, it.second) }
            } else null
        }
    }

    // Default Benin City Center: King's Square / Ring Road
    val defaultCenter = remember { LatLng(6.3350, 5.6037) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultCenter, 14f)
    }

    var userHasPanned by remember { mutableStateOf(false) }

    // Detect user pan/drag interaction to decouple auto-follow
    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving && cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
            userHasPanned = true
        }
    }

    // Dynamic map properties: Bolt clean styling & traffic
    val mapProperties by remember(isSatellite, showTraffic, isDarkTheme) {
        derivedStateOf {
            MapProperties(
                mapType = if (isSatellite) MapType.HYBRID else MapType.NORMAL,
                isTrafficEnabled = showTraffic,
                mapStyleOptions = if (isSatellite) null else (if (isDarkTheme) BoltMapStyle.Dark else BoltMapStyle.Light),
                isMyLocationEnabled = false
            )
        }
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            myLocationButtonEnabled = false,
            rotationGesturesEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = true,
            zoomGesturesEnabled = true
        )
    }

    // Smooth courier animation state
    val smoothCourierState = rememberSmoothCourierState(
        targetLat = courierLatitude,
        targetLng = courierLongitude,
        targetBearing = courierBearing
    )

    // Animated Breathing Radar Pulse
    val infiniteTransition = rememberInfiniteTransition(label = "RadarBeacon")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = 65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    // Marker icons - safely resolved once Google Maps engine is initialized
    var courierIcon by remember { mutableStateOf<com.google.android.gms.maps.model.BitmapDescriptor?>(null) }
    var pickupIcon by remember { mutableStateOf<com.google.android.gms.maps.model.BitmapDescriptor?>(null) }
    var deliveryIcon by remember { mutableStateOf<com.google.android.gms.maps.model.BitmapDescriptor?>(null) }

    LaunchedEffect(Unit) {
        try {
            MapsInitializer.initialize(context)
            courierIcon = MapMarkerFactory.getCourierMarkerIcon(context)
            pickupIcon = MapMarkerFactory.getPickupMarkerIcon(context)
            deliveryIcon = MapMarkerFactory.getDeliveryMarkerIcon(context)
        } catch (t: Throwable) {
            android.util.Log.w("NativeGoogleMapView", "Marker icon init deferred: ${t.message}")
        }
    }

    // Auto-frame initial camera bounds
    LaunchedEffect(pickupLatLng, deliveryLatLng, smoothCourierState.currentPosition, hasNoBooking) {
        if (userHasPanned) return@LaunchedEffect

        val courierPos = smoothCourierState.currentPosition
        if (courierPos != null) {
            // When courier is active, center on courier with smooth zoom
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(courierPos, 15.5f),
                1000
            )
        } else if (pickupLatLng != null && deliveryLatLng != null) {
            // Fit both pickup and delivery bounds
            val builder = LatLngBounds.builder()
            builder.include(pickupLatLng!!)
            builder.include(deliveryLatLng!!)
            try {
                val bounds = builder.build()
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, 120),
                    1000
                )
            } catch (_: Exception) {}
        } else if (pickupLatLng != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(pickupLatLng!!, 15f),
                1000
            )
        } else if (userCoords != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(userCoords.first, userCoords.second), 15f),
                1000
            )
        }
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings,
            onMapLoaded = {
                try {
                    if (courierIcon == null) courierIcon = MapMarkerFactory.getCourierMarkerIcon(context)
                    if (pickupIcon == null) pickupIcon = MapMarkerFactory.getPickupMarkerIcon(context)
                    if (deliveryIcon == null) deliveryIcon = MapMarkerFactory.getDeliveryMarkerIcon(context)
                } catch (_: Throwable) {}
            },
            onMapClick = { latLng ->
                if (hasNoBooking && onMapClick != null) {
                    onMapClick(latLng)
                }
            }
        ) {
            // 1. Pickup Pin (Origin)
            pickupLatLng?.let { pPos ->
                Marker(
                    state = rememberMarkerState(position = pPos),
                    title = "Pickup",
                    snippet = pickupAddress.ifBlank { "Pickup Location" },
                    icon = pickupIcon,
                    anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f)
                )
            }

            // 2. Delivery Pin (Destination)
            deliveryLatLng?.let { dPos ->
                Marker(
                    state = rememberMarkerState(position = dPos),
                    title = "Destination",
                    snippet = deliveryAddress.ifBlank { "Delivery Location" },
                    icon = deliveryIcon,
                    anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f)
                )
            }

            // 3. Active Vehicle / Courier Marker
            val activeCourierPos = smoothCourierState.currentPosition
            if (activeCourierPos != null) {
                // Pulsing Radar Beacon
                Circle(
                    center = activeCourierPos,
                    radius = pulseRadius.toDouble(),
                    fillColor = Gold.copy(alpha = pulseAlpha),
                    strokeColor = Gold.copy(alpha = pulseAlpha * 1.5f),
                    strokeWidth = 2f
                )

                // High-precision vehicle marker with bearing rotation
                Marker(
                    state = rememberMarkerState(position = activeCourierPos),
                    title = if (isRider) "My Location" else "Courier",
                    snippet = if (isRider) "Active Delivery Navigation" else "Courier in transit",
                    icon = courierIcon,
                    rotation = smoothCourierState.currentBearing,
                    anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                    flat = true
                )
            }

            // 4. Double-Cased Route Polyline
            val activeRoute = when {
                routePoints.isNotEmpty() -> routePoints
                pickupLatLng != null && deliveryLatLng != null -> listOf(pickupLatLng!!, deliveryLatLng!!)
                else -> emptyList()
            }

            if (activeRoute.size >= 2) {
                // Outer Casing (Deep contrast border)
                Polyline(
                    points = activeRoute,
                    color = Obsidian.copy(alpha = 0.75f),
                    width = 16f,
                    jointType = JointType.ROUND,
                    startCap = RoundCap(),
                    endCap = RoundCap(),
                    zIndex = 1f
                )

                // Inner Vibrant Core (Gold)
                Polyline(
                    points = activeRoute,
                    color = Gold,
                    width = 9f,
                    jointType = JointType.ROUND,
                    startCap = RoundCap(),
                    endCap = RoundCap(),
                    zIndex = 2f
                )
            }
        }

        // Floating Recenter Pill Button (appears when user pans)
        AnimatedVisibility(
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
                    coroutineScope.launch {
                        val courierPos = smoothCourierState.currentPosition
                        val target = courierPos ?: pickupLatLng ?: deliveryLatLng ?: defaultCenter
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(target, 15.5f),
                            800
                        )
                    }
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
