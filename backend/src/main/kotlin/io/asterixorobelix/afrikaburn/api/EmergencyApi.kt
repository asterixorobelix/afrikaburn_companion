package io.asterixorobelix.afrikaburn.api

import io.asterixorobelix.afrikaburn.domain.ContactType
import io.asterixorobelix.afrikaburn.domain.EmergencyContact
import io.asterixorobelix.afrikaburn.domain.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.util.UUID

/**
 * Emergency contacts API endpoints implementation following the OpenAPI specification.
 * Provides access to emergency and safety contact information for events.
 */
fun Route.emergencyApi() {
    route("/api/v1/events/{eventId}/emergency-contacts") {
        get {
            try {
                // Extract eventId path parameter
                val eventId = call.parameters["eventId"] ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(
                            error = "missing_parameter",
                            message = "Event ID is required"
                        )
                    )
                    return@get
                }

                // Validate UUID format
                try {
                    UUID.fromString(eventId)
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(
                            error = "invalid_parameter",
                            message = "Invalid event ID format"
                        )
                    )
                    return@get
                }

                // Get mock emergency contacts for the event
                val emergencyContacts = getMockEmergencyContacts(eventId)
                
                call.respond(HttpStatusCode.OK, emergencyContacts)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        error = "internal_server_error",
                        message = "An unexpected error occurred while fetching emergency contacts"
                    )
                )
            }
        }
    }
}

/**
 * Returns mock emergency contact data for development.
 * In production, this would query the database filtered by event ID.
 * 
 * These contacts are based on realistic AfrikaBurn emergency services.
 */
private fun getMockEmergencyContacts(eventId: String): List<EmergencyContact> {
    return listOf(
        // Rangers - Primary safety and conflict resolution
        EmergencyContact(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Rangers HQ",
            contactType = ContactType.RANGER,
            phoneNumber = "+27 87 654 3210",
            radioChannel = "Channel 3",
            latitude = -32.392204,
            longitude = 19.415494,
            description = "Primary contact for safety issues, lost persons, and conflict resolution",
            isAvailable24Hours = true,
            priority = 100
        ),
        EmergencyContact(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Rangers Outpost - 3 o'clock",
            contactType = ContactType.RANGER,
            radioChannel = "Channel 3",
            latitude = -32.389854,
            longitude = 19.418344,
            description = "Rangers station at 3 o'clock sector",
            isAvailable24Hours = true,
            priority = 80
        ),
        
        // Medical Services
        EmergencyContact(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Medical Centre - Main",
            contactType = ContactType.MEDICAL,
            phoneNumber = "+27 87 654 3211",
            radioChannel = "Channel 5",
            latitude = -32.391504,
            longitude = 19.415894,
            description = "Main medical facility for emergencies and first aid",
            isAvailable24Hours = true,
            priority = 100
        ),
        EmergencyContact(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Medical Outpost - Binnekring",
            contactType = ContactType.MEDICAL,
            radioChannel = "Channel 5",
            latitude = -32.394604,
            longitude = 19.413294,
            description = "Secondary medical station in Binnekring area",
            isAvailable24Hours = true,
            priority = 90
        ),
        
        // Emergency Services
        EmergencyContact(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Fire Safety Team",
            contactType = ContactType.EMERGENCY,
            phoneNumber = "+27 87 654 3212",
            radioChannel = "Channel 7",
            latitude = -32.392804,
            longitude = 19.416094,
            description = "Fire safety and suppression team. Contact for all fire-related emergencies",
            isAvailable24Hours = true,
            priority = 95
        ),
        EmergencyContact(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Site Ops Emergency",
            contactType = ContactType.EMERGENCY,
            phoneNumber = "+27 87 654 3213",
            radioChannel = "Channel 1",
            description = "Infrastructure emergencies, power outages, water issues",
            isAvailable24Hours = true,
            priority = 85
        ),
        EmergencyContact(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Sanctuary",
            contactType = ContactType.EMERGENCY,
            radioChannel = "Channel 8",
            latitude = -32.393104,
            longitude = 19.414494,
            description = "Psychological support and crisis intervention",
            operatingHours = "24/7 during event",
            isAvailable24Hours = true,
            priority = 70
        ),
        
        // Administrative Contacts
        EmergencyContact(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Event Control",
            contactType = ContactType.ADMIN,
            phoneNumber = "+27 87 654 3214",
            radioChannel = "Channel 1",
            latitude = -32.392004,
            longitude = 19.415294,
            description = "Main event coordination and information hub",
            isAvailable24Hours = true,
            priority = 60
        ),
        EmergencyContact(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "Gate Operations",
            contactType = ContactType.ADMIN,
            phoneNumber = "+27 87 654 3215",
            radioChannel = "Channel 2",
            description = "Entry/exit issues, wristbands, vehicle passes",
            operatingHours = "06:00 - 22:00",
            isAvailable24Hours = false,
            priority = 50
        ),
        EmergencyContact(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            name = "DPW (Department of Public Works)",
            contactType = ContactType.ADMIN,
            radioChannel = "Channel 4",
            description = "Infrastructure support, porto-potties, general site maintenance",
            operatingHours = "07:00 - 19:00",
            isAvailable24Hours = false,
            priority = 40
        )
    )
}