package com.esdispatch.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.esdispatch.data.FirebaseManager
import com.google.android.gms.location.*

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val TAG = "LocationService"
    private val CHANNEL_ID = "ESDispatch_Location_Channel"

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val userId = FirebaseManager.auth?.currentUser?.uid
                    if (userId != null) {
                        broadcastLocationToFirestore(userId, location)
                    }
                }
            }
        }
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1001, notification)
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()
            
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission missing: ${e.message}")
        }
    }

    private fun broadcastLocationToFirestore(userId: String, location: Location) {
        val db = FirebaseManager.firestore ?: return
        val map = hashMapOf(
            "riderId" to userId,
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "speed" to location.speed,
            "bearing" to location.bearing,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("fleet_locations").document(userId)
            .set(map)
            .addOnSuccessListener { Log.d(TAG, "Rider location broadcasted.") }
            .addOnFailureListener { e -> Log.e(TAG, "Failed to broadcast location: ${e.message}") }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ESDispatch Rider Active")
            .setContentText("Broadcasting live location to headquarters...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Rider Location Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
