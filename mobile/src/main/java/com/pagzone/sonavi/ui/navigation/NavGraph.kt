package com.pagzone.sonavi.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pagzone.sonavi.ui.screen.page.AddSoundPage
import com.pagzone.sonavi.ui.screen.page.HomePage
import com.pagzone.sonavi.ui.screen.page.LibraryPage

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = NavRoute.Home.route) {
        composable(NavRoute.Home.route) {
            HomePage(modifier)
        }
        composable(NavRoute.AddSound.route) {
            AddSoundPage(modifier)
        }
        composable(NavRoute.Library.route) {
            LibraryPage(modifier)
        }
    }
}