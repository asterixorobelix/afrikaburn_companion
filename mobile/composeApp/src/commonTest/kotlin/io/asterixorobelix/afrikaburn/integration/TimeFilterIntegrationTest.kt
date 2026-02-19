package io.asterixorobelix.afrikaburn.integration

import io.asterixorobelix.afrikaburn.data.datasource.JsonResourceDataSource
import io.asterixorobelix.afrikaburn.data.repository.ProjectsRepositoryImpl
import io.asterixorobelix.afrikaburn.domain.repository.ProjectsRepository
import io.asterixorobelix.afrikaburn.domain.usecase.projects.GetProjectsByTypeUseCase
import io.asterixorobelix.afrikaburn.models.Artist
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType
import io.asterixorobelix.afrikaburn.models.TimeFilter
import io.asterixorobelix.afrikaburn.presentation.projects.ProjectTabViewModel
import io.asterixorobelix.afrikaburn.presentation.projects.ProjectsUiState
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

/**
 * Integration tests for time filtering functionality across the full architecture
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TimeFilterIntegrationTest {
    
    private lateinit var dataSource: MockJsonResourceDataSourceForTimeFilter
    private lateinit var repository: ProjectsRepository
    private lateinit var getProjectsByTypeUseCase: GetProjectsByTypeUseCase
    private lateinit var viewModel: ProjectTabViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    private val realCampData = listOf(
        // Daytime only camps
        ProjectItem(
            name = "The Vagabonds",
            description = "Wellness and relaxation camp",
            artist = Artist("Vagabonds Team"),
            code = "vag",
            status = "Fam • Day Time"
        ),
        ProjectItem(
            name = "Hydration Station",
            description = "Water distribution during day",
            artist = Artist("Hydration Team"),
            code = "hyd",
            status = "Fam • Day Time"
        ),
        ProjectItem(
            name = "Sunrisers",
            description = "Morning coffee service",
            artist = Artist("Coffee Team"),
            code = "sun",
            status = "Fam • Morning"
        ),
        
        // Nighttime only camps
        ProjectItem(
            name = "Space Cowboys",
            description = "Galactic space station for nighttime dance",
            artist = Artist("Space Cowboys"),
            code = "scb",
            status = "All Night"
        ),
        ProjectItem(
            name = "Sweet Love Cinema",
            description = "Kid-friendly movies from sunset to morning",
            artist = Artist("Cinema Team"),
            code = "slc",
            status = "Fam • Night Time"
        ),
        
        // Mixed time camps
        ProjectItem(
            name = "aTypical Bar",
            description = "Serving interaction day or night",
            artist = Artist("aTypical Team"),
            code = "atb",
            status = "Fam • Day Time, Night Time, All Night"
        ),
        ProjectItem(
            name = "Garden of Weeden",
            description = "Multiple spaces for different times",
            artist = Artist("Garden Team"),
            code = "gow",
            status = "Fam • Day Time, Night Time"
        ),
        
        // Other/unspecified time camps
        ProjectItem(
            name = "ALEGRA SPACE STATION",
            description = "Space station with flexible schedule",
            artist = Artist("ALEGRA Team"),
            code = "ass",
            status = "Fam • Other"
        ),
        ProjectItem(
            name = "The Prize Cock",
            description = "Neighbourhood bar with standard hours",
            artist = Artist("Prize Team"),
            code = "tpc",
            status = "Other"
        )
    )
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        dataSource = MockJsonResourceDataSourceForTimeFilter()
        repository = ProjectsRepositoryImpl(dataSource)
        getProjectsByTypeUseCase = GetProjectsByTypeUseCase(repository)
        // Set up default mock data before creating ViewModel to prevent cache issues
        dataSource.setProjectsForType(ProjectType.CAMPS, realCampData)
        viewModel = ProjectTabViewModel(getProjectsByTypeUseCase, ProjectType.CAMPS)
    }
    
    private fun clearRepositoryCache() {
        (repository as ProjectsRepositoryImpl).clearCache()
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `full integration should load all camps with default ALL time filter`() = runTest {
        // Given camp data already set up in @BeforeTest
        // Verify data was loaded during initialization
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should load all camps with ALL time filter
        val state = viewModel.uiState.first() as ProjectsUiState.Content
        assertFalse(state.isRefreshing)
        assertEquals(TimeFilter.ALL, state.timeFilter)
        assertEquals(realCampData, state.projects)
        assertEquals(realCampData, state.filteredProjects)
    }
    
    @Test
    fun `daytime filter should work end-to-end with real camp data`() = runTest {
        // Given fresh data setup
        clearRepositoryCache()
        dataSource.setProjectsForType(ProjectType.CAMPS, realCampData)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When filtering for daytime
        viewModel.updateTimeFilter(TimeFilter.DAYTIME)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show only daytime and mixed camps
        val state = viewModel.uiState.first() as ProjectsUiState.Content
        assertEquals(TimeFilter.DAYTIME, state.timeFilter)
        assertEquals(5, state.filteredProjects.size) // 3 daytime + 2 mixed
        
        val campNames = state.filteredProjects.map { it.name }
        assertTrue(campNames.contains("The Vagabonds"))
        assertTrue(campNames.contains("Hydration Station"))
        assertTrue(campNames.contains("Sunrisers"))
        assertTrue(campNames.contains("aTypical Bar"))
        assertTrue(campNames.contains("Garden of Weeden"))
        assertFalse(campNames.contains("Space Cowboys"))
        assertFalse(campNames.contains("Sweet Love Cinema"))
        assertFalse(campNames.contains("ALEGRA SPACE STATION"))
        assertFalse(campNames.contains("The Prize Cock"))
    }
    
    @Test
    fun `nighttime filter should work end-to-end with real camp data`() = runTest {
        // Given fresh data setup
        clearRepositoryCache()
        dataSource.setProjectsForType(ProjectType.CAMPS, realCampData)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When filtering for nighttime
        viewModel.updateTimeFilter(TimeFilter.NIGHTTIME)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show only nighttime and mixed camps
        val state = viewModel.uiState.first() as ProjectsUiState.Content
        assertEquals(TimeFilter.NIGHTTIME, state.timeFilter)
        assertEquals(4, state.filteredProjects.size) // 2 nighttime + 2 mixed
        
        val campNames = state.filteredProjects.map { it.name }
        assertTrue(campNames.contains("Space Cowboys"))
        assertTrue(campNames.contains("Sweet Love Cinema"))
        assertTrue(campNames.contains("aTypical Bar"))
        assertTrue(campNames.contains("Garden of Weeden"))
        assertFalse(campNames.contains("The Vagabonds"))
        assertFalse(campNames.contains("Hydration Station"))
        assertFalse(campNames.contains("Sunrisers"))
        assertFalse(campNames.contains("ALEGRA SPACE STATION"))
        assertFalse(campNames.contains("The Prize Cock"))
    }
    
    @Test
    fun `family and time filters should work together end-to-end`() = runTest {
        // Given fresh data setup
        clearRepositoryCache()
        dataSource.setProjectsForType(ProjectType.CAMPS, realCampData)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When applying both family and daytime filters
        viewModel.toggleFamilyFilter()
        viewModel.updateTimeFilter(TimeFilter.DAYTIME)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show only family-friendly daytime camps
        val state = viewModel.uiState.first() as ProjectsUiState.Content
        assertTrue(state.isFamilyFilterEnabled)
        assertEquals(TimeFilter.DAYTIME, state.timeFilter)
        assertEquals(5, state.filteredProjects.size) // All daytime camps in this dataset are family-friendly
        
        val campNames = state.filteredProjects.map { it.name }
        assertTrue(campNames.contains("The Vagabonds"))
        assertTrue(campNames.contains("Hydration Station"))
        assertTrue(campNames.contains("Sunrisers"))
        assertTrue(campNames.contains("aTypical Bar"))
        assertTrue(campNames.contains("Garden of Weeden"))
    }
    
    @Test
    fun `search with time filter should work end-to-end`() = runTest {
        // Given fresh data setup
        clearRepositoryCache()
        dataSource.setProjectsForType(ProjectType.CAMPS, realCampData)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When searching for "space" with nighttime filter
        viewModel.updateSearchQuery("space")
        viewModel.updateTimeFilter(TimeFilter.NIGHTTIME)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show only nighttime camps matching "space"
        val state = viewModel.uiState.first() as ProjectsUiState.Content
        assertEquals("space", state.searchQuery)
        assertEquals(TimeFilter.NIGHTTIME, state.timeFilter)
        assertEquals(2, state.filteredProjects.size) // Space Cowboys and Garden of Weeden (contains "spaces")
        
        val campNames = state.filteredProjects.map { it.name }
        assertTrue(campNames.contains("Space Cowboys"))
        assertTrue(campNames.contains("Garden of Weeden"))
    }
    
    @Test
    fun `all three filters should work together end-to-end`() = runTest {
        // Given fresh data setup
        clearRepositoryCache()
        dataSource.setProjectsForType(ProjectType.CAMPS, realCampData)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When applying family filter, nighttime filter, and search
        viewModel.toggleFamilyFilter()
        viewModel.updateTimeFilter(TimeFilter.NIGHTTIME)
        viewModel.updateSearchQuery("Cinema")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show only family-friendly nighttime camps matching "Cinema"
        val state = viewModel.uiState.first() as ProjectsUiState.Content
        assertTrue(state.isFamilyFilterEnabled)
        assertEquals(TimeFilter.NIGHTTIME, state.timeFilter)
        assertEquals("Cinema", state.searchQuery)
        assertEquals(1, state.filteredProjects.size)
        assertEquals("Sweet Love Cinema", state.filteredProjects.first().name)
    }
    
    @Test
    fun `time filter with no matches should show empty results`() = runTest {
        // Given only "Other" camps (no specific time)
        val otherCamps = realCampData.filter { it.status.contains("Other") }
        clearRepositoryCache()
        dataSource.setProjectsForType(ProjectType.CAMPS, otherCamps)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When filtering for daytime
        viewModel.updateTimeFilter(TimeFilter.DAYTIME)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show empty results
        val state = viewModel.uiState.first() as ProjectsUiState.Content
        assertEquals(TimeFilter.DAYTIME, state.timeFilter)
        assertTrue(state.filteredProjects.isEmpty())
        assertTrue(state.isShowingEmptySearch())
    }
    
    @Test
    fun `resetting time filter should restore all camps`() = runTest {
        // Given daytime filter applied
        clearRepositoryCache()
        dataSource.setProjectsForType(ProjectType.CAMPS, realCampData)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateTimeFilter(TimeFilter.DAYTIME)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When resetting to ALL
        viewModel.updateTimeFilter(TimeFilter.ALL)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show all camps again
        val state = viewModel.uiState.first() as ProjectsUiState.Content
        assertEquals(TimeFilter.ALL, state.timeFilter)
        assertEquals(realCampData.size, state.filteredProjects.size)
        assertEquals(realCampData, state.filteredProjects)
    }
    
    @Test
    fun `complex filter combinations should work correctly`() = runTest {
        // Given fresh data setup
        clearRepositoryCache()
        dataSource.setProjectsForType(ProjectType.CAMPS, realCampData)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When applying complex filter combinations
        viewModel.updateSearchQuery("garden")
        viewModel.toggleFamilyFilter()
        viewModel.updateTimeFilter(TimeFilter.DAYTIME)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val firstState = viewModel.uiState.first() as ProjectsUiState.Content
        
        // Then change time filter while keeping other filters
        viewModel.updateTimeFilter(TimeFilter.NIGHTTIME)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val secondState = viewModel.uiState.first() as ProjectsUiState.Content
        
        // Both should find Garden of Weeden (family-friendly, operates day and night, matches "garden")
        assertEquals(1, firstState.filteredProjects.size)
        assertEquals("Garden of Weeden", firstState.filteredProjects.first().name)
        
        assertEquals(1, secondState.filteredProjects.size)
        assertEquals("Garden of Weeden", secondState.filteredProjects.first().name)
        
        // Verify all filters are applied
        assertTrue(firstState.isFamilyFilterEnabled)
        assertEquals("garden", firstState.searchQuery)
        assertEquals(TimeFilter.DAYTIME, firstState.timeFilter)
        
        assertTrue(secondState.isFamilyFilterEnabled)
        assertEquals("garden", secondState.searchQuery)
        assertEquals(TimeFilter.NIGHTTIME, secondState.timeFilter)
    }
}

private class MockJsonResourceDataSourceForTimeFilter : JsonResourceDataSource {
    private val projectsMap = mutableMapOf<ProjectType, List<ProjectItem>>()
    private val errorsMap = mutableMapOf<ProjectType, String>()
    
    fun setProjectsForType(type: ProjectType, projects: List<ProjectItem>) {
        projectsMap[type] = projects
        errorsMap.remove(type)
    }
    
    fun setErrorForType(type: ProjectType, errorMessage: String) {
        errorsMap[type] = errorMessage
        projectsMap.remove(type)
    }
    
    override suspend fun loadProjectsByType(type: ProjectType): List<ProjectItem> {
        errorsMap[type]?.let { error ->
            throw Exception(error)
        }
        
        return projectsMap[type] ?: emptyList()
    }
}
