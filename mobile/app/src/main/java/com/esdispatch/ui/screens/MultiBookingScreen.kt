package com.esdispatch.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.History
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
import com.esdispatch.ui.theme.*
import com.esdispatch.ui.components.RoundedSheet
import com.esdispatch.ui.components.ScreenHeader
import com.esdispatch.ui.components.WalletCheckoutSheet
import com.esdispatch.viewmodel.DeliveryViewModel
import com.esdispatch.viewmodel.PendingQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.ContactsContract
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import com.esdispatch.util.CargoFeasibilityValidator
import android.content.Intent

data class MultiPickupStop(
    val id: String = java.util.UUID.randomUUID().toString(),
    val address: String = "",
    val recipientName: String = "",
    val recipientPhone: String = "",
    val itemDescription: String = ""
)

@Composable
fun MultiBookingScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    BackHandler { onNavigate("BACK") }

    val draft by viewModel.parcelDraft.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var stops by remember {
        mutableStateOf(
            listOf(
                MultiPickupStop(
                    address = if (draft.pickupAddress.isNotBlank()) draft.pickupAddress else "Murtala Muhammed Rd, Ikeja",
                    recipientName = draft.receiverName,
                    recipientPhone = draft.receiverPhone,
                    itemDescription = ""
                )
            )
        )
    }
    var delivery by remember { mutableStateOf(draft.deliveryAddress) }
    var itemName by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("4.5") }
    val cargoValidation = remember(itemName, weight) {
        CargoFeasibilityValidator.validateCargo(itemName = itemName, weightKg = weight.toDoubleOrNull() ?: 1.0)
    }
    var showCheckoutSheet by remember { mutableStateOf(false) }
    var showPaystackSheet by remember { mutableStateOf(false) }
    var pendingAmount by remember { mutableStateOf(0.0) }

    var sName by remember { mutableStateOf(draft.senderName) }
    var sPhone by remember { mutableStateOf(draft.senderPhone) }
    var rName by remember { mutableStateOf(draft.receiverName) }
    var rPhone by remember { mutableStateOf(draft.receiverPhone) }

    var activeContactPickerStopIndex by remember { mutableIntStateOf(-1) }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val contactUri = result.data?.data
            if (contactUri != null) {
                try {
                    val projection = arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    )
                    context.contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                            val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            val name = if (nameIndex >= 0) cursor.getString(nameIndex) ?: "" else ""
                            val num = if (numberIndex >= 0) (cursor.getString(numberIndex) ?: "").replace(" ", "").replace("-", "") else ""
                            if (activeContactPickerStopIndex in stops.indices) {
                                stops = stops.toMutableList().apply {
                                    val cur = this[activeContactPickerStopIndex]
                                    this[activeContactPickerStopIndex] = cur.copy(
                                        recipientName = if (name.isNotBlank()) name else cur.recipientName,
                                        recipientPhone = if (num.isNotBlank()) num else cur.recipientPhone
                                    )
                                }
                            } else {
                                if (name.isNotBlank()) rName = name
                                if (num.isNotBlank()) rPhone = num
                            }
                            Toast.makeText(context, "Contact loaded successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to read contact", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Autocomplete states
    var deliveryFocused by remember { mutableStateOf(false) }
    var focusedPickupIndex by remember { mutableStateOf(-1) }
    var suggestionItems by remember { mutableStateOf<List<com.esdispatch.utils.SearchResultItem>>(emptyList()) }

    // Address search using AddressDatabase (Benin City + Lagos) + Mapbox
    fun performSearch(query: String) {
        if (query.isNotBlank()) {
            val localResults = com.esdispatch.data.AddressDatabase.searchItems(query)
            suggestionItems = localResults
            if (query.length >= 2) {
                coroutineScope.launch {
                    try {
                        val full = viewModel.searchAddressAutocompleteItems(query)
                        if (full.isNotEmpty()) {
                            suggestionItems = full
                        }
                    } catch (_: Exception) {}
                }
            }
        } else {
            suggestionItems = emptyList()
        }
    }

    // Pricing Calculation
    val pendingQuote by viewModel.pendingQuote.collectAsState()

    LaunchedEffect(stops, delivery, weight) {
        val firstPickup = stops.firstOrNull()?.address ?: ""
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
                stopsCount = (stops.size - 1).coerceAtLeast(0),
                insuranceType = "none"
            )
        } else {
            viewModel.clearQuote()
        }
    }

    LaunchedEffect(draft) {
        if (delivery.isEmpty()) delivery = draft.deliveryAddress
    }

    LaunchedEffect(stops, delivery, sName, sPhone, rName, rPhone) {
        val firstStop = stops.firstOrNull()
        viewModel.updateDraftPickup(firstStop?.address ?: "")
        viewModel.updateDraftDelivery(delivery)
        viewModel.updateDraftSenderInfo(sName, sPhone)
        viewModel.updateDraftReceiverInfo(rName, rPhone)
        if (stops.size > 1) {
            viewModel.updateDraftAdditionalStops(
                stops.drop(1).map {
                    "${it.address} | Contact: ${it.recipientName} (${it.recipientPhone}) | Item: ${it.itemDescription}"
                }
            )
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
                title = "Multi-Pickup Booking",
                onBack = { onNavigate("BACK") }
            )

            RoundedSheet(
                modifier = Modifier.weight(1f),
                containerColor = if (isDark) BackgroundDark else BackgroundLight
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) {
                            deliveryFocused = false
                            focusedPickupIndex = -1
                        }
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

                // Dynamic Pickups Section
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    border = BorderStroke(1.dp, if (isDark) BorderDark else BorderLight),
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pickups & Stops (${stops.size}/5)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = accentColor
                            )

                            if (stops.size < 5) {
                                TextButton(
                                    onClick = {
                                        stops = stops + MultiPickupStop()
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

                        stops.forEachIndexed { index, stop ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (isDark) Charcoal.copy(alpha = 0.5f) else GoldenWhite,
                                border = BorderStroke(1.dp, if (isDark) BorderDark else BorderLight),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isDark) Gold.copy(alpha = 0.15f) else Obsidian.copy(alpha = 0.08f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "STOP #${index + 1}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                color = accentColor,
                                                letterSpacing = 1.sp
                                            )
                                        }

                                        if (stops.size > 1) {
                                            IconButton(
                                                onClick = {
                                                    stops = stops.toMutableList().apply { removeAt(index) }
                                                    if (focusedPickupIndex == index) {
                                                        focusedPickupIndex = -1
                                                        suggestionItems = emptyList()
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Filled.Delete, "Remove Stop", tint = Color.Red, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Address input
                                    OutlinedTextField(
                                        value = stop.address,
                                        onValueChange = { newValue ->
                                            stops = stops.toMutableList().apply { this[index] = stop.copy(address = newValue) }
                                            deliveryFocused = false
                                            focusedPickupIndex = index
                                            performSearch(newValue)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onFocusChanged {
                                                if (it.isFocused) {
                                                    deliveryFocused = false
                                                    focusedPickupIndex = index
                                                    performSearch(stop.address)
                                                }
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        placeholder = { Text("Stop Address #${index + 1}", color = TextGray) },
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

                                    // Autocomplete Dropdown for this stop
                                    if (focusedPickupIndex == index && suggestionItems.isNotEmpty()) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 240.dp)
                                                .padding(vertical = 8.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = Charcoal),
                                            border = BorderStroke(1.dp, accentColor),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                                        ) {
                                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                                suggestionItems.forEach { item ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                stops = stops.toMutableList().apply { this[index] = stop.copy(address = item.displayInput) }
                                                                focusedPickupIndex = -1
                                                                suggestionItems = emptyList()
                                                            }
                                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(30.dp)
                                                                .clip(CircleShape)
                                                                .background(Gold.copy(alpha = 0.15f)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(Icons.Filled.Place, null, tint = Gold, modifier = Modifier.size(16.dp))
                                                        }
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(item.title, color = AppTextColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                            if (item.fullAddress.isNotBlank() && item.fullAddress != item.title) {
                                                                Spacer(modifier = Modifier.height(2.dp))
                                                                Text(item.fullAddress, color = TextGray, fontSize = 11.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                                            }
                                                        }
                                                    }
                                                    HorizontalDivider(color = if (isLight) BorderLight else Color(0xFF2E2E2E))
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Item description for stop
                                    OutlinedTextField(
                                        value = stop.itemDescription,
                                        onValueChange = { newValue ->
                                            stops = stops.toMutableList().apply { this[index] = stop.copy(itemDescription = newValue) }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        placeholder = { Text("Item at Stop #${index + 1} (e.g. Parcel, Documents)", color = TextGray) },
                                        textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Normal, fontSize = 13.sp),
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

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Contact header + "From Contacts" button
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Stop Recipient / Contact", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentColor)
                                        Text(
                                            text = "From Contacts",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Obsidian,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Gold)
                                                .clickable {
                                                    activeContactPickerStopIndex = index
                                                    contactPickerLauncher.launch(
                                                        Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                                                    )
                                                }
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = stop.recipientName,
                                            onValueChange = { newValue ->
                                                stops = stops.toMutableList().apply { this[index] = stop.copy(recipientName = newValue) }
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(16.dp),
                                            placeholder = { Text("Name", color = TextGray) },
                                            leadingIcon = { Icon(Icons.Filled.Person, null, tint = accentIconColor, modifier = Modifier.size(16.dp)) },
                                            textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
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
                                            value = stop.recipientPhone,
                                            onValueChange = { newValue ->
                                                stops = stops.toMutableList().apply { this[index] = stop.copy(recipientPhone = newValue) }
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(16.dp),
                                            placeholder = { Text("Phone", color = TextGray) },
                                            leadingIcon = { Icon(Icons.Filled.Phone, null, tint = accentIconColor, modifier = Modifier.size(16.dp)) },
                                            textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
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
                    Column(modifier = Modifier.padding(20.dp)) {
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
                        if (deliveryFocused && suggestionItems.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Charcoal),
                                border = BorderStroke(1.dp, accentColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                    suggestionItems.forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    delivery = item.displayInput
                                                    deliveryFocused = false
                                                    suggestionItems = emptyList()
                                                }
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(CircleShape)
                                                    .background(Gold.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Filled.Place, null, tint = Gold, modifier = Modifier.size(16.dp))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(item.title, color = AppTextColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                if (item.fullAddress.isNotBlank() && item.fullAddress != item.title) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(item.fullAddress, color = TextGray, fontSize = 11.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                                }
                                            }
                                        }
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
                    Column(modifier = Modifier.padding(20.dp)) {
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

                        if (!cargoValidation.isFeasible) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = cargoValidation.rejectionReason ?: "Motorcycle limit exceeded (Max 20kg, 45cm³)",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Sender Contact Details", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = accentColor)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = sName,
                            onValueChange = { sName = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            placeholder = { Text("Sender Name", color = TextGray) },
                            leadingIcon = { Icon(Icons.Filled.Person, null, tint = accentIconColor, modifier = Modifier.size(18.dp)) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp),
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
                            value = sPhone,
                            onValueChange = { sPhone = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            placeholder = { Text("Sender Phone", color = TextGray) },
                            leadingIcon = { Icon(Icons.Filled.Phone, null, tint = accentIconColor, modifier = Modifier.size(18.dp)) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp),
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

                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Receiver Contact Details", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = accentColor)
                            Text(
                                "From Contacts",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Obsidian,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Gold)
                                    .clickable {
                                        activeContactPickerStopIndex = -1
                                        contactPickerLauncher.launch(
                                            Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                                        )
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = rName,
                            onValueChange = { rName = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            placeholder = { Text("Receiver Name", color = TextGray) },
                            leadingIcon = { Icon(Icons.Filled.Person, null, tint = accentIconColor, modifier = Modifier.size(18.dp)) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp),
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
                            value = rPhone,
                            onValueChange = { rPhone = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            placeholder = { Text("Receiver Phone", color = TextGray) },
                            leadingIcon = { Icon(Icons.Filled.Phone, null, tint = accentIconColor, modifier = Modifier.size(18.dp)) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp),
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
        }
    }

    // Bottom Pricing Summary - overlayed at bottom of outer Box
    if (focusedPickupIndex == -1 && !deliveryFocused) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Charcoal,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
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

                val firstPickup = stops.firstOrNull()?.address ?: ""
                val isAddressesValid = firstPickup.trim().length >= 6 && delivery.trim().length >= 6
                val isStopsValid = stops.all { it.address.trim().isNotBlank() }
                val isContactValid = sName.trim().isNotBlank() && sPhone.trim().isNotBlank() && rName.trim().isNotBlank() && rPhone.trim().isNotBlank()
                val isBookingEnabled = isAddressesValid && isStopsValid && isContactValid && cargoValidation.isFeasible && pendingQuote is PendingQuote.Success

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
    }

    val quotePrice = (pendingQuote as? PendingQuote.Success)?.price ?: 0.0

    if (showCheckoutSheet) {
        WalletCheckoutSheet(
            bookingPrice = quotePrice,
            walletBalance = viewModel.walletBalance.collectAsState().value,
            onConfirmWalletPayment = {
                showCheckoutSheet = false
                val firstStop = stops.firstOrNull()
                viewModel.updateDraftPickup(firstStop?.address ?: "")
                viewModel.updateDraftDelivery(delivery)
                viewModel.updateDraftSenderInfo(sName, sPhone)
                viewModel.updateDraftReceiverInfo(rName, rPhone)
                if (stops.size > 1) {
                    viewModel.updateDraftAdditionalStops(
                        stops.drop(1).map {
                            "${it.address} | Contact: ${it.recipientName} (${it.recipientPhone}) | Item: ${it.itemDescription}"
                        }
                    )
                }
                viewModel.finalizeDraftPrice("Multi", quotePrice)
                viewModel.confirmBooking { ok, msg ->
                    if (ok) {
                        onNavigate("PaymentSuccess")
                    } else {
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                }
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
                val firstStop = stops.firstOrNull()
                viewModel.updateDraftPickup(firstStop?.address ?: "")
                viewModel.updateDraftDelivery(delivery)
                viewModel.updateDraftSenderInfo(sName, sPhone)
                viewModel.updateDraftReceiverInfo(rName, rPhone)
                if (stops.size > 1) {
                    viewModel.updateDraftAdditionalStops(
                        stops.drop(1).map {
                            "${it.address} | Contact: ${it.recipientName} (${it.recipientPhone}) | Item: ${it.itemDescription}"
                        }
                    )
                }
                viewModel.finalizeDraftPrice("Multi", quotePrice)
                viewModel.confirmBooking { ok, msg ->
                    if (ok) {
                        onNavigate("PaymentSuccess")
                    } else {
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                }
            },
            onDismiss = { showPaystackSheet = false }
        )
    }
    }
}
