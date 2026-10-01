package com.esdispatch.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.esdispatch.data.Parcel
import com.esdispatch.data.ParcelStatus
import com.esdispatch.data.SupportChatMessage
import com.esdispatch.ui.components.RoundedSheet
import com.esdispatch.ui.components.ScreenHeader
import com.esdispatch.ui.theme.*
import com.esdispatch.util.FormatUtils
import com.esdispatch.viewmodel.DeliveryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * Transforms Markdown bold tokens (**bold text**) into real Jetpack Compose AnnotatedString Bold styling.
 * Eliminates ugly asterisks in ESAI responses.
 */
internal fun parseMarkdownBold(text: String): AnnotatedString {
    if (!text.contains("**")) {
        return AnnotatedString(text)
    }
    return buildAnnotatedString {
        val parts = text.split("**")
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(part)
                }
            } else {
                append(part)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportChatScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val isDark = isDarkTheme
    val pageBg = if (isDark) BackgroundDark else GoldenWhite
    val cardBg = if (isDark) Obsidian else GoldenWhiteLight
    val primaryText = if (isDark) Color.White else Obsidian
    val secondaryText = if (isDark) TextGray else Color(0xFF666666)

    val currentUserId by viewModel.firebaseUserId.collectAsState()
    val currentUserName by viewModel.userName.collectAsState()
    val parcels by viewModel.parcels.collectAsState()
    val activeParcels = remember(parcels) {
        parcels.filter { it.status in listOf(ParcelStatus.PENDING, ParcelStatus.ASSIGNED, ParcelStatus.ARRIVED_PICKUP, ParcelStatus.PICKED_UP, ParcelStatus.TRANSIT) }
    }

    // Ticket State
    val ticketNumber = remember(currentUserId) {
        val hash = (currentUserId ?: "GUEST").takeLast(6).uppercase()
        "TK-$hash"
    }
    var urgency by remember { mutableStateOf("Standard") } // "Standard" or "Urgent"
    var selectedParcel by remember { mutableStateOf<Parcel?>(activeParcels.firstOrNull()) }

    // Human Representative Handover State
    var isRepresentativeJoined by remember { mutableStateOf(false) }
    var isSwitchingToHuman by remember { mutableStateOf(false) }
    var representativeName by remember { mutableStateOf("Sarah M.") }
    var representativeRole by remember { mutableStateOf("Senior Support Specialist") }

    // Chat Message State
    val chatMessages by viewModel.supportChatMessages.collectAsState()
    var messageText by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<SupportChatMessage?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSending by remember { mutableStateOf(false) }
    var isAiTyping by remember { mutableStateOf(false) }
    var typingAgentText by remember { mutableStateOf("ESAI is typing...") }
    var showVoiceCallHUD by remember { mutableStateOf(false) }
    var isCallActive by remember { mutableStateOf(false) }
    var callActiveSeconds by remember { mutableStateOf(0) }
    var isRecordingVoiceNote by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0) }
    var showParcelSelectorSheet by remember { mutableStateOf(false) }

    LaunchedEffect(isCallActive) {
        if (isCallActive) {
            while (isCallActive) {
                delay(1000L)
                callActiveSeconds++
            }
        } else {
            callActiveSeconds = 0
        }
    }

    val latestActionMsgId = remember(chatMessages) {
        chatMessages.lastOrNull {
            it.isAi && (it.messageText.contains("motorcycle", ignoreCase = true) ||
                    it.messageText.contains("courier", ignoreCase = true) ||
                    it.messageText.contains("dispatch", ignoreCase = true) ||
                    it.messageText.contains("book", ignoreCase = true) ||
                    it.messageText.contains("fare", ignoreCase = true))
        }?.id
    }

    LaunchedEffect(isRecordingVoiceNote) {
        if (isRecordingVoiceNote) {
            recordingDuration = 0
            while (isRecordingVoiceNote) {
                delay(1000L)
                recordingDuration++
            }
        }
    }

    val listState = rememberLazyListState()

    // SLA Remaining Timer (Standard: 2 hrs; Urgent: 15 mins)
    var slaSecondsRemaining by remember(urgency) {
        mutableStateOf(if (urgency == "Urgent") 15 * 60 else 2 * 60 * 60)
    }

    LaunchedEffect(urgency) {
        while (slaSecondsRemaining > 0) {
            delay(1000L)
            slaSecondsRemaining--
        }
    }

    // Listen to support chat messages on mount
    LaunchedEffect(ticketNumber) {
        viewModel.startListeningToSupportChat(ticketNumber)
    }

    // Auto-scroll to bottom on new messages
    LaunchedEffect(chatMessages.size, isAiTyping, isSwitchingToHuman) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // Initial greeting if chat is empty
    LaunchedEffect(chatMessages) {
        if (chatMessages.isEmpty()) {
            delay(400)
            val welcomeText = "Hello ${currentUserName.ifBlank { "there" }}! I am ESAI, your intelligent logistics concierge. How can I assist you with your deliveries, bookings, or inquiries today?"
            viewModel.sendSupportChatMessage(
                messageText = welcomeText,
                ticketId = ticketNumber,
                deliveryId = selectedParcel?.id ?: "",
                isAi = true,
                urgency = urgency
            )
        }
    }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    selectedImageBitmap = BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Could not load selected image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to trigger AI or Representative Response
    fun triggerAssistantResponse(userPrompt: String) {
        val lower = userPrompt.lowercase()
        val isHumanRequest = lower.contains("human") || lower.contains("agent") || lower.contains("representative") ||
                lower.contains("customer care") || lower.contains("speak to someone") || lower.contains("talk to someone") ||
                lower.contains("call me") || lower.contains("manager")

        coroutineScope.launch {
            if (isHumanRequest && !isRepresentativeJoined) {
                // Switching Flow
                isAiTyping = true
                typingAgentText = "ESAI is typing..."
                delay(1200)

                val handoverMsg = "You have a customer care representative available now. I'm switching you over to dispatch operations right away..."
                viewModel.sendSupportChatMessage(
                    messageText = handoverMsg,
                    ticketId = ticketNumber,
                    deliveryId = selectedParcel?.id ?: "",
                    isAi = true,
                    urgency = urgency
                )
                isAiTyping = false

                // Transitioning indicator
                isSwitchingToHuman = true
                delay(2200)
                isSwitchingToHuman = false
                isRepresentativeJoined = true
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                // Human Representative Joins
                isAiTyping = true
                typingAgentText = "$representativeName is typing..."
                delay(1500)
                isAiTyping = false

                val repIntro = "Hello ${currentUserName.ifBlank { "there" }}! My name is $representativeName from ESDispatch Central Operations. I've taken over your ticket (#$ticketNumber). How can I assist you directly?"
                viewModel.sendSupportChatMessage(
                    messageText = repIntro,
                    ticketId = ticketNumber,
                    deliveryId = selectedParcel?.id ?: "",
                    replyToText = "",
                    replyToSender = "",
                    imageUrl = "",
                    avatarUrl = "",
                    isAi = false,
                    urgency = urgency
                )
            } else if (!isRepresentativeJoined) {
                // ESAI Smart Logistics Logic
                isAiTyping = true
                typingAgentText = "ESAI is typing..."
                delay(1600)

                val parcel = selectedParcel
                val activeParcelsList = activeParcels
                val aiReply = when {
                    lower.contains("inquir") || lower.contains("shipment") || lower.contains("selected") -> {
                        if (parcel != null) {
                            val displayId = FormatUtils.formatDisplayTrackingId(parcel.id)
                            when (parcel.status) {
                                ParcelStatus.DELIVERED -> {
                                    "I have linked your completed shipment **$displayId** (${parcel.itemName.ifBlank { "Delivery Package" }}). " +
                                            "Our system records that this delivery was completed to **${parcel.deliveryAddress.ifBlank { "recipient address" }}** with verified recipient PIN handover. " +
                                            "Would you like an electronic receipt, want to tip the assigned courier, or need to report any discrepancy?"
                                }
                                ParcelStatus.TRANSIT, ParcelStatus.OUT_FOR_DELIVERY -> {
                                    val courierInfo = if (parcel.courierName.isNotBlank()) "Courier **${parcel.courierName}** (${parcel.riderBikeNumber.ifBlank { "Dispatch Motorcycle" }})" else "An assigned fleet courier"
                                    "Your shipment **$displayId** is currently in **TRANSIT**. $courierInfo is en route along Benin City corridors towards **${parcel.deliveryAddress}**. " +
                                            "Telemetry confirms normal pace. Tap **Track on Live Radar** below for real-time map GPS tracking."
                                }
                                ParcelStatus.ARRIVED_PICKUP -> {
                                    "Courier **${parcel.courierName.ifBlank { "Fleet Rider" }}** has arrived at the pickup location (**${parcel.pickupAddress}**) to collect **$displayId**."
                                }
                                ParcelStatus.PENDING -> {
                                    "Shipment **$displayId** (${parcel.itemName}) is registered and queued in our Central Dispatch Pool. Couriers within your pickup radius are receiving the mission broadcast right now."
                                }
                                ParcelStatus.CANCELLED -> {
                                    "Shipment **$displayId** was cancelled. Any associated delivery charges or escrow holds have been refunded back to your wallet balance."
                                }
                                else -> {
                                    "Shipment **$displayId** (${parcel.itemName}) is currently in status **${parcel.status.name}**. Let me know how I can assist you with this order."
                                }
                            }
                        } else {
                            "You can link any active or completed delivery to this ticket by tapping the parcel selector bar above."
                        }
                    }
                    lower.contains("where") || lower.contains("track") || lower.contains("status") || lower.contains("rider") -> {
                        if (parcel != null) {
                            val displayId = FormatUtils.formatDisplayTrackingId(parcel.id)
                            "Your shipment **$displayId** (${parcel.itemName.ifBlank { "Package" }}) is currently **${parcel.status.name}**." +
                                    (if (parcel.courierName.isNotBlank()) "\n• Assigned Courier: **${parcel.courierName}**" else "\n• Searching available fleet couriers.") +
                                    (if (parcel.deliveryAddress.isNotBlank()) "\n• Destination: **${parcel.deliveryAddress}**" else "") +
                                    "\nOur telemetry indicates safe transit across Benin City corridors. Tap 'Live Radar' on your dashboard for live GPS tracking."
                        } else if (activeParcelsList.isNotEmpty()) {
                            val p = activeParcelsList.first()
                            val displayId = FormatUtils.formatDisplayTrackingId(p.id)
                            "You have an active shipment **$displayId** (${p.itemName}). Status: **${p.status.name}**. Assigned courier: **${p.courierName.ifBlank { "Searching rider pool" }}**."
                        } else {
                            "You currently have no active deliveries in transit. To book a new courier, tap 'Send Package' on your dashboard, or use the dispatch draft below."
                        }
                    }
                    lower.contains("book") || lower.contains("send") || lower.contains("dispatch") || lower.contains("bike") -> {
                        "I have prepared an express dispatch draft for you below. Our motorcycle couriers are on standby across Ring Road, GRA, Uselu, and Airport Road corridors. Tap **Confirm & Book** to proceed or **Change Route** to adjust landmarks."
                    }
                    lower.contains("delay") || lower.contains("traffic") || lower.contains("late") -> {
                        "Benin City operational corridors (Ring Road, Uselu, and Sapele Road) occasionally experience peak congestion. Our fleet routing dynamically reroutes riders to avoid bottlenecks. Your delivery is prioritized!"
                    }
                    lower.contains("address") || lower.contains("change") || lower.contains("redirect") -> {
                        "To update the delivery destination for an active parcel, please provide the new landmark or street name here. I will notify the assigned courier immediately."
                    }
                    lower.contains("wallet") || lower.contains("refund") || lower.contains("payment") || lower.contains("balance") -> {
                        "All payments and escrow deposits in ESDispatch are securely protected. If a dispatch was cancelled, your refund is automatically returned to your wallet balance within seconds."
                    }
                    lower.contains("market") || lower.contains("store") || lower.contains("product") -> {
                        "Our Marketplace connects verified Benin City vendors. Orders placed through Marketplace stores include automated dispatch delivery directly to your doorstep."
                    }
                    else -> {
                        "Thank you for reaching out to ESDispatch Support. I have logged this inquiry under Ticket **#$ticketNumber** ($urgency priority). " +
                                (if (parcel != null) "Currently linked to shipment **${FormatUtils.formatDisplayTrackingId(parcel.id)}**. " else "") +
                                "How can I assist you with your deliveries, bookings, or account today?"
                    }
                }

                viewModel.sendSupportChatMessage(
                    messageText = aiReply,
                    ticketId = ticketNumber,
                    deliveryId = selectedParcel?.id ?: "",
                    isAi = true,
                    urgency = urgency
                )
                isAiTyping = false
            } else {
                // Representative response
                isAiTyping = true
                typingAgentText = "$representativeName is typing..."
                delay(2000)
                isAiTyping = false

                val repReply = "I have updated your ticket notes regarding: \"${userPrompt.take(40)}\". Our dispatch team is monitoring this closely."
                viewModel.sendSupportChatMessage(
                    messageText = repReply,
                    ticketId = ticketNumber,
                    deliveryId = selectedParcel?.id ?: "",
                    isAi = false,
                    urgency = urgency
                )
            }
        }
    }

    // Send Message Handler
    fun handleSendMessage(textToSend: String = messageText) {
        val cleanText = textToSend.trim()
        val hasImage = selectedImageBitmap != null

        if (cleanText.isBlank() && !hasImage) return
        if (isSending) return

        isSending = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

        val quotedText = replyingTo?.messageText ?: ""
        val quotedSender = replyingTo?.senderName ?: ""
        val bitmapToUpload = selectedImageBitmap

        // Clear local inputs
        messageText = ""
        replyingTo = null
        selectedImageUri = null
        selectedImageBitmap = null

        if (hasImage && bitmapToUpload != null) {
            val stream = ByteArrayOutputStream()
            bitmapToUpload.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            val bytes = stream.toByteArray()

            viewModel.uploadSupportChatImage(ticketNumber, bytes) { success, downloadUrl ->
                isSending = false
                if (success && downloadUrl != null) {
                    viewModel.sendSupportChatMessage(
                        messageText = cleanText,
                        ticketId = ticketNumber,
                        deliveryId = selectedParcel?.id ?: "",
                        replyToText = quotedText,
                        replyToSender = quotedSender,
                        imageUrl = downloadUrl,
                        isAi = false,
                        urgency = urgency
                    )
                    triggerAssistantResponse(cleanText.ifBlank { "Attached image" })
                } else {
                    Toast.makeText(context, "Failed to upload image. Sending text only.", Toast.LENGTH_SHORT).show()
                    viewModel.sendSupportChatMessage(
                        messageText = cleanText,
                        ticketId = ticketNumber,
                        deliveryId = selectedParcel?.id ?: "",
                        replyToText = quotedText,
                        replyToSender = quotedSender,
                        isAi = false,
                        urgency = urgency
                    )
                    triggerAssistantResponse(cleanText)
                }
            }
        } else {
            isSending = false
            viewModel.sendSupportChatMessage(
                messageText = cleanText,
                ticketId = ticketNumber,
                deliveryId = selectedParcel?.id ?: "",
                replyToText = quotedText,
                replyToSender = quotedSender,
                isAi = false,
                urgency = urgency
            )
            triggerAssistantResponse(cleanText)
        }
    }

    // MAIN CONTAINER: Standard HeaderBgColor with RoundedSheet
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HeaderBgColor)
    ) {
        // 1. Unified Standard ScreenHeader
        ScreenHeader(
            title = "Customer Support",
            onBack = { onNavigate("Dashboard") },
            rightContent = {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (urgency == "Urgent") Color(0xFFFF5252).copy(alpha = 0.2f) else Obsidian.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (urgency == "Urgent") Color(0xFFFF5252) else Obsidian.copy(alpha = 0.3f)),
                    modifier = Modifier.clickable {
                        urgency = if (urgency == "Standard") "Urgent" else "Standard"
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        Toast.makeText(context, "Ticket priority updated to $urgency", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (urgency == "Urgent") {
                            Icon(Icons.Filled.Bolt, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = urgency.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (urgency == "Urgent") Color(0xFFFF5252) else Obsidian
                        )
                    }
                }
            }
        )

        // 2. Rounded Luxury Sheet
        RoundedSheet(
            modifier = Modifier.weight(1f),
            containerColor = pageBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                // Top Meta Card: Ticket Info, SLA Countdown & Linked Parcel
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.25f) else Slate.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isRepresentativeJoined) Color(0xFF4CAF50) else Gold,
                                    modifier = Modifier.size(8.dp)
                                ) {}
                                Text(
                                    text = ticketNumber,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = primaryText
                                )
                                Text(
                                    text = "•",
                                    fontSize = 12.sp,
                                    color = secondaryText
                                )
                                Text(
                                    text = if (isRepresentativeJoined) "Representative Live" else "ESAI Active",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isRepresentativeJoined) Color(0xFF4CAF50) else Gold
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Live Concierge Voice Call Button
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Gold,
                                    modifier = Modifier.clickable {
                                        isCallActive = true
                                        showVoiceCallHUD = true
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Filled.Call, contentDescription = "Voice Call", tint = Obsidian, modifier = Modifier.size(13.dp))
                                        Text("Voice Call", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Obsidian)
                                    }
                                }

                                // SLA Countdown
                                val minutes = slaSecondsRemaining / 60
                                val seconds = slaSecondsRemaining % 60
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isDark) LuxuryBlack else GoldenWhite,
                                    border = BorderStroke(0.8.dp, if (urgency == "Urgent") Color(0xFFFF5252) else Gold.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "SLA ${String.format("%02d:%02d", minutes, seconds)}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (urgency == "Urgent") Color(0xFFFF5252) else primaryText,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        // Linked Shipment Selector Row
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) Charcoal.copy(alpha = 0.7f) else Slate.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, if (selectedParcel != null) Gold.copy(alpha = 0.5f) else (if (isDark) Slate.copy(alpha = 0.4f) else Slate)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showParcelSelectorSheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.LocalShipping,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(16.dp)
                                )
                                if (selectedParcel != null) {
                                    val p = selectedParcel!!
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Linked: ${FormatUtils.formatDisplayTrackingId(p.id)} • ${p.itemName.ifBlank { "Delivery Package" }}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Status: ${p.status.name} • Tap to switch parcel",
                                            fontSize = 9.sp,
                                            color = secondaryText
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Gold
                                    ) {
                                        Text(
                                            text = "Switch",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Obsidian,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = if (parcels.isNotEmpty()) "Select shipment to inquire (${parcels.size} available)..." else "No parcels found • Inquire generally",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Gold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        Icons.Filled.ChevronRight,
                                        contentDescription = null,
                                        tint = Gold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Quick 1-tap Active Parcel Chips if active shipments exist
                        if (activeParcels.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                item {
                                    Text("Active:", fontSize = 10.sp, color = secondaryText, fontWeight = FontWeight.SemiBold)
                                }
                                items(activeParcels) { p ->
                                    val isSelected = selectedParcel?.id == p.id
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) Gold else (if (isDark) Charcoal else Slate.copy(alpha = 0.35f)),
                                        border = BorderStroke(0.8.dp, if (isSelected) Gold else Gold.copy(alpha = 0.3f)),
                                        modifier = Modifier.clickable {
                                            selectedParcel = if (isSelected) null else p
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            if (!isSelected) {
                                                handleSendMessage("Inquiring about active shipment ${FormatUtils.formatDisplayTrackingId(p.id)} (${p.itemName.ifBlank { "Package" }})")
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = FormatUtils.formatDisplayTrackingId(p.id),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Obsidian else primaryText,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Smooth Transition Banner (When switching from ESAI to Human Agent)
                AnimatedVisibility(
                    visible = isSwitchingToHuman,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Gold.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Gold)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(color = Gold, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Column {
                                Text("Connecting with Support Specialist...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryText)
                                Text("Sarah M. from Central Dispatch is joining the chat", fontSize = 10.sp, color = secondaryText)
                            }
                        }
                    }
                }

                // Minimized Voice Call Floating Banner
                AnimatedVisibility(
                    visible = isCallActive && !showVoiceCallHUD,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showVoiceCallHUD = true
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Charcoal else GoldenWhiteSurface,
                        border = BorderStroke(1.dp, Gold)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(shape = CircleShape, color = Color(0xFF4CAF50), modifier = Modifier.size(8.dp)) {}
                                Text(
                                    text = "Voice Call in Progress (${String.format("%02d:%02d", callActiveSeconds / 60, callActiveSeconds % 60)})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryText
                                )
                                Text("• Tap to view", fontSize = 10.sp, color = Gold, fontWeight = FontWeight.SemiBold)
                            }
                            IconButton(
                                onClick = {
                                    val duration = callActiveSeconds
                                    isCallActive = false
                                    val mins = duration / 60
                                    val secs = duration % 60
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.sendSupportChatMessage(
                                        messageText = "Voice Concierge Call Completed (${String.format("%02d:%02d", mins, secs)}). Inquiry resolved.",
                                        ticketId = ticketNumber,
                                        deliveryId = selectedParcel?.id ?: "",
                                        isAi = true,
                                        urgency = urgency
                                    )
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE53935))
                            ) {
                                Icon(Icons.Filled.CallEnd, contentDescription = "End Call", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Active Representative Pill
                if (isRepresentativeJoined) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Charcoal else GoldenWhiteLight,
                        border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Gold),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("SM", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Obsidian)
                                }
                                Column {
                                    Text(representativeName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryText)
                                    Text(representativeRole, fontSize = 9.sp, color = Color(0xFF4CAF50))
                                }
                            }
                            Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Chat Messages Feed
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        // Privacy & Encryption Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = secondaryText, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "End-to-end encrypted dispatch channel",
                                fontSize = 10.sp,
                                color = secondaryText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    items(chatMessages, key = { it.id }) { msg ->
                        val isUser = msg.senderRole == "customer" || msg.senderId == currentUserId
                        val isLatestAction = (msg.id == latestActionMsgId)
                        SupportMessageBubble(
                            message = msg,
                            isUser = isUser,
                            isDark = isDark,
                            primaryText = primaryText,
                            secondaryText = secondaryText,
                            isLatestAction = isLatestAction,
                            onSwipeToReply = {
                                replyingTo = msg
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            onNavigate = onNavigate,
                            onQuickAction = { prompt ->
                                handleSendMessage(prompt)
                            }
                        )
                    }

                    // Live Typing Indicator
                    if (isAiTyping) {
                        item {
                            TypingIndicatorBubble(
                                text = typingAgentText,
                                isDark = isDark,
                                primaryText = primaryText
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                // Dynamic Context-Aware Suggestions
                val lastMsg = chatMessages.lastOrNull()
                val lastMsgText = lastMsg?.messageText?.lowercase().orEmpty()
                val dynamicSuggestions = remember(lastMsg, selectedParcel, activeParcels) {
                    val list = mutableListOf<Pair<String, androidx.compose.ui.graphics.vector.ImageVector>>()
                    val curParcel = selectedParcel
                    when {
                        lastMsgText.contains("book") || lastMsgText.contains("send") || lastMsgText.contains("dispatch") -> {
                            list.add("Book motorcycle dispatch" to Icons.Filled.DirectionsBike)
                            list.add("Estimate fare to destination" to Icons.Filled.Payments)
                            list.add("Pick up from current location" to Icons.Filled.MyLocation)
                        }
                        curParcel != null && (curParcel.status == ParcelStatus.TRANSIT || curParcel.status == ParcelStatus.OUT_FOR_DELIVERY) -> {
                            list.add("Track courier live location" to Icons.Filled.Radar)
                            list.add("Call assigned fleet rider" to Icons.Filled.Phone)
                            list.add("Estimated arrival time" to Icons.Filled.Schedule)
                        }
                        curParcel != null && (curParcel.status == ParcelStatus.ARRIVED || curParcel.status == ParcelStatus.ARRIVED_PICKUP) -> {
                            list.add("Confirm parcel handover" to Icons.Filled.CheckCircle)
                            list.add("View verification code" to Icons.Filled.Security)
                        }
                        lastMsgText.contains("delay") || lastMsgText.contains("traffic") -> {
                            list.add("Request dispatch priority" to Icons.Filled.Bolt)
                            list.add("Speak to operations supervisor" to Icons.Filled.SupportAgent)
                        }
                        lastMsgText.contains("wallet") || lastMsgText.contains("refund") -> {
                            list.add("Check wallet balance" to Icons.Filled.AccountBalanceWallet)
                            list.add("View transaction history" to Icons.Filled.ReceiptLong)
                        }
                        else -> {
                            list.add("Book a delivery in Benin City" to Icons.Filled.LocalShipping)
                            if (activeParcels.isNotEmpty()) {
                                list.add("Where is my active shipment?" to Icons.Filled.LocationOn)
                            }
                            list.add("Speak to human specialist" to Icons.Filled.Headphones)
                            list.add("Check delivery rates" to Icons.Filled.Payments)
                        }
                    }
                    list
                }

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dynamicSuggestions) { (prompt, icon) ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isDark) Charcoal else Slate.copy(alpha = 0.35f),
                            border = BorderStroke(0.8.dp, if (isDark) Gold.copy(alpha = 0.3f) else Slate),
                            modifier = Modifier.clickable {
                                handleSendMessage(prompt)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(icon, contentDescription = null, tint = Gold, modifier = Modifier.size(13.dp))
                                Text(
                                    text = prompt,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryText
                                )
                            }
                        }
                    }
                }

                // Quoted Reply Preview Bar
                AnimatedVisibility(
                    visible = replyingTo != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    replyingTo?.let { reply ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) Charcoal else GoldenWhiteLight,
                            border = BorderStroke(1.dp, Gold)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(24.dp),
                                        color = Gold
                                    ) {}
                                    Column {
                                        Text(
                                            text = "Replying to ${reply.senderName.ifBlank { "Support" }}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Gold
                                        )
                                        Text(
                                            text = reply.messageText.ifBlank { "[Attachment]" },
                                            fontSize = 10.sp,
                                            color = secondaryText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { replyingTo = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Cancel Reply", tint = secondaryText, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Selected Image Thumbnail Preview
                if (selectedImageBitmap != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Charcoal else GoldenWhiteLight,
                        border = BorderStroke(1.dp, Gold)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Image(
                                    bitmap = selectedImageBitmap!!.asImageBitmap(),
                                    contentDescription = "Selected Photo Preview",
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Text("Ready to upload with message", fontSize = 11.sp, color = primaryText)
                            }
                            IconButton(
                                onClick = {
                                    selectedImageBitmap = null
                                    selectedImageUri = null
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove Photo", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Bottom Input Row: Adapts between standard text/attachment mode and voice note recording mode
                if (isRecordingVoiceNote) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Discard Voice Note
                        IconButton(
                            onClick = {
                                isRecordingVoiceNote = false
                                recordingDuration = 0
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE53935).copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Discard Recording", tint = Color(0xFFE53935), modifier = Modifier.size(20.dp))
                        }

                        // Waveform & Recording Timer
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = if (isDark) Obsidian else Color.White,
                            border = BorderStroke(1.dp, Gold)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFE53935),
                                        modifier = Modifier.size(8.dp)
                                    ) {}
                                    Text(
                                        text = String.format("0:%02d", recordingDuration),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryText
                                    )
                                }

                                WaveformBars()
                            }
                        }

                        // Send Voice Note Button
                        Surface(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .clickable {
                                    val duration = recordingDuration
                                    isRecordingVoiceNote = false
                                    recordingDuration = 0
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    handleSendMessage("Voice Note (0:${String.format("%02d", duration)}) - Dispatch motorcycle to Airport Road, Benin City")
                                },
                            shape = CircleShape,
                            color = Gold
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Send, contentDescription = "Send Voice Note", tint = Obsidian, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Attachment Icon Button
                        IconButton(
                            onClick = {
                                imagePickerLauncher.launch("image/*")
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Charcoal else Slate.copy(alpha = 0.35f))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AddPhotoAlternate,
                                contentDescription = "Attach Photo",
                                tint = if (selectedImageBitmap != null) Gold else primaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Voice Note Recording Toggle
                        IconButton(
                            onClick = {
                                isRecordingVoiceNote = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Charcoal else Slate.copy(alpha = 0.35f))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Mic,
                                contentDescription = "Record Voice Note",
                                tint = primaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Text Input Field
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("Ask support or report an issue...", fontSize = 13.sp, color = secondaryText) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp, max = 110.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = if (isDark) Slate.copy(alpha = 0.4f) else Slate,
                                focusedContainerColor = if (isDark) Obsidian else Color.White,
                                unfocusedContainerColor = if (isDark) Obsidian else Color.White,
                                focusedTextColor = primaryText,
                                unfocusedTextColor = primaryText
                            ),
                            singleLine = false,
                            maxLines = 3
                        )

                        // Send / Live Voice Call Button (Gold circular with Obsidian icon)
                        val hasInput = messageText.isNotBlank() || selectedImageBitmap != null
                        Surface(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .clickable(enabled = !isSending) {
                                    if (hasInput) {
                                        handleSendMessage()
                                    } else {
                                        isCallActive = true
                                        showVoiceCallHUD = true
                                    }
                                },
                            shape = CircleShape,
                            color = Gold
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isSending) {
                                    CircularProgressIndicator(color = Obsidian, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else if (hasInput) {
                                    Icon(
                                        imageVector = Icons.Filled.Send,
                                        contentDescription = "Send Message",
                                        tint = Obsidian,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Call,
                                        contentDescription = "Live Voice Concierge Call",
                                        tint = Obsidian,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Full-Screen Live Voice Call HUD Modal Overlay
        AnimatedVisibility(
            visible = showVoiceCallHUD,
            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(350)) { it / 2 },
            exit = fadeOut(animationSpec = tween(250)) + slideOutVertically(animationSpec = tween(300)) { it / 2 }
        ) {
            VoiceCallHUDModal(
                isDark = isDark,
                primaryText = primaryText,
                secondaryText = secondaryText,
                isRepresentativeJoined = isRepresentativeJoined,
                representativeName = representativeName,
                callSeconds = callActiveSeconds,
                onDismiss = { showVoiceCallHUD = false },
                onEndCall = { durationSec, _ ->
                    val duration = if (durationSec > 0) durationSec else callActiveSeconds
                    isCallActive = false
                    showVoiceCallHUD = false
                    val mins = duration / 60
                    val secs = duration % 60
                    val summaryText = "Voice Concierge Call Completed (${String.format("%02d:%02d", mins, secs)}). Inquiry resolved."
                    viewModel.sendSupportChatMessage(
                        messageText = summaryText,
                        ticketId = ticketNumber,
                        deliveryId = selectedParcel?.id ?: "",
                        isAi = true,
                        urgency = urgency
                    )
                },
                onNavigateToBooking = {
                    isCallActive = false
                    showVoiceCallHUD = false
                    onNavigate("SendParcel")
                },
                onNavigateToTracking = {
                    isCallActive = false
                    showVoiceCallHUD = false
                    onNavigate("ActiveTracking")
                }
            )
        }

        // 4. Select Parcel Bottom Sheet (All shipments - active, delivered, past)
        if (showParcelSelectorSheet) {
            SelectParcelBottomSheet(
                parcels = parcels,
                selectedParcelId = selectedParcel?.id,
                isDark = isDark,
                primaryText = primaryText,
                secondaryText = secondaryText,
                onDismiss = { showParcelSelectorSheet = false },
                onSelect = { parcel ->
                    selectedParcel = parcel
                    showParcelSelectorSheet = false
                    val displayId = FormatUtils.formatDisplayTrackingId(parcel.id)
                    handleSendMessage("Inquiring about shipment #$displayId (${parcel.itemName.ifBlank { "Delivery Package" }})")
                }
            )
        }
    }
}

@Composable
private fun SupportMessageBubble(
    message: SupportChatMessage,
    isUser: Boolean,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
    isLatestAction: Boolean = true,
    onSwipeToReply: () -> Unit,
    onNavigate: (String) -> Unit = {},
    onQuickAction: (String) -> Unit = {}
) {
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))

    val bubbleBg = if (isUser) {
        Gold
    } else {
        if (isDark) Obsidian else GoldenWhiteLight
    }

    val bubbleTextColor = if (isUser) {
        Obsidian // MANDATORY CONTRAST LOCK: Never white on gold!
    } else {
        primaryText
    }

    val timeString = remember(message.timestamp) {
        try {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            sdf.format(Date(message.timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
            .draggable(
                state = rememberDraggableState { delta ->
                    val newX = offsetX + delta
                    if (newX in -120f..120f) {
                        offsetX = newX
                    }
                },
                orientation = Orientation.Horizontal,
                onDragStopped = {
                    if (Math.abs(offsetX) > 50f) {
                        onSwipeToReply()
                    }
                    offsetX = 0f
                }
            ),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Sender Info Header
            if (!isUser) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                ) {
                    Text(
                        text = if (message.isAi) "ESAI Virtual Assistant" else message.senderName.ifBlank { "Support Agent" },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gold
                    )
                    if (message.isAi) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Gold.copy(alpha = 0.2f)
                        ) {
                            Text("AI", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Gold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                }
            }

            // Quoted Reply Card inside Bubble
            if (message.replyToText.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isUser) Obsidian.copy(alpha = 0.15f) else (if (isDark) Charcoal else Slate.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .width(2.5.dp)
                                .height(20.dp),
                            color = if (isUser) Obsidian else Gold
                        ) {}
                        Column {
                            Text(
                                text = message.replyToSender.ifBlank { "Reply" },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUser) Obsidian else Gold
                            )
                            Text(
                                text = message.replyToText,
                                fontSize = 9.sp,
                                color = if (isUser) Obsidian.copy(alpha = 0.8f) else secondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Main Message Container
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 2.dp,
                    bottomEnd = if (isUser) 2.dp else 16.dp
                ),
                color = bubbleBg,
                border = if (!isUser) BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.2f) else Slate.copy(alpha = 0.5f)) else null
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    // Attached Image if present
                    if (message.imageUrl.isNotBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(message.imageUrl),
                            contentDescription = "Attached Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .padding(bottom = 6.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    if (message.messageText.isNotBlank()) {
                        Text(
                            text = parseMarkdownBold(message.messageText),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = bubbleTextColor,
                            lineHeight = 18.sp
                        )
                    }

                    // Interactive Action Cards for AI Assistant messages
                    if (!isUser && message.isAi) {
                        val msgLower = message.messageText.lowercase()
                        when {
                            msgLower.contains("motorcycle") || msgLower.contains("courier") || msgLower.contains("dispatch") || msgLower.contains("book") || msgLower.contains("fare") -> {
                                ChatBookingActionCard(
                                    isDark = isDark,
                                    isActionActive = isLatestAction,
                                    onConfirm = { onNavigate("SendParcel") },
                                    onSelectLocation = { onQuickAction("Select destination for courier dispatch") }
                                )
                            }
                            message.deliveryId.isNotBlank() || msgLower.contains("shipment") || msgLower.contains("transit") || msgLower.contains("radar") || msgLower.contains("track") -> {
                                ChatTrackingActionCard(
                                    deliveryId = message.deliveryId.ifBlank { "ESD-TRK-784" },
                                    isDark = isDark,
                                    onOpenLiveMap = { onNavigate("ActiveTracking") }
                                )
                            }
                            msgLower.contains("wallet") || msgLower.contains("refund") || msgLower.contains("balance") || msgLower.contains("escrow") -> {
                                ChatWalletActionCard(
                                    isDark = isDark,
                                    onOpenWallet = { onNavigate("Wallet") }
                                )
                            }
                            msgLower.contains("address") || msgLower.contains("location") || msgLower.contains("landmark") -> {
                                ChatLocationActionCard(
                                    isDark = isDark,
                                    onLocationChosen = { loc -> onQuickAction("Set delivery location: $loc") }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Timestamp & Delivery reference
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (message.deliveryId.isNotBlank()) {
                            Text(
                                text = FormatUtils.formatDisplayTrackingId(message.deliveryId),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUser) Obsidian.copy(alpha = 0.7f) else Gold
                            )
                            Text("•", fontSize = 9.sp, color = if (isUser) Obsidian.copy(alpha = 0.7f) else secondaryText)
                        }
                        Text(
                            text = timeString,
                            fontSize = 9.sp,
                            color = if (isUser) Obsidian.copy(alpha = 0.7f) else secondaryText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TypingIndicatorBubble(
    text: String,
    isDark: Boolean,
    primaryText: Color
) {
    val infiniteTransition = rememberInfiniteTransition()
    val dot1Scale by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val dot2Scale by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val dot3Scale by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        modifier = Modifier.padding(start = 4.dp, top = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isDark) Obsidian else GoldenWhiteLight,
            border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gold)

                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(modifier = Modifier.size(5.dp).scale(dot1Scale).clip(CircleShape).background(Gold))
                    Box(modifier = Modifier.size(5.dp).scale(dot2Scale).clip(CircleShape).background(Gold))
                    Box(modifier = Modifier.size(5.dp).scale(dot3Scale).clip(CircleShape).background(Gold))
                }
            }
        }
    }
}

@Composable
private fun WaveformBars() {
    val infiniteTransition = rememberInfiniteTransition()
    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, delayMillis = 100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 26f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val bar4Height by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, delayMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val bar5Height by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(460, delayMillis = 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.height(28.dp)
    ) {
        Surface(modifier = Modifier.width(3.dp).height(bar1Height.dp), shape = RoundedCornerShape(2.dp), color = Gold) {}
        Surface(modifier = Modifier.width(3.dp).height(bar2Height.dp), shape = RoundedCornerShape(2.dp), color = Gold) {}
        Surface(modifier = Modifier.width(3.dp).height(bar3Height.dp), shape = RoundedCornerShape(2.dp), color = Gold) {}
        Surface(modifier = Modifier.width(3.dp).height(bar4Height.dp), shape = RoundedCornerShape(2.dp), color = Gold) {}
        Surface(modifier = Modifier.width(3.dp).height(bar5Height.dp), shape = RoundedCornerShape(2.dp), color = Gold) {}
    }
}

@Composable
private fun ChatBookingActionCard(
    isDark: Boolean,
    isActionActive: Boolean = true,
    onConfirm: () -> Unit,
    onSelectLocation: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Charcoal else GoldenWhiteSurface,
        border = BorderStroke(1.dp, if (isActionActive) Gold else Gold.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        Icons.Filled.DirectionsBike,
                        contentDescription = null,
                        tint = if (isActionActive) Gold else TextGray,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        if (isActionActive) "EXPRESS DISPATCH DRAFT" else "EXPRESS DISPATCH [EXPIRED DRAFT]",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isActionActive) Gold else TextGray
                    )
                }
                Text(
                    "₦1,200",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDark) (if (isActionActive) Color.White else TextGray) else (if (isActionActive) Obsidian else TextGray)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "• From: Benin Central (Ring Road)",
                    fontSize = 11.sp,
                    color = if (isActionActive) (if (isDark) Color.White else Obsidian) else TextGray,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "• To: Airport Road / GRA Corridor",
                    fontSize = 11.sp,
                    color = if (isActionActive) (if (isDark) Color.White else Obsidian) else TextGray,
                    fontWeight = FontWeight.Medium
                )
            }

            if (!isActionActive) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDark) Obsidian.copy(alpha = 0.5f) else Slate.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Draft superseded by newer inquiry",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextGray,
                        modifier = Modifier.padding(vertical = 6.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDark) Obsidian else Slate.copy(alpha = 0.2f),
                        border = BorderStroke(0.8.dp, Gold.copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f).clickable { onSelectLocation() }
                    ) {
                        Text(
                            text = "Change Route",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold,
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Gold,
                        modifier = Modifier.weight(1.3f).clickable { onConfirm() }
                    ) {
                        Text(
                            text = "Confirm & Book",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Obsidian,
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatTrackingActionCard(
    deliveryId: String,
    isDark: Boolean,
    onOpenLiveMap: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Charcoal else GoldenWhiteSurface,
        border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(shape = CircleShape, color = Color(0xFF4CAF50), modifier = Modifier.size(7.dp)) {}
                    Text("LIVE SHIPMENT RADAR", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF4CAF50))
                }
                Text(FormatUtils.formatDisplayTrackingId(deliveryId), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Gold)
            }

            Text("Assigned Courier: Central Fleet Rider • In Transit on Ring Road", fontSize = 11.sp, color = if (isDark) Color.White else Obsidian)

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Gold,
                modifier = Modifier.fillMaxWidth().clickable { onOpenLiveMap() }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 7.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Radar, contentDescription = null, tint = Obsidian, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Track on Live Radar", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Obsidian)
                }
            }
        }
    }
}

