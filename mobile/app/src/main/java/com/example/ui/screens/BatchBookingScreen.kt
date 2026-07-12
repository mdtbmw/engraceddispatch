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

val nigerianLandmarks = listOf(
    "Aso Rock Presidential Villa, Abuja",
    "Murtala Muhammed International Airport, Ikeja, Lagos",
    "Lekki Conservation Centre, Lagos",
    "National Arts Theatre, Iganmu, Lagos",
    "University of Ibadan, Ibadan",
    "Millennium Park, Maitama, Abuja",
    "Zuma Rock, Madalla, Suleja",
    "Tarkwa Bay Beach, Victoria Island, Lagos",
    "Yankari Game Reserve, Bauchi",
    "Olumo Rock, Abeokuta, Ogun State",
    "Kajuru Castle, Kajuru, Kaduna",
    "Idanre Hill, Idanre, Ondo State",
    "Agodi Gardens, Ibadan",
    "Nike Art Gallery, Lekki, Lagos",
    "Port Harcourt Pleasure Park, Port Harcourt"
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
    var destinations by remember { mutableStateOf(if (draft.deliveryAddress.isNotBlank()) listOf(draft.deliveryAddress) else listOf("Herbert Macaulay Way, Yaba, Lagos")) }
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
                        val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                        val addresses = geocoder.getFromLocationName(query, 5)
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
                        if (pickupFocused && suggestions.isNotEmpty()) {
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
                    Column(modifier = Modifier.padding(20.dp)) {
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
                    Column(modifier = Modifier.padding(20.dp)) {
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
