package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import com.example.data.*
import com.example.ui.components.RoundedSheet
import com.example.ui.components.ScreenHeader
import com.example.ui.theme.*
import com.example.viewmodel.DeliveryViewModel
import kotlinx.coroutines.launch

@Composable
fun AIDispatchManagerScreen(
    viewModel: DeliveryViewModel,
    onBack: () -> Unit
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    AdminAuthGuard(viewModel = viewModel, isLight = isLight, onBack = onBack) {
        AIDispatchManagerContent(viewModel = viewModel, onBack = onBack)
    }
}

@Composable
fun AdminAuthGuard(
    viewModel: DeliveryViewModel,
    isLight: Boolean,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    val userEmail by viewModel.userEmail.collectAsState()
    val isAdminVerified by viewModel.isAdminVerified.collectAsState()
    var adminKeyInput by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf<String?>(null) }
    var isVerifying by remember { mutableStateOf(false) }

    val isAuthorized = isAdminVerified

    if (isAuthorized) {
        content()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isLight) BackgroundLight else BackgroundDark)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Gold.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Admin Security",
                    tint = Gold,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Admin Authorization Required",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = if (isLight) Obsidian else Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Secure Firestore role verification required to access the Enraced Dispatch Control Center and administrative override tools.",
                fontSize = 12.sp,
                color = if (isLight) TextGray else Color.LightGray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = adminKeyInput,
                onValueChange = { adminKeyInput = it; authError = null },
                label = { Text("Admin Secret Key / Passcode") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = if (isLight) Obsidian else Color.White)
            )
            if (authError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = authError!!, color = Color.Red, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    isVerifying = true
                    viewModel.verifyAdminAccess(adminKeyInput) { success ->
                        isVerifying = false
                        if (!success) {
                            authError = "Invalid Admin Key or unauthorized role."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isVerifying) "Verifying Server Role..." else "Verify Admin Access", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(45.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Return to Dashboard", color = if (isLight) Obsidian else Color.White)
            }
        }
    }
}

@Composable
fun AIDispatchManagerContent(
    viewModel: DeliveryViewModel,
    onBack: () -> Unit
) {
    val chatMessages by viewModel.aiChatMessages.collectAsState()
    val isThinking by viewModel.aiIsThinking.collectAsState()

    val riders by viewModel.aiRiders.collectAsState()
    val assignmentReason by viewModel.aiSmartAssignmentReason.collectAsState()
    val assignmentList by viewModel.aiSmartAssignmentList.collectAsState()
    val riskReport by viewModel.aiRiskReport.collectAsState()
    val podAnalysis by viewModel.aiPODAnalysis.collectAsState()
    val fraudAlerts by viewModel.aiFraudAlerts.collectAsState()
    val trafficCongested by viewModel.aiTrafficCongested.collectAsState()
    val confidenceScore by viewModel.aiConfidenceScore.collectAsState()
    val predictions by viewModel.aiDemandPredictions.collectAsState()
    val incidents by viewModel.aiIncidentReports.collectAsState()
    val weights by viewModel.aiLearningWeights.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val surfaceColor = AppBackground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HeaderBgColor)
    ) {
        ScreenHeader(
            title = "AI Shipping Assistant",
            onBack = onBack
        )

        RoundedSheet(
            modifier = Modifier.weight(1f),
            containerColor = surfaceColor
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Premium Dynamic Tab Switcher Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isLight) TextGray.copy(alpha = 0.3f) else Charcoal),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val tabs = listOf("Customer Chat", "Control Center", "Insights")
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) {
                                        if (isLight) Obsidian else Gold
                                    } else {
                                        Color.Transparent
                                    }
                                )
                                .clickable { selectedTab = index }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) {
                                    if (isLight) Gold else Obsidian
                                } else {
                                    if (isLight) Obsidian.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.6f)
                                }
                            )
                        }
                    }
                }

                // Render selected Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        0 -> {
                            CustomerAssistantTab(
                                messages = chatMessages,
                                isThinking = isThinking,
                                onSendMessage = { viewModel.sendChatMessage(it) },
                                onClearChat = { viewModel.clearChat() }
                            )
                        }
                        1 -> {
                            ControlCenterTab(
                                viewModel = viewModel,
                                riders = riders,
                                assignmentReason = assignmentReason,
                                assignmentList = assignmentList,
                                riskReport = riskReport,
                                podAnalysis = podAnalysis,
                                fraudAlerts = fraudAlerts,
                                trafficCongested = trafficCongested,
                                confidenceScore = confidenceScore,
                                onTriggerReroute = { viewModel.triggerLiveRerouting() },
                                onCheckPOD = { viewModel.checkProofOfDelivery() },
                                onRunFraudScan = { viewModel.scanForFraud() },
                                onTweakLearning = { viewModel.triggerSelfLearningFeedback() }
                            )
                        }
                        2 -> {
                            InsightsDashboardTab(
                                predictions = predictions,
                                riders = riders,
                                incidents = incidents,
                                weights = weights,
                                onTweakLearning = { viewModel.triggerSelfLearningFeedback() },
                                onRemoveIncident = { viewModel.removeIncident(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 1: CUSTOMER AI ASSISTANT ---
@Composable
fun CustomerAssistantTab(
    messages: List<AIChatMessage>,
    isThinking: Boolean,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val quickActions = listOf(
        "Book motorcycle dispatch",
        "Where is Richard Dheo?",
        "Check weather & traffic risks",
        "Estimate delivery cost"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat bubbles
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(message = msg)
                }

                if (isThinking) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Obsidian)
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Operations Manager is thinking...",
                                        fontSize = 12.sp,
                                        color = Gold,
                                        fontWeight = FontWeight.Medium
                                    )
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 1.5.dp,
                                        color = Gold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Scroll to bottom helper
            LaunchedEffect(messages.size, isThinking) {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }
        }

        // Suggested quick action chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickActions.forEach { action ->
                Surface(
                    modifier = Modifier.clickable {
                        onSendMessage(action)
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Obsidian,
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = action,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Input bottom bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Obsidian,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Clear chat button
                IconButton(
                    onClick = onClearChat,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(LuxuryBlack)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear Chat",
                        tint = Color.Red.copy(alpha = 0.8f)
                    )
                }

                // Input box
                TextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Ask the virtual operations manager...", fontSize = 13.sp, color = TextGray) },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = LuxuryBlack,
                        unfocusedContainerColor = LuxuryBlack,
                        disabledContainerColor = LuxuryBlack,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                // Send button
                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            onSendMessage(textInput)
                            textInput = ""
                            keyboardController?.hide()
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Gold),
                    enabled = textInput.isNotBlank() && !isThinking
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Obsidian // Strict check: NO WHITE ON GOLD
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: AIChatMessage) {
    val isUser = message.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentWidth(align = if (isUser) Alignment.End else Alignment.Start)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 16.dp
                    )
                )
                .background(if (isUser) Gold else Obsidian)
                .border(
                    width = if (isUser) 0.dp else 1.dp,
                    color = if (isUser) Color.Transparent else Gold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = message.text,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                // Strict check: NO WHITE ON GOLD
                color = if (isUser) Obsidian else Color.White,
                fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}


