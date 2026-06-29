package com.example.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.Delivery
import com.example.ui.DeliveryViewModel
import com.example.ui.components.DeliveryMapView
import com.example.ui.navigation.AppView
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class TurnInstruction(
    val text: String,
    val distance: String,
    val icon: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapNavigationScreen(viewModel: DeliveryViewModel, trackingNumber: String) {
    val delivery by viewModel.currentTrackingDelivery.collectAsState()
    var showBottomSheet by remember { mutableStateOf(true) }
    var otpInput by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf("") }

    var routeCoords by remember { mutableStateOf<List<Pair<Double, Double>>>(emptyList()) }
    var instructions by remember { mutableStateOf<List<TurnInstruction>>(emptyList()) }
    var totalDistance by remember { mutableStateOf("") }
    var totalDuration by remember { mutableStateOf("") }
    var loadingRoute by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(trackingNumber) {
        if (delivery == null || delivery?.trackingNumber != trackingNumber) {
            viewModel.loadDeliveryByTracking(trackingNumber)
        }
    }

    LaunchedEffect(delivery) {
        val d = delivery ?: return@LaunchedEffect
        val pickupLat = d.pickupLatitude ?: return@LaunchedEffect
        val pickupLng = d.pickupLongitude ?: return@LaunchedEffect
        val deliveryLat = d.deliveryLatitude ?: return@LaunchedEffect
        val deliveryLng = d.deliveryLongitude ?: return@LaunchedEffect

        loadingRoute = true
        scope.launch {
            fetchDirections(pickupLat, pickupLng, deliveryLat, deliveryLng)?.let { result ->
                routeCoords = result.first
                instructions = result.second
                totalDistance = result.third
                totalDuration = result.fourth
            }
            loadingRoute = false
        }
    }

    Box(Modifier.fillMaxSize().background(LuxuryBlack)) {
        delivery?.let { d ->
            DeliveryMapView(
                pickupLat = d.pickupLatitude ?: 6.5244,
                pickupLng = d.pickupLongitude ?: 3.3792,
                deliveryLat = d.deliveryLatitude ?: 6.4643,
                deliveryLng = d.deliveryLongitude ?: 3.3942,
                riderLat = 6.5044,
                riderLng = 3.3692,
                modifier = Modifier.fillMaxSize(),
                interactive = true,
                routePoints = routeCoords.ifEmpty { null }
            )

            Column(Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { viewModel.navigateBack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextMain)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(d.trackingNumber, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                            Text(d.status.replace("_", " "), color = BiroBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(Modifier.size(36.dp).clip(CircleShape).background(BiroBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.NearMe, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                if (loadingRoute) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 8.dp
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BiroBlue, strokeWidth = 2.dp)
                            Spacer(Modifier.width(12.dp))
                            Text("Loading route...", color = TextGray, fontSize = 12.sp)
                        }
                    }
                } else if (instructions.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).heightIn(max = 200.dp),
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 8.dp
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(
                                Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("$totalDistance", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                                Text("~$totalDuration", color = BiroBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("Next: ${instructions.firstOrNull()?.text ?: ""}", color = TextGray, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (d.status == "ASSIGNED") {
                        PremiumGradientButton(
                            text = "Confirm Pickup - Navigate to Origin",
                            icon = Icons.Default.Navigation,
                            onClick = { viewModel.updateDeliveryStatus(d.trackingNumber, "PICKED_UP") },
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        )
                    } else if (d.status == "PICKED_UP") {
                        PremiumGradientButton(
                            text = "Start Transit to Destination",
                            icon = Icons.Default.Route,
                            onClick = { viewModel.updateDeliveryStatus(d.trackingNumber, "OUT_FOR_DELIVERY") },
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        )
                    } else if (d.status == "OUT_FOR_DELIVERY") {
                        PremiumGradientButton(
                            text = "Complete Delivery - Enter OTP",
                            icon = Icons.Default.Verified,
                            onClick = { showBottomSheet = true },
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        )
                    } else if (d.status == "DELIVERED") {
                        OutlinedGradientButton(
                            text = "Back to Dashboard",
                            icon = Icons.Default.Home,
                            onClick = { viewModel.navigateToRoot(AppView.Dashboard) },
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(d.itemName, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("${d.pickupAddress.split(",").first()} \u2192 ${d.deliveryAddress.split(",").first()}",
                                    color = TextGray, fontSize = 10.sp, maxLines = 1)
                            }
                            Text("\u20A6${d.totalAmount.toInt()}", color = BiroBlue, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        } ?: run {
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = BiroBlue)
                Spacer(Modifier.height(16.dp))
                Text("Loading navigation...", color = Color.White)
                Spacer(Modifier.height(16.dp))
                OutlinedGradientButton("Back", onClick = { viewModel.navigateBack() })
            }
        }
    }

    if (showBottomSheet && delivery != null && delivery!!.status == "OUT_FOR_DELIVERY") {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val scope2 = rememberCoroutineScope()
        val dismiss: () -> Unit = { scope2.launch { sheetState.hide(); showBottomSheet = false } }

        ModalBottomSheet(
            onDismissRequest = dismiss,
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
                Text("Verify Delivery", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
                Spacer(Modifier.height(8.dp))
                Text("Ask customer for their 4-digit security code", color = TextGray, fontSize = 13.sp)
                Spacer(Modifier.height(16.dp))

                Surface(color = BiroBlue.copy(alpha = 0.08f), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Customer's OTP", color = TextGray, fontSize = 12.sp)
                        Text(delivery!!.otpCode, color = BiroBlue, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 8.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = otpInput,
                    onValueChange = { if (it.length <= 4) { otpInput = it; otpError = "" } },
                    placeholder = { Text("Enter 4-digit OTP") },
                    singleLine = true,
                    isError = otpError.isNotEmpty(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                        focusedBorderColor = BiroBlue, unfocusedBorderColor = CardBorderGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (otpError.isNotEmpty()) {
                    Text(otpError, color = DangerRed, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                }

                Spacer(Modifier.height(20.dp))
                PremiumGradientButton(
                    text = "Verify & Complete Handover",
                    onClick = {
                        if (otpInput.length != 4) {
                            otpError = "Enter a valid 4-digit code"
                        } else if (otpInput != delivery!!.otpCode) {
                            otpError = "Invalid OTP. Please try again."
                        } else {
                            viewModel.verifyDeliveryOtp(delivery!!.trackingNumber, otpInput) { success ->
                                if (success) dismiss()
                                else otpError = "Verification failed. Try again."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                )
            }
        }
    }
}

private suspend fun fetchDirections(
    pickupLat: Double, pickupLng: Double,
    deliveryLat: Double, deliveryLng: Double
): FourTuple<List<Pair<Double, Double>>, List<TurnInstruction>, String, String>? {
    return withContext(Dispatchers.IO) {
        try {
            val token = BuildConfig.MAPBOX_ACCESS_TOKEN
            if (token.isBlank()) return@withContext null

            val url = "https://api.mapbox.com/directions/v5/mapbox/driving/$pickupLng,$pickupLat;$deliveryLng,$deliveryLat?access_token=$token&geometries=geojson&steps=true&language=en&overview=full"

            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null

            val json = JSONObject(body)
            val route = json.getJSONArray("routes").getJSONObject(0)
            val geometry = route.getJSONObject("geometry")
            val coordsArray = geometry.getJSONArray("coordinates")

            val coords = mutableListOf<Pair<Double, Double>>()
            for (i in 0 until coordsArray.length()) {
                val pt = coordsArray.getJSONArray(i)
                coords.add(Pair(pt.getDouble(1), pt.getDouble(0)))
            }

            val legs = route.getJSONArray("legs").getJSONObject(0)
            val distance = legs.getDouble("distance")
            val duration = legs.getDouble("duration")

            val distanceText = if (distance < 1000) "${distance.toInt()} m" else "${"%.1f".format(distance / 1000)} km"
            val durationText = if (duration < 60) "${duration.toInt()} sec" else "${"%.0f".format(duration / 60)} min"

            val steps = legs.getJSONArray("steps")
            val turnList = mutableListOf<TurnInstruction>()
            for (i in 0 until steps.length()) {
                val step = steps.getJSONObject(i)
                val maneuver = step.getJSONObject("maneuver")
                val instruction = maneuver.getString("instruction")
                val stepDist = step.getDouble("distance")
                val stepDistText = if (stepDist < 1000) "${stepDist.toInt()} m" else "${"%.1f".format(stepDist / 1000)} km"
                val icon = maneuver.optString("type", "turn")

                val turnIcon = when (icon) {
                    "turn" -> Icons.Default.TurnRight
                    "straight" -> Icons.Default.ArrowUpward
                    "ramp", "fork" -> Icons.Default.ArrowForward
                    "arrive" -> Icons.Default.LocationOn
                    else -> Icons.Default.Navigation
                }
                turnList.add(TurnInstruction(
                    text = instruction,
                    distance = stepDistText,
                    icon = {
                        Icon(
                            imageVector = turnIcon,
                            contentDescription = null,
                            tint = BiroBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                ))
            }

            FourTuple(coords, turnList, distanceText, durationText)
        } catch (e: Exception) {
            Log.e("MapNavigation", "Directions API error: ${e.message}", e)
            null
        }
    }
}

private data class FourTuple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
