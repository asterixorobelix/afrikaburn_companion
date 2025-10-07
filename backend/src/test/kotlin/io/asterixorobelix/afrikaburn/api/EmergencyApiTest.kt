package io.asterixorobelix.afrikaburn.api

import io.asterixorobelix.afrikaburn.domain.ContactType
import io.asterixorobelix.afrikaburn.domain.EmergencyContact
import io.asterixorobelix.afrikaburn.plugins.configureHTTP
import io.asterixorobelix.afrikaburn.plugins.configureRouting
import io.asterixorobelix.afrikaburn.plugins.configureSerialization
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmergencyApiTest {
    
    @Test
    fun `test get emergency contacts returns list of contacts`() = testApplication {
        application {
            configureSerialization()
            configureHTTP()
            configureRouting()
        }
        
        val eventId = "123e4567-e89b-12d3-a456-426614174000"
        val response = client.get("/api/v1/events/$eventId/emergency-contacts")
        
        assertEquals(HttpStatusCode.OK, response.status)
        
        val contacts = Json.decodeFromString<List<EmergencyContact>>(response.bodyAsText())
        assertTrue(contacts.isNotEmpty())
        
        // Verify we have all contact types
        val contactTypes = contacts.map { it.contactType }.distinct()
        assertTrue(contactTypes.contains(ContactType.RANGER))
        assertTrue(contactTypes.contains(ContactType.MEDICAL))
        assertTrue(contactTypes.contains(ContactType.EMERGENCY))
        assertTrue(contactTypes.contains(ContactType.ADMIN))
    }
    
    @Test
    fun `test get emergency contacts with invalid event id returns bad request`() = testApplication {
        application {
            configureSerialization()
            configureHTTP()
            configureRouting()
        }
        
        val invalidEventId = "not-a-uuid"
        val response = client.get("/api/v1/events/$invalidEventId/emergency-contacts")
        
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
    
    @Test
    fun `test emergency contacts have realistic AfrikaBurn data`() = testApplication {
        application {
            configureSerialization()
            configureHTTP()
            configureRouting()
        }
        
        val eventId = "123e4567-e89b-12d3-a456-426614174000"
        val response = client.get("/api/v1/events/$eventId/emergency-contacts")
        val contacts = Json.decodeFromString<List<EmergencyContact>>(response.bodyAsText())
        
        // Verify Rangers HQ exists with 24/7 availability
        val rangersHq = contacts.find { it.name == "Rangers HQ" }
        assertTrue(rangersHq != null)
        assertTrue(rangersHq.isAvailable24Hours)
        assertEquals(ContactType.RANGER, rangersHq.contactType)
        assertEquals(100, rangersHq.priority)
        
        // Verify Medical Centre exists
        val medicalCentre = contacts.find { it.name == "Medical Centre - Main" }
        assertTrue(medicalCentre != null)
        assertTrue(medicalCentre.isAvailable24Hours)
        assertEquals(ContactType.MEDICAL, medicalCentre.contactType)
        
        // Verify Sanctuary exists for psychological support
        val sanctuary = contacts.find { it.name == "Sanctuary" }
        assertTrue(sanctuary != null)
        assertEquals(ContactType.EMERGENCY, sanctuary.contactType)
        assertTrue(sanctuary.description?.contains("psychological", ignoreCase = true) ?: false)
    }
}