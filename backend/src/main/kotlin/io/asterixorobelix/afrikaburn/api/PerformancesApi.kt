package io.asterixorobelix.afrikaburn.api

import io.asterixorobelix.afrikaburn.domain.ErrorResponse
import io.asterixorobelix.afrikaburn.domain.EventPerformance
import io.asterixorobelix.afrikaburn.domain.PerformanceCategory
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

fun Route.performancesApi() {
    route("/api/v1/events/{eventId}/performances") {
        get {
            // Extract path parameter
            val eventId = call.parameters["eventId"]
            
            // Validate eventId format
            if (eventId == null || !isValidUuid(eventId)) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(
                        error = "not_found",
                        message = "Event not found"
                    )
                )
                return@get
            }
            
            // Extract query parameters
            val dateParam = call.request.queryParameters["date"]
            val categoryParam = call.request.queryParameters["category"]
            
            // Validate date format if provided
            val filterDate = dateParam?.let { 
                try {
                    LocalDate.parse(it)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(
                            error = "bad_request",
                            message = "Invalid date format. Expected format: YYYY-MM-DD"
                        )
                    )
                    return@get
                }
            }
            
            // Validate category if provided
            val filterCategory = categoryParam?.let { 
                try {
                    PerformanceCategory.valueOf(it)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(
                            error = "bad_request",
                            message = "Invalid category. Valid values are: ${PerformanceCategory.values().joinToString(", ")}"
                        )
                    )
                    return@get
                }
            }
            
            // Generate mock data
            val performances = generateMockPerformances(eventId)
            
            // Filter performances based on query parameters
            val filteredPerformances = performances.filter { performance ->
                val matchesDate = filterDate == null || 
                    LocalDateTime.parse(performance.startTime).toLocalDate() == filterDate
                val matchesCategory = filterCategory == null || 
                    performance.category == filterCategory
                
                matchesDate && matchesCategory
            }
            
            // Return filtered performances
            call.respond(HttpStatusCode.OK, filteredPerformances)
        }
    }
}

private fun isValidUuid(value: String): Boolean {
    return try {
        UUID.fromString(value)
        true
    } catch (e: Exception) {
        false
    }
}

