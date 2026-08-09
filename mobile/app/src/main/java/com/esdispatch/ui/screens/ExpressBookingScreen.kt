package com.esdispatch.ui.screens

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
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import com.esdispatch.ui.theme.*
import com.esdispatch.ui.components.RoundedSheet
import com.esdispatch.ui.components.ScreenHeader
import com.esdispatch.ui.components.WalletCheckoutSheet
import com.esdispatch.viewmodel.DeliveryViewModel
import com.esdispatch.viewmodel.PendingQuote
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

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

    val currentUserName by viewModel.userName.collectAsState()
    val currentUserPhone by viewModel.userPhone.collectAsState()
    val adminDiscountEnabled by viewModel.adminDiscountEnabled.collectAsState()
    val adminDiscountPercent by viewModel.adminDiscountPercent.collectAsState()

    var sName by remember { mutableStateOf(draft.senderName.ifBlank { currentUserName }) }
    var sPhone by remember { mutableStateOf(draft.senderPhone.ifBlank { currentUserPhone }) }
    var rName by remember { mutableStateOf(draft.receiverName) }
    var rPhone by remember { mutableStateOf(draft.receiverPhone) }

    var itemName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Electronics") }
    var weight by remember { mutableStateOf("1.5") }
    
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        coroutineScope.launch {
            Toast.makeText(context, "🎯 Detecting precise GPS location...", Toast.LENGTH_SHORT).show()
            val detected = withContext(Dispatchers.IO) {
                detectUserLocation(context)
            }
            pickup = detected
            if (granted) {
                Toast.makeText(context, "Location updated: $detected", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "GPS permission denied. Estimated location: $detected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val contactPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickContact()
    ) { uri ->
        if (uri != null) {
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        rName = cursor.getString(nameIndex) ?: ""
                    }
                    val hasPhoneIndex = cursor.getColumnIndex(android.provider.ContactsContract.Contacts.HAS_PHONE_NUMBER)
                    if (hasPhoneIndex >= 0 && cursor.getString(hasPhoneIndex) == "1") {
                        val idIndex = cursor.getColumnIndex(android.provider.ContactsContract.Contacts._ID)
                        if (idIndex >= 0) {
                            val contactId = cursor.getString(idIndex)
                            val phones = context.contentResolver.query(
                                android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                                null, null
                            )
                            if (phones != null && phones.moveToFirst()) {
                                val numberIndex = phones.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                                if (numberIndex >= 0) {
                                    rPhone = phones.getString(numberIndex) ?: ""
                                }
                                phones.close()
                            }
                        }
                    }
                    cursor.close()
                    Toast.makeText(context, "Contact loaded successfully!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read contact", Toast.LENGTH_SHORT).show()
            }
        }
    }
    var isInstantSpeed by remember { mutableStateOf(true) }
    var showCheckoutSheet by remember { mutableStateOf(false) }
    var showPaystackSheet by remember { mutableStateOf(false) }
    var pendingAmount by remember { mutableStateOf(0.0) }

    // Suggestions & Autocomplete state
    var focusedField by remember { mutableStateOf<String?>(null) } // pickup, delivery
    var apiSuggestionItems by remember { mutableStateOf<List<com.esdispatch.utils.SearchResultItem>>(emptyList()) }
    var isSearchingSuggestions by remember { mutableStateOf(false) }

    val activeQuery = remember(pickup, delivery, focusedField) {
        when (focusedField) {
            "pickup" -> pickup
            "delivery" -> delivery
            else -> ""
        }
    }

    // Address search using comprehensive Benin City + Lagos database
    fun findAddressMatchItems(query: String): List<com.esdispatch.utils.SearchResultItem> {
        return if (query.isBlank()) {
            val defaults = mutableListOf<com.esdispatch.utils.SearchResultItem>()
            val home = viewModel.homeAddress.value
            if (home.isNotBlank()) defaults.add(com.esdispatch.utils.SearchResultItem("🏠 Saved Home", home))
            val work = viewModel.workAddress.value
            if (work.isNotBlank()) defaults.add(com.esdispatch.utils.SearchResultItem("💼 Saved Work", work))
            defaults.addAll(com.esdispatch.data.AddressDatabase.getDefaults().take(6).map { it.toSearchResult() })
            defaults.distinctBy { it.displayInput }
        } else {
            com.esdispatch.data.AddressDatabase.searchItems(query)
        }
    }

    LaunchedEffect(draft) {
        if (pickup.isEmpty()) pickup = draft.pickupAddress
        if (delivery.isEmpty()) delivery = draft.deliveryAddress
    }

    LaunchedEffect(pickup, delivery) {
        viewModel.updateDraftPickup(pickup)
        viewModel.updateDraftDelivery(delivery)
    }

    // Address autocomplete via AddressDatabase (instant) + Mapbox Places (async refinement)
    LaunchedEffect(activeQuery) {
        if (activeQuery.isBlank() || activeQuery.length < 2) {
            apiSuggestionItems = emptyList()
            return@LaunchedEffect
        }
        apiSuggestionItems = findAddressMatchItems(activeQuery)

        if (activeQuery.length >= 2) {
            isSearchingSuggestions = true
            kotlinx.coroutines.delay(300L) // debounce
            try {
                val fullResults = viewModel.searchAddressAutocompleteItems(activeQuery)
                if (fullResults.isNotEmpty()) {
                    apiSuggestionItems = fullResults
                }
            } catch (e: Exception) {
                // Keep local matches fallback
            } finally {
                isSearchingSuggestions = false
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

    LaunchedEffect(pickup, delivery, weight) {
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
                insuranceType = "none"
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
                                        lineHeight = 14.sp
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
                        if (adminDiscountEnabled) {
                            Surface(
                                color = Gold.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Gold),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = Gold, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("⚡ ${adminDiscountPercent}% EXPRESS DISCOUNT ACTIVE", color = GoldLight, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                        Text("Prices shown will include your exclusive savings!", color = TextGray, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

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

                        // Inline Autocomplete Dropdown for Pickup
                        val suggestionItemsForPickup = if (focusedField == "pickup") {
                            if (activeQuery.isNotBlank() && activeQuery.length >= 2) {
                                if (apiSuggestionItems.isNotEmpty()) apiSuggestionItems else findAddressMatchItems(activeQuery)
                            } else {
                                findAddressMatchItems(activeQuery)
                            }
                        } else emptyList()

                        if (focusedField == "pickup" && (suggestionItemsForPickup.isNotEmpty() || isSearchingSuggestions)) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isDark) MapStandardBg else GoldenWhite),
                                border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.4f) else Slate),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        if (isSearchingSuggestions) "🔍 Searching places & addresses..." else "💡 Verified Location Matches:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Gold else Obsidian,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                    suggestionItemsForPickup.take(5).forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    pickup = item.displayInput
                                                    focusedField = null
                                                }
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isDark) Gold.copy(alpha = 0.15f) else Obsidian.copy(alpha = 0.08f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Place,
                                                    contentDescription = null,
                                                    tint = if (isDark) Gold else Obsidian,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = item.title,
                                                    fontSize = 13.sp,
                                                    color = if (isDark) Color.White else Obsidian,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (item.fullAddress.isNotBlank() && item.fullAddress != item.title) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = item.fullAddress,
                                                        fontSize = 11.sp,
                                                        color = TextGray,
                                                        fontWeight = FontWeight.Normal,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        Surface(
                            color = Gold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clickable {
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
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Detect Current Location",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isLight) Obsidian else GoldLight
                                )
                            }
                        }

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
                                        focusedField = null
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

                        // Inline Autocomplete Dropdown for Delivery
                        val suggestionItemsForDelivery = if (focusedField == "delivery") {
                            if (activeQuery.isNotBlank() && activeQuery.length >= 2) {
                                if (apiSuggestionItems.isNotEmpty()) apiSuggestionItems else findAddressMatchItems(activeQuery)
                            } else {
                                findAddressMatchItems(activeQuery)
                            }
                        } else emptyList()

                        if (focusedField == "delivery" && (suggestionItemsForDelivery.isNotEmpty() || isSearchingSuggestions)) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isDark) MapStandardBg else GoldenWhite),
                                border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.4f) else Slate),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        if (isSearchingSuggestions) "🔍 Searching places & addresses..." else "💡 Verified Location Matches:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Gold else Obsidian,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                    suggestionItemsForDelivery.take(5).forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    delivery = item.displayInput
                                                    focusedField = null
                                                }
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isDark) Gold.copy(alpha = 0.15f) else Obsidian.copy(alpha = 0.08f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Place,
                                                    contentDescription = null,
                                                    tint = if (isDark) Gold else Obsidian,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = item.title,
                                                    fontSize = 13.sp,
                                                    color = if (isDark) Color.White else Obsidian,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (item.fullAddress.isNotBlank() && item.fullAddress != item.title) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = item.fullAddress,
                                                        fontSize = 11.sp,
                                                        color = TextGray,
                                                        fontWeight = FontWeight.Normal,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sender & Receiver Contact details Card
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    border = BorderStroke(1.dp, if (isDark) BorderDark else BorderLight),
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Sender Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚡ Sender Info", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Gold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = sName,
                                onValueChange = { sName = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                placeholder = { Text("Name", color = TextGray) },
                                leadingIcon = { Icon(Icons.Filled.Person, null, tint = accentIconColor, modifier = Modifier.size(18.dp)) },
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
                            OutlinedTextField(
                                value = sPhone,
                                onValueChange = { sPhone = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                placeholder = { Text("Phone", color = TextGray) },
                                leadingIcon = { Icon(Icons.Filled.Phone, null, tint = accentIconColor, modifier = Modifier.size(18.dp)) },
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
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Receiver Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚡ Receiver Info", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Gold)
                            Text(
                                "From Contacts",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Obsidian,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Gold)
                                    .clickable {
                                        contactPickerLauncher.launch(null)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = rName,
                                onValueChange = { rName = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                placeholder = { Text("Name", color = TextGray) },
                                leadingIcon = { Icon(Icons.Filled.Person, null, tint = accentIconColor, modifier = Modifier.size(18.dp)) },
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
                            OutlinedTextField(
                                value = rPhone,
                                onValueChange = { rPhone = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                placeholder = { Text("Phone", color = TextGray) },
                                leadingIcon = { Icon(Icons.Filled.Phone, null, tint = accentIconColor, modifier = Modifier.size(18.dp)) },
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
                    Column(modifier = Modifier.padding(20.dp)) {
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


            }
        }
    }

        // Price & Continue Button at bottom - overlayed (hidden when typing address to prevent screen occlusion)
        if (focusedField == null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Charcoal,
                tonalElevation = 8.dp
            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
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
                    viewModel.updateDraftSenderInfo(sName, sPhone)
                    viewModel.updateDraftReceiverInfo(rName, rPhone)
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
                    viewModel.updateDraftSenderInfo(sName, sPhone)
                    viewModel.updateDraftReceiverInfo(rName, rPhone)
                    viewModel.finalizeDraftPrice("Express", quotePrice)
                    viewModel.confirmBooking()
                    onNavigate("PaymentSuccess")
                },
                onDismiss = { showPaystackSheet = false }
            )
        }
    }
}
}
