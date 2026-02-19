package io.asterixorobelix.afrikaburn

import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.unlock_welcome_message
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.asterixorobelix.afrikaburn.domain.service.UnlockConditionManager
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType
import io.asterixorobelix.afrikaburn.navigation.BottomNavigationBar
import io.asterixorobelix.afrikaburn.navigation.NavigationDestination
import io.asterixorobelix.afrikaburn.platform.CrashLogger
import io.asterixorobelix.afrikaburn.platform.FirebaseConfigChecker
import io.asterixorobelix.afrikaburn.platform.LocationData
import io.asterixorobelix.afrikaburn.platform.LocationService
import io.asterixorobelix.afrikaburn.platform.PermissionState
import io.asterixorobelix.afrikaburn.ui.directions.DirectionsScreen
import io.asterixorobelix.afrikaburn.ui.home.HomeScreen
import io.asterixorobelix.afrikaburn.ui.more.MoreScreen
import io.asterixorobelix.afrikaburn.ui.projects.ProjectDetailScreen
import io.asterixorobelix.afrikaburn.ui.projects.ProjectsScreen
import io.asterixorobelix.afrikaburn.ui.about.AboutScreen
import io.asterixorobelix.afrikaburn.di.koinMapViewModel
import io.asterixorobelix.afrikaburn.ui.screens.map.MapScreen
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

private const val PROJECT_DETAIL_ROUTE = "project_detail"
private const val PROJECT_TYPE_ARG = "projectType"

@Composable
@Preview
fun App() {
    // Dark mode is the primary experience per brand docs
    val isDarkTheme = true

    // Inject services
    val crashLogger: CrashLogger = koinInject()
    val unlockManager: UnlockConditionManager = koinInject()
    val locationService: LocationService = koinInject()

    // Location state for geofence check
    var currentLocation by remember { mutableStateOf<LocationData?>(null) }

    // Track if we showed welcome message this session
    var hasShownWelcome by rememberSaveable { mutableStateOf(false) }

    // Snackbar host state for welcome message
    val snackbarHostState = remember { SnackbarHostState() }

    // Get welcome message resource
    val welcomeMessage = stringResource(Res.string.unlock_welcome_message)

    // Initialize crash logging and check location
    InitializeAppServices(crashLogger, locationService) { location ->
        currentLocation = location
    }

    // Evaluate unlock state with current location
    val isUnlocked = unlockManager.isUnlocked(currentLocation)
    val visibleDestinations = NavigationDestination.getVisibleDestinations(isUnlocked)
    val startDestination = determineStartDestination(isUnlocked)

    // Show welcome message only on fresh unlock (happened THIS session, not from persistence)
    val shouldShowWelcome = isUnlocked && !hasShownWelcome && unlockManager.wasJustUnlocked()
    ShowWelcomeMessage(shouldShowWelcome, snackbarHostState, welcomeMessage) {
        hasShownWelcome = true
    }

    AppTheme(useDarkTheme = isDarkTheme) {
        AppScaffold(
            snackbarHostState = snackbarHostState,
            visibleDestinations = visibleDestinations,
            startDestination = startDestination,
            isUnlocked = isUnlocked
        )
    }
}

@Composable
private fun InitializeAppServices(
    crashLogger: CrashLogger,
    locationService: LocationService,
    onLocationReceived: (LocationData?) -> Unit
) {
    LaunchedEffect(Unit) {
        crashLogger.initialize()
        crashLogger.log("App started successfully")

        // Check Firebase configuration status
        FirebaseConfigChecker.logConfigurationStatus(crashLogger)

        // Try to get location for geofence check
        val permission = locationService.checkPermission()
        if (permission == PermissionState.GRANTED) {
            onLocationReceived(locationService.getCurrentLocation())
        }
    }
}

private fun determineStartDestination(isUnlocked: Boolean): String {
    return if (isUnlocked) {
        NavigationDestination.Home.route
    } else {
        NavigationDestination.Directions.route
    }
}

