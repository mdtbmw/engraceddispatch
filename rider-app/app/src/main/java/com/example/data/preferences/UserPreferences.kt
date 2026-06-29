package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {
    companion object {
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_PHONE = stringPreferencesKey("user_phone")
        private val USER_PHOTO = stringPreferencesKey("user_photo")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
        private val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        private val WALLET_BALANCE = doublePreferencesKey("wallet_balance")
        private val WALLET_CARD_NUMBER = stringPreferencesKey("wallet_card_number")
        private val WALLET_CARD_EXPIRY = stringPreferencesKey("wallet_card_expiry")
        private val USER_RATING = floatPreferencesKey("user_rating")
        private val USER_TOTAL_DELIVERIES = intPreferencesKey("user_total_deliveries")
        private val USER_TOTAL_EARNED = doublePreferencesKey("user_total_earned")
        private val USER_MEMBER_SINCE = stringPreferencesKey("user_member_since")
    }

    val authToken: Flow<String?> = context.dataStore.data.map { it[AUTH_TOKEN] }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[IS_LOGGED_IN] ?: false }
    val userName: Flow<String> = context.dataStore.data.map { it[USER_NAME] ?: "" }
    val userEmail: Flow<String> = context.dataStore.data.map { it[USER_EMAIL] ?: "" }
    val userPhone: Flow<String> = context.dataStore.data.map { it[USER_PHONE] ?: "" }
    val userPhoto: Flow<String> = context.dataStore.data.map { it[USER_PHOTO] ?: "" }
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: true }
    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[DARK_MODE] ?: false }
    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { it[BIOMETRIC_ENABLED] ?: false }
    val walletBalance: Flow<Double> = context.dataStore.data.map { it[WALLET_BALANCE] ?: 0.0 }
    val walletCardNumber: Flow<String> = context.dataStore.data.map { it[WALLET_CARD_NUMBER] ?: "8241" }
    val walletCardExpiry: Flow<String> = context.dataStore.data.map { it[WALLET_CARD_EXPIRY] ?: "12/29" }
    val userRating: Flow<Float> = context.dataStore.data.map { it[USER_RATING] ?: 4.9f }
    val userTotalDeliveries: Flow<Int> = context.dataStore.data.map { it[USER_TOTAL_DELIVERIES] ?: 0 }
    val userTotalEarned: Flow<Double> = context.dataStore.data.map { it[USER_TOTAL_EARNED] ?: 0.0 }
    val userMemberSince: Flow<String> = context.dataStore.data.map { it[USER_MEMBER_SINCE] ?: "" }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { it[AUTH_TOKEN] = token }
    }

    suspend fun saveUser(name: String, email: String, phone: String, photo: String) {
        context.dataStore.edit {
            it[USER_NAME] = name
            it[USER_EMAIL] = email
            it[USER_PHONE] = phone
            if (photo.isNotEmpty()) it[USER_PHOTO] = photo
        }
    }

    suspend fun saveUserStats(rating: Float, totalDeliveries: Int, totalEarned: Double, memberSince: String) {
        context.dataStore.edit {
            it[USER_RATING] = rating
            it[USER_TOTAL_DELIVERIES] = totalDeliveries
            it[USER_TOTAL_EARNED] = totalEarned
            if (memberSince.isNotEmpty()) it[USER_MEMBER_SINCE] = memberSince
        }
    }

    suspend fun setLoggedIn(value: Boolean) {
        context.dataStore.edit { it[IS_LOGGED_IN] = value }
    }

    suspend fun setNotificationsEnabled(value: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS_ENABLED] = value }
    }

    suspend fun setDarkMode(value: Boolean) {
        context.dataStore.edit { it[DARK_MODE] = value }
    }

    suspend fun setBiometricEnabled(value: Boolean) {
        context.dataStore.edit { it[BIOMETRIC_ENABLED] = value }
    }

    suspend fun saveWalletBalance(balance: Double) {
        context.dataStore.edit { it[WALLET_BALANCE] = balance }
    }

    suspend fun saveWalletCardInfo(cardNumber: String, expiry: String) {
        context.dataStore.edit {
            it[WALLET_CARD_NUMBER] = cardNumber
            it[WALLET_CARD_EXPIRY] = expiry
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
