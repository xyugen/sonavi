package com.pagzone.sonavi.model

data class ClassificationResult(
    val label: String,
    val confidence: Float,
    val isCritical: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
