package com.pagzone.sonavi.model

data class EmergencyContactUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)