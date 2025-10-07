package io.asterixorobelix.afrikaburn.domain

import kotlinx.serialization.Serializable

@Serializable
data class ArtInstallation(
    val id: String,
    val event_id: String,
    val name: String,
    val artist_name: String,
    val description: String? = null,
    val latitude: Double,
    val longitude: Double,
    val photo_urls: List<String> = emptyList(),
    val artist_bio: String? = null,
    val interactive_features: List<String> = emptyList(),
    val is_hidden: Boolean = false,
    val unlock_timestamp: Long? = null,
    val qr_code: String? = null,
    val last_updated: Long
)