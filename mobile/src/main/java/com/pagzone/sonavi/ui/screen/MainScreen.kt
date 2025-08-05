package com.pagzone.sonavi.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pagzone.sonavi.ui.component.BottomNavBar
import com.pagzone.sonavi.ui.component.TopAppBar
import com.pagzone.sonavi.ui.navigation.AppNavHost
import com.pagzone.sonavi.ui.navigation.NavRoute
import com.pagzone.sonavi.viewmodel.ClientDataViewModel

@Preview(showSystemUi = true)
@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    viewModel: ClientDataViewModel = viewModel(),
    onStartListening: () -> Unit = {},
    onStopListening: () -> Unit = {}
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.secondary,
            Color.Transparent
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentTitle = NavRoute.fromRoute(currentRoute)?.label ?: ""

    val isListening by viewModel.isListening.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = currentTitle,
                isListenModeChecked = isListening,
                isListenModeEnabled = isConnected,
                onListenModeChange = { value ->
                    if (value) {
                        onStartListening()
                    } else {
                        onStopListening()
                    }
                })
        },
        bottomBar = {
            BottomNavBar(navController)
        }) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradient)
                .padding(innerPadding)
        ) {
            AppNavHost(
                navController,
                modifier = Modifier.padding(horizontal = 21.dp),
                viewModel = viewModel
            )
        }
    }
}