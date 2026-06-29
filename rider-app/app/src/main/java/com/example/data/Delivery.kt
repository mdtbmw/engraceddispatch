package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deliveries")
data class Delivery(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackingNumber: String,
    val deliveryType: String, // Express, Economy, Batch, Multi-Pickup
    val status: String, // PENDING, ASSIGNED, PICKED_UP, DELIVERED, CANCELLED
    val totalAmount: Double,
    val scheduledAt: String,
    val createdAt: Long = System.currentTimeMillis(),
    val pickupAddress: String,
    val deliveryAddress: String,
    val itemName: String,
    val itemWeight: Double,
    val otpCode: String,
    val otpVerified: Boolean = false,
    val photoProofUri: String? = null,
    val riderName: String = "Sani Ibrahim",
    val riderBikeNumber: String = "LAG-5832-BK",
    val riderRating: Float = 4.8f,
    val etaMinutes: Int = 15,
    val pickupLatitude: Double? = null,
    val pickupLongitude: Double? = null,
    val deliveryLatitude: Double? = null,
    val deliveryLongitude: Double? = null
)
