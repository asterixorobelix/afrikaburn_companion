package io.asterixorobelix.afrikaburn.data.datasource

import io.asterixorobelix.afrikaburn.models.ProjectType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class JsonResourceDataSourceImplTest {
    
    @Test
    fun `loadProjectsByType should map project types to correct file names`() = runTest {
        // Given a data source implementation (we test the mapping logic)
        val dataSource = JsonResourceDataSourceImpl()
        
        // When/Then we verify the correct file mapping exists in ProjectType enum
        assertEquals("WTFArtworks.json", ProjectType.ART.fileName)
        assertEquals("WTFPerformances.json", ProjectType.PERFORMANCES.fileName)
        assertEquals("WTFEvents.json", ProjectType.EVENTS.fileName)
        assertEquals("WTFRovingArtworks.json", ProjectType.MOBILE_ART.fileName)
        assertEquals("WTFMutantVehicles.json", ProjectType.VEHICLES.fileName)
        assertEquals("WTFThemeCamps.json", ProjectType.CAMPS.fileName)
    }
    
    @Test
    fun `loadProjectsByType should throw DataSourceException when file not found`() = runTest {
        // Given a data source that will fail to find a file
        val dataSource = JsonResourceDataSourceImpl()
        
        // When trying to load from non-existent project type
        // Then should throw DataSourceException
        assertFailsWith<DataSourceException> {
            dataSource.loadProjectsByType(ProjectType.ART)
        }
    }
    
    @Test
    fun `DataSourceException should contain meaningful error message`() = runTest {
        // Given a data source that will fail
        val dataSource = JsonResourceDataSourceImpl()
        
        // When catching the exception
        val exception = assertFailsWith<DataSourceException> {
            dataSource.loadProjectsByType(ProjectType.ART)
        }
        
        // Then should have meaningful error message
        assertTrue(
            exception.message?.contains("Failed to load Art") == true,
            "Exception message should contain project type: ${exception.message}"
        )
    }
    
    @Test
    fun `DataSourceException should preserve original cause`() = runTest {
        // Given a data source that will fail
        val dataSource = JsonResourceDataSourceImpl()
        
        // When catching the exception
        val exception = assertFailsWith<DataSourceException> {
            dataSource.loadProjectsByType(ProjectType.PERFORMANCES)
        }
        
        // Then should have a cause
        assertTrue(
            exception.cause != null,
            "DataSourceException should preserve the original cause"
        )
    }
    
    @Test
    fun `project types should have correct display names`() {
        // Test that all project types have meaningful display names
        assertEquals("Art", ProjectType.ART.displayName)
        assertEquals("Performances", ProjectType.PERFORMANCES.displayName)
        assertEquals("Events", ProjectType.EVENTS.displayName)
        assertEquals("Mobile Art", ProjectType.MOBILE_ART.displayName)
        assertEquals("Vehicles", ProjectType.VEHICLES.displayName)
        assertEquals("Camps", ProjectType.CAMPS.displayName)
    }
}