package com.esdispatch.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Pure native audio synthesizer and haptic feedback controller for ESDispatch.
 * Zero external .mp3 dependencies. Generates dynamic PCM waveforms with exponential
 * decay envelopes using Android AudioTrack.
 */
object SoundManager {
    private const val TAG = "SoundManager"
    private const val SAMPLE_RATE = 44100
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var soundEnabled: Boolean = true
    private var hapticsEnabled: Boolean = true
    private var vibrator: Vibrator? = null

    fun initialize(context: Context) {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize vibrator: ${e.message}")
        }
    }

    fun setPreferences(sound: Boolean, haptics: Boolean) {
        soundEnabled = sound
        hapticsEnabled = haptics
    }

    /**
     * Crisp, tactile click pop for tabs, button presses, and selection chips.
     */
    fun playClick() {
        triggerHaptic(HapticType.LIGHT_CLICK)
        if (!soundEnabled) return

        scope.launch {
            val durationMs = 35
            val numSamples = (SAMPLE_RATE * durationMs) / 1000
            val pcm = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                // 800Hz pitch-sliding downward to 350Hz with sharp decay
                val freq = 800.0 - (450.0 * (i.toDouble() / numSamples))
                val env = exp(-t * 80.0)
                val sample = sin(2.0 * PI * freq * t) * env * 0.4
                pcm[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(pcm)
        }
    }

    /**
     * Silky smooth upward frequency sweep for dispatch broadcast / booking creation.
     */
    fun playDispatchSweep() {
        triggerHaptic(HapticType.MEDIUM_IMPACT)
        if (!soundEnabled) return

        scope.launch {
            val durationMs = 180
            val numSamples = (SAMPLE_RATE * durationMs) / 1000
            val pcm = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                // 350Hz rising smoothly to 1100Hz
                val progress = i.toDouble() / numSamples
                val freq = 350.0 + (750.0 * progress * progress)
                val env = sin(PI * progress) // smooth parabolic envelope
                val sample = sin(2.0 * PI * freq * t) * env * 0.5
                pcm[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(pcm)
        }
    }

    /**
     * 3-note harmonic arpeggio (C6, E6, G6) for successful payment, delivery completion, or wallet funding.
     */
    fun playSuccessArpeggio() {
        triggerHaptic(HapticType.SUCCESS_DOUBLE)
        if (!soundEnabled) return

        scope.launch {
            val noteDurationMs = 70
            val notes = listOf(1046.50, 1318.51, 1567.98) // C6, E6, G6
            val samplesPerNote = (SAMPLE_RATE * noteDurationMs) / 1000
            val totalSamples = samplesPerNote * notes.size
            val pcm = ShortArray(totalSamples)

            notes.forEachIndexed { noteIdx, freq ->
                val offset = noteIdx * samplesPerNote
                for (i in 0 until samplesPerNote) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val progress = i.toDouble() / samplesPerNote
                    val env = exp(-progress * 4.0)
                    val sample = sin(2.0 * PI * freq * t) * env * 0.45
                    pcm[offset + i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
            playPcm(pcm)
        }
    }

    /**
     * Soft low dual-tone buzz for validation failure or incorrect PIN.
     */
    fun playErrorBuzz() {
        triggerHaptic(HapticType.ERROR_PULSE)
        if (!soundEnabled) return

        scope.launch {
            val durationMs = 160
            val numSamples = (SAMPLE_RATE * durationMs) / 1000
            val pcm = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                val env = exp(-t * 15.0)
                // Dissonant dual tone 180Hz + 235Hz
                val tone1 = sin(2.0 * PI * 180.0 * t)
                val tone2 = sin(2.0 * PI * 235.0 * t)
                val sample = ((tone1 + tone2) * 0.5) * env * 0.5
                pcm[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(pcm)
        }
    }

    /**
     * Crystalline ping for arrival detection and proximity alerts.
     */
    fun playGeofencePing() {
        triggerHaptic(HapticType.MEDIUM_IMPACT)
        if (!soundEnabled) return

        scope.launch {
            val durationMs = 140
            val numSamples = (SAMPLE_RATE * durationMs) / 1000
            val pcm = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                val env = exp(-t * 22.0)
                val sample = sin(2.0 * PI * 1760.0 * t) * env * 0.4
                pcm[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(pcm)
        }
    }

    private fun playPcm(pcm: ShortArray) {
        var track: AudioTrack? = null
        try {
            val bufferSize = pcm.size * 2
            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcm, 0, pcm.size)
            track.play()
            val sleepMs = ((pcm.size.toDouble() / SAMPLE_RATE) * 1000).toLong() + 30
            Thread.sleep(sleepMs)
        } catch (e: Exception) {
            Log.w(TAG, "AudioTrack playback error: ${e.message}")
        } finally {
            try {
                track?.stop()
                track?.release()
            } catch (_: Exception) {}
        }
    }

    private fun triggerHaptic(type: HapticType) {
        if (!hapticsEnabled) return
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                when (type) {
                    HapticType.LIGHT_CLICK -> {
                        vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                    }
                    HapticType.MEDIUM_IMPACT -> {
                        vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    }
                    HapticType.SUCCESS_DOUBLE -> {
                        vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                    }
                    HapticType.ERROR_PULSE -> {
                        vib.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 50, 40), -1))
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                when (type) {
                    HapticType.LIGHT_CLICK -> vib.vibrate(15)
                    HapticType.MEDIUM_IMPACT -> vib.vibrate(35)
                    HapticType.SUCCESS_DOUBLE -> vib.vibrate(longArrayOf(0, 30, 40, 30), -1)
                    HapticType.ERROR_PULSE -> vib.vibrate(longArrayOf(0, 50, 50, 50), -1)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Haptic vibration error: ${e.message}")
        }
    }

    enum class HapticType {
        LIGHT_CLICK,
        MEDIUM_IMPACT,
        SUCCESS_DOUBLE,
        ERROR_PULSE
    }
}
