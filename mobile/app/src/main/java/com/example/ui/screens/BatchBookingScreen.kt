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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.MyLocation

val nigerianLandmarks = listOf(
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

@Composable
fun BatchBookingScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val draft by viewModel.parcelDraft.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var pickup by remember { mutableStateOf(draft.pickupAddress) }
    var destinations by remember { mutableStateOf(if (draft.deliveryAddress.isNotBlank()) listOf(draft.deliveryAddress) else listOf("University of Benin, Ugbowo Campus, Benin City")) }

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
                } else if (activeAutoDetectField.startsWith("dest_")) {
                    val idx = activeAutoDetectField.removePrefix("dest_").toIntOrNull() ?: 0
                    val newList = destinations.toMutableList()
                    if (idx < newList.size) {
                        newList[idx] = detected
                        destinations = newList
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

    var showCheckoutSheet by remember { mutableStateOf(false) }
    var showPaystackSheet by remember { mutableStateOf(false) }
    var pendingAmount by remember { mutableStateOf(0.0) }
    var itemName by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("5.0") }

    // Autocomplete states
    var pickupFocused by remember { mutableStateOf(false) }
    var focusedDestinationIndex by remember { mutableStateOf(-1) }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }

    // Coroutine-driven geocoder search
    fun performSearch(query: String) {
        if (query.length > 3) {
            coroutineScope.launch {
                val results = mutableListOf<String>()
                try {
                    withContext(Dispatchers.IO) {
                        val token = try { com.example.BuildConfig.MAPBOX_ACCESS_TOKEN } catch (e: Throwable) { "" }
                        if (token.isNotBlank() && token != "mapbox_access_token_placeholder") {
                            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
                            val url = java.net.URL("https://api.mapbox.com/geocoding/v5/mapbox.places/$encodedQuery.json?access_token=$token&country=ng&limit=5&proximity=6.3350,5.6037")
                            val urlConnection = url.openConnection() as java.net.HttpURLConnection
                            urlConnection.connectTimeout = 3000
                            urlConnection.readTimeout = 3000
                            val response = urlConnection.inputStream.bufferedReader().use { it.readText() }
                            val jsonObject = org.json.JSONObject(response)
                            val features = jsonObject.optJSONArray("features")
                            if (features != null) {
                                for (i in 0 until features.length()) {
                                    val feat = features.getJSONObject(i)
                                    val placeName = feat.optString("place_name")
                                    if (!placeName.isNullOrBlank()) {
                                        results.add(placeName)
                                    }
                                }
                            }
                        } else {
                            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                            val addresses = geocoder.getFromLocationName(query, 5)
                            if (addresses != null && addresses.isNotEmpty()) {
                                for (addr in addresses) {
                                    addr.getAddressLine(0)?.let { results.add(it) }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("BatchSearch", "Primary Mapbox/System geocoder search failed: ${e.message}. Trying Nominatim...")
                }
                
                if (results.isEmpty()) {
                    try {
                        withContext(Dispatchers.IO) {
                            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
                            val url = java.net.URL("https://nominatim.openstreetmap.org/search?format=json&q=$encodedQuery&addressdetails=1&limit=5&countrycodes=ng")
                            val urlConnection = url.openConnection() as java.net.HttpURLConnection
                            urlConnection.setRequestProperty("User-Agent", "EngracedDispatchAndroidApp/1.0 (reachheytek@gmail.com)")
                            urlConnection.connectTimeout = 3000
                            urlConnection.readTimeout = 3000
                            val response = urlConnection.inputStream.bufferedReader().use { it.readText() }
                            val jsonArray = org.json.JSONArray(response)
                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                val displayName = obj.optString("display_name")
                                if (!displayName.isNullOrBlank()) {
                                    results.add(displayName)
                                }
                            }
                        }
                    } catch (err: Exception) {
                        android.util.Log.e("BatchSearch", "Nominatim fallback failed: ${err.message}")
                    }
                }
                val filteredLandmarks = nigerianLandmarks.filter { it.contains(query, ignoreCase = true) }
                results.addAll(filteredLandmarks)
                suggestions = results.distinct()
            }
        } else {
            suggestions = emptyList()
        }
    }

    // Pricing Calculation
    val pendingQuote by viewModel.pendingQuote.collectAsState()

    LaunchedEffect(pickup, destinations, weight) {
        val firstDest = destinations.firstOrNull() ?: ""
        if (pickup.isNotBlank() && firstDest.isNotBlank() && pickup.length >= 6 && firstDest.length >= 6) {
            viewModel.calculateDynamicPriceAsync(
                serviceType = "Batch",
                pickup = pickup,
                delivery = firstDest,
                weight = weight.toDoubleOrNull() ?: 1.0,
                quantity = destinations.size,
                length = 20,
                width = 15,
                height = 10,
                stopsCount = destinations.size - 1,
                insuranceType = "none"
            )
        } else {
            viewModel.clearQuote()
        }
    }

    LaunchedEffect(draft) {
        if (pickup.isEmpty()) pickup = draft.pickupAddress
    }

    LaunchedEffect(pickup, destinations) {
        viewModel.updateDraftPickup(pickup)
        viewModel.updateDraftDelivery(destinations.firstOrNull() ?: "")
    }

    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val fieldBgColor = Charcoal
    val fieldTextColor = if (isLight) Obsidian else Color.White
    val fieldBorderColor = if (isLight) Slate else Gold.copy(alpha = 0.3f)
    val accentColor = if (isLight) Obsidian else Gold
    val accentIconColor = if (isLight) Obsidian else Gold

    val scrollState = rememberScrollState()

    val isDark = !isLight
    val batchBorderColor = if (isDark) BorderDark else Slate
    val dividerColor = if (isLight) Slate else Color(0xFF2E2E2E)

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
                title = "Batch Booking",
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
                                        if (destinations.isNotEmpty()) {
                                            val mutable = destinations.toMutableList()
                                            mutable[0] = addr
                                            destinations = mutable
                                        } else {
                                            destinations = listOf(addr)
                                        }
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

                // Pickup Addresses Section
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    border = BorderStroke(1.dp, batchBorderColor),
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Pickup Location",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = accentColor,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = pickup,
                            onValueChange = {
                                pickup = it
                                focusedDestinationIndex = -1
                                performSearch(it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged {
                                    pickupFocused = it.isFocused
                                    if (it.isFocused) {
                                        focusedDestinationIndex = -1
                                        performSearch(pickup)
                                    }
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

                        // Autocomplete Dropdown for Pickup
                        if (pickupFocused && suggestions.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Charcoal),
                                border = BorderStroke(1.dp, accentColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                    suggestions.forEach { suggestion ->
                                        Text(
                                            text = suggestion,
                                            color = AppTextColor,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    pickup = suggestion
                                                    pickupFocused = false
                                                    suggestions = emptyList()
                                                }
                                                .padding(14.dp),
                                            fontSize = 13.sp
                                        )
                                        HorizontalDivider(color = dividerColor)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Destinations Section
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    border = BorderStroke(1.dp, batchBorderColor),
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Destinations (${destinations.size}/5)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = accentColor
                            )

                            if (destinations.size < 5) {
                                TextButton(
                                    onClick = {
                                        destinations = destinations + ""
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = accentColor)
                                ) {
                                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Stop", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        destinations.forEachIndexed { index, dest ->
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = dest,
                                        onValueChange = { newValue ->
                                            val mutable = destinations.toMutableList()
                                            mutable[index] = newValue
                                            destinations = mutable
                                            pickupFocused = false
                                            focusedDestinationIndex = index
                                            performSearch(newValue)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .onFocusChanged {
                                                if (it.isFocused) {
                                                    pickupFocused = false
                                                    focusedDestinationIndex = index
                                                    performSearch(dest)
                                                }
                                            },
                                        shape = RoundedCornerShape(20.dp),
                                        placeholder = { Text("Dropoff Address ${index + 1}", color = TextGray) },
                                        leadingIcon = { Icon(Icons.Filled.Navigation, null, tint = accentIconColor) },
                                        trailingIcon = {
                                            IconButton(onClick = {
                                                activeAutoDetectField = "dest_$index"
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

                                    if (destinations.size > 1) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = {
                                                val mutable = destinations.toMutableList()
                                                mutable.removeAt(index)
                                                destinations = mutable
                                                if (focusedDestinationIndex == index) {
                                                    focusedDestinationIndex = -1
                                                    suggestions = emptyList()
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Filled.Delete, "Remove Dropoff", tint = Color.Red, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }

                                // Autocomplete Dropdown for currently focused dropoff
                                if (focusedDestinationIndex == index && suggestions.isNotEmpty()) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 200.dp)
                                            .padding(vertical = 8.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Charcoal),
                                        border = BorderStroke(1.dp, accentColor),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                                    ) {
                                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                            suggestions.forEach { suggestion ->
                                                Text(
                                                    text = suggestion,
                                                    color = AppTextColor,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            val mutable = destinations.toMutableList()
                                                            mutable[index] = suggestion
                                                            destinations = mutable
                                                            focusedDestinationIndex = -1
                                                            suggestions = emptyList()
                                                        }
                                                        .padding(14.dp),
                                                    fontSize = 13.sp
                                                )
                                                HorizontalDivider(color = dividerColor)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Item info section
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    border = BorderStroke(1.dp, batchBorderColor),
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Batch Package Info",
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
                            placeholder = { Text("Consolidated cargo description", color = TextGray) },
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
                            value = weight,
                            onValueChange = { weight = it },
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            label = { Text("Total combined weight (kg)", color = TextGray) },
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

        // Bottom Pricing Summary - overlayed
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
                    Text("Batch Price", fontSize = 12.sp, color = TextGray, fontWeight = FontWeight.Bold)
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

                val firstDest = destinations.firstOrNull() ?: ""
                val isAddressesValid = pickup.trim().length >= 6 && firstDest.trim().length >= 6
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
                    Text("Book Batch", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = if (isBookingEnabled) Gold else TextGray)
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
                    viewModel.updateDraftDelivery(destinations.firstOrNull() ?: "")
                    viewModel.finalizeDraftPrice("Batch", quotePrice)
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
                    viewModel.updateDraftDelivery(destinations.firstOrNull() ?: "")
                    viewModel.finalizeDraftPrice("Batch", quotePrice)
                    viewModel.confirmBooking()
                    onNavigate("PaymentSuccess")
                },
                onDismiss = { showPaystackSheet = false }
            )
        }
    }
}
