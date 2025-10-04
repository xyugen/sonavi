package com.pagzone.sonavi.model

data class ProfileSettings(
    val name: String = "User",
    val address: String = "",
    val hasCurrentLocation: Boolean = false
)
