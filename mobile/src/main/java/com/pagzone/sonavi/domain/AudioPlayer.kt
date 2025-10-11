package com.pagzone.sonavi.domain

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class AudioPlayer() {
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false

    suspend fun playAudio(audioData: FloatArray, onComplete: () -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                stopPlayback()

                val sampleRate = 16000

                // Convert FloatArray to ShortArray
                // Assuming float values are normalized between -1.0 and 1.0
                val shortData = ShortArray(audioData.size) { i ->
                    (audioData[i] * Short.MAX_VALUE).roundToInt().coerceIn(
                        Short.MIN_VALUE.toInt(),
                        Short.MAX_VALUE.toInt()
                    ).toShort()
                }

                val bufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(shortData.size * 2)

                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)  // Changed to STATIC mode
                    .build()

                // Write all data at once in STATIC mode
                val bytesWritten = audioTrack?.write(shortData, 0, shortData.size) ?: 0
                Log.d("AudioPlayer", "Bytes written: $bytesWritten / ${shortData.size}")

                if (bytesWritten > 0) {
                    audioTrack?.play()
                    isPlaying = true
                    Log.d("AudioPlayer", "Started playing")

                    // Wait for playback to complete
                    // Calculate duration based on sample rate
                    val durationMs = (shortData.size * 1000L) / sampleRate
                    Log.d("AudioPlayer", "Expected duration: ${durationMs}ms")

                    // Poll the playback state
                    val startTime = System.currentTimeMillis()
                    while (isPlaying && audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        delay(50) // Check every 50ms

                        // Safety timeout (duration + 500ms buffer)
                        if (System.currentTimeMillis() - startTime > durationMs + 500) {
                            Log.d("AudioPlayer", "Playback timeout reached")
                            break
                        }

                        // Check if we've reached the end of the buffer
                        val playbackHeadPosition = audioTrack?.playbackHeadPosition ?: 0
                        if (playbackHeadPosition >= shortData.size - (sampleRate / 10)) {
                            // Near the end (within 100ms), wait a bit more for buffer to finish
                            delay(100)
                            break
                        }
                    }

                    Log.d("AudioPlayer", "Playback finished")
                }

                audioTrack?.stop()
                audioTrack?.release()
                audioTrack = null
                isPlaying = false

                withContext(Dispatchers.Main) {
                    Log.d("AudioPlayer", "onComplete()")
                    onComplete()
                }
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Error during playback", e)
                e.printStackTrace()
                isPlaying = false
                audioTrack?.release()
                audioTrack = null
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }

    fun stopPlayback() {
        isPlaying = false

        audioTrack?.apply {
            try {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    Log.d("AudioPlayer", "Stopping playback")
                    stop()
                }
                release()
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Error stopping playback", e)
                e.printStackTrace()
            }
        }
        audioTrack = null
    }

    fun isPlaying() = isPlaying
}