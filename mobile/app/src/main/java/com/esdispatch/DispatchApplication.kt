package com.esdispatch

import android.app.Application
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DispatchApplication : Application() {
    companion object {
        lateinit var instance: DispatchApplication
            private set

        const val PREFS = "esdispatch_crash_prefs"
        const val KEY_LAST_CRASH = "last_crash_trace"
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        installCrashHandler()
        initializeFirebaseSafely()
    }

    private fun installCrashHandler() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val trace = dumpTrace(throwable)
                android.util.Log.e("ESDispatchCrash", trace)
                saveCrashToFile(trace)
                prefs().edit().putString(KEY_LAST_CRASH, trace).apply()
            } catch (e: Throwable) {
                android.util.Log.e("ESDispatchCrash", "Crash handler failed: ${e.message}")
            }
            previous?.uncaughtException(thread, throwable)
        }
    }

    private fun prefs() = getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)

    private fun dumpTrace(t: Throwable): String {
        val sw = StringWriter()
        t.printStackTrace(PrintWriter(sw))
        val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        return "=== ESDispatch Crash @ $stamp ===" +
                "\nBuild: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}), ${Build.MODEL}" +
                "\nPackage: $packageName, version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})" +
                "\n\n$sw"
    }

    private fun saveCrashToFile(trace: String) {
        try {
            val extDir = getExternalFilesDir(null) ?: return
            extDir.mkdirs()
            val file = java.io.File(extDir, "esdispatch_crash.txt")
            file.writeText(trace)
            android.util.Log.i("ESDispatchCrash", "Crash log written to ${file.absolutePath}")
        } catch (e: Throwable) {
            android.util.Log.e("ESDispatchCrash", "Could not write app crash file: ${e.message}")
        }

        if (Build.VERSION.SDK_INT >= 29) {
            try {
                val resolver = contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "esdispatch_crash.txt")
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                uri?.let {
                    resolver.openOutputStream(it)?.use { os -> os.write(trace.toByteArray()) }
                }
                android.util.Log.i("ESDispatchCrash", "Crash log written to Downloads folder")
            } catch (e: Throwable) {
                android.util.Log.e("ESDispatchCrash", "Could not write Downloads crash file: ${e.message}")
            }
        }
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
