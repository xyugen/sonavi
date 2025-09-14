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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

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
    private val DEFAULT_THRESHOLD = 0.5f
    private val FEWSHOT_THRESHOLD = 0.75f

    private val emaScores = mutableMapOf<String, Float>()

    init {
        val modelBuffer = context.assets.open("yamnnet.tflite").use { it.readBytes() }
        interpreter = Interpreter(ByteBuffer.allocateDirect(modelBuffer.size).apply {
            order(ByteOrder.nativeOrder())
            put(modelBuffer)
        })

        coroutineScope.launch {
            soundRepository.getAllSounds().collect { value ->
                sounds = value
            }
        }

        labels = context.assets.open("yamnet_labels.txt").bufferedReader().readLines()
    }

    private fun mergePredictions(yamnetScores: FloatArray): Map<String, Float> {
        val merged = mutableMapOf<String, Float>()

        for (sound in sounds) {
            val sum = sound.yamnetIndices.sumOf { yamnetScores[it].toDouble() }
            merged[sound.displayName] = sum.toFloat()
        }
        return merged
    }

    private fun getTopPrediction(merged: Map<String, Float>): Pair<String, Float> {
        val (label, score) = merged.maxByOrNull { it.value } ?: return "Unknown" to 0f
        return label to score
    }

    private fun updateEma(merged: Map<String, Float>) {
        for ((label, score) in merged) {
            val prev = emaScores[label] ?: score
            emaScores[label] =
                Constants.Classifier.SMOOTHING_ALPHA * score + (1 - Constants.Classifier.SMOOTHING_ALPHA) * prev
        }
    }

    suspend fun classify(audioInput: FloatArray): Pair<String, Float> {
        // 1. Run inference
        val outputScores = Array(1) { FloatArray(labels.size) }
        interpreter.run(arrayOf(audioInput), outputScores)

        // 2. Get allowed labels from prefs
        Log.d(
            "HybridYamnetClassifier",
            "Getting allowed prefs: $sounds"
        )
        val allowedLabels = sounds
            .filter { it.isEnabled } // TODO: Snoozed
            .map { it.displayName }
            .toSet()

        // 3. Merge predictions (if you’re grouping multiple YAMNet labels into one)
        val scores = outputScores[0]
        val merged = mergePredictions(scores)
        Log.d("HybridYamnetClassifier", "Merged: $merged")

        // 4. Smooth scores with EMA
        updateEma(merged)

        // 5. Apply filtering: only keep allowed labels
        val filteredScores = emaScores.filterKeys { it in allowedLabels }

        if (filteredScores.isEmpty()) {
            Log.d("HybridYamnetClassifier", "No allowed predictions")
            return "" to 0f
        }

        // 6. Get top prediction among allowed
        val top = getTopPrediction(filteredScores)
        Log.d("HybridYamnetClassifier", "Top (filtered): $top")

        return top
    }

    private fun isSnoozed(pref: SoundPreference): Boolean {
        return pref.snoozedUntil?.let { System.currentTimeMillis() < it } ?: false
    }
}