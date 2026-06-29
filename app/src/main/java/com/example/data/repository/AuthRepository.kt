package com.example.data.repository

import com.example.data.FirebaseService
import com.example.data.api.NetworkResult
import com.example.data.models.*
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth get() = FirebaseService.auth

    suspend fun login(email: String, password: String): NetworkResult<AuthResponse> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return NetworkResult.Error("Login failed")
            val token = firebaseUser.getIdToken(false).await().token ?: ""
            val user = User(
                id = firebaseUser.uid.hashCode().toLong(),
                fullName = firebaseUser.displayName ?: email.substringBefore("@"),
                email = firebaseUser.email ?: email,
                phone = firebaseUser.phoneNumber ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                isVerified = firebaseUser.isEmailVerified,
            )
            NetworkResult.Success(AuthResponse(token, user, "Login successful"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            NetworkResult.Error("Invalid email or password", 401)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Login failed")
        }
    }

    suspend fun register(name: String, email: String, phone: String, password: String): NetworkResult<AuthResponse> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return NetworkResult.Error("Registration failed")
            val token = firebaseUser.getIdToken(false).await().token ?: ""
            val user = User(
                id = firebaseUser.uid.hashCode().toLong(),
                fullName = name,
                email = firebaseUser.email ?: email,
                phone = phone,
                photoUrl = "",
                isVerified = false,
            )
            // Save user profile to Firestore
            FirebaseService.db.collection("customers").document(firebaseUser.uid).set(
                mapOf(
                    "fullName" to name,
                    "email" to email,
                    "phone" to phone,
                    "photoUrl" to "",
                    "isVerified" to true,
                    "rating" to 5.0,
                    "totalDeliveries" to 0,
                    "createdAt" to java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date()),
                )
            )
            NetworkResult.Success(AuthResponse(token, user, "Registration successful"))
        } catch (e: FirebaseAuthUserCollisionException) {
            NetworkResult.Error("Email already registered", 409)
        } catch (e: FirebaseAuthWeakPasswordException) {
            NetworkResult.Error("Password must be at least 6 characters", 400)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Registration failed")
        }
    }

    suspend fun logout() {
        auth.signOut()
    }
}
