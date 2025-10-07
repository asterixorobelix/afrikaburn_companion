package io.asterixorobelix.afrikaburn.models

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectItemTest {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    @Test
    fun `ProjectItem should deserialize from JSON correctly`() {
        // Given JSON with all fields
        val jsonString = """
            {
                "Name": "Test Art Installation",
                "Description": "A beautiful test installation",
                "Artist": {"s": "Test Artist"},
                "code": "TEST001",
                "status": "Confirmed"
            }
        """.trimIndent()
        
        // When deserializing
        val projectItem = json.decodeFromString<ProjectItem>(jsonString)
        
        // Then all fields should be mapped correctly
        assertEquals("Test Art Installation", projectItem.name)
        assertEquals("A beautiful test installation", projectItem.description)
        assertEquals("Test Artist", projectItem.artist.name)
        assertEquals("TEST001", projectItem.code)
        assertEquals("Confirmed", projectItem.status)
    }
    
    @Test
    fun `ProjectItem should handle missing optional fields`() {
        // Given JSON with only required fields
        val jsonString = """
            {
                "Name": "Minimal Project",
                "Description": "Just the basics"
            }
        """.trimIndent()
        
        // When deserializing
        val projectItem = json.decodeFromString<ProjectItem>(jsonString)
        
        // Then should use default values
        assertEquals("Minimal Project", projectItem.name)
        assertEquals("Just the basics", projectItem.description)
        assertEquals("", projectItem.artist.name)
        assertEquals("", projectItem.code)
        assertEquals("", projectItem.status)
    }
    
    @Test
    fun `ProjectItem should handle empty artist object`() {
        // Given JSON with empty artist
        val jsonString = """
            {
                "Name": "Project Without Artist",
                "Description": "No artist specified",
                "Artist": {}
            }
        """.trimIndent()
        
        // When deserializing
        val projectItem = json.decodeFromString<ProjectItem>(jsonString)
        
        // Then artist should have empty name
        assertEquals("Project Without Artist", projectItem.name)
        assertEquals("", projectItem.artist.name)
    }
    
    @Test
    fun `ProjectItem should serialize to JSON correctly`() {
        // Given a ProjectItem
        val projectItem = ProjectItem(
            name = "Test Project",
            description = "Test Description",
            artist = Artist("Test Artist"),
            code = "TEST123",
            status = "Active"
        )
        
        // When serializing
        val jsonString = json.encodeToString(ProjectItem.serializer(), projectItem)
        
        // Then should contain all fields
        val expectedFields = listOf(
            "\"Name\":\"Test Project\"",
            "\"Description\":\"Test Description\"",
            "\"Artist\":{\"s\":\"Test Artist\"}",
            "\"code\":\"TEST123\"",
            "\"status\":\"Active\""
        )
        
        expectedFields.forEach { field ->
            assertTrue(jsonString.contains(field),
                "JSON should contain $field, but was: $jsonString")
        }
    }
    
    @Test
    fun `Artist should deserialize from different JSON formats`() {
        // Test the nested "s" property format
        val artistJson = """{"s": "Artist Name"}"""
        val artist = json.decodeFromString<Artist>(artistJson)
        assertEquals("Artist Name", artist.name)
    }
    
    @Test
    fun `Artist should handle missing name field`() {
        // Given JSON without name
        val artistJson = """{}"""
        
        // When deserializing
        val artist = json.decodeFromString<Artist>(artistJson)
        
        // Then should use default empty string
        assertEquals("", artist.name)
    }
}