package com.example.data.models

data class Address(
    val id: Long = 0,
    val label: String = "",
    val address: String = "",
    val isDefault: Boolean = false
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class UserPreferencesData(
    val notificationsEnabled: Boolean = true,
    val biometricEnabled: Boolean = false,
    val language: String = "English"
)

data class PaymentMethod(
    val id: Long = 0,
    val type: String = "",
    val last4: String = "",
    val expiry: String? = null,
    val isDefault: Boolean = false
)

data class StatusUpdateRequest(val status: String)
data class OtpVerifyRequest(val otp: String)
