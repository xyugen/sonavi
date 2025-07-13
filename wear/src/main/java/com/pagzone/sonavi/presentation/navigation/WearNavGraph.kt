package com.pagzone.sonavi.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pagzone.sonavi.presentation.ui.WelcomeScreen
import com.pagzone.sonavi.presentation.viewmodel.WearViewModel

@Composable
fun WearNavGraph(navController: NavHostController, viewModel: WearViewModel) {
    val isConnected by viewModel.isConnected.collectAsState()

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(
                isConnected = isConnected,
                onStartListening = { navController.navigate("listening") }
            )
        }
        composable("listening") {
//            ListeningPagerScreen(
//                onStopListening = { navController.popBackStack() },
//                detectedSounds = viewModel.detectedSounds
//            )
        }
    }
}