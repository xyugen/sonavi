package com.pagzone.sonavi.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pagzone.sonavi.data.repository.PrefsRepository
import com.pagzone.sonavi.ui.screen.page.AddSoundPage
import com.pagzone.sonavi.ui.screen.page.HomePage
import com.pagzone.sonavi.ui.screen.page.LibraryPage
import com.pagzone.sonavi.ui.screen.page.ProfilePage
import com.pagzone.sonavi.viewmodel.ClientDataViewModel
import com.psoffritti.taptargetcompose.TapTargetCoordinator

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    clientDataViewModel: ClientDataViewModel = viewModel()
) {
    val prefsRepository = PrefsRepository(navController.context)

    var showHomeTutorial by remember { mutableStateOf(!prefsRepository.getBoolean("tutorial_home_completed")) }
    var showLibraryTutorial by remember { mutableStateOf(!prefsRepository.getBoolean("tutorial_library_completed")) }
    var showAddSoundTutorial by remember { mutableStateOf(!prefsRepository.getBoolean("tutorial_add_sound_completed")) }
    var showProfileTutorial by remember { mutableStateOf(!prefsRepository.getBoolean("tutorial_profile_completed")) }

    NavHost(
        navController = navController, startDestination = NavRoute.Home.route,
    ) {
        composable(NavRoute.Home.route) {
            TapTargetCoordinator(
                showTapTargets = showHomeTutorial,
                onComplete = {
                    showHomeTutorial = false
                    prefsRepository.saveBoolean("tutorial_home_completed", true)
                }
            ) {
                HomePage(modifier, clientDataViewModel)
            }
        }
        composable(NavRoute.AddSound.route) {
            TapTargetCoordinator(
                showTapTargets = showAddSoundTutorial,
                onComplete = {
                    showAddSoundTutorial = false
                    prefsRepository.saveBoolean("tutorial_add_sound_completed", true)
                }
            ) {
                AddSoundPage(modifier)
            }
        }
        composable(NavRoute.Library.route) {
            TapTargetCoordinator(
                showTapTargets = showLibraryTutorial,
                onComplete = {
                    showLibraryTutorial = false
                    prefsRepository.saveBoolean("tutorial_library_completed", true)
                }
            ) {
                LibraryPage(modifier)
            }
        }
        composable(
            NavRoute.Profile.route
        ) {
            TapTargetCoordinator(
                showTapTargets = showProfileTutorial,
                onComplete = {
                    showProfileTutorial = false
                    prefsRepository.saveBoolean("tutorial_profile_completed", true)
                }
            ) {
                ProfilePage(modifier)
            }
        }
    }
}