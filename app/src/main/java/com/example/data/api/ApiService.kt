package com.example.data.api

data class FundRequest(val amount: Double)
data class RefreshTokenRequest(val token: String)
data class ReviewRequest(val rating: Int, val comment: String = "")
data class ReferralData(val code: String, val totalEarned: Double, val totalReferrals: Int)
data class ReferralEntry(val id: Long, val name: String, val date: String, val reward: Double)
data class PricingData(val basePrice: Double, val surgeAmount: Int, val currency: String = "NGN")

data class Promotion(
    val id: Long = 0,
    val title: String = "",
    val subtitle: String = "",
    val value: String = "",
    val icon: String = "Bolt",
    val expiresAt: String? = null,
    val terms: String = ""
)

data class NotificationItem(
    val id: Long = 0,
    val title: String = "",
    val message: String = "",
    val type: String = "info",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val action: String? = null,
    val trackingNumber: String? = null
)
