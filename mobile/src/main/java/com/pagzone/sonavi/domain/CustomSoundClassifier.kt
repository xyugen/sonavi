package com.pagzone.sonavi.domain

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pagzone.sonavi.model.SoundProfile
import kotlin.math.sqrt

class CustomSoundClassifier(context: Context) {
    private val embeddingModel = SoundEmbeddingModel(context)

    // Cache: SoundProfile.id -> List<normalized FloatArray>
    private val prototypeCache = mutableMapOf<Long, List<FloatArray>>()

    // Utility: L2-normalize in-place and return false on zero-norm
    private fun l2Normalize(vec: FloatArray): Boolean {
        var sum = 0f
        for (v in vec) sum += v * v
        val norm = sqrt(sum)
        if (norm <= 1e-8f) return false
        for (i in vec.indices) vec[i] = vec[i] / norm
        return true
    }

    // Fast cosine on already-normalized vectors -> just dot product
    private fun cosineNormalized(a: FloatArray, b: FloatArray): Float {
        // assumes both are L2-normalized
        var dot = 0f
        for (i in a.indices) dot += a[i] * b[i]
        return dot.coerceIn(-1f, 1f)
    }

    // Parse stored embeddings into normalized prototypelist (cache them)
    private fun getPrototypesForSound(sound: SoundProfile): List<FloatArray> {
        val id = sound.id
        val cached = prototypeCache[id]
        if (cached != null) return cached

        val parsed = parseEmbeddingsList(sound.mfccEmbedding) // returns List<FloatArray> or empty
        val normalized = parsed.mapNotNull { emb ->
            // clone so we don't mutate the DB object if needed
            val cl = emb.copyOf()
            if (l2Normalize(cl)) cl else null
        }.ifEmpty {
            // fallback: if parsing failed, keep empty (classifier will skip)
            emptyList()
        }

        prototypeCache[id] = normalized
        return normalized
    }

    // Main matching: returns (SoundProfile?, similarity)
    fun matchCustomSounds(
        audioInput: FloatArray,
        customSounds: List<SoundProfile>
    ): Pair<SoundProfile?, Float> {
        // 1) Extract embedding (should be a pooled embedding for the full audio)
        val audioEmbedding = embeddingModel.extractEmbeddingFromLongAudio(audioInput)

        if (!l2Normalize(audioEmbedding)) {
            Log.w("CustomClassifier", "Input embedding has zero norm")
            return null to 0f
        }

        var bestSound: SoundProfile? = null
        var bestSim = -1f

        for (sound in customSounds) {
            val prototypes = getPrototypesForSound(sound)
            if (prototypes.isEmpty()) {
                Log.w("CustomClassifier", "No prototypes for sound: ${sound.name}")
                continue
            }

            // compute max similarity across prototypes (robust to variants)
            var localBest = -1f
            for (p in prototypes) {
                // assume prototypes are pre-normalized
                val sim = cosineNormalized(audioEmbedding, p)
                if (sim > localBest) localBest = sim
            }

            Log.d("CustomClassifier", "Sound=${sound.name}, bestSim=$localBest, threshold=${sound.threshold}")

            // store best overall
            if (localBest > bestSim) {
                bestSim = localBest
                bestSound = sound
            }
        }

        if (bestSound != null && bestSim >= bestSound.threshold) {
            Log.d("CustomClassifier", "Match: ${bestSound.name} sim=$bestSim")
            return bestSound to bestSim
        }

        Log.d("CustomClassifier", "No custom match above threshold")
        return null to 0f
    }

    // Example parser — adapt to actual DB format (JSON, CSV, etc.)
    private fun parseEmbeddingsList(serialized: String?): List<FloatArray> {
        if (serialized.isNullOrBlank()) return emptyList()

        return try {
            val gson = Gson()
            val type = object : TypeToken<List<FloatArray>>() {}.type
            gson.fromJson<List<FloatArray>>(serialized, type) ?: emptyList()
        } catch (e: Exception) {
            try {
                // Fallback: maybe it was a single embedding array, not a list
                val gson = Gson()
                val type = object : TypeToken<FloatArray>() {}.type
                val singleArray = gson.fromJson<FloatArray>(serialized, type)
                if (singleArray != null) listOf(singleArray) else emptyList()
            } catch (_: Exception) {
                Log.w("CustomClassifier", "Failed parsing embeddings: ${e.message}")
                emptyList()
            }
        }
    }
}
