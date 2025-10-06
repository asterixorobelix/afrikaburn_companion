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
 * Contract tests for POST /sync/full endpoint
 * Validates API compliance with OpenAPI specification for smart sync
 */
class SyncApiTest : DescribeSpec({
    describe("POST /sync/full endpoint") {
        val testDeviceId = UUID.randomUUID().toString()
        val testEventId = UUID.randomUUID().toString()
        
        it("should return 200 with sync response for valid request") {
            testApplication {
                val requestBody = buildJsonObject {
                    put("device_id", testDeviceId)
                    put("event_id", testEventId)
                    put("max_storage_bytes", 2000000000L) // 2GB
                    put("priority_packages", buildJsonArray {
                        add("safety")
                        add("maps")
                        add("static")
                    })
                    put("last_sync_timestamp", 0L)
                }
                
                val response = client.post("/api/v1/sync/full") {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody.toString())
                }
                
                // Contract validation: Status code
                response.status shouldBe HttpStatusCode.OK
                
                // Contract validation: Content-Type
                response.headers[HttpHeaders.ContentType] shouldContain "application/json"
                
                // Contract validation: Response structure
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                
                // Required fields from OpenAPI spec
                jsonResponse.containsKey("sync_id") shouldBe true
                jsonResponse.containsKey("total_size_bytes") shouldBe true
                jsonResponse.containsKey("content_packages") shouldBe true
                
                // Validate sync_id is UUID format
                val syncId = jsonResponse["sync_id"]?.jsonPrimitive?.content
                UUID.fromString(syncId) // Should not throw exception
                
                // Validate total_size_bytes is within 2GB limit
                val totalSize = jsonResponse["total_size_bytes"]?.jsonPrimitive?.long
                (totalSize!! <= 2000000000L) shouldBe true
                
                // Validate content_packages structure
                val contentPackages = jsonResponse["content_packages"]?.jsonArray
                contentPackages?.forEach { packageElement ->
                    val packageObj = packageElement.jsonObject
                    packageObj.containsKey("id") shouldBe true
                    packageObj.containsKey("name") shouldBe true
                    packageObj.containsKey("priority") shouldBe true
                    packageObj.containsKey("size_bytes") shouldBe true
                    packageObj.containsKey("version") shouldBe true
                }
            }
        }
        
        it("should return 413 when requested content exceeds 2GB limit") {
            testApplication {
                val requestBody = buildJsonObject {
                    put("device_id", testDeviceId)
                    put("event_id", testEventId)
                    put("max_storage_bytes", 5000000000L) // 5GB - exceeds limit
                }
                
                val response = client.post("/api/v1/sync/full") {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody.toString())
                }
                
                // Contract validation: Status code for payload too large
                response.status shouldBe HttpStatusCode.PayloadTooLarge
                
                // Contract validation: Error response structure
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                jsonResponse.containsKey("error") shouldBe true
                jsonResponse.containsKey("message") shouldBe true
                
                val errorMessage = jsonResponse["message"]?.jsonPrimitive?.content
                errorMessage shouldContain "2GB"
            }
        }
        
        it("should return 400 for missing required fields") {
            testApplication {
                val requestBody = buildJsonObject {
                    // Missing device_id and event_id
                    put("max_storage_bytes", 2000000000L)
                }
                
                val response = client.post("/api/v1/sync/full") {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody.toString())
                }
                
                response.status shouldBe HttpStatusCode.BadRequest
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                jsonResponse.containsKey("error") shouldBe true
                jsonResponse.containsKey("message") shouldBe true
            }
        }
        
        it("should validate device_id and event_id are valid UUIDs") {
            testApplication {
                val requestBody = buildJsonObject {
                    put("device_id", "invalid-uuid")
                    put("event_id", "also-invalid")
                }
                
                val response = client.post("/api/v1/sync/full") {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody.toString())
                }
                
                response.status shouldBe HttpStatusCode.BadRequest
            }
        }
        
        it("should handle priority-based content selection") {
            testApplication {
                val requestBody = buildJsonObject {
                    put("device_id", testDeviceId)
                    put("event_id", testEventId)
                    put("max_storage_bytes", 500000000L) // 500MB - limited
                    put("priority_packages", buildJsonArray {
                        add("safety")   // Priority 1 (highest)
                        add("maps")     // Priority 2
                    })
                }
                
                val response = client.post("/api/v1/sync/full") {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody.toString())
                }
                
                response.status shouldBe HttpStatusCode.OK
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                val contentPackages = jsonResponse["content_packages"]?.jsonArray
                
                // Validate priority ordering (safety content should be included first)
                contentPackages?.let { packages ->
                    if (packages.size > 1) {
                        val priorities = packages.map { 
                            it.jsonObject["priority"]?.jsonPrimitive?.int ?: Int.MAX_VALUE
                        }
                        // Should be in ascending priority order (1 = highest priority)
                        priorities.zipWithNext { a, b -> a <= b }.all { it } shouldBe true
                    }
                }
            }
        }
    }
})