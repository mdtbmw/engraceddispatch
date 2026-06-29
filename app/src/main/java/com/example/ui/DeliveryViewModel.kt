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
import com.example.data.api.NotificationItem
import com.example.data.api.Promotion
import com.example.data.api.ReferralData
import com.example.data.api.ReferralEntry
import com.example.data.models.*
import com.example.data.preferences.UserPreferences
import com.example.data.repository.AuthRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.ui.navigation.AppView
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
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

    private val _walletBalance = MutableStateFlow(0.0)
    val walletBalance = _walletBalance.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions = _transactions.asStateFlow()

    private val _currentUser = MutableStateFlow(User())
    val currentUser = _currentUser.asStateFlow()

    private val _currentTrackingDelivery = MutableStateFlow<Delivery?>(null)
    val currentTrackingDelivery = _currentTrackingDelivery.asStateFlow()

    private val _selectedDeliveryType = MutableStateFlow("Express")
    val selectedDeliveryType = _selectedDeliveryType.asStateFlow()

    private val _otpVerificationEnabled = MutableStateFlow(true)
    val otpVerificationEnabled = _otpVerificationEnabled.asStateFlow()

    private val _navigationHistory = mutableListOf<AppView>()
    private var _initialized = false
    private val uid get() = FirebaseService.currentUser?.uid ?: ""

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
            _walletBalance.value = preferences.walletBalance.first()
            val email = preferences.userEmail.first()
            val name = preferences.userName.first()
            val phone = preferences.userPhone.first()
            val photo = preferences.userPhoto.first()
            val rating = preferences.userRating.first()
            val deliveries = preferences.userTotalDeliveries.first()
            val earned = preferences.userTotalEarned.first()
            val memberSince = preferences.userMemberSince.first()

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

        viewModelScope.launch {
            preferences.walletBalance.collect { balance ->
                _walletBalance.value = balance
            }
        }

        FirebaseService.auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null && uid.isNotEmpty()) {
                viewModelScope.launch {
                    loadUserFromFirestore()
                    repository.syncDeliveriesFromFirestore(uid)
                    loadTransactionsFromFirestore()
                    loadNotificationsFromFirestore()
                    loadOtpSettingsFromFirestore()
                }
            }
        }

        viewModelScope.launch { loadOtpSettingsFromFirestore() }

        loadTransactions()
        checkAuthState()
    }

    private suspend fun loadUserFromFirestore() {
        if (uid.isEmpty()) return
        try {
            val doc = FirebaseService.db.collection("customers").document(uid).get().await()
            if (doc.exists()) {
                val name = doc.getString("fullName") ?: ""
                val email = doc.getString("email") ?: ""
                val phone = doc.getString("phone") ?: ""
                val photo = doc.getString("photoUrl") ?: ""
                val rating = doc.getDouble("rating")?.toFloat() ?: 5.0f
                val totalDeliveries = doc.getLong("totalDeliveries")?.toInt() ?: 0
                val memberSince = doc.getString("createdAt") ?: ""
                val walletBal = doc.getDouble("walletBalance") ?: 0.0

                _currentUser.value = _currentUser.value.copy(
                    fullName = name, email = email, phone = phone,
                    photoUrl = photo, rating = rating,
                    totalDeliveries = totalDeliveries, memberSince = memberSince
                )
                _walletBalance.value = walletBal
                preferences.saveUser(name, email, phone, photo)
                preferences.saveWalletBalance(walletBal)
                preferences.saveUserStats(rating, totalDeliveries, 0.0, memberSince)
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
                        loadUserFromFirestore()
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
                    fullName = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "User",
                    email = firebaseUser.email ?: "",
                    phone = firebaseUser.phoneNumber ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                    isVerified = firebaseUser.isEmailVerified,
                )
                preferences.saveAuthToken(token)
                preferences.setLoggedIn(true)
                preferences.saveUser(user.fullName, user.email, user.phone, user.photoUrl)
                preferences.saveUserStats(user.rating, user.totalDeliveries, user.totalEarned, user.memberSince)
                _currentUser.value = user

                if (uid.isNotEmpty()) {
                    val doc = FirebaseService.db.collection("customers").document(uid).get().await()
                    if (!doc.exists()) {
                        FirebaseService.db.collection("customers").document(uid).set(mapOf(
                            "fullName" to user.fullName, "email" to user.email, "phone" to user.phone,
                            "photoUrl" to user.photoUrl, "isVerified" to true, "rating" to 5.0,
                            "totalDeliveries" to 0, "walletBalance" to 0.0,
                            "createdAt" to java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                        )).await()
                        token?.let {
                            try {
                                val url = java.net.URL("${FirebaseService.BACKEND_BASE_URL}/api/auth/set-role")
                                val conn = url.openConnection() as java.net.HttpURLConnection
                                conn.requestMethod = "POST"
                                conn.setRequestProperty("Content-Type", "application/json")
                                conn.setRequestProperty("Authorization", "Bearer $it")
                                conn.doOutput = true
                                conn.outputStream.write("{\"role\":\"customer\"}".toByteArray())
                                conn.outputStream.close()
                                conn.responseCode
                                conn.disconnect()
                            } catch (_: Exception) {}
                        }
                    } else {
                        loadUserFromFirestore()
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
                    _currentUser.value = data.user
                    _walletBalance.value = 0.0
                    preferences.saveWalletBalance(0.0)
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
                    FirebaseService.db.collection("customers").document(uid)
                        .update(mapOf("fullName" to name, "email" to email, "phone" to phone))
                        .await()
                } catch (e: Exception) {
                    _error.value = "Network error: Profile updated locally"
                }
            }
            _isLoading.value = false
        }
    }

    // ===== WALLET =====
    fun fundWallet(amount: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val newBalance = _walletBalance.value + amount
            _walletBalance.value = newBalance
            preferences.saveWalletBalance(newBalance)

            val tx = Transaction(
                title = "Wallet Topup",
                description = "Ref: ESD-TX-${(100000..999999).random()}",
                amount = amount,
                type = TransactionType.CREDIT,
                status = TransactionStatus.COMPLETED,
                createdAt = System.currentTimeMillis()
            )
            _transactions.value = listOf(tx) + _transactions.value

            if (uid.isNotEmpty()) {
                try {
                    FirebaseService.db.collection("customers").document(uid)
                        .update("walletBalance", FieldValue.increment(amount))
                        .await()
                    FirebaseService.db.collection("transactions").add(
                        mapOf(
                            "userId" to uid, "title" to tx.title, "description" to tx.description,
                            "amount" to tx.amount, "type" to "CREDIT", "status" to "COMPLETED",
                            "createdAt" to tx.createdAt, "reference" to tx.description
                        )
                    ).await()
                } catch (_: Exception) { }
            }
            _isLoading.value = false
        }
    }

    fun withdrawFunds(amount: Double): Boolean {
        if (_walletBalance.value < amount) return false
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val newBalance = _walletBalance.value - amount
            _walletBalance.value = newBalance
            preferences.saveWalletBalance(newBalance)

            val tx = Transaction(
                title = "Withdrawal",
                description = "Ref: ESD-WD-${(100000..999999).random()}",
                amount = amount,
                type = TransactionType.DEBIT,
                status = TransactionStatus.COMPLETED,
                createdAt = System.currentTimeMillis()
            )
            _transactions.value = listOf(tx) + _transactions.value

            if (uid.isNotEmpty()) {
                try {
                    FirebaseService.db.collection("customers").document(uid)
                        .update("walletBalance", FieldValue.increment(-amount))
                        .await()
                    FirebaseService.db.collection("transactions").add(
                        mapOf(
                            "userId" to uid, "title" to tx.title, "description" to tx.description,
                            "amount" to tx.amount, "type" to "DEBIT", "status" to "COMPLETED",
                            "createdAt" to tx.createdAt, "reference" to tx.description
                        )
                    ).await()
                } catch (_: Exception) { }
            }
            _isLoading.value = false
        }
        return true
    }

    fun deductForBooking(amount: Double): Boolean {
        if (_walletBalance.value < amount) return false
        val newBalance = _walletBalance.value - amount
        _walletBalance.value = newBalance
        viewModelScope.launch {
            preferences.saveWalletBalance(newBalance)
            if (uid.isNotEmpty()) {
                try {
                    FirebaseService.db.collection("customers").document(uid)
                        .update("walletBalance", FieldValue.increment(-amount))
                        .await()
                } catch (_: Exception) { }
            }
        }

        val tx = Transaction(
            title = "Delivery Fee",
            description = "Dispatch service charge",
            amount = amount,
            type = TransactionType.DEBIT,
            status = TransactionStatus.COMPLETED,
            createdAt = System.currentTimeMillis()
        )
        _transactions.value = listOf(tx) + _transactions.value
        return true
    }

    fun loadTransactions() {
        viewModelScope.launch { loadTransactionsFromFirestore() }
    }

    private suspend fun loadTransactionsFromFirestore() {
        if (uid.isEmpty()) {
            val defaultTransactions = listOf(
                Transaction(title = "Wallet Topup (Webpay)", description = "Ref: ESD-TX-892401", amount = 5000.0, type = TransactionType.CREDIT, status = TransactionStatus.COMPLETED, createdAt = System.currentTimeMillis() - 3600000),
                Transaction(title = "Delivery: ESD-EXP-8241", description = "Express Service Fee", amount = 1500.0, type = TransactionType.DEBIT, status = TransactionStatus.COMPLETED, createdAt = System.currentTimeMillis() - 86400000),
                Transaction(title = "Delivery: ESD-ECO-4123", description = "Economy Pool Share Fee", amount = 800.0, type = TransactionType.DEBIT, status = TransactionStatus.COMPLETED, createdAt = System.currentTimeMillis() - 172800000),
                Transaction(title = "Wallet Topup (Bank Transfer)", description = "Ref: ESD-TX-891042", amount = 20000.0, type = TransactionType.CREDIT, status = TransactionStatus.COMPLETED, createdAt = System.currentTimeMillis() - 259200000)
            )
            _transactions.value = defaultTransactions
            return
        }
        try {
            val snapshot = FirebaseService.db.collection("transactions")
                .whereEqualTo("userId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            _transactions.value = snapshot.documents.mapNotNull { doc ->
                val d = doc.data ?: return@mapNotNull null
                Transaction(
                    title = d["title"] as? String ?: "",
                    description = d["description"] as? String ?: "",
                    amount = (d["amount"] as? Number)?.toDouble() ?: 0.0,
                    type = if (d["type"] == "CREDIT") TransactionType.CREDIT else TransactionType.DEBIT,
                    status = when (d["status"] as? String) {
                        "PENDING" -> TransactionStatus.PENDING
                        "COMPLETED" -> TransactionStatus.COMPLETED
                        else -> TransactionStatus.FAILED
                    },
                    createdAt = (d["createdAt"] as? Long) ?: System.currentTimeMillis()
                )
            }
        } catch (_: Exception) {
            if (_transactions.value.isEmpty()) {
                loadTransactions()
            }
        }
    }

    // ===== DELIVERIES =====
    fun selectDeliveryType(type: String) {
        _selectedDeliveryType.value = type
    }

    fun createBooking(
        pickup: String,
        delivery: String,
        itemName: String,
        itemWeight: Double,
        date: String,
        time: String,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val basePrice = getBasePrice(_selectedDeliveryType.value)
            val surge = getSurgeAmount(_selectedDeliveryType.value)
            val totalCost = basePrice + surge

            if (_walletBalance.value < totalCost) {
                _error.value = "Insufficient wallet balance. Please fund your wallet."
                _isLoading.value = false
                return@launch
            }

            val trackingNum = "ESD-" + _selectedDeliveryType.value.take(3).uppercase() + "-" + (1000..9999).random()
            val otpEnabled = _otpVerificationEnabled.value
            val otp = if (otpEnabled) (1000..9999).random().toString() else ""
            val otpVerified = !otpEnabled

            val riderNames = listOf("Sani Ibrahim", "Chukwuemeka Obi", "Tunde Bakare", "Emeka Nwosu", "Adebayo Oladipo")
            val bikeNumbers = listOf("LAG-5832-BK", "LAG-3291-YZ", "LAG-7453-MN", "LAG-1087-QR", "LAG-6294-ST")
            val selectedRider = riderNames.random()
            val selectedBike = bikeNumbers.random()
            val eta = (10..35).random()

            val newDelivery = Delivery(
                trackingNumber = trackingNum,
                deliveryType = _selectedDeliveryType.value,
                status = "PENDING",
                totalAmount = totalCost,
                scheduledAt = if (date == "Immediate") "Immediate" else "$date, $time",
                pickupAddress = pickup,
                deliveryAddress = delivery,
                itemName = itemName,
                itemWeight = itemWeight,
                otpCode = otp,
                otpVerified = otpVerified,
                riderName = selectedRider,
                riderBikeNumber = selectedBike,
                riderRating = (4.0f + Math.random() * 1.0f).toFloat(),
                etaMinutes = eta
            )

            deductForBooking(totalCost)

            val id = repository.insert(newDelivery)
            val createdDelivery = newDelivery.copy(id = id)

            if (uid.isNotEmpty()) {
                try {
                    val deliveryMap = mapOf(
                        "trackingNumber" to trackingNum,
                        "deliveryType" to _selectedDeliveryType.value,
                        "status" to "PENDING",
                        "totalAmount" to totalCost,
                        "scheduledAt" to newDelivery.scheduledAt,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "pickupAddress" to pickup,
                        "deliveryAddress" to delivery,
                        "itemName" to itemName,
                        "itemWeight" to itemWeight,
                        "otpCode" to otp,
                        "otpVerified" to otpVerified,
                        "riderName" to selectedRider,
                        "riderBikeNumber" to selectedBike,
                        "riderRating" to newDelivery.riderRating,
                        "etaMinutes" to eta,
                        "userId" to uid
                    )
                    FirebaseService.db.collection("deliveries").add(deliveryMap).await()
                } catch (_: Exception) { }
            }

            _currentTrackingDelivery.value = createdDelivery
            _currentView.value = AppView.ActiveTracking(createdDelivery.trackingNumber)
            _isLoading.value = false
            onSuccess(createdDelivery.trackingNumber)
        }
    }

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
                            doc.reference.update(mapOf(
                                "otpVerified" to true, "status" to "DELIVERED"
                            )).await()
                        }
                    } catch (_: Exception) { }
                }

                val earned = _currentUser.value.totalEarned + current.totalAmount * 0.3
                val deliveriesCount = _currentUser.value.totalDeliveries + 1
                preferences.saveUserStats(_currentUser.value.rating, deliveriesCount, earned, _currentUser.value.memberSince)
                _currentUser.value = _currentUser.value.copy(totalDeliveries = deliveriesCount, totalEarned = earned)

                if (uid.isNotEmpty()) {
                    try {
                        FirebaseService.db.collection("customers").document(uid)
                            .update(mapOf("totalDeliveries" to FieldValue.increment(1)))
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

    fun getBasePrice(type: String): Double = when (type) {
        "Express" -> 1500.0
        "Economy" -> 800.0
        "Batch" -> 2500.0
        "Multi-Pickup" -> 3500.0
        else -> 1200.0
    }

    fun getSurgeAmount(type: String): Int = if (type == "Express") 525 else 0

    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage = _selectedLanguage.asStateFlow()

    fun updateLanguage(lang: String) {
        _selectedLanguage.value = lang
        savePreferencesToFirestore()
    }

    // ===== SETTINGS =====
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
                    // Re-authenticate first
                    val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email ?: "", current)
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
                FirebaseService.db.collection("customers").document(uid)
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
                loadUserFromFirestore()
                repository.syncDeliveriesFromFirestore(uid)
                loadTransactionsFromFirestore()
                loadNotificationsFromFirestore()
                loadPromotionsFromFirestore()
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

    // ===== NEW SCREEN METHODS =====
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications = _notifications.asStateFlow()

    fun loadNotifications() {
        viewModelScope.launch { loadNotificationsFromFirestore() }
    }

    private suspend fun loadNotificationsFromFirestore() {
        if (uid.isEmpty()) {
            _notifications.value = listOf(
                NotificationItem(1, "Delivery Assigned", "Your package ESD-EXP-8241 has been assigned to Sani Ibrahim", "tracking", false, System.currentTimeMillis() - 1800000, "ESD-EXP-8241"),
                NotificationItem(2, "Payment Received", "Wallet topup of \u20A65,000 successful", "wallet", false, System.currentTimeMillis() - 7200000),
                NotificationItem(3, "Promotion Alert", "Express delivery 15% off this weekend!", "promo", true, System.currentTimeMillis() - 86400000),
                NotificationItem(4, "Delivery Completed", "Package ESD-ECO-4123 delivered successfully", "tracking", true, System.currentTimeMillis() - 172800000, "ESD-ECO-4123"),
                NotificationItem(5, "Referral Reward", "You earned \u20A62,000 from your referral", "referral", true, System.currentTimeMillis() - 259200000)
            )
            return
        }
        try {
            val snapshot = FirebaseService.db.collection("notifications")
                .whereEqualTo("userId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()
            _notifications.value = snapshot.documents.mapNotNull { doc ->
                val d = doc.data ?: return@mapNotNull null
                NotificationItem(
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
                    NotificationItem(1, "Delivery Assigned", "Your package ESD-EXP-8241 has been assigned to Sani Ibrahim", "tracking", false, System.currentTimeMillis() - 1800000, "ESD-EXP-8241"),
                    NotificationItem(2, "Payment Received", "Wallet topup of \u20A65,000 successful", "wallet", false, System.currentTimeMillis() - 7200000),
                    NotificationItem(3, "Promotion Alert", "Express delivery 15% off this weekend!", "promo", true, System.currentTimeMillis() - 86400000),
                    NotificationItem(4, "Delivery Completed", "Package ESD-ECO-4123 delivered successfully", "tracking", true, System.currentTimeMillis() - 172800000, "ESD-ECO-4123"),
                    NotificationItem(5, "Referral Reward", "You earned \u20A62,000 from your referral", "referral", true, System.currentTimeMillis() - 259200000)
                )
            }
        }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            _notifications.value = _notifications.value.map { if (it.id == id) it.copy(isRead = true) else it }
            if (uid.isNotEmpty()) {
                try {
                    val snapshot = FirebaseService.db.collection("notifications")
                        .whereEqualTo("userId", uid).limit(20).get().await()
                    val docs = snapshot.documents.filter { it.id.hashCode().toLong() == id }
                    docs.firstOrNull()?.reference?.update("isRead", true)?.await()
                } catch (_: Exception) { }
            }
        }
    }

    private val _addresses = MutableStateFlow<List<Address>>(emptyList())
    val addresses = _addresses.asStateFlow()

    fun loadAddresses() {
        viewModelScope.launch {
            if (uid.isEmpty()) {
                _addresses.value = listOf(
                    Address(1, "Home", "Engraced Logistics Hub, Maryland, Lagos", true),
                    Address(2, "Work", "Chevron Drive, Lekki Phase 1, Lagos", false)
                )
                return@launch
            }
            try {
                val snapshot = FirebaseService.db.collection("addresses")
                    .whereEqualTo("userId", uid).get().await()
                _addresses.value = snapshot.documents.mapNotNull { doc ->
                    val d = doc.data
                    Address(
                        id = doc.id.hashCode().toLong(),
                        label = d["label"] as? String ?: "",
                        address = d["address"] as? String ?: "",
                        isDefault = d["isDefault"] as? Boolean ?: false
                    )
                }
            } catch (_: Exception) {
                _addresses.value = listOf(
                    Address(1, "Home", "Engraced Logistics Hub, Maryland, Lagos", true),
                    Address(2, "Work", "Chevron Drive, Lekki Phase 1, Lagos", false)
                )
            }
        }
    }

    fun saveAddress(label: String, address: String) {
        viewModelScope.launch {
            val newAddr = Address(
                id = System.currentTimeMillis(),
                label = label,
                address = address,
                isDefault = _addresses.value.isEmpty()
            )
            _addresses.value = _addresses.value + newAddr
            if (uid.isNotEmpty()) {
                try {
                    FirebaseService.db.collection("addresses").add(
                        mapOf("userId" to uid, "label" to label, "address" to address, "isDefault" to newAddr.isDefault)
                    ).await()
                } catch (_: Exception) { }
            }
        }
    }

    fun submitReview(rating: Int, comment: String, trackingNumber: String) {
        viewModelScope.launch {
            if (uid.isNotEmpty()) {
                try {
                    FirebaseService.db.collection("reviews").add(
                        mapOf("userId" to uid, "rating" to rating, "comment" to comment, "trackingNumber" to trackingNumber, "createdAt" to System.currentTimeMillis())
                    ).await()
                } catch (_: Exception) { }
            }
        }
    }

    private val _referralCode = MutableStateFlow("")
    val referralCode = _referralCode.asStateFlow()
    private val _referralStats = MutableStateFlow(ReferralData("", 0.0, 0))
    val referralStats = _referralStats.asStateFlow()
    private val _referralHistory = MutableStateFlow<List<ReferralEntry>>(emptyList())
    val referralHistory = _referralHistory.asStateFlow()

    fun loadReferralData() {
        viewModelScope.launch {
            val code = "ESD-${_currentUser.value.fullName.take(4).uppercase()}-${(1000..9999).random()}"
            _referralCode.value = code
            _referralStats.value = ReferralData(code, 0.0, 0)

            if (uid.isNotEmpty()) {
                try {
                    val snap = FirebaseService.db.collection("referrals")
                        .whereEqualTo("userId", uid).limit(1).get().await()
                    val doc = snap.documents.firstOrNull()
                    if (doc != null) {
                        val d = doc.data
                        _referralCode.value = d?.get("code") as? String ?: code
                        _referralStats.value = ReferralData(
                            code = d?.get("code") as? String ?: code,
                            totalEarned = (d?.get("totalEarned") as? Number)?.toDouble() ?: 0.0,
                            totalReferrals = (d?.get("totalReferrals") as? Number)?.toInt() ?: 0
                        )
                    }
                } catch (_: Exception) { }
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

    // ===== PAYSTACK PAYMENT =====
    data class PaystackInitResult(val authorizationUrl: String, val reference: String)

    fun initPaystackPayment(amount: Double, onResult: (PaystackInitResult?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val email = _currentUser.value.email.ifEmpty { FirebaseService.currentUser?.email ?: "user@engraced.com" }
            try {
                val token = FirebaseService.getCurrentUserIdToken() ?: ""
                val url = URL("${FirebaseService.BACKEND_BASE_URL}/api/payment/initialize")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.doOutput = true
                val body = JSONObject().apply {
                    put("amount", (amount * 100).toInt())
                    put("email", email)
                }
                OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
                val responseCode = conn.responseCode
                if (responseCode == 200) {
                    val resp = conn.inputStream.bufferedReader().readText()
                    val json = JSONObject(resp)
                    val authUrl = json.optString("authorization_url")
                    val reference = json.optString("reference")
                    if (authUrl.isNotEmpty()) {
                        onResult(PaystackInitResult(authUrl, reference))
                    } else {
                        _error.value = json.optString("message", "Payment initialization failed")
                        onResult(null)
                    }
                } else {
                    _error.value = "Payment server error ($responseCode)"
                    onResult(null)
                }
                conn.disconnect()
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
                onResult(null)
            }
            _isLoading.value = false
        }
    }

    fun verifyPaystackPayment(reference: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = FirebaseService.getCurrentUserIdToken() ?: ""
                val url = URL("${FirebaseService.BACKEND_BASE_URL}/api/payment/verify")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.doOutput = true
                val body = JSONObject().put("reference", reference)
                OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
                val responseCode = conn.responseCode
                if (responseCode == 200) {
                    val resp = conn.inputStream.bufferedReader().readText()
                    val json = JSONObject(resp)
                    val success = json.optBoolean("success", false)
                    if (success) {
                        // Reload wallet balance from Firestore after payment
                        if (uid.isNotEmpty()) {
                            val doc = FirebaseService.db.collection("customers").document(uid).get().await()
                            val balance = doc.getDouble("walletBalance") ?: 0.0
                            _walletBalance.value = balance
                            preferences.saveWalletBalance(balance)
                        }
                        loadTransactionsFromFirestore()
                    }
                    onComplete(success)
                } else {
                    onComplete(false)
                }
                conn.disconnect()
            } catch (_: Exception) {
                onComplete(false)
            }
            _isLoading.value = false
        }
    }

    private val _promotions = MutableStateFlow<List<Promotion>>(emptyList())
    val promotions = _promotions.asStateFlow()

    fun loadPromotions() {
        viewModelScope.launch { loadPromotionsFromFirestore() }
    }

    private suspend fun loadPromotionsFromFirestore() {
        try {
            val snapshot = FirebaseService.db.collection("promotions")
                .whereEqualTo("isActive", true).get().await()
            _promotions.value = snapshot.documents.mapNotNull { doc ->
                val d = doc.data ?: return@mapNotNull null
                Promotion(
                    id = doc.id.hashCode().toLong(),
                    title = d["title"] as? String ?: "",
                    subtitle = d["subtitle"] as? String ?: "",
                    value = d["value"] as? String ?: "",
                    icon = d["icon"] as? String ?: "Bolt",
                    expiresAt = d["expiresAt"] as? String,
                    terms = d["terms"] as? String ?: ""
                )
            }
        } catch (_: Exception) { }
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
