package io.asterixorobelix.afrikaburn.data.repository

import io.asterixorobelix.afrikaburn.data.datasource.JsonResourceDataSource
import io.asterixorobelix.afrikaburn.domain.repository.ProjectsRepository
import io.asterixorobelix.afrikaburn.models.Artist
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
        val originalError = "Failed to load JSON file"
        val dataSource = MockJsonResourceDataSourceForRepository().apply {
            setErrorResponse(originalError)
        }
        val repository: ProjectsRepository = ProjectsRepositoryImpl(dataSource)
        
        // When getting projects
        val result = repository.getProjectsByType(ProjectType.ART)
        
        // Then should return failure with wrapped error message
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
        assertEquals("Unexpected error loading Art", result.exceptionOrNull()?.message)
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

    @Test
    fun `getProjectsByType should only call data source once for same type under concurrent load`() = runTest {
        // Given a data source that tracks call count
        val dataSource = MockJsonResourceDataSourceForRepository().apply {
            setSuccessResponse(sampleProjects)
        }
        val repository = ProjectsRepositoryImpl(dataSource)

        // When multiple coroutines request the same type simultaneously
        val results = (1..10).map {
            async { repository.getProjectsByType(ProjectType.ART) }
        }.awaitAll()

        // Then all results are successful and match expected data
        assertTrue(results.all { it.isSuccess })
        assertTrue(results.all { it.getOrNull() == sampleProjects })

        // And data source was called only once (cache served the rest)
        assertEquals(1, dataSource.loadCallCount)
    }
}

internal class MockJsonResourceDataSourceForRepository : JsonResourceDataSource {
    private var shouldThrowError = false
    private var errorMessage = ""
    private var projects = emptyList<ProjectItem>()
    var lastRequestedType: ProjectType? = null
    var loadCallCount = 0
    
    fun setSuccessResponse(projectList: List<ProjectItem>) {
        shouldThrowError = false
        projects = projectList
    }
    
    fun setErrorResponse(message: String) {
        shouldThrowError = true
        errorMessage = message
    }
    
    override suspend fun loadProjectsByType(type: ProjectType): List<ProjectItem> {
        loadCallCount++
        lastRequestedType = type
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }
        return projects
    }
}