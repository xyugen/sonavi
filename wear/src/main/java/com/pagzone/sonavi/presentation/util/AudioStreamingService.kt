package com.pagzone.sonavi.presentation.util

import android.Manifest
import android.R.drawable.presence_audio_online
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
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
    private lateinit var audioRecord: AudioRecord
    private lateinit var channelClient: ChannelClient
    private lateinit var channel: ChannelClient.Channel
    private var streamingJob: Job? = null
    private var outputStream: OutputStream? = null

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Sound Detection Active")
            .setContentText("Listening for sounds")
            .setSmallIcon(presence_audio_online)
            .setOngoing(true)
            .build()

        startForeground(1, notification)

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            16000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        isRecording = false
        streamingJob?.cancel()
        try {
            if (::audioRecord.isInitialized && audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                audioRecord.stop()
                audioRecord.release()
            }
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
                    stopSelf()
                }
            }

            ACTION_STOP -> {
                stopStreaming(channelClient, channel)
                stopSelf()
            }
        }
        return START_STICKY
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startStreaming(nodeId: String) {
        isRecording = true
        streamingJob = lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Reinitialize AudioRecord if it was released
                if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                    audioRecord = AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        16000,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize
                    )
                }

                channelClient = Wearable.getChannelClient(this@AudioStreamingService)
                channel = channelClient.openChannel(nodeId, MIC_AUDIO_PATH)
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Failed to open channel", exception)
                        stopSelf()
                    }.await()
                outputStream = channelClient.getOutputStream(channel).await()

                audioRecord.startRecording()
                val buffer = ByteArray(bufferSize)

                while (isRecording && isActive) {
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        try {
                            outputStream?.write(buffer, 0, read)
                            outputStream?.flush()
                        } catch (e: IOException) {
                            Log.e(TAG, "Audio streaming interrupted", e)
                            break // Exit loop gracefully
                        } catch (e: Exception) {
                            Log.e(TAG, "Unexpected error in audio streaming", e)
                            break
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Streaming setup failed", e)
            } catch (e: IllegalStateException) {
                Log.e(TAG, "AudioRecord error", e)
            } finally {
                Log.d(TAG, "Cleaning up audio stream")
                try {
                    if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                        audioRecord.stop()
                    }
                    outputStream?.close()

                    // Update the actual state
                    notifyListeningStopped()
                } catch (e: IOException) {
                    Log.e(TAG, "Error closing stream", e)
                }
                outputStream = null
            }
        }
    }

    private fun notifyListeningStopped() {
        // Access your existing repository instance
        WearRepositoryImpl.toggleListening(false)
    }

    private fun stopStreaming(channelClient: ChannelClient, channel: ChannelClient.Channel) {
        isRecording = false
        try {
            if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                audioRecord.stop()
            }
            channelClient.close(channel)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping stream", e)
        }
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID, // same ID as used in NotificationCompat.Builder
            "Audio Streaming", // human-readable name
            NotificationManager.IMPORTANCE_DEFAULT
        )
        serviceChannel.enableLights(false)
        serviceChannel.enableVibration(false)
        serviceChannel.setSound(null, null)

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    companion object {
        private const val TAG = "AudioStreamingService"

        const val NOTIFICATION_CHANNEL_ID = "streaming_channel"

        const val ACTION_START = "START_STREAM"
        const val ACTION_STOP = "STOP_STREAM"
        const val EXTRA_NODE_ID = "EXTRA_NODE_ID"
    }
}