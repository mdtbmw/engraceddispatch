package com.esdispatch.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
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
        val prefs = getSharedPreferences("esdispatch_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
        
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

        // Upgraded Notification Channels with Custom Sounds
        const val CHANNEL_DISPATCH = "es_dispatch_v2"
        const val CHANNEL_TRACKING = "es_tracking_v2"
        const val CHANNEL_ARRIVAL = "es_arrival_v2"
        const val CHANNEL_GENERAL = "es_general_v2"

        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val packageName = context.packageName

                // Custom Sound URIs
                val dispatchSoundUri = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://$packageName/raw/es_dispatch_alert")
                val trackingSoundUri = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://$packageName/raw/es_delivery_update")
                val arrivalSoundUri = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://$packageName/raw/es_arrived_alert")
                val generalSoundUri = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://$packageName/raw/es_success_chime")

                val instantAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                    .build()

                val standardAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()

                // 1. Rider Dispatch Channel (High importance, distinct dispatch sweep, lockscreen public)
                val dispatchChannel = NotificationChannel(
                    CHANNEL_DISPATCH,
                    "Rider Dispatch Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Urgent notifications when new dispatches or assignments arrive"
                    enableLights(true)
                    lightColor = Color.parseColor("#FFB800")
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 300, 150, 300)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                    setSound(dispatchSoundUri, instantAttributes)
                }

                // 2. Tracking Progress Channel (High importance, soothing arpeggio chime, lockscreen progress)
                val trackingChannel = NotificationChannel(
                    CHANNEL_TRACKING,
                    "Delivery Tracking & Progress",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Live progress and stage updates for active deliveries"
                    enableLights(true)
                    lightColor = Color.parseColor("#FFB800")
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 200, 100, 200)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                    setSound(trackingSoundUri, standardAttributes)
                }

                // 3. Arrival Alerts Channel (High importance, resonant dual chime)
                val arrivalChannel = NotificationChannel(
                    CHANNEL_ARRIVAL,
                    "Courier Arrival Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alerts when courier has arrived at pickup or delivery destination"
                    enableLights(true)
                    lightColor = Color.parseColor("#FFB800")
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 400, 200, 400)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                    setSound(arrivalSoundUri, instantAttributes)
                }

                // 4. General Announcements / Completed Handover
                val generalChannel = NotificationChannel(
                    CHANNEL_GENERAL,
                    "Delivery Completions & Credits",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Delivery completions, tips, security PINs, and wallet credits"
                    enableLights(true)
                    lightColor = Color.parseColor("#FFB800")
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                    setSound(generalSoundUri, standardAttributes)
                }

                notificationManager.createNotificationChannels(
                    listOf(dispatchChannel, trackingChannel, arrivalChannel, generalChannel)
                )
            }
        }

        fun determineProgress(status: String?, text: String): Int {
            val st = status?.uppercase() ?: ""
            val lower = text.lowercase()
            if (lower.contains("pin") || lower.contains("otp") || lower.contains("code") || lower.contains("verify") || lower.contains("verification")) {
                return 0 // Do not display a misleading progress bar on OTP/PIN verification alerts
            }
            return when {
                st.contains("DELIVERED") || lower.contains("delivered") -> 100
                st.contains("ARRIVED") || lower.contains("arrived") -> 90
                st.contains("TRANSIT") || st.contains("OUT_FOR_DELIVERY") || lower.contains("transit") -> 70
                st.contains("PICK") || lower.contains("picked") -> 50
                st.contains("ASSIGN") || lower.contains("assigned") -> 35
                st.contains("PENDING") || st.contains("BOOK") || lower.contains("booked") || lower.contains("pending") -> 10
                else -> 0
            }
        }

        fun showNotification(
            context: Context,
            title: String,
            message: String,
            parcelId: String? = null,
            status: String? = null
        ) {
            createNotificationChannels(context)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val packageName = context.packageName
            val st = status?.uppercase() ?: ""
            val lower = "$title $message".lowercase()

            // Select channel & sound based on stage
            val (channelId, soundName) = when {
                lower.contains("dispatch available") || lower.contains("trip assigned") || lower.contains("new dispatch") || parcelId == "DISPATCH" ->
                    Pair(CHANNEL_DISPATCH, "es_dispatch_alert")
                st.contains("ARRIVED") || lower.contains("arrived") ->
                    Pair(CHANNEL_ARRIVAL, "es_arrived_alert")
                st.contains("DELIVERED") || lower.contains("delivered") || parcelId == "GIFT" ->
                    Pair(CHANNEL_GENERAL, "es_success_chime")
                else ->
                    Pair(CHANNEL_TRACKING, "es_delivery_update")
            }

            val soundUri = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://$packageName/raw/$soundName")

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                if (parcelId != null) {
                    putExtra("parcelId", parcelId)
                    putExtra("action", "track")
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val smallIconRes = try {
                R.drawable.ic_notification
            } catch (e: Exception) {
                R.drawable.ic_logo
            }

            val isCompletedOrCancelled = st.contains("DELIVERED") || st.contains("CANCELLED") || lower.contains("delivered") || lower.contains("cancelled")
            val isOngoingDelivery = parcelId != null && parcelId != "GIFT" && parcelId != "OTP" && !isCompletedOrCancelled

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(smallIconRes)
                .setColor(Color.parseColor("#FFB800"))
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setCategory(if (isOngoingDelivery) NotificationCompat.CATEGORY_NAVIGATION else NotificationCompat.CATEGORY_STATUS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Shows full content on Lock Screen
                .setSound(soundUri)
                .setAutoCancel(!isOngoingDelivery)
                .setOngoing(isOngoingDelivery) // Pinned to lockscreen while delivery is active

            // Dynamic stage progress on Lock Screen & Notification Drawer (10%, 35%, 50%, 70%, 90%, 100%)
            if (parcelId != null && parcelId != "GIFT" && parcelId != "OTP") {
                val progress = determineProgress(status, "$title $message")
                if (progress in 1..99) {
                    builder.setProgress(100, progress, false)
                } else if (progress >= 100) {
                    builder.setProgress(0, 0, false)
                }
            }

            // Interactive Quick Action: Track Live / View Dispatch
            val actionTitle = if (lower.contains("dispatch") || lower.contains("assigned")) "View Dispatch" else "Track Live"
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
            builder.addAction(android.R.drawable.ic_menu_compass, actionTitle, trackPendingIntent)

            // Stable notification ID per parcel so status updates update in-place without notification clutter
            val notificationId = if (!parcelId.isNullOrBlank() && parcelId != "GIFT" && parcelId != "OTP") {
                Math.abs(parcelId.hashCode())
            } else {
                (System.currentTimeMillis() and 0x7FFFFFFF).toInt()
            }
            notificationManager.notify(notificationId, builder.build())
        }

        /**
         * Test trigger for users to test their lockscreen notification and satisfying custom chime.
         */
        fun triggerTestNotification(context: Context) {
            showNotification(
                context = context,
                title = "ESDispatch • Audio & Lockscreen Test",
                message = "Satisfying custom chime active. Live progress and status will display on your lockscreen.",
                parcelId = "TEST_${System.currentTimeMillis()}",
                status = "TRANSIT"
            )
        }
    }
}
