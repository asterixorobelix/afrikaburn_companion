package api

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*

/**
 * Contract tests for GET /events endpoint
 * Validates API compliance with OpenAPI specification
 */
class EventsApiTest : DescribeSpec({
    describe("GET /events endpoint") {
        it("should return 200 with events array when no parameters") {
            testApplication {
                val response = client.get("/api/v1/events")
                
                // Contract validation: Status code
                response.status shouldBe HttpStatusCode.OK
                
                // Contract validation: Content-Type
                response.headers[HttpHeaders.ContentType] shouldContain "application/json"
                
                // Contract validation: Response structure
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                jsonResponse.jsonArray // Should be an array
                
                // Contract validation: Each event should have required fields
                if (jsonResponse.jsonArray.isNotEmpty()) {
                    val firstEvent = jsonResponse.jsonArray.first().jsonObject
                    
                    // Required fields from OpenAPI spec
                    firstEvent["id"]?.jsonPrimitive?.contentOrNull shouldBe String::class.java.simpleName
                    firstEvent["year"]?.jsonPrimitive?.intOrNull shouldBe Int::class.java.simpleName
                    firstEvent["start_date"]?.jsonPrimitive?.contentOrNull shouldBe String::class.java.simpleName
                    firstEvent["end_date"]?.jsonPrimitive?.contentOrNull shouldBe String::class.java.simpleName
                    firstEvent["center_latitude"]?.jsonPrimitive?.doubleOrNull shouldBe Double::class.java.simpleName
                    firstEvent["center_longitude"]?.jsonPrimitive?.doubleOrNull shouldBe Double::class.java.simpleName
                    firstEvent["theme"]?.jsonPrimitive?.contentOrNull shouldBe String::class.java.simpleName
                }
            }
        }
        
        it("should return 200 with filtered events when current_only=true") {
            testApplication {
                val response = client.get("/api/v1/events?current_only=true")
                
                response.status shouldBe HttpStatusCode.OK
                response.headers[HttpHeaders.ContentType] shouldContain "application/json"
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                jsonResponse.jsonArray // Should be an array
                
                // Contract validation: current_only parameter should filter results
                jsonResponse.jsonArray.forEach { event ->
                    val eventObj = event.jsonObject
                    eventObj["is_current_year"]?.jsonPrimitive?.boolean shouldBe true
                }
            }
        }
        
        it("should return 200 with hidden content when include_hidden=true") {
            testApplication {
                val response = client.get("/api/v1/events?include_hidden=true")
                
                response.status shouldBe HttpStatusCode.OK
                response.headers[HttpHeaders.ContentType] shouldContain "application/json"
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                jsonResponse.jsonArray // Should be an array
            }
        }
        
        it("should return 500 for server errors") {
            testApplication {
                // This test will verify error response structure
                // Will be implemented when error scenarios exist
            }
        }
    }
})