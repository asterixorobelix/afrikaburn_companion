package io.asterixorobelix.afrikaburn.presentation.projects

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
    
    private lateinit var viewModel: ProjectsViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ProjectsViewModel()
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
        val content = initialState as ProjectsScreenUiState.Content
        assertEquals(0, content.currentTabIndex)
        assertEquals(6, content.tabs.size)
        assertEquals(ProjectType.ART, content.tabs[0])
        assertEquals(ProjectType.PERFORMANCES, content.tabs[1])
        assertEquals(ProjectType.EVENTS, content.tabs[2])
        assertEquals(ProjectType.MOBILE_ART, content.tabs[3])
        assertEquals(ProjectType.VEHICLES, content.tabs[4])
        assertEquals(ProjectType.CAMPS, content.tabs[5])
    }
    
    @Test
    fun `updateCurrentTab should update tab index`() = runTest {
        // Given initial state
        val initialState = viewModel.screenUiState.first() as ProjectsScreenUiState.Content
        assertEquals(0, initialState.currentTabIndex)
        
        // When updating to tab 2
        viewModel.updateCurrentTab(2)
        
        // Then tab index should be updated
        val updatedState = viewModel.screenUiState.first() as ProjectsScreenUiState.Content
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
            val state = viewModel.screenUiState.first() as ProjectsScreenUiState.Content
            assertEquals(index, state.currentTabIndex)
        }
    }
    
    @Test
    fun `tabs should be in correct order`() = runTest {
        // When getting tabs
        val state = viewModel.screenUiState.first() as ProjectsScreenUiState.Content
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
