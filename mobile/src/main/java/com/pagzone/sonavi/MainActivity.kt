package com.pagzone.sonavi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pagzone.sonavi.ui.screen.MainScreen
import com.pagzone.sonavi.ui.theme.SonaviTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SonaviTheme {
                MainScreen()
            }
        }
    }
}