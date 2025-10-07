package io.asterixorobelix.afrikaburn.domain.usecase

import io.asterixorobelix.afrikaburn.domain.model.MOOPReport
import io.asterixorobelix.afrikaburn.domain.model.MOOPSeverity
import io.asterixorobelix.afrikaburn.domain.model.MOOPStatus
import io.asterixorobelix.afrikaburn.domain.model.MOOPType
import io.asterixorobelix.afrikaburn.domain.model.getCurrentTimestamp
import io.asterixorobelix.afrikaburn.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Use case for reporting MOOP (Matter Out Of Place) incidents
 * 
 * Handles:
 * - MOOP report creation with GPS location
 * - Photo attachment support
 * - Offline storage and sync queue management
 * - Field validation
 * - Network availability detection
 */
class ReportMOOPUseCase(
    private val moopRepository: MOOPRepository,
    private val syncRepository: SyncRepository,
    private val deviceIdProvider: DeviceIdProvider
) {
    
    /**
     * Report MOOP incident
     * 
     * @param latitude GPS latitude of the MOOP location
     * @param longitude GPS longitude of the MOOP location
     * @param description Required description of the MOOP
     * @param severity Required severity level of the MOOP
     * @param moopType Type of MOOP (defaults to OTHER)
     * @param photoUrl Optional photo attachment URL
     * @param estimatedCleanupTime Optional estimated cleanup time in minutes
     * @param requiresSpecialEquipment Whether special equipment is needed for cleanup
     * @param isHazardous Whether the MOOP is hazardous material
     * @param notes Optional additional notes
     * @return Flow emitting ReportResult with report ID and sync status
     */
    @OptIn(ExperimentalUuidApi::class)
    fun reportMOOP(
        latitude: Double,
        longitude: Double,
        description: String,
        severity: MOOPSeverity,
        moopType: MOOPType = MOOPType.OTHER,
        photoUrl: String? = null,
        estimatedCleanupTime: Int? = null,
        requiresSpecialEquipment: Boolean = false,
        isHazardous: Boolean = false,
        notes: String? = null
    ): Flow<ReportResult> = flow {
        // Emit initial validation state
        emit(ReportResult.Validating)
        
        // Validate required fields
        val validationErrors = validateReport(
            description = description,
            severity = severity,
            latitude = latitude,
            longitude = longitude
        )
        
        if (validationErrors.isNotEmpty()) {
            emit(ReportResult.ValidationFailed(validationErrors))
            return@flow
        }
        
        // Create report
        val reportId = Uuid.random().toString()
        val timestamp = getCurrentTimestamp()
        
        val report = MOOPReport(
            id = reportId,
            deviceId = deviceIdProvider.getDeviceId(),
            latitude = latitude,
            longitude = longitude,
            description = description.trim(),
            severity = severity,
            moopType = moopType,
            photoUrl = photoUrl,
            status = MOOPStatus.REPORTED,
            reportedTimestamp = timestamp,
            resolvedTimestamp = null,
            resolvedBy = null,
            estimatedCleanupTime = estimatedCleanupTime,
            requiresSpecialEquipment = requiresSpecialEquipment,
            isHazardous = isHazardous,
            notes = notes?.trim(),
            lastUpdated = timestamp
        )
        
        // Save to local storage
        emit(ReportResult.SavingLocally)
        
        try {
            moopRepository.saveMOOPReport(report)
            emit(ReportResult.SavedLocally(reportId))
        } catch (e: Exception) {
            emit(ReportResult.Failed("Failed to save report locally: ${e.message}"))
            return@flow
        }
        
        // Check network availability
        val isNetworkAvailable = syncRepository.isNetworkAvailable()
        
        if (isNetworkAvailable) {
            // Try immediate sync
            emit(ReportResult.Syncing(reportId))
            
            try {
                val syncResult = moopRepository.syncMOOPReport(reportId)
                if (syncResult.success) {
                    emit(ReportResult.Success(
                        reportId = reportId,
                        syncStatus = SyncStatus.SYNCED,
                        message = "MOOP report submitted successfully"
                    ))
                } else {
                    // Failed to sync but saved locally
                    queueForSync(reportId)
                    emit(ReportResult.Success(
                        reportId = reportId,
                        syncStatus = SyncStatus.QUEUED,
                        message = "MOOP report saved and queued for sync"
                    ))
                }
            } catch (e: Exception) {
                // Network error - queue for later sync
                queueForSync(reportId)
                emit(ReportResult.Success(
                    reportId = reportId,
                    syncStatus = SyncStatus.QUEUED,
                    message = "MOOP report saved offline and will sync when network is available"
                ))
            }
        } else {
            // No network - queue for later sync
            queueForSync(reportId)
            emit(ReportResult.Success(
                reportId = reportId,
                syncStatus = SyncStatus.QUEUED,
                message = "MOOP report saved offline and will sync when network is available"
            ))
        }
    }
    
    /**
     * Validate report fields
     */
    private fun validateReport(
        description: String,
        severity: MOOPSeverity,
        latitude: Double,
        longitude: Double
    ): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        
        // Validate description
        when {
            description.isBlank() -> {
                errors.add(ValidationError.MISSING_DESCRIPTION)
            }
            description.length < 10 -> {
                errors.add(ValidationError.DESCRIPTION_TOO_SHORT)
            }
            description.length > 500 -> {
                errors.add(ValidationError.DESCRIPTION_TOO_LONG)
            }
        }
        
        // Validate GPS coordinates
        if (latitude !in -90.0..90.0) {
            errors.add(ValidationError.INVALID_LATITUDE)
        }
        
        if (longitude !in -180.0..180.0) {
            errors.add(ValidationError.INVALID_LONGITUDE)
        }
        
        // Check if coordinates are within Tankwa Karoo region (approximate bounds)
        if (!isInTankwaKaroo(latitude, longitude)) {
            errors.add(ValidationError.LOCATION_OUT_OF_BOUNDS)
        }
        
        return errors
    }
    
    /**
     * Check if coordinates are within Tankwa Karoo region
     * Approximate bounds for the area
     */
    private fun isInTankwaKaroo(latitude: Double, longitude: Double): Boolean {
        // Tankwa Karoo approximate boundaries
        val minLat = -32.8
        val maxLat = -32.2
        val minLon = 19.7
        val maxLon = 20.3
        
        return latitude in minLat..maxLat && longitude in minLon..maxLon
    }
    
    /**
     * Queue report for sync when network is available
     */
    private suspend fun queueForSync(reportId: String) {
        val syncManager = syncRepository.getSyncManager()
        syncManager?.let {
            val updatedManager = it.copy(
                pendingSyncItems = it.pendingSyncItems + "moop:$reportId",
                lastUpdated = getCurrentTimestamp()
            )
            syncRepository.updateSyncManager(updatedManager)
        }
    }
}

