package com.esdispatch.utils

import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class SearchResultItem(
    val title: String,
    val fullAddress: String,
    val lat: Double? = null,
    val lng: Double? = null
) {
    val displayInput: String
        get() {
            if (title.isBlank() || title.equals("Current Location", ignoreCase = true) || title == fullAddress || fullAddress.startsWith(title)) {
                return fullAddress
            }
            return "$title, $fullAddress"
        }
}

object GeocoderUtils {
    
    suspend fun getFromLocationNameCompat(geocoder: Geocoder, locationName: String, maxResults: Int): List<Address>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocationName(locationName, maxResults, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: List<Address>) {
                        continuation.resume(addresses)
                    }
                    override fun onError(errorMessage: String?) {
                        continuation.resume(null)
                    }
                })
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocationName(locationName, maxResults)
        }
    }

    suspend fun getFromLocationCompat(geocoder: Geocoder, latitude: Double, longitude: Double, maxResults: Int): List<Address>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(latitude, longitude, maxResults, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: List<Address>) {
                        continuation.resume(addresses)
                    }
                    override fun onError(errorMessage: String?) {
                        continuation.resume(null)
                    }
                })
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(latitude, longitude, maxResults)
        }
    }

    suspend fun fetchMapboxPlacesAutocompleteItems(query: String): List<SearchResultItem> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (query.isBlank() || query.length < 2) return@withContext emptyList()
        val results = mutableListOf<SearchResultItem>()
        try {
            val token = com.esdispatch.BuildConfig.MAPBOX_ACCESS_TOKEN
            if (!token.isNullOrBlank()) {
                val encodedQuery = java.net.URLEncoder.encode(query.trim(), "UTF-8")
                val urlString = "https://api.mapbox.com/geocoding/v5/mapbox.places/$encodedQuery.json?access_token=$token&autocomplete=true&types=poi,address,neighborhood,locality,place,landmark&limit=10"
                val url = java.net.URL(urlString)
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                if (conn.responseCode == 200) {
                    val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                    val jsonObj = org.json.JSONObject(jsonStr)
                    val features = jsonObj.optJSONArray("features")
                    if (features != null) {
                        for (i in 0 until features.length()) {
                            val feat = features.getJSONObject(i)
                            val placeName = feat.optString("place_name")
                            val textName = feat.optString("text")
                            val center = feat.optJSONArray("center")
                            val lng = if (center != null && center.length() >= 2) center.getDouble(0) else null
                            val lat = if (center != null && center.length() >= 2) center.getDouble(1) else null
                            
                            val title = if (textName.isNotBlank() && textName != placeName) textName else placeName.split(",").firstOrNull()?.trim() ?: placeName
                            val address = if (placeName.contains(title) && placeName != title) placeName.removePrefix(title).removePrefix(",").trim() else placeName

                            if (placeName.isNotBlank()) {
                                results.add(SearchResultItem(title = title, fullAddress = address, lat = lat, lng = lng))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MapboxPlaces", "Mapbox Places API autocomplete error: ${e.message}")
        }
        return@withContext results
    }

    suspend fun fetchMapboxPlacesAutocomplete(query: String): List<String> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (query.isBlank() || query.length < 2) return@withContext emptyList()
        val results = mutableListOf<String>()
        try {
            val token = com.esdispatch.BuildConfig.MAPBOX_ACCESS_TOKEN
            if (!token.isNullOrBlank()) {
                val encodedQuery = java.net.URLEncoder.encode(query.trim(), "UTF-8")
                val urlString = "https://api.mapbox.com/geocoding/v5/mapbox.places/$encodedQuery.json?access_token=$token&autocomplete=true&types=poi,address,neighborhood,locality,place,landmark&limit=10"
                val url = java.net.URL(urlString)
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                if (conn.responseCode == 200) {
                    val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                    val jsonObj = org.json.JSONObject(jsonStr)
                    val features = jsonObj.optJSONArray("features")
                    if (features != null) {
                        for (i in 0 until features.length()) {
                            val feat = features.getJSONObject(i)
                            val placeName = feat.optString("place_name")
                            if (placeName.isNotBlank() && !results.contains(placeName)) {
                                results.add(placeName)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MapboxPlaces", "Mapbox Places API autocomplete error: ${e.message}")
        }
        return@withContext results
    }

    private val reverseGeocodeCache = java.util.concurrent.ConcurrentHashMap<String, String>()

    suspend fun reverseGeocodeCoordinates(context: android.content.Context, lat: Double, lng: Double): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val cacheKey = String.format(java.util.Locale.US, "%.4f,%.4f", lat, lng)
        reverseGeocodeCache[cacheKey]?.let { return@withContext it }

        // 1. High-Accuracy Mapbox Reverse Geocoding FIRST
        try {
            val token = com.esdispatch.BuildConfig.MAPBOX_ACCESS_TOKEN
            if (!token.isNullOrBlank()) {
                val urlString = "https://api.mapbox.com/geocoding/v5/mapbox.places/$lng,$lat.json?access_token=$token&limit=1"
                val url = java.net.URL(urlString)
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                if (conn.responseCode == 200) {
                    val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                    val jsonObj = org.json.JSONObject(jsonStr)
                    val features = jsonObj.optJSONArray("features")
                    if (features != null && features.length() > 0) {
                        val firstObj = features.getJSONObject(0)
                        val placeName = firstObj.optString("place_name")
                        val text = firstObj.optString("text")
                        val addressNum = firstObj.optString("address")
                        
                        val formatted = when {
                            addressNum.isNotBlank() && text.isNotBlank() -> {
                                "$addressNum $text, " + placeName.substringAfter(", ").replace(", Nigeria", "").replace(", Edo", "")
                            }
                            placeName.isNotBlank() -> {
                                placeName.replace(", Nigeria", "").replace(", Edo", "")
                            }
                            else -> text
                        }
                        
                        if (formatted.isNotBlank()) {
                            reverseGeocodeCache[cacheKey] = formatted
                            return@withContext formatted
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ReverseGeocode", "Mapbox reverse geocode error: ${e.message}")
        }

        // 2. Android System Geocoder fallback
        try {
            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
            val addrs = getFromLocationCompat(geocoder, lat, lng, 1)
            if (!addrs.isNullOrEmpty()) {
                val line = addrs[0].getAddressLine(0)
                if (!line.isNullOrBlank()) {
                    val cleanLine = line.replace(", Nigeria", "").replace(", Edo", "")
                    reverseGeocodeCache[cacheKey] = cleanLine
                    return@withContext cleanLine
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ReverseGeocode", "Android Geocoder error: ${e.message}")
        }

        val fallback = String.format(java.util.Locale.US, "%.5f, %.5f", lat, lng)
        return@withContext fallback
    }

    suspend fun geocodeAddress(context: android.content.Context, address: String): Pair<Double, Double>? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (address.isBlank()) return@withContext null
        try {
            val token = com.esdispatch.BuildConfig.MAPBOX_ACCESS_TOKEN
            if (!token.isNullOrBlank()) {
                val encoded = java.net.URLEncoder.encode(address.trim(), "UTF-8")
                val url = java.net.URL("https://api.mapbox.com/geocoding/v5/mapbox.places/$encoded.json?access_token=$token&limit=1")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                if (conn.responseCode == 200) {
                    val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                    val obj = org.json.JSONObject(jsonStr)
                    val features = obj.optJSONArray("features")
                    if (features != null && features.length() > 0) {
                        val center = features.getJSONObject(0).optJSONArray("center")
                        if (center != null && center.length() >= 2) {
                            val lng = center.getDouble(0)
                            val lat = center.getDouble(1)
                            return@withContext Pair(lat, lng)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("GeocoderUtils", "Mapbox geocode error: ${e.message}")
        }
        try {
            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
            val list = getFromLocationNameCompat(geocoder, address, 1)
            if (!list.isNullOrEmpty()) {
                return@withContext Pair(list[0].latitude, list[0].longitude)
            }
        } catch (e: Exception) {
            android.util.Log.e("GeocoderUtils", "Android geocode error: ${e.message}")
        }
        return@withContext null
    }
}

