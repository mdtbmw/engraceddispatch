package com.esdispatch.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esdispatch.ui.theme.*

@Composable
fun StatsGrid(
    activeCount: Int,
    completedCount: Int,
    deliveryCount: Int,
    loyaltyPoints: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatsTile(
                title = "Active Shipments",
                value = "$activeCount",
                icon = Icons.Filled.LocalShipping,
                iconColor = Gold,
                modifier = Modifier.weight(1f)
            )
            StatsTile(
                title = "Completed Orders",
                value = "$completedCount",
                icon = Icons.Filled.CheckCircle,
                iconColor = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val promoSavings = (deliveryCount * 750) + (loyaltyPoints * 10)
            StatsTile(
                title = "Promo Savings",
                value = "₦${String.format("%,d", promoSavings)} Saved",
                icon = Icons.Filled.Redeem,
                iconColor = Gold,
                modifier = Modifier.weight(1.3f)
            )
            StatsTile(
                title = "Reward Points",
                value = "$loyaltyPoints Pts",
                icon = Icons.Filled.CardGiftcard,
                iconColor = Gold,
                modifier = Modifier.weight(0.7f)
            )
        }
    }
}

@Composable
fun StatsTile(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(84.dp),
        shape = RoundedCornerShape(24.dp),
        color = Gold,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Obsidian),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Obsidian.copy(alpha = 0.75f),
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Obsidian,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
