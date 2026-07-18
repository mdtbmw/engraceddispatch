@file:Suppress("DEPRECATION")
package com.example.ui.components

import com.example.BuildConfig
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Email
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalContext
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.testTag
import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import com.example.ui.theme.*
import androidx.compose.ui.res.painterResource
import kotlin.math.sin
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.drawscope.rotate

// --- Stacked Canvas 3D Boxes ---
@Composable
fun Box3D(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    count: Int = 1
) {
    val sizePx = with(LocalDensity.current) { size.toPx() }
    val spacingPx = with(LocalDensity.current) { 4.dp.toPx() }

    Canvas(
        modifier = modifier
            .size(width = size + (if (count > 1) 12.dp else 0.dp), height = size + (if (count > 1) 12.dp else 0.dp))
    ) {
        for (i in (count - 1) downTo 0) {
            val offsetX = i * spacingPx * 1.5f
            val offsetY = -i * spacingPx * 1.2f
            
            drawIsometricBox(
                size = sizePx,
                offsetX = offsetX,
                offsetY = sizePx * 0.2f + offsetY
            )
        }
    }
}

private fun DrawScope.drawIsometricBox(size: Float, offsetX: Float, offsetY: Float) {
    // 3D isometric diamond math
    val h = size * 0.5f // half size
    val quarter = size * 0.25f
    val halfH = size * 0.3f

    // Top Face
    val topPath = Path().apply {
        moveTo(offsetX + h, offsetY)
        lineTo(offsetX + h * 2, offsetY + quarter)
        lineTo(offsetX + h, offsetY + quarter * 2)
        lineTo(offsetX, offsetY + quarter)
        close()
    }
    drawPath(topPath, color = GoldLight)
    drawPath(topPath, color = Gold, style = strokeStyle())

    // Left Face
    val leftPath = Path().apply {
        moveTo(offsetX, offsetY + quarter)
        lineTo(offsetX + h, offsetY + quarter * 2)
        lineTo(offsetX + h, offsetY + quarter * 2 + h)
        lineTo(offsetX, offsetY + quarter + h)
        close()
    }
    drawPath(leftPath, color = GoldDark)
    drawPath(leftPath, color = Gold, style = strokeStyle())

    // Right Face
    val rightPath = Path().apply {
        moveTo(offsetX + h, offsetY + quarter * 2)
        lineTo(offsetX + h * 2, offsetY + quarter)
        lineTo(offsetX + h * 2, offsetY + quarter + h)
        lineTo(offsetX + h, offsetY + quarter * 2 + h)
        close()
    }
    drawPath(rightPath, color = Gold)
    drawPath(rightPath, color = Gold, style = strokeStyle())
}

private fun strokeStyle() = androidx.compose.ui.graphics.drawscope.Stroke(
    width = 3f,
    join = StrokeJoin.Round
)

// --- Custom Bottom Nav Notched Shape ---
class NotchedNavShape(private val notchRadius: Dp = 38.dp) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            with(density) {
                val R = notchRadius.toPx()
                val r = 16.dp.toPx() // Fillet radius
                val centerX = size.width / 2

                // Calculate the geometry parameters of the double-filleted dome
                val x_f = -Math.sqrt((R * R + 2 * R * r).toDouble()).toFloat()
                val alpha = Math.toDegrees(Math.asin((r / (R + r)).toDouble())).toFloat()

                val leftFilletStartAngle = 90f
                val leftFilletSweep = -(90f - alpha)

                val domeStartAngle = 180f + alpha
                val domeSweep = 180f - 2 * alpha

                val rightFilletStartAngle = 180f - alpha
                val rightFilletSweep = -(90f - alpha)

                val cornerRadius = 24.dp.toPx() // Rounded top-left and top-right ends of the docker

                // Start at left edge, just below top-left corner curve
                moveTo(0f, cornerRadius)
                
                // Top-left rounded corner
                quadraticTo(0f, 0f, cornerRadius, 0f)

                // 1. Line to the start of the left fillet
                lineTo(centerX + x_f, 0f)

