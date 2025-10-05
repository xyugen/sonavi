package com.pagzone.sonavi.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ThemeState {
    var isDarkMode by mutableStateOf(false)
}

val LocalThemeState = compositionLocalOf { ThemeState() }