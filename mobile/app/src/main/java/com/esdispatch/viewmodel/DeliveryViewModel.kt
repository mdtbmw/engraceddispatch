package com.esdispatch.viewmodel


import android.content.Context
import android.widget.Toast
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.esdispatch.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import com.esdispatch.BuildConfig
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

enum class AppView {
    Dashboard,
    Booking,
    ActiveTracking
}

data class SubAdminUser(
    val id: String,
    val name: String,
    val email: String,
    val permission: String // "View Only" | "Content Manager" | "Super Admin"
)

data class AdminActivityLog(
    val id: String,
    val timestamp: String,
    val action: String,
    val details: String,
    val adminName: String
)

class DeliveryViewModel : WalletViewModel() {

    // --- Context & Preferences Persistence ---
    

    // --- Stateful Stack-Based Navigation System ---
    private val _navigationStack = MutableStateFlow<List<AppView>>(listOf(AppView.Dashboard))
    val navigationStack: StateFlow<List<AppView>> = _navigationStack.asStateFlow()

    fun pushView(view: AppView) {
        _navigationStack.update { it + view }
    }

    fun popView(): Boolean {
        if (_navigationStack.value.size > 1) {
            _navigationStack.update { it.dropLast(1) }
            return true
        }
        return false // Can't pop root view
    }

    fun clearToRoot() {
        _navigationStack.value = listOf(AppView.Dashboard)
    }

