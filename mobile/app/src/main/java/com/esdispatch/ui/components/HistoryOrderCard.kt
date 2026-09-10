package com.esdispatch.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.esdispatch.data.Parcel
import com.esdispatch.data.ParcelStatus
import com.esdispatch.ui.theme.*

/**
 * Standardized HistoryOrderCard for OrderLogsScreen.
 * Conforms to ESDispatch Design System Section 11.2.
 */
@Composable
fun HistoryOrderCard(
    parcel: Parcel,
    onClick: () -> Unit,
    onRebook: () -> Unit,
    onViewReceipt: () -> Unit,
    modifier: Modifier = Modifier,
    onCancel: (() -> Unit)? = null
) {
    val dark = isDarkTheme

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .tactilePress(scaleDown = 0.98f, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Charcoal, // Adaptive Charcoal surface
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, if (dark) BorderDark else Slate)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Row 1: Title & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = parcel.itemName.ifBlank { "Standard Delivery" },
                    fontSize = 15.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = AppTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (parcel.price > 0) {
                    PriceDisplay(amount = parcel.price, variant = PriceVariant.COMPACT)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 2: Subtitle (#ID + Date) & Status Badge (+ OTP if active)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${parcel.id} • ${parcel.dateString.ifBlank { "Completed" }}",
                    fontSize = 11.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = TextGray
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (parcel.otpCode.isNotBlank() && parcel.status !in listOf(ParcelStatus.DELIVERED, ParcelStatus.CANCELLED)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (dark) Gold.copy(alpha = 0.15f) else Gold.copy(alpha = 0.2f),
                            border = BorderStroke(0.8.dp, Gold)
                        ) {
                            Text(
                                text = "OTP: ${parcel.otpCode}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = SpaceGrotesk,
                                color = if (dark) Gold else Obsidian,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    StatusBadge(
                        status = parcel.status,
                        size = StatusBadgeSize.COMPACT,
                        showLivePulse = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Compact Structured Route
            RouteDisplay(
                pickupAddress = parcel.pickupAddress,
                deliveryAddress = parcel.deliveryAddress,
                variant = RouteVariant.COMPACT
            )

            Spacer(modifier = Modifier.height(12.dp))

            val isCancellable = onCancel != null && parcel.status in listOf(
                ParcelStatus.PENDING,
                ParcelStatus.QUEUED,
                ParcelStatus.RESERVED_NEXT,
                ParcelStatus.ASSIGNED
            )

            // Row 4: Secondary Action Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isCancellable) {
                    OutlinedButton(
                        onClick = { onCancel?.invoke() },
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE53935)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "Cancel Order",
                            fontSize = 12.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onRebook,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (dark) Color(0x40FFB800) else Slate),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (dark) Gold else Obsidian
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Repeat,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Rebook Route",
                            fontSize = 12.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onViewReceipt,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (dark) Color(0xFF222226) else GoldenWhiteSurface,
                        contentColor = if (dark) Color.White else Obsidian
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Details",
                        fontSize = 12.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
