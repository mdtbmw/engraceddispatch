@file:Suppress("DEPRECATION")
package com.example.ui.screens

import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.rotate
import com.example.ui.components.QuiltedBackground
import com.example.ui.components.Box3D
import com.example.ui.theme.*
import com.example.viewmodel.DeliveryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

// ====================================================================================================
// 1. BRAND ANIMATED SPLASH SCREEN (LOGO ANIMATES IN -> SPRINGS -> ANIMATES OUT -> ONBOARDING)
// ====================================================================================================
@Composable
fun SplashScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    var animateIn by remember { mutableStateOf(false) }
    var animateOut by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = when {
            animateOut -> 85f
            animateIn -> 1f
            else -> 0f
        },
        animationSpec = if (animateOut) {
            tween(900, easing = EaseInQuart)
        } else {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        },
        label = "logoScale"
    )

    val screenScale by animateFloatAsState(
        targetValue = if (animateOut) 1.2f else 1.0f,
        animationSpec = tween(900, easing = EaseInQuart),
        label = "screenScale"
    )

    // Elegant breathing glow animation behind logo
    val glowTransition = rememberInfiniteTransition(label = "glowTransition")
    val glowAlpha by glowTransition.animateFloat(
        initialValue = 0.04f,
        targetValue = 0.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Subtle breath pulse for the logo to make it feel organic and standard
    val breathScale by glowTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        delay(300)
        animateIn = true
        delay(2200) // Beautiful brand viewing delay
        animateOut = true
        delay(550) // Wait for animate out to finish
        
        val prefs = context.getSharedPreferences("engraced_dispatch_prefs", android.content.Context.MODE_PRIVATE)
        val hasLocalUser = !prefs.getString("local_uid", "").isNullOrEmpty() && !prefs.getString("local_email", "").isNullOrEmpty()
        val hasFirebaseUser = com.example.data.FirebaseManager.auth?.currentUser != null && com.example.data.FirebaseManager.auth?.currentUser?.isAnonymous == false
        
        if (hasLocalUser || hasFirebaseUser) {
            onNavigate("Preloader/Dashboard")
        } else {
            onNavigate("Preloader/Onboarding")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark) // Keep solid black/obsidian premium feel
            .graphicsLayer {
                scaleX = screenScale
                scaleY = screenScale
            },
        contentAlignment = Alignment.Center
    ) {
        // Luxury Quilted pattern lines under splash
        QuiltedBackground(
            modifier = Modifier.fillMaxSize(),
            lineColor = Color.White.copy(alpha = 0.03f)
        ) {}

        // Ambient Gold Glow behind centered logo (Ultra clean, luxury feeling)
        Box(
            modifier = Modifier
                .size(320.dp)
                .alpha(if (animateOut) 0f else 1f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Gold.copy(alpha = glowAlpha),
                            Color.Transparent
                        )
                    )
                )
        )

        // Raw Logo Icon centered on its own, completely independent of any containers, scaling in/out fully
        Icon(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "Logo",
            tint = Gold,
            modifier = Modifier
                .align(Alignment.Center)
                .size(110.dp)
                .graphicsLayer {
                    val finalScale = if (animateOut) logoScale else logoScale * breathScale
                    scaleX = finalScale
                    scaleY = finalScale
                    alpha = if (animateOut) {
                        // Fade out very late in the transition so it doesn't vanish early
                        (1f - (logoScale / 85f)).coerceIn(0f, 1f)
                    } else {
                        1f
                    }
                }
        )
    }
}

