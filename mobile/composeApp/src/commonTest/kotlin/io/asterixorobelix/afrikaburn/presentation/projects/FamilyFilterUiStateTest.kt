package io.asterixorobelix.afrikaburn.presentation.projects

import io.asterixorobelix.afrikaburn.models.Artist
import io.asterixorobelix.afrikaburn.models.ProjectItem
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FamilyFilterUiStateTest {
    
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
    fun `isShowingEmptySearch should return true when family filter is enabled with no results`() {
        // Given state with family filter enabled but no filtered results
        val state = ProjectsUiState(
            projects = sampleProjects,
            filteredProjects = emptyList(),
            isLoading = false,
            error = null,
            searchQuery = "",
            isFamilyFilterEnabled = true
        )
        
        // Then should show empty search
        assertTrue(state.isShowingEmptySearch())
    }
    
    @Test
    fun `isShowingEmptySearch should return true when both search and family filter are active with no results`() {
        // Given state with both filters active but no results
        val state = ProjectsUiState(
            projects = sampleProjects,
            filteredProjects = emptyList(),
            isLoading = false,
            error = null,
            searchQuery = "nonexistent",
            isFamilyFilterEnabled = true
        )
        
        // Then should show empty search
        assertTrue(state.isShowingEmptySearch())
    }
    
    @Test
    fun `isShowingEmptySearch should return false when family filter is enabled with results`() {
        // Given state with family filter enabled and results
        val state = ProjectsUiState(
            projects = sampleProjects,
            filteredProjects = listOf(sampleProjects.first()),
            isLoading = false,
            error = null,
            searchQuery = "",
            isFamilyFilterEnabled = true
        )
        
        // Then should not show empty search
        assertFalse(state.isShowingEmptySearch())
    }
    
    @Test
    fun `hasActiveFilters should return true when family filter is enabled`() {
        // Given state with family filter enabled
        val state = ProjectsUiState(
            projects = sampleProjects,
            filteredProjects = sampleProjects,
            isLoading = false,
            error = null,
            searchQuery = "",
            isFamilyFilterEnabled = true
        )
        
        // Then should have active filters
        assertTrue(state.hasActiveFilters())
    }
    
    @Test
    fun `hasActiveFilters should return true when search query is not empty`() {
        // Given state with search query
        val state = ProjectsUiState(
            projects = sampleProjects,
            filteredProjects = sampleProjects,
            isLoading = false,
            error = null,
            searchQuery = "test",
            isFamilyFilterEnabled = false
        )
        
        // Then should have active filters
        assertTrue(state.hasActiveFilters())
    }
    
    @Test
    fun `hasActiveFilters should return true when both search and family filter are active`() {
        // Given state with both filters active
        val state = ProjectsUiState(
            projects = sampleProjects,
            filteredProjects = sampleProjects,
            isLoading = false,
            error = null,
            searchQuery = "test",
            isFamilyFilterEnabled = true
        )
        
        // Then should have active filters
        assertTrue(state.hasActiveFilters())
    }
    
    @Test
    fun `hasActiveFilters should return false when no filters are active`() {
        // Given state with no filters
        val state = ProjectsUiState(
            projects = sampleProjects,
            filteredProjects = sampleProjects,
            isLoading = false,
            error = null,
            searchQuery = "",
            isFamilyFilterEnabled = false
        )
        
        // Then should not have active filters
        assertFalse(state.hasActiveFilters())
    }
    
    @Test
    fun `default state should have family filter disabled`() {
        // Given default state
        val state = ProjectsUiState()
        
        // Then family filter should be disabled
        assertFalse(state.isFamilyFilterEnabled)
        assertFalse(state.hasActiveFilters())
    }
}