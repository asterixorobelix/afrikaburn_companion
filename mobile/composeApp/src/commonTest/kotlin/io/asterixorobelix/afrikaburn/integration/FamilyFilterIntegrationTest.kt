package io.asterixorobelix.afrikaburn.integration

import io.asterixorobelix.afrikaburn.data.datasource.JsonResourceDataSource
import io.asterixorobelix.afrikaburn.data.repository.ProjectsRepositoryImpl
import io.asterixorobelix.afrikaburn.domain.repository.ProjectsRepository
import io.asterixorobelix.afrikaburn.models.Artist
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType
import io.asterixorobelix.afrikaburn.presentation.projects.ProjectTabViewModel
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
 * Integration tests for family filtering functionality across the full architecture
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FamilyFilterIntegrationTest {
    
    private lateinit var dataSource: MockJsonResourceDataSourceForFamilyFilter
    private lateinit var repository: ProjectsRepository
    private lateinit var viewModel: ProjectTabViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    private val campProjects = listOf(
        ProjectItem(
            name = "The Vagabonds",
            description = "Wellness and relaxation camp where everyone feels at home",
            artist = Artist("Vagabonds Team"),
            code = "vag",
            status = "Fam • Day Time"
        ),
        ProjectItem(
            name = "ALEGRA SPACE STATION",
            description = "Burn equivalent of the International Space Station",
            artist = Artist("ALEGRA Team"),
            code = "ass",
            status = "Fam • Other"
        ),
        ProjectItem(
            name = "Pétanque, pastis and the clochette factory",
            description = "Lively, welcoming space where pétanque meets pastis",
            artist = Artist("Pétanque Team"),
            code = "pet",
            status = "Fam(ish) • Other"
        ),
        ProjectItem(
            name = "Space Cowboys",
            description = "Galactic space station of AfrikaBurn",
            artist = Artist("Space Cowboys"),
            code = "scb",
            status = "All Night"
        ),
        ProjectItem(
            name = "Purple Spanking Booth",
            description = "Playful, light-hearted spankings for adults",
            artist = Artist("Purple Team"),
            code = "psp",
            status = "Day Time, Night Time, All Night"
        )
    )
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        dataSource = MockJsonResourceDataSourceForFamilyFilter()
        repository = ProjectsRepositoryImpl(dataSource)
        viewModel = ProjectTabViewModel(repository, ProjectType.CAMPS)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `full integration should load all camps initially`() = runTest {
        // Given camp data in data source
        dataSource.setProjectsForType(ProjectType.CAMPS, campProjects)
        
        // When loading camps
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should load all camps
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertEquals(campProjects, state.projects)
        assertEquals(campProjects, state.filteredProjects)
        assertFalse(state.isFamilyFilterEnabled)
    }
    
    @Test
    fun `family filter should work end-to-end with real-like camp data`() = runTest {
        // Given camp data loaded
        dataSource.setProjectsForType(ProjectType.CAMPS, campProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When enabling family filter
        viewModel.toggleFamilyFilter()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show only family-friendly camps
        val state = viewModel.uiState.first()
        assertTrue(state.isFamilyFilterEnabled)
        assertEquals(3, state.filteredProjects.size) // Vagabonds, ALEGRA, Pétanque (Fam and Fam(ish))
        
        val familyCampNames = state.filteredProjects.map { it.name }
        assertTrue(familyCampNames.contains("The Vagabonds"))
        assertTrue(familyCampNames.contains("ALEGRA SPACE STATION"))
        assertTrue(familyCampNames.contains("Pétanque, pastis and the clochette factory"))
        assertFalse(familyCampNames.contains("Space Cowboys"))
        assertFalse(familyCampNames.contains("Purple Spanking Booth"))
    }
    
    @Test
    fun `search and family filter should work together end-to-end`() = runTest {
        // Given camp data loaded
        dataSource.setProjectsForType(ProjectType.CAMPS, campProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When searching for "space" with family filter
        viewModel.updateSearchQuery("space")
        viewModel.toggleFamilyFilter()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show only family-friendly camps matching "space"
        val state = viewModel.uiState.first()
        assertEquals("space", state.searchQuery)
        assertTrue(state.isFamilyFilterEnabled)
        assertEquals(1, state.filteredProjects.size)
        assertEquals("ALEGRA SPACE STATION", state.filteredProjects.first().name)
    }
    
    @Test
    fun `search for non-family camp with family filter should show empty results`() = runTest {
        // Given camp data loaded
        dataSource.setProjectsForType(ProjectType.CAMPS, campProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When searching for non-family camp with family filter
        viewModel.updateSearchQuery("Cowboys")
        viewModel.toggleFamilyFilter()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show no results
        val state = viewModel.uiState.first()
        assertEquals("Cowboys", state.searchQuery)
        assertTrue(state.isFamilyFilterEnabled)
        assertTrue(state.filteredProjects.isEmpty())
        assertTrue(state.isShowingEmptySearch())
    }
    
    @Test
    fun `family filter should persist through search changes`() = runTest {
        // Given family filter enabled
        dataSource.setProjectsForType(ProjectType.CAMPS, campProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.toggleFamilyFilter()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When changing search multiple times
        viewModel.updateSearchQuery("space")
        testDispatcher.scheduler.advanceUntilIdle()
        val firstState = viewModel.uiState.first()
        
        viewModel.updateSearchQuery("vagabonds")
        testDispatcher.scheduler.advanceUntilIdle()
        val secondState = viewModel.uiState.first()
        
        viewModel.updateSearchQuery("")
        testDispatcher.scheduler.advanceUntilIdle()
        val finalState = viewModel.uiState.first()
        
        // Then family filter should remain enabled throughout
        assertTrue(firstState.isFamilyFilterEnabled)
        assertTrue(secondState.isFamilyFilterEnabled)
        assertTrue(finalState.isFamilyFilterEnabled)
        
        // And final state should show all family camps
        assertEquals(3, finalState.filteredProjects.size)
    }
    
    @Test
    fun `disable family filter should restore all camps`() = runTest {
        // Given family filter enabled with search
        dataSource.setProjectsForType(ProjectType.CAMPS, campProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.toggleFamilyFilter()
        viewModel.updateSearchQuery("space")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When disabling family filter
        viewModel.toggleFamilyFilter()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show all camps matching search (both family and non-family)
        val state = viewModel.uiState.first()
        assertFalse(state.isFamilyFilterEnabled)
        assertEquals("space", state.searchQuery)
        assertEquals(2, state.filteredProjects.size) // ALEGRA and Space Cowboys
        
        val campNames = state.filteredProjects.map { it.name }
        assertTrue(campNames.contains("ALEGRA SPACE STATION"))
        assertTrue(campNames.contains("Space Cowboys"))
    }
    
    @Test
    fun `family filter should work with different status formats`() = runTest {
        // Given camps with various status formats
        val diverseStatusCamps = listOf(
            ProjectItem(name = "Camp A", description = "Test", status = "Fam"),
            ProjectItem(name = "Camp B", description = "Test", status = "fam • Day Time"),
            ProjectItem(name = "Camp C", description = "Test", status = "Day Time • Fam • Other"),
            ProjectItem(name = "Camp D", description = "Test", status = "Fam(ish) • Night Time"),
            ProjectItem(name = "Camp E", description = "Test", status = "Night Time"),
            ProjectItem(name = "Camp F", description = "Test", status = "")
        )
        
        dataSource.setProjectsForType(ProjectType.CAMPS, diverseStatusCamps)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When enabling family filter
        viewModel.toggleFamilyFilter()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should correctly identify family camps regardless of format
        val state = viewModel.uiState.first()
        assertEquals(4, state.filteredProjects.size) // A, B, C, D should be family-friendly
        
        val familyCampNames = state.filteredProjects.map { it.name }
        assertTrue(familyCampNames.contains("Camp A"))
        assertTrue(familyCampNames.contains("Camp B"))
        assertTrue(familyCampNames.contains("Camp C"))
        assertTrue(familyCampNames.contains("Camp D"))
        assertFalse(familyCampNames.contains("Camp E"))
        assertFalse(familyCampNames.contains("Camp F"))
    }
}

private class MockJsonResourceDataSourceForFamilyFilter : JsonResourceDataSource {
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