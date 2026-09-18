package com.esdispatch.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ReportProblem
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
                            text = "Report Delivery Issue",
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
                            CustomToastBridge.show("Issue reported. Central operations notified.", ToastType.SUCCESS)
                            onDismiss()
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
