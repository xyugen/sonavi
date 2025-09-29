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
        if (a.size != b.size) return 0f

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

        val audioEmbedding = embeddingModel.extractEmbedding(audioInput)

        val similarities = customSounds.mapNotNull { sound ->
            // Parse stored embedding
            val storedEmbedding = parseEmbedding(sound.mfccEmbedding) ?: return@mapNotNull null

            // Calculate similarity
            val similarity = cosineSimilarity(audioEmbedding, storedEmbedding)

            sound to similarity
        }

        return similarities.maxByOrNull { it.second } ?: (null to 0f)
    }

    // Parse embedding from string (stored as JSON array)
    private fun parseEmbedding(embeddingString: String?): FloatArray? {
        if (embeddingString == null) return null

        return try {
            val gson = Gson()
            gson.fromJson(embeddingString, FloatArray::class.java)
        } catch (e: Exception) {
            Log.e("CustomClassifier", "Error parsing embedding", e)
            null
        }
    }

    fun cleanup() {
        embeddingModel.cleanup()
    }
}