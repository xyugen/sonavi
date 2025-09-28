package com.pagzone.sonavi.util

class Constants {
    object MessagePaths {
        const val START_LISTENING_PATH = "/start_listening"
        const val STOP_LISTENING_PATH = "/stop_listening"

        const val MIC_AUDIO_PATH = "/mic_audio"

        const val SOUND_DETECTED_PATH = "/sound_detected"
    }
    object Classifier {
        const val CONFIDENCE_THRESHOLD = 0.5f // 50% confidence
        const val SMOOTHING_ALPHA = 0.75f
    }
    object SoundProfile {
        val DEFAULT_VIBRATION_PATTERN = listOf<Long>(0, 300, 1000, 300)
    }
    object Capabilities {
        const val WEAR_CAPABILITY = "wear"
    }
    object DataStoreKeys {
        // PROFILE
        const val PROFILE_NAME = "profile_name"
        const val PROFILE_ADDRESS = "profile_address"
        const val PROFILE_SETTINGS = "profile_settings"
    }
    object RoomKeys {
        const val EMERGENCY_CONTACTS = "emergency_contacts"
        const val SOUND_PROFILES = "sound_profiles"
        const val SOUND_PREFERENCES = "sound_preferences"
        const val VIBRATION_PATTERNS = "vibration_patterns"
    }
    object Sms {
        const val SMS_PERMISSION_REQUEST_CODE = 1001
    }
}