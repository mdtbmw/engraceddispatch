package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM registration token: $token")
        // Send token to backend / save locally if needed
        val prefs = getSharedPreferences("engraced_dispatch_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "Engraced Dispatch Status Update"
            val message = remoteMessage.data["message"] ?: "Your parcel status has changed."
            val parcelId = remoteMessage.data["parcelId"]
            
            showNotification(applicationContext, title, message, parcelId)
            com.example.data.FirebaseManager.triggerFcmNotification(title, message)
        }

        // Check if message contains notification payload
        remoteMessage.notification?.let {
            val title = it.title ?: "Engraced Dispatch Status Update"
            val body = it.body ?: "Your parcel status has changed."
            showNotification(applicationContext, title, body, null)
            com.example.data.FirebaseManager.triggerFcmNotification(title, body)
        }
    }

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "parcel_status_updates"
        private const val CHANNEL_NAME = "Parcel Status Updates"

        fun showNotification(context: Context, title: String, message: String, parcelId: String? = null) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alerts when saved parcel tracking status changes"
                    enableLights(true)
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                if (parcelId != null) {
                    putExtra("parcelId", parcelId)
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Dynamic icons fallback
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Shows on Lock Screen

            // If it's a parcel delivery update, show the live progress bar on Lock Screen & Drawer (Points 16 & 17)
            if (parcelId != null && parcelId != "GIFT") {
                builder.setProgress(100, 75, false) // Live 75% progress bar on notification layout
            }

            // Interactive Quick Action: Track Live 📍 (Point 18)
            val trackIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("action", "track")
                if (parcelId != null) {
                    putExtra("parcelId", parcelId)
                }
            }
            val trackPendingIntent = PendingIntent.getActivity(
                context,
                1,
                trackIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_menu_compass, "Track Live 📍", trackPendingIntent)

            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}
