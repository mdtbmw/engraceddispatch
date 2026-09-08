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

data class KnownLandmark(
    val title: String,
    val fullAddress: String,
    val keywords: List<String>,
    val lat: Double,
    val lng: Double
)

object GeocoderUtils {

    private val POPULAR_LANDMARKS = listOf(
        KnownLandmark("ADP Junction / Edo State ADP", "Ogba Road / Airport Road, Benin City", listOf("adp", "adp junction", "agricultural development"), 6.2941, 5.5972),
        KnownLandmark("University of Benin Teaching Hospital (UBTH)", "Ugbowo Lagos Road, Benin City", listOf("ubth", "teaching hospital", "ugbowo hospital"), 6.3982, 5.6111),
        KnownLandmark("University of Benin (Main Campus)", "Ugbowo, Benin City", listOf("uniben", "ugbowo campus", "university of benin"), 6.4024, 5.6166),
        KnownLandmark("University of Benin (Ekehuan Campus)", "Ekehuan Road, Benin City", listOf("ekehuan campus", "uniben ekehuan"), 6.3215, 5.5921),
        KnownLandmark("King's Square (Ring Road)", "Ring Road / King's Square, Benin City", listOf("ring road", "kings square", "king square", "oba market"), 6.3350, 5.6200),
        KnownLandmark("Government Reserved Area (GRA)", "GRA, Benin City", listOf("gra", "benin gra", "boundary road"), 6.3150, 5.6180),
        KnownLandmark("Ramat Park", "Ikpoba Hill, Benin City", listOf("ramat", "ramat park", "ikpoba hill"), 6.3533, 5.6542),
        KnownLandmark("Benin City Airport", "Airport Road, GRA, Benin City", listOf("airport", "benin airport", "air port"), 6.3175, 5.5995),
        KnownLandmark("Chicken Republic (Sapele Road)", "Sapele Road, Benin City", listOf("chicken republic", "chic", "chicken rep", "chick", "cr"), 6.3210, 5.6280),
        KnownLandmark("Chicken Republic (Airport Road)", "Airport Road, GRA, Benin City", listOf("chicken republic airport", "chic airport", "chicken republic"), 6.3140, 5.6080),
        KnownLandmark("Chicken Republic (Ugbowo)", "Ugbowo Lagos Road, Benin City", listOf("chicken republic ugbowo", "chic ugbowo"), 6.3910, 5.6120),
        KnownLandmark("KFC Benin City", "Sapele Road, Benin City", listOf("kfc", "kentucky", "kfc benin"), 6.3240, 5.6260),
        KnownLandmark("Domino's Pizza & Cold Stone Creamery", "Sapele Road, Benin City", listOf("dominos", "domino", "cold stone", "coldstone", "pizza"), 6.3255, 5.6250),
        KnownLandmark("Kilimanjaro Fast Food", "Airport Road / Ekehuan Road, Benin City", listOf("kilimanjaro", "kiliman"), 6.3160, 5.6090),
        KnownLandmark("Market Square Supermarket", "Sapele Road, Benin City", listOf("market square", "mk square", "marketsquare"), 6.3190, 5.6270),
        KnownLandmark("Kada Cinemas & Entertainment Centre", "Sapele Road, Benin City", listOf("kada", "kada cinema", "kada cinemas"), 6.3225, 5.6265),
        KnownLandmark("Edo State Secretariat Complex", "Sapele Road, Benin City", listOf("eksa", "secretariat", "state secretariat"), 6.3300, 5.6230),
        KnownLandmark("Samuel Ogbemudia Stadium", "Stadium Road, Benin City", listOf("stadium", "ogbemudia stadium"), 6.3310, 5.6110),
        KnownLandmark("Benson Idahosa University (BIU)", "Ugbor Road, GRA, Benin City", listOf("biu", "benson idahosa"), 6.2990, 5.6210),
        KnownLandmark("Oba Market", "Ring Road, Benin City", listOf("oba market"), 6.3340, 5.6190),
        KnownLandmark("New Benin Market", "New Benin, Benin City", listOf("new benin", "new benin market"), 6.3500, 5.6250),
        KnownLandmark("Uselu Market", "Lagos-Benin Expressway, Uselu, Benin City", listOf("uselu", "uselu market"), 6.3750, 5.6150),
        KnownLandmark("Oluku Toll Gate", "Benin-Lagos Expressway, Oluku", listOf("oluku", "toll gate", "oluku toll gate"), 6.4250, 5.6020),
        KnownLandmark("Ogba Zoo & Nature Park", "Airport Road, Benin City", listOf("ogba zoo", "ogba park", "zoo"), 6.2890, 5.5880),
        KnownLandmark("Country Home Motel Road", "Off Sapele Road, Benin City", listOf("country home", "country home motel"), 6.2950, 5.6320),
        KnownLandmark("Ihama Road", "GRA, Benin City", listOf("ihama", "ihama road"), 6.3180, 5.6150),
        KnownLandmark("Adesuwa Road", "GRA, Benin City", listOf("adesuwa", "adusuwa", "adesuwa road"), 6.3120, 5.6220),
        KnownLandmark("Ugbor Village / Road", "GRA, Benin City", listOf("ugbor", "ugbor road"), 6.2980, 5.6220),
        KnownLandmark("Etete Layout", "GRA, Benin City", listOf("etete", "etete road"), 6.3050, 5.6260),
        KnownLandmark("Akpakpava Road", "Akpakpava, Benin City", listOf("akpakpava", "akpakpava rd"), 6.3360, 5.6280),
        KnownLandmark("Mission Road", "Benin City", listOf("mission road", "mission rd"), 6.3380, 5.6220),
        KnownLandmark("Siluko Road", "Benin City", listOf("siluko", "siluko road", "siluko rd"), 6.3510, 5.6050),
        KnownLandmark("Textile Mill Road", "Benin City", listOf("textile mill", "textile mill road"), 6.3620, 5.6080),
        KnownLandmark("Murtala Mohammed Way", "Benin City", listOf("mm way", "murtala mohammed", "murtala mohammed way"), 6.3450, 5.6350)
    )