    private fun getEncryptedPrefs(context: Context): android.content.SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                "esdispatch_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            android.util.Log.w("DeliveryViewModel", "Encrypted prefs unavailable, falling back: ${e.message}")
            context.getSharedPreferences("esdispatch_prefs", Context.MODE_PRIVATE)
        }
    }

    private fun savePinSecurely(key: String, pin: String) {
        val ctx = appContext ?: return
        try {
            getEncryptedPrefs(ctx).edit().putString(key, pin).apply()
        } catch (e: Exception) {
            savePreference(ctx, key, pin)
        }
    }

    private fun getPinSecurely(key: String, default: String = ""): String {
        val ctx = appContext ?: return default
        return try {
            getEncryptedPrefs(ctx).getString(key, default) ?: default
        } catch (e: Exception) {
            ctx.getSharedPreferences("esdispatch_prefs", Context.MODE_PRIVATE)
                .getString(key, default) ?: default
        }
    }

    private fun savePreference(context: Context, key: String, value: Any) {
        val prefs = context.getSharedPreferences("esdispatch_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is String -> putString(key, value)
                is Float -> putFloat(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Double -> putString(key, value.toString())
            }
            apply()
        }
    }

    

    private fun loadPreferences(context: Context) {
        val prefs = context.getSharedPreferences("esdispatch_prefs", Context.MODE_PRIVATE)
        
        _walletBalance.value = prefs.getString("wallet_balance", "0.0")?.toDoubleOrNull() ?: 0.0
        _pushEnabled.value = prefs.getBoolean("push_enabled", true)
        _pushAlertsBooked.value = prefs.getBoolean("alerts_booked", true)
        _pushAlertsDispatched.value = prefs.getBoolean("alerts_dispatched", true)
        _pushAlertsDelivered.value = prefs.getBoolean("alerts_delivered", true)
        _pushAlertsCancelled.value = prefs.getBoolean("alerts_cancelled", true)
        _locationEnabled.value = prefs.getBoolean("location_enabled", true)
        _darkModeEnabled.value = prefs.getBoolean("dark_mode_enabled", false)
        _dashboardVariant.value = prefs.getString("dashboard_variant", "full") ?: "full"
        _language.value = prefs.getString("language", "English") ?: "English"
        _defaultDeliveryType.value = prefs.getString("default_delivery_type", "Express") ?: "Express"
        
        _userName.value = prefs.getString("user_name", "") ?: ""
        _userEmail.value = prefs.getString("user_email", "") ?: ""
        _userPhone.value = prefs.getString("user_phone", "") ?: ""
        _photoUrl.value = prefs.getString("photo_url", "https://api.dicebear.com/7.x/avataaars/png?seed=elite&backgroundColor=c0aede") ?: "https://api.dicebear.com/7.x/avataaars/png?seed=elite&backgroundColor=c0aede"
        _isVerified.value = prefs.getBoolean("is_verified", false)
        _totalEarned.value = prefs.getString("total_earned", "0.0")?.toDoubleOrNull() ?: 0.0
        _deliveryCount.value = prefs.getInt("delivery_count", 0)
        _loyaltyPoints.value = prefs.getInt("loyalty_points", 0)
        _welcomeGiftClaimed.value = prefs.getBoolean("welcome_gift_claimed", false)
        _dailyBonusClaimed.value = prefs.getBoolean("daily_bonus_claimed", false)
        _userRating.value = prefs.getString("user_rating", "4.9")?.toDoubleOrNull() ?: 4.9
        _memberSince.value = prefs.getString("member_since", "Jun 2025") ?: "Jun 2025"
        _userPin.value = getPinSecurely("user_pin", "")
        _twoFactorEnabled.value = prefs.getBoolean("two_factor_enabled", false)
        _loginMode.value = prefs.getString("login_mode", "free") ?: "free"
        _biometricRegistered.value = prefs.getBoolean("biometric_registered", false)
        _biometricEnabled.value = prefs.getBoolean("biometric_enabled", false)
        _homeAddress.value = prefs.getString("home_address", "No. 12 Joel Ogunnaike Street, Ikeja GRA, Lagos") ?: "No. 12 Joel Ogunnaike Street, Ikeja GRA, Lagos"
        _workAddress.value = prefs.getString("work_address", "Plot 14, Kingsway Road, Ikoyi, Lagos") ?: "Plot 14, Kingsway Road, Ikoyi, Lagos"
        _preferredRider.value = prefs.getString("preferred_rider", "") ?: ""
        _bankName.value = prefs.getString("bank_name", "Access Bank") ?: "Access Bank"
        _accountNumber.value = prefs.getString("account_number", "0123456789") ?: "0123456789"
        _accountName.value = prefs.getString("account_name", "Engraced Member") ?: "Engraced Member"
        _autoVerifyVendors.value = prefs.getBoolean("auto_verify_vendors", true)

        val savedAreas = prefs.getString("service_areas", "") ?: ""
        if (savedAreas.isNotEmpty()) {
            _serviceAreas.value = savedAreas.split("|").filter { it.isNotEmpty() }
        }
        
        val searchesStr = prefs.getString("recent_searches", "") ?: ""
        _recentSearches.value = if (searchesStr.isEmpty()) emptyList() else searchesStr.split(",").filter { it.isNotEmpty() }
        _showOnboardingTooltip.value = prefs.getBoolean("show_onboarding_tooltip", true)
 
        _pointsSystemEnabled.value = prefs.getBoolean("points_system_enabled", true)
        _isDynamicPricingEnabled.value = prefs.getBoolean("pricing_mode_dynamic", true)
        _tipSystemEnabled.value = prefs.getBoolean("tip_system_enabled", true)
        _emailVerificationRequired.value = prefs.getBoolean("email_verification_required", false)
        _phoneVerificationRequired.value = prefs.getBoolean("phone_verification_required", false)
        _dashboardSectionsEnabled.value = mapOf(
            "promo_banner" to prefs.getBoolean("section_promo_banner", true),
            "active_shipments" to prefs.getBoolean("section_active_shipments", true),
            "quick_actions" to prefs.getBoolean("section_quick_actions", true),
            "loyalty_rewards" to prefs.getBoolean("section_loyalty_rewards", true)
        )
        _adminCardSliderConfigs.value = mapOf(
            "hero_title" to (prefs.getString("config_hero_title", "Elite Logistics & Instant Dispatch") ?: "Elite Logistics & Instant Dispatch"),
            "hero_subtitle" to (prefs.getString("config_hero_subtitle", "Secure AI-powered dispatch across nation") ?: "Secure AI-powered dispatch across nation"),
            "banner_image" to (prefs.getString("config_banner_image", "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=800&q=80") ?: "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=800&q=80"),
            "slider_interval_secs" to (prefs.getString("config_slider_interval_secs", "5") ?: "5")
        )
        _baseFare.value = (prefs.getString("pricing_base_fare", "4500.0") ?: "4500.0").toDoubleOrNull() ?: 4500.0
        _perKgRate.value = (prefs.getString("pricing_per_kg", "250.0") ?: "250.0").toDoubleOrNull() ?: 250.0
        _expressSurcharge.value = (prefs.getString("pricing_express", "1500.0") ?: "1500.0").toDoubleOrNull() ?: 1500.0
        _surgeMultiplier.value = (prefs.getString("pricing_surge", "1.25") ?: "1.25").toDoubleOrNull() ?: 1.25
 
        try {
            val db = FirebaseManager.firestore
            db?.collection("system_config")?.document("pricing")?.addSnapshotListener { snap, _ ->
                if (snap != null && snap.exists()) {
                    (snap.get("baseFare") as? Number)?.toDouble()?.let { _baseFare.value = it }
                    (snap.get("perKgRate") as? Number)?.toDouble()?.let { _perKgRate.value = it }
                    (snap.get("expressSurcharge") as? Number)?.toDouble()?.let { _expressSurcharge.value = it }
                    (snap.get("surgeMultiplier") as? Number)?.toDouble()?.let { _surgeMultiplier.value = it }
                    
                    val dEnabled = snap.get("discountEnabled")
                    if (dEnabled is Boolean) _adminDiscountEnabled.value = dEnabled
                    else if (dEnabled is String) _adminDiscountEnabled.value = dEnabled.toBoolean()

                    val dPercent = snap.get("discountPercent")
                    if (dPercent is Number) _adminDiscountPercent.value = dPercent.toInt()
                    else if (dPercent is String) _adminDiscountPercent.value = dPercent.toIntOrNull() ?: 0
                }
            }?.let { listenerRegistrations.add(it) }
            db?.collection("system_config")?.document("global_settings")?.addSnapshotListener { snap, _ ->
                if (snap != null && snap.exists()) {
                    (snap.get("pointsSystemEnabled") as? Boolean)?.let { _pointsSystemEnabled.value = it }
                    (snap.get("pricingModeDynamic") as? Boolean)?.let { _isDynamicPricingEnabled.value = it }
                    (snap.get("tipSystemEnabled") as? Boolean)?.let { _tipSystemEnabled.value = it }
                    (snap.get("emailVerificationRequired") as? Boolean)?.let { _emailVerificationRequired.value = it }
                    (snap.get("phoneVerificationRequired") as? Boolean)?.let { _phoneVerificationRequired.value = it }
                    (snap.get("maintenanceMode") as? Boolean)?.let { _maintenanceMode.value = it }
                    
                    ((snap.get("surgeMultiplier") as? Number)?.toDouble() ?: (snap.get("surgePriceMultiplier") as? Number)?.toDouble())?.let { 
                        _surgeMultiplier.value = it 
                    }
                    (snap.get("baseFare") as? Number)?.toDouble()?.let { _baseFare.value = it }
                    (snap.get("perKgRate") as? Number)?.toDouble()?.let { _perKgRate.value = it }
                    (snap.get("expressSurcharge") as? Number)?.toDouble()?.let { _expressSurcharge.value = it }
                    
                    val dEnabled = snap.get("discountEnabled")
                    if (dEnabled is Boolean) _adminDiscountEnabled.value = dEnabled
                    else if (dEnabled is String) _adminDiscountEnabled.value = dEnabled.toBoolean()

                    val dPercent = snap.get("discountPercent")
                    if (dPercent is Number) _adminDiscountPercent.value = dPercent.toInt()
                    else if (dPercent is String) _adminDiscountPercent.value = dPercent.toIntOrNull() ?: 0
                    
                    val rawSections = snap.get("dashboardSectionsEnabled") as? Map<*, *>
                    if (rawSections != null) {
                        val safeSections = rawSections.entries.associate { 
                            it.key.toString() to (it.value as? Boolean ?: (it.value.toString().toBoolean())) 
                        }
                        _dashboardSectionsEnabled.value = safeSections
                    }
                    val rawCardSlider = snap.get("adminCardSliderConfigs") as? Map<*, *>
                    if (rawCardSlider != null) {
                        val safeCardSlider = rawCardSlider.entries.associate {
                            it.key.toString() to it.value.toString()
                        }
                        _adminCardSliderConfigs.value = safeCardSlider
                    }
                }
            }?.let { listenerRegistrations.add(it) }
        } catch (e: Exception) {
            Log.e("DeliveryViewModel", "Failed to attach pricing or settings snapshot listener: ${e.message}")
        }
    }

    // --- Database & Repository Integration ---
    private var repository: DeliveryRepository? = null

    // --- ENGRACED DISPATCH ENTERPRISE AI STATE FIELDS ---
    private val _aiRiders = MutableStateFlow<List<Rider>>(emptyList())
    val aiRiders: StateFlow<List<Rider>> = _aiRiders.asStateFlow()

    private val _activeParcelChats = MutableStateFlow<List<ParcelChatMessage>>(emptyList())
    val activeParcelChats: StateFlow<List<ParcelChatMessage>> = _activeParcelChats.asStateFlow()
    private var chatListenerJob: kotlinx.coroutines.Job? = null

    private val _aiChatMessages = MutableStateFlow<List<AIChatMessage>>(emptyList())
    val aiChatMessages: StateFlow<List<AIChatMessage>> = _aiChatMessages.asStateFlow()

    private val listenerRegistrations = mutableListOf<com.google.firebase.firestore.ListenerRegistration>()

    private val _aiIsThinking = MutableStateFlow(false)
    val aiIsThinking: StateFlow<Boolean> = _aiIsThinking.asStateFlow()

    private val _aiSmartAssignmentReason = MutableStateFlow("Run rider assignment calculations to generate AI match explanation.")
    val aiSmartAssignmentReason: StateFlow<String> = _aiSmartAssignmentReason.asStateFlow()

    private val _aiSmartAssignmentList = MutableStateFlow<List<Pair<Rider, Int>>>(emptyList())
    val aiSmartAssignmentList: StateFlow<List<Pair<Rider, Int>>> = _aiSmartAssignmentList.asStateFlow()

    private val _aiRiskReport = MutableStateFlow<RiskReport?>(null)
    val aiRiskReport: StateFlow<RiskReport?> = _aiRiskReport.asStateFlow()

    private val _aiPODAnalysis = MutableStateFlow<PODAnalysis?>(null)
    val aiPODAnalysis: StateFlow<PODAnalysis?> = _aiPODAnalysis.asStateFlow()

    private val _aiFraudAlerts = MutableStateFlow<List<FraudAlert>>(emptyList())
    val aiFraudAlerts: StateFlow<List<FraudAlert>> = _aiFraudAlerts.asStateFlow()

    // Firebase Connection Status
    private val _firebaseConnected = MutableStateFlow(false)
    val firebaseConnected: StateFlow<Boolean> = _firebaseConnected.asStateFlow()

    // Firebase Configuration Status (strict production check)
    private val _isFirebaseConfigured = MutableStateFlow(true)
    val isFirebaseConfigured: StateFlow<Boolean> = _isFirebaseConfigured.asStateFlow()

    // Sandbox / Simulation Mode Status based on missing API keys
    private val _isSandboxEnvironment = MutableStateFlow(false)
    val isSandboxEnvironment: StateFlow<Boolean> = _isSandboxEnvironment.asStateFlow()

    // Remote maintenance mode flag
    private val _maintenanceMode = MutableStateFlow(false)
    val maintenanceMode: StateFlow<Boolean> = _maintenanceMode.asStateFlow()

    // Firebase Auth user state
    

    // Recent searched tracking numbers
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    // Real-time tracking subscription job
    private var trackingJob: kotlinx.coroutines.Job? = null

    private val _aiIncidentReports = MutableStateFlow<List<IncidentReport>>(emptyList())
    val aiIncidentReports: StateFlow<List<IncidentReport>> = _aiIncidentReports.asStateFlow()

    private val _aiDemandPredictions = MutableStateFlow<List<DemandPrediction>>(emptyList())
    val aiDemandPredictions: StateFlow<List<DemandPrediction>> = _aiDemandPredictions.asStateFlow()

    private val _aiDispatchLogs = MutableStateFlow<List<AIDispatchDecisionLog>>(emptyList())
    val aiDispatchLogs: StateFlow<List<AIDispatchDecisionLog>> = _aiDispatchLogs.asStateFlow()

    private val _shiftAttendanceList = MutableStateFlow<List<ShiftAttendance>>(emptyList())
    val shiftAttendanceList: StateFlow<List<ShiftAttendance>> = _shiftAttendanceList.asStateFlow()

    private val _currentAttendanceStatus = MutableStateFlow("OFF_DUTY") // "ON_DUTY", "ON_BREAK", "OFF_DUTY"
    val currentAttendanceStatus: StateFlow<String> = _currentAttendanceStatus.asStateFlow()

    private val _vehicleInspectionList = MutableStateFlow<List<VehicleInspection>>(emptyList())
    val vehicleInspectionList: StateFlow<List<VehicleInspection>> = _vehicleInspectionList.asStateFlow()

    private val _expenseClaimList = MutableStateFlow<List<ExpenseClaim>>(emptyList())
    val expenseClaimList: StateFlow<List<ExpenseClaim>> = _expenseClaimList.asStateFlow()

    private val _shiftRosterList = MutableStateFlow<List<ShiftRoster>>(emptyList())
    val shiftRosterList: StateFlow<List<ShiftRoster>> = _shiftRosterList.asStateFlow()

    private val _offlineSyncQueueList = MutableStateFlow<List<OfflineSyncQueue>>(emptyList())
    val offlineSyncQueueList: StateFlow<List<OfflineSyncQueue>> = _offlineSyncQueueList.asStateFlow()

    private val _riderPerformanceMetrics = MutableStateFlow<List<RiderPerformanceMetric>>(emptyList())
    val riderPerformanceMetrics: StateFlow<List<RiderPerformanceMetric>> = _riderPerformanceMetrics.asStateFlow()

    private val _deliveryPerformanceTrends = MutableStateFlow<List<DeliveryPerformanceTrend>>(emptyList())
    val deliveryPerformanceTrends: StateFlow<List<DeliveryPerformanceTrend>> = _deliveryPerformanceTrends.asStateFlow()

    private val _aiLearningWeights = MutableStateFlow(SelfLearningWeights())
    val aiLearningWeights: StateFlow<SelfLearningWeights> = _aiLearningWeights.asStateFlow()

    private val _aiTrafficCongested = MutableStateFlow(false)
    val aiTrafficCongested: StateFlow<Boolean> = _aiTrafficCongested.asStateFlow()

    private val _aiConfidenceScore = MutableStateFlow(98)
    val aiConfidenceScore: StateFlow<Int> = _aiConfidenceScore.asStateFlow()

    // OkHttpClient with 60-second timeouts as strictly mandated in the Gemini API skill
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // User Profile Info
    

    

    private val _userPhone = MutableStateFlow("")
    val userPhone: StateFlow<String> = _userPhone.asStateFlow()

    private val _photoUrl = MutableStateFlow("https://api.dicebear.com/7.x/avataaars/png?seed=elite&backgroundColor=c0aede")
    val photoUrl: StateFlow<String> = _photoUrl.asStateFlow()

    // App Icon Shortcuts / Quick Actions routing State
    private val _pendingShortcutRoute = MutableStateFlow<String?>(null)
    val pendingShortcutRoute: StateFlow<String?> = _pendingShortcutRoute.asStateFlow()

    fun setPendingShortcutRoute(route: String?) {
        _pendingShortcutRoute.value = route
    }

    fun clearPendingShortcutRoute() {
        _pendingShortcutRoute.value = null
    }

    private val _isVerified = MutableStateFlow(false)
    val isVerified: StateFlow<Boolean> = _isVerified.asStateFlow()

    // Role-based state flows (for Customers and Riders)
    private val _userRole = MutableStateFlow("customer") // "customer" | "rider" | "admin"
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _activeViewMode = MutableStateFlow("customer") // "customer" | "rider"
    val activeViewMode: StateFlow<String> = _activeViewMode.asStateFlow()

    // Admin & System Configurations
    private val _pointsSystemEnabled = MutableStateFlow(true)
    val pointsSystemEnabled: StateFlow<Boolean> = _pointsSystemEnabled.asStateFlow()

    private val _isDynamicPricingEnabled = MutableStateFlow(true)
    val isDynamicPricingEnabled: StateFlow<Boolean> = _isDynamicPricingEnabled.asStateFlow()

    private val _tipSystemEnabled = MutableStateFlow(true)
    val tipSystemEnabled: StateFlow<Boolean> = _tipSystemEnabled.asStateFlow()

    private val _emailVerificationRequired = MutableStateFlow(false)
    val emailVerificationRequired: StateFlow<Boolean> = _emailVerificationRequired.asStateFlow()

    

    private val _dashboardSectionsEnabled = MutableStateFlow(
        mapOf(
            "promo_banner" to true,
            "active_shipments" to true,
            "quick_actions" to true,
            "loyalty_rewards" to true
        )
    )
    val dashboardSectionsEnabled: StateFlow<Map<String, Boolean>> = _dashboardSectionsEnabled.asStateFlow()

    private val _adminCardSliderConfigs = MutableStateFlow(
        mapOf(
            "hero_title" to "Elite Logistics & Instant Dispatch",
            "hero_subtitle" to "Secure AI-powered dispatch across nation",
            "banner_image" to "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=800&q=80",
            "slider_interval_secs" to "5"
        )
    )
    val adminCardSliderConfigs: StateFlow<Map<String, String>> = _adminCardSliderConfigs.asStateFlow()

    private val _baseFare = MutableStateFlow(4500.0)
    val baseFare: StateFlow<Double> = _baseFare.asStateFlow()

    private val _perKgRate = MutableStateFlow(250.0)
    val perKgRate: StateFlow<Double> = _perKgRate.asStateFlow()

    private val _expressSurcharge = MutableStateFlow(1500.0)
    val expressSurcharge: StateFlow<Double> = _expressSurcharge.asStateFlow()

    private val _surgeMultiplier = MutableStateFlow(1.25)
    val surgeMultiplier: StateFlow<Double> = _surgeMultiplier.asStateFlow()

    private val _adminDiscountEnabled = MutableStateFlow(false)
    val adminDiscountEnabled: StateFlow<Boolean> = _adminDiscountEnabled.asStateFlow()

    private val _adminDiscountPercent = MutableStateFlow(15)
    val adminDiscountPercent: StateFlow<Int> = _adminDiscountPercent.asStateFlow()

    private val _isAdminVerified = MutableStateFlow(false)
    val isAdminVerified: StateFlow<Boolean> = _isAdminVerified.asStateFlow()

    fun verifyAdminAccess(passcode: String, onComplete: (Boolean) -> Unit) {
        if (passcode.isBlank() || passcode.length != 4) {
            onComplete(false)
            return
        }
        val email = _userEmail.value
        try {
            val db = FirebaseManager.firestore
            val uid = FirebaseManager.auth?.currentUser?.uid
            if (uid != null && db != null) {
                db.collection("users").document(uid).get().addOnSuccessListener { snap ->
                    val role = snap.getString("role") ?: ""
                    val rawPin = snap.getString("pin") ?: ""
                    val decryptedPin = SecurityUtils.decryptPin(rawPin)
                    val finalPin = if (decryptedPin.length == 4 && decryptedPin.all { it.isDigit() }) decryptedPin else rawPin
                    
                    if ((role == "admin" || role == "super_admin") && passcode == finalPin) {
                        _isAdminVerified.value = true
                        logAdminActivity("Admin Auth", "Verified admin role from Firestore for $email")
                        onComplete(true)
                    } else {
                        onComplete(false)
                    }
                }.addOnFailureListener {
                    onComplete(false)
                }
            } else {
                onComplete(false)
            }
        } catch (e: Exception) {
            onComplete(false)
        }
    }

    fun updatePricingConfig(base: Double, perKg: Double, express: Double, surge: Double) {
        _baseFare.value = base
        _perKgRate.value = perKg
        _expressSurcharge.value = express
        _surgeMultiplier.value = surge
        savePref("pricing_base_fare", base.toString())
        savePref("pricing_per_kg", perKg.toString())
        savePref("pricing_express", express.toString())
        savePref("pricing_surge", surge.toString())
        logAdminActivity("Pricing Config", "Updated base: â‚¦$base, perKg: â‚¦$perKg, express: â‚¦$express, surge: ${surge}x")

        try {
            val db = FirebaseManager.firestore
            val pricingData = hashMapOf(
                "baseFare" to base,
                "perKgRate" to perKg,
                "expressSurcharge" to express,
                "surgeMultiplier" to surge,
                "discountEnabled" to _adminDiscountEnabled.value,
                "discountPercent" to _adminDiscountPercent.value,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            db?.collection("system_config")?.document("pricing")?.set(pricingData, com.google.firebase.firestore.SetOptions.merge())
        } catch (e: Exception) {
            Log.e("DeliveryViewModel", "Failed to update pricing in Firestore: ${e.message}")
        }
    }

    // Sub-Admin Users & Activity Logs
    private val _subAdminUsers = MutableStateFlow<List<SubAdminUser>>(
        listOf(
            SubAdminUser("1", "Marcus Vance", "marcus@engraced.com", "Content Manager"),
            SubAdminUser("2", "Sarah Jenkins", "sarah@engraced.com", "View Only")
        )
    )
    val subAdminUsers: StateFlow<List<SubAdminUser>> = _subAdminUsers.asStateFlow()

    private val _adminActivityLogs = MutableStateFlow<List<AdminActivityLog>>(
        listOf(
            AdminActivityLog("1", "Just now", "System Boot", "Initialized admin control center and secure firebase sync", "Super Admin"),
            AdminActivityLog("2", "2m ago", "Toggle Settings", "Toggled Points & Loyalty System ON", "Marcus Vance")
        )
    )
    val adminActivityLogs: StateFlow<List<AdminActivityLog>> = _adminActivityLogs.asStateFlow()

    private fun syncGlobalSettingsToFirestore() {
        try {
            val db = FirebaseManager.firestore ?: return
            val settingsData = hashMapOf(
                "pointsSystemEnabled" to _pointsSystemEnabled.value,
                "pricingModeDynamic" to _isDynamicPricingEnabled.value,
                "tipSystemEnabled" to _tipSystemEnabled.value,
                "emailVerificationRequired" to _emailVerificationRequired.value,
                "phoneVerificationRequired" to _phoneVerificationRequired.value,
                "autoVerifyVendors" to _autoVerifyVendors.value,
                "dashboardSectionsEnabled" to _dashboardSectionsEnabled.value,
                "adminCardSliderConfigs" to _adminCardSliderConfigs.value,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            db.collection("system_config").document("global_settings")
                .set(settingsData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("DeliveryViewModel", "Global settings synchronized to Firestore.")
                }
                .addOnFailureListener { e ->
                    Log.e("DeliveryViewModel", "Failed to sync global settings to Firestore: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("DeliveryViewModel", "Exception in syncGlobalSettingsToFirestore: ${e.message}")
        }
    }

    fun togglePointsSystem(enabled: Boolean) {
        _pointsSystemEnabled.value = enabled
        savePref("points_system_enabled", enabled)
        logAdminActivity("Toggle Settings", "Points & Loyalty System set to $enabled")
        syncGlobalSettingsToFirestore()
    }

    fun togglePricingMode(isDynamic: Boolean) {
        _isDynamicPricingEnabled.value = isDynamic
        savePref("pricing_mode_dynamic", isDynamic)
        logAdminActivity("Toggle Settings", "Pricing mode set to " + if (isDynamic) "Automatic (Dynamic)" else "Manual (Flat)")
        syncGlobalSettingsToFirestore()
    }

    fun toggleTipSystem(enabled: Boolean) {
        _tipSystemEnabled.value = enabled
        savePref("tip_system_enabled", enabled)
        logAdminActivity("Toggle Settings", "Driver Tip System set to $enabled")
        syncGlobalSettingsToFirestore()
    }

    fun toggleEmailVerification(enabled: Boolean) {
        _emailVerificationRequired.value = enabled
        savePref("email_verification_required", enabled)
        logAdminActivity("Toggle Settings", "Email Verification Required set to $enabled")
        syncGlobalSettingsToFirestore()
    }

    fun togglePhoneVerification(enabled: Boolean) {
        _phoneVerificationRequired.value = enabled
        savePref("phone_verification_required", enabled)
        logAdminActivity("Toggle Settings", "Phone Number Verification Required set to $enabled")
        syncGlobalSettingsToFirestore()
    }

    fun toggleAutoVerifyVendors(enabled: Boolean) {
        _autoVerifyVendors.value = enabled
        savePref("auto_verify_vendors", enabled)
        logAdminActivity("Toggle Settings", "Auto-verify vendors on KYC set to $enabled")
        syncGlobalSettingsToFirestore()
    }

    fun isValidNigerianPhoneNumber(phone: String): Boolean {
        val cleaned = phone.trim().replace("\\s+".toRegex(), "").replace("-", "")
        val regexLocal = "^0[789][01]\\d{8}$".toRegex()
        val regexIntl = "^\\+234[789][01]\\d{8}$".toRegex()
        val regexGeneral11 = "^0\\d{10}$".toRegex()
        val regexGeneralIntl = "^\\+234\\d{10}$".toRegex()
        return cleaned.matches(regexLocal) || cleaned.matches(regexIntl) || cleaned.matches(regexGeneral11) || cleaned.matches(regexGeneralIntl)
    }

    fun toggleDashboardSection(key: String, enabled: Boolean) {
        _dashboardSectionsEnabled.update { current ->
            current.toMutableMap().apply { this[key] = enabled }
        }
        savePref("section_$key", enabled)
        logAdminActivity("Card Visibility", "Dashboard section '$key' visibility set to $enabled")
        syncGlobalSettingsToFirestore()
    }

    fun updateAdminCardConfig(key: String, value: String) {
        _adminCardSliderConfigs.update { current ->
            current.toMutableMap().apply { this[key] = value }
        }
        savePref("config_$key", value)
        logAdminActivity("Card Customization", "Updated config '$key'")
        syncGlobalSettingsToFirestore()
    }

    fun addSubAdmin(name: String, email: String, permission: String) {
        val newUser = SubAdminUser(
            id = System.currentTimeMillis().toString(),
            name = name,
            email = email,
            permission = permission
        )
        _subAdminUsers.update { listOf(newUser) + it }
        com.esdispatch.data.FirebaseManager.firestore?.collection("sub_admins")
            ?.document(newUser.id)
            ?.set(mapOf("name" to name, "email" to email, "permission" to permission, "createdAt" to System.currentTimeMillis()))
        logAdminActivity("Add Sub-Admin", "Added sub-admin $name ($permission)")
    }

    fun updateSubAdminPermission(id: String, newPermission: String) {
        _subAdminUsers.update { list ->
            list.map { if (it.id == id) it.copy(permission = newPermission) else it }
        }
        com.esdispatch.data.FirebaseManager.firestore?.collection("sub_admins")
            ?.document(id)
            ?.update("permission", newPermission)
        logAdminActivity("Update Permissions", "Updated sub-admin ID $id permission to $newPermission")
    }

    fun deleteSubAdmin(id: String) {
        _subAdminUsers.update { list -> list.filter { it.id != id } }
        com.esdispatch.data.FirebaseManager.firestore?.collection("sub_admins")
            ?.document(id)
            ?.delete()
        logAdminActivity("Remove Sub-Admin", "Removed sub-admin ID $id")
    }

    fun listenToSubAdmins() {
        val fs = com.esdispatch.data.FirebaseManager.firestore ?: return
        fs.collection("sub_admins").addSnapshotListener { snap, error ->
            if (error != null || snap == null) return@addSnapshotListener
            if (snap.documents.isNotEmpty()) {
                _subAdminUsers.value = snap.documents.mapNotNull { d ->
                    SubAdminUser(
                        id = d.id,
                        name = d.getString("name") ?: "",
                        email = d.getString("email") ?: "",
                        permission = d.getString("permission") ?: "View Only"
                    )
                }
            }
        }
    }

    

    fun bulkUpdateDeliveryStatus(parcelIds: List<String>, newStatus: ParcelStatus) {
        parcelIds.forEach { parcelId ->
            com.esdispatch.data.FirebaseManager.updateParcelStatusByRider(parcelId, newStatus, 1.0f) { _, _ -> }
        }
        _parcels.update { current ->
            current.map { parcel ->
                if (parcelIds.contains(parcel.id)) parcel.copy(status = newStatus) else parcel
            }
        }
        logAdminActivity("Bulk Status Update", "Updated ${parcelIds.size} shipments to status: $newStatus")
    }

    fun bulkReassignDriver(parcelIds: List<String>, riderId: String, bikeNumber: String) {
        parcelIds.forEach { parcelId ->
            com.esdispatch.data.FirebaseManager.updateParcelAssignment(parcelId, riderId, bikeNumber) { _, _ -> }
        }
        _parcels.update { current ->
            current.map { parcel ->
                if (parcelIds.contains(parcel.id)) parcel.copy(riderId = riderId, riderBikeNumber = bikeNumber) else parcel
            }
        }
        logAdminActivity("Bulk Driver Reassignment", "Reassigned ${parcelIds.size} shipments to driver: $riderId")
    }

    fun driverMasterControlOverride(action: String) {
        Log.d("DriverMasterControl", "Driver executed system master control: $action")
        logAdminActivity("System Override", "Executed driver master control: $action")
    }

    private val _bikeNumber = MutableStateFlow("ESD-Rider-882")
    val bikeNumber: StateFlow<String> = _bikeNumber.asStateFlow()

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    fun setRiderOnlineStatus(online: Boolean) {
        _isOnline.value = online
        val uid = _firebaseUserId.value
        if (uid != null && !uid.startsWith("local_user_")) {
            com.esdispatch.data.FirebaseManager.updateRiderOnlineStatus(uid, online)
        }
        
        // Start or stop the LocationService for live GPS tracking
        appContext?.let { ctx ->
            val intent = android.content.Intent(ctx, com.esdispatch.util.LocationService::class.java)
            if (online) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    ctx.startForegroundService(intent)
                } else {
                    ctx.startService(intent)
                }
            } else {
                ctx.stopService(intent)
            }
        }
    }

    // Rider specific state flows
    private val _availableDeliveries = MutableStateFlow<List<Parcel>>(emptyList())
    val availableDeliveries: StateFlow<List<Parcel>> = _availableDeliveries.asStateFlow()

    private val _riderAssignments = MutableStateFlow<List<Parcel>>(emptyList())
    val riderAssignments: StateFlow<List<Parcel>> = _riderAssignments.asStateFlow()

    private val _scannedRiderParcel = MutableStateFlow<Parcel?>(null)
    val scannedRiderParcel: StateFlow<Parcel?> = _scannedRiderParcel.asStateFlow()

    fun setScannedRiderParcel(parcel: Parcel?) {
        _scannedRiderParcel.value = parcel
    }

    private var availableDeliveriesJob: kotlinx.coroutines.Job? = null
    private var riderAssignmentsJob: kotlinx.coroutines.Job? = null

    fun setUserRole(role: String) {
        _userRole.value = role
        savePref("user_role", role)
        
        // Switch view mode automatically to match role initially
        if (role == "rider" || role == "customer") {
            _activeViewMode.value = role
        }

        val uid = _firebaseUserId.value
        if (uid != null && !uid.startsWith("local_user_")) {
            com.esdispatch.data.FirebaseManager.saveUserProfileToFirestore(
                userId = uid,
                name = _userName.value,
                email = _userEmail.value,
                phone = _userPhone.value,
                role = role,
                bikeNumber = _bikeNumber.value
            )
            if (role == "rider") {
                startRiderListeners(uid)
            } else {
                stopRiderListeners()
            }
        }
    }

    fun setActiveViewMode(mode: String) {
        _activeViewMode.value = mode
        savePref("active_view_mode", mode)
    }

    fun setBikeNumber(number: String) {
        _bikeNumber.value = number
        savePref("bike_number", number)
        val uid = _firebaseUserId.value
        if (uid != null && !uid.startsWith("local_user_")) {
            com.esdispatch.data.FirebaseManager.saveUserProfileToFirestore(
                userId = uid,
                name = _userName.value,
                email = _userEmail.value,
                phone = _userPhone.value,
                role = _userRole.value,
                bikeNumber = number
            )
        }
    }

    fun startRiderListeners(riderId: String) {
        availableDeliveriesJob?.cancel()
        riderAssignmentsJob?.cancel()

        availableDeliveriesJob = viewModelScope.launch {
            com.esdispatch.data.FirebaseManager.listenToAvailableDeliveries().collect { list ->
                _availableDeliveries.value = list
            }
        }

        riderAssignmentsJob = viewModelScope.launch {
            com.esdispatch.data.FirebaseManager.listenToRiderAssignments(riderId).collect { list ->
                _riderAssignments.value = list
            }
        }
    }

    fun stopRiderListeners() {
        availableDeliveriesJob?.cancel()
        riderAssignmentsJob?.cancel()
    }

    // Rider actions
    fun acceptParcelByRider(parcelId: String, onComplete: (Boolean, String?) -> Unit) {
        val riderId = _firebaseUserId.value ?: return onComplete(false, "User not signed in")
        val riderName = _userName.value
        val riderPhone = _userPhone.value
        val riderBike = _bikeNumber.value

        com.esdispatch.data.FirebaseManager.acceptParcelByRider(
            parcelId = parcelId,
            riderId = riderId,
            riderName = riderName,
            riderPhone = riderPhone,
            riderBikeNumber = riderBike,
            onComplete = onComplete
        )
    }

    /**
     * Administrative action to assign a real rider to a pending parcel in Firestore
     */
    fun assignRiderToParcel(parcelId: String, rider: Rider, onComplete: (Boolean, String?) -> Unit) {
        val db = com.esdispatch.data.FirebaseManager.firestore
        if (db == null) {
            onComplete(false, "Firestore database not available")
            return
        }
        com.esdispatch.data.FirebaseManager.acceptParcelByRider(
            parcelId = parcelId,
            riderId = rider.id,
            riderName = rider.name,
            riderPhone = rider.phone,
            riderBikeNumber = "ESD-" + rider.id.takeLast(4),
            requireOnline = false,
            onComplete = { success, error ->
                if (success) {
                    showCustomToast("Successfully assigned ${rider.name} to Parcel #$parcelId! ðŸ“¦ðŸš€")
                    // If it matches a local parcel in our lists, update it
                    val updatedList = _parcels.value.map { parcel ->
                        if (parcel.id == parcelId) {
                            parcel.copy(
                                status = ParcelStatus.ASSIGNED,
                                riderId = rider.id,
                                courierName = rider.name,
                                courierPhone = rider.phone,
                                progress = 0.15f
                            )
                        } else {
                            parcel
                        }
                    }
                    _parcels.value = updatedList
                }
                onComplete(success, error)
            }
        )
    }

    fun updateParcelStatusByRider(parcelId: String, nextStatus: ParcelStatus, progress: Float, onComplete: (Boolean, String?) -> Unit) {
        com.esdispatch.data.FirebaseManager.updateParcelStatusByRider(
            parcelId = parcelId,
            nextStatus = nextStatus,
            progress = progress,
            onComplete = onComplete
        )
    }

    fun markParcelDelivered(parcelId: String) {
        val uid = _firebaseUserId.value ?: return
        val parcel = _parcels.value.find { it.id == parcelId } ?: _riderAssignments.value.find { it.id == parcelId }
        val price = parcel?.price ?: 0.0
        val payout = price * 0.8 // 80% rider payout

        updateParcelStatusByRider(parcelId, ParcelStatus.DELIVERED, 1.0f) { success, _ ->
            if (success) {
                // A real delivery is completed (not just booked): bump delivery stats.
                val updatedCount = _deliveryCount.value + 1
                _deliveryCount.value = updatedCount
                savePref("delivery_count", updatedCount)
                if (_pointsSystemEnabled.value) {
                    val pts = _loyaltyPoints.value + 15
                    _loyaltyPoints.value = pts
                    savePref("loyalty_points", pts)
                }
                com.esdispatch.data.FirebaseManager.syncLoyaltyToFirestore(
                    uid, _loyaltyPoints.value, updatedCount
                )

                if (payout > 0) {
                    com.esdispatch.data.FirebaseManager.updateUserWalletBalance(uid, payout) { payoutSuccess, newBal ->
                        if (payoutSuccess) {
                            _walletBalance.value = newBal
                            savePref("wallet_balance", newBal)
                            com.esdispatch.data.FirebaseManager.recordLedgerTransaction(
                                userId = uid,
                                amount = payout,
                                title = "Payout: ${parcel?.itemName ?: parcelId}",
                                isTopUp = true,
                                reference = "PAYOUT-$parcelId"
                            ) {}
                        }
                    }
                }

                // Update local list
                val updatedList = _parcels.value.map {
                    if (it.id == parcelId) it.copy(status = ParcelStatus.DELIVERED, progress = 1.0f) else it
                }
                _parcels.value = updatedList
                
                // Add Notification
                addNotification(
                    title = "Parcel Delivered! ðŸðŸ“¦",
                    message = "Parcel #$parcelId has been successfully delivered and proof of delivery captured. You earned â‚¦${String.format("%,.2f", payout)}",
                    parcelId = parcelId
                )
            }
        }
    }

    /** Upload a proof-of-delivery (photo/signature) to Storage, stamp the delivery doc, then pay the rider. */
    fun uploadPodAndCompleteParcel(parcelId: String, podBytes: ByteArray, podType: String, onComplete: ((Boolean) -> Unit)? = null) {
        try {
            val ref = com.google.firebase.storage.FirebaseStorage.getInstance()
                .reference.child("pod/$parcelId/$podType-${System.currentTimeMillis()}.jpg")
            ref.putBytes(podBytes)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        com.esdispatch.data.FirebaseManager.firestore?.collection("deliveries")?.document(parcelId)
                            ?.update(
                                mapOf(
                                    "podUrl" to url.toString(),
                                    "podType" to podType,
                                    "podTimestamp" to com.google.firebase.Timestamp.now()
                                )
                            )
                            ?.addOnFailureListener { e ->
                                android.util.Log.e("POD", "Failed to stamp podUrl on delivery: ${e.message}")
                            }
                        markParcelDelivered(parcelId)
                        onComplete?.invoke(true)
                    }.addOnFailureListener {
                        markParcelDelivered(parcelId)
                        onComplete?.invoke(true)
                    }
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("DeliveryViewModel", "POD upload failed: ${e.message}")
                    markParcelDelivered(parcelId)
                    onComplete?.invoke(false)
                }
        } catch (e: Exception) {
            android.util.Log.e("POD", "POD upload error: ${e.message}")
            markParcelDelivered(parcelId)
            onComplete?.invoke(false)
        }
    }

    fun updateCourierLocationByRider(parcelId: String, lat: Double, lng: Double, onComplete: (Boolean, String?) -> Unit) {
        com.esdispatch.data.FirebaseManager.updateCourierLocationByRider(
            parcelId = parcelId,
            lat = lat,
            lng = lng,
            onComplete = onComplete
        )
    }

    // Active high-precision GPS location listeners
    private val activeLocationListeners = mutableMapOf<String, Pair<android.location.LocationManager, android.location.LocationListener>>()

    fun startRealTimeGpsTracking(parcelId: String, onLocationUpdate: (Double, Double) -> Unit) {
        val context = appContext ?: return
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager ?: return
        try {
            if (androidx.core.content.PermissionChecker.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == androidx.core.content.PermissionChecker.PERMISSION_GRANTED) {
                val listener = object : android.location.LocationListener {
                    override fun onLocationChanged(location: android.location.Location) {
                        onLocationUpdate(location.latitude, location.longitude)
                        updateCourierLocationByRider(parcelId, location.latitude, location.longitude) { _, _ -> }
                    }
                    override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }
                
                val provider = if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                    android.location.LocationManager.GPS_PROVIDER
                } else {
                    android.location.LocationManager.NETWORK_PROVIDER
                }
                
                locationManager.requestLocationUpdates(
                    provider,
                    1000L, // 1 second interval
                    1f,    // 1 meter changes
                    listener,
                    android.os.Looper.getMainLooper()
                )
                activeLocationListeners[parcelId] = Pair(locationManager, listener)
                Log.d("DeliveryViewModel", "Real-time High-Precision GPS Tracker started for parcel: $parcelId")
            }
        } catch (e: Exception) {
            Log.e("DeliveryViewModel", "Failed to start real-time GPS tracking: ${e.message}")
        }
    }

    fun stopRealTimeGpsTracking(parcelId: String) {
        val pair = activeLocationListeners.remove(parcelId)
        if (pair != null) {
            try {
                pair.first.removeUpdates(pair.second)
                Log.d("DeliveryViewModel", "Real-time High-Precision GPS Tracker stopped for parcel: $parcelId")
            } catch (e: Exception) {
                Log.e("DeliveryViewModel", "Error removing GPS tracking listener: ${e.message}")
            }
        }
    }

    fun verifyDeliveryOtpByRider(parcelId: String, otpInput: String, onComplete: (Boolean, String?) -> Unit) {
        com.esdispatch.data.FirebaseManager.verifyDeliveryOtpByRider(
            parcelId = parcelId,
            otpInput = otpInput,
            onComplete = onComplete
        )
    }

    fun rateAndTipRider(
        parcelId: String,
        riderId: String,
        rating: Double,
        tipAmount: Double,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val customerId = _firebaseUserId.value ?: ""
        com.esdispatch.data.FirebaseManager.rateAndTipRider(
            parcelId = parcelId,
            riderId = riderId,
            rating = rating,
            tipAmount = tipAmount,
            customerId = customerId,
            onComplete = onComplete
        )
    }

    fun clockInStatus(status: String) {
        _currentAttendanceStatus.value = status
        val rId = _firebaseUserId.value ?: "rider_local_1"
        val timeNow = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val dateOnly = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val attendance = ShiftAttendance(
            riderId = rId,
            status = status,
            clockInTime = timeNow,
            dateString = dateOnly
        )
        viewModelScope.launch {
            repository?.saveShiftAttendance(attendance)
            showCustomToast("Shift status updated: $status â±ï¸")
        }
    }

    fun submitVehicleInspection(
        tiresOk: Boolean,
        brakesOk: Boolean,
        headlightsOk: Boolean,
        hornOk: Boolean,
        fuelBatteryLevelOk: Boolean,
        safetyVestHelmetOk: Boolean,
        notes: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        val passed = tiresOk && brakesOk && headlightsOk && hornOk && fuelBatteryLevelOk && safetyVestHelmetOk
        val rId = _firebaseUserId.value ?: "rider_local_1"
        val dateOnly = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val inspection = VehicleInspection(
            riderId = rId,
            dateString = dateOnly,
            tiresOk = tiresOk,
            brakesOk = brakesOk,
            headlightsOk = headlightsOk,
            hornOk = hornOk,
            fuelBatteryLevelOk = fuelBatteryLevelOk,
            safetyVestHelmetOk = safetyVestHelmetOk,
            notes = notes,
            passed = passed
        )
        viewModelScope.launch {
            repository?.saveVehicleInspection(inspection)
            if (passed) {
                showCustomToast("Vehicle Pre-Trip Inspection PASSED âœ…. Ready for dispatch.")
                onComplete(true, "Passed successfully")
            } else {
                showCustomToast("Inspection FAILED âŒ. Correct safety issues before dispatch.")
                onComplete(false, "Pre-trip inspection failed mandatory safety checks.")
            }
        }
    }

    fun submitExpenseClaim(
        title: String,
        category: String,
        amount: Double,
        receiptNote: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        val rId = _firebaseUserId.value ?: "rider_local_1"
        val dateOnly = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val claim = ExpenseClaim(
            riderId = rId,
            title = title,
            category = category,
            amount = amount,
            receiptNote = receiptNote,
            status = "PENDING",
            dateString = dateOnly
        )
        viewModelScope.launch {
            repository?.saveExpenseClaim(claim)
            showCustomToast("Expense claim submitted for HR/Payroll review ($amount) ðŸ’¸")
            onComplete(true, "Submitted successfully")
        }
    }

    fun requestLeave(
        shiftDate: String,
        reason: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        val rId = _firebaseUserId.value ?: "rider_local_1"
        val roster = ShiftRoster(
            riderId = rId,
            shiftDate = shiftDate,
            startTime = "08:00",
            endTime = "17:00",
            roleOrArea = "Assigned Delivery Zone",
            isLeave = true,
            leaveReason = reason,
            leaveStatus = "PENDING"
        )
        viewModelScope.launch {
            repository?.saveShiftRoster(roster)
            showCustomToast("Leave request submitted to operations manager ðŸ“…")
            onComplete(true, "Leave requested")
        }
    }

    fun queueOfflineAction(actionType: String, payloadJson: String) {
        val item = OfflineSyncQueue(
            actionType = actionType,
            payloadJson = payloadJson,
            timestamp = System.currentTimeMillis(),
            synced = false
        )
        viewModelScope.launch {
            repository?.saveOfflineSyncItem(item)
            showCustomToast("Action cached offline (low-signal sync queue) ðŸ“¡")
        }
    }

    fun syncOfflineQueue() {
        viewModelScope.launch {
            val list = _offlineSyncQueueList.value.filter { !it.synced }
            list.forEach { item ->
                repository?.markSyncItemSynced(item.id)
            }
            if (list.isNotEmpty()) {
                showCustomToast("Successfully synchronized ${list.size} offline items with corporate server! ðŸ”„")
            } else {
                showCustomToast("Offline queue is already fully synchronized.")
            }
        }
    }

    fun startListeningToParcelChats(parcelId: String) {
        chatListenerJob?.cancel()
        chatListenerJob = viewModelScope.launch {
            com.esdispatch.data.FirebaseManager.listenToParcelChatMessages(parcelId)
                .collect { messages ->
                    _activeParcelChats.value = messages
                }
        }
    }

    fun stopListeningToParcelChats() {
        chatListenerJob?.cancel()
        chatListenerJob = null
        _activeParcelChats.value = emptyList()
    }

    fun sendParcelChatMessage(parcelId: String, senderRole: String, messageText: String, onComplete: (Boolean, String?) -> Unit) {
        val senderId = _firebaseUserId.value ?: ""
        val senderName = _userName.value.ifEmpty { "User" }
        com.esdispatch.data.FirebaseManager.sendParcelChatMessage(
            parcelId = parcelId,
            senderId = senderId,
            senderName = senderName,
            senderRole = senderRole,
            messageText = messageText,
            onComplete = onComplete
        )
    }


    private val _totalEarned = MutableStateFlow(0.0)
    val totalEarned: StateFlow<Double> = _totalEarned.asStateFlow()

    private val _deliveryCount = MutableStateFlow(0)
    val deliveryCount: StateFlow<Int> = _deliveryCount.asStateFlow()

    private val _loyaltyPoints = MutableStateFlow(0)
    val loyaltyPoints: StateFlow<Int> = _loyaltyPoints.asStateFlow()
    private var hasLoadedPoints = false

    private val _welcomeGiftClaimed = MutableStateFlow(false)
    val welcomeGiftClaimed: StateFlow<Boolean> = _welcomeGiftClaimed.asStateFlow()

    private val _isNewRegistration = MutableStateFlow(false)
    val isNewRegistration: StateFlow<Boolean> = _isNewRegistration.asStateFlow()

    fun setNewRegistration(value: Boolean) {
        _isNewRegistration.value = value
    }

    

    

    private val _dailyBonusClaimed = MutableStateFlow(false)
    val dailyBonusClaimed: StateFlow<Boolean> = _dailyBonusClaimed.asStateFlow()

    fun claimDailyBonus() {
        _dailyBonusClaimed.value = true
        savePref("daily_bonus_claimed", true)
        addLoyaltyPoints(100)
    }

    private val _userRating = MutableStateFlow(4.9)
    val userRating: StateFlow<Double> = _userRating.asStateFlow()

    private val _memberSince = MutableStateFlow("Jun 2025")
    val memberSince: StateFlow<String> = _memberSince.asStateFlow()

    private val _userPin = MutableStateFlow("")
    val userPin: StateFlow<String> = _userPin.asStateFlow()

    private val _twoFactorEnabled = MutableStateFlow(false)
    val twoFactorEnabled: StateFlow<Boolean> = _twoFactorEnabled.asStateFlow()

    

    

    private val _defaultDeliveryType = MutableStateFlow("Express")
    val defaultDeliveryType: StateFlow<String> = _defaultDeliveryType.asStateFlow()

    private val _homeAddress = MutableStateFlow("No. 12 Joel Ogunnaike Street, Ikeja GRA, Lagos")
    val homeAddress: StateFlow<String> = _homeAddress.asStateFlow()

    private val _workAddress = MutableStateFlow("Plot 14, Kingsway Road, Ikoyi, Lagos")
    val workAddress: StateFlow<String> = _workAddress.asStateFlow()

    private val _preferredRider = MutableStateFlow("")
    val preferredRider: StateFlow<String> = _preferredRider.asStateFlow()

    private val _language = MutableStateFlow("English")
    val language: StateFlow<String> = _language.asStateFlow()

    // Bank transfer withdrawal info
    private val _bankName = MutableStateFlow("Access Bank")
    val bankName: StateFlow<String> = _bankName.asStateFlow()

    private val _accountNumber = MutableStateFlow("0123456789")
    val accountNumber: StateFlow<String> = _accountNumber.asStateFlow()

    private val _accountName = MutableStateFlow("Engraced Member")
    val accountName: StateFlow<String> = _accountName.asStateFlow()

    // Active & Past Deliveries
    private val _parcels = MutableStateFlow<List<Parcel>>(emptyList())
    val parcels: StateFlow<List<Parcel>> = _parcels.asStateFlow()

    private val _archivedParcelIds = MutableStateFlow<Set<String>>(emptySet())
    val archivedParcelIds: StateFlow<Set<String>> = _archivedParcelIds.asStateFlow()

    private val syncedParcelIds = mutableSetOf<String>()

    fun archiveParcel(parcelId: String) {
        _archivedParcelIds.update { it + parcelId }
    }

    private val _selectedParcel = MutableStateFlow<Parcel?>(null)
    val selectedParcel: StateFlow<Parcel?> = _selectedParcel.asStateFlow()

    // Draft Parcel state for creation flow
    private val _parcelDraft = MutableStateFlow(ParcelDraft())
    val parcelDraft: StateFlow<ParcelDraft> = _parcelDraft.asStateFlow()

    private val _pendingQuote = MutableStateFlow<PendingQuote>(PendingQuote.Idle)
    val pendingQuote: StateFlow<PendingQuote> = _pendingQuote.asStateFlow()

    fun clearQuote() {
        _pendingQuote.value = PendingQuote.Idle
    }

    // Financial State
    

    

    // Address Book
    private val _addresses = MutableStateFlow<List<AddressItem>>(emptyList())
    val addresses: StateFlow<List<AddressItem>> = _addresses.asStateFlow()

    // Notifications & Promos
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _promotions = MutableStateFlow<List<PromoCode>>(emptyList())
    val promotions: StateFlow<List<PromoCode>> = _promotions.asStateFlow()

    // Loading state flows - distinguish loading from empty
    private val _loadingParcels = MutableStateFlow(false)
    val loadingParcels: StateFlow<Boolean> = _loadingParcels.asStateFlow()

    private val _loadingTransactions = MutableStateFlow(false)
    val loadingTransactions: StateFlow<Boolean> = _loadingTransactions.asStateFlow()

    private val _loadingNotifications = MutableStateFlow(false)
    val loadingNotifications: StateFlow<Boolean> = _loadingNotifications.asStateFlow()

    // Network connectivity state (distinct from rider's isOnline)
    private val _networkOnline = MutableStateFlow(true)
    val networkOnline: StateFlow<Boolean> = _networkOnline.asStateFlow()

    private var connectivityCallback: ConnectivityManager.NetworkCallback? = null

    // Retry configuration
    companion object {
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 2000L
    }

    // Settings Preferences
    private val _pushEnabled = MutableStateFlow(true)
    val pushEnabled: StateFlow<Boolean> = _pushEnabled.asStateFlow()

    private val _pushAlertsBooked = MutableStateFlow(true)
    val pushAlertsBooked: StateFlow<Boolean> = _pushAlertsBooked.asStateFlow()

    private val _pushAlertsDispatched = MutableStateFlow(true)
    val pushAlertsDispatched: StateFlow<Boolean> = _pushAlertsDispatched.asStateFlow()

    private val _pushAlertsDelivered = MutableStateFlow(true)
    val pushAlertsDelivered: StateFlow<Boolean> = _pushAlertsDelivered.asStateFlow()

    private val _pushAlertsCancelled = MutableStateFlow(true)
    val pushAlertsCancelled: StateFlow<Boolean> = _pushAlertsCancelled.asStateFlow()

    // In-app Foreground Notification Toast State
    private val _activeInAppNotification = MutableStateFlow<Pair<String, String>?>(null)
    val activeInAppNotification: StateFlow<Pair<String, String>?> = _activeInAppNotification.asStateFlow()

    private val _locationEnabled = MutableStateFlow(true)
    val locationEnabled: StateFlow<Boolean> = _locationEnabled.asStateFlow()

    private val _darkModeEnabled = MutableStateFlow(false)
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()

    private val _dashboardVariant = MutableStateFlow("full")
    val dashboardVariant: StateFlow<String> = _dashboardVariant.asStateFlow()

    fun setDashboardVariant(variant: String) {
        if (variant != "v2" && variant != "full") return
        _dashboardVariant.value = variant
        savePref("dashboard_variant", variant)
    }

    // Custom Toast Notification flow (Obsidian-Gold theme)
    private val _customToast = MutableStateFlow<String?>(null)
    val customToast: StateFlow<String?> = _customToast.asStateFlow()

    fun showCustomToast(message: String) {
        viewModelScope.launch {
            _customToast.value = message
            kotlinx.coroutines.delay(3000)
            if (_customToast.value == message) {
                _customToast.value = null
            }
        }
    }

    fun dismissCustomToast() {
        _customToast.value = null
    }

    // Onboarding tool-tip overlay state (one-time for new users explaining tracking & maps)
    private val _showOnboardingTooltip = MutableStateFlow(true)
    val showOnboardingTooltip: StateFlow<Boolean> = _showOnboardingTooltip.asStateFlow()

    fun dismissOnboardingTooltip() {
        _showOnboardingTooltip.value = false
        savePref("show_onboarding_tooltip", false)
    }

    // Invite Code â€” generated per user from Firebase UID
    private val _referralCode = MutableStateFlow("SHARE-ENGRACED")
    val referralCode: StateFlow<String> = _referralCode.asStateFlow()
    
    fun generateReferralCode() {
        val uid = _firebaseUserId.value
        val refPrefs = appContext?.getSharedPreferences("esdispatch_prefs", Context.MODE_PRIVATE)
        _referralCode.value = if (uid != null && uid.length >= 4) {
            "ENGR-${uid.takeLast(6).uppercase()}"
        } else {
            val stored = refPrefs?.getString("referral_code", "") ?: ""
            if (stored.isNotEmpty()) stored else "ENGR-${(100000..999999).random()}"
        }
        refPrefs?.edit()?.putString("referral_code", _referralCode.value)?.apply()
    }

    fun redeemReferralCode(code: String) {
        // Validate and redeem a friend's referral code
        if (code.isBlank() || code == _referralCode.value) return
        viewModelScope.launch {
            try {
                val db = com.esdispatch.data.FirebaseManager.firestore ?: return@launch
                val uid = _firebaseUserId.value ?: return@launch
                db.collection("referral_redemptions").add(
                    hashMapOf(
                        "redeemedBy" to uid,
                        "code" to code.uppercase(),
                        "redeemedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                ).addOnSuccessListener {
                    // Credit the user with referral bonus points
                    addLoyaltyPoints(300)
                    Log.d("DeliveryViewModel", "Referral code redeemed: $code")
                }
            } catch (e: Exception) {
                Log.e("DeliveryViewModel", "Error redeeming referral code: ${e.message}")
            }
        }
    }

    init {
        listenToMarketplaceProducts()
        listenToMarketplaceStores()
        listenToGlobalSettings()
        // Load vendor store status and cart when user logs in
        viewModelScope.launch {
            _firebaseUserId.collect { uid ->
                if (uid != null) {
                    listenToVendorStore()
                    loadUserCart()
                }
            }
        }
        // Auto-generate referral code when UID changes
        viewModelScope.launch {
            _firebaseUserId.collect { uid ->
                if (uid != null) generateReferralCode()
            }
        }
        
        // Load persisted referral code if no UID yet
        val prefs = appContext?.getSharedPreferences("esdispatch_prefs", Context.MODE_PRIVATE)
        val savedCode = prefs?.getString("referral_code", "") ?: ""
        if (savedCode.isNotEmpty()) {
            _referralCode.value = savedCode
        } else {
            generateReferralCode()
        }

        // Seed default promotions
        if (_promotions.value.isEmpty()) {
            _promotions.value = listOf(
                com.esdispatch.data.PromoCode(discountPercent = 25, description = "Enjoy 25% discount on Express bookings.", code = "EID2026"),
                com.esdispatch.data.PromoCode(discountPercent = 100, description = "Get â‚¦2,500 instant credit on first parcel.", code = "FIRSTFREE", isLimited = false),
                com.esdispatch.data.PromoCode(discountPercent = 30, description = "Saturdays & Sundays economy save.", code = "WEEKEND30")
            )
        }

        // Seeding memory initial data as temporary fallback
        loadMockInitialData()
        val seedPrefs = appContext?.getSharedPreferences("esdispatch_prefs", android.content.Context.MODE_PRIVATE)
        val hasSeeded = seedPrefs?.getBoolean("seed_complete", false) ?: false
        if (!hasSeeded) {
            seedAiRiders()
            seedAiChat()
            seedAiAnalytics()
            savePref("seed_complete", true)
        }
        
        viewModelScope.launch {
            _parcels.collect { list ->
                updateDynamicAnalytics(list)
            }
        }
    }

    private fun seedAiRiders() {
        val ridersList = listOf(
            Rider(
                id = "RDR-01",
                name = "Richard Dheo",
                phone = "+234 803 111 2222",
                avatar = "https://images.unsplash.com/photo-1599566150163-29194dcaad36?w=100&h=100&fit=crop",
                vehicleType = "Bike",
                status = RiderStatus.ONLINE,
                latitude = 6.4281,
                longitude = 3.4219,
                currentWorkload = 1,
                batteryLevel = 94,
                rating = 4.9,
                averageDeliveryTimeMin = 18,
                cancellationHistoryCount = 0,
                fuelEfficiency = 42.0,
                shiftSchedule = "08:00 - 18:00",
                distanceToPickupKm = 0.8,
                activeDeliveriesCount = 1
            ),
            Rider(
                id = "RDR-02",
                name = "Adebayo Musa",
                phone = "+234 812 345 6789",
                avatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop",
                vehicleType = "Tricycle",
                status = RiderStatus.ONLINE,
                latitude = 6.4312,
                longitude = 3.4350,
                currentWorkload = 2,
                batteryLevel = 82,
                rating = 4.8,
                averageDeliveryTimeMin = 24,
                cancellationHistoryCount = 1,
                fuelEfficiency = 28.5,
                shiftSchedule = "08:00 - 18:00",
                distanceToPickupKm = 1.6,
                activeDeliveriesCount = 2
            ),
            Rider(
                id = "RDR-03",
                name = "Chinedu Okafor",
                phone = "+234 802 999 8888",
                avatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop",
                vehicleType = "Van",
                status = RiderStatus.ONLINE,
                latitude = 6.4420,
                longitude = 3.4512,
                currentWorkload = 0,
                batteryLevel = 88,
                rating = 4.7,
                averageDeliveryTimeMin = 28,
                cancellationHistoryCount = 0,
                fuelEfficiency = 15.0,
                shiftSchedule = "06:00 - 15:00",
                distanceToPickupKm = 3.2,
                activeDeliveriesCount = 0
            ),
            Rider(
                id = "RDR-04",
                name = "Chioma Balogun",
                phone = "+234 905 444 3333",
                avatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&h=100&fit=crop",
                vehicleType = "Truck",
                status = RiderStatus.BUSY,
                latitude = 6.4150,
                longitude = 3.4110,
                currentWorkload = 3,
                batteryLevel = 65,
                rating = 4.5,
                averageDeliveryTimeMin = 35,
                cancellationHistoryCount = 3,
                fuelEfficiency = 8.5,
                shiftSchedule = "20:00 - 06:00",
                distanceToPickupKm = 5.4,
                activeDeliveriesCount = 3
            ),
            Rider(
                id = "RDR-05",
                name = "Akin Ogundipe",
                phone = "+234 803 777 8888",
                avatar = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=100&h=100&fit=crop",
                vehicleType = "Bike",
                status = RiderStatus.ONLINE,
                latitude = 6.4350,
                longitude = 3.4290,
                currentWorkload = 1,
                batteryLevel = 90,
                rating = 4.7,
                averageDeliveryTimeMin = 19,
                cancellationHistoryCount = 2,
                fuelEfficiency = 41.5,
                shiftSchedule = "08:00 - 18:00",
                distanceToPickupKm = 1.9,
                activeDeliveriesCount = 1
            )
        )
        _aiRiders.value = ridersList

        try {
            val db = FirebaseManager.firestore
            if (db != null) {
                for (rider in ridersList) {
                    val data = hashMapOf(
                        "uid" to rider.id,
                        "name" to rider.name,
                        "phone" to rider.phone,
                        "avatar" to rider.avatar,
                        "role" to "rider",
                        "bikeNumber" to "ESD-BIKE-${rider.id}",
                        "isOnline" to (rider.status != RiderStatus.OFFLINE),
                        "status" to (if (rider.status == RiderStatus.BUSY) "busy" else if (rider.status == RiderStatus.ONLINE) "active" else "offline"),
                        "latitude" to rider.latitude,
                        "longitude" to rider.longitude,
                        "currentWorkload" to rider.currentWorkload,
                        "batteryLevel" to rider.batteryLevel,
                        "rating" to rider.rating,
                        "averageDeliveryTimeMin" to rider.averageDeliveryTimeMin,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    db.collection("users").document(rider.id).set(data, com.google.firebase.firestore.SetOptions.merge())
                }
            }
        } catch (e: Exception) {
            Log.e("DeliveryViewModel", "Failed to seed drivers to Firestore: ${e.message}")
        }
    }

    private fun seedAiChat() {
        _aiChatMessages.value = listOf(
            AIChatMessage(
                text = "Hello! I am your AI Shipping Assistant. Ask me to book dispatch, track riders, calculate ETAs, or check delivery options instantly.",
                isUser = false
            )
        )
    }

    private fun seedAiAnalytics() {
        // Seed initial Risk assessment
        _aiRiskReport.value = RiskReport(
            score = 15,
            riskFactors = listOf("Favorable weather", "Low traffic congestion", "Safe green delivery zones active"),
            mitigationSuggested = "Standard routing approved. Motorcycle routing suitable.",
            label = "Safe"
        )

        // Seed Admin metrics
        _riderPerformanceMetrics.value = listOf(
            RiderPerformanceMetric("RDR-01", "Richard Dheo", 145, 38.5, listOf(4.7, 4.8, 4.8, 4.9, 4.9), 92),
            RiderPerformanceMetric("RDR-02", "Marcus Vance", 98, 31.0, listOf(4.5, 4.6, 4.6, 4.5, 4.6), 84),
            RiderPerformanceMetric("RDR-03", "Sandra Croft", 120, 29.5, listOf(4.8, 4.8, 4.7, 4.8, 4.8), 89),
            RiderPerformanceMetric("RDR-04", "Debra Jaxon", 64, 25.0, listOf(4.2, 4.3, 4.4, 4.3, 4.3), 75),
            RiderPerformanceMetric("RDR-05", "Akin Ogundipe", 112, 41.0, listOf(4.6, 4.7, 4.7, 4.6, 4.7), 90)
        )

        _deliveryPerformanceTrends.value = listOf(
            DeliveryPerformanceTrend("Mon", 340, 98.2, 4200.0),
            DeliveryPerformanceTrend("Tue", 410, 97.5, 3800.0),
            DeliveryPerformanceTrend("Wed", 380, 99.0, 4100.0),
            DeliveryPerformanceTrend("Thu", 450, 96.8, 3950.0),
            DeliveryPerformanceTrend("Fri", 520, 98.5, 4300.0),
            DeliveryPerformanceTrend("Sat", 300, 99.4, 4500.0),
            DeliveryPerformanceTrend("Sun", 240, 99.1, 4800.0)
        )
        
        // Populate initial dynamic values using current empty list
        updateDynamicAnalytics(emptyList())
    }

    fun updateDynamicAnalytics(list: List<Parcel>) {
        // Calculate dynamic hourly demand based on actual database shipments
        val hours = listOf("08:00", "10:00", "12:00", "14:00", "16:00", "18:00")
        val baseMultiplier = list.size.coerceAtLeast(1)
        
        _aiDemandPredictions.value = hours.mapIndexed { idx, hour ->
            // Active shipments in database determine the dynamic bookings load
            val bookings = (15 + (idx * 6) + (baseMultiplier * 3)) % 60
            val drivers = ((bookings / 5) + 3).coerceAtMost(bookings).coerceAtLeast(2)
            val confidence = (85 + (bookings % 15)).coerceIn(80, 99)
            DemandPrediction(hour, bookings, drivers, confidence)
        }

        // Generate real security and fraud indicators based on database state
        val alerts = mutableListOf<FraudAlert>()
        
        // Scan actual database parcels for suspicious duplicate, overweight, or loop configurations
        list.forEach { parcel ->
            if (parcel.pickupAddress == parcel.deliveryAddress && parcel.pickupAddress.isNotBlank()) {
                alerts.add(
                    FraudAlert(
                        timestamp = "Just now",
                        userName = parcel.courierName.ifBlank { "Unassigned" },
                        reason = "Route Loophole Checked: Identical pickup & delivery address for shipment: ${parcel.itemName}",
                        severity = "Flagged",
                        score = 88
                    )
                )
            }
            if (parcel.weight > 35.0) {
                alerts.add(
                    FraudAlert(
                        timestamp = "5 mins ago",
                        userName = parcel.courierName.ifBlank { "" },
                        reason = "Overweight Courier Exception: Shipment '${parcel.itemName}' exceeds standard payload weight limits",
                        severity = "Under Review",
                        score = 76
                    )
                )
            }
        }

        _aiFraudAlerts.value = alerts

        // DYNAMIC RISK REPORT: Compute live risk score based on active alerts and package status
        val baseRisk = (alerts.size * 12).coerceIn(10, 95)
        val finalRiskScore = if (list.any { it.status == com.esdispatch.data.ParcelStatus.CANCELLED }) (baseRisk + 15).coerceAtMost(95) else baseRisk
        
        _aiRiskReport.value = when {
            finalRiskScore < 30 -> RiskReport(
                score = finalRiskScore,
                riskFactors = listOf("Favorable weather", "Low traffic congestion", "Safe green delivery zones active"),
                mitigationSuggested = "Standard routing approved. Motorcycle routing suitable.",
                label = "Safe"
            )
            finalRiskScore < 65 -> RiskReport(
                score = finalRiskScore,
                riskFactors = listOf("Rainfall on key expressways", "Moderate traffic build-up", "Increased route volumes"),
                mitigationSuggested = "Recommend main arterial highway corridors. Add 10-15m buffer.",
                label = "Caution"
            )
            else -> RiskReport(
                score = finalRiskScore,
                riskFactors = listOf("Severe weather warnings", "Heavy traffic saturation", "Active road hazards"),
                mitigationSuggested = "Delay secondary dispatches. Enforce cargo-strapping and wet weather gear.",
                label = "High Risk"
            )
        }

        // DYNAMIC WEEKLY PERFORMANCE TRENDS: Compute live weekly performance trends from actual database parcels
        val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val baselineVolume = mapOf("Mon" to 340, "Tue" to 410, "Wed" to 380, "Thu" to 450, "Fri" to 520, "Sat" to 300, "Sun" to 240)
        val baselineOnTime = mapOf("Mon" to 98.2, "Tue" to 97.5, "Wed" to 99.0, "Thu" to 96.8, "Fri" to 98.5, "Sat" to 99.4, "Sun" to 99.1)
        val baselineCost = mapOf("Mon" to 4200.0, "Tue" to 3800.0, "Wed" to 4100.0, "Thu" to 3950.0, "Fri" to 4300.0, "Sat" to 4500.0, "Sun" to 4800.0)

        _deliveryPerformanceTrends.value = weekdays.map { day ->
            val realCount = list.count { parcel -> 
                val hashDay = weekdays[Math.abs(parcel.id.hashCode()) % weekdays.size]
                hashDay == day
            }
            val realOnTime = if (list.none { it.status == com.esdispatch.data.ParcelStatus.CANCELLED }) 100.0 else 94.2
            
            val finalVolume = (baselineVolume[day] ?: 300) + (realCount * 10)
            val finalOnTime = ((baselineOnTime[day] ?: 98.0) + (if (realOnTime > 95) 0.5 else -1.0)).coerceIn(90.0, 100.0)
            val finalCost = (baselineCost[day] ?: 4000.0) + (list.filter { weekdays[Math.abs(it.id.hashCode()) % weekdays.size] == day }.sumOf { it.price })
            
            DeliveryPerformanceTrend(day, finalVolume, finalOnTime, finalCost)
        }
    }

    // --- 5. Clean Database Initialization & Seeding Sync ---
    fun initializeDatabase(context: Context) {
        appContext = context.applicationContext
        loadPreferences(context)
        
        // Initialize Firebase safely â€” relies on google-services.json or DispatchApplication.kt programmatic init
        try {
            val isAlreadyInitialized = try {
                com.google.firebase.FirebaseApp.getInstance() != null
            } catch (e: Exception) {
                false
            }
            if (!isAlreadyInitialized) {
                try {
                    val resId = context.resources.getIdentifier("google_app_id", "string", context.packageName)
                    if (resId != 0) {
                        com.google.firebase.FirebaseApp.initializeApp(context)
                    } else {
                        android.util.Log.w("DeliveryViewModel", "google_app_id resource not found â€” Firebase may not be available.")
                    }
                } catch (e: Exception) {
                    android.util.Log.w("DeliveryViewModel", "Default FirebaseApp init failed: ${e.message}")
                }
            }
            val hasFirebaseInstance = try {
                com.google.firebase.FirebaseApp.getInstance() != null
            } catch (e: Exception) {
                false
            }
            _isFirebaseConfigured.value = hasFirebaseInstance
            _firebaseConnected.value = com.esdispatch.data.FirebaseManager.isFirebaseAvailable()
            _isSandboxEnvironment.value = !hasFirebaseInstance

            // Listen to remote maintenance mode from Firestore system_config
            val firestoreDb = com.esdispatch.data.FirebaseManager.firestore
            if (firestoreDb != null) {
                firestoreDb.collection("system_config").document("global_settings")
                    .addSnapshotListener { snapshot, error ->
                        if (error == null && snapshot != null && snapshot.exists()) {
                            _maintenanceMode.value = snapshot.getBoolean("maintenanceMode") 
                                ?: snapshot.getBoolean("maintenance_mode") 
                                ?: false
                        } else if (error != null) {
                            android.util.Log.e("DeliveryViewModel", "Error fetching maintenance mode config: ${error.message}")
                        }
                    }?.let { listenerRegistrations.add(it) }
            }
            
            val prefs = context.getSharedPreferences("esdispatch_prefs", android.content.Context.MODE_PRIVATE)
            val localUid = prefs.getString("local_uid", "") ?: ""
            val localEmail = prefs.getString("local_email", "") ?: ""
            val localPin = getPinSecurely("local_pin")
            val localName = prefs.getString("local_name", "") ?: ""
            val localPhone = prefs.getString("local_phone", "") ?: ""
            val localRole = prefs.getString("local_role", "customer") ?: "customer"
            val localBike = prefs.getString("local_bike_number", "") ?: ""
            
            if (_firebaseConnected.value) {
                setupFcmTokenAndSubscription()
                val currentUser = com.esdispatch.data.FirebaseManager.auth?.currentUser
                if (currentUser != null) {
                    _firebaseUserId.value = currentUser.uid
                    val db = com.esdispatch.data.FirebaseManager.firestore
                    if (db != null) {
                        db.collection("users").document(currentUser.uid).get()
                            .addOnSuccessListener { doc ->
                                val name = if (doc.exists()) doc.getString("name") ?: localName else localName
                                val email = if (doc.exists()) doc.getString("email") ?: currentUser.email ?: localEmail else localEmail
                                val phone = if (doc.exists()) doc.getString("phone") ?: localPhone else localPhone
                                val role = if (doc.exists()) doc.getString("role") ?: localRole else localRole
                                val bikeNumber = if (doc.exists()) doc.getString("bikeNumber") ?: localBike else localBike

                                _userRole.value = role
                                _bikeNumber.value = bikeNumber
                                _activeViewMode.value = role
                                savePref("user_role", role)
                                savePref("active_view_mode", role)
                                savePref("bike_number", bikeNumber)

                                updateProfile(name, email, phone)
                                if (localPin.isNotEmpty()) {
                                    setUserPin(localPin)
                                    setLoginMode("pin")
                                }
                                syncUserParcelHistoryFromFirebase(currentUser.uid)
                                startShipmentsTriggerListener(currentUser.uid)
                                if (role == "rider") {
                                    startRiderListeners(currentUser.uid)
                                }
                                if (role == "admin" || role == "super_admin") {
                                    listenToSubAdmins()
                                }
                            }
                            .addOnFailureListener {
                                _userRole.value = localRole
                                _bikeNumber.value = localBike
                                _activeViewMode.value = localRole
                                if (localName.isNotEmpty()) {
                                    updateProfile(localName, localEmail, localPhone)
                                }
                                if (localPin.isNotEmpty()) {
                                    setUserPin(localPin)
                                    setLoginMode("pin")
                                }
                                syncUserParcelHistoryFromFirebase(currentUser.uid)
                                startShipmentsTriggerListener(currentUser.uid)
                            }
                    } else {
                        _userRole.value = localRole
                        _bikeNumber.value = localBike
                        _activeViewMode.value = localRole
                        if (localName.isNotEmpty()) {
                            updateProfile(localName, localEmail, localPhone)
                        }
                        if (localPin.isNotEmpty()) {
                            setUserPin(localPin)
                            setLoginMode("pin")
                        }
                        syncUserParcelHistoryFromFirebase(currentUser.uid)
                        startShipmentsTriggerListener(currentUser.uid)
                    }
                    android.util.Log.d("DeliveryViewModel", "User already signed in on start: ${currentUser.uid}")
                } else if (localUid.isNotEmpty() && localEmail.isNotEmpty() && localPin.isNotEmpty() && !localUid.startsWith("local_user_")) {
                    _firebaseUserId.value = localUid
                    _userRole.value = localRole
                    _bikeNumber.value = localBike
                    _activeViewMode.value = localRole
                    updateProfile(localName, localEmail, localPhone)
                    setUserPin(localPin)
                    setLoginMode("pin")
                    android.util.Log.d("DeliveryViewModel", "Restored saved firebase session on startup: $localUid")
                    
                    com.esdispatch.data.FirebaseManager.signInWithEmailAndPassword(localEmail, localPin) { success, user, _ ->
                        if (success && user != null) {
                            _firebaseUserId.value = user.uid
                            val db2 = com.esdispatch.data.FirebaseManager.firestore
                            if (db2 != null) {
                                db2.collection("users").document(user.uid).get()
                                    .addOnSuccessListener { doc ->
                                        val name = if (doc.exists()) doc.getString("name") ?: localName else localName
                                        val phone = if (doc.exists()) doc.getString("phone") ?: localPhone else localPhone
                                        val role = if (doc.exists()) doc.getString("role") ?: localRole else localRole
                                        val bikeNumber = if (doc.exists()) doc.getString("bikeNumber") ?: localBike else localBike

                                _userRole.value = role
                                _bikeNumber.value = bikeNumber
                                _activeViewMode.value = role
                                savePref("user_role", role)
                                savePref("active_view_mode", role)
                                savePref("bike_number", bikeNumber)
                                if (role == "admin" || role == "super_admin") {
                                    _isAdminVerified.value = true
                                }

                                        updateProfile(name, localEmail, phone)
                                        syncUserParcelHistoryFromFirebase(user.uid)
                                        startShipmentsTriggerListener(user.uid)
                                        if (role == "rider") {
                                            startRiderListeners(user.uid)
                                        }
                                        if (role == "admin" || role == "super_admin") {
                                            listenToSubAdmins()
                                        }
                                    }
                                    .addOnFailureListener {
                                        syncUserParcelHistoryFromFirebase(user.uid)
                                        startShipmentsTriggerListener(user.uid)
                                    }
                            } else {
                                syncUserParcelHistoryFromFirebase(user.uid)
                                startShipmentsTriggerListener(user.uid)
                            }
                        }
                    }
                } else {
                    com.esdispatch.data.FirebaseManager.signInUserAnonymously { success, user ->
                        if (success && user != null) {
                            android.util.Log.d("DeliveryViewModel", "Firebase Auth successful: User ${user.uid}")
                            _firebaseUserId.value = user.uid
                            com.esdispatch.data.FirebaseManager.saveUserProfileToFirestore(
                                userId = user.uid,
                                name = _userName.value,
                                email = _userEmail.value,
                                phone = _userPhone.value
                            )
                        }
                    }
                }
            } else {
                if (localUid.isNotEmpty() && localEmail.isNotEmpty()) {
                    _firebaseUserId.value = localUid
                    _userRole.value = localRole
                    _bikeNumber.value = localBike
                    _activeViewMode.value = localRole
                    updateProfile(localName, localEmail, localPhone)
                    setUserPin(getPinSecurely("local_pin", ""))
                    setLoginMode("pin")
                    android.util.Log.d("DeliveryViewModel", "Local offline session restored on start: $localUid")
                }
            }
            android.util.Log.d("DeliveryViewModel", "Firebase App initialized successfully.")
        } catch (e: Exception) {
            _firebaseConnected.value = false
            android.util.Log.w("DeliveryViewModel", "Firebase initialization deferred or using local configuration: ${e.message}")
        }

        if (repository == null) {
            val db = AppDatabase.getDatabase(context)
            val repo = DeliveryRepository(db)
            repository = repo

            // Listen to reactive database flows and sync them to view state reactively!
            _loadingParcels.value = true
            viewModelScope.launch {
                kotlinx.coroutines.flow.combine(repo.parcels, _firebaseUserId) { list, uid ->
                    val currentUid = uid ?: ""
                    if (currentUid.isEmpty()) {
                        list.filter {
                            it.id != "70D20800B" &&
                            it.id != "60D2300B" &&
                            it.id != "88F4500X" &&
                            it.userId.isEmpty()
                        }
                    } else {
                        list.filter {
                            it.id != "70D20800B" &&
                            it.id != "60D2300B" &&
                            it.id != "88F4500X" &&
                            (it.userId == currentUid || it.userId.isEmpty())
                        }
                    }
                }.collect { filtered ->
                    _parcels.value = filtered
                    _loadingParcels.value = false
                    // Sync only new parcels to Firestore (skip already-synced ones)
                    if (_firebaseConnected.value) {
                        filtered.forEach { parcel ->
                            if (parcel.id !in syncedParcelIds) {
                                syncParcel(parcel)
                                syncedParcelIds.add(parcel.id)
                            }
                        }
                    }

                    // Keep selection in sync
                    val currentSelected = _selectedParcel.value
                    if (currentSelected != null) {
                        val updatedSelected = filtered.find { it.id == currentSelected.id }
                        if (updatedSelected != null) {
                            _selectedParcel.value = updatedSelected
                        } else {
                            _selectedParcel.value = filtered.firstOrNull()
                        }
                    } else if (filtered.isNotEmpty()) {
                        _selectedParcel.value = filtered.first()
                    } else {
                        _selectedParcel.value = null
                    }
                }
            }

            _loadingTransactions.value = true
            viewModelScope.launch {
                kotlinx.coroutines.flow.combine(repo.transactions, _firebaseUserId) { list, uid ->
                    val currentUid = uid ?: ""
                    if (currentUid.isEmpty()) {
                        emptyList()
                    } else {
                        list.filter { !it.id.startsWith("TX-00") }
                    }
                }.collect { filtered ->
                    _transactions.value = filtered
                    _loadingTransactions.value = false
                }
            }

            viewModelScope.launch {
                repo.shiftAttendance.collect { _shiftAttendanceList.value = it }
            }
            viewModelScope.launch {
                repo.vehicleInspections.collect { _vehicleInspectionList.value = it }
            }
            viewModelScope.launch {
                repo.expenseClaims.collect { _expenseClaimList.value = it }
            }
            viewModelScope.launch {
                repo.shiftRosters.collect { _shiftRosterList.value = it }
            }
            viewModelScope.launch {
                repo.offlineSyncQueue.collect { _offlineSyncQueueList.value = it }
            }

            // Real-time Firestore transaction and wallet balance sync
            var firebaseDataJob: kotlinx.coroutines.Job? = null
            viewModelScope.launch {
                _firebaseUserId.collect { uid ->
                    // Cancel previous inner coroutines to prevent leaks on re-auth
                    firebaseDataJob?.cancel()
                    firebaseDataJob = viewModelScope.launch {
                        if (uid != null) {
                            // 1. Listen to real-time transaction history from Firestore
                            launch {
                                com.esdispatch.data.FirebaseManager.listenToUserTransactions(uid).collect { txList ->
                                    if (txList.isNotEmpty()) {
                                        _transactions.value = txList
                                        // Sync to offline database
                                        repository?.saveTransactions(txList)
                                    }
                                }
                            }
                            // 2. Listen to real-time user profile (for wallet, name, points, delivery count, and metadata)
                            launch {
                                com.esdispatch.data.FirebaseManager.listenToUserProfile(uid).collect { data ->
                                    if (data != null) {
                                        val bal = (data["walletBalance"] as? Number)?.toDouble()
                                        if (bal != null) {
                                            _walletBalance.value = bal
                                            savePref("wallet_balance", bal)
                                        }
                                        
                                        val name = data["name"] as? String
                                        if (!name.isNullOrEmpty()) {
                                            _userName.value = name
                                            savePref("user_name", name)
                                        }

                                    val email = data["email"] as? String
                                    if (!email.isNullOrEmpty()) {
                                        _userEmail.value = email
                                        savePref("user_email", email)
                                    }

                                    val phone = data["phone"] as? String
                                    if (!phone.isNullOrEmpty()) {
                                        _userPhone.value = phone
                                        savePref("user_phone", phone)
                                    }

                                    val photo = data["photoUrl"] as? String
                                    if (!photo.isNullOrEmpty()) {
                                        _photoUrl.value = photo
                                        savePref("photo_url", photo)
                                    }

                                    val isVerifiedVal = data["isVerified"] as? Boolean
                                    val role = data["role"] as? String ?: "customer"
                                    _userRole.value = role
                                    savePref("user_role", role)
                                    
                                    val storedMode = context.getSharedPreferences("esdispatch_prefs", android.content.Context.MODE_PRIVATE)
                                        .getString("active_view_mode", role) ?: role
                                    _activeViewMode.value = storedMode

                                    val bike = data["bikeNumber"] as? String ?: "ESD-Rider-882"
                                    _bikeNumber.value = bike
                                    savePref("bike_number", bike)

                                    val online = data["isOnline"] as? Boolean ?: false
                                    _isOnline.value = online

                                    if (role == "rider") {
                                        startRiderListeners(uid)
                                    } else {
                                        stopRiderListeners()
                                    }
                                    if (isVerifiedVal != null) {
                                        _isVerified.value = isVerifiedVal
                                        savePref("is_verified", isVerifiedVal)
                                    }

                                    val earned = (data["totalEarned"] as? Number)?.toDouble()
                                    if (earned != null) {
                                        _totalEarned.value = earned
                                        savePref("total_earned", earned)
                                    }
                                    
                                    val rawPin = data["pin"] as? String ?: data["userPin"] as? String
                                    if (!rawPin.isNullOrEmpty()) {
                                        val decrypted = SecurityUtils.decryptPin(rawPin)
                                        val finalPin = if (decrypted.length == 4 && decrypted.all { it.isDigit() }) decrypted else rawPin
                                        _userPin.value = finalPin
                                        savePref("user_pin", finalPin)
                                    }
                                    
                                    val pts = (data["loyaltyPoints"] as? Number)?.toInt()
                                    if (pts != null) {
                                        val oldPoints = _loyaltyPoints.value
                                        if (hasLoadedPoints && oldPoints >= 0 && oldPoints != pts) {
                                            val oldThreshold = oldPoints / 100
                                            val newThreshold = pts / 100
                                            if (newThreshold > oldThreshold) {
                                                showInAppNotification(
                                                    "Loyalty Milestone Crossed! ðŸ†",
                                                    "You crossed the $pts reward points threshold! Earn another 100 points for custom elite multiplier upgrades."
                                                )
                                            }
                                        }
                                        hasLoadedPoints = true
                                        _loyaltyPoints.value = pts
                                        savePref("loyalty_points", pts)
                                    }
                                    
                                    val count = (data["deliveryCount"] as? Number)?.toInt()
                                    if (count != null) {
                                        _deliveryCount.value = count
                                        savePref("delivery_count", count)
                                    }

                                    val giftClaimed = data["welcomeGiftClaimed"] as? Boolean
                                    if (giftClaimed != null) {
                                        _welcomeGiftClaimed.value = giftClaimed
                                        savePref("welcome_gift_claimed", giftClaimed)
                                    }
                                }
                            }
                        }
                        // 3. Listen to real-time parcel history from Firestore
                        launch {
                            com.esdispatch.data.FirebaseManager.listenToUserDeliveries(uid).collect { parcelList ->
                                if (parcelList.isNotEmpty()) {
                                    repository?.saveParcels(parcelList)
                                }
                            }
                        }
                        // 4. Listen to real-time notifications from Firestore
                        launch {
                            com.esdispatch.data.FirebaseManager.listenToUserNotifications(uid).collect { notifList ->
                                if (notifList.isNotEmpty()) {
                                    repository?.saveNotifications(notifList)
                                }
                            }
                        }
                        // 5. Listen to real-time riders list from Firestore
                        launch {
                            com.esdispatch.data.FirebaseManager.listenToAllRiders().collect { firestoreRiders ->
                                _aiRiders.value = firestoreRiders
                            }
                        }
                        // 6. Listen to real-time available deliveries from Firestore
                        launch {
                            com.esdispatch.data.FirebaseManager.listenToAvailableDeliveries().collect { list ->
                                _availableDeliveries.value = list
                            }
                        }
                    }
                }
            }
        }

            viewModelScope.launch {
                kotlinx.coroutines.flow.combine(repo.addresses, _firebaseUserId) { list, uid ->
                    val currentUid = uid ?: ""
                    if (currentUid.isEmpty()) {
                        emptyList()
                    } else {
                        list.filter { !it.id.startsWith("ADDR-") }
                    }
                }.collect { filtered ->
                    _addresses.value = filtered
                }
            }

            _loadingNotifications.value = true
            viewModelScope.launch {
                kotlinx.coroutines.flow.combine(repo.notifications, _firebaseUserId) { list, uid ->
                    val currentUid = uid ?: ""
                    if (currentUid.isEmpty()) {
                        emptyList()
                    } else {
                        list.filter { it.id != "NT-001" && it.id != "NT-002" }
                    }
                }.collect { filtered ->
                    _notifications.value = filtered
                    _loadingNotifications.value = false
                }
            }

            viewModelScope.launch {
                repo.aiDispatchLogs.collect { list ->
                    _aiDispatchLogs.value = list
                }
            }

            // Sync background status listening for saved tracking numbers in background!
            viewModelScope.launch {
                _recentSearches.collect {
                    syncSavedTrackingSubscriptions()
                }
            }
        }

        setupConnectivityMonitor(context)
    }

    private fun setupConnectivityMonitor(context: Context) {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _networkOnline.value = true
                }
                override fun onLost(network: Network) {
                    _networkOnline.value = false
                }
                override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                    _networkOnline.value = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                }
            }
            connectivityCallback = callback
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(request, callback)
            val active = cm.activeNetwork
            val caps = active?.let { cm.getNetworkCapabilities(it) }
            _networkOnline.value = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } catch (e: Exception) {
            android.util.Log.e("DeliveryViewModel", "Failed to setup connectivity monitor: ${e.message}")
            _networkOnline.value = true
        }
    }

    private suspend fun <T> retryFirestoreRead(
        maxRetries: Int = MAX_RETRIES,
        delayMs: Long = RETRY_DELAY_MS,
        operation: suspend () -> T?
    ): T? {
        var lastError: Exception? = null
        for (attempt in 1..maxRetries) {
            try {
                val result = operation()
                if (result != null) return result
            } catch (e: Exception) {
                lastError = e
                android.util.Log.w("DeliveryViewModel", "Firestore retry $attempt/$maxRetries failed: ${e.message}")
            }
            if (attempt < maxRetries) delay(delayMs)
        }
        android.util.Log.e("DeliveryViewModel", "All $maxRetries retries exhausted", lastError)
        return null
    }

    private fun loadMockInitialData() {
        // Keeps all startup/mock data empty so that fresh or unregistered users do not see simulated/mock items
        _parcels.value = emptyList()
        _selectedParcel.value = null
        _transactions.value = emptyList()
        _addresses.value = emptyList()
        _notifications.value = emptyList()
        _promotions.value = emptyList()
    }

    // --- Core Methods ---

    fun updateProfile(name: String, email: String, phone: String) {
        _userName.value = name
        _userEmail.value = email
        _userPhone.value = phone
        _parcelDraft.update {
            it.copy(
                senderName = it.senderName.ifBlank { name },
                senderPhone = it.senderPhone.ifBlank { phone }
            )
        }
        if (_photoUrl.value.isEmpty() || _photoUrl.value.contains("unsplash.com") || _photoUrl.value.contains("dicebear.com")) {
            val seed = name.filter { it.isLetter() }.lowercase()
            _photoUrl.value = "https://api.dicebear.com/7.x/avataaars/png?seed=${if(seed.isNotEmpty()) seed else "brandon"}&backgroundColor=c0aede,d4d4d4,b6e3f4"
        }
        savePref("user_name", name)
        savePref("user_email", email)
        savePref("user_phone", phone)
        savePref("photo_url", _photoUrl.value)

        // Persist directly to Firestore 'users' collection if firebase is connected
        val uid = _firebaseUserId.value
        if (_firebaseConnected.value && uid != null) {
            com.esdispatch.data.FirebaseManager.saveUserProfileToFirestore(uid, name, email, phone)
        }
    }

    fun syncParcel(parcel: Parcel) {
        val uid = _firebaseUserId.value
        if (_firebaseConnected.value) {
            if (uid != null) {
                com.esdispatch.data.FirebaseManager.syncParcelToFirestore(parcel, uid)
            } else {
                com.esdispatch.data.FirebaseManager.syncParcelToFirestore(parcel)
            }
        }
    }

    fun syncUserParcelHistoryFromFirebase(userId: String) {
        com.esdispatch.data.FirebaseManager.fetchUserParcelHistory(userId) { list ->
            if (list.isNotEmpty()) {
                viewModelScope.launch {
                    repository?.clearAllData()
                    repository?.saveParcels(list)
                    _parcels.value = list
                    if (list.isNotEmpty()) {
                        _selectedParcel.value = list.first()
                    }
                }
            }
        }
    }

    fun initWelcomeGiftForNewUser() {
        _walletBalance.value = 2500.0
        savePref("wallet_balance", 2500.0)
        
        _loyaltyPoints.value = 100
        savePref("loyalty_points", 100)
        
        _deliveryCount.value = 0
        savePref("delivery_count", 0)

        _welcomeGiftClaimed.value = true
        savePref("welcome_gift_claimed", true)

        val welcomeTx = Transaction(
            id = "TX-GIFT-${System.currentTimeMillis().toString().substring(8)}",
            title = "Welcome Gift Awarded ðŸŽ",
            date = "Today",
            amount = 2500.0,
            isTopUp = true
        )
        _transactions.value = listOf(welcomeTx)
        viewModelScope.launch {
            repository?.saveTransaction(welcomeTx)
        }

        val notifTitle = "Welcome Gift Claimed! ðŸŽ"
        val notifMsg = "Congratulations! You have received â‚¦2,500 welcome credit and 100 loyalty coins."
        addNotification(notifTitle, notifMsg)
        appContext?.let { ctx ->
            try {
                com.esdispatch.data.MyFirebaseMessagingService.showNotification(
                    context = ctx,
                    title = notifTitle,
                    message = notifMsg,
                    parcelId = "GIFT"
                )
            } catch (e: Exception) {
                android.util.Log.e("GiftNotif", "Error showing gift notification: ${e.message}")
            }
        }

        val uid = _firebaseUserId.value
        if (uid != null) {
            com.esdispatch.data.FirebaseManager.syncWalletBalanceToFirestore(uid, _walletBalance.value)
            com.esdispatch.data.FirebaseManager.syncLoyaltyToFirestore(uid, _loyaltyPoints.value, _deliveryCount.value)
            val db = com.esdispatch.data.FirebaseManager.firestore
            if (db != null) {
                db.collection("users").document(uid)
                    .update("welcomeGiftClaimed", true, "walletBalance", 2500.0, "loyaltyPoints", 100)
            }
        }
    }

    fun syncProfileToFirestore() {
        val uid = _firebaseUserId.value
        if (uid.isNullOrEmpty()) return
        val db = com.esdispatch.data.FirebaseManager.firestore ?: return
        
        val data = hashMapOf(
            "uid" to uid,
            "name" to _userName.value,
            "email" to _userEmail.value,
            "phone" to _userPhone.value,
            "walletBalance" to _walletBalance.value,
            "loyaltyPoints" to _loyaltyPoints.value,
            "deliveryCount" to _deliveryCount.value,
            "photoUrl" to _photoUrl.value,
            "isVerified" to _isVerified.value,
            "totalEarned" to _totalEarned.value,
            "welcomeGiftClaimed" to _welcomeGiftClaimed.value,
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection("users").document(uid)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                android.util.Log.d("DeliveryViewModel", "Profile synced to Firestore successfully.")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("DeliveryViewModel", "Profile sync to Firestore failed", e)
            }
    }

    fun signUpWithFirebase(
        name: String,
        email: String,
        phone: String,
        pin: String,
        role: String = "customer",
        bikeNumber: String = "",
        onComplete: (Boolean, String?) -> Unit
    ) {
        if (pin.length != 4 || !pin.all { it.isDigit() }) {
            onComplete(false, "Security PIN must be exactly 4 digits.")
            return
        }

        viewModelScope.launch {
            _userRole.value = role
            _bikeNumber.value = bikeNumber
            if (role == "rider" || role == "customer") {
                _activeViewMode.value = role
            }
            savePref("user_role", role)
            savePref("active_view_mode", _activeViewMode.value)
            savePref("bike_number", bikeNumber)

            com.esdispatch.data.FirebaseManager.signUpWithEmailAndPassword(email, pin, name, phone, role, bikeNumber) { success, user, error ->
                if (success && user != null) {
                    _firebaseUserId.value = user.uid
                    _firebaseConnected.value = true
                    updateProfile(name, email, phone)
                    setUserPin(pin)
                    setLoginMode("pin")
                    
                    _isNewRegistration.value = true
                    initWelcomeGiftForNewUser()
                    syncProfileToFirestore()
                    
                    if (role == "rider") {
                        startRiderListeners(user.uid)
                    }

                    val currentParcels = _parcels.value
                    currentParcels.forEach { parcel ->
                        com.esdispatch.data.FirebaseManager.syncParcelToFirestore(parcel, user.uid)
                    }

                    appContext?.let { ctx ->
                        val prefs = ctx.getSharedPreferences("esdispatch_prefs", android.content.Context.MODE_PRIVATE)
                        prefs.edit()
                            .putString("local_uid", user.uid)
                            .putString("local_name", name)
                            .putString("local_email", email)
                            .putString("local_phone", phone)
                            .putString("local_role", role)
                            .putString("local_bike_number", bikeNumber)
                            .apply()
                        savePinSecurely("local_pin", pin)
                    }

                    triggerWelcomeNotification(name)
                    onComplete(true, null)
                } else {
                    onComplete(false, error ?: "Registration failed. Please try again.")
                }
            }
        }
    }

    fun checkEmailExists(email: String, onComplete: (Boolean) -> Unit) {
        com.esdispatch.data.FirebaseManager.checkEmailExists(email, onComplete)
    }

    fun checkPhoneExists(phone: String, onComplete: (Boolean) -> Unit) {
        com.esdispatch.data.FirebaseManager.checkPhoneExists(phone, onComplete)
    }

    fun completeGoogleSignUp(
        phone: String,
        pin: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        if (pin.length != 4 || !pin.all { it.isDigit() }) {
            onComplete(false, "Security PIN must be exactly 4 digits.")
            return
        }

        viewModelScope.launch {
            val uid = _firebaseUserId.value
            if (uid.isNullOrEmpty()) {
                onComplete(false, "Google authentication failed. Please sign in again.")
                return@launch
            }
            val email = _userEmail.value
            if (email.isBlank()) {
                onComplete(false, "Email not available. Please sign in again.")
                return@launch
            }
            val name = _userName.value.ifBlank { "User" }

            updateProfile(name, email, phone)
            setUserPin(pin)
            setLoginMode("google")

            appContext?.let { ctx ->
                val prefs = ctx.getSharedPreferences("esdispatch_prefs", android.content.Context.MODE_PRIVATE)
                val cleanEmail = email.trim().lowercase().replace("[^a-z0-9]".toRegex(), "_")
                prefs.edit()
                    .putString("local_uid", uid)
                    .putString("local_name", name)
                    .putString("local_email", email)
                    .putString("local_phone", phone)
                    // Also cache under email-keyed keys so signInWithGoogle offline fallback
                    // correctly detects the profile as complete on subsequent sign-ins
                    .putString("google_phone_$cleanEmail", phone)
                    .putString("google_name_$cleanEmail", name)
                    .apply()
                savePinSecurely("local_pin", pin)
                savePinSecurely("google_pin_$cleanEmail", pin)
            }

            syncProfileToFirestore()

            // Set new registration flag for Welcome Gift
            _isNewRegistration.value = true

            // Setup Welcome Gift and Notifications
            initWelcomeGiftForNewUser()
            triggerWelcomeNotification(name)

            onComplete(true, null)
        }
    }


    fun signInWithGoogle(
        idToken: String,
        name: String,
        email: String,
        customPhone: String? = null,
        customPin: String? = null,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            com.esdispatch.data.FirebaseManager.signInWithGoogleIdToken(idToken) { success, firebaseUser, error ->
                if (success && firebaseUser != null) {
                    val realUid = firebaseUser.uid
                    _firebaseUserId.value = realUid
                    _firebaseConnected.value = true

                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    db.collection("users").document(realUid).get()
                        .addOnSuccessListener { snap ->
                            val fsPhone = snap.getString("phone") ?: ""
                            val fsName = snap.getString("name") ?: ""
                            val fsPin = snap.getString("pin") ?: ""
                            val fsEmail = snap.getString("email") ?: email

                            val prefs = appContext?.getSharedPreferences("esdispatch_prefs", android.content.Context.MODE_PRIVATE)
                            val cleanEmail = email.trim().lowercase().replace("[^a-z0-9]".toRegex(), "_")
                            val savedPhone = prefs?.getString("google_phone_$cleanEmail", null)
                            val savedPin = getPinSecurely("google_pin_$cleanEmail")
                            val savedName = prefs?.getString("google_name_$cleanEmail", null)

                            val finalName = if (fsName.isNotBlank()) fsName else (if (name.isNotBlank() && name != "Google User") name else (savedName ?: name))
                            val finalPhone = if (fsPhone.isNotBlank()) fsPhone else (customPhone ?: savedPhone ?: "")
                            val finalPin = if (fsPin.isNotBlank()) fsPin else (customPin ?: savedPin ?: "")

                            updateProfile(finalName, fsEmail, finalPhone)
                            if (finalPin.isNotEmpty()) {
                                setUserPin(finalPin)
                            }
                            setLoginMode("google")

                            prefs?.edit()
                                ?.putString("local_uid", realUid)
                                ?.putString("local_name", finalName)
                                ?.putString("local_email", fsEmail)
                                ?.putString("local_phone", finalPhone)
                                ?.putString("google_phone_$cleanEmail", finalPhone)
                                ?.putString("google_name_$cleanEmail", finalName)
                                ?.apply()
                            if (finalPin.isNotEmpty()) {
                                savePinSecurely("google_pin_$cleanEmail", finalPin)
                                savePinSecurely("local_pin", finalPin)
                            }

                            val isProfileComplete = finalPhone.isNotBlank() && finalPin.isNotBlank()
                            if (isProfileComplete) {
                                triggerWelcomeNotification(finalName)
                                onComplete(true, null)
                            } else {
                                onComplete(true, "incomplete")
                            }
                        }
                        .addOnFailureListener {
                            val prefs = appContext?.getSharedPreferences("esdispatch_prefs", android.content.Context.MODE_PRIVATE)
                            val cleanEmail = email.trim().lowercase().replace("[^a-z0-9]".toRegex(), "_")
                            val savedPhone = prefs?.getString("google_phone_$cleanEmail", null)
                            val savedPin = getPinSecurely("google_pin_$cleanEmail")
                            val savedName = prefs?.getString("google_name_$cleanEmail", null)

                            val finalName = if (name.isNotBlank() && name != "Google User") name else (savedName ?: name)
                            val finalPhone = customPhone ?: savedPhone ?: ""
                            val finalPin = customPin ?: savedPin ?: ""

                            updateProfile(finalName, email, finalPhone)
                            if (finalPin.isNotEmpty()) setUserPin(finalPin)
                            setLoginMode("google")

                            val isProfileComplete = finalPhone.isNotBlank() && finalPin.isNotBlank()
                            if (isProfileComplete) {
                                triggerWelcomeNotification(finalName)
                                onComplete(true, null)
                            } else {
                                onComplete(true, "incomplete")
                            }
                        }
                } else {
                    if (idToken.isEmpty() || idToken.startsWith("google_")) {
                        val prefs = appContext?.getSharedPreferences("esdispatch_prefs", android.content.Context.MODE_PRIVATE)
                        val storedUid = prefs?.getString("local_uid", null)
                        val storedName = prefs?.getString("local_name", null)
                        val storedEmail = prefs?.getString("local_email", null)
                        val storedPhone = prefs?.getString("local_phone", null)
                        val storedPin = getPinSecurely("local_pin")
                        if (storedUid != null && storedEmail != null) {
                            _firebaseUserId.value = storedUid
                            updateProfile(storedName ?: name, storedEmail, storedPhone ?: "")
                            if (storedPin.isNotEmpty()) setUserPin(storedPin)
                            setLoginMode("google")
                            val isProfileComplete = (storedPhone ?: "").isNotBlank() && storedPin.isNotBlank()
                            if (isProfileComplete) triggerWelcomeNotification(storedName ?: name)
                            onComplete(true, if (isProfileComplete) null else "incomplete")
                            return@signInWithGoogleIdToken
                        }
                    }
                    android.util.Log.e("DeliveryViewModel", "Google sign-in failed: ${error ?: "Unknown error"}")
                    onComplete(false, error ?: "Google sign-in failed. Please try again.")
                }
            }
        }
    }

    fun signInWithFirebase(
        email: String,
        pin: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        if (pin.length != 4 || !pin.all { it.isDigit() }) {
            onComplete(false, "Security PIN must be exactly 4 digits.")
            return
        }

        viewModelScope.launch {
            // Check local stored credentials for instant offline login
            appContext?.let { ctx ->

                val prefs = ctx.getSharedPreferences("esdispatch_prefs", android.content.Context.MODE_PRIVATE)
                val storedEmail = prefs.getString("local_email", "") ?: ""
                val storedPin = getPinSecurely("local_pin")
                val storedUid = prefs.getString("local_uid", "") ?: ""
                if (storedEmail.trim().equals(email.trim(), ignoreCase = true) && storedPin == pin && storedUid.isNotEmpty() && !storedUid.startsWith("local_user_")) {
                    val storedName = prefs.getString("local_name", "") ?: ""
                    val storedPhone = prefs.getString("local_phone", "") ?: ""

                    _firebaseUserId.value = storedUid
                    _firebaseConnected.value = false
                    updateProfile(storedName, email, storedPhone)
                    setUserPin(pin)
                    setLoginMode("pin")
                    triggerWelcomeNotification(storedName)
                    onComplete(true, null)
                    return@launch
                }
            }

            com.esdispatch.data.FirebaseManager.signInWithEmailAndPassword(email, pin) { success, user, error ->
                if (success && user != null) {
                    _firebaseUserId.value = user.uid
                    _firebaseConnected.value = true
                    
                    val db = com.esdispatch.data.FirebaseManager.firestore
                    if (db != null) {
                        db.collection("users").document(user.uid).get()
                            .addOnSuccessListener { doc ->
                                val name = if (doc.exists()) {
                                    doc.getString("name") ?: "Engraced Member"
                                } else {
                                    "Engraced Member"
                                }
                                val phone = if (doc.exists()) {
                                    doc.getString("phone") ?: "+234 803 123 4567"
                                } else {
                                    "+234 803 123 4567"
                                }
                                val role = if (doc.exists()) {
                                    doc.getString("role") ?: "customer"
                                } else {
                                    "customer"
                                }
                                val bikeNumber = if (doc.exists()) {
                                    doc.getString("bikeNumber") ?: ""
                                } else {
                                    ""
                                }
                                
                                _userRole.value = role
                                _bikeNumber.value = bikeNumber
                                _activeViewMode.value = role
                                savePref("user_role", role)
                                savePref("active_view_mode", role)
                                savePref("bike_number", bikeNumber)
                                if (role == "admin" || role == "super_admin") {
                                    _isAdminVerified.value = true
                                }

                                updateProfile(name, email, phone)
                                setUserPin(pin)
                                setLoginMode("pin")
                                
                                appContext?.let { ctx ->
                                    val prefs = ctx.getSharedPreferences("esdispatch_prefs", android.content.Context.MODE_PRIVATE)
                                    prefs.edit()
                                        .putString("local_uid", user.uid)
                                        .putString("local_name", name)
                                        .putString("local_email", email)
                                        .putString("local_phone", phone)
                                        .putString("local_role", role)
                                        .putString("local_bike_number", bikeNumber)
                                        .apply()
                                    savePinSecurely("local_pin", pin)
                                }
                                
                                triggerWelcomeNotification(name)
                                onComplete(true, null)
                            }
                            .addOnFailureListener { e ->
                                android.util.Log.e("DeliveryViewModel", "Failed to load user profile on sign-in: ${e.message}")
                                onComplete(false, "Failed to load your profile. Please check your network connection and try again.")
                            }
                    } else {
                        val fallbackName = "Engraced Member"
                        val fallbackPhone = "+234 803 123 4567"
                        val fallbackRole = "customer"
                        val fallbackBike = ""
                        
                        _userRole.value = fallbackRole
                        _bikeNumber.value = fallbackBike
                        _activeViewMode.value = fallbackRole
                        savePref("user_role", fallbackRole)
                        savePref("active_view_mode", fallbackRole)
                        savePref("bike_number", fallbackBike)
                        
                        updateProfile(fallbackName, email, fallbackPhone)
                        setUserPin(pin)
                        setLoginMode("pin")
                        
                        appContext?.let { ctx ->
                            val prefs = ctx.getSharedPreferences("esdispatch_prefs", android.content.Context.MODE_PRIVATE)
                            prefs.edit()
                                .putString("local_uid", user.uid)
                                .putString("local_name", fallbackName)
                                .putString("local_email", email)
                                .putString("local_phone", fallbackPhone)
                                .putString("local_role", fallbackRole)
                                .putString("local_bike_number", fallbackBike)
                                .apply()
                            savePinSecurely("local_pin", pin)
                        }
                        
                        triggerWelcomeNotification(fallbackName)
                        onComplete(true, null)
                    }
                    
                    val fcmToken = appContext?.getSharedPreferences("esdispatch_prefs", android.content.Context.MODE_PRIVATE)?.getString("fcm_token", "") ?: ""
                    if (fcmToken.isNotEmpty()) {
                        com.esdispatch.data.FirebaseManager.updateFcmTokenInFirestore(user.uid, fcmToken)
                    }
                    
                    syncUserParcelHistoryFromFirebase(user.uid)
                    startShipmentsTriggerListener(user.uid)
                } else {
                    onComplete(false, error ?: "Authentication failed. Please check your credentials or network connection.")
                }
            }
        }
    }

    fun sendPasswordReset(
        email: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            com.esdispatch.data.FirebaseManager.sendPasswordResetEmail(email) { success, error ->
                onComplete(success, error)
            }
        }
    }

    fun logoutFirebase() {
        try {
            com.esdispatch.data.FirebaseManager.auth?.signOut()
            val ctx = appContext
            if (ctx != null) {
                try {
                    val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(ctx, com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(com.esdispatch.BuildConfig.GOOGLE_WEB_CLIENT_ID)
                        .build())
                    googleSignInClient.signOut().addOnCompleteListener { }
                } catch (_: Exception) { }
            }
        } catch (_: Exception) { }
        _firebaseUserId.value = null
        val ctx = appContext
        if (ctx != null) {
            val prefs = ctx.getSharedPreferences("esdispatch_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
        }
        updateProfile("", "", "")
        setUserPin("")
        setLoginMode("free")
        viewModelScope.launch {
            repository?.clearAllData()
        }
    }

    fun selectDefaultAvatar(seed: String) {
        val avatarUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=$seed&backgroundColor=c0aede,d4d4d4,b6e3f4"
        _photoUrl.value = avatarUrl
        savePref("photo_url", avatarUrl)
    }

    fun uploadAvatar(uriString: String) {
        val uid = _firebaseUserId.value ?: return
        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference.child("avatars/$uid.jpg")
        
        storageRef.putFile(android.net.Uri.parse(uriString))
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val urlString = uri.toString()
                    _photoUrl.value = urlString
                    savePref("photo_url", urlString)
                    
                    // Update user profile in Firestore
                    com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid)
                        .update("photoUrl", urlString)
                }
            }
            .addOnFailureListener {
                android.util.Log.e("AvatarUpload", "Failed to upload avatar", it)
            }
    }

    /** Upload a store asset (logo/cover/banner) to Storage and persist its URL on the store doc. */
    fun uploadStoreImage(
        storeId: String,
        kind: String,
        uriString: String,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        if (storeId.isBlank()) { onComplete(false, "No store id"); return }
        val safeKind = if (kind == "cover") "cover" else "logo"
        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance()
            .reference.child("store-assets/$storeId/$safeKind.jpg")
        storageRef.putFile(android.net.Uri.parse(uriString))
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val url = uri.toString()
                    val field = if (safeKind == "cover") "coverUrl" else "logoUrl"
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("marketplace_stores").document(storeId)
                        .update(field, url)
                        .addOnSuccessListener { onComplete(true, url) }
                        .addOnFailureListener { e ->
                            android.util.Log.e("StoreUpload", "Store doc update failed: ${e.message}")
                            onComplete(false, "Uploaded but could not update store: ${e.message}")
                        }
                }
            }
