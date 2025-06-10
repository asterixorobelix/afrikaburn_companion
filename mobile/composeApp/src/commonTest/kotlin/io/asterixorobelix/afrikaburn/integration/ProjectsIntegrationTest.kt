package io.asterixorobelix.afrikaburn.integration

import io.asterixorobelix.afrikaburn.data.datasource.JsonResourceDataSource
import io.asterixorobelix.afrikaburn.data.repository.ProjectsRepositoryImpl
import io.asterixorobelix.afrikaburn.domain.repository.ProjectsRepository
import io.asterixorobelix.afrikaburn.models.Artist
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType
import io.asterixorobelix.afrikaburn.presentation.projects.ProjectTabViewModel
import io.asterixorobelix.afrikaburn.presentation.projects.ProjectsViewModel
import io.mockk.coEvery
import io.mockk.mockk
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

/**
 * Integration tests that verify the full flow from DataSource -> Repository -> ViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProjectsIntegrationTest {
    
    private lateinit var dataSource: JsonResourceDataSource
    private lateinit var repository: ProjectsRepository
    private lateinit var projectsViewModel: ProjectsViewModel
    private lateinit var projectTabViewModel: ProjectTabViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    private val sampleArtProjects = listOf(
        ProjectItem(
            name = "Fire Sculpture",
            description = "A magnificent fire art installation",
            artist = Artist("Fire Artist"),
            code = "FIRE001",
            status = "Confirmed"
        ),
        ProjectItem(
            name = "LED Garden",
            description = "Interactive LED installation in the shape of a garden",
            artist = Artist("LED Creator"),
            code = "LED002",
            status = "In Progress"
        )
    )
    
    private val samplePerformanceProjects = listOf(
        ProjectItem(
            name = "Desert Dance",
            description = "Contemporary dance performance",
            artist = Artist("Dance Troupe"),
            code = "DANCE001",
            status = "Confirmed"
        )
    )
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Set up the full dependency chain with MockK
        dataSource = mockk()
        repository = ProjectsRepositoryImpl(dataSource)
        projectsViewModel = ProjectsViewModel(repository)
        projectTabViewModel = ProjectTabViewModel(repository, ProjectType.ART)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `full flow from data source to UI state should work correctly`() = runTest {
        // Given successful data source
        coEvery { dataSource.loadProjectsByType(ProjectType.ART) } returns sampleArtProjects
        
        // When loading projects in tab view model
        projectTabViewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then UI state should reflect loaded data
        val uiState = projectTabViewModel.uiState.first()
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
        assertEquals(sampleArtProjects, uiState.projects)
        assertEquals(sampleArtProjects, uiState.filteredProjects)
    }
    
    @Test
    fun `error flow from data source to UI state should work correctly`() = runTest {
        // Given data source that will fail
        val errorMessage = "Failed to load JSON file"
        coEvery { dataSource.loadProjectsByType(ProjectType.ART) } throws Exception(errorMessage)
        
        // When loading projects
        projectTabViewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then UI state should reflect error
        val uiState = projectTabViewModel.uiState.first()
        assertFalse(uiState.isLoading)
        assertNotNull(uiState.error)
        assertEquals(errorMessage, uiState.error)
        assertTrue(uiState.projects.isEmpty())
        assertTrue(uiState.filteredProjects.isEmpty())
    }
    
    @Test
    fun `search functionality should work across the full stack`() = runTest {
        // Given loaded projects
        coEvery { dataSource.loadProjectsByType(ProjectType.ART) } returns sampleArtProjects
        projectTabViewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When searching for "Fire"
        projectTabViewModel.updateSearchQuery("Fire")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should filter correctly
        val uiState = projectTabViewModel.uiState.first()
        assertEquals("Fire", uiState.searchQuery)
        assertEquals(1, uiState.filteredProjects.size)
        assertEquals("Fire Sculpture", uiState.filteredProjects.first().name)
    }
    
    @Test
    fun `different project types should load different data`() = runTest {
        // Given different data for different project types
        coEvery { dataSource.loadProjectsByType(ProjectType.ART) } returns sampleArtProjects
        coEvery { dataSource.loadProjectsByType(ProjectType.PERFORMANCES) } returns samplePerformanceProjects
        
        // When creating view models for different types
        val artViewModel = ProjectTabViewModel(repository, ProjectType.ART)
        val performanceViewModel = ProjectTabViewModel(repository, ProjectType.PERFORMANCES)
        
        artViewModel.loadProjects()
        performanceViewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then each should load correct data
        val artState = artViewModel.uiState.first()
        val performanceState = performanceViewModel.uiState.first()
        
        assertEquals(sampleArtProjects, artState.projects)
        assertEquals(samplePerformanceProjects, performanceState.projects)
    }
    
    @Test
    fun `projects view model should manage tab state correctly`() = runTest {
        // Given projects view model
        val viewModel = projectsViewModel
        
        // When updating tab
        viewModel.updateCurrentTab(3)
        
        // Then state should be updated
        val screenState = viewModel.screenUiState.first()
        assertEquals(3, screenState.currentTabIndex)
        assertEquals(ProjectType.MOBILE_ART, screenState.tabs[3])
    }
    
    @Test
    fun `retry functionality should work across full stack`() = runTest {
        // Given initial error
        coEvery { dataSource.loadProjectsByType(ProjectType.ART) } throws Exception("Network error")
        projectTabViewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify error state
        val errorState = projectTabViewModel.uiState.first()
        assertNotNull(errorState.error)
        
        // When fixing data source and retrying
        coEvery { dataSource.loadProjectsByType(ProjectType.ART) } returns sampleArtProjects
        projectTabViewModel.retryLoading()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should recover successfully
        val successState = projectTabViewModel.uiState.first()
        assertNull(successState.error)
        assertEquals(sampleArtProjects, successState.projects)
    }
}