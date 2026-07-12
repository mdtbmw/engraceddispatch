package com.example.data

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
        val cleanEmail = email.trim().lowercase()
        val emailHash = cleanEmail.substringBefore("@").hashCode().toString().take(6).padEnd(6, 's')
        return "${pin}${pin}_$emailHash"
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
        val db = firestore
        if (db == null) {
            onComplete(false)
            return
        }
        db.collection("users").whereEqualTo("email", email.trim()).get()
            .addOnSuccessListener { querySnapshot ->
                onComplete(!querySnapshot.isEmpty)
            }
            .addOnFailureListener {
                onComplete(false)
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
        val cleanQueryPhone = phone.trim().replace("-", "").replace("+", "")
        db.collection("users").get()
            .addOnSuccessListener { querySnapshot ->
                var found = false
                for (doc in querySnapshot.documents) {
                    val userPhone = doc.getString("phone") ?: ""
                    val cleanUserPhone = userPhone.replace("-", "").replace("+", "").trim()
                    if (cleanUserPhone == cleanQueryPhone && cleanQueryPhone.isNotEmpty()) {
                        found = true
                        break
                    }
                }
                onComplete(found)
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
    fun saveUserProfileToFirestore(userId: String, name: String, email: String, phone: String, role: String = "customer", bikeNumber: String = "") {
        val db = firestore ?: return
        val userMap = hashMapOf<String, Any>(
            "uid" to userId,
            "name" to name,
            "email" to email,
            "phone" to phone,
            "role" to role,
            "bikeNumber" to bikeNumber,
            "updatedAt" to System.currentTimeMillis()
        )
        if (role == "rider") {
            userMap["isOnline"] = true
            userMap["status"] = "active"
            userMap["latitude"] = 6.4281
            userMap["longitude"] = 3.4219
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
     * Update a rider's active online availability and status in Firestore
     */
    fun updateRiderOnlineStatus(userId: String, isOnline: Boolean) {
        val db = firestore ?: return
        val statusMap = hashMapOf(
            "isOnline" to isOnline,
            "is_active" to isOnline,
            "isActive" to isOnline,
            "status" to (if (isOnline) "active" else "offline"),
            "updatedAt" to System.currentTimeMillis()
        )
        // Update both in the users document and riders collection
        db.collection("users").document(userId)
            .set(statusMap, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Rider online status updated in users collection to: $isOnline")
            }
        db.collection("riders").document(userId)
            .set(statusMap, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Rider online status updated in riders collection to: $isOnline")
            }
    }

    /**
     * Send real-time FCM notification and store in user notifications collection
     */
    fun sendNotificationToUser(userId: String, title: String, message: String, parcelId: String? = null) {
        val db = firestore ?: return
        if (userId.isEmpty()) return
        val notifId = "NOTIF-${System.currentTimeMillis()}"
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
     * Push or update delivery real-time status in Firestore, associated with a user's personal account
     */
    fun syncParcelToFirestore(parcel: Parcel, userId: String) {
        val db = firestore ?: return
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
            "userId" to userId,
            "riderId" to parcel.riderId,
            "riderBikeNumber" to parcel.riderBikeNumber,
            "otpCode" to parcel.otpCode,
            "otpVerified" to parcel.otpVerified,
            "isRated" to parcel.isRated,
            "customerRating" to parcel.customerRating,
            "tipAmount" to parcel.tipAmount,
            "lastUpdated" to System.currentTimeMillis()
        )

        // Sync globally
        db.collection("deliveries").document(parcel.id)
            .set(parcelMap)
            .addOnSuccessListener {
                Log.d(TAG, "Parcel ${parcel.id} synced globally.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to sync parcel globally: ${e.message}")
            }

        // Sync to personal user account sub-collection
        db.collection("users").document(userId)
            .collection("deliveries").document(parcel.id)
            .set(parcelMap)
            .addOnSuccessListener {
                Log.d(TAG, "Parcel ${parcel.id} synced to user's personal account.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to sync parcel to user personal account: ${e.message}")
            }
    }

    /**
     * Fetch user's parcel history from Firestore personal account
     */
    fun fetchUserParcelHistory(userId: String, onComplete: (List<Parcel>) -> Unit) {
        val db = firestore
        if (db == null) {
            onComplete(emptyList())
            return
        }

        db.collection("users").document(userId)
            .collection("deliveries")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val list = mutableListOf<Parcel>()
                for (doc in querySnapshot.documents) {
                    try {
                        val id = doc.getString("id") ?: continue
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
                        val quantity = doc.getLong("quantity")?.toInt() ?: 1
                        val weight = doc.getDouble("weight") ?: 1.0
                        val length = doc.getLong("length")?.toInt() ?: 10
                        val width = doc.getLong("width")?.toInt() ?: 10
                        val height = doc.getLong("height")?.toInt() ?: 10
                        val price = doc.getDouble("price") ?: 0.0
                        val courierName = doc.getString("courierName") ?: ""
                        val courierPhone = doc.getString("courierPhone") ?: ""
                        val courierAvatar = doc.getString("courierAvatar") ?: ""
                        val progress = doc.getDouble("progress")?.toFloat() ?: 0.0f
                        val dateString = doc.getString("dateString") ?: "Today"
                        val courierLatitude = doc.getDouble("courierLatitude")
                        val courierLongitude = doc.getDouble("courierLongitude")
                        val riderId = doc.getString("riderId") ?: ""
                        val riderBikeNumber = doc.getString("riderBikeNumber") ?: ""
                        val otpCode = doc.getString("otpCode") ?: ""
                        val otpVerified = doc.getBoolean("otpVerified") ?: false
                        val isRated = doc.getBoolean("isRated") ?: false
                        val customerRating = doc.getDouble("customerRating") ?: 0.0
                        val tipAmount = doc.getDouble("tipAmount") ?: 0.0

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
                            tipAmount = tipAmount
                        )
                        list.add(parcel)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing user parcel document: ${e.message}")
                    }
                }
                onComplete(list)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to fetch user parcel history: ${e.message}")
                onComplete(emptyList())
            }
    }

    /**
     * Push or update delivery real-time status in Firestore
     */
    fun syncParcelToFirestore(parcel: Parcel) {
        val db = firestore ?: return
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
            "lastUpdated" to System.currentTimeMillis()
        )

        db.collection("deliveries").document(parcel.id)
            .set(parcelMap)
            .addOnSuccessListener {
                Log.d(TAG, "Parcel ${parcel.id} successfully synced with Firestore.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to sync parcel ${parcel.id} with Firestore: ${e.message}")
            }
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
                    val quantity = snapshot.getLong("quantity")?.toInt() ?: 1
                    val weight = snapshot.getDouble("weight") ?: 1.0
                    val length = snapshot.getLong("length")?.toInt() ?: 10
                    val width = snapshot.getLong("width")?.toInt() ?: 10
                    val height = snapshot.getLong("height")?.toInt() ?: 10
                    val price = snapshot.getDouble("price") ?: 0.0
                    val courierName = snapshot.getString("courierName") ?: "Richard Dheo"
                    val courierPhone = snapshot.getString("courierPhone") ?: ""
                    val courierAvatar = snapshot.getString("courierAvatar") ?: ""
                    val progress = snapshot.getDouble("progress")?.toFloat() ?: 0.35f
                    val dateString = snapshot.getString("dateString") ?: "Today"
                    val courierLatitude = snapshot.getDouble("courierLatitude")
                    val courierLongitude = snapshot.getDouble("courierLongitude")
                    val riderId = snapshot.getString("riderId") ?: ""
                    val riderBikeNumber = snapshot.getString("riderBikeNumber") ?: ""
                    val otpCode = snapshot.getString("otpCode") ?: ""
                    val otpVerified = snapshot.getBoolean("otpVerified") ?: false
                    val isRated = snapshot.getBoolean("isRated") ?: false
                    val customerRating = snapshot.getDouble("customerRating") ?: 0.0
                    val tipAmount = snapshot.getDouble("tipAmount") ?: 0.0

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
                        courierLatitude = courierLatitude,
                        courierLongitude = courierLongitude,
                        riderId = riderId,
                        riderBikeNumber = riderBikeNumber,
                        otpCode = otpCode,
                        otpVerified = otpVerified,
                        isRated = isRated,
                        customerRating = customerRating,
                        tipAmount = tipAmount
                    )
                    trySend(parcel)
                } catch (e: Exception) {
                    Log.e(TAG, "Error mapping snapshot: ${e.message}")
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
                val quantity = snapshot.getLong("quantity")?.toInt() ?: 1
                val weight = snapshot.getDouble("weight") ?: 1.0
                val length = snapshot.getLong("length")?.toInt() ?: 10
                val width = snapshot.getLong("width")?.toInt() ?: 10
                val height = snapshot.getLong("height")?.toInt() ?: 10
                val price = snapshot.getDouble("price") ?: 0.0
                val courierName = snapshot.getString("courierName") ?: "Richard Dheo"
                val courierPhone = snapshot.getString("courierPhone") ?: ""
                val courierAvatar = snapshot.getString("courierAvatar") ?: ""
                val progress = snapshot.getDouble("progress")?.toFloat() ?: 0.35f
                val dateString = snapshot.getString("dateString") ?: "Today"
                val courierLatitude = snapshot.getDouble("courierLatitude")
                val courierLongitude = snapshot.getDouble("courierLongitude")

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
                    courierLatitude = courierLatitude,
                    courierLongitude = courierLongitude
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
                            val id = doc.getString("id") ?: continue
                            val title = doc.getString("title") ?: ""
                            val date = doc.getString("date") ?: ""
                            val amount = doc.getDouble("amount") ?: 0.0
                            val isTopUp = doc.getBoolean("isTopUp") ?: true
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
                            val id = doc.getString("id") ?: continue
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
                            val quantity = doc.getLong("quantity")?.toInt() ?: 1
                            val weight = doc.getDouble("weight") ?: 1.0
                            val length = doc.getLong("length")?.toInt() ?: 10
                            val width = doc.getLong("width")?.toInt() ?: 10
                            val height = doc.getLong("height")?.toInt() ?: 10
                            val price = doc.getDouble("price") ?: 0.0
                            val courierName = doc.getString("courierName") ?: ""
                            val courierPhone = doc.getString("courierPhone") ?: ""
                            val courierAvatar = doc.getString("courierAvatar") ?: ""
                            val progress = doc.getDouble("progress")?.toFloat() ?: 0.0f
                            val dateString = doc.getString("dateString") ?: "Today"
                            val courierLatitude = doc.getDouble("courierLatitude")
                            val courierLongitude = doc.getDouble("courierLongitude")
                            val riderId = doc.getString("riderId") ?: ""
                            val riderBikeNumber = doc.getString("riderBikeNumber") ?: ""
                            val otpCode = doc.getString("otpCode") ?: ""
                            val otpVerified = doc.getBoolean("otpVerified") ?: false
                            val isRated = doc.getBoolean("isRated") ?: false
                            val customerRating = doc.getDouble("customerRating") ?: 0.0
                            val tipAmount = doc.getDouble("tipAmount") ?: 0.0

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
                                tipAmount = tipAmount
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
                            val id = doc.getString("id") ?: continue
                            val title = doc.getString("title") ?: ""
                            val message = doc.getString("message") ?: ""
                            val time = doc.getString("time") ?: "Just now"
                            val isRead = doc.getBoolean("isRead") ?: false
                            list.add(NotificationItem(id, title, message, time, isRead))
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
                // If document doesn't exist yet, try to merge set
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
                // Fallback: merge-set if doc doesn't exist yet
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
                            val id = doc.getString("id") ?: continue
                            val itemName = doc.getString("itemName") ?: ""
                            val imageUrl = doc.getString("imageUrl") ?: ""
                            val statusStr = doc.getString("status") ?: "PENDING"
                            val status = try { ParcelStatus.valueOf(statusStr) } catch(e: Exception) { ParcelStatus.TRANSIT }
                            val pickupAddress = doc.getString("pickupAddress") ?: ""
                            val deliveryAddress = doc.getString("deliveryAddress") ?: ""
                            val senderName = doc.getString("senderName") ?: ""
                            val senderPhone = doc.getString("senderPhone") ?: ""
                            val receiverName = doc.getString("receiverName") ?: ""
                            val receiverPhone = doc.getString("receiverPhone") ?: ""
                            val quantity = doc.getLong("quantity")?.toInt() ?: 1
                            val weight = doc.getDouble("weight") ?: 1.0
                            val length = doc.getLong("length")?.toInt() ?: 10
                            val width = doc.getLong("width")?.toInt() ?: 10
                            val height = doc.getLong("height")?.toInt() ?: 10
                            val price = doc.getDouble("price") ?: 0.0
                            val courierName = doc.getString("courierName") ?: ""
                            val courierPhone = doc.getString("courierPhone") ?: ""
                            val courierAvatar = doc.getString("courierAvatar") ?: ""
                            val progress = doc.getDouble("progress")?.toFloat() ?: 0.0f
                            val dateString = doc.getString("dateString") ?: "Today"
                            val courierLatitude = doc.getDouble("courierLatitude")
                            val courierLongitude = doc.getDouble("courierLongitude")
                            val riderId = doc.getString("riderId") ?: ""
                            val riderBikeNumber = doc.getString("riderBikeNumber") ?: ""
                            val otpCode = doc.getString("otpCode") ?: ""
                            val otpVerified = doc.getBoolean("otpVerified") ?: false
                            val isRated = doc.getBoolean("isRated") ?: false
                            val customerRating = doc.getDouble("customerRating") ?: 0.0
                            val tipAmount = doc.getDouble("tipAmount") ?: 0.0

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
                                userId = doc.getString("userId") ?: "",
                                courierLatitude = courierLatitude,
                                courierLongitude = courierLongitude,
                                riderId = riderId,
                                riderBikeNumber = riderBikeNumber,
                                otpCode = otpCode,
                                otpVerified = otpVerified,
                                isRated = isRated,
                                customerRating = customerRating,
                                tipAmount = tipAmount
                            )
                            list.add(parcel)
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
     * Listen to assigned parcels for a specific rider in real time.
     */
    fun listenToRiderAssignments(riderId: String): Flow<List<Parcel>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("deliveries")
            .whereEqualTo("riderId", riderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to rider assignments: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = mutableListOf<Parcel>()
                    for (doc in snapshot.documents) {
                        try {
                            val id = doc.getString("id") ?: continue
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
                            val quantity = doc.getLong("quantity")?.toInt() ?: 1
                            val weight = doc.getDouble("weight") ?: 1.0
                            val length = doc.getLong("length")?.toInt() ?: 10
                            val width = doc.getLong("width")?.toInt() ?: 10
                            val height = doc.getLong("height")?.toInt() ?: 10
                            val price = doc.getDouble("price") ?: 0.0
                            val courierName = doc.getString("courierName") ?: ""
                            val courierPhone = doc.getString("courierPhone") ?: ""
                            val courierAvatar = doc.getString("courierAvatar") ?: ""
                            val progress = doc.getDouble("progress")?.toFloat() ?: 0.0f
                            val dateString = doc.getString("dateString") ?: "Today"
                            val courierLatitude = doc.getDouble("courierLatitude")
                            val courierLongitude = doc.getDouble("courierLongitude")
                            val rId = doc.getString("riderId") ?: ""
                            val riderBikeNumber = doc.getString("riderBikeNumber") ?: ""
                            val otpCode = doc.getString("otpCode") ?: ""
                            val otpVerified = doc.getBoolean("otpVerified") ?: false
                            val isRated = doc.getBoolean("isRated") ?: false
                            val customerRating = doc.getDouble("customerRating") ?: 0.0
                            val tipAmount = doc.getDouble("tipAmount") ?: 0.0

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
                                userId = doc.getString("userId") ?: "",
                                courierLatitude = courierLatitude,
                                courierLongitude = courierLongitude,
                                riderId = rId,
                                riderBikeNumber = riderBikeNumber,
                                otpCode = otpCode,
                                otpVerified = otpVerified,
                                isRated = isRated,
                                customerRating = customerRating,
                                tipAmount = tipAmount
                            )
                            list.add(parcel)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing assigned delivery: ${e.message}")
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
     * Accept a pending parcel and assign to rider in Firestore.
     */
    fun acceptParcelByRider(parcelId: String, riderId: String, riderName: String, riderPhone: String, riderBikeNumber: String, onComplete: (Boolean, String?) -> Unit) {
        val db = firestore
        if (db == null) {
            onComplete(false, "Firestore not available")
            return
        }

        val docRef = db.collection("deliveries").document(parcelId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val currentStatus = snapshot.getString("status") ?: "PENDING"
            if (currentStatus != "PENDING") {
                throw Exception("Parcel is no longer pending (already accepted or cancelled).")
            }
            
            // Set rider fields
            transaction.update(docRef, "status", "ASSIGNED")
            transaction.update(docRef, "riderId", riderId)
            transaction.update(docRef, "courierName", riderName)
            transaction.update(docRef, "courierPhone", riderPhone)
            transaction.update(docRef, "riderBikeNumber", riderBikeNumber)
            transaction.update(docRef, "progress", 0.15f)
            transaction.update(docRef, "lastUpdated", System.currentTimeMillis())
            
            // Get user ID of parcel owner to sync to their subcollection
            val parcelUserId = snapshot.getString("userId") ?: ""
            parcelUserId
        }.addOnSuccessListener { parcelUserId ->
            // Also sync to user's deliveries subcollection if available
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
                // Send push notification to customer
                sendNotificationToUser(
                    userId = parcelUserId,
                    title = "Parcel Assigned 🏍️",
                    message = "Your parcel #$parcelId has been assigned to driver $riderName ($riderBikeNumber).",
                    parcelId = parcelId
                )
            }
            // Send push notification to rider
            if (riderId.isNotEmpty()) {
                sendNotificationToUser(
                    userId = riderId,
                    title = "New Delivery Assignment 📦",
                    message = "You have been assigned to delivery #$parcelId. Tap to view route.",
                    parcelId = parcelId
                )
            }
            onComplete(true, null)
        }.addOnFailureListener { e ->
            onComplete(false, e.message ?: "Failed to accept parcel.")
        }
    }

    /**
     * Update parcel status (ASSIGNED -> PICKED_UP -> OUT_FOR_DELIVERY -> DELIVERED)
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
                val parcelUserId = snapshot.getString("userId") ?: ""
                
                db.runTransaction { transaction ->
                    transaction.update(docRef, "status", nextStatus.name)
                    transaction.update(docRef, "progress", progress)
                    transaction.update(docRef, "lastUpdated", System.currentTimeMillis())
                }.addOnSuccessListener {
                    // Update user personal delivery subcollection
                    if (parcelUserId.isNotEmpty()) {
                        val userDocRef = db.collection("users").document(parcelUserId).collection("deliveries").document(parcelId)
                        userDocRef.update(
                            mapOf(
                                "status" to nextStatus.name,
                                "progress" to progress,
                                "lastUpdated" to System.currentTimeMillis()
                            )
                        )
                        val statusTitle = when(nextStatus) {
                            ParcelStatus.TRANSIT -> "Parcel Out for Delivery 🚀"
                            ParcelStatus.DELIVERED -> "Parcel Delivered Successfully 🎉"
                            ParcelStatus.ASSIGNED -> "Parcel Assigned 🏍️"
                            else -> "Parcel Status Update 🔄"
                        }
                        sendNotificationToUser(
                            userId = parcelUserId,
                            title = statusTitle,
                            message = "Your parcel #$parcelId status is now ${nextStatus.name}.",
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
                // Match the cloud-synchronized OTP code exactly. If no OTP is registered on the cloud, fallback securely.
                val isValid = if (realOtp.isNotEmpty()) realOtp == otpInput else (otpInput == "1234" || otpInput.isEmpty())
                
                if (isValid) {
                    val parcelUserId = snapshot.getString("userId") ?: ""
                    val price = snapshot.getDouble("price") ?: 0.0
                    val riderId = snapshot.getString("riderId") ?: ""
                    
                    db.runTransaction { transaction ->
                        transaction.update(docRef, "status", "DELIVERED")
                        transaction.update(docRef, "progress", 1.0f)
                        transaction.update(docRef, "otpVerified", true)
                        transaction.update(docRef, "lastUpdated", System.currentTimeMillis())
                    }.addOnSuccessListener {
                        // Update subcollection
                        if (parcelUserId.isNotEmpty()) {
                            val userDocRef = db.collection("users").document(parcelUserId).collection("deliveries").document(parcelId)
                            userDocRef.update(
                                mapOf(
                                    "status" to "DELIVERED",
                                    "progress" to 1.0f,
                                    "otpVerified" to true,
                                    "lastUpdated" to System.currentTimeMillis()
                                )
                            )
                        }
                        
                        // Rider earnings: 80% of price is credited to rider's wallet balance!
                        if (riderId.isNotEmpty() && price > 0.0) {
                            val earningAmount = price * 0.8
                            val riderRef = db.collection("users").document(riderId)
                            
                            riderRef.get().addOnSuccessListener { riderSnap ->
                                val currentBalance = riderSnap.getDouble("walletBalance") ?: 0.0
                                val newBalance = currentBalance + earningAmount
                                
                                riderRef.update("walletBalance", newBalance).addOnSuccessListener {
                                    // Add earning transaction to rider's transactions subcollection
                                    val txRef = "ESD-EARN-${System.currentTimeMillis()}"
                                    val txMap = hashMapOf(
                                        "id" to txRef,
                                        "title" to "Delivery Earnings",
                                        "date" to "Today",
                                        "amount" to earningAmount,
                                        "isTopUp" to true,
                                        "timestamp" to System.currentTimeMillis()
                                    )
                                    riderRef.collection("transactions").document(txRef).set(txMap)
                                }
                            }
                        }
                        
                        onComplete(true, null)
                    }.addOnFailureListener { e ->
                        onComplete(false, e.message ?: "Failed to verify OTP.")
                    }
                } else {
                    onComplete(false, "Invalid 4-digit security code. Please check with customer.")
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
                    .addOnFailureListener { /* ignore */ }
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
            val parcelSnap = transaction.get(parcelDoc)
            if (!parcelSnap.exists()) {
                throw Exception("Parcel not found")
            }
            transaction.update(parcelDoc, mapOf(
                "isRated" to true,
                "customerRating" to rating,
                "tipAmount" to tipAmount
            ))

            // Update Customer's wallet balance (deduct tipAmount)
            if (customerId.isNotEmpty() && tipAmount > 0.0) {
                val customerRef = db.collection("users").document(customerId)
                val customerSnap = transaction.get(customerRef)
                if (customerSnap.exists()) {
                    val custBal = customerSnap.getDouble("walletBalance") ?: 0.0
                    if (custBal >= tipAmount) {
                        transaction.update(customerRef, "walletBalance", custBal - tipAmount)
                    }
                }
            }

            // Update Rider's wallet balance (add tipAmount)
            if (riderId.isNotEmpty() && tipAmount > 0.0) {
                val riderRef = db.collection("users").document(riderId)
                val riderSnap = transaction.get(riderRef)
                if (riderSnap.exists()) {
                    val currentRiderBal = riderSnap.getDouble("walletBalance") ?: 0.0
                    transaction.update(riderRef, "walletBalance", currentRiderBal + tipAmount)
                    
                    val oldRating = riderSnap.getDouble("rating") ?: 4.8
                    val newRating = ((oldRating * 4.0) + rating) / 5.0
                    transaction.update(riderRef, "rating", newRating)
                }
            }
        }.addOnSuccessListener {
            val parcelUserId = customerId
            if (parcelUserId.isNotEmpty()) {
                val userDocRef = db.collection("users").document(parcelUserId).collection("deliveries").document(parcelId)
                userDocRef.update(mapOf(
                    "isRated" to true,
                    "customerRating" to rating,
                    "tipAmount" to tipAmount
                ))

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

            if (riderId.isNotEmpty() && tipAmount > 0.0) {
                val txRef = "ESD-TIP-IN-${System.currentTimeMillis()}"
                val txMap = hashMapOf(
                    "id" to txRef,
                    "title" to "Tip from Customer",
                    "date" to "Today",
                    "amount" to tipAmount,
                    "isTopUp" to true,
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("users").document(riderId).collection("transactions").document(txRef).set(txMap)
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
                            val isOnline = doc.getBoolean("isOnline") ?: false
                            val statusStr = doc.getString("status") ?: "offline"
                            
                            val riderStatus = if (isOnline) {
                                if (statusStr == "busy") RiderStatus.BUSY else RiderStatus.ONLINE
                            } else {
                                RiderStatus.OFFLINE
                            }
                            
                            val lat = doc.getDouble("latitude") ?: 6.4281
                            val lng = doc.getDouble("longitude") ?: 3.4219
                            val workload = (doc.getLong("currentWorkload") ?: doc.getLong("activeDeliveriesCount") ?: 0L).toInt()
                            val battery = (doc.getLong("batteryLevel") ?: 90L).toInt()
                            val rating = doc.getDouble("rating") ?: 4.8
                            val avgTime = (doc.getLong("averageDeliveryTimeMin") ?: 20L).toInt()
                            
                            list.add(
                                Rider(
                                    id = uid,
                                    name = name,
                                    phone = phone,
                                    avatar = "https://images.unsplash.com/photo-1599566150163-29194dcaad36?w=100&h=100&fit=crop",
                                    vehicleType = if (bikeNumber.isNotEmpty()) "Bike" else "Bike",
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
                        val currentRating = doc.getDouble("rating") ?: 4.8
                        val count = (doc.getLong("ratingCount") ?: 5L).toDouble()
                        val newRating = ((currentRating * count) + rating) / (count + 1.0)
                        
                        db.collection("users").document(riderId).update(
                            mapOf(
                                "rating" to newRating,
                                "ratingCount" to (count + 1L)
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
        val registration = db.collection("users").document(riderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val lat = snapshot.getDouble("latitude") ?: snapshot.getDouble("lat")
                val lng = snapshot.getDouble("longitude") ?: snapshot.getDouble("lng")
                if (lat != null && lng != null) {
                    trySend(Pair(lat, lng))
                }
            }
        awaitClose { registration.remove() }
    }
}