@Composable
private fun ChatWalletActionCard(
    isDark: Boolean,
    onOpenWallet: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Charcoal else GoldenWhiteSurface,
        border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = Gold, modifier = Modifier.size(15.dp))
                    Text("WALLET & ESCROW SHIELD", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Gold)
                }
                Text("PROTECTED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
            }

            Text("All transactions, dispatch fees, and refunds are instant and protected under ESDispatch Escrow.", fontSize = 11.sp, color = if (isDark) Color.White else Obsidian)

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Gold,
                modifier = Modifier.fillMaxWidth().clickable { onOpenWallet() }
            ) {
                Text(
                    text = "Open Wallet Ledger",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Obsidian,
                    modifier = Modifier.padding(vertical = 7.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ChatLocationActionCard(
    isDark: Boolean,
    onLocationChosen: (String) -> Unit
) {
    val hubs = listOf("Ring Road", "UNIBEN Gate", "GRA", "Airport Rd", "Sapele Rd", "Ikpoba Hill")
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Charcoal else GoldenWhiteSurface,
        border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Gold, modifier = Modifier.size(15.dp))
                Text("SELECT BENIN CITY HUB", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Gold)
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                items(hubs) { hub ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isDark) Obsidian else Slate.copy(alpha = 0.25f),
                        border = BorderStroke(0.7.dp, Gold.copy(alpha = 0.3f)),
                        modifier = Modifier.clickable { onLocationChosen(hub) }
                    ) {
                        Text(
                            text = hub,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Gold,
                modifier = Modifier.fillMaxWidth().clickable { onLocationChosen("Current GPS Location") }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.MyLocation, contentDescription = null, tint = Obsidian, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Use Current GPS Location", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Obsidian)
                }
            }
        }
    }
}

