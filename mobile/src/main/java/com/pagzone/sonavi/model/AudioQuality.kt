package com.pagzone.sonavi.model

data class AudioQuality(
    val rmsLevel: Float,
    val snr: Float,
    val duration: Float,
    val quality: QualityLevel
)