package com.esdispatch.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.esdispatch.data.FirebaseManager

import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

open class BaseViewModel : ViewModel() {
    var appContext: Context? = null
    protected val _firebaseUserId = MutableStateFlow<String?>(null)
    val firebaseUserId: StateFlow<String?> = _firebaseUserId.asStateFlow()

    open fun savePref(key: String, value: Any) {
        val ctx = appContext ?: return
        val prefs = ctx.getSharedPreferences("esdispatch_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is String -> putString(key, value)
                is Float -> putFloat(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Double -> putString(key, value.toString())
            }
            apply()
        }
    }

    open fun addNotification(title: String, message: String, parcelId: String = "") {
        // Implementation will be delegated or handled in DeliveryViewModel
    }

    open fun logAdminActivity(action: String, details: String) {
        // Implementation will be delegated or handled in DeliveryViewModel
    }
}
