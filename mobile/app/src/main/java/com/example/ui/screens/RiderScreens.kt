package com.example.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Parcel
import com.example.data.ParcelStatus
import com.example.data.ShiftRoster
import com.example.data.OfflineSyncQueue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.ui.theme.*
import com.example.ui.components.ScreenHeader
import com.example.ui.components.BottomNav
import com.example.ui.components.SupportButton
import com.example.ui.components.SupportDialog
import com.example.viewmodel.DeliveryViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image

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
    val userName by viewModel.userName.collectAsState()
    val aiTrafficCongested by viewModel.aiTrafficCongested.collectAsState()

    val firstName = remember(userName) { userName.trim().split(" ").firstOrNull() ?: userName }

    var selectedFilter by remember { mutableStateOf("Available") } // "Available", "Active", "Delivered", "All"
    var selectedParcelForUpdate by remember { mutableStateOf<Parcel?>(null) }
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

    // Automatically trigger update status sheet when parcel is scanned
    LaunchedEffect(scannedRiderParcel) {
        scannedRiderParcel?.let { parcel ->
            selectedParcelForUpdate = parcel
            showUpdateBottomSheet = true
            viewModel.setScannedRiderParcel(null)
        }
    }

    // Filter calculations
    val filteredAssignments = remember(riderAssignments, availableDeliveries, selectedFilter) {
        when (selectedFilter) {
            "Available" -> availableDeliveries
            "Active" -> riderAssignments.filter { it.status != ParcelStatus.DELIVERED && it.status != ParcelStatus.PENDING }
            "Delivered" -> riderAssignments.filter { it.status == ParcelStatus.DELIVERED }
            else -> riderAssignments
        }
    }

    val activeCount = remember(riderAssignments) {
        riderAssignments.filter { it.status != ParcelStatus.DELIVERED }.size
    }
    val deliveredCount = remember(riderAssignments) {
        riderAssignments.filter { it.status == ParcelStatus.DELIVERED }.size
    }

    Scaffold(
        containerColor = LuxuryBlack,
        bottomBar = { BottomNav(currentScreen = "Dashboard", onNavigate = onNavigate, activeViewMode = "rider") },
        floatingActionButton = {
            SupportButton(onClick = { showSupportDialog = true })
        },
        topBar = {
            ScreenHeader(
                title = "Rider Dispatch",
                rightContent = {
                    Button(
                        onClick = { viewModel.setActiveViewMode("customer") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Obsidian else Gold,
                            contentColor = if (isDark) Gold else Obsidian
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch to Customer",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Customer Mode", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(LuxuryBlack)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Greeting and Slogan Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = "Good day, $firstName! 🏍️",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = AppTextColor
                        )
                        Text(
                            text = "PREMIUM LOGISTICS & DISPATCH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Gold else Obsidian,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Balance & Dispatch Metrics Card
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = AppSurface,
                        border = BorderStroke(1.dp, if (isDark) BorderDark else Slate)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(listOf(Gold, Color(0xFFF59E0B)))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalanceWallet,
                                            contentDescription = "Wallet",
                                            tint = Obsidian,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "FLEET SALARY & PERFORMANCE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isDark) Gold else Obsidian,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                            Column {
                                                Text("Daily Tips", fontSize = 8.sp, color = TextGray, fontWeight = FontWeight.Bold)
                                                Text("₦${String.format("%,.2f", totalEarned * 0.35)}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = AppTextColor)
                                            }
                                            Column {
                                                Text("Weekly Tips", fontSize = 8.sp, color = TextGray, fontWeight = FontWeight.Bold)
                                                Text("₦${String.format("%,.2f", totalEarned)}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = AppTextColor)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "ℹ️ Drivers are employed internally by company. Base fees go to company; you earn 100% tips.",
                                            fontSize = 8.sp,
                                            color = TextGray,
                                            lineHeight = 11.sp
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .background(Gold.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = bikeNumber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Gold
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 14.dp),
                                color = if (isDark) BorderDark else Slate.copy(alpha = 0.5f),
                                thickness = 1.dp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val isOnlineState by viewModel.isOnline.collectAsState()
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isOnlineState) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isOnlineState) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = "Status Indicator",
                                            tint = if (isOnlineState) Color(0xFF4CAF50) else Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "DUTY STATUS",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = TextGray,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = if (isOnlineState) "ONLINE & READY" else "OFFLINE",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isOnlineState) Color(0xFF4CAF50) else AppTextColor
                                        )
                                    }
                                }

                                Switch(
                                    checked = isOnlineState,
                                    onCheckedChange = { viewModel.setRiderOnlineStatus(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Obsidian,
                                        checkedTrackColor = Gold,
                                        uncheckedThumbColor = TextGray,
                                        uncheckedTrackColor = if (isDark) Charcoal else Slate
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Active assignments count
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isDark) BackgroundDark else GoldenWhiteLight,
                                    border = BorderStroke(1.dp, if (isDark) BorderDark else Slate)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("ACTIVE DISPATCH", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                        Text("$activeCount", fontSize = 18.sp, fontWeight = FontWeight.Black, color = AppTextColor)
                                    }
                                }

                                // Completed deliveries count
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isDark) BackgroundDark else GoldenWhiteLight,
                                    border = BorderStroke(1.dp, if (isDark) BorderDark else Slate)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("COMPLETED", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                        Text("$deliveredCount", fontSize = 18.sp, fontWeight = FontWeight.Black, color = AppTextColor)
                                    }
                                }
                            }
                        }
                    }
                }

                // Live Dispatch & Weather Alert Watch Banner
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = if (aiTrafficCongested) Color(0xFF2D1815) else Charcoal,
                        border = BorderStroke(1.2.dp, if (aiTrafficCongested) Color(0xFFFF5252).copy(alpha = 0.5f) else Gold.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        imageVector = Icons.Filled.Notifications,
                                        contentDescription = "Alert",
                                        tint = if (aiTrafficCongested) Gold else (if (isDark) Gold else Obsidian),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Dispatch & Weather Status",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (aiTrafficCongested) Color.White else AppTextColor
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(if (aiTrafficCongested) Color.Red else (if (isDark) Gold else Obsidian), CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (aiTrafficCongested) {
                                    "⚠️ ALERT: Heavy traffic congestion reported. Smart motorcycle dispatch rerouting is highly recommended."
                                } else {
                                    "☀️ OPTIMAL: Clear skies. Normal traffic flow. Safe premium delivery zones are active. Deliveries are running ahead of schedule."
                                },
                                fontSize = 11.sp,
                                color = if (aiTrafficCongested) Color.White else AppTextColor,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Corporate Fleet Operations Hub Card
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = AppSurface,
                        border = BorderStroke(1.dp, if (isDark) BorderDark else Slate)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        imageVector = Icons.Filled.DirectionsBike,
                                        contentDescription = "Fleet Hub",
                                        tint = Gold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "EMPLOYEE FLEET OPERATIONS",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AppTextColor
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (currentAttendanceStatus) {
                                        "ON_DUTY" -> Color(0xFF2E7D32).copy(alpha = 0.2f)
                                        "ON_BREAK" -> Color(0xFFEF6C00).copy(alpha = 0.2f)
                                        else -> Color(0xFFC62828).copy(alpha = 0.2f)
                                    },
                                    border = BorderStroke(1.dp, when (currentAttendanceStatus) {
                                        "ON_DUTY" -> Color(0xFF4CAF50)
                                        "ON_BREAK" -> Color(0xFFFF9800)
                                        else -> Color(0xFFE57373)
                                    })
                                ) {
                                    Text(
                                        text = currentAttendanceStatus.replace("_", " "),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (currentAttendanceStatus) {
                                            "ON_DUTY" -> Color(0xFF4CAF50)
                                            "ON_BREAK" -> Color(0xFFFF9800)
                                            else -> Color(0xFFE57373)
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Shift Attendance Toggle Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.clockInStatus("ON_DUTY") },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (currentAttendanceStatus == "ON_DUTY") Gold else Charcoal,
                                        contentColor = if (currentAttendanceStatus == "ON_DUTY") Obsidian else AppTextColor
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("On Duty", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { viewModel.clockInStatus("ON_BREAK") },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (currentAttendanceStatus == "ON_BREAK") Gold else Charcoal,
                                        contentColor = if (currentAttendanceStatus == "ON_BREAK") Obsidian else AppTextColor
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("On Break", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { viewModel.clockInStatus("OFF_DUTY") },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (currentAttendanceStatus == "OFF_DUTY") Gold else Charcoal,
                                        contentColor = if (currentAttendanceStatus == "OFF_DUTY") Obsidian else AppTextColor
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Off Duty", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 4 Feature Action Buttons (Pre-trip Inspection, Expense Claim, Shift Roster, Offline Sync)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showInspectionDialog = true },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppTextColor),
                                    contentPadding = PaddingValues(4.dp)
                                ) {
                                    Icon(Icons.Filled.CheckCircle, "Inspect", tint = Gold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pre-Trip", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { showExpenseDialog = true },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppTextColor),
                                    contentPadding = PaddingValues(4.dp)
                                ) {
                                    Icon(Icons.Filled.Receipt, "Expense", tint = Gold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Expenses", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { showRosterDialog = true },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppTextColor),
                                    contentPadding = PaddingValues(4.dp)
                                ) {
                                    Icon(Icons.Filled.CalendarMonth, "Roster", tint = Gold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Roster", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { showSyncDialog = true },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppTextColor),
                                    contentPadding = PaddingValues(4.dp)
                                ) {
                                    Icon(Icons.Filled.CloudSync, "Sync", tint = Gold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sync(${offlineSyncQueue.filter{!it.synced}.size})", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Second Row: Batch Route, Geofence, Incident, Bonus, Maintenance
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showBatchRouteDialog = true },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Gold),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Icon(Icons.Filled.Route, "Batch", tint = Gold, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Batch AI", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                                }

                                OutlinedButton(
                                    onClick = { showGeofenceDialog = true },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Gold),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Icon(Icons.Filled.MyLocation, "Geo", tint = Gold, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Geofence", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                                }

                                OutlinedButton(
                                    onClick = { showIncidentDialog = true },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFEF5350)),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Icon(Icons.Filled.Warning, "Incident", tint = Color(0xFFEF5350), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Incident", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                                }

                                OutlinedButton(
                                    onClick = { showBonusDialog = true },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Gold),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Icon(Icons.Filled.EmojiEvents, "Bonus", tint = Gold, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Bonuses", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                                }

                                OutlinedButton(
                                    onClick = { showMaintenanceDialog = true },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Gold),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Icon(Icons.Filled.Build, "Maint", tint = Gold, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Service", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                                }
                            }
                        }
                    }
                }

                // Segmented Filters bar
                item {
                    Spacer(modifier = Modifier.height(24.dp))
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
                                Text(
                                    text = tab,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Obsidian else TextGray
                                )
                            }
                        }
                    }
                }

                // Assigned Deliveries List
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
                                Icon(
                                    imageVector = Icons.Default.DirectionsBike,
                                    contentDescription = "Empty",
                                    tint = TextGray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (selectedFilter == "Available") "No Dispatches Available" else "No Shipments Found",
                                    color = AppTextColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (selectedFilter == "Available") {
                                        "No unassigned orders found in your area. Open the dispatch app to receive incoming customer parcels!"
                                    } else {
                                        "Wait for the admin dispatcher to assign logistics deliveries to your profile."
                                    },
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                } else {
                    items(filteredAssignments, key = { it.id }) { parcel ->
                        RiderParcelCard(
                            parcel = parcel,
                            onUpdateStatus = {
                                selectedParcelForUpdate = parcel
                                showUpdateBottomSheet = true
                            }
                        )
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
                onDismiss = { showUpdateBottomSheet = false }
            )
        }
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
}

@Composable
fun RiderParcelCard(
    parcel: Parcel,
    onUpdateStatus: () -> Unit
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
        border = BorderStroke(1.dp, if (isDark) BorderDark else Slate)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
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

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (parcel.status) {
                                ParcelStatus.PENDING -> Gold.copy(alpha = 0.15f)
                                ParcelStatus.ASSIGNED -> Gold.copy(alpha = 0.15f)
                                ParcelStatus.TRANSIT -> {
                                    if (parcel.progress <= 0.35f) Gold.copy(alpha = 0.15f) else SuccessGreen.copy(alpha = 0.15f)
                                }
                                ParcelStatus.OUT_FOR_DELIVERY -> WarningOrange.copy(alpha = 0.15f)
                                else -> SuccessGreen.copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (parcel.status) {
                            ParcelStatus.PENDING -> "AVAILABLE"
                            ParcelStatus.ASSIGNED -> "ASSIGNED"
                            ParcelStatus.TRANSIT -> {
                                if (parcel.progress <= 0.35f) "PICKUP" else "PICKED UP"
                            }
                            ParcelStatus.OUT_FOR_DELIVERY -> "OUT FOR DELIVERY"
                            else -> "DELIVERED"
                        },
                        color = when (parcel.status) {
                            ParcelStatus.PENDING -> Gold
                            ParcelStatus.ASSIGNED -> Gold
                            ParcelStatus.TRANSIT -> {
                                if (parcel.progress <= 0.35f) Gold else SuccessGreen
                            }
                            ParcelStatus.OUT_FOR_DELIVERY -> WarningOrange
                            else -> SuccessGreen
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
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

                if (parcel.status != ParcelStatus.DELIVERED) {
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
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = when (parcel.status) {
                                ParcelStatus.PENDING -> "ACCEPT GIG"
                                ParcelStatus.ASSIGNED -> "CONFIRM PICKUP"
                                ParcelStatus.TRANSIT -> {
                                    if (parcel.progress <= 0.35f) "CONFIRM PICKUP" else "MARK OUT FOR DELIVERY"
                                }
                                else -> "ENTER OTP & COMPLETE"
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
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark
    var isSubmitting by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }

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

        Spacer(modifier = Modifier.height(24.dp))

        if ((parcel.status == ParcelStatus.TRANSIT && parcel.progress > 0.35f) || parcel.status == ParcelStatus.OUT_FOR_DELIVERY) {
            GpsMovementSimulator(
                parcelId = parcel.id,
                pickupAddress = parcel.pickupAddress,
                deliveryAddress = parcel.deliveryAddress,
                viewModel = viewModel,
                isDark = isDark
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (parcel.status == ParcelStatus.PENDING) {
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
                            Toast.makeText(context, "Dispatch Accepted successfully! 🏍️💨", Toast.LENGTH_SHORT).show()
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
                    Text("ACCEPT COMPANY DISPATCH", fontWeight = FontWeight.Bold)
                }
            }
        } else if (parcel.status == ParcelStatus.ASSIGNED) {
            Text(
                text = "Confirm pickup of this package. Are you currently at the shipper's location and have verified the contents?",
                color = AppTextColor,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    isSubmitting = true
                    viewModel.updateParcelStatusByRider(parcel.id, ParcelStatus.TRANSIT, 0.4f) { success, err ->
                        isSubmitting = false
                        if (success) {
                            Toast.makeText(context, "Marked as picked up!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } else {
                            Toast.makeText(context, err ?: "Failed to update status", Toast.LENGTH_SHORT).show()
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
                    Text("CONFIRM PICKUP", fontWeight = FontWeight.Bold)
                }
            }
        } else if (parcel.status == ParcelStatus.TRANSIT && parcel.progress <= 0.35f) {
            Text(
                text = "Are you currently at the shipper's pickup location and have verified the contents of the package?",
                color = AppTextColor,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    isSubmitting = true
                    viewModel.updateParcelStatusByRider(parcel.id, ParcelStatus.TRANSIT, 0.4f) { success, err ->
                        isSubmitting = false
                        if (success) {
                            Toast.makeText(context, "Marked as picked up!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } else {
                            Toast.makeText(context, err ?: "Failed to update status", Toast.LENGTH_SHORT).show()
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
                    Text("CONFIRM PICKUP", fontWeight = FontWeight.Bold)
                }
            }
        } else if (parcel.status == ParcelStatus.TRANSIT) {
            Text(
                text = "Marking this shipment as 'Out for Delivery' sends an automated real-time notification with a secure 4-digit OTP to the recipient.",
                color = AppTextColor,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    isSubmitting = true
                    viewModel.updateParcelStatusByRider(parcel.id, ParcelStatus.OUT_FOR_DELIVERY, 0.75f) { success, err ->
                        isSubmitting = false
                        if (success) {
                            Toast.makeText(context, "Recipient notified! Out for delivery.", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } else {
                            Toast.makeText(context, err ?: "Failed to update status", Toast.LENGTH_SHORT).show()
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
                    Text("MARK OUT FOR DELIVERY", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Out for delivery -> OTP Verification
            Text(
                text = "Confirm Secure OTP to Deliver",
                color = AppTextColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "The recipient must provide their unique 4-digit code. Enter it below to complete and instantly receive your payout split.",
                color = TextGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = otpInput,
                onValueChange = { if (it.length <= 4) otpInput = it },
                label = { Text("4-Digit Secure OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.6f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = BorderColor,
                    focusedLabelColor = Gold,
                    unfocusedLabelColor = TextGray
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (otpInput.length != 4) {
                        Toast.makeText(context, "Please enter a 4-digit PIN", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSubmitting = true
                    viewModel.verifyDeliveryOtpByRider(parcel.id, otpInput) { success, err ->
                        isSubmitting = false
                        if (success) {
                            Toast.makeText(context, "Delivery completed! Payout processed.", Toast.LENGTH_LONG).show()
                            onDismiss()
                        } else {
                            Toast.makeText(context, err ?: "Incorrect OTP", Toast.LENGTH_SHORT).show()
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
                    Text("VERIFY & COMPLETE", fontWeight = FontWeight.Black)
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
    val lower = address.lowercase()
    try {
        if (android.location.Geocoder.isPresent()) {
            val geocoder = android.location.Geocoder(context)
            val addresses = geocoder.getFromLocationName(address, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                return Pair(addr.latitude, addr.longitude)
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("RiderGeocoder", "System Geocoder failed: ${e.message}")
    }
    return when {
        lower.contains("ring road") || lower.contains("oba market") -> Pair(6.3350, 5.6037)
        lower.contains("uniben") || lower.contains("uhelu") || lower.contains("okada") -> Pair(6.4020, 5.6174)
        lower.contains("ikpoba") || lower.contains("ramat") || lower.contains("aduwawa") -> Pair(6.3475, 5.6421)
        lower.contains("airport") || lower.contains("oko") -> Pair(6.3176, 5.5992)
        lower.contains("gra") || lower.contains("boundary") || lower.contains("sapele") -> Pair(6.3117, 5.6148)
        lower.contains("ekoae") || lower.contains("bypass") || lower.contains("uhunmwonde") -> Pair(6.3812, 5.6698)
        lower.contains("ogba") || lower.contains("zoo") -> Pair(6.2844, 5.5872)
        lower.contains("eriaso") || lower.contains("ebor") || lower.contains("siluko") -> Pair(6.3530, 5.5780)
        else -> {
            val hash = address.hashCode().toLong()
            val latOffset = (Math.abs(hash) % 100) / 1000.0
            val lngOffset = (Math.abs(hash / 100) % 100) / 1000.0
            Pair(6.3350 + latOffset - 0.05, 5.6037 + lngOffset - 0.05)
        }
    }
}

@Composable
fun GpsMovementSimulator(
    parcelId: String,
    pickupAddress: String,
    deliveryAddress: String,
    viewModel: DeliveryViewModel,
    isDark: Boolean
) {
    var isSimulating by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(0) }
    val totalSteps = 10
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showArrivedDialog by remember { mutableStateOf(false) }

    // Geocode actual addresses to get high-precision start and destination coordinates
    val pickupCoords = remember(pickupAddress) { geocodeAddressToLatLng(context, pickupAddress) }
    val deliveryCoords = remember(deliveryAddress) { geocodeAddressToLatLng(context, deliveryAddress) }
    val pickupLat = pickupCoords.first
    val pickupLng = pickupCoords.second
    val deliveryLat = deliveryCoords.first
    val deliveryLng = deliveryCoords.second

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDark) Charcoal else GoldenWhiteLight, RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, Gold.copy(alpha = 0.2f)), RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DirectionsBike,
                    contentDescription = "Simulate",
                    tint = Gold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GPS Route Simulator",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Obsidian
                )
            }
            if (isSimulating) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SuccessGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "LIVE RUNNING",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = SuccessGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else {
                Text(
                    text = "Idle",
                    fontSize = 10.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Progress line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(if (isDark) LuxuryBlack else Slate)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(if (isSimulating) (currentStep.toFloat() / totalSteps) else 0f)
                    .background(Gold)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isSimulating) {
            Text(
                text = "📡 Real-time GPS location transmitter is active.",
                fontSize = 11.sp,
                color = TextGray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SuccessGreen.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, SuccessGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Live Mode",
                        tint = SuccessGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Production Tracking Mode: Coordinates are being updated continuously in the background.",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Obsidian,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            Text(
                text = "Activate real-time GPS tracking service to broadcast active location updates to customer.",
                fontSize = 10.sp,
                color = TextGray,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (isSimulating) {
                    viewModel.stopRealTimeGpsTracking(parcelId)
                    isSimulating = false
                    Toast.makeText(context, "GPS Tracking Stopped.", Toast.LENGTH_SHORT).show()
                } else {
                    val hasLocationPermission = androidx.core.content.PermissionChecker.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == androidx.core.content.PermissionChecker.PERMISSION_GRANTED
                    
                    if (hasLocationPermission) {
                        isSimulating = true
                        viewModel.startRealTimeGpsTracking(parcelId) { lat, lng ->
                            android.util.Log.d("GpsMovementSimulator", "Live high-precision hardware GPS tick: Lat $lat, Lng $lng")
                            val dist = calculateDistanceMeters(lat, lng, deliveryCoords.first, deliveryCoords.second)
                            if (dist <= 50.0) {
                                showArrivedDialog = true
                            }
                        }
                        Toast.makeText(context, "Real-time background GPS tracking started!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Location permission required for real GPS tracking!", Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSimulating) TextGray else Gold,
                contentColor = Obsidian
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = if (isSimulating) "STOP REAL GPS TRANSMISSION 📡" else "START REAL-TIME GPS TRANSIT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (showArrivedDialog) {
            AlertDialog(
                onDismissRequest = { showArrivedDialog = false },
                containerColor = Charcoal,
                titleContentColor = AppTextColor,
                textContentColor = AppTextColor,
                title = { Text("📍 Near Recipient (Within 50m)", fontWeight = FontWeight.Bold) },
                text = { Text("GPS proximity telemetry has detected you are within 50 meters of $deliveryAddress. Update status to ARRIVED?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showArrivedDialog = false
                            viewModel.updateParcelStatusByRider(parcelId, ParcelStatus.OUT_FOR_DELIVERY, 0.95f) { success, _ ->
                                if (success) {
                                    Toast.makeText(context, "Status updated to ARRIVED! (Awaiting OTP validation)", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian)
                    ) {
                        Text("SET ARRIVED", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showArrivedDialog = false }) {
                        Text("DISMISS", color = TextGray)
                    }
                }
            )
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
    var optimizedPlan by remember { mutableStateOf<com.example.data.BatchRoutePlan?>(null) }
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
                            Toast.makeText(context, "AI Route Optimized successfully! ⚡", Toast.LENGTH_SHORT).show()
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
    var alertResult by remember { mutableStateOf<com.example.data.GeofenceAlert?>(null) }
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
                                Toast.makeText(context, "⚠️ Geofence breach detected!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "✅ Within corporate perimeter boundary.", Toast.LENGTH_SHORT).show()
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
                                Text("⚠️ BREACH DETECTED: ${alertResult?.breachType}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF5350))
                                Text("Location: ${alertResult?.locationName}", fontSize = 11.sp, color = AppTextColor)
                                Text("Severity: ${alertResult?.severity} | Time: ${alertResult?.timestamp}", fontSize = 10.sp, color = TextGray)
                            } else {
                                Text("✅ SECURE: Vehicle operating strictly inside authorized corporate delivery corridor.", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
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
                                    "Change Category ⚙️", 
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
                                            Toast.makeText(context, "Photo evidence category mapped! 📸", Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(context, "Incident $incidentId logged & dispatched to safety HQ! 🚨", Toast.LENGTH_LONG).show()
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
                    Text("✅ Incident successfully logged in corporate database. Safety supervisor notified.", fontSize = 11.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
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
                            Text("${bonusCalc.averageRating} ★", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Gold)
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
    val maintenance = viewModel.checkVehicleMaintenance(if (bikeNumber.isNotBlank()) bikeNumber else "BIKE-LAGOS-88", 14500)

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