// ====================================================================================================
// 2. DYNAMIC PRELOADER SYNC SCREEN (POST-LOGIN / SIGNUP ENGINE SYNCING WITH BIKE RIDING & GOLD SWIPE)
// ====================================================================================================
@Composable
fun BikeRiderAnimation(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bikeSpins")
    
    // Wheel spin rotation
    val wheelRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wheelRotation"
    )

    // Chassis bobbing up and down slightly
    val bobbingOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobbingOffset"
    )

    // Ground line offsets to simulate road movement
    val groundOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -100f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "groundOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f + 20f // base ground line level

            // 1. DRAW MOVING ROAD DOTS/DASHES under the bike
            val roadY = centerY + 30f
            val roadLength = 160.dp.toPx()
            val dashWidth = 30f
            val spaceWidth = 20f
            val totalDash = dashWidth + spaceWidth
            
            // Draw a solid dark road base first
            drawLine(
                color = Color.DarkGray.copy(alpha = 0.3f),
                start = Offset(centerX - roadLength, roadY),
                end = Offset(centerX + roadLength, roadY),
                strokeWidth = 6f
            )

            // Clip road area and draw moving dashes
            val startX = centerX - roadLength
            val endX = centerX + roadLength
            var currentX = startX + (groundOffset % totalDash)
            while (currentX < endX + totalDash) {
                val drawStartX = maxOf(currentX, startX)
                val drawEndX = minOf(currentX + dashWidth, endX)
                if (drawEndX > drawStartX) {
                    drawLine(
                        color = Gold.copy(alpha = 0.8f),
                        start = Offset(drawStartX, roadY),
                        end = Offset(drawEndX, roadY),
                        strokeWidth = 4f
                    )
                }
                currentX += totalDash
            }

            // 2. BIKE CHASSIS & GEOMETRY (with bobbing offset applied to bike body)
            val bobY = bobbingOffset
            val rearHubX = centerX - 50.dp.toPx()
            val frontHubX = centerX + 50.dp.toPx()
            val hubY = centerY + 10f

            val bottomBracketX = centerX - 10.dp.toPx()
            val bottomBracketY = centerY + 10f + bobY

            val seatX = centerX - 25.dp.toPx()
            val seatY = centerY - 25.dp.toPx() + bobY

            val handlebarX = centerX + 30.dp.toPx()
            val handlebarY = centerY - 45.dp.toPx() + bobY

            // 3. DRAW ROTATING WHEELS
            val wheelRadius = 22.dp.toPx()
            
            // Rear Wheel
            drawCircle(
                color = Color.DarkGray,
                radius = wheelRadius,
                center = Offset(rearHubX, hubY),
                style = Stroke(width = 8f)
            )
            drawCircle(
                color = Gold,
                radius = wheelRadius - 4f,
                center = Offset(rearHubX, hubY),
                style = Stroke(width = 2f)
            )
            // Rear Wheel spokes
            for (i in 0 until 4) {
                val angleRad = Math.toRadians((wheelRotation + (i * 45f)).toDouble())
                val cos = Math.cos(angleRad).toFloat()
                val sin = Math.sin(angleRad).toFloat()
                drawLine(
                    color = TextGray.copy(alpha = 0.6f),
                    start = Offset(rearHubX, hubY),
                    end = Offset(rearHubX + cos * wheelRadius, hubY + sin * wheelRadius),
                    strokeWidth = 3f
                )
            }

            // Front Wheel
            drawCircle(
                color = Color.DarkGray,
                radius = wheelRadius,
                center = Offset(frontHubX, hubY),
                style = Stroke(width = 8f)
            )
            drawCircle(
                color = Gold,
                radius = wheelRadius - 4f,
                center = Offset(frontHubX, hubY),
                style = Stroke(width = 2f)
            )
            // Front Wheel spokes
            for (i in 0 until 4) {
                val angleRad = Math.toRadians((wheelRotation + (i * 45f) + 22.5f).toDouble())
                val cos = Math.cos(angleRad).toFloat()
                val sin = Math.sin(angleRad).toFloat()
                drawLine(
                    color = TextGray.copy(alpha = 0.6f),
                    start = Offset(frontHubX, hubY),
                    end = Offset(frontHubX + cos * wheelRadius, hubY + sin * wheelRadius),
                    strokeWidth = 3f
                )
            }

            // 4. DRAW BIKE FRAME LINES
            // Rear stay
            drawLine(color = TextGray, start = Offset(rearHubX, hubY), end = Offset(bottomBracketX, bottomBracketY), strokeWidth = 5f)
            // Seat stay
            drawLine(color = TextGray, start = Offset(rearHubX, hubY), end = Offset(seatX, seatY), strokeWidth = 5f)
            // Chain tube
            drawLine(color = TextGray, start = Offset(bottomBracketX, bottomBracketY), end = Offset(seatX, seatY), strokeWidth = 6f)
            // Down tube
            drawLine(color = TextGray, start = Offset(bottomBracketX, bottomBracketY), end = Offset(handlebarX, handlebarY), strokeWidth = 6f)
            // Top tube
            drawLine(color = TextGray, start = Offset(seatX, seatY), end = Offset(handlebarX, handlebarY), strokeWidth = 5f)
            // Front fork
            drawLine(color = TextGray, start = Offset(handlebarX, handlebarY), end = Offset(frontHubX, hubY), strokeWidth = 5f)

            // Handlebar grip
            drawLine(
                color = Color.Black,
                start = Offset(handlebarX - 8.dp.toPx(), handlebarY - 4.dp.toPx()),
                end = Offset(handlebarX + 8.dp.toPx(), handlebarY - 4.dp.toPx()),
                strokeWidth = 8f
            )

            // Seat/Saddle
            drawRoundRect(
                color = Color.Black,
                topLeft = Offset(seatX - 12.dp.toPx(), seatY - 4.dp.toPx()),
                size = Size(24.dp.toPx(), 8.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // 5. DRAW GOLD DELIVERY BOX ON BACK
            val boxWidth = 36.dp.toPx()
            val boxHeight = 32.dp.toPx()
            val boxLeft = rearHubX - 10.dp.toPx()
            val boxTop = seatY - 14.dp.toPx()
            
            // Box support rack
            drawLine(color = Color.DarkGray, start = Offset(rearHubX, hubY), end = Offset(boxLeft + boxWidth/2f, boxTop + boxHeight), strokeWidth = 4f)
            drawLine(color = Color.DarkGray, start = Offset(seatX, seatY), end = Offset(boxLeft, boxTop + boxHeight/2f), strokeWidth = 4f)

            // Draw the Gold Box
            drawRoundRect(
                color = Gold,
                topLeft = Offset(boxLeft, boxTop),
                size = Size(boxWidth, boxHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
            // Box border
            drawRoundRect(
                color = Obsidian,
                topLeft = Offset(boxLeft, boxTop),
                size = Size(boxWidth, boxHeight),
                style = Stroke(width = 3f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
            // Box strap or detail line
            drawLine(
                color = Obsidian,
                start = Offset(boxLeft + boxWidth / 2f, boxTop),
                end = Offset(boxLeft + boxWidth / 2f, boxTop + boxHeight),
                strokeWidth = 3f
            )

            // 6. DRAW COURIER RIDER (SITTING/LEANING SEAT -> HANDLEBARS)
            // Rider spine/body
            val riderHipX = seatX + 2.dp.toPx()
            val riderHipY = seatY - 4.dp.toPx()
            val riderTorsoX = centerX + 10.dp.toPx()
            val riderTorsoY = centerY - 50.dp.toPx() + bobY

            // Torso
            drawLine(color = Obsidian, start = Offset(riderHipX, riderHipY), end = Offset(riderTorsoX, riderTorsoY), strokeWidth = 8f)
            
            // Rider Head/Helmet
            val headX = riderTorsoX + 5.dp.toPx()
            val headY = riderTorsoY - 15.dp.toPx()
            drawCircle(
                color = Gold,
                radius = 10.dp.toPx(),
                center = Offset(headX, headY)
            )
            // Helmet visor/detail
            drawArc(
                color = Color.Black,
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = true,
                topLeft = Offset(headX - 10.dp.toPx(), headY - 10.dp.toPx()),
                size = Size(20.dp.toPx(), 20.dp.toPx())
            )

            // Arm (Torso to handlebars)
            drawLine(color = Obsidian, start = Offset(riderTorsoX, riderTorsoY), end = Offset(handlebarX, handlebarY), strokeWidth = 5f)

            // Legs (Hip to Pedals)
            val pedalAngleRad = Math.toRadians((wheelRotation * 1.5f).toDouble())
            val pedalCos = Math.cos(pedalAngleRad).toFloat()
            val pedalSin = Math.sin(pedalAngleRad).toFloat()
            val crankLength = 8.dp.toPx()
            val pedalX = bottomBracketX + pedalCos * crankLength
            val pedalY = bottomBracketY + pedalSin * crankLength

            val kneeX = (riderHipX + pedalX) / 2f + 12.dp.toPx()
            val kneeY = (riderHipY + pedalY) / 2f - 4.dp.toPx()

            // Upper leg
            drawLine(color = Obsidian, start = Offset(riderHipX, riderHipY), end = Offset(kneeX, kneeY), strokeWidth = 5f)
            // Lower leg
            drawLine(color = Obsidian, start = Offset(kneeX, kneeY), end = Offset(pedalX, pedalY), strokeWidth = 5f)
            // Foot pedal line
            drawLine(color = Color.Black, start = Offset(pedalX - 6f, pedalY), end = Offset(pedalX + 6f, pedalY), strokeWidth = 3f)
        }
    }
}

@Composable
fun PreloaderScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit,
    nextRoute: String = "Dashboard"
) {
    // Animated progress from 0f to 1f over 3.0 seconds
    val progressAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(3000, easing = EaseInOutSine)
        )
    }

    // Pulse scale for logo
    val logoPulse = rememberInfiniteTransition(label = "logoPulse")
    val pulseScale by logoPulse.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoScale"
    )

    // Zoom in animation state
    var isZoomingIn by remember { mutableStateOf(false) }
    LaunchedEffect(progressAnim.value) {
        if (progressAnim.value >= 1f) {
            isZoomingIn = true
        }
    }

    val zoomScale by animateFloatAsState(
        targetValue = if (isZoomingIn) 85f else 1.0f,
        animationSpec = if (isZoomingIn) tween(900, easing = EaseInQuart) else spring(),
        label = "zoomScale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (isZoomingIn) 0f else 1f,
        animationSpec = tween(400, easing = LinearEasing),
        label = "contentAlpha"
    )

    // On zoom complete, navigate to nextRoute
    LaunchedEffect(zoomScale) {
        if (isZoomingIn && zoomScale >= 80f) {
            onNavigate(nextRoute)
        }
    }

    // Smooth step-by-step progress status text based on progressAnim
    val progressStatus = when {
        progressAnim.value < 0.25f -> "Initializing Dispatch Network..."
        progressAnim.value < 0.50f -> "Securing Encrypted Ledger..."
        progressAnim.value < 0.75f -> "Connecting GPS & Route Planners..."
        progressAnim.value < 0.95f -> "Syncing Active Rider Pools..."
        else -> "Ready to Dispatch."
    }

    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val isDark = !isLight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isLight) BackgroundLight else LuxuryBlack) // Dynamic background
    ) {
        // Luxury Quilted pattern lines under splash
        QuiltedBackground(
            modifier = Modifier.fillMaxSize(),
            lineColor = if (isLight) Color.Black.copy(alpha = 0.03f) else Color.White.copy(alpha = 0.03f)
        ) {}

        // PROGRESS LOADING AT TOP EDGE OF SCREEN (Polished Glow and Linear Gradient)
        val progressBrush = Brush.horizontalGradient(
            colors = if (isLight) {
                listOf(
                    Obsidian.copy(alpha = 0.6f),
                    Obsidian,
                    Color.Black.copy(alpha = 0.9f)
                )
            } else {
                listOf(
                    GoldDark,
                    Gold,
                    Color.White.copy(alpha = 0.9f)
                )
            }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(if (isLight) Color.Black.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.08f))
                .align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progressAnim.value)
                    .background(progressBrush)
            )
            // Elegant drop-down glow
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressAnim.value)
                    .align(Alignment.BottomStart)
                    .height(6.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = if (isLight) {
                                listOf(
                                    Obsidian.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            } else {
                                listOf(
                                    Gold.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            }
                        )
                    )
            )
        }

        // Center Area: Centered, scaled brand row or separate zooming logo icon on its own
        if (!isZoomingIn) {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        alpha = contentAlpha
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Gold rounded square acting as the logo container, containing the Obsidian-tinted logo
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Gold)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = "Logo",
                        tint = Obsidian, // Obsidian-tinted logo
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                // Vertical Column containing top-line "ENGRACE" and bottom-line "DISPATCH"
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "ENGRACE",
                        fontSize = 28.sp,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = if (isLight) Obsidian else Color.White
                    )
                    Text(
                        text = "DISPATCH",
                        fontSize = 20.sp,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        color = if (isLight) Obsidian else Gold
                    )
                }
            }
        } else {
            // Centered fully on its own, completely independent of the Row or any bounding containers, to scale in smoothly and fully
            Icon(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Logo",
                tint = if (isLight) Obsidian else Gold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 44.dp, height = 72.dp)
                    .graphicsLayer {
                        scaleX = zoomScale
                        scaleY = zoomScale
                    }
            )
        }

        // Bottom section: Capitalized slogan "PREMIUM LOGISTICS & DISPATCH", followed by progress status text
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 48.dp)
                .graphicsLayer { alpha = contentAlpha },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "PREMIUM LOGISTICS & DISPATCH",
                fontSize = 13.sp,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.5.sp,
                color = if (isLight) Obsidian else Gold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Text(
                text = progressStatus,
                fontSize = 12.sp,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Medium,
                color = if (isLight) Color.Black.copy(alpha = 0.6f) else TextGray.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

// ====================================================================================================
// 3. ONBOARDING SCREEN (FITTED IMAGES, GORGEOUS WALKTHROUGH FLOW)
// ====================================================================================================
@Composable
fun OnboardingScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark
    
    val pages = listOf(
        OnboardingPage(
            title = "Sending parcels\nmade simple",
            desc = "Fast, secure, and highly reliable courier deliveries at your fingertips, anytime.",
            imageRes = R.drawable.ic_onboarding_sending,
            badge = "Fastest",
            badgeColor = Color(0xFF4CAF50)
        ),
        OnboardingPage(
            title = "Real-time\nprecise tracking",
            desc = "Watch your deliveries travel in real-time with our interactive live map tracking.",
            imageRes = R.drawable.ic_onboarding_tracking,
            badge = "Live Map",
            badgeColor = Gold
        ),
        OnboardingPage(
            title = "Secure wallet\n& instant payments",
            desc = "Top up your in-app wallet in seconds for frictionless, safe, and transparent payments.",
            imageRes = R.drawable.ic_onboarding_wallet,
            badge = "Protected",
            badgeColor = Obsidian
        )
    )

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val activePage = pages[pagerState.currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryBlack)
    ) {
        // Subtle blurry gold light background glowing effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Gold.copy(alpha = if (isDark) 0.08f else 0.04f),
                            Color.Transparent
                        ),
                        radius = 1200f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Upper Onboarding Header with HorizontalPager for fitted image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.1f)
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(Color.Black)
            ) {
                androidx.compose.foundation.pager.HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { pageIndex ->
                    val page = pages[pageIndex]
                    Box(modifier = Modifier.fillMaxSize()) {
                        // The fitted onboarding image
                        AsyncImage(
                            model = page.imageRes,
                            contentDescription = page.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Subtle dark vignette overlay to make text readable
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.4f),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.6f)
                                        )
                                    )
                                )
                        )
                    }
                }

                // Floating Dynamic Badge inside the header (overlay)
                Surface(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 24.dp, top = 24.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, BorderLight),
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(activePage.badgeColor, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = activePage.badge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Obsidian
                        )
                    }
                }

                // Skip Button inside the header (overlay)
                Text(
                    text = "Skip",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 16.dp, end = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigate("Login") }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )

                // SLIDE INDICATOR on top the image but on the header bottom just down
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    pages.forEachIndexed { index, _ ->
                        val isSelected = index == pagerState.currentPage
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 28.dp else 8.dp,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            label = "indicatorWidth"
                        )
                        val color = if (isSelected) Gold else Color.White.copy(alpha = 0.5f)
                        
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .background(color, shape = CircleShape)
                        )
                    }
                }
            }

            // Lower Content (Title, Description, and Walkthrough Controls)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Animated content switch for title and description
                AnimatedContent(
                    targetState = activePage,
                    transitionSpec = {
                        (slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut())
                            .using(SizeTransform(clip = false))
                    },
                    label = "textTransition"
                ) { targetPage ->
                    Column {
                        Text(
                            text = targetPage.title,
                            fontSize = 32.sp,
                            lineHeight = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = AppTextColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = targetPage.desc,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextGray,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Centered, big, tall action button at the bottom
                Button(
                    onClick = {
                        val currentPageIndex = pagerState.currentPage
                        if (currentPageIndex < pages.lastIndex) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(currentPageIndex + 1)
                            }
                        } else {
                            onNavigate("Login")
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Gold else Obsidian),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.lastIndex) "GET STARTED" else "NEXT",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Obsidian else Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

private data class OnboardingPage(
    val title: String,
    val desc: String,
    val imageRes: Int,
    val badge: String,
    val badgeColor: Color
)
