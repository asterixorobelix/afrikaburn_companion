package io.asterixorobelix.afrikaburn.api

import io.asterixorobelix.afrikaburn.domain.ErrorResponse
import io.asterixorobelix.afrikaburn.domain.Event
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Events API endpoints implementation following the OpenAPI specification.
 */
fun Route.eventsApi() {
    route("/api/v1/events") {
        get {
            try {
                // Extract query parameters
                val currentOnly = call.request.queryParameters["current_only"]?.toBoolean() ?: false
                val includeHidden = call.request.queryParameters["include_hidden"]?.toBoolean() ?: false

                // Get mock events
                val allEvents = getMockEvents()

                // Filter events based on parameters
                var filteredEvents = if (currentOnly) {
                    allEvents.filter { it.isCurrentYear }
                } else {
                    allEvents
                }

                // For includeHidden, we would normally check location verification
                // For now, we'll just return all events if includeHidden is false
                // (in real implementation, hidden events would be filtered out without location verification)
                
                call.respond(HttpStatusCode.OK, filteredEvents)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        error = "internal_server_error",
                        message = "An unexpected error occurred while fetching events"
                    )
                )
            }
        }
    }
}

/**
 * Returns mock event data for development.
 * In production, this would query the database.
 */
private fun getMockEvents(): List<Event> {
    val currentYear = LocalDate.now().year
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    return listOf(
        // Current year event
        Event(
            id = UUID.randomUUID().toString(),
            year = currentYear,
            startDate = LocalDate.of(currentYear, 4, 24).format(formatter),
            endDate = LocalDate.of(currentYear, 5, 1).format(formatter),
            centerLatitude = -32.392204,
            centerLongitude = 19.415494,
            radiusKm = 5.0,
            theme = "The Multiverse",
            isCurrentYear = true,
            lastUpdated = System.currentTimeMillis()
        ),
        // Previous year event
        Event(
            id = UUID.randomUUID().toString(),
            year = currentYear - 1,
            startDate = LocalDate.of(currentYear - 1, 4, 24).format(formatter),
            endDate = LocalDate.of(currentYear - 1, 5, 1).format(formatter),
            centerLatitude = -32.392204,
            centerLongitude = 19.415494,
            radiusKm = 5.0,
            theme = "Time Machine",
            isCurrentYear = false,
            lastUpdated = System.currentTimeMillis()
        ),
        // 2020 event
        Event(
            id = UUID.randomUUID().toString(),
            year = 2020,
            startDate = LocalDate.of(2020, 4, 20).format(formatter),
            endDate = LocalDate.of(2020, 4, 27).format(formatter),
            centerLatitude = -32.392204,
            centerLongitude = 19.415494,
            radiusKm = 5.0,
            theme = "Ephemeropolis",
            isCurrentYear = false,
            lastUpdated = System.currentTimeMillis()
        )
    )
}