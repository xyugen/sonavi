package com.pagzone.sonavi.model

import android.net.Uri

sealed class AudioSource {
    data class Recording(val data: FloatArray) : AudioSource() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Recording
            return data.contentEquals(other.data)
        }
        override fun hashCode(): Int = data.contentHashCode()
    }

    data class Upload(
        val uri: Uri,
        val data: FloatArray,
        val fileName: String,
        val duration: Float
    ) : AudioSource() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Upload
            return uri == other.uri && data.contentEquals(other.data)
        }
        override fun hashCode(): Int = 31 * uri.hashCode() + data.contentHashCode()
    }
}