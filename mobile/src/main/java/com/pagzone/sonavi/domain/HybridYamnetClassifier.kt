package com.pagzone.sonavi.domain

import android.content.Context
import android.util.Log
import com.pagzone.sonavi.data.repository.SoundRepository
import com.pagzone.sonavi.di.AudioClassifierEntryPoint
import com.pagzone.sonavi.model.SoundPreference
import com.pagzone.sonavi.model.SoundProfile
import com.pagzone.sonavi.util.Constants
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Date

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

    // Change to store SoundProfile references instead of just display names
    private val emaScores = mutableMapOf<SoundProfile, Float>()

    init {
        val modelBuffer = context.assets.open("yamnnet.tflite").use { it.readBytes() }
        interpreter = Interpreter(ByteBuffer.allocateDirect(modelBuffer.size).apply {
            order(ByteOrder.nativeOrder())
            put(modelBuffer)
        })

        coroutineScope.launch {
            soundRepository.getAllSounds().collect { value ->
                sounds = value
                // Clear EMA scores when sounds change to avoid stale references
                emaScores.clear()
            }
        }

        labels = context.assets.open("yamnet_labels.txt").bufferedReader().readLines()
    }

    // Return Map<SoundProfile, Float> instead of Map<String, Float>
    private fun mergePredictions(yamnetScores: FloatArray): Map<SoundProfile, Float> {
        val merged = mutableMapOf<SoundProfile, Float>()

        for (sound in sounds) {
            val sum = sound.yamnetIndices.sumOf { yamnetScores[it].toDouble() }
            merged[sound] = sum.toFloat()
        }
        return merged
    }

    // Return Pair<SoundProfile?, Float> instead of Pair<String, Float>
    private fun getTopPrediction(merged: Map<SoundProfile, Float>): Pair<SoundProfile?, Float> {
        val entry = merged.maxByOrNull { it.value }
        return if (entry != null) {
            entry.key to entry.value
        } else {
            null to 0f
        }
    }

    // Update to work with SoundProfile keys
    private fun updateEma(merged: Map<SoundProfile, Float>) {
        for ((soundProfile, score) in merged) {
            val prev = emaScores[soundProfile] ?: score
            emaScores[soundProfile] =
                Constants.Classifier.SMOOTHING_ALPHA * score + (1 - Constants.Classifier.SMOOTHING_ALPHA) * prev
        }
    }

    // Main classification method now returns Pair<SoundProfile?, Float>
    fun classify(audioInput: FloatArray): Pair<SoundProfile?, Float> {
        // 1. Run inference
        val outputScores = Array(1) { FloatArray(labels.size) }
        interpreter.run(arrayOf(audioInput), outputScores)

        // 2. Get allowed sound profiles from repository
        Log.d(
            "HybridYamnetClassifier",
            "Getting allowed prefs: $sounds"
        )
        val allowedSounds = sounds.filter {
            it.isEnabled && (it.snoozedUntil == null || it.snoozedUntil.before(Date()))
        }

        // 3. Merge predictions for allowed sounds only
        val scores = outputScores[0]
        val allMerged = mergePredictions(scores)
        val allowedMerged = allMerged.filterKeys { it in allowedSounds }

        Log.d("HybridYamnetClassifier", "Merged: ${allowedMerged.mapKeys { it.key.displayName }}")

        // 4. Smooth scores with EMA (only for allowed sounds)
        updateEma(allowedMerged)

        // 5. Filter EMA scores to only include allowed sounds
        val filteredScores = emaScores.filterKeys { it in allowedSounds }

        if (filteredScores.isEmpty()) {
            Log.d("HybridYamnetClassifier", "No allowed predictions")
            return null to 0f
        }

        // 6. Get top prediction among allowed sounds
        val (topSoundProfile, topScore) = getTopPrediction(filteredScores)
        Log.d("HybridYamnetClassifier", "Top (filtered): ${topSoundProfile?.displayName} -> $topScore")

        return topSoundProfile to topScore
    }

    // You can add threshold checking methods that work with SoundProfile
    fun meetsThreshold(soundProfile: SoundProfile?, score: Float): Boolean {
        return soundProfile?.let { profile ->
            score >= profile.threshold
        } ?: false
    }

    // Clean up resources
    fun cleanup() {
        coroutineScope.cancel()
        interpreter.close()
    }
}