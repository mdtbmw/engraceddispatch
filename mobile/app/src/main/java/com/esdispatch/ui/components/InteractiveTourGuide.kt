package com.esdispatch.ui.components

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.esdispatch.ui.theme.*
import com.esdispatch.util.SoundManager
import kotlinx.coroutines.launch

/**
 * Global Registry for Tour Target Bounds on Screen
 */
object TourTargetRegistry {
    val targetBounds = mutableStateMapOf<String, Rect>()
}

/**
 * Modifier to attach to any composable to make it a spotlight target in the feature tour.
 */
fun Modifier.tourSpotlightTarget(tag: String): Modifier = this.onGloballyPositioned { coordinates ->
    if (coordinates.isAttached) {
        TourTargetRegistry.targetBounds[tag] = coordinates.boundsInRoot()
    }
}

data class SpotlightStep(
    val stepIndex: Int,
    val tag: String,
    val title: String,
    val description: String,
    val primaryButtonText: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

/**
 * True Contextual Feature Spotlight Tour
 *
 * 1. Highlights the real UI feature with a cutout spotlight and pulsing gold halo.
 * 2. Positions a tooltip pop-up directly adjacent to the feature with a directional pointer arrow.
 * 3. LIGHT MODE BY DEFAULT (white card, Obsidian text, gold accents) unless the user has dark mode enabled.
 * 4. ONE-TIME ONLY: Persisted in SharedPreferences ("feature_spotlight_completed_v3").
 */
@Composable
fun InteractiveTourGuide(
    modifier: Modifier = Modifier,
    isDark: Boolean,
    forceShow: Boolean = false,
    listState: LazyListState? = null,
    onTourFinished: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences("esdispatch_tour_prefs", Context.MODE_PRIVATE)
    }
    val tourCompletedKey = "feature_spotlight_completed_v3"
    var isVisible by remember {
        mutableStateOf(forceShow || !prefs.getBoolean(tourCompletedKey, false))
    }

    LaunchedEffect(forceShow) {
        if (forceShow) isVisible = true
    }

    if (!isVisible) return

    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val steps = remember {
        listOf(
            SpotlightStep(
                stepIndex = 1,
                tag = "quick_actions",
                title = "Quick Actions: Wallet & Market",
                description = "Top up your escrow-protected balance for instant 1-tap bookings, or explore the verified merchant logistics equipment store.",
                primaryButtonText = "Next",
                icon = Hugeicons.Solid.Wallet
            ),
            SpotlightStep(
                stepIndex = 2,
                tag = "hero_carousel",
                title = "Featured Merchant Showcase",
                description = "Swipe through daily curated dispatch supplies, motorcycle gear, and exclusive partner promotions with direct checkout.",
                primaryButtonText = "Next",
                icon = Hugeicons.Solid.Storefront
            ),
            SpotlightStep(
                stepIndex = 3,
                tag = "stats_grid",
                title = "Real-Time Fleet & Order Metrics",
                description = "Instantly monitor your active shipments, completed deliveries, promo discount savings, and accumulated loyalty points.",
                primaryButtonText = "Next",
                icon = Hugeicons.Solid.Route
            ),
            SpotlightStep(
                stepIndex = 4,
                tag = "bottom_dock",
                title = "Zero-Lag Dock Navigation",
                description = "Glide smoothly between your Command Dashboard, Live Tracking Map, Wallet, Order Logs, and Account Settings.",
                primaryButtonText = "Got It",
                icon = Hugeicons.Solid.CheckCircle
            )
        )
    }

    var currentStepIndex by remember { mutableStateOf(0) }
    val currentStep = steps[currentStepIndex]

    // Fallback bounds if the component has not reported its measured bounds yet
    val measuredTargetRect = TourTargetRegistry.targetBounds[currentStep.tag]
    val targetRect = remember(measuredTargetRect, currentStep.tag, screenWidthPx, screenHeightPx) {
        measuredTargetRect ?: when (currentStep.tag) {
            "quick_actions" -> Rect(
                left = with(density) { 16.dp.toPx() },
                top = with(density) { 100.dp.toPx() },
                right = screenWidthPx - with(density) { 16.dp.toPx() },
                bottom = with(density) { 190.dp.toPx() }
            )
            "hero_carousel" -> Rect(
                left = with(density) { 16.dp.toPx() },
                top = with(density) { 200.dp.toPx() },
                right = screenWidthPx - with(density) { 16.dp.toPx() },
                bottom = with(density) { 430.dp.toPx() }
            )
            "stats_grid" -> Rect(
                left = with(density) { 16.dp.toPx() },
                top = with(density) { 440.dp.toPx() },
                right = screenWidthPx - with(density) { 16.dp.toPx() },
                bottom = with(density) { 620.dp.toPx() }
            )
            "bottom_dock" -> Rect(
                left = with(density) { 20.dp.toPx() },
                top = screenHeightPx - with(density) { 92.dp.toPx() },
                right = screenWidthPx - with(density) { 20.dp.toPx() },
                bottom = screenHeightPx - with(density) { 24.dp.toPx() }
            )
            else -> Rect(0f, 0f, screenWidthPx, 200f)
        }
    }

    // Smooth spring interpolation for the spotlight cutout transition between steps
    val animLeft by animateFloatAsState(
        targetValue = targetRect.left,
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f),
        label = "spotlightLeft"
    )
    val animTop by animateFloatAsState(
        targetValue = targetRect.top,
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f),
        label = "spotlightTop"
    )
    val animRight by animateFloatAsState(
        targetValue = targetRect.right,
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f),
        label = "spotlightRight"
    )
    val animBottom by animateFloatAsState(
        targetValue = targetRect.bottom,
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f),
        label = "spotlightBottom"
    )

    // Animated breathing halo around the spotlight target
    val infiniteTransition = rememberInfiniteTransition(label = "spotlightHalo")
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "haloAlpha"
    )
    val haloExpansion by infiniteTransition.animateFloat(
        initialValue = 2f,
        targetValue = with(density) { 12.dp.toPx() },
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "haloExpansion"
    )

    fun completeTour() {
        SoundManager.playSuccessArpeggio()
        prefs.edit().putBoolean(tourCompletedKey, true).apply()
        isVisible = false
        onTourFinished()
    }

    fun nextStep() {
        SoundManager.playClick()
        if (currentStepIndex < steps.lastIndex) {
            currentStepIndex++
            // Auto scroll lazy list to ensure the spotlighted feature is in focus
            if (listState != null) {
                coroutineScope.launch {
                    when (currentStepIndex) {
                        0, 1 -> listState.animateScrollToItem(0)
                        2 -> listState.animateScrollToItem(1)
                    }
                }
            }
        } else {
            completeTour()
        }
    }

    fun prevStep() {
        SoundManager.playClick()
        if (currentStepIndex > 0) {
            currentStepIndex--
            if (listState != null) {
                coroutineScope.launch {
                    when (currentStepIndex) {
                        0, 1 -> listState.animateScrollToItem(0)
                    }
                }
            }
        }
    }

    val cutoutPadding = with(density) { 8.dp.toPx() }
    val cornerRadius = with(density) { 18.dp.toPx() }

    // Determine whether tooltip should sit below or above the target based on available screen space
    val spaceAbovePx = (animTop - cutoutPadding).coerceAtLeast(0f)
    val spaceBelowPx = (screenHeightPx - (animBottom + cutoutPadding)).coerceAtLeast(0f)
    val minClearancePx = with(density) { 240.dp.toPx() }
    val placeBelow = if (spaceBelowPx >= minClearancePx) {
        true
    } else if (spaceAbovePx >= minClearancePx) {
        false
    } else {
        spaceBelowPx >= spaceAbovePx
    }

    // THEME PALETTE: Light Mode by default, Dark Mode only if isDark is active
    val cardBg = if (isDark) Color(0xFF1C1C1E) else GoldenWhiteSurface
    val cardBorder = if (isDark) Gold else Gold.copy(alpha = 0.5f)
    val titleColor = if (isDark) Color.White else Obsidian
    val descColor = if (isDark) Color(0xFFD1D5DB) else Color(0xFF4B5563)
    val badgeBg = if (isDark) Gold.copy(alpha = 0.18f) else Gold.copy(alpha = 0.15f)
    val badgeText = if (isDark) Gold else Obsidian
    val arrowColor = cardBg

    LaunchedEffect(currentStepIndex) {
        if (listState != null) {
            when (currentStepIndex) {
                0, 1 -> listState.animateScrollToItem(0)
                2 -> listState.animateScrollToItem(1)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1500f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* strictly absorb and block background interactions so the tour is not dismissed accidentally */ }
    ) {
        // 1. Scrim Canvas with transparent cutout (NO outline / NO artificial border)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        ) {
            // Darkened dimmed background
            drawRect(Color.Black.copy(alpha = 0.72f))

            val left = animLeft - cutoutPadding
            val top = animTop - cutoutPadding
            val width = (animRight - animLeft) + cutoutPadding * 2
            val height = (animBottom - animTop) + cutoutPadding * 2

            // Clean, borderless cutout spotlight showing the native underlying feature with NO outline stroke
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(width, height),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                blendMode = BlendMode.Clear
            )
        }

        // 2. Tooltip Pop-up Card with Directional Pointer Arrow
        val arrowWidthDp = 20.dp
        val arrowHeightDp = 10.dp
        val arrowCenterXPx = ((animLeft + animRight) / 2f).coerceIn(
            with(density) { 48.dp.toPx() },
            screenWidthPx - with(density) { 48.dp.toPx() }
        )

        // Intelligently calculate clamped vertical position for the tooltip so it never bleeds off-screen
        val safeTopMarginDp = 56.dp
        val safeBottomMarginDp = 96.dp // clears bottom floating navigation dock
        val screenHeightDp = configuration.screenHeightDp.dp

        val tooltipModifier = if (placeBelow) {
            val topOffsetDp = with(density) { (animBottom + cutoutPadding + 14.dp.toPx()).toDp() }
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = topOffsetDp.coerceIn(safeTopMarginDp, screenHeightDp - 260.dp))
                .padding(horizontal = 16.dp)
        } else {
            val bottomOffsetDp = with(density) { (screenHeightPx - (animTop - cutoutPadding) + 14.dp.toPx()).toDp() }
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomOffsetDp.coerceIn(safeBottomMarginDp, screenHeightDp - 260.dp))
                .padding(horizontal = 16.dp)
        }

        Column(
            modifier = tooltipModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pointer arrow pointing UP (if tooltip is below target)
            if (placeBelow) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    val arrowOffsetDp = with(density) { (arrowCenterXPx - 24.dp.toPx() - (arrowWidthDp.toPx() / 2f)).toDp() }
                    Canvas(
                        modifier = Modifier
                            .offset(x = arrowOffsetDp.coerceAtLeast(8.dp))
                            .size(arrowWidthDp, arrowHeightDp)
                    ) {
                        val path = Path().apply {
                            moveTo(size.width / 2f, 0f)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(path, color = arrowColor)
                        drawPath(path, color = cardBorder, style = Stroke(width = 1.5.dp.toPx()))
                    }
                }
            }

            // The Tooltip Pop-up Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = Color.Black.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.5.dp, cardBorder),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header row: Step pill & Skip Tour
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = badgeBg,
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "STEP ${currentStep.stepIndex} OF ${steps.size}",
                                color = badgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                letterSpacing = 1.sp
                            )
                        }

                        TextButton(
                            onClick = { completeTour() },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Skip",
                                color = TextGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title with leading icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Gold.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedHugeIcon(
                                icon = currentStep.icon,
                                tint = Gold,
                                size = 18.dp
                            )
                        }
                        Text(
                            text = currentStep.title,
                            color = titleColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Concise feature explanation
                    Text(
                        text = currentStep.description,
                        color = descColor,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Bottom Navigation Row: Indicator pills & Next button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Progress dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            steps.forEachIndexed { idx, _ ->
                                val isActive = idx == currentStepIndex
                                val dotWidth by animateDpAsState(
                                    targetValue = if (isActive) 18.dp else 6.dp,
                                    animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f),
                                    label = "dotWidth"
                                )
                                Box(
                                    modifier = Modifier
                                        .height(6.dp)
                                        .width(dotWidth)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            if (isActive) Gold 
                                            else (if (isDark) Color.White.copy(alpha = 0.25f) else Color(0xFFD1D5DB))
                                        )
                                )
                            }
                        }

                        // Buttons (Back & Next)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (currentStepIndex > 0) {
                                OutlinedButton(
                                    onClick = { prevStep() },
                                    modifier = Modifier.height(40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.5f) else Slate),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (isDark) Gold else Obsidian
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp)
                                ) {
                                    Text("Back", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { nextStep() },
                                modifier = Modifier.height(40.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Gold,
                                    contentColor = Obsidian // STRICT LOCK: Obsidian text on Gold background!
                                ),
                                contentPadding = PaddingValues(horizontal = 20.dp)
                            ) {
                                Text(
                                    text = currentStep.primaryButtonText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            // Pointer arrow pointing DOWN (if tooltip is above target)
            if (!placeBelow) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    val arrowOffsetDp = with(density) { (arrowCenterXPx - 24.dp.toPx() - (arrowWidthDp.toPx() / 2f)).toDp() }
                    Canvas(
                        modifier = Modifier
                            .offset(x = arrowOffsetDp.coerceAtLeast(8.dp))
                            .size(arrowWidthDp, arrowHeightDp)
                    ) {
                        val path = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width, 0f)
                            lineTo(size.width / 2f, size.height)
                            close()
                        }
                        drawPath(path, color = arrowColor)
                        drawPath(path, color = cardBorder, style = Stroke(width = 1.5.dp.toPx()))
                    }
                }
            }
        }
    }
}
