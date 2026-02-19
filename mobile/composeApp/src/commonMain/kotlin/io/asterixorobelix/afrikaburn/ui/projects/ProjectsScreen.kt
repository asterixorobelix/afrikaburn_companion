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
import androidx.compose.runtime.LaunchedEffect
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
import io.asterixorobelix.afrikaburn.presentation.projects.ProjectsScreenUiState
import io.asterixorobelix.afrikaburn.presentation.projects.ProjectsUiState
import io.asterixorobelix.afrikaburn.presentation.projects.contentOrDefault
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private const val CONTENT_STATE_LOADING = "loading"
private const val CONTENT_STATE_ERROR = "error"
private const val CONTENT_STATE_EMPTY = "empty"
private const val CONTENT_STATE_SUCCESS = "success"

@Composable
fun ProjectsScreen(
    initialProjectType: ProjectType? = null,
    onInitialProjectTypeConsumed: () -> Unit = {},
    onProjectClick: ((io.asterixorobelix.afrikaburn.models.ProjectItem) -> Unit)? = null
) {
    val viewModel = koinProjectsViewModel()
    val screenState by viewModel.screenUiState.collectAsState()
    val screenContent = resolveScreenContent(screenState)
    val tabTitles = projectsTabTitles()
    val tabLabels = rememberTabLabels(screenContent, tabTitles)

    val pagerState = rememberPagerState(
        initialPage = screenContent.currentTabIndex,
        pageCount = { screenContent.tabs.size }
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(initialProjectType, screenContent.tabs) {
        val index = initialProjectType?.let { screenContent.tabs.indexOf(it) } ?: -1
        if (index >= 0) {
            if (index != pagerState.currentPage) {
                pagerState.scrollToPage(index)
                viewModel.updateCurrentTab(index)
            }
            onInitialProjectTypeConsumed()
        }
    }

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
            val projectType = screenContent.tabs[page]
            ProjectTabContent(
                projectType = projectType,
                onProjectClick = onProjectClick
            )
        }
    }
}

@Composable
private fun resolveScreenContent(
    screenState: ProjectsScreenUiState
): ProjectsScreenUiState.Content {
    return when (screenState) {
        is ProjectsScreenUiState.Content ->
            screenState
        is ProjectsScreenUiState.Empty ->
            screenState.content
        is ProjectsScreenUiState.Error ->
            screenState.content
        ProjectsScreenUiState.Loading ->
            ProjectsScreenUiState.Content()
    }
}

@Composable
private fun projectsTabTitles(): List<String> {
    return listOf(
        stringResource(Res.string.tab_art),
        stringResource(Res.string.tab_performances),
        stringResource(Res.string.tab_events),
        stringResource(Res.string.tab_mobile_art),
        stringResource(Res.string.tab_vehicles),
        stringResource(Res.string.tab_camps)
    )
}

@Composable
private fun rememberTabLabels(
    screenContent: ProjectsScreenUiState.Content,
    tabTitles: List<String>
): List<String> {
    return screenContent.tabs.mapIndexed { index, projectType ->
        val tabVm = koinProjectTabViewModel(projectType)
        val tabState by tabVm.uiState.collectAsState()
        val count = (tabState as? ProjectsUiState.Content)?.totalProjectCount ?: 0
        val title = tabTitles[index]
        if (count > 0) "$title ($count)" else title
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
    val contentState = uiState.contentOrDefault()
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ProjectSearchBar(
            searchQuery = contentState.searchQuery,
            onSearchQueryChange = tabViewModel::updateSearchQuery,
            placeholderText = "Search ${projectType.displayName.lowercase()}..."
        )

        if (projectType == ProjectType.CAMPS) {
            ProjectFilterChips(
                isFamilyFilterEnabled = contentState.isFamilyFilterEnabled,
                onToggleFamilyFilter = tabViewModel::toggleFamilyFilter,
                timeFilter = contentState.timeFilter,
                onTimeFilterChange = tabViewModel::updateTimeFilter
            )
        }

        Spacer(modifier = Modifier.height(Dimens.spacingSmall))

        if (contentState.isShowingResults() && contentState.hasActiveFilters()) {
            Text(
                text = stringResource(
                    Res.string.a11y_search_results_count,
                    contentState.filteredProjects.size
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
            contentState = contentState,
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
    contentState: ProjectsUiState.Content,
    projectType: ProjectType,
    listState: LazyListState,
    onRetry: () -> Unit,
    onClearSearch: () -> Unit,
    onClearFilters: () -> Unit,
    onProjectClick: ((io.asterixorobelix.afrikaburn.models.ProjectItem) -> Unit)? = null
) {
    val contentStateKey = when (uiState) {
        ProjectsUiState.Loading -> CONTENT_STATE_LOADING
        is ProjectsUiState.Error -> CONTENT_STATE_ERROR
        is ProjectsUiState.Empty -> CONTENT_STATE_EMPTY
        is ProjectsUiState.Content -> {
            if (contentState.isShowingEmptySearch()) CONTENT_STATE_EMPTY else CONTENT_STATE_SUCCESS
        }
    }

    AnimatedContent(
        targetState = contentStateKey,
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
                error = (uiState as? ProjectsUiState.Error)?.message ?: "Unknown error",
                onRetry = onRetry
            )
            CONTENT_STATE_EMPTY -> {
                val emptyStateType = determineEmptyStateType(
                    searchQuery = contentState.searchQuery,
                    isFamilyFilterEnabled = contentState.isFamilyFilterEnabled,
                    timeFilter = contentState.timeFilter,
                    hasProjects = contentState.filteredProjects.isNotEmpty()
                )
                EmptyStateContent(
                    emptyStateType = emptyStateType,
                    projectType = projectType,
                    searchQuery = contentState.searchQuery,
                    hasActiveFilters = contentState.hasActiveFilters(),
                    onClearSearch = onClearSearch,
                    onClearFilters = onClearFilters
                )
            }
            CONTENT_STATE_SUCCESS -> ProjectList(
                projects = contentState.filteredProjects,
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
