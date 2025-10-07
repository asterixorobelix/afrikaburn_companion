package io.asterixorobelix.afrikaburn.api

import io.asterixorobelix.afrikaburn.domain.MutantVehicle
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.util.UUID

/**
 * Mutant Vehicles API endpoints implementation following the OpenAPI specification.
 * Provides access to mutant vehicle information for a specific event.
 */
fun Route.mutantVehiclesApi() {
    route("/api/v1") {
        get("/events/{eventId}/mutant-vehicles") {
            try {
                // Extract eventId path parameter
                val eventId = call.parameters["eventId"]
                
                // Validate eventId is a valid UUID
                if (eventId == null || !isValidUUID(eventId)) {
                    call.respond(HttpStatusCode.NotFound, mapOf(
                        "error" to "not_found",
                        "message" to "Event not found"
                    ))
                    return@get
                }
                
                // Extract search query parameter
                val searchQuery = call.request.queryParameters["search"]?.lowercase()
                
                // Mock data for mutant vehicles
                // In production, this would query the database
                // Coordinates are based on Tankwa Karoo National Park location
                val mockMutantVehicles = listOf(
                    MutantVehicle(
                        id = UUID.randomUUID().toString(),
                        eventId = eventId,
                        name = "Fire Dragon",
                        description = "A massive dragon-shaped vehicle that breathes fire. Features multiple decks for dancing and socializing.",
                        ownerName = "Dragon Collective",
                        photoUrls = listOf(
                            "https://example.com/images/fire-dragon-1.jpg",
                            "https://example.com/images/fire-dragon-2.jpg"
                        ),
                        scheduleInfo = "Daily burns at sunset, roaming throughout the night",
                        lastKnownLatitude = -32.2421,
                        lastKnownLongitude = 20.0956,
                        lastLocationUpdate = System.currentTimeMillis() - 3600000, // 1 hour ago
                        searchTags = listOf("fire", "dragon", "dance", "music", "flame effects"),
                        isActive = true,
                        lastUpdated = System.currentTimeMillis()
                    ),
                    MutantVehicle(
                        id = UUID.randomUUID().toString(),
                        eventId = eventId,
                        name = "Disco Shark",
                        description = "A glittering shark on wheels playing disco and funk. Complete with mirror ball teeth and laser eyes.",
                        ownerName = "Funky Fish Crew",
                        photoUrls = listOf(
                            "https://example.com/images/disco-shark.jpg"
                        ),
                        scheduleInfo = "Nightly disco sessions from 10pm to 2am",
                        lastKnownLatitude = -32.2385,
                        lastKnownLongitude = 20.0935,
                        lastLocationUpdate = System.currentTimeMillis() - 1800000, // 30 minutes ago
                        searchTags = listOf("disco", "shark", "music", "dance", "lights", "funk"),
                        isActive = true,
                        lastUpdated = System.currentTimeMillis()
                    ),
                    MutantVehicle(
                        id = UUID.randomUUID().toString(),
                        eventId = eventId,
                        name = "The Time Machine",
                        description = "A steampunk-inspired contraption with gears, steam effects, and Victorian-era decorations. Travels through musical decades.",
                        ownerName = "Temporal Mechanics Guild",
                        photoUrls = listOf(
                            "https://example.com/images/time-machine-1.jpg",
                            "https://example.com/images/time-machine-2.jpg",
                            "https://example.com/images/time-machine-3.jpg"
                        ),
                        scheduleInfo = "Scheduled time travel sessions at noon and midnight",
                        lastKnownLatitude = null,
                        lastKnownLongitude = null,
                        lastLocationUpdate = null,
                        searchTags = listOf("steampunk", "time", "victorian", "gears", "steam"),
                        isActive = false, // Not currently active
                        lastUpdated = System.currentTimeMillis()
            ),
            MutantVehicle(
                        id = UUID.randomUUID().toString(),
                        eventId = eventId,
                        name = "Rainbow Unicorn",
                        description = "A magical unicorn that shoots rainbow lasers and plays uplifting trance music. Features a horn that lights up with the beat.",
                        ownerName = "Unicorn Dreams Collective",
                        photoUrls = listOf(
                            "https://example.com/images/rainbow-unicorn.jpg"
                        ),
                        scheduleInfo = "Appears at random magical moments throughout the event",
                        lastKnownLatitude = -32.2455,
                        lastKnownLongitude = 20.0976,
                        lastLocationUpdate = System.currentTimeMillis() - 7200000, // 2 hours ago
                        searchTags = listOf("unicorn", "rainbow", "magic", "trance", "lights", "fantasy"),
                        isActive = true,
                        lastUpdated = System.currentTimeMillis()
            ),
            MutantVehicle(
                        id = UUID.randomUUID().toString(),
                        eventId = eventId,
                        name = "Desert Submarine",
                        description = "A submarine navigating the desert sands. Complete with periscope viewing platform and underwater-themed sound system.",
                        ownerName = "Captain Nemo's Crew",
                        photoUrls = listOf(
                            "https://example.com/images/desert-submarine-1.jpg",
                            "https://example.com/images/desert-submarine-2.jpg"
                        ),
                        scheduleInfo = "Surfaces every evening at 8pm for deep sea disco",
                        lastKnownLatitude = -32.2395,
                        lastKnownLongitude = 20.0946,
                        lastLocationUpdate = System.currentTimeMillis() - 300000, // 5 minutes ago
                        searchTags = listOf("submarine", "nautical", "underwater", "periscope", "ocean"),
                        isActive = true,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
                
                // Filter vehicles based on search query if provided
                val filteredVehicles = if (searchQuery != null) {
                    mockMutantVehicles.filter { vehicle ->
                        // Search in name, description, owner name, and tags
                        vehicle.name.lowercase().contains(searchQuery) ||
                        vehicle.description.lowercase().contains(searchQuery) ||
                        vehicle.ownerName?.lowercase()?.contains(searchQuery) == true ||
                        vehicle.searchTags.any { tag -> tag.lowercase().contains(searchQuery) }
                    }
                } else {
                    mockMutantVehicles
                }
        
                // Return filtered list
                call.respond(HttpStatusCode.OK, filteredVehicles)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf(
                        "error" to "internal_server_error",
                        "message" to "An unexpected error occurred while fetching mutant vehicles"
                    )
                )
            }
        }
    }
}

private fun isValidUUID(uuid: String): Boolean {
    return try {
        UUID.fromString(uuid)
        true
    } catch (e: IllegalArgumentException) {
        false
    }
}