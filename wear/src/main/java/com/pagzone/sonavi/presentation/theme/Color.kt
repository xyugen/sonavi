package com.pagzone.sonavi.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Blue40 = Color(0xFF6090fa)
val Blue10 = Color(0xFFdbe6fe)
val Blue5 = Color(0xFFeff6ff)

val Slate90 = Color(0xFF0f172b)
val Slate80 = Color(0xFF1d293d)
val Slate70 = Color(0xFF314158)

val Gray10 = Color(0xFFF5F4F3)

val Red40 = Color(0xFFff6467)

@Immutable
data class AppColors(
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val tertiary: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val disabled: Color,
    val error: Color,
    val onError: Color
)

val LocalAppColors = staticCompositionLocalOf {
    AppColors(
        primary = Color.Unspecified,
        onPrimary = Color.Unspecified,
        secondary = Color.Unspecified,
        onSecondary = Color.Unspecified,
        tertiary = Color.Unspecified,
        surface = Color.Unspecified,
        onSurface = Color.Unspecified,
        surfaceVariant = Color.Unspecified,
        disabled = Color.Unspecified,
        error = Color.Unspecified,
        onError = Color.Unspecified
    )
}

val ExtendedColors = AppColors(
    primary = Blue40,
    onPrimary = Blue5,
    secondary = Slate70,
    onSecondary = Gray10,
    tertiary = Slate70,
    surface = Slate90,
    onSurface = Gray10,
    surfaceVariant = Blue10,
    disabled = Slate80,
    error = Red40,
    onError = Gray10
)