package io.asterixorobelix.afrikaburn.presentation.projects

import io.asterixorobelix.afrikaburn.models.ProjectItem

data class ProjectsUiState(
    val projects: List<ProjectItem> = emptyList(),
    val filteredProjects: List<ProjectItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val isFamilyFilterEnabled: Boolean = false
) {
    fun isShowingResults(): Boolean = !isLoading && error == null
    
    fun isShowingEmptySearch(): Boolean = 
        isShowingResults() && (searchQuery.isNotEmpty() || isFamilyFilterEnabled) && filteredProjects.isEmpty()
    
    fun hasActiveFilters(): Boolean = searchQuery.isNotEmpty() || isFamilyFilterEnabled
}