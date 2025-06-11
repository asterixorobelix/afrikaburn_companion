package io.asterixorobelix.afrikaburn.ui.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import io.asterixorobelix.afrikaburn.Dimens
import io.asterixorobelix.afrikaburn.di.koinProjectsViewModel
import io.asterixorobelix.afrikaburn.di.koinProjectTabViewModel
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType
import io.asterixorobelix.afrikaburn.models.TimeFilter
import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.tab_art
import afrikaburn.composeapp.generated.resources.tab_camps
import afrikaburn.composeapp.generated.resources.tab_events
import afrikaburn.composeapp.generated.resources.tab_mobile_art
import afrikaburn.composeapp.generated.resources.tab_performances
import afrikaburn.composeapp.generated.resources.tab_vehicles
import afrikaburn.composeapp.generated.resources.cd_search_icon
import afrikaburn.composeapp.generated.resources.cd_clear_search
import afrikaburn.composeapp.generated.resources.loading_projects
import afrikaburn.composeapp.generated.resources.error_loading_projects
import afrikaburn.composeapp.generated.resources.button_retry
import afrikaburn.composeapp.generated.resources.cd_retry_button
import afrikaburn.composeapp.generated.resources.cd_error_icon
import afrikaburn.composeapp.generated.resources.cd_no_results_icon
import afrikaburn.composeapp.generated.resources.no_results_found
import afrikaburn.composeapp.generated.resources.no_results_for_query
import afrikaburn.composeapp.generated.resources.cd_artist_icon
import afrikaburn.composeapp.generated.resources.filter_family_friendly_short
import afrikaburn.composeapp.generated.resources.cd_family_filter
import afrikaburn.composeapp.generated.resources.filter_time_all
import afrikaburn.composeapp.generated.resources.filter_time_daytime
import afrikaburn.composeapp.generated.resources.filter_time_nighttime
import afrikaburn.composeapp.generated.resources.cd_time_filter

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
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                            viewModel.updateCurrentTab(index)
                        }
                    },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
            }
        }
        
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
private fun ProjectTabContent(projectType: ProjectType) {
    val tabViewModel = koinProjectTabViewModel(projectType)
    val uiState by tabViewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search bar
        SearchBar(
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = tabViewModel::updateSearchQuery,
            placeholderText = "Search ${projectType.displayName.lowercase()}..."
        )
        
        // Filter chips (only for Camps)
        if (projectType == ProjectType.CAMPS) {
            FilterChipsRow(
                isFamilyFilterEnabled = uiState.isFamilyFilterEnabled,
                onToggleFamilyFilter = tabViewModel::toggleFamilyFilter,
                timeFilter = uiState.timeFilter,
                onTimeFilterChange = tabViewModel::updateTimeFilter,
                modifier = Modifier.padding(horizontal = Dimens.paddingMedium)
            )
        }
        
        // Content based on state
        when {
            uiState.isLoading -> {
                LoadingContent()
            }
            uiState.error != null -> {
                ErrorContent(
                    error = uiState.error ?: "Unknown error",
                    onRetry = tabViewModel::retryLoading,
                    onDismissError = tabViewModel::clearError
                )
            }
            uiState.isShowingEmptySearch() -> {
                EmptySearchContent(
                    searchQuery = uiState.searchQuery,
                    projectType = projectType.displayName,
                    isFamilyFilterEnabled = uiState.isFamilyFilterEnabled,
                    timeFilter = uiState.timeFilter
                )
            }
            else -> {
                ProjectList(projects = uiState.filteredProjects)
            }
        }
    }
}

