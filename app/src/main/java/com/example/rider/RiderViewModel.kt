package com.example.rider

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirebaseService
import com.example.data.preferences.UserPreferences
import com.example.rider.models.*
import com.example.rider.navigation.RiderView
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RiderViewModel(application: Application) : AndroidViewModel(application) {
    val preferences = UserPreferences(application)
    private val _currentView = MutableStateFlow<RiderView>(RiderView.Splash)
    val currentView = _currentView.asStateFlow()
    private val _navigationHistory = mutableListOf<RiderView>()
    private val uid get() = FirebaseService.currentUser?.uid ?: ""

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _riderProfile = MutableStateFlow(RiderProfile())
    val riderProfile = _riderProfile.asStateFlow()
    private val _riderDeliveries = MutableStateFlow<List<RiderDelivery>>(emptyList())
    val riderDeliveries = _riderDeliveries.asStateFlow()
    private val _currentDelivery = MutableStateFlow<RiderDelivery?>(null)
    val currentDelivery = _currentDelivery.asStateFlow()
    private val _riderStats = MutableStateFlow(RiderStats())
    val riderStats = _riderStats.asStateFlow()
    private val _isOnline = MutableStateFlow(false)
    val isOnline = _isOnline.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.riderIsOnline.collect { _isOnline.value = it }
        }
        checkRiderSession()
    }

    private fun checkRiderSession() {
        FirebaseService.auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null && uid.isNotEmpty()) {
                viewModelScope.launch {
                    loadRiderFromFirestore()
                    loadDeliveriesFromFirestore()
                }
            }
        }
    }

    private suspend fun loadRiderFromFirestore() {
        if (uid.isEmpty()) return
        try {
            val doc = FirebaseService.db.collection("riders").document(uid).get().await()
            if (doc.exists()) {
                val profile = RiderProfile(
                    fullName = doc.getString("fullName") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    photoUrl = doc.getString("photoUrl") ?: "",
                    bikeNumber = doc.getString("bikeNumber") ?: "",
                    bikeModel = doc.getString("bikeModel") ?: "",
                    rating = doc.getDouble("rating")?.toFloat() ?: 5.0f,
                    totalDeliveries = doc.getLong("totalDeliveries")?.toInt() ?: 0,
                    memberSince = doc.getString("joinedAt") ?: "",
                    isOnline = doc.getBoolean("isOnline") ?: false,
                    currentZone = doc.getString("zone") ?: "Lagos Mainland"
                )
                _riderProfile.value = profile
                _isOnline.value = profile.isOnline
                preferences.setRider(true)
                preferences.saveUser(profile.fullName, profile.email, profile.phone, profile.photoUrl)
                preferences.saveRiderProfile(profile.bikeNumber, profile.bikeModel, profile.currentZone)
                preferences.setRiderOnline(profile.isOnline)
            }
        } catch (_: Exception) { }
    }

    private suspend fun loadDeliveriesFromFirestore() {
        if (uid.isEmpty()) return
        try {
            val snapshot = FirebaseService.db.collection("deliveries")
                .whereEqualTo("riderId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            _riderDeliveries.value = snapshot.documents.mapNotNull { doc ->
                val d = doc.data ?: return@mapNotNull null
                RiderDelivery(
                    id = doc.id.hashCode().toLong(),
                    trackingNumber = d["trackingNumber"] as? String ?: "",
                    status = d["status"] as? String ?: "ASSIGNED",
                    pickupAddress = d["pickupAddress"] as? String ?: "",
                    deliveryAddress = d["deliveryAddress"] as? String ?: "",
                    customerName = d["customerName"] as? String ?: d["customerPhone"] as? String ?: "Customer",
                    customerPhone = d["customerPhone"] as? String ?: "",
                    itemName = d["itemName"] as? String ?: "",
                    itemWeight = (d["itemWeight"] as? Number)?.toDouble() ?: 0.0,
                    otpCode = d["otpCode"] as? String ?: "",
                    otpVerified = d["otpVerified"] as? Boolean ?: false,
                    photoProofUri = d["photoProofUri"] as? String,
                    scheduledAt = d["scheduledAt"] as? String ?: "Immediate",
                    distance = "3.2 km",
                    etaMinutes = (d["etaMinutes"] as? Number)?.toInt() ?: 15,
                    deliveryType = d["deliveryType"] as? String ?: "Express",
                    totalAmount = (d["totalAmount"] as? Number)?.toDouble() ?: 0.0
                )
            }
            loadStats()
        } catch (_: Exception) { }
    }

    fun navigateTo(view: RiderView) {
        _navigationHistory.add(_currentView.value)
        _currentView.value = view
    }

    fun navigateBack() {
        if (_navigationHistory.isNotEmpty()) _currentView.value = _navigationHistory.removeLast()
        else _currentView.value = RiderView.Dashboard
    }

    fun navigateToRoot(view: RiderView) {
        _navigationHistory.clear()
        _currentView.value = view
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true; _error.value = null
            try {
                val result = FirebaseService.auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user
                if (firebaseUser != null) {
                    preferences.setRider(true)
                    preferences.setLoggedIn(true)
                    preferences.saveUser(
                        firebaseUser.displayName ?: email.substringBefore("@"),
                        email, firebaseUser.phoneNumber ?: "", ""
                    )
                    _navigationHistory.clear()
                    if (uid.isNotEmpty()) loadRiderFromFirestore()
                    onSuccess()
                } else {
                    _error.value = "Login failed"
                }
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _error.value = "Invalid email or password"
            } catch (e: Exception) {
                _error.value = e.message ?: "Login failed"
            }
            _isLoading.value = false
        }
    }

    fun loadRiderData() {
        viewModelScope.launch {
            _riderProfile.value = RiderProfile(
                fullName = preferences.userName.first(),
                email = preferences.userEmail.first(),
                phone = preferences.userPhone.first(),
                photoUrl = preferences.userPhoto.first(),
                bikeNumber = preferences.riderBikeNumber.first(),
                bikeModel = preferences.riderBikeModel.first(),
                currentZone = preferences.riderZone.first(),
                totalDeliveries = preferences.riderTotalDeliveries.first()
            )
            _isOnline.value = preferences.riderIsOnline.first()
        }
        if (uid.isNotEmpty()) {
            viewModelScope.launch {
                loadRiderFromFirestore()
                loadDeliveriesFromFirestore()
            }
        }
    }

    fun loadDeliveries() {
        viewModelScope.launch {
            if (uid.isNotEmpty()) {
                loadDeliveriesFromFirestore()
            } else {
                if (_riderDeliveries.value.isEmpty()) {
                    _riderDeliveries.value = listOf(
                        RiderDelivery(1, "ESD-EXP-8241", "ASSIGNED", "Engraced Hub, Maryland", "15 Admiralty Way, Lekki", "John Doe", "+2348012345678", "Electronics Package", 2.5, "4921", false, scheduledAt = "Immediate", distance = "3.2 km", etaMinutes = 18),
                        RiderDelivery(2, "ESD-ECO-4123", "PICKED_UP", "ShopRite, Ikeja", "42 Bourdillon Rd, Ikoyi", "Jane Smith", "+2348098765432", "Groceries Box", 4.0, "7638", false, scheduledAt = "Today, 10:30 AM", distance = "5.1 km", etaMinutes = 12)
                    )
                }
            }
        }
    }

    fun loadDeliveryByTracking(tracking: String) {
        viewModelScope.launch {
            val d = _riderDeliveries.value.find { it.trackingNumber == tracking }
            _currentDelivery.value = d

            if (uid.isNotEmpty() && d == null) {
                try {
                    val snapshot = FirebaseService.db.collection("deliveries")
                        .whereEqualTo("trackingNumber", tracking).limit(1).get().await()
                    val doc = snapshot.documents.firstOrNull()?.data ?: return@launch
                    _currentDelivery.value = RiderDelivery(
                        id = snapshot.documents.first().id.hashCode().toLong(),
                        trackingNumber = doc["trackingNumber"] as? String ?: tracking,
                        status = doc["status"] as? String ?: "ASSIGNED",
                        pickupAddress = doc["pickupAddress"] as? String ?: "",
                        deliveryAddress = doc["deliveryAddress"] as? String ?: "",
                        customerName = doc["customerName"] as? String ?: "Customer",
                        customerPhone = doc["customerPhone"] as? String ?: "",
                        itemName = doc["itemName"] as? String ?: "",
                        itemWeight = (doc["itemWeight"] as? Number)?.toDouble() ?: 0.0,
                        otpCode = doc["otpCode"] as? String ?: "",
                        otpVerified = doc["otpVerified"] as? Boolean ?: false,
                        scheduledAt = doc["scheduledAt"] as? String ?: "Immediate",
                        etaMinutes = (doc["etaMinutes"] as? Number)?.toInt() ?: 15,
                        deliveryType = doc["deliveryType"] as? String ?: "Express",
                        totalAmount = (doc["totalAmount"] as? Number)?.toDouble() ?: 0.0
                    )
                } catch (_: Exception) { }
            }
        }
    }

    fun updateDeliveryStatus(tracking: String, newStatus: String) {
        viewModelScope.launch {
            _riderDeliveries.value = _riderDeliveries.value.map {
                if (it.trackingNumber == tracking) it.copy(status = newStatus) else it
            }
            _currentDelivery.value = _currentDelivery.value?.copy(status = newStatus)

            if (uid.isNotEmpty()) {
                try {
                    val snap = FirebaseService.db.collection("deliveries")
                        .whereEqualTo("trackingNumber", tracking).limit(1).get().await()
                    snap.documents.firstOrNull()?.reference
                        ?.update("status", newStatus)?.await()
                } catch (_: Exception) { }
            }
            loadStats()
        }
    }

    fun verifyOtp(tracking: String, otp: String): Boolean {
        if (otp.length != 4) return false
        val delivery = _currentDelivery.value ?: return false
        if (otp == delivery.otpCode) {
            updateDeliveryStatus(tracking, "DELIVERED")
            viewModelScope.launch {
                _currentDelivery.value = _currentDelivery.value?.copy(otpVerified = true)
                if (uid.isNotEmpty()) {
                    try {
                        val snap = FirebaseService.db.collection("deliveries")
                            .whereEqualTo("trackingNumber", tracking).limit(1).get().await()
                        snap.documents.firstOrNull()?.reference
                            ?.update("otpVerified", true)?.await()
                        FirebaseService.db.collection("riders").document(uid)
                            .update("totalDeliveries", FieldValue.increment(1)).await()
                    } catch (_: Exception) { }
                }
            }
            return true
        }
        return false
    }

    fun uploadPhoto(tracking: String, uri: String) {
        viewModelScope.launch {
            _currentDelivery.value = _currentDelivery.value?.copy(photoProofUri = uri)
            if (uid.isNotEmpty()) {
                try {
                    val snap = FirebaseService.db.collection("deliveries")
                        .whereEqualTo("trackingNumber", tracking).limit(1).get().await()
                    snap.documents.firstOrNull()?.reference
                        ?.update("photoProofUri", uri)?.await()
                } catch (_: Exception) { }
            }
        }
    }

    fun toggleOnline() {
        val newStatus = !_isOnline.value
        _isOnline.value = newStatus
        viewModelScope.launch {
            preferences.setRiderOnline(newStatus)
            if (uid.isNotEmpty()) {
                try {
                    FirebaseService.db.collection("riders").document(uid)
                        .update("isOnline", newStatus).await()
                } catch (_: Exception) { }
            }
        }
    }

    private fun loadStats() {
        val total = _riderDeliveries.value.size
        val completed = _riderDeliveries.value.count { it.status == "DELIVERED" }
        _riderStats.value = RiderStats(
            todayDeliveries = total,
            todayCompleted = completed,
            totalDeliveries = _riderProfile.value.totalDeliveries,
            rating = _riderProfile.value.rating
        )
    }

    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch { preferences.setDarkMode(enabled) }
    }

    fun updateBiometric(enabled: Boolean) {
        viewModelScope.launch { preferences.setBiometricEnabled(enabled) }
    }

    fun logout() {
        viewModelScope.launch {
            FirebaseService.auth.signOut()
            preferences.clearAll()
            _navigationHistory.clear()
            _currentView.value = RiderView.Splash
        }
    }
}
