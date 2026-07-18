package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.ScreenHeader
import com.example.ui.components.RoundedSheet
import com.example.ui.components.QuiltedBackground
import com.example.ui.theme.*
import com.example.viewmodel.DeliveryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

suspend fun detectUserLocation(context: android.content.Context): String {
    // 1. Try GPS Location via LocationManager
    try {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager
            val providers = locationManager?.getProviders(true)
            var bestLocation: android.location.Location? = null
            if (providers != null) {
                for (provider in providers) {
                    val loc = locationManager.getLastKnownLocation(provider) ?: continue
                    if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                        bestLocation = loc
                    }
                }
            }
            if (bestLocation != null) {
                val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                val addresses = geocoder.getFromLocation(bestLocation.latitude, bestLocation.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addrLine = addresses[0].getAddressLine(0)
                    if (!addrLine.isNullOrBlank()) return addrLine
                }
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("DetectLocation", "GPS detection failed: ${e.message}")
    }

    // 2. Fallback to GeoIP API (extremely robust for virtual/server environments)
    try {
        val url = java.net.URL("https://ipapi.co/json/")
        val urlConnection = url.openConnection() as java.net.HttpURLConnection
        urlConnection.setRequestProperty("User-Agent", "EngracedDispatchAndroidApp/1.0")
        urlConnection.connectTimeout = 3000
        urlConnection.readTimeout = 3000
        val response = urlConnection.inputStream.bufferedReader().use { it.readText() }
        val json = org.json.JSONObject(response)
        val city = json.optString("city")
        val region = json.optString("region")
        val country = json.optString("country_name")
        val postal = json.optString("postal")
        if (!city.isNullOrBlank() && !country.isNullOrBlank()) {
            return "$city, $region, $postal, $country"
        }
    } catch (e: Exception) {
        android.util.Log.e("DetectLocation", "GeoIP fallback failed: ${e.message}")
    }

    return "Lekki Conservation Centre, Lekki-Epe Expressway, Lagos"
}