@Composable
fun VoiceCallHUDModal(
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
    isRepresentativeJoined: Boolean,
    representativeName: String,
    callSeconds: Int = 0,
    onDismiss: () -> Unit,
    onEndCall: (durationSec: Int, actionTaken: String) -> Unit,
    onNavigateToBooking: () -> Unit = {},
    onNavigateToTracking: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }

    // Concentric breathing rings animation for active speech/connection
    val infiniteTransition = rememberInfiniteTransition(label = "VoiceCallRings")
    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1Scale"
    )
    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1Alpha"
    )
    val ring2Scale by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, delayMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2Scale"
    )
    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, delayMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2Alpha"
    )

    // Animated audio waveform heights (reacts rhythmically during call)
    val wave1 by infiniteTransition.animateFloat(initialValue = 6f, targetValue = 24f, animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "w1")
    val wave2 by infiniteTransition.animateFloat(initialValue = 16f, targetValue = 8f, animationSpec = infiniteRepeatable(tween(480, delayMillis = 80, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "w2")
    val wave3 by infiniteTransition.animateFloat(initialValue = 10f, targetValue = 28f, animationSpec = infiniteRepeatable(tween(390, delayMillis = 160, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "w3")
    val wave4 by infiniteTransition.animateFloat(initialValue = 22f, targetValue = 6f, animationSpec = infiniteRepeatable(tween(450, delayMillis = 240, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "w4")
    val wave5 by infiniteTransition.animateFloat(initialValue = 8f, targetValue = 22f, animationSpec = infiniteRepeatable(tween(470, delayMillis = 120, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "w5")
    val wave6 by infiniteTransition.animateFloat(initialValue = 18f, targetValue = 10f, animationSpec = infiniteRepeatable(tween(410, delayMillis = 200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "w6")
    val wave7 by infiniteTransition.animateFloat(initialValue = 7f, targetValue = 20f, animationSpec = infiniteRepeatable(tween(460, delayMillis = 280, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "w7")

    // Theme-adaptive styling: strictly NOT black or dark mode unless dark theme is enabled!
    val screenBg = if (isDark) LuxuryBlack else GoldenWhite
    val controlBg = if (isDark) Charcoal else Color.White
    val controlBorder = if (isDark) Gold.copy(alpha = 0.25f) else Slate.copy(alpha = 0.4f)
    val callerTitle = if (isRepresentativeJoined) representativeName else "Support Concierge"
    val callerSubtitle = if (isRepresentativeJoined) "Customer Care Specialist" else "Live Dispatch Assistant"
    val formattedDuration = String.format("%02d:%02d", callSeconds / 60, callSeconds % 60)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(99f)
            .background(screenBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Bar: Clean, organized header with minimize and connection status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDismiss()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Charcoal else Slate.copy(alpha = 0.25f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Minimize Call",
                        tint = if (isDark) Gold else Obsidian,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ESDISPATCH CONCIERGE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Gold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (callSeconds < 3) Gold else Color(0xFF4CAF50),
                            modifier = Modifier.size(6.dp)
                        ) {}
                        Text(
                            text = if (callSeconds < 3) "Connecting..." else "Connected",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (callSeconds < 3) Gold else (if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32))
                        )
                    }
                }

                // Call Duration Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Charcoal else Color.White,
                    border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.3f) else Slate.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = formattedDuration,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Gold else Obsidian,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // 2. Center Stage: Caller avatar with clean animated breathing rings, name, status, and waveform
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.size(190.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer Ring 1
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .scale(ring1Scale)
                            .clip(CircleShape)
                            .border(1.5.dp, Gold.copy(alpha = if (isDark) ring1Alpha * 0.45f else ring1Alpha * 0.25f), CircleShape)
                    )
                    // Outer Ring 2
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .scale(ring2Scale)
                            .clip(CircleShape)
                            .border(1.5.dp, Gold.copy(alpha = if (isDark) ring2Alpha * 0.35f else ring2Alpha * 0.20f), CircleShape)
                    )
                    // Core Orb: Obsidian surface with Gold border and icon ensures maximum contrast in both light and dark modes
                    Box(
                        modifier = Modifier
                            .size(108.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Charcoal else Obsidian)
                            .border(2.5.dp, Gold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRepresentativeJoined) Icons.Filled.SupportAgent else Icons.Filled.HeadsetMic,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = callerTitle,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Obsidian,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = callerSubtitle,
                    fontSize = 13.sp,
                    color = secondaryText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Clean status chip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isMuted) Color(0xFFE53935).copy(alpha = 0.12f) else (if (isDark) Charcoal else Slate.copy(alpha = 0.22f)),
                    border = BorderStroke(0.8.dp, if (isMuted) Color(0xFFE53935).copy(alpha = 0.4f) else (if (isDark) Gold.copy(alpha = 0.25f) else Color.Transparent))
                ) {
                    Text(
                        text = if (callSeconds < 3) "Establishing voice line..." else if (isMuted) "Microphone is muted" else "Voice line active",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isMuted) Color(0xFFE53935) else (if (isDark) Gold else Obsidian),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Rhythmic Audio Waveform Bars (Clean, uncluttered, animated)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    val bars = listOf(wave1, wave2, wave3, wave4, wave5, wave6, wave7)
                    bars.forEach { h ->
                        Surface(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height((if (isMuted) 4f else h).dp),
                            shape = RoundedCornerShape(2.dp),
                            color = if (isMuted) TextGray else Gold
                        ) {}
                    }
                }
            }

            // 3. Bottom Call Controls: Clean, symmetrical, beautifully spaced
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Control 1: Mute Toggle
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = {
                            isMuted = !isMuted
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(if (isMuted) Color(0xFFE53935).copy(alpha = 0.15f) else controlBg)
                            .border(1.dp, if (isMuted) Color(0xFFE53935) else controlBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                            contentDescription = if (isMuted) "Unmute Microphone" else "Mute Microphone",
                            tint = if (isMuted) Color(0xFFE53935) else (if (isDark) Color.White else Obsidian),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = if (isMuted) "Unmute" else "Mute",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) TextGray else Obsidian
                    )
                }

                // Control 2: Speakerphone Toggle
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = {
                            isSpeakerOn = !isSpeakerOn
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(if (isSpeakerOn) Gold.copy(alpha = 0.2f) else controlBg)
                            .border(1.dp, if (isSpeakerOn) Gold else controlBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isSpeakerOn) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                            contentDescription = if (isSpeakerOn) "Turn Off Speaker" else "Turn On Speaker",
                            tint = if (isSpeakerOn) (if (isDark) Gold else Obsidian) else (if (isDark) Color.White else Obsidian),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "Speaker",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) TextGray else Obsidian
                    )
                }

                // Control 3: In-Call Chat / Minimize (Returns cleanly to chat while call persists)
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(controlBg)
                            .border(1.dp, controlBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Chat,
                            contentDescription = "Text Chat",
                            tint = if (isDark) Color.White else Obsidian,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "Message",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) TextGray else Obsidian
                    )
                }

                // Control 4: End Call Button (Prominent red circular button)
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onEndCall(callSeconds, "")
                        },
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = "End",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53935)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectParcelBottomSheet(
    parcels: List<Parcel>,
    selectedParcelId: String?,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
    onDismiss: () -> Unit,
    onSelect: (Parcel) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(parcels, searchQuery) {
        if (searchQuery.isBlank()) parcels
        else parcels.filter {
            it.id.contains(searchQuery, ignoreCase = true) ||
            it.itemName.contains(searchQuery, ignoreCase = true) ||
            it.deliveryAddress.contains(searchQuery, ignoreCase = true) ||
            it.pickupAddress.contains(searchQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Obsidian else GoldenWhite,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Gold)
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Select Shipment to Inquire",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = primaryText
                    )
                    Text(
                        text = "Choose any active or past delivery to discuss with ESAI",
                        fontSize = 12.sp,
                        color = secondaryText
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = if (isDark) Charcoal else Slate.copy(alpha = 0.3f),
                    modifier = Modifier.clickable { onDismiss() }
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = primaryText,
                        modifier = Modifier.padding(6.dp).size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by ID, item name, or address...", fontSize = 12.sp, color = secondaryText) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = if (isDark) Slate.copy(alpha = 0.4f) else Slate,
                    focusedContainerColor = if (isDark) Charcoal else GoldenWhiteSurface,
                    unfocusedContainerColor = if (isDark) Charcoal else GoldenWhiteSurface,
                    focusedTextColor = primaryText,
                    unfocusedTextColor = primaryText
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Inventory2, contentDescription = null, tint = secondaryText, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No shipments found", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = primaryText)
                        Text("All deliveries booked on your account will appear here.", fontSize = 11.sp, color = secondaryText)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { parcel ->
                        val isSelected = parcel.id == selectedParcelId
                        val displayId = FormatUtils.formatDisplayTrackingId(parcel.id)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) (if (isDark) Charcoal else GoldenWhiteSurface) else (if (isDark) Charcoal.copy(alpha = 0.6f) else GoldenWhiteLight),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) Gold else (if (isDark) Slate.copy(alpha = 0.3f) else Slate.copy(alpha = 0.5f))
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(parcel)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = when (parcel.status) {
                                        ParcelStatus.DELIVERED -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                        ParcelStatus.TRANSIT, ParcelStatus.OUT_FOR_DELIVERY -> Gold.copy(alpha = 0.2f)
                                        ParcelStatus.CANCELLED -> Color(0xFFFF5252).copy(alpha = 0.15f)
                                        else -> Gold.copy(alpha = 0.15f)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = when (parcel.status) {
                                                ParcelStatus.DELIVERED -> Icons.Filled.CheckCircle
                                                ParcelStatus.TRANSIT, ParcelStatus.OUT_FOR_DELIVERY -> Icons.Filled.DirectionsBike
                                                ParcelStatus.CANCELLED -> Icons.Filled.Cancel
                                                else -> Icons.Filled.LocalShipping
                                            },
                                            contentDescription = null,
                                            tint = when (parcel.status) {
                                                ParcelStatus.DELIVERED -> Color(0xFF4CAF50)
                                                ParcelStatus.TRANSIT, ParcelStatus.OUT_FOR_DELIVERY -> Gold
                                                ParcelStatus.CANCELLED -> Color(0xFFFF5252)
                                                else -> Gold
                                            },
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(displayId, fontSize = 12.sp, fontWeight = FontWeight.Black, color = primaryText)
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = when (parcel.status) {
                                                ParcelStatus.DELIVERED -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                                ParcelStatus.TRANSIT -> Gold.copy(alpha = 0.2f)
                                                else -> Slate.copy(alpha = 0.3f)
                                            }
                                        ) {
                                            Text(
                                                text = parcel.status.name,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (parcel.status) {
                                                    ParcelStatus.DELIVERED -> Color(0xFF4CAF50)
                                                    ParcelStatus.TRANSIT -> Gold
                                                    else -> primaryText
                                                },
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = parcel.itemName.ifBlank { "Standard Dispatch Item" },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = primaryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (parcel.deliveryAddress.isNotBlank()) {
                                        Text(
                                            text = "To: ${parcel.deliveryAddress}",
                                            fontSize = 10.sp,
                                            color = secondaryText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(Icons.Filled.Check, contentDescription = "Selected", tint = Gold, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

