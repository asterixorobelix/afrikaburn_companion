package io.asterixorobelix.afrikaburn.api

import io.asterixorobelix.afrikaburn.domain.WeatherAlert
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

fun Route.weatherRoutes() {
    route("/api/v1/events/{eventId}/weather-alerts") {
        get {
            val eventId = call.parameters["eventId"] 
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing eventId"))
            
            // Validate UUID format
            try {
                UUID.fromString(eventId)
            } catch (e: IllegalArgumentException) {
                return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid eventId format"))
            }
            
            // Return mock weather alerts with typical AfrikaBurn conditions
            val weatherAlerts = listOf(
                WeatherAlert(
                    id = UUID.randomUUID().toString(),
                    alertType = "dust_storm",
                    severity = "high",
                    title = "Dust Storm Advisory",
                    description = "Strong winds expected this afternoon with reduced visibility. Secure loose items and wear goggles when outside. Dust masks recommended.",
                    startTime = LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.UTC).toString(),
                    endTime = LocalDateTime.now().plusHours(6).toInstant(ZoneOffset.UTC).toString(),
                    isActive = true,
                    lastUpdated = System.currentTimeMillis()
                ),
                WeatherAlert(
                    id = UUID.randomUUID().toString(),
                    alertType = "extreme_heat",
                    severity = "medium",
                    title = "Extreme Heat Warning",
                    description = "Temperatures expected to reach 42°C (108°F) during midday. Stay hydrated, seek shade during peak hours, and check on campmates regularly.",
                    startTime = LocalDateTime.now().plusDays(1).withHour(10).toInstant(ZoneOffset.UTC).toString(),
                    endTime = LocalDateTime.now().plusDays(1).withHour(17).toInstant(ZoneOffset.UTC).toString(),
                    isActive = true,
                    lastUpdated = System.currentTimeMillis()
                ),
                WeatherAlert(
                    id = UUID.randomUUID().toString(),
                    alertType = "high_wind",
                    severity = "low",
                    title = "Wind Advisory",
                    description = "Moderate winds of 25-35 km/h expected overnight. Ensure tents and structures are properly secured.",
                    startTime = LocalDateTime.now().withHour(22).toInstant(ZoneOffset.UTC).toString(),
                    endTime = LocalDateTime.now().plusDays(1).withHour(6).toInstant(ZoneOffset.UTC).toString(),
                    isActive = true,
                    lastUpdated = System.currentTimeMillis()
                )
            )
            
            call.respond(HttpStatusCode.OK, weatherAlerts)
        }
    }
}