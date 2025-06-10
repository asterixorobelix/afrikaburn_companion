package io.asterixorobelix.afrikaburn.presentation.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.asterixorobelix.afrikaburn.domain.repository.ProjectsRepository
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProjectTabViewModel(
    private val repository: ProjectsRepository,
    private val projectType: ProjectType
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectsUiState())
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()

    init {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            repository.getProjectsByType(projectType)
                .onSuccess { projects ->
                    _uiState.value = _uiState.value.copy(
                        projects = projects,
                        filteredProjects = filterProjects(
                            projects = projects, 
                            searchQuery = _uiState.value.searchQuery,
                            familyFilter = _uiState.value.isFamilyFilterEnabled
                        ),
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredProjects = filterProjects(
                projects = _uiState.value.projects, 
                searchQuery = query,
                familyFilter = _uiState.value.isFamilyFilterEnabled
            )
        )
    }
    
    fun toggleFamilyFilter() {
        val newFamilyFilter = !_uiState.value.isFamilyFilterEnabled
        _uiState.value = _uiState.value.copy(
            isFamilyFilterEnabled = newFamilyFilter,
            filteredProjects = filterProjects(
                projects = _uiState.value.projects,
                searchQuery = _uiState.value.searchQuery,
                familyFilter = newFamilyFilter
            )
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun retryLoading() {
        loadProjects()
    }

    private fun filterProjects(
        projects: List<ProjectItem>, 
        searchQuery: String,
        familyFilter: Boolean
    ): List<ProjectItem> {
        var filteredProjects = projects
        
        // Apply family filter first
        if (familyFilter) {
            filteredProjects = filteredProjects.filter { it.isFamilyFriendly }
        }
        
        // Apply search query filter
        if (searchQuery.isNotEmpty()) {
            filteredProjects = filteredProjects.filter { project ->
                project.name.contains(searchQuery, ignoreCase = true) ||
                project.description.contains(searchQuery, ignoreCase = true) ||
                project.artist.name.contains(searchQuery, ignoreCase = true)
            }
        }
        
        return filteredProjects
    }
}