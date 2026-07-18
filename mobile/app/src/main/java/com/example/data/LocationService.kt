package com.example.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class LocationService : Service() {

    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private var trackingParcelId: String? = null

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == "STOP_TRACKING") {
            stopTracking()
            stopSelf()
            return START_NOT_STICKY
        }

        val parcelId = intent?.getStringExtra("parcelId")
        if (parcelId != null) {
            trackingParcelId = parcelId
            startTracking(parcelId)
            startForeground(NOTIFICATION_ID, buildNotification())
        }

        return START_STICKY
    }

    private fun startTracking(parcelId: String) {
        try {
            stopTracking() // clear any active first
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    Log.d("LocationService", "Location updated: ${location.latitude}, ${location.longitude}")
                    // Sync directly to Firestore
                    FirebaseManager.updateCourierLocationByRider(parcelId, location.latitude, location.longitude) { _, _ -> }
                }
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            val provider = if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
                LocationManager.GPS_PROVIDER
            } else {
                LocationManager.NETWORK_PROVIDER
            }

            if (androidx.core.content.PermissionChecker.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == androidx.core.content.PermissionChecker.PERMISSION_GRANTED) {
                locationManager?.requestLocationUpdates(
                    provider,
                    2000L, // 2 seconds
                    2f,    // 2 meters
                    locationListener!!,
                    android.os.Looper.getMainLooper()
                )
            }
        } catch (e: Exception) {
            Log.e("LocationService", "Error starting location tracking: ${e.message}")
        }
    }

    private fun stopTracking() {
        locationListener?.let {
            locationManager?.removeUpdates(it)
        }
        locationListener = null
    }

    override fun onDestroy() {
        stopTracking()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val title = "ENGRACED Dispatch active"
        val message = "Courier location tracking is active in the background."
        
        val intent = Intent(this, com.example.MainActivity::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(
            this,
            2,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Dispatcher Background Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background GPS location tracking for riders"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 991
        private const val CHANNEL_ID = "courier_background_tracking"
    }
}
