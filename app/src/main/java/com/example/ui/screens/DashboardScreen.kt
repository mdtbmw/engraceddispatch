package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import java.util.Calendar
import kotlinx.coroutines.delay
import androidx.compose.ui.text.style.TextOverflow
import com.example.data.Delivery
import com.example.ui.DeliveryViewModel
import com.example.ui.navigation.AppView
import com.example.ui.theme.*
import com.example.ui.components.*

private data class DashPromo(val title: String, val subtitle: String, val icon: ImageVector, val gradient: Brush, val value: String)

private val promoGradients = listOf(
    BrandGradient,
    Brush.horizontalGradient(listOf(BiroBlue, DarkGradientBlue)),
    Brush.horizontalGradient(listOf(DarkGradientBlue, BiroBlue))
)

private val promos = listOf(
    DashPromo("Express 15% Off", "First ride promo", Icons.Default.Bolt, promoGradients[0], "SAVE 15%"),
    DashPromo("Same-Day Free", "Free delivery today", Icons.Default.Schedule, promoGradients[1], "FREE"),
    DashPromo("Referral Bonus", "Earn \u20A62000 per friend", Icons.Default.People, promoGradients[2], "\u20A62k")
)

@Composable
fun DashboardScreen(viewModel: DeliveryViewModel) {
    val deliveries by viewModel.allDeliveries.collectAsState()
    val walletBalance by viewModel.walletBalance.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val listState = rememberLazyListState()
    val pagerState = rememberPagerState(pageCount = { promos.size })
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    var overscrollProgress by remember { mutableFloatStateOf(0f) }
    var isRefreshing by remember { mutableStateOf(false) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.y > 0f && overscrollProgress >= 0f) {
                    val pull = (overscrollProgress + available.y / 400f).coerceIn(0f, 1.2f)
                    overscrollProgress = pull
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (overscrollProgress > 0f) {
                    val consumedY = available.y.coerceIn(-overscrollProgress * 200f, 0f)
                    overscrollProgress = (overscrollProgress + consumedY / 200f).coerceAtLeast(0f)
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (overscrollProgress >= 1f) {
                    isRefreshing = true
                    viewModel.refreshAllData {
                        isRefreshing = false
                    }
                }
                overscrollProgress = 0f
                return Velocity.Zero
            }
        }
    }

    val density = LocalDensity.current.density

    val headerHeight by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 115.dp
            else { (220f - (listState.firstVisibleItemScrollOffset / density).coerceIn(0f, 105f)).dp }
        }
    }
    val collapseProgress by remember {
        derivedStateOf { ((220f - headerHeight.value) / 105f).coerceIn(0f, 1f) }
    }

    val activeCount = deliveries.count { it.status in listOf("PENDING", "ASSIGNED", "PICKED_UP") }
    val completedCount = deliveries.count { it.status == "DELIVERED" }
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when { hour < 12 -> "Good morning"; hour < 17 -> "Good afternoon"; else -> "Good evening" }

    val headerCorner = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
    val pullExtra = (overscrollProgress * 30f).dp

    val displayName = currentUser.fullName.split(" ").firstOrNull()?.uppercase() ?: "USER"

    Box(Modifier.fillMaxSize().background(BackgroundGray)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection),
            contentPadding = PaddingValues(top = 236.dp, bottom = 120.dp)
        ) {
            if (isSearchActive) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search tracking, item, address...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BiroBlue) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, contentDescription = "Clear") }
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedBorderColor = BiroBlue, unfocusedBorderColor = CardBorderGray),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    val searchResults = viewModel.searchDeliveries(searchQuery)
                    if (searchQuery.isNotEmpty() && searchResults.isNotEmpty()) {
                        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                            Text("Search Results (${searchResults.size})", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                            searchResults.take(5).forEach { d ->
                                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(12.dp)).clickable { viewModel.setTrackingDelivery(d); viewModel.navigateTo(AppView.ActiveTracking(d.trackingNumber)) }) {
                                    Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(36.dp).clip(CircleShape).background(BiroBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Inventory2, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(18.dp)) }
                                        Spacer(Modifier.width(10.dp))
                                        Column(Modifier.weight(1f)) { Text(d.trackingNumber, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold); Text("${d.itemName} - \u20A6${d.totalAmount.toInt()}", color = TextGray, fontSize = 11.sp) }
                                        StatusBadge(d.status)
                                    }
                                }
                            }
                        }
                    } else if (searchQuery.isNotEmpty() && searchResults.isEmpty()) {
                        EmptyState(Icons.Default.SearchOff, "No results found")
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
            item { HeroCard(viewModel) }
            item { Spacer(Modifier.height(16.dp)) }
            item { ServicesRow(viewModel) }
            item { Spacer(Modifier.height(20.dp)) }
            item { GradientPromos(pagerState = pagerState, overscrollProgress = overscrollProgress, viewModel = viewModel) }
            item { Spacer(Modifier.height(20.dp)) }
            item { ActiveDeliveryCard(deliveries, viewModel) }
            item { Spacer(Modifier.height(20.dp)) }
            item { StatsGrid(activeCount, completedCount, walletBalance, currentUser.rating, viewModel) }
            item { Spacer(Modifier.height(20.dp)) }
            item { ReferralCard(viewModel) }
            item { Spacer(Modifier.height(20.dp)) }
            item { RecentDeliveriesSection(deliveries, viewModel) }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight + pullExtra)
                .shadow(8.dp, headerCorner, clip = false)
                .background(BrandGradient, headerCorner)
        ) {
            if (collapseProgress > 0.6f) CollapsedHeader(greeting, displayName, viewModel)
            else ExpandedHeader(greeting, displayName, activeCount, walletBalance, collapseProgress, viewModel, onSearchToggle = { isSearchActive = !isSearchActive })
        }

        if (overscrollProgress > 0.03f && !isRefreshing) {
            Box(Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = headerHeight + pullExtra + 8.dp), contentAlignment = Alignment.Center) {
                Surface(shape = RoundedCornerShape(20.dp), color = BiroBlue, shadowElevation = 4.dp) {
                    Text(
                        if (overscrollProgress >= 1f) "\u2193 Release to refresh" else "\u2193 Pull to refresh",
                        color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (isRefreshing) {
            Box(Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = headerHeight + pullExtra + 8.dp), contentAlignment = Alignment.Center) {
                Surface(shape = RoundedCornerShape(20.dp), color = BiroBlue, shadowElevation = 4.dp) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp)); Text("Refreshing...", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GradientPromos(pagerState: androidx.compose.foundation.pager.PagerState, overscrollProgress: Float, viewModel: DeliveryViewModel) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp).padding(bottom = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Promotions", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Text("View all", color = BiroBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { viewModel.navigateTo(AppView.Promotions) })
        }
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().height(150.dp), beyondViewportPageCount = 1) { i ->
            val p = promos[i]
            val pageOffset = (pagerState.currentPage - i) + pagerState.currentPageOffsetFraction
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).graphicsLayer {
                    scaleX = 1f - 0.05f * kotlin.math.abs(pageOffset)
                    scaleY = 1f - 0.05f * kotlin.math.abs(pageOffset)
                    alpha = 1f - 0.2f * kotlin.math.abs(pageOffset.coerceIn(-1f, 1f))
                }.clip(RoundedCornerShape(24.dp)).clickable { viewModel.navigateTo(AppView.Promotions) }
            ) {
                Box(Modifier.fillMaxSize().background(p.gradient, RoundedCornerShape(24.dp))) {
                    Row(Modifier.fillMaxSize().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                                Text(p.value, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(p.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            Text(p.subtitle, color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                        }
                        Box(Modifier.size(56.dp).background(Color.White.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(p.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp).padding(top = 8.dp), horizontalArrangement = Arrangement.Center) {
            repeat(promos.size) { idx ->
                val s by animateDpAsState(targetValue = if (pagerState.currentPage == idx) 20.dp else 6.dp, animationSpec = tween(300), label = "dot")
                val a by animateFloatAsState(targetValue = if (pagerState.currentPage == idx) 1f else 0.35f, animationSpec = tween(300), label = "dotA")
                Box(Modifier.padding(horizontal = 3.dp).height(6.dp).width(s).clip(RoundedCornerShape(3.dp)).background(BiroBlue.copy(alpha = a)))
            }
        }
    }
}

@Composable
private fun CollapsedHeader(greeting: String, displayName: String, viewModel: DeliveryViewModel) {
    val user by viewModel.currentUser.collectAsState()
    Row(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = user.photoUrl, contentDescription = "Profile", modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)).clickable { viewModel.navigateTo(AppView.Profile) })
            Spacer(Modifier.width(10.dp))
            Column { Text(greeting, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp); Text(displayName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold) }
        }
        Box(Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)).clickable { viewModel.navigateTo(AppView.Notifications) }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)) }
    }
}

