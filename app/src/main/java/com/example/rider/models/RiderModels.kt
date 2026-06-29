package com.example.rider.models

data class RiderProfile(
    val id: Long = 0,
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val bikeNumber: String = "",
    val bikeModel: String = "",
    val rating: Float = 5.0f,
    val totalDeliveries: Int = 0,
    val memberSince: String = "",
    val isOnline: Boolean = false,
    val currentZone: String = "Lagos Mainland"
)

data class RiderDelivery(
    val id: Long = 0,
    val trackingNumber: String = "",
    val status: String = "ASSIGNED",
    val pickupAddress: String = "",
    val deliveryAddress: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val itemName: String = "",
    val itemWeight: Double = 0.0,
    val otpCode: String = "",
    val otpVerified: Boolean = false,
    val photoProofUri: String? = null,
    val scheduledAt: String = "",
    val distance: String = "2.3 km",
    val etaMinutes: Int = 15,
    val deliveryType: String = "Express",
    val totalAmount: Double = 0.0,
    val notes: String = ""
)

data class RiderStats(
    val todayDeliveries: Int = 0,
    val todayCompleted: Int = 0,
    val totalDeliveries: Int = 0,
    val rating: Float = 5.0f,
    val onTimeRate: Int = 98
)

data class RiderLoginRequest(val email: String, val password: String)
data class RiderAuthResponse(val token: String, val rider: RiderProfile, val message: String = "Success")
data class StatusUpdateBody(val status: String)
data class OtpVerifyBody(val otp: String)
data class OnlineStatusBody(val isOnline: Boolean)
