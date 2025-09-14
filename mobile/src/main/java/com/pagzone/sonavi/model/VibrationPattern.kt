package com.pagzone.sonavi.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pagzone.sonavi.util.Constants.RoomKeys.VIBRATION_PATTERNS

@Entity(tableName = VIBRATION_PATTERNS)
data class VibrationPattern(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val pattern: String, // JSON array [vibrate_ms, pause_ms, vibrate_ms, ...]
    val isDefault: Boolean = false
)
