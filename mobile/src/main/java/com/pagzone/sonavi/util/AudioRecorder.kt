package com.pagzone.sonavi.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt

class AudioRecorder(private val context: Context) {
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var recordedData = mutableListOf<Short>()

    private val recentSamples = mutableListOf<Short>()
    private val maxRecentSamples = 160
    private val samplesLock = Any() // Add lock object

    fun getCurrentAmplitude(): Float {
        // Copy samples under lock to avoid concurrent modification
        val samples = synchronized(samplesLock) {
            recentSamples.toList()
        }

        if (samples.isEmpty()) return 0f

        val rms = sqrt(
            samples.map { (it / 32768.0).pow(2) }.average()
        ).toFloat()

        return (rms * 5f).coerceIn(0f, 1f)
    }

    fun getCurrentBuffer(): FloatArray {
        synchronized(samplesLock) {
            return recentSamples.map { it / 32768f }.toFloatArray()
        }
    }

    fun startRecording(onError: (String) -> Unit = {}): Boolean {
        // Check permission first
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onError("Microphone permission not granted")
            return false
        }

        // Calculate proper buffer size
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            onError("Device doesn't support this audio configuration")
            return false
        }

        // Use buffer size at least 2x the minimum
        val bufferSize = minBufferSize * 2

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            // Check if initialization succeeded
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                onError("Failed to initialize AudioRecord")
                audioRecord?.release()
                audioRecord = null
                return false
            }

            recordedData.clear()
            audioRecord?.startRecording()

            // Verify recording started
            if (audioRecord?.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                onError("Failed to start recording")
                audioRecord?.release()
                audioRecord = null
                return false
            }

            // Start recording loop
            recordingJob = CoroutineScope(Dispatchers.IO).launch {
                val buffer = ShortArray(bufferSize / 2)

                while (isActive && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0

                    if (readSize > 0) {
                        recordedData.addAll(buffer.take(readSize))

                        // Update recent samples under lock
                        synchronized(samplesLock) {
                            recentSamples.addAll(buffer.take(readSize))
                            if (recentSamples.size > maxRecentSamples) {
                                // Keep only the most recent samples
                                val excess = recentSamples.size - maxRecentSamples
                                repeat(excess) { recentSamples.removeAt(0) }
                            }
                        }
                    }
                }
            }

            return true

        } catch (e: SecurityException) {
            onError("Permission denied: ${e.message}")
            return false
        } catch (e: IllegalArgumentException) {
            onError("Invalid audio parameters: ${e.message}")
            return false
        } catch (e: Exception) {
            onError("Recording error: ${e.message}")
            return false
        }
    }

    fun stopRecording(): FloatArray {
        recordingJob?.cancel()

        try {
            audioRecord?.stop()
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error stopping recording", e)
        }

        audioRecord?.release()
        audioRecord = null

        val floatData = FloatArray(recordedData.size) { i ->
            recordedData[i] / 32768.0f
        }

        // Clear samples under lock
        synchronized(samplesLock) {
            recentSamples.clear()
        }

        recordedData.clear()
        return floatData
    }

    fun isRecording(): Boolean {
        return audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING
    }
}