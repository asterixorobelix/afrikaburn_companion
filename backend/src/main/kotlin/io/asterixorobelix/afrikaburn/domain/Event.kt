package io.asterixorobelix.afrikaburn.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val id: String,
    val year: Int,
    @SerialName("start_date")
    val startDate: String,
    @SerialName("end_date")
    val endDate: String,
    @SerialName("center_latitude")
    val centerLatitude: Double,
    @SerialName("center_longitude")
    val centerLongitude: Double,
    @SerialName("radius_km")
    val radiusKm: Double = 5.0,
    val theme: String,
    @SerialName("is_current_year")
    val isCurrentYear: Boolean = false,
    @SerialName("last_updated")
    val lastUpdated: Long
)