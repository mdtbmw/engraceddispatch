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
 * Computes the tangent heading of the road polyline closest to the given position,
 * pointing forward along the line towards the destination.
 */
fun computeRouteTangentHeading(position: LatLng, roadPoints: List<LatLng>): Float? {
    if (roadPoints.size < 2) return null
    var bestIndex = 0
    var minDistance = Double.MAX_VALUE
    for (i in 0 until roadPoints.size - 1) {
        val p1 = roadPoints[i]
        val p2 = roadPoints[i + 1]
        val dist = com.google.maps.android.PolyUtil.distanceToLine(position, p1, p2)
        if (dist < minDistance) {
            minDistance = dist
            bestIndex = i
        }
    }
    // Snap to the forward heading along this segment towards destination
    if (minDistance <= 250.0) {
        val heading = SphericalUtil.computeHeading(roadPoints[bestIndex], roadPoints[bestIndex + 1]).toFloat()
        return (heading % 360f + 360f) % 360f
    }
    return null
}

/**
 * Data class representing the split of a route polyline at the courier's location:
 * - traveledPoints: path from route origin up to the rider (greyed out)
 * - remainingPoints: path from the rider onward to the destination (vibrant Gold)
 */
data class RouteSplit(
    val traveledPoints: List<LatLng>,
    val remainingPoints: List<LatLng>
)

/**
 * Splits an active route polyline directly at the courier's location with zero gap.
 * As the rider moves forward, the traveled trail grows behind them and is greyed out,
 * while the remaining path ahead stays crisp brand Gold.
 */
