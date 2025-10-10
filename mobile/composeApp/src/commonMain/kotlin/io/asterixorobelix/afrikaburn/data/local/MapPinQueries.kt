package io.asterixorobelix.afrikaburn.data.local

import io.asterixorobelix.afrikaburn.data.repository.DatabaseMapPin

/**
 * Mock SQLDelight queries for MapPin
 * Placeholder implementation for compilation
 */
class MapPinQueries {
    
    fun selectMapPinsByMap(mapId: String): MockQuery<DatabaseMapPin> {
        return MockQuery<DatabaseMapPin>(emptyList<DatabaseMapPin>())
    }
    
    fun selectMapPinsByContentType(mapId: String, contentType: String): MockQuery<DatabaseMapPin> {
        return MockQuery<DatabaseMapPin>(emptyList<DatabaseMapPin>())
    }
    
    fun selectMapPinsNearLocation(mapId: String, latitude: Double, longitude: Double, radiusKm: Double): MockQuery<DatabaseMapPin> {
        return MockQuery<DatabaseMapPin>(emptyList<DatabaseMapPin>())
    }
    
    fun insertMapPin(
        id: String,
        mapId: String,
        contentType: String,
        contentId: String,
        latitude: Double,
        longitude: Double,
        iconType: String,
        layerId: String,
        isVisible: Long,
        priority: Long
    ) {
        // Mock implementation
    }
    
    fun updateMapPin(
        mapId: String,
        contentType: String,
        contentId: String,
        latitude: Double,
        longitude: Double,
        iconType: String,
        layerId: String,
        isVisible: Long,
        priority: Long,
        id: String
    ) {
        // Mock implementation
    }
    
    fun deleteMapPin(pinId: String) {
        // Mock implementation
    }
    
    fun deleteMapPinsByContentId(contentId: String) {
        // Mock implementation
    }
    
    fun transaction(body: () -> Unit) {
        body()
    }
}