package com.pagzone.sonavi.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pagzone.sonavi.presentation.theme.SonaviTheme
import com.pagzone.sonavi.presentation.ui.WelcomeScreen

@Composable
fun WearApp(modifier: Modifier = Modifier) {
    SonaviTheme {
        WelcomeScreen()
    }
}