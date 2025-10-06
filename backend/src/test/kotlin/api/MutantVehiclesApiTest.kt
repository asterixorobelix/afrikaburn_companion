package api

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*
import java.util.*

/**
 * Contract tests for GET /events/{eventId}/mutant-vehicles endpoint
 * Validates API compliance with OpenAPI specification
 * 
 * These tests MUST FAIL initially as implementation doesn't exist yet.
 * Following TDD approach - tests define the contract before implementation.
 */
class MutantVehiclesApiTest : DescribeSpec({
    describe("GET /events/{eventId}/mutant-vehicles endpoint") {
        val testEventId = UUID.randomUUID().toString()
        
        it("should return 200 with mutant vehicles array for valid eventId") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/mutant-vehicles")
                
                // Contract validation: Status code
                response.status shouldBe HttpStatusCode.OK
                
                // Contract validation: Content-Type
                response.headers[HttpHeaders.ContentType] shouldContain "application/json"
                
                // Contract validation: Response structure
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                jsonResponse.jsonArray // Should be an array
                
                // Contract validation: Each mutant vehicle should have required fields
                if (jsonResponse.jsonArray.isNotEmpty()) {
                    val firstVehicle = jsonResponse.jsonArray.first().jsonObject
                    
                    // Required fields from OpenAPI spec
                    firstVehicle.containsKey("id") shouldBe true
                    firstVehicle.containsKey("event_id") shouldBe true
                    firstVehicle.containsKey("name") shouldBe true
                    firstVehicle.containsKey("description") shouldBe true
                    
                    // Validate data types for required fields
                    val vehicleId = firstVehicle["id"]?.jsonPrimitive?.content
                    if (vehicleId != null) {
                        UUID.fromString(vehicleId) // Should be valid UUID
                    }
                    
                    val eventId = firstVehicle["event_id"]?.jsonPrimitive?.content
                    if (eventId != null) {
                        UUID.fromString(eventId) // Should be valid UUID
                    }
                    
                    // Validate string length constraints from spec
                    val name = firstVehicle["name"]?.jsonPrimitive?.content
                    if (name != null) {
                        name.isNotBlank() shouldBe true
                        (name.length <= 200) shouldBe true
                    }
                    
                    val description = firstVehicle["description"]?.jsonPrimitive?.content
                    if (description != null) {
                        description.isNotBlank() shouldBe true
                        (description.length <= 2000) shouldBe true
                    }
                }
            }
        }
        
        it("should return filtered vehicles based on search parameter") {
            testApplication {
                val searchQuery = "sound"
                val response = client.get("/api/v1/events/$testEventId/mutant-vehicles?search=$searchQuery")
                
                response.status shouldBe HttpStatusCode.OK
                response.headers[HttpHeaders.ContentType] shouldContain "application/json"
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                jsonResponse.jsonArray // Should be an array
                
                // Contract validation: Search should filter results
                if (jsonResponse.jsonArray.isNotEmpty()) {
                    val vehicle = jsonResponse.jsonArray.first().jsonObject
                    
                    // At least one of name, description, or search_tags should contain search term
                    val name = vehicle["name"]?.jsonPrimitive?.content?.lowercase()
                    val description = vehicle["description"]?.jsonPrimitive?.content?.lowercase()
                    val searchTags = vehicle["search_tags"]?.jsonArray
                    
                    val hasSearchTermInName = name?.contains(searchQuery.lowercase()) ?: false
                    val hasSearchTermInDescription = description?.contains(searchQuery.lowercase()) ?: false
                    val hasSearchTermInTags = searchTags?.any { 
                        it.jsonPrimitive.content.lowercase().contains(searchQuery.lowercase())
                    } ?: false
                    
                    // At least one should match (backend search logic)
                    (hasSearchTermInName || hasSearchTermInDescription || hasSearchTermInTags) shouldBe true
                }
            }
        }
        
        it("should validate optional fields and data types") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/mutant-vehicles")
                
                response.status shouldBe HttpStatusCode.OK
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                
                if (jsonResponse.jsonArray.isNotEmpty()) {
                    val vehicle = jsonResponse.jsonArray.first().jsonObject
                    
                    // Optional string fields validation
                    if (vehicle.containsKey("owner_name")) {
                        val ownerName = vehicle["owner_name"]?.jsonPrimitive?.content
                        if (ownerName != null) {
                            (ownerName.length <= 200) shouldBe true
                        }
                    }
                    
                    if (vehicle.containsKey("schedule_info")) {
                        val scheduleInfo = vehicle["schedule_info"]?.jsonPrimitive?.content
                        if (scheduleInfo != null) {
                            (scheduleInfo.length <= 500) shouldBe true
                        }
                    }
                    
                    // Location fields validation (nullable)
                    if (vehicle.containsKey("last_known_latitude")) {
                        val lat = vehicle["last_known_latitude"]?.jsonPrimitive?.doubleOrNull
                        if (lat != null) {
                            (lat >= -90.0 && lat <= 90.0) shouldBe true
                        }
                    }
                    
                    if (vehicle.containsKey("last_known_longitude")) {
                        val lng = vehicle["last_known_longitude"]?.jsonPrimitive?.doubleOrNull
                        if (lng != null) {
                            (lng >= -180.0 && lng <= 180.0) shouldBe true
                        }
                    }
                    
                    // Timestamp fields validation
                    if (vehicle.containsKey("last_location_update")) {
                        val timestamp = vehicle["last_location_update"]?.jsonPrimitive?.longOrNull
                        if (timestamp != null) {
                            (timestamp > 0) shouldBe true
                        }
                    }
                    
                    if (vehicle.containsKey("last_updated")) {
                        vehicle["last_updated"]?.jsonPrimitive?.longOrNull shouldBe Long::class.java.simpleName
                    }
                    
                    // Boolean fields validation
                    if (vehicle.containsKey("is_active")) {
                        vehicle["is_active"]?.jsonPrimitive?.booleanOrNull shouldBe Boolean::class.java.simpleName
                    }
                    
                    // Arrays validation
                    if (vehicle.containsKey("photo_urls")) {
                        vehicle["photo_urls"]?.jsonArray // Should be an array
                    }
                    
                    if (vehicle.containsKey("search_tags")) {
                        vehicle["search_tags"]?.jsonArray // Should be an array
                    }
                }
            }
        }
        
        it("should return 404 for invalid eventId") {
            testApplication {
                val invalidId = "invalid-uuid"
                val response = client.get("/api/v1/events/$invalidId/mutant-vehicles")
                
                // Contract validation: Error response structure
                response.status shouldBe HttpStatusCode.NotFound
                response.headers[HttpHeaders.ContentType] shouldContain "application/json"
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                jsonResponse.containsKey("error") shouldBe true
                jsonResponse.containsKey("message") shouldBe true
            }
        }
        
        it("should handle empty search parameter gracefully") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/mutant-vehicles?search=")
                
                // Should either return all vehicles or handle empty search gracefully
                response.status shouldBe HttpStatusCode.OK
                response.headers[HttpHeaders.ContentType] shouldContain "application/json"
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                jsonResponse.jsonArray // Should be an array
            }
        }
        
        it("should validate photo URLs format") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/mutant-vehicles")
                
                response.status shouldBe HttpStatusCode.OK
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                
                if (jsonResponse.jsonArray.isNotEmpty()) {
                    val vehicle = jsonResponse.jsonArray.first().jsonObject
                    
                    // Photo URLs array validation
                    if (vehicle.containsKey("photo_urls")) {
                        val photoUrls = vehicle["photo_urls"]?.jsonArray
                        if (photoUrls != null && photoUrls.isNotEmpty()) {
                            photoUrls.forEach { urlElement ->
                                val url = urlElement.jsonPrimitive.content
                                // Basic URL validation - should start with http/https
                                (url.startsWith("http://") || url.startsWith("https://")) shouldBe true
                            }
                        }
                    }
                }
            }
        }
        
        it("should validate search tags array") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/mutant-vehicles")
                
                response.status shouldBe HttpStatusCode.OK
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                
                if (jsonResponse.jsonArray.isNotEmpty()) {
                    val vehicle = jsonResponse.jsonArray.first().jsonObject
                    
                    // Search tags validation
                    if (vehicle.containsKey("search_tags")) {
                        val searchTags = vehicle["search_tags"]?.jsonArray
                        if (searchTags != null && searchTags.isNotEmpty()) {
                            searchTags.forEach { tagElement ->
                                val tag = tagElement.jsonPrimitive.content
                                // Tags should be non-empty strings
                                tag.isNotBlank() shouldBe true
                            }
                        }
                    }
                }
            }
        }
        
        it("should handle special characters in search query") {
            testApplication {
                val specialSearchQuery = "art & music"
                val encodedQuery = "art%20%26%20music"
                val response = client.get("/api/v1/events/$testEventId/mutant-vehicles?search=$encodedQuery")
                
                // Should handle URL encoded search terms
                response.status shouldBe HttpStatusCode.OK
                response.headers[HttpHeaders.ContentType] shouldContain "application/json"
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                jsonResponse.jsonArray // Should be an array
            }
        }
        
        it("should validate location data consistency") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/mutant-vehicles")
                
                response.status shouldBe HttpStatusCode.OK
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                
                if (jsonResponse.jsonArray.isNotEmpty()) {
                    val vehicle = jsonResponse.jsonArray.first().jsonObject
                    
                    // Location consistency validation
                    val hasLatitude = vehicle.containsKey("last_known_latitude") && 
                                    vehicle["last_known_latitude"]?.jsonPrimitive?.isString == false
                    val hasLongitude = vehicle.containsKey("last_known_longitude") && 
                                     vehicle["last_known_longitude"]?.jsonPrimitive?.isString == false
                    val hasLocationUpdate = vehicle.containsKey("last_location_update") && 
                                          vehicle["last_location_update"]?.jsonPrimitive?.isString == false
                    
                    // If any location field is present, validate consistency
                    if (hasLatitude || hasLongitude || hasLocationUpdate) {
                        // If coordinates are present, timestamp should also be present
                        if (hasLatitude && hasLongitude) {
                            hasLocationUpdate shouldBe true
                        }
                    }
                }
            }
        }
        
        it("should validate vehicle activity status") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/mutant-vehicles")
                
                response.status shouldBe HttpStatusCode.OK
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                
                if (jsonResponse.jsonArray.isNotEmpty()) {
                    val vehicle = jsonResponse.jsonArray.first().jsonObject
                    
                    // is_active field validation
                    if (vehicle.containsKey("is_active")) {
                        val isActive = vehicle["is_active"]?.jsonPrimitive?.boolean
                        // Should be a boolean value
                        (isActive is Boolean) shouldBe true
                    }
                    
                    // Default value should be true according to spec
                    val isActive = vehicle["is_active"]?.jsonPrimitive?.booleanOrNull
                    if (isActive != null) {
                        (isActive is Boolean) shouldBe true
                    }
                }
            }
        }
    }
})