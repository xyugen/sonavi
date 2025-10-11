package com.pagzone.sonavi.model


data class RealtimeQuality(
    val rmsLevel: Float,
    val snr: Float,
    val isPeaking: Boolean,
    val isQuiet: Boolean,
    val isNoisy: Boolean,
    val canUse: Boolean,
    val suggestion: String
)
