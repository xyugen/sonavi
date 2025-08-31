package com.pagzone.sonavi.domain

import android.content.Context
import android.util.Log
import com.pagzone.sonavi.util.Constants
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.collections.iterator

class HybridYamnetClassifier(context: Context) {
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

        labels = context.assets.open("yamnet_labels.txt").bufferedReader().readLines()
    }

    private fun mergePredictions(yamnetScores: FloatArray): Map<String, Float> {
        val merged = mutableMapOf<String, Float>()

        // Group duplicate or similar YAMNet labels
        val mergeMap: Map<String, List<Int>> = mapOf(
            // Human vocalizations
            "Speech" to listOf(0),
            "Shout" to listOf(6, 9, 11), // Shout, Yell, Screaming
            "Children shouting" to listOf(10),
            "Baby cry" to listOf(19),
            "Groan" to listOf(33),
            "Growling" to listOf(74),
            "Caterwaul" to listOf(80),

            // Animals
            "Snake" to listOf(129),
            "Rattle" to listOf(130),

            // Vehicles & traffic
            "Bicycle bell" to listOf(198),
            "Thunder" to listOf(281),
            "Fire" to listOf(292, 293), // Fire + Crackle
            "Vehicle" to listOf(294),
            "Motorcycle (road)" to listOf(300, 320), // Road + General
            "Car" to listOf(301),
            "Vehicle horn" to listOf(302, 312), // Car horn + Truck/Air horn
            "Car alarm" to listOf(304),
            "Skidding" to listOf(306, 307), // Skidding + Tire squeal
            "Reversing beep" to listOf(313),
            "Emergency vehicle" to listOf(316),
            "Police car siren" to listOf(317),
            "Ambulance siren" to listOf(318),
            "Fire truck siren" to listOf(319),
            "Train horn" to listOf(324, 325), // Whistle + Horn

            // Household
            "Doorbell" to listOf(349),
            "Siren" to listOf(390, 391), // General + Civil defense
            "Buzzer" to listOf(392),
            "Smoke alarm" to listOf(393),
            "Fire alarm" to listOf(395),

            // Explosives / Impact
            "Explosion" to listOf(420),
            "Gunshot" to listOf(421, 422, 423, 424, 425),
            "Eruption" to listOf(429),
            "Boom" to listOf(430),
            "Crack" to listOf(434),
            "Glass breaking" to listOf(435, 437, 464), // Glass, Shatter, Breaking
            "Bang" to listOf(460),
            "Crushing" to listOf(473),
            "Beep" to listOf(475),
            "Clitter" to listOf(483),
        )

        for ((customLabel, indices) in mergeMap) {
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
            emaScores[label] = Constants.Classifier.SMOOTHING_ALPHA * score + (1 - Constants.Classifier.SMOOTHING_ALPHA) * prev
        }
    }

    fun classify(audioInput: FloatArray): Pair<String, Float> {
        val inputBuffer = audioInput

        // Run YAMNet
        val outputScores = Array(1) { FloatArray(labels.size) }
        interpreter.run(arrayOf(inputBuffer), outputScores)

        // Get prediction scores
        val scores = outputScores[0]
        val merged = mergePredictions(scores)
        Log.d("HybridYamnetClassifier", "Merged: $merged")

        // Apply Exponential Moving Average to smoothen prediction
        updateEma(merged)

        // Get top prediction
        val top = getTopPrediction(emaScores)
        Log.d("HybridYamnetClassifier", "Top: $top")
        return top
//        return labels[maxIndex] to scores[maxIndex]
//
//        // 1️⃣ Default YAMNet prediction
//        val maxIdx = scores.indices.maxByOrNull { scores[it] } ?: 0
//        val defaultLabel = labels[maxIdx]
//        val defaultConfidence = scores[maxIdx]
//
//        // 2️⃣ Few-shot prediction (if available)
//        val fewShotResult = if (protoStore.examples.isNotEmpty()) {
//            classifyFewShot(embedding)
//        } else null
//
//        // 3️⃣ Decide final output
//        return when {
//            fewShotResult != null && fewShotResult.second >= FEWSHOT_THRESHOLD -> fewShotResult
//            defaultConfidence >= DEFAULT_THRESHOLD -> defaultLabel to defaultConfidence
//            else -> "Unknown" to 0f
//        }
    }

//    private fun classifyFewShot(embedding: FloatArray): Pair<String, Float> {
//        var bestLabel = "Unknown"
//        var bestScore = 0f
//
//        for ((label, storedEmbeddings) in protoStore.examples) {
//            val avgEmbedding =
//                storedEmbeddings.reduce { acc, e -> acc.zip(e) { a, b -> a + b }.toFloatArray() }
//                    .map { it / storedEmbeddings.size }.toFloatArray()
//
//            val similarity = cosineSimilarity(embedding, avgEmbedding)
//            if (similarity > bestScore) {
//                bestScore = similarity
//                bestLabel = label
//            }
//        }
//        return bestLabel to bestScore
//    }
}