// --- TAB 2: OPERATIONS CONTROL CENTER ---
@Composable
fun ControlCenterTab(
    viewModel: DeliveryViewModel,
    riders: List<Rider>,
    assignmentReason: String,
    assignmentList: List<Pair<Rider, Int>>,
    riskReport: RiskReport?,
    podAnalysis: PODAnalysis?,
    fraudAlerts: List<FraudAlert>,
    trafficCongested: Boolean,
    confidenceScore: Int,
    onTriggerReroute: () -> Unit,
    onCheckPOD: () -> Unit,
    onRunFraudScan: () -> Unit,
    onTweakLearning: () -> Unit
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val isDark = !isLight
    val containerBg = Charcoal
    val cardBorder = if (isLight) BorderStroke(1.dp, TextGray.copy(alpha = 0.4f)) else BorderStroke(1.dp, Gold.copy(alpha = 0.15f))
    val textHighlight = if (isLight) Obsidian else Gold
    val labelColor = if (isLight) TextGray else TextGray
    val subTextColor = if (isLight) TextGray else TextGray.copy(alpha = 0.7f)
    val bodyTextColor = if (isLight) Obsidian else TextGray
    val iconTint = if (isLight) Obsidian else Gold

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Admin & System Control Center Card
        item {
            AdminSystemControlCard(viewModel = viewModel)
        }

        // Control Center Header Cards (Confidence & Risk status)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Confidence gauge card
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    color = containerBg,
                    border = cardBorder
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("AI Confidence", fontSize = 11.sp, color = labelColor, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$confidenceScore%", fontSize = 32.sp, fontWeight = FontWeight.Black, color = textHighlight)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (trafficCongested) "Degraded (Expressway delay)" else "Stable (98.6% precision)",
                            fontSize = 10.sp,
                            color = if (trafficCongested) Color(0xFFE53935) else Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Live Congestion Trigger Card
                Surface(
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(20.dp),
                    color = containerBg,
                    border = if (trafficCongested) BorderStroke(1.5.dp, Color.Red.copy(alpha = 0.5f)) else cardBorder
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Live Rerouter", fontSize = 11.sp, color = labelColor, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = onTriggerReroute,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (trafficCongested) Color.Red else Gold,
                                contentColor = Obsidian
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text(
                                text = if (trafficCongested) "CONGESTION ACTIVE" else "RE-ROUTE ACTIVE TRIP",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = if (trafficCongested) "Rerouted courier Richard around gridlock" else "Normal operational routing active",
                            fontSize = 8.5.sp,
                            color = subTextColor,
                            lineHeight = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Section 1: Fleet Roster Status
        item {
            CardSectionHeader("OPERATIONAL FLEET ROSTER", Icons.Default.People)
        }

        items(riders) { rider ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = containerBg,
                border = cardBorder
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar icon placeholder with vehicle shape
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isLight) BackgroundLight else LuxuryBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (rider.vehicleType) {
                                "Bike" -> Icons.Default.TwoWheeler
                                "Tricycle" -> Icons.Default.RvHookup
                                "Van" -> Icons.Default.AirportShuttle
                                "Truck" -> Icons.Default.LocalShipping
                                else -> Icons.Default.DirectionsBike
                            },
                            contentDescription = rider.vehicleType,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = rider.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTextColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (rider.status == RiderStatus.ONLINE) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color(0xFFFBC02D).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = rider.status.name,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (rider.status == RiderStatus.ONLINE) Color(0xFF4CAF50) else if (isLight) Obsidian else Color(0xFFFBC02D),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${rider.vehicleType} • ${rider.shiftSchedule}",
                            fontSize = 11.sp,
                            color = subTextColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = "rating", tint = iconTint, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${rider.rating}", fontSize = 12.sp, color = AppTextColor, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "🔋 ${rider.batteryLevel}%",
                            fontSize = 10.sp,
                            color = if (rider.batteryLevel > 70) Color(0xFF4CAF50) else if (isLight) Obsidian else Color(0xFFFBC02D)
                        )
                    }
                }
            }
        }

        // Section 2: Smart Rider Matching Console
        item {
            val availableDeliveries by viewModel.availableDeliveries.collectAsState()
            var selectedParcel by remember { mutableStateOf<Parcel?>(null) }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = containerBg,
                border = cardBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoMode, contentDescription = "AI MATCH", tint = iconTint, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rider Assignment Engine", fontSize = 14.sp, fontWeight = FontWeight.Black, color = AppTextColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "REAL UNASSIGNED SHIPMENTS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = labelColor,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    if (availableDeliveries.isEmpty()) {
                        Text(
                            text = "No pending shipments currently waiting for dispatch. Book real shipments to test end-to-end flow! 📦",
                            fontSize = 11.sp,
                            color = subTextColor,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    } else {
                        // Horizontal scrollable list of available (pending) parcels
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableDeliveries.forEach { parcel ->
                                val isSelected = selectedParcel?.id == parcel.id
                                Card(
                                    modifier = Modifier
                                        .width(200.dp)
                                        .clickable {
                                            selectedParcel = parcel
                                            viewModel.runSmartAssignment(parcel.pickupAddress, parcel.weight, false)
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Gold else (if (isLight) BackgroundLight else LuxuryBlack)
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) Gold else (if (isLight) TextGray.copy(alpha = 0.4f) else Gold.copy(alpha = 0.15f)))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "#${parcel.id}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isSelected) Obsidian else textHighlight
                                        )
                                        Text(
                                            text = parcel.itemName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Obsidian else AppTextColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Weight: ${parcel.weight}kg",
                                            fontSize = 10.sp,
                                            color = if (isSelected) Obsidian else subTextColor
                                        )
                                        Text(
                                            text = "To: ${parcel.deliveryAddress}",
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = if (isSelected) Obsidian else subTextColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (selectedParcel != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "SELECTED SHIPMENT: #${selectedParcel?.id}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = labelColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pickup: ${selectedParcel?.pickupAddress}\nDelivery: ${selectedParcel?.deliveryAddress}\nWeight: ${selectedParcel?.weight}kg",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = AppTextColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isLight) BackgroundLight else LuxuryBlack, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (assignmentList.isNotEmpty() && selectedParcel != null) {
                        Text(
                            "RANKED ALGORITHM PROPOSALS (SELECT TO ASSIGN)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = labelColor,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            assignmentList.take(3).forEachIndexed { rank, pair ->
                                val rider = pair.first
                                val matchPercentage = pair.second
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isLight) BackgroundLight else LuxuryBlack.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("#${rank+1}", fontSize = 11.sp, color = textHighlight, fontWeight = FontWeight.Black)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(rider.name, fontSize = 11.sp, color = AppTextColor, fontWeight = FontWeight.Bold)
                                            Text("(${rider.vehicleType} • Rating: ${rider.rating}★)", fontSize = 9.sp, color = subTextColor)
                                        }
                                    }
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (matchPercentage > 85) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color(0xFFFBC02D).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "$matchPercentage%",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (matchPercentage > 85) Color(0xFF4CAF50) else if (isLight) Obsidian else Color(0xFFFBC02D),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                selectedParcel?.let { parcel ->
                                                    viewModel.assignRiderToParcel(parcel.id, rider) { success, _ ->
                                                        if (success) {
                                                            selectedParcel = null
                                                        }
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("ASSIGN", fontSize = 9.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (selectedParcel == null) "Select an unassigned shipment from the list above to calculate best rider match profiles and execute dispatch assignments." else assignmentReason,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = bodyTextColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isLight) BackgroundLight else LuxuryBlack, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    )
                }
            }
        }

        // Section 3: Proof of Delivery Vision Scan & Fraud Detection
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // POD station
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    color = containerBg,
                    border = cardBorder
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Vision", tint = iconTint, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("POD Vision AI", fontSize = 12.sp, fontWeight = FontWeight.Black, color = AppTextColor)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onCheckPOD,
                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text("VERIFY POD WITH AI", fontSize = 8.5.sp, fontWeight = FontWeight.Black)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        if (podAnalysis != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                PodStatusRow("Package Visible", podAnalysis.packageVisible)
                                PodStatusRow("Customer Received", podAnalysis.customerReceived)
                                PodStatusRow("GPS Tag Matches", podAnalysis.locationVerified)
                                Text("Fake Score: ${podAnalysis.fakeConfidence}% (Verified)", fontSize = 9.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text("Launch scan to inspect receipt image features.", fontSize = 9.sp, color = labelColor)
                        }
                    }
                }

                // Fraud audit
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    color = containerBg,
                    border = cardBorder
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = "Security", tint = iconTint, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Fraud Auditor", fontSize = 12.sp, fontWeight = FontWeight.Black, color = AppTextColor)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onRunFraudScan,
                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text("AUDIT BLOCKED NODES", fontSize = 8.5.sp, fontWeight = FontWeight.Black)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        if (fraudAlerts.isNotEmpty()) {
                            Text("Anomalous IPs Suspended: ${fraudAlerts.size}", fontSize = 9.5.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Blocked duplicate coordinates on VPN.", fontSize = 8.5.sp, color = subTextColor)
                        } else {
                            Text("No suspicious activities flagged in system.", fontSize = 9.sp, color = labelColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PodStatusRow(label: String, checked: Boolean) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val labelColor = if (isLight) TextGray else TextGray

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 9.sp, color = labelColor)
        Icon(
            imageVector = if (checked) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (checked) Color(0xFF4CAF50) else Color(0xFFE53935),
            modifier = Modifier.size(10.dp)
        )
    }
}

