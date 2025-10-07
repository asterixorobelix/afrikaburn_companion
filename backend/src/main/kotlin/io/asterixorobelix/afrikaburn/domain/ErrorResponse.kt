package io.asterixorobelix.afrikaburn.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Error response model according to the OpenAPI specification.
 * Used for consistent error handling across all API endpoints.
 */
@Serializable
data class ErrorResponse(
    @SerialName("error")
    val error: String,
    
    @SerialName("message")
    val message: String,
    
    @SerialName("details")
    val details: Map<String, String>? = null
)