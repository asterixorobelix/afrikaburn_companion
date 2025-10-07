package io.asterixorobelix.afrikaburn.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventPerformance(
    val id: String,
    @SerialName("event_id")
    val eventId: String,
    val name: String,
    val description: String? = null,
    @SerialName("performer_name")
    val performerName: String? = null,
    @SerialName("start_time")
    val startTime: String,
    @SerialName("end_time")
    val endTime: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val venue: String? = null,
    val category: PerformanceCategory,
    @SerialName("is_hidden")
    val isHidden: Boolean = false,
    @SerialName("last_updated")
    val lastUpdated: Long
)

@Serializable
enum class PerformanceCategory {
    @SerialName("Workshop")
    Workshop,
    @SerialName("Music")
    Music,
    @SerialName("Art")
    Art,
    @SerialName("Talk")
    Talk,
    @SerialName("Dance")
    Dance,
    @SerialName("Wellness")
    Wellness,
    @SerialName("Other")
    Other
}