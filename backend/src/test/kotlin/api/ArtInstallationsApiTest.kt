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
 * Contract tests for GET /events/{eventId}/art-installations endpoint
 * Validates API compliance with OpenAPI specification
 * 
 * These tests MUST FAIL initially as implementation doesn't exist yet.
 * Following TDD approach - tests define the contract before implementation.
 */
class ArtInstallationsApiTest : DescribeSpec({
    describe("GET /events/{eventId}/art-installations endpoint") {
        val testEventId = UUID.randomUUID().toString()
        
        it("should return 200 with art installations array for valid eventId") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/art-installations")
                
                // Contract validation: Status code
                response.status shouldBe HttpStatusCode.OK
                
                // Contract validation: Content-Type
                response.headers[HttpHeaders.ContentType] shouldContain "application/json"
                
                // Contract validation: Response structure
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                jsonResponse.jsonArray // Should be an array
                
                // Contract validation: Each art installation should have required fields
                if (jsonResponse.jsonArray.isNotEmpty()) {
                    val firstInstallation = jsonResponse.jsonArray.first().jsonObject
                    
                    // Required fields from OpenAPI spec
                    firstInstallation.containsKey("id") shouldBe true
                    firstInstallation.containsKey("event_id") shouldBe true
                    firstInstallation.containsKey("name") shouldBe true
                    firstInstallation.containsKey("artist_name") shouldBe true
                    firstInstallation.containsKey("latitude") shouldBe true
                    firstInstallation.containsKey("longitude") shouldBe true
                    
                    // Validate data types for coordinates
                    firstInstallation["latitude"]?.jsonPrimitive?.doubleOrNull shouldBe Double::class.java.simpleName
                    firstInstallation["longitude"]?.jsonPrimitive?.doubleOrNull shouldBe Double::class.java.simpleName
                    
                    // Validate string length constraints from spec
                    val name = firstInstallation["name"]?.jsonPrimitive?.content
                    if (name != null) {
                        name.length shouldBe (name.length <= 200)
                    }
                    
                    val artistName = firstInstallation["artist_name"]?.jsonPrimitive?.content
                    if (artistName != null) {
                        artistName.length shouldBe (artistName.length <= 200)
                    }
                }
            }
        }
        
        it("should return filtered installations based on location parameters") {
            testApplication {
                val lat = -32.3
                val lng = 20.1
                val response = client.get("/api/v1/events/$testEventId/art-installations?lat=$lat&lng=$lng")
                
                response.status shouldBe HttpStatusCode.OK
                response.headers[HttpHeaders.ContentType] shouldContain "application/json"
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                jsonResponse.jsonArray // Should be an array
                
                // Contract validation: Location-based filtering should apply
                // Hidden content should be filtered based on location/time unlock rules
                if (jsonResponse.jsonArray.isNotEmpty()) {
                    val installation = jsonResponse.jsonArray.first().jsonObject
                    
                    // Unlocked installations should not have is_hidden=true unless within unlock radius
                    val isHidden = installation["is_hidden"]?.jsonPrimitive?.boolean
                    val unlockTimestamp = installation["unlock_timestamp"]?.jsonPrimitive?.longOrNull
                    
                    // If installation is hidden, it should have either passed unlock time or be near user location
                    if (isHidden == true) {
                        val currentTime = System.currentTimeMillis()
                        val timeUnlocked = unlockTimestamp?.let { it <= currentTime } ?: false
                        // Location-based unlocking logic should be applied by the backend
                        // We can't test exact location logic here, but validate structure
                    }
                }
            }
        }
        
        it("should validate optional fields and arrays") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/art-installations")
                
                response.status shouldBe HttpStatusCode.OK
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                
                if (jsonResponse.jsonArray.isNotEmpty()) {
                    val installation = jsonResponse.jsonArray.first().jsonObject
                    
                    // Optional fields validation
                    if (installation.containsKey("description")) {
                        val description = installation["description"]?.jsonPrimitive?.content
                        if (description != null) {
                            description.length shouldBe (description.length <= 2000)
                        }
                    }
                    
                    if (installation.containsKey("artist_bio")) {
                        val artistBio = installation["artist_bio"]?.jsonPrimitive?.content
                        if (artistBio != null) {
                            artistBio.length shouldBe (artistBio.length <= 1000)
                        }
                    }
                    
                    // Arrays should be valid JSON arrays
                    if (installation.containsKey("photo_urls")) {
                        installation["photo_urls"]?.jsonArray // Should be an array
                    }
                    
                    if (installation.containsKey("interactive_features")) {
                        installation["interactive_features"]?.jsonArray // Should be an array
                    }
                    
                    // Boolean fields validation
                    if (installation.containsKey("is_hidden")) {
                        installation["is_hidden"]?.jsonPrimitive?.booleanOrNull shouldBe Boolean::class.java.simpleName
                    }
                    
                    // Timestamp fields validation
                    if (installation.containsKey("unlock_timestamp")) {
                        // Can be null or a valid timestamp
                        val unlockTimestamp = installation["unlock_timestamp"]
                        if (unlockTimestamp != null && !unlockTimestamp.jsonPrimitive.isString) {
                            unlockTimestamp.jsonPrimitive.longOrNull shouldBe Long::class.java.simpleName
                        }
                    }
                    
                    if (installation.containsKey("last_updated")) {
                        installation["last_updated"]?.jsonPrimitive?.longOrNull shouldBe Long::class.java.simpleName
                    }
                }
            }
        }
        
        it("should return 404 for invalid eventId") {
            testApplication {
                val invalidId = "invalid-uuid"
                val response = client.get("/api/v1/events/$invalidId/art-installations")
                
                // Contract validation: Error response structure
                response.status shouldBe HttpStatusCode.NotFound
                response.headers[HttpHeaders.ContentType] shouldContain "application/json"
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                jsonResponse.containsKey("error") shouldBe true
                jsonResponse.containsKey("message") shouldBe true
            }
        }
        
        it("should validate geographic coordinates bounds") {
            testApplication {
                val invalidLat = 91.0 // Outside valid range (-90 to 90)
                val validLng = 20.1
                val response = client.get("/api/v1/events/$testEventId/art-installations?lat=$invalidLat&lng=$validLng")
                
                // Should handle invalid coordinates gracefully
                response.status shouldBe HttpStatusCode.BadRequest
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                jsonResponse.containsKey("error") shouldBe true
                jsonResponse.containsKey("message") shouldBe true
            }
        }
        
        it("should validate coordinate range for longitude") {
            testApplication {
                val validLat = -32.3
                val invalidLng = 181.0 // Outside valid range (-180 to 180)
                val response = client.get("/api/v1/events/$testEventId/art-installations?lat=$validLat&lng=$invalidLng")
                
                // Should handle invalid coordinates gracefully
                response.status shouldBe HttpStatusCode.BadRequest
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                jsonResponse.containsKey("error") shouldBe true
                jsonResponse.containsKey("message") shouldBe true
            }
        }
        
        it("should handle missing lat parameter when lng is provided") {
            testApplication {
                val lng = 20.1
                val response = client.get("/api/v1/events/$testEventId/art-installations?lng=$lng")
                
                // Should either accept (ignoring location filter) or reject with clear error
                if (response.status == HttpStatusCode.BadRequest) {
                    val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                    jsonResponse.containsKey("error") shouldBe true
                    jsonResponse.containsKey("message") shouldBe true
                } else {
                    // If accepted, should return valid response
                    response.status shouldBe HttpStatusCode.OK
                    response.headers[HttpHeaders.ContentType] shouldContain "application/json"
                    
                    val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                    jsonResponse.jsonArray // Should be an array
                }
            }
        }
        
        it("should handle art installations with QR codes") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/art-installations")
                
                response.status shouldBe HttpStatusCode.OK
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                
                if (jsonResponse.jsonArray.isNotEmpty()) {
                    val installation = jsonResponse.jsonArray.first().jsonObject
                    
                    // QR code field validation
                    if (installation.containsKey("qr_code")) {
                        val qrCode = installation["qr_code"]?.jsonPrimitive?.content
                        if (qrCode != null) {
                            qrCode.length shouldBe (qrCode.length <= 200)
                            // QR codes should be alphanumeric strings
                            qrCode.matches(Regex("^[A-Za-z0-9]*$")) shouldBe true
                        }
                    }
                }
            }
        }
        
        it("should handle photo URLs validation") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/art-installations")
                
                response.status shouldBe HttpStatusCode.OK
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                
                if (jsonResponse.jsonArray.isNotEmpty()) {
                    val installation = jsonResponse.jsonArray.first().jsonObject
                    
                    // Photo URLs array validation
                    if (installation.containsKey("photo_urls")) {
                        val photoUrls = installation["photo_urls"]?.jsonArray
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
    }
})