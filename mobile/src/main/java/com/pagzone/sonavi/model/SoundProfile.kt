package com.pagzone.sonavi.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pagzone.sonavi.util.Constants.Classifier.CONFIDENCE_THRESHOLD
import com.pagzone.sonavi.util.Constants.RoomKeys.SOUND_PROFILES
import com.pagzone.sonavi.util.Constants.SoundProfile.DEFAULT_VIBRATION_PATTERN
import java.util.Date

@Entity(tableName = SOUND_PROFILES)
data class SoundProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Basic Info
    val name: String,
    val displayName: String, // Editable for custom, same as name for built-in
    val isBuiltIn: Boolean,
    val isEnabled: Boolean = true,
    val snoozedUntil: Date? = null, // When snooze expires, null = not snoozed

    // YAMNet Integration
    val yamnetIndices: List<Int> = emptyList(), // JSON array of indices for built-in sounds

    // MFCC embedding
    val mfccEmbedding: String? = null, // For custom sounsd

    // Settings
    val threshold: Float = CONFIDENCE_THRESHOLD, // Detection confidence threshold (0.0 - 1.0)
    val isCritical: Boolean = false,
    val vibrationPattern: List<Long> = DEFAULT_VIBRATION_PATTERN,

    // Emergency
    val emergencyCooldownMinutes: Int = 5,
    val lastEmergencyMessageSent: Date? = null,

    // Metadata
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val lastDetectedAt: Date? = null
)