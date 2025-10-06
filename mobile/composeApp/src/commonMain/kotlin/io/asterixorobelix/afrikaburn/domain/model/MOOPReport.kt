package io.asterixorobelix.afrikaburn.domain.model

/**
 * Domain model for MOOP (Matter Out Of Place) reports
 * 
 * Supporting Leave No Trace principles in the Tankwa Karoo desert.
 */
data class MOOPReport(
    val id: String,
    val deviceId: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val severity: MOOPSeverity,
    val moopType: MOOPType,
    val photoUrl: String?,
    val status: MOOPStatus = MOOPStatus.REPORTED,
    val reportedTimestamp: Long,
    val resolvedTimestamp: Long?,
    val resolvedBy: String?,
    val estimatedCleanupTime: Int?, // minutes
    val requiresSpecialEquipment: Boolean = false,
    val isHazardous: Boolean = false,
    val notes: String?,
    val lastUpdated: Long
) {
    fun isValid(): Boolean {
        return id.isNotBlank() &&
               deviceId.isNotBlank() &&
               latitude in -90.0..90.0 &&
               longitude in -180.0..180.0 &&
               description.isNotBlank() &&
               reportedTimestamp > 0
    }
    
    fun isResolved(): Boolean = status == MOOPStatus.RESOLVED
    fun requiresUrgentAction(): Boolean = severity == MOOPSeverity.HIGH || isHazardous
    fun getAgeInHours(): Long = (getCurrentTimestamp() - reportedTimestamp) / (60 * 60 * 1000)
}

enum class MOOPSeverity { LOW, MEDIUM, HIGH }
enum class MOOPStatus { REPORTED, IN_PROGRESS, RESOLVED, CANCELLED }
enum class MOOPType {
    TRASH, RECYCLING, HAZARDOUS, FOOD_WASTE, HUMAN_WASTE,
    CONSTRUCTION_DEBRIS, VEHICLE_FLUIDS, ELECTRONICS, OTHER
}