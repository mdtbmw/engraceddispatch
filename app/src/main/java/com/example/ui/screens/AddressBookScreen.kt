package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DeliveryViewModel
import com.example.ui.theme.*
import com.example.ui.components.*
import kotlinx.coroutines.launch

data class SavedAddress(
    val id: Long,
    val label: String,
    val address: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isDefault: Boolean = false
)

@Composable
fun AddressBookScreen(viewModel: DeliveryViewModel) {
    val addresses by viewModel.addresses.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAddresses()
    }

    Box(Modifier.fillMaxSize().background(LuxuryBlack)) {
        Column(Modifier.fillMaxSize().background(BrandGradient)) {
            ScreenHeader(title = "ADDRESS BOOK", onBack = { viewModel.navigateBack() })

            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundGray),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 120.dp)) {
                    Text("Saved Addresses", color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(16.dp))

                    if (addresses.isEmpty()) {
                        EmptyState(Icons.Default.LocationOff, "No saved addresses", "Add your frequent pickup and delivery locations")
                    } else {
                        addresses.forEach { addr ->
                            val icon = when (addr.label.lowercase()) {
                                "home" -> Icons.Default.Home
                                "work" -> Icons.Default.Work
                                else -> Icons.Default.LocationOn
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clip(RoundedCornerShape(16.dp)).clickable { },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, if (addr.isDefault) BiroBlue else CardBorderGray)
                            ) {
                                Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(40.dp).clip(CircleShape).background(BiroBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                        Icon(icon, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(addr.label, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                            if (addr.isDefault) {
                                                Spacer(Modifier.width(8.dp))
                                                Surface(color = BiroBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                                                    Text("Default", color = BiroBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                }
                                            }
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(addr.address, color = TextGray, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    PremiumGradientButton("Add New Address", icon = Icons.Default.Add, onClick = { showAddSheet = true }, modifier = Modifier.fillMaxWidth().height(48.dp))
                }
            }
        }
    }

    if (showAddSheet) AddAddressSheet(viewModel) { showAddSheet = false }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAddressSheet(viewModel: DeliveryViewModel, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dismiss: () -> Unit = { scope.launch { sheetState.hide(); onDismiss() } }
    var label by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = dismiss, sheetState = sheetState, containerColor = Color.White, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Text("Add Address", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label (e.g. Home, Work)") }, leadingIcon = { Icon(Icons.Default.Label, contentDescription = null) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = addressFieldColors())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = BiroBlue) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = addressFieldColors())
            Spacer(Modifier.height(24.dp))
            PremiumGradientButton(
                "Save Address",
                onClick = {
                    if (label.isNotBlank() && address.isNotBlank()) {
                        viewModel.saveAddress(label, address)
                    }
                    dismiss()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = dismiss, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp), border = BorderStroke(1.dp, CardBorderGray)) {
                Text("Cancel", color = TextGray, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun addressFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextMain, unfocusedTextColor = TextMain,
    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
    focusedBorderColor = BiroBlue, unfocusedBorderColor = CardBorderGray,
    focusedLabelColor = BiroBlue, unfocusedLabelColor = TextGray, cursorColor = BiroBlue
)
