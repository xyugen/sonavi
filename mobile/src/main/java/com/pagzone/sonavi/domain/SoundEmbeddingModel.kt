package com.pagzone.sonavi.domain

import android.content.Context
import android.util.Log
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory
import be.tarsos.dsp.mfcc.MFCC
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class SoundEmbeddingModel(context: Context) {
    private val interpreter: Interpreter

    init {
        val modelBuffer = loadModelFile(context, "yamnnet.tflite")
        interpreter = Interpreter(modelBuffer)
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

    // Extract embedding from audio
    fun extractEmbedding(audioInput: FloatArray): FloatArray {
        val sampleRate = 16000
        val bufferSize = 1024
        val overlap = bufferSize / 2
        val numCoefficients = 64   // Matches YAMNet input bands
        val numFrames = 96         // Matches YAMNet input frames

//        val dispatcher = AudioDispatcher(
//            UniversalAudioInputStream(
//                audioInput,
//                TarsosDSPAudioFormat(sampleRate.toFloat(), bufferSize, 1, true, true)
//            ), bufferSize, overlap
//
//        )
        val dispatcher =
            AudioDispatcherFactory.fromFloatArray(audioInput, sampleRate, bufferSize, overlap)
        val mfcc = MFCC(
            bufferSize, sampleRate.toFloat(), numCoefficients, 30, 300f,
            (sampleRate / 2).toFloat()
        )

        val mfccFrames = mutableListOf<FloatArray>()
        dispatcher.addAudioProcessor(mfcc)
        dispatcher.addAudioProcessor(object : AudioProcessor {
            override fun processingFinished() {
                //vvv error b/c mfcc instance variable is null
                //float[] mfccArr = mfcc.getMFCC();
                mfccFrames.add(mfcc.mfcc.clone())
                Log.d("SoundEmbeddingModel", "processingFinished")
            }

            override fun process(audioEvent: AudioEvent?): Boolean {
                // breakpoint or logging to console doesn't enter function
                return true
            }
        })
        dispatcher.run()

        val features = Array(1) { Array(numFrames) { FloatArray(numCoefficients) } }
        for (i in 0 until numFrames) {
            val frame = if (i < mfccFrames.size) mfccFrames[i] else FloatArray(numCoefficients)
            features[0][i] = frame
        }

        // Output: [1, 128]
        val outputArray = Array(1) { FloatArray(128) }
        interpreter.run(features, outputArray)

        return outputArray[0]
    }

    // Average multiple embeddings (for prototype creation)
    fun averageEmbeddings(embeddings: List<FloatArray>): FloatArray {
        if (embeddings.isEmpty()) return FloatArray(128)

        val result = FloatArray(embeddings[0].size)
        for (embedding in embeddings) {
            for (i in embedding.indices) {
                result[i] += embedding[i]
            }
        }

        for (i in result.indices) {
            result[i] /= embeddings.size
        }

        return result
    }

    fun cleanup() {
        interpreter.close()
    }
}