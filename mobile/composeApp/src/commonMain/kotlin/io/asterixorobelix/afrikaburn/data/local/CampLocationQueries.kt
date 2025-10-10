package io.asterixorobelix.afrikaburn.data.local

import io.asterixorobelix.afrikaburn.data.repository.DatabaseCampLocation

/**
 * Mock SQLDelight queries for CampLocation
 * Placeholder implementation for compilation
 */
class CampLocationQueries {
    
    fun selectCampLocationsByDevice(deviceId: String): MockQuery<DatabaseCampLocation> {
        return MockQuery<DatabaseCampLocation>(emptyList<DatabaseCampLocation>())
    }
    
    fun selectCampLocationById(locationId: String): MockQuery<DatabaseCampLocation?> {
        return MockQuery<DatabaseCampLocation?>(null)
    }
    
    fun selectActiveCampLocation(deviceId: String): MockQuery<DatabaseCampLocation?> {
        return MockQuery<DatabaseCampLocation?>(null)
    }
    
    fun insertCampLocation(
        id: String,
        deviceId: String,
        latitude: Double,
        longitude: Double,
        name: String?,
        notes: String?,
        markedTimestamp: Long,
        isActive: Long
    ) {
        // Mock implementation
    }
    
    fun updateCampLocation(
        deviceId: String,
        latitude: Double,
        longitude: Double,
        name: String?,
        notes: String?,
        markedTimestamp: Long,
        isActive: Long,
        id: String
    ) {
        // Mock implementation
    }
    
    fun setActiveCampLocation(locationId: String) {
        // Mock implementation
    }
    
    fun deleteCampLocation(locationId: String) {
        // Mock implementation
    }
    
    fun transaction(body: () -> Unit) {
        body()
    }
}