package io.asterixorobelix.afrikaburn.api

import io.asterixorobelix.afrikaburn.domain.ThemeCamp
import io.asterixorobelix.afrikaburn.plugins.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.*
import io.ktor.client.plugins.contentnegotiation.*

class ThemeCampsApiTest {
    
    private val testEventId = "550e8400-e29b-41d4-a716-446655440000"
    
    @Test
    fun `GET theme camps should return list of camps without location`() = testApplication {
        application {
            configureRouting()
            configureSerialization()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }
        }
        
        val response = client.get("/api/v1/events/$testEventId/theme-camps")
        
        assertEquals(HttpStatusCode.OK, response.status)
        val camps: List<ThemeCamp> = response.body()
        
        // Should return only non-hidden camps when no location provided
        assertTrue(camps.isNotEmpty())
        assertTrue(camps.all { !it.isHidden })
    }
    
    @Test
    fun `GET theme camps should return hidden camps when within location radius`() = testApplication {
        application {
            configureRouting()
            configureSerialization()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }
        }
        
        // Location near the Secret Speakeasy (-32.3910, 19.4480)
        val response = client.get("/api/v1/events/$testEventId/theme-camps") {
            parameter("lat", -32.3912)
            parameter("lng", 19.4478)
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        val camps: List<ThemeCamp> = response.body()
        
        // Should include hidden camps when user is nearby
        assertTrue(camps.isNotEmpty())
        assertTrue(camps.any { it.isHidden && it.name == "Secret Speakeasy" })
    }
    
    @Test
    fun `GET theme camps should exclude hidden camps when outside location radius`() = testApplication {
        application {
            configureRouting()
            configureSerialization()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }
        }
        
        // Location far from any hidden camps (10km away)
        val response = client.get("/api/v1/events/$testEventId/theme-camps") {
            parameter("lat", -32.2000)
            parameter("lng", 19.5000)
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        val camps: List<ThemeCamp> = response.body()
        
        // Should not include hidden camps when user is far away
        assertTrue(camps.isNotEmpty())
        assertTrue(camps.all { !it.isHidden })
    }
    
    @Test
    fun `GET theme camps should return bad request for invalid event ID`() = testApplication {
        application {
            configureRouting()
            configureSerialization()
        }
        
        val response = client.get("/api/v1/events/invalid-uuid/theme-camps")
        
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
    
    @Test
    fun `GET theme camps should validate required fields in response`() = testApplication {
        application {
            configureRouting()
            configureSerialization()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }
        }
        
        val response = client.get("/api/v1/events/$testEventId/theme-camps")
        
        assertEquals(HttpStatusCode.OK, response.status)
        val camps: List<ThemeCamp> = response.body()
        
        // Validate all required fields are present
        camps.forEach { camp ->
            assertNotNull(camp.id)
            assertEquals(testEventId, camp.eventId)
            assertTrue(camp.name.isNotEmpty())
            assertNotNull(camp.latitude)
            assertNotNull(camp.longitude)
            assertNotNull(camp.lastUpdated)
        }
    }
}