                // 2. Left fillet arc
                arcTo(
                    rect = Rect(
                        left = centerX + x_f - r,
                        top = -2 * r,
                        right = centerX + x_f + r,
                        bottom = 0f
                    ),
                    startAngleDegrees = leftFilletStartAngle,
                    sweepAngleDegrees = leftFilletSweep,
                    forceMoveTo = false
                )

                // 3. Dome arc
                arcTo(
                    rect = Rect(
                        left = centerX - R,
                        top = -R,
                        right = centerX + R,
                        bottom = R
                    ),
                    startAngleDegrees = domeStartAngle,
                    sweepAngleDegrees = domeSweep,
                    forceMoveTo = false
                )

                // 4. Right fillet arc
                arcTo(
                    rect = Rect(
                        left = centerX - x_f - r,
                        top = -2 * r,
                        right = centerX - x_f + r,
                        bottom = 0f
                    ),
                    startAngleDegrees = rightFilletStartAngle,
                    sweepAngleDegrees = rightFilletSweep,
                    forceMoveTo = false
                )

                // 5. Line to the start of the top-right corner curve
                lineTo(size.width - cornerRadius, 0f)

                // Top-right rounded corner
                quadraticTo(size.width, 0f, size.width, cornerRadius)

                // 6. Rest of the bar bounding box
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
        }
        return Outline.Generic(path)
    }
}

// --- Custom Floating Bottom Navigation ---
@Composable
fun BottomNav(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    activeViewMode: String = "customer",
    userRole: String = "customer"
) {
    val selectedColor = Gold
    val unselectedColor = GoldenWhiteLight.copy(alpha = 0.75f)
    val GoldGradient = Brush.linearGradient(listOf(Gold, GoldDark))
    val DarkSurface = Obsidian
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark
    val navBorderColor = BorderColor

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.Transparent)
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(82.dp),
            shape = NotchedNavShape(38.dp),
            color = DarkSurface,
            shadowElevation = 0.dp,
            border = null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activeViewMode == "rider") {
                    // Rider tabs (Manifest, Payroll, Spacer, Tracking, Profile)
                    BottomNavItem(
                        icon = { Icon(Icons.Filled.DirectionsBike, "Manifest", tint = if (currentScreen == "Dashboard") selectedColor else unselectedColor) },
                        label = "Manifest",
                        isSelected = currentScreen == "Dashboard",
                        modifier = Modifier.width(52.dp),
                        onClick = { onNavigate("Dashboard") }
                    )

                    BottomNavItem(
                        icon = { Icon(Icons.Filled.AccountBalanceWallet, "Payroll", tint = if (currentScreen == "Wallet") selectedColor else unselectedColor) },
                        label = "Payroll",
                        isSelected = currentScreen == "Wallet",
                        modifier = Modifier.width(52.dp),
                        onClick = { onNavigate("Wallet") }
                    )

                    Spacer(modifier = Modifier.width(64.dp))

                    BottomNavItem(
                        icon = { Icon(Icons.Filled.Map, "Tracking", tint = if (currentScreen == "ActiveTracking") selectedColor else unselectedColor) },
                        label = "Tracking",
                        isSelected = currentScreen == "ActiveTracking",
                        modifier = Modifier.width(52.dp),
                        onClick = { onNavigate("ActiveTracking") }
                    )

                    BottomNavItem(
                        icon = { Icon(Icons.Filled.Person, "Profile", tint = if (currentScreen == "Profile") selectedColor else unselectedColor) },
                        label = "Profile",
                        isSelected = currentScreen == "Profile",
                        modifier = Modifier.width(52.dp),
                        onClick = { onNavigate("Profile") }
                    )
                } else {
                    // Customer tabs (Home, Orders, Spacer, Tracking, Profile)
                    BottomNavItem(
                        icon = { Icon(Icons.Filled.Home, "Home", tint = if (currentScreen == "Dashboard") selectedColor else unselectedColor) },
                        label = "Home",
                        isSelected = currentScreen == "Dashboard",
                        modifier = Modifier.width(52.dp),
                        onClick = { onNavigate("Dashboard") }
                    )

                    BottomNavItem(
                        icon = { Icon(Icons.Filled.Inbox, "Orders", tint = if (currentScreen == "OrderLogs") selectedColor else unselectedColor) },
                        label = "Order",
                        isSelected = currentScreen == "OrderLogs",
                        modifier = Modifier.width(52.dp),
                        onClick = { onNavigate("OrderLogs") }
                    )

                    Spacer(modifier = Modifier.width(64.dp))

                    BottomNavItem(
                        icon = { Icon(Icons.Filled.Map, "Tracking", tint = if (currentScreen == "ActiveTracking") selectedColor else unselectedColor) },
                        label = "Tracking",
                        isSelected = currentScreen == "ActiveTracking",
                        modifier = Modifier.width(52.dp),
                        onClick = { onNavigate("ActiveTracking") }
                    )

                    BottomNavItem(
                        icon = { Icon(Icons.Filled.Person, "Profile", tint = if (currentScreen == "Profile") selectedColor else unselectedColor) },
                        label = "Profile",
                        isSelected = currentScreen == "Profile",
                        modifier = Modifier.width(52.dp),
                        onClick = { onNavigate("Profile") }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-28).dp)
                .size(60.dp)
                .clip(CircleShape)
                .background(GoldGradient)
                .clickable { onNavigate("SendParcel") },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Create Shipment",
                tint = Obsidian, // STRICT LOCK: NO WHITE ON GOLD
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: @Composable () -> Unit,
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier.width(56.dp),
    onClick: () -> Unit
) {
    val selectedColor = Gold
    val unselectedColor = GoldenWhiteLight.copy(alpha = 0.75f)

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon()
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) selectedColor else unselectedColor,
            maxLines = 1
        )
    }
}

