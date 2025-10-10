package io.asterixorobelix.afrikaburn.domain.usecase

import io.asterixorobelix.afrikaburn.domain.model.CampLocation
import io.asterixorobelix.afrikaburn.domain.model.CampLocationType
import io.asterixorobelix.afrikaburn.domain.model.CampType
import io.asterixorobelix.afrikaburn.domain.model.LocationPrivacy
import io.asterixorobelix.afrikaburn.domain.model.getCurrentTimestamp
import io.asterixorobelix.afrikaburn.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Use case for marking and managing user's camp GPS location.
 * 
 * Handles saving camp locations with validation, ensuring coordinates are within
 * event boundaries, and managing offline storage. Supports updating and removing
 * camp locations with optional camp name and notes.
 */
class MarkCampLocationUseCase(
    private val mapRepository: MapRepository
) {
    /**
     * Save or update user's camp location with validation.
     * 
     * @param deviceId Unique device identifier
     * @param latitude GPS latitude coordinate
     * @param longitude GPS longitude coordinate
     * @param campName Optional name for the camp
     * @param notes Optional notes about the camp
     * @param accuracy GPS accuracy in meters (optional)
     * @param altitude Elevation in meters (optional)
     * @return Result with success or validation errors
     */
    @OptIn(ExperimentalUuidApi::class)
    suspend fun saveCampLocation(
        deviceId: String,
        latitude: Double,
        longitude: Double,
        campName: String? = null,
        notes: String? = null,
        accuracy: Double? = null,
        altitude: Double? = null
    ): Result<CampLocation> {
        // Validate input parameters
        val validationResult = validateCampLocation(
            deviceId = deviceId,
            latitude = latitude,
            longitude = longitude,
            campName = campName
        )
        
        if (!validationResult.isValid) {
            return Result.failure(
                CampLocationException.ValidationError(validationResult.errors)
            )
        }
        
        return try {
            val timestamp = getCurrentTimestamp()
            
            // Check if user already has an active camp location
            val existingActiveLocation = mapRepository.getActiveCampLocation(deviceId)
            
            // Create new camp location
            val campLocation = CampLocation(
                id = Uuid.random().toString(),
                deviceId = deviceId,
                name = campName ?: "My Camp",
                description = "Personal camp location",
                latitude = latitude,
                longitude = longitude,
                altitude = altitude,
                accuracy = accuracy,
                isUserMarked = true,
                isOfficial = false,
                locationType = CampLocationType.PERSONAL_CAMP,
                address = null,
                whatThreeWords = null,
                campType = CampType.PERSONAL,
                capacity = null,
                amenities = emptyList(),
                contacts = emptyList(),
                operatingHours = null,
                isActive = true,
                isShared = false,
                shareCode = null,
                sharedByDeviceId = null,
                privacyLevel = LocationPrivacy.PRIVATE,
                tags = emptyList(),
                notes = notes,
                photos = emptyList(),
                lastVisited = timestamp,
                visitCount = 1,
                isEmergencyLocation = false,
                emergencyType = null,
                createdAt = timestamp,
                lastUpdated = timestamp
            )
            
            // Deactivate existing active location if present
            existingActiveLocation?.let {
                val deactivated = it.copy(
                    isActive = false,
                    lastUpdated = timestamp
                )
                mapRepository.updateCampLocation(deactivated)
            }
            
            // Save new camp location
            mapRepository.saveCampLocation(campLocation)
            
            Result.success(campLocation)
        } catch (e: Exception) {
            Result.failure(
                CampLocationException.StorageError("Failed to save camp location: ${e.message}")
            )
        }
    }
    
    /**
     * Update existing camp location.
     * 
     * @param locationId ID of the location to update
     * @param campName Optional new name
     * @param notes Optional new notes
     * @param amenities Optional list of amenities
     * @param tags Optional list of tags
     * @return Result with updated location or error
     */
    suspend fun updateCampLocation(
        locationId: String,
        campName: String? = null,
        notes: String? = null,
        amenities: List<String>? = null,
        tags: List<String>? = null
    ): Result<CampLocation> {
        return try {
            val existingLocation = mapRepository.getCampLocationById(locationId)
                ?: return Result.failure(
                    CampLocationException.NotFound("Camp location not found")
                )
            
            // Only allow updating user-marked locations
            if (!existingLocation.isUserMarked) {
                return Result.failure(
                    CampLocationException.PermissionError("Cannot update official locations")
                )
            }
            
            val timestamp = getCurrentTimestamp()
            val updatedLocation = existingLocation.copy(
                name = campName ?: existingLocation.name,
                notes = notes ?: existingLocation.notes,
                amenities = amenities ?: existingLocation.amenities,
                tags = tags ?: existingLocation.tags,
                lastUpdated = timestamp
            )
            
            // Validate the updated location
            if (!updatedLocation.isValid()) {
                return Result.failure(
                    CampLocationException.ValidationError(listOf("Invalid location data"))
                )
            }
            
            mapRepository.updateCampLocation(updatedLocation)
            Result.success(updatedLocation)
        } catch (e: Exception) {
            Result.failure(
                CampLocationException.StorageError("Failed to update camp location: ${e.message}")
            )
        }
    }
    
    /**
     * Remove camp location.
     * 
     * @param locationId ID of the location to remove
     * @return Result indicating success or failure
     */
    suspend fun removeCampLocation(locationId: String): Result<Unit> {
        return try {
            val existingLocation = mapRepository.getCampLocationById(locationId)
                ?: return Result.failure(
                    CampLocationException.NotFound("Camp location not found")
                )
            
            // Only allow removing user-marked locations
            if (!existingLocation.isUserMarked) {
                return Result.failure(
                    CampLocationException.PermissionError("Cannot remove official locations")
                )
            }
            
            mapRepository.deleteCampLocation(locationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(
                CampLocationException.StorageError("Failed to remove camp location: ${e.message}")
            )
        }
    }
    
    /**
     * Get user's camp locations.
     * 
     * @param deviceId Device identifier
     * @return Flow of camp locations for the device
     */
    fun getUserCampLocations(deviceId: String): Flow<List<CampLocation>> {
        return mapRepository.observeCampLocationsByDevice(deviceId)
    }
    
    /**
     * Get active camp location for device.
     * 
     * @param deviceId Device identifier
     * @return Active camp location or null
     */
    suspend fun getActiveCampLocation(deviceId: String): CampLocation? {
        return mapRepository.getActiveCampLocation(deviceId)
    }
    
    /**
     * Set a camp location as active (deactivates others).
     * 
     * @param locationId ID of the location to activate
     * @return Result indicating success or failure
     */
    suspend fun setActiveCampLocation(locationId: String): Result<Unit> {
        return try {
            val location = mapRepository.getCampLocationById(locationId)
                ?: return Result.failure(
                    CampLocationException.NotFound("Camp location not found")
                )
            
            // Only allow activating user-marked locations
            if (!location.isUserMarked) {
                return Result.failure(
                    CampLocationException.PermissionError("Cannot activate official locations")
                )
            }
            
            mapRepository.setActiveCampLocation(locationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(
                CampLocationException.StorageError("Failed to set active camp location: ${e.message}")
            )
        }
    }
    
    /**
     * Validate camp location parameters.
     * 
     * @param deviceId Device identifier
     * @param latitude GPS latitude
     * @param longitude GPS longitude
     * @param campName Optional camp name
     * @return Validation result with any errors
     */
    private fun validateCampLocation(
        deviceId: String,
        latitude: Double,
        longitude: Double,
        campName: String?
    ): CampLocationValidationResult {
        val errors = mutableListOf<String>()
        
        // Validate device ID
        if (deviceId.isBlank()) {
            errors.add("Device ID is required")
        }
        
        // Validate coordinates
        if (latitude !in -90.0..90.0) {
            errors.add("Invalid latitude: must be between -90 and 90")
        }
        
        if (longitude !in -180.0..180.0) {
            errors.add("Invalid longitude: must be between -180 and 180")
        }
        
        // Validate event boundaries (AfrikaBurn Tankwa Karoo)
        if (!isWithinEventBoundary(latitude, longitude)) {
            errors.add("Location is outside event boundary")
        }
        
        // Validate camp name if provided
        campName?.let {
            if (it.length > MAX_CAMP_NAME_LENGTH) {
                errors.add("Camp name too long (max $MAX_CAMP_NAME_LENGTH characters)")
            }
            if (it.isBlank()) {
                errors.add("Camp name cannot be empty")
            }
        }
        
        return CampLocationValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Check if coordinates are within AfrikaBurn event boundary.
     * 
     * @param latitude GPS latitude
     * @param longitude GPS longitude
     * @return True if within boundary
     */
    private fun isWithinEventBoundary(latitude: Double, longitude: Double): Boolean {
        // AfrikaBurn Tankwa Karoo event boundaries
        return latitude in EVENT_MIN_LATITUDE..EVENT_MAX_LATITUDE &&
               longitude in EVENT_MIN_LONGITUDE..EVENT_MAX_LONGITUDE
    }
    
    /**
     * Mark current location as visited.
     * 
     * @param locationId ID of the location to mark as visited
     * @return Result with updated location or error
     */
    suspend fun markLocationAsVisited(locationId: String): Result<CampLocation> {
        return try {
            val location = mapRepository.getCampLocationById(locationId)
                ?: return Result.failure(
                    CampLocationException.NotFound("Camp location not found")
                )
            
            val updatedLocation = location.markAsVisited()
            mapRepository.updateCampLocation(updatedLocation)
            
            Result.success(updatedLocation)
        } catch (e: Exception) {
            Result.failure(
                CampLocationException.StorageError("Failed to mark location as visited: ${e.message}")
            )
        }
    }
    
    /**
     * Get locations near specified coordinates.
     * 
     * @param latitude Center latitude
     * @param longitude Center longitude
     * @param radiusKm Search radius in kilometers
     * @param includeOfficial Whether to include official locations
     * @return Flow of nearby camp locations
     */
    fun getNearbyLocations(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 1.0,
        includeOfficial: Boolean = false
    ): Flow<List<CampLocation>> = flow {
        try {
            // This would typically query the repository for nearby locations
            // For now, emit an empty list as the repository method isn't available
            emit(emptyList())
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    companion object {
        // AfrikaBurn Tankwa Karoo event boundaries
        private const val EVENT_MIN_LATITUDE = -32.35
        private const val EVENT_MAX_LATITUDE = -32.15
        private const val EVENT_MIN_LONGITUDE = 19.95
        private const val EVENT_MAX_LONGITUDE = 20.15
        
        // Validation limits
        private const val MAX_CAMP_NAME_LENGTH = 50
    }
}

/**
 * Validation result for camp location.
 */
data class CampLocationValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)

/**
 * Exceptions for camp location operations.
 */
sealed class CampLocationException(message: String) : Exception(message) {
    class ValidationError(val errors: List<String>) : 
        CampLocationException("Validation failed: ${errors.joinToString("; ")}")
    
    class StorageError(message: String) : CampLocationException(message)
    
    class NotFound(message: String) : CampLocationException(message)
    
    class PermissionError(message: String) : CampLocationException(message)
}