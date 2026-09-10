package com.esdispatch.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.esdispatch.data.Parcel
import com.esdispatch.data.ParcelStatus
import com.esdispatch.ui.theme.*

/**
 * Standardized DeliveryCard component for Active Shipments.
 * Conforms to ESDispatch Design System Section 11.1.
 */
@Composable
fun DeliveryCard(
    parcel: Parcel,
    onClick: () -> Unit,
    onQuickMap: (Parcel) -> Unit,
    onCopyTrackingId: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isDarkTheme
    val clipboard = LocalClipboardManager.current

    val surfaceColor = if (dark) Color(0xFF141416) else GoldenWhiteSurface
    val borderColor = if (dark) Color(0xFF242426) else Slate

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .tactilePress(scaleDown = 0.98f, onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = surfaceColor,
        shadowElevation = 0.dp, // Rule: Zero muddy drop shadows
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Row 1: Header (Item Title + ID, and StatusBadge on right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = parcel.itemName.ifBlank { "Standard Delivery" },
                        fontSize = 16.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = AppTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "#${parcel.id}",
                        fontSize = 12.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        color = TextGray
                    )
                }

                StatusBadge(
                    status = parcel.status,
                    size = StatusBadgeSize.DEFAULT,
                    useAdminLabel = false
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Metadata + Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Service Tier / Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (dark) Color(0xFF222226) else Color(0xFFEDE5D0))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = parcel.dateString.ifBlank { "Active Delivery" },
                            fontSize = 10.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            color = if (dark) Color.White.copy(alpha = 0.8f) else Obsidian
                        )
                    }

                    if (parcel.price > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        PriceDisplay(amount = parcel.price, variant = PriceVariant.MUTED)
                    }
                }

                // Quick Action Icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconActionButton(
                        icon = Icons.Filled.ContentCopy,
                        contentDescription = "Copy Tracking ID",
                        onClick = {
                            clipboard.setText(AnnotatedString(parcel.id))
                            onCopyTrackingId(parcel.id)
                        },
                        variant = IconActionVariant.GHOST,
                        iconSize = 15.dp,
                        hitSize = 36.dp
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconActionButton(
                        icon = Icons.Filled.Navigation,
                        contentDescription = "Quick Map View",
                        onClick = { onQuickMap(parcel) },
                        variant = IconActionVariant.TONAL,
                        iconSize = 15.dp,
                        hitSize = 36.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 3: Structured Route Block
            RouteDisplay(
                pickupAddress = parcel.pickupAddress,
                deliveryAddress = parcel.deliveryAddress,
                variant = RouteVariant.CARD
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Row 4: Lifecycle Progress Rail
            val progressTarget = when (parcel.status) {
                ParcelStatus.PENDING -> 0.15f
                ParcelStatus.QUEUED -> 0.25f
                ParcelStatus.RESERVED_NEXT -> 0.35f
                ParcelStatus.ASSIGNED -> 0.50f
                ParcelStatus.PICKED_UP -> 0.65f
                ParcelStatus.TRANSIT, ParcelStatus.OUT_FOR_DELIVERY -> 0.80f
                ParcelStatus.ARRIVED, ParcelStatus.HANDOVER_VERIFIED -> 0.90f
                ParcelStatus.DELIVERED -> 1.0f
                ParcelStatus.CANCELLED -> 0.0f
            }
            val animatedProgress by animateFloatAsState(
                targetValue = progressTarget,
                label = "deliveryProgress"
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Gold,
                trackColor = if (dark) Color(0xFF242426) else Slate,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Row 5: Rider Info OR Queue Explanation
            if (parcel.courierName.isNotBlank() || parcel.riderBikeNumber.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (dark) Color(0xFF1B1B1E) else GoldenWhiteLight)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (parcel.courierAvatar.isNotBlank()) {
                        AsyncImage(
                            model = parcel.courierAvatar,
                            contentDescription = "Courier Avatar",
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .border(1.dp, Gold, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Gold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = if (dark) Gold else Obsidian,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = parcel.courierName.ifBlank { "Assigned Rider" },
                            fontSize = 12.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            color = AppTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (parcel.riderBikeNumber.isNotBlank()) {
                            Text(
                                text = "Bike: ${parcel.riderBikeNumber}",
                                fontSize = 10.sp,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                color = TextGray
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (dark) Color(0xFF1B1B1E) else GoldenWhiteLight)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Gold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (parcel.status == ParcelStatus.QUEUED) "Waiting for available rider in dispatch queue" else "Request received and awaiting automated assignment",
                        fontSize = 11.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        color = TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row 6: Primary Footer Action
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = Obsidian // Strict rule: NO white on gold
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    text = "Track Delivery Details",
                    fontSize = 13.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}