// --- Quilted Obsidian/Dark Pattern Brush ---
@Composable
fun QuiltedBackground(
    modifier: Modifier = Modifier,
    lineColor: Color = Color.White.copy(alpha = 0.04f),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
    ) {
        content()
    }
}

// --- Custom High-Craft Map tracking system ---
// IMPORTANT AI AGENT / DEVELOPER STRICT INTEGRATION GUIDELINES:
// 1. DO NOT REMOVE THIS COMPOSABLE OR MODIFY ITS COLOR SIGNATURES (e.g. Gold, Obsidian, AppSurface).
// 2. TO CONNECT TO REAL MAPBOX SDK:
//    - Add 'com.mapbox.maps:android:11.3.0' or 'com.mapbox.navigation:android:2.18.0' to your app/build.gradle.kts dependencies.
//    - In settings.gradle.kts, add the Mapbox Maven Repository:
//      maven {
//          url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
//          credentials {
//              username = "mapbox"
//              password = "YOUR_MAPBOX_SECRET_TOKEN" // Put in Secrets Panel as MAPBOX_SECRET_TOKEN
//          }
//      }
//    - Declare your Mapbox Public Token in AndroidManifest.xml:
//      <meta-data android:name="mapbox_access_token" android:value="@string/mapbox_access_token"/>
//    - Replace this Custom Canvas drawing with Mapbox's MapView inside AndroidView { context -> MapView(context).apply { ... } }
//    - ALWAYS wrap MapView inside a custom Theme style block. Ensure Mapbox Style is set to Style.DARK or Style.SATELLITE to keep the premium dark/gold luxury aesthetic intact!
// 3. TO BIND TO REAL BACKEND DATABASE / REAL-TIME WEBSOCKET:
//    - Bind the 'progress' and location offsets to a real-time Flow/LiveData supplied by your Firebase/Spanner Database or real-time Courier tracking updates.
//    - Maintain the smooth interpolation logic (e.g. spring or animateFloatAsState) to prevent erratic visual jumps when coordinates update.
@Composable
fun MapCanvas(
    modifier: Modifier = Modifier,
    progress: Float = 0.5f, // animate courier along path
    isSatellite: Boolean = false,
    showTraffic: Boolean = true,
    zoom: Float = 14.5f
) {
    val context = LocalContext.current
    val isDarkTheme = MaterialTheme.colorScheme.background == BackgroundDark
    val mapboxToken = BuildConfig.MAPBOX_ACCESS_TOKEN

    val htmlContent = remember(isSatellite, showTraffic, zoom, isDarkTheme) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
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
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map;
                var courierMarker;
                var routeLine;
                
                // Benin City coordinates
                var startLat = 6.3350, startLng = 5.6037;
                var endLat = 6.4020, endLng = 5.6174;
                
                var pathPoints = [
                    [startLat, startLng],
                    [startLat + (endLat - startLat) * 0.33, startLng + (endLng - startLng) * 0.33],
                    [startLat + (endLat - startLat) * 0.66, startLng + (endLng - startLng) * 0.66],
                    [endLat, endLng]
                ];
                
                function initMap() {
                    map = L.map('map', {
                        zoomControl: false,
                        attributionControl: false
                    }).setView([startLat, startLng], $zoom);

                    var tileUrl = '${if (isSatellite) "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}" else if (isDarkTheme) "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png" else "https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"}';
                    
                    L.tileLayer(tileUrl, {
                        maxZoom: 20
                    }).addTo(map);

                    // Add Route Line
                    routeLine = L.polyline(pathPoints, {
                        color: '#D4AF37',
                        weight: 4,
                        opacity: 0.85
                    }).addTo(map);

                    // Start marker
                    L.circleMarker([startLat, startLng], {
                        radius: 8,
                        color: '#000000',
                        fillColor: '#FFFFFF',
                        fillOpacity: 1,
                        weight: 3
                    }).addTo(map).bindPopup("Pickup Point");

                    // End marker
                    L.circleMarker([endLat, endLng], {
                        radius: 8,
                        color: '#D4AF37',
                        fillColor: '#FFFFFF',
                        fillOpacity: 1,
                        weight: 3
                    }).addTo(map).bindPopup("Delivery Destination");

                    // Courier icon
                    var courierIcon = L.divIcon({
                        className: 'pulsing-courier',
                        html: '<div style="background-color: #D4AF37; width: 16px; height: 16px; border-radius: 50%; border: 2px solid white; box-shadow: 0 0 10px rgba(0,0,0,0.5);"></div>',
                        iconSize: [20, 20],
                        iconAnchor: [10, 10]
                    });

                    courierMarker = L.marker([startLat, startLng], { icon: courierIcon }).addTo(map);
                    
                    // Fit bounds of path
                    map.fitBounds(routeLine.getBounds(), { padding: [40, 40] });

                    // Initial progress update
                    updateCourierLocation($progress);
                }

                function updateCourierLocation(progressVal) {
                    if (!map || !pathPoints || !courierMarker) return;
                    var segmentCount = pathPoints.length - 1;
                    var segmentIdx = Math.floor(progressVal * segmentCount);
                    if (segmentIdx >= segmentCount) { segmentIdx = segmentCount - 1; }
                    var segmentProgress = (progressVal * segmentCount) - segmentIdx;
                    
                    var p1 = pathPoints[segmentIdx];
                    var p2 = pathPoints[segmentIdx + 1];
                    
                    var courierLat = p1[0] + (p2[0] - p1[0]) * segmentProgress;
                    var courierLng = p1[1] + (p2[1] - p1[1]) * segmentProgress;

                    courierMarker.setLatLng([courierLat, courierLng]);
                }

                window.onload = initMap;
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                webViewClient = WebViewClient()
                loadDataWithBaseURL(
                    "https://checkout.paystack.com",
                    htmlContent,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        modifier = modifier.fillMaxSize(),
        update = { webView ->
            webView.evaluateJavascript("if (typeof updateCourierLocation === 'function') { updateCourierLocation($progress); }", null)
        }
    )
}

