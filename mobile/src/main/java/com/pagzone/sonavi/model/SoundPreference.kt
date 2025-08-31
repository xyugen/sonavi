package com.pagzone.sonavi.model

data class SoundPreference(
    val label: String,
    val enabled: Boolean = true,
    val snoozedUntil: Long? = null
)
