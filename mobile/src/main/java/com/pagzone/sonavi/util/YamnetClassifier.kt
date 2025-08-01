package com.pagzone.sonavi.util

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

class YamnetClassifier(context: Context) {
    private val labels: List<String>
    private val interpreter: Interpreter

    init {
        // Load labels
        labels = context.assets.open("yamnet_labels.txt").bufferedReader().readLines()

        // Load TFLite model
        val model = loadModelFile(context, "yamnnet.tflite")
        interpreter = Interpreter(model)
    }

    private fun loadModelFile(context: Context, filename: String): ByteBuffer {
        val fileDescriptor = context.assets.openFd(filename)
        val inputStream = fileDescriptor.createInputStream()
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun classify(audioData: FloatArray): Pair<String, Float> {
        // Input: 1D float[waveform]
        val input = arrayOf(audioData)

        // Output: [N, 521] float scores
        val output = Array(1) { FloatArray(labels.size) }

        interpreter.run(input, output)

        // Get the top-scoring label
        val scores = output[0]
        val maxIndex = scores.indices.maxByOrNull { scores[it] } ?: -1
        val confidence = scores[maxIndex]
        val label = labels.getOrNull(maxIndex) ?: "Unknown"

        return label to confidence
    }

    fun close() {
        interpreter.close()
    }
}