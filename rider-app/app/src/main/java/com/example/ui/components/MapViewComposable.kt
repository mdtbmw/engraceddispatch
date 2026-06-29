package com.example.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgePadding
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.StyleURI
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures

data class MapMarker(
    val latitude: Double,
    val longitude: Double,
    val title: String = "",
    val color: AndroidColor = AndroidColor.rgb(92, 88, 255)
)

@OptIn(MapboxExperimental::class)
@Composable
fun DeliveryMapView(
    pickupLat: Double?,
    pickupLng: Double?,
    deliveryLat: Double?,
    deliveryLng: Double?,
    riderLat: Double? = null,
    riderLng: Double? = null,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
    routePoints: List<Pair<Double, Double>>? = null,
    onMapClick: ((com.mapbox.maps.LatLng) -> Unit)? = null
) {
    val context = LocalContext.current
    val hasValidLocations = pickupLat != null && pickupLng != null

    if (!hasValidLocations) {
        MapPlaceholder(modifier)
        return
    }

    val centerLat = deliveryLat ?: pickupLat!!
    val centerLng = deliveryLng ?: pickupLng!!
    val avgLat = (pickupLat!! + centerLat) / 2.0
    val avgLng = (pickupLng!! + centerLng) / 2.0

    var mapView by remember { mutableStateOf<MapView?>(null) }

    AndroidView(
        factory = { ctx ->
            val mv = MapView(ctx, MapInitOptions(ctx))
            mv.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(Point(avgLng, avgLat))
                    .zoom(14.0)
                    .padding(EdgePadding(80.0, 50.0, 80.0, 50.0))
                    .build()
            )
            mv.mapboxMap.loadStyleUri(StyleURI.MAPBOX_STREETS) { style ->
                val annotationApi = mv.annotations
                val pointManager = annotationApi.createPointAnnotationManager()
                pointManager.deleteAll()

                val pickupPoint = PointAnnotationOptions()
                    .withPoint(Point(pickupLng!!, pickupLat!!))
                    .withIconImage("marker-15")
                    .withIconColor(AndroidColor.rgb(92, 88, 255))
                pointManager.create(pickupPoint)

                if (deliveryLat != null && deliveryLng != null) {
                    val deliveryPoint = PointAnnotationOptions()
                        .withPoint(Point(deliveryLng, deliveryLat))
                        .withIconImage("marker-15")
                        .withIconColor(AndroidColor.rgb(249, 115, 22))
                    pointManager.create(deliveryPoint)
                }

                val coords = mutableListOf<Point>()
                if (routePoints != null && routePoints.isNotEmpty()) {
                    for ((lat, lng) in routePoints) {
                        coords.add(Point.fromLngLat(lng, lat))
                    }
                } else {
                    coords.add(Point.fromLngLat(pickupLng!!, pickupLat!!))
                    if (deliveryLat != null && deliveryLng != null) {
                        coords.add(Point.fromLngLat(deliveryLng, deliveryLat))
                    }
                }

                val lineString = LineString.fromLngLats(coords)
                val feature = Feature.fromGeometry(lineString)
                val fc = FeatureCollection.fromFeatures(arrayOf(feature))
                val source = com.mapbox.maps.extension.style.sources.generated.GeoJSONSource.Builder("route-source")
                    .data(fc.toJson())
                    .build()
                style.addSource(source)

                val lineLayer = com.mapbox.maps.extension.style.layers.generated.LineLayer("route-layer", "route-source").apply {
                    lineColor(AndroidColor.rgb(92, 88, 255))
                    lineWidth(4.0)
                    lineOpacity(0.85)
                    lineCap(LineCap.ROUND)
                    lineJoin(LineJoin.ROUND)
                }
                style.addLayer(lineLayer)
            }

            if (!interactive) {
                mv.gestures.scrollEnabled = false
                mv.gestures.pinchToZoomEnabled = false
                mv.gestures.doubleTapToZoomInEnabled = false
            }

            if (onMapClick != null) {
                mv.mapboxMap.addOnMapClickListener { point ->
                    onMapClick(point)
                    true
                }
            }

            mapView = mv
            mv
        },
        modifier = modifier.clip(RoundedCornerShape(20.dp))
    )

    DisposableEffect(Unit) {
        onDispose { mapView?.onDestroy() }
    }
}

@Composable
fun MapPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.ui.Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Map,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Map view",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Text(
                    "Loading locations...",
                    color = TextGray,
                    fontSize = 10.sp
                )
            }
        }
    }
}
