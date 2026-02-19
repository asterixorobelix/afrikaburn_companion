package io.asterixorobelix.afrikaburn.presentation.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.asterixorobelix.afrikaburn.domain.usecase.projects.GetProjectsByTypeUseCase
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType
import io.asterixorobelix.afrikaburn.models.TimeFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProjectTabViewModel(
    private val getProjectsByTypeUseCase: GetProjectsByTypeUseCase,
    private val projectType: ProjectType
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProjectsUiState>(ProjectsUiState.Loading)
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()

    init {
        loadProjects()
    }

    fun loadProjects() {
        val content = currentContent()
        if (content.isRefreshing) return

        viewModelScope.launch {
            _uiState.value = if (_uiState.value is ProjectsUiState.Content) {
                content.copy(isRefreshing = true)
            } else {
                ProjectsUiState.Loading
            }

            getProjectsByTypeUseCase(GetProjectsByTypeUseCase.Params(projectType))
                .onSuccess { projects ->
                    val updatedContent = content.copy(
                        projects = projects,
                        filteredProjects = filterProjects(
                            projects = projects,
                            searchQuery = content.searchQuery,
                            familyFilter = content.isFamilyFilterEnabled,
                            timeFilter = content.timeFilter
                        ),
                        isRefreshing = false
                    )
                    _uiState.value = if (projects.isEmpty()) {
                        ProjectsUiState.Empty(updatedContent)
                    } else {
                        updatedContent
                    }
                }
                .onFailure { exception ->
                    val errorMessage = exception.message ?: "Unknown error occurred"
                    _uiState.value = ProjectsUiState.Error(
                        message = errorMessage,
                        content = content.copy(isRefreshing = false)
                    )
                }
        }
    }

    fun updateSearchQuery(query: String) {
        val content = currentContent()
        _uiState.value = content.copy(
            searchQuery = query,
            filteredProjects = filterProjects(
                projects = content.projects,
                searchQuery = query,
                familyFilter = content.isFamilyFilterEnabled,
                timeFilter = content.timeFilter
            )
        )
    }
    
    fun toggleFamilyFilter() {
        val content = currentContent()
        val newFamilyFilter = !content.isFamilyFilterEnabled
        _uiState.value = content.copy(
            isFamilyFilterEnabled = newFamilyFilter,
            filteredProjects = filterProjects(
                projects = content.projects,
                searchQuery = content.searchQuery,
                familyFilter = newFamilyFilter,
                timeFilter = content.timeFilter
            )
        )
    }
    
    fun updateTimeFilter(timeFilter: TimeFilter) {
        val content = currentContent()
        _uiState.value = content.copy(
            timeFilter = timeFilter,
            filteredProjects = filterProjects(
                projects = content.projects,
                searchQuery = content.searchQuery,
                familyFilter = content.isFamilyFilterEnabled,
                timeFilter = timeFilter
            )
        )
    }

    fun clearError() {
        val content = currentContent()
        _uiState.value = content.copy(isRefreshing = false)
    }

    fun retryLoading() {
        loadProjects()
    }

    fun clearSearchQuery() {
        updateSearchQuery("")
    }

    fun clearFilters() {
        val content = currentContent()
        _uiState.value = content.copy(
            isFamilyFilterEnabled = false,
            timeFilter = TimeFilter.ALL,
            filteredProjects = filterProjects(
                projects = content.projects,
                searchQuery = content.searchQuery,
                familyFilter = false,
                timeFilter = TimeFilter.ALL
            )
        )
    }

    private fun filterProjects(
        projects: List<ProjectItem>, 
        searchQuery: String,
        familyFilter: Boolean,
        timeFilter: TimeFilter
    ): List<ProjectItem> {
        var filteredProjects = projects
        
        // Apply family filter first
        if (familyFilter) {
            filteredProjects = filteredProjects.filter { it.isFamilyFriendly }
        }
        
        // Apply time filter
        if (timeFilter != TimeFilter.ALL) {
            filteredProjects = filteredProjects.filter { it.matchesTimeFilter(timeFilter) }
        }
        
        // Apply search query filter last
        if (searchQuery.isNotEmpty()) {
            filteredProjects = filteredProjects.filter { project ->
                project.name.contains(searchQuery, ignoreCase = true) ||
                project.description.contains(searchQuery, ignoreCase = true) ||
                project.artist.name.contains(searchQuery, ignoreCase = true)
            }
        }
        
        return filteredProjects
    }

    private fun currentContent(): ProjectsUiState.Content {
        return when (val state = _uiState.value) {
            is ProjectsUiState.Content -> state
            is ProjectsUiState.Empty -> state.content
            is ProjectsUiState.Error -> state.content
            ProjectsUiState.Loading -> ProjectsUiState.Content()
        }
    }
}