// --- Pattern 1 Helper Components ---
@Composable
fun ScreenHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    rightContent: @Composable (() -> Unit)? = null,
    showLogo: Boolean = false,
    backgroundColor: Color? = null
) {
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark
    val headerBgColor = backgroundColor ?: (if (isDark) Gold else Obsidian)
    val headerContentColor = if (headerBgColor == Gold) Obsidian else GoldenWhiteLight
    val backButtonBg = if (headerBgColor == Gold) Obsidian.copy(alpha = 0.15f) else GoldenWhiteLight.copy(alpha = 0.15f)
    val backButtonTint = if (headerBgColor == Gold) Obsidian else Gold

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(headerBgColor)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (onBack != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(backButtonBg)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = backButtonTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(40.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (showLogo) {
                    Icon(
                        painter = painterResource(id = com.example.R.drawable.ic_logo),
                        contentDescription = "Logo",
                        tint = headerContentColor,
                        modifier = Modifier
                            .size(width = 24.dp, height = 38.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = title.uppercase(),
                    fontSize = 15.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = headerContentColor
                )
            }

            if (rightContent != null) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(
                        androidx.compose.material3.LocalContentColor provides headerContentColor
                    ) {
                        rightContent()
                    }
                }
            } else {
                Spacer(modifier = Modifier.size(40.dp))
            }
        }
    }
}

