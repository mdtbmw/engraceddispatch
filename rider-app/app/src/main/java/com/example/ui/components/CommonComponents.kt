package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun Modifier.gradientBackground(shape: androidx.compose.ui.graphics.Shape): Modifier = this.then(
    Modifier.background(BrandGradient, shape)
)

@Composable
fun PremiumGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val bg = if (enabled) BrandGradient else BrandGradient
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(52.dp).background(bg, RoundedCornerShape(12.dp))
    ) {
        if (icon != null) { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White); Spacer(Modifier.width(8.dp)) }
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun OutlinedGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(1.5.dp, BiroBlue),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(52.dp)
    ) {
        if (icon != null) { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = BiroBlue); Spacer(Modifier.width(8.dp)) }
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BiroBlue)
    }
}

@Composable
fun ScreenHeader(
    title: String,
    onBack: () -> Unit,
    rightContent: @Composable () -> Unit = { Spacer(Modifier.size(48.dp)) }
) {
    Box(Modifier.fillMaxWidth().statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
            Text(title.uppercase(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
            rightContent()
        }
    }
}

@Composable
fun ColumnScope.RoundedSheet(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BackgroundGray),
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
        modifier = Modifier.fillMaxWidth().weight(1f)
    ) {
        Column(
            Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 120.dp),
            content = content
        )
    }
}

@Composable
fun ScreenScaffold(
    title: String,
    onBack: () -> Unit,
    rightContent: @Composable () -> Unit = { Spacer(Modifier.size(48.dp)) },
    content: @Composable ColumnScope.() -> Unit
) {
    Box(Modifier.fillMaxSize().background(LuxuryBlack)) {
        Column(Modifier.fillMaxSize().background(BrandGradient)) {
            ScreenHeader(title = title, onBack = onBack, rightContent = rightContent)
            RoundedSheet(content = content)
        }
    }
}

@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(title, color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, modifier = modifier.padding(bottom = 12.dp))
}

@Composable
fun StatusBadge(status: String) {
    val (bg, fg) = when (status) {
        "PENDING" -> Color(0xFFFEF3C7) to Color(0xFFD97706)
        "ASSIGNED" -> Color(0xFFDBEAFE) to Color(0xFF2563EB)
        "PICKED_UP" -> Color(0xFFE0E7FF) to Color(0xFF4F46E5)
        "DELIVERED" -> Color(0xFFD1FAE5) to Color(0xFF059669)
        "CANCELLED" -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
        else -> Color(0xFFF1F5F9) to TextGray
    }
    Surface(color = bg, shape = RoundedCornerShape(6.dp)) {
        Text(status.replace("_", " "), color = fg, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, subtitle: String = "") {
    Column(Modifier.fillMaxWidth().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = TextGray, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        if (subtitle.isNotEmpty()) { Spacer(Modifier.height(4.dp)); Text(subtitle, color = TextGray, fontSize = 12.sp, textAlign = TextAlign.Center) }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String, subtitle: String = "", onClick: () -> Unit = {},
    trailing: @Composable () -> Unit = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) }
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorderGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(BiroBlue.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    if (subtitle.isNotEmpty()) Text(subtitle, color = TextGray, fontSize = 11.sp)
                }
            }
            trailing()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, valueColor: Color = TextMain) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextGray, fontSize = 13.sp)
        Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ShimmerBox(modifier: Modifier = Modifier, height: Dp, width: Dp = Dp.Unspecified, corners: Dp = 12.dp) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val t by transition.animateFloat(0f, 1000f, infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart), label = "shimmer_t")
    val brush = Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFF1F5F9), Color(0xFFE2E8F0)), start = Offset(t - 250f, t - 250f), end = Offset(t + 100f, t + 100f))
    val m = if (width == Dp.Unspecified) modifier.fillMaxWidth().height(height) else modifier.size(width, height)
    Box(modifier = m.clip(RoundedCornerShape(corners)).background(brush))
}

// ===== ANIMATED COUNTER =====
@Composable
fun AnimatedCounter(
    targetValue: Number,
    prefix: String = "\u20A6",
    format: String = "%,.0f",
    color: Color = TextMain,
    fontSize: androidx.compose.ui.unit.TextUnit = 28.sp,
    fontWeight: FontWeight = FontWeight.ExtraBold
) {
    val animValue = remember { Animatable(0f) }
    LaunchedEffect(targetValue) {
        animValue.snapTo(0f)
        animValue.animateTo(
            targetValue.toFloat(),
            spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow)
        )
    }
    Text("$prefix${String.format(format, animValue.value)}", color = color, fontSize = fontSize, fontWeight = fontWeight)
}

// ===== CONFETTI EFFECT =====
@Composable
fun ConfettiEffect(modifier: Modifier = Modifier, onDone: () -> Unit = {}) {
    val particles = remember { (0 until 30).map { Particle() } }
    var active by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1500)
        active = false
        onDone()
    }

    if (active) {
        Canvas(modifier = modifier.fillMaxSize()) {
            particles.forEach { p ->
                val progress = (System.currentTimeMillis() % p.duration) / p.duration.toFloat()
                val x = p.startX + (p.endX - p.startX) * progress
                val y = p.startY + (p.endY - p.startY) * progress + 100f * kotlin.math.sin(progress * Math.PI).toFloat()
                val alpha = (1f - progress).coerceIn(0f, 1f)
                drawCircle(color = p.color.copy(alpha = alpha), radius = p.radius, center = Offset(x, y))
            }
        }
    }
}

private data class Particle(
    val startX: Float = (50..900).random().toFloat(),
    val startY: Float = -20f,
    val endX: Float = (100..800).random().toFloat(),
    val endY: Float = (300..1200).random().toFloat(),
    val radius: Float = (4..10).random().toFloat(),
    val color: Color = listOf(BiroBlue, SuccessGreen, Color(0xFFF59E0B), Color(0xFF8B5CF6), DangerRed).random(),
    val duration: Long = (800..1500).random().toLong()
)

// ===== PULSE RING =====
@Composable
fun PulseRing(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "pulse_alpha"
    )
    Box(modifier = modifier.graphicsLayer { scaleX = pulseScale; scaleY = pulseScale; alpha = pulseAlpha }.background(BiroBlue.copy(alpha = 0.3f), CircleShape))
}

// ===== GRADIENT BORDER =====
fun Modifier.gradientBorder(strokeWidth: Dp = 2.dp, shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp)): Modifier = this.then(
    Modifier.drawBehind {
        drawRoundRect(
            brush = BrandGradient,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
            style = Stroke(width = strokeWidth.toPx())
        )
    }
)

// ===== HAPTIC BUTTON WRAPPER =====
@Composable
fun HapticButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    PremiumGradientButton(
        text = text,
        onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onClick() },
        modifier = modifier,
        icon = icon,
        enabled = enabled
    )
}

// ===== STAGGERED ENTRANCE ITEM =====
@Composable
fun StaggeredItem(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 50L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)) { it / 2 }
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessBottomSheet(message: String, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dismiss: () -> Unit = { scope.launch { sheetState.hide(); onDismiss() } }

    ModalBottomSheet(
        onDismissRequest = dismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(64.dp).background(SuccessGreen.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(36.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Success", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
            Spacer(Modifier.height(8.dp))
            Text(message, color = TextGray, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Button(onClick = dismiss, colors = ButtonDefaults.buttonColors(containerColor = BiroBlue), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("Done", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
