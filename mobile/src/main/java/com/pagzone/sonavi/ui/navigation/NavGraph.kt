package com.pagzone.sonavi.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pagzone.sonavi.ui.screen.page.AddSoundPage
import com.pagzone.sonavi.ui.screen.page.HomePage
import com.pagzone.sonavi.ui.screen.page.LibraryPage
import com.pagzone.sonavi.ui.screen.page.ProfilePage
import com.pagzone.sonavi.viewmodel.ClientDataViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    clientDataViewModel: ClientDataViewModel = viewModel()
) {
    NavHost(
        navController = navController, startDestination = NavRoute.Home.route,
    ) {
        composable(NavRoute.Home.route) {
            HomePage(modifier, clientDataViewModel)
        }
        composable(NavRoute.AddSound.route) {
            AddSoundPage(modifier)
        }
        composable(NavRoute.Library.route) {
            LibraryPage(modifier)
        }

        // Not in bottom navigation
        composable(
            NavRoute.Profile.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(500)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(500)
                )
            }) {
            ProfilePage(navController, modifier)
        }
    }
}