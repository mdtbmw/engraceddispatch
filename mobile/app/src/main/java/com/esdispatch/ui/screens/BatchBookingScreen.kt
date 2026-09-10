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
import com.esdispatch.ui.theme.Hugeicons
import com.esdispatch.ui.theme.AnimatedHugeIcon
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.ContactsContract
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.activity.compose.BackHandler
import com.esdispatch.util.CargoFeasibilityValidator
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import android.content.Intent

data class BatchDestinationItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val destinationAddress: String = "",
    val recipientName: String = "",
    val recipientPhone: String = "",
    val itemName: String = "",
    val weight: String = "1.5"
)

val beninLandmarks = listOf(
    "Ring Road (King's Square), City Center, Benin City",
    "Benin City Airport, Airport Road, Benin City",
    "Oba's Palace, Oba Ovonramwen Square, Benin City",
    "University of Benin (UNIBEN), Ugbowo, Benin City",
    "UBTH (Teaching Hospital), Ugbowo, Benin City",
    "Ihama Road, GRA, Benin City",
    "Boundary Road, GRA, Benin City",
    "Ugbor Road, GRA, Benin City",
    "Ramat Park, Ikpoba Hill, Benin City",
    "New Benin Market, New Benin, Benin City",
    "Uselu Market, Uselu, Benin City",
    "Sapele Road, Benin City",
    "Upper Sakponba Road, Benin City",
    "Ekenwan Road (Campus Area), Benin City",
    "Central Hospital, Sapele Road, Benin City",
    "Ogba Zoo & Nature Park, Airport Road Extension, Benin City"
)

