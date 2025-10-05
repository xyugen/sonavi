package com.pagzone.sonavi.presentation.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pagzone.sonavi.presentation.ui.ListeningPagerScreen
import com.pagzone.sonavi.presentation.ui.WelcomeScreen
import com.pagzone.sonavi.presentation.viewmodel.WearViewModel

@Composable
fun WearNavGraph(
    navController: NavHostController,
    viewModel: WearViewModel,
    startListening: () -> Unit,
    stopListening: () -> Unit
) {
    val isConnected by viewModel.isConnected.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val detectedSound by viewModel.soundPrediction.collectAsState()

    LaunchedEffect(isConnected) {
        Log.d("WearNavGraph", "isConnected changed to: $isConnected")
        val currentRoute = navController.currentDestination?.route
        if (!isConnected && currentRoute != "welcome") {
            navController.popBackStack(route = "welcome", inclusive = false)

        }
    }

    LaunchedEffect(isListening) {
        Log.d("WearNavGraph", "isListening changed to: $isListening")
        val currentRoute = navController.currentDestination?.route
        if (isListening && currentRoute != "listening") {
            navController.navigate("listening")
        } else if (!isListening && currentRoute == "listening") {
            navController.popBackStack(route = "welcome", inclusive = false)
        }
    }

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(
                isConnected = isConnected,
                onStartListening = {
                    startListening()
                    navController.navigate("listening")
                }
            )
        }
        composable("listening") {
            ListeningPagerScreen(
                onStopListening = {
                    stopListening()
                    navController.popBackStack()
                },
                detectedSound = detectedSound
            )
        }
    }
}