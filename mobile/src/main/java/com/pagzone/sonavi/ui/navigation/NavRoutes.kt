package com.pagzone.sonavi.ui.navigation

import com.pagzone.sonavi.R
import kotlinx.serialization.Serializable

@Serializable
sealed class NavRoute(val route: String, val label: String, val iconRes: Int) {
    @Serializable
    data object Home : NavRoute("home", "Home", R.drawable.ic_home)

    @Serializable
    data object AddSound : NavRoute("add_sound", "Add Sound", R.drawable.ic_add_circle)

    @Serializable
    data object Library :
        NavRoute("library", "Library", R.drawable.ic_music_note)

    companion object {
        val bottomNavItems = listOf(Home, AddSound, Library)
    }
}