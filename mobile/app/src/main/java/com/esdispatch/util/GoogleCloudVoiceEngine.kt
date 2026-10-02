package com.esdispatch.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Base64
import android.util.Log
import com.esdispatch.BuildConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * Google Cloud Journey Voice Engine
 * Provides studio-grade, human-like voice synthesis using Google Cloud Text-to-Speech
 * with Journey voices (en-US-Journey-F) streamed into an Android MediaPlayer.
 * Fallbacks gracefully to high-quality system neural voice if network/key issues occur.
 */
object GoogleCloudVoiceEngine {
    private const val TAG = "GoogleCloudVoiceEngine"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var mediaPlayer: MediaPlayer? = null
    private var isMuted = false
    private var currentSpeakJob: Job? = null

    private val _isSpeakingFlow = MutableStateFlow(false)
    val isSpeakingFlow: StateFlow<Boolean> = _isSpeakingFlow.asStateFlow()

    fun initialize(context: Context) {
        VoiceGuidanceManager.initialize(context)
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
        if (muted) {
            stop()
        }
        VoiceGuidanceManager.setMuted(muted)
    }

    fun stop() {
        currentSpeakJob?.cancel()
        currentSpeakJob = null
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.reset()
                player.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping media player: ${e.message}")
        }
        VoiceGuidanceManager.stop()
        _isSpeakingFlow.value = false
    }

    fun speak(
        context: Context,
        text: String,
        onDone: (() -> Unit)? = null
    ) {
        if (isMuted) {
            onDone?.invoke()
            return
        }

        val cleanText = text.replace(Regex("\\*\\*|#|_|`"), "").trim()
        if (cleanText.isBlank()) {
            onDone?.invoke()
            return
        }

        stop()

        currentSpeakJob = scope.launch {
            _isSpeakingFlow.value = true
            val ttsKey = BuildConfig.TTS_API_KEY.ifBlank { BuildConfig.GEMINI_API_KEY }

            var success = false
            if (ttsKey.isNotBlank() && !ttsKey.contains("PLACEHOLDER") && ttsKey != "MY_GEMINI_API_KEY") {
                success = synthesizeCloudSpeech(context, cleanText, ttsKey, onDone)
            }

            if (!success) {
                // Smooth fallback to Android neural engine
                withContext(Dispatchers.Main) {
                    VoiceGuidanceManager.speak(cleanText, isUrgent = true) {
                        _isSpeakingFlow.value = false
                        onDone?.invoke()
                    }
                }
            }
        }
    }

    private suspend fun synthesizeCloudSpeech(
        context: Context,
        text: String,
        apiKey: String,
        onDone: (() -> Unit)?
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://texttospeech.googleapis.com/v1/text:synthesize?key=$apiKey"

                val jsonBody = JSONObject().apply {
                    put("input", JSONObject().put("text", text))
                    put("voice", JSONObject().apply {
                        put("languageCode", "en-US")
                        put("name", "en-US-Journey-F")
                        put("ssmlGender", "FEMALE")
                    })
                    put("audioConfig", JSONObject().apply {
                        put("audioEncoding", "MP3")
                        put("speakingRate", 1.0)
                        put("pitch", 0.0)
                    })
                }

                val request = Request.Builder()
                    .url(url)
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = httpClient.newCall(request).execute()
                response.use { resp ->
                    if (!resp.isSuccessful) {
                        Log.w(TAG, "Cloud TTS returned HTTP ${resp.code}: ${resp.body?.string()}")
                        return@withContext false
                    }

                    val bodyStr = resp.body?.string() ?: return@withContext false
                    val jsonResp = JSONObject(bodyStr)
                    val base64Audio = jsonResp.optString("audioContent", "")
                    if (base64Audio.isBlank()) return@withContext false

                    val audioBytes = Base64.decode(base64Audio, Base64.DEFAULT)
                    val tempFile = File(context.cacheDir, "cloud_tts_${System.currentTimeMillis()}.mp3")
                    FileOutputStream(tempFile).use { fos ->
                        fos.write(audioBytes)
                    }

                    withContext(Dispatchers.Main) {
                        playMp3File(tempFile, onDone)
                    }
                    return@withContext true
                }
            } catch (e: Exception) {
                Log.w(TAG, "synthesizeCloudSpeech failed: ${e.message}")
                return@withContext false
            }
        }
    }

    private fun playMp3File(file: File, onDone: (() -> Unit)?) {
        try {
            val player = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener { mp ->
                    mp.reset()
                    mp.release()
                    mediaPlayer = null
                    _isSpeakingFlow.value = false
                    try { file.delete() } catch (_: Exception) {}
                    onDone?.invoke()
                }
                setOnErrorListener { mp, _, _ ->
                    mp.reset()
                    mp.release()
                    mediaPlayer = null
                    _isSpeakingFlow.value = false
                    try { file.delete() } catch (_: Exception) {}
                    onDone?.invoke()
                    true
                }
            }
            mediaPlayer = player
            player.start()
        } catch (e: Exception) {
            Log.w(TAG, "playMp3File error: ${e.message}")
            _isSpeakingFlow.value = false
            try { file.delete() } catch (_: Exception) {}
            onDone?.invoke()
        }
    }

    fun shutdown() {
        stop()
        VoiceGuidanceManager.shutdown()
    }
}
