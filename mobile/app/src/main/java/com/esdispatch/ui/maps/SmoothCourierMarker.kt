package com.esdispatch.ui.maps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import kotlin.math.abs

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
    targetBearing: Float = 0f
): SmoothCourierState {
    val state = remember { SmoothCourierState() }

    LaunchedEffect(targetLat, targetLng) {
        if (targetLat != null && targetLng != null && targetLat != 0.0 && targetLng != 0.0) {
            state.animateToPosition(LatLng(targetLat, targetLng))
        }
    }

    LaunchedEffect(targetBearing) {
        state.animateToBearing(targetBearing)
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

    suspend fun animateToPosition(newPos: LatLng) {
        val prev = currentPosition
        if (prev == null) {
            currentPosition = newPos
            previousPosition = newPos
            return
        }

        previousPosition = prev
        val startPos = prev
        positionProgress.snapTo(0f)

        // Interpolate over 1200ms with smooth ease-out spring
        positionProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        ) {
            val interpolated = SphericalUtil.interpolate(startPos, newPos, value.toDouble())
            currentPosition = interpolated
        }
    }

    suspend fun animateToBearing(targetBearing: Float) {
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

    /**
     * Bolt-style vehicle marker:
     * - Gold pulsing halo
     * - Deep obsidian disc
     * - Forward-facing chevron/arrow
     */
    fun getCourierMarkerIcon(context: Context): BitmapDescriptor {
        courierIconCache?.let { return it }

        val size = 96
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val center = size / 2f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Outer Gold ring / shadow
        paint.color = android.graphics.Color.parseColor("#40FFB800")
        paint.style = Paint.Style.FILL
        canvas.drawCircle(center, center, center - 2f, paint)

        // Mid Gold border
        paint.color = android.graphics.Color.parseColor("#FFB800")
        paint.style = Paint.Style.FILL
        canvas.drawCircle(center, center, center - 8f, paint)

        // Inner Obsidian disc
        paint.color = android.graphics.Color.parseColor("#0E0E10")
        canvas.drawCircle(center, center, center - 14f, paint)

        // Directional motorcycle / navigation arrow in Gold
        paint.color = android.graphics.Color.parseColor("#FFB800")
        paint.style = Paint.Style.FILL
        val path = android.graphics.Path()
        path.moveTo(center, center - 18)
        path.lineTo(center + 14, center + 14)
        path.lineTo(center, center + 7)
        path.lineTo(center - 14, center + 14)
        path.close()
        canvas.drawPath(path, paint)

        val descriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
        courierIconCache = descriptor
        return descriptor
    }

    /**
     * Minimalist Gold Pickup Pin (Origin waypoint)
     */
    fun getPickupMarkerIcon(): BitmapDescriptor {
        pickupIconCache?.let { return it }

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
        return descriptor
    }

    /**
     * Minimalist Obsidian Destination Pin (Dropoff waypoint)
     */
    fun getDeliveryMarkerIcon(): BitmapDescriptor {
        deliveryIconCache?.let { return it }

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
        return descriptor
    }
}