@Composable
private fun ExpandedHeader(greeting: String, displayName: String, activeCount: Int, walletBalance: Double, progress: Float, viewModel: DeliveryViewModel, onSearchToggle: () -> Unit) {
    val user by viewModel.currentUser.collectAsState()
    val alpha = (1f - progress * 1.5f).coerceIn(0f, 1f)
    val searchAlpha = (1f - progress * 2f).coerceIn(0f, 1f)
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    AsyncImage(model = user.photoUrl, contentDescription = "Profile", modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)).clickable { viewModel.navigateTo(AppView.Profile) })
                    Spacer(Modifier.width(12.dp))
                    Column { Text(greeting, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold); Text(displayName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold) }
                }
                Box(Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)).clickable { viewModel.navigateTo(AppView.Notifications) }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().graphicsLayer(alpha = alpha)) {
                Surface(color = Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(20.dp)) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalMall, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(12.dp)); Spacer(Modifier.width(4.dp))
                        Text("$activeCount active", color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.width(8.dp))
                        Text("\u2022", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp); Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(10.dp)); Spacer(Modifier.width(4.dp))
                        Text("\u20A6${String.format("%,.0f", walletBalance)}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(54.dp).graphicsLayer { this.alpha = searchAlpha; translationY = (progress * 20f) }.clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.12f)).clickable { onSearchToggle() }, contentAlignment = Alignment.CenterStart) {
            Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp)); Spacer(Modifier.width(10.dp)); Text("Search tracking number...", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp) }
        }
    }
}

