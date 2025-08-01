package com.pagzone.sonavi.util

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ModelUtils {
    companion object {
        fun loadYamnetLabels(context: Context): List<String> {
            return context.assets.open("yamnet_labels.txt").bufferedReader().readLines()
        }

        fun byteArrayToFloatArray(byteArray: ByteArray): FloatArray {
            val floatArray = FloatArray(byteArray.size / 2)
            val byteBuffer = ByteBuffer.wrap(byteArray)
                .order(ByteOrder.LITTLE_ENDIAN)

            for (i in floatArray.indices) {
                floatArray[i] = byteBuffer.short.toFloat() / Short.MAX_VALUE
            }

            return floatArray
        }

        fun decodeWavToFloatArray(wavFile: File): FloatArray {
            val inputStream = FileInputStream(wavFile)
            val wavHeaderSize = 44
            val bytes = inputStream.readBytes().drop(wavHeaderSize).toByteArray()

            // Convert PCM 16-bit LE to floats between -1.0 and 1.0
            val shortBuffer = ByteBuffer.wrap(bytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asShortBuffer()

            val shortArray = ShortArray(shortBuffer.limit())
            shortBuffer.get(shortArray)

            return shortArray.map { it / 32768.0f }.toFloatArray()
        }
    }
}