fun splitRouteAtCourier(
    geometry: List<LatLng>,
    courierPos: LatLng?
): RouteSplit {
    if (courierPos == null || geometry.size < 2) {
        return RouteSplit(
            traveledPoints = emptyList(),
            remainingPoints = geometry
        )
    }

    var bestSegment = 0
    var minDistance = Double.MAX_VALUE
    var bestFraction = 0.0

    for (i in 0 until geometry.size - 1) {
        val a = geometry[i]
        val b = geometry[i + 1]
        val dist = com.google.maps.android.PolyUtil.distanceToLine(courierPos, a, b)
        if (dist < minDistance) {
            minDistance = dist
            bestSegment = i
            val segLen = SphericalUtil.computeDistanceBetween(a, b)
            bestFraction = if (segLen > 0.1) {
                val dAP = SphericalUtil.computeDistanceBetween(a, courierPos)
                val dBP = SphericalUtil.computeDistanceBetween(b, courierPos)
                (((dAP * dAP) - (dBP * dBP) + (segLen * segLen)) / (2 * segLen * segLen)).coerceIn(0.0, 1.0)
            } else 0.0
        }
    }

    // Exact snapped point on the road segment
    val a = geometry[bestSegment]
    val b = geometry[bestSegment + 1]
    val snappedOnSegment = SphericalUtil.interpolate(a, b, bestFraction)

    // Traveled: starts at geometry[0], goes through intermediate vertices, ends exactly at snappedOnSegment
    val traveled = mutableListOf<LatLng>()
    for (i in 0..bestSegment) {
        traveled.add(geometry[i])
    }
    traveled.add(snappedOnSegment)

    // Remaining: starts exactly at snappedOnSegment, continues through remaining vertices to destination
    val remaining = mutableListOf<LatLng>()
    remaining.add(snappedOnSegment)
    for (i in (bestSegment + 1) until geometry.size) {
        remaining.add(geometry[i])
    }

    return RouteSplit(
        traveledPoints = traveled,
        remainingPoints = remaining
    )
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

    LaunchedEffect(targetLat, targetLng, targetBearing, animate, roadPoints) {
        val rawPosition = validMapPosition(targetLat, targetLng)
        if (rawPosition == null) {
            state.clear()
        } else {
            // Strictly snap to route polyline so bike sits and drives directly on the line
            val snappedPosition = if (roadPoints.size >= 2) {
                var bestSegment = 0
                var minDistance = Double.MAX_VALUE
                var bestFraction = 0.0
                for (i in 0 until roadPoints.size - 1) {
                    val a = roadPoints[i]
                    val b = roadPoints[i + 1]
                    val dist = com.google.maps.android.PolyUtil.distanceToLine(rawPosition, a, b)
                    if (dist < minDistance) {
                        minDistance = dist
                        bestSegment = i
                        val segLen = SphericalUtil.computeDistanceBetween(a, b)
                        bestFraction = if (segLen > 0.1) {
                            val dAP = SphericalUtil.computeDistanceBetween(a, rawPosition)
                            val dBP = SphericalUtil.computeDistanceBetween(b, rawPosition)
                            (((dAP * dAP) - (dBP * dBP) + (segLen * segLen)) / (2 * segLen * segLen)).coerceIn(0.0, 1.0)
                        } else 0.0
                    }
                }
                if (minDistance <= 250.0) {
                    val a = roadPoints[bestSegment]
                    val b = roadPoints[bestSegment + 1]
                    SphericalUtil.interpolate(a, b, bestFraction)
                } else rawPosition
            } else rawPosition

            // Lock bearing strictly to the road segment heading so the bike points forward along the line
            // Realistically, bikes follow the road and do not rotate 360 degrees
            val routeBearing = computeRouteTangentHeading(snappedPosition, roadPoints)
            val effectiveBearing = routeBearing ?: targetBearing.takeIf { it.isFinite() && it != 0f } ?: state.currentBearing

            val prevPos = state.currentPosition
            val distance = if (prevPos != null) SphericalUtil.computeDistanceBetween(prevPos, snappedPosition) else 0.0

            if (prevPos == null || distance >= 1.5) {
                state.animateToPosition(snappedPosition, animate, roadPoints, effectiveBearing)
            } else {
                state.animateToBearing(effectiveBearing, animate)
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

    suspend fun animateToPosition(
        newPos: LatLng,
        animate: Boolean = true,
        roadPoints: List<LatLng> = emptyList(),
        fallbackBearing: Float = 0f
    ) {
        val prev = currentPosition
        if (prev == null || !animate || SphericalUtil.computeDistanceBetween(prev, newPos) > 300) {
            currentPosition = newPos
            previousPosition = newPos
            if (fallbackBearing.isFinite() && fallbackBearing != 0f) {
                currentBearing = (fallbackBearing % 360f + 360f) % 360f
            }
            return
        }

        previousPosition = prev
        val startPos = prev
        val startIndex = roadPoints.indices.minByOrNull { SphericalUtil.computeDistanceBetween(startPos, roadPoints[it]) }
        val endIndex = roadPoints.indices.minByOrNull { SphericalUtil.computeDistanceBetween(newPos, roadPoints[it]) }
        val candidate = if (startIndex != null && endIndex != null && endIndex > startIndex &&
            SphericalUtil.computeDistanceBetween(startPos, roadPoints[startIndex]) < 100 &&
            SphericalUtil.computeDistanceBetween(newPos, roadPoints[endIndex]) < 100) {
            listOf(startPos) + roadPoints.subList(startIndex, endIndex + 1) + newPos
        } else listOf(startPos, newPos)
        val path = if (SphericalUtil.computeLength(candidate) <= SphericalUtil.computeDistanceBetween(startPos, newPos) * 2 + 40)
            candidate else listOf(startPos, newPos)
        val segments = path.zipWithNext().map { (a, b) -> SphericalUtil.computeDistanceBetween(a, b) }
        val pathLength = segments.sum()
        positionProgress.snapTo(0f)

        // Animate between received fixes, smoothly turning towards road tangent
        positionProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        ) {
            var remaining = pathLength * value
            var segment = 0
            while (segment < segments.lastIndex && remaining > segments[segment]) {
                remaining -= segments[segment]
                segment++
            }
            val fraction = if (segments[segment] > 0) (remaining / segments[segment]).coerceIn(0.0, 1.0) else 1.0
            currentPosition = SphericalUtil.interpolate(path[segment], path[segment + 1], fraction)

            // Smoothly align bike bearing to active line segment heading without 360 spinning
            if (path[segment] != path[segment + 1]) {
                val segHeading = SphericalUtil.computeHeading(path[segment], path[segment + 1]).toFloat()
                if (segHeading.isFinite()) {
                    val targetHeading = (segHeading % 360f + 360f) % 360f
                    val angleDelta = computeShortestAngle(currentBearing, targetHeading)
                    currentBearing = (currentBearing + angleDelta * 0.25f + 360f) % 360f
                }
            }
        }

        // Settle bearing at destination segment orientation
        if (path.size >= 2) {
            val finalSegHeading = SphericalUtil.computeHeading(path[path.size - 2], path.last()).toFloat()
            if (finalSegHeading.isFinite()) {
                val finalTarget = (finalSegHeading % 360f + 360f) % 360f
                val delta = computeShortestAngle(currentBearing, finalTarget)
                currentBearing = (currentBearing + delta + 360f) % 360f
            }
        }
    }

    suspend fun animateToBearing(targetBearing: Float, animate: Boolean = true) {
        if (!targetBearing.isFinite()) return
        val normalizedTarget = (targetBearing % 360f + 360f) % 360f
        if (!animate) {
            currentBearing = normalizedTarget
            return
        }
        val delta = computeShortestAngle(currentBearing, normalizedTarget)
        if (abs(delta) < 1.0f) return

        val target = currentBearing + delta
        bearingAnim.snapTo(currentBearing)
        bearingAnim.animateTo(
            targetValue = target,
            animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing)
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
    private var userIconCache: BitmapDescriptor? = null

    /** Transparent bike artwork, properly proportioned to street width (46dp). */
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
            // Reduced to 46dp for sleek, realistic street-level scale
            val height = (46 * resources.displayMetrics.density).toInt()
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
     * Precision Blue Pin / Pulse Indicator for User Location (standard Google Maps style).
     */
    fun getUserLocationMarkerIcon(context: Context? = null): BitmapDescriptor? {
        userIconCache?.let { return it }
        return try {
            if (context != null) {
                try {
                    com.google.android.gms.maps.MapsInitializer.initialize(context)
                } catch (_: Throwable) {}
            }
            val size = 52
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val center = size / 2f
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // Outer white ring
            paint.color = android.graphics.Color.WHITE
            paint.style = Paint.Style.FILL
            canvas.drawCircle(center, center, center - 2f, paint)

            // Royal Blue core
            paint.color = android.graphics.Color.parseColor("#1A73E8")
            canvas.drawCircle(center, center, center - 8f, paint)

            // Center white dot
            paint.color = android.graphics.Color.WHITE
            canvas.drawCircle(center, center, 5f, paint)

            val descriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
            userIconCache = descriptor
            descriptor
        } catch (_: Throwable) {
            null
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

    /**
     * Polished, flat circular pin with Brand Gold border containing the customer's profile photo
     * or monogram initials. Flat with zero drop shadow per user & branding guidelines.
     */
    fun getCustomerAvatarMarkerIcon(
        context: Context? = null,
        avatarBitmap: Bitmap? = null,
        initials: String = "C"
    ): BitmapDescriptor? {
        return try {
            if (context != null) {
                try {
                    com.google.android.gms.maps.MapsInitializer.initialize(context)
                } catch (_: Throwable) {}
            }
            val density = context?.resources?.displayMetrics?.density ?: 2f
            val size = (44 * density).toInt().coerceAtLeast(64)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val center = size / 2f
            val radius = center - (2 * density)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // Outer Brand Gold ring
            paint.color = android.graphics.Color.parseColor("#FFB800")
            paint.style = Paint.Style.FILL
            canvas.drawCircle(center, center, radius, paint)

            // Inner dark Obsidian disc
            val innerRadius = radius - (3.5f * density)
            paint.color = android.graphics.Color.parseColor("#131312")
            canvas.drawCircle(center, center, innerRadius, paint)

            if (avatarBitmap != null) {
                val clipPath = android.graphics.Path().apply {
                    addCircle(center, center, innerRadius, android.graphics.Path.Direction.CW)
                }
                canvas.save()
                canvas.clipPath(clipPath)
                val srcRect = android.graphics.Rect(0, 0, avatarBitmap.width, avatarBitmap.height)
                val dstRect = android.graphics.RectF(center - innerRadius, center - innerRadius, center + innerRadius, center + innerRadius)
                canvas.drawBitmap(avatarBitmap, srcRect, dstRect, paint)
                canvas.restore()
            } else {
                paint.color = android.graphics.Color.parseColor("#FFB800")
                paint.textSize = innerRadius * 0.95f
                paint.textAlign = Paint.Align.CENTER
                paint.isFakeBoldText = true
                val cleanInitials = initials.trim().take(2).uppercase().ifBlank { "C" }
                val textBounds = android.graphics.Rect()
                paint.getTextBounds(cleanInitials, 0, cleanInitials.length, textBounds)
                val textY = center + (textBounds.height() / 2f) - textBounds.bottom
                canvas.drawText(cleanInitials, center, textY, paint)
            }

            BitmapDescriptorFactory.fromBitmap(bitmap)
        } catch (_: Throwable) {
            getDeliveryMarkerIcon(context)
        }
    }

    /**
     * Subtle Curb Stop / Safe Parking Spot beacon for Non-Vehicular Pedestrian Handover.
     */
    fun getCurbMarkerIcon(context: Context? = null): BitmapDescriptor? {
        return try {
            if (context != null) {
                try {
                    com.google.android.gms.maps.MapsInitializer.initialize(context)
                } catch (_: Throwable) {}
            }
            val density = context?.resources?.displayMetrics?.density ?: 2f
            val size = (32 * density).toInt().coerceAtLeast(44)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val center = size / 2f
            val radius = center - (2 * density)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // Outer Gold border
            paint.color = android.graphics.Color.parseColor("#FFB800")
            paint.style = Paint.Style.FILL
            canvas.drawCircle(center, center, radius, paint)

            // Inner dark Obsidian disc
            val innerRadius = radius - (2.5f * density)
            paint.color = android.graphics.Color.parseColor("#131312")
            canvas.drawCircle(center, center, innerRadius, paint)

            // "P" symbol for Safe Parking / Curb stop
            paint.color = android.graphics.Color.parseColor("#FFB800")
            paint.textSize = innerRadius * 1.15f
            paint.textAlign = Paint.Align.CENTER
            paint.isFakeBoldText = true
            val textBounds = android.graphics.Rect()
            paint.getTextBounds("P", 0, 1, textBounds)
            val textY = center + (textBounds.height() / 2f) - textBounds.bottom
            canvas.drawText("P", center, textY, paint)

            BitmapDescriptorFactory.fromBitmap(bitmap)
        } catch (_: Throwable) {
            null
        }
    }
}
