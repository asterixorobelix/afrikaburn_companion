package io.asterixorobelix.afrikaburn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.asterixorobelix.afrikaburn.navigation.BottomNavigationBar
import io.asterixorobelix.afrikaburn.navigation.NavigationDestination
import io.asterixorobelix.afrikaburn.platform.CrashLogger
import io.asterixorobelix.afrikaburn.platform.FirebaseConfigChecker
import io.asterixorobelix.afrikaburn.ui.directions.DirectionsScreen
import io.asterixorobelix.afrikaburn.ui.projects.ProjectsScreen
import io.asterixorobelix.afrikaburn.ui.about.AboutScreen
import io.asterixorobelix.afrikaburn.ui.discovery.EventDiscoveryScreen
import io.asterixorobelix.afrikaburn.ui.map.OfflineMapScreen
import io.asterixorobelix.afrikaburn.ui.schedule.PersonalScheduleScreen
import io.asterixorobelix.afrikaburn.ui.safety.SafetyScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    val isDarkTheme = isSystemInDarkTheme()

    // Initialize crash logging
    val crashLogger: CrashLogger = koinInject()
    LaunchedEffect(Unit) {
        crashLogger.initialize()
        crashLogger.log("App started successfully")
        
        // Check Firebase configuration status
        FirebaseConfigChecker.logConfigurationStatus(crashLogger)
    }
    
    AppTheme(useDarkTheme = isDarkTheme) {
            val navController = rememberNavController()
            val currentBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = currentBackStackEntry?.destination?.route
            
            Scaffold(
                modifier = Modifier
                    .safeContentPadding()
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                bottomBar = {
                    BottomNavigationBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = NavigationDestination.Discovery.route,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable(NavigationDestination.Discovery.route) {
                        EventDiscoveryScreen()
                    }
                    composable(NavigationDestination.Map.route) {
                        OfflineMapScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable(NavigationDestination.Schedule.route) {
                        PersonalScheduleScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToEvent = { eventId ->
                                // Navigate to event detail screen when implemented
                            },
                            onNavigateToAddItem = {
                                // Navigate to add item screen when implemented
                            }
                        )
                    }
                    composable(NavigationDestination.Safety.route) {
                        SafetyScreen()
                    }
                    composable(NavigationDestination.About.route) {
                        AboutScreen()
                    }
                    
                    // Keep deprecated routes for backwards compatibility
                    composable(NavigationDestination.Projects.route) {
                        ProjectsScreen()
                    }
                    composable(NavigationDestination.Directions.route) {
                        DirectionsScreen()
                    }
                }
            }
        }
    }