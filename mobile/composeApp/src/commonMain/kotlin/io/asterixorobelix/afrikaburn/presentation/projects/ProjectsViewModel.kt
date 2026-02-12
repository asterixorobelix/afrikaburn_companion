package io.asterixorobelix.afrikaburn.presentation.projects

import androidx.lifecycle.ViewModel
import io.asterixorobelix.afrikaburn.domain.repository.ProjectsRepository
import io.asterixorobelix.afrikaburn.models.ProjectType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProjectsScreenUiState(
    val currentTabIndex: Int = 0,
    val tabs: List<ProjectType> = ProjectType.entries
)

class ProjectsViewModel(
    private val repository: ProjectsRepository
) : ViewModel() {

    private val _screenUiState = MutableStateFlow(ProjectsScreenUiState())
    val screenUiState: StateFlow<ProjectsScreenUiState> = _screenUiState.asStateFlow()

    // Create individual ViewModels for each tab
    private val _tabViewModels = mutableMapOf<ProjectType, ProjectTabViewModel>()

    fun getTabViewModel(projectType: ProjectType): ProjectTabViewModel {
        return _tabViewModels.getOrPut(projectType) {
            ProjectTabViewModel(repository, projectType)
        }
    }

    fun updateCurrentTab(index: Int) {
        val state = _screenUiState.value
        if (index in state.tabs.indices) {
            _screenUiState.value = state.copy(currentTabIndex = index)
        }
    }

    fun getCurrentProjectType(): ProjectType {
        val state = _screenUiState.value
        return state.tabs[state.currentTabIndex.coerceIn(state.tabs.indices)]
    }

    override fun onCleared() {
        super.onCleared()
        _tabViewModels.values.forEach { it.cleanup() }
        _tabViewModels.clear()
    }
}