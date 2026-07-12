package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.History
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun EconomyBookingScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val draft by viewModel.parcelDraft.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var pickup by remember { mutableStateOf(draft.pickupAddress) }
    var delivery by remember { mutableStateOf(draft.deliveryAddress) }
    var itemName by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("2.5") }
    
    // Package Dimensions
    var length by remember { mutableStateOf("25") }
    var width by remember { mutableStateOf("20") }
    var height by remember { mutableStateOf("15") }

    // Delivery Window Picker
    var selectedWindow by remember { mutableStateOf("2-3 Days") }
    val deliveryWindows = listOf("Next Day", "2-3 Days", "Eco Saver")

    // Pricing Calculation
    val pendingQuote by viewModel.pendingQuote.collectAsState()

    LaunchedEffect(pickup, delivery, weight, length, width, height) {
        if (pickup.isNotBlank() && delivery.isNotBlank() && pickup.length >= 6 && delivery.length >= 6) {
            viewModel.calculateDynamicPriceAsync(
                serviceType = "Economy",
                pickup = pickup,
                delivery = delivery,
                weight = weight.toDoubleOrNull() ?: 1.0,
                quantity = 1,
                length = length.toIntOrNull() ?: 20,
                width = width.toIntOrNull() ?: 15,
                height = height.toIntOrNull() ?: 10,
                stopsCount = 0,
                insuranceType = "none"
            )
        } else {
            viewModel.clearQuote()
        }
    }

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
            "The Palms Shopping Mall, Bisway Road, Lekki, Lagos",
            "Eko Hotels & Suites, Plot 1415 Adetokunbo Ademola Street, Victoria Island, Lagos",
            "Civic Centre, Ozumba Mbadiwe Avenue, Victoria Island, Lagos",
            "Murtala Muhammed International Airport (LOS), Airport Road, Ikeja, Lagos",
            "Central Business District, Abuja",
            "Lekki Conservation Centre, Lekki-Epe Expressway, Lagos",
            "Ikeja City Mall, Obafemi Awolowo Way, Ikeja, Lagos",
            "National Theatre, Iganmu, Surulere, Lagos",
            "University of Lagos, Akoka, Yaba, Lagos"
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
            if (home.isNotBlank() && home != "No. 12 Joel Ogunnaike Street, Ikeja GRA, Lagos") {
                predictiveList.add("🏠 Home: $home")
            }
            val work = viewModel.workAddress.value
            if (work.isNotBlank() && work != "Plot 14, Kingsway Road, Ikoyi, Lagos") {
                predictiveList.add("💼 Work: $work")
            }
            predictiveList.addAll(listOf(
                "Murtala Muhammed International Airport (LOS), Airport Road, Ikeja, Lagos",
                "Ikeja City Mall, Obafemi Awolowo Way, Ikeja, Lagos",
                "Lekki Conservation Centre, Lekki-Epe Expressway, Lagos",
                "Central Business District, Abuja",
                "University of Lagos, Akoka, Yaba, Lagos"
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
                val encodedQuery = java.net.URLEncoder.encode(activeQuery, "UTF-8")
                val url = java.net.URL("https://nominatim.openstreetmap.org/search?format=json&q=$encodedQuery&addressdetails=1&limit=5")
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
            } catch (e: Exception) {
                android.util.Log.e("AddressSearch", "API search failed: ${e.message}")
                withContext(Dispatchers.Main) {
                    apiSuggestions = findAddressMatches(activeQuery)
                    isSearchingSuggestions = false
                }
            }
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
                title = "Economy Booking",
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
                        .padding(horizontal = 24.dp, vertical = 24.dp)
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
                                shape = RoundedCornerShape(16.dp),
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
                    Column(modifier = Modifier.padding(20.dp)) {
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
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isDark) MapStandardBg else GoldenWhite),
                                border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.25f) else Slate),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        if (isSearchingSuggestions) "🔍 Searching locations..." else "💡 AI Suggestion Matches:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Gold else Obsidian,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
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
                    Column(modifier = Modifier.padding(20.dp)) {
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
                            placeholder = { Text("Item Name (e.g., Heavy Box)", color = TextGray) },
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

                // Dimensions Section
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    border = BorderStroke(1.dp, if (isDark) BorderDark else BorderLight),
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Package Dimensions (cm)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = accentColor,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = length,
                                onValueChange = { length = it },
                                modifier = Modifier
                                    .weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                label = { Text("Length", color = TextGray, fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Bold, fontSize = 13.sp),
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

                            OutlinedTextField(
                                value = width,
                                onValueChange = { width = it },
                                modifier = Modifier
                                    .weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                label = { Text("Width", color = TextGray, fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Bold, fontSize = 13.sp),
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

                            OutlinedTextField(
                                value = height,
                                onValueChange = { height = it },
                                modifier = Modifier
                                    .weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                label = { Text("Height", color = TextGray, fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Bold, fontSize = 13.sp),
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

                // Delivery Window Selector
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    border = BorderStroke(1.dp, if (isDark) BorderDark else BorderLight),
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Delivery Window",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = accentColor,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            deliveryWindows.forEach { win ->
                                val isSelected = selectedWindow == win
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSelected) Gold else (if (isLight) GoldenWhiteLight else Obsidian))
                                        .clickable { selectedWindow = win },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = win,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Obsidian else TextGray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isLight) Color(0xFFF9FAFB) else Obsidian, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Icon(Icons.Filled.Info, null, tint = accentIconColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Economy offers cost-effective rates by bundling shipments along the route.",
                                fontSize = 11.sp,
                                color = if (isLight) Obsidian else TextGray,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

        // Pricing Bottom Bar - overlayed
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Charcoal,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Economy Price", fontSize = 12.sp, color = TextGray, fontWeight = FontWeight.Bold)
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
                    Text("Book Economy", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = if (isBookingEnabled) Gold else TextGray)
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
                    viewModel.finalizeDraftPrice("Economy", quotePrice)
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
                    viewModel.finalizeDraftPrice("Economy", quotePrice)
                    viewModel.confirmBooking()
                    onNavigate("PaymentSuccess")
                },
                onDismiss = { showPaystackSheet = false }
            )
        }
    }
}
