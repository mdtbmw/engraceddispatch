package com.esdispatch.utils

import android.content.Context
import android.util.Log
import com.esdispatch.BuildConfig
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

data class RouteStep(
    val instruction: String,
    val distanceText: String,
    val durationText: String,
    val maneuver: String? = null
)

data class RouteData(
    val points: List<LatLng>,
    val distanceMeters: Long,
    val durationSeconds: Long,
    val distanceText: String,
    val durationText: String,
    val steps: List<RouteStep> = emptyList()
)

object GoogleMapsHelper {
    private const val TAG = "GoogleMapsHelper"

    /**
     * Uber-Grade Luxury Obsidian Dark Map Style JSON.
     * Features deep charcoal/obsidian roads, dark midnight water, minimal distraction, and sharp contrast.
     */
    const val DARK_MAP_STYLE_JSON = """
    [
      { "elementType": "geometry", "stylers": [{ "color": "#17181c" }] },
      { "elementType": "labels.icon", "stylers": [{ "visibility": "off" }] },
      { "elementType": "labels.text.fill", "stylers": [{ "color": "#8c96a5" }] },
      { "elementType": "labels.text.stroke", "stylers": [{ "color": "#17181c" }] },
      { "featureType": "administrative", "elementType": "geometry", "stylers": [{ "color": "#5a626e" }] },
      { "featureType": "administrative.country", "elementType": "labels.text.fill", "stylers": [{ "color": "#9aa4b2" }] },
      { "featureType": "administrative.locality", "elementType": "labels.text.fill", "stylers": [{ "color": "#d0d6e0" }] },
      { "featureType": "poi", "stylers": [{ "visibility": "off" }] },
      { "featureType": "road", "elementType": "geometry.fill", "stylers": [{ "color": "#23262d" }] },
      { "featureType": "road", "elementType": "geometry.stroke", "stylers": [{ "color": "#1b1d22" }] },
      { "featureType": "road", "elementType": "labels.text.fill", "stylers": [{ "color": "#8e99a8" }] },
      { "featureType": "road.highway", "elementType": "geometry.fill", "stylers": [{ "color": "#2d313a" }] },
      { "featureType": "road.highway", "elementType": "geometry.stroke", "stylers": [{ "color": "#20232a" }] },
      { "featureType": "road.highway", "elementType": "labels.text.fill", "stylers": [{ "color": "#e0e6ed" }] },
      { "featureType": "transit", "stylers": [{ "visibility": "simplified" }] },
      { "featureType": "water", "elementType": "geometry", "stylers": [{ "color": "#0d1017" }] },
      { "featureType": "water", "elementType": "labels.text.fill", "stylers": [{ "color": "#4a5568" }] }
    ]
    """

    /**
     * Uber-Grade Minimalist Clean Silver Light Map Style JSON.
     * Features crisp white roads, soft slate borders, calm pale water, zero harsh yellow/orange clutters.
     */
    const val LIGHT_MAP_STYLE_JSON = """
    [
      { "elementType": "geometry", "stylers": [{ "color": "#f8f9fb" }] },
      { "elementType": "labels.icon", "stylers": [{ "visibility": "off" }] },
      { "elementType": "labels.text.fill", "stylers": [{ "color": "#2d3748" }] },
      { "elementType": "labels.text.stroke", "stylers": [{ "color": "#ffffff" }] },
      { "featureType": "administrative", "elementType": "geometry", "stylers": [{ "color": "#cbd5e1" }] },
      { "featureType": "poi", "stylers": [{ "visibility": "off" }] },
      { "featureType": "road", "elementType": "geometry.fill", "stylers": [{ "color": "#ffffff" }] },
      { "featureType": "road", "elementType": "geometry.stroke", "stylers": [{ "color": "#e2e8f0" }] },
      { "featureType": "road", "elementType": "labels.text.fill", "stylers": [{ "color": "#1e293b" }] },
      { "featureType": "road.highway", "elementType": "geometry.fill", "stylers": [{ "color": "#f1f5f9" }] },
      { "featureType": "road.highway", "elementType": "geometry.stroke", "stylers": [{ "color": "#cbd5e1" }] },
      { "featureType": "road.highway", "elementType": "labels.text.fill", "stylers": [{ "color": "#0f172a" }] },
      { "featureType": "water", "elementType": "geometry", "stylers": [{ "color": "#dbeafe" }] },
      { "featureType": "water", "elementType": "labels.text.fill", "stylers": [{ "color": "#3b82f6" }] }
    ]
    """

    fun getMapStyle(isDark: Boolean): MapStyleOptions {
        return MapStyleOptions(if (isDark) DARK_MAP_STYLE_JSON else LIGHT_MAP_STYLE_JSON)
    }

