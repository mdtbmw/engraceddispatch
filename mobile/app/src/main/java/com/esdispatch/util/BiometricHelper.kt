package com.esdispatch.util

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {

    /**
     * Authenticators set supporting Class 3 (Strong), Class 2 (Weak / Optical sensors e.g. Samsung A-series),
     * and Device Credentials (PIN / Pattern / Password).
     */
    fun getSupportedAuthenticators(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        }
    }

    /**
     * Check authentication capability.
     */
    fun canAuthenticate(context: Context): Int {
        val biometricManager = BiometricManager.from(context)
        val authenticators = getSupportedAuthenticators()
        return biometricManager.canAuthenticate(authenticators)
    }

    /**
     * Returns true if biometrics or device credentials are operational and ready.
     */
    fun isBiometricAvailable(context: Context): Boolean {
        val status = canAuthenticate(context)
        return status == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Check if hardware exists even if not enrolled yet.
     */
    fun hasBiometricHardware(context: Context): Boolean {
        val status = canAuthenticate(context)
        return status != BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE &&
               status != BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE
    }

    fun getStatusMessage(status: Int): String {
        return when (status) {
            BiometricManager.BIOMETRIC_SUCCESS -> "Ready & Enrolled"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "No biometric or device PIN enrolled"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No biometric sensor available"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Sensor currently busy or unavailable"
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "Security update required"
            else -> "Biometric authentication unsupported"
        }
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String = "Biometric Verification",
        subtitle: String = "Verify identity to proceed",
        description: String = "Place your finger on the sensor or use your lock screen PIN",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val biometricManager = BiometricManager.from(activity)
        val authenticators = getSupportedAuthenticators()

        when (val status = biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val executor = ContextCompat.getMainExecutor(activity)
                val biometricPrompt = BiometricPrompt(
                    activity,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                                errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                                onError("$errString")
                            }
                        }

                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            onSuccess()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            onError("Biometric not recognized. Please try again.")
                        }
                    }
                )

                val promptBuilder = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setDescription(description)

                // If DEVICE_CREDENTIAL is supported, do not call setNegativeButtonText
                if ((authenticators and BiometricManager.Authenticators.DEVICE_CREDENTIAL) != 0) {
                    promptBuilder.setAllowedAuthenticators(authenticators)
                } else {
                    promptBuilder.setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
                    )
                    promptBuilder.setNegativeButtonText("Cancel")
                }

                try {
                    biometricPrompt.authenticate(promptBuilder.build())
                } catch (e: Exception) {
                    onError("Failed to start biometric prompt: ${e.message}")
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                onError("No biometric features available on this device.")
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                onError("Biometric sensor is currently unavailable.")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                onError("Please enroll your fingerprint or screen lock in device settings first.")
            }
            else -> {
                onError(getStatusMessage(status))
            }
        }
    }
}
