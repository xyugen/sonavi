package com.pagzone.sonavi.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ActivityCompat
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

        // request permission if not granted
        if (!hasPermission(Manifest.permission.RECORD_AUDIO)) {
            requestPermission(Manifest.permission.RECORD_AUDIO)
        }

        if (!hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermission(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

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

    private fun hasPermission(permission: String): Boolean {
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), 1001)
    }

    private fun startListening() {
        viewModel.startListening()
    }

    private fun stopListening() {
        viewModel.stopListening()
    }
}