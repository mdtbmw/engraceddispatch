package com.esdispatch.util

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * Native Android Text-To-Speech engine for turn-by-turn navigation,
 * proximity arrivals, and dispatch voice alerts.
 */
object VoiceGuidanceManager : TextToSpeech.OnInitListener {
    private const val TAG = "VoiceGuidanceManager"
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var isMuted = false
    private var lastSpokenPhrase = ""
    private var lastSpokenTime = 0L

    fun initialize(context: Context) {
        if (tts == null) {
            try {
                tts = TextToSpeech(context.applicationContext, this)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to initialize TextToSpeech: ${e.message}")
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.UK)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to US English
                tts?.setLanguage(Locale.US)
            }
            tts?.setSpeechRate(0.95f) // Natural, intelligible driving cadence
            tts?.setPitch(1.0f)
            isInitialized = true
            Log.d(TAG, "TextToSpeech successfully initialized.")
        } else {
            Log.w(TAG, "TextToSpeech initialization returned status: $status")
            isInitialized = false
        }
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
        if (muted) {
            stop()
        }
    }

    fun isMuted(): Boolean = isMuted

    fun toggleMuted(): Boolean {
        isMuted = !isMuted
        if (isMuted) stop()
        return isMuted
    }

    fun speak(text: String, isUrgent: Boolean = false) {
        if (isMuted || !isInitialized || text.isBlank()) return

        val now = System.currentTimeMillis()
        // Deduplicate repeating guidance within 8 seconds unless urgent
        if (!isUrgent && text.equals(lastSpokenPhrase, ignoreCase = true) && (now - lastSpokenTime < 8000L)) {
            return
        }

        lastSpokenPhrase = text
        lastSpokenTime = now

        try {
            val queueMode = if (isUrgent) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            }
            tts?.speak(text, queueMode, params, "TTS_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Log.w(TAG, "Error speaking text: ${e.message}")
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        isInitialized = false
    }
}
