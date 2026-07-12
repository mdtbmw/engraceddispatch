package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.Parcel
import com.example.data.ParcelStatus
import com.example.ui.components.BottomNav
import com.example.ui.components.ScreenHeader
import com.example.ui.components.RoundedSheet
import com.example.ui.components.StaggeredItem
import com.example.ui.theme.*
import com.example.viewmodel.DeliveryViewModel

@Composable
fun OrderLogsScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val parcels by viewModel.parcels.collectAsState()
    var activeFilter by remember { mutableStateOf("All") }
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark

    val filteredParcels = remember(parcels, activeFilter) {
        when (activeFilter) {
            "Transit" -> parcels.filter { it.status == ParcelStatus.TRANSIT }
            "Delivered" -> parcels.filter { it.status == ParcelStatus.DELIVERED }
            "Cancelled" -> parcels.filter { it.status == ParcelStatus.CANCELLED }
            else -> parcels
        }
    }

    var currentPage by remember { mutableStateOf(0) }
    val itemsPerPage = 4

    val totalPages = remember(filteredParcels) {
        ((filteredParcels.size + itemsPerPage - 1) / itemsPerPage).coerceAtLeast(1)
    }

    LaunchedEffect(activeFilter) {
        currentPage = 0
    }

    val paginatedParcels = remember(filteredParcels, currentPage) {
        val startIndex = currentPage * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, filteredParcels.size)
        if (startIndex < filteredParcels.size) {
            filteredParcels.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HeaderBgColor)
        ) {
            ScreenHeader(
                title = "Order History",
                onBack = { onNavigate("Dashboard") }
            )

            RoundedSheet(
                modifier = Modifier.weight(1f),
                containerColor = if (isDark) BackgroundDark else BackgroundLight
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                // Horizontal scrollable tags filters
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("All", "Transit", "Delivered", "Cancelled")
                    items(filters) { filter ->
                        val isSelected = activeFilter == filter
                        val selectedBg = if (isDark) Gold else Obsidian
                        val selectedText = if (isDark) Obsidian else Gold
                        val unselectedBg = if (isDark) Charcoal else Color(0xFFF1F5F9)
                        val unselectedBorder = if (isDark) Color(0xFF2C2C2C) else Color(0xFFE2E8F0)
                        val unselectedText = if (isDark) TextGray else TextGray

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) selectedBg else unselectedBg)
                                .border(1.dp, if (isSelected) selectedBg else unselectedBorder, CircleShape)
                                .clickable { activeFilter = filter }
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = filter,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) selectedText else unselectedText
                            )
                        }
                    }
                }

                // Order History Logs Items List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (paginatedParcels.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No historical parcels found", color = TextGray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    itemsIndexed(paginatedParcels) { index, parcel ->
                        // StaggeredItem animated entry
                        StaggeredItem(index = index) {
                            OrderHistoryItem(
                                parcel = parcel,
                                onClick = {
                                    viewModel.selectParcelForTracking(parcel.id)
                                    onNavigate("ActiveTracking")
                                }
                            )
                        }
                    }
                }

                // Clean and cool pagination controls
                if (totalPages > 1) {
                    PaginationControls(
                        currentPage = currentPage,
                        totalPages = totalPages,
                        onPageSelected = { currentPage = it }
                    )
                }
            }
        }
    }
}
}