@Composable
fun RoundedSheet(
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    contentColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background == BackgroundDark
    val containerCol = containerColor ?: (if (isDarkTheme) Obsidian else GoldenWhiteLight)
    val contentCol = contentColor ?: (if (isDarkTheme) GoldLight else Obsidian)

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerCol
        )
    ) {
        val overrideStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = Poppins,
            color = contentCol
        )
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalTextStyle provides overrideStyle,
            androidx.compose.material3.LocalContentColor provides contentCol
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp)
            ) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    shape: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    scrimColor: Color = Color.Black.copy(alpha = 0.5f),
    content: @Composable ColumnScope.() -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background == BackgroundDark
    val containerCol = if (isDarkTheme) Obsidian else GoldenWhiteLight
    val contentCol = if (isDarkTheme) GoldLight else Obsidian

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        containerColor = containerCol,
        contentColor = contentCol,
        shape = shape,
        scrimColor = scrimColor
    ) {
        val overrideStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = Poppins,
            color = contentCol
        )
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalTextStyle provides overrideStyle,
            androidx.compose.material3.LocalContentColor provides contentCol
        ) {
            content()
        }
    }
}

@Composable
fun StaggeredItem(
    index: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 50L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 }
    ) {
        content()
    }
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val isDark = MaterialTheme.colorScheme.background == BackgroundDark
    val shimmerColors = if (isDark) {
        listOf(
            Color(0xFF1F1F1F),
            Color(0xFF2C2C2C),
            Color(0xFF1F1F1F)
        )
    } else {
        listOf(
            Color(0xFFE2E8F0),
            Color(0xFFEDF2F7),
            Color(0xFFE2E8F0)
        )
    }

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim + 200f, translateAnim + 200f)
    )

    Box(
        modifier = modifier
            .background(brush, shape)
    )
}

