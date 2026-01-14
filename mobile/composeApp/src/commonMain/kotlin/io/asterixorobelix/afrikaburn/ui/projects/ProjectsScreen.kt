package io.asterixorobelix.afrikaburn.ui.projects

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.button_retry
import afrikaburn.composeapp.generated.resources.cd_error_icon
import afrikaburn.composeapp.generated.resources.cd_retry_button
import afrikaburn.composeapp.generated.resources.error_loading_projects
import afrikaburn.composeapp.generated.resources.loading_projects
import afrikaburn.composeapp.generated.resources.tab_art
import afrikaburn.composeapp.generated.resources.tab_camps
import afrikaburn.composeapp.generated.resources.tab_events
import afrikaburn.composeapp.generated.resources.tab_mobile_art
import afrikaburn.composeapp.generated.resources.tab_performances
import afrikaburn.composeapp.generated.resources.tab_vehicles
import io.asterixorobelix.afrikaburn.Dimens
import io.asterixorobelix.afrikaburn.di.koinProjectTabViewModel
import io.asterixorobelix.afrikaburn.di.koinProjectsViewModel
import io.asterixorobelix.afrikaburn.models.ProjectType
import io.asterixorobelix.afrikaburn.models.TimeFilter
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private const val CONTENT_STATE_LOADING = "loading"
private const val CONTENT_STATE_ERROR = "error"
private const val CONTENT_STATE_EMPTY = "empty"
private const val CONTENT_STATE_SUCCESS = "success"

@Composable
fun ProjectsScreen() {
    val viewModel = koinProjectsViewModel()
    val screenState by viewModel.screenUiState.collectAsState()

    val tabs = listOf(
        stringResource(Res.string.tab_art),
        stringResource(Res.string.tab_performances),
        stringResource(Res.string.tab_events),
        stringResource(Res.string.tab_mobile_art),
        stringResource(Res.string.tab_vehicles),
        stringResource(Res.string.tab_camps)
    )

    val pagerState = rememberPagerState(
        initialPage = screenState.currentTabIndex,
        pageCount = { screenState.tabs.size }
    )
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ProjectsTabRow(
            tabs = tabs,
            selectedTabIndex = pagerState.currentPage,
            onTabSelected = { index ->
                coroutineScope.launch {
                    pagerState.animateScrollToPage(index)
                    viewModel.updateCurrentTab(index)
                }
            }
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val projectType = screenState.tabs[page]
            ProjectTabContent(projectType = projectType)
        }
    }
}

@Composable
private fun ProjectsTabRow(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        edgePadding = Dimens.paddingMedium
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
    }
}

@Composable
private fun ProjectTabContent(projectType: ProjectType) {
    val tabViewModel = koinProjectTabViewModel(projectType)
    val uiState by tabViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search section with improved spacing
        ProjectSearchBar(
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = tabViewModel::updateSearchQuery,
            placeholderText = "Search ${projectType.displayName.lowercase()}..."
        )

        // Filter chips section (only for Camps) with visual separation
        if (projectType == ProjectType.CAMPS) {
            ProjectFilterChips(
                isFamilyFilterEnabled = uiState.isFamilyFilterEnabled,
                onToggleFamilyFilter = tabViewModel::toggleFamilyFilter,
                timeFilter = uiState.timeFilter,
                onTimeFilterChange = tabViewModel::updateTimeFilter
            )
        }

        // Spacer for visual breathing room before content
        Spacer(modifier = Modifier.height(Dimens.spacingSmall))

        // Animated content state transitions
        AnimatedContentState(
            isLoading = uiState.isLoading,
            error = uiState.error,
            isEmptySearch = uiState.isShowingEmptySearch(),
            searchQuery = uiState.searchQuery,
            projectType = projectType,
            isFamilyFilterEnabled = uiState.isFamilyFilterEnabled,
            timeFilter = uiState.timeFilter,
            filteredProjects = uiState.filteredProjects,
            onRetry = tabViewModel::retryLoading,
            onDismissError = tabViewModel::clearError,
            onClearSearch = tabViewModel::clearSearchQuery,
            onClearFilters = tabViewModel::clearFilters
        )
    }
}

@Composable
private fun AnimatedContentState(
    isLoading: Boolean,
    error: String?,
    isEmptySearch: Boolean,
    searchQuery: String,
    projectType: ProjectType,
    isFamilyFilterEnabled: Boolean,
    timeFilter: TimeFilter,
    filteredProjects: List<io.asterixorobelix.afrikaburn.models.ProjectItem>,
    onRetry: () -> Unit,
    onDismissError: () -> Unit,
    onClearSearch: () -> Unit,
    onClearFilters: () -> Unit
) {
    val contentState = when {
        isLoading -> CONTENT_STATE_LOADING
        error != null -> CONTENT_STATE_ERROR
        isEmptySearch -> CONTENT_STATE_EMPTY
        else -> CONTENT_STATE_SUCCESS
    }

    AnimatedContent(
        targetState = contentState,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(Dimens.animationDurationMedium)
            ) togetherWith fadeOut(
                animationSpec = tween(Dimens.animationDurationShort)
            )
        },
        label = "ContentStateAnimation"
    ) { state ->
        when (state) {
            CONTENT_STATE_LOADING -> LoadingContent()
            CONTENT_STATE_ERROR -> ErrorContent(
                error = error ?: "Unknown error",
                onRetry = onRetry
            )
            CONTENT_STATE_EMPTY -> {
                val emptyStateType = determineEmptyStateType(
                    searchQuery = searchQuery,
                    isFamilyFilterEnabled = isFamilyFilterEnabled,
                    timeFilter = timeFilter,
                    hasProjects = filteredProjects.isNotEmpty()
                )
                EmptyStateContent(
                    emptyStateType = emptyStateType,
                    projectType = projectType,
                    searchQuery = searchQuery,
                    hasActiveFilters = hasActiveFilters(isFamilyFilterEnabled, timeFilter),
                    onClearSearch = onClearSearch,
                    onClearFilters = onClearFilters
                )
            }
            CONTENT_STATE_SUCCESS -> ProjectList(projects = filteredProjects)
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingLarge)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimens.iconSizeLarge)
            )
            Text(
                text = stringResource(Res.string.loading_projects),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = stringResource(Res.string.cd_error_icon),
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(Dimens.iconSizeExtraLarge)
        )

        Spacer(modifier = Modifier.height(Dimens.spacingLarge))

        Text(
            text = stringResource(Res.string.error_loading_projects),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimens.spacingSmall))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimens.spacingExtraLarge))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(Res.string.cd_retry_button),
                modifier = Modifier.size(Dimens.iconSizeSmall)
            )
            Spacer(modifier = Modifier.width(Dimens.spacingSmall))
            Text(
                text = stringResource(Res.string.button_retry),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

