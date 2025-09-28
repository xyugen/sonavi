package com.pagzone.sonavi.domain

import android.content.Context
import android.util.Log
import com.pagzone.sonavi.data.repository.SoundRepository
import com.pagzone.sonavi.di.AudioClassifierEntryPoint
import com.pagzone.sonavi.model.SoundProfile
import com.pagzone.sonavi.util.Constants
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Date
import kotlin.math.sqrt

class HybridYamnetClassifier(
    context: Context
) {
    private val soundRepository: SoundRepository by lazy {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AudioClassifierEntryPoint::class.java
        )
        hiltEntryPoint.getSoundRepository()
    }

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var sounds: List<SoundProfile> = emptyList()

    private val interpreter: Interpreter
    private val labels: List<String>

    private val emaScores = mutableMapOf<SoundProfile, Float>()

    // Environment awareness
    private var environmentProfile = EnvironmentProfile()

    data class EnvironmentProfile(
        var avgNoiseLevel: Float = 0f,
        var dominantFreqBands: List<Int> = emptyList(),
        var adaptationFactor: Float = 1.0f
    )

    init {
        val modelBuffer = context.assets.open("yamnnet.tflite").use { it.readBytes() }
        interpreter = Interpreter(ByteBuffer.allocateDirect(modelBuffer.size).apply {
            order(ByteOrder.nativeOrder())
            put(modelBuffer)
        })

        coroutineScope.launch {
            soundRepository.getAllSounds().collect { value ->
                sounds = value
                emaScores.clear()
            }
        }

        labels = context.assets.open("yamnet_labels.txt").bufferedReader().readLines()
    }

    private fun mergePredictions(yamnetScores: FloatArray): Map<SoundProfile, Float> {
        val merged = mutableMapOf<SoundProfile, Float>()

        for (sound in sounds) {
            if (sound.yamnetIndices.isEmpty()) {
                merged[sound] = 0f
                continue
            }

            // Get individual scores for this sound's indices
            val indexScores = sound.yamnetIndices.map { yamnetScores[it] }

            // Use top 2-3 scores with simple weighting
            val sortedScores = indexScores.sortedDescending()
            val topScore = sortedScores[0]
            val secondScore = sortedScores.getOrNull(1) ?: 0f
            val thirdScore = sortedScores.getOrNull(2) ?: 0f

            // Simple weighted combination: 85% top + 7.5% second + 2.5% third
            merged[sound] = topScore * 0.9f + secondScore * 0.075f + thirdScore * 0.025f
        }

        return merged
    }

    private fun getTopPrediction(merged: Map<SoundProfile, Float>): Pair<SoundProfile?, Float> {
        val entry = merged.maxByOrNull { it.value }
        return if (entry != null) entry.key to entry.value else null to 0f
    }

    private fun updateEma(merged: Map<SoundProfile, Float>) {
        for ((soundProfile, score) in merged) {
            val prev = emaScores[soundProfile] ?: score
            emaScores[soundProfile] =
                Constants.Classifier.SMOOTHING_ALPHA * score +
                        (1 - Constants.Classifier.SMOOTHING_ALPHA) * prev
        }
    }

    // Update environment profile
    private fun updateEnvironmentProfile(audio: FloatArray) {
        val rms = sqrt(audio.map { it * it }.average()).toFloat()
        environmentProfile.avgNoiseLevel =
            0.1f * rms + 0.9f * environmentProfile.avgNoiseLevel

        Log.d(
            "HybridYamnetClassifier",
            "Updated envProfile: avgNoise=${environmentProfile.avgNoiseLevel}"
        )
    }

    // Adjust scores depending on environment
    private fun adjustForEnvironment(sound: SoundProfile, score: Float): Float {
        val noisyBoost = when {
            environmentProfile.avgNoiseLevel > 0.3f && sound.isCritical -> 1.2f
            environmentProfile.avgNoiseLevel > 0.5f -> 0.8f
            else -> 1.0f
        }
        return score * noisyBoost
    }

    // 🔹 Main classify method
    fun classify(audioInput: FloatArray): Pair<SoundProfile?, Float> {
        // 1. Update environment stats
        updateEnvironmentProfile(audioInput)

        // 2. Run inference
        val outputScores = Array(1) { FloatArray(labels.size) }
        interpreter.run(arrayOf(audioInput), outputScores)

        val allowedSounds = sounds.filter {
            it.isEnabled && (it.snoozedUntil == null || it.snoozedUntil.before(Date()))
        }

        val scores = outputScores[0]
        val allMerged = mergePredictions(scores)
        val allowedMerged = allMerged.filterKeys { it in allowedSounds }

        Log.d("HybridYamnetClassifier", "Merged: ${allowedMerged.mapKeys { it.key.displayName }}")

        // 3. EMA smoothing
        updateEma(allowedMerged)

        val filteredScores = emaScores.filterKeys { it in allowedSounds }
        if (filteredScores.isEmpty()) {
            Log.d("HybridYamnetClassifier", "No allowed predictions")
            return null to 0f
        }

        // 4. Apply environment adjustments
        val environmentAdjusted = filteredScores.mapValues { (sound, score) ->
            adjustForEnvironment(sound, score)
        }

        // 5. Get final top prediction
        val (topSoundProfile, topScore) = getTopPrediction(environmentAdjusted)
        Log.d(
            "HybridYamnetClassifier",
            "Top (env adjusted): ${topSoundProfile?.displayName} -> $topScore"
        )

        return topSoundProfile to topScore
    }
}