@Composable
fun OrderHistoryItem(
    parcel: Parcel,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark
    val innerBgColor = if (isDark) Color(0xFF1D1D1D) else GoldenWhiteLight

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Charcoal,
        border = BorderStroke(1.dp, if (isDark) BorderDark else Slate),
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // A subtle left status accent indicator bar
            val accentColor = when (parcel.status) {
                ParcelStatus.PENDING -> Color(0xFF2196F3)
                ParcelStatus.ASSIGNED -> Color(0xFF9C27B0)
                ParcelStatus.TRANSIT -> if (isDark) Gold else Obsidian
                ParcelStatus.OUT_FOR_DELIVERY -> Color(0xFFFF9800)
                ParcelStatus.DELIVERED -> Color(0xFF4CAF50)
                ParcelStatus.CANCELLED -> Color(0xFFF44336)
            }
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically)
                    .background(accentColor)
            )

            Column(modifier = Modifier.weight(1f).padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(innerBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (parcel.status == ParcelStatus.DELIVERED) {
                            Icon(Icons.Filled.Inbox, null, tint = if (isDark) Gold else Obsidian, modifier = Modifier.size(24.dp))
                        } else {
                            Image(
                                painter = rememberAsyncImagePainter(parcel.imageUrl),
                                contentDescription = parcel.itemName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text("ID: ${parcel.id}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = AppTextColor)
                        Text(parcel.itemName, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextGray)
                    }
                }

                // Dynamic Styled Tag
                val tagColor = when (parcel.status) {
                    ParcelStatus.PENDING -> Color(0xFF2196F3)
                    ParcelStatus.ASSIGNED -> Color(0xFF9C27B0)
                    ParcelStatus.TRANSIT -> if (isDark) Gold else Obsidian
                    ParcelStatus.OUT_FOR_DELIVERY -> Color(0xFFFF9800)
                    ParcelStatus.DELIVERED -> Color(0xFF4CAF50)
                    ParcelStatus.CANCELLED -> Color(0xFFF44336)
                }

                val tagBg = when (parcel.status) {
                    ParcelStatus.PENDING -> Color(0x202196F3)
                    ParcelStatus.ASSIGNED -> Color(0x209C27B0)
                    ParcelStatus.TRANSIT -> if (isDark) Gold.copy(alpha = 0.15f) else Obsidian.copy(alpha = 0.08f)
                    ParcelStatus.OUT_FOR_DELIVERY -> Color(0x20FF9800)
                    ParcelStatus.DELIVERED -> Color(0x204CAF50)
                    ParcelStatus.CANCELLED -> Color(0x20F44336)
                }

                val tagText = when (parcel.status) {
                    ParcelStatus.PENDING -> "PENDING DISPATCH"
                    ParcelStatus.ASSIGNED -> "COURIER ASSIGNED"
                    ParcelStatus.TRANSIT -> "IN TRANSIT"
                    ParcelStatus.OUT_FOR_DELIVERY -> "OUT FOR DELIVERY"
                    ParcelStatus.DELIVERED -> "DELIVERED"
                    ParcelStatus.CANCELLED -> "CANCELLED"
                }

                Box(
                    modifier = Modifier
                        .background(tagBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tagText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = tagColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer info: delivery progress date / price metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (parcel.status == ParcelStatus.TRANSIT) Icons.Filled.LocalShipping else Icons.Filled.AccessTime,
                        contentDescription = "Timing",
                        tint = TextGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (parcel.status == ParcelStatus.TRANSIT) "Arriving Today" else parcel.dateString,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray
                    )
                }

                Text(
                    text = "₦${String.format("%,.2f", parcel.price)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDark) Gold else Obsidian
                )
            }
        }
    }
}
}

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (totalPages <= 1) return
    
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark
    val activeColor = if (isDark) Gold else Obsidian
    val activeTextColor = if (isDark) Obsidian else Gold
    val inactiveColor = if (isDark) Charcoal else Color(0xFFF1F5F9)
    val inactiveTextColor = if (isDark) TextGray else TextGray
    val borderColor = if (isDark) Color(0xFF2C2C2C) else Color(0xFFE2E8F0)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Prev button
        IconButton(
            onClick = { if (currentPage > 0) onPageSelected(currentPage - 1) },
            enabled = currentPage > 0,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (currentPage > 0) inactiveColor else inactiveColor.copy(alpha = 0.5f))
                .border(1.dp, borderColor, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous Page",
                tint = if (currentPage > 0) (if (isDark) Gold else Obsidian) else TextGray,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Page Numbers
        for (i in 0 until totalPages) {
            val isSelected = i == currentPage
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) activeColor else inactiveColor)
                    .border(1.dp, if (isSelected) activeColor else borderColor, CircleShape)
                    .clickable { onPageSelected(i) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${i + 1}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) activeTextColor else inactiveTextColor
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Next button
        IconButton(
            onClick = { if (currentPage < totalPages - 1) onPageSelected(currentPage + 1) },
            enabled = currentPage < totalPages - 1,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (currentPage < totalPages - 1) inactiveColor else inactiveColor.copy(alpha = 0.5f))
                .border(1.dp, borderColor, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next Page",
                tint = if (currentPage < totalPages - 1) (if (isDark) Gold else Obsidian) else TextGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
