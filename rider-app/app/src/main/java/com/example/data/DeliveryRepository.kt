package com.example.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class DeliveryRepository(private val deliveryDao: DeliveryDao) {
    private val db = FirebaseFirestore.getInstance()

    val allDeliveries: Flow<List<Delivery>> = deliveryDao.getAllDeliveries()

    fun getDeliveryByTrackingFlow(trackingNumber: String): Flow<Delivery?> {
        return deliveryDao.getDeliveryByTrackingFlow(trackingNumber)
    }

    fun getDeliveryById(id: Long): Flow<Delivery?> {
        return deliveryDao.getDeliveryById(id)
    }

    suspend fun getDeliveryByTracking(trackingNumber: String): Delivery? {
        return deliveryDao.getDeliveryByTracking(trackingNumber)
    }

    suspend fun syncDeliveriesFromFirestore(riderId: String) {
        try {
            val snapshot = db.collection("deliveries")
                .whereEqualTo("riderId", riderId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val deliveries = snapshot.documents.mapNotNull { doc ->
                val d = doc.toObject(Delivery::class.java)
                d?.copy(id = doc.id.hashCode().toLong().let { if (it == 0L) 1L else it })
            }
            deliveries.forEach { deliveryDao.insertDelivery(it) }
        } catch (_: Exception) { }
    }

    suspend fun insert(delivery: Delivery): Long {
        val localId = deliveryDao.insertDelivery(delivery)
        try {
            val riderId = FirebaseService.currentUser?.uid ?: return localId
            val docRef = db.collection("deliveries").document()
            val firestoreData = delivery.copy(id = localId)
            docRef.set(firestoreData).await()
        } catch (_: Exception) { }
        return localId
    }

    suspend fun update(delivery: Delivery) {
        deliveryDao.updateDelivery(delivery)
        try {
            val snapshot = db.collection("deliveries")
                .whereEqualTo("id", delivery.id)
                .limit(1)
                .get()
                .await()
            snapshot.documents.firstOrNull()?.let { doc ->
                doc.reference.set(delivery).await()
            }
        } catch (_: Exception) { }
    }

    suspend fun delete(delivery: Delivery) {
        deliveryDao.deleteDelivery(delivery)
        try {
            val snapshot = db.collection("deliveries")
                .whereEqualTo("id", delivery.id)
                .limit(1)
                .get()
                .await()
            snapshot.documents.firstOrNull()?.let { doc ->
                doc.reference.delete().await()
            }
        } catch (_: Exception) { }
    }
}
