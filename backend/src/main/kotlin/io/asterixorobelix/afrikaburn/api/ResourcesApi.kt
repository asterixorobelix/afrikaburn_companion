package io.asterixorobelix.afrikaburn.api

import io.asterixorobelix.afrikaburn.domain.Availability
import io.asterixorobelix.afrikaburn.domain.ErrorResponse
import io.asterixorobelix.afrikaburn.domain.ResourceLocation
import io.asterixorobelix.afrikaburn.domain.ResourceType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.util.UUID

/**
 * Resources API endpoints implementation following the OpenAPI specification.
 */
fun Route.resourcesApi() {
    route("/api/v1/events/{eventId}/resource-locations") {
        get {
            try {
                // Extract eventId path parameter
                val eventId = call.parameters["eventId"] ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(
                            error = "missing_event_id",
                            message = "Event ID is required"
                        )
                    )
                    return@get
                }

                // Validate eventId is a valid UUID
                try {
                    UUID.fromString(eventId)
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(
                            error = "invalid_event_id",
                            message = "Event ID must be a valid UUID"
                        )
                    )
                    return@get
                }

                // Extract optional resource_type query parameter
                val resourceTypeParam = call.request.queryParameters["resource_type"]
                
                // Get mock resource locations
                val allResourceLocations = getMockResourceLocations(eventId)

                // Filter by resource type if specified
                val filteredLocations = if (resourceTypeParam != null) {
                    try {
                        val resourceType = ResourceType.valueOf(resourceTypeParam.uppercase())
                        allResourceLocations.filter { it.resourceType == resourceType }
                    } catch (e: IllegalArgumentException) {
                        // Invalid resource type provided
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(
                                error = "invalid_resource_type",
                                message = "Invalid resource type. Valid values are: water, ice, help, medical, food, services"
                            )
                        )
                        return@get
                    }
                } else {
                    allResourceLocations
                }

                call.respond(HttpStatusCode.OK, filteredLocations)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        error = "internal_server_error",
                        message = "An unexpected error occurred while fetching resource locations"
                    )
                )
            }
        }
    }
}

/**
 * Returns mock resource location data for development.
 * In production, this would query the database.
 * 
 * Creates realistic desert resource points distributed across the event area.
 */
private fun getMockResourceLocations(eventId: String): List<ResourceLocation> {
    val currentTime = System.currentTimeMillis()
    val baseLatitude = -32.392204
    val baseLongitude = 19.415494
    
    return listOf(
        // Water stations - distributed around the playa
        ResourceLocation(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Center Camp Water Station",
            resourceType = ResourceType.WATER,
            latitude = baseLatitude,
            longitude = baseLongitude,
            availability = Availability.AVAILABLE,
            lastStatusUpdate = currentTime,
            operatingHours = "24/7",
            description = "Main water station at Center Camp. Bring your own container.",
            isVerified = true
        ),
        ResourceLocation(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "3 O'Clock Water Point",
            resourceType = ResourceType.WATER,
            latitude = baseLatitude + 0.015,
            longitude = baseLongitude + 0.020,
            availability = Availability.AVAILABLE,
            lastStatusUpdate = currentTime - 3600000, // 1 hour ago
            operatingHours = "24/7",
            description = "Water refill station. Please conserve water.",
            isVerified = true
        ),
        ResourceLocation(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "9 O'Clock Water Point",
            resourceType = ResourceType.WATER,
            latitude = baseLatitude - 0.012,
            longitude = baseLongitude - 0.018,
            availability = Availability.LIMITED,
            lastStatusUpdate = currentTime - 1800000, // 30 minutes ago
            operatingHours = "24/7",
            description = "Limited water available. Use sparingly.",
            isVerified = true
        ),
        
        // Ice stations
        ResourceLocation(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Center Camp Ice Sales",
            resourceType = ResourceType.ICE,
            latitude = baseLatitude + 0.002,
            longitude = baseLongitude - 0.001,
            availability = Availability.AVAILABLE,
            lastStatusUpdate = currentTime,
            operatingHours = "8:00 - 18:00",
            description = "Ice sales. R20 per bag. Cash only.",
            isVerified = true
        ),
        ResourceLocation(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "6 O'Clock Ice Depot",
            resourceType = ResourceType.ICE,
            latitude = baseLatitude - 0.025,
            longitude = baseLongitude,
            availability = Availability.UNAVAILABLE,
            lastStatusUpdate = currentTime - 7200000, // 2 hours ago
            operatingHours = "10:00 - 16:00",
            description = "SOLD OUT - Check back tomorrow morning",
            isVerified = false
        ),
        
        // Help/Ranger stations
        ResourceLocation(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Ranger Station Alpha",
            resourceType = ResourceType.HELP,
            latitude = baseLatitude + 0.008,
            longitude = baseLongitude + 0.012,
            availability = Availability.AVAILABLE,
            lastStatusUpdate = currentTime,
            operatingHours = "24/7",
            description = "Rangers on duty. For emergencies and assistance.",
            isVerified = true
        ),
        ResourceLocation(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Ranger Outpost 3",
            resourceType = ResourceType.HELP,
            latitude = baseLatitude - 0.020,
            longitude = baseLongitude + 0.025,
            availability = Availability.AVAILABLE,
            lastStatusUpdate = currentTime - 600000, // 10 minutes ago
            operatingHours = "24/7",
            description = "Remote ranger station. Radio contact available.",
            isVerified = true
        ),
        
        // Medical stations
        ResourceLocation(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Main Medical Center",
            resourceType = ResourceType.MEDICAL,
            latitude = baseLatitude + 0.005,
            longitude = baseLongitude + 0.003,
            availability = Availability.AVAILABLE,
            lastStatusUpdate = currentTime,
            operatingHours = "24/7",
            description = "Full medical facilities. Emergency and non-emergency care.",
            isVerified = true
        ),
        ResourceLocation(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "First Aid Station - Deep Playa",
            resourceType = ResourceType.MEDICAL,
            latitude = baseLatitude + 0.030,
            longitude = baseLongitude - 0.015,
            availability = Availability.AVAILABLE,
            lastStatusUpdate = currentTime - 1200000, // 20 minutes ago
            operatingHours = "24/7",
            description = "Basic first aid and heat emergency treatment.",
            isVerified = true
        ),
        
        // Food vendors
        ResourceLocation(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Center Camp Café",
            resourceType = ResourceType.FOOD,
            latitude = baseLatitude - 0.002,
            longitude = baseLongitude + 0.002,
            availability = Availability.AVAILABLE,
            lastStatusUpdate = currentTime,
            operatingHours = "7:00 - 22:00",
            description = "Coffee, tea, and light snacks available for purchase.",
            isVerified = true
        ),
        
        // Services
        ResourceLocation(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Porta-Potty Bank A",
            resourceType = ResourceType.SERVICES,
            latitude = baseLatitude + 0.010,
            longitude = baseLongitude - 0.008,
            availability = Availability.AVAILABLE,
            lastStatusUpdate = currentTime - 3600000, // 1 hour ago
            operatingHours = "24/7",
            description = "Portable toilets. Serviced daily.",
            isVerified = true
        ),
        ResourceLocation(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Bike Repair Camp",
            resourceType = ResourceType.SERVICES,
            latitude = baseLatitude - 0.007,
            longitude = baseLongitude + 0.010,
            availability = Availability.LIMITED,
            lastStatusUpdate = currentTime - 1800000, // 30 minutes ago
            operatingHours = "10:00 - 18:00",
            description = "Basic bike repairs. Limited parts available.",
            isVerified = false
        )
    )
}