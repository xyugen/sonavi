package com.pagzone.sonavi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.pagzone.sonavi.data.repository.ClientDataRepositoryImpl
import com.pagzone.sonavi.ui.navigation.NavigationManager
import com.pagzone.sonavi.ui.screen.MainScreen
import com.pagzone.sonavi.ui.theme.SonaviTheme
import com.pagzone.sonavi.viewmodel.ClientDataViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val clientDataViewModel: ClientDataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ClientDataRepositoryImpl.init(this)

        setContent {
            SonaviTheme {
                val navController = rememberNavController()
                LaunchedEffect(Unit) {
                    NavigationManager.setNavController(navController)
                }

                MainScreen(
                    navController = navController,
                    onStartWearableActivityClick = ::startWearableActivity,
                    onStartListening = ::startListening,
                    onStopListening = ::stopListening
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()

        startWearableActivity()
        clientDataViewModel.initializeListeners()
    }

    override fun onStop() {
        super.onStop()

        clientDataViewModel.destroyListeners()
    }

    /**
     * Cleans up navigation resources to prevent memory leaks.
     */
    override fun onDestroy() {
        super.onDestroy()

        NavigationManager.clear()
    }

    private fun startWearableActivity() {
        clientDataViewModel.startWearableActivity()
    }

    private fun startListening() {
        clientDataViewModel.startListening()
    }

    private fun stopListening() {
        clientDataViewModel.stopListening()
    }
}