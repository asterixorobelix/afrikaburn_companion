package io.asterixorobelix.afrikaburn.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a mutant vehicle at the event.
 * Based on OpenAPI schema definition for MutantVehicle.
 */
@Serializable
data class MutantVehicle(
    val id: String,
    @SerialName("event_id")
    val eventId: String,
    val name: String,
    val description: String,
    @SerialName("owner_name")
    val ownerName: String? = null,
    @SerialName("photo_urls")
    val photoUrls: List<String> = emptyList(),
    @SerialName("schedule_info")
    val scheduleInfo: String? = null,
    @SerialName("last_known_latitude")
    val lastKnownLatitude: Double? = null,
    @SerialName("last_known_longitude")
    val lastKnownLongitude: Double? = null,
    @SerialName("last_location_update")
    val lastLocationUpdate: Long? = null,
    @SerialName("search_tags")
    val searchTags: List<String> = emptyList(),
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("last_updated")
    val lastUpdated: Long? = null
)