package com.pagzone.sonavi.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "detection_logs",
    foreignKeys = [
        ForeignKey(
            entity = SoundProfile::class,
            parentColumns = ["id"],
            childColumns = ["soundProfileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("soundProfileId")]
)
data class DetectionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val soundProfileId: Long,
    val confidence: Float,
    val timestamp: Date = Date(),
    val wasEmergencyTriggered: Boolean = false,
    val location: String? = null // Optional GPS coordinates
)
