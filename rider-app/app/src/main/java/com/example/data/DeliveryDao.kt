package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryDao {
    @Query("SELECT * FROM deliveries ORDER BY createdAt DESC")
    fun getAllDeliveries(): Flow<List<Delivery>>

    @Query("SELECT * FROM deliveries WHERE trackingNumber = :trackingNumber LIMIT 1")
    suspend fun getDeliveryByTracking(trackingNumber: String): Delivery?

    @Query("SELECT * FROM deliveries WHERE trackingNumber = :trackingNumber LIMIT 1")
    fun getDeliveryByTrackingFlow(trackingNumber: String): Flow<Delivery?>

    @Query("SELECT * FROM deliveries WHERE id = :id LIMIT 1")
    fun getDeliveryById(id: Long): Flow<Delivery?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDelivery(delivery: Delivery): Long

    @Update
    suspend fun updateDelivery(delivery: Delivery)

    @Delete
    suspend fun deleteDelivery(delivery: Delivery)
}
