package io.asterixorobelix.afrikaburn.presentation.projects

import io.asterixorobelix.afrikaburn.domain.repository.ProjectsRepository
import io.asterixorobelix.afrikaburn.models.Artist
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FamilyFilterViewModelTest {
    
    private lateinit var repository: MockProjectsRepositoryForFamilyFilter
    private lateinit var viewModel: ProjectTabViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    private val familyProjects = listOf(
        ProjectItem(
            name = "Family Camp 1",
            description = "A camp for families",
            artist = Artist("Family Artist"),
            code = "FAM001",
            status = "Fam • Day Time"
        ),
        ProjectItem(
            name = "Family Camp 2",
            description = "Another family camp",
            artist = Artist("Another Artist"),
            code = "FAM002", 
            status = "Fam(ish) • Day Time, Night Time"
        )
    )
    
    private val nonFamilyProjects = listOf(
        ProjectItem(
            name = "Adult Camp 1",
            description = "Adults only camp",
            artist = Artist("Adult Artist"),
            code = "ADU001",
            status = "Night Time, All Night"
        ),
        ProjectItem(
            name = "Adult Camp 2",
            description = "Another adults camp", 
            artist = Artist("Another Adult Artist"),
            code = "ADU002",
            status = "All Night"
        )
    )
    
    private val mixedProjects = familyProjects + nonFamilyProjects
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = MockProjectsRepositoryForFamilyFilter()
        viewModel = ProjectTabViewModel(repository, ProjectType.CAMPS)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `family filter should be disabled by default`() = runTest {
        // Given projects loaded
        repository.setSuccessResponse(mixedProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When getting initial state
        val state = viewModel.uiState.first()
        
        // Then family filter should be disabled and all projects shown
        assertFalse(state.isFamilyFilterEnabled)
        assertEquals(mixedProjects, state.filteredProjects)
    }
    
    @Test
    fun `toggleFamilyFilter should enable family filtering`() = runTest {
        // Given projects loaded
        repository.setSuccessResponse(mixedProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When toggling family filter
        viewModel.toggleFamilyFilter()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show only family-friendly projects
        val state = viewModel.uiState.first()
        assertTrue(state.isFamilyFilterEnabled)
        assertEquals(familyProjects, state.filteredProjects)
    }
    
    @Test
    fun `toggleFamilyFilter twice should return to showing all projects`() = runTest {
        // Given projects loaded and family filter enabled
        repository.setSuccessResponse(mixedProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.toggleFamilyFilter()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When toggling family filter again
        viewModel.toggleFamilyFilter()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show all projects again
        val state = viewModel.uiState.first()
        assertFalse(state.isFamilyFilterEnabled)
        assertEquals(mixedProjects, state.filteredProjects)
    }
    
    @Test
    fun `family filter with search query should filter both ways`() = runTest {
        // Given projects loaded
        repository.setSuccessResponse(mixedProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When enabling family filter and searching
        viewModel.toggleFamilyFilter()
        viewModel.updateSearchQuery("Camp 1")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show only family-friendly projects matching search
        val state = viewModel.uiState.first()
        assertTrue(state.isFamilyFilterEnabled)
        assertEquals("Camp 1", state.searchQuery)
        assertEquals(1, state.filteredProjects.size)
        assertEquals("Family Camp 1", state.filteredProjects.first().name)
    }
    
    @Test
    fun `search query with family filter should work in any order`() = runTest {
        // Given projects loaded
        repository.setSuccessResponse(mixedProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When searching first then enabling family filter
        viewModel.updateSearchQuery("Camp 1")
        viewModel.toggleFamilyFilter()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show only family-friendly projects matching search
        val state = viewModel.uiState.first()
        assertTrue(state.isFamilyFilterEnabled)
        assertEquals("Camp 1", state.searchQuery)
        assertEquals(1, state.filteredProjects.size)
        assertEquals("Family Camp 1", state.filteredProjects.first().name)
    }
    
    @Test
    fun `family filter with no family projects should show empty results`() = runTest {
        // Given only non-family projects
        repository.setSuccessResponse(nonFamilyProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When enabling family filter
        viewModel.toggleFamilyFilter()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show no results
        val state = viewModel.uiState.first()
        assertTrue(state.isFamilyFilterEnabled)
        assertTrue(state.filteredProjects.isEmpty())
        assertTrue(state.isShowingEmptySearch())
    }
    
    @Test
    fun `family filter should work with only family projects`() = runTest {
        // Given only family projects
        repository.setSuccessResponse(familyProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When enabling family filter
        viewModel.toggleFamilyFilter()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show all projects (since they're all family-friendly)
        val state = viewModel.uiState.first()
        assertTrue(state.isFamilyFilterEnabled)
        assertEquals(familyProjects, state.filteredProjects)
        assertFalse(state.isShowingEmptySearch())
    }
    
    @Test
    fun `hasActiveFilters should return true when family filter is enabled`() = runTest {
        // Given projects loaded
        repository.setSuccessResponse(mixedProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When enabling family filter
        viewModel.toggleFamilyFilter()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should have active filters
        val state = viewModel.uiState.first()
        assertTrue(state.hasActiveFilters())
    }
    
    @Test
    fun `hasActiveFilters should return true when both search and family filter are active`() = runTest {
        // Given projects loaded
        repository.setSuccessResponse(mixedProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When enabling both search and family filter
        viewModel.updateSearchQuery("Family")
        viewModel.toggleFamilyFilter()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should have active filters
        val state = viewModel.uiState.first()
        assertTrue(state.hasActiveFilters())
    }
    
    @Test
    fun `clearing search with family filter active should maintain family filtering`() = runTest {
        // Given projects loaded with both filters active
        repository.setSuccessResponse(mixedProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.toggleFamilyFilter()
        viewModel.updateSearchQuery("Family")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When clearing search
        viewModel.updateSearchQuery("")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show all family projects
        val state = viewModel.uiState.first()
        assertTrue(state.isFamilyFilterEnabled)
        assertEquals("", state.searchQuery)
        assertEquals(familyProjects, state.filteredProjects)
    }
}

private class MockProjectsRepositoryForFamilyFilter : ProjectsRepository {
    private var shouldThrowError = false
    private var errorMessage = ""
    private var projects = emptyList<ProjectItem>()
    
    fun setSuccessResponse(projectList: List<ProjectItem>) {
        shouldThrowError = false
        projects = projectList
    }
    
    fun setErrorResponse(message: String) {
        shouldThrowError = true
        errorMessage = message
    }
    
    override suspend fun getProjectsByType(type: ProjectType): Result<List<ProjectItem>> {
        return if (shouldThrowError) {
            Result.failure(Exception(errorMessage))
        } else {
            Result.success(projects)
        }
    }
}