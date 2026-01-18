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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.navigation.BottomNavigationBar
import io.asterixorobelix.afrikaburn.navigation.NavigationDestination
import io.asterixorobelix.afrikaburn.platform.CrashLogger
import io.asterixorobelix.afrikaburn.platform.FirebaseConfigChecker
import io.asterixorobelix.afrikaburn.ui.directions.DirectionsScreen
import io.asterixorobelix.afrikaburn.ui.projects.ProjectDetailScreen
import io.asterixorobelix.afrikaburn.ui.projects.ProjectsScreen
import io.asterixorobelix.afrikaburn.ui.about.AboutScreen
import io.asterixorobelix.afrikaburn.ui.screens.map.MapScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

private const val PROJECT_DETAIL_ROUTE = "project_detail"

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

            // State holder for selected project (used for detail navigation)
            var selectedProject by remember { mutableStateOf<ProjectItem?>(null) }

            // Determine if bottom bar should be shown (hide on detail screens)
            val showBottomBar = remember(currentRoute) {
                currentRoute != null && !currentRoute.startsWith(PROJECT_DETAIL_ROUTE)
            }

            Scaffold(
                modifier = Modifier
                    .safeContentPadding()
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                bottomBar = {
                    if (showBottomBar) {
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
                }
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = NavigationDestination.Projects.route,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable(NavigationDestination.Projects.route) {
                        ProjectsScreen(
                            onProjectClick = { project ->
                                selectedProject = project
                                navController.navigate(PROJECT_DETAIL_ROUTE)
                            }
                        )
                    }
                    composable(NavigationDestination.Map.route) {
                        MapScreen(
                            onProjectClick = { project ->
                                selectedProject = project
                                navController.navigate(PROJECT_DETAIL_ROUTE)
                            }
                        )
                    }
                    composable(NavigationDestination.Directions.route) {
                        DirectionsScreen()
                    }
                    composable(NavigationDestination.About.route) {
                        AboutScreen()
                    }
                    composable(route = PROJECT_DETAIL_ROUTE) {
                        selectedProject?.let { project ->
                            ProjectDetailScreen(
                                project = project,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }