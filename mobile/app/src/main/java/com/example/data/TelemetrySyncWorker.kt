package com.example.data

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.first
import org.json.JSONObject

class TelemetrySyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("TelemetrySyncWorker", "WorkManager task triggered! Commencing background offline telemetry sync...")
        val db = AppDatabase.getDatabase(applicationContext)
        val queueDao = db.offlineSyncQueueDao()
        val attendanceDao = db.shiftAttendanceDao()
        val inspectionDao = db.vehicleInspectionDao()
        val expenseDao = db.expenseClaimDao()

        val firestore = FirebaseFirestore.getInstance()

        try {
            // 1. Sync offline queue
            val pendingItems = queueDao.getPendingSyncItems().first()
            if (pendingItems.isNotEmpty()) {
                Log.d("TelemetrySyncWorker", "Found ${pendingItems.size} pending offline sync items.")
                pendingItems.forEach { item ->
                    try {
                        val payload = JSONObject(item.payloadJson)
                        val firestoreMap = mutableMapOf<String, Any>()
                        val keys = payload.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            firestoreMap[key] = payload.get(key)
                        }

                        val collectionName = when (item.actionType) {
                            "EXPENSE" -> "expense_claims"
                            "INSPECTION" -> "vehicle_inspections"
                            "GPS_LOG" -> "driver_gps_logs"
                            else -> "deliveries_sync"
                        }

                        firestore.collection(collectionName)
                            .document(item.id)
                            .set(firestoreMap, SetOptions.merge())

                        queueDao.markSynced(item.id)
                    } catch (e: Exception) {
                        Log.e("TelemetrySyncWorker", "Failed to sync queue item ${item.id}", e)
                    }
                }
            }

            // 2. Sync Shift Attendance
            val attendanceList = attendanceDao.getAllAttendance().first()
            attendanceList.forEach { att ->
                val attMap = hashMapOf(
                    "id" to att.id,
                    "riderId" to att.riderId,
                    "dateString" to att.dateString,
                    "clockInTime" to att.clockInTime,
                    "clockOutTime" to att.clockOutTime,
                    "totalHours" to att.totalHours,
                    "status" to att.status
                )
                firestore.collection("shift_attendance")
                    .document(att.id)
                    .set(attMap, SetOptions.merge())
            }

            // 3. Sync Vehicle Inspections
            val inspections = inspectionDao.getAllInspections().first()
            inspections.forEach { ins ->
                val insMap = hashMapOf(
                    "id" to ins.id,
                    "riderId" to ins.riderId,
                    "dateString" to ins.dateString,
                    "brakesOk" to ins.brakesOk,
                    "tiresOk" to ins.tiresOk,
                    "headlightsOk" to ins.headlightsOk,
                    "hornOk" to ins.hornOk,
                    "fuelBatteryLevelOk" to ins.fuelBatteryLevelOk,
                    "safetyVestHelmetOk" to ins.safetyVestHelmetOk,
                    "notes" to ins.notes,
                    "passed" to ins.passed
                )
                firestore.collection("vehicle_inspections")
                    .document(ins.id)
                    .set(insMap, SetOptions.merge())
            }

            // 4. Sync Expense Claims
            val expenses = expenseDao.getAllExpenses().first()
            expenses.forEach { exp ->
                val expMap = hashMapOf(
                    "id" to exp.id,
                    "riderId" to exp.riderId,
                    "title" to exp.title,
                    "dateString" to exp.dateString,
                    "amount" to exp.amount,
                    "category" to exp.category,
                    "receiptNote" to exp.receiptNote,
                    "status" to exp.status
                )
                firestore.collection("expense_claims")
                    .document(exp.id)
                    .set(expMap, SetOptions.merge())
            }

            Log.d("TelemetrySyncWorker", "Background telemetry sync completed successfully!")
            return Result.success()
        } catch (e: Exception) {
            Log.e("TelemetrySyncWorker", "Error executing background telemetry sync", e)
            return Result.retry()
        }
    }
}
