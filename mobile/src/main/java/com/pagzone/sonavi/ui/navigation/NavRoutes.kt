package com.pagzone.sonavi.ui.navigation

import com.pagzone.sonavi.R
import kotlinx.serialization.Serializable

@Serializable
sealed class NavRoute(val route: String, val label: String, val iconRes: Int? = null) {
    @Serializable
    data object Home : NavRoute("home", "Home", R.drawable.ic_home)

    @Serializable
    data object AddSound : NavRoute("add_sound", "Add Sound", R.drawable.ic_add_circle)

    @Serializable
    data object Library :
        NavRoute("library", "Library", R.drawable.ic_music_note)

    @Serializable
    data object Profile :
        NavRoute("profile", "Profile")

    companion object {
        val bottomNavItems = listOf(Home, AddSound, Library)

        fun fromRoute(route: String?): NavRoute? = when (route) {
            Home.route -> Home
            AddSound.route -> AddSound
            Library.route -> Library
            else -> null
        }
    }
}