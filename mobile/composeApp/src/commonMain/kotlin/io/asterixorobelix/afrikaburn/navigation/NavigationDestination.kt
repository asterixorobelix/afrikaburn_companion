package io.asterixorobelix.afrikaburn.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
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
        val allDestinations = listOf(Projects, Directions, About)
    }
}