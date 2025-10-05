package com.pagzone.sonavi.model

import kotlinx.serialization.Serializable

@Serializable
data class SoundPredictionDTO(
    val label: String,
    val confidence: Float,
    val isCritical: Boolean,
    val vibration: VibrationEffectDTO
)
