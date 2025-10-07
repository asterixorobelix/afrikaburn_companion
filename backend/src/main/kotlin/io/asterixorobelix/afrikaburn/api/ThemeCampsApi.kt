package io.asterixorobelix.afrikaburn.api

import io.asterixorobelix.afrikaburn.domain.ErrorResponse
import io.asterixorobelix.afrikaburn.domain.ThemeCamp
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.math.*

/**
 * API endpoint for theme camps
 * Implements location-based content unlocking
 */
fun Route.themeCampsApi() {
    get("/api/v1/events/{eventId}/theme-camps") {
            // Extract path parameter
            val eventId = call.parameters["eventId"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "bad_request",
                    message = "Event ID is required"
                )
            )
            
            // Validate UUID format
            try {
                UUID.fromString(eventId)
            } catch (e: IllegalArgumentException) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        error = "invalid_event_id",
                        message = "Event ID must be a valid UUID"
                    )
                )
            }
            
            // Extract query parameters for location-based unlocking
            val userLat = call.request.queryParameters["lat"]?.toDoubleOrNull()
            val userLng = call.request.queryParameters["lng"]?.toDoubleOrNull()
            
            // Validate coordinate bounds if provided
            if (userLat != null && (userLat < -90 || userLat > 90)) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        error = "invalid_coordinates",
                        message = "Latitude must be between -90 and 90"
                    )
                )
            }
            
            if (userLng != null && (userLng < -180 || userLng > 180)) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        error = "invalid_coordinates",
                        message = "Longitude must be between -180 and 180"
                    )
                )
            }
            
            // Get theme camps (mock data for now)
            val themeCamps = getMockThemeCamps(eventId)
            
            // Filter based on location if coordinates provided
            val filteredCamps = if (userLat != null && userLng != null) {
                filterThemeCampsByLocation(themeCamps, userLat, userLng)
            } else {
                // If no location provided, filter out all hidden camps
                themeCamps.filter { !it.isHidden }
            }
            
            call.respond(HttpStatusCode.OK, filteredCamps)
    }
}

/**
 * Filters theme camps based on user location
 * Hidden camps are shown if user is within 5km radius
 */
private fun filterThemeCampsByLocation(
    camps: List<ThemeCamp>,
    userLat: Double,
    userLng: Double
): List<ThemeCamp> {
    return camps.filter { camp ->
        if (!camp.isHidden) {
            // Non-hidden camps are always visible
            true
        } else {
            // Check if user is within 5km of the camp
            val distance = calculateDistance(userLat, userLng, camp.latitude, camp.longitude)
            distance <= 5.0 // 5km radius
        }
    }
}

/**
 * Calculates distance between two coordinates using Haversine formula
 * Returns distance in kilometers
 */
private fun calculateDistance(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val earthRadius = 6371.0 // Earth's radius in kilometers
    
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    
    return earthRadius * c
}

/**
 * Returns mock theme camp data for testing
 */
private fun getMockThemeCamps(eventId: String): List<ThemeCamp> {
    val currentTime = System.currentTimeMillis()
    
    return listOf(
        ThemeCamp(
            id = "550e8400-e29b-41d4-a716-446655440001",
            eventId = eventId,
            name = "Camp Chaos",
            description = "Welcome to organized chaos! Daily workshops on flow arts, fire spinning lessons at sunset.",
            latitude = -32.3917,
            longitude = 19.4470,
            contactInfo = "chaos@burnermail.com",
            activities = listOf("Flow Arts", "Fire Spinning", "Morning Yoga"),
            amenities = listOf("Shade Structure", "Chill Space", "Tea Service"),
            qrCode = "CAMP_CHAOS_2024",
            photoUrl = "https://example.com/camps/chaos.jpg",
            isHidden = false,
            unlockTimestamp = null,
            lastUpdated = currentTime
        ),
        ThemeCamp(
            id = "550e8400-e29b-41d4-a716-446655440002",
            eventId = eventId,
            name = "The Oasis",
            description = "Desert refuge with 24/7 chill music and hammock garden. Cold brew coffee every morning!",
            latitude = -32.3925,
            longitude = 19.4465,
            contactInfo = "oasis@burnermail.com",
            activities = listOf("Hammock Lounging", "Cold Brew Bar", "Sunset DJ Sets"),
            amenities = listOf("Hammocks", "Misting System", "Coffee Bar"),
            qrCode = "OASIS_2024",
            photoUrl = "https://example.com/camps/oasis.jpg",
            isHidden = false,
            unlockTimestamp = null,
            lastUpdated = currentTime
        ),
        ThemeCamp(
            id = "550e8400-e29b-41d4-a716-446655440003",
            eventId = eventId,
            name = "Secret Speakeasy",
            description = "Hidden bar serving prohibition-era cocktails. Find us if you can! Password: 'dusty boots'",
            latitude = -32.3910,
            longitude = 19.4480,
            contactInfo = null,
            activities = listOf("Cocktail Service", "Jazz Nights", "Secret Shows"),
            amenities = listOf("Full Bar", "Live Music", "Velvet Lounge"),
            qrCode = "SPEAKEASY_SECRET",
            photoUrl = null,
            isHidden = true, // This camp is hidden and requires location proximity
            unlockTimestamp = null,
            lastUpdated = currentTime
        ),
        ThemeCamp(
            id = "550e8400-e29b-41d4-a716-446655440004",
            eventId = eventId,
            name = "Ubuntu Village",
            description = "Community kitchen serving traditional South African food. Braais every evening!",
            latitude = -32.3930,
            longitude = 19.4450,
            contactInfo = "ubuntu@burnermail.com",
            activities = listOf("Community Dinners", "Braai Nights", "Storytelling Circle"),
            amenities = listOf("Kitchen", "Dining Area", "Fire Circle"),
            qrCode = "UBUNTU_2024",
            photoUrl = "https://example.com/camps/ubuntu.jpg",
            isHidden = false,
            unlockTimestamp = null,
            lastUpdated = currentTime
        ),
        ThemeCamp(
            id = "550e8400-e29b-41d4-a716-446655440005",
            eventId = eventId,
            name = "Dusty Disco",
            description = "24-hour disco under the stars. Bring your best moves and shiniest outfit!",
            latitude = -32.3905,
            longitude = 19.4475,
            contactInfo = "disco@burnermail.com",
            activities = listOf("24hr Dancing", "Disco Yoga", "Glitter Station"),
            amenities = listOf("Dance Floor", "Sound System", "Disco Ball"),
            qrCode = "DUSTY_DISCO_2024",
            photoUrl = "https://example.com/camps/disco.jpg",
            isHidden = false,
            unlockTimestamp = null,
            lastUpdated = currentTime
        ),
        ThemeCamp(
            id = "550e8400-e29b-41d4-a716-446655440006",
            eventId = eventId,
            name = "Time Travelers Lounge",
            description = "Step through time in our anachronistic paradise. Victorian tea time meets cyberpunk rave.",
            latitude = -32.3895,
            longitude = 19.4485,
            contactInfo = null,
            activities = listOf("Time Travel Tea", "Retro-Future Dance", "Costume Swap"),
            amenities = listOf("Time Machine", "Tea Service", "Costume Closet"),
            qrCode = "TIME_TRAVEL_2024",
            photoUrl = null,
            isHidden = true, // Another hidden camp
            unlockTimestamp = null,
            lastUpdated = currentTime
        )
    )
}