    private val ACRONYM_MAP = mapOf(
        "adp" to "ADP Junction Agricultural Development Programme Benin City",
        "ubth" to "University of Benin Teaching Hospital UBTH Ugbowo",
        "uniben" to "University of Benin UNIBEN Ugbowo",
        "gra" to "Government Reserved Area GRA Benin City",
        "biu" to "Benson Idahosa University BIU Benin City",
        "cr" to "Chicken Republic Benin City",
        "chic" to "Chicken Republic Benin City",
        "chick" to "Chicken Republic Benin City"
    )

    fun expandQuery(query: String): String {
        val trimmed = query.trim().lowercase()
        return ACRONYM_MAP[trimmed] ?: query
    }
    
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

    suspend fun fetchMapboxPlacesAutocompleteItems(
        query: String,
        proximityLng: Double? = null,
        proximityLat: Double? = null
    ): List<SearchResultItem> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (query.isBlank() || query.length < 2) return@withContext emptyList()
        val results = mutableListOf<SearchResultItem>()

        // 1. Proactive matching against Nigerian / Benin City Landmarks & Acronyms
        val cleanQ = query.trim().lowercase()
        val matchingLandmarks = POPULAR_LANDMARKS.filter { lm ->
            lm.keywords.any { kw ->
                kw.equals(cleanQ, ignoreCase = true) ||
                kw.startsWith(cleanQ) ||
                cleanQ.startsWith(kw) ||
                (cleanQ.length >= 3 && kw.contains(cleanQ))
            } || lm.title.lowercase().contains(cleanQ)
        }.map {
            SearchResultItem(
                title = it.title,
                fullAddress = it.fullAddress,
                lat = it.lat,
                lng = it.lng
            )
        }
        results.addAll(matchingLandmarks)

