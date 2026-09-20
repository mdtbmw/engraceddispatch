package com.esdispatch.util

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.esdispatch.MainActivity
import com.esdispatch.R
import com.esdispatch.data.FirebaseManager
import com.esdispatch.data.ParcelStatus
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val TAG = "LocationService"
    private val CHANNEL_ID = "ESDispatch_Location_Channel"
    private val NOTIFICATION_ID = 1001

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                handleNewLocation(location)
            }
        }
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) {
            Log.e(TAG, "Cannot start LocationService: Location permissions not granted")
            stopSelf()
            return START_NOT_STICKY
        }

        intent?.let {
            val pId = it.getStringExtra(EXTRA_PARCEL_ID)
            if (!pId.isNullOrBlank()) activeParcelId = pId
            val bId = it.getStringExtra(EXTRA_BATCH_ID)
            if (!bId.isNullOrBlank()) activeBatchId = bId
            val sLat = it.getDoubleExtra(EXTRA_STOP_LAT, 0.0)
            val sLng = it.getDoubleExtra(EXTRA_STOP_LNG, 0.0)
            if (sLat != 0.0 && sLng != 0.0) {
                activeStopLat = sLat
                activeStopLng = sLng
            }
            val sType = it.getStringExtra(EXTRA_STOP_TYPE)
            if (!sType.isNullOrBlank()) activeStopType = sType
        }

        try {
            val notification = createNotification("Live GPS Active", "Broadcasting real-time courier telemetry...")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            startLocationUpdates()
            _isServiceRunning.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting LocationService foreground: ${e.message}")
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateIntervalMillis(2000L)
            .setMinUpdateDistanceMeters(2.0f)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "FusedLocationProviderClient updates requested.")
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission missing: ${e.message}")
        }
    }

    private fun handleNewLocation(location: Location) {
        // Validate coordinates
        if (location.latitude < -90.0 || location.latitude > 90.0 ||
            location.longitude < -180.0 || location.longitude > 180.0 ||
            (location.latitude == 0.0 && location.longitude == 0.0)
        ) {
            Log.w(TAG, "Ignoring invalid coordinate: ${location.latitude}, ${location.longitude}")
            return
        }

        // Update in-memory reactive state flow
        _liveLocationFlow.value = location

        val userId = FirebaseManager.auth?.currentUser?.uid ?: return
        val gpsTimestamp = if (location.time > 0L) location.time else System.currentTimeMillis()

        serviceScope.launch {
            broadcastToFirebase(userId, location, gpsTimestamp)
            checkProximityArrival(location)
        }
    }

    private fun broadcastToFirebase(userId: String, location: Location, gpsTimestamp: Long) {
        val db = FirebaseManager.firestore ?: return

        // 1. Broadcast to fleet_locations
        val fleetData = hashMapOf<String, Any>(
            "riderId" to userId,
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "lat" to location.latitude,
            "lng" to location.longitude,
            "accuracy" to location.accuracy,
            "speed" to location.speed,
            "bearing" to location.bearing,
            "heading" to location.bearing,
            "timestamp" to gpsTimestamp,
            "updatedAt" to System.currentTimeMillis(),
            "activeBookingId" to (activeParcelId ?: ""),
            "activeBatchId" to (activeBatchId ?: "")
        )

        db.collection("fleet_locations").document(userId)
            .set(fleetData, com.google.firebase.firestore.SetOptions.merge())
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update fleet_locations: ${e.message}")
            }

        // Dual-write to users/{userId} so Cloud Functions auto-dispatch matches actual coordinates
        val userLocationUpdate = hashMapOf<String, Any>(
            "lat" to location.latitude,
            "lng" to location.longitude,
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "lastGpsTimestamp" to gpsTimestamp
        )
        db.collection("users").document(userId)
            .set(userLocationUpdate, com.google.firebase.firestore.SetOptions.merge())
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to update user GPS coordinates: ${e.message}")
            }

        // 2. Broadcast to active delivery document if assigned
        val pId = activeParcelId
        if (!pId.isNullOrBlank()) {
            val deliveryUpdate = hashMapOf<String, Any>(
                "courierLatitude" to location.latitude,
                "courierLongitude" to location.longitude,
                "courierBearing" to location.bearing.toDouble(),
                "courierSpeed" to location.speed.toDouble(),
                "courierAccuracy" to location.accuracy.toDouble(),
                "courierLastUpdated" to gpsTimestamp
            )
            db.collection("deliveries").document(pId)
                .update(deliveryUpdate)
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to update active delivery telemetry: ${e.message}")
                }
        }
    }

    private fun checkProximityArrival(location: Location) {
        val pId = activeParcelId ?: return
        val stopLat = activeStopLat ?: return
        val stopLng = activeStopLng ?: return
        if (stopLat == 0.0 || stopLng == 0.0) return

        val results = FloatArray(1)
        Location.distanceBetween(location.latitude, location.longitude, stopLat, stopLng, results)
        val distanceMeters = results[0]

        if (distanceMeters <= 50.0f && !hasTriggeredArrivalForCurrentStop) {
            hasTriggeredArrivalForCurrentStop = true
            Log.d(TAG, "Proximity arrival detected for stop: $distanceMeters m")
            val db = FirebaseManager.firestore ?: return
            val nextStatus = if (activeStopType == "PICKUP") ParcelStatus.ARRIVED_PICKUP else ParcelStatus.ARRIVED
            db.collection("deliveries").document(pId)
                .update(mapOf("status" to nextStatus.name))
                .addOnSuccessListener {
                    Log.d(TAG, "Proximity arrival status updated to ${nextStatus.name}")
                }
        }
    }

    private fun createNotification(title: String, message: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Rider Live GPS Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Continuous background GPS telemetry for live dispatch orders"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        _isServiceRunning.value = false
        Log.d(TAG, "LocationService destroyed.")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_PARCEL_ID = "extra_parcel_id"
        const val EXTRA_BATCH_ID = "extra_batch_id"
        const val EXTRA_STOP_LAT = "extra_stop_lat"
        const val EXTRA_STOP_LNG = "extra_stop_lng"
        const val EXTRA_STOP_TYPE = "extra_stop_type" // "PICKUP" or "DELIVERY"

        @Volatile
        var activeParcelId: String? = null

        @Volatile
        var activeBatchId: String? = null

        @Volatile
        var activeStopLat: Double? = null

        @Volatile
        var activeStopLng: Double? = null

        @Volatile
        var activeStopType: String = "DELIVERY"

        @Volatile
        var hasTriggeredArrivalForCurrentStop = false

        private val _liveLocationFlow = MutableStateFlow<Location?>(null)
        val liveLocationFlow: StateFlow<Location?> = _liveLocationFlow.asStateFlow()

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        fun start(
            context: Context,
            parcelId: String? = null,
            batchId: String? = null,
            stopLat: Double? = null,
            stopLng: Double? = null,
            stopType: String = "DELIVERY"
        ) {
            activeParcelId = parcelId
            activeBatchId = batchId
            activeStopLat = stopLat
            activeStopLng = stopLng
            activeStopType = stopType
            hasTriggeredArrivalForCurrentStop = false

            val intent = Intent(context, LocationService::class.java).apply {
                putExtra(EXTRA_PARCEL_ID, parcelId)
                putExtra(EXTRA_BATCH_ID, batchId)
                if (stopLat != null) putExtra(EXTRA_STOP_LAT, stopLat)
                if (stopLng != null) putExtra(EXTRA_STOP_LNG, stopLng)
                putExtra(EXTRA_STOP_TYPE, stopType)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e("LocationService", "Failed to start service: ${e.message}")
            }
        }

        fun updateActiveStop(
            parcelId: String,
            stopLat: Double?,
            stopLng: Double?,
            stopType: String
        ) {
            activeParcelId = parcelId
            activeStopLat = stopLat
            activeStopLng = stopLng
            activeStopType = stopType
            hasTriggeredArrivalForCurrentStop = false
        }

        fun stop(context: Context) {
            activeParcelId = null
            activeBatchId = null
            activeStopLat = null
            activeStopLng = null
            hasTriggeredArrivalForCurrentStop = false
            try {
                val intent = Intent(context, LocationService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                Log.e("LocationService", "Failed to stop service: ${e.message}")
            }
        }
    }
}