    private var placesClient: PlacesClient? = null
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Initializes Google Places SDK once using the build-configured API Key.
     */
    fun getPlacesClient(context: Context): PlacesClient {
        if (placesClient == null) {
            val apiKey = BuildConfig.MAPS_API_KEY
            if (!Places.isInitialized() && apiKey.isNotBlank()) {
                try {
                    Places.initialize(context.applicationContext, apiKey)
                } catch (e: Throwable) {
                    Log.e(TAG, "Places.initialize failed: ${e.message}")
                }
            }
            placesClient = try {
                Places.createClient(context.applicationContext)
            } catch (e: Throwable) {
                Log.e(TAG, "Places.createClient failed: ${e.message}")
                null
            }
        }
        return placesClient ?: Places.createClient(context.applicationContext)
    }

    /**
     * Autocomplete address search using Google Places SDK.
     * Searches Nigeria ("NG") and Benin City area, seamlessly combined with local landmarks.
     */
    suspend fun searchPlaces(context: Context, query: String): List<SearchResultItem> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return@withContext emptyList()

        val results = mutableListOf<SearchResultItem>()

        // 1. Google Places Autocomplete query
        try {
            val client = getPlacesClient(context)
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(trimmed)
                .setCountries("NG")
                .build()

            val response = suspendCancellableCoroutine { cont ->
                client.findAutocompletePredictions(request)
                    .addOnSuccessListener { resp ->
                        if (cont.isActive) cont.resume(resp)
                    }
                    .addOnFailureListener { exc ->
                        Log.w(TAG, "Places autocomplete failed: ${exc.message}")
                        if (cont.isActive) cont.resume(null)
                    }
            }

            if (response != null) {
                for (prediction in response.autocompletePredictions) {
                    val primary = prediction.getPrimaryText(null).toString()
                    val full = prediction.getFullText(null).toString()
                    results.add(
                        SearchResultItem(
                            title = primary,
                            fullAddress = full
                        )
                    )
                }
            }
        } catch (e: Throwable) {
            Log.w(TAG, "searchPlaces exception: ${e.message}")
        }

        // 2. Augment with local known landmarks if query matches
        try {
            val localMatches = GeocoderUtils.fetchMapboxPlacesAutocompleteItems(trimmed, 5.6037, 6.3350)
            for (local in localMatches) {
                if (results.none { it.title.equals(local.title, ignoreCase = true) || it.fullAddress.equals(local.fullAddress, ignoreCase = true) }) {
                    results.add(local)
                }
            }
        } catch (e: Throwable) {}

