package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseService {
    val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser

    // Backend API base URL (Express serverless on Vercel)
    // For production: "https://api.engraceddispatch.com"
    // For emulator dev: "http://10.0.2.2:3000"
    const val BACKEND_BASE_URL = "https://api.engraceddispatch.com"

    const val PAYSTACK_CALLBACK_SCHEME = "engraceddispatch"

    suspend fun getCurrentUserIdToken(): String? {
        return try {
            currentUser?.getIdToken(false)?.await()?.token
        } catch (e: Exception) { null }
    }
}
