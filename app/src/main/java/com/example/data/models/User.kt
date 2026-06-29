package com.example.data.models

data class User(
    val id: Long = 0,
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val isVerified: Boolean = true,
    val rating: Float = 5.0f,
    val totalDeliveries: Int = 0,
    val totalEarned: Double = 0.0,
    val memberSince: String = ""
)

data class LoginRequest(val email: String, val password: String)
data class SignUpRequest(val fullName: String, val email: String, val phone: String, val password: String)
data class AuthResponse(val token: String, val user: User, val message: String = "Success")
