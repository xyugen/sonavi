package com.pagzone.sonavi.presentation.util

import java.util.Locale

fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return when {
        hours > 0 -> String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs)
        else -> String.format(Locale.getDefault(), "%d:%02d", minutes, secs)
    }
}