@Composable
fun PinInputField(
    pin: String,
    onPinChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    obscureText: Boolean = true
) {
    val maxDigits = 4
    val focusRequesters = remember { List(4) { FocusRequester() } }

    // Create a list of 4 characters representing the PIN
    val digits = remember(pin) {
        List(4) { index -> pin.getOrNull(index)?.toString() ?: "" }
    }

    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val isDark = !isLight

    // Auto-focus the first empty field on launch
    LaunchedEffect(Unit) {
        val firstEmptyIndex = digits.indexOfFirst { it.isEmpty() }.coerceIn(0, 3)
        try {
            focusRequesters[firstEmptyIndex].requestFocus()
        } catch (e: Exception) {
            // ignore if not attached yet
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until maxDigits) {
            val digit = digits[i]
            val isFocused = remember { mutableStateOf(false) }
            val isActive = isFocused.value

            val scale by animateFloatAsState(
                targetValue = if (isActive) 1.08f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "boxScale"
            )

            // Adaptive colors
            val boxBg = if (isLight) {
                if (isActive) BorderLight else if (digit.isNotEmpty()) GoldenWhite else GoldenWhiteLight
            } else {
                if (isActive) Charcoal else if (digit.isNotEmpty()) Charcoal.copy(alpha = 0.8f) else Charcoal.copy(alpha = 0.4f)
            }

            val boxBorder = if (isError) {
                ErrorRed
            } else if (isLight) {
                if (isActive) Obsidian else if (digit.isNotEmpty()) Obsidian.copy(alpha = 0.5f) else TextGray.copy(alpha = 0.5f)
            } else {
                if (isActive) Gold else if (digit.isNotEmpty()) Gold.copy(alpha = 0.6f) else TextGray.copy(alpha = 0.2f)
            }

            val dotBg = if (isLight) Obsidian else Gold
            val textColor = if (isLight) Obsidian else Color.White
            val cursorColor = if (isLight) Obsidian else Gold

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .background(boxBg)
                    .border(1.5.dp, boxBorder, RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusRequesters[i].requestFocus()
                    },
                contentAlignment = Alignment.Center
            ) {
                // BasicTextField inside each box
                BasicTextField(
                    value = digit,
                    onValueChange = { newValue ->
                        // Handle paste / autofill of multiple digits
                        if (newValue.length > 1) {
                            val digitsOnly = newValue.filter { it.isDigit() }
                            if (digitsOnly.length == 4) {
                                onPinChange(digitsOnly)
                                focusRequesters[3].requestFocus()
                            } else {
                                val lastChar = digitsOnly.lastOrNull()?.toString() ?: ""
                                val newPin = pin.substring(0, i) + lastChar + if (i + 1 < pin.length) pin.substring(i + 1) else ""
                                onPinChange(newPin.take(4))
                                if (lastChar.isNotEmpty() && i < 3) {
                                    focusRequesters[i + 1].requestFocus()
                                }
                            }
                        } else if (newValue.isEmpty()) {
                            // Backspace / clear
                            val newPin = pin.take(i) + (if (i + 1 < pin.length) pin.substring(i + 1) else "")
                            onPinChange(newPin)
                            if (i > 0) {
                                focusRequesters[i - 1].requestFocus()
                            }
                        } else if (newValue.all { it.isDigit() }) {
                            // Single digit entered
                            val newPin = pin.take(i) + newValue + pin.drop(i + 1)
                            onPinChange(newPin.take(4))
                            if (i < 3) {
                                focusRequesters[i + 1].requestFocus()
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = if (i == 3) ImeAction.Done else ImeAction.Next
                    ),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.Transparent, // hide the actual input text to draw our beautiful dots/text!
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    cursorBrush = SolidColor(Color.Transparent),
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequesters[i])
                        .onFocusChanged { isFocused.value = it.isFocused }
                )

                // Render content over the text field
                if (digit.isNotEmpty()) {
                    if (obscureText) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(dotBg)
                        )
                    } else {
                        Text(
                            text = digit,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                } else if (isActive) {
                    val cursorAlpha = rememberInfiniteTransition(label = "cursorAlpha").animateFloat(
                        initialValue = 0.2f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "cursor"
                    )
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(20.dp)
                            .background(cursorColor.copy(alpha = cursorAlpha.value))
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletCheckoutSheet(
    bookingPrice: Double,
    walletBalance: Double,
    onConfirmWalletPayment: () -> Unit,
    onFundRequired: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark
    val surfaceColor = if (isDark) Charcoal else GoldenWhiteLight
    val textColor = if (isDark) Color.White else Obsidian
    val isSufficient = walletBalance >= bookingPrice
    val missingAmount = bookingPrice - walletBalance

    AppModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Logistics Checkout",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = if (isDark) Gold else Obsidian
                )
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = if (isDark) Gold else TextGray
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = if (isDark) Obsidian else GoldenWhite),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.2.dp, if (isDark) Gold.copy(alpha = 0.3f) else Gold.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Delivery Fee",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "₦${String.format("%,.2f", bookingPrice)}",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = if (isDark) Gold else Obsidian
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(DividerColor)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Available Balance",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "₦${String.format("%,.2f", walletBalance)}",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isSufficient) SuccessGreen else ErrorRed
                        )
                    }
                }
            }

            if (!isSufficient) {
                Surface(
                    color = if (isDark) ErrorRed.copy(alpha = 0.12f) else ErrorRed.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.3f) else ErrorRed.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Warning",
                            tint = if (isDark) Gold else ErrorRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Insufficient funds. You are short of ₦${String.format("%,.2f", missingAmount)}. Top up your wallet securely to confirm booking.",
                            fontFamily = Poppins,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) GoldLight else ErrorRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                Surface(
                    color = if (isDark) SuccessGreen.copy(alpha = 0.12f) else SuccessGreen.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secured",
                            tint = if (isDark) Gold else SuccessGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Your wallet has sufficient balance. Payment will be deducted directly from your secured digital wallet.",
                            fontFamily = Poppins,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color.White.copy(alpha = 0.9f) else SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (isSufficient) {
                SwipeToConfirmButton(
                    text = "Swipe to Confirm Payment",
                    onConfirm = {
                        onConfirmWalletPayment()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            } else {
                Button(
                    onClick = {
                        onFundRequired(missingAmount)
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Gold else Obsidian
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "FUND & BOOK NOW",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 15.sp,
                        color = if (isDark) Obsidian else Gold
                    )
                }
            }
        }
    }
}

