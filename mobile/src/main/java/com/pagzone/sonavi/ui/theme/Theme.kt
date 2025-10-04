package com.pagzone.sonavi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

private val LightColorScheme = lightColorScheme(
    primary = Blue50,
    onPrimary = Blue5,
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
    primary = Blue60,                        // Changed: Brighter for visibility
    onPrimary = Blue5,
    secondary = Slate70,                     // Changed: Slightly lighter
    onSecondary = Gray10,
    tertiary = Slate70,                      // Changed: Slightly lighter
    surface = Slate90,
    onSurface = Gray10,
    surfaceDim = Gray95,
    surfaceVariant = Blue95,                 // Changed: Much darker to differentiate from surface
    primaryContainer = Blue90,               // Changed: Darker than primary for contrast
    secondaryContainer = Slate90,
    onPrimaryContainer = Blue20,             // Changed: Lighter for better readability
    onSecondaryContainer = Slate20,          // Changed: Lighter for better readability
    tertiaryContainer = Slate90,
    onTertiaryContainer = Slate20,           // Changed: Lighter for better readability
    surfaceContainer = Gray90,               // Changed: Between surface and surfaceDim
    error = Red70,                           // Changed: Brighter for visibility
    onError = Gray10,
    errorContainer = Red90,
    onErrorContainer = Red30,                // Changed: Brighter for contrast
    background = Gray95,
    onBackground = Gray10,                   // Changed: Slightly lighter for softer text
)

@Composable
fun SonaviTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val themeState = remember { ThemeState() }

    // Only set initial value, don't overwrite on recomposition
    LaunchedEffect(Unit) {
        themeState.isDarkMode = darkTheme
    }

    val colorScheme = if (themeState.isDarkMode) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    CompositionLocalProvider(LocalThemeState provides themeState) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}