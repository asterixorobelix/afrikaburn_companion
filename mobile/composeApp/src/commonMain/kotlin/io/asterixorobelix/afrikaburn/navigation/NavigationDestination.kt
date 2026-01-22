package io.asterixorobelix.afrikaburn.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationDestination(
    val route: String,
    val title: String,
    val contentDescription: String,
    val icon: ImageVector
) {
    object Projects : NavigationDestination(
        route = "projects",
        title = "Projects",
        contentDescription = "Projects icon",
        icon = Icons.Default.Build
    )

    object Map : NavigationDestination(
        route = "map",
        title = "Map",
        contentDescription = "View map of event area",
        icon = Icons.Default.Place
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
        val allDestinations: List<NavigationDestination> by lazy {
            listOf(Projects, Map, Directions, About)
        }

        /**
         * Returns destinations visible based on unlock state.
         * When locked: Only Directions and About tabs are visible.
         * When unlocked: All tabs are visible.
         */
        fun getVisibleDestinations(isUnlocked: Boolean): List<NavigationDestination> {
            return if (isUnlocked) {
                allDestinations
            } else {
                listOf(Directions, About)
            }
        }

        /**
         * Tabs that require unlock to be visible.
         */
        val lockedDestinations: Set<NavigationDestination> = setOf(Projects, Map)
    }
}