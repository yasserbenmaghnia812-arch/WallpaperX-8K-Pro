package com.example.ui.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticFeedbackUtil {

    enum class HapticType {
        LIGHT, MEDIUM, HEAVY, SUCCESS
    }

    fun performHaptic(context: Context, type: HapticType = HapticType.LIGHT) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (type) {
                    HapticType.LIGHT -> VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE)
                    HapticType.MEDIUM -> VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                    HapticType.HEAVY -> VibrationEffect.createOneShot(65, VibrationEffect.DEFAULT_AMPLITUDE)
                    HapticType.SUCCESS -> VibrationEffect.createWaveform(longArrayOf(0, 30, 50, 40), -1)
                }
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val millis = when (type) {
                    HapticType.LIGHT -> 15L
                    HapticType.MEDIUM -> 35L
                    HapticType.HEAVY -> 65L
                    HapticType.SUCCESS -> 80L
                }
                vibrator.vibrate(millis)
            }
        } catch (e: Exception) {
            // Ignore if vibration fails or permission denied
        }
    }
}
