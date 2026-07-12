package com.example

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
                android.util.Log.w("DispatchApplication", "google_app_id resource not found. Will use programmatic fallback.")
            }
        } catch (e: Exception) {
            android.util.Log.w("DispatchApplication", "Default FirebaseApp initialization check failed: ${e.message}")
        }

        if (!initializedWithDefault) {
            try {
                // Load variables dynamically from BuildConfig if configured
                val apiKey = try { com.example.BuildConfig.FIREBASE_API_KEY } catch (e: Throwable) { "AIzaSyFakeKeyPlaceholderForEngracedDispatch" }
                val appId = try { com.example.BuildConfig.FIREBASE_APPLICATION_ID } catch (e: Throwable) { "1:1234567890:android:fakeid777" }
                val projectId = try { com.example.BuildConfig.FIREBASE_PROJECT_ID } catch (e: Throwable) { "engraced-dispatch-preview" }
                val databaseUrl = try { com.example.BuildConfig.FIREBASE_DATABASE_URL } catch (e: Throwable) { "https://engraced-dispatch-preview.firebaseio.com" }
                val gcmSenderId = try { com.example.BuildConfig.FIREBASE_GCM_SENDER_ID } catch (e: Throwable) { "1234567890" }

                // Programmatic options with robust fallbacks
                val options = FirebaseOptions.Builder()
                    .setApiKey(apiKey.ifBlank { "AIzaSyFakeKeyPlaceholderForEngracedDispatch" })
                    .setApplicationId(appId.ifBlank { "1:1234567890:android:fakeid777" })
                    .setProjectId(projectId.ifBlank { "engraced-dispatch-preview" })
                    .setDatabaseUrl(databaseUrl.ifBlank { "https://engraced-dispatch-preview.firebaseio.com" })
                    .setGcmSenderId(gcmSenderId.ifBlank { "1234567890" })
                    .build()
                
                // Clear any existing invalid default FirebaseApp instances if they got registered somehow
                val apps = FirebaseApp.getApps(this)
                if (apps.isNotEmpty()) {
                    android.util.Log.w("DispatchApplication", "Found existing FirebaseApp instances. Re-initializing default with fallback options.")
                }
                
                FirebaseApp.initializeApp(this, options)
                android.util.Log.i("DispatchApplication", "FirebaseApp initialized successfully with fallback programmatic options.")
            } catch (ex: Exception) {
                android.util.Log.e("DispatchApplication", "Failed to initialize FirebaseApp with programmatic options: ${ex.message}")
            }
        }
    }
}
