package io.asterixorobelix.afrikaburn.domain.model

/**
 * Domain model representing resource locations at AfrikaBurn
 * 
 * Critical resources like water, food, medical services, and sanitation
 * facilities essential for survival in the harsh Tankwa Karoo desert.
 */
data class ResourceLocation(
    val id: String,
    val name: String,
    val description: String?,
    val resourceType: ResourceType,
    val latitude: Double,
    val longitude: Double,
    val isOfficial: Boolean = true,
    val isActive: Boolean = true,
    val capacity: Int?,
    val currentLoad: Int?,
    val operatingHours: String?,
    val contact: String?,
    val amenities: List<String> = emptyList(),
    val accessibility: List<String> = emptyList(),
    val requirements: List<String> = emptyList(),
    val pricing: String?,
    val lastUpdated: Long,
    val createdAt: Long
) {
    companion object {
        val CRITICAL_RESOURCES = listOf(
            ResourceType.WATER,
            ResourceType.MEDICAL,
            ResourceType.EMERGENCY
        )
    }
    
    fun isValid(): Boolean {
        return id.isNotBlank() &&
               name.isNotBlank() &&
               latitude in -90.0..90.0 &&
               longitude in -180.0..180.0 &&
               (capacity == null || capacity > 0) &&
               (currentLoad == null || currentLoad >= 0)
    }
    
    fun isCritical(): Boolean = resourceType in CRITICAL_RESOURCES
    fun isAvailable(): Boolean = isActive && (capacity == null || (currentLoad ?: 0) < capacity)
    fun getCapacityPercentage(): Int? {
        return if (capacity != null && currentLoad != null) {
            ((currentLoad.toDouble() / capacity) * 100).toInt()
        } else null
    }
}

enum class ResourceType {
    WATER, FOOD, MEDICAL, TOILETS, SHOWERS, FUEL, CHARGING, 
    WIFI, EMERGENCY, INFORMATION, TOOLS, WORKSHOP, OTHER
}