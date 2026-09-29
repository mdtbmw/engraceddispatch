package com.esdispatch.ui.maps

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.esdispatch.BuildConfig
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

internal fun validMapPosition(lat: Double?, lng: Double?): LatLng? =
    if (lat != null && lng != null && lat.isFinite() && lng.isFinite() &&
        lat in -90.0..90.0 && lng in -180.0..180.0 && (lat != 0.0 || lng != 0.0)) LatLng(lat, lng) else null

internal data class RoadWaypoint(val position: LatLng?, val address: String) {
    val available get() = position != null || address.isNotBlank()
    fun json(): JSONObject = position?.let {
        JSONObject().put("location", JSONObject().put("latLng", JSONObject()
            .put("latitude", it.latitude).put("longitude", it.longitude)))
    } ?: JSONObject().put("address", address)
}

internal data class RoadStep(val points: List<LatLng>, val instruction: String)
internal data class RoadLeg(
    val points: List<LatLng>, val steps: List<RoadStep>,
    val meters: Double, val seconds: Double, val congested: Boolean
)
internal data class RoadRoute(val legs: List<RoadLeg>)

data class RouteGuidance(
    val title: String = "Finding your route",
    val detail: String = "Loading road directions…",
    val etaSeconds: Double? = null,
    val distanceMeters: Double? = null,
    val loading: Boolean = false,
    val canRetry: Boolean = false,
    val congested: Boolean = false,
    val destination: LatLng? = null
)

internal class RoadRouteState {
    var route by mutableStateOf<RoadRoute?>(null)
    var loading by mutableStateOf(false)
    var failed by mutableStateOf(false)
}

/** Poll only while visible. GPS updates do not cancel in-flight requests or issue one per frame. */
@Composable
internal fun rememberRoadRoute(
    context: Context,
    pickup: RoadWaypoint,
    delivery: RoadWaypoint,
    phase: String,
    courier: LatLng?,
    fresh: Boolean,
    retry: Int
): RoadRouteState {
    val state = remember(pickup, delivery, phase) { RoadRouteState() }
    val latestCourier by rememberUpdatedState(courier)
    val latestFresh by rememberUpdatedState(fresh)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(state, lifecycle, retry) {
        val hasEndpoints = when (phase) {
            "delivery" -> delivery.available
            "return" -> pickup.available
            else -> pickup.available && delivery.available
        }
        if (phase == "none" || !hasEndpoints) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            var lastRequest = 0L
            var lastOrigin: LatLng? = null
            var usedCourier = false
            var lastSuccess = 0L
            while (isActive) {
                val now = android.os.SystemClock.elapsedRealtime()
                val position = latestCourier.takeIf { latestFresh && phase != "planned" }
                // An active delivery needs an actual rider location, never the pickup as a substitute.
                if (phase != "planned" && position == null) {
                    delay(5000)
                    continue
                }
                val activePoints = state.route?.legs?.firstOrNull()?.points.orEmpty()
                val offRoute = position != null && activePoints.size > 1 &&
                    !PolyUtil.isLocationOnPath(position, activePoints, false, 65.0)
                val moved = position != null && lastOrigin?.let {
                    SphericalUtil.computeDistanceBetween(it, position) >= 200
                } == true
                val due = lastRequest == 0L || (position != null) != usedCourier ||
                    (state.failed && now - lastRequest >= 30000) ||
                    (offRoute && now - lastRequest >= 15000) ||
                    (moved && now - lastRequest >= 60000) ||
                    (phase != "planned" && now - lastSuccess >= 120000 && now - lastRequest >= 30000)
                if (due) {
                    lastRequest = now
                    lastOrigin = position
                    usedCourier = position != null
                    state.loading = true
                    // Keep existing route visible while updating directions to prevent visual flashing and route loss
                    try {
                        val origin = position?.let { RoadWaypoint(it, "") } ?: pickup
                        val target = if (phase == "return") pickup else delivery
                        val intermediate = pickup.takeIf { phase == "pickup" && position != null }
                        val result = fetchRoadRoute(context, origin, target, intermediate)
                        ensureActive()
                        state.route = result
                        lastSuccess = android.os.SystemClock.elapsedRealtime()
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        state.failed = true
                    } finally {
                        state.loading = false
                    }
                }
                delay(5000)
            }
        }
    }
    return state
}

