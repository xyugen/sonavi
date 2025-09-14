package com.pagzone.sonavi.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pagzone.sonavi.util.Constants.RoomKeys.SOUND_PROFILES
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

    // YAMNet Integration
    val yamnetIndices: List<Int> = emptyList(), // JSON array of indices for built-in sounds

    // MFCC embedding
    val mfccEmbedding: String? = null, // For custom sounsd

    // Settings
    val threshold: Float = 0.5f, // Detection confidence threshold (0.0 - 1.0)
    val isCritical: Boolean = false,
    val vibrationPattern: List<Long> = listOf(0, 300, 1000, 300),

    // Metadata
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val lastDetectedAt: Date? = null
)