package io.asterixorobelix.afrikaburn.presentation.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.asterixorobelix.afrikaburn.domain.repository.ProjectsRepository
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
                        filteredProjects = filterProjects(projects, _uiState.value.searchQuery),
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
            filteredProjects = filterProjects(_uiState.value.projects, query)
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun retryLoading() {
        loadProjects()
    }

    private fun filterProjects(
        projects: List<io.asterixorobelix.afrikaburn.models.ProjectItem>, 
        query: String
    ): List<io.asterixorobelix.afrikaburn.models.ProjectItem> {
        if (query.isEmpty()) return projects
        
        return projects.filter { project ->
            project.name.contains(query, ignoreCase = true) ||
            project.description.contains(query, ignoreCase = true) ||
            project.artist.name.contains(query, ignoreCase = true)
        }
    }
}