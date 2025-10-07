package io.asterixorobelix.afrikaburn.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Theme camp entity matching OpenAPI specification
 */
@Serializable
data class ThemeCamp(
    val id: String,
    @SerialName("event_id")
    val eventId: String,
    val name: String,
    val description: String? = null,
    val latitude: Double,
    val longitude: Double,
    @SerialName("contact_info")
    val contactInfo: String? = null,
    val activities: List<String> = emptyList(),
    val amenities: List<String> = emptyList(),
    @SerialName("qr_code")
    val qrCode: String? = null,
    @SerialName("photo_url")
    val photoUrl: String? = null,
    @SerialName("is_hidden")
    val isHidden: Boolean = false,
    @SerialName("unlock_timestamp")
    val unlockTimestamp: Long? = null,
    @SerialName("last_updated")
    val lastUpdated: Long
)