@Composable
private fun ShowWelcomeMessage(
    shouldShowWelcome: Boolean,
    snackbarHostState: SnackbarHostState,
    welcomeMessage: String,
    onWelcomeShown: () -> Unit
) {
    LaunchedEffect(shouldShowWelcome) {
        if (shouldShowWelcome) {
            snackbarHostState.showSnackbar(welcomeMessage)
            onWelcomeShown()
        }
    }
}

@Composable
private fun AppScaffold(
    snackbarHostState: SnackbarHostState,
    visibleDestinations: List<NavigationDestination>,
    startDestination: String,
    isUnlocked: Boolean
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val currentBaseRoute = remember(currentRoute) { currentRoute?.substringBefore("?") }

    // State holder for selected project (used for detail navigation)
    var selectedProject by remember { mutableStateOf<ProjectItem?>(null) }

    // Hide bottom bar on detail screens and sub-routes (Directions/About from More)
    val topLevelRoutes = remember(visibleDestinations) {
        visibleDestinations.map { it.route }.toSet()
    }
    val unlockedSubRoutes = remember {
        setOf(
            NavigationDestination.Directions.route,
            NavigationDestination.About.route
        )
    }
    val showBottomBar = remember(currentBaseRoute, topLevelRoutes, isUnlocked) {
        currentBaseRoute != null && (
            currentBaseRoute in topLevelRoutes ||
                (isUnlocked && currentBaseRoute in unlockedSubRoutes)
            )
    }

    Scaffold(
        modifier = Modifier
            .safeContentPadding()
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    destinations = visibleDestinations
                )
            }
        }
    ) { paddingValues ->
        AppNavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues),
            selectedProject = selectedProject,
            onProjectSelected = { selectedProject = it }
        )
    }
}

@Suppress("LongMethod")
@Composable
private fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier,
    selectedProject: ProjectItem?,
    onProjectSelected: (ProjectItem) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(NavigationDestination.Home.route) {
            HomeScreen(
                onCategoryClick = { projectType ->
                    navController.navigate(
                        "${NavigationDestination.Explore.route}?$PROJECT_TYPE_ARG=${projectType.name}"
                    ) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                onSurvivalGuideClick = {
                    navController.navigate(NavigationDestination.Directions.route)
                },
                onProjectClick = { project ->
                    onProjectSelected(project)
                    navController.navigate(PROJECT_DETAIL_ROUTE)
                }
            )
        }
        composable(
            route = "${NavigationDestination.Explore.route}?$PROJECT_TYPE_ARG={$PROJECT_TYPE_ARG}",
            arguments = listOf(
                navArgument(PROJECT_TYPE_ARG) {
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val projectTypeArg = backStackEntry.arguments?.getString(PROJECT_TYPE_ARG)
            val initialProjectType = projectTypeArg?.let { arg ->
                ProjectType.entries.firstOrNull { it.name == arg }
            }
            ProjectsScreen(
                initialProjectType = initialProjectType,
                onProjectClick = { project ->
                    onProjectSelected(project)
                    navController.navigate(PROJECT_DETAIL_ROUTE)
                }
            )
        }
        composable(NavigationDestination.Map.route) {
            MapScreen(
                onProjectClick = { project ->
                    onProjectSelected(project)
                    navController.navigate(PROJECT_DETAIL_ROUTE)
                }
            )
        }
        composable(NavigationDestination.More.route) {
            MoreScreen(
                onDirectionsClick = {
                    navController.navigate(NavigationDestination.Directions.route)
                },
                onAboutClick = {
                    navController.navigate(NavigationDestination.About.route)
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
                val mapViewModel = koinMapViewModel()
                ProjectDetailScreen(
                    project = project,
                    onBackClick = { navController.popBackStack() },
                    onShowOnMap = { lat, lng ->
                        mapViewModel.navigateToLocation(lat, lng)
                        navController.navigate(NavigationDestination.Map.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
