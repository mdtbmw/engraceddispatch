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

internal data class NonVehicularAccess(
    val isNonVehicular: Boolean = false,
    val curbPoint: LatLng? = null,
    val walkingPoints: List<LatLng> = emptyList(),
    val walkingDistanceMeters: Double = 0.0
)

internal data class RoadStep(val points: List<LatLng>, val instruction: String)
internal data class RoadLeg(
    val points: List<LatLng>,
    val steps: List<RoadStep>,
    val meters: Double,
    val seconds: Double,
    val congested: Boolean,
    val nonVehicularAccess: NonVehicularAccess = NonVehicularAccess()
)
internal data class RoadRoute(
    val legs: List<RoadLeg>,
    val fullContinuousPoints: List<LatLng> = emptyList(),
    val walkingSpur: List<LatLng> = emptyList(),
    val curbPoint: LatLng? = null,
    val isNonVehicular: Boolean = false,
    val walkingDistanceMeters: Double = 0.0
)

data class RouteGuidance(
    val title: String = "Finding your route",
    val detail: String = "Loading road directions…",
    val etaSeconds: Double? = null,
    val distanceMeters: Double? = null,
    val loading: Boolean = false,
    val canRetry: Boolean = false,
    val congested: Boolean = false,
    val destination: LatLng? = null,
    val isNonVehicularCurb: Boolean = false,
    val curbWalkingMeters: Double = 0.0
)

internal class RoadRouteState {
    var route by mutableStateOf<RoadRoute?>(null)
    var loading by mutableStateOf(false)
    var failed by mutableStateOf(false)
}

/**
 * Creates an instant fallback route polyline on frame 0 between waypoints,
 * ensuring riders and customers see the route line and ETA immediately upon opening the map.
 */
internal fun generateInstantCorridorRoute(
    origin: LatLng,
    destination: LatLng,
    intermediate: LatLng? = null
): RoadRoute {
    val legs = mutableListOf<RoadLeg>()
    if (intermediate != null) {
        legs.add(createInstantLeg(origin, intermediate))
        legs.add(createInstantLeg(intermediate, destination))
    } else {
        legs.add(createInstantLeg(origin, destination))
    }
    val fullPoints = legs.flatMap { it.points }
    return RoadRoute(
        legs = legs,
        fullContinuousPoints = fullPoints
    )
}

