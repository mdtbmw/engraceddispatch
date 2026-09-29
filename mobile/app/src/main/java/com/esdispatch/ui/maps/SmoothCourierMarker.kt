package com.esdispatch.ui.maps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import kotlin.math.abs
import kotlinx.coroutines.launch

/**
 * Calculates the shortest angular difference between two bearings in degrees (-180..180).
 * Prevents awkward 350-degree flips when crossing the 0/360 north meridian.
 */
fun computeShortestAngle(from: Float, to: Float): Float {
    val diff = (to - from) % 360f
    return when {
        diff > 180f -> diff - 360f
        diff < -180f -> diff + 360f
        else -> diff
    }
}

/**
 * State holder that smoothly animates LatLng position and bearing angle.
 */
@Composable
fun rememberSmoothCourierState(
    targetLat: Double?,
    targetLng: Double?,
    targetBearing: Float = 0f,
    animate: Boolean = true,
    roadPoints: List<LatLng> = emptyList()
): SmoothCourierState {
    val state = remember { SmoothCourierState() }

    LaunchedEffect(targetLat, targetLng, targetBearing, animate) {
        val position = validMapPosition(targetLat, targetLng)
        if (position == null) {
            state.clear()
        } else {
            kotlinx.coroutines.coroutineScope {
                launch { state.animateToPosition(position, animate, roadPoints) }
                launch { state.animateToBearing(targetBearing.takeIf { it.isFinite() } ?: 0f, animate) }
            }
        }
    }

    return state
}

class SmoothCourierState {
    var currentPosition by mutableStateOf<LatLng?>(null)
        private set

    var currentBearing by mutableFloatStateOf(0f)
        private set

    private var previousPosition: LatLng? = null
    private val positionProgress = Animatable(1f)
    private val bearingAnim = Animatable(0f)

    fun clear() { currentPosition = null }

    suspend fun animateToPosition(newPos: LatLng, animate: Boolean = true, roadPoints: List<LatLng> = emptyList()) {
        val prev = currentPosition
        if (prev == null || !animate || SphericalUtil.computeDistanceBetween(prev, newPos) > 300) {
            currentPosition = newPos
            previousPosition = newPos
            return
        }

        previousPosition = prev
        val startPos = prev
        val startIndex = roadPoints.indices.minByOrNull { SphericalUtil.computeDistanceBetween(startPos, roadPoints[it]) }
        val endIndex = roadPoints.indices.minByOrNull { SphericalUtil.computeDistanceBetween(newPos, roadPoints[it]) }
        val candidate = if (startIndex != null && endIndex != null && endIndex > startIndex &&
            SphericalUtil.computeDistanceBetween(startPos, roadPoints[startIndex]) < 35 &&
            SphericalUtil.computeDistanceBetween(newPos, roadPoints[endIndex]) < 35) {
            listOf(startPos) + roadPoints.subList(startIndex, endIndex + 1) + newPos
        } else listOf(startPos, newPos)
        val path = if (SphericalUtil.computeLength(candidate) <= SphericalUtil.computeDistanceBetween(startPos, newPos) * 2 + 40)
            candidate else listOf(startPos, newPos)
        val segments = path.zipWithNext().map { (a, b) -> SphericalUtil.computeDistanceBetween(a, b) }
        val pathLength = segments.sum()
        positionProgress.snapTo(0f)

        // Animate only between received fixes, following road bends when both fixes match the route.
        positionProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        ) {
            var remaining = pathLength * value
            var segment = 0
            while (segment < segments.lastIndex && remaining > segments[segment]) {
                remaining -= segments[segment]
                segment++
            }
            val fraction = if (segments[segment] > 0) (remaining / segments[segment]).coerceIn(0.0, 1.0) else 1.0
            currentPosition = SphericalUtil.interpolate(path[segment], path[segment + 1], fraction)
        }
    }

    suspend fun animateToBearing(targetBearing: Float, animate: Boolean = true) {
        if (!animate) { currentBearing = (targetBearing % 360f + 360f) % 360f; return }
        val delta = computeShortestAngle(currentBearing, targetBearing)
        if (abs(delta) < 0.5f) return

        val target = currentBearing + delta
        bearingAnim.snapTo(currentBearing)
        bearingAnim.animateTo(
            targetValue = target,
            animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing)
        ) {
            currentBearing = (value % 360f + 360f) % 360f
        }
    }
}