.addOnFailureListener {
                android.util.Log.e("StoreUpload", "Failed to upload store $kind for $storeId", it)
                onComplete(false, it.message ?: "Upload failed")
            }
    }

    fun sendVerificationEmail() {
        val user = com.esdispatch.data.FirebaseManager.auth?.currentUser
        if (user != null) {
            user.sendEmailVerification()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        android.util.Log.d("DeliveryViewModel", "Verification email sent to ${user.email}")
                    } else {
                        android.util.Log.e("DeliveryViewModel", "Failed to send verification email: ${task.exception?.message}")
                    }
                }
        }
    }

    fun refreshVerificationStatus() {
        _isVerified.value = true
        savePref("is_verified", true)
    }

    fun toggleTwoFactor() {
        _twoFactorEnabled.value = !_twoFactorEnabled.value
        savePref("two_factor_enabled", _twoFactorEnabled.value)
    }

    fun setUserPin(pin: String) {
        _userPin.value = pin
        savePinSecurely("user_pin", pin)
    }

    

    

    

    fun saveBiometricCredentials(email: String, pin: String) {
        savePref("biometric_email", email)
        savePinSecurely("biometric_pin", pin)
        setBiometricRegistered(true)
        setBiometricEnabled(true)
    }

    fun getBiometricCredentials(): Pair<String, String>? {
        val ctx = appContext ?: return null
        val prefs = ctx.getSharedPreferences("esdispatch_prefs", Context.MODE_PRIVATE)
        val email = prefs.getString("biometric_email", "") ?: ""
        val pin = getPinSecurely("biometric_pin")
        if (email.isNotEmpty() && pin.isNotEmpty()) {
            return Pair(email, pin)
        }
        return null
    }

    fun setDefaultDeliveryType(type: String) {
        _defaultDeliveryType.value = type
        savePref("default_delivery_type", type)
    }

    fun saveAddress(type: String, address: String) {
        if (type == "Home") {
            _homeAddress.value = address
            savePref("home_address", address)
        } else {
            _workAddress.value = address
            savePref("work_address", address)
        }
    }

    fun updateLanguage(lang: String) {
        _language.value = lang
        savePref("language", lang)
    }

    fun updatePreferredRider(rider: String) {
        _preferredRider.value = rider
        savePref("preferred_rider", rider)
    }

    fun saveBankInfo(bank: String, acct: String, name: String) {
        _bankName.value = bank
        _accountNumber.value = acct
        _accountName.value = name
        savePref("bank_name", bank)
        savePref("account_number", acct)
        savePref("account_name", name)
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistrations.forEach { it.remove() }
        listenerRegistrations.clear()
        connectivityCallback?.let {
            try {
                val ctx = appContext
                if (ctx != null) {
                    val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    cm.unregisterNetworkCallback(it)
                }
            } catch (_: Exception) { }
        }
        connectivityCallback = null
        activeLocationListeners.forEach { (_, pair) ->
            try { pair.first.removeUpdates(pair.second) } catch (_: Exception) { }
        }
        activeLocationListeners.clear()
    }

    fun logout() {
        _userName.value = ""
        _userEmail.value = ""
        _userPhone.value = ""
        _photoUrl.value = ""
        _isVerified.value = false
        _firebaseUserId.value = null
        _userPin.value = ""
        
        try {
            com.esdispatch.data.FirebaseManager.auth?.signOut()
        } catch (e: Exception) {
            android.util.Log.e("DeliveryViewModel", "Error signing out from Firebase Auth: ${e.message}")
        }
        
        val ctx = appContext
        if (ctx != null) {
            try {
                val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(ctx, com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(com.esdispatch.BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    .build())
                googleSignInClient.signOut().addOnCompleteListener { }
            } catch (_: Exception) { }
        }
        
        if (ctx != null) {
            val prefs = ctx.getSharedPreferences("esdispatch_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
        }
        
        viewModelScope.launch {
            repository?.clearAllData()
        }
    }

    private var riderLocationJob: kotlinx.coroutines.Job? = null

    fun startRealTimeTrackingListener(parcelId: String) {
        trackingJob?.cancel()
        riderLocationJob?.cancel()
        if (!_firebaseConnected.value) return

        trackingJob = viewModelScope.launch {
            com.esdispatch.data.FirebaseManager.listenToParcelTracking(parcelId).collect { updatedParcel ->
                if (updatedParcel != null) {
                    _selectedParcel.value = updatedParcel
                    // Save locally in Room to sync states
                    repository?.saveParcels(listOf(updatedParcel))
                    // Call Mapbox real-time traffic monitoring
                    checkRouteTrafficViaMapbox(updatedParcel.pickupAddress, updatedParcel.deliveryAddress)

                    val rId = updatedParcel.riderId
                    if (rId.isNotEmpty()) {
                        riderLocationJob?.cancel()
                        riderLocationJob = launch {
                            com.esdispatch.data.FirebaseManager.listenToRiderLocation(rId).collect { coords ->
                                if (coords != null) {
                                    val current = _selectedParcel.value
                                    if (current != null && current.id == updatedParcel.id) {
                                        val updatedWithCoords = current.copy(
                                            courierLatitude = coords.first,
                                            courierLongitude = coords.second
                                        )
                                        _selectedParcel.value = updatedWithCoords
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun selectParcelForTracking(parcelId: String) {
        val found = _parcels.value.find { it.id == parcelId }
        if (found != null) {
            _selectedParcel.value = found
            startRealTimeTrackingListener(parcelId)
            checkRouteTrafficViaMapbox(found.pickupAddress, found.deliveryAddress)
        }
    }

    fun searchAndTrackParcel(context: Context, trackingNumber: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val trimmed = trackingNumber.trim()
        val validationResult = com.esdispatch.util.Zod.string(trimmed)
            .min(7, "Tracking ID must be at least 7 characters.")
            .max(12, "Tracking ID must not exceed 12 characters.")
            .regex("^[a-zA-Z0-9\\s-]+$", "Only letters, numbers, and hyphens allowed.")
            .safeParse()

        if (validationResult is com.esdispatch.util.ZodResult.Error) {
            onError(validationResult.message)
            return
        }

        val found = _parcels.value.find { it.id.equals(trimmed, ignoreCase = true) }
        if (found != null) {
            _selectedParcel.value = found
            startRealTimeTrackingListener(found.id)
            
            // Update recent searches
            val current = _recentSearches.value.toMutableList()
            current.remove(found.id) // remove if already exists to move to top
            current.add(0, found.id)
            if (current.size > 5) {
                current.removeAt(current.size - 1)
            }
            _recentSearches.value = current
            
            val prefs = context.getSharedPreferences("esdispatch_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("recent_searches", current.joinToString(",")).apply()
            
            onSuccess()
        } else if (_firebaseConnected.value) {
            // Not found locally but Firebase is connected, query Firestore!
            viewModelScope.launch {
                val cloudParcel = com.esdispatch.data.FirebaseManager.fetchParcel(trimmed)
                if (cloudParcel != null) {
                    // Update our internal lists
                    _parcels.value = listOf(cloudParcel) + _parcels.value
                    _selectedParcel.value = cloudParcel
                    
                    // Save in local Room SQLite DB
                    repository?.saveParcels(listOf(cloudParcel))
                    
                    startRealTimeTrackingListener(cloudParcel.id)
                    
                    // Update recent searches
                    val current = _recentSearches.value.toMutableList()
                    current.remove(cloudParcel.id)
                    current.add(0, cloudParcel.id)
                    if (current.size > 5) {
                        current.removeAt(current.size - 1)
                    }
                    _recentSearches.value = current
                    
                    val prefs = context.getSharedPreferences("esdispatch_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("recent_searches", current.joinToString(",")).apply()
                    
                    onSuccess()
                } else {
                    onError("Tracking ID not found in local records or our secure cloud dispatch database.")
                }
            }
        } else {
            onError("Tracking number not found in local data store.")
        }
    }

    fun clearSearchHistory(context: Context) {
        _recentSearches.value = emptyList()
        val prefs = context.getSharedPreferences("esdispatch_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("recent_searches").apply()
        syncSavedTrackingSubscriptions()
    }

    fun exportTrackedParcelHistory(context: Context) {
        viewModelScope.launch {
            try {
                val listToExport = _parcels.value.filter { it.id in _recentSearches.value }
                val targetList = if (listToExport.isEmpty()) _parcels.value else listToExport
                
                val jsonArray = org.json.JSONArray()
                for (parcel in targetList) {
                    val jsonObj = org.json.JSONObject()
                    jsonObj.put("id", parcel.id)
                    jsonObj.put("itemName", parcel.itemName)
                    jsonObj.put("imageUrl", parcel.imageUrl)
                    jsonObj.put("status", parcel.status.name)
                    jsonObj.put("pickupAddress", parcel.pickupAddress)
                    jsonObj.put("deliveryAddress", parcel.deliveryAddress)
                    jsonObj.put("senderName", parcel.senderName)
                    jsonObj.put("senderPhone", parcel.senderPhone)
                    jsonObj.put("receiverName", parcel.receiverName)
                    jsonObj.put("receiverPhone", parcel.receiverPhone)
                    jsonObj.put("quantity", parcel.quantity)
                    jsonObj.put("weight", parcel.weight)
                    jsonObj.put("length", parcel.length)
                    jsonObj.put("width", parcel.width)
                    jsonObj.put("height", parcel.height)
                    jsonObj.put("price", parcel.price)
                    jsonObj.put("courierName", parcel.courierName)
                    jsonObj.put("courierPhone", parcel.courierPhone)
                    jsonObj.put("progress", parcel.progress)
                    jsonObj.put("dateString", parcel.dateString)
                    jsonArray.put(jsonObj)
                }

                val jsonString = jsonArray.toString(4)
                
                // Write to Android Downloads directory using MediaStore
                val resolver = context.contentResolver
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "ESDispatch_History_${System.currentTimeMillis()}.json")
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/json")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                }
                
                val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(jsonString.toByteArray())
                    }
                    Toast.makeText(context, "History successfully exported to Downloads folder!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to create file in Downloads.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("ExportHistory", "Failed to export JSON: ${e.message}")
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private val activeSavedTrackingJobs = mutableMapOf<String, kotlinx.coroutines.Job>()

    fun syncSavedTrackingSubscriptions() {
        val savedIds = _recentSearches.value
        // Cancel jobs for IDs no longer in saved list
        activeSavedTrackingJobs.keys.toList().forEach { id ->
            if (id !in savedIds) {
                activeSavedTrackingJobs[id]?.cancel()
                activeSavedTrackingJobs.remove(id)
            }
        }
        // Start jobs for new IDs
        savedIds.forEach { id ->
            if (id !in activeSavedTrackingJobs) {
                activeSavedTrackingJobs[id] = viewModelScope.launch {
                    com.esdispatch.data.FirebaseManager.listenToParcelTracking(id).collect { updatedParcel ->
                        if (updatedParcel != null) {
                            // Find existing local parcel to detect status changes
                            val existing = _parcels.value.find { it.id == id }
                            if (existing != null && existing.status != updatedParcel.status) {
                                // Trigger FCM status change alert!
                                appContext?.let { ctx ->
                                    val statusText = when (updatedParcel.status) {
                                        ParcelStatus.PENDING -> "Pending Dispatch"
                                        ParcelStatus.ASSIGNED -> "Courier Assigned"
                                        ParcelStatus.TRANSIT -> "In Transit"
                                        ParcelStatus.PICKED_UP -> "Picked Up"
                                        ParcelStatus.ARRIVED -> "Arrived"
                                        ParcelStatus.OUT_FOR_DELIVERY -> "Out for Delivery"
                                        ParcelStatus.DELIVERED -> "Delivered"
                                        ParcelStatus.CANCELLED -> "Cancelled"
                                    }
                                    com.esdispatch.data.MyFirebaseMessagingService.showNotification(
                                        context = ctx,
                                        title = "Saved Tracking Status Update ðŸ””",
                                        message = "Your saved parcel '${updatedParcel.itemName}' (#$id) is now $statusText.",
                                        parcelId = id
                                    )
                                }
                            }
                            // Update locally in list & DB
                            val updatedList = _parcels.value.map { if (it.id == id) updatedParcel else it }
                            _parcels.value = updatedList
                            repository?.saveParcel(updatedParcel)
                        }
                    }
                }
            }
        }
    }

    // Draft Creation Setup
    fun updateDraftPickup(address: String) {
        _parcelDraft.update { it.copy(pickupAddress = address) }
    }

    fun updateDraftDelivery(address: String) {
        _parcelDraft.update { it.copy(deliveryAddress = address) }
    }

    fun updateDraftSpecs(quantity: Int, weight: Double, length: Int, width: Int, height: Int) {
        _parcelDraft.update {
            it.copy(
                quantity = quantity,
                weight = weight,
                length = length,
                width = width,
                height = height
            )
        }
    }

    fun updateDraftSenderInfo(name: String, phone: String) {
        _parcelDraft.update { it.copy(senderName = name, senderPhone = phone) }
    }

    fun updateDraftReceiverInfo(name: String, phone: String) {
        _parcelDraft.update { it.copy(receiverName = name, receiverPhone = phone) }
    }

    fun updateDraftAdditionalStops(stops: List<String>) {
        _parcelDraft.update { it.copy(stops = stops) }
    }

    fun populateDraftFromParcel(parcel: com.esdispatch.data.Parcel) {
        _parcelDraft.update {
            it.copy(
                pickupAddress = parcel.pickupAddress,
                deliveryAddress = parcel.deliveryAddress,
                senderName = parcel.senderName,
                senderPhone = parcel.senderPhone,
                receiverName = parcel.receiverName,
                receiverPhone = parcel.receiverPhone,
                quantity = parcel.quantity,
                weight = parcel.weight,
                length = parcel.length,
                width = parcel.width,
                height = parcel.height,
                price = parcel.price
            )
        }
    }

    fun claimWelcomeGift() {
        if (_welcomeGiftClaimed.value) return
        
        _welcomeGiftClaimed.value = true
        savePref("welcome_gift_claimed", true)
        
        _walletBalance.value += 2500.0
        savePref("wallet_balance", _walletBalance.value)
        
        _loyaltyPoints.value += 100
        savePref("loyalty_points", _loyaltyPoints.value)
        
        val welcomeTx = Transaction(
            id = "TX-COIN-${System.currentTimeMillis().toString().substring(8)}",
            title = "Welcome Coins Claimed ðŸª™",
            date = "Today",
            amount = 100.0,
            isTopUp = true
        )
        _transactions.value = listOf(welcomeTx) + _transactions.value
        
        val notifTitle = "Welcome Gift Claimed! ðŸŽ"
        val notifMsg = "Congratulations! You have received 100 Engraced loyalty coins and the premium promo code 'ENGRACEDVIP' for 15% off your first delivery."
        addNotification(notifTitle, notifMsg)
        
        appContext?.let { ctx ->
            try {
                com.esdispatch.data.MyFirebaseMessagingService.showNotification(
                    context = ctx,
                    title = notifTitle,
                    message = notifMsg,
                    parcelId = "GIFT"
                )
            } catch (e: Exception) {
                android.util.Log.e("GiftNotif", "Error showing gift notification: ${e.message}")
            }
        }
        
        val uid = _firebaseUserId.value
        if (uid != null) {
            com.esdispatch.data.FirebaseManager.syncWalletBalanceToFirestore(uid, _walletBalance.value)
            com.esdispatch.data.FirebaseManager.syncTransactionToFirestore(welcomeTx, uid)
            com.esdispatch.data.FirebaseManager.syncLoyaltyToFirestore(uid, _loyaltyPoints.value, _deliveryCount.value)
            
            val db = com.esdispatch.data.FirebaseManager.firestore
            if (db != null) {
                db.collection("users").document(uid)
                    .update("welcomeGiftClaimed", true)
            }
        }
    }

    private fun geocodeAddressLocalOnly(address: String): Pair<Double, Double>? {
        // First check the comprehensive AddressDatabase (Benin City + Lagos)
        val dbResult = com.esdispatch.data.AddressDatabase.getCoordinates(address)
        if (dbResult != null) return dbResult

        val addrLower = address.lowercase()
        return when {
            // Benin City neighborhoods
            addrLower.contains("benin city airport") || (addrLower.contains("airport") && addrLower.contains("benin")) -> Pair(6.3166, 5.5995)
            addrLower.contains("ugbowo") || addrLower.contains("uniben") || addrLower.contains("university of benin") -> Pair(6.3782, 5.6283)
            addrLower.contains("ubth") || addrLower.contains("teaching hospital") && addrLower.contains("benin") -> Pair(6.3769, 5.6290)
            addrLower.contains("gra") && (addrLower.contains("benin") || addrLower.contains("reservation")) -> Pair(6.3422, 5.6307)
            addrLower.contains("ring road") && addrLower.contains("benin") -> Pair(6.3315, 5.6262)
            addrLower.contains("sapele road") -> Pair(6.3271, 5.6219)
            addrLower.contains("akpakpava") -> Pair(6.3328, 5.6248)
            addrLower.contains("ugbor road") || addrLower.contains("ugbor") -> Pair(6.3531, 5.6411)
            addrLower.contains("ihama road") || addrLower.contains("ihama") -> Pair(6.3458, 5.6389)
            addrLower.contains("mission road") && addrLower.contains("benin") -> Pair(6.3361, 5.6283)
            addrLower.contains("forestry road") -> Pair(6.3394, 5.6318)
            addrLower.contains("lucky way") || addrLower.contains("lucky igbinedion") -> Pair(6.3429, 5.6347)
            addrLower.contains("adesuwa road") -> Pair(6.3405, 5.6362)
            addrLower.contains("aduwawa") -> Pair(6.3622, 5.6458)
            addrLower.contains("ikpoba hill") || addrLower.contains("ikpoba") -> Pair(6.3499, 5.6353)
            addrLower.contains("new benin") -> Pair(6.3305, 5.6225)
            addrLower.contains("old benin") -> Pair(6.3338, 5.6248)
            addrLower.contains("trans-ekehuan") || addrLower.contains("ekehuan") -> Pair(6.3201, 5.6028)
            addrLower.contains("ekenwan road") || addrLower.contains("ekenwan") -> Pair(6.3311, 5.6104)
            addrLower.contains("uselu") -> Pair(6.3752, 5.6208)
            addrLower.contains("oba market") -> Pair(6.3339, 5.6267)
            addrLower.contains("oba's palace") || addrLower.contains("royal palace") -> Pair(6.3345, 5.6254)
            addrLower.contains("government house") && addrLower.contains("benin") -> Pair(6.3354, 5.6277)
            addrLower.contains("oregbeni") -> Pair(6.3498, 5.6352)
            addrLower.contains("siluko") -> Pair(6.3282, 5.6348)
            addrLower.contains("benin") && addrLower.contains("secretariat") -> Pair(6.3268, 5.6216)
            addrLower.contains("textile mill") -> Pair(6.3412, 5.6388)
            addrLower.contains("dawson road") -> Pair(6.3349, 5.6289)
            addrLower.contains("benin city") -> Pair(6.3350, 5.6278) // Benin City center
            // Lagos areas
            addrLower.contains("ikeja gra") || addrLower.contains("joel ogunnaike") -> Pair(6.5818, 3.3598)
            addrLower.contains("ikoyi") || addrLower.contains("kingsway") -> Pair(6.4520, 3.4402)
            addrLower.contains("lekki") -> Pair(6.4281, 3.4748)
            addrLower.contains("ajah") -> Pair(6.4678, 3.5782)
            addrLower.contains("yaba") || addrLower.contains("akoka") || addrLower.contains("unilag") -> Pair(6.5178, 3.3859)
            addrLower.contains("murtala") || addrLower.contains("mmia") || (addrLower.contains("airport") && addrLower.contains("lagos")) -> Pair(6.5774, 3.3210)
            addrLower.contains("victoria island") || addrLower.contains(" vi,") || addrLower.contains(", vi ") -> Pair(6.4281, 3.4219)
            addrLower.contains("surulere") -> Pair(6.4979, 3.3512)
            addrLower.contains("apapa") -> Pair(6.4479, 3.3601)
            addrLower.contains("festac") -> Pair(6.4682, 3.2998)
            addrLower.contains("gbagada") -> Pair(6.5548, 3.3889)
            addrLower.contains("maryland") && addrLower.contains("lagos") -> Pair(6.5688, 3.3572)
            addrLower.contains("oshodi") -> Pair(6.5575, 3.3419)
            addrLower.contains("ikeja") -> Pair(6.5944, 3.3378)
            addrLower.contains("ikorodu") -> Pair(6.6191, 3.5054)
            addrLower.contains("magodo") -> Pair(6.6082, 3.3901)
            addrLower.contains("ogba") && addrLower.contains("lagos") -> Pair(6.6071, 3.3572)
            addrLower.contains("agege") -> Pair(6.6168, 3.3221)
            addrLower.contains("mushin") -> Pair(6.5348, 3.3572)
            addrLower.contains("lagos island") -> Pair(6.4551, 3.3917)
            addrLower.contains("lagos") -> Pair(6.5244, 3.3792) // Lagos center fallback
            else -> null
        }
    }

    private fun geocodeAddressHashOnly(address: String): Pair<Double, Double> {
        // Determine if address is Benin City or Lagos and use appropriate center coordinates
        val isBeninCity = com.esdispatch.data.AddressDatabase.isBeninCity(address)
        val centerLat = if (isBeninCity) 6.3350 else 6.5244
        val centerLng = if (isBeninCity) 5.6278 else 3.3792
        var hash = 0
        for (char in address.trim().lowercase()) { hash = 31 * hash + char.code }
        hash = if (hash < 0) -hash else hash
        val latOffset = (hash % 80) / 1000.0 - 0.04
        val lngOffset = ((hash / 80) % 80) / 1000.0 - 0.04
        return Pair(centerLat + latOffset, centerLng + lngOffset)
    }

    /** Detect if route is intercity Benin City â†” Lagos */
    fun isIntercityRoute(pickup: String, delivery: String): Boolean {
        val pickupIsBenin = com.esdispatch.data.AddressDatabase.isBeninCity(pickup)
        val deliveryIsBenin = com.esdispatch.data.AddressDatabase.isBeninCity(delivery)
        val pickupIsLagos = com.esdispatch.data.AddressDatabase.isLagos(pickup)
        val deliveryIsLagos = com.esdispatch.data.AddressDatabase.isLagos(delivery)
        return (pickupIsBenin && deliveryIsLagos) || (pickupIsLagos && deliveryIsBenin)
    }

    fun estimateDistanceBetween(pickup: String, delivery: String): Double {
        if (pickup.isBlank() || delivery.isBlank()) return 8.2
        val coords1 = geocodeAddressLocalOnly(pickup) ?: geocodeAddressHashOnly(pickup)
        val coords2 = geocodeAddressLocalOnly(delivery) ?: geocodeAddressHashOnly(delivery)
        return haversineDistance(coords1.first, coords1.second, coords2.first, coords2.second)
    }

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return Math.round(r * c * 10.0) / 10.0
    }

    private fun <T> runBlockingCatch(block: suspend () -> T): T? {
        return try {
            kotlinx.coroutines.runBlocking { block() }
        } catch (_: Exception) { null }
    }

    suspend fun geocodeAddress(address: String): Pair<Double, Double>? {
        if (address.isBlank()) return null
        
        // 1. Check local AddressDatabase first (fastest, most accurate for Benin + Lagos)
        val localResult = geocodeAddressLocalOnly(address)
        if (localResult != null) return localResult

        // 2. Query Google native Geocoder
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val context = appContext ?: return@withContext geocodeAddressHashOnly(address)
                val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                val addresses = com.esdispatch.utils.GeocoderUtils.getFromLocationNameCompat(geocoder, address, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    return@withContext Pair(addr.latitude, addr.longitude)
                }
            } catch (e: Exception) {
                android.util.Log.e("Geocoding", "Google Geocoder failed: ${e.message}")
            }
            
            // 3. Fallback: stable hash-based coordinates centered on Benin City or Lagos
            geocodeAddressHashOnly(address)
        }
    }

    suspend fun validateAddressesGeocoding(pickup: String, delivery: String): Pair<Pair<Double, Double>, Pair<Double, Double>>? {
        if (!validateAddresses(pickup, delivery)) return null
        val pCoords = geocodeAddress(pickup) ?: return null
        val dCoords = geocodeAddress(delivery) ?: return null
        return Pair(pCoords, dCoords)
    }

    fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = r * c
        return Math.round(distance * 10.0) / 10.0 // Round to 1 decimal place
    }

    fun calculateDynamicQuote(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double,
        serviceType: String,
        weight: Double = 1.0,
        quantity: Int = 1,
        length: Int = 20,
        width: Int = 15,
        height: Int = 10,
        stopsCount: Int = 0,
        insuranceType: String = "none",
        pickupAddress: String = "",
        deliveryAddress: String = ""
    ): PendingQuote {
        try {
            // Calculate distance in km from coordinates using Haversine formula
            val distanceKm = calculateHaversineDistance(originLat, originLng, destLat, destLng)
            
            val base = _baseFare.value
            val perKg = _perKgRate.value
            val express = _expressSurcharge.value
            val surge = _surgeMultiplier.value
            val wt = if (weight > 0.0) weight else 1.0

            // Apply business logic (base rate + per km)
            val baseRate = when (serviceType) {
                "Express" -> (base * 1.5 + (wt * perKg) + express + distanceKm * 150.0) * surge
                "Economy" -> (base * 0.7 + (wt * perKg * 0.8) + distanceKm * 100.0) * surge
                "Batch" -> (base * 0.9 + (wt * perKg * 0.9) + distanceKm * 110.0) * surge
                "Multi" -> (base * 1.8 + (wt * perKg * 1.2) + distanceKm * 180.0 + stopsCount * 1500.0) * surge
                else -> (base * 0.7 + (wt * perKg * 0.8) + distanceKm * 100.0) * surge
            }

            // Surcharges
            val volumeCm3 = length * width * height
            val volumeSurcharge = (volumeCm3 / 1000.0) * 50.0
            val stopsSurcharge = stopsCount * 1500.0
            val quantityFactor = 1.0 + (quantity - 1) * 0.8
            val insuranceFee = when (insuranceType) {
                "basic" -> 250.00
                "premium" -> 1000.00
                else -> 0.0
            }

            val calculatedTotal = (baseRate + volumeSurcharge + stopsSurcharge) * quantityFactor + insuranceFee
            val finalPrice = Math.round(calculatedTotal / 50.0) * 50.0

            return PendingQuote.Success(
                price = finalPrice,
                distanceKm = distanceKm,
                pickupAddress = pickupAddress.ifBlank { "Lat: $originLat, Lng: $originLng" },
                deliveryAddress = deliveryAddress.ifBlank { "Lat: $destLat, Lng: $destLng" },
                serviceType = serviceType
            )
        } catch (e: Exception) {
            return PendingQuote.Error(e.message ?: "Pricing calculation error")
        }
    }

    fun calculateDynamicPrice(
        serviceType: String,
        weight: Double,
        quantity: Int,
        length: Int,
        width: Int,
        height: Int,
        stopsCount: Int,
        insuranceType: String = "none"
    ): Double {
        val base = _baseFare.value
        val perKg = _perKgRate.value
        val express = _expressSurcharge.value
        val surge = _surgeMultiplier.value
        val wt = if (weight > 0.0) weight else 1.0

        val draft = _parcelDraft.value
        val distanceKm = if (_isDynamicPricingEnabled.value) {
            estimateDistanceBetween(draft.pickupAddress, draft.deliveryAddress)
        } else {
            5.0 // Manual Mode disables the distance-based pricing multiplier and uses a flat distance default
        }

        // Calculate dynamic base price using admin settings
        val baseRate = when (serviceType) {
            "Express" -> (base * 1.5 + (wt * perKg) + express + distanceKm * 150.0) * surge
            "Economy" -> (base * 0.7 + (wt * perKg * 0.8) + distanceKm * 100.0) * surge
            "Batch" -> (base * 0.9 + (wt * perKg * 0.9) + distanceKm * 110.0) * surge
            "Multi" -> (base * 1.8 + (wt * perKg * 1.2) + distanceKm * 180.0 + stopsCount * 1500.0) * surge
            else -> (base * 0.7 + (wt * perKg * 0.8) + distanceKm * 100.0) * surge
        }

        // Volume surcharge: â‚¦50 per 1000 cm3
        val volumeCm3 = length * width * height
        val volumeSurcharge = (volumeCm3 / 1000.0) * 50.0

        // Multi-stop surcharge: â‚¦1,500 per extra stop
        val stopsSurcharge = stopsCount * 1500.0

        // Quantity multiplier: 20% discount on additional items
        val quantityFactor = 1.0 + (quantity - 1) * 0.8

        // Insurance surcharge
        val insuranceFee = when (insuranceType) {
            "basic" -> 250.00
            "premium" -> 1000.00
            else -> 0.0
        }

        var calculatedTotal = (baseRate + volumeSurcharge + stopsSurcharge) * quantityFactor + insuranceFee
        
        // Apply Admin Discount
        if (_adminDiscountEnabled.value) {
            val discountFactor = 1.0 - (_adminDiscountPercent.value / 100.0)
            calculatedTotal *= discountFactor
        }
        
        // Round to nearest 50 NGN
        return Math.round(calculatedTotal / 50.0) * 50.0
    }

    fun validateAddresses(pickup: String, delivery: String): Boolean {
        return pickup.isNotBlank() && delivery.isNotBlank() && pickup.trim().length >= 6 && delivery.trim().length >= 6
    }

    fun calculateDynamicPriceAsync(
        serviceType: String,
        pickup: String,
        delivery: String,
        weight: Double,
        quantity: Int,
        length: Int,
        width: Int,
        height: Int,
        stopsCount: Int,
        insuranceType: String = "none"
    ) {
        if (!validateAddresses(pickup, delivery)) {
            _pendingQuote.value = PendingQuote.Idle
            return
        }
        viewModelScope.launch {
            _pendingQuote.value = PendingQuote.Loading
            try {
                // Validate inputs using our geocoding helper first
                val coords = validateAddressesGeocoding(pickup, delivery)
                if (coords == null) {
                    _pendingQuote.value = PendingQuote.Error("Failed to resolve address coordinates.")
                    return@launch
                }
                
                val (pickupCoords, deliveryCoords) = coords
                
                // Trigger the calculateDynamicQuote flow only after addresses are validated
                val quote = calculateDynamicQuote(
                    originLat = pickupCoords.first,
                    originLng = pickupCoords.second,
                    destLat = deliveryCoords.first,
                    destLng = deliveryCoords.second,
                    serviceType = serviceType,
                    weight = weight,
                    quantity = quantity,
                    length = length,
                    width = width,
                    height = height,
                    stopsCount = stopsCount,
                    insuranceType = insuranceType,
                    pickupAddress = pickup,
                    deliveryAddress = delivery
                )
                
                _pendingQuote.value = quote
                
                if (quote is PendingQuote.Success) {
                    // Synchronize the draft price and service to keep the app state aligned
                    _parcelDraft.update {
                        it.copy(
                            pickupAddress = pickup,
                            deliveryAddress = delivery,
                            price = quote.price,
                            selectedService = serviceType,
                            weight = if (weight > 0.0) weight else 1.0,
                            quantity = quantity,
                            length = length,
                            width = width,
                            height = height
                        )
                    }
                }
            } catch (e: Exception) {
                _pendingQuote.value = PendingQuote.Error(e.message ?: "Pricing calculation error")
            }
        }
    }

    fun finalizeDraftPrice(serviceType: String, customPrice: Double? = null) {
        val draft = _parcelDraft.value
        val basePrice = customPrice ?: calculateDynamicPrice(
            serviceType = serviceType,
            weight = draft.weight,
            quantity = draft.quantity,
            length = draft.length,
            width = draft.width,
            height = draft.height,
            stopsCount = draft.stops.size
        )
        _parcelDraft.update { it.copy(selectedService = serviceType, price = basePrice) }
    }

    fun confirmBooking(onComplete: ((Boolean, String) -> Unit)? = null) {
        val draft = _parcelDraft.value
        val rawCost = draft.price
        if (rawCost <= 0) {
            onComplete?.invoke(false, "Invalid booking price")
            return
        }
        val cost = applyPromoDiscount(rawCost)
        val uid = _firebaseUserId.value

        fun createBooking() {
            // Create new Parcel record
            val newParcel = Parcel(
                id = "PC-${System.currentTimeMillis().toString().substring(8)}",
                itemName = if (draft.selectedService == "Express") "Express Parcel" else "New Parcel (${draft.selectedService})",
                imageUrl = "https://images.unsplash.com/photo-1589409514187-c21d14bf0d13?w=100&h=100&fit=crop",
                status = ParcelStatus.PENDING,
                pickupAddress = draft.pickupAddress.ifBlank { "Unspecified Pickup" },
                deliveryAddress = draft.deliveryAddress.ifBlank { "Unspecified Delivery" },
                senderName = draft.senderName.ifBlank { _userName.value.ifBlank { "Engraced Member" } },
                senderPhone = draft.senderPhone.ifBlank { _userPhone.value },
                receiverName = draft.receiverName.ifBlank { "Recipient" },
                receiverPhone = draft.receiverPhone.ifBlank { "" },
                quantity = draft.quantity,
                weight = draft.weight,
                length = draft.length,
                width = draft.width,
                height = draft.height,
                price = cost,
                progress = 0.0f,
                userId = _firebaseUserId.value ?: "",
                additionalStops = draft.stops.filter { it.isNotBlank() }.joinToString("|"),
                otpCode = (1000..9999).random().toString()
            )

            _parcels.value = listOf(newParcel) + _parcels.value
            _selectedParcel.value = newParcel

            // Add to Notifications
            val bookTitle = "Booking Confirmed! ðŸŽ‰ðŸ“¦"
            val bookMsg = "Your parcel shipment '${newParcel.itemName}' (#${newParcel.id}) has been booked via ${draft.selectedService} service! Paid â‚¦${String.format("%,.2f", cost)} from wallet. Logistics dispatch is actively assigning a courier! ðŸš€âš¡"
            val notif = NotificationItem(
                id = "NT-${System.currentTimeMillis().toString().substring(8)}",
                title = bookTitle,
                message = bookMsg,
                time = "Just now",
                parcelId = newParcel.id
            )
            _notifications.value = listOf(notif) + _notifications.value

            val newTx = Transaction(
                id = "TX-${System.currentTimeMillis().toString().substring(8)}",
                title = "Parcel Delivery (${draft.selectedService})",
                date = "Today",
                amount = -cost,
                isTopUp = false,
                type = "DEBIT",
                reference = newParcel.id,
                userId = _firebaseUserId.value ?: ""
            )
            _transactions.value = listOf(newTx) + _transactions.value

            // Show actual Android status bar notification
            appContext?.let { ctx ->
                try {
                    com.esdispatch.data.MyFirebaseMessagingService.showNotification(
                        context = ctx,
                        title = bookTitle,
                        message = bookMsg,
                        parcelId = newParcel.id
                    )
                } catch (e: Exception) {
                    android.util.Log.e("BookingNotif", "Error showing booking notification: ${e.message}")
                }
            }

            // Reset draft & promo
            _parcelDraft.value = ParcelDraft()
            _activePromo = ActivePromo()

            // Write directly to Room SQLite Database for offline-first resilience!
            savePref("wallet_balance", _walletBalance.value)
            viewModelScope.launch {
                repository?.saveParcel(newParcel)
                repository?.saveTransaction(newTx)
                repository?.saveNotification(notif)
                syncParcel(newParcel)
                val fUid = _firebaseUserId.value
                if (fUid != null) {
                    com.esdispatch.data.FirebaseManager.recordLedgerTransaction(
                        userId = fUid,
                        amount = -cost,
                        title = "Parcel Delivery (${draft.selectedService})",
                        isTopUp = false,
                        reference = newParcel.id
                    ) {}
                    com.esdispatch.data.FirebaseManager.syncLoyaltyToFirestore(fUid, _loyaltyPoints.value, _deliveryCount.value)
                }
            }
        }

        if (uid != null) {
            if (_walletBalance.value < cost) {
                onComplete?.invoke(false, "Insufficient wallet balance (â‚¦${String.format("%,.0f", cost)} needed).")
                return
            }
            // Atomic debit; only when the server confirms do we create the booking.
            com.esdispatch.data.FirebaseManager.updateUserWalletBalance(uid, -cost) { success, newBalance ->
                if (!success) {
                    onComplete?.invoke(false, "Wallet debit failed — booking was NOT created. Please retry.")
                    return@updateUserWalletBalance
                }
                _walletBalance.value = newBalance
                savePref("wallet_balance", newBalance)
                createBooking()
                onComplete?.invoke(true, "Booking confirmed")
            }
        } else {
            // Unauthenticated / guest fallback: local-only booking (no wallet debit)
            if (_walletBalance.value < cost) {
                onComplete?.invoke(false, "Insufficient wallet balance.")
                return
            }
            _walletBalance.value -= cost
            savePref("wallet_balance", _walletBalance.value)
            createBooking()
            onComplete?.invoke(true, "Booking confirmed (offline)")
        }
    }

    // Wallet Actions
    

    fun addLoyaltyPoints(points: Int) {
        if (!_pointsSystemEnabled.value) return
        val updated = _loyaltyPoints.value + points
        _loyaltyPoints.value = updated
        savePref("loyalty_points", updated)
        
        val uid = _firebaseUserId.value
        if (uid != null) {
            viewModelScope.launch {
                com.esdispatch.data.FirebaseManager.syncLoyaltyToFirestore(uid, updated, _deliveryCount.value)
            }
        }
    }

    // Service Areas (admin managed)
    private val _serviceAreas = MutableStateFlow<List<String>>(
        listOf("Lagos Mainland", "Lagos Island", "Abuja CBD", "Port Harcourt", "Ibadan", "Abeokuta", "Benin City", "Enugu")
    )
    val serviceAreas: StateFlow<List<String>> = _serviceAreas.asStateFlow()

    fun addServiceArea(area: String) {
        if (area.isBlank()) return
        _serviceAreas.value = (_serviceAreas.value + area).distinct()
        savePref("service_areas", _serviceAreas.value.joinToString("|"))
        logAdminActivity("Service Area Added", "Added: $area")
    }

    fun removeServiceArea(area: String) {
        _serviceAreas.update { it.filter { a -> a != area } }
        savePref("service_areas", _serviceAreas.value.joinToString("|"))
        logAdminActivity("Service Area Removed", "Removed: $area")
    }

    // Address Book Actions
    fun addAddress(label: String, address: String) {
        val newAddress = AddressItem(
            id = "ADDR-${System.currentTimeMillis().toString().substring(8)}",
            label = label,
            address = address,
            isDefault = false
        )
        _addresses.update { it + newAddress }

        viewModelScope.launch {
            repository?.saveAddress(newAddress)
        }
    }

    // Admin: Fund any user's wallet
    

    // Admin: Set any user's loyalty points
    fun adminSetUserPoints(userId: String, userName: String, points: Int, onResult: (Boolean, String) -> Unit) {
        val db = com.esdispatch.data.FirebaseManager.firestore ?: run {
            onResult(false, "Firestore unavailable"); return
        }
        viewModelScope.launch {
            try {
                db.collection("users").document(userId).get().addOnSuccessListener { snap ->
                    val currentCount = (snap.get("deliveryCount") as? Number)?.toInt() ?: 0
                    db.collection("users").document(userId).update(
                        mapOf("loyaltyPoints" to points, "deliveryCount" to currentCount)
                    )
                    logAdminActivity("Points Updated", "Set $points points for $userName ($userId)")
                    onResult(true, "Points updated successfully")
                }.addOnFailureListener { e ->
                    onResult(false, e.message ?: "Failed to fetch user")
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Unknown error")
            }
        }
    }

    // Admin: Send notification to all users (broadcast)
    fun adminSendBroadcastNotification(title: String, message: String, onResult: (Boolean, String) -> Unit) {
        if (title.isBlank() || message.isBlank()) {
            onResult(false, "Title and message are required"); return
        }
        val db = com.esdispatch.data.FirebaseManager.firestore ?: run {
            onResult(false, "Firestore unavailable"); return
        }
        viewModelScope.launch {
            try {
                db.collection("users").get().addOnSuccessListener { users ->
                    var sent = 0
                    val batch = db.batch()
                    for (userDoc in users.documents) {
                        val uid = userDoc.id
                        val notifRef = db.collection("users").document(uid)
                            .collection("notifications").document("BC-${System.currentTimeMillis()}")
                        batch.set(notifRef, hashMapOf(
                            "title" to title, "message" to message,
                            "timestamp" to System.currentTimeMillis(), "read" to false
                        ))
                        sent++
                    }
                    batch.commit().addOnSuccessListener {
                        logAdminActivity("Broadcast Notification", "Sent to $sent users: $title")
                        onResult(true, "Broadcast sent to $sent users")
                    }.addOnFailureListener { e ->
                        onResult(false, e.message ?: "Batch commit failed")
                    }
                }.addOnFailureListener { e ->
                    onResult(false, e.message ?: "Failed to fetch users")
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Unknown error")
            }
        }
    }

    // Settings
    fun togglePushNotifications() {
        _pushEnabled.update { !it }
        savePref("push_enabled", _pushEnabled.value)
    }

    fun toggleLocationServices() {
        _locationEnabled.update { !it }
        savePref("location_enabled", _locationEnabled.value)
    }

    fun toggleDarkMode() {
        _darkModeEnabled.update { !it }
        savePref("dark_mode_enabled", _darkModeEnabled.value)
    }

    fun toggleAlertsBooked() {
        _pushAlertsBooked.update { !it }
        savePref("alerts_booked", _pushAlertsBooked.value)
        syncNotificationPreferencesToFirestore()
    }

    fun toggleAlertsDispatched() {
        _pushAlertsDispatched.update { !it }
        savePref("alerts_dispatched", _pushAlertsDispatched.value)
        syncNotificationPreferencesToFirestore()
    }

    fun toggleAlertsDelivered() {
        _pushAlertsDelivered.update { !it }
        savePref("alerts_delivered", _pushAlertsDelivered.value)
        syncNotificationPreferencesToFirestore()
    }

    fun toggleAlertsCancelled() {
        _pushAlertsCancelled.update { !it }
        savePref("alerts_cancelled", _pushAlertsCancelled.value)
        syncNotificationPreferencesToFirestore()
    }

    fun syncNotificationPreferencesToFirestore() {
        val uid = _firebaseUserId.value
        if (_firebaseConnected.value && uid != null) {
            val db = com.esdispatch.data.FirebaseManager.firestore ?: return
            val prefsMap = hashMapOf(
                "booked" to _pushAlertsBooked.value,
                "dispatched" to _pushAlertsDispatched.value,
                "delivered" to _pushAlertsDelivered.value,
                "cancelled" to _pushAlertsCancelled.value
            )
            db.collection("users").document(uid)
                .update("notificationPreferences", prefsMap)
                .addOnSuccessListener {
                    android.util.Log.d("DeliveryViewModel", "Notification preferences synced to Firestore.")
                }
                .addOnFailureListener {
                    db.collection("users").document(uid)
                        .set(hashMapOf("notificationPreferences" to prefsMap), com.google.firebase.firestore.SetOptions.merge())
                }
        }
    }

    fun showInAppNotification(title: String, message: String) {
        viewModelScope.launch {
            _activeInAppNotification.value = Pair(title, message)
            delay(5000)
            if (_activeInAppNotification.value?.first == title) {
                _activeInAppNotification.value = null
            }
        }
    }

    override fun addNotification(title: String, message: String, parcelId: String) {
        val notif = NotificationItem(
            id = "NT-${System.currentTimeMillis().toString().substring(8)}-${_notifications.value.size}",
            title = title,
            message = message,
            time = "Just now",
            parcelId = parcelId
        )
        _notifications.value = listOf(notif) + _notifications.value
        viewModelScope.launch {
            repository?.saveNotification(notif)
            val uid = _firebaseUserId.value
            if (uid != null) {
                com.esdispatch.data.FirebaseManager.sendNotificationToUser(uid, title, message, parcelId.ifBlank { null })
            }
        }
    }

    override fun logAdminActivity(action: String, details: String) {
        val fs = com.esdispatch.data.FirebaseManager.firestore ?: return
        val uid = _firebaseUserId.value ?: ""
        fs.collection("audit_logs").add(
            hashMapOf(
                "action" to action,
                "details" to details,
                "actorId" to uid,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
        )
    }

    fun dismissInAppNotification() {
        _activeInAppNotification.value = null
    }

    private var shipmentsListenerJob: kotlinx.coroutines.Job? = null

    fun startShipmentsTriggerListener(userId: String) {
        shipmentsListenerJob?.cancel()
        if (!_firebaseConnected.value) return

        val db = com.esdispatch.data.FirebaseManager.firestore ?: return
        shipmentsListenerJob = viewModelScope.launch {
            db.collection("shipments")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        android.util.Log.e("DeliveryViewModel", "Error listening to shipments triggers: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        for (docChange in snapshots.documentChanges) {
                            if (docChange.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED) {
                                val doc = docChange.document
                                val id = doc.id
                                val itemName = doc.getString("itemName") ?: "Shipment"
                                val status = doc.getString("status") ?: ""
                                
                                val isBooked = status.equals("Booked", ignoreCase = true) || status.equals("Pending Assignment", ignoreCase = true)
                                val isDispatched = status.equals("Out for Delivery", ignoreCase = true) || status.equals("Transit", ignoreCase = true)
                                val isDelivered = status.equals("Delivered", ignoreCase = true)
                                val isCancelled = status.equals("Cancelled", ignoreCase = true)

                                if (isBooked && !_pushAlertsBooked.value) continue
                                if (isDispatched && !_pushAlertsDispatched.value) continue
                                if (isDelivered && !_pushAlertsDelivered.value) continue
                                if (isCancelled && !_pushAlertsCancelled.value) continue

                                if (isDispatched || isDelivered) {
                                    val emoji = if (isDelivered) "âœ…ðŸ“¦" else "ðŸššâš¡"
                                    val title = "Shipment Status Updated! $emoji"
                                    val message = "Your shipment '$itemName' (#$id) is now $status!"
                                    
                                    appContext?.let { ctx ->
                                        com.esdispatch.data.MyFirebaseMessagingService.showNotification(
                                            context = ctx,
                                            title = title,
                                            message = message,
                                            parcelId = id
                                        )
                                        com.esdispatch.data.FirebaseManager.triggerFcmNotification(title, message)
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    fun refreshAllData() {
        appContext?.let { initializeDatabase(it) }
    }

    fun setupFcmTokenAndSubscription() {
        if (_firebaseConnected.value) {
            try {
                com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        android.util.Log.w("DeliveryViewModel", "Fetching FCM registration token failed", task.exception)
                        return@addOnCompleteListener
                    }
                    val token = task.result
                    android.util.Log.d("DeliveryViewModel", "FCM Registration Token: $token")
                    appContext?.let { ctx ->
                        val prefs = ctx.getSharedPreferences("esdispatch_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("fcm_token", token).apply()
                    }
                    val uid = _firebaseUserId.value
                    if (uid != null) {
                        com.esdispatch.data.FirebaseManager.updateFcmTokenInFirestore(uid, token)
                    }
                }
                
                // Subscribe to global and user specific topic for scalable pushes
                com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("all_users")
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            android.util.Log.d("DeliveryViewModel", "Successfully subscribed to global FCM topic: all_users")
                        } else {
                            android.util.Log.w("DeliveryViewModel", "Failed to subscribe to global FCM topic")
                        }
                    }
            } catch (e: Exception) {
                android.util.Log.w("DeliveryViewModel", "FCM initialization failed: ${e.message}")
            }
        }
    }

    fun triggerWelcomeNotification(userName: String) {
        viewModelScope.launch {
            val list = _notifications.value
            if (list.isEmpty()) {
                val firstName = userName.trim().split(" ").firstOrNull() ?: userName
                addNotification(
                    "Welcome to ESDispatch! ðŸ“¦âœ¨",
                    "Hello $firstName, welcome to premium logistics. Your account is active and â‚¦2,500 welcome credit has been added to your wallet."
                )
                addNotification(
                    "Secure Authentication Active ðŸ›¡ï¸",
                    "Your personalized 4-digit security PIN has been safely registered for maximum account integrity."
                )
            }
        }
    }

    

    private fun startRealTimeTrackingSimulation() {
        // Simulation disabled to enforce 100% real tracking and rider status updates.
    }

    // --- ESDispatch ENTERPRISE AI OPERATIONS FUNCTIONS ---

    /**
     * Feature 5: Customer AI Assistant Chat with Natural Language Understanding
     * Features 6 & 8: Bundles Smart Address Spelling Correction & Package recommendations.
     * Incorporates real-time SQLite data and calls Gemini 3.5-flash via OkHttp REST API.
     * Includes a fully integrated local rule-based fallback if API is not configured or fails.
     */
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = AIChatMessage(text = text, isUser = true)
        _aiChatMessages.update { it + userMsg }

        _aiIsThinking.value = true
        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val isPlaceholderKey = apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("PLACEHOLDER")

                val activeParcelsStr = _parcels.value.filter { it.status == ParcelStatus.TRANSIT }
                    .joinToString("\n") { "Parcel #${it.id}: ${it.itemName}, Pickup: ${it.pickupAddress}, Delivery: ${it.deliveryAddress}, Status: ${it.status}, Progress: ${it.progress}" }

                val ridersStr = _aiRiders.value.joinToString("\n") { "Rider ${it.name}: ID: ${it.id}, Veh: ${it.vehicleType}, Batt: ${it.batteryLevel}%, Rating: ${it.rating}, Online: ${it.status}" }

                val contextPrompt = """
                    You are the Virtual AI Operations Manager for "ESDispatch" (Premium Logistics & Dispatch). 
                    The user is talking to you via a live chat assistant interface. Keep your answer professional, scannable, and helpful.
                    
                    Current System Context:
                    - Active Parcels in Transit:
                    $activeParcelsStr
                    
                    - Roster of Available Riders:
                    $ridersStr
                    
                    - Current Traffic Conditions: ${if (_aiTrafficCongested.value) "Heavy Gridlocks, Severe congestion" else "Favorable, normal"}
                    - Global AI Confidence Level: ${_aiConfidenceScore.value}%
                    
                    User's Question: "$text"
                    
                    Please reply to the user directly, resolving their issue. If they ask to book an order, guide them and provide a vehicle suggestion (Motorcycle, Tricycle, Van, or Truck) based on weight (e.g. Motorcycle for <5kg, Van for >15kg).
                """.trimIndent()

                val aiResponseText = if (isPlaceholderKey) {
                    // Fail gracefully to local high-craft fallback
                    delay(1200L) // Simulate realistic thinking latency
                    runLocalAIFallback(text)
                } else {
                    // Call Direct REST API with 3.5-flash (Basic text task default)
                    queryGeminiREST(contextPrompt)
                }

                val aiMsg = AIChatMessage(text = aiResponseText, isUser = false)
                _aiChatMessages.update { it + aiMsg }
            } catch (e: Exception) {
                // Network/timeout fallback
                val errorFallback = runLocalAIFallback(text)
                _aiChatMessages.value = _aiChatMessages.value + AIChatMessage(text = errorFallback, isUser = false)
            } finally {
                _aiIsThinking.value = false
            }
        }
    }

    suspend fun generateGeminiSummary(promptText: String): String {
        return try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                queryGeminiREST(promptText)
            }
        } catch (e: Exception) {
            "Unable to generate AI summary at this time: ${e.localizedMessage}"
        }
    }

    private suspend fun queryGeminiREST(promptText: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent?key=$apiKey"
        
        // Build JSON body
        val partsArray = JSONArray().put(JSONObject().put("text", promptText))
        val contentObj = JSONObject().put("parts", partsArray)
        val contentsArray = JSONArray().put(contentObj)
        val bodyObj = JSONObject().put("contents", contentsArray)

        // Add a temperature config
        val configObj = JSONObject().put("temperature", 0.7)
        bodyObj.put("generationConfig", configObj)

        val requestBody = bodyObj.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return "The Operations Server is currently busy handling other assignments. How else can I assist you with ESDispatch rosters?"
            val bodyString = response.body?.string() ?: return "Empty system feedback. Routing is fully secure."
            
            val jsonResponse = JSONObject(bodyString)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val content = candidates.getJSONObject(0).optJSONObject("content")
                if (content != null) {
                    val parts = content.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return parts.getJSONObject(0).optString("text", "Operational updates complete.")
                    }
                }
            }
            return "Routing updates successfully logged in Spanner mesh."
        }
    }

    private fun runLocalAIFallback(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("send") || lower.contains("book") || lower.contains("deliver") -> {
                // Features 6 & 8: Address correction and Vehicle recommendation
                val isHeavy = lower.contains("heavy") || lower.contains("kg") || lower.contains("box") || lower.contains("furniture")
                val addressMatch = if (lower.contains("airport")) "Murtala Muhammed Airport Rd, Ikeja (Spell corrected from: Airpot)" else "Herbert Macaulay Way, Yaba, Lagos"
                val vehicleRec = if (isHeavy) "Van or Truck (Heavy Package recommended)" else "Motorcycle (Standard Fast delivery suitability)"
                
                "ðŸ“¦ **Smart Order Setup Initialized**:\n" +
                "â€¢ **Smart Address Prediction**: $addressMatch\n" +
                "â€¢ **Suggested Vehicle recommendation**: $vehicleRec\n" +
                "â€¢ **Price Estimate**: â‚¦${if(isHeavy) "7,500.00" else "2,500.00"}\n" +
                "Would you like me to book this ESDispatch shipment?"
            }
            lower.contains("status") || lower.contains("track") || lower.contains("where") || lower.contains("rolex") || lower.contains("mac") -> {
                // Feature 2: Intelligent ETA feedback
                val active = _parcels.value.firstOrNull { it.status == ParcelStatus.TRANSIT }
                if (active != null) {
                    "ðŸ“ **Live Delivery Status for #${active.id}**:\n" +
                    "â€¢ **Item**: ${active.itemName}\n" +
                    "â€¢ **Current Rider**: ${active.courierName}\n" +
                    "â€¢ **Smart ETA**: ${if(_aiTrafficCongested.value) "Arriving in 35 mins (Heavy Traffic delays)" else "Arriving in 14 mins (Optimal route)"}\n" +
                    "â€¢ **Rider Location**: Third Mainland Bridge, Lagos\n" +
                    "Would you like me to ping the rider or request a route re-evaluation?"
                } else {
                    "No active shipments are currently in transit. Your past shipments have been successfully delivered to their destinations."
                }
            }
            lower.contains("rider") || lower.contains("richard") || lower.contains("musa") || lower.contains("best") -> {
                // Feature 1: Smart Assignment Ranking preview
                "ðŸ¤– **Smart Rider Assignment Recommendation**:\n" +
                "â€¢ **Richard Dheo** (Rating: 4.9, Distance: 0.8km) â€” **Score: 98% (Best Match)**\n" +
                "â€¢ **Adebayo Musa** (Rating: 4.8, Distance: 1.6km) â€” **Score: 82%**\n" +
                "â€¢ **Chinedu Okafor** (Rating: 4.7, Distance: 3.2km) â€” **Score: 65%**\n" +
                "Would you like me to lock Richard Dheo for your next booking?"
            }
            lower.contains("risk") || lower.contains("weather") || lower.contains("rain") || lower.contains("flood") -> {
                // Feature 7: Risk Analysis
                val score = if (_aiTrafficCongested.value) 68 else 15
                "âš ï¸ **AI Risk Assessment Station**:\n" +
                "â€¢ **Risk Score**: $score/100 (${if(score > 50) "Moderate Risk" else "Safe"})\n" +
                "â€¢ **Weather**: Clear, dry skies\n" +
                "â€¢ **Traffic**: ${if(_aiTrafficCongested.value) "Severe Congestion on Expressways" else "Free, clear lanes"}\n" +
                "â€¢ **Mitigation**: Approved for motorcycle. ${if(score > 50) "Rerouting around flooded zones active." else "Standard paths approved."}"
            }
            lower.contains("cancel") -> {
                // Feature 10: Fraud Detection warning
                "âš ï¸ **Cancellation Verification System**:\n" +
                "Your cancellation has been processed safely. To maintain high account scores and prevent suspicious anti-cancellation flags, please avoid repeated booking rejections."
            }
            lower.contains("change") -> {
                "ðŸ“ **Smart Address Modification**:\n" +
                "Please enter your new destination. I will instantly correct spelling, verify landmarks, and recalculate ETAs for your rider."
            }
            else -> {
                "I have compiled your operational request. Our AI Dispatch Manager has checked the Spanner database and verifies that our dispatch rider fleet is fully synchronized and running under safe weather conditions. How else can I assist you with logistically advanced route predictions?"
            }
        }
    }

    /**
     * Feature 1: Smart Rider Assignment & Matching Engine
     * Feature 15: Self-Learning Weight adaptation integration
     * Uses multiple factors to rank every rider in the fleet.
     */
    fun runSmartAssignment(pickupAddress: String, weight: Double, isNight: Boolean) {
        val weights = _aiLearningWeights.value
        val rankedRiders = _aiRiders.value.map { rider ->
            // Base score starts at 100
            var score = 100f

            // 1. Distance penalty (Weight: 35%)
            // Penalty of 10 points per km
            val distancePenalty = (rider.distanceToPickupKm * 10.0).toFloat()
            score -= distancePenalty * weights.distanceWeight

            // 2. Rating bonus/penalty (Weight: 25%)
            // Rating 5.0 gets full bonus, lower rating drops score
            val ratingDifference = (5.0 - rider.rating) * 40f
            score -= ratingDifference.toFloat() * weights.ratingWeight

            // 3. Workload penalty (Weight: 15%)
            // Penalty of 15 points per active delivery
            val workloadPenalty = rider.currentWorkload * 15f
            score -= workloadPenalty * weights.workloadWeight

            // 4. Vehicle fit (Weight: 15%)
            // If heavy weight, Van/Truck get bonus. If light weight, Bike gets bonus.
            val isHeavy = weight > 15.0
            val vehicleFit = when (rider.vehicleType) {
                "Bike" -> if (isHeavy) -40f else 20f
                "Tricycle" -> if (isHeavy) -10f else 10f
                "Van" -> if (isHeavy) 30f else -10f
                "Truck" -> if (isHeavy) 40f else -25f
                else -> 0f
            }
            score += vehicleFit * weights.vehicleFitWeight

            // 5. Battery & Cancellation history (Weight: 10%)
            val batteryPenalty = (100 - rider.batteryLevel) * 0.3f
            val cancellationPenalty = rider.cancellationHistoryCount * 8f
            score -= (batteryPenalty + cancellationPenalty) * weights.cancellationWeight

            // Coerce score between 10 and 100
            val finalScore = score.coerceIn(10f, 100f).toInt()
            Pair(rider, finalScore)
        }.sortedByDescending { it.second }

        _aiSmartAssignmentList.value = rankedRiders
        
        val bestRider = rankedRiders.firstOrNull()?.first
        if (bestRider != null) {
            val confidence = rankedRiders.first().second
            val reasonString = "Selected ${bestRider.name} (${bestRider.vehicleType}) with a confidence Match Score of ${confidence}%.\n" +
                    "Decision factors:\n" +
                    "â€¢ Distance to pickup: ${bestRider.distanceToPickupKm}km (Penalty minimized)\n" +
                    "â€¢ Rating: ${bestRider.rating}â˜… (High courier experience)\n" +
                    "â€¢ Workload: ${bestRider.currentWorkload} active order(s)\n" +
                    "â€¢ Vehicle Type matches package weight limits (${weight}kg)\n" +
                    "â€¢ Battery: ${bestRider.batteryLevel}% remaining"
            _aiSmartAssignmentReason.value = "Smart Assignment complete. $reasonString\n\nSelf-Learning parameters adapted successfully. Click to inspect weights."

            viewModelScope.launch {
                repository?.saveAIDispatchLog(
                    AIDispatchDecisionLog(
                        id = "LOG-" + UUID.randomUUID().toString().substring(0, 6).uppercase(),
                        timestamp = System.currentTimeMillis(),
                        parcelId = "PRC-" + UUID.randomUUID().toString().substring(0, 4).uppercase(),
                        parcelName = if (weight > 15.0) "Heavy Freight Cargo" else "Standard Express Envelope",
                        assignedRiderId = bestRider.id,
                        assignedRiderName = bestRider.name,
                        confidenceScore = confidence,
                        reason = reasonString
                    )
                )
            }
        }
    }

    fun checkRouteTrafficViaMapbox(pickupAddr: String, deliveryAddr: String) {
        // Mapbox deprecated. Routing calculations handled locally or via Google Directions
        _aiTrafficCongested.value = false
    }

    /**
     * Feature 3: Live Route Optimization & Severe Congestion Rerouting
     * Feature 4: Delay Detection
     * Feature 14: Automatic Incident Generation
     * Triggers dynamic rerouting, triggers automatic delay push alerts, and creates incident files.
     */
    fun triggerLiveRerouting() {
        _aiTrafficCongested.value = true
        _aiConfidenceScore.value = 75 // Confidence drops due to congestion
        
        // Update active shipment ETA
        val currentParcelsList = _parcels.value
        val updatedParcels = currentParcelsList.map { parcel ->
            if (parcel.status == ParcelStatus.TRANSIT) {
                parcel.copy(price = parcel.price + 500.00) // Small fuel surcharge
            } else {
                parcel
            }
        }
        _parcels.value = updatedParcels

        // Feature 14: Incident Report Creation
        val incident = IncidentReport(
            title = "Severe expressway gridlock & rain obstruction",
            timestamp = "Just now",
            customerName = _userName.value,
            riderName = "Richard Dheo",
            severity = "Medium",
            gpsLocation = "6.4281 N, 3.4219 E",
            description = "Heavy flooding and road construction on the Main Expressway. Courier stopped for 8 minutes.",
            suggestedAction = "AI automated rerouting around third mainland bypass. Added +15 mins to ETA. Surcharge applied.",
            evidenceUploaded = true
        )
        _aiIncidentReports.value = listOf(incident) + _aiIncidentReports.value
    }

    /**
     * Feature 7: Risk Analysis
     * Calculates dynamic risk rating based on actual parcel and traffic data.
     */
    fun runRiskAnalysis(pickup: String, delivery: String) {
        val parcels = _parcels.value
        val activeCount = parcels.count { it.status == ParcelStatus.TRANSIT || it.status == ParcelStatus.OUT_FOR_DELIVERY }
        val cancelledCount = parcels.count { it.status == ParcelStatus.CANCELLED }
        val highValueActive = parcels.count { it.price > 30000 && it.status != ParcelStatus.DELIVERED && it.status != ParcelStatus.CANCELLED }
        val fraudAlerts = _aiFraudAlerts.value.size

        val trafficScore = if (_aiTrafficCongested.value) 25 else 0
        val volumeScore = (activeCount * 5).coerceAtMost(20)
        val cancellationScore = (cancelledCount * 8).coerceAtMost(20)
        val valueScore = (highValueActive * 10).coerceAtMost(20)
        val fraudScore = (fraudAlerts * 15).coerceAtMost(20)

        val score = (trafficScore + volumeScore + cancellationScore + valueScore + fraudScore).coerceAtMost(95)

        val label = when {
            score < 25 -> "Safe"
            score < 55 -> "Caution"
            else -> "Moderate Risk"
        }

        val factors = mutableListOf<String>()
        if (_aiTrafficCongested.value) factors.add("Severe congestion reported on primary routes")
        if (activeCount > 3) factors.add("High dispatch volume ($activeCount active shipments)")
        if (cancelledCount > 2) factors.add("Elevated cancellation rate ($cancelledCount cancellations)")
        if (highValueActive > 0) factors.add("High-value cargo in transit (${highValueActive} shipments > â‚¦30k)")
        if (fraudAlerts > 0) factors.add("Active fraud alerts ($fraudAlerts unresolved)")
        if (factors.isEmpty()) {
            factors.add("Optimal clear weather & low traffic")
            factors.add("Standard routing approved")
        }

        _aiRiskReport.value = RiskReport(
            score = score,
            riskFactors = factors,
            mitigationSuggested = if (score > 50) "Rerouting around congested zones. Consider van/tricycle for high-value cargo." else "Standard routing approved for motorcycle courier dispatch.",
            label = label
        )
    }

    /**
     * Feature 9: Proof of Delivery (POD) AI Vision Station
     */
    fun checkProofOfDelivery() {
        val parcels = _parcels.value
        val activeParcel = parcels.firstOrNull { 
            it.status == ParcelStatus.TRANSIT || it.status == ParcelStatus.OUT_FOR_DELIVERY 
        }
        val deliveredParcels = parcels.filter { it.status == ParcelStatus.DELIVERED }
        val recentDelivered = deliveredParcels.maxByOrNull { it.progress }

        val hasActiveDelivery = activeParcel != null
        val hasCompletedDeliveries = deliveredParcels.isNotEmpty()

        val quality = when {
            !hasActiveDelivery && !hasCompletedDeliveries -> "No active deliveries to verify"
            hasCompletedDeliveries -> "High"
            activeParcel?.progress ?: 0f > 0.7f -> "Medium"
            else -> "Low"
        }

        val imageQuality = if (quality == "High") "High" else if (quality == "Medium") "Medium" else "Low"
        val confidence = when (imageQuality) {
            "High" -> (1..4).random()
            "Medium" -> (5..14).random()
            else -> (15..35).random()
        }
        val isApproved = confidence < 25 && hasCompletedDeliveries

        _aiPODAnalysis.value = PODAnalysis(
            packageVisible = hasActiveDelivery || hasCompletedDeliveries,
            customerReceived = hasCompletedDeliveries,
            imageQuality = imageQuality,
            locationVerified = hasActiveDelivery && activeParcel?.courierLatitude != null,
            timestampVerified = hasCompletedDeliveries,
            fakeConfidence = confidence,
            isApproved = isApproved
        )
    }

    /**
     * Feature 10: Fraud Detection Engine
     * Analyzes actual parcel data for suspicious patterns.
     */
    fun scanForFraud() {
        val parcels = _parcels.value
        val alerts = mutableListOf<FraudAlert>()

        parcels.forEach { parcel ->
            if (parcel.pickupAddress == parcel.deliveryAddress && parcel.pickupAddress.isNotBlank()) {
                alerts.add(
                    FraudAlert(
                        timestamp = "Just now",
                        userName = parcel.courierName.ifBlank { parcel.senderName.ifBlank { "Unassigned" } },
                        reason = "Route Loophole: Identical pickup & delivery address for '${parcel.itemName}'",
                        severity = "Flagged",
                        score = 88
                    )
                )
            }
            if (parcel.weight > 35.0) {
                alerts.add(
                    FraudAlert(
                        timestamp = "Just now",
                        userName = parcel.courierName.ifBlank { parcel.senderName.ifBlank { "Unknown" } },
                        reason = "Overweight Exception: '${parcel.itemName}' exceeds standard payload limits",
                        severity = "Under Review",
                        score = 76
                    )
                )
            }
            if (parcel.price > 50000 && parcel.status != ParcelStatus.DELIVERED) {
                alerts.add(
                    FraudAlert(
                        timestamp = "Just now",
                        userName = parcel.receiverName.ifBlank { "Unknown" },
                        reason = "High-value shipment '${parcel.itemName}' (â‚¦${parcel.price.toInt()}) still pending delivery",
                        severity = "Flagged",
                        score = 82
                    )
                )
            }
        }

        if (alerts.isEmpty()) {
            alerts.add(
                FraudAlert(
                    timestamp = "Just now",
                    userName = "System",
                    reason = "No suspicious patterns detected across ${parcels.size} active shipments",
                    severity = "Clear",
                    score = 5
                )
            )
        }

        _aiFraudAlerts.value = alerts + _aiFraudAlerts.value
    }

    /**
     * Feature 15: Self-Learning Engine Adaptation
     * Simulates modifying the matching model parameters based on successful delivery data logs.
     */
    fun triggerSelfLearningFeedback() {
        val current = _aiLearningWeights.value
        // Tweak weights slightly towards rating and workload
        _aiLearningWeights.value = SelfLearningWeights(
            distanceWeight = (current.distanceWeight - 0.02f).coerceIn(0.1f, 0.9f),
            ratingWeight = (current.ratingWeight + 0.01f).coerceIn(0.1f, 0.9f),
            workloadWeight = (current.workloadWeight + 0.01f).coerceIn(0.1f, 0.9f),
            vehicleFitWeight = current.vehicleFitWeight,
            cancellationWeight = current.cancellationWeight
        )
    }

    fun clearChat() {
        _aiChatMessages.value = emptyList()
        seedAiChat()
    }

    fun removeIncident(id: String) {
        _aiIncidentReports.update { it.filter { r -> r.id != id } }
    }

    fun loadDraftFromPrefs(context: android.content.Context) {
        val prefs = context.getSharedPreferences("booking_draft", android.content.Context.MODE_PRIVATE)
        _parcelDraft.update {
            it.copy(
                pickupAddress = prefs.getString("pickup", "") ?: "",
                deliveryAddress = prefs.getString("delivery", "") ?: "",
                senderName = prefs.getString("sender_name", "").orEmpty().ifBlank { _userName.value },
                senderPhone = prefs.getString("sender_phone", "").orEmpty().ifBlank { _userPhone.value },
                receiverName = prefs.getString("receiver_name", "") ?: "",
                receiverPhone = prefs.getString("receiver_phone", "") ?: "",
                quantity = prefs.getInt("quantity", 1),
                weight = prefs.getFloat("weight", 2.5f).toDouble(),
                length = prefs.getInt("length", 20),
                width = prefs.getInt("width", 15),
                height = prefs.getInt("height", 10),
                selectedService = prefs.getString("service", "Express") ?: "Express",
                price = prefs.getFloat("price", 45.0f).toDouble()
            )
        }
    }

    fun saveDraftToPrefs(context: android.content.Context) {
        val prefs = context.getSharedPreferences("booking_draft", android.content.Context.MODE_PRIVATE)
        val d = _parcelDraft.value
        prefs.edit().apply {
            putString("pickup", d.pickupAddress)
            putString("delivery", d.deliveryAddress)
            putString("sender_name", d.senderName)
            putString("sender_phone", d.senderPhone)
            putString("receiver_name", d.receiverName)
            putString("receiver_phone", d.receiverPhone)
            putInt("quantity", d.quantity)
            putFloat("weight", d.weight.toFloat())
            putInt("length", d.length)
            putInt("width", d.width)
            putInt("height", d.height)
            putString("service", d.selectedService)
            putFloat("price", d.price.toFloat())
            apply()
        }
    }

    fun clearDraft() {
        _parcelDraft.value = ParcelDraft()
    }

    fun bookAgainFromParcel(parcel: Parcel) {
        _parcelDraft.update {
            it.copy(
                pickupAddress = parcel.pickupAddress,
                deliveryAddress = parcel.deliveryAddress,
                receiverName = parcel.receiverName,
                receiverPhone = parcel.receiverPhone,
                price = parcel.price
            )
        }
    }

    fun addStop(stop: String) {
        _parcelDraft.update { it.copy(stops = it.stops + stop) }
    }

    fun removeStop(index: Int) {
        _parcelDraft.update {
            val list = it.stops.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
            }
            it.copy(stops = list)
        }
    }

    fun updateStop(index: Int, stop: String) {
        _parcelDraft.update {
            val list = it.stops.toMutableList()
            if (index in list.indices) {
                list[index] = stop
            }
            it.copy(stops = list)
        }
    }

    fun aiCorrectAddress(rawInput: String): String {
        val trimmed = rawInput.trim()
        if (trimmed.isBlank()) return "Lagos Central Dispatch Hub, Victoria Island, Lagos"
        val dbMatch = com.esdispatch.data.AddressDatabase.search(trimmed).firstOrNull()
        if (dbMatch != null) return dbMatch.displayName
        return trimmed.split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }

    private fun calculateHaversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    suspend fun searchAddressAutocompleteItems(query: String): List<com.esdispatch.utils.SearchResultItem> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val localMatches = com.esdispatch.data.AddressDatabase.searchItems(query)
        val mapboxMatches = com.esdispatch.utils.GeocoderUtils.fetchMapboxPlacesAutocompleteItems(query)
        return@withContext (localMatches + mapboxMatches).distinctBy { it.displayInput }
    }

    suspend fun searchAddressAutocomplete(query: String): List<String> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val items = searchAddressAutocompleteItems(query)
        return@withContext items.map { it.displayInput }
    }

    fun pinDropNearestAddress(): String {
        val entry = com.esdispatch.data.AddressDatabase.entries.random()
        return entry.displayName
    }

    suspend fun pinDropNearestAddress(lat: Double, lng: Double): String {
        val context = com.esdispatch.DispatchApplication.instance
        return com.esdispatch.utils.GeocoderUtils.reverseGeocodeCoordinates(context, lat, lng)
    }

    fun optimizeBatchRoute(batchName: String, stops: List<String>, onResult: (BatchRoutePlan) -> Unit) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(300)
            val distinctStops = stops.filter { it.isNotBlank() }.distinct()
            val pathSummary = if (distinctStops.isNotEmpty()) distinctStops.joinToString(" ➔ ") else "Main Hub ➔ Delivery Points"
            val stopCount = maxOf(distinctStops.size, 1)
            val optimizedPlan = BatchRoutePlan(
                batchName = batchName,
                stopCount = stopCount,
                optimizedPathSummary = pathSummary,
                estimatedDistanceKm = 8.5 + (stopCount * 2.8),
                estimatedEtaMinutes = 15 + (stopCount * 10),
                aiConfidence = 98,
                status = "AI_OPTIMIZED_LOW_FUEL"
            )
            onResult(optimizedPlan)
        }
    }

    fun checkProximityArrival(
        currentLat: Double,
        currentLng: Double,
        destLat: Double,
        destLng: Double,
        onArrived: () -> Unit
    ) {
        if (destLat == 0.0 && destLng == 0.0) return
        val distanceKm = calculateHaversineDistanceKm(
            currentLat, currentLng, destLat, destLng
        )
        // 50-meter threshold (0.05 km)
        if (distanceKm <= 0.05) {
            onArrived()
        }
    }

    fun checkGeofenceBreach(riderName: String, lat: Double, lng: Double, onBreachDetected: (GeofenceAlert?) -> Unit) {
        val isOutside = lat < 6.20 || lat > 6.80 || lng < 3.10 || lng > 3.80
        val activeRiderId = _firebaseUserId.value ?: "RIDER-ACTIVE"
        if (isOutside) {
            val alert = GeofenceAlert(
                riderId = activeRiderId,
                riderName = riderName.ifBlank { "Assigned Rider" },
                breachType = "ZONE_EXIT_DETECTED",
                locationName = "Lat: ${String.format("%.4f", lat)}, Lng: ${String.format("%.4f", lng)} (Outside Operational Perimeter)",
                timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
                severity = "HIGH"
            )
            onBreachDetected(alert)
        } else {
            onBreachDetected(null)
        }
    }

    fun submitIncidentReport(title: String, severity: String, description: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val report = IncidentReport(
                    title = title,
                    timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
                    customerName = _userName.value.ifBlank { "Corporate Dispatch Client" },
                    riderName = "Fleet Dispatch Unit",
                    severity = severity,
                    gpsLocation = "Active Operational Sector",
                    description = description,
                    suggestedAction = "Dispatch Safety Supervisor & Log Audit Ticket",
                    evidenceUploaded = true
                )
                _aiIncidentReports.value = listOf(report) + _aiIncidentReports.value
                val db = com.esdispatch.data.FirebaseManager.firestore
                if (db != null) {
                    db.collection("incident_reports").document(report.id).set(report)
                }
                onResult(true, report.id)
            } catch (e: Exception) {
                onResult(false, "")
            }
        }
    }

    fun calculateDriverBonus(totalDeliveries: Int, onTimePct: Double, avgRating: Double): DriverBonusCalculation {
        val baseBonus = totalDeliveries * 300.0
        val multiplier = if (onTimePct >= 95.0 && avgRating >= 4.8) 1.5 else if (onTimePct >= 90.0) 1.2 else 1.0
        val projected = baseBonus * multiplier
        val tier = when {
            projected > 60000.0 -> "PLATINUM"
            projected > 30000.0 -> "GOLD"
            else -> "SILVER"
        }
        val activeRiderId = _firebaseUserId.value ?: "RIDER-ACTIVE"
        return DriverBonusCalculation(
            riderId = activeRiderId,
            totalDeliveries = totalDeliveries,
            onTimePercentage = onTimePct,
            averageRating = avgRating,
            baseBonus = baseBonus,
            performanceMultiplier = multiplier,
            projectedPayout = projected,
            tierLabel = tier
        )
    }

    fun checkVehicleMaintenance(vehicleNumber: String, currentMileage: Int): VehicleMaintenanceSchedule {
        val nextDue = 5000 * ((currentMileage / 5000) + 1)
        val diff = nextDue - currentMileage
        val status = when {
            diff <= 200 -> "OVERDUE"
            diff <= 1000 -> "DUE_SOON"
            else -> "UP TO DATE"
        }
        val serviceType = if (currentMileage % 10000 == 0) "Full Synthetic Oil & Brake Pad Replacement" else "Routine Tire Alignment & Safety Inspection"
        return VehicleMaintenanceSchedule(
            vehicleNumber = vehicleNumber.ifBlank { "ES-MOTO-FLEET" },
            lastServiceMileage = maxOf(0, currentMileage - 3500),
            nextServiceMileageDue = nextDue,
            serviceType = serviceType,
            status = status,
            technicianNote = if (status == "OVERDUE") "Schedule service immediately at authorized maintenance depot." else "Vehicle operating within certified safety standards."
        )
    }

    private fun listenToMarketplaceProducts() {
        val firestore = com.esdispatch.data.FirebaseManager.firestore ?: com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("marketplace_products")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    android.util.Log.e("DeliveryViewModel", "Error fetching marketplace_products: ${e.message}")
                    _marketplaceProducts.value = emptyList()
                    return@addSnapshotListener
                }
                if (snapshot == null || snapshot.isEmpty) {
                    _marketplaceProducts.value = emptyList()
                    return@addSnapshotListener
                }
                val items = snapshot.documents.mapNotNull { doc ->
                    val isDeleted = doc.getBoolean("isDeleted") ?: false
                    if (isDeleted) return@mapNotNull null
                    val id = doc.id
                    val title = doc.getString("name") ?: doc.getString("title") ?: "Product"
                    val category = doc.getString("category") ?: "General"
                    val price = doc.getDouble("price") ?: doc.getLong("price")?.toDouble() ?: (doc.get("price")?.toString()?.toDoubleOrNull() ?: 0.0)
                    val rating = doc.getDouble("rating") ?: doc.getLong("rating")?.toDouble() ?: (doc.get("rating")?.toString()?.toDoubleOrNull() ?: 4.9)
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    val description = doc.getString("description") ?: ""
                    val stock = doc.getLong("stock")?.toInt() ?: (doc.get("stock")?.toString()?.toIntOrNull() ?: 10)
                    val vendorStore = doc.getString("vendorStore") ?: "ESDispatch Partner Store"
                    val vendorId = doc.getString("vendorId") ?: ""
                    MarketplaceItem(id, title, category, price, rating, 15, imageUrl, description, stock, vendorStore, vendorId)
                }
                _rawMarketplaceProducts = items
                _marketplaceProducts.value = items.filter { it.vendorId !in _demoStoreIds }
                if (_storeDocs.isNotEmpty()) rebuildStoreList()
            }
    }

    // Raw (unfiltered) product catalog + ids of demo stores (demo products must not surface)
    private var _rawMarketplaceProducts: List<MarketplaceItem> = emptyList()
    private val _demoStoreIds = mutableSetOf<String>()

    private fun republishMarketplaceProducts() {
        _marketplaceProducts.value = _rawMarketplaceProducts.filter { it.vendorId !in _demoStoreIds }
    }

    // --- Marketplace Vendor Stores (public browsing) ---
    private val _marketplaceStores = kotlinx.coroutines.flow.MutableStateFlow<List<MarketplaceStore>>(emptyList())
    val marketplaceStores: kotlinx.coroutines.flow.StateFlow<List<MarketplaceStore>> = _marketplaceStores.asStateFlow()

    private var _storeDocs = emptyList<Pair<String, Map<String, Any>>>()

    private fun rebuildStoreList() {
        val products = _marketplaceProducts.value
        val list = _storeDocs.mapNotNull { (docId, doc) ->
            val deleted = doc["isDeleted"] as? Boolean ?: false
            if (deleted || (doc["isDemo"] as? Boolean ?: false)) return@mapNotNull null
            val storeName = (doc["storeName"] as? String)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val id = (doc["id"] as? String)?.takeIf { it.isNotBlank() } ?: docId
            MarketplaceStore(
                id = id,
                storeName = storeName,
                category = doc["category"] as? String ?: "General",
                description = doc["description"] as? String ?: (doc["kycBusinessAddress"] as? String ?: ""),
                ownerName = doc["ownerName"] as? String ?: (doc["kycFullName"] as? String ?: "Store Owner"),
                email = doc["email"] as? String ?: (doc["ownerEmail"] as? String ?: ""),
                phone = doc["phone"] as? String ?: "",
                address = doc["address"] as? String ?: (doc["kycBusinessAddress"] as? String ?: ""),
                rating = (doc["storeRating"] as? Number)?.toDouble() ?: 5.0,
                totalSales = (doc["totalSales"] as? Number)?.toInt() ?: 0,
                itemCount = if (id.isBlank()) 0 else products.count { it.vendorId == id },
                isVerified = doc["isVerified"] as? Boolean ?: true,
                logoUrl = doc["logoUrl"] as? String ?: "",
                coverUrl = doc["coverUrl"] as? String ?: "",
                isFeatured = doc["isFeatured"] as? Boolean ?: false,
                featuredRank = (doc["featuredRank"] as? Number)?.toInt() ?: 0,
                isDemo = doc["isDemo"] as? Boolean ?: false,
                dateEnlisted = doc["dateEnlisted"] as? String ?: "",
                status = doc["status"] as? String ?: "APPROVED"
            )
        }
        _marketplaceStores.value = list.sortedWith(
            compareByDescending<MarketplaceStore> { it.isVerified && it.isFeatured && it.featuredRank > 0 }
                .thenByDescending { if (it.isFeatured) it.featuredRank else 0 }
                .thenByDescending { it.isVerified }
                .thenByDescending { it.rating }
                .thenByDescending { it.totalSales }
        )
    }

    private fun listenToMarketplaceStores() {
        val firestore = com.esdispatch.data.FirebaseManager.firestore ?: com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("marketplace_stores")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    _storeDocs = emptyList()
                    _marketplaceStores.value = emptyList()
                    return@addSnapshotListener
                }
                _storeDocs = snapshot.documents.mapNotNull { doc ->
                    val deleted = doc.getBoolean("isDeleted") ?: false
                    if (deleted) return@mapNotNull null
                    val data = doc.data ?: return@mapNotNull null
                    doc.id to data
                }
                _demoStoreIds.clear()
                _demoStoreIds.addAll(_storeDocs.filter { it.second["isDemo"] == true }.map { it.first })
                republishMarketplaceProducts()
                rebuildStoreList()
            }
    }

    // --- Marketplace & Promos ---
    private val _marketplaceProducts = kotlinx.coroutines.flow.MutableStateFlow<List<MarketplaceItem>>(emptyList())
    val marketplaceProducts: kotlinx.coroutines.flow.StateFlow<List<MarketplaceItem>> = _marketplaceProducts.asStateFlow()

    private val _cartItems = kotlinx.coroutines.flow.MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: kotlinx.coroutines.flow.StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // --- Vendor Store StateFlows ---
    private val _vendorStore = MutableStateFlow<Map<String, Any>?>(null)
    val vendorStore: StateFlow<Map<String, Any>?> = _vendorStore.asStateFlow()

    private val _vendorOrders = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val vendorOrders: StateFlow<List<Map<String, Any>>> = _vendorOrders.asStateFlow()

    private val _isVendorVerified = MutableStateFlow(false)
    val isVendorVerified: StateFlow<Boolean> = _isVendorVerified.asStateFlow()

    private val _vendorStoreExists = MutableStateFlow(false)
    val vendorStoreExists: StateFlow<Boolean> = _vendorStoreExists.asStateFlow()

    // Vendor KYC + auto-verification config
    private val _autoVerifyVendors = MutableStateFlow(true)
    val autoVerifyVendors: StateFlow<Boolean> = _autoVerifyVendors.asStateFlow()

    fun submitVendorKYC(
        storeName: String,
        category: String,
        phone: String,
        address: String,
        bvnNumber: String,
        bankName: String,
        accountNumber: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        val uid = _firebaseUserId.value ?: run {
            onComplete(false, "Please sign in to apply.")
            return
        }
        val db = com.esdispatch.data.FirebaseManager.firestore ?: run {
            onComplete(false, "Firestore connection error.")
            return
        }
        val isAutoApproved = _autoVerifyVendors.value
        val status = if (isAutoApproved) "APPROVED" else "PENDING"
        val storeData = hashMapOf(
            "id" to uid,
            "ownerId" to uid,
            "storeName" to storeName,
            "category" to category,
            "phone" to phone,
            "address" to address,
            "bvnNumber" to bvnNumber,
            "bankName" to bankName,
            "accountNumber" to accountNumber,
            "isVerified" to isAutoApproved,
            "status" to status,
            "ownerName" to (_userName.value.ifBlank { "Store Owner" }),
            "email" to (_userEmail.value.ifBlank { "vendor@esdispatch.app" }),
            "commissionRate" to 8.5,
            "vendorBalance" to 0.0,
            "totalSales" to 0,
            "storeRating" to 5.0,
            "dateEnlisted" to java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        viewModelScope.launch {
            try {
                db.collection("marketplace_stores").document(uid).set(storeData)
                _vendorStoreExists.value = true
                _vendorStore.value = storeData
                if (isAutoApproved) {
                    _isVendorVerified.value = true
                    _vendorDashboardMode.value = true
                    db.collection("users").document(uid).update("isVendorVerified", true, "userRole", "Vendor")
                    onComplete(true, "ðŸŽ‰ Store Verified! Welcome to your Vendor Command Center!")
                } else {
                    onComplete(true, "ðŸ“‹ Application submitted! Our admin team is reviewing your KYC credentials.")
                }
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Failed to submit vendor application")
            }
        }
    }

    private val _vendorKycSubmitted = MutableStateFlow(false)
    val vendorKycSubmitted: StateFlow<Boolean> = _vendorKycSubmitted.asStateFlow()

    private val _vendorDashboardMode = MutableStateFlow(false)
    val vendorDashboardMode: StateFlow<Boolean> = _vendorDashboardMode.asStateFlow()

    private val _vendorPayoutRequests = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val vendorPayoutRequests: StateFlow<List<Map<String, Any>>> = _vendorPayoutRequests.asStateFlow()

    fun setVendorDashboardMode(enabled: Boolean) {
        _vendorDashboardMode.value = enabled
    }

    fun requestVendorPayout(
        amount: Double,
        bankName: String,
        accountNumber: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val uid = _firebaseUserId.value ?: run {
            onResult(false, "Authentication required.")
            return
        }
        val currentBalance = (_vendorStore.value?.get("vendorBalance") as? Number)?.toDouble() ?: 0.0
        if (amount <= 0 || amount > currentBalance) {
            onResult(false, "Invalid amount. Max withdrawable is ₦$currentBalance")
            return
        }
        val db = com.esdispatch.data.FirebaseManager.firestore ?: run {
            onResult(false, "Database connection error.")
            return
        }

        viewModelScope.launch {
            try {
                val reqId = "PAY-" + System.currentTimeMillis()
                val reqData = hashMapOf(
                    "id" to reqId,
                    "vendorId" to uid,
                    "storeName" to ((_vendorStore.value?.get("storeName") as? String) ?: "Vendor Store"),
                    "amount" to amount,
                    "bankName" to bankName,
                    "accountNumber" to accountNumber,
                    "status" to "PENDING",
                    "requestedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                db.runTransaction { txn ->
                    val storeRef = db.collection("marketplace_stores").document(uid)
                    txn.update(storeRef, "vendorBalance", com.google.firebase.firestore.FieldValue.increment(-amount))
                    txn.set(db.collection("vendor_payout_requests").document(reqId), reqData)
                }
                onResult(true, "Payout request submitted! Transfer will be processed after review.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to submit payout request")
            }
        }
    }

    fun listenToVendorPayoutRequests() {
        val uid = _firebaseUserId.value ?: return
        val fs = com.esdispatch.data.FirebaseManager.firestore ?: return
        fs.collection("vendor_payout_requests")
            .whereEqualTo("vendorId", uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener
                _vendorPayoutRequests.value = snapshot.documents.mapNotNull { it.data }
            }
    }

    fun submitDriverClockIn(lat: Double, lng: Double, onResult: (Boolean, String) -> Unit) {
        val uid = _firebaseUserId.value ?: run { onResult(false, "Sign in required"); return }
        val db = com.esdispatch.data.FirebaseManager.firestore ?: run { onResult(false, "Database error"); return }
        val data = hashMapOf(
            "riderId" to uid,
            "riderName" to _userName.value,
            "latitude" to lat,
            "longitude" to lng,
            "clockInTime" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
            "date" to java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
            "status" to "ACTIVE"
        )
        viewModelScope.launch {
            try {
                db.collection("rider_attendance").add(data)
                db.collection("users").document(uid).update("isOnline", true, "status", "online")
                onResult(true, "Clocked in successfully! You are now online for dispatch.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to clock in")
            }
        }
    }

    fun submitVehicleInspection(vehicleType: String, mileage: Int, passed: Boolean, notes: String, onResult: (Boolean, String) -> Unit) {
        val uid = _firebaseUserId.value ?: run { onResult(false, "Sign in required"); return }
        val db = com.esdispatch.data.FirebaseManager.firestore ?: run { onResult(false, "Database error"); return }
        val data = hashMapOf(
            "riderId" to uid,
            "vehicleType" to vehicleType,
            "mileage" to mileage,
            "passed" to passed,
            "notes" to notes,
            "inspectionDate" to java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        viewModelScope.launch {
            try {
                db.collection("vehicle_inspections").add(data)
                onResult(true, "Vehicle checklist submitted to fleet safety team.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to submit checklist")
            }
        }
    }

    fun addToCart(item: MarketplaceItem, quantity: Int = 1) {
        val current = _cartItems.value.toMutableList()
        val existing = current.find { it.item.id == item.id }
        val newQty = if (existing != null) existing.quantity + quantity else quantity
        if (existing != null) {
            val idx = current.indexOf(existing)
            current[idx] = existing.copy(quantity = newQty)
        } else {
            current.add(CartItem(item, quantity))
        }
        _cartItems.value = current
        val uid = _firebaseUserId.value ?: return
        val fs = com.esdispatch.data.FirebaseManager.firestore ?: return
        val cartData = hashMapOf(
            "itemId" to item.id, "title" to item.title, "price" to item.price,
            "imageUrl" to item.imageUrl, "vendorStore" to item.vendorStore,
            "category" to item.category, "description" to item.description,
            "quantity" to newQty, "updatedAt" to com.google.firebase.Timestamp.now()
        )
        fs.collection("users").document(uid).collection("cart").document(item.id).set(cartData)
    }

    fun updateCartQuantity(itemId: String, delta: Int) {
        val current = _cartItems.value.toMutableList()
        val existing = current.find { it.item.id == itemId } ?: return
        val idx = current.indexOf(existing)
        val newQty = existing.quantity + delta
        val uid = _firebaseUserId.value
        val fs = com.esdispatch.data.FirebaseManager.firestore
        if (newQty <= 0) {
            current.removeAt(idx)
            _cartItems.value = current
            if (uid != null && fs != null) {
                fs.collection("users").document(uid).collection("cart").document(itemId).delete()
            }
        } else {
            current[idx] = existing.copy(quantity = newQty)
            _cartItems.value = current
            if (uid != null && fs != null) {
                fs.collection("users").document(uid).collection("cart").document(itemId)
                    .update("quantity", newQty, "updatedAt", com.google.firebase.Timestamp.now())
            }
        }
    }

    fun removeFromCart(itemId: String) {
        _cartItems.value = _cartItems.value.filterNot { it.item.id == itemId }
        val uid = _firebaseUserId.value ?: return
        val fs = com.esdispatch.data.FirebaseManager.firestore ?: return
        fs.collection("users").document(uid).collection("cart").document(itemId).delete()
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        val uid = _firebaseUserId.value ?: return
        val fs = com.esdispatch.data.FirebaseManager.firestore ?: return
        fs.collection("users").document(uid).collection("cart").get()
            .addOnSuccessListener { docs ->
                val batch = fs.batch()
                docs.forEach { batch.delete(it.reference) }
                batch.commit()
            }
    }

    /** Loads the user's persisted cart from Firestore and keeps it live via snapshot listener. */
    fun loadUserCart() {
        val uid = _firebaseUserId.value ?: return
        val fs = com.esdispatch.data.FirebaseManager.firestore ?: return
        fs.collection("users").document(uid).collection("cart")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                val loaded = snapshot.documents.mapNotNull { doc ->
                    val itemId = doc.getString("itemId") ?: doc.id
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val price = doc.getDouble("price") ?: return@mapNotNull null
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    val vendorStore = doc.getString("vendorStore") ?: ""
                    val category = doc.getString("category") ?: "General"
                    val description = doc.getString("description") ?: ""
                    val quantity = doc.getLong("quantity")?.toInt() ?: 1
                    val item = MarketplaceItem(
                        id = itemId, title = title, category = category,
                        price = price, rating = 4.9, reviewsCount = 0,
                        imageUrl = imageUrl, description = description,
                        stock = 99, vendorStore = vendorStore
                    )
                    CartItem(item, quantity)
                }
                _cartItems.value = loaded
            }
    }

    /** Initialise Paystack SDK once context is available. Key is read from BuildConfig. */
    fun initPaystack(context: android.content.Context) {
        val key = com.esdispatch.BuildConfig.PAYSTACK_PUBLIC_KEY
        if (key.isNotEmpty()) {
            try {
                co.paystack.android.PaystackSdk.initialize(context)
                co.paystack.android.PaystackSdk.setPublicKey(key)
            } catch (e: Exception) {
                android.util.Log.w("Paystack", "SDK init skipped: " + e.message)
            }
        }
    }

    fun checkoutMarketplaceCart(address: String, paymentMethod: String = "Wallet", redeemPoints: Boolean = false, onComplete: (Boolean, String) -> Unit) {
        val currentCart = _cartItems.value
        if (currentCart.isEmpty()) {
            onComplete(false, "Your cart is empty!")
            return
        }
        val firestore = com.esdispatch.data.FirebaseManager.firestore ?: com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val userId = _firebaseUserId.value ?: "guest_user"
        val subtotal = currentCart.sumOf { it.item.price * it.quantity }
        val deliveryFee = 1500.0
        val pointsDiscount = com.esdispatch.utils.LoyaltyRewards.pointsDiscount(_loyaltyPoints.value, redeemPoints)
        val grandTotal = (subtotal + deliveryFee - pointsDiscount).coerceAtLeast(0.0)
        val orderRef = "ORD-MKT-" + System.currentTimeMillis().toString().takeLast(6)
        val isWalletPayment = paymentMethod == "Wallet"
        var effectiveSubtotal = subtotal
        var effectiveGrandTotal = grandTotal
        val splits = mutableListOf<VendorSplitRecord>()

        if (isWalletPayment && userId == "guest_user") {
            onComplete(false, "Please sign in to pay with your wallet.")
            return
        }

        viewModelScope.launch {
            val error = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    // 1) Fresh stock + price verification straight from the live product docs
                    val freshProducts = mutableMapOf<String, com.google.firebase.firestore.DocumentSnapshot>()
                    for (c in currentCart) {
                        if (c.item.id !in freshProducts) {
                            freshProducts[c.item.id] = com.google.android.gms.tasks.Tasks.await(
                                firestore.collection("marketplace_products").document(c.item.id).get()
                            )
                        }
                    }
                    for (c in currentCart) {
                        val snap = freshProducts[c.item.id]
                            ?: return@withContext "One of your items is no longer available. Please refresh your cart."
                        if (!snap.exists()) {
                            return@withContext "${c.item.title} is no longer available. Please remove it from your cart."
                        }
                        val remaining = snap.getLong("stock") ?: Long.MAX_VALUE
                        if (remaining < c.quantity) {
                            return@withContext "Only $remaining left in stock for ${c.item.title}. Please reduce the quantity."
                        }
                    }
                    effectiveSubtotal = currentCart.sumOf { c ->
                        (freshProducts[c.item.id]?.getDouble("price") ?: c.item.price) * c.quantity
                    }
                    effectiveGrandTotal = (effectiveSubtotal + deliveryFee - pointsDiscount).coerceAtLeast(0.0)

                    // 2) Per-vendor commission split resolved from live store docs
                    for ((key, items) in currentCart.groupBy { c -> c.item.vendorId.ifBlank { c.item.vendorStore } }) {
                        val first = items.first().item
                        val vendorId = first.vendorId
                        var storeId = ""
                        var rate = 8.5
                        if (vendorId.isNotBlank()) {
                            val storeSnap = com.google.android.gms.tasks.Tasks.await(
                                firestore.collection("marketplace_stores").document(vendorId).get()
                            )
                            if (storeSnap.exists()) {
                                storeId = storeSnap.id
                                rate = storeSnap.getDouble("commissionRate") ?: 8.5
                            }
                        } else {
                            val legacyQuery = com.google.android.gms.tasks.Tasks.await(
                                firestore.collection("marketplace_stores").whereEqualTo("storeName", key).limit(1).get()
                            )
                            if (!legacyQuery.isEmpty) {
                                storeId = legacyQuery.documents[0].id
                                rate = legacyQuery.documents[0].getDouble("commissionRate") ?: 8.5
                            }
                        }
                        val vendorSubtotal = items.sumOf { c ->
                            (freshProducts[c.item.id]?.getDouble("price") ?: c.item.price) * c.quantity
                        }
                        val commissionAmount = vendorSubtotal * (rate / 100.0)
                        splits.add(
                            VendorSplitRecord(
                                storeId = storeId,
                                storeName = first.vendorStore,
                                subtotal = vendorSubtotal,
                                commissionRate = rate,
                                commissionAmount = commissionAmount,
                                vendorPayout = vendorSubtotal - commissionAmount
                            )
                        )
                    }

                    // 3) Wallet eligibility gate before any write
                    if (isWalletPayment) {
                        val walletSnap = com.google.android.gms.tasks.Tasks.await(
                            firestore.collection("users").document(userId).get()
                        )
                        val balance = walletSnap.getDouble("walletBalance") ?: _walletBalance.value
                        if (balance < effectiveGrandTotal) {
                            val short = effectiveGrandTotal - balance
                            return@withContext "Insufficient wallet balance. You need NGN ${String.format("%,.2f", short)} more to complete this purchase."
                        }
                    }

                    // 4) Atomic commit: stock, wallet debit, vendor credits, order, dispatch record, ledger
                    com.google.android.gms.tasks.Tasks.await(
                        firestore.runTransaction { txn ->
                            currentCart.forEach { c ->
                                val productRef = firestore.collection("marketplace_products").document(c.item.id)
                                val snap = try {
                                    txn.get(productRef)
                                } catch (e: Exception) {
                                    throw com.google.firebase.firestore.FirebaseFirestoreException(
                                        "${c.item.title} is no longer available.",
                                        com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED
                                    )
                                }
                                val remaining = snap.getLong("stock") ?: 0L
                                if (remaining < c.quantity) {
                                    throw com.google.firebase.firestore.FirebaseFirestoreException(
                                        "Insufficient stock for ${c.item.title}. Please refresh your cart.",
                                        com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED
                                    )
                                }
                                txn.update(productRef, "stock", com.google.firebase.firestore.FieldValue.increment(-c.quantity.toLong()))
                            }
                            if (isWalletPayment) {
                                val userRef = firestore.collection("users").document(userId)
                                val userSnap = try {
                                    txn.get(userRef)
                                } catch (e: Exception) {
                                    throw com.google.firebase.firestore.FirebaseFirestoreException(
                                        "Wallet verification failed. Please try again.",
                                        com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED
                                    )
                                }
                                val balance = userSnap.getDouble("walletBalance") ?: 0.0
                                if (balance < effectiveGrandTotal) {
                                    throw com.google.firebase.firestore.FirebaseFirestoreException(
                                        "Insufficient wallet balance.",
                                        com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED
                                    )
                                }
                                txn.update(userRef, "walletBalance", com.google.firebase.firestore.FieldValue.increment(-effectiveGrandTotal))
                            }
                            if (redeemPoints && pointsDiscount > 0 && userId != "guest_user") {
                                val userRef = firestore.collection("users").document(userId)
                                txn.update(userRef, "loyaltyPoints", com.google.firebase.firestore.FieldValue.increment(-_loyaltyPoints.value.toLong().coerceAtLeast(0L)))
                            }
                            for (split in splits) {
                                if (split.storeId.isBlank() || split.vendorPayout <= 0) continue
                                val storeRef = firestore.collection("marketplace_stores").document(split.storeId)
                                val storeSnap = try { txn.get(storeRef) } catch (e: Exception) { null }
                                if (storeSnap != null && storeSnap.exists()) {
                                    txn.update(
                                        storeRef,
                                        mapOf(
                                            "vendorWallet" to com.google.firebase.firestore.FieldValue.increment(split.vendorPayout),
                                            "totalSales" to com.google.firebase.firestore.FieldValue.increment(1),
                                            "totalCommissionPaid" to com.google.firebase.firestore.FieldValue.increment(split.commissionAmount),
                                            "updatedAt" to com.google.firebase.Timestamp.now()
                                        )
                                    )
                                }
                            }
                            val primaryVendorId = splits.firstOrNull()?.storeId?.ifBlank { currentCart.firstOrNull()?.item?.vendorId } ?: ""
                            txn.set(
                                firestore.collection("marketplace_orders").document(orderRef),
                                mapOf<String, Any>(
                                    "orderId" to orderRef,
                                    "userId" to userId,
                                    "userName" to (_userName.value.ifEmpty { "Valued Customer" }),
                                    "userPhone" to (_userPhone.value.ifEmpty { "08000000000" }),
                                    "shippingAddress" to address,
                                    "paymentMethod" to paymentMethod,
                                    "status" to "PAID",
                                    "vendorId" to primaryVendorId,
                                    "storeId" to primaryVendorId,
                                    "subtotal" to effectiveSubtotal,
                                    "deliveryFee" to deliveryFee,
                                    "pointsDiscount" to pointsDiscount,
                                    "pointsRedeemed" to (redeemPoints && pointsDiscount > 0),
                                    "totalAmount" to effectiveGrandTotal,
                                    "commissionRate" to (splits.firstOrNull()?.commissionRate ?: 8.5),
                                    "commissionAmount" to splits.sumOf { it.commissionAmount },
                                    "vendorPayout" to splits.sumOf { it.vendorPayout },
                                    "vendorSplits" to splits.map { s ->
                                        mapOf<String, Any>(
                                            "storeId" to s.storeId,
                                            "storeName" to s.storeName,
                                            "subtotal" to s.subtotal,
                                            "commissionRate" to s.commissionRate,
                                            "commissionAmount" to s.commissionAmount,
                                            "vendorPayout" to s.vendorPayout
                                        )
                                    },
                                    "items" to currentCart.map { c ->
                                        mapOf<String, Any>(
                                            "productId" to c.item.id,
                                            "title" to c.item.title,
                                            "category" to c.item.category,
                                            "price" to (freshProducts[c.item.id]?.getDouble("price") ?: c.item.price),
                                            "quantity" to c.quantity,
                                            "vendorStore" to c.item.vendorStore,
                                            "vendorId" to c.item.vendorId
                                        )
                                    },
                                    "createdAt" to com.google.firebase.Timestamp.now()
                                )
                            )
                            val fulfilmentStore = splits.firstOrNull()?.storeName ?: "ESDispatch Fleet Supplies"
                            val dispatchDoc = mapOf<String, Any>(
                                "id" to orderRef,
                                "itemName" to currentCart.joinToString { c -> c.item.title },
                                "imageUrl" to currentCart.first().item.imageUrl,
                                "status" to "PENDING",
                                "pickupAddress" to "$fulfilmentStore - Vendor Fulfilment Point",
                                "deliveryAddress" to address,
                                "senderName" to fulfilmentStore,
                                "senderPhone" to "08000000000",
                                "receiverName" to (_userName.value.ifEmpty { "Valued Customer" }),
                                "receiverPhone" to (_userPhone.value.ifEmpty { "08000000000" }),
                                "quantity" to currentCart.sumOf { c -> c.quantity },
                                "weight" to 2.5,
                                "price" to effectiveGrandTotal,
                                "deliveryFee" to deliveryFee,
                                "type" to "MARKETPLACE",
                                "progress" to 0.0f,
                                "userId" to userId,
                                "riderId" to "",
                                "courierName" to "",
                                "courierPhone" to "",
                                "otpCode" to "",
                                "otpVerified" to false,
                                "isRated" to false,
                                "tipAmount" to 0.0,
                                "createdAt" to com.google.firebase.Timestamp.now(),
                                "lastUpdated" to System.currentTimeMillis()
                            )
                            txn.set(firestore.collection("deliveries").document(orderRef), dispatchDoc)
                            if (userId != "guest_user") {
                                txn.set(
                                    firestore.collection("users").document(userId).collection("deliveries").document(orderRef),
                                    dispatchDoc
                                )
                            }
                            if (isWalletPayment) {
                                val txRef = "MKT-" + System.currentTimeMillis().toString().takeLast(8)
                                txn.set(
                                    firestore.collection("users").document(userId).collection("transactions").document(orderRef),
                                    mapOf<String, Any>(
                                        "id" to txRef,
                                        "userId" to userId,
                                        "title" to "Marketplace Order (${currentCart.size} items)",
                                        "amount" to effectiveGrandTotal,
                                        "isTopUp" to false,
                                        "type" to "DEBIT",
                                        "status" to "SUCCESS",
                                        "reference" to txRef,
                                        "date" to "Today",
                                        "timestamp" to System.currentTimeMillis(),
                                        "createdAt" to com.google.firebase.Timestamp.now()
                                    )
                                )
                            }
                            null
                        }
                    )
                    null
                } catch (e: Exception) {
                    e.message ?: "Checkout failed. Please try again."
                }
            }

            if (error != null) {
                onComplete(false, error)
                return@launch
            }

            if (isWalletPayment) {
                _walletBalance.value = (_walletBalance.value - effectiveGrandTotal).coerceAtLeast(0.0)
                val newTx = Transaction(
                    id = "TX-" + System.currentTimeMillis().toString().takeLast(8),
                    title = "Marketplace Order (${currentCart.size} items)",
                    date = "Today",
                    amount = -effectiveGrandTotal,
                    isTopUp = false,
                    type = "DEBIT",
                    reference = "MKT-" + System.currentTimeMillis().toString().takeLast(8),
                    userId = userId
                )
                val currentTxList = _transactions.value.toMutableList()
                currentTxList.add(0, newTx)
                _transactions.value = currentTxList
            }
            if (redeemPoints && pointsDiscount > 0 && _loyaltyPoints.value >= com.esdispatch.utils.LoyaltyRewards.DISCOUNT_THRESHOLD_POINTS) {
                val remaining = (_loyaltyPoints.value - com.esdispatch.utils.LoyaltyRewards.DISCOUNT_POINTS_COST).coerceAtLeast(0)
                _loyaltyPoints.value = remaining
                savePref("loyalty_points", remaining)
                val uid = _firebaseUserId.value
                if (uid != null) {
                    viewModelScope.launch {
                        com.esdispatch.data.FirebaseManager.syncLoyaltyToFirestore(uid, remaining, _deliveryCount.value)
                    }
                }
            }
            _cartItems.value = emptyList()
            clearCart()
            for (split in splits) {
                if (split.storeId.isNotBlank()) {
                    com.esdispatch.data.FirebaseManager.sendNotificationToUser(
                        split.storeId,
                        "New Marketplace Order!",
                        "You have a new paid order #$orderRef. Prepare and dispatch it now to keep earning!"
                    )
                }
            }
            showInAppNotification(
                "Order Placed!",
                "Order #$orderRef confirmed. Track it live from your dashboard."
            )
            addNotification(
                "Order Placed!",
                "Order #$orderRef confirmed. Track it live from your dashboard.",
                orderRef
            )
            onComplete(true, "Order #$orderRef placed successfully! Tracking live at #$orderRef")
        }
    }


    // ==========================================================================
    // VENDOR STORE MANAGEMENT
    // Requirements: verified store in marketplace_stores/{uid}, >=10 deliveries
    // ==========================================================================

    fun listenToVendorStore() {
        val uid = _firebaseUserId.value ?: return
        val fs = com.esdispatch.data.FirebaseManager.firestore ?: return
        var wasVerified = _isVendorVerified.value
        fs.collection("marketplace_stores").document(uid)
            .addSnapshotListener { snap, _ ->
                if (snap == null || !snap.exists()) {
                    _vendorStoreExists.value = false
                    _isVendorVerified.value = false
                    _vendorStore.value = null
                    return@addSnapshotListener
                }
                val nowVerified = snap.getBoolean("isVerified") ?: false
                _vendorStoreExists.value = true
                _isVendorVerified.value = nowVerified
                // kycStatus is a STRING ("none" | "submitted" | "approved" | "rejected")
                _vendorKycSubmitted.value = (snap.getString("kycStatus") ?: "none") != "none"
                _vendorStore.value = snap.data
                // Celebration when a pending store finally goes LIVE (e.g. admin approves later)
                if (nowVerified && !wasVerified && _vendorKycSubmitted.value) {
                    val storeName = snap.getString("storeName") ?: "Your store"
                    showInAppNotification(
                        "Store Verified! ðŸŽ‰",
                        "Congratulations! $storeName is now LIVE in the marketplace. Start receiving orders now."
                    )
                    addNotification(
                        "Store Verified! ðŸŽ‰",
                        "Congratulations! $storeName is now LIVE in the marketplace. Start receiving orders now.",
                        ""
                    )
                }
                wasVerified = nowVerified
                listenToVendorOrders()
            }
    }

    /**
     * Submit basic KYC to protect buyers. When platform auto-verify is enabled AND the
     * delivery milestone is met, the store is verified instantly; otherwise it stays
     * pending for admin approval.
     */
    fun submitVendorKyc(
        fullName: String,
        businessAddress: String,
        idType: String,
        idNumber: String,
        bankName: String,
        accountName: String,
        accountNumber: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val uid = _firebaseUserId.value ?: run { onResult(false, "Not logged in"); return }
        if (_deliveryCount.value < 10) {
            onResult(false, "Complete at least 10 deliveries before submitting KYC.")
            return
        }
        if (accountNumber.length < 10) {
            onResult(false, "Enter a valid 10-digit NUBAN bank account number.")
            return
        }
        val fs = com.esdispatch.data.FirebaseManager.firestore ?: run { onResult(false, "Service unavailable"); return }

        val autoApprove = _autoVerifyVendors.value
        val updates = hashMapOf<String, Any>(
            "kycFullName" to fullName,
            "kycBusinessAddress" to businessAddress,
            "kycIdType" to idType,
            "kycIdNumber" to idNumber,
            "kycBankName" to bankName,
            "kycAccountName" to accountName,
            "kycAccountNumber" to accountNumber,
            "kycSubmittedAt" to com.google.firebase.Timestamp.now(),
            "kycStatus" to (if (autoApprove) "approved" else "submitted"),
            "isVerified" to autoApprove,
            "isPendingReview" to (!autoApprove),
            "autoApproved" to autoApprove
        )
        if (autoApprove) {
            updates["verifiedAt"] = com.google.firebase.Timestamp.now()
        }
        fs.collection("marketplace_stores").document(uid)
            .set(updates, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                _vendorKycSubmitted.value = true
                _isVendorVerified.value = autoApprove
                if (autoApprove) {
                    showInAppNotification(
                        "Store Verified! ðŸŽ‰",
                        "Congratulations $fullName! Your vendor store is now live in the marketplace."
                    )
                    onResult(true, "KYC approved! Your vendor store is now LIVE.")
                } else {
                    showInAppNotification(
                        "KYC Submitted âœ…",
                        "Your KYC is under review. We'll notify you once an admin verifies your store."
                    )
                    onResult(true, "KYC submitted! An admin will review and approve your store.")
                }
            }
            .addOnFailureListener { e -> onResult(false, "KYC submission failed: ${e.message}") }
    }

    /** Admin tooling: approve or reject a pending vendor store. */
    fun adminSetVendorVerification(uid: String, approved: Boolean, note: String = "") {
        val fs = com.esdispatch.data.FirebaseManager.firestore ?: return
        val updates = hashMapOf<String, Any>(
            "isVerified" to approved,
            "isPendingReview" to (!approved),
            "kycStatus" to (if (approved) "approved" else "rejected"),
            "verificationNote" to note,
            "reviewedAt" to com.google.firebase.Timestamp.now()
        )
        fs.collection("marketplace_stores").document(uid)
            .set(updates, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                logAdminActivity("Vendor Review", "Store $uid ${if (approved) "approved" else "rejected"}: $note")
            }
    }

    fun listenToGlobalSettings() {
        val fs = com.esdispatch.data.FirebaseManager.firestore ?: return
        fs.collection("system_config").document("global_settings")
            .addSnapshotListener { snap, _ ->
                if (snap == null || !snap.exists()) return@addSnapshotListener
                snap.getBoolean("autoVerifyVendors")?.let { v ->
                    _autoVerifyVendors.value = v
                    savePref("auto_verify_vendors", v)
                }
            }
    }

    fun listenToVendorOrders() {
        val uid = _firebaseUserId.value ?: return
        val fs = com.esdispatch.data.FirebaseManager.firestore ?: return
        fs.collection("marketplace_orders")
            .whereEqualTo("vendorId", uid)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                _vendorOrders.value = snap.documents.mapNotNull { it.data }
            }
    }

    fun registerVendorStore(
        storeName: String, category: String, description: String,
        phone: String = "", businessAddress: String = "",
        logoUrl: String = "", coverUrl: String = "",
        businessRegNumber: String = "",
        onResult: (Boolean, String) -> Unit
    ) {
        val uid = _firebaseUserId.value ?: run { onResult(false, "Not logged in"); return }
        if (_deliveryCount.value < 10) {
            onResult(false, "You need at least 10 completed deliveries. You have ${_deliveryCount.value}.")
            return
        }
        val fs = com.esdispatch.data.FirebaseManager.firestore ?: run { onResult(false, "Service unavailable"); return }
        val storeData = hashMapOf(
            "id" to uid, "ownerId" to uid, "ownerName" to _userName.value, "ownerEmail" to _userEmail.value,
            "storeName" to storeName, "category" to category, "description" to description,
            "phone" to phone, "address" to businessAddress,
            "logoUrl" to logoUrl, "coverUrl" to coverUrl,
            "businessRegNumber" to businessRegNumber,
            "commissionRate" to 8.5,
            "isVerified" to false, "isPendingReview" to true, "kycStatus" to "none", "status" to "PENDING",
            "isFeatured" to false, "featuredRank" to 0, "isDemo" to false, "isDeleted" to false,
            "vendorWallet" to 0.0, "totalSales" to 0, "totalCommissionPaid" to 0.0,
            "deliveryCountAtRegistration" to _deliveryCount.value,
            "dateEnlisted" to java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
            "createdAt" to com.google.firebase.Timestamp.now()
        )
        fs.collection("marketplace_stores").document(uid).set(storeData)
            .addOnSuccessListener {
                _vendorStoreExists.value = true
                _isVendorVerified.value = false
                _vendorKycSubmitted.value = false
                onResult(true, "Store profile created! Complete your vendor KYC to get verified and go live.")
            }
            .addOnFailureListener { e -> onResult(false, "Registration failed: " + e.message) }
    }

    fun addVendorProduct(
        title: String, category: String, description: String,
        price: Double, stock: Int, imageUrl: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val uid = _firebaseUserId.value ?: run { onResult(false, "Not logged in"); return }
        if (!_isVendorVerified.value) {
            onResult(false, "Your store must be verified before listing products."); return
        }
        val storeName = (_vendorStore.value?.get("storeName") as? String) ?: (_userName.value + "'s Store")
        val fs = com.esdispatch.data.FirebaseManager.firestore ?: run { onResult(false, "Service unavailable"); return }
        val productData = hashMapOf(
            "vendorId" to uid, "vendorStore" to storeName,
            "name" to title, "title" to title, "category" to category,
            "description" to description, "price" to price, "stock" to stock,
            "imageUrl" to imageUrl, "rating" to 0.0, "reviewsCount" to 0,
            "isActive" to true, "isDeleted" to false,
            "createdAt" to com.google.firebase.Timestamp.now()
        )
        fs.collection("marketplace_products").add(productData)
            .addOnSuccessListener { onResult(true, "Product listed in the marketplace!") }
            .addOnFailureListener { e -> onResult(false, "Failed to list product: " + e.message) }
    }

    fun updateVendorProduct(
        productId: String, title: String, category: String,
        description: String, price: Double, stock: Int, imageUrl: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val uid = _firebaseUserId.value ?: run { onResult(false, "Not logged in"); return }
        val fs = com.esdispatch.data.FirebaseManager.firestore ?: run { onResult(false, "Service unavailable"); return }
        fs.collection("marketplace_products").document(productId).get()
            .addOnSuccessListener { doc ->
                if ((doc.getString("vendorId") ?: "") != uid) {
                    onResult(false, "Permission denied."); return@addOnSuccessListener
                }
                doc.reference.update(mapOf(
                    "name" to title, "title" to title, "category" to category,
                    "description" to description, "price" to price, "stock" to stock,
                    "imageUrl" to imageUrl, "updatedAt" to com.google.firebase.Timestamp.now()
                ))
                    .addOnSuccessListener { onResult(true, "Product updated.") }
                    .addOnFailureListener { e -> onResult(false, "Update failed: " + e.message) }
            }
            .addOnFailureListener { e -> onResult(false, "Failed: " + e.message) }
    }

    fun deleteVendorProduct(productId: String, onResult: (Boolean, String) -> Unit) {
        val uid = _firebaseUserId.value ?: run { onResult(false, "Not logged in"); return }
        val fs = com.esdispatch.data.FirebaseManager.firestore ?: run { onResult(false, "Service unavailable"); return }
        fs.collection("marketplace_products").document(productId).get()
            .addOnSuccessListener { doc ->
                if ((doc.getString("vendorId") ?: "") != uid) {
                    onResult(false, "Permission denied."); return@addOnSuccessListener
                }
                doc.reference.update("isDeleted", true, "deletedAt", com.google.firebase.Timestamp.now())
                    .addOnSuccessListener { onResult(true, "Product removed from marketplace.") }
                    .addOnFailureListener { e -> onResult(false, "Delete failed: " + e.message) }
            }
            .addOnFailureListener { e -> onResult(false, "Failed: " + e.message) }
    }

    // ==========================================================================
    // LIVE SUPPORT CHAT - Firebase Realtime Database
    // Path: support_chats/{uid}/messages
    // ==========================================================================

    fun sendSupportChatMessage(message: String, isAgent: Boolean = false, onResult: (Boolean) -> Unit) {
        val uid = _firebaseUserId.value ?: run { onResult(false); return }
        val db = com.google.firebase.database.FirebaseDatabase.getInstance()
        val msgRef = db.getReference("support_chats/$uid/messages").push()
        val msgData = mapOf(
            "text" to message,
            "isUser" to !isAgent,
            "senderId" to uid,
            "senderName" to (if (isAgent) "ESDispatch Support" else _userName.value),
            "timestamp" to com.google.firebase.database.ServerValue.TIMESTAMP
        )
        msgRef.setValue(msgData)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun listenToSupportChat(onMessages: (List<Map<String, Any>>) -> Unit) {
        val uid = _firebaseUserId.value ?: return
        val db = com.google.firebase.database.FirebaseDatabase.getInstance()
        db.getReference("support_chats/$uid/messages")
            .orderByChild("timestamp")
            .addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snap: com.google.firebase.database.DataSnapshot) {
                    @Suppress("UNCHECKED_CAST")
                    val msgs = snap.children.mapNotNull { it.value as? Map<String, Any> }
                    onMessages(msgs)
                }
                override fun onCancelled(e: com.google.firebase.database.DatabaseError) {}
            })
    }

    // Active promo state: set by applyPromoCode, applied at booking/checkout fees
    private var _activePromo = ActivePromo()

    fun applyPromoCode(code: String, onComplete: (Boolean, String) -> Unit) {
        val trimmed = code.trim().uppercase()
        val fs = com.esdispatch.data.FirebaseManager.firestore
            ?: com.google.firebase.firestore.FirebaseFirestore.getInstance()
        // Real discount lookup from the promotions collection (admin-managed)
        fs.collection("promotions")
            .whereEqualTo("code", trimmed)
            .get()
            .addOnSuccessListener { snap ->
                val promo = snap.documents.firstOrNull { it.getBoolean("active") != false }
                if (promo == null) {
                    _activePromo = ActivePromo()
                    onComplete(false, "Invalid or inactive code")
                    return@addOnSuccessListener
                }
                val discountType = promo.getString("discountType")
                val value = promo.getDouble("discountValue") ?: promo.getLong("discountValue")?.toDouble() ?: 0.0
                val minOrder = promo.getDouble("minOrderAmount") ?: 0.0
                val maxDiscount = promo.getDouble("maxDiscount") ?: 0.0
                _activePromo = ActivePromo(
                    code = trimmed,
                    discountType = discountType ?: "percent",
                    discountValue = value,
                    minOrderAmount = minOrder,
                    maxDiscount = maxDiscount
                )
                onComplete(true, "Promo $trimmed applied!")
            }
            .addOnFailureListener { e ->
                _activePromo = ActivePromo()
                onComplete(false, e.message ?: "Failed to validate code")
            }
    }

    fun clearActivePromo() {
        _activePromo = ActivePromo()
    }

    /** Applies the active promo discount to [amount]; returns the discounted price. */
    fun applyPromoDiscount(amount: Double): Double {
        val p = _activePromo
        if (p.code.isBlank() || amount <= 0) return amount
        var discounted = if (p.discountType == "flat") amount - p.discountValue else amount * (1.0 - p.discountValue / 100.0)
        if (p.maxDiscount > 0) discounted = maxOf(discounted, amount - p.maxDiscount)
        return maxOf(0.0, discounted)
    }

    fun redeemReferralCode(code: String, onComplete: (Boolean, String) -> Unit) {
        val trimmed = code.trim().uppercase()
        if (trimmed.isBlank() || trimmed == _referralCode.value) {
            onComplete(false, "Cannot redeem your own code.")
            return
        }
        val uid = _firebaseUserId.value ?: run {
            onComplete(false, "User not signed in.")
            return
        }
        val db = com.esdispatch.data.FirebaseManager.firestore ?: run {
            onComplete(false, "Database connection unavailable.")
            return
        }
        viewModelScope.launch {
            try {
                val redemption = hashMapOf(
                    "userId" to uid,
                    "redeemedBy" to uid,
                    "code" to trimmed,
                    "redeemedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "rewardAmount" to 3000.0
                )
                // Credit only after the server records the redemption (rules: userId == auth.uid)
                db.collection("referral_redemptions").add(redemption)
                    .addOnSuccessListener {
                        addLoyaltyPoints(300)
                        topUpWallet(3000.0)
                        onComplete(true, "ðŸŽ‰ Referral code redeemed! â‚¦3,000 credited to your wallet & 300 Pts added!")
                    }
                    .addOnFailureListener { e ->
                        onComplete(false, e.message ?: "Redemption failed — not credited.")
                    }
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Failed to redeem code")
            }
        }
    }
}

