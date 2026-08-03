package com.esdispatch.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.esdispatch.ui.theme.*
import com.esdispatch.viewmodel.DeliveryViewModel

@Composable
fun LoyaltyRewardsCard(
    viewModel: DeliveryViewModel,
    loyaltyPoints: Int,
    deliveryCount: Int,
    referralCode: String,
    onTriggerConfetti: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val bonusClaimed by viewModel.dailyBonusClaimed.collectAsState()
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark

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

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // VIP Rewards Section
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(28.dp),
            color = Obsidian,
            border = BorderStroke(1.2.dp, Gold.copy(alpha = 0.3f)),
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
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
                            text = "ES VIP Rewards",
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

                    Button(
                        onClick = {
                            if (!bonusClaimed) {
                                viewModel.claimDailyBonus()
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
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Milestone Achievements",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

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
                                onTriggerConfetti()
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

        // Referral Section
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(28.dp),
            color = Obsidian,
            border = BorderStroke(1.dp, if (isDark) BorderDark else Slate),
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
                            .background(if (isDark) Gold.copy(alpha = 0.15f) else Obsidian.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Redeem, null, tint = if (isDark) Gold else Obsidian, modifier = Modifier.size(18.dp))
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = 1.dp,
                                color = if (isDark) Gold.copy(alpha = 0.3f) else Slate,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDark) Charcoal else GoldenWhite)
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
                            color = if (isDark) Gold else Obsidian
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
                                tint = if (isDark) Gold else Obsidian,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
