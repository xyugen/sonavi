package com.pagzone.sonavi.domain

import com.pagzone.sonavi.model.RealtimeQuality
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt

class RealtimeAudioQualityAnalyzer {

    fun analyzeBuffer(audioData: FloatArray): RealtimeQuality {
        if (audioData.isEmpty()) {
            return RealtimeQuality(
                rmsLevel = 0f,
                snr = 0f,
                isPeaking = false,
                isQuiet = false,
                isNoisy = false,
                canUse = false,
                suggestion = "No input"
            )
        }

        val rms = calculateRMS(audioData)
        val peak = audioData.maxOf { abs(it) }
        val zeroCrossRate = calculateZeroCrossRate(audioData)
        val snr = estimateSNR(audioData)

        val isPeaking = peak > 0.95f        // Clipping warning
        val isQuiet = rms < 0.04f           // Too quiet warning
        val isNoisy = zeroCrossRate > 0.25f || snr < 15  // High noise warning (SNR < 15 dB)

        return RealtimeQuality(
            rmsLevel = rms,
            snr = snr,
            isPeaking = isPeaking,
            isQuiet = isQuiet,
            isNoisy = isNoisy,

            // Real-time "usable" flag
            canUse = !isPeaking && !isQuiet && !isNoisy,

            // Feedback message
            suggestion = when {
                isPeaking -> "⚠️ Too loud - audio is clipping"
                isQuiet -> "⚠️ Too quiet - move closer to sound"
                isNoisy -> "⚠️ Too noisy or low SNR - find quieter place"
                rms > 0.15f -> "✓ Excellent level"
                rms > 0.10f -> "✓ Good level"
                else -> "⚠️ Acceptable but quiet"
            }
        ).also {
            android.util.Log.d(
                "RealtimeAudioQualityAnalyzer",
                "rms=$rms, peak=$peak, zcr=$zeroCrossRate, snr=$snr dB, usable=${it.canUse}"
            )
        }
    }

    private fun calculateRMS(audio: FloatArray) =
        sqrt(audio.map { it * it }.average()).toFloat()

    private fun calculateZeroCrossRate(audio: FloatArray): Float {
        var count = 0
        for (i in 1 until audio.size)
            if (audio[i - 1] * audio[i] < 0) count++
        return count.toFloat() / audio.size
    }

    /**
     * Simple SNR estimate:
     * - Assumes noise floor = quietest 20% of samples.
     * - Calculates RMS ratio (signal vs noise).
     */
    private fun estimateSNR(audio: FloatArray): Float {
        val absSorted = audio.map { abs(it) }.sorted()
        val noiseFloor = absSorted.take((audio.size * 0.2).toInt()).average().toFloat()
        val signalPower = calculateRMS(audio)
        val noisePower = noiseFloor.takeIf { it > 0 } ?: 1e-6f
        return (20 * log10(signalPower / noisePower)).coerceIn(-10f, 50f)
    }
}