@Composable
private fun HeroCard(viewModel: DeliveryViewModel) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1.1f)) { Text("Move Anything,\nAnywhere", color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 28.sp); Spacer(Modifier.height(6.dp)); Text("Fast. Reliable. Always on time.", color = TextMuted, fontSize = 12.sp); Spacer(Modifier.height(16.dp)); PremiumGradientButton("+ New Delivery", onClick = { viewModel.selectDeliveryType("Express"); viewModel.navigateTo(AppView.BookingExpress) }, modifier = Modifier.height(38.dp), icon = Icons.Default.Add) }
            Box(Modifier.weight(0.9f).height(100.dp), contentAlignment = Alignment.Center) { Box(Modifier.size(80.dp).background(BiroBlue.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(40.dp)) } }
        }
    }
}

@Composable
private fun ServicesRow(viewModel: DeliveryViewModel) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ServiceCard("Express", Icons.Default.Bolt, Color(0xFFDCFCE7), Color(0xFF16A34A), viewModel)
        ServiceCard("Economy", Icons.Default.LocalMall, Color(0xFFDBEAFE), Color(0xFF2563EB), viewModel)
        ServiceCard("Batch", Icons.Default.Inventory2, Color(0xFFF3E8FF), Color(0xFF9333EA), viewModel)
        ServiceCard("Multi", Icons.Default.AltRoute, Color(0xFFFFEDD5), Color(0xFFEA580C), viewModel)
    }
}

@Composable
private fun RowScope.ServiceCard(title: String, icon: ImageVector, bg: Color, iconColor: Color, viewModel: DeliveryViewModel) {
    val dest = when (title) {
        "Express" -> AppView.BookingExpress
        "Economy" -> AppView.BookingEconomy
        "Batch" -> AppView.BookingBatch
        "Multi" -> AppView.BookingMulti
        else -> AppView.BookingExpress
    }
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).clickable { viewModel.selectDeliveryType(title); viewModel.navigateTo(dest) }) {
        Column(Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.height(8.dp)); Text(title, color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold); Text("Delivery", color = TextMuted, fontSize = 9.sp)
        }
    }
}

