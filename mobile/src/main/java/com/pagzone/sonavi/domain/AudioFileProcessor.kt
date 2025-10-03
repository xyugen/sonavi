package com.pagzone.sonavi.domain

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

class AudioFileProcessor(private val context: Context) {

    data class ProcessingResult(
        val success: Boolean,
        val audioData: FloatArray? = null,
        val fileName: String? = null,
        val duration: Float? = null,
        val error: String? = null
    )

    suspend fun processAudioFile(uri: Uri): ProcessingResult = withContext(Dispatchers.IO) {
        try {
            // Get file info
            val fileName = getFileName(uri)

            // Extract audio data
            val audioData = extractAudioData(uri)

            if (audioData == null || audioData.isEmpty()) {
                return@withContext ProcessingResult(
                    success = false,
                    error = "Failed to read audio data"
                )
            }

            // Validate duration (max 10 seconds at 16kHz = 160,000 samples)
            val duration = audioData.size / 16000f
            if (duration > 10f) {
                return@withContext ProcessingResult(
                    success = false,
                    error = "Audio file too long (max 10 seconds, got ${String.format("%.1f", duration)}s)"
                )
            }

            if (duration < 0.5f) {
                return@withContext ProcessingResult(
                    success = false,
                    error = "Audio file too short (minimum 0.5 seconds)"
                )
            }

            // Normalize audio
            val normalizedAudio = normalizeAudio(audioData)

            ProcessingResult(
                success = true,
                audioData = normalizedAudio,
                fileName = fileName,
                duration = duration
            )

        } catch (e: Exception) {
            Log.e("AudioFileProcessor", "Error processing audio file", e)
            ProcessingResult(
                success = false,
                error = "Error: ${e.message ?: "Unknown error"}"
            )
        }
    }

    private fun extractAudioData(uri: Uri): FloatArray? {
        return try {
            val mediaExtractor = MediaExtractor()
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                mediaExtractor.setDataSource(pfd.fileDescriptor)

                // Find audio track
                val trackIndex = findAudioTrack(mediaExtractor) ?: return null
                mediaExtractor.selectTrack(trackIndex)

                val format = mediaExtractor.getTrackFormat(trackIndex)
                val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                val channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

                // Decode audio
                val decoder = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
                decoder.configure(format, null, null, 0)
                decoder.start()

                val audioSamples = mutableListOf<Short>()
                val bufferInfo = MediaCodec.BufferInfo()
                var isEOS = false

                while (!isEOS) {
                    // Input
                    val inputBufferIndex = decoder.dequeueInputBuffer(10000)
                    if (inputBufferIndex >= 0) {
                        val inputBuffer = decoder.getInputBuffer(inputBufferIndex)!!
                        val sampleSize = mediaExtractor.readSampleData(inputBuffer, 0)

                        if (sampleSize < 0) {
                            decoder.queueInputBuffer(
                                inputBufferIndex, 0, 0, 0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                        } else {
                            decoder.queueInputBuffer(
                                inputBufferIndex, 0, sampleSize,
                                mediaExtractor.sampleTime, 0
                            )
                            mediaExtractor.advance()
                        }
                    }

                    // Output
                    val outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000)
                    if (outputBufferIndex >= 0) {
                        val outputBuffer = decoder.getOutputBuffer(outputBufferIndex)!!

                        if (bufferInfo.size > 0) {
                            val chunk = ShortArray(bufferInfo.size / 2)
                            outputBuffer.asShortBuffer().get(chunk)
                            audioSamples.addAll(chunk.toList())
                        }

                        decoder.releaseOutputBuffer(outputBufferIndex, false)

                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            isEOS = true
                        }
                    }
                }

                decoder.stop()
                decoder.release()
                mediaExtractor.release()

                // Convert to mono if stereo
                val monoSamples = if (channelCount == 2) {
                    convertStereoToMono(audioSamples.toShortArray())
                } else {
                    audioSamples.toShortArray()
                }

                // Resample to 16kHz if needed
                val resampledSamples = if (sampleRate != 16000) {
                    resample(monoSamples, sampleRate, 16000)
                } else {
                    monoSamples
                }

                // Convert to float array [-1, 1]
                resampledSamples.map { it / 32768f }.toFloatArray()
            }
        } catch (e: Exception) {
            Log.e("AudioFileProcessor", "Error extracting audio", e)
            null
        }
    }

    private fun findAudioTrack(extractor: MediaExtractor): Int? {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/") == true) {
                return i
            }
        }
        return null
    }

    private fun convertStereoToMono(stereo: ShortArray): ShortArray {
        val mono = ShortArray(stereo.size / 2)
        for (i in mono.indices) {
            mono[i] = ((stereo[i * 2].toInt() + stereo[i * 2 + 1].toInt()) / 2).toShort()
        }
        return mono
    }

    private fun resample(input: ShortArray, fromRate: Int, toRate: Int): ShortArray {
        if (fromRate == toRate) return input

        val ratio = fromRate.toFloat() / toRate
        val outputSize = (input.size / ratio).toInt()
        val output = ShortArray(outputSize)

        for (i in output.indices) {
            val srcIndex = (i * ratio).toInt()
            output[i] = if (srcIndex < input.size) input[srcIndex] else 0
        }

        return output
    }

    private fun normalizeAudio(audio: FloatArray): FloatArray {
        val maxAbs = audio.maxOfOrNull { abs(it) } ?: 1f
        return if (maxAbs > 0) {
            audio.map { it / maxAbs }.toFloatArray()
        } else {
            audio
        }
    }

    private fun getFileName(uri: Uri): String {
        var fileName = "uploaded_audio.${getFileExtension(uri)}"

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }

        return fileName
    }

    private fun getFileExtension(uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)
        return when (mimeType) {
            "audio/mpeg" -> "mp3"
            "audio/mp4", "audio/m4a" -> "m4a"
            "audio/wav", "audio/x-wav" -> "wav"
            "audio/ogg" -> "ogg"
            "audio/flac" -> "flac"
            else -> "audio"
        }
    }
}