@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    placeholderText: String
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimens.paddingMedium,
                vertical = Dimens.paddingSmall
            ),
        placeholder = {
            Text(
                text = placeholderText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(Res.string.cd_search_icon),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(Res.string.cd_clear_search),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        textStyle = MaterialTheme.typography.bodyMedium,
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
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
    onRetry: () -> Unit,
    onDismissError: () -> Unit
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
            modifier = Modifier
                .size(Dimens.iconSizeExtraLarge)
                .padding(bottom = Dimens.paddingMedium)
        )
        
        Text(
            text = stringResource(Res.string.error_loading_projects),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = Dimens.paddingSmall)
        )
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = Dimens.paddingLarge)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
        ) {
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
                Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                Text(
                    text = stringResource(Res.string.button_retry),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun EmptySearchContent(
    searchQuery: String,
    projectType: String,
    isFamilyFilterEnabled: Boolean = false,
    timeFilter: TimeFilter = TimeFilter.ALL
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = stringResource(Res.string.cd_no_results_icon),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(Dimens.iconSizeExtraLarge)
                .padding(bottom = Dimens.paddingMedium)
        )
        
        Text(
            text = stringResource(Res.string.no_results_found, projectType.lowercase()),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = Dimens.paddingSmall)
        )
        
        Text(
            text = when {
                searchQuery.isNotEmpty() && isFamilyFilterEnabled && timeFilter != TimeFilter.ALL -> 
                    "No results for \"$searchQuery\" with filters"
                searchQuery.isNotEmpty() && isFamilyFilterEnabled -> 
                    "No results for \"$searchQuery\" with family filter"
                searchQuery.isNotEmpty() && timeFilter != TimeFilter.ALL -> 
                    "No results for \"$searchQuery\" with ${timeFilter.displayName.lowercase()} filter"
                searchQuery.isNotEmpty() -> 
                    stringResource(Res.string.no_results_for_query, searchQuery)
                isFamilyFilterEnabled && timeFilter != TimeFilter.ALL -> 
                    "No family-friendly ${timeFilter.displayName.lowercase()} ${projectType.lowercase()} found"
                isFamilyFilterEnabled -> 
                    "No family-friendly ${projectType.lowercase()} found"
                timeFilter != TimeFilter.ALL -> 
                    "No ${timeFilter.displayName.lowercase()} ${projectType.lowercase()} found"
                else -> 
                    "No ${projectType.lowercase()} found"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProjectList(projects: List<ProjectItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = Dimens.paddingMedium,
            vertical = Dimens.paddingSmall
        )
    ) {
        items(projects) { project ->
            ProjectCard(project = project)
        }
    }
}

@Composable
private fun ProjectCard(project: ProjectItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationSmall)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingLarge)
        ) {
            ProjectCardHeader(project = project)
            ProjectCardDescription(description = project.description)
            ProjectCardStatusBadge(status = project.status)
        }
    }
}

@Composable
private fun ProjectCardHeader(project: ProjectItem) {
    Text(
        text = project.name,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = Dimens.paddingSmall)
    )
    
    if (project.artist.name.isNotEmpty()) {
        ProjectCardArtistInfo(artistName = project.artist.name)
        Divider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(bottom = Dimens.paddingSmall)
        )
    }
}

@Composable
private fun ProjectCardArtistInfo(artistName: String) {
    Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = Dimens.paddingSmall)
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = stringResource(Res.string.cd_artist_icon),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimens.paddingMedium)
        )
        Spacer(modifier = Modifier.width(Dimens.paddingSmall))
        Text(
            text = artistName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProjectCardDescription(description: String) {
    Text(
        text = description,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
    )
}

@Composable
private fun ProjectCardStatusBadge(status: String) {
    if (status.isNotEmpty()) {
        Spacer(modifier = Modifier.height(Dimens.paddingMedium))
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(
                    horizontal = Dimens.paddingSmall,
                    vertical = Dimens.paddingExtraSmall
                )
            )
        }
    }
}

@Composable
private fun FilterChipsRow(
    isFamilyFilterEnabled: Boolean,
    onToggleFamilyFilter: () -> Unit,
    timeFilter: TimeFilter,
    onTimeFilterChange: (TimeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = Dimens.paddingSmall),
        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
    ) {
        // Family filter chip
        FilterChip(
            onClick = onToggleFamilyFilter,
            label = {
                Text(
                    text = stringResource(Res.string.filter_family_friendly_short),
                    style = MaterialTheme.typography.labelMedium
                )
            },
            selected = isFamilyFilterEnabled,
            colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                containerColor = MaterialTheme.colorScheme.surface,
                labelColor = MaterialTheme.colorScheme.onSurface
            ),
            border = androidx.compose.material3.FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isFamilyFilterEnabled,
                borderColor = MaterialTheme.colorScheme.outline,
                selectedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
        
        // Time filter chips
        TimeFilter.values().forEach { filter ->
            FilterChip(
                onClick = { onTimeFilterChange(filter) },
                label = {
                    Text(
                        text = when (filter) {
                            TimeFilter.ALL -> stringResource(Res.string.filter_time_all)
                            TimeFilter.DAYTIME -> stringResource(Res.string.filter_time_daytime)
                            TimeFilter.NIGHTTIME -> stringResource(Res.string.filter_time_nighttime)
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                selected = timeFilter == filter,
                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                border = androidx.compose.material3.FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = timeFilter == filter,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    }
}