@Composable
fun BatchBookingScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val draft by viewModel.parcelDraft.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var pickup by remember { mutableStateOf(draft.pickupAddress) }
    var batchStops by remember {
        mutableStateOf(
            listOf(
                BatchDestinationItem(
                    destinationAddress = if (draft.deliveryAddress.isNotBlank()) draft.deliveryAddress else "14 Ihama Road, GRA, Benin City",
                    recipientName = draft.receiverName,
                    recipientPhone = draft.receiverPhone,
                    itemName = "Package 1",
                    weight = "1.5"
                )
            )
        )
    }
    var activeContactPickerStopIndex by remember { mutableStateOf(-1) }

    var showCheckoutSheet by remember { mutableStateOf(false) }
    var showPaystackSheet by remember { mutableStateOf(false) }
    var pendingAmount by remember { mutableStateOf(0.0) }

    var sName by remember { mutableStateOf(draft.senderName) }
    var sPhone by remember { mutableStateOf(draft.senderPhone) }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
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
                            val phoneVal = if (numberIndex >= 0) (cursor.getString(numberIndex) ?: "").replace(" ", "").replace("-", "") else ""

                            if (activeContactPickerStopIndex in batchStops.indices) {
                                val updated = batchStops.toMutableList()
                                updated[activeContactPickerStopIndex] = updated[activeContactPickerStopIndex].copy(
                                    recipientName = if (name.isNotBlank()) name else updated[activeContactPickerStopIndex].recipientName,
                                    recipientPhone = if (phoneVal.isNotBlank()) phoneVal else updated[activeContactPickerStopIndex].recipientPhone
                                )
                                batchStops = updated
                                Toast.makeText(context, "Contact loaded: $name", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("BatchBookingScreen", "Contact pick error: ${e.message}")
                    Toast.makeText(context, "Could not load contact details directly. Please enter manually.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Autocomplete states
    var pickupFocused by remember { mutableStateOf(false) }
    var focusedDestinationIndex by remember { mutableStateOf(-1) }
    var suggestionItems by remember { mutableStateOf<List<com.esdispatch.utils.SearchResultItem>>(emptyList()) }

    // Address search using AddressDatabase (Benin City) + Mapbox
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

    // Cumulative Fleet Payload Metrics
    val totalBatchWeight = batchStops.sumOf { it.weight.toDoubleOrNull() ?: 1.0 }
    val isOverweight = totalBatchWeight > 20.0

    // Pricing Calculation
    val pendingQuote by viewModel.pendingQuote.collectAsState()

    LaunchedEffect(pickup, batchStops) {
        val firstDest = batchStops.firstOrNull()?.destinationAddress ?: ""
        if (pickup.isNotBlank() && firstDest.isNotBlank() && pickup.length >= 6 && firstDest.length >= 6) {
            viewModel.calculateDynamicPriceAsync(
                serviceType = "Batch",
                pickup = pickup,
                delivery = firstDest,
                weight = totalBatchWeight,
                quantity = batchStops.size,
                length = 20,
                width = 15,
                height = 10,
                stopsCount = (batchStops.size - 1).coerceAtLeast(0),
                insuranceType = "none"
            )
        } else {
            viewModel.clearQuote()
        }
    }

    LaunchedEffect(draft) {
        if (pickup.isEmpty()) pickup = draft.pickupAddress
    }

    LaunchedEffect(pickup, batchStops, sName, sPhone) {
        val firstStop = batchStops.firstOrNull()
        viewModel.updateDraftPickup(pickup)
        viewModel.updateDraftDelivery(firstStop?.destinationAddress ?: "")
        viewModel.updateDraftSenderInfo(sName, sPhone)
        viewModel.updateDraftReceiverInfo(firstStop?.recipientName ?: "", firstStop?.recipientPhone ?: "")
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
        BackHandler { onNavigate("BACK") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HeaderBgColor)
        ) {
            ScreenHeader(
                title = "Batch Booking",
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
                            focusManager.clearFocus()
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
                                border = BorderStroke(1.dp, Gold.copy(alpha = 0.2f)),
                                modifier = Modifier
                                    .width(210.dp)
                                    .height(78.dp)
                                    .clickable {
                                        if (batchStops.isNotEmpty()) {
                                            val mutable = batchStops.toMutableList()
                                            mutable[0] = mutable[0].copy(
                                                destinationAddress = addr,
                                                recipientName = name,
                                                recipientPhone = phone
                                            )
                                            batchStops = mutable
                                        } else {
                                            batchStops = listOf(
                                                BatchDestinationItem(
                                                    destinationAddress = addr,
                                                    recipientName = name,
                                                    recipientPhone = phone
                                                )
                                            )
                                        }
                                        Toast.makeText(context, "Recipient details loaded!", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.History, null, tint = Gold, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLight) Obsidian else Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = addr,
                                        fontSize = 10.sp,
                                        color = TextGray,
                                        lineHeight = 13.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
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
                    Column(modifier = Modifier.padding(20.dp)) {
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
                        if (pickupFocused && suggestionItems.isNotEmpty()) {
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
                                                    pickup = item.displayInput
                                                    pickupFocused = false
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
                                        HorizontalDivider(color = dividerColor)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sender Info Card
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    border = BorderStroke(1.dp, batchBorderColor),
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Sender Contact Details", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Gold)
                        Spacer(modifier = Modifier.height(14.dp))
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = sName,
                                onValueChange = { sName = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                placeholder = { Text("Sender Name", color = TextGray) },
                                leadingIcon = { Icon(Icons.Filled.Person, null, tint = accentIconColor, modifier = Modifier.size(18.dp)) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Bold, fontSize = 13.sp),
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
                            val isSPhoneValid = sPhone.isBlank() || viewModel.isValidNigerianPhoneNumber(sPhone)
                            OutlinedTextField(
                                value = sPhone,
                                onValueChange = { sPhone = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                isError = sPhone.isNotBlank() && !isSPhoneValid,
                                supportingText = {
                                    if (sPhone.isNotBlank() && !isSPhoneValid) {
                                        Text("Invalid Nigerian phone number", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                                    }
                                },
                                placeholder = { Text("Sender Phone (e.g. 08012345678)", color = TextGray) },
                                leadingIcon = { Icon(Icons.Filled.Phone, null, tint = if (sPhone.isNotBlank() && !isSPhoneValid) MaterialTheme.colorScheme.error else accentIconColor, modifier = Modifier.size(18.dp)) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (sPhone.isNotBlank() && !isSPhoneValid) MaterialTheme.colorScheme.error else accentColor,
                                    unfocusedBorderColor = if (sPhone.isNotBlank() && !isSPhoneValid) MaterialTheme.colorScheme.error else fieldBorderColor,
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

                // Cumulative Fleet Cargo Capacity Meter
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Charcoal,
                    border = BorderStroke(1.dp, if (isOverweight) Color(0xFFFF5252) else Gold.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AnimatedHugeIcon(
                                    icon = Hugeicons.Solid.Weight,
                                    contentDescription = "Weight Meter",
                                    tint = if (isOverweight) Color(0xFFFF5252) else Gold,
                                    size = 20.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Fleet Payload Meter", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = AppTextColor)
                            }
                            Text(
                                "${String.format("%.1f", totalBatchWeight)}kg / 20.0kg",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = if (isOverweight) Color(0xFFFF5252) else Gold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        val meterProgress = (totalBatchWeight.toFloat() / 20.0f).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = meterProgress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (isOverweight) Color(0xFFFF5252) else Gold,
                            trackColor = LuxuryBlack
                        )
                        if (isOverweight) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Total batch weight exceeds 20kg motorcycle fleet capacity. Please reduce payload or split destinations.",
                                fontSize = 11.sp,
                                color = Color(0xFFFF5252),
                                lineHeight = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Structured Dropoff Stop Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dropoff Destinations (${batchStops.size}/5)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = accentColor
                    )

                    if (batchStops.size < 5) {
                        Button(
                            onClick = {
                                batchStops = batchStops + BatchDestinationItem(
                                    itemName = "Package ${batchStops.size + 1}",
                                    weight = "1.5"
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp), tint = Obsidian)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Stop", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Obsidian)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                batchStops.forEachIndexed { index, stop ->
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = Charcoal,
                        border = BorderStroke(1.dp, batchBorderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Stop Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Gold.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        "STOP #${index + 1}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Gold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        letterSpacing = 1.sp
                                    )
                                }

                                if (batchStops.size > 1) {
                                    IconButton(
                                        onClick = {
                                            val mutable = batchStops.toMutableList()
                                            mutable.removeAt(index)
                                            batchStops = mutable
                                            if (focusedDestinationIndex == index) {
                                                focusedDestinationIndex = -1
                                                suggestionItems = emptyList()
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Filled.Delete, "Remove Stop", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Destination Address Field
                            OutlinedTextField(
                                value = stop.destinationAddress,
                                onValueChange = { newAddr ->
                                    val mutable = batchStops.toMutableList()
                                    mutable[index] = mutable[index].copy(destinationAddress = newAddr)
                                    batchStops = mutable
                                    pickupFocused = false
                                    focusedDestinationIndex = index
                                    performSearch(newAddr)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged {
                                        if (it.isFocused) {
                                            pickupFocused = false
                                            focusedDestinationIndex = index
                                            performSearch(stop.destinationAddress)
                                        }
                                    },
                                shape = RoundedCornerShape(16.dp),
                                placeholder = { Text("Dropoff Address for Stop #${index + 1}", color = TextGray, fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Filled.Navigation, null, tint = accentIconColor, modifier = Modifier.size(18.dp)) },
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

                            // Autocomplete Dropdown for currently focused dropoff
                            if (focusedDestinationIndex == index && suggestionItems.isNotEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 220.dp)
                                        .padding(vertical = 6.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isDark) MapStandardBg else GoldenWhite),
                                    border = BorderStroke(1.dp, accentColor),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                                ) {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        suggestionItems.forEach { item ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        val mutable = batchStops.toMutableList()
                                                        mutable[index] = mutable[index].copy(destinationAddress = item.displayInput)
                                                        batchStops = mutable
                                                        focusedDestinationIndex = -1
                                                        suggestionItems = emptyList()
                                                    }
                                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(Gold.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Filled.Place, null, tint = Gold, modifier = Modifier.size(14.dp))
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(item.title, color = AppTextColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    if (item.fullAddress.isNotBlank() && item.fullAddress != item.title) {
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(item.fullAddress, color = TextGray, fontSize = 10.sp, maxLines = 1)
                                                    }
                                                }
                                            }
                                            HorizontalDivider(color = dividerColor)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Recipient Info Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Recipient", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                Text(
                                    "From Contacts",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Obsidian,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Gold)
                                        .clickable {
                                            activeContactPickerStopIndex = index
                                            contactPickerLauncher.launch(
                                                Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                                            )
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = stop.recipientName,
                                    onValueChange = { newName ->
                                        val mutable = batchStops.toMutableList()
                                        mutable[index] = mutable[index].copy(recipientName = newName)
                                        batchStops = mutable
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    placeholder = { Text("Recipient Name", color = TextGray, fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Filled.Person, null, tint = accentIconColor, modifier = Modifier.size(16.dp)) },
                                    textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Bold, fontSize = 12.sp),
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

                                val isStopPhoneValid = stop.recipientPhone.isBlank() || viewModel.isValidNigerianPhoneNumber(stop.recipientPhone)
                                OutlinedTextField(
                                    value = stop.recipientPhone,
                                    onValueChange = { newPhone ->
                                        val mutable = batchStops.toMutableList()
                                        mutable[index] = mutable[index].copy(recipientPhone = newPhone)
                                        batchStops = mutable
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    isError = stop.recipientPhone.isNotBlank() && !isStopPhoneValid,
                                    supportingText = {
                                        if (stop.recipientPhone.isNotBlank() && !isStopPhoneValid) {
                                            Text("Invalid Nigerian phone number", color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
                                        }
                                    },
                                    placeholder = { Text("Recipient Phone (e.g. 08012345678)", color = TextGray, fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Filled.Phone, null, tint = if (stop.recipientPhone.isNotBlank() && !isStopPhoneValid) MaterialTheme.colorScheme.error else accentIconColor, modifier = Modifier.size(16.dp)) },
                                    textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (stop.recipientPhone.isNotBlank() && !isStopPhoneValid) MaterialTheme.colorScheme.error else accentColor,
                                        unfocusedBorderColor = if (stop.recipientPhone.isNotBlank() && !isStopPhoneValid) MaterialTheme.colorScheme.error else fieldBorderColor,
                                        focusedContainerColor = fieldBgColor,
                                        unfocusedContainerColor = fieldBgColor,
                                        focusedTextColor = fieldTextColor,
                                        unfocusedTextColor = fieldTextColor,
                                        focusedPlaceholderColor = TextGray,
                                        unfocusedPlaceholderColor = TextGray
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Item Name & Weight - Stacked Vertically
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = stop.itemName,
                                    onValueChange = { newDesc ->
                                        val mutable = batchStops.toMutableList()
                                        mutable[index] = mutable[index].copy(itemName = newDesc)
                                        batchStops = mutable
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    placeholder = { Text("Parcel content / description", color = TextGray, fontSize = 12.sp) },
                                    leadingIcon = { AnimatedHugeIcon(Hugeicons.Solid.Package, null, tint = accentIconColor, size = 16.dp) },
                                    textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
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
                                    value = stop.weight,
                                    onValueChange = { newWeight ->
                                        val mutable = batchStops.toMutableList()
                                        mutable[index] = mutable[index].copy(weight = newWeight)
                                        batchStops = mutable
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    label = { Text("Package Weight (kg) - Max 20kg", color = TextGray, fontSize = 10.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, fontWeight = FontWeight.Bold, fontSize = 12.sp),
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

                                val stopFeasibility = CargoFeasibilityValidator.validateCargo(
                                    itemName = stop.itemName,
                                    weightKg = stop.weight.toDoubleOrNull() ?: 1.0
                                )
                                if (!stopFeasibility.isFeasible) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = stopFeasibility.warningMessage ?: "Item exceeds motorcycle limit",
                                        color = Color(0xFFFF5252),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        // Bottom Pricing Summary - overlayed (hidden when typing address to prevent screen occlusion)
        if (!pickupFocused && focusedDestinationIndex == -1) {
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
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
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

                val firstDest = batchStops.firstOrNull()?.destinationAddress ?: ""
                val isAddressesValid = pickup.trim().length >= 6 && batchStops.isNotEmpty() && batchStops.all { it.destinationAddress.trim().length >= 6 }
                val isSPhoneValid = sPhone.isBlank() || viewModel.isValidNigerianPhoneNumber(sPhone)
                val isAllRecipientsPhoneValid = batchStops.all { it.recipientPhone.isNotBlank() && viewModel.isValidNigerianPhoneNumber(it.recipientPhone) }
                val isContactValid = sName.trim().isNotBlank() && isSPhoneValid && batchStops.all { it.recipientName.trim().isNotBlank() } && isAllRecipientsPhoneValid
                val isCargoValid = !isOverweight && batchStops.all { !com.esdispatch.util.CargoFeasibilityValidator.isStrictlyInfeasible(it.itemName, it.weight.toDoubleOrNull() ?: 1.0) }
                val isBookingEnabled = isAddressesValid && isContactValid && isCargoValid && pendingQuote is PendingQuote.Success

                Button(
                    onClick = {
                        if (!isContactValid) {
                            Toast.makeText(context, "Please provide valid Nigerian contact phone numbers", Toast.LENGTH_SHORT).show()
                        } else {
                            showCheckoutSheet = true
                        }
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
        }

        val quotePrice = (pendingQuote as? PendingQuote.Success)?.price ?: 0.0

        if (showCheckoutSheet) {
            WalletCheckoutSheet(
                bookingPrice = quotePrice,
                walletBalance = viewModel.walletBalance.collectAsState().value,
                onConfirmWalletPayment = {
                    showCheckoutSheet = false
                    val firstStop = batchStops.firstOrNull()
                    viewModel.updateDraftPickup(pickup)
                    viewModel.updateDraftDelivery(firstStop?.destinationAddress ?: "")
                    viewModel.updateDraftSenderInfo(sName, sPhone)
                    viewModel.updateDraftReceiverInfo(firstStop?.recipientName ?: "", firstStop?.recipientPhone ?: "")
                    viewModel.updateDraftSpecs(
                        quantity = batchStops.size,
                        weight = totalBatchWeight,
                        length = 20,
                        width = 15,
                        height = 10
                    )
                    viewModel.updateDraftAdditionalStops(batchStops.drop(1).map { it.destinationAddress })
                    viewModel.finalizeDraftPrice("Batch", quotePrice)
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
                    val firstStop = batchStops.firstOrNull()
                    viewModel.topUpWallet(pendingAmount)
                    viewModel.updateDraftPickup(pickup)
                    viewModel.updateDraftDelivery(firstStop?.destinationAddress ?: "")
                    viewModel.updateDraftSenderInfo(sName, sPhone)
                    viewModel.updateDraftReceiverInfo(firstStop?.recipientName ?: "", firstStop?.recipientPhone ?: "")
                    viewModel.updateDraftSpecs(
                        quantity = batchStops.size,
                        weight = totalBatchWeight,
                        length = 20,
                        width = 15,
                        height = 10
                    )
                    viewModel.updateDraftAdditionalStops(batchStops.drop(1).map { it.destinationAddress })
                    viewModel.finalizeDraftPrice("Batch", quotePrice)
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
