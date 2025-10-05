package com.pagzone.sonavi.presentation.model

data class SoundPrediction(
    val label: String,
    val confidence: Float,
    val isCritical: Boolean = false
)
