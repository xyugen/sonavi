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

    LaunchedEffect(isConnected) {
        Log.d("WearNavGraph", "isConnected changed to: $isConnected")
        if (!isConnected) {
            Log.d("WearNavGraph", "is not connected")
            // Prevent navigating if already on welcome
            if (navController.currentDestination?.route != "welcome") {
                navController.popBackStack(route = "welcome", inclusive = false)
            }
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
                detectedSound = "Test"
            )
        }
    }
}