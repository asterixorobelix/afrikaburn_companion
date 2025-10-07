package io.asterixorobelix.afrikaburn.api

import io.asterixorobelix.afrikaburn.domain.ArtInstallation
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.util.UUID
import kotlin.math.sqrt
import kotlin.math.pow

fun Route.artInstallationsApi() {
    route("/api/v1") {
        get("/events/{eventId}/art-installations") {
        // Extract eventId path parameter
        val eventId = call.parameters["eventId"] ?: run {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing event ID"))
            return@get
        }

        // Validate UUID format
        try {
            UUID.fromString(eventId)
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid event ID format"))
            return@get
        }

        // Extract optional lat/lng query parameters for location-based unlocking
        val userLat = call.request.queryParameters["lat"]?.toDoubleOrNull()
        val userLng = call.request.queryParameters["lng"]?.toDoubleOrNull()

        // Mock data for art installations
        val allArtInstallations = getMockArtInstallations(eventId)

        // Filter based on location unlock rules if coordinates are provided
        val filteredInstallations = if (userLat != null && userLng != null) {
            allArtInstallations.filter { installation ->
                // If not hidden, always show
                if (!installation.is_hidden) return@filter true

                // Check if unlocked by timestamp
                installation.unlock_timestamp?.let { unlockTime ->
                    if (System.currentTimeMillis() >= unlockTime) return@filter true
                }

                // Check if unlocked by proximity (within 50 meters)
                val distance = calculateDistance(
                    userLat, userLng,
                    installation.latitude, installation.longitude
                )
                distance <= 0.05 // 50 meters in km
            }
        } else {
            // Without location, only show non-hidden installations
            allArtInstallations.filter { !it.is_hidden }
        }

        call.respond(HttpStatusCode.OK, filteredInstallations)
        }
    }
}

// Helper function to calculate distance between two coordinates in kilometers
private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val lat1Rad = Math.toRadians(lat1)
    val lat2Rad = Math.toRadians(lat2)

    val a = sin(dLat / 2).pow(2) + sin(dLon / 2).pow(2) * cos(lat1Rad) * cos(lat2Rad)
    val c = 2 * asin(sqrt(a))
    return earthRadiusKm * c
}

// Mock data generator
private fun getMockArtInstallations(eventId: String): List<ArtInstallation> {
    val currentTime = System.currentTimeMillis()
    
    return listOf(
        ArtInstallation(
            id = UUID.randomUUID().toString(),
            event_id = eventId,
            name = "The Burning Embrace",
            artist_name = "Alexandra Chen",
            description = "A 30-foot tall sculpture made from recycled metal, representing human connection and unity. Interactive LED lights respond to touch and movement.",
            latitude = -30.7125,
            longitude = 23.9875,
            photo_urls = listOf(
                "https://example.com/art/burning-embrace-1.jpg",
                "https://example.com/art/burning-embrace-2.jpg"
            ),
            artist_bio = "Alexandra Chen is an installation artist focusing on interactive experiences that bring communities together.",
            interactive_features = listOf("Touch-sensitive LEDs", "Sound activation", "Mobile app integration"),
            is_hidden = false,
            unlock_timestamp = null,
            qr_code = "QR-ART-001",
            last_updated = currentTime
        ),
        ArtInstallation(
            id = UUID.randomUUID().toString(),
            event_id = eventId,
            name = "Desert Dreams Portal",
            artist_name = "Collective Imagination",
            description = "A mysterious portal structure that appears to bend reality. Hidden until you discover it by wandering the playa.",
            latitude = -30.7089,
            longitude = 23.9912,
            photo_urls = listOf("https://example.com/art/desert-dreams.jpg"),
            artist_bio = "Collective Imagination is a group of 12 artists from around the world.",
            interactive_features = listOf("Augmented reality experiences", "Hidden messages"),
            is_hidden = true,
            unlock_timestamp = null, // Only unlockable by proximity
            qr_code = "QR-ART-002",
            last_updated = currentTime
        ),
        ArtInstallation(
            id = UUID.randomUUID().toString(),
            event_id = eventId,
            name = "The Time Capsule",
            artist_name = "Future Memory Collective",
            description = "An installation that unlocks different content throughout the event. Each day reveals new interactive elements.",
            latitude = -30.7150,
            longitude = 23.9850,
            photo_urls = listOf("https://example.com/art/time-capsule.jpg"),
            artist_bio = "Future Memory Collective explores the intersection of time, memory, and community.",
            interactive_features = listOf("Time-based content", "Community message board", "Digital time capsule"),
            is_hidden = true,
            unlock_timestamp = currentTime + 86400000, // Unlocks 24 hours from now
            qr_code = "QR-ART-003",
            last_updated = currentTime
        ),
        ArtInstallation(
            id = UUID.randomUUID().toString(),
            event_id = eventId,
            name = "Echoes of Afrika",
            artist_name = "Themba Mokoena",
            description = "A sound sculpture that captures and replays voices from the community, creating an evolving audio landscape.",
            latitude = -30.7105,
            longitude = 23.9895,
            photo_urls = listOf(
                "https://example.com/art/echoes-1.jpg",
                "https://example.com/art/echoes-2.jpg",
                "https://example.com/art/echoes-3.jpg"
            ),
            artist_bio = "Themba Mokoena is a South African sound artist exploring community voices and collective memory.",
            interactive_features = listOf("Voice recording station", "Playback dome", "Sound visualization"),
            is_hidden = false,
            unlock_timestamp = null,
            qr_code = "QR-ART-004",
            last_updated = currentTime
        ),
        ArtInstallation(
            id = UUID.randomUUID().toString(),
            event_id = eventId,
            name = "The Constellation Map",
            artist_name = "Star Collective",
            description = "A ground-level star map that lights up at night, showing constellations visible from the Southern Hemisphere.",
            latitude = -30.7132,
            longitude = 23.9888,
            photo_urls = listOf("https://example.com/art/constellation.jpg"),
            artist_bio = "Star Collective creates installations that connect earthbound humans with the cosmos.",
            interactive_features = listOf("Motion-activated lighting", "Constellation stories", "Star navigation guide"),
            is_hidden = false,
            unlock_timestamp = null,
            qr_code = "QR-ART-005",
            last_updated = currentTime
        )
    )
}

// Extension function for trigonometric calculations
private fun sin(value: Double) = kotlin.math.sin(value)
private fun cos(value: Double) = kotlin.math.cos(value)
private fun asin(value: Double) = kotlin.math.asin(value)