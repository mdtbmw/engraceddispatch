package com.esdispatch.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esdispatch.data.Parcel
import com.esdispatch.data.ParcelStatus
import com.esdispatch.data.ShiftRoster
import com.esdispatch.data.OfflineSyncQueue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.esdispatch.ui.theme.*
import com.esdispatch.ui.components.ScreenHeader
import com.esdispatch.ui.components.BottomNav
import com.esdispatch.ui.components.SupportButton
import com.esdispatch.ui.components.SupportDialog
import com.esdispatch.ui.components.PinInputField
import com.esdispatch.viewmodel.DeliveryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.geometry.Offset
import androidx.compose.animation.core.animate
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import com.esdispatch.ui.components.QuiltedBackground
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.zIndex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderDashboardScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark

    // State collections
    val bikeNumber by viewModel.bikeNumber.collectAsState()
    val riderAssignments by viewModel.riderAssignments.collectAsState()
    val availableDeliveries by viewModel.availableDeliveries.collectAsState()
    val scannedRiderParcel by viewModel.scannedRiderParcel.collectAsState()
    val totalEarned by viewModel.totalEarned.collectAsState()
    val totalTipsEarned by viewModel.totalTipsEarned.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val photoUrl by viewModel.photoUrl.collectAsState()
    val isOnlineState by viewModel.isOnline.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    val firstName = remember(userName) { userName.trim().split(" ").firstOrNull() ?: userName }

    var selectedFilter by remember { mutableStateOf("Available") } // "Available", "Active", "Delivered", "All"
    var selectedParcelForUpdate by remember { mutableStateOf<Parcel?>(null) }
    var selectedParcelForWaybill by remember { mutableStateOf<Parcel?>(null) }
    var showUpdateBottomSheet by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }

    val currentAttendanceStatus by viewModel.currentAttendanceStatus.collectAsState()
    val vehicleInspections by viewModel.vehicleInspectionList.collectAsState()
    val expenseClaims by viewModel.expenseClaimList.collectAsState()
    val shiftRosters by viewModel.shiftRosterList.collectAsState()
    val offlineSyncQueue by viewModel.offlineSyncQueueList.collectAsState()

    var showInspectionDialog by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showRosterDialog by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var showBatchRouteDialog by remember { mutableStateOf(false) }
    var showGeofenceDialog by remember { mutableStateOf(false) }
    var showIncidentDialog by remember { mutableStateOf(false) }
    var showBonusDialog by remember { mutableStateOf(false) }
    var showMaintenanceDialog by remember { mutableStateOf(false) }
    var showPermissionReadinessDialog by remember { mutableStateOf(false) }

    // Automatically trigger update status sheet when parcel is scanned
    LaunchedEffect(scannedRiderParcel) {
        scannedRiderParcel?.let { parcel ->
            selectedParcelForUpdate = parcel
            showUpdateBottomSheet = true
            viewModel.setScannedRiderParcel(null)
        }
    }

    // Auto-dismiss bottom sheet if the currently selected parcel was cancelled
    LaunchedEffect(riderAssignments) {
        val current = selectedParcelForUpdate
        if (current != null) {
            val latest = riderAssignments.find { it.id == current.id }
            if (latest == null || latest.status == ParcelStatus.CANCELLED) {
                showUpdateBottomSheet = false
                selectedParcelForUpdate = null
            }
        }
    }

    val activeCount = remember(riderAssignments) {
        riderAssignments.filter {
            it.status != ParcelStatus.DELIVERED &&
            it.status != ParcelStatus.CANCELLED &&
            it.status != ParcelStatus.RETURNED &&
            it.status != ParcelStatus.HANDOVER_VERIFIED
        }.size
    }

    // Automatically switch to "Active" tab when active assignments exist so courier is never stuck on empty Available
    LaunchedEffect(activeCount) {
        if (activeCount > 0 && selectedFilter == "Available") {
            selectedFilter = "Active"
        }
    }
    val deliveredCount = remember(riderAssignments) {
        riderAssignments.filter { it.status == ParcelStatus.DELIVERED || it.status == ParcelStatus.HANDOVER_VERIFIED }.size
    }

    val todayDeliveredCount by viewModel.todayDeliveredCount.collectAsState()
    val dailyTargetRides by viewModel.dailyRiderTargetRides.collectAsState()
    val dailyTargetPoints by viewModel.dailyRiderTargetPoints.collectAsState()
    val pointNairaVal by viewModel.pointNairaValue.collectAsState()
    val fleetRank by viewModel.riderFleetRank.collectAsState()

    val arrivedParcel = remember(riderAssignments) {
        riderAssignments.firstOrNull { 
            (it.status == ParcelStatus.ARRIVED || it.status == ParcelStatus.HANDOVER_VERIFIED) &&
            it.status != ParcelStatus.CANCELLED
        }
    }

    val density = LocalDensity.current
    val maxScrollDistancePx = with(density) { 235.dp.toPx() }
    val maxOverscrollPx = 120f
    val refreshThreshold = 80f

    var scrollOffset by remember { mutableFloatStateOf(0f) }
    var isRefreshing by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            viewModel.refreshAllData()
            kotlinx.coroutines.delay(600)
            isRefreshing = false
        }
    }

    val listState = rememberLazyListState()

    val isAtTop = remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }

    val progress by remember {
        derivedStateOf { (scrollOffset / maxScrollDistancePx).coerceIn(0f, 1f) }
    }

    val headerHeightDp = remember(progress) {
        val minHeight = 115.dp
        val maxHeight = 440.dp
        minHeight + (maxHeight - minHeight) * (1f - progress)
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                return if (delta < 0) {
                    if (scrollOffset < 0f) {
                        val newOffset = (scrollOffset - delta).coerceAtMost(0f)
                        val consumed = scrollOffset - newOffset
                        scrollOffset = newOffset
                        Offset(0f, consumed)
                    } else if (scrollOffset < maxScrollDistancePx) {
                        val newOffset = (scrollOffset - delta).coerceAtMost(maxScrollDistancePx)
                        val consumed = scrollOffset - newOffset
                        scrollOffset = newOffset
                        Offset(0f, consumed)
                    } else {
                        Offset.Zero
                    }
                } else {
                    Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                if (delta > 0) {
                    if (scrollOffset > 0f) {
                        val newOffset = (scrollOffset - delta).coerceAtLeast(0f)
                        val consumedOffset = scrollOffset - newOffset
                        scrollOffset = newOffset
                        return Offset(0f, consumedOffset)
                    } else if (scrollOffset <= 0f && source == NestedScrollSource.UserInput) {
                        val newOffset = (scrollOffset - delta).coerceAtLeast(-maxOverscrollPx)
                        val consumedOffset = scrollOffset - newOffset
                        scrollOffset = newOffset
                        return Offset(0f, consumedOffset)
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (scrollOffset <= -refreshThreshold) {
                    if (!isRefreshing) {
                        isRefreshing = true
                        viewModel.refreshAllData()
                    }
                }
                if (scrollOffset < 0f) {
                    animate(scrollOffset, 0f) { value, _ ->
                        scrollOffset = value
                    }
                }
                return super.onPreFling(available)
            }
        }
    }

    val baseAssignments = remember(riderAssignments, availableDeliveries, selectedFilter, isOnlineState, activeCount) {
        when (selectedFilter) {
            "Available" -> {
                if (!isOnlineState || activeCount > 0) {
                    emptyList()
                } else {
                    // Sort available deliveries closest to the rider first
                    availableDeliveries.sortedBy { p ->
                        val lat = p.pickupLat ?: 6.3350
                        val lng = p.pickupLng ?: 5.6037
                        viewModel.calculateDistanceToRider(lat, lng)
                    }
                }
            }
            "Active" -> riderAssignments.filter {
                it.status != ParcelStatus.DELIVERED &&
                it.status != ParcelStatus.PENDING &&
                it.status != ParcelStatus.CANCELLED &&
                it.status != ParcelStatus.RETURNED &&
                it.status != ParcelStatus.HANDOVER_VERIFIED
            }
            "Delivered" -> riderAssignments.filter { it.status == ParcelStatus.DELIVERED || it.status == ParcelStatus.HANDOVER_VERIFIED }
            else -> riderAssignments
        }
    }

    LaunchedEffect(activeCount) {
        if (activeCount == 0 && selectedFilter == "Active") {
            selectedFilter = "Available"
        }
    }

    val filteredAssignments = remember(baseAssignments, searchQuery) {
        if (searchQuery.isBlank()) {
            baseAssignments
        } else {
            val q = searchQuery.trim()
            baseAssignments.filter {
                it.id.contains(q, ignoreCase = true) ||
                it.itemName.contains(q, ignoreCase = true) ||
                it.receiverName.contains(q, ignoreCase = true) ||
                it.receiverPhone.contains(q, ignoreCase = true) ||
                it.deliveryAddress.contains(q, ignoreCase = true) ||
                it.senderName.contains(q, ignoreCase = true)
            }
        }
    }

    Scaffold(
        containerColor = LuxuryBlack,
        bottomBar = { BottomNav(currentScreen = "Dashboard", onNavigate = onNavigate, activeViewMode = "rider", userRole = userRole) },
        floatingActionButton = {
            SupportButton(onClick = { showSupportDialog = true })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LuxuryBlack)
        ) {
            // Pull-to-refresh tactile indicator
            if (isRefreshing || scrollOffset < -10f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 10.dp)
                        .zIndex(50f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Charcoal.copy(alpha = 0.95f))
                            .border(BorderStroke(1.dp, Gold.copy(alpha = 0.6f)), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Gold,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = if (isRefreshing) "Syncing fleet dispatches..." else "Pull to refresh dispatches",
                            color = Gold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .nestedScroll(nestedScrollConnection),
                contentPadding = PaddingValues(top = 115.dp, bottom = 140.dp)
            ) {
                // Top spacer keeping content beneath collapsing header
                item {
                    Spacer(modifier = Modifier.height(325.dp * (1f - progress)))
                }

                // Segmented Filters bar
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Charcoal)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Available", "Active", "Delivered", "All").forEach { tab ->
                            val isSelected = selectedFilter == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Gold else Color.Transparent)
                                    .clickable { selectedFilter = tab },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tab,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) Obsidian else TextGray
                                    )
                                    if (tab == "Active" && activeCount > 0) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Obsidian else Gold)
                                                .breathingPulse(active = true, minScale = 0.8f, maxScale = 1.3f, durationMs = 1200)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Daily Mission Target Progress Card
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Charcoal,
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Gold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "DAILY MISSION TARGET",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isDark) Gold else Obsidian,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Gold.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "RANK: $fleetRank",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Gold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "$todayDeliveredCount / $dailyTargetRides Trips Completed",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = AppTextColor
                                    )
                                    Text(
                                        text = if (todayDeliveredCount >= dailyTargetRides) "Daily target achieved! Bonus unlocked." else "${(dailyTargetRides - todayDeliveredCount).coerceAtLeast(0)} more trips to unlock bonus",
                                        fontSize = 11.sp,
                                        color = TextGray
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Obsidian
                                ) {
                                    Text(
                                        text = "+$dailyTargetPoints PTS (₦${dailyTargetPoints * pointNairaVal})",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Gold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val targetProgress = (todayDeliveredCount.toFloat() / dailyTargetRides.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { targetProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Gold,
                                trackColor = Obsidian
                            )
                        }
                    }
                }

                // Multi-Stop Batch Trip Manifest Card (shown when 2+ active dispatches assigned)
                if (filteredAssignments.size >= 2 && selectedFilter != "Delivered") {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = Charcoal,
                            border = BorderStroke(1.2.dp, Gold.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(
                                            imageVector = Icons.Default.AltRoute,
                                            contentDescription = "Batch Manifest",
                                            tint = Gold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "MULTI-STOP TRIP MANIFEST",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isDark) Gold else Obsidian
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Gold.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "${filteredAssignments.size} STOPS ACTIVE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Gold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Optimal dispatch sequence active. Follow order stops below to minimize transit distance and fuel consumption.",
                                    fontSize = 10.sp,
                                    color = TextGray,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                // Deliveries List or Empty state
                if (filteredAssignments.isEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = Charcoal,
                            border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.3f) else Slate)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val emptyIcon = if (selectedFilter == "Available" && !isOnlineState) {
                                    Icons.Filled.PowerSettingsNew
                                } else if (selectedFilter == "Available" && activeCount > 0) {
                                    Icons.Filled.Navigation
                                } else {
                                    Icons.Default.DirectionsBike
                                }

                                Icon(
                                    imageVector = emptyIcon,
                                    contentDescription = "Status",
                                    tint = if (selectedFilter == "Available" && !isOnlineState) Gold else TextGray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                val emptyTitle = when {
                                    searchQuery.isNotBlank() -> "No Matching Shipments"
                                    selectedFilter == "Available" && !isOnlineState -> "Unavailable for Dispatch"
                                    selectedFilter == "Available" && activeCount > 0 -> "Active Mission In Progress"
                                    selectedFilter == "Available" -> "No Dispatches Available"
                                    selectedFilter == "Active" -> "No Active Deliveries"
                                    selectedFilter == "Delivered" -> "No Completed Deliveries Yet"
                                    else -> "No Shipments Found"
                                }

                                val emptySubtitle = when {
                                    searchQuery.isNotBlank() -> "No parcels matched '$searchQuery'. Try checking the tracking ID or recipient phone number."
                                    selectedFilter == "Available" && !isOnlineState -> "Turn your dispatch availability ON to receive incoming customer orders and proximity match dispatches."
                                    selectedFilter == "Available" && activeCount > 0 -> "Focus on your current delivery ($activeCount active stop). Complete this delivery before accepting new customer dispatches."
                                    selectedFilter == "Available" -> "All nearby parcels are currently assigned. New customer booking dispatches will appear here in real-time."
                                    selectedFilter == "Active" -> "Accept an available dispatch or wait for dispatcher assignment to begin a mission."
                                    selectedFilter == "Delivered" -> "Completed delivery logs and proof-of-delivery records will be recorded here."
                                    else -> "Wait for the admin dispatcher to assign logistics deliveries to your profile."
                                }

                                Text(
                                    text = emptyTitle,
                                    color = AppTextColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = emptySubtitle,
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )

                                if (selectedFilter == "Available" && !isOnlineState) {
                                    Spacer(modifier = Modifier.height(18.dp))
                                    Button(
                                        onClick = { viewModel.setRiderOnlineStatus(true) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Gold,
                                            contentColor = Obsidian
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text("Turn Availability ON", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                } else if (selectedFilter == "Available" && activeCount > 0) {
                                    Spacer(modifier = Modifier.height(18.dp))
                                    Button(
                                        onClick = { selectedFilter = "Active" },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Gold,
                                            contentColor = Obsidian
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text("View Active Mission", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(filteredAssignments, key = { it.id }) { parcel ->
                        val distanceKm = if (selectedFilter == "Available" || parcel.status == ParcelStatus.PENDING) {
                            val lat = parcel.pickupLat ?: 6.3350
                            val lng = parcel.pickupLng ?: 5.6037
                            viewModel.calculateDistanceToRider(lat, lng)
                        } else null

                        RiderParcelCard(
                            parcel = parcel,
                            distanceKm = distanceKm,
                            onUpdateStatus = {
                                selectedParcelForUpdate = parcel
                                showUpdateBottomSheet = true
                            },
                            onViewWaybill = {
                                selectedParcelForWaybill = parcel
                            }
                        )
                    }
                }
            }

            // Fixed Collapsing Header Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeightDp)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Obsidian)
            ) {
                QuiltedBackground(
                    modifier = Modifier.matchParentSize(),
                    lineColor = Color.White.copy(alpha = 0.04f)
                ) {}

                // 1. Expanded Header Content
                val expandedAlpha = (1f - progress / 0.6f).coerceIn(0f, 1f)
                if (expandedAlpha > 0f) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = expandedAlpha
                                scaleX = 0.92f + 0.08f * expandedAlpha
                                scaleY = 0.92f + 0.08f * expandedAlpha
                                translationY = -scrollOffset * 0.5f
                            }
                            .statusBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        // Top Bar: Active Indicator (normal text + motorcycle icon + yellow pulsing beacon) & Duty Switch (ON DUTY in Gold #FFB800)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Active Ride Indicator (Normal text with motorcycle icon and yellow pulsing dot)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedFilter = "Active" }
                                    .padding(vertical = 4.dp, horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBike,
                                    contentDescription = null,
                                    tint = if (activeCount > 0) Gold else TextGray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (activeCount > 0) "$activeCount Active Dispatches" else "0 Active Dispatches",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeCount > 0) Color.White else TextGray
                                )
                            }

                            // Duty Status Switch
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "DISPATCH AVAILABILITY",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextGray,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = if (isOnlineState) "AVAILABLE" else "UNAVAILABLE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isOnlineState) Gold else TextGray,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Switch(
                                    checked = isOnlineState,
                                    onCheckedChange = { targetState ->
                                        if (targetState) {
                                            val hasFineLocation = androidx.core.content.ContextCompat.checkSelfPermission(
                                                context,
                                                android.Manifest.permission.ACCESS_FINE_LOCATION
                                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                            if (!hasFineLocation) {
                                                showPermissionReadinessDialog = true
                                            } else {
                                                viewModel.setRiderOnlineStatus(true)
                                            }
                                        } else {
                                            viewModel.setRiderOnlineStatus(false)
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Obsidian,
                                        checkedTrackColor = Gold,
                                        uncheckedThumbColor = TextGray,
                                        uncheckedTrackColor = Charcoal
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(36.dp))

                        // Greeting Text
                        Column {
                            Text(
                                text = "Good day,",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$firstName!",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                WavingHand(isAtTopOrActive = isAtTop.value, fontSize = 30.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "SMILES DISPATCH RIDER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold,
                                letterSpacing = 2.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Search Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .background(Charcoal, shape = RoundedCornerShape(24.dp))
                                .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = Gold,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Search
                                ),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        if (filteredAssignments.isNotEmpty()) {
                                            selectedParcelForUpdate = filteredAssignments.first()
                                            showUpdateBottomSheet = true
                                        }
                                    }
                                ),
                                cursorBrush = SolidColor(Gold),
                                modifier = Modifier.weight(1f),
                                decorationBox = { innerTextField ->
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search manifest (ID, recipient, item)...",
                                                color = TextGray,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = TextGray,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { searchQuery = "" }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            val isPodActive = arrivedParcel != null
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isPodActive) Gold.copy(alpha = 0.15f) else Charcoal)
                                    .border(
                                        width = 1.dp,
                                        color = if (isPodActive) Gold else Slate,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable(enabled = isPodActive) {
                                        arrivedParcel?.let {
                                            onNavigate("ProofOfDelivery/${it.id}")
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DriveFileRenameOutline,
                                    contentDescription = "Handover / POD",
                                    tint = if (isPodActive) Gold else TextGray.copy(alpha = 0.35f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Dual Hero Action Cards: Left = Courier Tips, Right = Completed Deliveries
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Left: Courier Tips (navigates to Tip Wallet)
                            Surface(
                                onClick = { onNavigate("Wallet") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(80.dp),
                                shape = RoundedCornerShape(24.dp),
                                color = Charcoal,
                                shadowElevation = 0.dp,
                                border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Gold),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AnimatedHugeIcon(
                                            icon = Hugeicons.Solid.Wallet,
                                            contentDescription = "Tips",
                                            tint = Obsidian,
                                            size = 18.dp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(verticalArrangement = Arrangement.Center) {
                                        Text(
                                            text = "Courier Tips",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextGray,
                                            lineHeight = 13.sp,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "₦${String.format("%,.2f", totalTipsEarned)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = AppTextColor,
                                            lineHeight = 16.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            // Right: Completed Deliveries (filters manifest to Completed)
                            Surface(
                                onClick = { selectedFilter = "Delivered" },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(80.dp),
                                shape = RoundedCornerShape(24.dp),
                                color = Charcoal,
                                shadowElevation = 0.dp,
                                border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Gold),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Completed",
                                            tint = Obsidian,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(verticalArrangement = Arrangement.Center) {
                                        Text(
                                            text = "Deliveries",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextGray,
                                            lineHeight = 13.sp,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "$deliveredCount Completed",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = AppTextColor,
                                            lineHeight = 16.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Collapsed Content
                val collapsedAlpha = ((progress - 0.4f) / 0.6f).coerceIn(0f, 1f)
                if (collapsedAlpha > 0f) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = collapsedAlpha
                                translationY = (1f - collapsedAlpha) * 12.dp.toPx()
                            }
                            .statusBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBike,
                                contentDescription = null,
                                tint = if (activeCount > 0) Gold else TextGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Hello $firstName! ",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                WavingHand(isAtTopOrActive = true, fontSize = 17.sp)
                            }
                        }

                        // Duty Switch in Collapsed Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isOnlineState) "Available" else "Unavailable",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isOnlineState) Gold else TextGray
                            )
                            Switch(
                                checked = isOnlineState,
                                onCheckedChange = { targetState ->
                                    if (targetState) {
                                        val hasFineLocation = androidx.core.content.ContextCompat.checkSelfPermission(
                                            context,
                                            android.Manifest.permission.ACCESS_FINE_LOCATION
                                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                        if (!hasFineLocation) {
                                            showPermissionReadinessDialog = true
                                        } else {
                                            viewModel.setRiderOnlineStatus(true)
                                        }
                                    } else {
                                        viewModel.setRiderOnlineStatus(false)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Obsidian,
                                    checkedTrackColor = Gold,
                                    uncheckedThumbColor = TextGray,
                                    uncheckedTrackColor = Charcoal
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Bottom Sheet for status update
    if (showUpdateBottomSheet && selectedParcelForUpdate != null) {
        val parcel = selectedParcelForUpdate!!
        ModalBottomSheet(
            onDismissRequest = { showUpdateBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Charcoal,
            contentColor = AppTextColor,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            RiderUpdateBottomSheetContent(
                parcel = parcel,
                viewModel = viewModel,
                onDismiss = { showUpdateBottomSheet = false },
                onNavigateToPOD = onNavigate
            )
        }
    }

    // Bottom Sheet for digital waybill
    if (selectedParcelForWaybill != null) {
        RiderWaybillBottomSheet(
            parcel = selectedParcelForWaybill!!,
            onDismiss = { selectedParcelForWaybill = null }
        )
    }

    if (showSupportDialog) {
        SupportDialog(onDismiss = { showSupportDialog = false })
    }

    if (showInspectionDialog) {
        VehicleInspectionDialog(viewModel = viewModel, onDismiss = { showInspectionDialog = false })
    }
    if (showExpenseDialog) {
        ExpenseClaimDialog(viewModel = viewModel, onDismiss = { showExpenseDialog = false })
    }
    if (showRosterDialog) {
        ShiftRosterDialog(viewModel = viewModel, shiftRosters = shiftRosters, onDismiss = { showRosterDialog = false })
    }
    if (showSyncDialog) {
        OfflineSyncDialog(viewModel = viewModel, offlineSyncQueue = offlineSyncQueue, onDismiss = { showSyncDialog = false })
    }
    if (showBatchRouteDialog) {
        BatchRouteOptimizationDialog(viewModel = viewModel, onDismiss = { showBatchRouteDialog = false })
    }
    if (showGeofenceDialog) {
        GeofenceTelemetryDialog(viewModel = viewModel, onDismiss = { showGeofenceDialog = false })
    }
    if (showIncidentDialog) {
        IncidentReportDialog(viewModel = viewModel, onDismiss = { showIncidentDialog = false })
    }
    if (showBonusDialog) {
        BonusCalculatorDialog(viewModel = viewModel, deliveredCount = deliveredCount, onDismiss = { showBonusDialog = false })
    }
    if (showMaintenanceDialog) {
        VehicleMaintenanceDialog(viewModel = viewModel, bikeNumber = bikeNumber, onDismiss = { showMaintenanceDialog = false })
    }
    if (showPermissionReadinessDialog) {
        RiderPermissionReadinessDialog(
            onDismiss = {
                showPermissionReadinessDialog = false
                val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (granted) {
                    viewModel.setRiderOnlineStatus(true)
                }
            }
        )
    }
}

@Composable
fun RiderParcelCard(
    parcel: Parcel,
    distanceKm: Double? = null,
    onUpdateStatus: () -> Unit,
    onViewWaybill: () -> Unit = {}
) {
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark
    val innerBgColor = if (isDark) Color(0xFF1D1D1D) else GoldenWhiteLight
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = androidx.compose.ui.platform.LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(32.dp),
        color = AppSurface,
        border = BorderStroke(
            1.dp,
            when {
                parcel.exceptionType.isNotEmpty() -> Color(0xFFEF5350).copy(alpha = 0.6f)
                parcel.status == ParcelStatus.RESERVED_NEXT -> Color(0xFF7C4DFF).copy(alpha = 0.5f)
                isDark -> BorderDark
                else -> Slate
            }
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Proximity Vicinity Badge for nearby / same location dispatches
            if (distanceKm != null) {
                val isImmediateVicinity = distanceKm <= 0.5
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isImmediateVicinity) SuccessGreen.copy(alpha = 0.15f) else Gold.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, if (isImmediateVicinity) SuccessGreen.copy(alpha = 0.5f) else Gold.copy(alpha = 0.35f)),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isImmediateVicinity) Icons.Default.LocationOn else Icons.Default.NearMe,
                            contentDescription = null,
                            tint = if (isImmediateVicinity) SuccessGreen else Gold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val distanceLabel = if (isImmediateVicinity) {
                            if (distanceKm < 0.1) "AT YOUR CURRENT LOCATION • IMMEDIATE PICKUP" else "${String.format("%.0f", distanceKm * 1000)}m AWAY • SAME LOCATION VICINITY"
                        } else {
                            "${String.format("%.1f", distanceKm)} km away • Fast Pickup"
                        }
                        Text(
                            text = distanceLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isImmediateVicinity) SuccessGreen else Gold
                        )
                    }
                }
            }

            // Batch Stop Hierarchy Badge
            if (parcel.isBatch) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Gold.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = Gold, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "BATCH STOP ${parcel.batchItemIndex} OF ${parcel.batchTotalItems} • ${parcel.itemName.uppercase()}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Gold
                        )
                    }
                }
            }

            // Mission Hierarchy Badge
            if (parcel.status == ParcelStatus.RESERVED_NEXT) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF7C4DFF).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.35f)),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFB388FF), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("NEXT RESERVED MISSION • In Queue for Pickup", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFFB388FF))
                    }
                }
            } else if (parcel.status != ParcelStatus.PENDING && parcel.status != ParcelStatus.DELIVERED && parcel.status != ParcelStatus.CANCELLED) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Gold.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.25f)),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Gold))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("CURRENT ACTIVE MISSION", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Gold)
                    }
                }
            }

            // Field Exception Indicator
            if (parcel.exceptionType.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFD32F2F).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "FIELD EXCEPTION: ${parcel.exceptionType.replace('_', ' ')}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFF8A80)
                            )
                            if (parcel.exceptionReason.isNotEmpty()) {
                                Text(
                                    text = parcel.exceptionReason,
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(innerBgColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBike,
                            contentDescription = "Shipment",
                            tint = Gold,
                            modifier = Modifier.size(24.dp).align(Alignment.Center)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ID: #${parcel.id.take(8).uppercase()}",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = AppTextColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF2C2C2C) else BorderLight)
                                    .clickable {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(parcel.id))
                                        Toast.makeText(context, "Tracking ID copied!", Toast.LENGTH_SHORT).show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = if (isDark) Gold else Obsidian,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                        Text(
                            text = parcel.itemName,
                            fontSize = 11.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDark) Color(0xFF2C2C2C) else GoldenWhiteLight)
                            .border(BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.3f) else Slate))
                            .clickable { onViewWaybill() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "Waybill",
                            tint = if (isDark) Gold else Obsidian,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (parcel.status) {
                                    ParcelStatus.PENDING -> Gold.copy(alpha = 0.15f)
                                    ParcelStatus.QUEUED -> Color(0xFF0288D1).copy(alpha = 0.15f)
                                    ParcelStatus.RESERVED_NEXT -> Color(0xFF7C4DFF).copy(alpha = 0.15f)
                                    ParcelStatus.ASSIGNED -> Gold.copy(alpha = 0.15f)
                                    ParcelStatus.PICKED_UP -> Color(0xFF3F51B5).copy(alpha = 0.15f)
                                    ParcelStatus.TRANSIT -> Color(0xFF00ACC1).copy(alpha = 0.15f)
                                    ParcelStatus.OUT_FOR_DELIVERY -> WarningOrange.copy(alpha = 0.15f)
                                    ParcelStatus.ARRIVED -> Color(0xFF00897B).copy(alpha = 0.15f)
                                    ParcelStatus.HANDOVER_VERIFIED -> SuccessGreen.copy(alpha = 0.15f)
                                    ParcelStatus.DELIVERED -> SuccessGreen.copy(alpha = 0.15f)
                                    ParcelStatus.CANCELLED -> Color.Red.copy(alpha = 0.15f)
                                    else -> SuccessGreen.copy(alpha = 0.15f)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when (parcel.status) {
                                ParcelStatus.PENDING -> "AVAILABLE"
                                ParcelStatus.QUEUED -> "QUEUED"
                                ParcelStatus.RESERVED_NEXT -> "RESERVED NEXT"
                                ParcelStatus.ASSIGNED -> "ASSIGNED"
                                ParcelStatus.PICKED_UP -> "PICKED UP"
                                ParcelStatus.TRANSIT -> "IN TRANSIT"
                                ParcelStatus.OUT_FOR_DELIVERY -> "OUT FOR DELIVERY"
                                ParcelStatus.ARRIVED -> "ARRIVED"
                                ParcelStatus.HANDOVER_VERIFIED -> "VERIFIED"
                                ParcelStatus.DELIVERED -> "DELIVERED"
                                ParcelStatus.CANCELLED -> "CANCELLED"
                                else -> "UNKNOWN"
                            },
                            color = when (parcel.status) {
                                ParcelStatus.PENDING -> Gold
                                ParcelStatus.QUEUED -> Color(0xFF29B6F6)
                                ParcelStatus.RESERVED_NEXT -> Color(0xFFB388FF)
                                ParcelStatus.ASSIGNED -> Gold
                                ParcelStatus.PICKED_UP -> Color(0xFF7986CB)
                                ParcelStatus.TRANSIT -> Color(0xFF26C6DA)
                                ParcelStatus.OUT_FOR_DELIVERY -> WarningOrange
                                ParcelStatus.ARRIVED -> Color(0xFF00897B)
                                ParcelStatus.HANDOVER_VERIFIED -> SuccessGreen
                                ParcelStatus.DELIVERED -> SuccessGreen
                                ParcelStatus.CANCELLED -> Color.Red
                                else -> TextGray
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pickup & Delivery route details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Gold)
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(if (isDark) BorderDark else Slate)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PICKUP FROM: ${parcel.pickupAddress}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppTextColor,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "DELIVER TO: ${parcel.deliveryAddress}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppTextColor,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recipient detail and action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "RECIPIENT", color = TextGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text(text = parcel.receiverName, color = AppTextColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                if (parcel.status != ParcelStatus.DELIVERED && parcel.status != ParcelStatus.CANCELLED) {
                    Button(
                        onClick = onUpdateStatus,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (parcel.status) {
                                ParcelStatus.PENDING -> Gold
                                ParcelStatus.ASSIGNED -> Gold
                                ParcelStatus.OUT_FOR_DELIVERY -> SuccessGreen
                                else -> Gold
                            },
                            contentColor = Obsidian
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .tactilePress(scaleDown = 0.94f) { onUpdateStatus() }
                    ) {
                        val currentRiderUid = com.esdispatch.data.FirebaseManager.auth?.currentUser?.uid ?: ""
                        val isAssignedToCurrentRider = (parcel.riderId.isNotBlank() && parcel.riderId == currentRiderUid) ||
                            (parcel.driverId.isNotBlank() && parcel.driverId == currentRiderUid) ||
                            (parcel.reservedRiderId.isNotBlank() && parcel.reservedRiderId == currentRiderUid)
                        Text(
                            text = when {
                                parcel.status == ParcelStatus.PENDING && isAssignedToCurrentRider && parcel.isBatch -> "PICK UP ${parcel.itemName.ifBlank { "ITEM" }.uppercase()}"
                                parcel.status == ParcelStatus.PENDING && isAssignedToCurrentRider -> "CONFIRM PICKUP"
                                parcel.status == ParcelStatus.PENDING -> "ACCEPT DISPATCH"
                                parcel.status == ParcelStatus.ASSIGNED && parcel.isBatch -> "PICK UP ${parcel.itemName.ifBlank { "ITEM" }.uppercase()}"
                                parcel.status == ParcelStatus.ASSIGNED -> "CONFIRM PICKUP"
                                parcel.status == ParcelStatus.PICKED_UP -> "START TRANSIT"
                                parcel.status == ParcelStatus.TRANSIT || parcel.status == ParcelStatus.OUT_FOR_DELIVERY -> "MARK ARRIVED"
                                parcel.status == ParcelStatus.ARRIVED -> "ENTER PIN & DELIVER"
                                else -> "VIEW DETAILS"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = SuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Delivered & Settled",
                            fontSize = 12.sp,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RiderUpdateBottomSheetContent(
    parcel: Parcel,
    viewModel: DeliveryViewModel,
    onDismiss: () -> Unit,
    onNavigateToPOD: (String) -> Unit
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark
    var isSubmitting by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }

    LaunchedEffect(parcel.status) {
        if (parcel.status == ParcelStatus.CANCELLED) {
            Toast.makeText(context, "This shipment was cancelled.", Toast.LENGTH_LONG).show()
            onDismiss()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Update Shipment Status",
            color = AppTextColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Tracking ID: #${parcel.id.take(8).uppercase()}",
            color = TextGray,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp)
        )

        if (parcel.status != ParcelStatus.PENDING && parcel.status != ParcelStatus.DELIVERED && parcel.status != ParcelStatus.CANCELLED) {
            var showRiderChat by remember { mutableStateOf(false) }

            Button(
                onClick = { showRiderChat = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Charcoal,
                    contentColor = Gold
                ),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "Chat", tint = Gold)
                    Text("CHAT WITH RECIPIENT", fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 0.5.sp)
                }
            }

            if (showRiderChat) {
                ParcelChatDialog(
                    parcelId = parcel.id,
                    senderRole = "rider",
                    viewModel = viewModel,
                    onDismiss = { showRiderChat = false }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (parcel.status != ParcelStatus.DELIVERED && parcel.status != ParcelStatus.CANCELLED) {
            RiderLiveGpsTelemetryCard(
                parcelId = parcel.id,
                pickupAddress = parcel.pickupAddress,
                deliveryAddress = parcel.deliveryAddress,
                status = parcel.status,
                viewModel = viewModel,
                isDark = isDark,
                pickupLat = parcel.pickupLat,
                pickupLng = parcel.pickupLng,
                deliveryLat = parcel.deliveryLat,
                deliveryLng = parcel.deliveryLng
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Operational Field Exception Kit
        if (parcel.status != ParcelStatus.PENDING && parcel.status != ParcelStatus.DELIVERED && parcel.status != ParcelStatus.CANCELLED) {
            var showExceptionMenu by remember { mutableStateOf(false) }

            if (parcel.exceptionType.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFD32F2F).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ACTIVE EXCEPTION: ${parcel.exceptionType.replace('_', ' ')}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFF8A80)
                                )
                            }
                            TextButton(
                                onClick = {
                                    viewModel.resolveParcelException(parcel.id) { success, _ ->
                                        if (success) {
                                            Toast.makeText(context, "Exception cleared! Delivery resumed.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("CLEAR EXCEPTION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Gold)
                            }
                        }
                        if (parcel.exceptionReason.isNotEmpty()) {
                            Text(
                                text = parcel.exceptionReason,
                                fontSize = 11.sp,
                                color = AppTextColor,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                OutlinedButton(
                    onClick = { showExceptionMenu = !showExceptionMenu },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (showExceptionMenu) Color(0xFFEF5350).copy(alpha = 0.1f) else Color.Transparent,
                        contentColor = Color(0xFFEF5350)
                    )
                ) {
                    Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFEF5350))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (showExceptionMenu) "HIDE EXCEPTION KIT" else "FIELD EXCEPTION KIT (ISSUE REPORT)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF5350)
                    )
                }

                AnimatedVisibility(visible = showExceptionMenu) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Trigger 1: Recipient Unreachable
                        Button(
                            onClick = {
                                viewModel.reportParcelException(
                                    parcel.id,
                                    "RECIPIENT_UNREACHABLE",
                                    "Customer unreachable by call/doorbell. 10m countdown protocol initiated."
                                ) { success, _ ->
                                    if (success) {
                                        Toast.makeText(context, "10-minute unreachable protocol logged to dispatch.", Toast.LENGTH_LONG).show()
                                        showExceptionMenu = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = Color(0xFFFFB74D)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.4f))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PhoneDisabled, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Recipient Unreachable [10m Timer]", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Trigger 2: Sender Delay / Not Ready
                        Button(
                            onClick = {
                                viewModel.reportParcelException(
                                    parcel.id,
                                    "SENDER_DELAY",
                                    "Pickup parcel not packaged or sender unavailable at location."
                                ) { success, _ ->
                                    if (success) {
                                        Toast.makeText(context, "Sender delay logged to dispatch.", Toast.LENGTH_SHORT).show()
                                        showExceptionMenu = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = Color(0xFFFFB74D)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.4f))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sender Delay / Package Not Ready", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Trigger 3: Wrong Address / Inaccessible
                        Button(
                            onClick = {
                                viewModel.reportParcelException(
                                    parcel.id,
                                    "WRONG_ADDRESS",
                                    "Road flooded/impassable or delivery address does not exist in Benin sector."
                                ) { success, _ ->
                                    if (success) {
                                        Toast.makeText(context, "Inaccessible address logged to dispatch.", Toast.LENGTH_SHORT).show()
                                        showExceptionMenu = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = Color(0xFFEF5350)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.4f))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOff, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Wrong Address / Inaccessible Road", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Trigger 4: Rider Breakdown
                        Button(
                            onClick = {
                                viewModel.reportParcelException(
                                    parcel.id,
                                    "RIDER_BREAKDOWN",
                                    "Courier motorcycle breakdown or flat tire in transit. Urgent fleet reassignment required."
                                ) { success, _ ->
                                    if (success) {
                                        Toast.makeText(context, "Rider breakdown alert broadcast to fleet dispatcher!", Toast.LENGTH_LONG).show()
                                        showExceptionMenu = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = Color(0xFFEF5350)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Courier Motorcycle Breakdown", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        val currentRiderUid = com.esdispatch.data.FirebaseManager.auth?.currentUser?.uid ?: ""
        val isAssignedToCurrentRider = (parcel.riderId.isNotBlank() && parcel.riderId == currentRiderUid) ||
            (parcel.driverId.isNotBlank() && parcel.driverId == currentRiderUid) ||
            (parcel.reservedRiderId.isNotBlank() && parcel.reservedRiderId == currentRiderUid)

        if (parcel.status == ParcelStatus.PENDING && !isAssignedToCurrentRider) {
            Text(
                text = "This is an unassigned company dispatch parcel. Do you want to accept this order and bind it to your fleet delivery manifest?",
                color = AppTextColor,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    isSubmitting = true
                    viewModel.acceptParcelByRider(parcel.id) { success, err ->
                        isSubmitting = false
                        if (success) {
                            Toast.makeText(context, "Dispatch Accepted successfully!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } else {
                            Toast.makeText(context, err ?: "Failed to accept dispatch", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                shape = RoundedCornerShape(14.dp),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Obsidian, modifier = Modifier.size(20.dp))
                } else {
                    Text("ACCEPT DISPATCH", fontWeight = FontWeight.Bold)
                }
            }
        } else if (parcel.status == ParcelStatus.ASSIGNED || (parcel.status == ParcelStatus.PENDING && isAssignedToCurrentRider)) {
            val pickupPrompt = if (parcel.isBatch) {
                "Confirm pickup of '${parcel.itemName.ifBlank { "batch item" }}' (Stop #${parcel.batchItemIndex} of ${parcel.batchTotalItems}). Are you currently at the pickup location and have received this specific package?"
            } else {
                "Confirm pickup of this package. Are you currently at the shipper's pickup location and have received the parcel?"
            }
            Text(
                text = pickupPrompt,
                color = AppTextColor,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

                          Button(
                  onClick = {
                      onNavigateToPOD("ProofOfPickup/${parcel.id}")
                  },
                modifier = Modifier.fillMaxWidth().tactilePress(scaleDown = 0.96f),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                shape = RoundedCornerShape(14.dp),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Obsidian, modifier = Modifier.size(20.dp))
                } else {
                    val btnText = if (parcel.isBatch) "PICK UP ${parcel.itemName.ifBlank { "ITEM" }.uppercase()}" else "CONFIRM PICKUP"
                    Text(btnText, fontWeight = FontWeight.Bold)
                }
            }
        } else if (parcel.status == ParcelStatus.PICKED_UP) {
            Text(
                text = "Package collected. Begin journey to destination and notify the recipient that their shipment is in transit.",
                color = AppTextColor,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    isSubmitting = true
                    viewModel.updateParcelStatusByRider(parcel.id, ParcelStatus.TRANSIT, 0.60f) { success, err ->
                        isSubmitting = false
                        if (success) {
                            Toast.makeText(context, "In transit to destination! Recipient notified.", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } else {
                            Toast.makeText(context, err ?: "Failed to update status", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().tactilePress(scaleDown = 0.96f),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                shape = RoundedCornerShape(14.dp),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Obsidian, modifier = Modifier.size(20.dp))
                } else {
                    Text("START TRANSIT", fontWeight = FontWeight.Bold)
                }
            }
        } else if (parcel.status == ParcelStatus.TRANSIT || parcel.status == ParcelStatus.OUT_FOR_DELIVERY) {
            Text(
                text = "When you arrive at the recipient's delivery address, mark as arrived to prompt for the 4-digit handover PIN.",
                color = AppTextColor,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    isSubmitting = true
                    viewModel.updateParcelStatusByRider(parcel.id, ParcelStatus.ARRIVED, 0.90f) { success, err ->
                        isSubmitting = false
                        if (success) {
                            Toast.makeText(context, "Marked as arrived! Customer notified.", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } else {
                            Toast.makeText(context, err ?: "Failed to update status", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().tactilePress(scaleDown = 0.96f),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                shape = RoundedCornerShape(14.dp),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Obsidian, modifier = Modifier.size(20.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Obsidian)
                        Text("MARK ARRIVED AT DESTINATION", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (parcel.status == ParcelStatus.CANCELLED) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFD32F2F).copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFEF5350), modifier = Modifier.size(22.dp))
                    Text(
                        text = "This shipment has been cancelled by the customer or dispatcher. No further action is required.",
                        color = Color(0xFFFF8A80),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = Color.White),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("CLOSE", fontWeight = FontWeight.Bold)
            }
        } else {
            // Arrived / Handover -> Capture arrival photo (optional) & PIN Verification
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Charcoal else GoldenWhiteLight,
                border = BorderStroke(1.dp, Gold)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
                    Text(
                        text = "Arrived at Destination — Awaiting Handover PIN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) GoldLight else Obsidian
                    )
                }
            }

            Text(
                text = "Enter Customer Handover PIN",
                color = AppTextColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Ask the recipient for their 4-digit PIN. Enter it below to verify handover and proceed to mandatory photo proof.",
                color = TextGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            PinInputField(
                pin = otpInput,
                onPinChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) otpInput = it },
                obscureText = false,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (otpInput.length != 4) {
                        Toast.makeText(context, "Please enter the 4-digit PIN", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSubmitting = true
                    viewModel.verifyDeliveryOtpByRider(parcel.id, otpInput) { success, err ->
                        isSubmitting = false
                        if (success) {
                            Toast.makeText(context, "Handover verified! Please capture proof of delivery photo.", Toast.LENGTH_LONG).show()
                            onDismiss()
                            onNavigateToPOD("ProofOfDelivery/${parcel.id}")
                        } else {
                            Toast.makeText(context, err ?: "Incorrect PIN", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                enabled = !isSubmitting && otpInput.length == 4
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("VERIFY PIN & PROCEED TO PHOTO PROOF", fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("CANCEL", color = TextGray, fontWeight = FontWeight.Bold)
        }
    }
}

private fun geocodeAddressToLatLng(context: android.content.Context, address: String): Pair<Double, Double> {
    if (address.isBlank()) return Pair(6.3350, 5.6037)
    val dbCoord = com.esdispatch.data.AddressDatabase.getCoordinates(address)
    if (dbCoord != null) return dbCoord

    try {
        if (android.location.Geocoder.isPresent()) {
            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
            val query = if (address.contains("Benin", ignoreCase = true)) address else "$address, Benin City, Nigeria"
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(query, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                if (addr.latitude in 6.1..6.5 && addr.longitude in 5.4..5.8) {
                    return Pair(addr.latitude, addr.longitude)
                }
            }
        }
    } catch (_: Exception) {}

    val lower = address.lowercase()
    return when {
        lower.contains("ring road") || lower.contains("ring road benin") -> Pair(6.3350, 5.6037)
        lower.contains("uba") || lower.contains("uba junction") || lower.contains("uba roundabout") -> Pair(6.3400, 5.6100)
        lower.contains("sapele road") || lower.contains("sapele") -> Pair(6.3200, 5.5900)
        lower.contains("broadcasting") || lower.contains("bcos") || lower.contains("gba") -> Pair(6.3450, 5.6200)
        lower.contains("edo") || lower.contains("edo state") || lower.contains("edo government") -> Pair(6.3400, 5.6150)
        lower.contains("obakhavbe") || lower.contains("obakhavbay") -> Pair(6.3300, 5.5950)
        lower.contains("iguomon") || lower.contains("iguomon road") -> Pair(6.3500, 5.6250)
        lower.contains("iyekogba") || lower.contains("iyekogba road") -> Pair(6.3420, 5.6080)
        lower.contains("evboekhae") || lower.contains("evboekhae road") -> Pair(6.3380, 5.6020)
        lower.contains("ogida") || lower.contains("ogida quarter") -> Pair(6.3280, 5.5980)
        lower.contains("benin") || lower.contains("benin city") -> Pair(6.3350, 5.6037)
        else -> Pair(6.3350, 5.6037)
    }
}

@Composable
fun RiderLiveGpsTelemetryCard(
    parcelId: String,
    pickupAddress: String,
    deliveryAddress: String,
    status: ParcelStatus,
    viewModel: DeliveryViewModel,
    isDark: Boolean,
    pickupLat: Double? = null,
    pickupLng: Double? = null,
    deliveryLat: Double? = null,
    deliveryLng: Double? = null
) {
    val context = LocalContext.current
    val liveLoc by com.esdispatch.util.LocationService.liveLocationFlow.collectAsState()
    var deliveryCoords by remember(deliveryLat, deliveryLng) {
        mutableStateOf(
            if (deliveryLat != null && deliveryLng != null && deliveryLat != 0.0 && deliveryLng != 0.0) {
                Pair(deliveryLat, deliveryLng)
            } else {
                Pair(6.3450, 5.6250)
            }
        )
    }
    var pickupCoords by remember(pickupLat, pickupLng) {
        mutableStateOf(
            if (pickupLat != null && pickupLng != null && pickupLat != 0.0 && pickupLng != 0.0) {
                Pair(pickupLat, pickupLng)
            } else {
                Pair(6.3350, 5.6037)
            }
        )
    }

    LaunchedEffect(deliveryAddress, deliveryLat, deliveryLng) {
        if (deliveryLat == null || deliveryLng == null || deliveryLat == 0.0 || deliveryLng == 0.0) {
            withContext(Dispatchers.IO) {
                deliveryCoords = geocodeAddressToLatLng(context, deliveryAddress)
            }
        }
    }
    LaunchedEffect(pickupAddress, pickupLat, pickupLng) {
        if (pickupLat == null || pickupLng == null || pickupLat == 0.0 || pickupLng == 0.0) {
            withContext(Dispatchers.IO) {
                pickupCoords = geocodeAddressToLatLng(context, pickupAddress)
            }
        }
    }

    val targetCoords = if (status == ParcelStatus.TRANSIT || status == ParcelStatus.OUT_FOR_DELIVERY || status == ParcelStatus.ARRIVED) {
        deliveryCoords
    } else {
        pickupCoords
    }

    val targetLabel = if (status == ParcelStatus.TRANSIT || status == ParcelStatus.OUT_FOR_DELIVERY || status == ParcelStatus.ARRIVED) {
        "Recipient"
    } else {
        "Pickup"
    }

    val distanceMeters = remember(liveLoc, targetCoords) {
        liveLoc?.let { loc ->
            calculateDistanceMeters(loc.latitude, loc.longitude, targetCoords.first, targetCoords.second)
        }
    }

    val isNearDestination = (distanceMeters != null && distanceMeters <= 50.0)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Charcoal else GoldenWhiteLight,
        border = BorderStroke(1.2.dp, if (isNearDestination) Gold else Gold.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (liveLoc != null) SuccessGreen else Color(0xFFFF9500))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (liveLoc != null) "HARDWARE GPS ACTIVE" else "ACQUIRING GPS FIX...",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Gold,
                        letterSpacing = 0.5.sp
                    )
                }
                if (liveLoc != null) {
                    Text(
                        text = "±${liveLoc!!.accuracy.toInt()}m accuracy",
                        fontSize = 10.sp,
                        color = TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("SPEED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                    Text(
                        text = if (liveLoc != null) String.format(java.util.Locale.US, "%.1f km/h", liveLoc!!.speed * 3.6f) else "-- km/h",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = AppTextColor
                    )
                }
                Column {
                    Text("DISTANCE TO $targetLabel", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                    Text(
                        text = when {
                            distanceMeters == null -> "Calculating..."
                            distanceMeters >= 1000 -> String.format(java.util.Locale.US, "%.1f km", distanceMeters / 1000)
                            else -> "${distanceMeters.toInt()} m"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isNearDestination) Gold else AppTextColor
                    )
                }
                Column {
                    Text("HEADING", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                    Text(
                        text = if (liveLoc != null && liveLoc!!.hasBearing()) "${liveLoc!!.bearing.toInt()}°" else "--",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = AppTextColor
                    )
                }
            }

            if (isNearDestination && (status == ParcelStatus.TRANSIT || status == ParcelStatus.OUT_FOR_DELIVERY)) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        viewModel.updateParcelStatusByRider(parcelId, ParcelStatus.ARRIVED, 0.90f) { success, _ ->
                            if (success) {
                                Toast.makeText(context, "Marked as arrived! Recipient notified.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ARRIVED AT DESTINATION (<50m)", fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun VehicleInspectionDialog(
    viewModel: DeliveryViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var tiresOk by remember { mutableStateOf(true) }
    var brakesOk by remember { mutableStateOf(true) }
    var headlightsOk by remember { mutableStateOf(true) }
    var hornOk by remember { mutableStateOf(true) }
    var fuelBatteryOk by remember { mutableStateOf(true) }
    var safetyVestOk by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Charcoal,
        titleContentColor = AppTextColor,
        textContentColor = AppTextColor,
        title = { Text("Daily Vehicle Pre-Trip Inspection", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Mandatory corporate fleet safety check before accepting dispatches:", fontSize = 11.sp, color = TextGray)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = tiresOk, onCheckedChange = { tiresOk = it })
                    Text("Tires & Tread Pressure OK", fontSize = 12.sp, color = AppTextColor)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = brakesOk, onCheckedChange = { brakesOk = it })
                    Text("Brake System Functioning", fontSize = 12.sp, color = AppTextColor)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = headlightsOk, onCheckedChange = { headlightsOk = it })
                    Text("Headlights & Indicators", fontSize = 12.sp, color = AppTextColor)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = hornOk, onCheckedChange = { hornOk = it })
                    Text("Horn & Mirrors Operational", fontSize = 12.sp, color = AppTextColor)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = fuelBatteryOk, onCheckedChange = { fuelBatteryOk = it })
                    Text("Fuel / EV Battery Level Adequate (>50%)", fontSize = 12.sp, color = AppTextColor)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = safetyVestOk, onCheckedChange = { safetyVestOk = it })
                    Text("Safety Vest & Helmet Equipped", fontSize = 12.sp, color = AppTextColor)
                }

                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Inspection Notes / Maintenance Remarks") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = Slate,
                        focusedLabelColor = Gold,
                        unfocusedLabelColor = TextGray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSubmitting = true
                    viewModel.submitVehicleInspection(
                        tiresOk = tiresOk,
                        brakesOk = brakesOk,
                        headlightsOk = headlightsOk,
                        hornOk = hornOk,
                        fuelBatteryLevelOk = fuelBatteryOk,
                        safetyVestHelmetOk = safetyVestOk,
                        notes = notes
                    ) { success, _ ->
                        isSubmitting = false
                        if (success) onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                enabled = !isSubmitting
            ) {
                Text("SUBMIT INSPECTION", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}

@Composable
fun ExpenseClaimDialog(
    viewModel: DeliveryViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("FUEL") }
    var amountStr by remember { mutableStateOf("") }
    var receiptNote by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Charcoal,
        titleContentColor = AppTextColor,
        textContentColor = AppTextColor,
        title = { Text("Corporate Expense & Fuel Reimbursement", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Log fuel, charging, tolls, or minor emergency maintenance for company payroll reimbursement.", fontSize = 11.sp, color = TextGray)
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title (e.g. Weekly Fuel Topup)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (FUEL, CHARGING, TOLLS, MAINTENANCE)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount (e.g. 15000.0)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = receiptNote,
                    onValueChange = { receiptNote = it },
                    label = { Text("Receipt Number / Details") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (title.isNotEmpty() && amt > 0) {
                        viewModel.submitExpenseClaim(title, category, amt, receiptNote) { success, _ ->
                            if (success) onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian)
            ) {
                Text("SUBMIT CLAIM", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextGray) }
        }
    )
}

@Composable
fun ShiftRosterDialog(
    viewModel: DeliveryViewModel,
    shiftRosters: List<ShiftRoster>,
    onDismiss: () -> Unit
) {
    var leaveDate by remember { mutableStateOf("2026-07-15") }
    var leaveReason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Charcoal,
        titleContentColor = AppTextColor,
        textContentColor = AppTextColor,
        title = { Text("Shift Roster & Leave Management", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Assigned Working Hours: Monday - Saturday (08:00 - 17:00)", fontSize = 11.sp, color = Gold, fontWeight = FontWeight.Bold)
                
                Text("Request Time Off / Leave:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                OutlinedTextField(
                    value = leaveDate,
                    onValueChange = { leaveDate = it },
                    label = { Text("Leave Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = leaveReason,
                    onValueChange = { leaveReason = it },
                    label = { Text("Reason for Leave") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = {
                        if (leaveReason.isNotEmpty()) {
                            viewModel.requestLeave(leaveDate, leaveReason) { _, _ -> }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("SUBMIT LEAVE REQUEST", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Recent Roster & Leave Status:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                if (shiftRosters.isEmpty()) {
                    Text("No leave requests submitted.", fontSize = 11.sp, color = TextGray)
                } else {
                    shiftRosters.forEach { r ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BackgroundDark,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Date: ${r.shiftDate} | Status: ${r.leaveStatus}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gold)
                                Text("Reason: ${r.leaveReason.ifEmpty { "Regular Shift" }}", fontSize = 10.sp, color = AppTextColor)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = AppTextColor)) {
                Text("Close")
            }
        }
    )
}

@Composable
fun OfflineSyncDialog(
    viewModel: DeliveryViewModel,
    offlineSyncQueue: List<OfflineSyncQueue>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Charcoal,
        titleContentColor = AppTextColor,
        textContentColor = AppTextColor,
        title = { Text("Offline Queue & Sync Resilience", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Cached actions when operating in low-signal or basement delivery zones. Auto-syncs upon connection restore.", fontSize = 11.sp, color = TextGray)
                
                Button(
                    onClick = { viewModel.syncOfflineQueue() },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.CloudSync, "Sync Now", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SYNCHRONIZE NOW WITH SERVER", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Queue Items (${offlineSyncQueue.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                if (offlineSyncQueue.isEmpty()) {
                    Text("Queue is empty. All telemetry synced.", fontSize = 11.sp, color = TextGray)
                } else {
                    offlineSyncQueue.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BackgroundDark,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Action: ${item.actionType}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (item.synced) Color.Green else Gold)
                                Text("Synced: ${item.synced} | Time: ${item.timestamp}", fontSize = 10.sp, color = TextGray)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = AppTextColor)) {
                Text("Close")
            }
        }
    )
}

@Composable
fun BatchRouteOptimizationDialog(
    viewModel: DeliveryViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var batchName by remember { mutableStateOf("Corporate Metro Batch #104") }
    var stopInput by remember { mutableStateOf("Admiralty Way, Ozumba Mbadiwe, Marina Hub") }
    var optimizedPlan by remember { mutableStateOf<com.esdispatch.data.BatchRoutePlan?>(null) }
    var isOptimizing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Charcoal,
        titleContentColor = AppTextColor,
        textContentColor = AppTextColor,
        title = { Text("Multi-Stop Batch Route Optimization & AI ETA", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("AI reorders multi-package dispatch stops to minimize fuel burn and guarantee lowest ETA.", fontSize = 11.sp, color = TextGray)

                OutlinedTextField(
                    value = batchName,
                    onValueChange = { batchName = it },
                    label = { Text("Batch Dispatch Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stopInput,
                    onValueChange = { stopInput = it },
                    label = { Text("Delivery Addresses (Comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        isOptimizing = true
                        val stopsList = stopInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        viewModel.optimizeBatchRoute(batchName, stopsList) { plan ->
                            optimizedPlan = plan
                            isOptimizing = false
                            Toast.makeText(context, "AI Route Optimized successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isOptimizing
                ) {
                    Text(if (isOptimizing) "AI CALCULATING OPTIMAL PATH..." else "RUN AI ROUTE OPTIMIZATION", fontWeight = FontWeight.Bold)
                }

                optimizedPlan?.let { plan ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BackgroundDark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Batch: ${plan.batchName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Gold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Optimized Path: ${plan.optimizedPathSummary}", fontSize = 11.sp, color = AppTextColor)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Estimated Distance: ${plan.estimatedDistanceKm} km | ETA: ${plan.estimatedEtaMinutes} mins", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                            Text("AI Efficiency Confidence: ${plan.aiConfidence}%", fontSize = 10.sp, color = TextGray)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = AppTextColor)) {
                Text("Close")
            }
        }
    )
}

@Composable
fun GeofenceTelemetryDialog(
    viewModel: DeliveryViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var testLat by remember { mutableStateOf("6.45") }
    var testLng by remember { mutableStateOf("3.42") }
    var alertResult by remember { mutableStateOf<com.esdispatch.data.GeofenceAlert?>(null) }
    var checkedStatus by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Charcoal,
        titleContentColor = AppTextColor,
        textContentColor = AppTextColor,
        title = { Text("Real-Time Fleet Geofencing & Telemetry Alerts", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Monitors vehicle coordinates against corporate delivery operational zones and speed limits.", fontSize = 11.sp, color = TextGray)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = testLat,
                        onValueChange = { testLat = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = testLng,
                        onValueChange = { testLng = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = {
                        val lat = testLat.toDoubleOrNull() ?: 6.45
                        val lng = testLng.toDoubleOrNull() ?: 3.42
                        viewModel.checkGeofenceBreach("Fleet Rider Alpha", lat, lng) { alert ->
                            alertResult = alert
                            checkedStatus = true
                            if (alert != null) {
                                Toast.makeText(context, "Geofence breach detected!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Within corporate perimeter boundary.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CHECK GEOFENCE TELEMETRY", fontWeight = FontWeight.Bold)
                }

                if (checkedStatus) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (alertResult != null) Color(0xFFEF5350).copy(alpha = 0.2f) else Color(0xFF4CAF50).copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (alertResult != null) {
                                Text("BREACH DETECTED: ${alertResult?.breachType}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF5350))
                                Text("Location: ${alertResult?.locationName}", fontSize = 11.sp, color = AppTextColor)
                                Text("Severity: ${alertResult?.severity} | Time: ${alertResult?.timestamp}", fontSize = 10.sp, color = TextGray)
                            } else {
                                Text("SECURE: Vehicle operating strictly inside authorized corporate delivery corridor.", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = AppTextColor)) {
                Text("Close")
            }
        }
    )
}

@Composable
fun IncidentReportDialog(
    viewModel: DeliveryViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var title by remember { mutableStateOf("Minor Vehicle Breakdown") }
    var severity by remember { mutableStateOf("Medium") }
    var description by remember { mutableStateOf("Flat tire near Lekki Expressway. Replacement requested.") }
    var isSubmitted by remember { mutableStateOf(false) }
    
    var photoEvidenceCaptured by remember { mutableStateOf(false) }
    var selectedEvidenceType by remember { mutableStateOf("Flat Tire / Breakdown") }
    var showEvidenceSelector by remember { mutableStateOf(false) }
    
    var capturedPhotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedPhotoBitmap = bitmap
            photoEvidenceCaptured = true
            showEvidenceSelector = true // Prompt to select type after snap
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Charcoal,
        titleContentColor = AppTextColor,
        textContentColor = AppTextColor,
        title = { Text("Driver Incident & Accident Reporting (SOS)", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Submit digital incident logs with severity classification, photo evidence placeholders, and SOS dispatch integration.", fontSize = 11.sp, color = TextGray)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Incident Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = severity,
                    onValueChange = { severity = it },
                    label = { Text("Severity (Low, Medium, High, Critical)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Notes") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text("PHOTO EVIDENCE ATTACHMENT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                
                if (photoEvidenceCaptured) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, Gold),
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .background(Gold.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                val bitmap = capturedPhotoBitmap
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Live Evidence Shot",
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (selectedEvidenceType.contains("Breakdown")) Icons.Filled.DirectionsBike else Icons.Filled.Warning,
                                        contentDescription = "Attached Evidence",
                                        tint = Gold,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Evidence ID: ATTACH-${System.currentTimeMillis().toString().takeLast(6)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Type: $selectedEvidenceType", fontSize = 10.sp, color = TextGray)
                                Text("Location: Lekki Toll Gate GPS Verified", fontSize = 9.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                Text(
                                    "Change Category", 
                                    fontSize = 10.sp, 
                                    color = Gold, 
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { showEvidenceSelector = true }
                                )
                            }
                            IconButton(onClick = { 
                                photoEvidenceCaptured = false
                                capturedPhotoBitmap = null
                            }) {
                                Icon(Icons.Filled.Delete, "Delete", tint = Color(0xFFEF5350))
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            cameraLauncher.launch(null)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.PhotoCamera, "Camera", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SNAP LIVE PHOTO EVIDENCE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (showEvidenceSelector) {
                    AlertDialog(
                        onDismissRequest = { showEvidenceSelector = false },
                        title = { Text("Select Evidence Type", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppTextColor) },
                        containerColor = Charcoal,
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val evidenceTypes = listOf("Flat Tire / Breakdown", "Severe Road Gridlock", "Vehicle Crash / Accident", "Aggressive Recipient")
                                evidenceTypes.forEach { type ->
                                    Surface(
                                        onClick = {
                                            selectedEvidenceType = type
                                            photoEvidenceCaptured = true
                                            showEvidenceSelector = false
                                            Toast.makeText(context, "Photo evidence category mapped!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = Charcoal,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(type, modifier = Modifier.padding(12.dp), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        },
                        confirmButton = {}
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        viewModel.submitIncidentReport(title, severity, description) { success, incidentId ->
                            if (success) {
                                isSubmitted = true
                                Toast.makeText(context, "Incident $incidentId logged & dispatched to safety HQ!", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350), contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Warning, "SOS", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("TRANSMIT EMERGENCY SOS REPORT", fontWeight = FontWeight.Bold)
                }

                if (isSubmitted) {
                    Text("Incident successfully logged in corporate database. Safety supervisor notified.", fontSize = 11.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                keyboardController?.hide()
                focusManager.clearFocus()
                onDismiss()
            }, colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = AppTextColor)) {
                Text("Close")
            }
        }
    )
}

@Composable
fun BonusCalculatorDialog(
    viewModel: DeliveryViewModel,
    deliveredCount: Int,
    onDismiss: () -> Unit
) {
    val bonusCalc = viewModel.calculateDriverBonus(deliveredCount.coerceAtLeast(12), 97.5, 4.9)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Charcoal,
        titleContentColor = AppTextColor,
        textContentColor = AppTextColor,
        title = { Text("Corporate Performance Tier & Bonus Calculator", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Automated bonus tier calculation based on delivery volume, on-time percentage, and customer ratings.", fontSize = 11.sp, color = TextGray)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BackgroundDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Performance Tier:", fontSize = 12.sp, color = TextGray)
                            Text(bonusCalc.tierLabel, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Gold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Deliveries:", fontSize = 12.sp, color = TextGray)
                            Text("${bonusCalc.totalDeliveries} shipments", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("On-Time Percentage:", fontSize = 12.sp, color = TextGray)
                            Text("${bonusCalc.onTimePercentage}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Customer Rating:", fontSize = 12.sp, color = TextGray)
                            Text("${bonusCalc.averageRating}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Gold)
                        }
                        HorizontalDivider(color = BorderDark)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Projected Bonus Payout:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                            Text("₦${String.format(java.util.Locale.getDefault(), "%,.2f", bonusCalc.projectedPayout)}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Gold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = AppTextColor)) {
                Text("Close")
            }
        }
    )
}

@Composable
fun VehicleMaintenanceDialog(
    viewModel: DeliveryViewModel,
    bikeNumber: String,
    onDismiss: () -> Unit
) {
    val maintenance = viewModel.checkVehicleMaintenance(if (bikeNumber.isNotBlank()) bikeNumber else "BIKE-BENIN-08", 14500)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Charcoal,
        titleContentColor = AppTextColor,
        textContentColor = AppTextColor,
        title = { Text("Vehicle Maintenance & Servicing Scheduler", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Preventive maintenance tracker with mileage-based service interval reminders (oil change, tire rotation, brakes).", fontSize = 11.sp, color = TextGray)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BackgroundDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Vehicle ID:", fontSize = 12.sp, color = TextGray)
                            Text(maintenance.vehicleNumber, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Gold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Service Status:", fontSize = 12.sp, color = TextGray)
                            Text(maintenance.status, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (maintenance.status == "OVERDUE") Color(0xFFEF5350) else Color(0xFF4CAF50))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Last Service Mileage:", fontSize = 12.sp, color = TextGray)
                            Text("${maintenance.lastServiceMileage} km", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Next Due Mileage:", fontSize = 12.sp, color = TextGray)
                            Text("${maintenance.nextServiceMileageDue} km", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Recommended Service:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                        Text(maintenance.serviceType, fontSize = 11.sp, color = TextGray)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Technician Note: ${maintenance.technicianNote}", fontSize = 10.sp, color = Gold)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = AppTextColor)) {
                Text("Close")
            }
        }
    )
}

fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371000.0 // Earth radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return r * c
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderWaybillBottomSheet(
    parcel: Parcel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Charcoal,
        contentColor = AppTextColor,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Document Header
            Text(
                text = "ESDISPATCH",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = if (isDark) Gold else Obsidian,
                letterSpacing = 2.sp
            )
            Text(
                text = "SMILES DISPATCH RIDER",
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextGray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isDark) Color(0xFF1E1E1E) else GoldenWhiteLight,
                border = BorderStroke(1.dp, if (isDark) BorderDark else Slate)
            ) {
                Text(
                    text = "WAYBILL #${parcel.id.take(12).uppercase()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = AppTextColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Origin & Destination Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color(0xFF1A1A1A) else GoldenWhiteLight,
                border = BorderStroke(1.dp, if (isDark) BorderDark else Slate)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column {
                        Text(text = "SENDER / PICKUP", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextGray)
                        Text(text = parcel.senderName.ifBlank { "Sender" }, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                        Text(text = parcel.pickupAddress, fontSize = 11.sp, color = TextGray)
                    }
                    HorizontalDivider(color = if (isDark) BorderDark else Slate.copy(alpha = 0.5f))
                    Column {
                        Text(text = "CONSIGNEE / DESTINATION", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextGray)
                        Text(text = parcel.receiverName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                        Text(text = parcel.deliveryAddress, fontSize = 11.sp, color = TextGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Particulars & Handover OTP
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color(0xFF1A1A1A) else GoldenWhiteLight,
                border = BorderStroke(1.dp, if (isDark) BorderDark else Slate)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Item:", fontSize = 11.sp, color = TextGray)
                        Text(parcel.itemName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Weight:", fontSize = 11.sp, color = TextGray)
                        Text("${parcel.weight} kg", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Fare:", fontSize = 11.sp, color = TextGray)
                        Text("₦${String.format(java.util.Locale.getDefault(), "%,.2f", parcel.price)}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = if (isDark) Gold else Obsidian)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Customer Handover Code:", fontSize = 11.sp, color = TextGray)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = (if (isDark) Gold else Obsidian).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, (if (isDark) Gold else Obsidian).copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "ENTER UPON HANDOVER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = if (isDark) Gold else Obsidian,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Share & Close Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                android.content.Intent.EXTRA_TEXT,
                                "ESDISPATCH WAYBILL\nTracking ID: ${parcel.id}\nItem: ${parcel.itemName}\nRecipient: ${parcel.receiverName}\nLive Status: https://esdispatch.com/track/${parcel.id}"
                            )
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Waybill Receipt"))
                    },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Gold),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isDark) Gold else Obsidian)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian)
                ) {
                    Text("Close", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun RiderPermissionReadinessDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark

    var fineGranted by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    var backgroundGranted by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val powerManager = remember { context.getSystemService(android.content.Context.POWER_SERVICE) as? android.os.PowerManager }
    var batteryOptimized by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
            } else true
        )
    }

    var cameraGranted by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val fineLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
        fineGranted = map[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    val bgLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        backgroundGranted = granted
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraGranted = granted
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Charcoal else Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = Gold, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Dispatch Telemetry Readiness",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) GoldLight else Obsidian
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text(
                    text = "To guarantee real-time tracking during delivery journeys and prevent GPS interruptions when your screen locks, ensure these settings are enabled.",
                    fontSize = 12.sp,
                    color = TextGray,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Fine Location
                PermissionStatusRow(
                    title = "Precise Hardware GPS",
                    desc = "Required to calculate real routes and broadcast live location.",
                    isGranted = fineGranted,
                    onGrant = {
                        fineLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Background Location
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    PermissionStatusRow(
                        title = "Background Location",
                        desc = "Allows continuous tracking while phone screen is locked in transit.",
                        isGranted = backgroundGranted,
                        onGrant = {
                            if (!fineGranted) {
                                Toast.makeText(context, "Please grant Precise Location first.", Toast.LENGTH_SHORT).show()
                            } else {
                                bgLauncher.launch(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Battery Optimization
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    PermissionStatusRow(
                        title = "Battery Optimization Exemption",
                        desc = "Prevents OS from putting the dispatch service into deep sleep.",
                        isGranted = batteryOptimized,
                        onGrant = {
                            try {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = android.net.Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    context.startActivity(android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                                } catch (ex: Exception) {
                                    Toast.makeText(context, "Open Settings > Battery > Optimize battery usage", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Camera
                PermissionStatusRow(
                    title = "Camera Access",
                    desc = "Required to capture mandatory handover proof of delivery.",
                    isGranted = cameraGranted,
                    onGrant = {
                        cameraLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("DONE", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun PermissionStatusRow(
    title: String,
    desc: String,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Charcoal.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .border(BorderStroke(1.dp, if (isGranted) SuccessGreen.copy(alpha = 0.4f) else Gold.copy(alpha = 0.3f)), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
            Text(desc, fontSize = 10.sp, color = TextGray, lineHeight = 13.sp)
        }
        if (isGranted) {
            Surface(
                shape = CircleShape,
                color = SuccessGreen.copy(alpha = 0.15f),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Check, contentDescription = "Granted", tint = SuccessGreen, modifier = Modifier.size(16.dp))
                }
            }
        } else {
            Button(
                onClick = onGrant,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text("ENABLE", fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

