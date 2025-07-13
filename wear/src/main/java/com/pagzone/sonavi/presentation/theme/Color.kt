package com.pagzone.sonavi.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Pink80 = Color(0xFFEFB8C8)

val Pink40 = Color(0xFF7D5260)

val Blue80 = Color(0xFF4758B5)
val Blue75 = Color(0xFF3E66B5)
val Blue50 = Color(0xFF2D82B5)
val Blue20 = Color(0xFFBCE6FF)
val Blue10 = Color(0xFFCCE4F8)

val Green50 = Color(0xFF3DB52D)

val Gray90 = Color(0xFF212121)
val Gray80 = Color(0xFF1F1F1F)
val Gray20 = Color(0xFFE0E0E0)
val Gray10 = Color(0xFFF5F4F3)

val Red50 = Color(0xFFEB3B5A)

@Immutable
data class AppColors(
    val primary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val tertiary: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val disabled: Color
)

val LocalAppColors = staticCompositionLocalOf {
    AppColors(
        primary = Color.Unspecified,
        secondary = Color.Unspecified,
        onSecondary = Color.Unspecified,
        tertiary = Color.Unspecified,
        surface = Color.Unspecified,
        onSurface = Color.Unspecified,
        surfaceVariant = Color.Unspecified,
        disabled = Color.Unspecified
    )
}

val ExtendedColors = AppColors(
    primary = Blue80,
    secondary = Blue20,
    onSecondary = Gray90,
    tertiary = Pink40,
    surface = Gray10,
    onSurface = Gray90,
    surfaceVariant = Blue10,
    disabled = Gray80
)