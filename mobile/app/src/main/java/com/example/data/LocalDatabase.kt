package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- 1. Type Converters ---
class DatabaseConverters {
    @TypeConverter
    fun fromParcelStatus(status: ParcelStatus): String {
        return status.name
    }

    @TypeConverter
    fun toParcelStatus(status: String): ParcelStatus {
        return try {
            ParcelStatus.valueOf(status)
        } catch (e: Exception) {
            ParcelStatus.TRANSIT
        }
    }
}

// --- 2. Data Access Objects (DAOs) ---
@Dao
interface ParcelDao {
    @Query("SELECT * FROM parcels ORDER BY id DESC")
    fun getAllParcels(): Flow<List<Parcel>>

    @Query("SELECT * FROM parcels WHERE id = :id LIMIT 1")
    suspend fun getParcelById(id: String): Parcel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParcel(parcel: Parcel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParcels(parcels: List<Parcel>)

    @Query("DELETE FROM parcels WHERE id = :id")
    suspend fun deleteParcel(id: String)

    @Query("DELETE FROM parcels")
    suspend fun clearParcels()
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY id DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<Transaction>)

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()
}

@Dao
interface AddressDao {
    @Query("SELECT * FROM address_items ORDER BY label ASC")
    fun getAllAddresses(): Flow<List<AddressItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddresses(addresses: List<AddressItem>)

    @Query("DELETE FROM address_items WHERE id = :id")
    suspend fun deleteAddress(id: String)

    @Query("DELETE FROM address_items")
    suspend fun clearAddresses()
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY id DESC")
    fun getAllNotifications(): Flow<List<NotificationItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationItem>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("DELETE FROM notifications")
    suspend fun clearNotifications()
}

// --- AIDispatchDecisionLog Entity and DAO ---
@Entity(tableName = "ai_dispatch_logs")
data class AIDispatchDecisionLog(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val parcelId: String,
    val parcelName: String,
    val assignedRiderId: String,
    val assignedRiderName: String,
    val confidenceScore: Int,
    val reason: String
)

@Dao
interface AIDispatchDao {
    @Query("SELECT * FROM ai_dispatch_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AIDispatchDecisionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AIDispatchDecisionLog)

    @Query("DELETE FROM ai_dispatch_logs")
    suspend fun clearLogs()
}

@Dao
interface ShiftAttendanceDao {
    @Query("SELECT * FROM shift_attendance ORDER BY dateString DESC")
    fun getAllAttendance(): Flow<List<ShiftAttendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: ShiftAttendance)

    @Query("DELETE FROM shift_attendance")
    suspend fun clearAttendance()
}

@Dao
interface VehicleInspectionDao {
    @Query("SELECT * FROM vehicle_inspections ORDER BY dateString DESC")
    fun getAllInspections(): Flow<List<VehicleInspection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: VehicleInspection)

    @Query("DELETE FROM vehicle_inspections")
    suspend fun clearInspections()
}

@Dao
interface ExpenseClaimDao {
    @Query("SELECT * FROM expense_claims ORDER BY dateString DESC")
    fun getAllExpenses(): Flow<List<ExpenseClaim>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseClaim)

    @Query("DELETE FROM expense_claims")
    suspend fun clearExpenses()
}

@Dao
interface ShiftRosterDao {
    @Query("SELECT * FROM shift_rosters ORDER BY shiftDate DESC")
    fun getAllRosters(): Flow<List<ShiftRoster>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoster(roster: ShiftRoster)

    @Query("DELETE FROM shift_rosters")
    suspend fun clearRosters()
}

@Dao
interface OfflineSyncQueueDao {
    @Query("SELECT * FROM offline_sync_queue WHERE synced = 0 ORDER BY timestamp ASC")
    fun getPendingSyncItems(): Flow<List<OfflineSyncQueue>>

    @Query("SELECT * FROM offline_sync_queue ORDER BY timestamp DESC")
    fun getAllSyncItems(): Flow<List<OfflineSyncQueue>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncItem(item: OfflineSyncQueue)

