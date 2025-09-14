package com.pagzone.sonavi.service

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.gms.wearable.ChannelIOException
import com.pagzone.sonavi.data.repository.ClassificationResultRepositoryImpl
import com.pagzone.sonavi.domain.HybridYamnetClassifier
import com.pagzone.sonavi.model.ClassificationResult
import com.pagzone.sonavi.model.VibrationEffectDTO
import com.pagzone.sonavi.util.Constants
import com.pagzone.sonavi.viewmodel.ClientDataViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object AudioClassifierService {
    private lateinit var appContext: Context
    private lateinit var hybridYamnetClassifier: HybridYamnetClassifier

    private val clientDataViewModel = ClientDataViewModel()

    fun init(context: Context) {
        appContext = context.applicationContext
        hybridYamnetClassifier = HybridYamnetClassifier(context = appContext)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun classifyStream(inputStream: InputStream, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(1024)
            val shortBuffer = ShortArray(512)
            val floatBuffer = FloatArray(15600)
            var offset = 0

            try {
                while (isActive) {
                    val read = try {
                        inputStream.read(buffer)
                    } catch (e: ChannelIOException) {
                        Log.w("AudioClassifier", "Watch disconnected: ${e.message}")
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(appContext, "Watch disconnected", Toast.LENGTH_SHORT)
                                .show()
                        }
                        break
                    } catch (e: IOException) {
                        Log.e("AudioClassifier", "I/O error reading stream", e)
                        break
                    }

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
                        val (sound, confidence) = hybridYamnetClassifier.classify(floatBuffer)

                        if (sound != null && confidence > Constants.Classifier.CONFIDENCE_THRESHOLD) {
                            ClassificationResultRepositoryImpl.addResult(
                                ClassificationResult(sound.displayName, confidence)
                            )

                            clientDataViewModel.sendPrediction(
                                sound.displayName, confidence, VibrationEffectDTO(
                                    timings = sound.vibrationPattern,
                                    repeat = -1
                                )
                            )

                            Log.d("FewShot", "Label: ${sound.displayName}, Conf: $confidence")
                        }

                        offset = 0
                    }
                }
            } finally {
                try {
                    inputStream.close()
                    ClientDataViewModel().toggleListening(false)
                } catch (e: IOException) {
                    Log.w("AudioClassifier", "Error closing inputStream", e)
                }
            }
        }
    }
}