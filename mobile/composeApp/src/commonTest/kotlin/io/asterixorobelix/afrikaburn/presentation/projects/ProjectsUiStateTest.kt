package io.asterixorobelix.afrikaburn.presentation.projects

import io.asterixorobelix.afrikaburn.models.Artist
import io.asterixorobelix.afrikaburn.models.ProjectItem
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProjectsUiStateTest {
    
    private val sampleProjects = listOf(
        ProjectItem(
            name = "Test Project 1",
            description = "Description 1",
            artist = Artist("Artist 1"),
            code = "T001",
            status = "Active"
        ),
        ProjectItem(
            name = "Test Project 2",
            description = "Description 2",
            artist = Artist("Artist 2"),
            code = "T002",
            status = "Active"
        )
    )
    
    @Test
    fun `isShowingResults should return true when not loading and no error`() {
        // Given state with projects loaded successfully
        val state = ProjectsUiState(
            projects = sampleProjects,
            filteredProjects = sampleProjects,
            isLoading = false,
            error = null,
            searchQuery = ""
        )
        
        // Then should show results
        assertTrue(state.isShowingResults())
    }
    
    @Test
    fun `isShowingResults should return false when loading`() {
        // Given loading state
        val state = ProjectsUiState(
            projects = sampleProjects,
            filteredProjects = sampleProjects,
            isLoading = true,
            error = null,
            searchQuery = ""
        )
        
        // Then should not show results
        assertFalse(state.isShowingResults())
    }
    
    @Test
    fun `isShowingResults should return false when error exists`() {
        // Given error state
        val state = ProjectsUiState(
            projects = emptyList(),
            filteredProjects = emptyList(),
            isLoading = false,
            error = "Network error",
            searchQuery = ""
        )
        
        // Then should not show results
        assertFalse(state.isShowingResults())
    }
    
    @Test
    fun `isShowingEmptySearch should return true when searching with no results`() {
        // Given state with search query but no filtered results
        val state = ProjectsUiState(
            projects = sampleProjects,
            filteredProjects = emptyList(),
            isLoading = false,
            error = null,
            searchQuery = "nonexistent"
        )
        
        // Then should show empty search
        assertTrue(state.isShowingEmptySearch())
    }
    
    @Test
    fun `isShowingEmptySearch should return false when not searching`() {
        // Given state without search query
        val state = ProjectsUiState(
            projects = sampleProjects,
            filteredProjects = sampleProjects,
            isLoading = false,
            error = null,
            searchQuery = ""
        )
        
        // Then should not show empty search
        assertFalse(state.isShowingEmptySearch())
    }
    
    @Test
    fun `isShowingEmptySearch should return false when searching with results`() {
        // Given state with search query and filtered results
        val state = ProjectsUiState(
            projects = sampleProjects,
            filteredProjects = listOf(sampleProjects.first()),
            isLoading = false,
            error = null,
            searchQuery = "Test"
        )
        
        // Then should not show empty search
        assertFalse(state.isShowingEmptySearch())
    }
    
    @Test
    fun `isShowingEmptySearch should return false when loading`() {
        // Given loading state with search query
        val state = ProjectsUiState(
            projects = emptyList(),
            filteredProjects = emptyList(),
            isLoading = true,
            error = null,
            searchQuery = "test"
        )
        
        // Then should not show empty search (loading takes precedence)
        assertFalse(state.isShowingEmptySearch())
    }
    
    @Test
    fun `isShowingEmptySearch should return false when error exists`() {
        // Given error state with search query
        val state = ProjectsUiState(
            projects = emptyList(),
            filteredProjects = emptyList(),
            isLoading = false,
            error = "Network error",
            searchQuery = "test"
        )
        
        // Then should not show empty search (error takes precedence)
        assertFalse(state.isShowingEmptySearch())
    }
    
    @Test
    fun `default state should be correctly initialized`() {
        // Given default state
        val state = ProjectsUiState()
        
        // Then should have correct defaults
        assertFalse(state.isLoading)
        assertTrue(state.projects.isEmpty())
        assertTrue(state.filteredProjects.isEmpty())
        assertTrue(state.searchQuery.isEmpty())
        assertTrue(state.error == null)
        assertTrue(state.isShowingResults())
        assertFalse(state.isShowingEmptySearch())
    }
}