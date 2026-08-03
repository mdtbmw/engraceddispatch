package com.esdispatch.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esdispatch.data.FirebaseManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class AuthViewModel : BaseViewModel() {

    // --- Protected State Flows for Inheritance ---
    protected val _loginMode = MutableStateFlow("free") // free, pin, biometric
    val loginMode: StateFlow<String> = _loginMode.asStateFlow()

    protected val _biometricRegistered = MutableStateFlow(false)
    val biometricRegistered: StateFlow<Boolean> = _biometricRegistered.asStateFlow()

    protected val _biometricEnabled = MutableStateFlow(false)
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled.asStateFlow()

    protected val _phoneVerificationRequired = MutableStateFlow(false)
    val phoneVerificationRequired: StateFlow<Boolean> = _phoneVerificationRequired.asStateFlow()

    protected val _isGoogleAuthInProgress = MutableStateFlow(false)
    val isGoogleAuthInProgress: StateFlow<Boolean> = _isGoogleAuthInProgress.asStateFlow()
    
    protected val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()
    
    protected val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    fun setLoginMode(mode: String) {
        _loginMode.value = mode
    }

    fun setBiometricRegistered(reg: Boolean) {
        _biometricRegistered.value = reg
    }

    fun setBiometricEnabled(en: Boolean) {
        _biometricEnabled.value = en
    }

    fun setGoogleAuthInProgress(value: Boolean) {
        _isGoogleAuthInProgress.value = value
    }

    // Biometric methods are left empty for now to be implemented or overridden by DeliveryViewModel 
    // depending on where SharedPreferences is accessed.
}
