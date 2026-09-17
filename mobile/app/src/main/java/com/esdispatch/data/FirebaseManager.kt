package com.esdispatch.data

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import com.google.firebase.firestore.DocumentSnapshot

fun DocumentSnapshot.getSafeDouble(field: String, default: Double = 0.0): Double {
    val obj = get(field) ?: return default
    return when (obj) {
        is Number -> obj.toDouble()
        is String -> obj.toDoubleOrNull() ?: default
        else -> default
    }
}

fun DocumentSnapshot.getSafeDoubleNullable(field: String): Double? {
    val obj = get(field) ?: return null
    return when (obj) {
        is Number -> obj.toDouble()
        is String -> obj.toDoubleOrNull()
        else -> null
    }
}

fun DocumentSnapshot.getSafeInt(field: String, default: Int = 0): Int {
    val obj = get(field) ?: return default
    return when (obj) {
        is Number -> obj.toInt()
        is String -> obj.toIntOrNull() ?: default
        else -> default
    }
}

fun DocumentSnapshot.getSafeLong(field: String, default: Long = 0L): Long {
    val obj = get(field) ?: return default
    return when (obj) {
        is Number -> obj.toLong()
        is String -> obj.toLongOrNull() ?: default
        else -> default
    }
}

fun DocumentSnapshot.getSafeBoolean(field: String, default: Boolean = false): Boolean {
    val obj = get(field) ?: return default
    return when (obj) {
        is Boolean -> obj
        is String -> obj.toBoolean()
        is Number -> obj.toInt() != 0
        else -> default
    }
}

object FirebaseManager {
    private const val TAG = "FirebaseManager"

