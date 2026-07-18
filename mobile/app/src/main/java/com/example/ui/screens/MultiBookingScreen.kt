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

@Composable
fun MultiBookingScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val draft by viewModel.parcelDraft.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var pickups by remember { mutableStateOf(if (draft.pickupAddress.isNotBlank()) listOf(draft.pickupAddress) else listOf("King's Square, Benin City")) }
    var delivery by remember { mutableStateOf(draft.deliveryAddress) }

    var activeAutoDetectField by remember { mutableStateOf("delivery") }
    
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
                if (activeAutoDetectField == "delivery") {
                    delivery = detected
                } else if (activeAutoDetectField.startsWith("pickup_")) {
                    val idx = activeAutoDetectField.removePrefix("pickup_").toIntOrNull() ?: 0
                    val newList = pickups.toMutableList()
                    if (idx < newList.size) {
                        newList[idx] = detected
                        pickups = newList
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

    var itemName by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("4.5") }
    var showCheckoutSheet by remember { mutableStateOf(false) }
    var showPaystackSheet by remember { mutableStateOf(false) }
    var pendingAmount by remember { mutableStateOf(0.0) }

    // Autocomplete states
    var deliveryFocused by remember { mutableStateOf(false) }
    var focusedPickupIndex by remember { mutableStateOf(-1) }
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
                    android.util.Log.e("MultiSearch", "Primary Mapbox/System geocoder search failed: ${e.message}. Trying Nominatim...")
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
                        android.util.Log.e("MultiSearch", "Nominatim fallback failed: ${err.message}")
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

    LaunchedEffect(pickups, delivery, weight) {
        val firstPickup = pickups.firstOrNull() ?: ""
        if (firstPickup.isNotBlank() && delivery.isNotBlank() && firstPickup.length >= 6 && delivery.length >= 6) {
            viewModel.calculateDynamicPriceAsync(
                serviceType = "Multi",
                pickup = firstPickup,
                delivery = delivery,
                weight = weight.toDoubleOrNull() ?: 1.0,
                quantity = 1,
                length = 20,
                width = 15,
                height = 10,
                stopsCount = pickups.size - 1,
                insuranceType = "none"
            )
        } else {
            viewModel.clearQuote()
        }
    }

    LaunchedEffect(draft) {
        if (delivery.isEmpty()) delivery = draft.deliveryAddress
    }

    LaunchedEffect(pickups, delivery) {
        viewModel.updateDraftPickup(pickups.firstOrNull() ?: "")
        viewModel.updateDraftDelivery(delivery)
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
                title = "Multi-Pickup Booking",
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

                // Dynamic Pickups Section
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    border = BorderStroke(1.dp, if (isDark) BorderDark else BorderLight),
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
                                text = "Pickups (${pickups.size}/5)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = accentColor
                            )

                            if (pickups.size < 5) {
                                TextButton(
                                    onClick = {
                                        pickups = pickups + ""
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = accentColor)
                                ) {
                                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Pickup", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        pickups.forEachIndexed { index, pick ->
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = pick,
                                        onValueChange = { newValue ->
                                            val mutable = pickups.toMutableList()
                                            mutable[index] = newValue
                                            pickups = mutable
                                            deliveryFocused = false
                                            focusedPickupIndex = index
                                            performSearch(newValue)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .onFocusChanged {
                                                if (it.isFocused) {
                                                    deliveryFocused = false
                                                    focusedPickupIndex = index
                                                    performSearch(pick)
                                                }
                                            },
                                        shape = RoundedCornerShape(20.dp),
                                        placeholder = { Text("Pickup Address ${index + 1}", color = TextGray) },
                                        leadingIcon = { Icon(Icons.Filled.Place, null, tint = accentIconColor) },
                                        trailingIcon = {
                                            IconButton(onClick = {
                                                activeAutoDetectField = "pickup_$index"
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

                                    if (pickups.size > 1) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = {
                                                val mutable = pickups.toMutableList()
                                                mutable.removeAt(index)
                                                pickups = mutable
                                                if (focusedPickupIndex == index) {
                                                    focusedPickupIndex = -1
                                                    suggestions = emptyList()
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Filled.Delete, "Remove Pickup", tint = Color.Red, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }

                                // Autocomplete Dropdown for currently focused pickup
                                if (focusedPickupIndex == index && suggestions.isNotEmpty()) {
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
                                                            val mutable = pickups.toMutableList()
                                                            mutable[index] = suggestion
                                                            pickups = mutable
                                                            focusedPickupIndex = -1
                                                            suggestions = emptyList()
                                                        }
                                                        .padding(14.dp),
                                                    fontSize = 13.sp
                                                )
                                                HorizontalDivider(color = if (isLight) BorderLight else Color(0xFF2E2E2E))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Single Delivery Address Section
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    border = BorderStroke(1.dp, if (isDark) BorderDark else BorderLight),
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Delivery Destination",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = accentColor,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = delivery,
                            onValueChange = {
                                delivery = it
                                focusedPickupIndex = -1
                                performSearch(it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged {
                                    deliveryFocused = it.isFocused
                                    if (it.isFocused) {
                                        focusedPickupIndex = -1
                                        performSearch(delivery)
                                    }
                                },
                            shape = RoundedCornerShape(20.dp),
                            placeholder = { Text("Final Delivery Destination", color = TextGray) },
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

                        // Autocomplete Dropdown for Delivery
                        if (deliveryFocused && suggestions.isNotEmpty()) {
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
                                                    delivery = suggestion
                                                    deliveryFocused = false
                                                    suggestions = emptyList()
                                                }
                                                .padding(14.dp),
                                            fontSize = 13.sp
                                        )
                                        HorizontalDivider(color = if (isLight) BorderLight else Color(0xFF2E2E2E))
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
                    border = BorderStroke(1.dp, if (isDark) BorderDark else BorderLight),
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Item details",
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
                            placeholder = { Text("Cargo summary", color = TextGray) },
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
                    Text("Multi-Pickup Price", fontSize = 12.sp, color = TextGray, fontWeight = FontWeight.Bold)
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

                val firstPickup = pickups.firstOrNull() ?: ""
                val isAddressesValid = firstPickup.trim().length >= 6 && delivery.trim().length >= 6
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
                    Text("Book Multi-Pick", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = if (isBookingEnabled) Gold else TextGray)
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
                    viewModel.updateDraftPickup(pickups.firstOrNull() ?: "")
                    viewModel.updateDraftDelivery(delivery)
                    viewModel.finalizeDraftPrice("Multi", quotePrice)
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
                    viewModel.updateDraftPickup(pickups.firstOrNull() ?: "")
                    viewModel.updateDraftDelivery(delivery)
                    viewModel.finalizeDraftPrice("Multi", quotePrice)
                    viewModel.confirmBooking()
                    onNavigate("PaymentSuccess")
                },
                onDismiss = { showPaystackSheet = false }
            )
        }
    }
}
