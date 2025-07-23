package com.pagzone.sonavi.presentation.util

import android.Manifest
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
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

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            16000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
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
        streamingJob = lifecycleScope.launch(Dispatchers.IO) {
            try {
                channelClient = Wearable.getChannelClient(this@AudioStreamingService)
                channel = channelClient.openChannel(nodeId, MIC_AUDIO_PATH).await()
                outputStream = channelClient.getOutputStream(channel).await()

                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    16000,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )
                val buffer = ByteArray(bufferSize)
                audioRecord.startRecording()

                while (isActive) {
                    val read = audioRecord.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        outputStream?.write(buffer, 0, read)
                        outputStream?.flush()
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Streaming failed", e)
            } finally {
                Log.d(TAG, "Cleaning up audio stream")
                outputStream?.close()
                outputStream = null
                audioRecord.release()
            }
        }
    }

    private fun stopStreaming(channelClient: ChannelClient, channel: ChannelClient.Channel) {
        isRecording = false
        audioRecord.stop()
        channelClient.close(channel)
    }

    companion object {
        private const val TAG = "AudioStreamingService"

        const val ACTION_START = "START_STREAM"
        const val ACTION_STOP = "STOP_STREAM"
        const val EXTRA_NODE_ID = "EXTRA_NODE_ID"
    }
}