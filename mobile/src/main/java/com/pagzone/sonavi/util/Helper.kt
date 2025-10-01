package com.pagzone.sonavi.util

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

class Helper {
    companion object {
        fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

        fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
            val fileDescriptor = context.assets.openFd(modelName)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                fileDescriptor.startOffset,
                fileDescriptor.declaredLength
            )
        }

        // Average multiple embeddings to create prototype
        fun averageEmbeddings(embeddings: List<FloatArray>): FloatArray {
            if (embeddings.isEmpty()) {
                throw IllegalArgumentException("Cannot average empty embeddings list")
            }

            val embeddingSize = embeddings[0].size
            val averaged = FloatArray(embeddingSize)

            // Sum all embeddings
            for (embedding in embeddings) {
                require(embedding.size == embeddingSize) {
                    "All embeddings must have same size"
                }
                for (i in averaged.indices) {
                    averaged[i] += embedding[i]
                }
            }

            // Divide by count to get average
            for (i in averaged.indices) {
                averaged[i] /= embeddings.size
            }

            return averaged
        }

        // L2 normalization for better cosine similarity
        fun normalizeEmbedding(embedding: FloatArray): FloatArray {
            val norm = sqrt(embedding.map { it * it }.sum())
            return if (norm > 0) {
                embedding.map { it / norm }.toFloatArray()
            } else {
                embedding
            }
        }

        // Calculate quality metrics to help user know if recordings are good
        fun calculateEmbeddingQuality(embeddings: List<FloatArray>): Pair<Float, Float> {
            if (embeddings.size < 2) return 0f to 1f

            // Calculate pairwise similarities
            val similarities = mutableListOf<Float>()
            for (i in embeddings.indices) {
                for (j in i + 1 until embeddings.size) {
                    val sim = cosineSimilarity(embeddings[i], embeddings[j])
                    similarities.add(sim)
                }
            }

            val avgSimilarity = similarities.average().toFloat()
            val variance = similarities.map { (it - avgSimilarity) * (it - avgSimilarity) }
                .average().toFloat()

            return variance to avgSimilarity
        }

        fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
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

        // Parse embedding from JSON string
        fun parseEmbedding(embeddingString: String?): FloatArray? {
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
}