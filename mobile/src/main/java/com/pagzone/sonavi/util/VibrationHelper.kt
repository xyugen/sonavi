package com.pagzone.sonavi.util

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator

class VibrationHelper(context: Context) {
    private val vibrator = context.getSystemService(Vibrator::class.java)

    fun vibrate(vibrationEffect: VibrationEffect) {
        vibrator.vibrate(vibrationEffect)
    }

    fun cancel() {
        vibrator.cancel()
    }
}