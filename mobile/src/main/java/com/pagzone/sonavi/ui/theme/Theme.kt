package com.pagzone.sonavi.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Blue50,
    secondary = Blue20,
    onSecondary = Gray90,
    tertiary = Pink40,
    surface = Gray10,
    onSurface = Gray90,
    surfaceDim = Gray30,
    surfaceVariant = Blue10,
    primaryContainer = Blue15,
    onPrimaryContainer = Blue60,
    secondaryContainer = Blue16,
    onSecondaryContainer = Blue55,
    tertiaryContainer = Pink20,
    onTertiaryContainer = Pink70,
    surfaceContainer = Gray,
    error = Red70,
    errorContainer = Red20,
    onErrorContainer = Red80,

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun SonaviTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}