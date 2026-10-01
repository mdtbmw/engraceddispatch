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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            val welcomeText = "Hello ${currentUserName.ifBlank { "there" }}! 👋 I am ESAI, your intelligent logistics concierge. How can I assist you with your deliveries, bookings, or inquiries today?"
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
                            "You have an active shipment **$displayId** (${p.itemName}). Status: **${p.status.name}**."
                        } else {
                            "You currently have no active deliveries in transit. To book a new courier, tap 'Send Package' on your dashboard."
                        }
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
                        "Thank you for reaching out to ESDispatch Support. I have logged this inquiry under Ticket **#$ticketNumber** ($urgency priority). Is there anything specific about your shipments or account I can help clarify?"
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

                        // Active Parcel Link Selector
                        if (activeParcels.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                                Text("Linked Parcel:", fontSize = 11.sp, color = secondaryText)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(activeParcels) { p ->
                                        val isSelected = selectedParcel?.id == p.id
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) Gold else (if (isDark) Charcoal else Slate.copy(alpha = 0.3f)),
                                            modifier = Modifier.clickable {
                                                selectedParcel = if (isSelected) null else p
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                        SupportMessageBubble(
                            message = msg,
                            isUser = isUser,
                            isDark = isDark,
                            primaryText = primaryText,
                            secondaryText = secondaryText,
                            onSwipeToReply = {
                                replyingTo = msg
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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

                // Quick Replies Carousel
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickPrompts = listOf(
                        "Where is my rider?",
                        "Rider delay inquiry",
                        "Change delivery address",
                        "Speak to human agent",
                        "Wallet refund inquiry",
                        "Marketplace store support"
                    )
                    items(quickPrompts) { prompt ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isDark) Charcoal else Slate.copy(alpha = 0.35f),
                            border = BorderStroke(0.8.dp, if (isDark) Gold.copy(alpha = 0.3f) else Slate),
                            modifier = Modifier.clickable {
                                handleSendMessage(prompt)
                            }
                        ) {
                            Text(
                                text = prompt,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
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

                // Emoji Quick Reaction Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val emojis = listOf("👍", "🙏", "📦", "🛵", "⚡", "📍", "❤️", "😊")
                    emojis.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    messageText += emoji
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                .padding(4.dp)
                        )
                    }
                }

                // Bottom Input Row
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
                            .size(46.dp)
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

                    // Text Input Field
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Ask support or report an issue...", fontSize = 13.sp, color = secondaryText) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 46.dp, max = 110.dp),
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

                    // Send Button (Gold circular with Obsidian icon)
                    Surface(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .clickable(enabled = (messageText.isNotBlank() || selectedImageBitmap != null) && !isSending) {
                                handleSendMessage()
                            },
                        shape = CircleShape,
                        color = if (messageText.isNotBlank() || selectedImageBitmap != null) Gold else (if (isDark) Charcoal else Slate.copy(alpha = 0.4f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isSending) {
                                CircularProgressIndicator(color = Obsidian, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (messageText.isNotBlank() || selectedImageBitmap != null) Obsidian else secondaryText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
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
    onSwipeToReply: () -> Unit
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
                            text = message.messageText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = bubbleTextColor,
                            lineHeight = 18.sp
                        )
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
