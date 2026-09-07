package com.esdispatch.util

import com.esdispatch.viewmodel.ToastData
import com.esdispatch.viewmodel.ToastType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Universal Custom Luxury Toast Bridge
 * Dispatches structured, branded toast events across all Compose screens and activities
 * with synchronized Web Audio synthesizer tones.
 */
object CustomToastBridge {
    private val _toastFlow = MutableSharedFlow<ToastData>(extraBufferCapacity = 50)
    val toastFlow: SharedFlow<ToastData> = _toastFlow.asSharedFlow()

    fun show(message: String, type: ToastType = ToastType.INFO) {
        when (type) {
            ToastType.SUCCESS -> SoundManager.playSuccessArpeggio()
            ToastType.ERROR -> SoundManager.playErrorBuzz()
            ToastType.WARNING -> SoundManager.playGeofencePing()
            ToastType.INFO -> SoundManager.playClick()
        }
        _toastFlow.tryEmit(ToastData(message = message, type = type, id = System.nanoTime()))
    }
}
