package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Delivery
import com.example.data.DeliveryRepository
import com.example.data.FirebaseService
import com.example.data.api.NetworkResult
import com.example.data.models.*
import com.example.data.preferences.UserPreferences
import com.example.data.repository.AuthRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.ui.navigation.AppView
import java.text.SimpleDateFormat
import java.util.*

class DeliveryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DeliveryRepository
    private val authRepository: AuthRepository
    val preferences = UserPreferences(application)

    val allDeliveries: StateFlow<List<Delivery>>
    private val _currentView = MutableStateFlow<AppView>(AppView.Splash)
    val currentView = _currentView.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _currentUser = MutableStateFlow(User())
    val currentUser = _currentUser.asStateFlow()

    private val _currentTrackingDelivery = MutableStateFlow<Delivery?>(null)
    val currentTrackingDelivery = _currentTrackingDelivery.asStateFlow()

    private val _navigationHistory = mutableListOf<AppView>()
    private var _initialized = false
    private val uid get() = FirebaseService.currentUser?.uid ?: ""

    // Rider-specific state
    private val _isOnline = MutableStateFlow(false)
    val isOnline = _isOnline.asStateFlow()
    private val _otpVerificationEnabled = MutableStateFlow(true)
    val otpVerificationEnabled = _otpVerificationEnabled.asStateFlow()
    private val _notifications = MutableStateFlow<List<RiderNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    data class RiderNotification(
        val id: Long = 0, val title: String = "", val message: String = "",
        val type: String = "info", val isRead: Boolean = false,
        val createdAt: Long = System.currentTimeMillis(),
        val trackingNumber: String? = null
    )

    init {
        val database = AppDatabase.getDatabase(application)
        repository = DeliveryRepository(database.deliveryDao())
        authRepository = AuthRepository()

        allDeliveries = repository.allDeliveries.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        initFromPreferences()
    }

    private fun initFromPreferences() {
        if (_initialized) return
        _initialized = true

        viewModelScope.launch {
            val email = preferences.userEmail.first()
            val name = preferences.userName.first()
            val phone = preferences.userPhone.first()
            val photo = preferences.userPhoto.first()
            val rating = preferences.userRating.first()
            val deliveries = preferences.userTotalDeliveries.first()
            val earned = preferences.userTotalEarned.first()
            val memberSince = preferences.userMemberSince.first()
            val isOnline = preferences.riderIsOnline.first()

            _isOnline.value = isOnline

            if (name.isNotEmpty() || email.isNotEmpty()) {
                _currentUser.value = User(
                    fullName = name,
                    email = email,
                    phone = phone,
                    photoUrl = photo.ifEmpty { _currentUser.value.photoUrl },
                    rating = rating,
                    totalDeliveries = deliveries,
                    totalEarned = earned,
                    memberSince = memberSince
                )
            }
        }

        FirebaseService.auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null && uid.isNotEmpty()) {
                viewModelScope.launch {
                    loadRiderFromFirestore()
                    repository.syncDeliveriesFromFirestore(uid)
                    loadNotificationsFromFirestore()
                    loadOtpSettingsFromFirestore()
                }
            }
        }

        viewModelScope.launch { loadOtpSettingsFromFirestore() }

        checkAuthState()
    }

    private suspend fun loadRiderFromFirestore() {
        if (uid.isEmpty()) return
        try {
            val doc = FirebaseService.db.collection("riders").document(uid).get().await()
            if (doc.exists()) {
                val name = doc.getString("fullName") ?: ""
                val email = doc.getString("email") ?: ""
                val phone = doc.getString("phone") ?: ""
                val photo = doc.getString("photoUrl") ?: ""
                val rating = doc.getDouble("rating")?.toFloat() ?: 5.0f
                val totalDeliveries = doc.getLong("totalDeliveries")?.toInt() ?: 0
                val memberSince = doc.getString("joinedAt") ?: ""
                val isOnline = doc.getBoolean("isOnline") ?: false
                val bikeNumber = doc.getString("bikeNumber") ?: ""
                val bikeModel = doc.getString("bikeModel") ?: ""
                val zone = doc.getString("zone") ?: "Lagos Mainland"

                _currentUser.value = _currentUser.value.copy(
                    fullName = name, email = email, phone = phone,
                    photoUrl = photo, rating = rating,
                    totalDeliveries = totalDeliveries, memberSince = memberSince
                )
                _isOnline.value = isOnline
                preferences.saveUser(name, email, phone, photo)
                preferences.saveUserStats(rating, totalDeliveries, 0.0, memberSince)
                preferences.saveRiderProfile(bikeNumber, bikeModel, zone)
                preferences.setRiderOnline(isOnline)
            }
        } catch (_: Exception) { }
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            preferences.isLoggedIn.collect { loggedIn ->
                if (loggedIn) {
                    _currentView.value = AppView.Dashboard
                }
            }
        }
    }

    fun navigateTo(view: AppView) {
        _navigationHistory.add(_currentView.value)
        _currentView.value = view
    }

    fun navigateBack() {
        if (_navigationHistory.isNotEmpty()) {
            _currentView.value = _navigationHistory.removeLast()
        } else {
            _currentView.value = AppView.Dashboard
        }
    }

    fun navigateToRoot(view: AppView) {
        _navigationHistory.clear()
        _currentView.value = view
    }

    // ===== AUTH =====
    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = authRepository.login(email, password)
            when (result) {
                is NetworkResult.Success -> {
                    val data = result.data
                    preferences.saveAuthToken(data.token)
                    preferences.setLoggedIn(true)
                    preferences.saveUser(data.user.fullName, data.user.email, data.user.phone, data.user.photoUrl)
                    preferences.saveUserStats(data.user.rating, data.user.totalDeliveries, data.user.totalEarned, data.user.memberSince)
                    _currentUser.value = data.user

                    if (uid.isNotEmpty()) {
                        loadRiderFromFirestore()
                        repository.syncDeliveriesFromFirestore(uid)
                    }
                    _navigationHistory.clear()
                    onSuccess()
                }
                is NetworkResult.Error -> {
                    _error.value = result.message
                }
                is NetworkResult.Loading -> {}
            }
            _isLoading.value = false
        }
    }

    fun loginWithGoogle(idToken: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                val result = FirebaseService.auth.signInWithCredential(credential).await()
                val firebaseUser = result.user ?: run {
                    _error.value = "Google Sign-In failed"
                    _isLoading.value = false
                    return@launch
                }
                val token = firebaseUser.getIdToken(false).await().token ?: ""
                val user = User(
                    id = firebaseUser.uid.hashCode().toLong(),
                    fullName = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "Rider",
                    email = firebaseUser.email ?: "",
                    phone = firebaseUser.phoneNumber ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                    isVerified = firebaseUser.isEmailVerified,
                )
                preferences.saveAuthToken(token)
                preferences.setLoggedIn(true)
                preferences.saveUser(user.fullName, user.email, user.phone, user.photoUrl)
                preferences.saveUserStats(user.rating, user.totalDeliveries, user.totalEarned, user.memberSince)
                preferences.setRider(true)
                _currentUser.value = user

                if (uid.isNotEmpty()) {
                    val doc = FirebaseService.db.collection("riders").document(uid).get().await()
                    if (!doc.exists()) {
                        FirebaseService.db.collection("riders").document(uid).set(mapOf(
                            "fullName" to user.fullName, "email" to user.email, "phone" to user.phone,
                            "photoUrl" to user.photoUrl, "isVerified" to true, "rating" to 5.0,
                            "totalDeliveries" to 0, "walletBalance" to 0.0,
                            "status" to "active", "isOnline" to true,
                            "bikeNumber" to "", "bikeModel" to "", "zone" to "Lagos Mainland",
                            "joinedAt" to java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                        )).await()
                        token?.let {
                            try {
                                val url = java.net.URL("${FirebaseService.BACKEND_BASE_URL}/api/auth/set-role")
                                val conn = url.openConnection() as java.net.HttpURLConnection
                                conn.requestMethod = "POST"
                                conn.setRequestProperty("Content-Type", "application/json")
                                conn.setRequestProperty("Authorization", "Bearer $it")
                                conn.doOutput = true
                                conn.outputStream.write("{\"role\":\"rider\"}".toByteArray())
                                conn.outputStream.close()
                                conn.responseCode
                                conn.disconnect()
                            } catch (_: Exception) {}
                        }
                    } else {
                        loadRiderFromFirestore()
                    }
                    repository.syncDeliveriesFromFirestore(uid)
                }
                _navigationHistory.clear()
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Google Sign-In failed"
            }
            _isLoading.value = false
        }
    }

    fun register(name: String, email: String, phone: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = authRepository.register(name, email, phone, password)
            when (result) {
                is NetworkResult.Success -> {
                    val data = result.data
                    preferences.saveAuthToken(data.token)
                    preferences.setLoggedIn(true)
                    preferences.saveUser(name, email, phone, data.user.photoUrl)
                    preferences.saveUserStats(data.user.rating, data.user.totalDeliveries, data.user.totalEarned, data.user.memberSince)
                    preferences.setRider(true)
                    _currentUser.value = data.user
                    _navigationHistory.clear()
                    onSuccess()
                }
                is NetworkResult.Error -> {
                    _error.value = result.message
                }
                is NetworkResult.Loading -> {}
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            preferences.clearAll()
            _navigationHistory.clear()
            _currentView.value = AppView.Splash
        }
    }

    // ===== PROFILE =====
    fun updateProfile(name: String, email: String, phone: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            preferences.saveUser(name, email, phone, _currentUser.value.photoUrl)
            _currentUser.value = _currentUser.value.copy(fullName = name, email = email, phone = phone)

            if (uid.isNotEmpty()) {
                try {
                    FirebaseService.db.collection("riders").document(uid)
                        .update(mapOf("fullName" to name, "email" to email, "phone" to phone))
                        .await()
                } catch (e: Exception) {
                    _error.value = "Network error: Profile updated locally"
                }
            }
            _isLoading.value = false
        }
    }

    fun toggleOnlineStatus() {
        viewModelScope.launch {
            _isOnline.value = !_isOnline.value
            preferences.setRiderOnline(_isOnline.value)
            if (uid.isNotEmpty()) {
                try {
                    FirebaseService.db.collection("riders").document(uid)
                        .update("isOnline", _isOnline.value)
                        .await()
                } catch (_: Exception) { }
            }
        }
    }

    // ===== DELIVERIES =====
    fun loadDeliveryByTracking(trackingNumber: String) {
        viewModelScope.launch {
            val delivery = repository.getDeliveryByTracking(trackingNumber)
            if (delivery != null) {
                _currentTrackingDelivery.value = delivery
            }
        }
    }

    fun setTrackingDelivery(delivery: Delivery) {
        _currentTrackingDelivery.value = delivery
    }

    fun updateOtpInput(otp: String) {
        _currentTrackingDelivery.value?.let {
            _currentTrackingDelivery.value = it.copy(otpCode = otp)
        }
    }

    fun verifyOtp(): Boolean {
        val current = _currentTrackingDelivery.value ?: return false

        if (!_otpVerificationEnabled.value) {
            viewModelScope.launch {
                val updated = current.copy(otpVerified = true, status = "DELIVERED")
                repository.update(updated)
                _currentTrackingDelivery.value = updated
                if (uid.isNotEmpty()) {
                    try {
                        val snap = FirebaseService.db.collection("deliveries")
                            .whereEqualTo("trackingNumber", current.trackingNumber).limit(1).get().await()
                        snap.documents.firstOrNull()?.let { doc ->
                            doc.reference.update(mapOf("otpVerified" to true, "status" to "DELIVERED")).await()
                        }
                    } catch (_: Exception) { }
                }
                val deliveriesCount = _currentUser.value.totalDeliveries + 1
                preferences.saveUserStats(_currentUser.value.rating, deliveriesCount, _currentUser.value.totalEarned, _currentUser.value.memberSince)
                _currentUser.value = _currentUser.value.copy(totalDeliveries = deliveriesCount)
                if (uid.isNotEmpty()) {
                    try { FirebaseService.db.collection("riders").document(uid).update("totalDeliveries", FieldValue.increment(1)).await() } catch (_: Exception) { }
                }
            }
            return true
        }

        val inputOtp = current.otpCode
        if (inputOtp.length != 4) return false

        val storedDelivery = allDeliveries.value.find { it.id == current.id }
        val actualOtp = storedDelivery?.otpCode ?: current.otpCode

        var verified = false
        if (inputOtp == actualOtp) {
            viewModelScope.launch {
                val updated = current.copy(otpVerified = true, status = "DELIVERED")
                repository.update(updated)
                _currentTrackingDelivery.value = updated

                if (uid.isNotEmpty()) {
                    try {
                        val snap = FirebaseService.db.collection("deliveries")
                            .whereEqualTo("trackingNumber", current.trackingNumber).limit(1).get().await()
                        snap.documents.firstOrNull()?.let { doc ->
                            doc.reference.update(mapOf("otpVerified" to true, "status" to "DELIVERED")).await()
                        }
                    } catch (_: Exception) { }
                }

                val deliveriesCount = _currentUser.value.totalDeliveries + 1
                preferences.saveUserStats(_currentUser.value.rating, deliveriesCount, _currentUser.value.totalEarned, _currentUser.value.memberSince)
                _currentUser.value = _currentUser.value.copy(totalDeliveries = deliveriesCount)

                if (uid.isNotEmpty()) {
                    try {
                        FirebaseService.db.collection("riders").document(uid)
                            .update("totalDeliveries", FieldValue.increment(1))
                            .await()
                    } catch (_: Exception) { }
                }
            }
            verified = true
        }
        return verified
    }

    fun advanceDeliveryStatus() {
        val current = _currentTrackingDelivery.value ?: return
        val nextStatus = when (current.status) {
            "PENDING" -> "ASSIGNED"
            "ASSIGNED" -> "PICKED_UP"
            "PICKED_UP" -> "OUT_FOR_DELIVERY"
            "OUT_FOR_DELIVERY" -> "DELIVERED"
            else -> current.status
        }
        if (nextStatus == current.status) return
        val etaReduction = (3..8).random()
        val newEta = (current.etaMinutes - etaReduction).coerceAtLeast(2)
        val updated = current.copy(status = nextStatus, etaMinutes = newEta)

        viewModelScope.launch {
            repository.update(updated)
            _currentTrackingDelivery.value = updated

            if (uid.isNotEmpty()) {
                try {
                    val snap = FirebaseService.db.collection("deliveries")
                        .whereEqualTo("trackingNumber", current.trackingNumber).limit(1).get().await()
                    snap.documents.firstOrNull()?.let { doc ->
                        doc.reference.update(mapOf("status" to nextStatus, "etaMinutes" to newEta)).await()
                    }
                } catch (_: Exception) { }
            }
        }
    }

    fun assignRiderToDelivery(trackingNumber: String, riderName: String) {
        viewModelScope.launch {
            val stored = allDeliveries.value.find { it.trackingNumber == trackingNumber } ?: return@launch
            val updated = stored.copy(
                status = "ASSIGNED",
                riderName = riderName,
                riderBikeNumber = "LAG-5832-BK",
                etaMinutes = 20
            )
            repository.update(updated)
            if (uid.isNotEmpty()) {
                try {
                    val snap = FirebaseService.db.collection("deliveries")
                        .whereEqualTo("trackingNumber", trackingNumber).limit(1).get().await()
                    snap.documents.firstOrNull()?.let { doc ->
                        doc.reference.update(mapOf(
                            "status" to "ASSIGNED",
                            "riderName" to riderName,
                            "riderId" to uid
                        )).await()
                    }
                } catch (_: Exception) { }
            }
            refreshAllData()
        }
    }

    fun updateDeliveryStatus(trackingNumber: String, status: String) {
        viewModelScope.launch {
            val stored = allDeliveries.value.find { it.trackingNumber == trackingNumber } ?: return@launch
            val newEta = when (status) {
                "PICKED_UP" -> 15
                "OUT_FOR_DELIVERY" -> 8
                else -> stored.etaMinutes
            }
            val updated = stored.copy(status = status, etaMinutes = newEta)
            repository.update(updated)

            if (uid.isNotEmpty()) {
                try {
                    val snap = FirebaseService.db.collection("deliveries")
                        .whereEqualTo("trackingNumber", trackingNumber).limit(1).get().await()
                    snap.documents.firstOrNull()?.let { doc ->
                        doc.reference.update(mapOf("status" to status, "etaMinutes" to newEta)).await()
                    }
                } catch (_: Exception) { }
            }
            refreshAllData()
        }
    }

    fun verifyDeliveryOtp(trackingNumber: String, otp: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val stored = allDeliveries.value.find { it.trackingNumber == trackingNumber } ?: run { onComplete(false); return@launch }

            if (!_otpVerificationEnabled.value) {
                val updated = stored.copy(otpVerified = true, status = "DELIVERED")
                repository.update(updated)
                if (uid.isNotEmpty()) {
                    try {
                        val snap = FirebaseService.db.collection("deliveries")
                            .whereEqualTo("trackingNumber", trackingNumber).limit(1).get().await()
                        snap.documents.firstOrNull()?.let { doc ->
                            doc.reference.update(mapOf("otpVerified" to true, "status" to "DELIVERED")).await()
                        }
                    } catch (_: Exception) { }
                }
                val deliveriesCount = _currentUser.value.totalDeliveries + 1
                preferences.saveUserStats(_currentUser.value.rating, deliveriesCount, _currentUser.value.totalEarned, _currentUser.value.memberSince)
                _currentUser.value = _currentUser.value.copy(totalDeliveries = deliveriesCount)
                if (uid.isNotEmpty()) {
                    try { FirebaseService.db.collection("riders").document(uid).update("totalDeliveries", FieldValue.increment(1)).await() } catch (_: Exception) { }
                }
                refreshAllData()
                onComplete(true)
                return@launch
            }

            val updated = stored.copy(otpVerified = true, status = "DELIVERED")
            repository.update(updated)

            if (uid.isNotEmpty()) {
                try {
                    val snap = FirebaseService.db.collection("deliveries")
                        .whereEqualTo("trackingNumber", trackingNumber).limit(1).get().await()
                    snap.documents.firstOrNull()?.let { doc ->
                        doc.reference.update(mapOf("otpVerified" to true, "status" to "DELIVERED")).await()
                    }
                } catch (_: Exception) { }
            }

            val deliveriesCount = _currentUser.value.totalDeliveries + 1
            preferences.saveUserStats(_currentUser.value.rating, deliveriesCount, _currentUser.value.totalEarned, _currentUser.value.memberSince)
            _currentUser.value = _currentUser.value.copy(totalDeliveries = deliveriesCount)

            if (uid.isNotEmpty()) {
                try {
                    FirebaseService.db.collection("riders").document(uid)
                        .update("totalDeliveries", FieldValue.increment(1))
                        .await()
                } catch (_: Exception) { }
            }

            refreshAllData()
            onComplete(true)
        }
    }

    // ===== SETTINGS =====
    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage = _selectedLanguage.asStateFlow()

    fun updateLanguage(lang: String) {
        _selectedLanguage.value = lang
        savePreferencesToFirestore()
    }

    fun updateNotificationPrefs(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setNotificationsEnabled(enabled)
            savePreferencesToFirestore()
        }
    }

    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setDarkMode(enabled)
            savePreferencesToFirestore()
        }
    }

    fun updateBiometric(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setBiometricEnabled(enabled)
            savePreferencesToFirestore()
        }
    }

    fun changePassword(current: String, newPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val user = FirebaseService.currentUser
            if (user != null) {
                try {
                    val credential = EmailAuthProvider.getCredential(user.email ?: "", current)
                    user.reauthenticate(credential).await()
                    user.updatePassword(newPass).await()
                    onSuccess()
                } catch (e: Exception) {
                    onError(e.message ?: "Password change failed")
                }
            } else {
                onError("User is not authenticated")
            }
            _isLoading.value = false
        }
    }

    private fun savePreferencesToFirestore() {
        viewModelScope.launch {
            if (uid.isEmpty()) return@launch
            try {
                val notif = preferences.notificationsEnabled.first()
                val bio = preferences.biometricEnabled.first()
                val dark = preferences.darkMode.first()
                FirebaseService.db.collection("riders").document(uid)
                    .update(mapOf(
                        "notificationsEnabled" to notif,
                        "biometricEnabled" to bio,
                        "darkMode" to dark,
                        "language" to _selectedLanguage.value
                    )).await()
            } catch (_: Exception) { }
        }
    }

    fun refreshAllData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (uid.isNotEmpty()) {
                loadRiderFromFirestore()
                repository.syncDeliveriesFromFirestore(uid)
                loadNotificationsFromFirestore()
            }
            onComplete()
        }
    }

    // ===== SEARCH =====
    fun searchDeliveries(query: String): List<Delivery> {
        if (query.isBlank()) return allDeliveries.value
        return allDeliveries.value.filter {
            it.trackingNumber.contains(query, ignoreCase = true) ||
            it.itemName.contains(query, ignoreCase = true) ||
            it.deliveryType.contains(query, ignoreCase = true) ||
            it.pickupAddress.contains(query, ignoreCase = true) ||
            it.deliveryAddress.contains(query, ignoreCase = true)
        }
    }

    // ===== ERROR HANDLING =====
    fun clearError() {
        _error.value = null
    }

    fun deleteDelivery(delivery: Delivery) {
        viewModelScope.launch {
            repository.delete(delivery)
        }
    }

    // ===== NOTIFICATIONS =====
    fun loadNotifications() {
        viewModelScope.launch { loadNotificationsFromFirestore() }
    }

    private suspend fun loadNotificationsFromFirestore() {
        if (uid.isEmpty()) {
            _notifications.value = listOf(
                RiderNotification(1, "New Assignment", "Delivery ESD-EXP-8241 assigned to you", "tracking", false, System.currentTimeMillis() - 1800000, "ESD-EXP-8241"),
                RiderNotification(2, "Payment Received", "Delivery fee credited", "wallet", false, System.currentTimeMillis() - 7200000)
            )
            return
        }
        try {
            val snapshot = FirebaseService.db.collection("notifications")
                .whereEqualTo("riderId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()
            _notifications.value = snapshot.documents.mapNotNull { doc ->
                val d = doc.data ?: return@mapNotNull null
                RiderNotification(
                    id = doc.id.hashCode().toLong(),
                    title = d["title"] as? String ?: "",
                    message = d["message"] as? String ?: "",
                    type = d["type"] as? String ?: "info",
                    isRead = d["isRead"] as? Boolean ?: false,
                    createdAt = (d["createdAt"] as? Long) ?: System.currentTimeMillis(),
                    trackingNumber = d["trackingNumber"] as? String
                )
            }
        } catch (_: Exception) {
            if (_notifications.value.isEmpty()) {
                _notifications.value = listOf(
                    RiderNotification(1, "New Assignment", "Delivery ESD-EXP-8241 assigned to you", "tracking", false, System.currentTimeMillis() - 1800000, "ESD-EXP-8241"),
                    RiderNotification(2, "Payment Received", "Delivery fee credited", "wallet", false, System.currentTimeMillis() - 7200000)
                )
            }
        }
    }

    private suspend fun loadOtpSettingsFromFirestore() {
        try {
            val doc = FirebaseService.db.collection("settings").document("delivery").get().await()
            if (doc.exists()) {
                _otpVerificationEnabled.value = doc.getBoolean("otpVerificationEnabled") ?: true
            }
        } catch (_: Exception) { }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            _notifications.value = _notifications.value.map { if (it.id == id) it.copy(isRead = true) else it }
            if (uid.isNotEmpty()) {
                try {
                    val snapshot = FirebaseService.db.collection("notifications")
                        .whereEqualTo("riderId", uid).limit(20).get().await()
                    val docs = snapshot.documents.filter { it.id.hashCode().toLong() == id }
                    docs.firstOrNull()?.reference?.update("isRead", true)?.await()
                } catch (_: Exception) { }
            }
        }
    }
}

class DeliveryViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeliveryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeliveryViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
