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
}