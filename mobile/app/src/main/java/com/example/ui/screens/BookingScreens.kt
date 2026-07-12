package com.example.ui.screens

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ScreenHeader
import com.example.ui.components.RoundedSheet
import com.example.ui.components.WalletCheckoutSheet
import com.example.ui.components.QuiltedBackground
import com.example.ui.theme.*
import com.example.viewmodel.DeliveryViewModel
import com.example.viewmodel.PendingQuote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

@Composable
fun SendParcelScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val draft by viewModel.parcelDraft.collectAsState()
    val context = LocalContext.current
    val currentUserName by viewModel.userName.collectAsState()
    val currentUserPhone by viewModel.userPhone.collectAsState()

    var pickup by remember { mutableStateOf(draft.pickupAddress) }
    var delivery by remember { mutableStateOf(draft.deliveryAddress) }

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        if (pickup.isBlank() || pickup.contains("Murtala") || pickup.contains("Lekki")) {
            val detected = withContext(Dispatchers.IO) {
                detectUserLocation(context)
            }
            if (pickup.isBlank() || pickup.contains("Murtala") || pickup.contains("Lekki")) {
                pickup = detected
            }
        }
    }

    var quantity by remember { mutableStateOf(draft.quantity.toString()) }
    var weight by remember { mutableStateOf(draft.weight.toString()) }

    var length by remember { mutableStateOf(draft.length.toString()) }
    var width by remember { mutableStateOf(draft.width.toString()) }
    var height by remember { mutableStateOf(draft.height.toString()) }

    var sName by remember { mutableStateOf(draft.senderName.ifBlank { currentUserName }) }
    var sPhone by remember { mutableStateOf(draft.senderPhone.ifBlank { currentUserPhone }) }

    var rName by remember { mutableStateOf(draft.receiverName) }
    var rPhone by remember { mutableStateOf(draft.receiverPhone) }

    LaunchedEffect(currentUserName, currentUserPhone, draft) {
        if (sName.isBlank()) {
            sName = draft.senderName.ifBlank { currentUserName }
        }
        if (sPhone.isBlank()) {
            sPhone = draft.senderPhone.ifBlank { currentUserPhone }
        }
        if (pickup.isBlank() && draft.pickupAddress.isNotBlank()) {
            pickup = draft.pickupAddress
        }
        if (delivery.isBlank() && draft.deliveryAddress.isNotBlank()) {
            delivery = draft.deliveryAddress
        }
    }

    var selectedTab by remember { mutableStateOf(draft.selectedService.ifBlank { "Express" }) }

    val baseFare by viewModel.baseFare.collectAsState()
    val perKgRate by viewModel.perKgRate.collectAsState()
    val surgeMultiplier by viewModel.surgeMultiplier.collectAsState()
    val isDynamicPricingEnabled by viewModel.isDynamicPricingEnabled.collectAsState()
    val wt = weight.toDoubleOrNull() ?: 2.5
    val distanceKm = remember(draft.pickupAddress, draft.deliveryAddress, isDynamicPricingEnabled) {
        if (isDynamicPricingEnabled) {
            viewModel.estimateDistanceBetween(draft.pickupAddress, draft.deliveryAddress)
        } else {
            5.0 // Manual flat distance multiplier
        }
    }
    val calculatedAmt = remember(selectedTab, wt, baseFare, perKgRate, surgeMultiplier, distanceKm) {
        val base = when (selectedTab) {
            "Express" -> baseFare * 1.5 + (wt * perKgRate) + 1500.0 + (distanceKm * 150.0)
            "Economy" -> baseFare * 0.7 + (wt * perKgRate * 0.8) + (distanceKm * 100.0)
            "Batch" -> baseFare * 0.9 + (wt * perKgRate * 0.9) + (distanceKm * 110.0)
            else -> baseFare * 2.0 + (wt * perKgRate * 1.2) + (distanceKm * 180.0)
        }
        base * surgeMultiplier
    }

    var dropdownExpanded by remember { mutableStateOf(false) }

    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val fieldBgColor = if (isLight) GoldenWhiteLight else Charcoal
    val fieldTextColor = if (isLight) Obsidian else Color.White
    val fieldBorderColor = if (isLight) Slate else Gold.copy(alpha = 0.3f)

    val scrollState = rememberScrollState()

    val isDark = !isLight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryBlack)
    ) {
        val allParcels by viewModel.parcels.collectAsState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HeaderBgColor)
        ) {
            ScreenHeader(
                title = "Send Parcel",
                onBack = { onNavigate("Dashboard") }
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
                        .padding(bottom = 120.dp) // space for bottom CTA button
                ) {
                // 1. Smart "Book Again" from History Row
                if (allParcels.isNotEmpty()) {
                    Column(modifier = Modifier.padding(bottom = 20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚡ Smart 'Book Again' (Recent)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Gold
                            )
                            Text(
                                text = "${allParcels.size} available",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(allParcels.take(5)) { p ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Charcoal,
                                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .width(220.dp)
                                        .clickable {
                                            viewModel.bookAgainFromParcel(p)
                                            pickup = p.pickupAddress
                                            delivery = p.deliveryAddress
                                            Toast.makeText(context, "Loaded delivery history for ${p.itemName}!", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = p.itemName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "To: ${p.deliveryAddress}", fontSize = 10.sp, color = TextGray, maxLines = 1)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "₦${String.format("%,.0f", p.price)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gold)
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Gold.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "Rebook ⚡",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Gold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Addresses Card
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pickup & Delivery",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Gold
                            )
                            // AI Auto-Correct & Pin Drop Actions
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = {
                                        pickup = viewModel.aiCorrectAddress(pickup)
                                        delivery = viewModel.aiCorrectAddress(delivery)
                                        Toast.makeText(context, "✨ AI Address validation & correction applied!", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("✨ AI Correct", fontSize = 11.sp, color = Gold)
                                }
                                TextButton(
                                    onClick = {
                                        delivery = viewModel.pinDropNearestAddress()
                                        Toast.makeText(context, "📍 Pin-dropped nearest landmark detected!", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("📍 Drop Pin", fontSize = 11.sp, color = Gold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Address Form Input Fields (With vertical connector dash line)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Canvas(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(100.dp)
                                    .align(Alignment.CenterStart)
                                    .offset(x = 22.dp)
                            ) {
                                val dashPath = Path().apply {
                                    moveTo(0f, 0f)
                                    lineTo(0f, size.height)
                                }
                                drawPath(
                                    path = dashPath,
                                    color = Gold.copy(alpha = 0.3f),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = 4f,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    )
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                                                // Pickup address
                                OutlinedTextField(
                                    value = pickup,
                                    onValueChange = { pickup = it },
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    placeholder = { Text("Pickup address", color = TextGray) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Place,
                                            contentDescription = null,
                                            tint = Gold,
                                            modifier = Modifier.padding(start = 12.dp).size(22.dp)
                                        )
                                    },
                                    textStyle = TextStyle(color = fieldTextColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Gold,
                                        unfocusedBorderColor = fieldBorderColor,
                                        focusedContainerColor = fieldBgColor,
                                        unfocusedContainerColor = fieldBgColor,
                                        focusedTextColor = fieldTextColor,
                                        unfocusedTextColor = fieldTextColor,
                                        focusedPlaceholderColor = TextGray,
                                        unfocusedPlaceholderColor = TextGray
                                    )
                                )

                                                                // Delivery address
                                OutlinedTextField(
                                    value = delivery,
                                    onValueChange = { delivery = it },
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    placeholder = { Text("Delivery address", color = TextGray) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Place,
                                            contentDescription = null,
                                            tint = Gold,
                                            modifier = Modifier.padding(start = 12.dp).size(22.dp)
                                        )
                                    },
                                    textStyle = TextStyle(color = fieldTextColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Gold,
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

                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(if (isLight) BorderLight else Color(0xFF2E2E2E))
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Specs Layout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Quantity", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Box {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(fieldBgColor, RoundedCornerShape(20.dp))
                                            .clickable { dropdownExpanded = true }
                                            .border(1.dp, fieldBorderColor, RoundedCornerShape(20.dp))
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(quantity, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = fieldTextColor)
                                        Icon(Icons.Filled.ArrowDropDown, null, tint = Gold)
                                    }

                                    DropdownMenu(
                                        expanded = dropdownExpanded,
                                        onDismissRequest = { dropdownExpanded = false },
                                        modifier = Modifier.background(Charcoal)
                                    ) {
                                        (1..10).forEach { num ->
                                            DropdownMenuItem(
                                                text = { Text(text = "$num", color = AppTextColor, fontWeight = FontWeight.Bold) },
                                                onClick = {
                                                    quantity = num.toString()
                                                    dropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Weight", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = weight,
                                    onValueChange = { weight = it },
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    suffix = { Text("kg", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = TextStyle(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = fieldTextColor
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Gold,
                                        unfocusedBorderColor = fieldBorderColor,
                                        focusedContainerColor = fieldBgColor,
                                        unfocusedContainerColor = fieldBgColor,
                                        focusedTextColor = fieldTextColor,
                                        unfocusedTextColor = fieldTextColor
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Dimensions Setup
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DimensionItem(title = "Length", value = length, onValueChange = { length = it }, fieldBgColor, fieldTextColor, fieldBorderColor)
                            DimensionItem(title = "Width", value = width, onValueChange = { width = it }, fieldBgColor, fieldTextColor, fieldBorderColor)
                            DimensionItem(title = "Height", value = height, onValueChange = { height = it }, fieldBgColor, fieldTextColor, fieldBorderColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Sender & Receiver Contact details Card
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Sender Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sender Info.", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Gold)
                            Text(
                                "Use Default",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Obsidian,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Gold)
                                    .clickable {
                                        sName = currentUserName.ifBlank { "Guest Sender" }
                                        sPhone = currentUserPhone.ifBlank { "" }
                                        Toast.makeText(context, "Loaded profile defaults!", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = sName,
                                onValueChange = { sName = it },
                                modifier = Modifier
                                    .weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                placeholder = { Text("Name", color = TextGray) },
                                leadingIcon = { Icon(Icons.Filled.Person, null, tint = Gold, modifier = Modifier.size(18.dp)) },
                                textStyle = TextStyle(color = fieldTextColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Gold,
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
                                placeholder = { Text("Phone", color = TextGray) },
                                leadingIcon = { Icon(Icons.Filled.Phone, null, tint = Gold, modifier = Modifier.size(18.dp)) },
                                textStyle = TextStyle(color = fieldTextColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Gold,
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
                            Text("Receiver Info.", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Gold)
                            Text(
                                "From Contacts",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Obsidian,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Gold)
                                    .clickable {
                                        val past = allParcels.filter { it.receiverName.isNotBlank() && it.receiverPhone.isNotBlank() }
                                            .map { it.receiverName to it.receiverPhone }
                                            .distinctBy { it.first }
                                        if (past.isNotEmpty()) {
                                            val first = past.first()
                                            rName = first.first
                                            rPhone = first.second
                                            Toast.makeText(context, "Loaded past recipient: ${first.first}!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "No past recipients in database yet. Please enter details manually.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = rName,
                                onValueChange = { rName = it },
                                modifier = Modifier
                                    .weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                placeholder = { Text("Name", color = TextGray) },
                                leadingIcon = { Icon(Icons.Filled.Person, null, tint = Gold, modifier = Modifier.size(18.dp)) },
                                textStyle = TextStyle(color = fieldTextColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Gold,
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
                                placeholder = { Text("Phone", color = TextGray) },
                                leadingIcon = { Icon(Icons.Filled.Phone, null, tint = Gold, modifier = Modifier.size(18.dp)) },
                                textStyle = TextStyle(color = fieldTextColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Gold,
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

        // Bottom CTA Quote Button (stays pinned nicely at the bottom)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Charcoal,
            tonalElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                val isAddressesValid = pickup.trim().length >= 6 && delivery.trim().length >= 6
                val isFormValid = isAddressesValid && sName.isNotBlank() && sPhone.isNotBlank() && rName.isNotBlank() && rPhone.isNotBlank()

                Button(
                    onClick = {
                        viewModel.updateDraftPickup(pickup)
                        viewModel.updateDraftDelivery(delivery)
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
                    enabled = isFormValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Obsidian,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                        contentColor = Gold,
                        disabledContentColor = TextGray
                    ),
                    border = BorderStroke(1.2.dp, if (isFormValid) Gold else Color.Gray.copy(alpha = 0.3f))
                ) {
                    Text("Get a Quote", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = if (isFormValid) Gold else TextGray)
                }
            }
        }
    }
}

@Composable
fun DimensionItem(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    fieldBgColor: Color,
    fieldTextColor: Color,
    fieldBorderColor: Color
) {
    Column(
        modifier = Modifier.width(84.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            suffix = { Text("cm", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray) },
            textStyle = TextStyle(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = fieldTextColor
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gold,
                unfocusedBorderColor = fieldBorderColor,
                focusedContainerColor = fieldBgColor,
                unfocusedContainerColor = fieldBgColor,
                focusedTextColor = fieldTextColor,
                unfocusedTextColor = fieldTextColor
            )
        )
    }
}

@Composable
fun BookingSelectionScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val draft by viewModel.parcelDraft.collectAsState()
    val walletBalance by viewModel.walletBalance.collectAsState()
    var selectedTab by remember { mutableStateOf(draft.selectedService) }

    val wt = draft.weight.takeIf { it > 0.0 } ?: 1.0

    // Geocoding Coordinates State
    var pickupCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var deliveryCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var isGeocoding by remember { mutableStateOf(false) }

    LaunchedEffect(draft.pickupAddress, draft.deliveryAddress) {
        if (draft.pickupAddress.isNotBlank() && draft.deliveryAddress.isNotBlank()) {
            isGeocoding = true
            pickupCoords = viewModel.geocodeAddress(draft.pickupAddress)
            deliveryCoords = viewModel.geocodeAddress(draft.deliveryAddress)
            isGeocoding = false
        }
    }

    // Dynamic price quotes calculated from validated coordinates
    val quoteExpressSameDay = remember(pickupCoords, deliveryCoords, wt) {
        val p = pickupCoords ?: Pair(6.5244, 3.3792)
        val d = deliveryCoords ?: Pair(6.4281, 3.4219)
        viewModel.calculateDynamicQuote(
            originLat = p.first, originLng = p.second,
            destLat = d.first, destLng = d.second,
            serviceType = "Express", weight = wt, pickupAddress = draft.pickupAddress, deliveryAddress = draft.deliveryAddress
        )
    }
    val priceExpressSameDay = (quoteExpressSameDay as? PendingQuote.Success)?.price ?: 4500.0

    val quoteExpressNextDay = remember(pickupCoords, deliveryCoords, wt) {
        val p = pickupCoords ?: Pair(6.5244, 3.3792)
        val d = deliveryCoords ?: Pair(6.4281, 3.4219)
        viewModel.calculateDynamicQuote(
            originLat = p.first, originLng = p.second,
            destLat = d.first, destLng = d.second,
            serviceType = "Economy", weight = wt, pickupAddress = draft.pickupAddress, deliveryAddress = draft.deliveryAddress
        )
    }
    val priceExpressNextDay = ((quoteExpressNextDay as? PendingQuote.Success)?.price ?: 3500.0) * 0.8

    val quoteEconomy = remember(pickupCoords, deliveryCoords, wt) {
        val p = pickupCoords ?: Pair(6.5244, 3.3792)
        val d = deliveryCoords ?: Pair(6.4281, 3.4219)
        viewModel.calculateDynamicQuote(
            originLat = p.first, originLng = p.second,
            destLat = d.first, destLng = d.second,
            serviceType = "Economy", weight = wt, pickupAddress = draft.pickupAddress, deliveryAddress = draft.deliveryAddress
        )
    }
    val priceEconomy = (quoteEconomy as? PendingQuote.Success)?.price ?: 3000.0

    val quoteBatch = remember(pickupCoords, deliveryCoords, wt) {
        val p = pickupCoords ?: Pair(6.5244, 3.3792)
        val d = deliveryCoords ?: Pair(6.4281, 3.4219)
        viewModel.calculateDynamicQuote(
            originLat = p.first, originLng = p.second,
            destLat = d.first, destLng = d.second,
            serviceType = "Batch", weight = wt, pickupAddress = draft.pickupAddress, deliveryAddress = draft.deliveryAddress
        )
    }
    val priceBatch = (quoteBatch as? PendingQuote.Success)?.price ?: 5000.0

    val quoteMulti = remember(pickupCoords, deliveryCoords, wt) {
        val p = pickupCoords ?: Pair(6.5244, 3.3792)
        val d = deliveryCoords ?: Pair(6.4281, 3.4219)
        viewModel.calculateDynamicQuote(
            originLat = p.first, originLng = p.second,
            destLat = d.first, destLng = d.second,
            serviceType = "Multi", weight = wt, pickupAddress = draft.pickupAddress, deliveryAddress = draft.deliveryAddress
        )
    }
    val priceMulti = (quoteMulti as? PendingQuote.Success)?.price ?: 7500.0

    val calculatedAmt = remember(selectedTab, priceExpressSameDay, priceExpressNextDay, priceEconomy, priceBatch, priceMulti) {
        when (selectedTab) {
            "Express" -> priceExpressSameDay
            "Economy" -> priceEconomy
            "Batch" -> priceBatch
            "Multi" -> priceMulti
            else -> priceExpressSameDay
        }
    }

    var showCheckoutSheet by remember { mutableStateOf(false) }
    var showPaystackSheet by remember { mutableStateOf(false) }
    var pendingAmount by remember { mutableStateOf(0.0) }
    var fundingAmount by remember { mutableStateOf(0.0) }
    var onPaymentSuccessAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val scrollState = rememberScrollState()

    val accentBorderColor = if (isLight) Obsidian else Gold
    val accentIconColor = if (isLight) Obsidian else Gold
    val accentTextColor = if (isLight) Obsidian else Gold

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
                title = "Select Service",
                onBack = { onNavigate("SendParcelDetails") }
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
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                        .padding(bottom = 120.dp) // space for bottom checkout bar
                ) {
                // Tabs Picker Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Charcoal, CircleShape)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Express", "Economy", "Batch", "Multi").forEach { tab ->
                        val isSelected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Gold else Color.Transparent)
                                .clickable { selectedTab = tab },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) Obsidian else TextGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Quote Card display
                if (selectedTab == "Express") {
                    // Quote 1 Recommended
                    Surface(
                        onClick = {
                            pendingAmount = priceExpressSameDay
                            onPaymentSuccessAction = {
                                viewModel.finalizeDraftPrice("Express", priceExpressSameDay)
                                viewModel.confirmBooking()
                                onNavigate("PaymentSuccess")
                            }
                            showCheckoutSheet = true
                        },
                        shape = RoundedCornerShape(24.dp),
                        color = Charcoal,
                        border = BorderStroke(2.dp, accentBorderColor),
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // Recommended Tag at top end
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .background(
                                        Gold,
                                        RoundedCornerShape(bottomStart = 12.dp, topEnd = 24.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                        Text(
                                            "RECOMMENDED",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Obsidian
                                        )
                            }

                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Same Day", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = AppTextColor)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(Icons.Filled.Bolt, null, tint = accentIconColor, modifier = Modifier.size(16.dp))
                                        }
                                        Text("Delivery within 12 hours", fontSize = 12.sp, color = TextGray, fontWeight = FontWeight.Medium)
                                    }
                                    Text("₦${String.format("%,.2f", priceExpressSameDay)}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = accentTextColor)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        "Real-time tracking",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier
                                            .background(Obsidian, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                    Text(
                                        "Insurance included",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Obsidian,
                                        modifier = Modifier
                                            .background(Gold, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quote 2
                    Surface(
                        onClick = {
                            pendingAmount = priceExpressNextDay
                            onPaymentSuccessAction = {
                                viewModel.finalizeDraftPrice("Express", priceExpressNextDay)
                                viewModel.confirmBooking()
                                onNavigate("PaymentSuccess")
                            }
                            showCheckoutSheet = true
                        },
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Next Day", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = AppTextColor)
                                    Text("Delivery by tomorrow 6 PM", fontSize = 12.sp, color = TextGray, fontWeight = FontWeight.Medium)
                                }
                                Text("₦${String.format("%,.2f", priceExpressNextDay)}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = accentTextColor)
                            }
                        }
                    }
                } else if (selectedTab == "Economy") {
                    Surface(
                        onClick = {
                            pendingAmount = priceEconomy
                            onPaymentSuccessAction = {
                                viewModel.finalizeDraftPrice("Economy", priceEconomy)
                                viewModel.confirmBooking()
                                onNavigate("PaymentSuccess")
                            }
                            showCheckoutSheet = true
                        },
                        shape = RoundedCornerShape(24.dp),
                        color = Charcoal,
                        border = BorderStroke(2.dp, accentBorderColor),
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Standard", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = AppTextColor)
                                    Text("Delivery in 3-5 business days", fontSize = 12.sp, color = TextGray, fontWeight = FontWeight.Medium)
                                }
                                Text("₦${String.format("%,.2f", priceEconomy)}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = accentTextColor)
                            }
                        }
                    }
                } else if (selectedTab == "Batch") {
                    Surface(
                        onClick = {
                            pendingAmount = priceBatch
                            onPaymentSuccessAction = {
                                viewModel.finalizeDraftPrice("Batch", priceBatch)
                                viewModel.confirmBooking()
                                onNavigate("PaymentSuccess")
                            }
                            showCheckoutSheet = true
                        },
                        shape = RoundedCornerShape(24.dp),
                        color = Charcoal,
                        border = BorderStroke(2.dp, accentBorderColor),
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Multi-parcel", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = AppTextColor)
                                    Text("Bulk delivery optimization", fontSize = 12.sp, color = TextGray, fontWeight = FontWeight.Medium)
                                }
                                Text("₦${String.format("%,.2f", priceBatch)}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = accentTextColor)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Up to 10 parcels included", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentTextColor)
                        }
                    }
                } else {
                    Surface(
                        onClick = {
                            pendingAmount = priceMulti
                            onPaymentSuccessAction = {
                                viewModel.finalizeDraftPrice("Multi", priceMulti)
                                viewModel.confirmBooking()
                                onNavigate("PaymentSuccess")
                            }
                            showCheckoutSheet = true
                        },
                        shape = RoundedCornerShape(24.dp),
                        color = Charcoal,
                        border = BorderStroke(2.dp, accentBorderColor),
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Multi-Stop", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = AppTextColor)
                                    Text("Deliver to up to 5 destinations", fontSize = 12.sp, color = TextGray, fontWeight = FontWeight.Medium)
                                }
                                Text("₦${String.format("%,.2f", priceMulti)}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = accentTextColor)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Up to 5 addresses included", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentTextColor)
                        }
                    }
                }

                // Traffic & Weather Delivery Conditions Alert Block
                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Live Dispatch Conditions",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentTextColor,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Weather Warning Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isDark) Color(0xFF1B1B1B) else GoldenWhite, RoundedCornerShape(16.dp))
                                .border(1.dp, if (isDark) Color.Yellow.copy(alpha = 0.2f) else Slate, RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Cloud,
                                contentDescription = null,
                                tint = Color.Yellow,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Wet Road Weather Alert",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) Color.White else Obsidian
                                )
                                Text(
                                    text = "Overcast with heavy showers in Ikeja. Dispatch times adjusted with a +15 mins safety delay.",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Traffic Congestion Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isDark) Color(0xFF1B1B1B) else GoldenWhite, RoundedCornerShape(16.dp))
                                .border(1.dp, if (isDark) Color.Red.copy(alpha = 0.2f) else Slate, RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Traffic,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Live Congestion ETA Update",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) Color.White else Obsidian
                                )
                                Text(
                                    text = "Heavy traffic along Lagos-Ikorodu expressway. Average speed is 18 km/h. Live route ETA: 52 mins.",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Payment Card Selector
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Charcoal,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Payment Method", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = accentTextColor)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Obsidian, RoundedCornerShape(24.dp))
                                .border(2.dp, Gold, RoundedCornerShape(24.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Charcoal),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.AccountBalanceWallet, null, tint = Gold)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Prepaid Dispatch Wallet", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color.White)
                                    Text("Balance: ₦${String.format("%,.2f", walletBalance)}", fontSize = 11.sp, color = Gold, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Solid gold inner radio pill selector
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .border(6.dp, Gold, CircleShape)
                                    .background(Color.Transparent, CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Delivery charges will be securely deducted from your prepaid balance. If your balance is insufficient, you can fund it instantly via Paystack during payment verification.",
                            fontSize = 11.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }

        // Bottom select checkout bar
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
                    Text("Total Amount", fontSize = 12.sp, color = TextGray, fontWeight = FontWeight.Bold)
                    Text(
                        text = "₦${String.format("%,.2f", calculatedAmt)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = accentTextColor
                    )
                }

                Button(
                    onClick = {
                        pendingAmount = calculatedAmt
                        onPaymentSuccessAction = {
                            viewModel.finalizeDraftPrice(selectedTab)
                            viewModel.confirmBooking()
                            onNavigate("PaymentSuccess")
                        }
                        showCheckoutSheet = true
                    },
                    modifier = Modifier
                        .width(160.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Obsidian),
                    border = BorderStroke(1.2.dp, Gold)
                ) {
                    Text("Pay Now", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Gold)
                }
            }
        }

        if (showCheckoutSheet) {
            WalletCheckoutSheet(
                bookingPrice = pendingAmount,
                walletBalance = viewModel.walletBalance.collectAsState().value,
                onConfirmWalletPayment = {
                    showCheckoutSheet = false
                    onPaymentSuccessAction?.invoke()
                },
                onFundRequired = { missingAmt ->
                    showCheckoutSheet = false
                    fundingAmount = missingAmt
                    showPaystackSheet = true
                },
                onDismiss = { showCheckoutSheet = false }
            )
        }

        if (showPaystackSheet) {
            PaystackCheckoutSheet(
                amount = fundingAmount,
                onPaymentComplete = { reference ->
                    showPaystackSheet = false
                    viewModel.topUpWallet(fundingAmount)
                    onPaymentSuccessAction?.invoke()
                },
                onDismiss = { showPaystackSheet = false }
            )
        }
    }
}

@Composable
fun PaymentSuccessScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val scale = remember { Animatable(0f) }
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val isDark = !isLight
    val context = LocalContext.current
    val parcels by viewModel.parcels.collectAsState()
    val latestParcel = parcels.firstOrNull()
    val currentUserName by viewModel.userName.collectAsState()
    val currentUserPhone by viewModel.userPhone.collectAsState()

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

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
                title = "Payment Success",
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
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Celebration circle card
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(scale.value),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .background(Gold.copy(alpha = 0.15f), CircleShape)
                        )
                        Surface(
                            modifier = Modifier.size(110.dp),
                            shape = CircleShape,
                            color = Charcoal,
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f)),
                            shadowElevation = 0.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Success",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Payment Successful", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = AppTextColor)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("PREMIUM DISPATCH CONFIRMED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 1.5.sp)
                    Spacer(modifier = Modifier.height(24.dp))

                    // Scalloped / Premium Ticket Receipt Card
                    Surface(
                        color = Charcoal,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, if (isDark) BorderDark else Slate),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("ENGRACED DISPATCH", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Gold, fontFamily = SpaceGrotesk)
                                    Text("PREMIUM LOGISTICS & DISPATCH", fontSize = 8.sp, fontWeight = FontWeight.Medium, color = TextGray, letterSpacing = 1.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(Gold.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("PAID", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Gold)
                                }
                            }

                            // Thin Divider line
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderDark))

                            // Key Fields: Tracking ID & Amount Paid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("TRACKING ID", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .background(if (isDark) BackgroundDark else BackgroundLight, RoundedCornerShape(8.dp))
                                            .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = latestParcel?.id ?: "ENG-824-LGS",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isDark) Color.White else Obsidian,
                                            fontFamily = SpaceGrotesk
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("AMOUNT PAID", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                    Text(
                                        text = "₦${String.format("%,.2f", latestParcel?.price ?: 2500.00)}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Gold,
                                        fontFamily = SpaceGrotesk
                                    )
                                }
                            }

                            // Thin Divider line
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderDark))

                            // Sender & Receiver Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("SENDER", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                    Text(latestParcel?.senderName ?: currentUserName.ifBlank { "Elite Member" }, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(latestParcel?.senderPhone ?: currentUserPhone.ifBlank { "+234 803 123 4567" }, fontSize = 10.sp, color = TextGray)
                                }
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text("RECIPIENT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                    Text(latestParcel?.receiverName ?: "Tunde Balogun", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(latestParcel?.receiverPhone ?: "+234 812 345 6789", fontSize = 10.sp, color = TextGray)
                                }
                            }

                            // Pickup & Delivery Address
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("PICKUP ADDRESS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                Text(latestParcel?.pickupAddress ?: "No. 12 Obafemi Awolowo Way, Ikeja", fontSize = 11.sp, color = TextGray)
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("DELIVERY ADDRESS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                Text(latestParcel?.deliveryAddress ?: "Lekki Phase 1, Lagos", fontSize = 11.sp, color = TextGray)
                            }

                            // Item Description
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("ITEM DESCRIPTION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                Text(latestParcel?.itemName ?: "Premium Package Dispatch", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }

                            // Thin Divider line
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderDark))

                            // Footer Note
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Saved to your permanent Booking History",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Streamlined Navigation Flow Actions
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Primary Highlight Action: Track Shipment (Gold on Obsidian background / Black text on Gold)
                        Button(
                            onClick = { onNavigate("ActiveTracking") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Gold,
                                contentColor = Obsidian
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = Obsidian, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Track Live Shipment", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        // 2. Secondary Row Action: Share Details & Back to Home
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Share Button
                            Button(
                                onClick = {
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_TEXT, "Track my Engraced Dispatch shipment! ID: ${latestParcel?.id ?: "ENG-824-LGS"}")
                                    }
                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Shipment Details"))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Charcoal else GoldenWhite),
                                border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share details", tint = Gold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gold)
                                }
                            }

                            // Back Home Button
                            OutlinedButton(
                                onClick = { onNavigate("Dashboard") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.5.dp, Color(0xFF333333)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold)
                            ) {
                                Text("Back to Home", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ServiceSelectionScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val draft by viewModel.parcelDraft.collectAsState()
    var selectedService by remember { mutableStateOf("Express") }

    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val scrollState = rememberScrollState()

    val accentBorderColor = if (isLight) Obsidian else Gold
    val accentIconColor = if (isLight) Obsidian else Gold
    val accentTextColor = if (isLight) Obsidian else Gold

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
                title = "Select Service",
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
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                        .padding(bottom = 120.dp)
                ) {
                Text(
                    text = "Choose Your Service",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentTextColor
                )
                Text(
                    text = "Select the option that best fits your schedule and parcel type.",
                    fontSize = 14.sp,
                    color = TextGray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                val services = listOf(
                    ServiceOption(
                        id = "Express",
                        name = "Express Delivery",
                        desc = "Immediate dispatch, fastest delivery in 30-45 mins.",
                        price = "",
                        badge = "Fastest",
                        badgeColor = Gold,
                        icon = Icons.Filled.Bolt
                    ),
                    ServiceOption(
                        id = "Economy",
                        name = "Economy Delivery",
                        desc = "High efficiency, standard delivery in 2-3 hours.",
                        price = "",
                        badge = "Best Price",
                        badgeColor = Color(0xFF4CAF50),
                        icon = Icons.Filled.LocalShipping
                    ),
                    ServiceOption(
                        id = "Batch",
                        name = "Batch Delivery",
                        desc = "Optimized multi-stop bulk package delivery.",
                        price = "",
                        badge = "Eco Saver",
                        badgeColor = Color(0xFF00BCD4),
                        icon = Icons.Filled.Layers
                    ),
                    ServiceOption(
                        id = "Multi",
                        name = "Multi-Stop Delivery",
                        desc = "Deliver to up to 5 destinations in a single run.",
                        price = "",
                        badge = "Business",
                        badgeColor = Gold,
                        icon = Icons.Filled.Share
                    )
                )

                services.forEach { service ->
                    val isSelected = selectedService == service.id
                    val borderStroke = if (isSelected) {
                        BorderStroke(2.dp, accentBorderColor)
                    } else {
                        if (isLight) BorderStroke(1.dp, BorderLight) else BorderStroke(1.dp, Color(0xFF333333))
                    }
                    val containerColor = if (isLight) {
                        if (isSelected) Gold.copy(alpha = 0.08f) else Color.White
                    } else {
                        if (isSelected) Charcoal else Obsidian
                    }

                    Surface(
                        onClick = { selectedService = service.id },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = containerColor,
                        border = borderStroke,
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (isSelected) Gold.copy(alpha = 0.15f) else (if (isLight) GoldenWhiteLight else Obsidian)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = service.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) accentIconColor else TextGray,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = service.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AppTextColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = service.badgeColor.copy(alpha = 0.15f),
                                    ) {
                                        Text(
                                            text = service.badge,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isLight) Obsidian else service.badgeColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = service.desc,
                                    fontSize = 12.sp,
                                    color = TextGray,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                if (service.price.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = service.price,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = accentTextColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Charcoal,
            tonalElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.finalizeDraftPrice(selectedService)
                        when (selectedService) {
                            "Express" -> onNavigate("ExpressBooking")
                            "Economy" -> onNavigate("EconomyBooking")
                            "Batch" -> onNavigate("BatchBooking")
                            "Multi" -> onNavigate("MultiBooking")
                            else -> onNavigate("SendParcelDetails")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Obsidian),
                    border = BorderStroke(1.2.dp, Gold)
                ) {
                    Text(
                        text = "Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Gold
                    )
                }
            }
        }
    }
}

private data class ServiceOption(
    val id: String,
    val name: String,
    val desc: String,
    val price: String,
    val badge: String,
    val badgeColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
