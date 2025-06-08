package io.asterixorobelix.afrikaburn.domain

import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(val status: String, val timestamp: Long)