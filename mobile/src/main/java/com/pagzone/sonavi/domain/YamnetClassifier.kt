package com.pagzone.sonavi.domain

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.collections.iterator

class YamnetClassifier(context: Context) {
    private val tflite: Interpreter
    private val labels: List<String>

    init {
        val options = Interpreter.Options()
        tflite = Interpreter(loadModelFile(context, "yamnnet.tflite"), options)
        labels = loadLabels(context, "yamnet_labels.txt") // 521 labels
    }

    private fun loadModelFile(context: Context, filename: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    private fun mergeScores(
        scores: FloatArray,
        labels: List<String>,
        mergeMap: Map<String, List<String>>
    ): Map<String, Float> {
        val result = mutableMapOf<String, Float>()

        for ((newLabel, group) in mergeMap) {
            val indices = group.mapNotNull { lbl ->
                labels.indexOf(lbl).takeIf { it >= 0 }
            }
            if (indices.isNotEmpty()) {
                val groupScore = indices.maxOf { scores[it] } // or sumOf
                result[newLabel] = groupScore
            }
        }
        return result
    }

    private fun loadLabels(context: Context, filename: String): List<String> {
        return context.assets.open(filename).bufferedReader().readLines()
    }

    fun classify(audioData: FloatArray): Pair<String, Float> {
        val inputBuffer = arrayOf(audioData)
        val outputScores = Array(1) { FloatArray(labels.size) }
        tflite.run(inputBuffer, outputScores)

        val scores = outputScores[0]
        val maxIndex = scores.indices.maxByOrNull { scores[it] } ?: -1
        return labels[maxIndex] to scores[maxIndex]
    }
}