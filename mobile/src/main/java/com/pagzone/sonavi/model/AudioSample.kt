package com.pagzone.sonavi.model

import java.util.UUID

data class AudioSample(
    val id: String = UUID.randomUUID().toString(),
    val source: AudioSource,
    val addedAt: Long = System.currentTimeMillis(),
    val quality: AudioQuality? = null
)