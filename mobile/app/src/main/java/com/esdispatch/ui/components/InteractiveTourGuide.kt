package com.esdispatch.ui.components

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.esdispatch.util.SoundManager
import com.esdispatch.ui.theme.*

data class TourStep(
    val stepIndex: Int,
    val tag: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val primaryButtonText: String
)

@Composable
fun InteractiveTourGuide(
    modifier: Modifier = Modifier,
    isDark: Boolean,
    forceShow: Boolean = false,
    onTourFinished: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences("esdispatch_tour_prefs", Context.MODE_PRIVATE)
    }
    val tourCompletedKey = "tour_completed_v2"
    var isVisible by remember {
        mutableStateOf(forceShow || !prefs.getBoolean(tourCompletedKey, false))
    }

    LaunchedEffect(forceShow) {
        if (forceShow) isVisible = true
    }

    if (!isVisible) return

    val steps = remember {
        listOf(
            TourStep(
                stepIndex = 1,
                tag = "COMMAND CENTER",
                title = "Welcome to ESDispatch",
                description = "Experience premium, motorcycle-powered on-demand logistics across the city. Explore your new high-performance command center.",
                icon = Hugeicons.Solid.Flash,
                primaryButtonText = "Begin Tour"
            ),
            TourStep(
                stepIndex = 2,
                tag = "HERO QUICK ACCESS",
                title = "Wallet & Marketplace",
                description = "Top up your escrow-protected balance for 1-tap booking, and discover verified vendors and courier equipment in the Marketplace.",
                icon = Hugeicons.Solid.Wallet,
                primaryButtonText = "Next Step"
            ),
            TourStep(
                stepIndex = 3,
                tag = "MOTORCYCLE DISPATCH FLEET",
                title = "Strict Motorcycle Fleet",
                description = "Couriers operate nimble bikes strictly capped at 20kg (45cm³). No bulky freight, sacks, or heavy appliances — purely fast, agile parcel dispatch.",
                icon = Hugeicons.Solid.Motorcycle,
                primaryButtonText = "Next Step"
            ),
            TourStep(
                stepIndex = 4,
                tag = "LIVE TELEMETRY",
                title = "Uber-Grade Live Tracking",
                description = "Follow your courier live on dynamic maps with ESRI street-level overlays, turn-by-turn routing, and automated 50-meter proximity arrival alerts.",
                icon = Hugeicons.Solid.Route,
                primaryButtonText = "Next Step"
            ),
            TourStep(
                stepIndex = 5,
                tag = "SIGNATURE MOTION",
                title = "Floating Capsule Dock",
                description = "Glide effortlessly between your Dashboard, Live Tracking, Wallet, Order Logs, and Profile with zero-lag spring physics.",
                icon = Hugeicons.Solid.CheckCircle,
                primaryButtonText = "Get Started"
            )
        )
    }

    var currentStepIndex by remember { mutableStateOf(0) }
    val currentStep = steps[currentStepIndex]

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
        } else {
            completeTour()
        }
    }

    fun prevStep() {
        SoundManager.playClick()
        if (currentStepIndex > 0) {
            currentStepIndex--
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(150f)
            .background(Color.Black.copy(alpha = 0.82f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* block background clicks */ },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.92f)) togetherWith
                        (fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 0.92f))
            },
            label = "tourStepTransition"
        ) { step ->
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Charcoal else Color(0xFF1C1C1E)),
                border = BorderStroke(1.5.dp, Gold.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Bar: Step pill & Skip button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Gold.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "STEP ${step.stepIndex} OF ${steps.size}",
                                color = Gold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                letterSpacing = 1.sp
                            )
                        }

                        TextButton(
                            onClick = { completeTour() },
                            colors = ButtonDefaults.textButtonColors(contentColor = TextGray)
                        ) {
                            Text("Skip Tour", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Hero Icon with gold breathing aura
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Gold.copy(alpha = 0.25f), Color.Transparent)
                                )
                            )
                            .border(2.dp, Gold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedHugeIcon(
                            icon = step.icon,
                            tint = Gold,
                            size = 36.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Tagline Slogan
                    Text(
                        text = step.tag,
                        color = Gold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Title
                    Text(
                        text = step.title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Description
                    Text(
                        text = step.description,
                        color = Color.White.copy(alpha = 0.78f),
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Step indicator dots (Expanding pills)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        steps.forEachIndexed { idx, _ ->
                            val isActive = idx == currentStepIndex
                            val dotWidth by animateDpAsState(
                                targetValue = if (isActive) 24.dp else 8.dp,
                                animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f),
                                label = "dotWidth"
                            )
                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(dotWidth)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isActive) Gold else Color.White.copy(alpha = 0.2f))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    // Bottom Navigation Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentStepIndex > 0) {
                            OutlinedButton(
                                onClick = { prevStep() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold)
                            ) {
                                Text("Back", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Button(
                            onClick = { nextStep() },
                            modifier = Modifier
                                .weight(if (currentStepIndex > 0) 1.5f else 1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian)
                        ) {
                            Text(
                                text = step.primaryButtonText,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
