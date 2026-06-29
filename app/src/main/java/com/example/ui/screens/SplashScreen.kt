package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DeliveryViewModel
import com.example.ui.navigation.AppView
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class BlobShape(private val seed: Int = 0, private val pulse: Float = 0f) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width; val h = size.height
        val cx = w / 2f; val cy = h / 2f
        val baseR = minOf(cx, cy) * 0.82f
        val pulseR = baseR * (1f + 0.04f * sin(pulse * 2f * PI.toFloat()))
        val n = 8
        val pts = Array(n) { i ->
            val angle = i * 2f * PI.toFloat() / n + pulse * 0.08f
            val rf = when ((seed + i) % 4) {
                0 -> 1f; 1 -> 0.78f; 2 -> 1.12f; else -> 0.88f
            } + 0.08f * sin(pulse * 3f + i.toFloat() * 1.3f)
            Offset(cx + pulseR * rf * cos(angle).toFloat(), cy + pulseR * rf * sin(angle).toFloat())
        }
        val path = Path().apply {
            moveTo(pts[0].x, pts[0].y)
            for (i in 0 until n) {
                val p = pts[i]; val nxt = pts[(i + 1) % n]
                val cmx = (p.x + nxt.x) / 2f; val cmy = (p.y + nxt.y) / 2f
                val cpx = (p.x + cmx) / 2f; val cpy = (p.y + cmy) / 2f
                quadraticTo(cpx, cpy, nxt.x, nxt.y)
            }
            close()
        }
        return Outline.Generic(path)
    }
}

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val gradient: Brush,
    val seed: Int,
    val features: List<String>
)

private val pages = listOf(
    OnboardingPage(
        "Swift Delivery",
        "On-demand dispatch across Lagos.\nExpress, economy, batch delivery,\nall at your fingertips.",
        Icons.Default.TwoWheeler,
        BrandGradient,
        0,
        listOf("Same-day delivery", "Real-time pricing", "Multiple vehicle types")
    ),
    OnboardingPage(
        "Live Tracking",
        "Track every shipment in real-time.\nKnow exactly where your package is,\nevery step of the way.",
        Icons.Default.NearMe,
        Brush.horizontalGradient(listOf(BiroBlue, DarkGradientBlue)),
        1,
        listOf("GPS live map", "Rider location", "Delivery timeline")
    ),
    OnboardingPage(
        "Secure Pay",
        "Multiple payment methods.\nSecure transactions with instant\nconfirmation and digital receipts.",
        Icons.Default.AccountBalanceWallet,
        Brush.horizontalGradient(listOf(DarkGradientBlue, BiroBlue)),
        2,
        listOf("Wallet & cards", "Bank transfer", "Digital receipts")
    )
)

