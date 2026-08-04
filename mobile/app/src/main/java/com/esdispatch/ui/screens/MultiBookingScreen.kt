package com.esdispatch.ui.screens

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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone

@Composable
fun MultiBookingScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val draft by viewModel.parcelDraft.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var pickups by remember { mutableStateOf(if (draft.pickupAddress.isNotBlank()) listOf(draft.pickupAddress) else listOf("Murtala Muhammed Rd, Ikeja")) }
    var delivery by remember { mutableStateOf(draft.deliveryAddress) }
    var itemName by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("4.5") }
    var showCheckoutSheet by remember { mutableStateOf(false) }
    var showPaystackSheet by remember { mutableStateOf(false) }
    var pendingAmount by remember { mutableStateOf(0.0) }

    var sName by remember { mutableStateOf(draft.senderName) }
    var sPhone by remember { mutableStateOf(draft.senderPhone) }
    var rName by remember { mutableStateOf(draft.receiverName) }
    var rPhone by remember { mutableStateOf(draft.receiverPhone) }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        if (uri != null) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                
                val name = if (nameIndex >= 0) cursor.getString(nameIndex) else ""
                val hasPhone = if (hasPhoneIndex >= 0) cursor.getString(hasPhoneIndex) else "0"
                val id = if (idIndex >= 0) cursor.getString(idIndex) else ""
                
                rName = name
                if (hasPhone == "1") {
                    val phones = context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    if (phones != null && phones.moveToFirst()) {
                        val phoneIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        rPhone = if (phoneIndex >= 0) phones.getString(phoneIndex) else ""
                        phones.close()
                    }
                }
                cursor.close()
            }
        }
    }

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
                        val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                        val addresses = com.esdispatch.utils.GeocoderUtils.getFromLocationNameCompat(geocoder, query, 5)
                        if (addresses != null) {
                            for (addr in addresses) {
                                addr.getAddressLine(0)?.let { results.add(it) }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Fallback to local filtering
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

    LaunchedEffect(pickups, delivery, sName, sPhone, rName, rPhone) {
        viewModel.updateDraftPickup(pickups.firstOrNull() ?: "")
        viewModel.updateDraftDelivery(delivery)
        viewModel.updateDraftSenderInfo(sName, sPhone)
        viewModel.updateDraftReceiverInfo(rName, rPhone)
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
                                        shape = RoundedCornerShape(16.dp),
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
                        if (deliveryFocused && suggestions.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(16.dp),
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
                                    .clickable { contactPickerLauncher.launch(null) }
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

        // Bottom Pricing Summary - overlayed
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
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
                val isContactValid = sName.trim().isNotBlank() && sPhone.trim().isNotBlank() && rName.trim().isNotBlank() && rPhone.trim().isNotBlank()
                val isBookingEnabled = isAddressesValid && isContactValid && pendingQuote is PendingQuote.Success

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
                    viewModel.updateDraftSenderInfo(sName, sPhone)
                    viewModel.updateDraftReceiverInfo(rName, rPhone)
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
                    viewModel.updateDraftSenderInfo(sName, sPhone)
                    viewModel.updateDraftReceiverInfo(rName, rPhone)
                    viewModel.finalizeDraftPrice("Multi", quotePrice)
                    viewModel.confirmBooking()
                    onNavigate("PaymentSuccess")
                },
                onDismiss = { showPaystackSheet = false }
            )
        }
    }
}
