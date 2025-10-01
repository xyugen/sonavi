package com.pagzone.sonavi.domain

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.pagzone.sonavi.model.SoundProfile
import kotlin.math.sqrt

class CustomSoundClassifier(context: Context) {
    private val embeddingModel = SoundEmbeddingModel(context)

    // Calculate cosine similarity between embeddings
    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        if (a.size != b.size) {
            Log.w("CustomClassifier", "Embedding size mismatch: ${a.size} vs ${b.size}")
            return 0f
        }

        var dotProduct = 0f
        var normA = 0f
        var normB = 0f

        for (i in a.indices) {
            dotProduct += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }

        val denominator = sqrt(normA) * sqrt(normB)
        return if (denominator > 0f) dotProduct / denominator else 0f
    }

    // Match audio against custom sound prototypes
    fun matchCustomSounds(
        audioInput: FloatArray,
        customSounds: List<SoundProfile>
    ): Pair<SoundProfile?, Float> {

        // Extract embedding from input audio
        val audioEmbedding = embeddingModel.extractEmbeddingFromLongAudio(audioInput)

        Log.d("CustomClassifier", "Input embedding size: ${audioEmbedding.size}")

        // Calculate similarities with all custom sounds
        val similarities = customSounds.mapNotNull { sound ->
            // Parse stored embedding (now stores the prototype/average)
            val storedEmbedding = parseEmbedding(sound.mfccEmbedding) ?: run {
                Log.w("CustomClassifier", "Failed to parse embedding for: ${sound.name}")
                return@mapNotNull null
            }

            // Calculate similarity
            val similarity = cosineSimilarity(audioEmbedding, storedEmbedding)

            Log.d("CustomClassifier",
                "Sound: ${sound.name}, Similarity: $similarity, Threshold: ${sound.threshold}")

            sound to similarity
        }

        // Find best match
        val bestMatch = similarities.maxByOrNull { it.second }

        return if (bestMatch != null && bestMatch.second >= bestMatch.first.threshold) {
            Log.d("CustomClassifier", "Match found: ${bestMatch.first.name} (${bestMatch.second})")
            bestMatch
        } else {
            Log.d("CustomClassifier", "No match above threshold")
            null to 0f
        }
    }

    // Match with detailed results for all sounds (useful for debugging/tuning)
    fun matchCustomSoundsDetailed(
        audioInput: FloatArray,
        customSounds: List<SoundProfile>
    ): List<Triple<SoundProfile, Float, Boolean>> {

        val audioEmbedding = embeddingModel.extractEmbeddingFromLongAudio(audioInput)

        return customSounds.mapNotNull { sound ->
            val storedEmbedding = parseEmbedding(sound.mfccEmbedding) ?: return@mapNotNull null
            val similarity = cosineSimilarity(audioEmbedding, storedEmbedding)
            val isMatch = similarity >= sound.threshold

            Triple(sound, similarity, isMatch)
        }.sortedByDescending { it.second }
    }

    // Parse embedding from JSON string
    private fun parseEmbedding(embeddingString: String?): FloatArray? {
        if (embeddingString.isNullOrBlank()) return null

        return try {
            val gson = Gson()
            gson.fromJson(embeddingString, FloatArray::class.java)
        } catch (e: Exception) {
            Log.e("CustomClassifier", "Error parsing embedding", e)
            null
        }
    }
}