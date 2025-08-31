package com.pagzone.sonavi.domain

import android.content.Context
import android.util.Log
import com.pagzone.sonavi.data.repository.SoundPreferencesRepositoryImpl
import com.pagzone.sonavi.model.SoundPreference
import com.pagzone.sonavi.util.Constants
import kotlinx.coroutines.flow.first
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class HybridYamnetClassifier(
    context: Context
) {
    private val interpreter: Interpreter
    private val labels: List<String>
    private val DEFAULT_THRESHOLD = 0.5f
    private val FEWSHOT_THRESHOLD = 0.75f

    private val soundPreferencesRepository = SoundPreferencesRepositoryImpl

    private val emaScores = mutableMapOf<String, Float>()

    init {
        val modelBuffer = context.assets.open("yamnnet.tflite").use { it.readBytes() }
        interpreter = Interpreter(ByteBuffer.allocateDirect(modelBuffer.size).apply {
            order(ByteOrder.nativeOrder())
            put(modelBuffer)
        })

        labels = context.assets.open("yamnet_labels.txt").bufferedReader().readLines()
    }

    private fun mergePredictions(yamnetScores: FloatArray): Map<String, Float> {
        val merged = mutableMapOf<String, Float>()

        for ((customLabel, indices) in soundPreferencesRepository.mergeMap) {
            val sum = indices.sumOf { yamnetScores[it].toDouble() }
            merged[customLabel] = sum.toFloat()
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
            "Getting allowed prefs: ${
                soundPreferencesRepository.getPreferencesFlow(
                    soundPreferencesRepository.mergeMap.keys.toList()
                ).first()
            }"
        )
        val prefs =
            soundPreferencesRepository.getPreferencesFlow(soundPreferencesRepository.mergeMap.keys.toList())
                .first()
        val allowedLabels = prefs
            .filter { it.enabled && !isSnoozed(it) }
            .map { it.label }
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