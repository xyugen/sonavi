package com.pagzone.sonavi.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.pagzone.sonavi.presentation.data.repository.WearRepositoryImpl
import com.pagzone.sonavi.presentation.navigation.NavigationManager
import com.pagzone.sonavi.presentation.navigation.WearNavGraph
import com.pagzone.sonavi.presentation.theme.SonaviTheme
import com.pagzone.sonavi.presentation.viewmodel.WearViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: WearViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        WearRepositoryImpl.init(this)

        setContent {
            SonaviTheme {
                val navController = rememberNavController()
                LaunchedEffect(Unit) {
                    NavigationManager.setNavController(navController)
                }

                WearNavGraph(
                    navController = navController,
                    viewModel = viewModel,
                    startListening = ::startListening,
                    stopListening = ::stopListening
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.startWearableActivity()
        viewModel.initializeListeners()
    }

    override fun onStop() {
        super.onStop()

        viewModel.destroyListeners()
    }

    override fun onDestroy() {
        super.onDestroy()

        NavigationManager.clear()
    }

    fun startListening() {
        viewModel.startListening()
    }

    fun stopListening() {
        viewModel.stopListening()
    }
}