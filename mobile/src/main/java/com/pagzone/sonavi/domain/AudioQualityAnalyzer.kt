package com.pagzone.sonavi.domain

import com.pagzone.sonavi.model.AudioQuality
import com.pagzone.sonavi.model.QualityLevel
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sqrt

class AudioQualityAnalyzer {

    fun analyzeQuality(audioData: FloatArray): AudioQuality {
        val rms = calculateRMS(audioData)
        val snr = estimateSNR(audioData)
        val duration = audioData.size / 16000f

        val quality = when {
            rms > 0.15f && snr > 15f -> QualityLevel.EXCELLENT
            rms > 0.08f && snr > 10f -> QualityLevel.GOOD
            rms > 0.04f && snr > 5f -> QualityLevel.FAIR
            else -> QualityLevel.POOR
        }

        return AudioQuality(
            rmsLevel = rms,
            snr = snr,
            duration = duration,
            quality = quality
        )
    }

    private fun calculateRMS(audio: FloatArray): Float {
        return sqrt(audio.map { it * it }.average()).toFloat()
    }

    private fun estimateSNR(audio: FloatArray): Float {
        val frameSize = 512
        val frames = audio.toList().chunked(frameSize)

        val rmsValues = frames.map { frame ->
            sqrt(frame.map { it * it }.average()).toFloat()
        }.sorted()

        // Estimate noise from quietest 20% of frames
        val noiseFrameCount = max(1, (rmsValues.size * 0.2).toInt())
        val noiseLevel = rmsValues.take(noiseFrameCount).average().toFloat()

        // Estimate signal from loudest 20% of frames
        val signalFrameCount = max(1, (rmsValues.size * 0.2).toInt())
        val signalLevel = rmsValues.takeLast(signalFrameCount).average().toFloat()

        // Calculate SNR in dB
        return if (noiseLevel > 0) {
            20 * log10(signalLevel / noiseLevel)
        } else {
            30f // High SNR if no noise detected
        }
    }
}