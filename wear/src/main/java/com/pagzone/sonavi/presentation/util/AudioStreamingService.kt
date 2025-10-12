package com.pagzone.sonavi.presentation.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import com.pagzone.sonavi.R
import com.pagzone.sonavi.presentation.data.repository.WearRepositoryImpl
import com.pagzone.sonavi.presentation.util.Constants.MessagePaths.MIC_AUDIO_PATH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.io.OutputStream

class AudioStreamingService : LifecycleService() {
    private val bufferSize = AudioRecord.getMinBufferSize(
        16000,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private var isRecording = false
    private var audioRecord: AudioRecord? = null
    private var channelClient: ChannelClient? = null  // ✅ Make nullable
    private var channel: ChannelClient.Channel? = null  // ✅ Make nullable
    private var streamingJob: Job? = null
    private var outputStream: OutputStream? = null

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // ✅ Start foreground immediately with proper type
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Sound Detection Active")
            .setContentText("Listening for sounds")
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        )

        // ✅ Initialize AudioRecord here
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                16000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing RECORD_AUDIO permission", e)
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRecording = false
        streamingJob?.cancel()

        try {
            audioRecord?.let {
                if (it.state == AudioRecord.STATE_INITIALIZED) {
                    it.stop()
                    it.release()
                }
            }
            audioRecord = null
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START -> {
                val nodeId = intent.getStringExtra(EXTRA_NODE_ID)
                if (nodeId != null) {
                    startStreaming(nodeId)
                } else {
                    Log.e(TAG, "No nodeId provided")
                    stopSelf()
                }
            }

            ACTION_STOP -> {
                // ✅ Safe null-check
                stopStreaming()
                stopSelf()
            }
        }

        return START_NOT_STICKY  // ✅ Changed from START_STICKY
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun startStreaming(nodeId: String) {
        if (isRecording) {
            Log.w(TAG, "Already recording")
            return
        }

        isRecording = true
        streamingJob = lifecycleScope.launch(Dispatchers.IO) {
            try {
                // ✅ Check if AudioRecord was initialized
                val recorder = audioRecord
                if (recorder == null || recorder.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "AudioRecord not initialized")
                    stopSelf()
                    return@launch
                }

                // ✅ Initialize channel client
                channelClient = Wearable.getChannelClient(this@AudioStreamingService)

                channel = channelClient!!.openChannel(nodeId, MIC_AUDIO_PATH)
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Failed to open channel", exception)
                        stopSelf()
                    }
                    .await()

                outputStream = channelClient!!.getOutputStream(channel!!).await()

                recorder.startRecording()
                val buffer = ByteArray(bufferSize)

                while (isRecording && isActive) {
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        try {
                            outputStream?.write(buffer, 0, read)
                            outputStream?.flush()
                        } catch (e: IOException) {
                            Log.e(TAG, "Audio streaming interrupted", e)
                            break
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Streaming setup failed", e)
            } catch (e: IllegalStateException) {
                Log.e(TAG, "AudioRecord error", e)
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error", e)
            } finally {
                cleanup()
            }
        }
    }

    private fun cleanup() {
        Log.d(TAG, "Cleaning up audio stream")
        try {
            audioRecord?.let {
                if (it.state == AudioRecord.STATE_INITIALIZED) {
                    it.stop()
                }
            }

            outputStream?.close()
            outputStream = null

            channel?.let { channelClient?.close(it) }
            channel = null
            channelClient = null

            notifyListeningStopped()
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }

    private fun notifyListeningStopped() {
        WearRepositoryImpl.toggleListening(false)
    }

    private fun stopStreaming() {
        isRecording = false
        streamingJob?.cancel()
        cleanup()
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Audio Streaming",
            NotificationManager.IMPORTANCE_LOW  // ✅ Changed to LOW
        ).apply {
            enableLights(false)
            enableVibration(false)
            setSound(null, null)
            setShowBadge(false)
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    companion object {
        private const val TAG = "AudioStreamingService"
        private const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL_ID = "streaming_channel"
        const val ACTION_START = "START_STREAM"
        const val ACTION_STOP = "STOP_STREAM"
        const val EXTRA_NODE_ID = "EXTRA_NODE_ID"
    }
}