package io.asterixorobelix.afrikaburn.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * MOOP (Matter Out of Place) report model.
 * Represents a report submitted by users about litter or debris that needs cleanup.
 */
@Serializable
data class MOOPReport(
    @SerialName("id")
    val id: String,
    
    @SerialName("latitude")
    val latitude: Double,
    
    @SerialName("longitude")
    val longitude: Double,
    
    @SerialName("description")
    val description: String,
    
    @SerialName("photo_url")
    val photoUrl: String? = null,
    
    @SerialName("severity")
    val severity: MOOPSeverity,
    
    @SerialName("status")
    val status: MOOPStatus = MOOPStatus.REPORTED,
    
    @SerialName("reported_timestamp")
    val reportedTimestamp: Long
)

/**
 * Request model for creating a new MOOP report.
 */
@Serializable
data class MOOPReportRequest(
    @SerialName("latitude")
    val latitude: Double,
    
    @SerialName("longitude")
    val longitude: Double,
    
    @SerialName("description")
    val description: String,
    
    @SerialName("severity")
    val severity: MOOPSeverity,
    
    @SerialName("photo_base64")
    val photoBase64: String? = null
)

/**
 * MOOP severity levels.
 */
@Serializable
enum class MOOPSeverity {
    @SerialName("low")
    LOW,
    
    @SerialName("medium")
    MEDIUM,
    
    @SerialName("high")
    HIGH
}

/**
 * MOOP report status.
 */
@Serializable
enum class MOOPStatus {
    @SerialName("reported")
    REPORTED,
    
    @SerialName("in_progress")
    IN_PROGRESS,
    
    @SerialName("resolved")
    RESOLVED
}