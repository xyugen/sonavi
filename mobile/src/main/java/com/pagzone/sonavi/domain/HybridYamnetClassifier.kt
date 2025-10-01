package com.pagzone.sonavi.domain

import android.content.Context
import android.util.Log
import com.pagzone.sonavi.data.repository.SoundRepository
import com.pagzone.sonavi.di.AudioClassifierEntryPoint
import com.pagzone.sonavi.model.SoundProfile
import com.pagzone.sonavi.util.Constants
import com.pagzone.sonavi.util.Helper
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
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

    private val customSoundClassifier = CustomSoundClassifier(context)

    data class EnvironmentProfile(
        var avgNoiseLevel: Float = 0f,
        var dominantFreqBands: List<Int> = emptyList(),
        var adaptationFactor: Float = 1.0f
    )

    init {
        val modelBuffer = Helper.loadModelFile(context, "yamnnet.tflite")
        interpreter = Interpreter(modelBuffer)

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

    // Main classify method
    fun classify(audioInput: FloatArray): Pair<SoundProfile?, Float> {
        // Separate built-in and custom sounds
        val allowedSounds = sounds.filter {
            it.isEnabled && (it.snoozedUntil == null || it.snoozedUntil.before(Date()))
        }

        val builtInSounds = allowedSounds.filter { it.isBuiltIn }
        val customSounds = allowedSounds.filter { !it.isBuiltIn && it.mfccEmbedding != null }

        // 1. Classify built-in sounds with YAMNet (existing logic)
        val (builtInSound, builtInScore) = if (builtInSounds.isNotEmpty()) {
            classifyBuiltIn(audioInput, builtInSounds)
        } else {
            null to 0f
        }

        // 2. Classify custom sounds with embeddings
        val (customSound, customScore) = if (customSounds.isNotEmpty()) {
            customSoundClassifier.matchCustomSounds(audioInput, customSounds)
        } else {
            null to 0f
        }

        Log.d("HybridClassifier", "Built-in: ${builtInSound?.displayName} = $builtInScore")
        Log.d("HybridClassifier", "Custom: ${customSound?.displayName} = $customScore")

        // 3. Return highest confidence prediction
        return if (builtInScore > customScore) {
            builtInSound to builtInScore
        } else {
            customSound to customScore
        }
    }

    private fun classifyBuiltIn(audioInput: FloatArray, builtInSounds: List<SoundProfile>): Pair<SoundProfile?, Float> {
        updateEnvironmentProfile(audioInput)

        val outputScores = Array(1) { FloatArray(labels.size) }
        interpreter.run(arrayOf(audioInput), outputScores)

        val scores = outputScores[0]
        val allMerged = mergePredictions(scores)
        val allowedMerged = allMerged.filterKeys { it in builtInSounds }

        updateEma(allowedMerged)

        val filteredScores = emaScores.filterKeys { it in builtInSounds }
        if (filteredScores.isEmpty()) return null to 0f

        val environmentAdjusted = filteredScores.mapValues { (sound, score) ->
            adjustForEnvironment(sound, score)
        }

        return getTopPrediction(environmentAdjusted)
    }
}