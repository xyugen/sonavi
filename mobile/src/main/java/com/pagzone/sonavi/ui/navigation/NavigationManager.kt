package com.pagzone.sonavi.ui.navigation

import androidx.navigation.NavHostController
import java.lang.ref.WeakReference

object NavigationManager {
    private var navControllerRef: WeakReference<NavHostController>? = null

    fun setNavController(navController: NavHostController) {
        navControllerRef = WeakReference(navController)
    }

    @Synchronized
    fun navigate(route: String) {
        navControllerRef?.get()?.navigate(route)
    }

    fun navigateAndPop(route: String, popUp: String) {
        navControllerRef?.get()?.navigate(route) {
            popUpTo(popUp) { inclusive = true }
        }
    }

    fun clear() {
        navControllerRef = null
    }
}