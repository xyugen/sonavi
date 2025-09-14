package com.pagzone.sonavi.model

import androidx.room.Embedded

data class DetectionLogWithSound(
    @Embedded val log: DetectionLog,
    val soundName: String
)
