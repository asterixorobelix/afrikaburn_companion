package io.asterixorobelix.afrikaburn.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationDestination(
    val route: String,
    val title: String,
    val contentDescription: String,
    val icon: ImageVector
) {
    object Discovery : NavigationDestination(
        route = "discovery",
        title = "Discovery",
        contentDescription = "Discovery icon",
        icon = Icons.Default.Explore
    )
    
    object Map : NavigationDestination(
        route = "map",
        title = "Map",
        contentDescription = "Map icon",
        icon = Icons.Default.Map
    )
    
    object Schedule : NavigationDestination(
        route = "schedule", 
        title = "Schedule",
        contentDescription = "Schedule icon",
        icon = Icons.Default.CalendarMonth
    )
    
    object Safety : NavigationDestination(
        route = "safety",
        title = "Safety",
        contentDescription = "Safety icon",
        icon = Icons.Default.Shield
    )
    
    object About : NavigationDestination(
        route = "about",
        title = "About",
        contentDescription = "About icon",
        icon = Icons.Default.Info
    )
    
    // Deprecated screens, kept for backwards compatibility
    @Deprecated("Use Discovery instead", ReplaceWith("Discovery"))
    object Projects : NavigationDestination(
        route = "projects",
        title = "Projects",
        contentDescription = "Projects icon",
        icon = Icons.Default.Build
    )
    
    @Deprecated("Use Map instead", ReplaceWith("Map"))
    object Directions : NavigationDestination(
        route = "directions", 
        title = "Directions",
        contentDescription = "Directions icon",
        icon = Icons.Default.LocationOn
    )
    
    companion object {
        val allDestinations = listOf(Discovery, Map, Schedule, Safety, About)
        val deprecatedDestinations = listOf(Projects, Directions)
    }
}