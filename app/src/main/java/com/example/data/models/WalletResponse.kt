package com.example.data.models

data class WalletResponse(
    val balance: Double = 0.0,
    val currency: String = "NGN",
    val userId: Long = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
