package io.asterixorobelix.afrikaburn.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationDestination(
    val route: String,
    val title: String,
    val contentDescription: String,
    val icon: ImageVector
) {
    object Home : NavigationDestination(
        route = "home",
        title = "Home",
        contentDescription = "Home screen",
        icon = Icons.Outlined.Home
    )

    object Explore : NavigationDestination(
        route = "explore",
        title = "Explore",
        contentDescription = "Explore projects",
        icon = Icons.Outlined.Explore
    )

    object Map : NavigationDestination(
        route = "map",
        title = "Map",
        contentDescription = "View map of event area",
        icon = Icons.Outlined.Map
    )

    object More : NavigationDestination(
        route = "more",
        title = "More",
        contentDescription = "More options",
        icon = Icons.Outlined.MoreHoriz
    )

    object Directions : NavigationDestination(
        route = "directions",
        title = "Directions",
        contentDescription = "Directions icon",
        icon = Icons.Default.LocationOn
    )

    object About : NavigationDestination(
        route = "about",
        title = "About",
        contentDescription = "About icon",
        icon = Icons.Default.Info
    )

    companion object {
        val unlockedDestinations: List<NavigationDestination> by lazy {
            listOf(Home, Explore, Map, More)
        }

        val lockedDestinations: List<NavigationDestination> by lazy {
            listOf(Directions, About)
        }

        fun getVisibleDestinations(isUnlocked: Boolean): List<NavigationDestination> {
            return if (isUnlocked) {
                unlockedDestinations
            } else {
                lockedDestinations
            }
        }
    }
}
