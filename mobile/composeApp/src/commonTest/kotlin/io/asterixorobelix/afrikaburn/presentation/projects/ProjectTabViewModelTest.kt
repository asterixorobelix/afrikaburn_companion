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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectTabViewModelTest {
    
    private lateinit var repository: ConfigurableProjectsRepository
    private lateinit var viewModel: ProjectTabViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    private val sampleProjects = listOf(
        ProjectItem(
            name = "Fire Art Installation",
            description = "A spectacular fire artwork that lights up the night",
            artist = Artist("John Artist"),
            code = "FA001",
            status = "Confirmed"
        ),
        ProjectItem(
            name = "Interactive Sculpture",
            description = "A participatory art piece that responds to touch",
            artist = Artist("Jane Creator"),
            code = "IS002",
            status = "In Progress"
        ),
        ProjectItem(
            name = "Light Installation",
            description = "Beautiful lights creating amazing patterns",
            artist = Artist("Light Master"),
            code = "LI003",
            status = "Confirmed"
        )
    )
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = ConfigurableProjectsRepository()
        // Set up default behavior to prevent initialization issues
        repository.setDefaultResult(Result.success(emptyList()))
        viewModel = ProjectTabViewModel(repository, ProjectType.ART)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should complete loading with empty data`() = runTest {
        // Given a fresh viewModel with default mock returning empty list
        // When getting state after coroutine completion
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.first()
        
        // Then it should show the loaded empty state
        assertFalse(state.isLoading)
        assertEquals(emptyList(), state.projects)
        assertEquals(emptyList(), state.filteredProjects)
        assertEquals("", state.searchQuery)
        assertNull(state.error)
    }
    
    @Test
    fun `loadProjects should update state with success data`() = runTest {
        // Given successful repository response
        repository.setResultForType(ProjectType.ART, Result.success(sampleProjects))
        
        // When loading projects
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then state should be updated with projects
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertEquals(sampleProjects, state.projects)
        assertEquals(sampleProjects, state.filteredProjects)
        assertNull(state.error)
    }
    
    @Test
    fun `loadProjects should update state with error`() = runTest {
        // Given repository that throws error
        val errorMessage = "Network error"
        repository.setResultForType(ProjectType.ART, Result.failure(Exception(errorMessage)))
        
        // When loading projects
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then state should contain error
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertEquals(emptyList(), state.projects)
        assertEquals(emptyList(), state.filteredProjects)
        assertEquals(errorMessage, state.error)
    }
    
    @Test
    fun `updateSearchQuery should filter projects by name`() = runTest {
        // Given projects are loaded
        repository.setResultForType(ProjectType.ART, Result.success(sampleProjects))
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When searching for "Fire"
        viewModel.updateSearchQuery("Fire")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then only matching projects should be filtered
        val state = viewModel.uiState.first()
        assertEquals("Fire", state.searchQuery)
        assertEquals(1, state.filteredProjects.size)
        assertEquals("Fire Art Installation", state.filteredProjects.first().name)
    }
    
    @Test
    fun `updateSearchQuery should filter projects by description`() = runTest {
        // Given projects are loaded
        repository.setResultForType(ProjectType.ART, Result.success(sampleProjects))
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When searching for "participatory"
        viewModel.updateSearchQuery("participatory")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then matching project should be filtered
        val state = viewModel.uiState.first()
        assertEquals("participatory", state.searchQuery)
        assertEquals(1, state.filteredProjects.size)
        assertEquals("Interactive Sculpture", state.filteredProjects.first().name)
    }
    
    @Test
    fun `updateSearchQuery should filter projects by artist name`() = runTest {
        // Given projects are loaded
        repository.setResultForType(ProjectType.ART, Result.success(sampleProjects))
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When searching for "Jane"
        viewModel.updateSearchQuery("Jane")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then matching project should be filtered
        val state = viewModel.uiState.first()
        assertEquals("Jane", state.searchQuery)
        assertEquals(1, state.filteredProjects.size)
        assertEquals("Interactive Sculpture", state.filteredProjects.first().name)
    }
    
    @Test
    fun `updateSearchQuery should be case insensitive`() = runTest {
        // Given projects are loaded
        repository.setResultForType(ProjectType.ART, Result.success(sampleProjects))
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When searching with different cases
        viewModel.updateSearchQuery("FIRE")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should still match
        val state = viewModel.uiState.first()
        assertEquals("FIRE", state.searchQuery)
        assertEquals(1, state.filteredProjects.size)
        assertEquals("Fire Art Installation", state.filteredProjects.first().name)
    }
    
    @Test
    fun `updateSearchQuery with empty string should show all projects`() = runTest {
        // Given projects are loaded and filtered
        repository.setResultForType(ProjectType.ART, Result.success(sampleProjects))
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateSearchQuery("Fire")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When clearing search
        viewModel.updateSearchQuery("")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then all projects should be shown
        val state = viewModel.uiState.first()
        assertEquals("", state.searchQuery)
        assertEquals(sampleProjects.size, state.filteredProjects.size)
        assertEquals(sampleProjects, state.filteredProjects)
    }
    
    @Test
    fun `retryLoading should reload projects`() = runTest {
        // Given initial error state
        repository.setResultForType(ProjectType.ART, Result.failure(Exception("Network error")))
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When retrying with successful response
        repository.setResultForType(ProjectType.ART, Result.success(sampleProjects))
        viewModel.retryLoading()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should load successfully
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertEquals(sampleProjects, state.projects)
        assertNull(state.error)
    }
    
    @Test
    fun `clearError should remove error from state`() = runTest {
        // Given error state
        repository.setResultForType(ProjectType.ART, Result.failure(Exception("Network error")))
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When clearing error
        viewModel.clearError()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then error should be null
        val state = viewModel.uiState.first()
        assertNull(state.error)
    }
    
    @Test
    fun `isShowingEmptySearch should return true when search has no results`() = runTest {
        // Given projects are loaded
        repository.setResultForType(ProjectType.ART, Result.success(sampleProjects))
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When searching for non-existent term
        viewModel.updateSearchQuery("nonexistent")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show empty search state
        val state = viewModel.uiState.first()
        assertTrue(state.isShowingEmptySearch())
        assertEquals("nonexistent", state.searchQuery)
        assertEquals(0, state.filteredProjects.size)
    }
    
    @Test
    fun `isShowingEmptySearch should return false when not searching`() = runTest {
        // Given projects are loaded without search
        repository.setResultForType(ProjectType.ART, Result.success(sampleProjects))
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should not show empty search state
        val state = viewModel.uiState.first()
        assertFalse(state.isShowingEmptySearch())
    }
}

private class ConfigurableProjectsRepository : ProjectsRepository {
    private val resultsMap = mutableMapOf<ProjectType, Result<List<ProjectItem>>>()
    private var defaultResult: Result<List<ProjectItem>> = Result.success(emptyList())
    
    fun setResultForType(type: ProjectType, result: Result<List<ProjectItem>>) {
        resultsMap[type] = result
    }
    
    fun setDefaultResult(result: Result<List<ProjectItem>>) {
        defaultResult = result
    }
    
    override suspend fun getProjectsByType(type: ProjectType): Result<List<ProjectItem>> {
        return resultsMap[type] ?: defaultResult
    }
}