// --- TAB 3: ADMIN INSIGHTS & ANALYTICS ---
@Composable
fun InsightsDashboardTab(
    predictions: List<DemandPrediction>,
    riders: List<Rider>,
    incidents: List<IncidentReport>,
    weights: SelfLearningWeights,
    onTweakLearning: () -> Unit,
    onRemoveIncident: (String) -> Unit
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val isDark = !isLight
    val containerBg = Charcoal
    val cardBorder = if (isLight) BorderStroke(1.dp, TextGray.copy(alpha = 0.4f)) else BorderStroke(1.dp, Gold.copy(alpha = 0.15f))
    val textHighlight = if (isLight) Obsidian else Gold
    val labelColor = if (isLight) TextGray else TextGray
    val subTextColor = if (isLight) TextGray else TextGray.copy(alpha = 0.7f)
    val bodyTextColor = if (isLight) Obsidian else TextGray
    val iconTint = if (isLight) Obsidian else Gold

    val gridColor = if (isLight) Color.Black.copy(alpha = 0.08f) else TextGray.copy(alpha = 0.15f)
    val barGradient = if (isLight) listOf(Obsidian, Obsidian.copy(alpha = 0.5f)) else listOf(Gold, Gold.copy(alpha = 0.4f))
    val barShadow = if (isLight) Color.Transparent else Color.Black.copy(alpha = 0.3f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Feature 12: Predictive Demand Custom Chart Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = containerBg,
                border = cardBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.TrendingUp, contentDescription = "Trend", tint = iconTint, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Predictive Demand Demand Forecast", fontSize = 14.sp, fontWeight = FontWeight.Black, color = AppTextColor)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = textHighlight
                        ) {
                            Text(
                                "TOMORROW",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isLight) Color.White else Obsidian,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Draw beautiful custom canvas chart representing hourly forecasted bookings
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val barWidth = 30.dp.toPx()
                            val spacing = (w - (barWidth * predictions.size)) / (predictions.size + 1)
                            val maxBookings = 60f

                            // Draw reference gridlines
                            drawLine(gridColor, Offset(0f, h * 0.25f), Offset(w, h * 0.25f), 1f)
                            drawLine(gridColor, Offset(0f, h * 0.5f), Offset(w, h * 0.5f), 1f)
                            drawLine(gridColor, Offset(0f, h * 0.75f), Offset(w, h * 0.75f), 1f)

                            predictions.forEachIndexed { i, pred ->
                                val x = spacing + i * (barWidth + spacing)
                                val barHeight = (pred.predictedBookings / maxBookings) * h
                                val y = h - barHeight

                                // Draw bar background shadow
                                if (isDark) {
                                    drawRoundRect(
                                        color = barShadow,
                                        topLeft = Offset(x + 2.dp.toPx(), y + 2.dp.toPx()),
                                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                    )
                                }

                                // Draw bar with gradient style
                                drawRoundRect(
                                    brush = Brush.verticalGradient(barGradient),
                                    topLeft = Offset(x, y),
                                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )
                            }
                        }
                    }

                    // Labels below bars
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        predictions.forEach { pred ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(44.dp)
                            ) {
                                Text(pred.hour, fontSize = 9.sp, color = labelColor, fontWeight = FontWeight.Bold)
                                Text("${pred.predictedBookings}bk", fontSize = 9.sp, color = AppTextColor, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isLight) Gold.copy(alpha = 0.15f) else Gold.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, if (isLight) Color.Transparent else Gold.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = "💡 Operational Suggestion: Peak predicted around 16:00 (55 bookings). Restrict rider shift check-outs and prioritize motorcycle dispatch pre-allocations.",
                            fontSize = 10.sp,
                            color = if (isLight) Obsidian else Gold,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        // Feature 15: Self-Learning Engine Weights Console
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = containerBg,
                border = cardBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Psychology, contentDescription = "Learning", tint = iconTint, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ML Self-Learning Weights", fontSize = 14.sp, fontWeight = FontWeight.Black, color = AppTextColor)
                        }

                        Button(
                            onClick = onTweakLearning,
                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("OPTIMIZE ENGINE", fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "The machine learning engine monitors delivery times and customer ratings, automatically adjusting matching factor weights to optimize speed.",
                        fontSize = 11.sp,
                        color = bodyTextColor,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    WeightSliderRow("Distance Proximity (GPS)", weights.distanceWeight)
                    WeightSliderRow("Courier Average Rating", weights.ratingWeight)
                    WeightSliderRow("Rider Current Workload", weights.workloadWeight)
                    WeightSliderRow("Vehicle Capability Fit", weights.vehicleFitWeight)
                    WeightSliderRow("Cancellation History Ratio", weights.cancellationWeight)
                }
            }
        }

        // Feature 13: Rider Performance Scorecard Metrics
        item {
            CardSectionHeader("RIDER PERFORMANCE SCORECARDS", Icons.Default.StarHalf)
        }

        items(riders) { rider ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = containerBg,
                border = cardBorder
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(rider.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("(${rider.vehicleType})", fontSize = 11.sp, color = subTextColor)
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (rider.rating >= 4.7) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color(0xFFFBC02D).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Scorecard Approved",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = if (rider.rating >= 4.7) Color(0xFF4CAF50) else if (isLight) Obsidian else Color(0xFFFBC02D),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ScorecardMetric("Avg Delivery", "${rider.averageDeliveryTimeMin} mins")
                        ScorecardMetric("Rating Avg", "${rider.rating}★")
                        ScorecardMetric("Cancellations", "${rider.cancellationHistoryCount}")
                        ScorecardMetric("Fuel Efficiency", "${rider.fuelEfficiency} km/L")
                    }
                }
            }
        }

        // Feature 14: Automatic Incident Logs
        item {
            CardSectionHeader("AUTOMATIC INCIDENT TIMELINE", Icons.Default.EventNote)
        }

        if (incidents.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = containerBg,
                    border = cardBorder
                ) {
                    Box(
                        modifier = Modifier.padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No transit incidents reported today. System clear.", fontSize = 11.sp, color = labelColor, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(incidents) { incident ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = containerBg,
                    border = if (isLight) BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f)) else BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFE53935)
                                ) {
                                    Text(
                                        text = incident.severity.uppercase(),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(incident.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTextColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }

                            IconButton(
                                onClick = { onRemoveIncident(incident.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = labelColor, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Rider: ${incident.riderName} • Customer: ${incident.customerName} • GPS: ${incident.gpsLocation}",
                            fontSize = 10.sp,
                            color = subTextColor
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = incident.description,
                            fontSize = 11.sp,
                            color = bodyTextColor,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = if (isLight) BackgroundLight else LuxuryBlack,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("AI RECOMMENDED MITIGATION:", fontSize = 8.5.sp, color = textHighlight, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(incident.suggestedAction, fontSize = 10.sp, color = AppTextColor, lineHeight = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeightSliderRow(label: String, value: Float) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val labelColor = if (isLight) TextGray else TextGray
    val textHighlight = if (isLight) Obsidian else Gold
    val progressColor = if (isLight) Obsidian else Gold

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 10.sp, color = labelColor)
            Text("${(value * 100).toInt()}%", fontSize = 10.sp, color = textHighlight, fontWeight = FontWeight.Black)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(if (isLight) TextGray.copy(alpha = 0.5f) else TextGray.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(value)
                    .fillMaxHeight()
                    .background(progressColor)
            )
        }
    }
}

@Composable
fun ScorecardMetric(label: String, value: String) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val labelColor = if (isLight) TextGray else TextGray

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 9.sp, color = labelColor, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 12.sp, color = AppTextColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CardSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val labelColor = if (isLight) TextGray else TextGray
    val iconTint = if (isLight) Obsidian else Gold

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Black, color = labelColor)
    }
}