        // 2. Mapbox Places Autocomplete query
        try {
            val token = com.esdispatch.BuildConfig.MAPBOX_ACCESS_TOKEN
            if (!token.isNullOrBlank()) {
                val expandedSearch = expandQuery(query)
                val encodedQuery = java.net.URLEncoder.encode(expandedSearch.trim(), "UTF-8")
                val proxParam = if (proximityLng != null && proximityLat != null) {
                    "&proximity=$proximityLng,$proximityLat"
                } else {
                    "&proximity=5.6037,6.3350"
                }
                val urlString = "https://api.mapbox.com/geocoding/v5/mapbox.places/$encodedQuery.json?access_token=$token&autocomplete=true&country=ng&types=poi,address,neighborhood,locality,place,landmark$proxParam&limit=10"
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

                            // Filter out generic country-only or region-only slop
                            if (placeName.equals("Nigeria", ignoreCase = true) ||
                                title.equals("Nigeria", ignoreCase = true) ||
                                title.isBlank() ||
                                (title.equals("Edo", ignoreCase = true) && address.isBlank())) {
                                continue
                            }

                            val cleanAddress = address.replace(", Nigeria", "").replace(", Edo", "").trim()
                            val item = SearchResultItem(title = title, fullAddress = if (cleanAddress.isNotBlank()) cleanAddress else address, lat = lat, lng = lng)
                            if (results.none { it.displayInput.contains(title, ignoreCase = true) || title.contains(it.title, ignoreCase = true) }) {
                                results.add(item)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MapboxPlaces", "Mapbox Places API autocomplete error: ${e.message}")
        }

        // 3. Native Android Geocoder fallback for local Nigerian POIs / businesses
        if (results.size < 4) {
            try {
                val appCtx = com.esdispatch.DispatchApplication.instance
                val geocoder = android.location.Geocoder(appCtx, java.util.Locale.getDefault())
                val expandedQuery = expandQuery(query)
                val queryWithCountry = if (expandedQuery.contains("Nigeria", ignoreCase = true)) expandedQuery.trim() else "${expandedQuery.trim()}, Benin City, Nigeria"
                val systemAddrs = getFromLocationNameCompat(geocoder, queryWithCountry, 6)
                if (!systemAddrs.isNullOrEmpty()) {
                    for (addr in systemAddrs) {
                        val fullLine = addr.getAddressLine(0) ?: ""
                        val feature = addr.featureName ?: addr.premises ?: addr.subThoroughfare ?: ""
                        val cleanLine = fullLine.replace(", Nigeria", "").replace(", Edo", "").trim()
                        if (cleanLine.equals("Nigeria", ignoreCase = true) || cleanLine.isBlank()) continue

                        val title = if (feature.isNotBlank() && feature != cleanLine && !cleanLine.startsWith(feature)) feature else cleanLine.split(",").firstOrNull()?.trim() ?: cleanLine
                        val subtitle = if (cleanLine.contains(title) && cleanLine != title) cleanLine.removePrefix(title).removePrefix(",").trim() else cleanLine
                        
                        if (cleanLine.isNotBlank() && results.none { it.displayInput.contains(cleanLine, ignoreCase = true) || cleanLine.contains(it.displayInput, ignoreCase = true) }) {
                            results.add(SearchResultItem(
                                title = title,
                                fullAddress = if (subtitle.isNotBlank()) subtitle else cleanLine,
                                lat = addr.latitude,
                                lng = addr.longitude
                            ))
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("MapboxPlaces", "Android Geocoder fallback error: ${e.message}")
            }
        }

        return@withContext results
    }

    suspend fun fetchMapboxPlacesAutocomplete(
        query: String,
        proximityLng: Double? = null,
        proximityLat: Double? = null
    ): List<String> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (query.isBlank() || query.length < 2) return@withContext emptyList()
        val items = fetchMapboxPlacesAutocompleteItems(query, proximityLng, proximityLat)
        return@withContext items.map { it.displayInput }
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

        // Check if address matches a known landmark first for instant resolution
        val cleanA = address.trim().lowercase()
        POPULAR_LANDMARKS.firstOrNull { lm ->
            lm.keywords.any { it.equals(cleanA, ignoreCase = true) } || lm.title.equals(address.trim(), ignoreCase = true)
        }?.let {
            return@withContext Pair(it.lat, it.lng)
        }

        try {
            val token = com.esdispatch.BuildConfig.MAPBOX_ACCESS_TOKEN
            if (!token.isNullOrBlank()) {
                val expandedAddress = expandQuery(address)
                val encoded = java.net.URLEncoder.encode(expandedAddress.trim(), "UTF-8")
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
            val expanded = expandQuery(address)
            val list = getFromLocationNameCompat(geocoder, expanded, 1)
            if (!list.isNullOrEmpty()) {
                return@withContext Pair(list[0].latitude, list[0].longitude)
            }
        } catch (e: Exception) {
            android.util.Log.e("GeocoderUtils", "Android geocode error: ${e.message}")
        }
        return@withContext null
    }
}
