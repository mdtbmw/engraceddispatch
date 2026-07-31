package com.esdispatch

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class DispatchApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeFirebaseSafely()
    }

    private fun initializeFirebaseSafely() {
        var initializedWithDefault = false
        try {
            val resId = resources.getIdentifier("google_app_id", "string", packageName)
            if (resId != 0) {
                FirebaseApp.initializeApp(this)
                android.util.Log.i("DispatchApplication", "FirebaseApp initialized with default provider.")
                initializedWithDefault = true
            } else {
                android.util.Log.w("DispatchApplication", "google_app_id resource not found. Will use BuildConfig fallback.")
            }
        } catch (e: Exception) {
            android.util.Log.w("DispatchApplication", "Default FirebaseApp initialization check failed: ${e.message}")
        }

        if (!initializedWithDefault) {
            try {
                val apiKey = try { com.esdispatch.BuildConfig.FIREBASE_API_KEY } catch (e: Throwable) { "" }
                val appId = try { com.esdispatch.BuildConfig.FIREBASE_APPLICATION_ID } catch (e: Throwable) { "" }
                val projectId = try { com.esdispatch.BuildConfig.FIREBASE_PROJECT_ID } catch (e: Throwable) { "" }
                val databaseUrl = try { com.esdispatch.BuildConfig.FIREBASE_DATABASE_URL } catch (e: Throwable) { "" }
                val gcmSenderId = try { com.esdispatch.BuildConfig.FIREBASE_GCM_SENDER_ID } catch (e: Throwable) { "" }

                if (apiKey.isBlank() || appId.isBlank()) {
                    android.util.Log.e("DispatchApplication", "Cannot initialize Firebase: missing API key or app ID in BuildConfig.")
                    return
                }

                val options = FirebaseOptions.Builder()
                    .setApiKey(apiKey)
                    .setApplicationId(appId)
                    .setProjectId(projectId.ifBlank { null })
                    .setDatabaseUrl(databaseUrl.ifBlank { null })
                    .setGcmSenderId(gcmSenderId.ifBlank { null })
                    .build()

                FirebaseApp.initializeApp(this, options)
                android.util.Log.i("DispatchApplication", "FirebaseApp initialized successfully with BuildConfig options.")
            } catch (ex: Exception) {
                android.util.Log.e("DispatchApplication", "Failed to initialize FirebaseApp: ${ex.message}")
            }
        }
    }
}
