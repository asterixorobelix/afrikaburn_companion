package io.asterixorobelix.afrikaburn.presentation.projects

import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.TimeFilter

data class ProjectsUiState(
    val projects: List<ProjectItem> = emptyList(),
    val filteredProjects: List<ProjectItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val isFamilyFilterEnabled: Boolean = false,
    val timeFilter: TimeFilter = TimeFilter.ALL
) {
    fun isShowingResults(): Boolean = !isLoading && error == null
    
    fun isShowingEmptySearch(): Boolean = 
        isShowingResults() && hasActiveFilters() && filteredProjects.isEmpty()
    
    fun hasActiveFilters(): Boolean = 
        searchQuery.isNotEmpty() || isFamilyFilterEnabled || timeFilter != TimeFilter.ALL
}