@Composable
fun SwipeToConfirmButton(
    text: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    var swipeOffset by remember { mutableStateOf(0f) }
    var isConfirmed by remember { mutableStateOf(false) }
    
    // Slider track is fillMaxWidth, thumb is 130.dp, we let the user drag up to trackWidth - thumbWidth
    var trackWidthPx by remember { mutableStateOf(0f) }
    val thumbWidthDp = 130.dp
    val thumbWidthPx = with(LocalDensity.current) { thumbWidthDp.toPx() }
    val maxSwipeDistance = remember(trackWidthPx, thumbWidthPx) {
        (trackWidthPx - thumbWidthPx - 16f).coerceAtLeast(0f)
    }

    val animatedOffset by animateFloatAsState(
        targetValue = if (isConfirmed) maxSwipeDistance else swipeOffset,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "offset"
    )

    val isDark = MaterialTheme.colorScheme.background == BackgroundDark
    val trackBg = if (isDark) Obsidian else GoldenWhite
    val handleColor = Gold
    val handleTextColor = Obsidian

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(29.dp))
            .background(trackBg)
            .border(BorderStroke(1.2.dp, if (isDark) Gold.copy(alpha = 0.3f) else Slate), RoundedCornerShape(29.dp))
            .onGloballyPositioned {
                trackWidthPx = it.size.width.toFloat()
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Track text (Centered)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isConfirmed) "SECURELY VERIFIED ✔" else text.uppercase(),
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 1.2.sp,
                color = if (isDark) GoldLight.copy(alpha = 0.5f) else Obsidian.copy(alpha = 0.5f)
            )
        }

        // Swipable handle/thumb
        val density = LocalDensity.current
        Box(
            modifier = Modifier
                .offset(x = with(density) { animatedOffset.toDp() })
                .padding(4.dp)
                .width(thumbWidthDp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(25.dp))
                .background(handleColor)
                .pointerInput(maxSwipeDistance) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (swipeOffset >= maxSwipeDistance * 0.75f) {
                                isConfirmed = true
                                swipeOffset = maxSwipeDistance
                                onConfirm()
                            } else {
                                swipeOffset = 0f
                            }
                        },
                        onDragCancel = {
                            swipeOffset = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            swipeOffset = (swipeOffset + dragAmount).coerceIn(0f, maxSwipeDistance)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = handleTextColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "SWIPE",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    color = handleTextColor
                )
            }
        }
    }
}

data class ConfettiParticle(
    val angle: Float,
    val speed: Float,
    val color: Color,
    val size: Float,
    val rotSpeed: Float,
    val shapeType: Int
)