private fun generateMockPerformances(eventId: String): List<EventPerformance> {
    val baseDateTime = LocalDateTime.of(2025, 5, 1, 10, 0)
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    return listOf(
        EventPerformance(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Desert Yoga at Sunrise",
            description = "Start your day with a rejuvenating yoga session as the sun rises over the Tankwa",
            performerName = "Maya Sunshine",
            startTime = baseDateTime.withHour(6).withMinute(0).format(formatter),
            endTime = baseDateTime.withHour(7).withMinute(30).format(formatter),
            latitude = -32.4789,
            longitude = 19.9876,
            venue = "Temple of Dreams",
            category = PerformanceCategory.Wellness,
            isHidden = false,
            lastUpdated = System.currentTimeMillis()
        ),
        EventPerformance(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Fire Spinning Workshop",
            description = "Learn the basics of fire poi and staff spinning. Safety equipment provided.",
            performerName = "Blaze Collective",
            startTime = baseDateTime.withHour(14).withMinute(0).format(formatter),
            endTime = baseDateTime.withHour(16).withMinute(0).format(formatter),
            latitude = -32.4812,
            longitude = 19.9834,
            venue = "Fire Circle Camp",
            category = PerformanceCategory.Workshop,
            isHidden = false,
            lastUpdated = System.currentTimeMillis()
        ),
        EventPerformance(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Electronic Desert Sessions",
            description = "Deep house and techno journey through the night",
            performerName = "DJ Solar Flare",
            startTime = baseDateTime.withHour(22).withMinute(0).format(formatter),
            endTime = baseDateTime.plusDays(1).withHour(2).withMinute(0).format(formatter),
            latitude = -32.4756,
            longitude = 19.9901,
            venue = "Dust Bowl Dance Floor",
            category = PerformanceCategory.Music,
            isHidden = false,
            lastUpdated = System.currentTimeMillis()
        ),
        EventPerformance(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Interactive Art Tour",
            description = "Guided tour of the main art installations with artist talks",
            performerName = "Art Rangers",
            startTime = baseDateTime.withHour(16).withMinute(30).format(formatter),
            endTime = baseDateTime.withHour(18).withMinute(30).format(formatter),
            latitude = -32.4798,
            longitude = 19.9856,
            venue = "Meeting Point: Center Camp",
            category = PerformanceCategory.Art,
            isHidden = false,
            lastUpdated = System.currentTimeMillis()
        ),
        EventPerformance(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Radical Self-Expression Talk",
            description = "Panel discussion on the principles of Burning Man culture",
            performerName = "Various Speakers",
            startTime = baseDateTime.plusDays(1).withHour(11).withMinute(0).format(formatter),
            endTime = baseDateTime.plusDays(1).withHour(12).withMinute(30).format(formatter),
            latitude = -32.4803,
            longitude = 19.9845,
            venue = "Speaker's Corner",
            category = PerformanceCategory.Talk,
            isHidden = false,
            lastUpdated = System.currentTimeMillis()
        ),
        EventPerformance(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Ecstatic Dance Journey",
            description = "Free-form movement meditation. No talking on the dance floor.",
            performerName = "Movement Medicine Crew",
            startTime = baseDateTime.plusDays(1).withHour(19).withMinute(0).format(formatter),
            endTime = baseDateTime.plusDays(1).withHour(21).withMinute(0).format(formatter),
            latitude = -32.4791,
            longitude = 19.9867,
            venue = "Sacred Space",
            category = PerformanceCategory.Dance,
            isHidden = false,
            lastUpdated = System.currentTimeMillis()
        ),
        EventPerformance(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Drum Circle Gathering",
            description = "Community drum circle open to all skill levels. Bring instruments or use ours.",
            performerName = "Rhythm Tribe",
            startTime = baseDateTime.plusDays(2).withHour(17).withMinute(0).format(formatter),
            endTime = baseDateTime.plusDays(2).withHour(19).withMinute(0).format(formatter),
            latitude = -32.4823,
            longitude = 19.9812,
            venue = "Drum Camp",
            category = PerformanceCategory.Music,
            isHidden = false,
            lastUpdated = System.currentTimeMillis()
        ),
        EventPerformance(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Costume Workshop",
            description = "Create your playa fashion with recycled materials",
            performerName = "Fashion Forward Camp",
            startTime = baseDateTime.plusDays(2).withHour(13).withMinute(0).format(formatter),
            endTime = baseDateTime.plusDays(2).withHour(15).withMinute(0).format(formatter),
            latitude = -32.4785,
            longitude = 19.9889,
            venue = "Fabric Dome",
            category = PerformanceCategory.Workshop,
            isHidden = false,
            lastUpdated = System.currentTimeMillis()
        ),
        EventPerformance(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Sunset Meditation",
            description = "Guided meditation as we watch the sun set over the Tankwa",
            performerName = "Peace Practitioners",
            startTime = baseDateTime.plusDays(2).withHour(18).withMinute(30).format(formatter),
            endTime = baseDateTime.plusDays(2).withHour(19).withMinute(30).format(formatter),
            latitude = -32.4767,
            longitude = 19.9923,
            venue = "Sunset Point",
            category = PerformanceCategory.Wellness,
            isHidden = false,
            lastUpdated = System.currentTimeMillis()
        ),
        EventPerformance(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Open Mic Night",
            description = "Share your talents! Poetry, music, comedy, and more",
            performerName = "Various Artists",
            startTime = baseDateTime.plusDays(3).withHour(20).withMinute(0).format(formatter),
            endTime = baseDateTime.plusDays(3).withHour(23).withMinute(0).format(formatter),
            latitude = -32.4809,
            longitude = 19.9834,
            venue = "The Stage",
            category = PerformanceCategory.Other,
            isHidden = false,
            lastUpdated = System.currentTimeMillis()
        )
    )
}