package com.pagzone.sonavi.util

class Constants {
    object MessagePaths {
        const val START_LISTENING_PATH = "/start_listening"
        const val STOP_LISTENING_PATH = "/stop_listening"

        const val MIC_AUDIO_PATH = "/mic_audio"

        const val SOUND_DETECTED_PATH = "/sound_detected"
    }
    object Classifier {
        const val CONFIDENCE_THRESHOLD = 0.6f
        const val SMOOTHING_ALPHA = 0.75f
    }
    object Capabilities {
        const val WEAR_CAPABILITY = "wear"
    }
}