/** Same Google provider as the map. Never replace failed road geometry with a straight line. */
private suspend fun fetchRoadRoute(
    context: Context, origin: RoadWaypoint, destination: RoadWaypoint, intermediate: RoadWaypoint?
): RoadRoute = withContext(Dispatchers.IO) {
    require(BuildConfig.GOOGLE_MAPS_API_KEY.isNotBlank())
    val connection = URL("https://routes.googleapis.com/directions/v2:computeRoutes")
        .openConnection() as HttpURLConnection
    try {
        connection.requestMethod = "POST"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("X-Goog-Api-Key", BuildConfig.GOOGLE_MAPS_API_KEY)
        connection.setRequestProperty("X-Android-Package", context.packageName)
        signingCertificate(context)?.let { connection.setRequestProperty("X-Android-Cert", it) }
        connection.setRequestProperty("X-Goog-FieldMask", listOf(
            "routes.legs.polyline.encodedPolyline", "routes.legs.duration", "routes.legs.distanceMeters",
            "routes.legs.steps.polyline.encodedPolyline", "routes.legs.steps.navigationInstruction",
            "routes.legs.travelAdvisory.speedReadingIntervals"
        ).joinToString(","))
        val payload = JSONObject().put("origin", origin.json()).put("destination", destination.json())
            // Two-wheeler routing is not available in every operating region. Request standard roads.
            .put("travelMode", "DRIVE").put("routingPreference", "TRAFFIC_AWARE")
            .put("polylineQuality", "HIGH_QUALITY").put("languageCode", "en")
            .put("extraComputations", JSONArray().put("TRAFFIC_ON_POLYLINE"))
        intermediate?.let { payload.put("intermediates", JSONArray().put(it.json())) }
        connection.outputStream.bufferedWriter().use { it.write(payload.toString()) }
        check(connection.responseCode == 200)
        val response = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        val legs = response.getJSONArray("routes").getJSONObject(0).getJSONArray("legs")
        RoadRoute(List(legs.length()) { index ->
            val leg = legs.getJSONObject(index)
            val points = PolyUtil.decode(leg.getJSONObject("polyline").getString("encodedPolyline"))
            require(points.size >= 2)
            val steps = leg.optJSONArray("steps") ?: JSONArray()
            val intervals = leg.optJSONObject("travelAdvisory")?.optJSONArray("speedReadingIntervals") ?: JSONArray()
            RoadLeg(points, List(steps.length()) { stepIndex ->
                val step = steps.getJSONObject(stepIndex)
                RoadStep(PolyUtil.decode(step.optJSONObject("polyline")?.optString("encodedPolyline").orEmpty()),
                    step.optJSONObject("navigationInstruction")?.optString("instructions").orEmpty())
            }, leg.getDouble("distanceMeters"), leg.getString("duration").removeSuffix("s").toDouble(),
                (0 until intervals.length()).any { intervals.getJSONObject(it).optString("speed") in listOf("SLOW", "TRAFFIC_JAM") })
        })
    } finally {
        connection.disconnect()
    }
}

@Suppress("DEPRECATION")
private fun signingCertificate(context: Context): String? = runCatching {
    val signature = if (Build.VERSION.SDK_INT >= 28) {
        context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            .signingInfo?.apkContentsSigners?.firstOrNull()
    } else {
        context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES).signatures?.firstOrNull()
    }
    signature?.let { MessageDigest.getInstance("SHA-1").digest(it.toByteArray()).joinToString("") { byte -> "%02X".format(byte) } }
}.getOrNull()

internal fun RoadLeg.nextInstruction(position: LatLng?): String {
    if (position == null || steps.isEmpty()) return "Follow the highlighted road"
    val index = steps.indices.minByOrNull { i ->
        val points = steps[i].points
        if (points.size < 2) Double.MAX_VALUE else points.zipWithNext().minOf { (a, b) ->
            PolyUtil.distanceToLine(position, a, b)
        }
    } ?: 0
    val current = steps[index]
    val next = steps.getOrNull(index + 1)
    if (current.points.isEmpty()) return current.instruction
    val closest = current.points.indices.minByOrNull { SphericalUtil.computeDistanceBetween(position, current.points[it]) } ?: 0
    val distance = SphericalUtil.computeLength(current.points.drop(closest)).toInt()
    return if (next != null && next.instruction.isNotBlank()) {
        "${if (distance >= 1000) "%.1f km".format(distance / 1000.0) else "$distance m"} • ${next.instruction}"
    } else current.instruction.ifBlank { "Continue to the destination" }
}

internal fun RoadLeg.distanceToNextTurn(position: LatLng?): Int {
    if (position == null || steps.isEmpty()) return 500
    val index = steps.indices.minByOrNull { i ->
        val points = steps[i].points
        if (points.size < 2) Double.MAX_VALUE else points.zipWithNext().minOf { (a, b) ->
            PolyUtil.distanceToLine(position, a, b)
        }
    } ?: 0
    val current = steps[index]
    if (current.points.isEmpty()) return 500
    val closest = current.points.indices.minByOrNull { SphericalUtil.computeDistanceBetween(position, current.points[it]) } ?: 0
    return SphericalUtil.computeLength(current.points.drop(closest)).toInt()
}
