package io.asterixorobelix.afrikaburn.presentation.projects

import io.asterixorobelix.afrikaburn.domain.repository.ProjectsRepository
import io.asterixorobelix.afrikaburn.models.Artist
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType
import io.asterixorobelix.afrikaburn.models.TimeFilter
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
class TimeFilterViewModelTest {
    
    private lateinit var repository: MockProjectsRepositoryForTimeFilter
    private lateinit var viewModel: ProjectTabViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    private val daytimeProjects = listOf(
        ProjectItem(
            name = "Morning Coffee Camp",
            description = "Early morning coffee service",
            artist = Artist("Coffee Team"),
            code = "MC001",
            status = "Fam • Morning"
        ),
        ProjectItem(
            name = "Daytime Workshop",
            description = "Educational workshops during the day",
            artist = Artist("Workshop Team"),
            code = "DW002",
            status = "Fam • Day Time"
        )
    )
    
    private val nighttimeProjects = listOf(
        ProjectItem(
            name = "Night Dance Party",
            description = "Dancing until dawn",
            artist = Artist("Dance Team"),
            code = "ND001",
            status = "Night Time"
        ),
        ProjectItem(
            name = "All Night Rave",
            description = "Non-stop party music",
            artist = Artist("Rave Team"),
            code = "ANR002",
            status = "All Night"
        )
    )
    
    private val mixedTimeProjects = listOf(
        ProjectItem(
            name = "24/7 Camp",
            description = "Operating around the clock",
            artist = Artist("24/7 Team"),
            code = "TFS001",
            status = "Morning, Day Time, Night Time, All Night"
        )
    )
    
    private val otherProjects = listOf(
        ProjectItem(
            name = "Flexible Schedule Camp",
            description = "Schedule varies",
            artist = Artist("Flexible Team"),
            code = "FS001",
            status = "Other"
        )
    )
    
