package io.asterixorobelix.afrikaburn.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing the user's personal camp pin on the map.
 * Only one camp pin is allowed per user.
 */
interface UserCampPinRepository {
    /**
     * Observe the user's camp pin. Emits null if no pin exists.
     */
    fun observeCampPin(): Flow<UserCampPinData?>

    /**
     * Get the current camp pin, or null if none exists.
     */
    suspend fun getCampPin(): UserCampPinData?

    /**
     * Save or update the user's camp pin location.
     * @param latitude GPS latitude
     * @param longitude GPS longitude
     * @param name Optional name for the pin (defaults to "My Camp")
     */
    suspend fun saveCampPin(latitude: Double, longitude: Double, name: String = "My Camp")

    /**
     * Update just the pin's location (for move operations).
     */
    suspend fun updateLocation(latitude: Double, longitude: Double)

    /**
     * Update the pin's name.
     */
    suspend fun updateName(name: String)

    /**
     * Delete the user's camp pin.
     */
    suspend fun deleteCampPin()

    /**
     * Check if a camp pin exists.
     */
    suspend fun hasCampPin(): Boolean
}

/**
 * Data class representing the user's camp pin.
 */
data class UserCampPinData(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long
)
