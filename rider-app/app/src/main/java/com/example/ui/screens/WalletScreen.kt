package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DeliveryViewModel
import com.example.ui.theme.*
import com.example.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(viewModel: DeliveryViewModel) {
    val deliveries by viewModel.allDeliveries.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Interactive checklist state
    var fuelChecked by remember { mutableStateOf(false) }
    var tiresChecked by remember { mutableStateOf(false) }
    var brakesChecked by remember { mutableStateOf(false) }
    var carrierChecked by remember { mutableStateOf(false) }
    var helmetChecked by remember { mutableStateOf(false) }

    val myDeliveriesCount = deliveries.count { it.riderName == currentUser.fullName && it.status == "DELIVERED" }

    ScreenScaffold(title = "SHIFT MANIFEST", onBack = { viewModel.navigateBack() }) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            
            // Vehicle Profile Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(180.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .background(BrandGradient, RoundedCornerShape(24.dp))
            ) {
                Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column { 
                            Text("ENGRACED FLEET MOTORCYCLE", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(Modifier.height(2.dp))
                            Text(currentUser.fullName.uppercase(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold) 
                        }
                        Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(24.dp))
                    }
                    Column { 
                        Text("VEHICLE ASSIGNMENT", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Text("LAG-5832-BK", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold) 
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { 
                        Text("Model: TVS HLX 150", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, letterSpacing = 0.5.sp)
                        Text("Hub: Maryland Logistics Center", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp) 
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Shift Progress Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, CardBorderGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("Daily Shift Targets", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(4.dp))
                    Text("Internal target parameters for dispatch logs", color = TextMuted, fontSize = 11.sp)
                    Spacer(Modifier.height(16.dp))
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delivery target completion", color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("$myDeliveriesCount / 10", color = BiroBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    
                    // Simple custom progress bar
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEEF2F6))
                    ) {
                        val progressFraction = (myDeliveriesCount.toFloat() / 10f).coerceIn(0f, 1f)
                        Box(
                            Modifier
                                .fillMaxWidth(progressFraction)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(BrandGradient)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            
            // Vehicle Inspection Checklist
            Text("Pre-Trip Vehicle Safety Checklist", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(4.dp))
            Text("Complete this safety manifest before starting deliveries", color = TextMuted, fontSize = 11.sp)
            Spacer(Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorderGray, RoundedCornerShape(20.dp))
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(8.dp)
            ) {
                ChecklistItem(label = "Motorcycle Fuel Level Checked", checked = fuelChecked, onCheckedChange = { fuelChecked = it })
                ChecklistItem(label = "Tire Pressure Verified", checked = tiresChecked, onCheckedChange = { tiresChecked = it })
                ChecklistItem(label = "Braking System Inspection", checked = brakesChecked, onCheckedChange = { brakesChecked = it })
                ChecklistItem(label = "Delivery Carrier Box Secured", checked = carrierChecked, onCheckedChange = { carrierChecked = it })
                ChecklistItem(label = "Helmet & Safety Gears Worn", checked = helmetChecked, onCheckedChange = { helmetChecked = it })
            }

            Spacer(Modifier.height(24.dp))
            
            // Dispatch Announcements
            Text("Fleet Bulletins", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(12.dp))
            
            val bulletins = listOf(
                "Fuel allowance credited for Maryland Hub riders." to "1 hour ago",
                "Traffic alert: Heavy delays near Lekki toll gate." to "2 hours ago",
                "Speed limits warning: Maintain under 60km/h on bridges." to "Yesterday"
            )
            
            bulletins.forEach { (text, time) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CardBorderGray),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                ) {
                    Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(BiroBlue.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(text, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(time, color = TextMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChecklistItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = BiroBlue,
                uncheckedColor = TextMuted
            )
        )
        Spacer(Modifier.width(8.dp))
        Text(label, color = if (checked) TextMain else TextGray, fontSize = 12.sp, fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium)
    }
}
