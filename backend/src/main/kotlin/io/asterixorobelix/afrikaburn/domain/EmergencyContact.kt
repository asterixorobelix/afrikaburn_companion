package io.asterixorobelix.afrikaburn.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an emergency contact for event safety and support.
 * Matches the EmergencyContact schema from the OpenAPI specification.
 */
@Serializable
data class EmergencyContact(
    @SerialName("id")
    val id: String,
    
    @SerialName("event_id")
    val eventId: String,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("contact_type")
    val contactType: ContactType,
    
    @SerialName("phone_number")
    val phoneNumber: String? = null,
    
    @SerialName("radio_channel")
    val radioChannel: String? = null,
    
    @SerialName("latitude")
    val latitude: Double? = null,
    
    @SerialName("longitude")
    val longitude: Double? = null,
    
    @SerialName("description")
    val description: String? = null,
    
    @SerialName("is_available_24_hours")
    val isAvailable24Hours: Boolean = false,
    
    @SerialName("operating_hours")
    val operatingHours: String? = null,
    
    @SerialName("priority")
    val priority: Int = 50
)

/**
 * Types of emergency contacts available at the event.
 */
@Serializable
enum class ContactType {
    @SerialName("ranger")
    RANGER,
    
    @SerialName("medical")
    MEDICAL,
    
    @SerialName("emergency")
    EMERGENCY,
    
    @SerialName("admin")
    ADMIN
}