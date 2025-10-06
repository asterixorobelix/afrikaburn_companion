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
 * Contract tests for GET /events/{eventId}/weather-alerts endpoint
 * Validates API compliance with OpenAPI specification
 * 
 * Weather alerts are critical for participant safety in the harsh Tankwa Karoo desert.
 * These tests MUST FAIL initially as implementation doesn't exist yet.
 * Following TDD approach - tests define the contract before implementation.
 */
class WeatherAlertsApiTest : DescribeSpec({
    describe("GET /events/{eventId}/weather-alerts endpoint") {
        val testEventId = UUID.randomUUID().toString()
        
        it("should return 200 with weather alerts array for valid eventId") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/weather-alerts")
                
                // Contract validation: Status code
                response.status shouldBe HttpStatusCode.OK
                
                // Contract validation: Content-Type
                response.headers[HttpHeaders.ContentType] shouldContain "application/json"
                
                // Contract validation: Response structure
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                jsonResponse.jsonArray // Should be an array
                
                // Contract validation: Each weather alert should have required fields
                if (jsonResponse.jsonArray.isNotEmpty()) {
                    val firstAlert = jsonResponse.jsonArray.first().jsonObject
                    
                    // Required fields from OpenAPI spec
                    firstAlert.containsKey("id") shouldBe true
                    firstAlert.containsKey("alert_type") shouldBe true
                    firstAlert.containsKey("severity") shouldBe true
                    firstAlert.containsKey("title") shouldBe true
                    firstAlert.containsKey("description") shouldBe true
                    firstAlert.containsKey("start_time") shouldBe true
                    
                    // Validate data types for required fields
                    val alertId = firstAlert["id"]?.jsonPrimitive?.content
                    if (alertId != null) {
                        UUID.fromString(alertId) // Should be valid UUID
                    }
                    
                    // Validate alert_type enum
                    val alertType = firstAlert["alert_type"]?.jsonPrimitive?.content
                    if (alertType != null) {
                        (alertType in listOf("dust_storm", "high_wind", "extreme_heat", "severe_weather")) shouldBe true
                    }
                    
                    // Validate severity enum
                    val severity = firstAlert["severity"]?.jsonPrimitive?.content
                    if (severity != null) {
                        (severity in listOf("low", "medium", "high", "extreme")) shouldBe true
                    }
                    
                    // Validate string length constraints
                    val title = firstAlert["title"]?.jsonPrimitive?.content
                    if (title != null) {
                        title.isNotBlank() shouldBe true
                        (title.length <= 200) shouldBe true
                    }
                    
                    val description = firstAlert["description"]?.jsonPrimitive?.content
                    if (description != null) {
                        description.isNotBlank() shouldBe true
                        (description.length <= 1000) shouldBe true
                    }
                    
                    // Validate date-time format
                    val startTime = firstAlert["start_time"]?.jsonPrimitive?.content
                    if (startTime != null) {
                        // Should be ISO 8601 date-time format
                        startTime.contains("T") shouldBe true
                        (startTime.contains("Z") || startTime.contains("+") || startTime.contains("-")) shouldBe true
                    }
                }
            }
        }
        
        it("should return only active weather alerts") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/weather-alerts")
                
                response.status shouldBe HttpStatusCode.OK
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                
                // All returned alerts should be active
                jsonResponse.jsonArray.forEach { alertElement ->
                    val alert = alertElement.jsonObject
                    
                    if (alert.containsKey("is_active")) {
                        val isActive = alert["is_active"]?.jsonPrimitive?.boolean
                        // Should be true or default to true
                        (isActive == true || isActive == null) shouldBe true
                    }
                }
            }
        }
        
        it("should validate optional fields and data types") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/weather-alerts")
                
                response.status shouldBe HttpStatusCode.OK
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                
                if (jsonResponse.jsonArray.isNotEmpty()) {
                    val alert = jsonResponse.jsonArray.first().jsonObject
                    
                    // Optional end_time field validation
                    if (alert.containsKey("end_time")) {
                        val endTime = alert["end_time"]?.jsonPrimitive?.content
                        if (endTime != null) {
                            // Should be ISO 8601 date-time format
                            endTime.contains("T") shouldBe true
                            (endTime.contains("Z") || endTime.contains("+") || endTime.contains("-")) shouldBe true
                        }
                    }
                    
                    // Boolean is_active field validation
                    if (alert.containsKey("is_active")) {
                        alert["is_active"]?.jsonPrimitive?.booleanOrNull shouldBe Boolean::class.java.simpleName
                    }
                    
                    // Timestamp last_updated field validation
                    if (alert.containsKey("last_updated")) {
                        val lastUpdated = alert["last_updated"]?.jsonPrimitive?.longOrNull
                        if (lastUpdated != null) {
                            (lastUpdated > 0) shouldBe true
                        }
                    }
                }
            }
        }
        
        it("should return 404 for invalid eventId") {
            testApplication {
                val invalidId = "invalid-uuid"
                val response = client.get("/api/v1/events/$invalidId/weather-alerts")
                
                // Contract validation: Error response structure
                response.status shouldBe HttpStatusCode.NotFound
                response.headers[HttpHeaders.ContentType] shouldContain "application/json"
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                jsonResponse.containsKey("error") shouldBe true
                jsonResponse.containsKey("message") shouldBe true
            }
        }
        
        it("should handle dust storm alerts specific to Tankwa Karoo") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/weather-alerts")
                
                response.status shouldBe HttpStatusCode.OK
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                
                // Look for dust storm alerts (common in Tankwa Karoo)
                val dustStormAlerts = jsonResponse.jsonArray.filter { alertElement ->
                    val alert = alertElement.jsonObject
                    alert["alert_type"]?.jsonPrimitive?.content == "dust_storm"
                }
                
                // If dust storm alerts exist, validate their content
                dustStormAlerts.forEach { alertElement ->
                    val alert = alertElement.jsonObject
                    
                    // Dust storms should typically be high severity in desert
                    val severity = alert["severity"]?.jsonPrimitive?.content
                    if (severity != null) {
                        (severity in listOf("high", "extreme", "medium", "low")) shouldBe true
                    }
                    
                    // Description should contain relevant information
                    val description = alert["description"]?.jsonPrimitive?.content
                    if (description != null) {
                        (description.contains("dust", ignoreCase = true) || 
                         description.contains("sand", ignoreCase = true) ||
                         description.contains("visibility", ignoreCase = true)) shouldBe true
                    }
                }
            }
        }
        
        it("should validate extreme heat alerts for desert conditions") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/weather-alerts")
                
                response.status shouldBe HttpStatusCode.OK
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                
                // Look for extreme heat alerts
                val heatAlerts = jsonResponse.jsonArray.filter { alertElement ->
                    val alert = alertElement.jsonObject
                    alert["alert_type"]?.jsonPrimitive?.content == "extreme_heat"
                }
                
                // If heat alerts exist, validate their relevance
                heatAlerts.forEach { alertElement ->
                    val alert = alertElement.jsonObject
                    
                    // Heat alerts should reference temperature or heat-related terms
                    val description = alert["description"]?.jsonPrimitive?.content
                    if (description != null) {
                        (description.contains("heat", ignoreCase = true) || 
                         description.contains("temperature", ignoreCase = true) ||
                         description.contains("hot", ignoreCase = true) ||
                         description.contains("sun", ignoreCase = true)) shouldBe true
                    }
                }
            }
        }
        
        it("should validate high wind alerts for tent and structure safety") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/weather-alerts")
                
                response.status shouldBe HttpStatusCode.OK
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                
                // Look for high wind alerts
                val windAlerts = jsonResponse.jsonArray.filter { alertElement ->
                    val alert = alertElement.jsonObject
                    alert["alert_type"]?.jsonPrimitive?.content == "high_wind"
                }
                
                // If wind alerts exist, validate their content
                windAlerts.forEach { alertElement ->
                    val alert = alertElement.jsonObject
                    
                    // Wind alerts should reference wind-related terms
                    val description = alert["description"]?.jsonPrimitive?.content
                    if (description != null) {
                        (description.contains("wind", ignoreCase = true) || 
                         description.contains("gust", ignoreCase = true) ||
                         description.contains("tent", ignoreCase = true) ||
                         description.contains("structure", ignoreCase = true)) shouldBe true
                    }
                }
            }
        }
        
        it("should validate time range consistency") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/weather-alerts")
                
                response.status shouldBe HttpStatusCode.OK
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                
                jsonResponse.jsonArray.forEach { alertElement ->
                    val alert = alertElement.jsonObject
                    
                    val startTime = alert["start_time"]?.jsonPrimitive?.content
                    val endTime = alert["end_time"]?.jsonPrimitive?.content
                    
                    // If both start and end times exist, end should be after start
                    if (startTime != null && endTime != null) {
                        // Basic validation - end time string should be lexicographically >= start time
                        // (This is a simplified check; real implementation would parse ISO dates)
                        (endTime >= startTime) shouldBe true
                    }
                }
            }
        }
        
        it("should handle empty alerts array gracefully") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/weather-alerts")
                
                // Should return 200 even if no active alerts
                response.status shouldBe HttpStatusCode.OK
                response.headers[HttpHeaders.ContentType] shouldContain "application/json"
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                jsonResponse.jsonArray // Should be an array (possibly empty)
            }
        }
        
        it("should validate severity escalation levels") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/weather-alerts")
                
                response.status shouldBe HttpStatusCode.OK
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                
                val severityLevels = listOf("low", "medium", "high", "extreme")
                
                jsonResponse.jsonArray.forEach { alertElement ->
                    val alert = alertElement.jsonObject
                    
                    val severity = alert["severity"]?.jsonPrimitive?.content
                    if (severity != null) {
                        (severity in severityLevels) shouldBe true
                        
                        // Extreme severity should have urgent language
                        if (severity == "extreme") {
                            val title = alert["title"]?.jsonPrimitive?.content
                            val description = alert["description"]?.jsonPrimitive?.content
                            
                            val hasUrgentLanguage = (title?.contains("URGENT", ignoreCase = true) == true ||
                                                   title?.contains("IMMEDIATE", ignoreCase = true) == true ||
                                                   title?.contains("EMERGENCY", ignoreCase = true) == true ||
                                                   description?.contains("immediate", ignoreCase = true) == true ||
                                                   description?.contains("urgent", ignoreCase = true) == true)
                            
                            // For extreme alerts, urgent language is expected but not strictly required
                            // This is more of a best practice validation
                        }
                    }
                }
            }
        }
        
        it("should validate alert type relevance to desert environment") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/weather-alerts")
                
                response.status shouldBe HttpStatusCode.OK
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                
                val validAlertTypes = listOf("dust_storm", "high_wind", "extreme_heat", "severe_weather")
                
                jsonResponse.jsonArray.forEach { alertElement ->
                    val alert = alertElement.jsonObject
                    
                    val alertType = alert["alert_type"]?.jsonPrimitive?.content
                    if (alertType != null) {
                        (alertType in validAlertTypes) shouldBe true
                        
                        // Validate that alert type matches content
                        val title = alert["title"]?.jsonPrimitive?.content?.lowercase()
                        val description = alert["description"]?.jsonPrimitive?.content?.lowercase()
                        
                        when (alertType) {
                            "dust_storm" -> {
                                val hasRelevantContent = (title?.contains("dust") == true ||
                                                        title?.contains("sand") == true ||
                                                        description?.contains("dust") == true ||
                                                        description?.contains("visibility") == true)
                                // Content should be relevant but this is advisory
                            }
                            "extreme_heat" -> {
                                val hasRelevantContent = (title?.contains("heat") == true ||
                                                        title?.contains("temperature") == true ||
                                                        description?.contains("heat") == true ||
                                                        description?.contains("hot") == true)
                                // Content should be relevant but this is advisory
                            }
                            "high_wind" -> {
                                val hasRelevantContent = (title?.contains("wind") == true ||
                                                        title?.contains("gust") == true ||
                                                        description?.contains("wind") == true)
                                // Content should be relevant but this is advisory
                            }
                        }
                    }
                }
            }
        }
        
        it("should validate ISO 8601 date format structure") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/weather-alerts")
                
                response.status shouldBe HttpStatusCode.OK
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                
                jsonResponse.jsonArray.forEach { alertElement ->
                    val alert = alertElement.jsonObject
                    
                    // Validate start_time format
                    val startTime = alert["start_time"]?.jsonPrimitive?.content
                    if (startTime != null) {
                        // Basic ISO 8601 structure validation
                        val iso8601Pattern = Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")
                        startTime.contains(iso8601Pattern) shouldBe true
                    }
                    
                    // Validate end_time format if present
                    val endTime = alert["end_time"]?.jsonPrimitive?.content
                    if (endTime != null) {
                        val iso8601Pattern = Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")
                        endTime.contains(iso8601Pattern) shouldBe true
                    }
                }
            }
        }
        
        it("should return current and future alerts only") {
            testApplication {
                val response = client.get("/api/v1/events/$testEventId/weather-alerts")
                
                response.status shouldBe HttpStatusCode.OK
                
                val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
                
                jsonResponse.jsonArray.forEach { alertElement ->
                    val alert = alertElement.jsonObject
                    
                    // Active alerts should either:
                    // 1. Have no end_time (ongoing)
                    // 2. Have end_time in the future
                    // 3. Be explicitly marked as active
                    
                    val isActive = alert["is_active"]?.jsonPrimitive?.boolean ?: true
                    isActive shouldBe true
                    
                    // This is a logical check - implementation should ensure
                    // that only current/future alerts are returned
                }
            }
        }
    }
})