@Composable
fun BookingFormScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val draft by viewModel.parcelDraft.collectAsState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var pickup by remember { mutableStateOf("") }
    var delivery by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var weight by remember { mutableStateOf("1.0") }
    var length by remember { mutableStateOf("20") }
    var width by remember { mutableStateOf("15") }
    var height by remember { mutableStateOf("10") }
    var sName by remember { mutableStateOf("") }
    var sPhone by remember { mutableStateOf("") }
    var rName by remember { mutableStateOf("") }
    var rPhone by remember { mutableStateOf("") }

    var additionalStops by remember { mutableStateOf(listOf<String>()) }
    var selectedInsurance by remember { mutableStateOf("none") } // none, basic, premium
    var focusedField by remember { mutableStateOf<String?>(null) } // pickup, delivery, stop_X

    var isScheduled by remember { mutableStateOf(false) }
    var scheduledDate by remember { mutableStateOf("Today, 4 PM") }
    var scheduledReminderEnabled by remember { mutableStateOf(true) }

    var dropdownExpanded by remember { mutableStateOf(false) }
    var showPinDropForField by remember { mutableStateOf<String?>(null) }
    
    val appliedPromo by viewModel.appliedPromoCode.collectAsState()
    val discountPercent by viewModel.promoDiscountPercent.collectAsState()
    var promoInput by remember { mutableStateOf("") }

    var apiSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isSearchingSuggestions by remember { mutableStateOf(false) }

    val activeQuery = remember(pickup, delivery, additionalStops, focusedField) {
        when {
            focusedField == "pickup" -> pickup
            focusedField == "delivery" -> delivery
            focusedField?.startsWith("stop_") == true -> {
                val idx = focusedField!!.removePrefix("stop_").toIntOrNull() ?: 0
                additionalStops.getOrNull(idx) ?: ""
            }
            else -> ""
        }
    }

    var activeAutoDetectField by remember { mutableStateOf("pickup") }
    val coroutineScope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        coroutineScope.launch {
            Toast.makeText(context, "🎯 Auto-detecting location...", Toast.LENGTH_SHORT).show()
            val detected = withContext(Dispatchers.IO) {
                detectUserLocation(context)
            }
            if (detected.isNotBlank()) {
                when {
                    activeAutoDetectField == "pickup" -> pickup = detected
                    activeAutoDetectField == "delivery" -> delivery = detected
                    activeAutoDetectField.startsWith("stop_") -> {
                        val idx = activeAutoDetectField.removePrefix("stop_").toIntOrNull() ?: 0
                        val newList = additionalStops.toMutableList()
                        if (idx < newList.size) {
                            newList[idx] = detected
                            additionalStops = newList
                        }
                    }
                }
                if (granted) {
                    Toast.makeText(context, "Location Auto-Detected: $detected", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "GPS permission denied. Estimated: $detected", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Could not detect location. Please type manually.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val addressDatabase = remember {
        listOf(
            "King's Square (Ring Road), Benin City, Edo State",
            "University of Benin (UNIBEN), Ugbowo Campus, Benin City",
            "University of Benin Teaching Hospital (UBTH), Benin City",
            "Benin Airport, Airport Road, Benin City",
            "Ramat Park, Ikpoba Hill, Benin City",
            "Kada Plaza, Sapele Road, Benin City",
            "Edo State Government House, GRA, Benin City",
            "National Museum Benin City, Ring Road, Benin City",
            "UNIBEN Ekehuan Campus, Ekehuan Road, Benin City",
            "Oba of Benin Palace, Ring Road, Benin City",
            "Uselu Market, Benin-Lagos Expressway, Benin City",
            "Ogba Zoo and Nature Park, Airport Road, Benin City",
            "Stella Obasanjo Hospital, Sapele Road, Benin City",
            "Aduwawa Motor Park, Benin City"
        )
    }

    fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }

    fun findAddressMatches(query: String): List<String> {
        if (query.isBlank()) {
            val predictiveList = mutableListOf<String>()
            val home = viewModel.homeAddress.value
            if (home.isNotBlank() && home != "No. 1 Ring Road, Benin City") {
                predictiveList.add("🏠 Home: $home")
            }
            val work = viewModel.workAddress.value
            if (work.isNotBlank() && work != "University of Benin, Ugbowo Campus, Benin City") {
                predictiveList.add("💼 Work: $work")
            }
            predictiveList.addAll(listOf(
                "King's Square (Ring Road), Benin City, Edo State",
                "University of Benin (UNIBEN), Ugbowo Campus, Benin City",
                "Benin Airport, Airport Road, Benin City",
                "Kada Plaza, Sapele Road, Benin City",
                "Ramat Park, Ikpoba Hill, Benin City"
            ))
            return predictiveList.distinct()
        }
        val cleanQuery = query.lowercase().trim()
        val typoMap = mapOf(
            "airpt" to "airport",
            "arpt" to "airport",
            "mll" to "mall",
            "lekky" to "lekki",
            "leki" to "lekki",
            "unilag" to "university of lagos",
            "univ" to "university",
            "sdat" to "sdat cricket ground",
            "crick" to "cricket",
            "ashok" to "ashok nagar",
            "dlf" to "dlf cyber city"
        )
        var expandedQuery = cleanQuery
        for ((typo, replacement) in typoMap) {
            if (cleanQuery.contains(typo)) {
                expandedQuery = expandedQuery.replace(typo, replacement)
            }
        }
        
        // Exact and fuzzy database matches
        val matches = addressDatabase.filter { address ->
            val addrLower = address.lowercase()
            addrLower.contains(cleanQuery) || addrLower.contains(expandedQuery) ||
            cleanQuery.split(" ").any { word -> word.length > 2 && addrLower.contains(word) }
        }.toMutableList()

        // Levenshtein intelligent autocorrect matching for words in database
        if (matches.isEmpty()) {
            val queryWords = cleanQuery.split(" ")
            for (address in addressDatabase) {
                val addrWords = address.lowercase().split(" ", ",", "(", ")")
                for (qw in queryWords) {
                    if (qw.length >= 3) {
                        for (aw in addrWords) {
                            if (aw.length >= 3 && levenshteinDistance(qw, aw) <= 1) {
                                matches.add(address)
                                break
                            }
                        }
                    }
                }
            }
        }
        return matches.distinct()
    }

    LaunchedEffect(Unit) {
        viewModel.loadDraftFromPrefs(context)
    }

    LaunchedEffect(pickup) {
        if (pickup.isBlank()) {
            delay(1000L)
            if (pickup.isBlank()) {
                val detected = withContext(Dispatchers.IO) {
                    detectUserLocation(context)
                }
                if (detected.isNotBlank() && pickup.isBlank()) {
                    pickup = detected
                    Toast.makeText(context, "Location Auto-Detected: $detected", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(activeQuery) {
        if (activeQuery.isBlank() || activeQuery.length < 3) {
            apiSuggestions = emptyList()
            return@LaunchedEffect
        }
        
        delay(400L)
        isSearchingSuggestions = true
        
        withContext(Dispatchers.IO) {
            try {
                val token = try { com.example.BuildConfig.MAPBOX_ACCESS_TOKEN } catch (e: Throwable) { "" }
                if (token.isBlank() || token == "mapbox_access_token_placeholder") {
                    throw Exception("Mapbox token not configured")
                }
                val encodedQuery = java.net.URLEncoder.encode(activeQuery, "UTF-8")
                val url = java.net.URL("https://api.mapbox.com/geocoding/v5/mapbox.places/$encodedQuery.json?access_token=$token&country=ng&limit=5&proximity=6.3350,5.6037")
                val urlConnection = url.openConnection() as java.net.HttpURLConnection
                urlConnection.connectTimeout = 3000
                urlConnection.readTimeout = 3000
                val response = urlConnection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = org.json.JSONObject(response)
                val features = jsonObject.optJSONArray("features")
                val results = mutableListOf<String>()
                if (features != null) {
                    for (i in 0 until features.length()) {
                        val feat = features.getJSONObject(i)
                        val placeName = feat.optString("place_name")
                        if (!placeName.isNullOrBlank()) {
                            results.add(placeName)
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    apiSuggestions = results
                    isSearchingSuggestions = false
                }
            } catch (e: Exception) {
                android.util.Log.e("AddressSearch", "Mapbox search failed: ${e.message}. Trying OSM Nominatim...")
                try {
                    val encodedQuery = java.net.URLEncoder.encode(activeQuery, "UTF-8")
                    val url = java.net.URL("https://nominatim.openstreetmap.org/search?format=json&q=$encodedQuery&addressdetails=1&limit=5&countrycodes=ng")
                    val urlConnection = url.openConnection() as java.net.HttpURLConnection
                    urlConnection.setRequestProperty("User-Agent", "EngracedDispatchAndroidApp/1.0 (reachheytek@gmail.com)")
                    urlConnection.connectTimeout = 3000
                    urlConnection.readTimeout = 3000
                    val response = urlConnection.inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = org.json.JSONArray(response)
                    val results = mutableListOf<String>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val displayName = obj.optString("display_name")
                        if (!displayName.isNullOrBlank()) {
                            results.add(displayName)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        apiSuggestions = results
                        isSearchingSuggestions = false
                    }
                } catch (err: Exception) {
                    android.util.Log.e("AddressSearch", "OSM Nominatim failed: ${err.message}")
                    withContext(Dispatchers.Main) {
                        apiSuggestions = findAddressMatches(activeQuery)
                        isSearchingSuggestions = false
                    }
                }
            }
        }
    }

    LaunchedEffect(draft) {
        if (pickup.isEmpty()) pickup = draft.pickupAddress
        if (delivery.isEmpty()) delivery = draft.deliveryAddress
        if (additionalStops.isEmpty() && draft.stops.isNotEmpty()) {
            additionalStops = draft.stops
        }
        quantity = draft.quantity.toString()
        weight = draft.weight.toString()
        length = draft.length.toString()
        width = draft.width.toString()
        height = draft.height.toString()
        sName = draft.senderName
        sPhone = draft.senderPhone
        rName = draft.receiverName
        rPhone = draft.receiverPhone
    }

    LaunchedEffect(pickup, delivery, additionalStops, quantity, weight, length, width, height, sName, sPhone, rName, rPhone) {
        viewModel.updateDraftPickup(pickup)
        viewModel.updateDraftDelivery(delivery)
        viewModel.updateDraftAdditionalStops(additionalStops)
        viewModel.updateDraftSpecs(
            quantity = quantity.toIntOrNull() ?: 1,
            weight = weight.toDoubleOrNull() ?: 1.0,
            length = length.toIntOrNull() ?: 20,
            width = width.toIntOrNull() ?: 15,
            height = height.toIntOrNull() ?: 10
        )
        viewModel.updateDraftSenderInfo(sName, sPhone)
        viewModel.updateDraftReceiverInfo(rName, rPhone)
        viewModel.finalizeDraftPrice(draft.selectedService)
        viewModel.saveDraftToPrefs(context)
    }

    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val isDark = !isLight
    val fieldBgColor = Charcoal
    val fieldTextColor = if (isLight) Obsidian else Color.White
    val fieldBorderColor = if (isLight) Slate else Gold.copy(alpha = 0.3f)
    val accentIconColor = if (isLight) Obsidian else Gold
    val accentTextColor = if (isLight) Obsidian else Gold

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HeaderBgColor)
        ) {
            ScreenHeader(
                title = "Send Parcel Details",
                onBack = { onNavigate("SendParcel") }
            )

            RoundedSheet(
                modifier = Modifier.weight(1f),
                containerColor = if (isDark) BackgroundDark else BackgroundLight
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 14.dp, vertical = 24.dp)
                        .padding(bottom = 120.dp)
                ) {
                    // --- Book Again suggestions using delivery history ---
                    val userParcels by viewModel.parcels.collectAsState()
                    val bookAgainList = remember(userParcels) {
                        userParcels.filter { it.deliveryAddress.isNotBlank() && it.receiverName.isNotBlank() }
                            .map { Triple(it.deliveryAddress, it.receiverName, it.receiverPhone) }
                            .distinctBy { it.first }
                            .take(4)
                    }

                    if (bookAgainList.isNotEmpty()) {
                        Text(
                            text = "Book Again (Recent Deliveries)",
                            fontSize = 12.sp,
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = accentTextColor,
                            modifier = Modifier.padding(bottom = 10.dp, top = 4.dp)
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            bookAgainList.forEach { (addr, name, phone) ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Charcoal),
                                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.15f)),
                                    modifier = Modifier
                                        .width(200.dp)
                                        .clickable {
                                            delivery = addr
                                            rName = name
                                            rPhone = phone
                                            Toast.makeText(context, "Recipient details loaded!", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.History, null, tint = Gold, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            addr,
                                            fontSize = 10.sp,
                                            color = TextGray,
                                            maxLines = 2,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Address Inputs Card
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    /*
                     * BACKEND INTEGRATION NOTE (AUTO-ADDRESS DETECTOR):
                     * As the user types in these fields, real-time address predictions and location detection 
                     * should be triggered via the Geocoding/Places API (e.g., Google Places Autocomplete or 
                     * Radar/Mapbox Search SDK).
                     * 
                     * TO IMPLEMENT:
                     * 1. Add a dynamic onValueChange listener query state.
                     * 2. Debounce keystrokes (e.g., 300ms delay) using a coroutine flow to optimize API request usage.
                     * 3. Send query coordinates and text to the backend autocomplete endpoint.
                     * 4. Pop up a dropdown list (using ExposedDropdownMenuBox or a custom lazy column popup) 
                     *    displaying matching results relative to the user's current GPS location.
                     * 5. When selected, update the fields and geocode the exact lat/lng into the selectedParcel coordinate system.
                     */
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Pickup & Delivery Details",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentTextColor,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Pickup Field
                            Column {
                                OutlinedTextField(
                                    value = pickup,
                                    onValueChange = { 
                                        pickup = it 
                                        focusedField = "pickup"
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onFocusChanged { if (it.isFocused) focusedField = "pickup" },
                                    shape = RoundedCornerShape(20.dp),
                                    placeholder = { Text("Pickup address", color = TextGray) },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Place, null, tint = accentIconColor, modifier = Modifier.size(22.dp))
                                    },
                                    trailingIcon = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = {
                                                activeAutoDetectField = "pickup"
                                                permissionLauncher.launch(
                                                    arrayOf(
                                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                                    )
                                                )
                                            }) {
                                                Icon(Icons.Filled.MyLocation, "Auto-detect location", tint = Gold, modifier = Modifier.size(20.dp))
                                            }
                                            IconButton(onClick = {
                                                showPinDropForField = "pickup"
                                            }) {
                                                Icon(Icons.Filled.Map, "Choose on map", tint = Gold, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    },
                                    textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (isLight) Obsidian else Gold,
                                        unfocusedBorderColor = fieldBorderColor,
                                        focusedContainerColor = fieldBgColor,
                                        unfocusedContainerColor = fieldBgColor,
                                        focusedTextColor = fieldTextColor,
                                        unfocusedTextColor = fieldTextColor,
                                        focusedPlaceholderColor = TextGray,
                                        unfocusedPlaceholderColor = TextGray
                                    )
                                )

                                // Predict frequently used pickup locations (Tap to apply)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(top = 6.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = Gold.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f)),
                                        modifier = Modifier.clickable {
                                            permissionLauncher.launch(
                                                arrayOf(
                                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Place,
                                                contentDescription = null,
                                                tint = Gold,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                "Detect Location",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isLight) Obsidian else GoldLight
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(2.dp))

                                    Text("⚡ Frequent:", fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.Bold)
                                    listOf("The Palms Mall", "Ikeja City Mall").forEach { freq ->
                                        Surface(
                                            color = Gold.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.2f)),
                                            modifier = Modifier.clickable {
                                                pickup = if (freq == "The Palms Mall") "The Palms Shopping Mall, Bisway Road, Lekki, Lagos" else "Ikeja City Mall, Obafemi Awolowo Way, Ikeja, Lagos"
                                                focusedField = null
                                            }
                                        ) {
                                            Text(
                                                freq,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isLight) Obsidian else GoldLight,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Dynamic Additional Stops (Multi-stop deliveries)
                            additionalStops.forEachIndexed { index, stopAddress ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = stopAddress,
                                        onValueChange = { newText ->
                                            additionalStops = additionalStops.toMutableList().apply { set(index, newText) }
                                            focusedField = "stop_$index"
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .onFocusChanged { if (it.isFocused) focusedField = "stop_$index" },
                                        shape = RoundedCornerShape(20.dp),
                                        placeholder = { Text("Delivery stop ${index + 1}", color = TextGray) },
                                        leadingIcon = {
                                            Icon(Icons.Filled.AddLocation, null, tint = Gold, modifier = Modifier.size(20.dp))
                                        },
                                        trailingIcon = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(onClick = {
                                                    activeAutoDetectField = "stop_$index"
                                                    permissionLauncher.launch(
                                                        arrayOf(
                                                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                                                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                                                        )
                                                    )
                                                }) {
                                                    Icon(Icons.Filled.MyLocation, "Auto-detect location", tint = Gold, modifier = Modifier.size(20.dp))
                                                }
                                                IconButton(onClick = {
                                                    showPinDropForField = "stop_$index"
                                                }) {
                                                    Icon(Icons.Filled.Map, "Choose on map", tint = Gold, modifier = Modifier.size(18.dp))
                                                }
                                                IconButton(onClick = {
                                                    additionalStops = additionalStops.toMutableList().apply { removeAt(index) }
                                                }) {
                                                    Icon(Icons.Filled.Close, "Remove stop", tint = Color.Red, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        },
                                        textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = if (isLight) Obsidian else Gold,
                                            unfocusedBorderColor = fieldBorderColor,
                                            focusedContainerColor = fieldBgColor,
                                            unfocusedContainerColor = fieldBgColor,
                                            focusedTextColor = fieldTextColor,
                                            unfocusedTextColor = fieldTextColor,
                                            focusedPlaceholderColor = TextGray,
                                            unfocusedPlaceholderColor = TextGray
                                        )
                                    )
                                }
                            }

                            // Final Delivery Destination Field
                            OutlinedTextField(
                                value = delivery,
                                onValueChange = { 
                                    delivery = it 
                                    focusedField = "delivery"
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { if (it.isFocused) focusedField = "delivery" },
                                shape = RoundedCornerShape(20.dp),
                                placeholder = { Text("Final delivery address", color = TextGray) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Navigation, null, tint = accentIconColor, modifier = Modifier.size(22.dp))
                                },
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = {
                                            activeAutoDetectField = "delivery"
                                            permissionLauncher.launch(
                                                arrayOf(
                                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        }) {
                                            Icon(Icons.Filled.MyLocation, "Auto-detect location", tint = Gold, modifier = Modifier.size(20.dp))
                                        }
                                        IconButton(onClick = {
                                            showPinDropForField = "delivery"
                                        }) {
                                            Icon(Icons.Filled.Map, "Choose on map", tint = Gold, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                },
                                textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isLight) Obsidian else Gold,
                                    unfocusedBorderColor = fieldBorderColor,
                                    focusedContainerColor = fieldBgColor,
                                    unfocusedContainerColor = fieldBgColor,
                                    focusedTextColor = fieldTextColor,
                                    unfocusedTextColor = fieldTextColor,
                                    focusedPlaceholderColor = TextGray,
                                    unfocusedPlaceholderColor = TextGray
                                )
                            )

                            // Multi-stop Action trigger
                            TextButton(
                                onClick = {
                                    additionalStops = additionalStops + ""
                                },
                                modifier = Modifier.align(Alignment.Start)
                            ) {
                                Icon(Icons.Filled.Add, null, tint = if (isDark) Gold else Obsidian, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Add Delivery Stop (Multi-Stop)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Gold else Obsidian
                                )
                            }

                            // AI Address Auto-Suggestions Dropdown Popup
                            val query = when {
                                focusedField == "pickup" -> pickup
                                focusedField == "delivery" -> delivery
                                focusedField?.startsWith("stop_") == true -> {
                                    val idx = focusedField!!.removePrefix("stop_").toIntOrNull() ?: 0
                                    additionalStops.getOrNull(idx) ?: ""
                                }
                                else -> ""
                            }
                            val suggestions = if (activeQuery.isNotBlank() && activeQuery.length >= 3) {
                                if (apiSuggestions.isNotEmpty()) apiSuggestions else findAddressMatches(activeQuery)
                            } else {
                                findAddressMatches(activeQuery)
                            }
                            if (suggestions.isNotEmpty() || isSearchingSuggestions) {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isDark) MapStandardBg else GoldenWhite),
                                    border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.25f) else Slate),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                if (isSearchingSuggestions) "🔍 Searching locations..." else "💡 AI Suggestion Matches:",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDark) Gold else Obsidian
                                            )
                                            if (isSearchingSuggestions) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                CircularProgressIndicator(
                                                    color = if (isDark) Gold else Obsidian,
                                                    modifier = Modifier.size(10.dp),
                                                    strokeWidth = 1.5.dp
                                                )
                                            }
                                        }
                                        suggestions.take(5).forEach { rawMatch ->
                                            val isHome = rawMatch.startsWith("🏠 Home: ")
                                            val isWork = rawMatch.startsWith("💼 Work: ")
                                            val cleanMatch = when {
                                                isHome -> rawMatch.removePrefix("🏠 Home: ")
                                                isWork -> rawMatch.removePrefix("💼 Work: ")
                                                else -> rawMatch
                                            }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        when {
                                                            focusedField == "pickup" -> pickup = cleanMatch
                                                            focusedField == "delivery" -> delivery = cleanMatch
                                                            focusedField?.startsWith("stop_") == true -> {
                                                                val idx = focusedField!!.removePrefix("stop_").toIntOrNull() ?: 0
                                                                if (idx in additionalStops.indices) {
                                                                    additionalStops = additionalStops.toMutableList().apply { set(idx, cleanMatch) }
                                                                }
                                                            }
                                                        }
                                                        focusedField = null
                                                    }
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val iconVector = when {
                                                    isHome -> Icons.Filled.Home
                                                    isWork -> Icons.Filled.Work
                                                    else -> Icons.Filled.Place
                                                 }
                                                 Icon(iconVector, null, tint = if (isDark) Gold else Obsidian, modifier = Modifier.size(16.dp))
                                                 Spacer(modifier = Modifier.width(8.dp))
                                                 Text(rawMatch, color = if (isDark) Color.White else Obsidian, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Parcel Specs Card
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Parcel Specifications",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentTextColor,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { quantity = it },
                                modifier = Modifier
                                    .weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                label = { Text("Qty", color = TextGray) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isLight) Obsidian else Gold,
                                    unfocusedBorderColor = fieldBorderColor,
                                    focusedContainerColor = fieldBgColor,
                                    unfocusedContainerColor = fieldBgColor,
                                    focusedTextColor = fieldTextColor,
                                    unfocusedTextColor = fieldTextColor,
                                    focusedLabelColor = if (isLight) Obsidian else Gold,
                                    unfocusedLabelColor = TextGray
                                )
                            )

                            OutlinedTextField(
                                value = weight,
                                onValueChange = { weight = it },
                                modifier = Modifier
                                    .weight(1.5f),
                                shape = RoundedCornerShape(20.dp),
                                label = { Text("Weight (kg)", color = TextGray) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isLight) Obsidian else Gold,
                                    unfocusedBorderColor = fieldBorderColor,
                                    focusedContainerColor = fieldBgColor,
                                    unfocusedContainerColor = fieldBgColor,
                                    focusedTextColor = fieldTextColor,
                                    unfocusedTextColor = fieldTextColor,
                                    focusedLabelColor = if (isLight) Obsidian else Gold,
                                    unfocusedLabelColor = TextGray
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = length,
                                onValueChange = { length = it },
                                modifier = Modifier
                                    .weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                label = { Text("L (cm)", color = TextGray) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isLight) Obsidian else Gold,
                                    unfocusedBorderColor = fieldBorderColor,
                                    focusedContainerColor = fieldBgColor,
                                    unfocusedContainerColor = fieldBgColor,
                                    focusedTextColor = fieldTextColor,
                                    unfocusedTextColor = fieldTextColor,
                                    focusedLabelColor = if (isLight) Obsidian else Gold,
                                    unfocusedLabelColor = TextGray
                                )
                            )
                            OutlinedTextField(
                                value = width,
                                onValueChange = { width = it },
                                modifier = Modifier
                                    .weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                label = { Text("W (cm)", color = TextGray) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isLight) Obsidian else Gold,
                                    unfocusedBorderColor = fieldBorderColor,
                                    focusedContainerColor = fieldBgColor,
                                    unfocusedContainerColor = fieldBgColor,
                                    focusedTextColor = fieldTextColor,
                                    unfocusedTextColor = fieldTextColor,
                                    focusedLabelColor = if (isLight) Obsidian else Gold,
                                    unfocusedLabelColor = TextGray
                                )
                            )
                            OutlinedTextField(
                                value = height,
                                onValueChange = { height = it },
                                modifier = Modifier
                                    .weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                label = { Text("H (cm)", color = TextGray) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isLight) Obsidian else Gold,
                                    unfocusedBorderColor = fieldBorderColor,
                                    focusedContainerColor = fieldBgColor,
                                    unfocusedContainerColor = fieldBgColor,
                                    focusedTextColor = fieldTextColor,
                                    unfocusedTextColor = fieldTextColor,
                                    focusedLabelColor = if (isLight) Obsidian else Gold,
                                    unfocusedLabelColor = TextGray
                                )
                            )
                        }

                        // Smart parcel size recommendations banner
                        val recommendedSize = remember(weight) {
                            val wVal = weight.toDoubleOrNull() ?: 1.0
                            when {
                                wVal <= 0.5 -> Triple(20, 15, 2) // Small Documents
                                wVal <= 2.0 -> Triple(30, 25, 5) // Medium Apparel
                                wVal <= 5.0 -> Triple(35, 30, 15) // Electronics/Standard
                                else -> Triple(50, 40, 30) // Large package
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            color = if (isDark) Gold.copy(alpha = 0.08f) else GoldLight.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.2f) else Slate),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    length = recommendedSize.first.toString()
                                    width = recommendedSize.second.toString()
                                    height = recommendedSize.third.toString()
                                    Toast.makeText(context, "AI Recommended Dimensions Applied!", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isDark) Gold else Obsidian,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Smart AI Size Recommendation",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isDark) Gold else Obsidian
                                    )
                                    Text(
                                        text = "Based on ${weight}kg: Suggesting ${recommendedSize.first}x${recommendedSize.second}x${recommendedSize.third} cm. Tap to apply.",
                                        fontSize = 11.sp,
                                        color = TextGray,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Insurance Selection Card
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Delivery Insurance Protection",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentTextColor,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        listOf(
                            Triple("none", "Standard (No Cover) - ₦0", "Send at your own risk. Basic delivery without additional damage protection."),
                            Triple("basic", "Basic Shield - ₦250", "Covers up to ₦20,000 for parcel damage or loss during transit."),
                            Triple("premium", "Premium Secure Shield - ₦1,000", "Full comprehensive coverage up to ₦150,000. Recommended for valuable packages or electronics.")
                        ).forEach { (type, label, desc) ->
                            val isSelected = selectedInsurance == type
                            Surface(
                                color = if (isSelected) Gold.copy(alpha = 0.15f) else Color.Transparent,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Gold else fieldBorderColor
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedInsurance = type }
                                    .padding(vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedInsurance = type },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = Gold,
                                                unselectedColor = TextGray
                                            )
                                        )
                                        Text(
                                            text = label,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) GoldLight else Color.White
                                        )
                                    }
                                    Text(
                                        text = desc,
                                        fontSize = 11.sp,
                                        color = if (isSelected) TextGray else TextGray,
                                        modifier = Modifier.padding(start = 36.dp, top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Contacts Card
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Sender Contact Details", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = accentTextColor)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = sName,
                                onValueChange = { sName = it },
                                modifier = Modifier
                                    .weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                placeholder = { Text("Sender Name", color = TextGray) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isLight) Obsidian else Gold,
                                    unfocusedBorderColor = fieldBorderColor,
                                    focusedContainerColor = fieldBgColor,
                                    unfocusedContainerColor = fieldBgColor,
                                    focusedTextColor = fieldTextColor,
                                    unfocusedTextColor = fieldTextColor,
                                    focusedPlaceholderColor = TextGray,
                                    unfocusedPlaceholderColor = TextGray
                                )
                            )
                            OutlinedTextField(
                                value = sPhone,
                                onValueChange = { sPhone = it },
                                modifier = Modifier
                                    .weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                placeholder = { Text("Sender Phone", color = TextGray) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isLight) Obsidian else Gold,
                                    unfocusedBorderColor = fieldBorderColor,
                                    focusedContainerColor = fieldBgColor,
                                    unfocusedContainerColor = fieldBgColor,
                                    focusedTextColor = fieldTextColor,
                                    unfocusedTextColor = fieldTextColor,
                                    focusedPlaceholderColor = TextGray,
                                    unfocusedPlaceholderColor = TextGray
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text("Receiver Contact Details", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = accentTextColor)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = rName,
                                onValueChange = { rName = it },
                                modifier = Modifier
                                    .weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                placeholder = { Text("Receiver Name", color = TextGray) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isLight) Obsidian else Gold,
                                    unfocusedBorderColor = fieldBorderColor,
                                    focusedContainerColor = fieldBgColor,
                                    unfocusedContainerColor = fieldBgColor,
                                    focusedTextColor = fieldTextColor,
                                    unfocusedTextColor = fieldTextColor,
                                    focusedPlaceholderColor = TextGray,
                                    unfocusedPlaceholderColor = TextGray
                                )
                            )
                            OutlinedTextField(
                                value = rPhone,
                                onValueChange = { rPhone = it },
                                modifier = Modifier
                                    .weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                placeholder = { Text("Receiver Phone", color = TextGray) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isLight) Obsidian else Gold,
                                    unfocusedBorderColor = fieldBorderColor,
                                    focusedContainerColor = fieldBgColor,
                                    unfocusedContainerColor = fieldBgColor,
                                    focusedTextColor = fieldTextColor,
                                    unfocusedTextColor = fieldTextColor,
                                    focusedPlaceholderColor = TextGray,
                                    unfocusedPlaceholderColor = TextGray
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Scheduled Deliveries & Smart Reminders Card
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Schedule Booking Delivery",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = accentTextColor
                                )
                                Text(
                                    text = "Book now and dispatch later",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }
                            Switch(
                                checked = isScheduled,
                                onCheckedChange = { isScheduled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Obsidian,
                                    checkedTrackColor = Gold,
                                    uncheckedThumbColor = TextGray,
                                    uncheckedTrackColor = BorderDark
                                )
                            )
                        }

                        if (isScheduled) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Select Scheduled Date & Time:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            // Horizontal schedule slots
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf("Today, 4 PM", "Tomorrow, 10 AM", "Tomorrow, 2 PM").forEach { slot ->
                                    val isSelected = scheduledDate == slot
                                    Surface(
                                        color = if (isSelected) Gold.copy(alpha = 0.15f) else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, if (isSelected) Gold else fieldBorderColor),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { scheduledDate = slot }
                                    ) {
                                        Text(
                                            text = slot,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) GoldLight else TextGray,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Smart reminders check box
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { scheduledReminderEnabled = !scheduledReminderEnabled }
                                    .background(Charcoal, RoundedCornerShape(14.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (scheduledReminderEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                    contentDescription = null,
                                    tint = if (scheduledReminderEnabled) Gold else TextGray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Smart Reminder Notification",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (scheduledReminderEnabled) GoldLight else Color.White
                                    )
                                    Text(
                                        text = "We will alert you 1 hour before pickup.",
                                        fontSize = 10.sp,
                                        color = TextGray
                                    )
                                }
                                Checkbox(
                                    checked = scheduledReminderEnabled,
                                    onCheckedChange = { scheduledReminderEnabled = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Gold,
                                        checkmarkColor = Obsidian,
                                        uncheckedColor = TextGray
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Charcoal,
                            border = BorderStroke(1.2.dp, Gold.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Gold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Apply Promo Voucher",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                if (appliedPromo != null) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Gold.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "CODE: $appliedPromo",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 13.sp,
                                                color = Gold
                                            )
                                            Text(
                                                text = "$discountPercent% Discount Applied Successfully!",
                                                fontSize = 11.sp,
                                                color = Color.Green
                                            )
                                        }
                                        IconButton(onClick = { viewModel.clearAppliedPromo() }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove Promo",
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = promoInput,
                                            onValueChange = { promoInput = it },
                                            placeholder = { Text("Enter Promo Code", color = TextGray) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Gold,
                                                unfocusedBorderColor = Gold.copy(alpha = 0.3f),
                                                focusedContainerColor = Obsidian,
                                                unfocusedContainerColor = Obsidian,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            singleLine = true
                                        )
                                        Button(
                                            onClick = {
                                                if (promoInput.isNotBlank()) {
                                                    viewModel.applyPromoCode(promoInput) { success, msg ->
                                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                        if (success) {
                                                            promoInput = ""
                                                        }
                                                    }
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Gold,
                                                contentColor = Obsidian
                                            ),
                                            modifier = Modifier.height(56.dp)
                                        ) {
                                            Text("Apply", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        // Bottom CTA Get Instant Quote Button
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Charcoal,
            tonalElevation = 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        if (pickup.isBlank() || delivery.isBlank()) {
                            Toast.makeText(context, "Please enter pickup and delivery addresses", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (sName.isBlank() || sPhone.isBlank()) {
                            Toast.makeText(context, "Please fill in sender information", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (rName.isBlank() || rPhone.isBlank()) {
                            Toast.makeText(context, "Please fill in receiver information", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        viewModel.updateDraftPickup(pickup)
                        viewModel.updateDraftDelivery(delivery)
                        viewModel.updateDraftAdditionalStops(additionalStops)
                        viewModel.updateDraftSpecs(
                            quantity.toIntOrNull() ?: 1,
                            weight.toDoubleOrNull() ?: 1.0,
                            length.toIntOrNull() ?: 10,
                            width.toIntOrNull() ?: 10,
                            height.toIntOrNull() ?: 10
                        )
                        viewModel.updateDraftSenderInfo(sName, sPhone)
                        viewModel.updateDraftReceiverInfo(rName, rPhone)
                        onNavigate("BookingSelection")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Obsidian),
                    border = BorderStroke(1.2.dp, Gold)
                ) {
                    Text("Get Instant Quote", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Gold)
                }
            }
        }

        if (showPinDropForField != null) {
            MapPinDropDialog(
                initialAddress = if (showPinDropForField == "pickup") pickup else delivery,
                onAddressSelected = { selectedAddr ->
                    if (showPinDropForField == "pickup") {
                        pickup = selectedAddr
                    } else {
                        delivery = selectedAddr
                    }
                    showPinDropForField = null
                },
                onDismiss = { showPinDropForField = null }
            )
        }
    }
}

@Composable
fun MapPinDropDialog(
    initialAddress: String,
    onAddressSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val landmarks = remember {
        listOf(
            Triple("Ikeja City Mall, Obafemi Awolowo Way, Ikeja, Lagos", 120f, -100f),
            Triple("Murtala Muhammed International Airport (LOS), Airport Road, Ikeja, Lagos", 0f, 0f),
            Triple("Lekki Conservation Centre, Lekki-Epe Expressway, Lagos", 100f, 150f),
            Triple("National Theatre, Iganmu, Surulere, Lagos", -150f, 100f),
            Triple("University of Lagos, Akoka, Yaba, Lagos", -80f, -120f),
            Triple("Central Business District, Abuja", -200f, -200f)
        )
    }

    val nearestLandmark = remember(offsetX, offsetY) {
        landmarks.minByOrNull { (_, x, y) ->
            val dx = offsetX - x
            val dy = offsetY - y
            dx * dx + dy * dy
        }?.first ?: "Custom Pin-Drop Location"
    }

    val transition = rememberInfiniteTransition("pinBounce")
    val pinBounce by transition.animateFloat(
        initialValue = -12f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.border(1.5.dp, Gold, RoundedCornerShape(28.dp)),
        confirmButton = {
            Button(
                onClick = { onAddressSelected(nearestLandmark) },
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Select Location", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        },
        title = {
            Text(
                "Drag Map to Drop Pin",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Gold,
                fontFamily = SpaceGrotesk
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Move your finger over the grid map below to reposition the delivery pin. The nearest Lagos address is automatically detected.",
                    fontSize = 11.sp,
                    color = TextGray
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BackgroundDark)
                        .border(1.dp, Gold.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val cx = w / 2f
                        val cy = h / 2f

                        drawRect(MapStandardBg)

                        val xStep = 30.dp.toPx()
                        var xLine = (offsetX % xStep)
                        while (xLine < w) {
                            drawLine(
                                color = MapBlock,
                                start = Offset(xLine, 0f),
                                end = Offset(xLine, h),
                                strokeWidth = 1.dp.toPx()
                            )
                            xLine += xStep
                        }

                        val yStep = 30.dp.toPx()
                        var yLine = (offsetY % yStep)
                        while (yLine < h) {
                            drawLine(
                                color = MapBlock,
                                start = Offset(0f, yLine),
                                end = Offset(w, yLine),
                                strokeWidth = 1.dp.toPx()
                            )
                            yLine += yStep
                        }

                        landmarks.forEach { (_, lx, ly) ->
                            val drawX = cx + lx + offsetX
                            val drawY = cy + ly + offsetY
                            
                            if (drawX in 0f..w && drawY in 0f..h) {
                                drawCircle(
                                    color = SuccessGreen.copy(alpha = 0.15f),
                                    radius = 24.dp.toPx(),
                                    center = Offset(drawX, drawY)
                               )
                                drawCircle(
                                    color = SuccessGreen,
                                    radius = 4.dp.toPx(),
                                    center = Offset(drawX, drawY)
                                )
                            }
                        }

                        drawLine(
                            color = TextGray.copy(alpha = 0.3f),
                            start = Offset(cx - 15.dp.toPx(), cy),
                            end = Offset(cx + 15.dp.toPx(), cy),
                            strokeWidth = 1.5.dp.toPx()
                        )
                        drawLine(
                            color = TextGray.copy(alpha = 0.3f),
                            start = Offset(cx, cy - 15.dp.toPx()),
                            end = Offset(cx, cy + 15.dp.toPx()),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }

                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = "Pin Drop",
                        tint = Gold,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = pinBounce.dp)
                            .size(36.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Charcoal,
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "DETECTED NEAREST ADDRESS:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold
                            )
                            Text(
                                text = nearestLandmark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        containerColor = BackgroundDark,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun BookingDetails(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val selectedParcel by viewModel.selectedParcel.collectAsState()
    val parcel = selectedParcel
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark
    val isLight = !isDark
    val adaptiveGoldText = if (isDark) Gold else Obsidian
    val adaptiveGoldIcon = if (isDark) Gold else Obsidian

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HeaderBgColor)
        ) {
            ScreenHeader(
                title = "Shipment Details",
                onBack = { onNavigate("Dashboard") }
            )

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = if (isDark) BackgroundDark else BackgroundLight
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 14.dp, vertical = 24.dp)
                        .padding(bottom = 120.dp)
                ) {
                if (parcel == null) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Shipment Selected", fontWeight = FontWeight.Bold, color = TextGray)
                    }
                } else {
                    // Main Info Card
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Charcoal,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Tracking ID",
                                        fontSize = 12.sp,
                                        color = TextGray,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "#${parcel.id}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = AppTextColor
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Gold.copy(alpha = 0.15f),
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Text(
                                        text = parcel.status.name,
                                        color = Gold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = parcel.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = parcel.itemName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AppTextColor
                                    )
                                    Text(
                                        text = "${parcel.weight} kg • ${parcel.quantity} pcs",
                                        fontSize = 13.sp,
                                        color = TextGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Addresses Card
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Charcoal,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Route Information",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = adaptiveGoldText,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Filled.Place, null, tint = adaptiveGoldIcon, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Pickup Location", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Bold)
                                    Text(parcel.pickupAddress, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Filled.Navigation, null, tint = adaptiveGoldIcon, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Delivery Destination", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Bold)
                                    Text(parcel.deliveryAddress, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // People Card
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Charcoal,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Contact Persons",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = adaptiveGoldText,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Sender", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Bold)
                                    Text(parcel.senderName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                                    Text(parcel.senderPhone, fontSize = 13.sp, color = TextGray, fontWeight = FontWeight.Medium)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Receiver", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Bold)
                                    Text(parcel.receiverName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                                    Text(parcel.receiverPhone, fontSize = 13.sp, color = TextGray, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Financials
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Charcoal,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Paid Amount", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = AppTextColor)
                            Text(
                                text = "₦${String.format("%,.2f", parcel.price)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isLight) Obsidian else Gold
                            )
                        }
                    }
                }
            }
        }
    }

        // Live Map Tracking CTA at bottom
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Charcoal,
            tonalElevation = 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = { onNavigate("ActiveTracking") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Obsidian),
                    border = BorderStroke(1.2.dp, Gold)
                ) {
                    Icon(Icons.Filled.Place, null, tint = Gold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Track on Live Map", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Gold)
                }
            }
        }
    }
}