        results
    }

    /**
     * Resolves an address string or place title to LatLng coordinates.
     */
    suspend fun geocodeAddress(context: Context, address: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        if (address.isBlank()) return@withContext null

        // Try GeocoderUtils first (handles landmarks, android geocoder, and fallbacks)
        val local = GeocoderUtils.geocodeAddress(context, address)
        if (local != null) return@withContext local

        // Try Google Geocoding API if configured
        val apiKey = BuildConfig.MAPS_API_KEY
        if (apiKey.isNotBlank()) {
            try {
                val encoded = java.net.URLEncoder.encode("$address, Edo State, Nigeria", "UTF-8")
                val url = "https://maps.googleapis.com/maps/api/geocode/json?address=$encoded&key=$apiKey"
                val req = Request.Builder().url(url).build()
                val resp = httpClient.newCall(req).execute()
                if (resp.isSuccessful) {
                    val body = resp.body?.string()
                    if (body != null) {
                        val json = JSONObject(body)
                        val results = json.optJSONArray("results")
                        if (results != null && results.length() > 0) {
                            val loc = results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location")
                            val lat = loc.getDouble("lat")
                            val lng = loc.getDouble("lng")
                            return@withContext Pair(lat, lng)
                        }
                    }
                }
            } catch (e: Throwable) {
                Log.w(TAG, "Google Geocoding API failed: ${e.message}")
            }
        }

        null
    }

    /**
     * Reverse geocodes coordinates to a clean, formatted street address.
     */
    suspend fun reverseGeocode(context: Context, lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
        if (lat == 0.0 && lng == 0.0) return@withContext "Unknown Location"

        // 1. Try Google Geocoding API for high-precision street names
        val apiKey = BuildConfig.MAPS_API_KEY
        if (apiKey.isNotBlank()) {
            try {
                val url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=$lat,$lng&key=$apiKey"
                val req = Request.Builder().url(url).build()
                val resp = httpClient.newCall(req).execute()
                if (resp.isSuccessful) {
                    val body = resp.body?.string()
                    if (body != null) {
                        val json = JSONObject(body)
                        val results = json.optJSONArray("results")
                        if (results != null && results.length() > 0) {
                            val formatted = results.getJSONObject(0).optString("formatted_address", "")
                            if (formatted.isNotBlank()) {
                                // Clean up Country suffix if redundant
                                return@withContext formatted.removeSuffix(", Nigeria").trim()
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                Log.w(TAG, "Google Reverse Geocoding failed: ${e.message}")
            }
        }

        // 2. Fallback to GeocoderUtils (local offline landmarks + Android Geocoder)
        GeocoderUtils.reverseGeocodeCoordinates(context, lat, lng)
    }

    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return Math.round(r * c * 10.0) / 10.0
    }

    /**
     * Fetches exact road directions between two GPS coordinates using the Google Directions API.
     * Decodes the overview_polyline into a List<LatLng> for native Polyline rendering.
     */
    suspend fun fetchDirections(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double
    ): RouteData? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.MAPS_API_KEY
        if (apiKey.isNotBlank()) {
            try {
                val url = "https://maps.googleapis.com/maps/api/directions/json" +
                        "?origin=$originLat,$originLng" +
                        "&destination=$destLat,$destLng" +
                        "&mode=driving" +
                        "&key=$apiKey"

                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val json = JSONObject(body)
                        val status = json.optString("status", "")
                        if (status == "OK") {
                            val routes = json.getJSONArray("routes")
                            if (routes.length() > 0) {
                                val route = routes.getJSONObject(0)
                                val overviewPolyline = route.getJSONObject("overview_polyline").getString("points")
                                val decodedPoints = PolyUtil.decode(overviewPolyline)

                                val leg = route.getJSONArray("legs").getJSONObject(0)
                                val distMeters = leg.getJSONObject("distance").getLong("value")
                                val distText = leg.getJSONObject("distance").getString("text")
                                val durSeconds = leg.getJSONObject("duration").getLong("value")
                                val durText = leg.getJSONObject("duration").getString("text")

                                val stepsArray = leg.optJSONArray("steps")
                                val routeSteps = mutableListOf<RouteStep>()
                                if (stepsArray != null) {
                                    for (s in 0 until stepsArray.length()) {
                                        val stepObj = stepsArray.getJSONObject(s)
                                        val rawHtml = stepObj.optString("html_instructions", "")
                                        val cleanInstruction = rawHtml.replace(Regex("<[^>]*>"), " ").replace(Regex("\\s+"), " ").trim()
                                        val sDist = stepObj.optJSONObject("distance")?.optString("text", "") ?: ""
                                        val sDur = stepObj.optJSONObject("duration")?.optString("text", "") ?: ""
                                        val maneuver = if (stepObj.has("maneuver")) stepObj.getString("maneuver") else null
                                        if (cleanInstruction.isNotBlank()) {
                                            routeSteps.add(RouteStep(cleanInstruction, sDist, sDur, maneuver))
                                        }
                                    }
                                }

                                return@withContext RouteData(
                                    points = decodedPoints,
                                    distanceMeters = distMeters,
                                    durationSeconds = durSeconds,
                                    distanceText = distText,
                                    durationText = durText,
                                    steps = routeSteps
                                )
                            }
                        } else {
                            Log.w(TAG, "Directions API returned status: $status")
                        }
                    }
                }
            } catch (e: Throwable) {
                Log.w(TAG, "fetchDirections error: ${e.message}")
            }
        }

        // Graceful fallback: straight-line interpolated path with Haversine distance
        val distKm = calculateDistanceKm(originLat, originLng, destLat, destLng)
        val distMeters = (distKm * 1000).toLong()
        val estDurationSec = ((distKm / 30.0) * 3600).toLong().coerceAtLeast(180) // 30 km/h avg speed

        val numSteps = 15
        val interpolated = mutableListOf<LatLng>()
        for (i in 0..numSteps) {
            val frac = i.toDouble() / numSteps
            val lat = originLat + (destLat - originLat) * frac
            val lng = originLng + (destLng - originLng) * frac
            interpolated.add(LatLng(lat, lng))
        }

        val fallbackSteps = listOf(
            RouteStep("Follow transit route toward destination corridor", String.format("%.1f km", distKm * 0.4), "3 mins", "straight"),
            RouteStep("Arrive at specified drop-off location", String.format("%.1f km", distKm * 0.6), String.format("%d mins", (estDurationSec / 60).coerceAtLeast(1)), "arrive")
        )

        RouteData(
            points = interpolated,
            distanceMeters = distMeters,
            durationSeconds = estDurationSec,
            distanceText = String.format("%.1f km", distKm),
            durationText = String.format("%d mins", (estDurationSec / 60).coerceAtLeast(1)),
            steps = fallbackSteps
        )
    }

    /**
     * Retrieves the current device GPS coordinates if permission is granted.
     */
    suspend fun getCurrentDeviceLocation(context: Context): Pair<Double, Double>? = withContext(Dispatchers.IO) {
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
                val loc: android.location.Location? = suspendCancellableCoroutine { cont ->
                    fusedClient.getCurrentLocation(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                        null
                    ).addOnSuccessListener { location ->
                        if (location != null) {
                            if (cont.isActive) cont.resume(location)
                        } else {
                            fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                                if (cont.isActive) cont.resume(lastLoc)
                            }.addOnFailureListener {
                                if (cont.isActive) cont.resume(null)
                            }
                        }
                    }.addOnFailureListener {
                        fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                            if (cont.isActive) cont.resume(lastLoc)
                        }.addOnFailureListener {
                            if (cont.isActive) cont.resume(null)
                        }
                    }
                }
                if (loc != null) {
                    return@withContext Pair(loc.latitude, loc.longitude)
                }
            }
        } catch (e: Throwable) {
            Log.w(TAG, "getCurrentDeviceLocation error: ${e.message}")
        }
        null
    }
}
