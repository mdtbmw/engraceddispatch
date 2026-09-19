package com.esdispatch.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esdispatch.data.Parcel
import com.esdispatch.data.ParcelStatus
import com.esdispatch.ui.theme.*
import com.esdispatch.util.CustomToastBridge
import com.esdispatch.viewmodel.DeliveryViewModel
import com.esdispatch.viewmodel.ToastType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisputeReportBottomSheet(
    parcel: Parcel,
    viewModel: DeliveryViewModel,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategory by remember { mutableStateOf("Courier Delayed / No Progress") }
    var description by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val initiallyDisputed = parcel.isDisputed || parcel.status == ParcelStatus.DISPUTED || parcel.disputeReason.isNotBlank()
    var hasSubmitted by remember { mutableStateOf(initiallyDisputed) }
    var activeCategory by remember { mutableStateOf(parcel.disputeReason.ifBlank { "Courier Delayed / No Progress" }) }
    var activeNotes by remember { mutableStateOf(parcel.disputeNotes) }

    val categories = listOf(
        "Courier Delayed / No Progress",
        "Package Damaged / Tampered",
        "Wrong Destination / Route",
        "Courier Unresponsive to Calls",
        "Billing / Pricing Discrepancy",
        "General Operational Inquiry"
    )

    AppModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Gold.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ReportProblem,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (hasSubmitted) "Issue Resolution Status" else "Report Delivery Issue",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isDark) GoldLight else Obsidian
                        )
                        Text(
                            text = "Order #${parcel.id.takeLast(6)} • ${parcel.itemName.ifBlank { "Shipment" }}",
                            fontSize = 12.sp,
                            color = TextGray
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextGray)
                }
            }

            if (hasSubmitted) {
                // Immediate Feedback Status Tracker: Submitted -> Under Review -> Resolved
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Charcoal else GoldenWhiteSurface,
                    border = BorderStroke(1.dp, if (isDark) BorderDark else Slate),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "CASE PROGRESS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = TextGray,
                            letterSpacing = 1.sp
                        )

                        // 3-Stage Progress Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Step 1: Submitted (Done)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2E7D32)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                                Text("Submitted", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Obsidian)
                            }

                            // Connecting Line 1
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(2.dp)
                                    .padding(horizontal = 6.dp)
                                    .background(Gold)
                            )

                            // Step 2: Under Review (Active)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Gold),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = Obsidian, modifier = Modifier.size(16.dp))
                                }
                                Text("Under Review", fontSize = 10.sp, fontWeight = FontWeight.Black, color = if (isDark) Gold else Color(0xFF996B00))
                            }

                            // Connecting Line 2
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(2.dp)
                                    .padding(horizontal = 6.dp)
                                    .background(if (isDark) BorderDark else Slate)
                            )

                            // Step 3: Resolved (Pending)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF262626) else Color(0xFFE0E0E0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                                }
                                Text("Resolved", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextGray)
                            }
                        }

                        // Case Status Information Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) Color(0xFF222226) else Color(0xFFF9F9FB),
                            border = BorderStroke(0.8.dp, if (isDark) BorderDark else Slate),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Reported Issue:", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Medium)
                                    Text(activeCategory, fontSize = 11.sp, color = if (isDark) Color.White else Obsidian, fontWeight = FontWeight.Bold)
                                }

                                if (activeNotes.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text("Notes:", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Medium)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(activeNotes, fontSize = 11.sp, color = if (isDark) Color.White else Obsidian, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Text(
                                    text = "Central dispatch operations has received this inquiry. A supervisor is reviewing courier GPS telemetry and order milestones.",
                                    fontSize = 11.sp,
                                    color = TextGray,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold,
                        contentColor = Obsidian
                    )
                ) {
                    Text(
                        text = "Done",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            } else {
                Text(
                    text = "Select the issue category so our central dispatch operations can assist and prioritize your case immediately.",
                    fontSize = 12.sp,
                    color = TextGray,
                    lineHeight = 17.sp
                )

                // Issue Categories
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        val bgColor = if (isSelected) {
                            if (isDark) Gold.copy(alpha = 0.15f) else Obsidian.copy(alpha = 0.08f)
                        } else {
                            if (isDark) Charcoal else GoldenWhiteLight
                        }
                        val borderColor = if (isSelected) Gold else Color.Transparent

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = category,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) (if (isDark) Gold else Obsidian) else AppOnSurface
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= 400) description = it },
                    label = { Text("Details or Notes (Optional)") },
                    placeholder = { Text("Describe what occurred so dispatch can intervene...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = if (isDark) BorderDark else BorderLight,
                        focusedLabelColor = Gold,
                        cursorColor = Gold
                    )
                )

                // Submit Button
                Button(
                    onClick = {
                        if (isSubmitting) return@Button
                        isSubmitting = true
                        viewModel.reportDeliveryIssue(parcel.id, selectedCategory, description) { success, errorMsg ->
                            isSubmitting = false
                            if (success) {
                                activeCategory = selectedCategory
                                activeNotes = description
                                hasSubmitted = true
                                CustomToastBridge.show("Issue reported. Central operations notified.", ToastType.SUCCESS)
                            } else {
                                CustomToastBridge.show(errorMsg ?: "Failed to record issue.", ToastType.ERROR)
                            }
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold,
                        contentColor = Obsidian
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Obsidian,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Submit to Dispatch Support",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
