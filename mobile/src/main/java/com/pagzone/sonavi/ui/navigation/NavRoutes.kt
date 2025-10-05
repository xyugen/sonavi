package com.pagzone.sonavi.ui.navigation

import com.pagzone.sonavi.R
import kotlinx.serialization.Serializable

@Serializable
sealed class NavRoute(val route: String, val label: String, val iconRes: Int? = null) {
    @Serializable
    data object Home : NavRoute("home", "Home", R.drawable.ic_house)

    @Serializable
    data object AddSound : NavRoute("add_sound", "Add Sound", R.drawable.ic_circle_plus)

    @Serializable
    data object Library :
        NavRoute("library", "Library", R.drawable.ic_book_audio)

    @Serializable
    data object Profile :
        NavRoute("profile", "Profile", R.drawable.ic_user_round_cog)

    companion object {
        val bottomNavItems = listOf(Home, AddSound, Library, Profile)

        fun fromRoute(route: String?): NavRoute? = when (route) {
            Home.route -> Home
            AddSound.route -> AddSound
            Library.route -> Library
            Profile.route -> Profile
            else -> null
        }
    }
}