/**
 * Result of MOOP report operation
 */
sealed class ReportResult {
    object Validating : ReportResult()
    object SavingLocally : ReportResult()
    
    data class ValidationFailed(
        val errors: List<ValidationError>
    ) : ReportResult()
    
    data class SavedLocally(
        val reportId: String
    ) : ReportResult()
    
    data class Syncing(
        val reportId: String
    ) : ReportResult()
    
    data class Success(
        val reportId: String,
        val syncStatus: SyncStatus,
        val message: String
    ) : ReportResult()
    
    data class Failed(
        val error: String
    ) : ReportResult()
}

/**
 * Sync status for MOOP reports
 */
enum class SyncStatus {
    SYNCED,    // Successfully synced with server
    QUEUED,    // Queued for sync when network available
    FAILED     // Sync failed (will retry)
}

/**
 * Validation errors for MOOP reports
 */
enum class ValidationError(val message: String) {
    MISSING_DESCRIPTION("Description is required"),
    DESCRIPTION_TOO_SHORT("Description must be at least 10 characters"),
    DESCRIPTION_TOO_LONG("Description must be less than 500 characters"),
    INVALID_LATITUDE("Invalid GPS latitude"),
    INVALID_LONGITUDE("Invalid GPS longitude"),
    LOCATION_OUT_OF_BOUNDS("Location is outside the Tankwa Karoo region")
}

/**
 * Interface for MOOP repository operations
 */
interface MOOPRepository {
    suspend fun saveMOOPReport(report: MOOPReport)
    suspend fun syncMOOPReport(reportId: String): SyncResult
    suspend fun getMOOPReport(reportId: String): MOOPReport?
    suspend fun getAllMOOPReports(): List<MOOPReport>
    suspend fun getPendingSyncReports(): List<MOOPReport>
    suspend fun updateMOOPReport(report: MOOPReport)
    suspend fun deleteMOOPReport(reportId: String)
}

/**
 * Data class for sync results
 */
data class SyncResult(
    val success: Boolean,
    val error: String? = null
)

/**
 * Interface for device ID provider
 */
interface DeviceIdProvider {
    fun getDeviceId(): String
}