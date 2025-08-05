package com.pagzone.sonavi.util

import android.content.Context
import android.util.Log
import com.pagzone.sonavi.data.repository.ClassificationResultRepositoryImpl
import com.pagzone.sonavi.model.ClassificationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object AudioClassifierService {
    private lateinit var appContext: Context
    private lateinit var classifier: YamnetClassifier

    fun init(context: Context) {
        appContext = context.applicationContext
        classifier = YamnetClassifier(appContext)
    }

    fun classifyStream(inputStream: InputStream, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(1024)
            val shortBuffer = ShortArray(512)
            val floatBuffer = FloatArray(15600)
            var offset = 0

            while (isActive) {
                val read = inputStream.read(buffer)
                if (read <= 0) break

                ByteBuffer.wrap(buffer, 0, read)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .asShortBuffer()
                    .get(shortBuffer, 0, read / 2)

                for (i in 0 until read / 2) {
                    if (offset < floatBuffer.size) {
                        floatBuffer[offset++] = shortBuffer[i] / 32768.0f
                    }
                }

                if (offset >= floatBuffer.size) {
                    val (label, confidence) = classifier.classify(floatBuffer)
                    val result = ClassificationResult(label, confidence)

                    if (confidence > 0.7f)
                        ClassificationResultRepositoryImpl.addResult(result)

                    Log.d("Yamnet", "Label: $label, Conf: $confidence")

                    offset = 0
                }
            }

            classifier.close()
        }
    }
}