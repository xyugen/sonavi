package com.pagzone.sonavi.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.wear.compose.material.MaterialTheme

@Composable
fun SonaviTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAppColors provides ExtendedColors
    ) {
        MaterialTheme(
            content = content
        )
    }
}

object AppTheme {
    val colors: AppColors
        @Composable
        get() = LocalAppColors.current
}