    @Query("UPDATE offline_sync_queue SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("DELETE FROM offline_sync_queue")
    suspend fun clearSyncItems()
}

// --- 3. Room Database ---
@Database(
    entities = [
        Parcel::class,
        Transaction::class,
        AddressItem::class,
        NotificationItem::class,
        AIDispatchDecisionLog::class,
        ShiftAttendance::class,
        VehicleInspection::class,
        ExpenseClaim::class,
        ShiftRoster::class,
        OfflineSyncQueue::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun parcelDao(): ParcelDao
    abstract fun transactionDao(): TransactionDao
    abstract fun addressDao(): AddressDao
    abstract fun notificationDao(): NotificationDao
    abstract fun aiDispatchDao(): AIDispatchDao
    abstract fun shiftAttendanceDao(): ShiftAttendanceDao
    abstract fun vehicleInspectionDao(): VehicleInspectionDao
    abstract fun expenseClaimDao(): ExpenseClaimDao
    abstract fun shiftRosterDao(): ShiftRosterDao
    abstract fun offlineSyncQueueDao(): OfflineSyncQueueDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gold_delivery_offline_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// --- 4. Abstracted Repository Class ---
class DeliveryRepository(private val db: AppDatabase) {
    val parcels: Flow<List<Parcel>> = db.parcelDao().getAllParcels()
    val transactions: Flow<List<Transaction>> = db.transactionDao().getAllTransactions()
    val addresses: Flow<List<AddressItem>> = db.addressDao().getAllAddresses()
    val notifications: Flow<List<NotificationItem>> = db.notificationDao().getAllNotifications()
    val aiDispatchLogs: Flow<List<AIDispatchDecisionLog>> = db.aiDispatchDao().getAllLogs()

    suspend fun getParcelById(id: String): Parcel? {
        return db.parcelDao().getParcelById(id)
    }

    suspend fun saveParcel(parcel: Parcel) {
        db.parcelDao().insertParcel(parcel)
    }

    suspend fun saveParcels(parcelsList: List<Parcel>) {
        db.parcelDao().insertParcels(parcelsList)
    }

    suspend fun deleteParcel(id: String) {
        db.parcelDao().deleteParcel(id)
    }

    suspend fun saveTransaction(transaction: Transaction) {
        db.transactionDao().insertTransaction(transaction)
    }

    suspend fun saveTransactions(transactionsList: List<Transaction>) {
        db.transactionDao().insertTransactions(transactionsList)
    }

    suspend fun saveAddress(address: AddressItem) {
        db.addressDao().insertAddress(address)
    }

    suspend fun saveAddresses(addressesList: List<AddressItem>) {
        db.addressDao().insertAddresses(addressesList)
    }

    suspend fun deleteAddress(id: String) {
        db.addressDao().deleteAddress(id)
    }

    suspend fun saveNotification(notification: NotificationItem) {
        db.notificationDao().insertNotification(notification)
    }

    suspend fun saveNotifications(notificationsList: List<NotificationItem>) {
        db.notificationDao().insertNotifications(notificationsList)
    }

    suspend fun markNotificationAsRead(id: String) {
        db.notificationDao().markAsRead(id)
    }

    suspend fun saveAIDispatchLog(log: AIDispatchDecisionLog) {
        db.aiDispatchDao().insertLog(log)
    }

    val shiftAttendance: Flow<List<ShiftAttendance>> = db.shiftAttendanceDao().getAllAttendance()
    val vehicleInspections: Flow<List<VehicleInspection>> = db.vehicleInspectionDao().getAllInspections()
    val expenseClaims: Flow<List<ExpenseClaim>> = db.expenseClaimDao().getAllExpenses()
    val shiftRosters: Flow<List<ShiftRoster>> = db.shiftRosterDao().getAllRosters()
    val offlineSyncQueue: Flow<List<OfflineSyncQueue>> = db.offlineSyncQueueDao().getAllSyncItems()
    val pendingSyncQueue: Flow<List<OfflineSyncQueue>> = db.offlineSyncQueueDao().getPendingSyncItems()

    suspend fun saveShiftAttendance(attendance: ShiftAttendance) {
        db.shiftAttendanceDao().insertAttendance(attendance)
    }

    suspend fun saveVehicleInspection(inspection: VehicleInspection) {
        db.vehicleInspectionDao().insertInspection(inspection)
    }

    suspend fun saveExpenseClaim(claim: ExpenseClaim) {
        db.expenseClaimDao().insertExpense(claim)
    }

    suspend fun saveShiftRoster(roster: ShiftRoster) {
        db.shiftRosterDao().insertRoster(roster)
    }

    suspend fun saveOfflineSyncItem(item: OfflineSyncQueue) {
        db.offlineSyncQueueDao().insertSyncItem(item)
    }

    suspend fun markSyncItemSynced(id: String) {
        db.offlineSyncQueueDao().markSynced(id)
    }

    suspend fun clearAllData() {
        db.parcelDao().clearParcels()
        db.transactionDao().clearTransactions()
        db.addressDao().clearAddresses()
        db.notificationDao().clearNotifications()
        db.aiDispatchDao().clearLogs()
        db.shiftAttendanceDao().clearAttendance()
        db.vehicleInspectionDao().clearInspections()
        db.expenseClaimDao().clearExpenses()
        db.shiftRosterDao().clearRosters()
        db.offlineSyncQueueDao().clearSyncItems()
    }
}
