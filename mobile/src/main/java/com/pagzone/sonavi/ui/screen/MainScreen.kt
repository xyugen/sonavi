package com.pagzone.sonavi.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.pagzone.sonavi.ui.component.BottomNavBar
import com.pagzone.sonavi.ui.navigation.AppNavHost

@Preview(showSystemUi = true)
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavBar(navController)
        }) { innerPadding ->
        AppNavHost(navController, Modifier.padding(innerPadding))
    }
}