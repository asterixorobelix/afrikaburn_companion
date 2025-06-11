package io.asterixorobelix.afrikaburn.presentation.projects

import io.asterixorobelix.afrikaburn.domain.repository.ProjectsRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectsViewModelTest {
    
    private lateinit var repository: ProjectsRepository
    private lateinit var viewModel: ProjectsViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = StubProjectsRepository()
        viewModel = ProjectsViewModel(repository)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should have all project types and default tab index`() = runTest {
        // When getting initial state
        val initialState = viewModel.screenUiState.first()
        
        // Then it should have all project types
        assertEquals(0, initialState.currentTabIndex)
        assertEquals(6, initialState.tabs.size)
        assertEquals(ProjectType.ART, initialState.tabs[0])
        assertEquals(ProjectType.PERFORMANCES, initialState.tabs[1])
        assertEquals(ProjectType.EVENTS, initialState.tabs[2])
        assertEquals(ProjectType.MOBILE_ART, initialState.tabs[3])
        assertEquals(ProjectType.VEHICLES, initialState.tabs[4])
        assertEquals(ProjectType.CAMPS, initialState.tabs[5])
    }
    
    @Test
    fun `updateCurrentTab should update tab index`() = runTest {
        // Given initial state
        val initialState = viewModel.screenUiState.first()
        assertEquals(0, initialState.currentTabIndex)
        
        // When updating to tab 2
        viewModel.updateCurrentTab(2)
        
        // Then tab index should be updated
        val updatedState = viewModel.screenUiState.first()
        assertEquals(2, updatedState.currentTabIndex)
        assertEquals(ProjectType.EVENTS, updatedState.tabs[updatedState.currentTabIndex])
    }
    
    @Test
    fun `updateCurrentTab should handle all valid tab indices`() = runTest {
        // Test all valid tab indices
        for (index in 0 until 6) {
            // When updating to each tab
            viewModel.updateCurrentTab(index)
            
            // Then tab index should be updated correctly
            val state = viewModel.screenUiState.first()
            assertEquals(index, state.currentTabIndex)
        }
    }
    
    @Test
    fun `tabs should be in correct order`() = runTest {
        // When getting tabs
        val state = viewModel.screenUiState.first()
        val tabs = state.tabs
        
        // Then they should be in the expected order
        assertEquals(ProjectType.ART, tabs[0])
        assertEquals(ProjectType.PERFORMANCES, tabs[1])
        assertEquals(ProjectType.EVENTS, tabs[2])
        assertEquals(ProjectType.MOBILE_ART, tabs[3])
        assertEquals(ProjectType.VEHICLES, tabs[4])
        assertEquals(ProjectType.CAMPS, tabs[5])
    }
}

private class StubProjectsRepository : ProjectsRepository {
    override suspend fun getProjectsByType(type: ProjectType): Result<List<ProjectItem>> {
        return Result.success(emptyList())
    }
}