private fun createInstantLeg(from: LatLng, to: LatLng): RoadLeg {
    val distance = SphericalUtil.computeDistanceBetween(from, to)
    val numSteps = 16
    val points = (0..numSteps).map { i ->
        val fraction = i.toDouble() / numSteps
        SphericalUtil.interpolate(from, to, fraction)
    }
    // Estimated dispatch bike speed ~ 28 km/h = 7.8 m/s
    val seconds = (distance / 7.8).coerceAtLeast(30.0)
    val step = RoadStep(points, "Proceed along route")
    return RoadLeg(
        points = points,
        steps = listOf(step),
        meters = distance,
        seconds = seconds,
        congested = false
    )
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
    retry: Int,
    initialOrigin: LatLng? = null
): RoadRouteState {
    val state = remember(pickup, delivery, phase, initialOrigin) { RoadRouteState() }
    val latestCourier by rememberUpdatedState(courier)
    val latestFresh by rememberUpdatedState(fresh)
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // Real road route begins loading on request — no fake straight line on frame 0

    LaunchedEffect(state, lifecycle, retry, initialOrigin) {
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
                val activePoints = if (phase == "delivery" && (state.route?.legs?.size ?: 0) > 1) {
                    state.route?.legs?.getOrNull(1)?.points.orEmpty()
                } else {
                    state.route?.legs?.firstOrNull()?.points.orEmpty()
                }
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
                        val originWaypoint = if (offRoute && position != null) {
                            RoadWaypoint(position, "")
                        } else if (initialOrigin != null && (phase == "pickup" || phase == "delivery")) {
                            RoadWaypoint(initialOrigin, "")
                        } else if (position != null) {
                            RoadWaypoint(position, "")
                        } else {
                            pickup
                        }
                        val target = if (phase == "return") pickup else delivery
                        val hasIntermediate = (phase == "pickup" || (phase == "delivery" && initialOrigin != null && !offRoute)) &&
                            pickup.position != null && originWaypoint.position != pickup.position
                        val intermediate = if (hasIntermediate) pickup else null
                        val result = fetchRoadRoute(context, originWaypoint, target, intermediate)
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

/** Same Google provider as the map with fast OSRM fallback to ensure zero route loading stalls. */
private suspend fun fetchRoadRoute(
    context: Context, origin: RoadWaypoint, destination: RoadWaypoint, intermediate: RoadWaypoint?
): RoadRoute = withContext(Dispatchers.IO) {
    try {
        fetchGoogleRoadRoute(context, origin, destination, intermediate)
    } catch (e: Exception) {
        android.util.Log.w("RoadRoute", "Google Routes failed: ${e.message}, falling back to OSRM road routing")
        fetchOsrmRoadRoute(origin, destination, intermediate)
    }
}

private fun fetchGoogleRoadRoute(
    context: Context, origin: RoadWaypoint, destination: RoadWaypoint, intermediate: RoadWaypoint?
): RoadRoute {
    require(BuildConfig.GOOGLE_MAPS_API_KEY.isNotBlank())
    val connection = URL("https://routes.googleapis.com/directions/v2:computeRoutes")
        .openConnection() as HttpURLConnection
    return try {
        connection.requestMethod = "POST"
        connection.connectTimeout = 4000
        connection.readTimeout = 4000
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

        val processedLegs = List(legs.length()) { index ->
            val leg = legs.getJSONObject(index)
            val rawPoints = PolyUtil.decode(leg.getJSONObject("polyline").getString("encodedPolyline")).toMutableList()
            require(rawPoints.size >= 2)
            val steps = leg.optJSONArray("steps") ?: JSONArray()
            val intervals = leg.optJSONObject("travelAdvisory")?.optJSONArray("speedReadingIntervals") ?: JSONArray()

            // 1. Connect origin waypoint directly into the start of the first leg
            if (index == 0 && origin.position != null) {
                val startDist = SphericalUtil.computeDistanceBetween(origin.position, rawPoints.first())
                if (startDist > 1.0) {
                    rawPoints.add(0, origin.position)
                }
            }

            // 2. Connect intermediate waypoint (Pickup) seamlessly between leg 0 and leg 1
            if (intermediate?.position != null) {
                if (index == 0) {
                    val endDist = SphericalUtil.computeDistanceBetween(rawPoints.last(), intermediate.position)
                    if (endDist > 1.0) {
                        rawPoints.add(intermediate.position)
                    }
                } else if (index == 1) {
                    val startDist = SphericalUtil.computeDistanceBetween(intermediate.position, rawPoints.first())
                    if (startDist > 1.0) {
                        rawPoints.add(0, intermediate.position)
                    }
                }
            }

            // 3. Connect final destination and detect Non-Vehicular Pedestrian Access
            var nonVehicular = NonVehicularAccess()
            if (index == legs.length() - 1 && destination.position != null) {
                val roadCurb = rawPoints.last()
                val curbDistance = SphericalUtil.computeDistanceBetween(roadCurb, destination.position)
                if (curbDistance > 25.0) {
                    nonVehicular = NonVehicularAccess(
                        isNonVehicular = true,
                        curbPoint = roadCurb,
                        walkingPoints = listOf(roadCurb, destination.position),
                        walkingDistanceMeters = curbDistance
                    )
                } else if (curbDistance > 1.0) {
                    rawPoints.add(destination.position)
                }
            }

            RoadLeg(
                points = rawPoints,
                steps = List(steps.length()) { stepIndex ->
                    val step = steps.getJSONObject(stepIndex)
                    RoadStep(
                        PolyUtil.decode(step.optJSONObject("polyline")?.optString("encodedPolyline").orEmpty()),
                        step.optJSONObject("navigationInstruction")?.optString("instructions").orEmpty()
                    )
                },
                meters = leg.getDouble("distanceMeters"),
                seconds = leg.getString("duration").removeSuffix("s").toDouble(),
                congested = (0 until intervals.length()).any {
                    intervals.getJSONObject(it).optString("speed") in listOf("SLOW", "TRAFFIC_JAM")
                },
                nonVehicularAccess = nonVehicular
            )
        }

        val allPoints = mutableListOf<LatLng>()
        for (l in processedLegs) {
            if (allPoints.isEmpty()) {
                allPoints.addAll(l.points)
            } else {
                if (allPoints.last() == l.points.first()) {
                    allPoints.addAll(l.points.drop(1))
                } else {
                    allPoints.addAll(l.points)
                }
            }
        }

        val lastNonVehicular = processedLegs.lastOrNull()?.nonVehicularAccess ?: NonVehicularAccess()
        RoadRoute(
            legs = processedLegs,
            fullContinuousPoints = allPoints,
            walkingSpur = lastNonVehicular.walkingPoints,
            curbPoint = lastNonVehicular.curbPoint,
            isNonVehicular = lastNonVehicular.isNonVehicular,
            walkingDistanceMeters = lastNonVehicular.walkingDistanceMeters
        )
    } finally {
        connection.disconnect()
    }
}

private fun fetchOsrmRoadRoute(
    origin: RoadWaypoint, destination: RoadWaypoint, intermediate: RoadWaypoint?
): RoadRoute {
    val origPos = origin.position ?: LatLng(6.3350, 5.6037)
    val destPos = destination.position ?: origPos

    val coords = if (intermediate?.position != null) {
        "${origPos.longitude},${origPos.latitude};${intermediate.position.longitude},${intermediate.position.latitude};${destPos.longitude},${destPos.latitude}"
    } else {
        "${origPos.longitude},${origPos.latitude};${destPos.longitude},${destPos.latitude}"
    }

    val urlStr = "https://router.project-osrm.org/route/v1/driving/$coords?overview=full&geometries=polyline&steps=true"
    val conn = URL(urlStr).openConnection() as HttpURLConnection
    return try {
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        conn.setRequestProperty("User-Agent", "ESDispatch-Android/2.0")
        check(conn.responseCode == 200)
        val text = conn.inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(text)
        check(root.optString("code") == "Ok")
        val routes = root.getJSONArray("routes")
        check(routes.length() > 0)
        val routeObj = routes.getJSONObject(0)
        val legsArray = routeObj.getJSONArray("legs")

        val processedLegs = List(legsArray.length()) { index ->
            val legObj = legsArray.getJSONObject(index)
            val stepsArray = legObj.optJSONArray("steps") ?: JSONArray()
            val rawPoints = mutableListOf<LatLng>()
            val stepsList = mutableListOf<RoadStep>()

            for (s in 0 until stepsArray.length()) {
                val stepObj = stepsArray.getJSONObject(s)
                val stepGeom = stepObj.optString("geometry")
                val stepPoints = if (stepGeom.isNotBlank()) PolyUtil.decode(stepGeom) else emptyList()
                rawPoints.addAll(stepPoints)

                val streetName = stepObj.optString("name")
                val maneuver = stepObj.optJSONObject("maneuver")
                val mType = maneuver?.optString("type").orEmpty()
                val mMod = maneuver?.optString("modifier").orEmpty()
                val instruction = when {
                    streetName.isNotBlank() && mMod.isNotBlank() -> "Turn $mMod onto $streetName"
                    streetName.isNotBlank() -> "Continue onto $streetName"
                    mMod.isNotBlank() -> "Turn $mMod"
                    mType.isNotBlank() -> mType.replaceFirstChar { it.uppercase() }
                    else -> "Continue along route"
                }
                stepsList.add(RoadStep(stepPoints, instruction))
            }

            val dedupedPoints = rawPoints.fold(mutableListOf<LatLng>()) { acc, p ->
                if (acc.isEmpty() || acc.last() != p) acc.add(p)
                acc
            }
            if (dedupedPoints.size < 2) {
                if (index == 0 && intermediate?.position != null) {
                    dedupedPoints.add(0, origPos)
                    dedupedPoints.add(intermediate.position)
                } else if (index == 1 && intermediate?.position != null) {
                    dedupedPoints.add(0, intermediate.position)
                    dedupedPoints.add(destPos)
                } else {
                    dedupedPoints.add(0, origPos)
                    dedupedPoints.add(destPos)
                }
            }

            // 1. Connect origin waypoint
            if (index == 0) {
                val startDist = SphericalUtil.computeDistanceBetween(origPos, dedupedPoints.first())
                if (startDist > 1.0) dedupedPoints.add(0, origPos)
            }

            // 2. Connect intermediate waypoint (Pickup)
            if (intermediate?.position != null) {
                if (index == 0) {
                    val endDist = SphericalUtil.computeDistanceBetween(dedupedPoints.last(), intermediate.position)
                    if (endDist > 1.0) dedupedPoints.add(intermediate.position)
                } else if (index == 1) {
                    val startDist = SphericalUtil.computeDistanceBetween(intermediate.position, dedupedPoints.first())
                    if (startDist > 1.0) dedupedPoints.add(0, intermediate.position)
                }
            }

            // 3. Connect final destination and detect non-vehicular access
            var nonVehicular = NonVehicularAccess()
            if (index == legsArray.length() - 1) {
                val roadCurb = dedupedPoints.last()
                val curbDistance = SphericalUtil.computeDistanceBetween(roadCurb, destPos)
                if (curbDistance > 25.0) {
                    nonVehicular = NonVehicularAccess(
                        isNonVehicular = true,
                        curbPoint = roadCurb,
                        walkingPoints = listOf(roadCurb, destPos),
                        walkingDistanceMeters = curbDistance
                    )
                } else if (curbDistance > 1.0) {
                    dedupedPoints.add(destPos)
                }
            }

            RoadLeg(
                points = dedupedPoints,
                steps = stepsList,
                meters = legObj.optDouble("distance", 0.0),
                seconds = legObj.optDouble("duration", 0.0),
                congested = false,
                nonVehicularAccess = nonVehicular
            )
        }

        val allPoints = mutableListOf<LatLng>()
        for (l in processedLegs) {
            if (allPoints.isEmpty()) {
                allPoints.addAll(l.points)
            } else {
                if (allPoints.last() == l.points.first()) {
                    allPoints.addAll(l.points.drop(1))
                } else {
                    allPoints.addAll(l.points)
                }
            }
        }

        val lastNonVehicular = processedLegs.lastOrNull()?.nonVehicularAccess ?: NonVehicularAccess()
        RoadRoute(
            legs = processedLegs,
            fullContinuousPoints = allPoints,
            walkingSpur = lastNonVehicular.walkingPoints,
            curbPoint = lastNonVehicular.curbPoint,
            isNonVehicular = lastNonVehicular.isNonVehicular,
            walkingDistanceMeters = lastNonVehicular.walkingDistanceMeters
        )
    } finally {
        conn.disconnect()
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
