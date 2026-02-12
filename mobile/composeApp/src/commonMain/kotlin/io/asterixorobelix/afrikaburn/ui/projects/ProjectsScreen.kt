package io.asterixorobelix.afrikaburn.ui.projects

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.a11y_search_results_count
import afrikaburn.composeapp.generated.resources.button_retry
import afrikaburn.composeapp.generated.resources.cd_error_icon
import afrikaburn.composeapp.generated.resources.cd_retry_button
import afrikaburn.composeapp.generated.resources.error_loading_projects
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
import io.asterixorobelix.afrikaburn.presentation.projects.ProjectsUiState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private const val CONTENT_STATE_LOADING = "loading"
private const val CONTENT_STATE_ERROR = "error"
private const val CONTENT_STATE_EMPTY = "empty"
private const val CONTENT_STATE_SUCCESS = "success"

@Composable
fun ProjectsScreen(
    onProjectClick: ((io.asterixorobelix.afrikaburn.models.ProjectItem) -> Unit)? = null
) {
    val viewModel = koinProjectsViewModel()
    val screenState by viewModel.screenUiState.collectAsState()

    val tabTitles = listOf(
        stringResource(Res.string.tab_art),
        stringResource(Res.string.tab_performances),
        stringResource(Res.string.tab_events),
        stringResource(Res.string.tab_mobile_art),
        stringResource(Res.string.tab_vehicles),
        stringResource(Res.string.tab_camps)
    )

    // Collect project counts for each tab
    val tabLabels = screenState.tabs.mapIndexed { index, projectType ->
        val tabVm = viewModel.getTabViewModel(projectType)
        val tabState by tabVm.uiState.collectAsState()
        val count = tabState.totalProjectCount
        val title = tabTitles[index]
        if (count > 0) "$title ($count)" else title
    }

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
            tabs = tabLabels,
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
            ProjectTabContent(
                projectType = projectType,
                onProjectClick = onProjectClick
            )
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
private fun ProjectTabContent(
    projectType: ProjectType,
    onProjectClick: ((io.asterixorobelix.afrikaburn.models.ProjectItem) -> Unit)? = null
) {
    val tabViewModel = koinProjectTabViewModel(projectType)
    val uiState by tabViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ProjectSearchBar(
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = tabViewModel::updateSearchQuery,
            placeholderText = "Search ${projectType.displayName.lowercase()}..."
        )

        if (projectType == ProjectType.CAMPS) {
            ProjectFilterChips(
                isFamilyFilterEnabled = uiState.isFamilyFilterEnabled,
                onToggleFamilyFilter = tabViewModel::toggleFamilyFilter,
                timeFilter = uiState.timeFilter,
                onTimeFilterChange = tabViewModel::updateTimeFilter
            )
        }

        Spacer(modifier = Modifier.height(Dimens.spacingSmall))

        if (uiState.isShowingResults() && uiState.hasActiveFilters()) {
            Text(
                text = stringResource(
                    Res.string.a11y_search_results_count,
                    uiState.filteredProjects.size
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = Dimens.paddingMedium)
                    .semantics { liveRegion = LiveRegionMode.Polite }
            )

            Spacer(modifier = Modifier.height(Dimens.spacingSmall))
        }

        AnimatedContentState(
            uiState = uiState,
            projectType = projectType,
            listState = listState,
            onRetry = tabViewModel::retryLoading,
            onClearSearch = tabViewModel::clearSearchQuery,
            onClearFilters = tabViewModel::clearFilters,
            onProjectClick = onProjectClick
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun AnimatedContentState(
    uiState: ProjectsUiState,
    projectType: ProjectType,
    listState: LazyListState,
    onRetry: () -> Unit,
    onClearSearch: () -> Unit,
    onClearFilters: () -> Unit,
    onProjectClick: ((io.asterixorobelix.afrikaburn.models.ProjectItem) -> Unit)? = null
) {
    val contentState = when {
        uiState.isLoading -> CONTENT_STATE_LOADING
        uiState.error != null -> CONTENT_STATE_ERROR
        uiState.isShowingEmptySearch() -> CONTENT_STATE_EMPTY
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
                error = uiState.error ?: "Unknown error",
                onRetry = onRetry
            )
            CONTENT_STATE_EMPTY -> {
                val emptyStateType = determineEmptyStateType(
                    searchQuery = uiState.searchQuery,
                    isFamilyFilterEnabled = uiState.isFamilyFilterEnabled,
                    timeFilter = uiState.timeFilter,
                    hasProjects = uiState.filteredProjects.isNotEmpty()
                )
                EmptyStateContent(
                    emptyStateType = emptyStateType,
                    projectType = projectType,
                    searchQuery = uiState.searchQuery,
                    hasActiveFilters = uiState.hasActiveFilters(),
                    onClearSearch = onClearSearch,
                    onClearFilters = onClearFilters
                )
            }
            CONTENT_STATE_SUCCESS -> ProjectList(
                projects = uiState.filteredProjects,
                listState = listState,
                onProjectClick = onProjectClick
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    ProjectListSkeleton()
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