@Composable
private fun ActiveDeliveryCard(deliveries: List<Delivery>, viewModel: DeliveryViewModel) {
    val active = deliveries.firstOrNull { it.status in listOf("PENDING", "ASSIGNED", "PICKED_UP", "OUT_FOR_DELIVERY") }
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Active Delivery", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold); Text("View all", color = BiroBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { viewModel.navigateTo(AppView.OrderLogs) }) }
        if (active == null) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(40.dp)); Spacer(Modifier.height(8.dp)); Text("No Active Shipments", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold); Text("All deliveries completed", color = TextMuted, fontSize = 11.sp) }
            }
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).clickable { viewModel.setTrackingDelivery(active); viewModel.navigateTo(AppView.ActiveTracking(active.trackingNumber)) }) {
                Column(Modifier.padding(20.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = BiroBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) { Text(active.trackingNumber, color = BiroBlue, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
                        Text("${active.etaMinutes} min", color = BiroBlue, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(8.dp).clip(CircleShape).background(BiroBlue)); Spacer(Modifier.width(10.dp)); Text(active.pickupAddress.split(",").first(), color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF97316))); Spacer(Modifier.width(10.dp)); Text(active.deliveryAddress.split(",").first(), color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                        }
                        Box(Modifier.size(60.dp).clip(CircleShape).background(BiroBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(30.dp)) }
                    }
                    Spacer(Modifier.height(16.dp)); HorizontalDivider(color = CardBorderGray); Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFE2E8F0)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp)) }; Spacer(Modifier.width(10.dp)); Column { Text(active.riderName, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold); Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp)); Spacer(Modifier.width(2.dp)); Text("4.9", color = TextMuted, fontSize = 11.sp) } } }
                        val context = androidx.compose.ui.platform.LocalContext.current
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFEEF2F6)).clickable {
                                android.widget.Toast.makeText(context, "Opening chat with ${active.riderName}...", android.widget.Toast.LENGTH_SHORT).show()
                            }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Forum, contentDescription = "Chat", tint = BiroBlue, modifier = Modifier.size(16.dp)) }
                            Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFEEF2F6)).clickable {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:+2348001234567"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    android.widget.Toast.makeText(context, "Call rider: +234 800 123 4567", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Phone, contentDescription = "Call", tint = BiroBlue, modifier = Modifier.size(16.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsGrid(activeCount: Int, completedCount: Int, walletBalance: Double, userRating: Float, viewModel: DeliveryViewModel) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatTile("$activeCount", "Active", Icons.Default.LocalMall, Color(0xFFEFF6FF), Color(0xFF2563EB)) { viewModel.navigateTo(AppView.OrderLogs) }
        StatTile("$completedCount", "Completed", Icons.Default.CheckBox, Color(0xFFECFDF5), Color(0xFF10B981)) { viewModel.navigateTo(AppView.OrderLogs) }
        StatTileAnimated(walletBalance, "Wallet", Icons.Default.AccountBalanceWallet, Color(0xFFEEEDFF), BiroBlue) { viewModel.navigateTo(AppView.Wallet) }
        StatTile(String.format("%.1f", userRating), "Rating", Icons.Default.Star, Color(0xFFEFF6FF), Color(0xFF3B82F6)) {}
    }
}

@Composable
private fun RowScope.StatTileAnimated(value: Double, label: String, icon: ImageVector, bg: Color, iconColor: Color, onClick: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(32.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp)) }
            Spacer(Modifier.height(6.dp)); AnimatedCounter(targetValue = value, prefix = "\u20A6", fontSize = 13.sp); Spacer(Modifier.height(2.dp)); Text(label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, lineHeight = 12.sp)
        }
    }
}

@Composable
private fun RowScope.StatTile(value: String, label: String, icon: ImageVector, bg: Color, iconColor: Color, onClick: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(32.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp)) }
            Spacer(Modifier.height(6.dp)); Text(value, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(2.dp)); Text(label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, lineHeight = 12.sp)
        }
    }
}

@Composable
private fun ReferralCard(viewModel: DeliveryViewModel) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(24.dp)).clickable { viewModel.navigateTo(AppView.Referral) }.background(BrandGradient, RoundedCornerShape(24.dp))) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1.2f)) { Text("Refer & Earn", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold); Spacer(Modifier.height(4.dp)); Text("Invite friends and earn rewards.", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp); Spacer(Modifier.height(12.dp)); Text("Invite Now \u2192", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            Box(Modifier.weight(0.8f).height(70.dp), contentAlignment = Alignment.Center) { Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(48.dp)) }
        }
    }
}

@Composable
private fun RecentDeliveriesSection(deliveries: List<Delivery>, viewModel: DeliveryViewModel) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Recent Deliveries", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold); Text("View all", color = BiroBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { viewModel.navigateTo(AppView.OrderLogs) }) }
        deliveries.take(3).forEach { d ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).clip(RoundedCornerShape(16.dp)).clickable { viewModel.setTrackingDelivery(d); viewModel.navigateTo(AppView.ActiveTracking(d.trackingNumber)) }) {
                Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(BiroBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Inventory2, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(20.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text(d.trackingNumber, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold); Text("${d.pickupAddress.split(",").first()} \u2192 ${d.deliveryAddress.split(",").first()}", color = TextGray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    StatusBadge(d.status)
                }
            }
        }
    }
}
