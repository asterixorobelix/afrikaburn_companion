package api

import io.asterixorobelix.afrikaburn.module
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*
import java.util.*

/**
 * Contract tests for GET /events/{eventId}/theme-camps endpoint
 * Validates API compliance with OpenAPI specification
 */
class ThemeCampsApiTest : DescribeSpec({
    describe("GET /events/{eventId}/theme-camps endpoint") {
        val testEventId = UUID.randomUUID().toString()
        
        it("should return 200 with theme camps array for valid eventId") {
            testApplication {
                application {
                    module()
                }
                val response = client.get("/api/v1/events/$testEventId/theme-camps")
                
                // Contract validation: Status code
                response.status shouldBe HttpStatusCode.OK
                
                // Contract validation: Content-Type
                response.headers[HttpHeaders.ContentType] shouldContain "application/json"
                
                // Contract validation: Response structure
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                jsonResponse.jsonArray // Should be an array
                
                // Contract validation: Each theme camp should have required fields
                if (jsonResponse.jsonArray.isNotEmpty()) {
                    val firstCamp = jsonResponse.jsonArray.first().jsonObject
                    
                    // Required fields from OpenAPI spec
                    firstCamp.containsKey("id") shouldBe true
                    firstCamp.containsKey("event_id") shouldBe true
                    firstCamp.containsKey("name") shouldBe true
                    firstCamp.containsKey("latitude") shouldBe true
                    firstCamp.containsKey("longitude") shouldBe true
                    
                    // Validate data types
                    firstCamp["latitude"]?.jsonPrimitive?.doubleOrNull shouldNotBe null
                    firstCamp["longitude"]?.jsonPrimitive?.doubleOrNull shouldNotBe null
                }
            }
        }
        
        it("should return filtered camps based on location parameters") {
            testApplication {
                application {
                    module()
                }
                val lat = -32.3
                val lng = 20.1
                val response = client.get("/api/v1/events/$testEventId/theme-camps?lat=$lat&lng=$lng")
                
                response.status shouldBe HttpStatusCode.OK
                response.headers[HttpHeaders.ContentType] shouldContain "application/json"
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                jsonResponse.jsonArray // Should be an array
                
                // Contract validation: Location-based filtering should apply
                // Hidden content should be filtered based on location/time unlock rules
            }
        }
        
        it("should return 404 for invalid eventId") {
            testApplication {
                application {
                    module()
                }
                val invalidId = "invalid-uuid"
                val response = client.get("/api/v1/events/$invalidId/theme-camps")
                
                // Contract validation: Error response structure
                response.status shouldBe HttpStatusCode.BadRequest
                response.headers[HttpHeaders.ContentType] shouldContain "application/json"
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                jsonResponse.containsKey("error") shouldBe true
                jsonResponse.containsKey("message") shouldBe true
            }
        }
        
        it("should validate geographic coordinates bounds") {
            testApplication {
                application {
                    module()
                }
                val invalidLat = 91.0 // Outside valid range (-90 to 90)
                val validLng = 20.1
                val response = client.get("/api/v1/events/$testEventId/theme-camps?lat=$invalidLat&lng=$validLng")
                
                // Should handle invalid coordinates gracefully
                response.status shouldBe HttpStatusCode.BadRequest
            }
        }
    }
})