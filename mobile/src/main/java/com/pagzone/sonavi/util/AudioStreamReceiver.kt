package com.pagzone.sonavi.util

import android.os.Environment
import android.util.Log
import com.pagzone.sonavi.util.AudioUtils.Companion.convertPcmToWav
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AudioStreamReceiver() {
    private var receivingJob: Job? = null
    private var outputStream: FileOutputStream? = null
    private var wavFile: File? = null

    fun start(inputStream: InputStream) {
        stop()

        val externalDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Sonavi"
        ).apply { if (!exists()) mkdirs() }

        val pcmFile = File(externalDir, PCM_FILE_NAME)
        outputStream = FileOutputStream(pcmFile)

        wavFile = File(externalDir, WAV_FILE_NAME)

        receivingJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val buffer = ByteArray(1024)
                while (isActive) {
                    val read = inputStream.read(buffer)
                    if (read == -1) break
                    outputStream?.write(buffer, 0, read)
                }
                Log.d(TAG, "PCM recording finished.")
            } catch (e: Exception) {
                Log.e(TAG, "Streaming error", e)
            } finally {
                outputStream?.close()
                convertPcmToWav(pcmFile, wavFile!!)
                Log.d(TAG, "Converted to WAV: ${wavFile!!.absolutePath}")
            }
        }
    }

    fun stop() {
        receivingJob?.cancel()
        receivingJob = null
    }

    companion object {
        private const val TAG = "AudioStreamReceiver"

        private const val PCM_FILE_NAME = "streamed_audio.pcm"
        private const val WAV_FILE_NAME = "streamed_audio.wav"
    }
}