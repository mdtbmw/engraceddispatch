package com.esdispatch.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.esdispatch.ui.theme.Gold
import com.esdispatch.ui.theme.isDarkTheme

enum class StatusTone {
    NEUTRAL,
    AMBER,
    PURPLE,
    BLUE,
    INDIGO,
    CYAN,
    TEAL,
    GREEN,
    RED
}

data class DeliveryStatusDisplay(
    val statusKey: String,
    val customerLabel: String,
    val adminLabel: String,
    val tone: StatusTone,
    val isLive: Boolean = false,
    val description: String = ""
) {
    @Composable
    fun containerColor(): Color {
        val dark = isDarkTheme
        return when (tone) {
            StatusTone.NEUTRAL -> if (dark) Color(0x26FFB800) else Color(0xFFFFF7E6)
            StatusTone.AMBER -> if (dark) Color(0x26F59E0B) else Color(0xFFFEF3C7)
            StatusTone.PURPLE -> if (dark) Color(0x268B5CF6) else Color(0xFFEDE9FE)
            StatusTone.BLUE -> if (dark) Color(0x263B82F6) else Color(0xFFDBEAFE)
            StatusTone.INDIGO -> if (dark) Color(0x266366F1) else Color(0xFFE0E7FF)
            StatusTone.CYAN -> if (dark) Color(0x2606B6D4) else Color(0xFFCFFAFE)
            StatusTone.TEAL -> if (dark) Color(0x2614B8A6) else Color(0xFFCCFBF1)
            StatusTone.GREEN -> if (dark) Color(0x2610B981) else Color(0xFFD1FAE5)
            StatusTone.RED -> if (dark) Color(0x26EF4444) else Color(0xFFFEE2E2)
        }
    }

    @Composable
    fun contentColor(): Color {
        val dark = isDarkTheme
        return when (tone) {
            StatusTone.NEUTRAL -> if (dark) Gold else Color(0xFF92400E)
            StatusTone.AMBER -> if (dark) Color(0xFFFBBF24) else Color(0xFF92400E)
            StatusTone.PURPLE -> if (dark) Color(0xFFA78BFA) else Color(0xFF5B21B6)
            StatusTone.BLUE -> if (dark) Color(0xFF60A5FA) else Color(0xFF1E40AF)
            StatusTone.INDIGO -> if (dark) Color(0xFF818CF8) else Color(0xFF3730A3)
            StatusTone.CYAN -> if (dark) Color(0xFF22D3EE) else Color(0xFF0E7490)
            StatusTone.TEAL -> if (dark) Color(0xFF2DD4BF) else Color(0xFF115E59)
            StatusTone.GREEN -> if (dark) Color(0xFF34D399) else Color(0xFF065F46)
            StatusTone.RED -> if (dark) Color(0xFFF87171) else Color(0xFF991B1B)
        }
    }

    @Composable
    fun borderColor(): Color {
        val dark = isDarkTheme
        return when (tone) {
            StatusTone.NEUTRAL -> if (dark) Gold.copy(alpha = 0.4f) else Color(0xFFFFD699)
            StatusTone.AMBER -> if (dark) Color(0xFFF59E0B).copy(alpha = 0.4f) else Color(0xFFFDE68A)
            StatusTone.PURPLE -> if (dark) Color(0xFF8B5CF6).copy(alpha = 0.4f) else Color(0xFFDDD6FE)
            StatusTone.BLUE -> if (dark) Color(0xFF3B82F6).copy(alpha = 0.4f) else Color(0xFFBFDBFE)
            StatusTone.INDIGO -> if (dark) Color(0xFF6366F1).copy(alpha = 0.4f) else Color(0xFFC7D2FE)
            StatusTone.CYAN -> if (dark) Color(0xFF06B6D4).copy(alpha = 0.4f) else Color(0xFFA5F3FC)
            StatusTone.TEAL -> if (dark) Color(0xFF14B8A6).copy(alpha = 0.4f) else Color(0xFF99F6E4)
            StatusTone.GREEN -> if (dark) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFA7F3D0)
            StatusTone.RED -> if (dark) Color(0xFFEF4444).copy(alpha = 0.4f) else Color(0xFFFECACA)
        }
    }

    companion object {
        fun fromParcelStatus(status: ParcelStatus): DeliveryStatusDisplay {
            return when (status) {
                ParcelStatus.PENDING -> DeliveryStatusDisplay(
                    statusKey = "received",
                    customerLabel = "Received",
                    adminLabel = "Received",
                    tone = StatusTone.NEUTRAL,
                    description = "Request received by dispatch"
                )
                ParcelStatus.QUEUED -> DeliveryStatusDisplay(
                    statusKey = "queued",
                    customerLabel = "Queued",
                    adminLabel = "Waiting for rider",
                    tone = StatusTone.AMBER,
                    description = "Awaiting available rider"
                )
                ParcelStatus.RESERVED_NEXT -> DeliveryStatusDisplay(
                    statusKey = "reserved",
                    customerLabel = "Rider reserved",
                    adminLabel = "Reserved next",
                    tone = StatusTone.PURPLE,
                    description = "Rider queued after current run"
                )
                ParcelStatus.ASSIGNED -> DeliveryStatusDisplay(
                    statusKey = "assigned",
                    customerLabel = "Rider assigned",
                    adminLabel = "Assigned",
                    tone = StatusTone.BLUE,
                    description = "Rider designated to route"
                )
                ParcelStatus.PICKED_UP -> DeliveryStatusDisplay(
                    statusKey = "picked_up",
                    customerLabel = "Parcel Picked Up",
                    adminLabel = "Picked Up",
                    tone = StatusTone.INDIGO,
                    isLive = true,
                    description = "Rider collected package from sender"
                )
                ParcelStatus.TRANSIT -> DeliveryStatusDisplay(
                    statusKey = "transit",
                    customerLabel = "In Transit",
                    adminLabel = "In Transit",
                    tone = StatusTone.CYAN,
                    isLive = true,
                    description = "Parcel on moving route"
                )
                ParcelStatus.OUT_FOR_DELIVERY -> DeliveryStatusDisplay(
                    statusKey = "out_for_delivery",
                    customerLabel = "Out for Delivery",
                    adminLabel = "Out for Delivery",
                    tone = StatusTone.CYAN,
                    isLive = true,
                    description = "Courier arriving shortly at destination"
                )
                ParcelStatus.ARRIVED -> DeliveryStatusDisplay(
                    statusKey = "arrived",
                    customerLabel = "Courier Arrived",
                    adminLabel = "Arrived",
                    tone = StatusTone.TEAL,
                    isLive = true,
                    description = "Rider within delivery proximity"
                )
                ParcelStatus.HANDOVER_VERIFIED -> DeliveryStatusDisplay(
                    statusKey = "handover_verified",
                    customerLabel = "Handover Verified",
                    adminLabel = "Handover Verified",
                    tone = StatusTone.TEAL,
                    isLive = true,
                    description = "PIN verified, capturing proof of delivery"
                )
                ParcelStatus.DELIVERED -> DeliveryStatusDisplay(
                    statusKey = "delivered",
                    customerLabel = "Delivered",
                    adminLabel = "Delivered",
                    tone = StatusTone.GREEN,
                    description = "Delivery completed and signed"
                )
                ParcelStatus.CANCELLED -> DeliveryStatusDisplay(
                    statusKey = "cancelled",
                    customerLabel = "Cancelled",
                    adminLabel = "Cancelled",
                    tone = StatusTone.RED,
                    description = "Delivery cancelled"
                )
            }
        }

        fun fromString(statusStr: String): DeliveryStatusDisplay {
            val normalized = statusStr.trim().uppercase()
            return try {
                fromParcelStatus(ParcelStatus.valueOf(normalized))
            } catch (_: Exception) {
                when (normalized) {
                    "RECEIVED", "PENDING" -> DeliveryStatusDisplay("received", "Received", "Received", StatusTone.NEUTRAL, description = "Request received by dispatch")
                    "QUEUED", "WAITING" -> DeliveryStatusDisplay("queued", "Queued", "Waiting for rider", StatusTone.AMBER, description = "Awaiting available rider")
                    "RESERVED", "RESERVED_NEXT" -> DeliveryStatusDisplay("reserved", "Rider reserved", "Reserved next", StatusTone.PURPLE, description = "Rider queued after current run")
                    "ASSIGNED" -> DeliveryStatusDisplay("assigned", "Rider assigned", "Assigned", StatusTone.BLUE, description = "Rider designated to route")
                    "PICKED_UP", "PICKUP" -> DeliveryStatusDisplay("picked_up", "Parcel Picked Up", "Picked Up", StatusTone.INDIGO, isLive = true, description = "Rider collected package from sender")
                    "TRANSIT" -> DeliveryStatusDisplay("transit", "In Transit", "In Transit", StatusTone.CYAN, isLive = true, description = "Parcel on moving route")
                    "OUT_FOR_DELIVERY" -> DeliveryStatusDisplay("out_for_delivery", "Out for Delivery", "Out for Delivery", StatusTone.CYAN, isLive = true, description = "Courier arriving shortly at destination")
                    "ARRIVED" -> DeliveryStatusDisplay("arrived", "Courier Arrived", "Arrived", StatusTone.TEAL, isLive = true, description = "Rider within delivery proximity")
                    "HANDOVER_VERIFIED" -> DeliveryStatusDisplay("handover_verified", "Handover Verified", "Handover Verified", StatusTone.TEAL, isLive = true, description = "PIN verified, capturing proof of delivery")
                    "DELIVERED", "COMPLETED" -> DeliveryStatusDisplay("delivered", "Delivered", "Delivered", StatusTone.GREEN, description = "Delivery completed and signed")
                    "CANCELLED", "CANCELED" -> DeliveryStatusDisplay("cancelled", "Cancelled", "Cancelled", StatusTone.RED, description = "Delivery cancelled")
                    "ISSUE", "NEEDS_ATTENTION" -> DeliveryStatusDisplay("issue", "Needs attention", "Issue", StatusTone.RED, description = "Operational exception")
                    else -> DeliveryStatusDisplay(statusStr.lowercase(), statusStr.replace('_', ' '), statusStr.replace('_', ' '), StatusTone.NEUTRAL)
                }
            }
        }
    }
}
