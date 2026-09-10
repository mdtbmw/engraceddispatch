package com.esdispatch.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esdispatch.ui.theme.*

enum class RouteVariant {
    COMPACT,
    CARD,
    TIMELINE,
    DETAIL
}

/**
 * Standardized RouteDisplay component for ESDispatch.
 * Section 12.1 & Section 14: Structured route presentation resilient to long Benin City addresses.
 */
@Composable
fun RouteDisplay(
    modifier: Modifier = Modifier,
    pickupAddress: String,
    deliveryAddress: String,
    variant: RouteVariant = RouteVariant.CARD,
    senderName: String? = null,
    receiverName: String? = null,
    pickupPhone: String? = null,
    deliveryPhone: String? = null,
    onCopyAddress: ((String) -> Unit)? = null
) {
    val dark = isDarkTheme
    val clipboard = LocalClipboardManager.current
    var isExpanded by remember { mutableStateOf(false) }

    when (variant) {
        RouteVariant.COMPACT -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pickupAddress.ifBlank { "Pickup origin" },
                    fontSize = 12.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(13.dp)
                )

                Text(
                    text = deliveryAddress.ifBlank { "Delivery address" },
                    fontSize = 12.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        }

        RouteVariant.CARD -> {
            val cardBg = if (dark) Color(0xFF161618) else GoldenWhiteLight.copy(alpha = 0.7f)
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .padding(14.dp)
            ) {
                // Pickup Node
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Gold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Gold)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PICKUP",
                            fontSize = 10.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp,
                            color = if (dark) Gold else Color(0xFF92400E)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = pickupAddress.ifBlank { "Pickup location pending" },
                            fontSize = 13.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            color = AppTextColor,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Vertical Connector Rail
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp, top = 2.dp, bottom = 2.dp)
                        .width(2.dp)
                        .height(18.dp)
                        .background(if (dark) Color(0xFF333333) else Slate)
                )

                // Dropoff Node
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (dark) Color(0x33FFFFFF) else Color(0x22121212)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = null,
                            tint = if (dark) Color.White else Obsidian,
                            modifier = Modifier.size(11.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DELIVERY",
                            fontSize = 10.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp,
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = deliveryAddress.ifBlank { "Destination pending" },
                            fontSize = 13.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            color = AppTextColor,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Expand / Collapse helper if address is long
                if (pickupAddress.length > 55 || deliveryAddress.length > 55) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { isExpanded = !isExpanded }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isExpanded) "Show less" else "Show full address",
                            fontSize = 11.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            color = if (dark) Gold else Obsidian
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = if (dark) Gold else Obsidian,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        RouteVariant.TIMELINE, RouteVariant.DETAIL -> {
            val bg = if (dark) Color(0xFF18181A) else GoldenWhiteSurface
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(bg)
                    .padding(16.dp)
            ) {
                // Section: Pickup
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Gold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(Gold)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PICKUP LOCATION",
                                fontSize = 11.sp,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Black,
                                color = if (dark) Gold else Color(0xFF92400E),
                                letterSpacing = 0.5.sp
                            )

                            if (variant == RouteVariant.DETAIL) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            clipboard.setText(AnnotatedString(pickupAddress))
                                            onCopyAddress?.invoke(pickupAddress)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = "Copy pickup address",
                                        tint = if (dark) Gold else Obsidian,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }

                        if (!senderName.isNullOrBlank()) {
                            Text(
                                text = "Sender: $senderName ${pickupPhone?.let { "($it)" } ?: ""}",
                                fontSize = 12.sp,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        Text(
                            text = pickupAddress.ifBlank { "Pickup location not specified" },
                            fontSize = 13.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            color = AppTextColor,
                            lineHeight = 19.sp
                        )
                    }
                }

                // Intermediate Rail
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp, top = 4.dp, bottom = 4.dp)
                        .width(2.dp)
                        .height(24.dp)
                        .background(if (dark) Color(0xFF333333) else Slate)
                )

                // Section: Delivery
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (dark) Color(0x33FFFFFF) else Color(0x22121212)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = null,
                            tint = if (dark) Color.White else Obsidian,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DESTINATION",
                                fontSize = 11.sp,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Black,
                                color = TextGray,
                                letterSpacing = 0.5.sp
                            )

                            if (variant == RouteVariant.DETAIL) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            clipboard.setText(AnnotatedString(deliveryAddress))
                                            onCopyAddress?.invoke(deliveryAddress)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = "Copy delivery address",
                                        tint = if (dark) Gold else Obsidian,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }

                        if (!receiverName.isNullOrBlank()) {
                            Text(
                                text = "Recipient: $receiverName ${deliveryPhone?.let { "($it)" } ?: ""}",
                                fontSize = 12.sp,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        Text(
                            text = deliveryAddress.ifBlank { "Destination not specified" },
                            fontSize = 13.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            color = AppTextColor,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        }
    }
}
