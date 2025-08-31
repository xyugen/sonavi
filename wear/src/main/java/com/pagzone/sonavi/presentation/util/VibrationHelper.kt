package com.pagzone.sonavi.presentation.util

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator

class VibrationHelper(context: Context) {
    private var vibrator: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    private val lastDetectionTimes = mutableMapOf<String, Long>()

    fun vibrate(vibrationEffect: VibrationEffect) {
        vibrator.vibrate(vibrationEffect)
    }

    fun shouldTrigger(label: String): Boolean {
        val now = System.currentTimeMillis()
        val lastTime = lastDetectionTimes[label] ?: 0L

        return if (now - lastTime > Constants.Vibration.COOLDOWN_MS) {
            lastDetectionTimes[label] = now
            true
        } else {
            false
        }
    }
}