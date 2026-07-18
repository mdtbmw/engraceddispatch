package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.TextUnit
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Map
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.animateColorAsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.animate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DirectionsBike
import coil.compose.rememberAsyncImagePainter
import com.example.data.Parcel
import com.example.data.ParcelStatus
import com.example.ui.components.BottomNav
import com.example.ui.components.Box3D
import com.example.ui.components.QuiltedBackground
import com.example.ui.components.ShimmerBox
import com.example.ui.components.AppModalBottomSheet
import com.example.ui.theme.*
import com.example.viewmodel.DeliveryViewModel
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource

class WavyLeftShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val waveWidth = size.width * 0.18f // wave width on the left
            moveTo(waveWidth, 0f)
            
            // Draw smooth wave along the left edge
            cubicTo(
                x1 = 0f, y1 = size.height * 0.25f,
                x2 = waveWidth * 2f, y2 = size.height * 0.75f,
                x3 = 0f, y3 = size.height
            )
            
            // Draw other straight edges to close the shape on the right
            lineTo(size.width, size.height)
            lineTo(size.width, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun DashboardScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val activeViewMode by viewModel.activeViewMode.collectAsState()
    if (activeViewMode == "rider") {
        RiderDashboardScreen(viewModel = viewModel, onNavigate = onNavigate)
        return
    }

    val pendingShortcutRoute by viewModel.pendingShortcutRoute.collectAsState()
    LaunchedEffect(pendingShortcutRoute) {
        pendingShortcutRoute?.let { route ->
            onNavigate(route)
            viewModel.clearPendingShortcutRoute()
        }
    }

    val parcels by viewModel.parcels.collectAsState()
    val archivedParcelIds by viewModel.archivedParcelIds.collectAsState()
    var selectedFilter by remember { mutableStateOf("In Transit") } // 'All', 'In Transit', 'Delivered'
    var quickViewParcel by remember { mutableStateOf<Parcel?>(null) }

    val unarchivedParcels = remember(parcels, archivedParcelIds) {
        parcels.filter { it.id !in archivedParcelIds }
    }

    val filteredParcels = remember(unarchivedParcels, selectedFilter) {
        when (selectedFilter) {
            "In Transit" -> unarchivedParcels.filter { it.status == ParcelStatus.TRANSIT || it.status == ParcelStatus.OUT_FOR_DELIVERY }
            "Delivered" -> unarchivedParcels.filter { it.status == ParcelStatus.DELIVERED }
            else -> unarchivedParcels // "All"
        }
    }

    val completedParcels = remember(parcels, archivedParcelIds) {
        parcels.filter { it.status == ParcelStatus.DELIVERED && it.id !in archivedParcelIds }
    }
    
    val activeParcels = remember(unarchivedParcels) {
        unarchivedParcels.filter { it.status == ParcelStatus.TRANSIT }
    }
    val walletBalance by viewModel.walletBalance.collectAsState()
    val referralCode by viewModel.referralCode.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val isDark by viewModel.darkModeEnabled.collectAsState()
    val photoUrl by viewModel.photoUrl.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val firstName = remember(userName) { userName.trim().split(" ").firstOrNull() ?: userName }
    val firebaseConnected by viewModel.firebaseConnected.collectAsState()
    val aiTrafficCongested by viewModel.aiTrafficCongested.collectAsState()
    val loyaltyPoints by viewModel.loyaltyPoints.collectAsState()
    val deliveryCount by viewModel.deliveryCount.collectAsState()
    val welcomeGiftClaimed by viewModel.welcomeGiftClaimed.collectAsState()
    val isNewRegistration by viewModel.isNewRegistration.collectAsState()

    var showWelcomeGiftDialog by remember { mutableStateOf(false) }
    val showOnboardingTooltip by viewModel.showOnboardingTooltip.collectAsState()
    LaunchedEffect(welcomeGiftClaimed, isNewRegistration) {
        if (!welcomeGiftClaimed && isNewRegistration) {
            kotlinx.coroutines.delay(1200)
            showWelcomeGiftDialog = true
        }
    }

    var triggerConfetti by remember { mutableStateOf(false) }
    var previousDeliveryCount by remember { mutableStateOf(-1) }
    LaunchedEffect(deliveryCount) {
        if (previousDeliveryCount != -1 && deliveryCount > previousDeliveryCount) {
            val oldM1 = previousDeliveryCount >= 1
            val oldM10 = previousDeliveryCount >= 10
            val oldM50 = previousDeliveryCount >= 50

            val newM1 = deliveryCount >= 1
            val newM10 = deliveryCount >= 10
            val newM50 = deliveryCount >= 50

            if ((newM1 && !oldM1) || (newM10 && !oldM10) || (newM50 && !oldM50)) {
                triggerConfetti = true
                viewModel.showInAppNotification(
                    "Milestone Achieved! 🏆",
                    "Congratulations! You unlocked a premium delivery achievement milestone."
                )
            }
        }
        previousDeliveryCount = deliveryCount
    }

    val headerContentColor = Color.White
    val headerContentSecondaryColor = Color.White.copy(alpha = 0.7f)
    val notificationBgColor = Color.White.copy(alpha = 0.1f)
    val profileBorderColor = Gold
    val quiltedLineColor = Color.White.copy(alpha = 0.04f)
    
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Infinite Auto-scroll state for Hero Carousel
    var currentHeroPage by remember { mutableStateOf(0) }
    val carouselSlides = listOf(
        CarouselSlide(
            title = "Move Anything,\nAnywhere",
            desc = "Premium, instant delivery at your doorstep.",
            imageUrl = "https://images.unsplash.com/photo-1512418491527-6f55e1112fb1?q=80&w=800&auto=format&fit=crop"
        ),
        CarouselSlide(
            title = "Supercharged\nExpress Network",
            desc = "City-wide delivery in under 45 minutes.",
            imageUrl = "https://images.unsplash.com/photo-1516541196182-6bdd0514013b?q=80&w=800&auto=format&fit=crop"
        ),
        CarouselSlide(
            title = "Fully Protected\nIn-Transit Guarantee",
            desc = "Live map tracking and automatic insurance.",
            imageUrl = "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?q=80&w=800&auto=format&fit=crop"
        )
    )

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(4000)
            currentHeroPage = (currentHeroPage + 1) % carouselSlides.size
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
            kotlinx.coroutines.delay(1500)
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

    val rawCircleProgress by remember {
        derivedStateOf { progress }
    }
    val circleProgress by animateFloatAsState(
        targetValue = rawCircleProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "circleProgressSpring"
    )

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

    Scaffold(
        containerColor = LuxuryBlack,
        bottomBar = { BottomNav(currentScreen = "Dashboard", onNavigate = onNavigate, activeViewMode = activeViewMode, userRole = userRole) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LuxuryBlack)
        ) {
            com.example.ui.components.ConfettiEffect(trigger = triggerConfetti, onFinished = { triggerConfetti = false })

            androidx.compose.animation.AnimatedVisibility(
                visible = showWelcomeGiftDialog,
                enter = fadeIn() + scaleIn(initialScale = 0.85f),
                exit = fadeOut() + scaleOut(targetScale = 0.85f),
                modifier = Modifier.zIndex(99f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Charcoal),
                        border = BorderStroke(1.5.dp, if (isDark) Gold else Obsidian)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Sparkling Header
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(if (isDark) Gold.copy(alpha = 0.15f) else Obsidian.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CardGiftcard,
                                    contentDescription = "Welcome Gift",
                                    tint = if (isDark) Gold else Obsidian,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "ENGRACED DISPATCH",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Gold else Obsidian,
                                letterSpacing = 2.sp
                            )
                            
                            Text(
                                text = "Welcome Reward Pack! 🎁",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = AppTextColor,
                                modifier = Modifier.padding(vertical = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            
                            Text(
                                text = "As a newly registered elite member, claim your instant premium delivery activation gift:",
                                fontSize = 12.sp,
                                color = TextGray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // ₦15,000 Delivery Credit Card
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isDark) Gold.copy(alpha = 0.08f) else Obsidian.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                    .border(1.dp, if (isDark) Gold.copy(alpha = 0.3f) else Slate, RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(if (isDark) Gold else Obsidian, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "₦",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isDark) Obsidian else Color.White // STRICT: No white on gold!
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "₦15,000.00",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isDark) Gold else Obsidian
                                    )
                                    Text(
                                        text = "Free Delivery Credit Balance",
                                        fontSize = 11.sp,
                                        color = TextGray
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // 100 Elite Loyalty Coins Card
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isDark) Gold.copy(alpha = 0.08f) else Obsidian.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                    .border(1.dp, if (isDark) Gold.copy(alpha = 0.3f) else Slate, RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(if (isDark) Gold else Obsidian, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "Coins",
                                        tint = if (isDark) Obsidian else Color.White // STRICT: No white on gold!
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "100 COINS",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isDark) Gold else Obsidian
                                    )
                                    Text(
                                        text = "Elite Loyalty Token Reward",
                                        fontSize = 11.sp,
                                        color = TextGray
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Claim Button
                            Button(
                                onClick = {
                                    viewModel.claimWelcomeGift()
                                    triggerConfetti = true
                                    showWelcomeGiftDialog = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDark) Gold else Obsidian,
                                    contentColor = if (isDark) Obsidian else Color.White // STRICT: No white on gold!
                                )
                            ) {
                                Text(
                                    text = "Claim My Welcome Gift 🎁",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "PREMIUM LOGISTICS & DISPATCH",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) GoldLight.copy(alpha = 0.6f) else TextGray,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }

            val showOnboardingTooltip by viewModel.showOnboardingTooltip.collectAsState()
            val showOnboardingOverlay = showOnboardingTooltip && !showWelcomeGiftDialog

            androidx.compose.animation.AnimatedVisibility(
                visible = showOnboardingOverlay,
                enter = fadeIn() + scaleIn(initialScale = 0.85f),
                exit = fadeOut() + scaleOut(targetScale = 0.85f),
                modifier = Modifier.zIndex(98f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Charcoal),
                        border = BorderStroke(1.5.dp, if (isDark) Gold else Obsidian)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(if (isDark) Gold.copy(alpha = 0.15f) else Obsidian.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Map,
                                    contentDescription = "Onboarding Guide",
                                    tint = if (isDark) Gold else Obsidian,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "ONBOARDING GUIDE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Gold else Obsidian,
                                letterSpacing = 2.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Tracking & Real-Time Maps",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = AppTextColor,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Feature 1: Formatting
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(if (isDark) Gold.copy(alpha = 0.1f) else Obsidian.copy(alpha = 0.05f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.QrCodeScanner,
                                        contentDescription = null,
                                        tint = if (isDark) Gold else Obsidian,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Smart Tracking ID Input",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppTextColor
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Type tracking IDs on the tracking screens; fields automatically insert hyphens (XXXX-XXXX-XXXX) for perfect readability.",
                                        fontSize = 11.sp,
                                        color = TextGray,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Feature 2: Map
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(if (isDark) Gold.copy(alpha = 0.1f) else Obsidian.copy(alpha = 0.05f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Navigation,
                                        contentDescription = null,
                                        tint = if (isDark) Gold else Obsidian,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Interactive Real-Time Maps",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppTextColor
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Tap on any active shipment or the Tracking tab to open live Mapbox-powered Leaflet & D3 maps featuring interactive Zoom, satellite overlays, and driver ETA.",
                                        fontSize = 11.sp,
                                        color = TextGray,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            Button(
                                onClick = {
                                    viewModel.dismissOnboardingTooltip()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDark) Gold else Obsidian,
                                    contentColor = if (isDark) Obsidian else Color.White
                                )
                            ) {
                                Text(
                                    text = "Got It, Let's Track! 🚀",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "ENGRACED DISPATCH • PREMIUM LOGISTICS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLight.copy(alpha = 0.6f),
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                }
            }

            // Subtle blurry gold light backgrounds (glowing effect matching docker)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Gold.copy(alpha = if (isDark) 0.04f else 0.02f),
                                Color.Transparent
                            ),
                            radius = 1200f
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Gold.copy(alpha = if (isDark) 0.05f else 0.03f),
                                Color.Transparent
                            ),
                            radius = 1200f
                        )
                    )
            )

            val isSandbox by viewModel.isSandboxEnvironment.collectAsState()

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .nestedScroll(nestedScrollConnection),
                contentPadding = PaddingValues(top = headerHeightDp + 16.dp, bottom = 140.dp)
            ) {

                // 1. HERO CAROUSEL CARD WITH STACKED CARD SLIDER DESIGN
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    var dragOffset by remember { mutableFloatStateOf(0f) }
                    val animatedDragOffset by animateFloatAsState(
                        targetValue = dragOffset,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "dragOffsetAnimation"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        if (dragOffset > 100f) {
                                            // Swipe Right -> Prev
                                            currentHeroPage = (currentHeroPage - 1 + carouselSlides.size) % carouselSlides.size
                                        } else if (dragOffset < -100f) {
                                            // Swipe Left -> Next
                                            currentHeroPage = (currentHeroPage + 1) % carouselSlides.size
                                        }
                                        dragOffset = 0f
                                    },
                                    onDragCancel = {
                                        dragOffset = 0f
                                    },
                                    onHorizontalDrag = { _, dragAmount ->
                                        dragOffset += dragAmount
                                    }
                                )
                            },
                        contentAlignment = Alignment.TopCenter
                    ) {
                        // Sort slides so the top one (relativeIndex == 0) is drawn last (on top)
                        val sortedIndices = carouselSlides.indices.toList().sortedByDescending { idx ->
                            (idx - currentHeroPage + carouselSlides.size) % carouselSlides.size
                        }

                        sortedIndices.forEach { index ->
                            val relativeIndex = (index - currentHeroPage + carouselSlides.size) % carouselSlides.size
                            
                            // Layout scaling and shifting
                            val scaleFactor = when (relativeIndex) {
                                0 -> 1.0f
                                1 -> 0.92f
                                else -> 0.84f
                            }
                            val yShift = when (relativeIndex) {
                                0 -> 0.dp
                                1 -> 18.dp
                                else -> 36.dp
                            }
                            val zIndexVal = when (relativeIndex) {
                                0 -> 3f
                                1 -> 2f
                                else -> 1f
                            }
                            val opacityVal = when (relativeIndex) {
                                0 -> 1.0f
                                1 -> 0.9f
                                else -> 0.8f
                            }

                            // Horizontal swipe shift only on the top foreground card
                            val xShift = if (relativeIndex == 0) {
                                animatedDragOffset.dp
                            } else {
                                0.dp
                            }

                            val animatedScale by animateFloatAsState(targetValue = scaleFactor, label = "scale_$index")
                            val animatedYShift by animateDpAsState(targetValue = yShift, label = "yShift_$index")
                            val animatedAlpha by animateFloatAsState(targetValue = opacityVal, label = "alpha_$index")

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .height(210.dp)
                                    .zIndex(zIndexVal)
                                    .graphicsLayer {
                                        scaleX = animatedScale
                                        scaleY = animatedScale
                                        translationX = xShift.toPx()
                                        translationY = animatedYShift.toPx()
                                        alpha = animatedAlpha
                                    },
                                shape = RoundedCornerShape(24.dp), // NO SHAPE MODIFICATIONS: Keep strictly at 24.dp
                                color = if (relativeIndex == 0) Obsidian else Gold,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (relativeIndex == 0) (if (isDark) BorderDark else Slate) else Gold.copy(alpha = 0.5f)
                                ),
                                shadowElevation = 0.dp
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    if (relativeIndex == 0) {
                                        // Top active card: Full high-quality photo & typography
                                        val slide = carouselSlides[index]
                                        Image(
                                            painter = rememberAsyncImagePainter(slide.imageUrl),
                                            contentDescription = slide.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(24.dp))
                                        )
                                        // Dark gradient overlay
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(
                                                            Color.Black.copy(alpha = 0.2f),
                                                            Color.Black.copy(alpha = 0.85f)
                                                        )
                                                    )
                                                )
                                        )
                                        // Content overlay
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(20.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Gold,
                                                modifier = Modifier.align(Alignment.Start)
                                            ) {
                                                Text(
                                                    text = "ENGRACED",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Obsidian,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = slide.title,
                                                    fontSize = 18.sp,
                                                    lineHeight = 22.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = slide.desc,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextGray.copy(alpha = 0.9f),
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        // Behind inactive cards: styled with absolute luxury Gold theme (No Gradient)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Gold),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(id = com.example.R.drawable.ic_logo),
                                                contentDescription = "Inactive slide logo",
                                                tint = Obsidian.copy(alpha = 0.15f),
                                                modifier = Modifier.size(80.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 3) {
                            val isSelected = i == currentHeroPage
                            val width = if (isSelected) 24.dp else 8.dp
                            Box(
                                modifier = Modifier
                                    .size(width = width, height = 8.dp)
                                    .background(
                                        color = if (isSelected) Gold else (if (isDark) Color(0xFF333333) else Color(0xFFE0E0E0)),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }

            // 2. STATS GRID
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Tile 1: Active
                        StatsTile(
                            title = "Active Shipments",
                            value = "${activeParcels.size}",
                            icon = Icons.Filled.LocalShipping,
                            iconColor = Gold,
                            modifier = Modifier.weight(1f)
                        )
                        // Tile 2: Completed
                        StatsTile(
                            title = "Completed Orders",
                            value = "${completedParcels.size}",
                            icon = Icons.Filled.CheckCircle,
                            iconColor = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Tile 3: Promo Savings (₦1,500 saved per booking)
                        val promoSavings = deliveryCount * 1500
                        StatsTile(
                            title = "Promo Savings",
                            value = "₦${String.format("%,d", promoSavings)} Saved",
                            icon = Icons.Filled.Redeem,
                            iconColor = Gold,
                            modifier = Modifier.weight(1.3f)
                        )
                        // Tile 4: Reward Points
                        StatsTile(
                            title = "Reward Points",
                            value = "$loyaltyPoints Pts",
                            icon = Icons.Filled.CardGiftcard,
                            iconColor = Gold,
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                }
            }





            // 3. SPECIAL OFFERS CAROUSEL (WHITE CARDS ONLY, CENTER SNAP)
            if (userRole != "rider") {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Special Offers",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppTextColor
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                val promoMockups = listOf(
                    PromoMock(title = "Festive Save", code = "EID2026", discount = "25% OFF", desc = "Enjoy 25% discount on Express bookings."),
                    PromoMock(title = "First Free", code = "FIRSTFREE", discount = "₦2,500.00", desc = "Get ₦2,500 instant credit on first parcel."),
                    PromoMock(title = "Weekend Rush", code = "WEEKEND30", discount = "30% OFF", desc = "Saturdays & Sundays economy save.")
                )

                var currentIndex by remember { mutableStateOf(0) }
                val swipeOffsetAnim = remember { Animatable(0f) }
                val coroutineScope = rememberCoroutineScope()

                // Auto-slide trigger to prevent coroutine cancellation issues during slide transitions
                var autoSlideTrigger by remember { mutableStateOf(0) }
                LaunchedEffect(autoSlideTrigger) {
                    if (autoSlideTrigger > 0) {
                        // Slide out to the left
                        swipeOffsetAnim.animateTo(-800f, animationSpec = tween(350))
                        currentIndex = (currentIndex + 1) % promoMockups.size
                        swipeOffsetAnim.snapTo(800f)
                        swipeOffsetAnim.animateTo(0f, animationSpec = spring(dampingRatio = 0.82f, stiffness = 300f))
                    }
                }

                LaunchedEffect(currentIndex) {
                    delay(5000L) // auto-play every 5 seconds
                    autoSlideTrigger++
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(215.dp) // Generous height for stacked offsets
                        .padding(horizontal = 24.dp)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    coroutineScope.launch {
                                        swipeOffsetAnim.snapTo(swipeOffsetAnim.value + dragAmount)
                                    }
                                },
                                onDragEnd = {
                                    coroutineScope.launch {
                                        val offset = swipeOffsetAnim.value
                                        if (offset > 200f) {
                                            // Swipe Right: animate out right and load previous card
                                            swipeOffsetAnim.animateTo(800f, animationSpec = tween(250))
                                            currentIndex = (currentIndex - 1 + promoMockups.size) % promoMockups.size
                                            swipeOffsetAnim.snapTo(-800f)
                                            swipeOffsetAnim.animateTo(0f, animationSpec = spring(dampingRatio = 0.82f, stiffness = 300f))
                                        } else if (offset < -200f) {
                                            // Swipe Left: animate out left and load next card
                                            swipeOffsetAnim.animateTo(-800f, animationSpec = tween(250))
                                            currentIndex = (currentIndex + 1) % promoMockups.size
                                            swipeOffsetAnim.snapTo(800f)
                                            swipeOffsetAnim.animateTo(0f, animationSpec = spring(dampingRatio = 0.82f, stiffness = 300f))
                                        } else {
                                            // Snap back to center
                                            swipeOffsetAnim.animateTo(0f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f))
                                        }
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Draw in reverse order (back card first) by sorting indices based on relativeIndex
                    val promoCount = promoMockups.size
                    val sortedIndices = promoMockups.indices.toList().sortedByDescending { idx ->
                        (idx - currentIndex + promoCount) % promoCount
                    }

                    sortedIndices.forEach { index ->
                        val relativeIndex = (index - currentIndex + promoCount) % promoCount
                        val promo = promoMockups[index]

                        // Determine scale, translation Y, and background color based on depth layer (relative index) dynamically
                        val swipeProgress = (kotlin.math.abs(swipeOffsetAnim.value) / 800f).coerceIn(0f, 1f)

                        val scale = when (relativeIndex) {
                            0 -> 1.0f
                            1 -> 0.90f + 0.10f * swipeProgress
                            else -> 0.80f + 0.10f * swipeProgress
                        }

                        val translationY = when (relativeIndex) {
                            0 -> 0.dp
                            1 -> (18f - 18f * swipeProgress).dp
                            else -> (36f - 18f * swipeProgress).dp
                        }

                        val baseColor = if (isDark) Charcoal else Color.White
                        val cardBgColor = if (relativeIndex == 0) {
                            baseColor
                        } else if (relativeIndex == 1) {
                            androidx.compose.ui.graphics.lerp(Gold, baseColor, swipeProgress)
                        } else {
                            Gold
                        }

                        val contentAlpha = if (relativeIndex == 0) {
                            1.0f - swipeProgress
                        } else if (relativeIndex == 1) {
                            swipeProgress
                        } else {
                            0f
                        }

                        val zIndexVal = when (relativeIndex) {
                            0 -> 3f
                            1 -> 2f
                            else -> 1f
                        }

                        val rotationZ = if (relativeIndex == 0) (swipeOffsetAnim.value / 40f) else 0f
                        val translationX = if (relativeIndex == 0) swipeOffsetAnim.value else 0f

                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = cardBgColor,
                            border = BorderStroke(1.dp, if (relativeIndex == 0) BorderLight else Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(165.dp)
                                .graphicsLayer {
                                    this.scaleX = scale
                                    this.scaleY = scale
                                    this.translationY = translationY.toPx()
                                    this.translationX = translationX
                                    this.rotationZ = rotationZ
                                }
                                .zIndex(zIndexVal),
                            shadowElevation = 0.dp
                        ) {
                            if (relativeIndex == 0) {
                                // Front Card: rich visual layout, texts, and custom Canvas isometric graphics
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .alpha(contentAlpha)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Left Details column
                                        Column(
                                            modifier = Modifier
                                                .weight(1.2f)
                                                .fillMaxHeight(),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = promo.title,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Obsidian
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Obsidian
                                                ) {
                                                    Text(
                                                        text = promo.discount,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Gold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = promo.desc,
                                                fontSize = 11.sp,
                                                color = Color(0xFF424242),
                                                fontWeight = FontWeight.Medium,
                                                lineHeight = 14.sp,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Obsidian, RoundedCornerShape(12.dp))
                                                    .padding(8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "CODE: ${promo.code}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .clickable {
                                                            Toast.makeText(context, "Promo code ${promo.code} applied successfully!", Toast.LENGTH_SHORT).show()
                                                        }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "Apply",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Gold
                                                    )
                                                }
                                            }
                                        }

                                        // Right Vector Illustration
                                        Box(
                                            modifier = Modifier
                                                .weight(0.8f)
                                                .fillMaxHeight(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Canvas(
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                val canvasWidth = size.width
                                                val canvasHeight = size.height

                                                // Glowing circle bg
                                                drawCircle(
                                                    color = Gold.copy(alpha = 0.12f),
                                                    radius = canvasWidth * 0.42f,
                                                    center = Offset(canvasWidth * 0.65f, canvasHeight * 0.5f)
                                                )

                                                // Speed trails
                                                val speedY1 = canvasHeight * 0.35f
                                                val speedY2 = canvasHeight * 0.52f
                                                val speedY3 = canvasHeight * 0.68f

                                                drawLine(
                                                    color = Gold.copy(alpha = 0.3f),
                                                    start = Offset(canvasWidth * 0.1f, speedY1),
                                                    end = Offset(canvasWidth * 0.45f, speedY1),
                                                    strokeWidth = 3f,
                                                    cap = StrokeCap.Round
                                                )
                                                drawLine(
                                                    color = Gold,
                                                    start = Offset(canvasWidth * 0.2f, speedY2),
                                                    end = Offset(canvasWidth * 0.55f, speedY2),
                                                    strokeWidth = 4f,
                                                    cap = StrokeCap.Round
                                                )
                                                drawLine(
                                                    color = Gold.copy(alpha = 0.3f),
                                                    start = Offset(canvasWidth * 0.15f, speedY3),
                                                    end = Offset(canvasWidth * 0.42f, speedY3),
                                                    strokeWidth = 3f,
                                                    cap = StrokeCap.Round
                                                )

                                                // Isometric Box
                                                val boxSize = canvasWidth * 0.28f
                                                val boxX = canvasWidth * 0.42f
                                                val boxY = canvasHeight * 0.28f

                                                // Front Left
                                                val pathFrontLeft = androidx.compose.ui.graphics.Path().apply {
                                                    moveTo(boxX, boxY + boxSize * 0.35f)
                                                    lineTo(boxX + boxSize * 0.5f, boxY + boxSize * 0.65f)
                                                    lineTo(boxX + boxSize * 0.5f, boxY + boxSize * 1.25f)
                                                    lineTo(boxX, boxY + boxSize * 0.95f)
                                                    close()
                                                }
                                                drawPath(pathFrontLeft, color = Obsidian)

                                                // Front Right
                                                val pathFrontRight = androidx.compose.ui.graphics.Path().apply {
                                                    moveTo(boxX + boxSize * 0.5f, boxY + boxSize * 0.65f)
                                                    lineTo(boxX + boxSize, boxY + boxSize * 0.35f)
                                                    lineTo(boxX + boxSize, boxY + boxSize * 0.95f)
                                                    lineTo(boxX + boxSize * 0.5f, boxY + boxSize * 1.25f)
                                                    close()
                                                }
                                                drawPath(pathFrontRight, color = Obsidian.copy(alpha = 0.85f))

                                                // Top Face
                                                val pathTopFace = androidx.compose.ui.graphics.Path().apply {
                                                    moveTo(boxX, boxY + boxSize * 0.35f)
                                                    lineTo(boxX + boxSize * 0.5f, boxY)
                                                    lineTo(boxX + boxSize, boxY + boxSize * 0.35f)
                                                    lineTo(boxX + boxSize * 0.5f, boxY + boxSize * 0.7f)
                                                    close()
                                                }
                                                drawPath(pathTopFace, color = Gold)

                                                // Box Tape Accent
                                                val pathTapeAccent = androidx.compose.ui.graphics.Path().apply {
                                                    moveTo(boxX + boxSize * 0.22f, boxY + boxSize * 0.48f)
                                                    lineTo(boxX + boxSize * 0.5f, boxY + boxSize * 0.62f)
                                                    lineTo(boxX + boxSize * 0.78f, boxY + boxSize * 0.48f)
                                                    lineTo(boxX + boxSize * 0.5f, boxY + boxSize * 0.34f)
                                                    close()
                                                }
                                                drawPath(pathTapeAccent, color = Color.White.copy(alpha = 0.65f))
                                            }
                                        }
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize())
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 3) {
                            val isSelected = i == currentIndex
                            val width = if (isSelected) 24.dp else 8.dp
                            Box(
                                modifier = Modifier
                                    .size(width = width, height = 8.dp)
                                    .background(
                                        color = if (isSelected) Gold else (if (isDark) Color(0xFF333333) else Color(0xFFE0E0E0)),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            }
            }

            // 3.5 LOYALTY & REWARDS DASHBOARD
            if (userRole != "rider") {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    val bonusClaimed by viewModel.dailyBonusClaimed.collectAsState()
                
                val currentTier = when {
                    loyaltyPoints < 100 -> "Bronze Club"
                    loyaltyPoints < 500 -> "Silver Tier"
                    loyaltyPoints < 1000 -> "Gold Elite"
                    else -> "Platinum VIP"
                }
                
                val tierProgress = when {
                    loyaltyPoints < 100 -> (loyaltyPoints / 100f).coerceIn(0f, 1f)
                    loyaltyPoints < 500 -> ((loyaltyPoints - 100) / 400f).coerceIn(0f, 1f)
                    loyaltyPoints < 1000 -> ((loyaltyPoints - 500) / 500f).coerceIn(0f, 1f)
                    else -> 1f
                }
                
                val nextTierDesc = when {
                    loyaltyPoints < 100 -> "100 Pts for Silver Tier"
                    loyaltyPoints < 500 -> "500 Pts for Gold Elite"
                    loyaltyPoints < 1000 -> "1,000 Pts for Platinum VIP"
                    else -> "Max Level Achieved 👑"
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = Obsidian, // Locked Obsidian background for premium high-contrast Gold details
                    border = BorderStroke(1.2.dp, Gold.copy(alpha = 0.3f)),
                    shadowElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    ) {
                        // Title Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = "VIP Status",
                                    tint = Gold,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Engraced VIP Rewards",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            Surface(
                                color = Gold.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = currentTier.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Gold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Points Counter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Accumulated Points",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextGray
                                )
                                Text(
                                    text = "$loyaltyPoints PTS",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Gold,
                                    lineHeight = 36.sp
                                )
                            }
                            
                            // Interactive Bonus claim button
                            Button(
                                onClick = {
                                    if (!bonusClaimed) {
                                        viewModel.claimDailyBonus()
                                        viewModel.showInAppNotification(
                                            "VIP Bonus Awarded! 🏆", 
                                            "100 Loyalty Points successfully added. Keep up the high dispatch count!"
                                        )
                                        Toast.makeText(context, "🏆 100 VIP Points Claimed!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = !bonusClaimed,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Gold,
                                    contentColor = Obsidian,
                                    disabledContainerColor = BorderDark,
                                    disabledContentColor = TextGray
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    text = if (bonusClaimed) "Claimed" else "Claim Daily",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Progress Bar to next tier
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tier Progress",
                                    fontSize = 10.sp,
                                    color = TextGray,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = nextTierDesc,
                                    fontSize = 10.sp,
                                    color = Gold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { tierProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = Gold,
                                trackColor = Charcoal
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        Divider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Milestone Achievements
                        Text(
                            text = "Milestone Achievements",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Milestone checklist
                        val milestones = listOf(
                            Triple("First Dispatch", "Complete your 1st delivery", deliveryCount >= 1),
                            Triple("Dispatcher Veteran", "Complete 10 deliveries", deliveryCount >= 10),
                            Triple("VIP Logistics Legend", "Complete 50 deliveries", deliveryCount >= 50)
                        )

                        milestones.forEach { (name, desc, achieved) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(enabled = achieved) {
                                        triggerConfetti = true
                                    }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (achieved) Icons.Filled.CheckCircle else Icons.Filled.Lock,
                                        contentDescription = null,
                                        tint = if (achieved) Gold else TextGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (achieved) Color.White else TextGray
                                        )
                                        Text(
                                            text = desc,
                                            fontSize = 10.sp,
                                            color = TextGray
                                        )
                                    }
                                }
                                if (achieved) {
                                    Text(
                                        text = "UNLOCKED",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Gold
                                    )
                                } else {
                                    Text(
                                        text = "LOCKED",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
            }

            // 4. REFERRAL CARD
            if (userRole != "rider") {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    val isSystemDark = MaterialTheme.colorScheme.background == BackgroundDark
                    val cardBorderColor = if (isSystemDark) Gold.copy(alpha = 0.3f) else Slate
                    val iconBgColor = Gold.copy(alpha = 0.15f)
                    val iconTintColor = Gold
                    val codeRowBorderColor = if (isSystemDark) Gold.copy(alpha = 0.3f) else Slate
                    val codeRowBgColor = if (isSystemDark) Charcoal else GoldenWhite
                    val codeTextColor = if (isSystemDark) Gold else Obsidian

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = Obsidian,
                    border = BorderStroke(1.dp, if (isSystemDark) BorderDark else Slate),
                    shadowElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Refer & Earn Credits",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Get ₦3,000 for every friend who signs up using your code.",
                            fontSize = 12.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(iconBgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Redeem, null, tint = iconTintColor, modifier = Modifier.size(18.dp))
                            }

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = 1.dp,
                                        color = codeRowBorderColor,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(codeRowBgColor)
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(referralCode))
                                        Toast.makeText(context, "Referral code copied!", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = referralCode,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = codeTextColor
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Copy Code",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Filled.ContentCopy,
                                        contentDescription = null,
                                        tint = iconTintColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            }

            // Delivery Summary visual breakdown card
            item {
                DeliverySummaryCard(
                    parcels = parcels,
                    isDark = isDark
                )
            }

            // 5. ACTIVE DELIVERIES SECTION
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (selectedFilter) {
                            "In Transit" -> "Active Deliveries"
                            "Delivered" -> "Delivered History"
                            else -> "All Deliveries"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppTextColor
                    )
                    Text(
                        text = "View All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Obsidian,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Gold)
                            .clickable { onNavigate("OrderLogs") }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 5b. FILTERING BAR (All, In Transit, Delivered)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .background(
                            color = if (isDark) Charcoal else GoldenWhiteLight,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filters = listOf("All", "In Transit", "Delivered")
                    filters.forEach { filterOption ->
                        val isSelected = selectedFilter == filterOption
                        val tabBg = if (isSelected) {
                            if (isDark) Gold else Obsidian
                        } else {
                            Color.Transparent
                        }
                        val tabTextColor = if (isSelected) {
                            if (isDark) Obsidian else Color.White
                        } else {
                            if (isDark) TextGray else TextGray
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(tabBg)
                                .clickable { selectedFilter = filterOption }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filterOption,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = tabTextColor
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isRefreshing) {
                // High-craft, dynamic skeleton loader
                items(3) { index ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Charcoal)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ShimmerBox(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                ShimmerBox(
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .height(16.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ShimmerBox(
                                    modifier = Modifier
                                        .fillMaxWidth(0.4f)
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            ShimmerBox(
                                modifier = Modifier
                                    .size(width = 50.dp, height = 24.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            } else {
                // Beautiful Empty state placeholder
                if (filteredParcels.isEmpty()) {
                    item {
                        val emptyBorderColor = if (isDark) Gold.copy(alpha = 0.3f) else Slate
                        val blurryGoldBrush = Brush.linearGradient(
                            colors = listOf(
                                Charcoal,
                                Gold.copy(alpha = 0.08f)
                            )
                        )
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                                .background(blurryGoldBrush, RoundedCornerShape(28.dp)),
                            shape = RoundedCornerShape(28.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.2.dp, emptyBorderColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AnimatedParcelIllustration()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = when (selectedFilter) {
                                        "In Transit" -> "No Active Shipments"
                                        "Delivered" -> "No Delivered Shipments"
                                        else -> "No Shipments Found"
                                    },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTextColor
                                )
                                Text(
                                    text = when (selectedFilter) {
                                        "In Transit" -> "You don't have any shipments on the road right now."
                                        "Delivered" -> "You don't have any completed deliveries yet."
                                        else -> "Your shipment list is currently empty."
                                    },
                                    fontSize = 12.sp,
                                    color = TextGray,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                                )
                                if (userRole != "rider") {
                                    Button(
                                        onClick = { onNavigate("SendParcel") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                                        shape = RoundedCornerShape(18.dp)
                                    ) {
                                        Text("Ship Something Now", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Obsidian)
                                    }
                                }
                            }
                        }
                    }
                }

                // Parcels List cards with Swipe-to-Archive
                items(filteredParcels, key = { it.id }) { parcel ->
                    SwipeToArchiveBox(
                        key = parcel.id,
                        onArchive = {
                            viewModel.archiveParcel(parcel.id)
                            Toast.makeText(context, "Parcel ${parcel.id} archived", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        ParcelCard(
                            parcel = parcel,
                            onClick = {
                                viewModel.selectParcelForTracking(parcel.id)
                                onNavigate("ActiveTracking")
                            },
                            onQuickView = { quickViewParcel = it },
                            onCopyTrackingId = { id -> viewModel.showCustomToast("Tracking ID copied: $id") }
                        )
                    }
                }
            }

            // 6. RECENT DELIVERIES (Last 3)
            val recentParcels = completedParcels.take(3)
            if (recentParcels.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Recent History",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppTextColor,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                items(recentParcels) { parcel ->
                    val recentImageBgColor = if (isDark) Color(0xFF1D1D1D) else GoldenWhiteLight
                    val recentPriceTextColor = if (isDark) Gold else Obsidian
                    val cardBorderColor = if (isDark) Gold.copy(alpha = 0.15f) else Slate

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = AppSurface,
                        shadowElevation = 0.dp,
                        border = BorderStroke(1.dp, if (isDark) BorderDark else Slate)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(recentImageBgColor)
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(parcel.imageUrl),
                                        contentDescription = parcel.itemName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(parcel.itemName, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = AppTextColor)
                                    Text("ID: ${parcel.id} • ${parcel.dateString}", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${parcel.pickupAddress.substringBefore(",")} ➔ ${parcel.deliveryAddress.substringBefore(",")}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextGray
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("₦${String.format("%,.2f", parcel.price)}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = recentPriceTextColor)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0x154CAF50)
                                    ) {
                                        Text(
                                            text = "Delivered",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF4CAF50),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            if (userRole != "rider") {
                                Divider(color = if (isDark) BorderDark else Slate.copy(alpha = 0.5f), thickness = 1.dp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.populateDraftFromParcel(parcel)
                                            viewModel.saveDraftToPrefs(context)
                                            onNavigate("BookingForm")
                                        }
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "Book Again",
                                        tint = Gold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Book This Route Again",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Gold
                                    )
                                }
                            }
                        }
                    }
                }
            }
            }

            // FIXED HEADER OVERLAY
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeightDp)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Obsidian)
            ) {
                QuiltedBackground(
                    modifier = Modifier.matchParentSize(),
                    lineColor = quiltedLineColor
                ) {}

                // 1. Expanded Content
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
                        // Profile & Notification Icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .graphicsLayer { rotationZ = progress * 360f }
                                    .drawBehind {
                                        drawCircle(
                                            color = profileBorderColor,
                                            style = Stroke(width = 3.dp.toPx())
                                        )
                                        if (progress > 0f) {
                                            drawArc(
                                                color = Gold,
                                                startAngle = -90f,
                                                sweepAngle = progress * 360f,
                                                useCenter = false,
                                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                            )
                                        }
                                    }
                                    .padding(3.dp)
                                    .clip(CircleShape)
                                    .clickable { onNavigate("Profile") }
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(if (photoUrl.isNotEmpty()) photoUrl else "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&h=100&fit=crop"),
                                    contentDescription = "Profile Pic",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Customer Stats Swap (Elite Member / Sent Count)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CardGiftcard, "Reward Points", tint = Gold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("$loyaltyPoints Pts", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = headerContentColor)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.LocalShipping, "Parcels Sent", tint = Gold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("$deliveryCount Sent", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = headerContentColor)
                                }
                            }

                            // Notification Ring
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(notificationBgColor)
                                    .clickable { onNavigate("Notifications") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = "Notifications",
                                    tint = headerContentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-4).dp, y = 4.dp)
                                        .size(10.dp)
                                        .background(Gold, shape = CircleShape)
                                        .border(2.dp, Obsidian, CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        // Greeting Text
                        Column {
                            Text(
                                text = "Hello,",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = headerContentSecondaryColor
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$firstName!",
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = headerContentColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                WavingHand(isAtTopOrActive = isAtTop.value, fontSize = 32.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        val searchBarBg = if (isDark) Charcoal else GoldenWhiteLight
                        val searchBarTextColor = if (isDark) Color.White else Obsidian
                        val searchIconTint = if (isDark) Gold else Obsidian
                        val scanIconTint = if (isDark) Gold else Obsidian
                        val scanIconBg = if (isDark) Color(0xFF121212) else GoldenWhite
                        val searchBarBorderColor = if (isDark) Gold.copy(alpha = 0.3f) else Slate

                        // Search Input Bar (Fully Functional with updated sizes)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .background(searchBarBg, shape = RoundedCornerShape(24.dp))
                                .border(1.dp, searchBarBorderColor, RoundedCornerShape(24.dp))
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search, 
                                contentDescription = "Search", 
                                tint = searchIconTint, 
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        if (searchQuery.isNotBlank()) {
                                            val found = viewModel.parcels.value.find { 
                                                it.id.contains(searchQuery, ignoreCase = true) || 
                                                it.itemName.contains(searchQuery, ignoreCase = true) 
                                            }
                                            if (found != null) {
                                                viewModel.selectParcelForTracking(found.id)
                                                onNavigate("ActiveTracking")
                                            } else {
                                                Toast.makeText(context, "No parcel found matching '$searchQuery'", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                    .padding(8.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = com.example.util.FormatUtils.formatTrackingId(it) },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = searchBarTextColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Search
                                ),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        if (searchQuery.isNotBlank()) {
                                            val found = viewModel.parcels.value.find { 
                                                it.id.contains(searchQuery, ignoreCase = true) || 
                                                it.itemName.contains(searchQuery, ignoreCase = true) 
                                            }
                                            if (found != null) {
                                                viewModel.selectParcelForTracking(found.id)
                                                onNavigate("ActiveTracking")
                                            } else {
                                                Toast.makeText(context, "No parcel found matching '$searchQuery'", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                ),
                                cursorBrush = SolidColor(if (isDark) Gold else Obsidian),
                                modifier = Modifier.weight(1f),
                                decorationBox = { innerTextField ->
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Track your parcel (e.g. 70D)",
                                                color = TextGray,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(scanIconBg)
                                    .clickable { onNavigate("Scanner") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.QrCodeScanner,
                                    contentDescription = "Scan",
                                    tint = scanIconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Dual Action Grid (Wallet & Track Order) Inside Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                onClick = { onNavigate("Wallet") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(80.dp),
                                shape = RoundedCornerShape(24.dp),
                                color = Charcoal,
                                shadowElevation = 0.dp,
                                border = BorderStroke(1.dp, if (isDark) BorderDark else Slate)
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
                                            imageVector = Icons.Filled.AccountBalanceWallet,
                                            contentDescription = "Wallet",
                                            tint = Obsidian,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(verticalArrangement = Arrangement.Center) {
                                        Text(
                                            text = "Wallet Balance",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextGray,
                                            lineHeight = 13.sp,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = if (walletBalance <= 0.0) "₦0.00" else "₦${String.format("%,.2f", walletBalance)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = AppTextColor,
                                            lineHeight = 16.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                            DashboardActionBtn(
                                title = "Track order",
                                icon = Icons.Filled.Place,
                                onClick = { onNavigate("ActiveTracking") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(80.dp)
                            )
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .graphicsLayer { rotationZ = progress * 360f }
                                    .drawBehind {
                                        drawCircle(
                                            color = profileBorderColor,
                                            style = Stroke(width = 2.dp.toPx())
                                        )
                                        if (progress > 0f) {
                                            drawArc(
                                                color = Gold,
                                                startAngle = -90f,
                                                sweepAngle = progress * 360f,
                                                useCenter = false,
                                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                            )
                                        }
                                    }
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .clickable { onNavigate("Profile") }
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(if (photoUrl.isNotEmpty()) photoUrl else "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&h=100&fit=crop"),
                                    contentDescription = "Profile Pic",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Hello $firstName! ",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = headerContentColor
                                )
                                WavingHand(isAtTopOrActive = true, fontSize = 18.sp)
                            }
                        }

                        // Notification Ring
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(notificationBgColor)
                                .clickable { onNavigate("Notifications") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notifications",
                                tint = headerContentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-2).dp, y = 2.dp)
                                    .size(8.dp)
                                    .background(Gold, shape = CircleShape)
                                    .border(1.5.dp, Obsidian, CircleShape)
                            )
                        }
                    }
                }
            }

            // Pull to Refresh indicator on top of Header
            if (scrollOffset < 0f || isRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 8.dp)
                        .height(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .background(Charcoal.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                            .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Gold,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Refreshing...",
                                color = AppTextColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = if (Math.abs(scrollOffset) >= refreshThreshold) "Release to refresh" else "Pull to refresh",
                                color = AppTextColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            if (quickViewParcel != null) {
                ParcelDetailBottomSheet(
                    parcel = quickViewParcel!!,
                    isDark = isDark,
                    onDismiss = { quickViewParcel = null }
                )
            }
        }
    }
}

@Composable
fun StatsTile(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark

    Surface(
        modifier = modifier.height(84.dp),
        shape = RoundedCornerShape(24.dp),
        color = Gold,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Obsidian),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Obsidian.copy(alpha = 0.75f),
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Obsidian,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

private data class CarouselSlide(
    val title: String,
    val desc: String,
    val imageUrl: String
)

private data class PromoMock(
    val title: String,
    val code: String,
    val discount: String,
    val desc: String
)

@Composable
fun DashboardActionBtn(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Charcoal,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, if (isDark) BorderDark else Slate)
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
                Icon(icon, null, tint = Obsidian, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = AppTextColor)
        }
    }
}
@Composable
fun SwipeToArchiveBox(
    key: Any,
    onArchive: () -> Unit,
    content: @Composable () -> Unit
) {
    var offsetX by remember(key) { mutableStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        label = "swipeOffset",
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        )
    )
    var isArchived by remember(key) { mutableStateOf(false) }

    if (!isArchived) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            // Background revealed under the card
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFFE53935)) // Premium Red archive background
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Inbox,
                        contentDescription = "Archive",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Archive",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Foreground swipeable card
            Box(
                modifier = Modifier
                    .offset { androidx.compose.ui.unit.IntOffset(animatedOffsetX.roundToInt(), 0) }
                    .pointerInput(key) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (offsetX < -150f) {
                                    // Trigger archive
                                    offsetX = -1000f
                                    onArchive()
                                    isArchived = true
                                } else {
                                    offsetX = 0f
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                // Only swipe to the left (negative offset)
                                offsetX = (offsetX + dragAmount).coerceAtMost(0f)
                            }
                        )
                    }
            ) {
                content()
            }
        }
    }
}

@Composable
fun ParcelCard(
    parcel: Parcel,
    onClick: () -> Unit,
    onQuickView: (Parcel) -> Unit,
    onCopyTrackingId: (String) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark
    val innerBgColor = if (isDark) Color(0xFF1D1D1D) else GoldenWhiteLight
    val timelineBgLineColor = if (isDark) Color(0xFF2C2C2C) else Slate
    val endDotBorderColor = if (isDark) Color(0xFF2C2C2C) else Slate

    val cardBorderColor = if (isDark) Gold.copy(alpha = 0.15f) else Slate

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(32.dp),
        color = AppSurface,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, if (isDark) BorderDark else Slate)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: Icon, Details, volume stacking representation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(innerBgColor)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(parcel.imageUrl),
                            contentDescription = parcel.itemName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("ID: ${parcel.id}", fontWeight = FontWeight.Black, fontSize = 15.sp, color = AppTextColor)
                            Spacer(modifier = Modifier.width(6.dp))

                            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                            val context = androidx.compose.ui.platform.LocalContext.current
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF2C2C2C) else BorderLight)
                                    .clickable {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(parcel.id))
                                        onCopyTrackingId(parcel.id)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "Copy tracking number",
                                    tint = if (isDark) Gold else Obsidian,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF2C2C2C) else BorderLight)
                                    .clickable { onQuickView(parcel) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Place,
                                    contentDescription = "Quick view map",
                                    tint = if (isDark) Gold else Obsidian,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            val statusBgColor = if (isDark) Gold.copy(alpha = 0.15f) else GoldenWhiteLight
                            val statusTextColor = if (isDark) Gold else Obsidian
                            Text(
                                text = parcel.status.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusTextColor,
                                modifier = Modifier
                                    .background(statusBgColor, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(parcel.itemName, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextGray)
                    }
                }

                // Signature 3D Gold isometric stack representing volumes
                Box3D(size = 32.dp, count = if (parcel.weight > 2.0) 3 else 2)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress timeline matched exactly with the mockup
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                // Background tracking line aligned perfectly with the centers of the 70.dp columns
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 35.dp)
                        .height(4.dp)
                        .background(timelineBgLineColor, CircleShape)
                        .align(Alignment.Center)
                )

                // Filled tracking progress aligned perfectly with the centers of the 70.dp columns
                Box(
                    modifier = Modifier
                        .fillMaxWidth(parcel.progress.coerceIn(0.08f, 1.0f))
                        .padding(horizontal = 35.dp)
                        .height(4.dp)
                        .background(Gold, CircleShape)
                        .align(Alignment.CenterStart)
                )

                // Markers and labels Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Start Point (Dubai)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(70.dp)
                    ) {
                        Box(
                            modifier = Modifier.height(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Obsidian, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Booked",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Gold, CircleShape)
                                .border(3.dp, Charcoal, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Obsidian,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = parcel.pickupAddress.substringBefore(","),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGray
                        )
                    }

                    // Middle Point (Transit / status text)
                    val middleStatusText = when (parcel.status) {
                        ParcelStatus.PENDING -> "Pending"
                        ParcelStatus.ASSIGNED -> "Assigned"
                        ParcelStatus.TRANSIT -> "Transit"
                        ParcelStatus.OUT_FOR_DELIVERY -> "Out"
                        ParcelStatus.DELIVERED -> "Transit"
                        ParcelStatus.CANCELLED -> "Cancelled"
                    }
                    val isAtLeastMiddle = parcel.progress >= 0.5f
                    val middleDotBg = if (isAtLeastMiddle) Gold else Charcoal
                    val middleDotBorderColor = if (isAtLeastMiddle) {
                        if (isDark) Charcoal else Slate
                    } else {
                        if (isDark) Gold else Obsidian
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(70.dp)
                    ) {
                        Box(
                            modifier = Modifier.height(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val isTransitActive = parcel.status == ParcelStatus.TRANSIT || parcel.status == ParcelStatus.OUT_FOR_DELIVERY
                            val middleCapsuleBg = if (isTransitActive) Gold else Obsidian
                            val middleCapsuleText = if (isTransitActive) Obsidian else Color.White
                            Box(
                                modifier = Modifier
                                    .background(middleCapsuleBg, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = middleStatusText,
                                    color = middleCapsuleText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(middleDotBg, CircleShape)
                                .border(if (isAtLeastMiddle) 3.dp else 5.dp, middleDotBorderColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "To",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppTextColor
                        )
                    }

                    // End Point (Destination)
                    val isDelivered = parcel.status == ParcelStatus.DELIVERED || parcel.progress >= 0.98f
                    val endDotBg = if (isDelivered) Gold else Charcoal
                    val endDotBorderColorDynamic = if (isDelivered) {
                        if (isDark) Charcoal else Slate
                    } else {
                        endDotBorderColor
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(70.dp)
                    ) {
                        Box(
                            modifier = Modifier.height(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDelivered) {
                                Box(
                                    modifier = Modifier
                                        .background(Gold, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Delivered",
                                        color = Obsidian,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(endDotBg, CircleShape)
                                .border(if (isDelivered) 3.dp else 4.dp, endDotBorderColorDynamic, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDelivered) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Obsidian,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = parcel.deliveryAddress.substringBefore(","),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val bottomActionColor = if (isDark) Gold else Obsidian
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Track Delivery Details",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = bottomActionColor
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = bottomActionColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun WavingHand(
    isAtTopOrActive: Boolean = true,
    fontSize: TextUnit = 32.sp
) {
    val transition = rememberInfiniteTransition(label = "wave")
    val angle by transition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "angle"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                rotationZ = if (isAtTopOrActive) angle else 0f
                transformOrigin = TransformOrigin(0.3f, 0.9f)
            }
    ) {
        Text("👋", fontSize = fontSize)
    }
}

@Composable
fun DeliverySummaryCard(
    parcels: List<Parcel>,
    isDark: Boolean
) {
    val transitCount = parcels.count { it.status == ParcelStatus.TRANSIT || it.status == ParcelStatus.OUT_FOR_DELIVERY }
    val deliveredCount = parcels.count { it.status == ParcelStatus.DELIVERED }
    val pendingCount = parcels.count { it.status == ParcelStatus.CANCELLED }

    val total = transitCount + deliveredCount + pendingCount

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        shape = RoundedCornerShape(28.dp),
        color = Obsidian,
        border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.2f) else BorderLight),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Delivery Summary Dashboard",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontFamily = SpaceGrotesk
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Real-time breakdown of all your dispatch orders",
                fontSize = 12.sp,
                color = TextGray,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (total == 0) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No parcels tracked yet.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextGray
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 12.dp.toPx()
                            val sizeMin = size.minDimension
                            val adjustedSize = sizeMin - strokeWidth
                            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                            val rectSize = Size(adjustedSize, adjustedSize)

                            val transitPct = if (total > 0) transitCount.toFloat() / total else 0f
                            val deliveredPct = if (total > 0) deliveredCount.toFloat() / total else 0f
                            val pendingPct = if (total > 0) pendingCount.toFloat() / total else 0f

                            var startAngle = -90f

                            if (deliveredPct > 0) {
                                val sweepAngle = deliveredPct * 360f
                                drawArc(
                                    color = Gold,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = rectSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                startAngle += sweepAngle
                            }

                            if (transitPct > 0) {
                                val sweepAngle = transitPct * 360f
                                drawArc(
                                    color = Color(0xFFFFB300),
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = rectSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                startAngle += sweepAngle
                            }

                            if (pendingPct > 0) {
                                val sweepAngle = pendingPct * 360f
                                drawArc(
                                    color = Color(0xFF6B7280),
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = rectSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = total.toString(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontFamily = SpaceGrotesk
                            )
                            Text(
                                text = "TOTAL",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Gold)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Delivered",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "$deliveredCount (${if (total > 0) (deliveredCount * 100 / total) else 0}%)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFB300))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "In Transit",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "$transitCount (${if (total > 0) (transitCount * 100 / total) else 0}%)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB300)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF6B7280))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pending/Cancelled",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "$pendingCount (${if (total > 0) (pendingCount * 100 / total) else 0}%)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractivePlaceholderMap(
    pickupAddress: String,
    deliveryAddress: String,
    progress: Float,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    // Interactive Zoom and Pan states
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val primaryColor = if (isDark) Gold else Obsidian
    val secondaryColor = if (isDark) Obsidian else Color.White
    val pathColor = if (isDark) Gold.copy(alpha = 0.5f) else Obsidian.copy(alpha = 0.4f)
    val mapGridColor = if (isDark) BorderDark else Slate
    val mapBgColor = if (isDark) Charcoal else GoldenWhite

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(mapBgColor)
            .border(BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.15f) else Slate), RoundedCornerShape(24.dp))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 3.0f)
                    offset = if (scale > 1f) {
                        offset + pan
                    } else {
                        Offset.Zero
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawContext.transform.translate(offset.x, offset.y)
            drawContext.transform.scale(scale, scale, pivot = center)

            // 1. Draw Map Grid/City Layout (Stylized grid lines representing streets)
            val stepX = 80f
            val stepY = 80f
            for (x in 0..(size.width / stepX).toInt() + 2) {
                drawLine(
                    color = mapGridColor,
                    start = Offset(x * stepX, -size.height),
                    end = Offset(x * stepX, size.height * 2),
                    strokeWidth = 1f
                )
            }
            for (y in -((size.height / stepY).toInt())..(size.height / stepY).toInt() + 2) {
                drawLine(
                    color = mapGridColor,
                    start = Offset(-size.width, y * stepY),
                    end = Offset(size.width * 2, y * stepY),
                    strokeWidth = 1f
                )
            }

            // Stylized city block rectangles for realistic map visual depth
            drawRoundRect(
                color = if (isDark) Color(0xFF161616) else GoldenWhiteLight,
                topLeft = Offset(50f, 50f),
                size = Size(180f, 120f),
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawRoundRect(
                color = if (isDark) Color(0xFF161616) else GoldenWhiteLight,
                topLeft = Offset(300f, 80f),
                size = Size(220f, 140f),
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawRoundRect(
                color = if (isDark) Color(0xFF161616) else GoldenWhiteLight,
                topLeft = Offset(100f, 260f),
                size = Size(240f, 160f),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // 2. Define route keypoints
            val startPoint = Offset(size.width * 0.2f, size.height * 0.7f)
            val endPoint = Offset(size.width * 0.8f, size.height * 0.3f)
            
            // Route bezier control point to make it look curvy/natural
            val controlPoint = Offset(size.width * 0.4f, size.height * 0.2f)
            
            val routePath = Path().apply {
                moveTo(startPoint.x, startPoint.y)
                quadraticTo(controlPoint.x, controlPoint.y, endPoint.x, endPoint.y)
            }

            // Draw route line
            drawPath(
                path = routePath,
                color = pathColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 6f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                )
            )

            // 3. Draw courier position along the bezier route
            val t = progress.coerceIn(0f, 1f)
            val mt = 1f - t
            val courierX = mt * mt * startPoint.x + 2f * mt * t * controlPoint.x + t * t * endPoint.x
            val courierY = mt * mt * startPoint.y + 2f * mt * t * controlPoint.y + t * t * endPoint.y
            val courierPoint = Offset(courierX, courierY)

            // Draw Pickup Marker
            drawCircle(
                color = if (isDark) Gold else Obsidian,
                radius = 16f,
                center = startPoint
            )
            drawCircle(
                color = if (isDark) Obsidian else Color.White,
                radius = 8f,
                center = startPoint
            )

            // Draw Delivery Marker
            drawCircle(
                color = Color(0xFF4CAF50), // Standard green for destination
                radius = 16f,
                center = endPoint
            )
            drawCircle(
                color = Color.White,
                radius = 8f,
                center = endPoint
            )

            // Draw Courier Pulse Ring
            drawCircle(
                color = (if (isDark) Gold else Obsidian).copy(alpha = 0.3f),
                radius = 32f,
                center = courierPoint
            )
            
            // Draw Courier Vehicle/Dot
            drawCircle(
                color = if (isDark) Gold else Obsidian,
                radius = 12f,
                center = courierPoint
            )
        }

        // Overlay Interactive Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { scale = (scale + 0.3f).coerceAtMost(3f) },
                containerColor = if (isDark) Gold else Obsidian,
                contentColor = if (isDark) Obsidian else Color.White,
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            FloatingActionButton(
                onClick = { scale = (scale - 0.3f).coerceAtLeast(0.5f) },
                containerColor = if (isDark) Gold else Obsidian,
                contentColor = if (isDark) Obsidian else Color.White,
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        // Touch to pan instructions
        Surface(
            color = if (isDark) Obsidian.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.75f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(12.dp)
        ) {
            Text(
                text = "Pinch to zoom • Drag to pan",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextGray,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcelDetailBottomSheet(
    parcel: Parcel,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    AppModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Shipment Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Gold
                    )
                    Text(
                        text = "ID: ${parcel.id}",
                        fontSize = 13.sp,
                        color = GoldLight.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                val statusBgColor = Gold
                val statusTextColor = Obsidian
                Text(
                    text = parcel.status.name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusTextColor,
                    modifier = Modifier
                        .background(statusBgColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            // Interactive Styled Map
            InteractivePlaceholderMap(
                pickupAddress = parcel.pickupAddress,
                deliveryAddress = parcel.deliveryAddress,
                progress = parcel.progress,
                isDark = isDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )

            // Parcel Meta Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isDark) Charcoal else GoldenWhiteLight,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Item Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Item Name", fontSize = 13.sp, color = TextGray)
                    Text(parcel.itemName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Obsidian)
                }

                // Pickup
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Pickup Address", fontSize = 13.sp, color = TextGray)
                    Text(
                        text = if (parcel.pickupAddress.length > 25) parcel.pickupAddress.take(25) + "..." else parcel.pickupAddress,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Obsidian
                    )
                }

                // Delivery
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Delivery Address", fontSize = 13.sp, color = TextGray)
                    Text(
                        text = if (parcel.deliveryAddress.length > 25) parcel.deliveryAddress.take(25) + "..." else parcel.deliveryAddress,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Obsidian
                    )
                }

                // Courier Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Courier Dispatcher", fontSize = 13.sp, color = TextGray)
                    Text(parcel.courierName.ifEmpty { "Adebayo Richard" }, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Obsidian)
                }

                // Check if rider is assigned and display profile details
                if (parcel.status == ParcelStatus.ASSIGNED || parcel.status == ParcelStatus.TRANSIT || parcel.status == ParcelStatus.OUT_FOR_DELIVERY) {
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Active Dispatch Rider Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isDark) LuxuryBlack else Color.White,
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rider Avatar with gold ring
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Gold.copy(alpha = 0.2f))
                                    .border(1.5.dp, Gold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.DirectionsBike,
                                    contentDescription = "Rider Icon",
                                    tint = Gold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ACTIVE DISPATCH COURIER",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Gold
                                )
                                Text(
                                    text = parcel.courierName.ifEmpty { "Adebayo Richard" },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isDark) Color.White else Obsidian
                                )
                                Text(
                                    text = "Plate: ${parcel.riderBikeNumber.ifEmpty { "LASG-3392" }} • Rating: 4.9 ★",
                                    fontSize = 10.sp,
                                    color = TextGray,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Call Action Button
                            val context = LocalContext.current
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen.copy(alpha = 0.15f))
                                    .clickable {
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${parcel.courierPhone.ifEmpty { "+234800COURIER" }}"))
                                        context.startActivity(dialIntent)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Call,
                                    contentDescription = "Call Courier",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Delivery OTP Handover Shield (if out for delivery)
                    if (parcel.status == ParcelStatus.OUT_FOR_DELIVERY) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Gold.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Lock,
                                        contentDescription = "Secure OTP",
                                        tint = Gold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SECURE HANDOVER OTP",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Gold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                val displayOtp = parcel.otpCode.ifEmpty { "4982" }
                                Text(
                                    text = displayOtp,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isDark) Color.White else Obsidian,
                                    letterSpacing = 4.sp
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = "Share this 4-digit code with the dispatch rider ONLY when they arrive and verify your parcel contents.",
                                    fontSize = 9.sp,
                                    color = TextGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 6.dp)
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
fun AnimatedParcelIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "emptyState")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "offsetY"
    )
    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "rotateAngle"
    )

    Box(
        modifier = Modifier
            .size(100.dp)
            .graphicsLayer {
                translationY = offsetY.dp.toPx()
                rotationZ = rotateAngle
            },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2
            val cy = h / 2
            val goldColor = Color(0xFFE5A93B)

            // Draw floating box shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.15f),
                topLeft = Offset(cx - 30.dp.toPx(), h - 15.dp.toPx()),
                size = Size(60.dp.toPx(), 8.dp.toPx())
            )

            // Let's draw an isometric, beautifully detailed shipping box in vector lines
            val path = Path().apply {
                // Front Right Face
                moveTo(cx, cy - 10.dp.toPx())
                lineTo(cx + 30.dp.toPx(), cy - 22.dp.toPx())
                lineTo(cx + 30.dp.toPx(), cy + 12.dp.toPx())
                lineTo(cx, cy + 24.dp.toPx())
                close()
            }
            drawPath(path, color = goldColor.copy(alpha = 0.15f))
            drawPath(path, color = goldColor, style = Stroke(width = 2.dp.toPx()))

            val leftFace = Path().apply {
                // Front Left Face
                moveTo(cx, cy - 10.dp.toPx())
                lineTo(cx - 30.dp.toPx(), cy - 22.dp.toPx())
                lineTo(cx - 30.dp.toPx(), cy + 12.dp.toPx())
                lineTo(cx, cy + 24.dp.toPx())
                close()
            }
            drawPath(leftFace, color = goldColor.copy(alpha = 0.1f))
            drawPath(leftFace, color = goldColor, style = Stroke(width = 2.dp.toPx()))

            val topFace = Path().apply {
                // Top Lid
                moveTo(cx, cy - 10.dp.toPx())
                lineTo(cx + 30.dp.toPx(), cy - 22.dp.toPx())
                lineTo(cx, cy - 34.dp.toPx())
                lineTo(cx - 30.dp.toPx(), cy - 22.dp.toPx())
                close()
            }
            drawPath(topFace, color = goldColor.copy(alpha = 0.25f))
            drawPath(topFace, color = goldColor, style = Stroke(width = 2.dp.toPx()))

            // Draw center premium tape
            val tapePath = Path().apply {
                moveTo(cx - 10.dp.toPx(), cy - 14.dp.toPx())
                lineTo(cx, cy - 10.dp.toPx())
                lineTo(cx + 10.dp.toPx(), cy - 14.dp.toPx())
                lineTo(cx, cy - 18.dp.toPx())
                close()
            }
            drawPath(tapePath, color = goldColor)

            // Dynamic waves / sparkles around the box
            drawCircle(
                color = goldColor.copy(alpha = 0.4f),
                radius = 3.dp.toPx(),
                center = Offset(cx - 38.dp.toPx(), cy - 15.dp.toPx())
            )
            drawCircle(
                color = goldColor.copy(alpha = 0.4f),
                radius = 2.dp.toPx(),
                center = Offset(cx + 42.dp.toPx(), cy - 5.dp.toPx())
            )
            drawCircle(
                color = goldColor.copy(alpha = 0.3f),
                radius = 1.5f.dp.toPx(),
                center = Offset(cx - 20.dp.toPx(), cy - 36.dp.toPx())
            )
        }
    }
}
