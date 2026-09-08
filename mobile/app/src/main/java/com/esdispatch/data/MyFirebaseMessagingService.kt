package com.esdispatch.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.esdispatch.MainActivity
import com.esdispatch.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM registration token: $token")
        // Persist locally and push to Firestore so backend can target this device
        val prefs = getSharedPreferences("esdispatch_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
        
        // Push token to Firestore so backend can target this device
        val uid = com.esdispatch.data.FirebaseManager.auth?.uid
        if (uid != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid)
                .update("fcmToken", token)
                .addOnSuccessListener { Log.d(TAG, "FCM token synced to Firestore") }
                .addOnFailureListener { e -> Log.e(TAG, "Failed to sync FCM token", e) }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "ESDispatch Status Update"
            val message = remoteMessage.data["message"] ?: "Your parcel status has changed."
            val parcelId = remoteMessage.data["parcelId"]
            val status = remoteMessage.data["status"]
            
            showNotification(applicationContext, title, message, parcelId, status)
            com.esdispatch.data.FirebaseManager.triggerFcmNotification(title, message)
        }

        // Check if message contains notification payload
        remoteMessage.notification?.let {
            val title = it.title ?: "ESDispatch Status Update"
            val body = it.body ?: "Your parcel status has changed."
            showNotification(applicationContext, title, body, null, null)
            com.esdispatch.data.FirebaseManager.triggerFcmNotification(title, body)
        }
    }

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "parcel_status_updates"
        private const val CHANNEL_NAME = "Parcel Status Updates"

        fun determineProgress(status: String?, text: String): Int {
            val st = status?.uppercase() ?: ""
            val lower = text.lowercase()
            return when {
                st.contains("DELIVERED") || lower.contains("delivered") -> 100
                st.contains("ARRIVED") || lower.contains("arrived") -> 90
                st.contains("TRANSIT") || st.contains("PICK") || lower.contains("transit") || lower.contains("picked") -> 65
                st.contains("ASSIGN") || lower.contains("assigned") -> 35
                st.contains("PENDING") || st.contains("BOOK") || lower.contains("booked") || lower.contains("pending") -> 15
                else -> 50
            }
        }

        fun showNotification(
            context: Context,
            title: String,
            message: String,
            parcelId: String? = null,
            status: String? = null
        ) {
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

            val smallIconRes = try {
                R.drawable.ic_logo
            } catch (e: Exception) {
                android.R.drawable.ic_dialog_info
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(smallIconRes)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Shows on Lock Screen

            // Dynamic stage progress on Lock Screen & Notification Drawer (15%, 35%, 65%, 90%, 100%)
            if (parcelId != null && parcelId != "GIFT") {
                val progress = determineProgress(status, "$title $message")
                if (progress >= 100) {
                    builder.setProgress(0, 0, false) // Completed: dismiss progress bar
                } else {
                    builder.setProgress(100, progress, false)
                }
            }

            // Interactive Quick Action: Track Live
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
            builder.addAction(android.R.drawable.ic_menu_compass, "Track Live", trackPendingIntent)

            // Stable notification ID per parcel so status updates modify existing notification cleanly
            val notificationId = if (!parcelId.isNullOrBlank() && parcelId != "GIFT") {
                Math.abs(parcelId.hashCode())
            } else {
                (System.currentTimeMillis() and 0x7FFFFFFF).toInt()
            }
            notificationManager.notify(notificationId, builder.build())
        }
    }
}
