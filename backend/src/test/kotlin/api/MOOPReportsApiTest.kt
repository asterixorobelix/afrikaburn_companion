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
 * Contract tests for POST /events/{eventId}/moop-reports endpoint
 * Validates API compliance with OpenAPI specification
 * 
 * MOOP (Matter Out of Place) reports are critical for Leave No Trace principles.
 * These tests MUST FAIL initially as implementation doesn't exist yet.
 * Following TDD approach - tests define the contract before implementation.
 */
class MOOPReportsApiTest : DescribeSpec({
    describe("POST /events/{eventId}/moop-reports endpoint") {
        val testEventId = UUID.randomUUID().toString()
        
        it("should return 201 with MOOP report for valid request") {
            testApplication {
                val requestBody = """
                {
                    "latitude": -32.3,
                    "longitude": 20.1,
                    "description": "Plastic bottles left near art installation",
                    "severity": "medium"
                }
                """.trimIndent()
                
                val response = client.post("/api/v1/events/$testEventId/moop-reports") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(requestBody)
                }
                
                // Contract validation: Status code
                response.status shouldBe HttpStatusCode.Created
                
                // Contract validation: Content-Type
                response.headers[HttpHeaders.ContentType] shouldContain "application/json"
                
                // Contract validation: Response structure
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                
                // Required fields from OpenAPI spec
                jsonResponse.containsKey("id") shouldBe true
                jsonResponse.containsKey("latitude") shouldBe true
                jsonResponse.containsKey("longitude") shouldBe true
                jsonResponse.containsKey("description") shouldBe true
                jsonResponse.containsKey("severity") shouldBe true
                
                // Validate data types
                val reportId = jsonResponse["id"]?.jsonPrimitive?.content
                if (reportId != null) {
                    UUID.fromString(reportId) // Should be valid UUID
                }
                
                jsonResponse["latitude"]?.jsonPrimitive?.doubleOrNull shouldBe Double::class.java.simpleName
                jsonResponse["longitude"]?.jsonPrimitive?.doubleOrNull shouldBe Double::class.java.simpleName
                
                // Validate severity enum
                val severity = jsonResponse["severity"]?.jsonPrimitive?.content
                severity shouldBe "medium"
                (severity in listOf("low", "medium", "high")) shouldBe true
                
                // Validate optional fields
                if (jsonResponse.containsKey("status")) {
                    val status = jsonResponse["status"]?.jsonPrimitive?.content
                    (status in listOf("reported", "in_progress", "resolved")) shouldBe true
                }
                
                if (jsonResponse.containsKey("reported_timestamp")) {
                    jsonResponse["reported_timestamp"]?.jsonPrimitive?.longOrNull shouldBe Long::class.java.simpleName
                }
            }
        }
        
        it("should handle MOOP report with photo attachment") {
            testApplication {
                val base64Photo = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg=="
                val requestBody = """
                {
                    "latitude": -32.2,
                    "longitude": 20.0,
                    "description": "Broken camping chair abandoned at theme camp",
                    "severity": "high",
                    "photo_base64": "$base64Photo"
                }
                """.trimIndent()
                
                val response = client.post("/api/v1/events/$testEventId/moop-reports") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(requestBody)
                }
                
                response.status shouldBe HttpStatusCode.Created
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                
                // Photo should be processed and return URL
                if (jsonResponse.containsKey("photo_url")) {
                    val photoUrl = jsonResponse["photo_url"]?.jsonPrimitive?.content
                    if (photoUrl != null) {
                        (photoUrl.startsWith("http://") || photoUrl.startsWith("https://")) shouldBe true
                    }
                }
                
                // Severity should be preserved
                jsonResponse["severity"]?.jsonPrimitive?.content shouldBe "high"
            }
        }
        
        it("should return 400 for missing required fields") {
            testApplication {
                val requestBody = """
                {
                    "description": "Missing coordinates"
                }
                """.trimIndent()
                
                val response = client.post("/api/v1/events/$testEventId/moop-reports") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(requestBody)
                }
                
                // Contract validation: Error response
                response.status shouldBe HttpStatusCode.BadRequest
                response.headers[HttpHeaders.ContentType] shouldContain "application/json"
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                jsonResponse.containsKey("error") shouldBe true
                jsonResponse.containsKey("message") shouldBe true
            }
        }
        
        it("should return 400 for invalid coordinates") {
            testApplication {
                val requestBody = """
                {
                    "latitude": 91.0,
                    "longitude": 181.0,
                    "description": "Invalid coordinates test",
                    "severity": "low"
                }
                """.trimIndent()
                
                val response = client.post("/api/v1/events/$testEventId/moop-reports") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(requestBody)
                }
                
                response.status shouldBe HttpStatusCode.BadRequest
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                jsonResponse.containsKey("error") shouldBe true
                
                // Error message should indicate coordinate validation issue
                val message = jsonResponse["message"]?.jsonPrimitive?.content
                if (message != null) {
                    (message.contains("latitude", ignoreCase = true) || 
                     message.contains("longitude", ignoreCase = true) ||
                     message.contains("coordinate", ignoreCase = true)) shouldBe true
                }
            }
        }
        
        it("should return 400 for invalid severity level") {
            testApplication {
                val requestBody = """
                {
                    "latitude": -32.3,
                    "longitude": 20.1,
                    "description": "Test with invalid severity",
                    "severity": "extreme"
                }
                """.trimIndent()
                
                val response = client.post("/api/v1/events/$testEventId/moop-reports") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(requestBody)
                }
                
                response.status shouldBe HttpStatusCode.BadRequest
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                jsonResponse.containsKey("error") shouldBe true
                
                // Error message should indicate severity validation issue
                val message = jsonResponse["message"]?.jsonPrimitive?.content
                if (message != null) {
                    message.contains("severity", ignoreCase = true) shouldBe true
                }
            }
        }
        
        it("should return 400 for description too long") {
            testApplication {
                val longDescription = "A".repeat(1001) // Exceeds 1000 character limit
                val requestBody = """
                {
                    "latitude": -32.3,
                    "longitude": 20.1,
                    "description": "$longDescription",
                    "severity": "low"
                }
                """.trimIndent()
                
                val response = client.post("/api/v1/events/$testEventId/moop-reports") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(requestBody)
                }
                
                response.status shouldBe HttpStatusCode.BadRequest
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                jsonResponse.containsKey("error") shouldBe true
                
                // Error message should indicate description length issue
                val message = jsonResponse["message"]?.jsonPrimitive?.content
                if (message != null) {
                    (message.contains("description", ignoreCase = true) || 
                     message.contains("length", ignoreCase = true) ||
                     message.contains("1000", ignoreCase = true)) shouldBe true
                }
            }
        }
        
        it("should return 400 for empty description") {
            testApplication {
                val requestBody = """
                {
                    "latitude": -32.3,
                    "longitude": 20.1,
                    "description": "",
                    "severity": "medium"
                }
                """.trimIndent()
                
                val response = client.post("/api/v1/events/$testEventId/moop-reports") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(requestBody)
                }
                
                response.status shouldBe HttpStatusCode.BadRequest
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                jsonResponse.containsKey("error") shouldBe true
            }
        }
        
        it("should return 404 for invalid eventId") {
            testApplication {
                val invalidId = "invalid-uuid"
                val requestBody = """
                {
                    "latitude": -32.3,
                    "longitude": 20.1,
                    "description": "Valid MOOP report for invalid event",
                    "severity": "low"
                }
                """.trimIndent()
                
                val response = client.post("/api/v1/events/$invalidId/moop-reports") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(requestBody)
                }
                
                response.status shouldBe HttpStatusCode.NotFound
                response.headers[HttpHeaders.ContentType] shouldContain "application/json"
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                jsonResponse.containsKey("error") shouldBe true
                jsonResponse.containsKey("message") shouldBe true
            }
        }
        
        it("should return 400 for malformed JSON") {
            testApplication {
                val malformedJson = """
                {
                    "latitude": -32.3,
                    "longitude": 20.1,
                    "description": "Missing closing brace",
                    "severity": "medium"
                """.trimIndent()
                
                val response = client.post("/api/v1/events/$testEventId/moop-reports") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(malformedJson)
                }
                
                response.status shouldBe HttpStatusCode.BadRequest
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                jsonResponse.containsKey("error") shouldBe true
            }
        }
        
        it("should handle coordinates within Tankwa Karoo bounds") {
            testApplication {
                // Tankwa Karoo approximate bounds from Event model
                val requestBody = """
                {
                    "latitude": -32.25,
                    "longitude": 20.05,
                    "description": "MOOP within AfrikaBurn site boundaries",
                    "severity": "medium"
                }
                """.trimIndent()
                
                val response = client.post("/api/v1/events/$testEventId/moop-reports") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(requestBody)
                }
                
                response.status shouldBe HttpStatusCode.Created
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                
                // Coordinates should be preserved exactly
                jsonResponse["latitude"]?.jsonPrimitive?.double shouldBe -32.25
                jsonResponse["longitude"]?.jsonPrimitive?.double shouldBe 20.05
            }
        }
        
        it("should validate content-type header requirement") {
            testApplication {
                val requestBody = """
                {
                    "latitude": -32.3,
                    "longitude": 20.1,
                    "description": "Request without proper content-type",
                    "severity": "low"
                }
                """.trimIndent()
                
                val response = client.post("/api/v1/events/$testEventId/moop-reports") {
                    // Omitting Content-Type header
                    setBody(requestBody)
                }
                
                // Should require application/json content-type
                response.status shouldBe HttpStatusCode.UnsupportedMediaType
            }
        }
        
        it("should handle all severity levels correctly") {
            testApplication {
                val severityLevels = listOf("low", "medium", "high")
                
                severityLevels.forEach { severity ->
                    val requestBody = """
                    {
                        "latitude": -32.3,
                        "longitude": 20.1,
                        "description": "Testing $severity severity level",
                        "severity": "$severity"
                    }
                    """.trimIndent()
                    
                    val response = client.post("/api/v1/events/$testEventId/moop-reports") {
                        header(HttpHeaders.ContentType, ContentType.Application.Json)
                        setBody(requestBody)
                    }
                    
                    response.status shouldBe HttpStatusCode.Created
                    
                    val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                    jsonResponse["severity"]?.jsonPrimitive?.content shouldBe severity
                }
            }
        }
        
        it("should set default status to reported") {
            testApplication {
                val requestBody = """
                {
                    "latitude": -32.3,
                    "longitude": 20.1,
                    "description": "Testing default status",
                    "severity": "low"
                }
                """.trimIndent()
                
                val response = client.post("/api/v1/events/$testEventId/moop-reports") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(requestBody)
                }
                
                response.status shouldBe HttpStatusCode.Created
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                
                // Default status should be "reported" according to spec
                if (jsonResponse.containsKey("status")) {
                    jsonResponse["status"]?.jsonPrimitive?.content shouldBe "reported"
                }
                
                // Timestamp should be set
                if (jsonResponse.containsKey("reported_timestamp")) {
                    val timestamp = jsonResponse["reported_timestamp"]?.jsonPrimitive?.long
                    if (timestamp != null) {
                        (timestamp > 0) shouldBe true
                    }
                }
            }
        }
    }
})