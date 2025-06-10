package io.asterixorobelix.afrikaburn.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectItem(
    @SerialName("Name") val name: String,
    @SerialName("Description") val description: String,
    @SerialName("Artist") val artist: Artist = Artist(),
    @SerialName("code") val code: String = "",
    @SerialName("status") val status: String = ""
) {
    /**
     * Determines if this project/camp is family-friendly based on the status field.
     * Returns true if status contains "Fam" (including "Fam(ish)")
     */
    val isFamilyFriendly: Boolean
        get() = status.contains("Fam", ignoreCase = true)
    
    /**
     * Determines if this project/camp operates during daytime based on the status field.
     * Returns true if status contains "Day Time" or "Morning"
     */
    val isDaytime: Boolean
        get() = status.contains("Day Time", ignoreCase = true) || 
                status.contains("Morning", ignoreCase = true)
    
    /**
     * Determines if this project/camp operates during nighttime based on the status field.
     * Returns true if status contains "Night Time" or "All Night"
     */
    val isNighttime: Boolean
        get() = status.contains("Night Time", ignoreCase = true) || 
                status.contains("All Night", ignoreCase = true)
    
    /**
     * Determines if this project/camp matches the given time filter
     */
    fun matchesTimeFilter(timeFilter: TimeFilter): Boolean {
        return when (timeFilter) {
            TimeFilter.ALL -> true
            TimeFilter.DAYTIME -> isDaytime
            TimeFilter.NIGHTTIME -> isNighttime
        }
    }
}