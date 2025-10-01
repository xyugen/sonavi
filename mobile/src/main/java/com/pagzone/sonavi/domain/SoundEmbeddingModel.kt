package com.pagzone.sonavi.domain

import android.content.Context
import android.util.Log
import com.pagzone.sonavi.util.Helper
import org.tensorflow.lite.Interpreter
import kotlin.math.sqrt

class SoundEmbeddingModel(context: Context) {
    private val interpreter: Interpreter

    init {
        val modelBuffer = Helper.loadModelFile(context, "yamnet_embedding.tflite")
        interpreter = Interpreter(modelBuffer)
    }

    fun extractEmbedding(audioInput: FloatArray): FloatArray {
        // YAMNet typically works with ~0.96 seconds of audio
        // At 16kHz, that's 15360 samples (but 16000 also works)
        val targetLength = 15600  // YAMNet's preferred input length

        // Pad or trim audio to target length
        val processedAudio = when {
            audioInput.size < targetLength -> {
                // Pad with zeros if too short
                audioInput + FloatArray(targetLength - audioInput.size)
            }

            audioInput.size > targetLength -> {
                // Trim if too long
                audioInput.copyOfRange(0, targetLength)
            }

            else -> audioInput
        }

        // Normalize audio to [-1, 1] range (important for YAMNet!)
        val maxAbs = processedAudio.maxOfOrNull { kotlin.math.abs(it) } ?: 1f
        val normalizedAudio = if (maxAbs > 0) {
            processedAudio.map { it / maxAbs }.toFloatArray()
        } else {
            processedAudio
        }

        Log.d("YAMNetEmbedding", "Input audio length: ${normalizedAudio.size}")
        Log.d(
            "YAMNetEmbedding",
            "Audio range: [${normalizedAudio.minOrNull()}, ${normalizedAudio.maxOrNull()}]"
        )

        // Run through YAMNet embedding model
        // Output shape will be [num_frames, 1024]
        // For ~1 second of audio, num_frames will be 1
        val outputArray = Array(1) { FloatArray(1024) }

        interpreter.run(normalizedAudio, outputArray)

        val embedding = outputArray[0]

        // DEBUG: Check if embedding is meaningful
        val mean = embedding.average()
        val stdDev = sqrt(embedding.map { (it - mean) * (it - mean) }.average())
        val min = embedding.minOrNull() ?: 0f
        val max = embedding.maxOrNull() ?: 0f

        Log.d("YAMNetEmbedding", "Embedding stats:")
        Log.d("YAMNetEmbedding", "  Mean: $mean, StdDev: $stdDev")
        Log.d("YAMNetEmbedding", "  Range: [$min, $max]")
        Log.d(
            "YAMNetEmbedding",
            "  First 10 values: ${embedding.take(10).joinToString { "%.4f".format(it) }}"
        )

        return embedding
    }

    // Helper function for longer audio (multiple seconds)
    fun extractEmbeddingFromLongAudio(audioInput: FloatArray): FloatArray {
        // For audio longer than 1 second, you have options:

        // Option 1: Average embeddings from multiple chunks
        val chunkSize = 15600
        val embeddings = mutableListOf<FloatArray>()

        var offset = 0
        while (offset + chunkSize <= audioInput.size) {
            val chunk = audioInput.copyOfRange(offset, offset + chunkSize)
            embeddings.add(extractEmbedding(chunk))
            // TODO: Test overlap percentage accuracy and efficiency
            offset += chunkSize / 4  // 75% overlap
        }

        // Average all embeddings
        if (embeddings.isEmpty()) {
            return extractEmbedding(audioInput)
        }

        val avgEmbedding = FloatArray(embeddings[0].size)
        for (emb in embeddings) {
            for (i in avgEmbedding.indices) {
                avgEmbedding[i] += emb[i]
            }
        }
        for (i in avgEmbedding.indices) {
            avgEmbedding[i] /= embeddings.size
        }

        Log.d("YAMNetEmbedding", "Averaged ${embeddings.size} chunks")
        return avgEmbedding
    }

    fun cleanup() {
        interpreter.close()
    }
}