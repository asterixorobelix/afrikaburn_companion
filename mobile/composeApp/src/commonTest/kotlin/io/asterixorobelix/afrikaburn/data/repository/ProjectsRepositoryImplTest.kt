package io.asterixorobelix.afrikaburn.data.repository

import io.asterixorobelix.afrikaburn.data.datasource.JsonResourceDataSource
import io.asterixorobelix.afrikaburn.domain.repository.ProjectsRepository
import io.asterixorobelix.afrikaburn.models.Artist
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProjectsRepositoryImplTest {
    
    private val sampleProjects = listOf(
        ProjectItem(
            name = "Art Installation 1",
            description = "Beautiful art piece",
            artist = Artist("Artist One"),
            code = "ART001",
            status = "Confirmed"
        ),
        ProjectItem(
            name = "Art Installation 2", 
            description = "Another art piece",
            artist = Artist("Artist Two"),
            code = "ART002",
            status = "In Progress"
        )
    )
    
    @Test
    fun `getProjectsByType should return success when data source succeeds`() = runTest {
        // Given successful data source
        val dataSource = MockJsonResourceDataSourceForRepository().apply {
            setSuccessResponse(sampleProjects)
        }
        val repository: ProjectsRepository = ProjectsRepositoryImpl(dataSource)
        
        // When getting projects
        val result = repository.getProjectsByType(ProjectType.ART)
        
        // Then should return success with projects
        assertTrue(result.isSuccess)
        assertEquals(sampleProjects, result.getOrNull())
    }
    
    @Test
    fun `getProjectsByType should return failure when data source throws exception`() = runTest {
        // Given data source that throws exception
        val errorMessage = "Failed to load JSON file"
        val dataSource = MockJsonResourceDataSourceForRepository().apply {
            setErrorResponse(errorMessage)
        }
        val repository: ProjectsRepository = ProjectsRepositoryImpl(dataSource)
        
        // When getting projects
        val result = repository.getProjectsByType(ProjectType.ART)
        
        // Then should return failure
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `getProjectsByType should handle different project types`() = runTest {
        // Given data source with different responses for different types
        val dataSource = MockJsonResourceDataSourceForRepository().apply {
            setSuccessResponse(sampleProjects)
        }
        val repository: ProjectsRepository = ProjectsRepositoryImpl(dataSource)
        
        // When getting different project types
        val artResult = repository.getProjectsByType(ProjectType.ART)
        val performanceResult = repository.getProjectsByType(ProjectType.PERFORMANCES)
        val eventsResult = repository.getProjectsByType(ProjectType.EVENTS)
        
        // Then all should succeed
        assertTrue(artResult.isSuccess)
        assertTrue(performanceResult.isSuccess)
        assertTrue(eventsResult.isSuccess)
    }
    
    @Test
    fun `getProjectsByType should return empty list when data source returns empty`() = runTest {
        // Given data source with empty response
        val dataSource = MockJsonResourceDataSourceForRepository().apply {
            setSuccessResponse(emptyList())
        }
        val repository: ProjectsRepository = ProjectsRepositoryImpl(dataSource)
        
        // When getting projects
        val result = repository.getProjectsByType(ProjectType.ART)
        
        // Then should return success with empty list
        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrNull())
    }
    
    @Test
    fun `getProjectsByType should pass correct project type to data source`() = runTest {
        // Given tracked data source
        val dataSource = MockJsonResourceDataSourceForRepository().apply {
            setSuccessResponse(sampleProjects)
        }
        val repository: ProjectsRepository = ProjectsRepositoryImpl(dataSource)
        
        // When getting specific project type
        repository.getProjectsByType(ProjectType.VEHICLES)
        
        // Then data source should be called with correct type
        assertEquals(ProjectType.VEHICLES, dataSource.lastRequestedType)
    }
}

internal class MockJsonResourceDataSourceForRepository : JsonResourceDataSource {
    private var shouldThrowError = false
    private var errorMessage = ""
    private var projects = emptyList<ProjectItem>()
    var lastRequestedType: ProjectType? = null
    
    fun setSuccessResponse(projectList: List<ProjectItem>) {
        shouldThrowError = false
        projects = projectList
    }
    
    fun setErrorResponse(message: String) {
        shouldThrowError = true
        errorMessage = message
    }
    
    override suspend fun loadProjectsByType(type: ProjectType): List<ProjectItem> {
        lastRequestedType = type
        
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }
        
        return projects
    }
}