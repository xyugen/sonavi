package com.pagzone.sonavi.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pagzone.sonavi.ui.component.BottomNavBar
import com.pagzone.sonavi.ui.component.TopAppBar
import com.pagzone.sonavi.ui.navigation.AppNavHost
import com.pagzone.sonavi.ui.navigation.NavRoute

@Preview(showSystemUi = true)
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.secondary,
            Color.Transparent
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentTitle = NavRoute.fromRoute(currentRoute)?.label ?: ""

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(title = currentTitle)
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
                navController
            )
        }
    }
}