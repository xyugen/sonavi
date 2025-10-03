package com.pagzone.sonavi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Blue50,
    secondary = Slate50,
    onSecondary = Gray90,
    tertiary = Slate50,
    surface = Gray50,
    onSurface = Gray90,
    surfaceDim = Gray30,
    surfaceVariant = Blue10,
    primaryContainer = Blue20,
    secondaryContainer = Slate10,
    onPrimaryContainer = Blue90,
    onSecondaryContainer = Slate90,
    tertiaryContainer = Sky10,
    onTertiaryContainer = Sky90,
    surfaceContainer = Gray,
    error = Red50,
    onError = White,
    errorContainer = Red10,
    onErrorContainer = Red80,
    background = Gray5,
    onBackground = Gray80,
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    secondary = Slate80,
    onSecondary = Gray10,
    tertiary = Slate80,
    surface = Gray90,
    onSurface = Gray10,
    surfaceDim = Gray95,
    surfaceVariant = Blue90,
    primaryContainer = Blue80,
    secondaryContainer = Slate90,
    onPrimaryContainer = Blue10,
    onSecondaryContainer = Slate10,
    tertiaryContainer = Slate90,
    onTertiaryContainer = Slate10,
    surfaceContainer = Gray95,
    error = Red80,
    onError = Gray10,
    errorContainer = Red90,
    onErrorContainer = Red20,
    background = Gray95,
    onBackground = Gray20,
)

@Composable
fun SonaviTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme // TODO: add dark theme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}