@Composable
fun ConfettiEffect(
    trigger: Boolean,
    onFinished: () -> Unit
) {
    if (!trigger) return

    val progress = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(trigger) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
        )
        onFinished()
    }

    val particles = remember(trigger) {
        List(75) {
            ConfettiParticle(
                angle = (Math.random() * 2 * Math.PI).toFloat(),
                speed = (Math.random() * 450 + 150).toFloat(),
                color = when ((Math.random() * 4).toInt()) {
                    0 -> Gold // Premium Gold
                    1 -> GoldLight // Gold Light
                    2 -> GoldDark.copy(alpha = 0.5f) // Alpha Gold
                    else -> Color.White // Elegant White
                },
                size = (Math.random() * 7 + 4).toFloat(),
                rotSpeed = (Math.random() * 720 - 360).toFloat(),
                shapeType = (Math.random() * 3).toInt()
            )
        }
    }

    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(110f)
    ) {
        val cx = size.width / 2
        val cy = size.height / 2.5f
        val p = progress.value

        particles.forEach { particle ->
            val xOffset = Math.cos(particle.angle.toDouble()) * particle.speed * p
            val gravity = 400f * p * p
            val yOffset = Math.sin(particle.angle.toDouble()) * particle.speed * p + gravity

            val px = cx + xOffset.toFloat()
            val py = cy + yOffset.toFloat()

            val rotation = particle.rotSpeed * p

            rotate(rotation, Offset(px, py)) {
                when (particle.shapeType) {
                    0 -> drawRect(
                        color = particle.color,
                        topLeft = Offset(px - particle.size, py - particle.size),
                        size = androidx.compose.ui.geometry.Size(particle.size * 2, particle.size * 2)
                    )
                    1 -> drawCircle(
                        color = particle.color,
                        radius = particle.size,
                        center = Offset(px, py)
                    )
                    else -> drawLine(
                        color = particle.color,
                        start = Offset(px - particle.size, py),
                        end = Offset(px + particle.size, py),
                        strokeWidth = 3f
                    )
                }
            }
        }
    }
}

@Composable
fun SupportButton(
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = Gold,
        shadowElevation = 6.dp,
        modifier = Modifier.testTag("support_button")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Help,
                contentDescription = "Support",
                tint = Obsidian,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Support",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Obsidian
            )
        }
    }
}

@Composable
fun SupportDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var expandedFaqIndex by remember { mutableStateOf<Int?>(null) }
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Charcoal,
        titleContentColor = AppTextColor,
        textContentColor = AppTextColor,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Gold)
                Text("Engraced Support Center", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Welcome to Engraced Dispatch Support. We are here to assist you 24/7 with your logistics and parcel deliveries.",
                    fontSize = 12.sp,
                    color = TextGray
                )
                
                Text(
                    text = "Frequently Asked Questions",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold
                )

                val faqs = listOf(
                    "How are drivers paid?" to "All drivers are employed internally by Engraced Dispatch. Base delivery fees are paid directly to the company. Drivers earn 100% of all customer tips received.",
                    "How do I mark a delivery as picked up?" to "Use the central QR scanner on the rider screen to scan the parcel barcode. This automatically verifies the assignment and updates the status to 'picked_up'.",
                    "How does real-time tracking work?" to "Customers can view live driver GPS coordinates updated directly in Firestore, allowing precise map marker tracking.",
                    "How do I contact admin support?" to "Tap the email button below to send an instant support request to support@engraceddispatch.com."
                )

                faqs.forEachIndexed { index, (q, a) ->
                    val isExpanded = expandedFaqIndex == index
                    Surface(
                        onClick = { expandedFaqIndex = if (isExpanded) null else index },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) BackgroundDark else Color(0xFFF3F4F6),
                        border = BorderStroke(1.dp, if (isDark) BorderDark else Slate)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(q, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTextColor, modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(a, fontSize = 11.sp, color = TextGray, lineHeight = 16.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                            data = android.net.Uri.parse("mailto:support@engraceddispatch.com?subject=Support%20Request%20-%20Engraced%20Dispatch")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Email support: support@engraceddispatch.com", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Email support@engraceddispatch.com", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Charcoal else Obsidian,
                    contentColor = if (isDark) Gold else Color.White
                ),
                border = if (isDark) BorderStroke(1.dp, Gold) else null
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}


