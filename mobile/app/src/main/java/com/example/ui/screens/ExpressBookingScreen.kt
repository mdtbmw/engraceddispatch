package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.components.RoundedSheet
import com.example.ui.components.ScreenHeader
import com.example.ui.components.WalletCheckoutSheet
import com.example.viewmodel.DeliveryViewModel
import com.example.viewmodel.PendingQuote
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.MyLocation

@Composable
fun ExpressBookingScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val draft by viewModel.parcelDraft.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var pickup by remember { mutableStateOf(draft.pickupAddress) }
    var delivery by remember { mutableStateOf(draft.deliveryAddress) }

    var activeAutoDetectField by remember { mutableStateOf("pickup") }
    
    val appliedPromo by viewModel.appliedPromoCode.collectAsState()
    val discountPercent by viewModel.promoDiscountPercent.collectAsState()
    var promoInput by remember { mutableStateOf("") }

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
                if (activeAutoDetectField == "pickup") {
                    pickup = detected
                } else {
                    delivery = detected
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

    var itemName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Electronics") }
    var weight by remember { mutableStateOf("1.5") }
    var declaredValue by remember { mutableStateOf("") }
    var specialInstructions by remember { mutableStateOf("") }
    var isInstantSpeed by remember { mutableStateOf(true) }
    var showCheckoutSheet by remember { mutableStateOf(false) }
    var showPaystackSheet by remember { mutableStateOf(false) }
    var pendingAmount by remember { mutableStateOf(0.0) }

    // Suggestions & Autocomplete state
    var focusedField by remember { mutableStateOf<String?>(null) } // pickup, delivery
    var apiSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isSearchingSuggestions by remember { mutableStateOf(false) }

    val activeQuery = remember(pickup, delivery, focusedField) {
        when (focusedField) {
            "pickup" -> pickup
            "delivery" -> delivery
            else -> ""
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
        
        val matches = addressDatabase.filter { address ->
            val addrLower = address.lowercase()
            addrLower.contains(cleanQuery) || addrLower.contains(expandedQuery) ||
            cleanQuery.split(" ").any { word -> word.length > 2 && addrLower.contains(word) }
        }.toMutableList()

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

    LaunchedEffect(draft) {
        if (pickup.isEmpty()) pickup = draft.pickupAddress
        if (delivery.isEmpty()) delivery = draft.deliveryAddress
    }

    LaunchedEffect(pickup, delivery) {
        viewModel.updateDraftPickup(pickup)
        viewModel.updateDraftDelivery(delivery)
    }

    LaunchedEffect(activeQuery) {
        if (activeQuery.isBlank() || activeQuery.length < 3) {
            apiSuggestions = emptyList()
            return@LaunchedEffect
        }
        
        kotlinx.coroutines.delay(400L)
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

    // Generate next 7 days for chronological timeline selector
    val calendar = Calendar.getInstance()
    val dates = remember {
        List(7) { index ->
            val day = calendar.clone() as Calendar
            day.add(Calendar.DAY_OF_YEAR, index)
            day.time
        }
    }
    var selectedDate by remember { mutableStateOf(dates[0]) }

    val categories = listOf("Electronics", "Documents", "Apparel", "Others")

    // Pricing Calculation
    val pendingQuote by viewModel.pendingQuote.collectAsState()
    val expressSurcharge by viewModel.expressSurcharge.collectAsState()

    LaunchedEffect(pickup, delivery, weight, isInstantSpeed) {
        if (pickup.isNotBlank() && delivery.isNotBlank() && pickup.length >= 6 && delivery.length >= 6) {
            viewModel.calculateDynamicPriceAsync(
                serviceType = "Express",
                pickup = pickup,
                delivery = delivery,
                weight = weight.toDoubleOrNull() ?: 1.0,
                quantity = 1,
                length = 20,
                width = 15,
                height = 10,
                stopsCount = 0,
                insuranceType = "none",
                speed = if (isInstantSpeed) "instant" else "standard"
            )
        } else {
            viewModel.clearQuote()
        }
    }

    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val fieldBgColor = if (isLight) GoldenWhiteLight else Charcoal
    val fieldTextColor = if (isLight) Obsidian else Color.White
    val fieldBorderColor = if (isLight) Slate else Gold.copy(alpha = 0.3f)
    val accentColor = if (isLight) Obsidian else Gold
    val accentIconColor = if (isLight) Obsidian else Gold

    val scrollState = rememberScrollState()

    val isDark = !isLight

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
                title = "Express Booking",
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
                        .padding(bottom = 140.dp) // extra space for bottom CTA bar
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
                        color = if (isLight) Obsidian else Gold,
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

                // Route Info Section
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    border = BorderStroke(1.dp, if (isDark) BorderDark else BorderLight),
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Route Addresses",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = accentColor,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = pickup,
                            onValueChange = { 
                                pickup = it 
                                focusedField = "pickup"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged {
                                    if (it.isFocused) focusedField = "pickup"
                                },
                            shape = RoundedCornerShape(20.dp),
                            placeholder = { Text("Pickup Location", color = TextGray) },
                            leadingIcon = { Icon(Icons.Filled.Place, null, tint = accentIconColor) },
                            trailingIcon = {
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
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
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
                                .padding(top = 8.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚡ Frequent:", fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.Bold)
                            listOf("The Palms Mall", "Ikeja City Mall").forEach { freq ->
                                Surface(
                                    color = Gold.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.2f)),
                                    modifier = Modifier.clickable {
                                        pickup = if (freq == "The Palms Mall") "The Palms Shopping Mall, Bisway Road, Lekki, Lagos" else "Ikeja City Mall, Obafemi Awolowo Way, Ikeja, Lagos"
                                    }
                                ) {
                                    Text(
                                        freq,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldLight,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = delivery,
                            onValueChange = { 
                                delivery = it 
                                focusedField = "delivery"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged {
                                    if (it.isFocused) focusedField = "delivery"
                                },
                            shape = RoundedCornerShape(20.dp),
                            placeholder = { Text("Delivery Destination", color = TextGray) },
                            leadingIcon = { Icon(Icons.Filled.Navigation, null, tint = accentIconColor) },
                            trailingIcon = {
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
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = fieldBorderColor,
                                focusedContainerColor = fieldBgColor,
                                unfocusedContainerColor = fieldBgColor,
                                focusedTextColor = fieldTextColor,
                                unfocusedTextColor = fieldTextColor,
                                focusedPlaceholderColor = TextGray,
                                unfocusedPlaceholderColor = TextGray
                            )
                        )

                        // AI Address Auto-Suggestions Dropdown Popup
                        val suggestions = if (activeQuery.isNotBlank() && activeQuery.length >= 3) {
                            if (apiSuggestions.isNotEmpty()) apiSuggestions else findAddressMatches(activeQuery)
                        } else {
                            findAddressMatches(activeQuery)
                        }
                        if ((focusedField != null) && (suggestions.isNotEmpty() || isSearchingSuggestions)) {
                            Spacer(modifier = Modifier.height(12.dp))
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
                                                    if (focusedField == "pickup") {
                                                        pickup = cleanMatch
                                                    } else if (focusedField == "delivery") {
                                                        delivery = cleanMatch
                                                    }
                                                    focusedField = null
                                                }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isHome) Icons.Filled.Place else if (isWork) Icons.Filled.Place else Icons.Filled.Navigation,
                                                contentDescription = null,
                                                tint = if (isDark) Gold else Obsidian,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = rawMatch,
                                                fontSize = 12.sp,
                                                color = if (isDark) Color.White else Obsidian,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Package details section
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    border = BorderStroke(1.dp, if (isDark) BorderDark else BorderLight),
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Item Details",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = accentColor,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = itemName,
                            onValueChange = { itemName = it },
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            placeholder = { Text("Item Name (e.g., iPhone 15 Pro)", color = TextGray) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = fieldBorderColor,
                                focusedContainerColor = fieldBgColor,
                                unfocusedContainerColor = fieldBgColor,
                                focusedTextColor = fieldTextColor,
                                unfocusedTextColor = fieldTextColor,
                                focusedPlaceholderColor = TextGray,
                                unfocusedPlaceholderColor = TextGray
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Category",
                            fontSize = 12.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Gold else (if (isLight) GoldenWhiteLight else Obsidian))
                                        .clickable { selectedCategory = cat },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Obsidian else TextGray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = weight,
                                onValueChange = { weight = it },
                                modifier = Modifier
                                    .weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                label = { Text("Weight (kg)", color = TextGray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = fieldBorderColor,
                                    focusedContainerColor = fieldBgColor,
                                    unfocusedContainerColor = fieldBgColor,
                                    focusedTextColor = fieldTextColor,
                                    unfocusedTextColor = fieldTextColor,
                                    focusedLabelColor = accentColor,
                                    unfocusedLabelColor = TextGray
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date selector section
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    border = BorderStroke(1.dp, if (isDark) BorderDark else BorderLight),
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Select Delivery Date",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = accentColor,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(dates) { date ->
                                val isSelected = selectedDate == date
                                val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                                val dateFormat = SimpleDateFormat("dd", Locale.getDefault())

                                Box(
                                    modifier = Modifier
                                        .size(width = 64.dp, height = 74.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(if (isSelected) Gold else (if (isLight) GoldenWhiteLight else Obsidian))
                                        .clickable { selectedDate = date },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dayFormat.format(date),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Obsidian else TextGray
                                        )
                                        Text(
                                            text = dateFormat.format(date),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isSelected) Obsidian else AppTextColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Delivery Speed Picker
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    border = BorderStroke(1.dp, if (isDark) BorderDark else BorderLight),
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Delivery Speed",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = accentColor,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isInstantSpeed = true }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = isInstantSpeed,
                                onClick = { isInstantSpeed = true },
                                colors = RadioButtonDefaults.colors(selectedColor = accentColor, unselectedColor = TextGray)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Instant Speed", fontWeight = FontWeight.Bold, color = AppTextColor)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Filled.Bolt, null, tint = accentIconColor, modifier = Modifier.size(16.dp))
                                }
                                Text("Delivery in 30-45 minutes (+₦${String.format("%,.2f", expressSurcharge)})", fontSize = 11.sp, color = TextGray)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isInstantSpeed = false }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = !isInstantSpeed,
                                onClick = { isInstantSpeed = false },
                                colors = RadioButtonDefaults.colors(selectedColor = accentColor, unselectedColor = TextGray)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Standard Speed", fontWeight = FontWeight.Bold, color = AppTextColor)
                                Text("Delivery within 3-4 hours (Standard price)", fontSize = 11.sp, color = TextGray)
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

        // Price & Continue Button at bottom - overlayed
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Charcoal,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Express Price", fontSize = 12.sp, color = TextGray, fontWeight = FontWeight.Bold)
                    when (val quote = pendingQuote) {
                        is PendingQuote.Success -> {
                            Text(
                                text = "₦${String.format("%,.2f", quote.price)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = accentColor
                            )
                        }
                        is PendingQuote.Loading -> {
                            CircularProgressIndicator(
                                color = Gold,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        is PendingQuote.Error -> {
                            Text(
                                text = "Calc Error",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        else -> {
                            Text(
                                text = "Enter addresses",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextGray
                            )
                        }
                    }
                }

                val isAddressesValid = pickup.trim().length >= 6 && delivery.trim().length >= 6
                val isBookingEnabled = isAddressesValid && pendingQuote is PendingQuote.Success

                Button(
                    onClick = {
                        showCheckoutSheet = true
                    },
                    enabled = isBookingEnabled,
                    modifier = Modifier
                        .width(180.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Obsidian,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                        contentColor = Gold,
                        disabledContentColor = TextGray
                    ),
                    border = BorderStroke(1.2.dp, if (isBookingEnabled) Gold else Color.Gray.copy(alpha = 0.3f))
                ) {
                    Text("Book Instant", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = if (isBookingEnabled) Gold else TextGray)
                }
            }
        }

        val quotePrice = (pendingQuote as? PendingQuote.Success)?.price ?: 0.0

        if (showCheckoutSheet) {
            WalletCheckoutSheet(
                bookingPrice = quotePrice,
                walletBalance = viewModel.walletBalance.collectAsState().value,
                onConfirmWalletPayment = {
                    showCheckoutSheet = false
                    viewModel.updateDraftPickup(pickup)
                    viewModel.updateDraftDelivery(delivery)
                    viewModel.finalizeDraftPrice("Express", quotePrice)
                    viewModel.confirmBooking()
                    onNavigate("PaymentSuccess")
                },
                onFundRequired = { missingAmt ->
                    showCheckoutSheet = false
                    pendingAmount = missingAmt
                    showPaystackSheet = true
                },
                onDismiss = { showCheckoutSheet = false }
            )
        }

        if (showPaystackSheet) {
            PaystackCheckoutSheet(
                amount = pendingAmount,
                onPaymentComplete = { reference ->
                    showPaystackSheet = false
                    viewModel.topUpWallet(pendingAmount)
                    viewModel.updateDraftPickup(pickup)
                    viewModel.updateDraftDelivery(delivery)
                    viewModel.finalizeDraftPrice("Express", quotePrice)
                    viewModel.confirmBooking()
                    onNavigate("PaymentSuccess")
                },
                onDismiss = { showPaystackSheet = false }
            )
        }
    }
}
