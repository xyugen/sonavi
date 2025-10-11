package com.pagzone.sonavi.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pagzone.sonavi.data.repository.PrefsRepository
import com.pagzone.sonavi.ui.navigation.NavRoute
import com.pagzone.sonavi.viewmodel.ClientDataViewModel
import com.psoffritti.taptargetcompose.TapTargetCoordinator

@Composable
fun ScreenWithScaffold(
    navController: NavHostController,
    clientDataViewModel: ClientDataViewModel,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f), Color.Transparent)
    )

    val isListening by clientDataViewModel.isListening.collectAsState()
    val isConnected by clientDataViewModel.isConnected.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentTitle = NavRoute.fromRoute(currentRoute)?.label ?: ""

    val showBars = currentRoute in NavRoute.bottomNavItems.map { it.route }

    val context = LocalContext.current
    val prefsRepository = PrefsRepository(context)

    var showTutorial by remember { mutableStateOf(!prefsRepository.getBoolean("tutorial_listen_mode_completed")) }

    TapTargetCoordinator(
        showTapTargets = showTutorial,
        onComplete = {
            showTutorial = false
            prefsRepository.saveBoolean("tutorial_listen_mode_completed", true)
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (showBars) TopAppBar(
                    title = currentTitle,
                    isListenModeChecked = isListening,
                    isListenModeEnabled = isConnected
                ) { value ->
                    if (value) onStartListening() else onStopListening()
                }
            },
            bottomBar = { if (showBars) BottomNavBar(navController) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                content(Modifier.padding(horizontal = 21.dp))
            }
        }
    }
}