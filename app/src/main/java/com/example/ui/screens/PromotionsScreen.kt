package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DeliveryViewModel
import com.example.ui.theme.*
import com.example.ui.components.*

private data class PromoCardData(
    val title: String,
    val subtitle: String,
    val value: String,
    val gradient: Brush,
    val icon: ImageVector,
    val terms: String = ""
)

private val allPromos = listOf(
    PromoCardData("Express 15% Off", "First ride promo - 15% off express deliveries", "SAVE 15%", BrandGradient, Icons.Default.Bolt, "Valid for first 3 express deliveries. Expires 30 days."),
    PromoCardData("Same-Day Free", "Free same-day delivery on all packages under 5kg", "FREE", Brush.horizontalGradient(listOf(BiroBlue, DarkGradientBlue)), Icons.Default.Schedule, "Limit 5 per month. Valid in Lagos only."),
    PromoCardData("Referral Bonus", "Earn ₦2,000 for every friend you refer", "₦2K", Brush.horizontalGradient(listOf(DarkGradientBlue, BiroBlue)), Icons.Default.People, "No limit. Friend must complete first delivery."),
    PromoCardData("Weekend Special", "20% off all batch deliveries on weekends", "20% OFF", BrandGradient, Icons.Default.Inventory2, "Weekends only. Max discount ₦5,000."),
    PromoCardData("New User Bonus", "First delivery absolutely free (up to ₦3,000)", "FREE", Brush.horizontalGradient(listOf(SuccessGreen, Color(0xFF059669))), Icons.Default.CardGiftcard, "New accounts only. Valid 7 days from signup."),
    PromoCardData("Loyalty Reward", "10% cashback on every 10th delivery", "10% BACK", Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFFA78BFA))), Icons.Default.WorkspacePremium, "Automatically applied. No expiry.")
)

@Composable
fun PromotionsScreen(viewModel: DeliveryViewModel) {
    val apiPromos by viewModel.promotions.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadPromotions()
    }

    val displayPromos = if (apiPromos.isNotEmpty()) {
        apiPromos.mapIndexed { idx, p ->
            val iconVec = when (p.icon.lowercase()) {
                "bolt" -> Icons.Default.Bolt
                "schedule" -> Icons.Default.Schedule
                "people" -> Icons.Default.People
                "inventory" -> Icons.Default.Inventory2
                "cardgiftcard" -> Icons.Default.CardGiftcard
                "workspacepremium" -> Icons.Default.WorkspacePremium
                else -> Icons.Default.Bolt
            }
            val grad = when (idx % 3) {
                0 -> BrandGradient
                1 -> Brush.horizontalGradient(listOf(BiroBlue, DarkGradientBlue))
                else -> Brush.horizontalGradient(listOf(DarkGradientBlue, BiroBlue))
            }
            PromoCardData(p.title, p.subtitle, p.value, grad, iconVec, p.terms)
        }
    } else {
        allPromos
    }

    Box(Modifier.fillMaxSize().background(LuxuryBlack)) {
        Column(Modifier.fillMaxSize().background(BrandGradient)) {
            ScreenHeader(title = "PROMOTIONS", onBack = { viewModel.navigateBack() })

            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundGray),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text("Available Offers", color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.height(4.dp))
                        Text("${displayPromos.size} promotions available", color = TextGray, fontSize = 12.sp)
                        Spacer(Modifier.height(12.dp))
                    }
                    items(displayPromos) { promo ->
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(Modifier.fillMaxWidth().background(promo.gradient, RoundedCornerShape(20.dp))) {
                                Row(Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                                            Text(promo.value, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Text(promo.title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                                        Text(promo.subtitle, color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
                                        if (promo.terms.isNotEmpty()) {
                                            Spacer(Modifier.height(6.dp))
                                            Text(promo.terms, color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
                                        }
                                    }
                                    Box(Modifier.size(48.dp).background(Color.White.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(promo.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}
