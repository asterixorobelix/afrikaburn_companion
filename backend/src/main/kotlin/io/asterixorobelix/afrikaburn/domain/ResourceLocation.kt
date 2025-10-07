package io.asterixorobelix.afrikaburn.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Resource location (water, ice, help stations) following the OpenAPI specification.
 */
@Serializable
data class ResourceLocation(
    val id: String,
    @SerialName("event_id")
    val eventId: String,
    val name: String,
    @SerialName("resource_type")
    val resourceType: ResourceType,
    val latitude: Double,
    val longitude: Double,
    val availability: Availability = Availability.UNKNOWN,
    @SerialName("last_status_update")
    val lastStatusUpdate: Long? = null,
    @SerialName("operating_hours")
    val operatingHours: String? = null,
    val description: String? = null,
    @SerialName("is_verified")
    val isVerified: Boolean = false
)

@Serializable
enum class ResourceType {
    @SerialName("water")
    WATER,
    @SerialName("ice")
    ICE,
    @SerialName("help")
    HELP,
    @SerialName("medical")
    MEDICAL,
    @SerialName("food")
    FOOD,
    @SerialName("services")
    SERVICES
}

@Serializable
enum class Availability {
    @SerialName("available")
    AVAILABLE,
    @SerialName("limited")
    LIMITED,
    @SerialName("unavailable")
    UNAVAILABLE,
    @SerialName("unknown")
    UNKNOWN
}