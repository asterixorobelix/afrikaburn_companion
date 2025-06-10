package io.asterixorobelix.afrikaburn.presentation.projects

import io.asterixorobelix.afrikaburn.models.ProjectType
import kotlin.test.Test
import kotlin.test.assertEquals

class ProjectsScreenUiStateTest {
    
    @Test
    fun `default state should have correct initial values`() {
        // Given default state
        val state = ProjectsScreenUiState()
        
        // Then should have expected defaults
        assertEquals(0, state.currentTabIndex)
        assertEquals(6, state.tabs.size)
        assertEquals(ProjectType.ART, state.tabs[0])
        assertEquals(ProjectType.PERFORMANCES, state.tabs[1])
        assertEquals(ProjectType.EVENTS, state.tabs[2])
        assertEquals(ProjectType.MOBILE_ART, state.tabs[3])
        assertEquals(ProjectType.VEHICLES, state.tabs[4])
        assertEquals(ProjectType.CAMPS, state.tabs[5])
    }
    
    @Test
    fun `tabs should be in correct order`() {
        // Given state
        val state = ProjectsScreenUiState()
        
        // Then tabs should be in expected order
        val expectedOrder = listOf(
            ProjectType.ART,
            ProjectType.PERFORMANCES,
            ProjectType.EVENTS,
            ProjectType.MOBILE_ART,
            ProjectType.VEHICLES,
            ProjectType.CAMPS
        )
        
        assertEquals(expectedOrder, state.tabs)
    }
    
    @Test
    fun `state with custom tab index should maintain tab order`() {
        // Given state with custom tab index
        val state = ProjectsScreenUiState(currentTabIndex = 3)
        
        // Then should maintain correct tab index and order
        assertEquals(3, state.currentTabIndex)
        assertEquals(ProjectType.MOBILE_ART, state.tabs[state.currentTabIndex])
        assertEquals(6, state.tabs.size)
    }
    
    @Test
    fun `copy function should work correctly`() {
        // Given initial state
        val initialState = ProjectsScreenUiState()
        
        // When copying with new tab index
        val updatedState = initialState.copy(currentTabIndex = 2)
        
        // Then should have updated tab index but same tabs
        assertEquals(2, updatedState.currentTabIndex)
        assertEquals(initialState.tabs, updatedState.tabs)
        assertEquals(0, initialState.currentTabIndex) // Original unchanged
    }
}