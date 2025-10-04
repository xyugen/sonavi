package com.pagzone.sonavi.ui.component

import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.pagzone.sonavi.R
import com.pagzone.sonavi.data.store.ProfileSettingsDataStore
import com.pagzone.sonavi.ui.theme.LocalThemeState
import kotlinx.coroutines.launch

@Composable
fun ThemeToggleButton(
    modifier: Modifier = Modifier
) {
    val themeState = LocalThemeState.current
    val context = LocalContext.current
    val dataStore = remember { ProfileSettingsDataStore(context) }
    val composableScope = rememberCoroutineScope()

    // Read initial preference once
    val themePreferences by dataStore.getThemePreference()
        .collectAsState(initial = themeState.isDarkMode)

    // Update theme state when preference changes
    LaunchedEffect(themePreferences) {
        themeState.isDarkMode = themePreferences
    }

    IconButton(
        onClick = {
            composableScope.launch {
                val newTheme = !themeState.isDarkMode
                dataStore.saveThemePreference(newTheme)
                Log.d("ThemeToggleButton", "Theme: ${themeState.isDarkMode} || $newTheme")
                // Theme will update through LaunchedEffect when dataStore emits new value
            }
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = if (themeState.isDarkMode) {
                ImageVector.vectorResource(R.drawable.ic_sun)
            } else {
                ImageVector.vectorResource(R.drawable.ic_moon)
            },
            contentDescription = if (themeState.isDarkMode) {
                "Switch to Light Mode"
            } else {
                "Switch to Dark Mode"
            },
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
    }
}