@Composable
fun SplashOnboardingScreen(viewModel: DeliveryViewModel) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val infinite = rememberInfiniteTransition(label = "pulse")
    val pulse by infinite.animateFloat(0f, 2f * PI.toFloat(), infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "pulse")

    var showSplash by remember { mutableStateOf(true) }
    val splashAlpha by animateFloatAsState(
        targetValue = if (showSplash) 1f else 0f,
        animationSpec = tween(600),
        label = "splashAlpha"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (showSplash) 0f else 1f,
        animationSpec = tween(600, delayMillis = 300),
        label = "contentAlpha"
    )

    val isLoggedIn by viewModel.preferences.isLoggedIn.collectAsState(initial = false)
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            showSplash = false
            delay(600)
            viewModel.navigateTo(AppView.Dashboard)
        } else {
            delay(2200)
            showSplash = false
        }
    }

    Box(Modifier.fillMaxSize().background(LuxuryBlack)) {
        // Animated background orbs
        Box(Modifier.fillMaxSize().graphicsLayer(alpha = 0.3f)) {
            Canvas(Modifier.fillMaxSize()) {
                val cx = size.width / 2f; val cy = size.height / 2f
                val r = minOf(cx, cy) * 1.2f
                for (i in 0..3) {
                    val angle = pulse + i * 1.57f
                    val x = cx + r * 0.45f * cos(angle.toDouble()).toFloat()
                    val y = cy + r * 0.35f * sin(angle.toDouble()).toFloat()
                    val rad = r * (0.5f + 0.2f * sin((pulse + i.toFloat()) * 0.7f).toFloat())
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                BiroBlue.copy(alpha = 0.18f),
                                BiroBlue.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            center = Offset(x, y),
                            radius = rad
                        ),
                        radius = rad, center = Offset(x, y)
                    )
                }
            }
        }

        // SPLASH SCREEN
        Box(
            Modifier.fillMaxSize()
                .graphicsLayer(alpha = splashAlpha)
        ) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val logoScale by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessLow),
                    label = "logoScale"
                )

                Box(
                    Modifier.size(120.dp)
                        .graphicsLayer {
                            scaleX = logoScale
                            scaleY = logoScale
                        }
                        .shadow(24.dp, CircleShape)
                        .background(BrandGradient, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.TwoWheeler,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    "ENGRACED",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp
                )
                Text(
                    "SMILE DISPATCH",
                    color = BiroBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )

                Spacer(Modifier.height(12.dp))

                val dotAlpha by rememberInfiniteTransition(label = "dot").animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
                    label = "dotPulse"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) {
                        Box(
                            Modifier.size(6.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = dotAlpha))
                        )
                    }
                }
            }
        }

        // ONBOARDING PAGER
        Column(
            Modifier.fillMaxSize()
                .graphicsLayer(alpha = contentAlpha)
                .padding(top = 48.dp)
        ) {
            // Top brand mark
            Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                Text(
                    "ENGRACED SMILE DISPATCH",
                    color = BiroBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 3.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                beyondViewportPageCount = 1
            ) { pageIndex ->
                val page = pages[pageIndex]
                val pageOffset = (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction

                Column(
                    Modifier.fillMaxSize().padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Animated blob with icon
                    Box(
                        Modifier.size(240.dp)
                            .graphicsLayer {
                                scaleX = 1f - 0.1f * kotlin.math.abs(pageOffset)
                                scaleY = 1f - 0.1f * kotlin.math.abs(pageOffset)
                                alpha = 1f - 0.3f * kotlin.math.abs(pageOffset.coerceIn(-1f, 1f))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier.size(240.dp)
                                .clip(BlobShape(page.seed, pulse))
                                .background(page.gradient)
                        )
                        Box(
                            Modifier.size(180.dp)
                                .clip(BlobShape(page.seed + 3, pulse + 1.5f))
                                .background(Color.White.copy(alpha = 0.08f))
                        )
                        Icon(
                            page.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(64.dp).graphicsLayer { alpha = 0.95f }
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    Text(
                        page.title,
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        page.subtitle,
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    // Feature pills
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        page.features.forEach { feature ->
                            Surface(
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    feature,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Page indicators
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { i ->
                    val width by animateDpAsState(
                        targetValue = if (pagerState.currentPage == i) 28.dp else 8.dp,
                        animationSpec = tween(300),
                        label = "dotW"
                    )
                    val alpha by animateFloatAsState(
                        targetValue = if (pagerState.currentPage == i) 1f else 0.3f,
                        animationSpec = tween(300),
                        label = "dotA"
                    )
                    Box(
                        Modifier.padding(horizontal = 3.dp)
                            .height(6.dp)
                            .width(width)
                            .alpha(alpha)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (pagerState.currentPage == i) Color.White else Color.White.copy(alpha = 0.3f))
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Bottom actions
            Row(
                Modifier.fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { viewModel.navigateTo(AppView.Login) }) {
                    Text(
                        "Skip",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            viewModel.navigateTo(AppView.Login)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = CircleShape,
                    modifier = Modifier.size(60.dp)
                        .shadow(12.dp, CircleShape, ambientColor = Color(0x405C58FF), spotColor = Color(0x605C58FF))
                        .background(BrandGradient, CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = if (pagerState.currentPage < pages.size - 1) "Next" else "Get Started",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
