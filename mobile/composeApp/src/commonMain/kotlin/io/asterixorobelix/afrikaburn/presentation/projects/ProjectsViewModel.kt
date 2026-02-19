package io.asterixorobelix.afrikaburn.presentation.projects

import androidx.lifecycle.ViewModel
import io.asterixorobelix.afrikaburn.models.ProjectType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface ProjectsScreenUiState {
    data object Loading : ProjectsScreenUiState
    data class Content(
        val currentTabIndex: Int = 0,
        val tabs: List<ProjectType> = ProjectType.entries,
        val isRefreshing: Boolean = false
    ) : ProjectsScreenUiState
    data class Empty(val content: Content) : ProjectsScreenUiState
    data class Error(val message: String, val content: Content) : ProjectsScreenUiState
}

class ProjectsViewModel : ViewModel() {

    private val _screenUiState = MutableStateFlow<ProjectsScreenUiState>(
        ProjectsScreenUiState.Content()
    )
    val screenUiState: StateFlow<ProjectsScreenUiState> = _screenUiState.asStateFlow()

    fun updateCurrentTab(index: Int) {
        val content = currentContent()
        if (index in content.tabs.indices) {
            _screenUiState.value = content.copy(currentTabIndex = index)
        }
    }

    fun getCurrentProjectType(): ProjectType {
        val content = currentContent()
        return content.tabs[content.currentTabIndex.coerceIn(content.tabs.indices)]
    }

    private fun currentContent(): ProjectsScreenUiState.Content {
        return when (val state = _screenUiState.value) {
            is ProjectsScreenUiState.Content -> state
            is ProjectsScreenUiState.Empty -> state.content
            is ProjectsScreenUiState.Error -> state.content
            ProjectsScreenUiState.Loading -> ProjectsScreenUiState.Content()
        }
    }
}
