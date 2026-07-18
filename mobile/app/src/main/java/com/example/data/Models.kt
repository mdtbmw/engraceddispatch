package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class ParcelStatus {
    PENDING, ASSIGNED, TRANSIT, DELIVERED, CANCELLED, OUT_FOR_DELIVERY
}

@Entity(tableName = "parcels")
data class Parcel(
    @PrimaryKey val id: String,
    val itemName: String,
    val imageUrl: String,
    val status: ParcelStatus,
    val pickupAddress: String,
    val deliveryAddress: String,
    val senderName: String,
    val senderPhone: String,
    val receiverName: String,
    val receiverPhone: String,
    val quantity: Int = 1,
    val weight: Double = 1.0,
    val length: Int = 10,
    val width: Int = 10,
    val height: Int = 10,
    val price: Double = 0.0,
    val courierName: String = "Richard Dheo",
    val courierPhone: String = "+971 50 123",
    val courierAvatar: String = "https://images.unsplash.com/photo-1599566150163-29194dcaad36?w=100&h=100&fit=crop",
    val progress: Float = 0.35f, // 0.0 to 1.0 for timeline progress
    val dateString: String = "Today",
    val userId: String = "",
    val courierLatitude: Double? = null,
    val courierLongitude: Double? = null,
    val additionalStops: String = "",
    val riderId: String = "",
    val riderBikeNumber: String = "",
    val otpCode: String = "",
    val otpVerified: Boolean = false,
    val isRated: Boolean = false,
    val customerRating: Double = 0.0,
    val tipAmount: Double = 0.0
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String,
    val title: String,
    val date: String,
    val amount: Double,
    val isTopUp: Boolean
)

@Entity(tableName = "address_items")
data class AddressItem(
    @PrimaryKey val id: String,
    val label: String, // Home, Office
    val address: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val time: String,
    val isRead: Boolean = false
)

data class PromoCode(
    val id: String = UUID.randomUUID().toString(),
    val discountPercent: Int,
    val description: String,
    val code: String,
    val isLimited: Boolean = true
)
data class BannerCard(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val interval: Int = 5,
    val order: Int = 0,
    val active: Boolean = true
)

data class ParcelDraft(
    val id: String = UUID.randomUUID().toString().substring(0, 8).uppercase(),
    val itemName: String = "",
    val category: String = "Documents",
    val weight: Double = 1.0,
    val quantity: Int = 1,
    val senderName: String = "",
    val senderPhone: String = "",
    val receiverName: String = "",
    val receiverPhone: String = "",
    val pickupAddress: String = "",
    val deliveryAddress: String = "",
    val deliveryInstructions: String = "",
    val insuranceSelected: Boolean = false,
    val price: Double = 0.0,
    val additionalStops: String = ""
)

data class ParcelChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderRole: String = "", // "customer" or "rider"
    val messageText: String = "",
    val timestamp: Long = 0L
)

@Entity(tableName = "shift_attendance")
data class ShiftAttendance(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val riderId: String,
    val status: String, // "ON_DUTY", "ON_BREAK", "OFF_DUTY"
    val clockInTime: String,
    val clockOutTime: String = "",
    val totalHours: Double = 0.0,
    val dateString: String
)

@Entity(tableName = "vehicle_inspections")
data class VehicleInspection(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val riderId: String,
    val dateString: String,
    val tiresOk: Boolean,
    val brakesOk: Boolean,
    val headlightsOk: Boolean,
    val hornOk: Boolean,
    val fuelBatteryLevelOk: Boolean,
    val safetyVestHelmetOk: Boolean,
    val notes: String,
    val passed: Boolean
)

@Entity(tableName = "expense_claims")
data class ExpenseClaim(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val riderId: String,
    val title: String,
    val category: String, // "FUEL", "CHARGING", "TOLLS", "MAINTENANCE"
    val amount: Double,
    val receiptNote: String,
    val status: String = "PENDING", // "PENDING", "APPROVED"
    val dateString: String
)

@Entity(tableName = "shift_rosters")
data class ShiftRoster(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val riderId: String,
    val shiftDate: String,
    val startTime: String,
    val endTime: String,
    val roleOrArea: String,
    val isLeave: Boolean = false,
    val leaveReason: String = "",
    val leaveStatus: String = "NONE" // "NONE", "PENDING", "APPROVED"
)

@Entity(tableName = "offline_sync_queue")
data class OfflineSyncQueue(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val actionType: String, // "UPDATE_STATUS", "GPS_LOG", "EXPENSE", "INSPECTION"
    val payloadJson: String,
    val timestamp: Long,
    val synced: Boolean = false
)

data class BatchRoutePlan(
    val id: String = UUID.randomUUID().toString().substring(0, 8).uppercase(),
    val batchName: String,
    val stopCount: Int,
    val optimizedPathSummary: String,
    val estimatedDistanceKm: Double,
    val estimatedEtaMinutes: Int,
    val aiConfidence: Int, // e.g. 96%
    val status: String = "OPTIMIZED"
)

data class GeofenceAlert(
    val id: String = "GEO-" + UUID.randomUUID().toString().substring(0, 6).uppercase(),
    val riderId: String,
    val riderName: String,
    val breachType: String, // "ZONE_EXIT", "SPEED_LIMIT_EXCEEDED", "UNAUTHORIZED_STOP"
    val locationName: String,
    val timestamp: String,
    val severity: String // "MEDIUM", "HIGH", "CRITICAL"
)

data class DriverBonusCalculation(
    val riderId: String,
    val totalDeliveries: Int,
    val onTimePercentage: Double,
    val averageRating: Double,
    val baseBonus: Double,
    val performanceMultiplier: Double,
    val projectedPayout: Double,
    val tierLabel: String // "PLATINUM", "GOLD", "SILVER"
)

data class VehicleMaintenanceSchedule(
    val id: String = "MNT-" + UUID.randomUUID().toString().substring(0, 6).uppercase(),
    val vehicleNumber: String,
    val lastServiceMileage: Int,
    val nextServiceMileageDue: Int,
    val serviceType: String, // "Oil Change", "Brake Pad Inspection", "Tire Rotation & Alignment"
    val status: String, // "DUE_SOON", "OVERDUE", "UP TO DATE"
    val technicianNote: String
)