/**
 * Generates crisp, high-resolution vector BitmapDescriptors for markers.
 */
object MapMarkerFactory {

    private var courierIconCache: BitmapDescriptor? = null
    private var pickupIconCache: BitmapDescriptor? = null
    private var deliveryIconCache: BitmapDescriptor? = null

    /** Transparent bike artwork supplied by the owner, facing north at zero bearing. */
    fun getCourierMarkerIcon(context: Context? = null): BitmapDescriptor? {
        courierIconCache?.let { return it }

        return try {
            if (context != null) {
                try {
                    com.google.android.gms.maps.MapsInitializer.initialize(context)
                } catch (_: Throwable) {}
            }

            val resources = requireNotNull(context).resources
            val drawable = requireNotNull(androidx.core.content.res.ResourcesCompat.getDrawable(
                resources, com.esdispatch.R.drawable.delivery_bike_top, context.theme))
            val height = (72 * resources.displayMetrics.density).toInt()
            val width = (height * 612f / 1459f).toInt().coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, width, height)
            drawable.draw(canvas)

            val descriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
            courierIconCache = descriptor
            descriptor
        } catch (t: Throwable) {
            android.util.Log.w("MapMarkerFactory", "getCourierMarkerIcon non-fatal fallback: ${t.message}")
            try {
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            } catch (_: Throwable) {
                null
            }
        }
    }

    /**
     * Minimalist Gold Pickup Pin (Origin waypoint)
     */
    fun getPickupMarkerIcon(context: Context? = null): BitmapDescriptor? {
        pickupIconCache?.let { return it }

        return try {
            if (context != null) {
                try {
                    com.google.android.gms.maps.MapsInitializer.initialize(context)
                } catch (_: Throwable) {}
            }

            val size = 64
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val center = size / 2f
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // Outer white border
            paint.color = android.graphics.Color.WHITE
            paint.style = Paint.Style.FILL
            canvas.drawCircle(center, center, center - 4f, paint)

            // Inner Gold core
            paint.color = android.graphics.Color.parseColor("#FFB800")
            canvas.drawCircle(center, center, center - 10f, paint)

            // Center Obsidian dot
            paint.color = android.graphics.Color.parseColor("#0E0E10")
            canvas.drawCircle(center, center, 6f, paint)

            val descriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
            pickupIconCache = descriptor
            descriptor
        } catch (t: Throwable) {
            android.util.Log.w("MapMarkerFactory", "getPickupMarkerIcon non-fatal fallback: ${t.message}")
            try {
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
            } catch (_: Throwable) {
                null
            }
        }
    }

    /**
     * Minimalist Obsidian Destination Pin (Dropoff waypoint)
     */
    fun getDeliveryMarkerIcon(context: Context? = null): BitmapDescriptor? {
        deliveryIconCache?.let { return it }

        return try {
            if (context != null) {
                try {
                    com.google.android.gms.maps.MapsInitializer.initialize(context)
                } catch (_: Throwable) {}
            }

            val size = 64
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val center = size / 2f
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // Outer Gold border
            paint.color = android.graphics.Color.parseColor("#FFB800")
            paint.style = Paint.Style.FILL
            canvas.drawCircle(center, center, center - 4f, paint)

            // Inner Obsidian core
            paint.color = android.graphics.Color.parseColor("#0E0E10")
            canvas.drawCircle(center, center, center - 10f, paint)

            // Center Gold dot
            paint.color = android.graphics.Color.parseColor("#FFB800")
            canvas.drawCircle(center, center, 6f, paint)

            val descriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
            deliveryIconCache = descriptor
            descriptor
        } catch (t: Throwable) {
            android.util.Log.w("MapMarkerFactory", "getDeliveryMarkerIcon non-fatal fallback: ${t.message}")
            try {
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            } catch (_: Throwable) {
                null
            }
        }
    }
}
