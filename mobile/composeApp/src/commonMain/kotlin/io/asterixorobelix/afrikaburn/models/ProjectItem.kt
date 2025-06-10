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
)