@Composable
fun AdminSystemControlCard(
    viewModel: DeliveryViewModel
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val containerBg = Charcoal
    val cardBorder = if (isLight) BorderStroke(1.dp, TextGray.copy(alpha = 0.4f)) else BorderStroke(1.dp, Gold.copy(alpha = 0.15f))
    val textHighlight = if (isLight) Obsidian else Gold
    val labelColor = if (isLight) TextGray else TextGray
    val iconTint = if (isLight) Obsidian else Gold

    val pointsEnabled by viewModel.pointsSystemEnabled.collectAsState()
    val isDynamicPricing by viewModel.isDynamicPricingEnabled.collectAsState()
    val tipEnabled by viewModel.tipSystemEnabled.collectAsState()
    val sections by viewModel.dashboardSectionsEnabled.collectAsState()
    val configs by viewModel.adminCardSliderConfigs.collectAsState()

    var heroTitleText by remember(configs["hero_title"]) { mutableStateOf(configs["hero_title"] ?: "") }
    var heroSubtitleText by remember(configs["hero_subtitle"]) { mutableStateOf(configs["hero_subtitle"] ?: "") }
    var bannerImgText by remember(configs["banner_image"]) { mutableStateOf(configs["banner_image"] ?: "") }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = containerBg,
        border = cardBorder
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    Text("ADMIN & SYSTEM CONTROL CENTER", fontSize = 12.sp, fontWeight = FontWeight.Black, color = textHighlight)
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isLight) Obsidian else Gold
                ) {
                    Text(
                        "LIVE SYNC",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isLight) Color.White else Obsidian,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = labelColor.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Distance-based Pricing Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Dynamic Distance Pricing", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isDynamicPricing) Gold.copy(alpha = 0.15f) else TextGray.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isDynamicPricing) "AUTO" else "MANUAL",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDynamicPricing) Gold else TextGray,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text("Auto-calculate fare using real-time GPS distance", fontSize = 10.sp, color = labelColor)
                }
                Switch(
                    checked = isDynamicPricing,
                    onCheckedChange = { viewModel.togglePricingMode(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = if (isLight) Color.White else Obsidian, checkedTrackColor = textHighlight)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Points System Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Points & Loyalty System", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                    Text("Enable loyalty point earnings on deliveries", fontSize = 10.sp, color = labelColor)
                }
                Switch(
                    checked = pointsEnabled,
                    onCheckedChange = { viewModel.togglePointsSystem(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = if (isLight) Color.White else Obsidian, checkedTrackColor = textHighlight)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Tip System Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Driver Tip System", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                    Text("Allow customers to add tips for drivers", fontSize = 10.sp, color = labelColor)
                }
                Switch(
                    checked = tipEnabled,
                    onCheckedChange = { viewModel.toggleTipSystem(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = if (isLight) Color.White else Obsidian, checkedTrackColor = textHighlight)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val emailVerifyReq by viewModel.emailVerificationRequired.collectAsState()
            val phoneVerifyReq by viewModel.phoneVerificationRequired.collectAsState()

            // 3. Email Verification Required Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Email Verification Required", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                    Text("Require email OTP verification on login/signup", fontSize = 10.sp, color = labelColor)
                }
                Switch(
                    checked = emailVerifyReq,
                    onCheckedChange = { viewModel.toggleEmailVerification(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = if (isLight) Color.White else Obsidian, checkedTrackColor = textHighlight)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Phone Verification Required Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Phone Number Verification Required", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                    Text("Require Firebase OTP phone number validation", fontSize = 10.sp, color = labelColor)
                }
                Switch(
                    checked = phoneVerifyReq,
                    onCheckedChange = { viewModel.togglePhoneVerification(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = if (isLight) Color.White else Obsidian, checkedTrackColor = textHighlight)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text("Dashboard Sections Visibility", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textHighlight)
            Spacer(modifier = Modifier.height(8.dp))

            val sectionLabels = mapOf(
                "promo_banner" to "Promotional Hero Banner",
                "active_shipments" to "Active Shipments Card",
                "quick_actions" to "Quick Action Grid",
                "loyalty_rewards" to "Loyalty Rewards Panel"
            )
            sectionLabels.forEach { (key, label) ->
                val isVisible = sections[key] ?: true
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, fontSize = 11.sp, color = AppTextColor)
                    Switch(
                        checked = isVisible,
                        onCheckedChange = { viewModel.toggleDashboardSection(key, it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = if (isLight) Color.White else Obsidian, checkedTrackColor = textHighlight)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text("Card & Slider Customization", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textHighlight)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = heroTitleText,
                onValueChange = { heroTitleText = it; viewModel.updateAdminCardConfig("hero_title", it) },
                label = { Text("Hero Banner Title", fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                textStyle = TextStyle(fontSize = 12.sp, color = AppTextColor)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = heroSubtitleText,
                onValueChange = { heroSubtitleText = it; viewModel.updateAdminCardConfig("hero_subtitle", it) },
                label = { Text("Hero Subtitle Text", fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                textStyle = TextStyle(fontSize = 12.sp, color = AppTextColor)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = bannerImgText,
                onValueChange = { bannerImgText = it; viewModel.updateAdminCardConfig("banner_image", it) },
                label = { Text("Banner Image URL", fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                textStyle = TextStyle(fontSize = 12.sp, color = AppTextColor)
            )

            Spacer(modifier = Modifier.height(14.dp))
            PricingConfigurationComponent(viewModel = viewModel, textHighlight = textHighlight, labelColor = labelColor, isLight = isLight)

            Spacer(modifier = Modifier.height(14.dp))
            Text("Driver System Master Controls", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textHighlight)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.driverMasterControlOverride("broadcast_surge") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = textHighlight, contentColor = if (isLight) Color.White else Obsidian),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("BROADCAST SURGE", fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
                OutlinedButton(
                    onClick = { viewModel.driverMasterControlOverride("force_sync_fleet") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textHighlight),
                    border = BorderStroke(1.dp, textHighlight),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("SYNC FLEET", fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = labelColor.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Sub-Admin User Management Interface
            SubAdminManagementSection(viewModel = viewModel, isLight = isLight, textHighlight = textHighlight, labelColor = labelColor)

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = labelColor.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Secure Admin Activity Log Component
            AdminActivityLogSection(viewModel = viewModel, isLight = isLight, textHighlight = textHighlight, labelColor = labelColor)

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = labelColor.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Bulk Action Delivery Management View
            BulkDeliveryManagementSection(viewModel = viewModel, isLight = isLight, textHighlight = textHighlight, labelColor = labelColor)

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = labelColor.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // User Wallet & Points Management
            UserWalletPointsManagement(viewModel = viewModel, isLight = isLight, textHighlight = textHighlight, labelColor = labelColor)

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = labelColor.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Broadcast Notification Section
            BroadcastNotificationSection(viewModel = viewModel, isLight = isLight, textHighlight = textHighlight, labelColor = labelColor)

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = labelColor.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Service Area Management Section
            ServiceAreaManagementSection(viewModel = viewModel, isLight = isLight, textHighlight = textHighlight, labelColor = labelColor)
        }
    }
}

@Composable
fun UserWalletPointsManagement(
    viewModel: DeliveryViewModel,
    isLight: Boolean,
    textHighlight: Color,
    labelColor: Color
) {
    var userSearch by remember { mutableStateOf("") }
    var foundUserId by remember { mutableStateOf("") }
    var foundUserName by remember { mutableStateOf("") }
    var fundAmount by remember { mutableStateOf("") }
    var pointsAmount by remember { mutableStateOf("") }
    var statusMsg by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("User Wallet & Points", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textHighlight)
            Text("Search by email/UID, credit wallet or set points", fontSize = 9.sp, color = labelColor)
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = userSearch,
            onValueChange = { userSearch = it; statusMsg = "" },
            label = { Text("Search User (email/UID)", fontSize = 10.sp) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            textStyle = TextStyle(fontSize = 12.sp, color = AppTextColor),
            singleLine = true
        )
        Button(
            onClick = {
                if (userSearch.isBlank()) return@Button
                isSearching = true
                statusMsg = ""
                val db = com.example.data.FirebaseManager.firestore
                if (db == null) { statusMsg = "Firestore unavailable"; isSearching = false; return@Button }
                db.collection("users")
                    .whereGreaterThanOrEqualTo("email", userSearch.lowercase())
                    .whereLessThanOrEqualTo("email", userSearch.lowercase() + "\uf8ff")
                    .get()
                    .addOnSuccessListener { snap ->
                        if (snap.documents.isNotEmpty()) {
                            val doc = snap.documents[0]
                            foundUserId = doc.id
                            foundUserName = doc.getString("name") ?: doc.getString("email") ?: "User"
                            statusMsg = "Found: ${foundUserName}"
                        } else {
                            // Try direct UID lookup
                            db.collection("users").document(userSearch).get()
                                .addOnSuccessListener { doc ->
                                    if (doc.exists()) {
                                        foundUserId = doc.id
                                        foundUserName = doc.getString("name") ?: doc.getString("email") ?: "User"
                                        statusMsg = "Found: ${foundUserName}"
                                    } else {
                                        foundUserId = ""
                                        foundUserName = ""
                                        statusMsg = "User not found"
                                    }
                                }
                                .addOnFailureListener { statusMsg = "Lookup failed" }
                        }
                        isSearching = false
                    }
                    .addOnFailureListener { statusMsg = "Search failed"; isSearching = false }
            },
            enabled = userSearch.isNotBlank() && !isSearching,
            colors = ButtonDefaults.buttonColors(containerColor = textHighlight, contentColor = if (isLight) Color.White else Obsidian),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(if (isSearching) "..." else "FIND", fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }

    if (statusMsg.isNotEmpty()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(statusMsg, fontSize = 10.sp, color = if (statusMsg.startsWith("Found")) Gold else Color.Red.copy(alpha = 0.8f))
    }

    if (foundUserId.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Obsidian.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, labelColor.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("User: $foundUserName", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                Text("UID: $foundUserId", fontSize = 8.sp, color = labelColor)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = fundAmount,
                        onValueChange = { fundAmount = it },
                        label = { Text("Wallet Amount (₦)", fontSize = 9.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = TextStyle(fontSize = 12.sp, color = AppTextColor),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            val amt = fundAmount.toDoubleOrNull() ?: return@Button
                            viewModel.adminFundUserWallet(foundUserId, foundUserName, amt) { ok, msg ->
                                statusMsg = if (ok) "✅ $msg" else "❌ $msg"
                                fundAmount = ""
                            }
                        },
                        enabled = fundAmount.toDoubleOrNull() != null && (fundAmount.toDoubleOrNull() ?: 0.0) > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("CREDIT", fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = pointsAmount,
                        onValueChange = { pointsAmount = it },
                        label = { Text("Loyalty Points", fontSize = 9.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = TextStyle(fontSize = 12.sp, color = AppTextColor),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            val pts = pointsAmount.toIntOrNull() ?: return@Button
                            viewModel.adminSetUserPoints(foundUserId, foundUserName, pts) { ok, msg ->
                                statusMsg = if (ok) "✅ $msg" else "❌ $msg"
                                pointsAmount = ""
                            }
                        },
                        enabled = pointsAmount.toIntOrNull() != null,
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("SET", fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun BroadcastNotificationSection(
    viewModel: DeliveryViewModel,
    isLight: Boolean,
    textHighlight: Color,
    labelColor: Color
) {
    var notifTitle by remember { mutableStateOf("") }
    var notifBody by remember { mutableStateOf("") }
    var statusMsg by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    Text("Send Broadcast Notification", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textHighlight)
    Spacer(modifier = Modifier.height(4.dp))
    Text("Push notification to all registered users", fontSize = 9.sp, color = labelColor)
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = notifTitle,
        onValueChange = { notifTitle = it; statusMsg = "" },
        label = { Text("Notification Title", fontSize = 10.sp) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        textStyle = TextStyle(fontSize = 12.sp, color = AppTextColor),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(6.dp))
    OutlinedTextField(
        value = notifBody,
        onValueChange = { notifBody = it; statusMsg = "" },
        label = { Text("Notification Message", fontSize = 10.sp) },
        modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
        shape = RoundedCornerShape(10.dp),
        textStyle = TextStyle(fontSize = 12.sp, color = AppTextColor),
        maxLines = 3
    )
    Spacer(modifier = Modifier.height(6.dp))
    Button(
        onClick = {
            if (notifTitle.isBlank() || notifBody.isBlank()) return@Button
            isSending = true
            statusMsg = ""
            viewModel.adminSendBroadcastNotification(notifTitle, notifBody) { ok, msg ->
                statusMsg = if (ok) "✅ $msg" else "❌ $msg"
                if (ok) { notifTitle = ""; notifBody = "" }
                isSending = false
            }
        },
        enabled = notifTitle.isNotBlank() && notifBody.isNotBlank() && !isSending,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = textHighlight, contentColor = if (isLight) Color.White else Obsidian),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            if (isSending) "SENDING..." else "SEND TO ALL USERS",
            fontSize = 10.sp, fontWeight = FontWeight.Black
        )
    }
    if (statusMsg.isNotEmpty()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(statusMsg, fontSize = 10.sp, color = if (statusMsg.startsWith("✅")) Gold else Color.Red.copy(alpha = 0.8f))
    }
}

@Composable
fun ServiceAreaManagementSection(
    viewModel: DeliveryViewModel,
    isLight: Boolean,
    textHighlight: Color,
    labelColor: Color
) {
    val serviceAreas by viewModel.serviceAreas.collectAsState()
    var newAreaName by remember { mutableStateOf("") }
    var statusMsg by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Service Areas", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textHighlight)
            Text("Manage supported delivery zones", fontSize = 9.sp, color = labelColor)
        }
    }
    Spacer(modifier = Modifier.height(8.dp))

    serviceAreas.forEach { area ->
        Surface(
            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
            shape = RoundedCornerShape(8.dp),
            color = Obsidian.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, labelColor.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(area, fontSize = 11.sp, color = AppTextColor)
                IconButton(
                    onClick = { viewModel.removeServiceArea(area) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(6.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = newAreaName,
            onValueChange = { newAreaName = it; statusMsg = "" },
            label = { Text("Add service area (city/district)", fontSize = 10.sp) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            textStyle = TextStyle(fontSize = 12.sp, color = AppTextColor),
            singleLine = true
        )
        Button(
            onClick = {
                if (newAreaName.isBlank()) return@Button
                viewModel.addServiceArea(newAreaName.trim())
                statusMsg = "Added: ${newAreaName.trim()}"
                newAreaName = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = textHighlight, contentColor = if (isLight) Color.White else Obsidian),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text("ADD", fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
    if (statusMsg.isNotEmpty()) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(statusMsg, fontSize = 9.sp, color = labelColor)
    }
}

@Composable
fun SubAdminManagementSection(
    viewModel: DeliveryViewModel,
    isLight: Boolean,
    textHighlight: Color,
    labelColor: Color
) {
    val subAdmins by viewModel.subAdminUsers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var selectedPermission by remember { mutableStateOf("View Only") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Sub-Admin Team & Permissions", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textHighlight)
            Text("Assign roles: 'View Only' or 'Content Manager'", fontSize = 9.sp, color = labelColor)
        }
        Button(
            onClick = { showAddDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = textHighlight, contentColor = if (isLight) Color.White else Obsidian),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text("+ Add Sub-Admin", fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    subAdmins.forEach { admin ->
        Surface(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(10.dp),
            color = if (isLight) Color.White.copy(alpha = 0.05f) else Obsidian.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, labelColor.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(admin.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                    Text(admin.email, fontSize = 9.sp, color = labelColor)
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = textHighlight.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = admin.permission,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = textHighlight
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val nextPerm = if (admin.permission == "View Only") "Content Manager" else "View Only"
                    OutlinedButton(
                        onClick = { viewModel.updateSubAdminPermission(admin.id, nextPerm) },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Toggle Role", fontSize = 8.sp, color = AppTextColor)
                    }
                    IconButton(
                        onClick = { viewModel.deleteSubAdmin(admin.id) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Sub-Admin Account", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Permission Level:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { selectedPermission = "View Only" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (selectedPermission == "View Only") textHighlight else Charcoal)
                        ) {
                            Text("View Only", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { selectedPermission = "Content Manager" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (selectedPermission == "Content Manager") textHighlight else Charcoal)
                        ) {
                            Text("Content Manager", fontSize = 10.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotBlank() && newEmail.isNotBlank()) {
                        viewModel.addSubAdmin(newName, newEmail, selectedPermission)
                        newName = ""
                        newEmail = ""
                        showAddDialog = false
                    }
                }) {
                    Text("Add Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AdminActivityLogSection(
    viewModel: DeliveryViewModel,
    isLight: Boolean,
    textHighlight: Color,
    labelColor: Color
) {
    val logs by viewModel.adminActivityLogs.collectAsState()

    Column {
        Text("Secure Admin Activity Audit Log", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textHighlight)
        Text("Tracks settings toggles, card visibility, and driver changes", fontSize = 9.sp, color = labelColor)
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(max = 160.dp),
            shape = RoundedCornerShape(10.dp),
            color = if (isLight) Color.White.copy(alpha = 0.05f) else Obsidian.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, labelColor.copy(alpha = 0.2f))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(logs) { log ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("[${log.action}]", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textHighlight)
                                Text(log.details, fontSize = 9.sp, color = AppTextColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text("By ${log.adminName} • ${log.timestamp}", fontSize = 8.sp, color = labelColor)
                        }
                    }
                    HorizontalDivider(color = labelColor.copy(alpha = 0.1f), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun BulkDeliveryManagementSection(
    viewModel: DeliveryViewModel,
    isLight: Boolean,
    textHighlight: Color,
    labelColor: Color
) {
    val parcels by viewModel.parcels.collectAsState()
    var selectedParcelIds by remember { mutableStateOf(setOf<String>()) }
    var targetStatus by remember { mutableStateOf(ParcelStatus.TRANSIT) }
    var targetRider by remember { mutableStateOf("ESD-Rider-882") }

    Column {
        Text("Bulk Delivery Management & Reassignment", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textHighlight)
        Text("Select multiple shipments to update status or reassign driver", fontSize = 9.sp, color = labelColor)
        Spacer(modifier = Modifier.height(8.dp))

        if (parcels.isEmpty()) {
            Text("No active pending deliveries found.", fontSize = 10.sp, color = labelColor)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 180.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(parcels) { parcel ->
                    val isSelected = selectedParcelIds.contains(parcel.id)
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable {
                            selectedParcelIds = if (isSelected) selectedParcelIds - parcel.id else selectedParcelIds + parcel.id
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) textHighlight.copy(alpha = 0.15f) else Charcoal,
                        border = BorderStroke(1.dp, if (isSelected) textHighlight else labelColor.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Shipment #${parcel.id.take(6)} • ${parcel.receiverName}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                                Text("Destination: ${parcel.deliveryAddress}", fontSize = 9.sp, color = labelColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Charcoal
                            ) {
                                Text(
                                    parcel.status.name,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textHighlight,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedParcelIds.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            viewModel.bulkUpdateDeliveryStatus(selectedParcelIds.toList(), targetStatus)
                            selectedParcelIds = emptySet()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = textHighlight, contentColor = if (isLight) Color.White else Obsidian),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text("Bulk Set Status", fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }

                    Button(
                        onClick = {
                            viewModel.bulkReassignDriver(selectedParcelIds.toList(), targetRider, "ESD-Bike-882")
                            selectedParcelIds = emptySet()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = textHighlight),
                        border = BorderStroke(1.dp, textHighlight),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text("Bulk Reassign Driver", fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun PricingConfigurationComponent(
    viewModel: DeliveryViewModel,
    textHighlight: Color,
    labelColor: Color,
    isLight: Boolean
) {
    val baseFareVal by viewModel.baseFare.collectAsState()
    val perKgVal by viewModel.perKgRate.collectAsState()
    val expressVal by viewModel.expressSurcharge.collectAsState()
    val surgeVal by viewModel.surgeMultiplier.collectAsState()

    var baseInput by remember(baseFareVal) { mutableStateOf(baseFareVal.toString()) }
    var perKgInput by remember(perKgVal) { mutableStateOf(perKgVal.toString()) }
    var expressInput by remember(expressVal) { mutableStateOf(expressVal.toString()) }
    var surgeInput by remember(surgeVal) { mutableStateOf(surgeVal.toString()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("PricingConfiguration: Global Pricing Logic", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textHighlight)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = baseInput,
            onValueChange = {
                baseInput = it
                val b = it.toDoubleOrNull() ?: 4500.0
                val k = perKgInput.toDoubleOrNull() ?: 250.0
                val e = expressInput.toDoubleOrNull() ?: 1500.0
                val s = surgeInput.toDoubleOrNull() ?: 1.25
                viewModel.updatePricingConfig(b, k, e, s)
            },
            label = { Text("Base Fare (₦)", fontSize = 10.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            textStyle = TextStyle(fontSize = 12.sp, color = AppTextColor)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = perKgInput,
            onValueChange = {
                perKgInput = it
                val b = baseInput.toDoubleOrNull() ?: 4500.0
                val k = it.toDoubleOrNull() ?: 250.0
                val e = expressInput.toDoubleOrNull() ?: 1500.0
                val s = surgeInput.toDoubleOrNull() ?: 1.25
                viewModel.updatePricingConfig(b, k, e, s)
            },
            label = { Text("Per KG Rate (₦)", fontSize = 10.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            textStyle = TextStyle(fontSize = 12.sp, color = AppTextColor)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = expressInput,
            onValueChange = {
                expressInput = it
                val b = baseInput.toDoubleOrNull() ?: 4500.0
                val k = perKgInput.toDoubleOrNull() ?: 250.0
                val e = it.toDoubleOrNull() ?: 1500.0
                val s = surgeInput.toDoubleOrNull() ?: 1.25
                viewModel.updatePricingConfig(b, k, e, s)
            },
            label = { Text("Express Surcharge (₦)", fontSize = 10.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            textStyle = TextStyle(fontSize = 12.sp, color = AppTextColor)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = surgeInput,
            onValueChange = {
                surgeInput = it
                val b = baseInput.toDoubleOrNull() ?: 4500.0
                val k = perKgInput.toDoubleOrNull() ?: 250.0
                val e = expressInput.toDoubleOrNull() ?: 1500.0
                val s = it.toDoubleOrNull() ?: 1.25
                viewModel.updatePricingConfig(b, k, e, s)
            },
            label = { Text("Peak Surge Multiplier", fontSize = 10.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            textStyle = TextStyle(fontSize = 12.sp, color = AppTextColor)
        )
    }
}