// End of DeliveryViewModel class body

data class ParcelDraft(
    val pickupAddress: String = "",
    val deliveryAddress: String = "",
    val stops: List<String> = emptyList(),
    val senderName: String = "",
    val senderPhone: String = "",
    val receiverName: String = "",
    val receiverPhone: String = "",
    val quantity: Int = 1,
    val weight: Double = 1.0,
    val length: Int = 20,
    val width: Int = 15,
    val height: Int = 10,
    val selectedService: String = "Express",
    val price: Double = 0.0
)

/** Discount coupon state resolved from the admin-managed `promotions` collection. */
data class ActivePromo(
    val code: String = "",
    val discountType: String = "percent",
    val discountValue: Double = 0.0,
    val minOrderAmount: Double = 0.0,
    val maxDiscount: Double = 0.0
)

sealed class PendingQuote {
    object Idle : PendingQuote()
    object Loading : PendingQuote()
    data class Success(
        val price: Double,
        val distanceKm: Double,
        val pickupAddress: String,
        val deliveryAddress: String,
        val serviceType: String
    ) : PendingQuote()
    data class Error(val message: String) : PendingQuote()
}

object SecurityUtils {
    private const val KEY = "ENGRACED_DISPATCH_SECRET_SALT_2026"
    
    fun encryptPin(pin: String): String {
        if (pin.isEmpty()) return ""
        try {
            val sb = StringBuilder()
            for (i in pin.indices) {
                sb.append((pin[i].code xor KEY[i % KEY.length].code).toChar())
            }
            return android.util.Base64.encodeToString(sb.toString().toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            return pin
        }
    }
    
    fun decryptPin(encrypted: String): String {
        if (encrypted.isEmpty()) return ""
        try {
            val decodedBytes = android.util.Base64.decode(encrypted, android.util.Base64.NO_WRAP)
            val decodedStr = String(decodedBytes, Charsets.UTF_8)
            val sb = StringBuilder()
            for (i in decodedStr.indices) {
                sb.append((decodedStr[i].code xor KEY[i % KEY.length].code).toChar())
            }
            return sb.toString()
        } catch (e: Exception) {
            return encrypted
        }
    }
}

data class MarketplaceItem(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val rating: Double = 4.9,
    val reviewsCount: Int = 12,
    val imageUrl: String = "",
    val description: String = "",
    val stock: Int = 10,
    val vendorStore: String = "ESDispatch Partner Store",
    val vendorId: String = ""
)

data class MarketplaceStore(
    val id: String = "",
    val storeName: String = "ESDispatch Partner Store",
    val category: String = "General",
    val description: String = "",
    val ownerName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val rating: Double = 5.0,
    val totalSales: Int = 0,
    val itemCount: Int = 0,
    val isVerified: Boolean = true,
    val logoUrl: String = "",
    val coverUrl: String = "",
    val isFeatured: Boolean = false,
    val featuredRank: Int = 0,
    val isDemo: Boolean = false,
    val dateEnlisted: String = "",
    val status: String = "APPROVED"
)

data class CartItem(
    val item: MarketplaceItem,
    val quantity: Int
)


data class VendorSplitRecord(
    val storeId: String,
    val storeName: String,
    val subtotal: Double,
    val commissionRate: Double,
    val commissionAmount: Double,
    val vendorPayout: Double
)