    private val _fcmNotifications = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 64)
    val fcmNotifications: SharedFlow<Pair<String, String>> = _fcmNotifications.asSharedFlow()

    fun triggerFcmNotification(title: String, body: String) {
        _fcmNotifications.tryEmit(Pair(title, body))
    }
    
    // Dynamically retrieved Firebase instances with safety guards to handle delayed FirebaseApp initialization
    val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseAuth initialization failed: ${e.message}")
            null
        }

    val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseFirestore initialization failed: ${e.message}")
            null
        }

    /**
     * Check if Firebase is ready and authenticated
     */
    fun isFirebaseAvailable(): Boolean {
        return auth != null && firestore != null
    }

    /**
     * Authenticate session anonymously or with email/password
     */
    fun signInUserAnonymously(onComplete: (Boolean, FirebaseUser?) -> Unit) {
        val authInstance = auth
        if (authInstance == null) {
            onComplete(false, null)
            return
        }
        
        authInstance.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Anonymous user signed in successfully: ${task.result?.user?.uid}")
                    onComplete(true, task.result?.user)
                } else {
                    Log.w(TAG, "Anonymous sign-in failed: ${task.exception?.message}")
                    onComplete(false, null)
                }
            }
    }

    fun getDynamicPassword(email: String, pin: String): String {
        val cleanPrefix = email.trim().lowercase().substringBefore("@")
        var hash = 0
        for (ch in cleanPrefix) {
            hash = (hash * 31) + ch.code
        }
        val absHashStr = kotlin.math.abs(hash).toString().take(6).padEnd(6, 's')
        return "${pin}${pin}_$absHashStr"
    }

    /**
     * Create account via Firebase Authentication (email and PIN mapping)
     */
    fun signUpWithEmailAndPassword(
        email: String,
        pin: String,
        name: String,
        phone: String,
        role: String = "customer",
        bikeNumber: String = "",
        onComplete: (Boolean, FirebaseUser?, String?) -> Unit
    ) {
        val authInstance = auth
        if (authInstance == null) {
            onComplete(false, null, "Firebase Authentication service not available.")
            return
        }
        val password = getDynamicPassword(email, pin)
        authInstance.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        saveUserProfileToFirestore(user.uid, name, email, phone, role, bikeNumber)
                        onComplete(true, user, null)
                    } else {
                        onComplete(false, null, "Failed to retrieve authenticated user account.")
                    }
                } else {
                    onComplete(false, null, task.exception?.localizedMessage ?: "Registration failed.")
                }
            }
    }

    /**
     * Check if email is already taken in real time
     */
    fun checkEmailExists(email: String, onComplete: (Boolean) -> Unit) {
        val authInstance = auth
        if (authInstance == null || email.isBlank() || !email.contains("@")) {
            onComplete(false)
            return
        }
        val currentEmail = authInstance.currentUser?.email
        if (currentEmail != null && currentEmail.equals(email.trim(), ignoreCase = true)) {
            // User is currently authenticated with this email (e.g. completing Google sign-up onboarding)
            onComplete(false)
            return
        }
        authInstance.fetchSignInMethodsForEmail(email.trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    onComplete(!signInMethods.isNullOrEmpty())
                } else {
                    onComplete(false)
                }
            }
    }

    /**
     * Check if phone number is already taken in real time
     */
    fun checkPhoneExists(phone: String, onComplete: (Boolean) -> Unit) {
        val db = firestore
        if (db == null) {
            onComplete(false)
            return
        }
        val currentUid = auth?.currentUser?.uid
        db.collection("users").whereEqualTo("phone", phone.trim())
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Filter out the user's own document if they are updating or completing onboarding
                val matchingDocs = querySnapshot.documents.filter { it.id != currentUid }
                onComplete(matchingDocs.isNotEmpty())
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    /**
     * Log in via Firebase Authentication (email and PIN mapping)
     */
    fun signInWithEmailAndPassword(
        email: String,
        pin: String,
        onComplete: (Boolean, FirebaseUser?, String?) -> Unit
    ) {
        val authInstance = auth
        if (authInstance == null) {
            onComplete(false, null, "Firebase Authentication service not available.")
            return
        }
        val password = getDynamicPassword(email, pin)
        authInstance.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, task.result?.user, null)
                } else {
                    onComplete(false, null, task.exception?.localizedMessage ?: "Invalid email or PIN.")
                }
            }
    }

    /**
     * Authenticate with Firebase using Google OAuth ID Token
     */
    fun signInWithGoogleIdToken(
        idToken: String,
        onComplete: (Boolean, FirebaseUser?, String?) -> Unit
    ) {
        val authInstance = auth
        if (authInstance == null) {
            onComplete(false, null, "Firebase Authentication service not available.")
            return
        }
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        authInstance.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        onComplete(true, user, null)
                    } else {
                        onComplete(false, null, "Failed to retrieve Google authenticated user account.")
                    }
                } else {
                    onComplete(false, null, task.exception?.localizedMessage ?: "Google sign-in credential link failed.")
                }
            }
    }

    /**
     * Send password/PIN reset email via Firebase Authentication
     */
    fun sendPasswordResetEmail(
        email: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val authInstance = auth
        if (authInstance == null) {
            onComplete(false, "Firebase Authentication service not available.")
            return
        }
        authInstance.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.localizedMessage ?: "Failed to send reset email.")
                }
            }
    }

    /**
     * Real-time listener for user profile session details in Firestore
     */
    fun saveUserProfileToFirestore(userId: String, name: String, email: String, phone: String, role: String? = null, bikeNumber: String = "") {
        val db = firestore ?: return
        val userMap = hashMapOf<String, Any>(
            "uid" to userId,
            "name" to name,
            "email" to email,
            "phone" to phone,
            "updatedAt" to System.currentTimeMillis()
        )
        if (!role.isNullOrBlank()) {
            userMap["role"] = role
        }
        if (bikeNumber.isNotBlank()) {
            userMap["bikeNumber"] = bikeNumber
        }
        if (role == "rider") {
            userMap["latitude"] = 6.3350
            userMap["longitude"] = 5.6037
            userMap["rating"] = 5.0
            userMap["currentWorkload"] = 0
            userMap["batteryLevel"] = 100
            userMap["averageDeliveryTimeMin"] = 15
        }
        
        db.collection("users").document(userId)
            .set(userMap, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "User profile synced to Firestore.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "User profile sync failed: ${e.message}")
            }
    }

    /**
     * Real-time listener for Admin Hero Banners & Slides from Firestore
     */
    fun listenToHeroBanners(): Flow<List<HeroSlideItem>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = db.collection("banners")
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.w(TAG, "Hero banners snapshot error: ${error?.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val slides = snapshot.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: ""
                    val subtitle = doc.getString("subtitle") ?: ""
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    val active = doc.getBoolean("active") ?: true
                    if (title.isNotBlank() || imageUrl.isNotBlank()) {
                        HeroSlideItem(
                            id = doc.id,
                            title = title,
                            subtitle = subtitle,
                            imageUrl = imageUrl,
                            active = active,
                            tag = "FEATURED",
                            actionText = "Explore"
                        )
                    } else null
                }
                trySend(slides)
            }

        awaitClose { registration.remove() }
    }

    /**
     * Update any user's online presence and lastSeen timestamp in Firestore
     */
    fun updateUserPresence(userId: String, isOnline: Boolean) {
        val db = firestore ?: return
        val presenceMap = hashMapOf(
            "isOnline" to isOnline,
            "status" to (if (isOnline) "online" else "offline"),
            "lastSeen" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection("users").document(userId)
            .set(presenceMap, com.google.firebase.firestore.SetOptions.merge())
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to update user presence: ${e.message}")
            }
    }

    /**
     * Update a rider's active online availability and status in Firestore
     */
    fun updateRiderOnlineStatus(userId: String, isOnline: Boolean) {
        val db = firestore ?: return
        val statusMap = hashMapOf(
            "isOnline" to isOnline,
            "is_active" to isOnline,
            "isActive" to isOnline,
            "status" to (if (isOnline) "active" else "offline"),
            "lastSeen" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection("users").document(userId)
            .set(statusMap, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Rider online status updated in users collection to: $isOnline")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update rider online status in users: ${e.message}")
            }
    }

    /**
     * Send real-time FCM notification and store in user notifications collection
     */
    fun sendNotificationToUser(userId: String, title: String, message: String, parcelId: String? = null, customNotifId: String? = null) {
        val db = firestore ?: return
        if (userId.isEmpty()) return
        val notifId = customNotifId ?: "NOTIF-${System.currentTimeMillis()}"
        val notifMap = hashMapOf(
            "id" to notifId,
            "title" to title,
            "message" to message,
            "time" to "Just now",
            "isRead" to false,
            "parcelId" to (parcelId ?: ""),
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("users").document(userId)
            .collection("notifications").document(notifId)
            .set(notifMap)
            .addOnSuccessListener {
                Log.d(TAG, "Notification stored for user $userId: $title")
                triggerFcmNotification(title, message)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to store notification: ${e.message}")
            }
    }

    /**
     * Clear all notifications for a specific user from Firestore
     */
    fun clearAllUserNotifications(userId: String, onComplete: ((Boolean) -> Unit)? = null) {
        val db = firestore ?: run {
            onComplete?.invoke(false)
            return
        }
        val targetUid = auth?.currentUser?.uid ?: userId
        if (targetUid.isBlank()) return
        db.collection("users").document(targetUid)
            .collection("notifications")
            .get()
            .addOnSuccessListener { snapshot ->
                val docs = snapshot.documents
                if (docs.isEmpty()) {
                    onComplete?.invoke(true)
                    return@addOnSuccessListener
                }
                val chunks = docs.chunked(400)
                var remaining = chunks.size
                var hadError = false
                for (chunk in chunks) {
                    val batch = db.batch()
                    chunk.forEach { doc -> batch.delete(doc.reference) }
                    batch.commit().addOnCompleteListener { task ->
                        if (!task.isSuccessful) hadError = true
                        remaining--
                        if (remaining == 0) {
                            onComplete?.invoke(!hadError)
                        }
                    }
                }
            }
            .addOnFailureListener { onComplete?.invoke(false) }
    }

    /**
     * Delete a single notification for a specific user from Firestore
     */
    fun deleteUserNotification(userId: String, notificationId: String, onComplete: ((Boolean) -> Unit)? = null) {
        val db = firestore ?: run {
            onComplete?.invoke(false)
            return
        }
        val targetUid = auth?.currentUser?.uid ?: userId
        if (targetUid.isBlank() || notificationId.isBlank()) return
        val docRef = db.collection("users").document(targetUid)
            .collection("notifications").document(notificationId)
        docRef.delete()
            .addOnSuccessListener { onComplete?.invoke(true) }
            .addOnFailureListener {
                db.collection("users").document(targetUid)
                    .collection("notifications")
                    .whereEqualTo("id", notificationId)
                    .get()
                    .addOnSuccessListener { snap ->
                        val b = db.batch()
                        snap.documents.forEach { b.delete(it.reference) }
                        b.commit().addOnCompleteListener { onComplete?.invoke(true) }
                    }
                    .addOnFailureListener { onComplete?.invoke(false) }
            }
    }

    /**
     * Update User Wallet Balance using an Atomic Transaction to prevent race conditions.
     */
    fun updateUserWalletBalance(userId: String, amountDelta: Double, onComplete: (Boolean, Double) -> Unit) {
        val db = firestore ?: run {
            onComplete(false, 0.0)
            return
        }
        val userRef = db.collection("users").document(userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentBalance = snapshot.getDouble("walletBalance") ?: 0.0
            val newBalance = currentBalance + amountDelta
            transaction.set(userRef, mapOf("walletBalance" to newBalance), com.google.firebase.firestore.SetOptions.merge())
            newBalance
        }.addOnSuccessListener { newBalance ->
            Log.d(TAG, "Wallet balance atomically updated to: $newBalance")
            onComplete(true, newBalance)
        }.addOnFailureListener { e ->
            Log.e(TAG, "Failed atomic wallet balance update: ${e.message}")
            onComplete(false, 0.0)
        }
    }

    /**
     * Record a Double-Entry Ledger Transaction in Firestore.
     */
    fun recordLedgerTransaction(
        userId: String,
        amount: Double,
        title: String,
        isTopUp: Boolean,
        reference: String,
        status: String = "SUCCESS",
        onComplete: (Boolean) -> Unit
    ) {
        val db = firestore
        if (db == null) {
            onComplete(false)
            return
        }

        val type = if (amount > 0 || isTopUp) "CREDIT" else "DEBIT"
        val transactionId = "TXN-${System.currentTimeMillis()}-${(1000..9999).random()}"

        val txnMap = hashMapOf(
            "id" to transactionId,
            "title" to title,
            "date" to java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
            "amount" to kotlin.math.abs(amount),
            "isTopUp" to isTopUp,
            "type" to type,
            "status" to status,
            "reference" to reference,
            "userId" to userId,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users").document(userId)
            .collection("transactions").document(transactionId)
            .set(txnMap)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to record ledger transaction: ${e.message}")
                onComplete(false)
            }
    }

    /**
     * Push or update delivery real-time status in Firestore, associated with a user's personal account
     */
    fun syncParcelToFirestore(parcel: Parcel, userId: String) {
        val db = firestore ?: return
        val currentAuthUid = auth?.currentUser?.uid
        val effectiveUserId = currentAuthUid ?: userId.ifBlank { parcel.userId }
        val parcelMap = hashMapOf(
            "id" to parcel.id,
            "itemName" to parcel.itemName,
            "imageUrl" to parcel.imageUrl,
            "status" to parcel.status.name,
            "pickupAddress" to parcel.pickupAddress,
            "deliveryAddress" to parcel.deliveryAddress,
            "senderName" to parcel.senderName,
            "senderPhone" to parcel.senderPhone,
            "receiverName" to parcel.receiverName,
            "receiverPhone" to parcel.receiverPhone,
            "quantity" to parcel.quantity,
            "weight" to parcel.weight,
            "length" to parcel.length,
            "width" to parcel.width,
            "height" to parcel.height,
            "price" to parcel.price,
            "courierName" to parcel.courierName,
            "courierPhone" to parcel.courierPhone,
            "courierAvatar" to parcel.courierAvatar,
            "progress" to parcel.progress,
            "dateString" to parcel.dateString,
            "userId" to effectiveUserId,
            "riderId" to parcel.riderId,
            "riderBikeNumber" to parcel.riderBikeNumber,
            "otpCode" to parcel.otpCode,
            "otpExpiresAt" to (System.currentTimeMillis() + 60 * 60 * 1000L),
            "otpVerified" to parcel.otpVerified,
            "isRated" to parcel.isRated,
            "customerRating" to parcel.customerRating,
            "tipAmount" to parcel.tipAmount,
            "additionalStops" to parcel.additionalStops,
            "category" to parcel.category.ifBlank { "Standard" },
            "createdAt" to if (parcel.createdAt > 0L) parcel.createdAt else System.currentTimeMillis(),
            "pickupLat" to parcel.pickupLat,
            "pickupLng" to parcel.pickupLng,
            "deliveryLat" to parcel.deliveryLat,
            "deliveryLng" to parcel.deliveryLng,
            "lastUpdated" to System.currentTimeMillis()
        )

        db.collection("deliveries").document(parcel.id)
            .set(parcelMap, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Parcel ${parcel.id} synced globally to deliveries collection.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to sync parcel globally: ${e.message}")
            }

        if (effectiveUserId.isNotBlank()) {
            db.collection("users").document(effectiveUserId)
                .collection("deliveries").document(parcel.id)
                .set(parcelMap, com.google.firebase.firestore.SetOptions.merge())
        }
    }

    /**
     * Fetch user's parcel history from Firestore personal account
     */
    fun parseParcelFromDoc(doc: DocumentSnapshot, fallbackUserId: String = ""): Parcel {
        val id = doc.getString("id")?.takeIf { it.isNotBlank() } ?: doc.id
        val itemName = doc.getString("itemName")?.takeIf { it.isNotBlank() } ?: "Standard Package"
        val imageUrl = doc.getString("imageUrl") ?: ""
        val statusStr = doc.getString("status") ?: ParcelStatus.TRANSIT.name
        val status = try { ParcelStatus.valueOf(statusStr) } catch(_: Exception) { ParcelStatus.TRANSIT }
        val pickupAddress = doc.getString("pickupAddress") ?: ""
        val deliveryAddress = doc.getString("deliveryAddress") ?: ""
        val senderName = doc.getString("senderName") ?: ""
        val senderPhone = doc.getString("senderPhone") ?: ""
        val receiverName = doc.getString("receiverName") ?: ""
        val receiverPhone = doc.getString("receiverPhone") ?: ""
        val quantity = doc.getSafeInt("quantity", 1)
        val weight = doc.getSafeDouble("weight", 1.0)
        val length = doc.getSafeInt("length", 10)
        val width = doc.getSafeInt("width", 10)
        val height = doc.getSafeInt("height", 10)
        val price = doc.getSafeDouble("price", 0.0)
        val courierName = doc.getString("courierName") ?: ""
        val courierPhone = doc.getString("courierPhone") ?: ""
        val courierAvatar = doc.getString("courierAvatar") ?: ""
        val progress = doc.getSafeDouble("progress", 0.0).toFloat()
        val dateString = doc.getString("dateString") ?: "Today"
        val courierLatitude = doc.getSafeDoubleNullable("courierLatitude")
        val courierLongitude = doc.getSafeDoubleNullable("courierLongitude")
        val riderId = doc.getString("riderId")?.takeIf { it.isNotBlank() } ?: doc.getString("driverId") ?: ""
        val riderBikeNumber = doc.getString("riderBikeNumber") ?: ""
        val otpCode = doc.getString("otpCode") ?: ""
        val otpVerified = doc.getSafeBoolean("otpVerified", false)
        val isRated = doc.getSafeBoolean("isRated", false)
        val customerRating = doc.getSafeDouble("customerRating", 0.0)
        val tipAmount = doc.getSafeDouble("tipAmount", 0.0)
        val additionalStops = doc.getString("additionalStops") ?: ""
        val reservedRiderId = doc.getString("reservedRiderId") ?: ""
        val reservedCourierName = doc.getString("reservedCourierName") ?: ""
        val reservedCourierPhone = doc.getString("reservedCourierPhone") ?: ""
        val podUrl = doc.getString("podUrl") ?: ""
        val podStatus = doc.getString("podStatus") ?: ""
        val payoutCredited = doc.getSafeBoolean("payoutCredited", false)
        val category = doc.getString("category")?.takeIf { it.isNotBlank() } ?: "Standard"
        val pickupLat = doc.getSafeDoubleNullable("pickupLat")
        val pickupLng = doc.getSafeDoubleNullable("pickupLng")
        val deliveryLat = doc.getSafeDoubleNullable("deliveryLat")
        val deliveryLng = doc.getSafeDoubleNullable("deliveryLng")
        val createdAt = doc.getSafeLong("createdAt", 0L)
        val userId = doc.getString("userId")?.takeIf { it.isNotBlank() } ?: fallbackUserId

        return Parcel(
            id = id,
            itemName = itemName,
            imageUrl = imageUrl,
            status = status,
            pickupAddress = pickupAddress,
            deliveryAddress = deliveryAddress,
            senderName = senderName,
            senderPhone = senderPhone,
            receiverName = receiverName,
            receiverPhone = receiverPhone,
            quantity = quantity,
            weight = weight,
            length = length,
            width = width,
            height = height,
            price = price,
            courierName = courierName,
            courierPhone = courierPhone,
            courierAvatar = courierAvatar,
            progress = progress,
            dateString = dateString,
            userId = userId,
            courierLatitude = courierLatitude,
            courierLongitude = courierLongitude,
            additionalStops = additionalStops,
            riderId = riderId,
            riderBikeNumber = riderBikeNumber,
            otpCode = otpCode,
            otpVerified = otpVerified,
            isRated = isRated,
            customerRating = customerRating,
            tipAmount = tipAmount,
            createdAt = createdAt,
            reservedRiderId = reservedRiderId,
            reservedCourierName = reservedCourierName,
            reservedCourierPhone = reservedCourierPhone,
            podUrl = podUrl,
            podStatus = podStatus,
            payoutCredited = payoutCredited,
            category = category,
            pickupLat = pickupLat,
            pickupLng = pickupLng,
            deliveryLat = deliveryLat,
            deliveryLng = deliveryLng
        )
    }

    fun fetchUserParcelHistory(userId: String, onComplete: (List<Parcel>) -> Unit) {
        val db = firestore
        if (db == null) {
            onComplete(emptyList())
            return
        }

        // Primary source of truth: root deliveries collection
        db.collection("deliveries")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { rootSnapshot ->
                val list = mutableListOf<Parcel>()
                val seenIds = mutableSetOf<String>()
                for (doc in rootSnapshot.documents) {
                    try {
                        val parcel = parseParcelFromDoc(doc, userId)
                        list.add(parcel)
                        seenIds.add(parcel.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing root parcel document: ${e.message}")
                    }
                }

                // Check user subcollection for legacy deliveries not yet mirrored to root
                db.collection("users").document(userId)
                    .collection("deliveries")
                    .get()
                    .addOnSuccessListener { subSnapshot ->
                        for (doc in subSnapshot.documents) {
                            try {
                                val id = doc.getString("id")?.takeIf { it.isNotBlank() } ?: doc.id
                                if (!seenIds.contains(id)) {
                                    val parcel = parseParcelFromDoc(doc, userId)
                                    list.add(parcel)
                                    seenIds.add(parcel.id)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing subcollection parcel: ${e.message}")
                            }
                        }
                        onComplete(list)
                    }
                    .addOnFailureListener {
                        onComplete(list)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to fetch deliveries from root collection: ${e.message}")
                db.collection("users").document(userId)
                    .collection("deliveries")
                    .get()
                    .addOnSuccessListener { subSnapshot ->
                        val list = subSnapshot.documents.mapNotNull { doc ->
                            try { parseParcelFromDoc(doc, userId) } catch (_: Exception) { null }
                        }
                        onComplete(list)
                    }
                    .addOnFailureListener {
                        onComplete(emptyList())
                    }
            }
    }

    /**
     * Fetch active promotions from Firestore
     */
    fun getPromotions(onComplete: (List<com.esdispatch.data.PromoCode>) -> Unit) {
        val db = firestore
        if (db == null) {
            onComplete(emptyList())
            return
        }

        db.collection("promotions").get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(com.esdispatch.data.PromoCode::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing promo document: ${e.message}")
                        null
                    }
                }
                onComplete(list)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to fetch promotions: ${e.message}")
                onComplete(emptyList())
            }
    }

    /**
     * Applies a promo code using a secure Firestore transaction.
     */
    fun applyPromoCode(code: String, onComplete: (Boolean, String) -> Unit) {
        val db = firestore
        val user = auth?.currentUser
        if (db == null || user == null) {
            onComplete(false, "Authentication required")
            return
        }

        val userRef = db.collection("users").document(user.uid)
        
        db.runTransaction { transaction ->
            val userSnapshot = transaction.get(userRef)
            if (!userSnapshot.exists()) {
                throw Exception("User not found")
            }
            
            val applied = userSnapshot.get("appliedPromos") as? List<String> ?: emptyList()
            if (applied.contains(code.uppercase())) {
                throw Exception("Promo code already applied")
            }
            
            val newApplied = applied + code.uppercase()
            transaction.update(userRef, "appliedPromos", newApplied)
        }.addOnSuccessListener {
            onComplete(true, "Promo code applied successfully!")
        }.addOnFailureListener { e ->
            onComplete(false, e.message ?: "Failed to apply promo code")
        }
    }

    /**
     * Push or update delivery real-time status in Firestore
     */
    fun syncParcelToFirestore(parcel: Parcel) {
        val currentAuthUid = auth?.currentUser?.uid
        val effectiveUserId = currentAuthUid ?: parcel.userId
        syncParcelToFirestore(parcel, effectiveUserId)
    }

    /**
     * Listens to real-time Firestore delivery tracking updates
     */
    fun listenToParcelTracking(parcelId: String): Flow<Parcel?> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val docRef = db.collection("deliveries").document(parcelId)
        val registration: ListenerRegistration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Snapshot listener error for $parcelId: ${error.message}")
                trySend(null)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                try {
                    val parcel = parseParcelFromDoc(snapshot)
                    trySend(parcel)
                } catch (e: Exception) {
                    Log.e(TAG, "Error mapping tracking snapshot: ${e.message}")
                    trySend(null)
                }
            } else {
                trySend(null)
            }
        }

        awaitClose {
            Log.d(TAG, "Closing snap listener for $parcelId")
            registration.remove()
        }
    }

    /**
     * Fetches a single parcel snapshot directly from Firestore.
     */
    suspend fun fetchParcel(parcelId: String): Parcel? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val db = firestore ?: return@withContext null
        try {
            val snapshot = com.google.android.gms.tasks.Tasks.await(
                db.collection("deliveries").document(parcelId).get()
            )
            if (snapshot != null && snapshot.exists()) {
                val id = snapshot.getString("id") ?: parcelId
                val itemName = snapshot.getString("itemName") ?: "Mac mini M2"
                val imageUrl = snapshot.getString("imageUrl") ?: ""
                val statusStr = snapshot.getString("status") ?: ParcelStatus.TRANSIT.name
                val status = try { ParcelStatus.valueOf(statusStr) } catch(e: Exception) { ParcelStatus.TRANSIT }
                val pickupAddress = snapshot.getString("pickupAddress") ?: ""
                val deliveryAddress = snapshot.getString("deliveryAddress") ?: ""
                val senderName = snapshot.getString("senderName") ?: ""
                val senderPhone = snapshot.getString("senderPhone") ?: ""
                val receiverName = snapshot.getString("receiverName") ?: ""
                val receiverPhone = snapshot.getString("receiverPhone") ?: ""
                val quantity = snapshot.getSafeInt("quantity", 1)
                val weight = snapshot.getSafeDouble("weight", 1.0)
                val length = snapshot.getSafeInt("length", 10)
                val width = snapshot.getSafeInt("width", 10)
                val height = snapshot.getSafeInt("height", 10)
                val price = snapshot.getSafeDouble("price", 0.0)
                val courierName = snapshot.getString("courierName") ?: ""
                val courierPhone = snapshot.getString("courierPhone") ?: ""
                val courierAvatar = snapshot.getString("courierAvatar") ?: ""
                val progress = snapshot.getSafeDouble("progress", 0.35).toFloat()
                val dateString = snapshot.getString("dateString") ?: "Today"
                val courierLatitude = snapshot.getSafeDoubleNullable("courierLatitude")
                val courierLongitude = snapshot.getSafeDoubleNullable("courierLongitude")
                val userId = snapshot.getString("userId") ?: ""
                val riderId = snapshot.getString("riderId") ?: ""
                val riderBikeNumber = snapshot.getString("riderBikeNumber") ?: ""
                val otpCode = snapshot.getString("otpCode") ?: ""
                val isRated = snapshot.getSafeBoolean("isRated", false)
                val customerRating = snapshot.getSafeDouble("customerRating", 0.0)
                val tipAmount = snapshot.getSafeDouble("tipAmount", 0.0)
                val otpVerified = snapshot.getSafeBoolean("otpVerified", false)
                val additionalStops = snapshot.getString("additionalStops") ?: ""

                Parcel(
                    id = id,
                    itemName = itemName,
                    imageUrl = imageUrl,
                    status = status,
                    pickupAddress = pickupAddress,
                    deliveryAddress = deliveryAddress,
                    senderName = senderName,
                    senderPhone = senderPhone,
                    receiverName = receiverName,
                    receiverPhone = receiverPhone,
                    quantity = quantity,
                    weight = weight,
                    length = length,
                    width = width,
                    height = height,
                    price = price,
                    courierName = courierName,
                    courierPhone = courierPhone,
                    courierAvatar = courierAvatar,
                    progress = progress,
                    dateString = dateString,
                    userId = userId,
                    courierLatitude = courierLatitude,
                    courierLongitude = courierLongitude,
                    riderId = riderId,
                    riderBikeNumber = riderBikeNumber,
                    otpCode = otpCode,
                    otpVerified = otpVerified,
                    isRated = isRated,
                    customerRating = customerRating,
                    tipAmount = tipAmount,
                    additionalStops = additionalStops
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching parcel from firestore: ${e.message}")
            null
        }
    }

    /**
     * Push or update a wallet transaction in Firestore, associated with a user's account
     */
    fun syncTransactionToFirestore(transaction: Transaction, userId: String) {
        val db = firestore ?: return
        val txMap = hashMapOf(
            "id" to transaction.id,
            "title" to transaction.title,
            "date" to transaction.date,
            "amount" to transaction.amount,
            "isTopUp" to transaction.isTopUp,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users").document(userId)
            .collection("transactions").document(transaction.id)
            .set(txMap)
            .addOnSuccessListener {
                Log.d(TAG, "Transaction ${transaction.id} synced to user profile.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to sync transaction to Firestore: ${e.message}")
            }
    }

    /**
     * Set up a real-time listener for the user's transaction history in Firestore
     */
    fun listenToUserTransactions(userId: String): Flow<List<Transaction>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("users").document(userId)
            .collection("transactions")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to transactions: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = mutableListOf<Transaction>()
                    for (doc in snapshot.documents) {
                        try {
                            val id = doc.getString("id")?.takeIf { it.isNotBlank() } ?: doc.id
                            val title = doc.getString("title") ?: ""
                            val date = doc.getString("date") ?: ""
                            val amount = doc.getSafeDouble("amount", 0.0)
                            val isTopUp = doc.getSafeBoolean("isTopUp", true)
                            list.add(Transaction(id, title, date, amount, isTopUp))
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing transaction doc: ${e.message}")
                        }
                    }
                    trySend(list)
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Set up a real-time listener for the user's personal deliveries from Firestore
     */
    fun listenToUserDeliveries(userId: String): Flow<List<Parcel>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("deliveries")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to user deliveries: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = mutableListOf<Parcel>()
                    for (doc in snapshot.documents) {
                        try {
                            val id = doc.getString("id")?.takeIf { it.isNotBlank() } ?: doc.id
                            val itemName = doc.getString("itemName") ?: ""
                            val imageUrl = doc.getString("imageUrl") ?: ""
                            val statusStr = doc.getString("status") ?: ParcelStatus.TRANSIT.name
                            val status = try { ParcelStatus.valueOf(statusStr) } catch(e: Exception) { ParcelStatus.TRANSIT }
                            val pickupAddress = doc.getString("pickupAddress") ?: ""
                            val deliveryAddress = doc.getString("deliveryAddress") ?: ""
                            val senderName = doc.getString("senderName") ?: ""
                            val senderPhone = doc.getString("senderPhone") ?: ""
                            val receiverName = doc.getString("receiverName") ?: ""
                            val receiverPhone = doc.getString("receiverPhone") ?: ""
                            val quantity = doc.getSafeInt("quantity", 1)
                            val weight = doc.getSafeDouble("weight", 1.0)
                            val length = doc.getSafeInt("length", 10)
                            val width = doc.getSafeInt("width", 10)
                            val height = doc.getSafeInt("height", 10)
                            val price = doc.getSafeDouble("price", 0.0)
                            val courierName = doc.getString("courierName") ?: ""
                            val courierPhone = doc.getString("courierPhone") ?: ""
                            val courierAvatar = doc.getString("courierAvatar") ?: ""
                            val progress = doc.getSafeDouble("progress", 0.0).toFloat()
                            val dateString = doc.getString("dateString") ?: "Today"
                            val courierLatitude = doc.getSafeDoubleNullable("courierLatitude")
                            val courierLongitude = doc.getSafeDoubleNullable("courierLongitude")
                            val riderId = doc.getString("riderId") ?: ""
                            val riderBikeNumber = doc.getString("riderBikeNumber") ?: ""
                            val otpCode = doc.getString("otpCode") ?: ""
                            val isRated = doc.getSafeBoolean("isRated", false)
                            val customerRating = doc.getSafeDouble("customerRating", 0.0)
                            val tipAmount = doc.getSafeDouble("tipAmount", 0.0)
                            val additionalStops = doc.getString("additionalStops") ?: ""
                            val otpVerified = doc.getSafeBoolean("otpVerified", false)

                            val parcel = Parcel(
                                id = id,
                                itemName = itemName,
                                imageUrl = imageUrl,
                                status = status,
                                pickupAddress = pickupAddress,
                                deliveryAddress = deliveryAddress,
                                senderName = senderName,
                                senderPhone = senderPhone,
                                receiverName = receiverName,
                                receiverPhone = receiverPhone,
                                quantity = quantity,
                                weight = weight,
                                length = length,
                                width = width,
                                height = height,
                                price = price,
                                courierName = courierName,
                                courierPhone = courierPhone,
                                courierAvatar = courierAvatar,
                                progress = progress,
                                dateString = dateString,
                                userId = userId,
                                courierLatitude = courierLatitude,
                                courierLongitude = courierLongitude,
                                riderId = riderId,
                                riderBikeNumber = riderBikeNumber,
                                otpCode = otpCode,
                                otpVerified = otpVerified,
                                isRated = isRated,
                                customerRating = customerRating,
                                tipAmount = tipAmount,
                                additionalStops = additionalStops
                            )
                            list.add(parcel)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing delivery document: ${e.message}")
                        }
                    }
                    trySend(list)
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Set up a real-time listener for the user's notifications in Firestore
     */
    fun listenToUserNotifications(userId: String): Flow<List<NotificationItem>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("users").document(userId)
            .collection("notifications")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to user notifications: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = mutableListOf<NotificationItem>()
                    for (doc in snapshot.documents) {
                        try {
                            val id = doc.id
                            val title = doc.getString("title") ?: ""
                            val message = doc.getString("message") ?: doc.getString("description") ?: ""
                            val isRead = doc.getBoolean("isRead") ?: doc.getBoolean("read") ?: false
                            val parcelId = doc.getString("parcelId") ?: ""
                            val ts = when (val raw = doc.get("createdAt") ?: doc.get("timestamp")) {
                                is com.google.firebase.Timestamp -> raw.toDate().time
                                is Number -> raw.toLong()
                                else -> System.currentTimeMillis()
                            }
                            val timeStr = doc.getString("time") ?: formatTimestampToHumanDate(ts)
                            list.add(NotificationItem(id, title, message, timeStr, isRead, parcelId, ts))
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing notification: ${e.message}")
                        }
                    }
                    trySend(list)
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    fun formatTimestampToHumanDate(timestamp: Long): String {
        if (timestamp <= 0L) return "Just now"
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        if (diff in 0..59_999) return "Just now"
        val calNow = java.util.Calendar.getInstance().apply { timeInMillis = now }
        val calMsg = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
        val isToday = calNow.get(java.util.Calendar.YEAR) == calMsg.get(java.util.Calendar.YEAR) &&
                calNow.get(java.util.Calendar.DAY_OF_YEAR) == calMsg.get(java.util.Calendar.DAY_OF_YEAR)
        val timeFormat = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
        if (isToday) return "Today, ${timeFormat.format(java.util.Date(timestamp))}"
        calNow.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val isYesterday = calNow.get(java.util.Calendar.YEAR) == calMsg.get(java.util.Calendar.YEAR) &&
                calNow.get(java.util.Calendar.DAY_OF_YEAR) == calMsg.get(java.util.Calendar.DAY_OF_YEAR)
        if (isYesterday) return "Yesterday, ${timeFormat.format(java.util.Date(timestamp))}"
        calNow.timeInMillis = now
        val daysDiff = (now - timestamp) / (24 * 60 * 60 * 1000L)
        if (daysDiff < 7) {
            val weekFormat = java.text.SimpleDateFormat("EEE, h:mm a", java.util.Locale.getDefault())
            return weekFormat.format(java.util.Date(timestamp))
        }
        val dateFormat = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date(timestamp))
    }

    /**
     * Set up a real-time listener for the user's saved addresses in Firestore
     */
    fun listenToUserAddresses(userId: String): Flow<List<AddressItem>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("users").document(userId)
            .collection("addresses")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to user addresses: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = mutableListOf<AddressItem>()
                    for (doc in snapshot.documents) {
                        try {
                            val id = doc.getString("id") ?: doc.id
                            val label = doc.getString("label") ?: "Saved Address"
                            val address = doc.getString("address") ?: ""
                            val isDefault = doc.getBoolean("isDefault") ?: false
                            if (address.isNotBlank()) {
                                list.add(AddressItem(id, label, address, isDefault))
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing address: ${e.message}")
                        }
                    }
                    trySend(list)
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Save an address to Firestore under users/{userId}/addresses/{address.id}
     */
    fun saveUserAddress(userId: String, address: AddressItem) {
        val db = firestore ?: return
        val data = mapOf(
            "id" to address.id,
            "label" to address.label,
            "address" to address.address,
            "isDefault" to address.isDefault,
            "updatedAt" to com.google.firebase.Timestamp.now()
        )
        db.collection("users").document(userId)
            .collection("addresses").document(address.id)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Address ${address.id} synced to Firestore.")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to sync address to Firestore: ${e.message}")
            }
    }

    /**
     * Delete an address from Firestore
     */
    fun deleteUserAddress(userId: String, addressId: String) {
        val db = firestore ?: return
        db.collection("users").document(userId)
            .collection("addresses").document(addressId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Address $addressId deleted from Firestore.")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to delete address from Firestore: ${e.message}")
            }
    }

    /**
     * Set default address in Firestore: marks target as true, others as false
     */
    fun setDefaultUserAddress(userId: String, addressId: String) {
        val db = firestore ?: return
        db.collection("users").document(userId)
            .collection("addresses").get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                for (doc in snapshot.documents) {
                    val isTarget = (doc.id == addressId || doc.getString("id") == addressId)
                    batch.update(doc.reference, "isDefault", isTarget)
                }
                batch.commit().addOnFailureListener { e ->
                    Log.w(TAG, "Failed to commit default address batch: ${e.message}")
                }
            }
    }

    /**
     * Sync wallet balance directly to the user's Firestore document
     */
    fun syncWalletBalanceToFirestore(userId: String, balance: Double) {
        val db = firestore ?: return
        db.collection("users").document(userId)
            .update("walletBalance", balance)
            .addOnSuccessListener {
                Log.d(TAG, "Wallet balance synced to Firestore.")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Wallet balance update failed, trying merge-set: ${e.message}")
                db.collection("users").document(userId)
                    .set(hashMapOf("walletBalance" to balance), com.google.firebase.firestore.SetOptions.merge())
            }
    }

    /**
     * Sync loyalty points and delivery count directly to the user's Firestore document
     */
    fun syncLoyaltyToFirestore(userId: String, points: Int, deliveryCount: Int) {
        val db = firestore ?: return
        val map = hashMapOf(
            "loyaltyPoints" to points,
            "deliveryCount" to deliveryCount
        )
        db.collection("users").document(userId)
            .set(map, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Loyalty and delivery stats synced to Firestore.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to sync loyalty stats to Firestore: ${e.message}")
            }
    }

    /**
     * Set up a real-time listener for the user's profile and wallet balance in Firestore
     */
    fun listenToUserProfile(userId: String): Flow<Map<String, Any>?> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to user profile: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.data)
                } else {
                    trySend(null)
                }
            }
        awaitClose {
            listener.remove()
        }
    }

    /**
     * Retrieve the FCM Token and update user profile in Firestore
     */
    fun updateFcmTokenInFirestore(userId: String, token: String) {
        val db = firestore ?: return
        db.collection("users").document(userId)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d(TAG, "FCM token updated for user $userId in Firestore.")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "FCM token update failed, trying merge-set: ${e.message}")
                db.collection("users").document(userId)
                    .set(hashMapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
            }
    }

    /**
     * Listen to unassigned parcels with status "PENDING" in real time.
     */
    fun listenToAvailableDeliveries(): Flow<List<Parcel>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("deliveries")
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to available deliveries: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = mutableListOf<Parcel>()
                    for (doc in snapshot.documents) {
                        try {
                            val parcel = parseParcelFromDoc(doc)
                            val assigned = parcel.riderId.ifBlank { parcel.reservedRiderId }
                            if (assigned.isBlank() || assigned.equals("unassigned", ignoreCase = true)) {
                                list.add(parcel)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing available delivery: ${e.message}")
                        }
                    }
                    trySend(list)
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Listen to assigned parcels for a specific rider in real time (both active and reserved).
     */
    fun listenToRiderAssignments(riderId: String): Flow<List<Parcel>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val activeMap = java.util.concurrent.ConcurrentHashMap<String, Parcel>()
        val reservedMap = java.util.concurrent.ConcurrentHashMap<String, Parcel>()

        fun emitCombined() {
            val combined = (activeMap.values + reservedMap.values).distinctBy { it.id }
                .sortedByDescending { it.createdAt }
            trySend(combined)
        }

        val activeListener = db.collection("deliveries")
            .whereEqualTo("riderId", riderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to active rider assignments: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    activeMap.clear()
                    for (doc in snapshot.documents) {
                        try {
                            val p = parseParcelFromDoc(doc)
                            activeMap[p.id] = p
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing assigned delivery: ${e.message}")
                        }
                    }
                    emitCombined()
                }
            }

        val reservedListener = db.collection("deliveries")
            .whereEqualTo("reservedRiderId", riderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to reserved rider assignments: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    reservedMap.clear()
                    for (doc in snapshot.documents) {
                        try {
                            val p = parseParcelFromDoc(doc)
                            reservedMap[p.id] = p
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing reserved delivery: ${e.message}")
                        }
                    }
                    emitCombined()
                }
            }

        awaitClose {
            activeListener.remove()
            reservedListener.remove()
        }
    }

    /**
     * Accept a pending parcel and assign to rider in Firestore atomically with reservation checks.
     */
    fun acceptParcelByRider(parcelId: String, riderId: String, riderName: String, riderPhone: String, riderBikeNumber: String, requireOnline: Boolean = true, onComplete: (Boolean, String?) -> Unit) {
        val db = firestore
        if (db == null) {
            onComplete(false, "Firestore not available")
            return
        }

        val docRef = db.collection("deliveries").document(parcelId)
        db.runTransaction { transaction ->
            if (requireOnline) {
                val riderSnap = transaction.get(db.collection("users").document(riderId))
                val isOnline = riderSnap.getBoolean("isOnline") ?: false
                if (!isOnline) {
                    throw Exception("You are offline. Go on duty to accept new dispatches.")
                }
            }

            val snapshot = transaction.get(docRef)
            if (!snapshot.exists()) {
                throw Exception("Parcel not found.")
            }
            val currentStatus = snapshot.getString("status") ?: "PENDING"
            val existingRider = snapshot.getString("riderId") ?: ""

            // Strict atomic acceptance: must be unassigned and in open booking stage
            if (existingRider.isNotBlank() || currentStatus !in listOf("PENDING", "QUEUED", "OFFERED")) {
                throw Exception("Trip no longer available. Accepted by another courier.")
            }
            
            val now = System.currentTimeMillis()

            // Atomically lock and assign rider
            transaction.update(docRef, "status", "ASSIGNED")
            transaction.update(docRef, "riderId", riderId)
            transaction.update(docRef, "courierName", riderName)
            transaction.update(docRef, "courierPhone", riderPhone)
            transaction.update(docRef, "riderBikeNumber", riderBikeNumber)
            transaction.update(docRef, "progress", 0.15f)
            transaction.update(docRef, "acceptedAt", now)
            transaction.update(docRef, "lastUpdated", now)
            
            // Log to timeline subcollection
            val timelineRef = docRef.collection("timeline").document()
            transaction.set(timelineRef, mapOf(
                "eventId" to timelineRef.id,
                "event" to "MISSION_ACCEPTED",
                "fromStatus" to currentStatus,
                "toStatus" to "ASSIGNED",
                "actorId" to riderId,
                "actorRole" to "rider",
                "timestamp" to now,
                "note" to "Courier accepted mission"
            ))

            snapshot.getString("userId") ?: ""
        }.addOnSuccessListener { parcelUserId ->
            if (parcelUserId.isNotEmpty()) {
                val userDocRef = db.collection("users").document(parcelUserId).collection("deliveries").document(parcelId)
                userDocRef.update(
                    mapOf(
                        "status" to "ASSIGNED",
                        "riderId" to riderId,
                        "courierName" to riderName,
                        "courierPhone" to riderPhone,
                        "riderBikeNumber" to riderBikeNumber,
                        "progress" to 0.15f,
                        "lastUpdated" to System.currentTimeMillis()
                    )
                ).addOnFailureListener { e ->
                    Log.e(TAG, "Failed to update subcollection: ${e.message}")
                }
                sendNotificationToUser(
                    userId = parcelUserId,
                    title = "Courier Assigned",
                    message = "Your delivery #$parcelId has been accepted by courier $riderName ($riderBikeNumber).",
                    parcelId = parcelId
                )
            }
            if (riderId.isNotEmpty()) {
                sendNotificationToUser(
                    userId = riderId,
                    title = "Mission Confirmed",
                    message = "You have successfully accepted mission #$parcelId. Follow GPS to pickup.",
                    parcelId = parcelId
                )
            }
            onComplete(true, null)
        }.addOnFailureListener { e ->
            onComplete(false, e.message ?: "Trip no longer available.")
        }
    }

    /**
     * Legal State Machine Transition Validation
     */
    private fun isLegalTransition(from: ParcelStatus, to: ParcelStatus): Boolean {
        if (from == to) return true
        return when (from) {
            ParcelStatus.PENDING, ParcelStatus.QUEUED, ParcelStatus.OFFERED -> 
                to in listOf(ParcelStatus.OFFERED, ParcelStatus.ASSIGNED, ParcelStatus.CANCELLED)
            ParcelStatus.RESERVED_NEXT ->
                to in listOf(ParcelStatus.ASSIGNED, ParcelStatus.CANCELLED)
            ParcelStatus.ASSIGNED -> 
                to in listOf(ParcelStatus.ARRIVED_PICKUP, ParcelStatus.PICKED_UP, ParcelStatus.CANCELLED, ParcelStatus.EMERGENCY)
            ParcelStatus.ARRIVED_PICKUP ->
                to in listOf(ParcelStatus.PICKED_UP, ParcelStatus.CANCELLED, ParcelStatus.EMERGENCY)
            ParcelStatus.PICKED_UP -> 
                to in listOf(ParcelStatus.TRANSIT, ParcelStatus.OUT_FOR_DELIVERY, ParcelStatus.RETURN_TO_SENDER, ParcelStatus.EMERGENCY, ParcelStatus.DISPUTED)
            ParcelStatus.TRANSIT, ParcelStatus.OUT_FOR_DELIVERY -> 
                to in listOf(ParcelStatus.ARRIVED, ParcelStatus.RECIPIENT_UNAVAILABLE, ParcelStatus.RETURN_TO_SENDER, ParcelStatus.EMERGENCY, ParcelStatus.DISPUTED)
            ParcelStatus.ARRIVED -> 
                to in listOf(ParcelStatus.HANDOVER_VERIFIED, ParcelStatus.DELIVERED, ParcelStatus.RECIPIENT_UNAVAILABLE, ParcelStatus.FAILED_DELIVERY, ParcelStatus.RETURN_TO_SENDER, ParcelStatus.DISPUTED)
            ParcelStatus.HANDOVER_VERIFIED -> 
                to in listOf(ParcelStatus.DELIVERED, ParcelStatus.DISPUTED)
            ParcelStatus.RECIPIENT_UNAVAILABLE -> 
                to in listOf(ParcelStatus.RETURN_TO_SENDER, ParcelStatus.ARRIVED, ParcelStatus.FAILED_DELIVERY, ParcelStatus.DISPUTED)
            ParcelStatus.RETURN_TO_SENDER -> 
                to in listOf(ParcelStatus.RETURNED, ParcelStatus.DISPUTED)
            ParcelStatus.FAILED_DELIVERY -> 
                to in listOf(ParcelStatus.RETURN_TO_SENDER, ParcelStatus.DISPUTED)
            ParcelStatus.EMERGENCY -> 
                to in listOf(ParcelStatus.ASSIGNED, ParcelStatus.RETURN_TO_SENDER, ParcelStatus.CANCELLED, ParcelStatus.DISPUTED)
            ParcelStatus.DELIVERED, ParcelStatus.CANCELLED, ParcelStatus.RETURNED -> 
                to == ParcelStatus.DISPUTED
            ParcelStatus.DISPUTED -> 
                to in listOf(ParcelStatus.DELIVERED, ParcelStatus.RETURNED, ParcelStatus.CANCELLED)
        }
    }

    /**
     * Update parcel status (Enforced by strict state machine & chain of custody timeline)
     */
    fun updateParcelStatusByRider(parcelId: String, nextStatus: ParcelStatus, progress: Float, onComplete: (Boolean, String?) -> Unit) {
        val db = firestore
        if (db == null) {
            onComplete(false, "Firestore not available")
            return
        }

        val docRef = db.collection("deliveries").document(parcelId)
        docRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val currentStatusStr = snapshot.getString("status") ?: "PENDING"
                val currentStatus = try {
                    ParcelStatus.valueOf(currentStatusStr)
                } catch (e: Exception) {
                    ParcelStatus.PENDING
                }

                if (!isLegalTransition(currentStatus, nextStatus)) {
                    val errMsg = "Illegal state transition from ${currentStatus.name} to ${nextStatus.name}"
                    Log.e(TAG, errMsg)
                    onComplete(false, errMsg)
                    return@addOnSuccessListener
                }

                val parcelUserId = snapshot.getString("userId") ?: ""
                val riderId = snapshot.getString("riderId") ?: ""
                val now = System.currentTimeMillis()
                
                db.runTransaction { transaction ->
                    transaction.update(docRef, "status", nextStatus.name)
                    transaction.update(docRef, "progress", progress)
                    transaction.update(docRef, "lastUpdated", now)

                    // Append immutable chain-of-custody event
                    val timelineRef = docRef.collection("timeline").document()
                    transaction.set(timelineRef, mapOf(
                        "eventId" to timelineRef.id,
                        "event" to "STATUS_CHANGE",
                        "fromStatus" to currentStatus.name,
                        "toStatus" to nextStatus.name,
                        "actorId" to riderId,
                        "actorRole" to "rider",
                        "timestamp" to now
                    ))
                }.addOnSuccessListener {
                    // Update user personal delivery subcollection
                    if (parcelUserId.isNotEmpty()) {
                        val userDocRef = db.collection("users").document(parcelUserId).collection("deliveries").document(parcelId)
                        userDocRef.update(
                            mapOf(
                                "status" to nextStatus.name,
                                "progress" to progress,
                                "lastUpdated" to now
                            )
                        )
                        val statusTitle = when(nextStatus) {
                            ParcelStatus.TRANSIT, ParcelStatus.OUT_FOR_DELIVERY -> "Parcel Out for Delivery"
                            ParcelStatus.DELIVERED -> "Parcel Delivered Successfully"
                            ParcelStatus.ASSIGNED -> "Parcel Assigned"
                            ParcelStatus.PICKED_UP -> "Parcel Picked Up"
                            ParcelStatus.ARRIVED -> "Rider Arrived at Destination"
                            ParcelStatus.RECIPIENT_UNAVAILABLE -> "Delivery Notice: Recipient Unavailable"
                            ParcelStatus.RETURN_TO_SENDER -> "Delivery Returning to Sender"
                            ParcelStatus.RETURNED -> "Parcel Returned to Sender"
                            else -> "Parcel Status Update"
                        }
                        sendNotificationToUser(
                            userId = parcelUserId,
                            title = statusTitle,
                            message = "Your parcel #$parcelId status is now ${nextStatus.name.replace('_', ' ')}.",
                            parcelId = parcelId
                        )
                    }
                    onComplete(true, null)
                }.addOnFailureListener { e ->
                    onComplete(false, e.message ?: "Failed to update status.")
                }
            } else {
                onComplete(false, "Parcel not found.")
            }
        }.addOnFailureListener { e ->
            onComplete(false, e.message ?: "Failed to fetch parcel.")
        }
    }

    /**
     * Report an operational field exception (e.g. Recipient Unreachable, Sender Delay, Inaccessible Address, Breakdown)
     */
    fun reportParcelException(parcelId: String, exceptionType: String, reason: String, onComplete: (Boolean, String?) -> Unit) {
        val db = firestore
        if (db == null) {
            onComplete(false, "Firestore not available")
            return
        }
        val docRef = db.collection("deliveries").document(parcelId)
        val now = System.currentTimeMillis()
        val updates = mapOf(
            "exceptionType" to exceptionType,
            "exceptionReason" to reason,
            "exceptionTimestamp" to now,
            "lastUpdated" to now
        )
        docRef.update(updates).addOnSuccessListener {
            // Also log to incident_reports collection for dispatcher audit
            val incidentMap = mapOf(
                "parcelId" to parcelId,
                "type" to "FIELD_EXCEPTION",
                "exceptionType" to exceptionType,
                "reason" to reason,
                "timestamp" to now
            )
            db.collection("incident_reports").add(incidentMap)
            onComplete(true, null)
        }.addOnFailureListener { e ->
            onComplete(false, e.message ?: "Failed to log exception")
        }
    }

    /**
     * Resolve and clear an operational field exception once field condition normalizes
     */
    fun resolveParcelException(parcelId: String, onComplete: (Boolean, String?) -> Unit) {
        val db = firestore
        if (db == null) {
            onComplete(false, "Firestore not available")
            return
        }
        val docRef = db.collection("deliveries").document(parcelId)
        val updates = mapOf(
            "exceptionType" to "",
            "exceptionReason" to "",
            "lastUpdated" to System.currentTimeMillis()
        )
        docRef.update(updates).addOnSuccessListener {
            onComplete(true, null)
        }.addOnFailureListener { e ->
            onComplete(false, e.message ?: "Failed to clear exception")
        }
    }

    /**
     * Persist an admin/dispatcher driver (re)assignment for one or more parcel docs
     */
    fun updateParcelAssignment(parcelId: String, riderId: String, riderBikeNumber: String, onComplete: (Boolean, String?) -> Unit) {
        val db = firestore
        if (db == null) {
            onComplete(false, "Firestore not available")
            return
        }
        val docRef = db.collection("deliveries").document(parcelId)
        docRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                db.runTransaction { transaction ->
                    transaction.update(docRef, "riderId", riderId)
                    transaction.update(docRef, "riderBikeNumber", riderBikeNumber)
                    transaction.update(docRef, "lastUpdated", System.currentTimeMillis())
                }.addOnSuccessListener {
                    val parcelUserId = snapshot.getString("userId") ?: ""
                    if (parcelUserId.isNotEmpty()) {
                        val userDocRef = db.collection("users").document(parcelUserId).collection("deliveries").document(parcelId)
                        userDocRef.update(
                            mapOf(
                                "riderId" to riderId,
                                "riderBikeNumber" to riderBikeNumber,
                                "lastUpdated" to System.currentTimeMillis()
                            )
                        )
                    }
                    onComplete(true, null)
                }.addOnFailureListener { e ->
                    onComplete(false, e.message ?: "Failed to update assignment.")
                }
            } else {
                onComplete(false, "Parcel not found.")
            }
        }.addOnFailureListener { e ->
            onComplete(false, e.message ?: "Failed to fetch parcel.")
        }
    }

    /**
     * Update real-time GPS courier coordinates during transit/delivery simulation
     */
    fun updateCourierLocationByRider(parcelId: String, lat: Double, lng: Double, onComplete: (Boolean, String?) -> Unit) {
        val db = firestore
        if (db == null) {
            onComplete(false, "Firestore not available")
            return
        }

        val docRef = db.collection("deliveries").document(parcelId)
        docRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val parcelUserId = snapshot.getString("userId") ?: ""
                
                db.runTransaction { transaction ->
                    transaction.update(docRef, "courierLatitude", lat)
                    transaction.update(docRef, "courierLongitude", lng)
                }.addOnSuccessListener {
                    if (parcelUserId.isNotEmpty()) {
                        val userDocRef = db.collection("users").document(parcelUserId).collection("deliveries").document(parcelId)
                        userDocRef.update(
                            mapOf(
                                "courierLatitude" to lat,
                                "courierLongitude" to lng
                            )
                        )
                    }
                    onComplete(true, null)
                }.addOnFailureListener { e ->
                    onComplete(false, e.message ?: "Failed to update GPS coordinates.")
                }
            } else {
                onComplete(false, "Parcel not found.")
            }
        }.addOnFailureListener { e ->
            onComplete(false, e.message ?: "Failed to fetch parcel.")
        }
    }

    /**
     * Verify OTP and complete delivery, updating status to DELIVERED
     */
    fun verifyDeliveryOtpByRider(parcelId: String, otpInput: String, onComplete: (Boolean, String?) -> Unit) {
        val db = firestore
        if (db == null) {
            onComplete(false, "Firestore not available")
            return
        }

        val docRef = db.collection("deliveries").document(parcelId)
        docRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val realOtp = snapshot.getString("otpCode") ?: ""
                val otpExpiresAt = snapshot.getLong("otpExpiresAt") ?: (System.currentTimeMillis() + 60 * 60 * 1000L)
                val otpAttempts = snapshot.getLong("otpAttempts") ?: 0L
                val isLocked = otpAttempts >= 5
                val isExpired = System.currentTimeMillis() > otpExpiresAt
                val isValid = !isLocked && !isExpired && realOtp.isNotEmpty() && realOtp == otpInput
                
                if (isValid) {
                    val parcelUserId = snapshot.getString("userId") ?: ""
                    val price = snapshot.getDouble("price") ?: 0.0
                    val riderId = snapshot.getString("riderId")?.takeIf { it.isNotBlank() }
                        ?: snapshot.getString("driverId") ?: ""
                    val alreadyPaid = snapshot.getBoolean("payoutCredited") ?: false
                    val payoutAmount = price * 0.80

                    db.runTransaction { transaction ->
                        transaction.update(docRef, "status", "HANDOVER_VERIFIED")
                        transaction.update(docRef, "progress", 0.95f)
                        transaction.update(docRef, "otpVerified", true)
                        transaction.update(docRef, "otpAttempts", 0)
                        transaction.update(docRef, "otpVerifiedAt", System.currentTimeMillis())
                        transaction.update(docRef, "lastUpdated", System.currentTimeMillis())
                    }.addOnSuccessListener {
                        // Update subcollection to HANDOVER_VERIFIED
                        if (parcelUserId.isNotEmpty()) {
                            val userDocRef = db.collection("users").document(parcelUserId).collection("deliveries").document(parcelId)
                            userDocRef.update(
                                mapOf(
                                    "status" to "HANDOVER_VERIFIED",
                                    "progress" to 0.95f,
                                    "otpVerified" to true,
                                    "lastUpdated" to System.currentTimeMillis()
                                )
                            )
                        }

                        onComplete(true, null)
                    }.addOnFailureListener { e ->
                        onComplete(false, e.message ?: "Failed to verify OTP.")
                    }
                } else {
                    // Record the failed attempt for lockout tracking
                    val attemptsMessage = when {
                        isLocked -> "Too many incorrect attempts. This delivery is locked — contact the dispatcher."
                        isExpired -> "This security code has expired. Contact the dispatcher to reissue."
                        else -> "Invalid 4-digit security code. Please check with customer."
                    }
                    if (!isLocked) {
                        db.collection("deliveries").document(parcelId)
                            .update("otpAttempts", com.google.firebase.firestore.FieldValue.increment(1))
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Failed to record OTP attempt: ${e.message}")
                            }
                    }
                    onComplete(false, attemptsMessage)
                }
            } else {
                onComplete(false, "Parcel not found.")
            }
        }.addOnFailureListener { e ->
            onComplete(false, e.message ?: "Failed to fetch parcel.")
        }
    }

    /**
     * Secure server-side validation and processing for delivery completion,
     * updating status to DELIVERED, awarding loyalty points (+15) in dedicated 'customer_loyalty_points' collection,
     * and securely crediting driver tips/earnings.
     */
    fun processDeliveryComplete(parcelId: String, userId: String, riderId: String, onComplete: (Boolean, String?) -> Unit) {
        val db = firestore
        if (db == null) {
            onComplete(false, "Firestore not available")
            return
        }

        val parcelRef = db.collection("deliveries").document(parcelId)
        val userRef = db.collection("users").document(userId)
        val loyaltyRecordRef = db.collection("customer_loyalty_points").document(parcelId)

        db.runTransaction { transaction ->
            val parcelSnap = transaction.get(parcelRef)
            if (!parcelSnap.exists()) {
                throw Exception("Parcel not found or already deleted.")
            }
            val currentStatus = parcelSnap.getString("status") ?: ""
            if (currentStatus == "DELIVERED") {
                return@runTransaction
            }

            transaction.update(parcelRef, mapOf(
                "status" to "DELIVERED",
                "progress" to 1.0f,
                "completedTimestamp" to System.currentTimeMillis()
            ))

            val loyaltySnap = transaction.get(loyaltyRecordRef)
            if (!loyaltySnap.exists()) {
                val loyaltyData = hashMapOf(
                    "parcelId" to parcelId,
                    "userId" to userId,
                    "pointsAwarded" to 15,
                    "awardedAt" to System.currentTimeMillis(),
                    "verified" to true
                )
                transaction.set(loyaltyRecordRef, loyaltyData)

                val userSnap = transaction.get(userRef)
                val currentPoints = if (userSnap.exists()) (userSnap.getLong("loyaltyPoints") ?: 350L).toInt() else 350
                val currentDeliveries = if (userSnap.exists()) (userSnap.getLong("deliveryCount") ?: 1L).toInt() else 1
                
                transaction.set(userRef, mapOf(
                    "loyaltyPoints" to (currentPoints + 15),
                    "deliveryCount" to (currentDeliveries + 1)
                ), com.google.firebase.firestore.SetOptions.merge())
            }
        }.addOnSuccessListener {
            if (userId.isNotEmpty()) {
                db.collection("users").document(userId).collection("deliveries").document(parcelId)
                    .update(mapOf("status" to "DELIVERED", "progress" to 1.0f))
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to update user delivery subcollection: ${e.message}")
                    }
            }
            onComplete(true, null)
        }.addOnFailureListener { e ->
            onComplete(false, e.message ?: "Failed to process delivery completion securely.")
        }
    }

    /**
     * Submit rating and tip for a courier, updating both the rider and customer profiles securely.
     */
    fun rateAndTipRider(
        parcelId: String,
        riderId: String,
        rating: Double,
        tipAmount: Double,
        customerId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val db = firestore
        if (db == null) {
            onComplete(false, "Firestore is not available")
            return
        }

        val parcelDoc = db.collection("deliveries").document(parcelId)
        db.runTransaction { transaction ->
            // --- ALL READS FIRST ---
            val parcelSnap = transaction.get(parcelDoc)
            if (!parcelSnap.exists()) {
                throw Exception("Parcel not found")
            }
            val effectiveRiderId = riderId.ifBlank {
                parcelSnap.getString("riderId") ?: parcelSnap.getString("driverId") ?: ""
            }

            // Customer snapshot read
            var customerRef: com.google.firebase.firestore.DocumentReference? = null
            var custBal = 0.0
            if (customerId.isNotEmpty() && tipAmount > 0.0) {
                val ref = db.collection("users").document(customerId)
                val customerSnap = transaction.get(ref)
                if (customerSnap.exists()) {
                    custBal = (customerSnap.get("walletBalance") as? Number)?.toDouble() ?: 0.0
                    if (custBal < tipAmount) {
                        throw Exception("Insufficient wallet balance (₦${custBal.toInt()}) for ₦${tipAmount.toInt()} tip")
                    }
                    customerRef = ref
                }
            }

            // Rider snapshot read
            var riderRef: com.google.firebase.firestore.DocumentReference? = null
            var currentRiderBal = 0.0
            var currentTips = 0.0
            var oldRating = 4.8
            var ratingCount = 1
            if (effectiveRiderId.isNotEmpty()) {
                val ref = db.collection("users").document(effectiveRiderId)
                val riderSnap = transaction.get(ref)
                if (riderSnap.exists()) {
                    currentRiderBal = (riderSnap.get("walletBalance") as? Number)?.toDouble() ?: 0.0
                    currentTips = (riderSnap.get("tipsEarned") as? Number)?.toDouble()
                        ?: (riderSnap.get("totalTips") as? Number)?.toDouble() ?: 0.0
                    oldRating = riderSnap.getDouble("rating") ?: 4.8
                    val deliveryCount = riderSnap.getLong("deliveryCount")?.toInt() ?: 1
                    ratingCount = deliveryCount.coerceAtLeast(1)
                    riderRef = ref
                }
            }

            // --- ALL WRITES AFTER READS ---
            transaction.update(parcelDoc, mapOf(
                "isRated" to true,
                "customerRating" to rating,
                "tipAmount" to tipAmount,
                "updatedAt" to com.google.firebase.Timestamp.now()
            ))

            if (customerRef != null) {
                transaction.update(customerRef, "walletBalance", custBal - tipAmount)
            }

            if (riderRef != null) {
                val riderUpdates = mutableMapOf<String, Any>()
                if (tipAmount > 0.0) {
                    riderUpdates["walletBalance"] = currentRiderBal + tipAmount
                    riderUpdates["tipsEarned"] = currentTips + tipAmount
                    riderUpdates["totalTips"] = currentTips + tipAmount
                }
                val newRating = ((oldRating * (ratingCount - 1)) + rating) / ratingCount
                riderUpdates["rating"] = newRating
                riderUpdates["ratingCount"] = ratingCount
                riderUpdates["updatedAt"] = com.google.firebase.Timestamp.now()
                transaction.update(riderRef, riderUpdates)
            }
        }.addOnSuccessListener {
            val parcelUserId = customerId
            if (parcelUserId.isNotEmpty()) {
                val userDocRef = db.collection("users").document(parcelUserId).collection("deliveries").document(parcelId)
                userDocRef.set(mapOf(
                    "isRated" to true,
                    "customerRating" to rating,
                    "tipAmount" to tipAmount,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                ), com.google.firebase.firestore.SetOptions.merge())

                if (tipAmount > 0.0) {
                    val txRef = "ESD-TIP-OUT-${System.currentTimeMillis()}"
                    val txMap = hashMapOf(
                        "id" to txRef,
                        "title" to "Tip to Courier",
                        "date" to "Today",
                        "amount" to tipAmount,
                        "isTopUp" to false,
                        "timestamp" to System.currentTimeMillis()
                    )
                    db.collection("users").document(parcelUserId).collection("transactions").document(txRef).set(txMap)
                }
            }

            val effectiveRiderId = riderId.ifBlank { "" }
            if (effectiveRiderId.isNotEmpty() && tipAmount > 0.0) {
                val txRef = "ESD-TIP-IN-${System.currentTimeMillis()}"
                val txMap = hashMapOf(
                    "id" to txRef,
                    "title" to "Tip from Customer",
                    "date" to "Today",
                    "amount" to tipAmount,
                    "isTopUp" to true,
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("users").document(effectiveRiderId).collection("transactions").document(txRef).set(txMap)
            }

            onComplete(true, null)
        }.addOnFailureListener { e ->
            onComplete(false, e.message ?: "Failed to submit rating & tip.")
        }
    }

    /**
     * Real-time listener for all registered riders in Firestore
     */
    fun listenToAllRiders(): Flow<List<Rider>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db.collection("users")
            .whereEqualTo("role", "rider")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to riders: ${error.message}")
                    return@addSnapshotListener
                }
                val list = mutableListOf<Rider>()
                if (snapshot != null) {
                    for (doc in snapshot.documents) {
                        try {
                            val uid = doc.id
                            val name = doc.getString("name") ?: "Rider"
                            val phone = doc.getString("phone") ?: ""
                            val bikeNumber = doc.getString("bikeNumber") ?: doc.getString("bike_number") ?: ""
                            val isOnline = doc.getSafeBoolean("isOnline", false)
                            val statusStr = doc.getString("status") ?: "offline"
                            
                            val riderStatus = if (isOnline) {
                                if (statusStr == "busy") RiderStatus.BUSY else RiderStatus.ONLINE
                            } else {
                                RiderStatus.OFFLINE
                            }
                            
                            val lat = doc.getSafeDoubleNullable("latitude")
                            val lng = doc.getSafeDoubleNullable("longitude")
                            val workload = doc.getSafeInt("currentWorkload", doc.getSafeInt("activeDeliveriesCount", 0))
                            val battery = doc.getSafeInt("batteryLevel", 90)
                            val rating = doc.getSafeDouble("rating", 4.8)
                            val avgTime = doc.getSafeInt("averageDeliveryTimeMin", 20)
                            
                            val photoUrl = doc.getString("photoUrl") ?: doc.getString("avatar") ?: doc.getString("avatarBase64") ?: ""
                            val riderAvatar = if (photoUrl.isNotBlank()) photoUrl else "https://images.unsplash.com/photo-1599566150163-29194dcaad36?w=100&h=100&fit=crop"
                            
                            list.add(
                                Rider(
                                    id = uid,
                                    name = name,
                                    phone = phone,
                                    avatar = riderAvatar,
                                    vehicleType = if (bikeNumber.isNotEmpty()) "Bike" else "Vehicle",
                                    status = riderStatus,
                                    latitude = lat,
                                    longitude = lng,
                                    currentWorkload = workload,
                                    batteryLevel = battery,
                                    rating = rating,
                                    averageDeliveryTimeMin = avgTime,
                                    cancellationHistoryCount = 0,
                                    fuelEfficiency = 42.0,
                                    shiftSchedule = "08:00 - 18:00",
                                    distanceToPickupKm = 1.0,
                                    activeDeliveriesCount = workload
                                )
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing rider document: ${e.message}")
                        }
                    }
                }
                trySend(list)
            }
        awaitClose {
            listener.remove()
        }
    }

    /**
     * Send a real-time chat message for a specific delivery parcel.
     */
    fun sendParcelChatMessage(
        parcelId: String,
        senderId: String,
        senderName: String,
        senderRole: String,
        messageText: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val db = firestore
        if (db == null) {
            onComplete(false, "Firestore is not available")
            return
        }

        val msgId = java.util.UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val msgMap = hashMapOf(
            "id" to msgId,
            "senderId" to senderId,
            "senderName" to senderName,
            "senderRole" to senderRole,
            "messageText" to messageText,
            "timestamp" to timestamp
        )

        db.collection("deliveries")
            .document(parcelId)
            .collection("chats")
            .document(msgId)
            .set(msgMap)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message ?: "Failed to send chat message.")
            }
    }

    /**
     * Listen in real-time to chat messages for a specific parcel delivery.
     */
    fun listenToParcelChatMessages(parcelId: String): Flow<List<ParcelChatMessage>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }

        val query = db.collection("deliveries")
            .document(parcelId)
            .collection("chats")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to parcel chats: ${error.message}")
                return@addSnapshotListener
            }

            val list = mutableListOf<ParcelChatMessage>()
            if (snapshot != null) {
                for (doc in snapshot.documents) {
                    try {
                        val id = doc.getString("id") ?: ""
                        val senderId = doc.getString("senderId") ?: ""
                        val senderName = doc.getString("senderName") ?: ""
                        val senderRole = doc.getString("senderRole") ?: ""
                        val text = doc.getString("messageText") ?: ""
                        val timestamp = doc.getLong("timestamp") ?: 0L

                        list.add(
                            ParcelChatMessage(
                                id = id,
                                senderId = senderId,
                                senderName = senderName,
                                senderRole = senderRole,
                                messageText = text,
                                timestamp = timestamp
                            )
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing chat message doc: ${e.message}")
                    }
                }
            }
            trySend(list)
        }

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Submit driver rating and feedback by customer, stored in driver profile and ratings subcollection.
     */
    fun submitDriverRating(
        riderId: String,
        parcelId: String,
        customerId: String,
        rating: Float,
        feedback: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val db = firestore
        if (db == null || riderId.isEmpty()) {
            onComplete(false, "Firestore or Rider ID unavailable")
            return
        }

        val ratingId = java.util.UUID.randomUUID().toString()
        val ratingMap = hashMapOf(
            "id" to ratingId,
            "parcelId" to parcelId,
            "customerId" to customerId,
            "rating" to rating,
            "feedback" to feedback,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users").document(riderId)
            .collection("ratings").document(ratingId)
            .set(ratingMap)
            .addOnSuccessListener {
                // Update average rating on rider document
                db.collection("users").document(riderId).get()
                    .addOnSuccessListener { doc ->
                        val currentRating = doc.getSafeDouble("rating", 4.8)
                        val count = doc.getSafeDouble("ratingCount", 0.0)
                        val newRating = ((currentRating * count) + rating) / (count + 1.0)
                        
                        db.collection("users").document(riderId).update(
                            mapOf(
                                "rating" to newRating,
                                "ratingCount" to (count + 1.0)
                            )
                        )
                    }
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message ?: "Failed to submit rating")
            }
    }

    /**
     * Real-time listener for a specific rider's live coordinates
     */
    fun listenToRiderLocation(riderId: String): Flow<Pair<Double, Double>?> = callbackFlow {
        val db = firestore
        if (db == null || riderId.isEmpty()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val registration = db.collection("fleet_locations").document(riderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val lat = snapshot.getSafeDoubleNullable("lat") ?: snapshot.getSafeDoubleNullable("latitude")
                val lng = snapshot.getSafeDoubleNullable("lng") ?: snapshot.getSafeDoubleNullable("longitude")
                if (lat != null && lng != null) {
                    trySend(Pair(lat, lng))
                }
            }
        awaitClose { registration.remove() }
    }

    /**
     * Send a real-time live support ticket chat message between customer and dispatch center.
     */
    fun sendSupportChatMessage(
        ticketId: String,
        senderId: String,
        senderName: String,
        senderRole: String,
        messageText: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val db = firestore
        if (db == null) {
            onComplete(false, "Firestore is not available")
            return
        }

        val msgId = java.util.UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val msgMap = hashMapOf(
            "id" to msgId,
            "senderId" to senderId,
            "senderName" to senderName,
            "senderRole" to senderRole,
            "messageText" to messageText,
            "timestamp" to timestamp
        )

        val chatDocRef = db.collection("support_chats").document(ticketId)
        chatDocRef.set(
            hashMapOf(
                "ticketId" to ticketId,
                "userId" to senderId,
                "userName" to senderName,
                "lastMessage" to messageText,
                "lastUpdated" to timestamp,
                "status" to "OPEN"
            ),
            com.google.firebase.firestore.SetOptions.merge()
        )

        chatDocRef.collection("messages")
            .document(msgId)
            .set(msgMap)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message ?: "Failed to send support message.") }
    }

    /**
     * Listen in real-time to support ticket chat messages for a user.
     */
    fun listenToSupportChatMessages(ticketId: String): Flow<List<SupportChatMessage>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }

        val query = db.collection("support_chats")
            .document(ticketId)
            .collection("messages")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to support chats: ${error.message}")
                return@addSnapshotListener
            }

            val list = mutableListOf<SupportChatMessage>()
            if (snapshot != null) {
                for (doc in snapshot.documents) {
                    try {
                        val id = doc.getString("id") ?: doc.id
                        val senderId = doc.getString("senderId") ?: ""
                        val senderName = doc.getString("senderName") ?: ""
                        val senderRole = doc.getString("senderRole") ?: "customer"
                        val text = doc.getString("messageText") ?: ""
                        val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()

                        list.add(
                            SupportChatMessage(
                                id = id,
                                senderId = senderId,
                                senderName = senderName,
                                senderRole = senderRole,
                                messageText = text,
                                timestamp = timestamp
                            )
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing support message: ${e.message}")
                    }
                }
            }
            trySend(list)
        }

        awaitClose { listener.remove() }
    }

    fun saveVerificationOtp(userId: String, code: String, onComplete: (Boolean, String?) -> Unit) {
        val db = firestore ?: run { onComplete(false, "Firestore unavailable"); return }
        val expiresAt = System.currentTimeMillis() + (10 * 60 * 1000L) // 10 minutes
        val data = hashMapOf(
            "code" to code,
            "expiresAt" to expiresAt,
            "attempts" to 0,
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("users").document(userId).collection("verification_otp").document("current")
            .set(data)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    fun verifyOtpCode(userId: String, enteredCode: String, onComplete: (Boolean, String) -> Unit) {
        val db = firestore ?: run { onComplete(false, "Firestore unavailable"); return }
        val docRef = db.collection("users").document(userId).collection("verification_otp").document("current")
        docRef.get().addOnSuccessListener { snap ->
            if (!snap.exists()) {
                onComplete(false, "No active OTP request found. Please request a new verification code.")
                return@addOnSuccessListener
            }
            val storedCode = snap.getString("code") ?: ""
            val expiresAt = snap.getLong("expiresAt") ?: 0L
            val attempts = snap.getLong("attempts") ?: 0L

            if (System.currentTimeMillis() > expiresAt) {
                onComplete(false, "Verification code has expired. Please request a new code.")
                return@addOnSuccessListener
            }

            if (attempts >= 5) {
                onComplete(false, "Too many failed attempts. Please request a new code.")
                return@addOnSuccessListener
            }

            if (enteredCode.trim() == storedCode.trim()) {
                db.collection("users").document(userId).set(
                    mapOf(
                        "isVerified" to true,
                        "verifiedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                ).addOnSuccessListener {
                    docRef.delete()
                    onComplete(true, "Verification successful! You are now a verified VIP member.")
                }.addOnFailureListener { e ->
                    onComplete(false, e.message ?: "Failed to update verification status.")
                }
            } else {
                docRef.update("attempts", com.google.firebase.firestore.FieldValue.increment(1))
                onComplete(false, "Invalid verification code. Please check and try again.")
            }
        }.addOnFailureListener { e ->
            onComplete(false, e.message ?: "Failed to verify code.")
        }
    }
}

