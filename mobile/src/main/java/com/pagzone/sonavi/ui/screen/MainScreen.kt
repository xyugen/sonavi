package com.pagzone.sonavi.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.pagzone.sonavi.ui.component.ScreenWithScaffold
import com.pagzone.sonavi.ui.navigation.AppNavHost
import com.pagzone.sonavi.viewmodel.ClientDataViewModel
import com.pagzone.sonavi.viewmodel.SoundPreferencesViewModel

@Preview(showSystemUi = true)
@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    clientDataViewModel: ClientDataViewModel = viewModel(),
    soundPreferencesViewModel: SoundPreferencesViewModel = viewModel(),
    onStartListening: () -> Unit = {},
    onStopListening: () -> Unit = {}
) {
    ScreenWithScaffold(
        navController = navController,
        clientDataViewModel = clientDataViewModel,
        onStartListening = onStartListening,
        onStopListening = onStopListening
    ) {
        AppNavHost(
            navController,
            modifier = Modifier.padding(horizontal = 21.dp),
            clientDataViewModel = clientDataViewModel,
            soundPreferencesViewModel = soundPreferencesViewModel
        )
    }
}
