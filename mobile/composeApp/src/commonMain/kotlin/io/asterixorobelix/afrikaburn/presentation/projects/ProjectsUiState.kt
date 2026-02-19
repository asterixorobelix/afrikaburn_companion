package io.asterixorobelix.afrikaburn.presentation.projects

import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.TimeFilter

sealed interface ProjectsUiState {
    data object Loading : ProjectsUiState

    data class Content(
        val projects: List<ProjectItem> = emptyList(),
        val filteredProjects: List<ProjectItem> = emptyList(),
        val searchQuery: String = "",
        val isFamilyFilterEnabled: Boolean = false,
        val timeFilter: TimeFilter = TimeFilter.ALL,
        val isRefreshing: Boolean = false
    ) : ProjectsUiState {
        val totalProjectCount: Int get() = projects.size

        fun isShowingResults(): Boolean = !isRefreshing

        fun isShowingEmptySearch(): Boolean =
            isShowingResults() && hasActiveFilters() && filteredProjects.isEmpty()

        fun hasActiveFilters(): Boolean =
            searchQuery.isNotEmpty() || isFamilyFilterEnabled || timeFilter != TimeFilter.ALL
    }

    data class Empty(val content: Content) : ProjectsUiState

    data class Error(val message: String, val content: Content) : ProjectsUiState
}

fun ProjectsUiState.contentOrDefault(): ProjectsUiState.Content {
    return when (this) {
        is ProjectsUiState.Content -> this
        is ProjectsUiState.Empty -> content
        is ProjectsUiState.Error -> content
        ProjectsUiState.Loading -> ProjectsUiState.Content()
    }
}