    private val allProjects = daytimeProjects + nighttimeProjects + mixedTimeProjects + otherProjects
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = MockProjectsRepositoryForTimeFilter()
        viewModel = ProjectTabViewModel(repository, ProjectType.CAMPS)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `time filter should be ALL by default`() = runTest {
        // Given projects loaded
        repository.setSuccessResponse(allProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When getting initial state
        val state = viewModel.uiState.first()
        
        // Then time filter should be ALL and show all projects
        assertEquals(TimeFilter.ALL, state.timeFilter)
        assertEquals(allProjects, state.filteredProjects)
    }
    
    @Test
    fun `updateTimeFilter to DAYTIME should show only daytime projects`() = runTest {
        // Given projects loaded
        repository.setSuccessResponse(allProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When updating time filter to DAYTIME
        viewModel.updateTimeFilter(TimeFilter.DAYTIME)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show only daytime and mixed projects
        val state = viewModel.uiState.first()
        assertEquals(TimeFilter.DAYTIME, state.timeFilter)
        assertEquals(3, state.filteredProjects.size) // daytime (2) + mixed (1)
        
        val projectNames = state.filteredProjects.map { it.name }
        assertTrue(projectNames.contains("Morning Coffee Camp"))
        assertTrue(projectNames.contains("Daytime Workshop"))
        assertTrue(projectNames.contains("24/7 Camp"))
        assertFalse(projectNames.contains("Night Dance Party"))
        assertFalse(projectNames.contains("All Night Rave"))
        assertFalse(projectNames.contains("Flexible Schedule Camp"))
    }
    
    @Test
    fun `updateTimeFilter to NIGHTTIME should show only nighttime projects`() = runTest {
        // Given projects loaded
        repository.setSuccessResponse(allProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When updating time filter to NIGHTTIME
        viewModel.updateTimeFilter(TimeFilter.NIGHTTIME)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show only nighttime and mixed projects
        val state = viewModel.uiState.first()
        assertEquals(TimeFilter.NIGHTTIME, state.timeFilter)
        assertEquals(3, state.filteredProjects.size) // nighttime (2) + mixed (1)
        
        val projectNames = state.filteredProjects.map { it.name }
        assertTrue(projectNames.contains("Night Dance Party"))
        assertTrue(projectNames.contains("All Night Rave"))
        assertTrue(projectNames.contains("24/7 Camp"))
        assertFalse(projectNames.contains("Morning Coffee Camp"))
        assertFalse(projectNames.contains("Daytime Workshop"))
        assertFalse(projectNames.contains("Flexible Schedule Camp"))
    }
    
    @Test
    fun `updateTimeFilter back to ALL should show all projects`() = runTest {
        // Given daytime filter is active
        repository.setSuccessResponse(allProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateTimeFilter(TimeFilter.DAYTIME)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When updating time filter back to ALL
        viewModel.updateTimeFilter(TimeFilter.ALL)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show all projects again
        val state = viewModel.uiState.first()
        assertEquals(TimeFilter.ALL, state.timeFilter)
        assertEquals(allProjects, state.filteredProjects)
    }
    
    @Test
    fun `time filter with search query should filter both ways`() = runTest {
        // Given projects loaded
        repository.setSuccessResponse(allProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When applying time filter and search
        viewModel.updateTimeFilter(TimeFilter.DAYTIME)
        viewModel.updateSearchQuery("Coffee")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show only daytime projects matching search
        val state = viewModel.uiState.first()
        assertEquals(TimeFilter.DAYTIME, state.timeFilter)
        assertEquals("Coffee", state.searchQuery)
        assertEquals(1, state.filteredProjects.size)
        assertEquals("Morning Coffee Camp", state.filteredProjects.first().name)
    }
    
    @Test
    fun `family filter and time filter should work together`() = runTest {
        // Given projects with family and time data
        val complexProjects = listOf(
            ProjectItem(
                name = "Family Day Camp",
                description = "Family-friendly daytime activities",
                status = "Fam • Day Time"
            ),
            ProjectItem(
                name = "Family Night Camp",
                description = "Family-friendly nighttime activities", 
                status = "Fam • Night Time"
            ),
            ProjectItem(
                name = "Adult Day Camp",
                description = "Adults-only daytime activities",
                status = "Day Time"
            ),
            ProjectItem(
                name = "Adult Night Camp",
                description = "Adults-only nighttime activities",
                status = "Night Time"
            )
        )
        
        repository.setSuccessResponse(complexProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When applying both family and time filters
        viewModel.toggleFamilyFilter()
        viewModel.updateTimeFilter(TimeFilter.DAYTIME)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show only family-friendly daytime camps
        val state = viewModel.uiState.first()
        assertTrue(state.isFamilyFilterEnabled)
        assertEquals(TimeFilter.DAYTIME, state.timeFilter)
        assertEquals(1, state.filteredProjects.size)
        assertEquals("Family Day Camp", state.filteredProjects.first().name)
    }
    
    @Test
    fun `time filter with no matching projects should show empty results`() = runTest {
        // Given only nighttime projects
        repository.setSuccessResponse(nighttimeProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When filtering for daytime
        viewModel.updateTimeFilter(TimeFilter.DAYTIME)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show no results
        val state = viewModel.uiState.first()
        assertEquals(TimeFilter.DAYTIME, state.timeFilter)
        assertTrue(state.filteredProjects.isEmpty())
        assertTrue(state.isShowingEmptySearch())
    }
    
    @Test
    fun `hasActiveFilters should return true when time filter is not ALL`() = runTest {
        // Given projects loaded
        repository.setSuccessResponse(allProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When setting time filter to DAYTIME
        viewModel.updateTimeFilter(TimeFilter.DAYTIME)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should have active filters
        val state = viewModel.uiState.first()
        assertTrue(state.hasActiveFilters())
    }
    
    @Test
    fun `hasActiveFilters should return false when time filter is ALL and no other filters`() = runTest {
        // Given projects loaded with default state
        repository.setSuccessResponse(allProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When checking filters in default state
        val state = viewModel.uiState.first()
        
        // Then should not have active filters
        assertFalse(state.hasActiveFilters())
    }
    
    @Test
    fun `search with time filter should work in any order`() = runTest {
        // Given projects loaded
        repository.setSuccessResponse(allProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When searching first then filtering time
        viewModel.updateSearchQuery("Camp")
        viewModel.updateTimeFilter(TimeFilter.NIGHTTIME)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should apply both filters correctly
        val state = viewModel.uiState.first()
        assertEquals("Camp", state.searchQuery)
        assertEquals(TimeFilter.NIGHTTIME, state.timeFilter)
        assertEquals(2, state.filteredProjects.size) // 24/7 Camp matches both
        
        val projectNames = state.filteredProjects.map { it.name }
        assertTrue(projectNames.contains("24/7 Camp"))
        assertTrue(projectNames.contains("Flexible Schedule Camp") == false) // Doesn't match time filter
    }
    
    @Test
    fun `clearing search with time filter active should maintain time filtering`() = runTest {
        // Given projects loaded with both filters active
        repository.setSuccessResponse(allProjects)
        viewModel.loadProjects()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateTimeFilter(TimeFilter.DAYTIME)
        viewModel.updateSearchQuery("Coffee")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When clearing search
        viewModel.updateSearchQuery("")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then should show all daytime projects
        val state = viewModel.uiState.first()
        assertEquals(TimeFilter.DAYTIME, state.timeFilter)
        assertEquals("", state.searchQuery)
        assertEquals(3, state.filteredProjects.size) // All daytime projects
    }
}

private class MockProjectsRepositoryForTimeFilter : ProjectsRepository {
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