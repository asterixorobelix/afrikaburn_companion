package io.asterixorobelix.afrikaburn.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Artist(
    @SerialName("s") val name: String = ""
)