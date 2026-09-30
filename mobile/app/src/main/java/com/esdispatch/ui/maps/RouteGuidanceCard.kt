package com.esdispatch.ui.maps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esdispatch.ui.theme.*
import kotlin.math.ceil

/**
 * Compact, unified navigation and route guidance HUD.
 * Provides clear road instructions, ETA, distance, voice toggle, external maps link,
 * and a dismiss/minimize button to give riders an unobstructed view of the map.
 */
@Composable
fun RouteGuidanceCard(
    guidance: RouteGuidance,
    onRetry: () -> Unit,
    onNavigate: (() -> Unit)? = null,
    isRider: Boolean = false,
    isVoiceMuted: Boolean = false,
    onToggleVoice: (() -> Unit)? = null,
    onOpenExternalMaps: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Obsidian,
        border = BorderStroke(1.dp, Gold.copy(alpha = 0.85f)),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Maneuver / Status Icon Badge
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Gold),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        guidance.isNonVehicularCurb -> Icons.Filled.DirectionsWalk
                        isRider -> Icons.Filled.Navigation
                        else -> Icons.Filled.DirectionsBike
                    },
                    contentDescription = null,
                    tint = Obsidian,
                    modifier = Modifier.size(17.dp)
                )
            }

            // Direction / Road Guidance details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = guidance.title.ifBlank { "Following route" },
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (guidance.isNonVehicularCurb) {
                    Text(
                        text = guidance.detail.ifBlank { "Park at curb • Walk into door (~${guidance.curbWalkingMeters.toInt()}m)" },
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (guidance.etaSeconds != null) {
                    val minutes = ceil(guidance.etaSeconds / 60).toInt().coerceAtLeast(1)
                    val distance = guidance.distanceMeters?.let { " • %.1f km".format(it / 1000) }.orEmpty()
                    val traffic = if (guidance.congested) " • Slow traffic" else ""
                    Text(
                        text = "About $minutes min$distance$traffic",
                        color = Color.White.copy(alpha = 0.90f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = guidance.detail.ifBlank { "Live route updated" },
                        color = TextGray,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Action controls (Voice, External Maps, Retry, Close/Minimize)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (guidance.canRetry) {
                    TextButton(
                        onClick = onRetry,
                        enabled = !guidance.loading,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Retry", fontSize = 10.sp, color = Gold, fontWeight = FontWeight.Bold)
                    }
                }

                if (isRider && onToggleVoice != null) {
                    IconButton(
                        onClick = onToggleVoice,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (isVoiceMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                            contentDescription = if (isVoiceMuted) "Unmute Voice" else "Mute Voice",
                            tint = if (isVoiceMuted) Color.Gray else Gold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (isRider && onOpenExternalMaps != null) {
                    IconButton(
                        onClick = onOpenExternalMaps,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Map,
                            contentDescription = "Open in Maps",
                            tint = Gold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (onDismiss != null) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Minimize Navigation",
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
