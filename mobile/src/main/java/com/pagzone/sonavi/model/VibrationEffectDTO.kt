package com.pagzone.sonavi.model

import kotlinx.serialization.Serializable

@Serializable
data class VibrationEffectDTO(
    val timings: List<Long>,
    val amplitudes: List<Int>,
    val repeat: Int
)
