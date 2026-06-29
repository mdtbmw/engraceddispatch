package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DeliveryViewModel
import com.example.ui.navigation.AppView
import com.example.ui.theme.*
import com.example.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderReviewScreen(viewModel: DeliveryViewModel) {
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    val delivery by viewModel.currentTrackingDelivery.collectAsState()

    Box(Modifier.fillMaxSize().background(LuxuryBlack)) {
        Column(Modifier.fillMaxSize().background(BrandGradient)) {
            ScreenHeader("RATE RIDER", onBack = { viewModel.navigateBack() })

            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundGray),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(Modifier.size(80.dp).background(BiroBlue.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(40.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(delivery?.riderName ?: "Rider", color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Text(delivery?.riderBikeNumber ?: "", color = TextGray, fontSize = 12.sp)
                    Spacer(Modifier.height(24.dp))
                    Text("How was your delivery experience?", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..5).forEach { i ->
                            val isSelected = i <= rating
                            IconButton(onClick = { rating = i }, modifier = Modifier.size(48.dp)) {
                                Icon(
                                    if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$i star",
                                    tint = if (isSelected) Color(0xFFF59E0B) else TextGray,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))

                    OutlinedTextField(
                        value = comment, onValueChange = { comment = it },
                        label = { Text("Share your experience (optional)") },
                        shape = RoundedCornerShape(16.dp),
                        minLines = 3, maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedBorderColor = BiroBlue, unfocusedBorderColor = CardBorderGray),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(24.dp))

                    PremiumGradientButton(
                        "Submit Rating",
                        icon = Icons.Default.Check,
                        onClick = {
                            delivery?.let { d ->
                                viewModel.submitReview(rating, comment, d.trackingNumber)
                            }
                            viewModel.navigateToRoot(AppView.Dashboard)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = rating > 0
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { viewModel.navigateToRoot(AppView.Dashboard) }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp), border = BorderStroke(1.dp, CardBorderGray)) {
                        Text("Skip", color = TextGray, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
