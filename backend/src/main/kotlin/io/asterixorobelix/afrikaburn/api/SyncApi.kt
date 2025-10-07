package io.asterixorobelix.afrikaburn.api

import io.asterixorobelix.afrikaburn.domain.ContentPackage
import io.asterixorobelix.afrikaburn.domain.ErrorResponse
import io.asterixorobelix.afrikaburn.domain.IncrementalSyncRequest
import io.asterixorobelix.afrikaburn.domain.SyncRequest
import io.asterixorobelix.afrikaburn.domain.SyncResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID

/**
 * Smart sync API endpoints implementation following the OpenAPI specification.
 * Provides both full and incremental sync capabilities with prioritized content delivery.
 */
fun Route.syncApi() {
    route("/api/v1/sync") {
        
        // POST /sync/full - Full content sync with 2GB limit logic
        post("/full") {
            try {
                val request = call.receive<SyncRequest>()
                
                // Validate request
                val validationError = validateSyncRequest(request)
                if (validationError != null) {
                    call.respond(HttpStatusCode.BadRequest, validationError)
                    return@post
                }
                
                // Check if requested storage exceeds 2GB limit
                if (request.maxStorageBytes > 2_000_000_000L) {
                    call.respond(
                        HttpStatusCode.PayloadTooLarge,
                        ErrorResponse(
                            error = "payload_too_large",
                            message = "Requested content exceeds 2GB limit",
                            details = mapOf(
                                "requested_bytes" to request.maxStorageBytes.toString(),
                                "max_allowed_bytes" to "2000000000"
                            )
                        )
                    )
                    return@post
                }
                
                // Generate sync response with prioritized content packages
                val syncResponse = generateFullSyncResponse(request)
                
                call.respond(HttpStatusCode.OK, syncResponse)
                
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        error = "internal_server_error",
                        message = "An error occurred during sync operation"
                    )
                )
            }
        }
        
        // POST /sync/incremental - Incremental updates since last sync
        post("/incremental") {
            try {
                val request = call.receive<IncrementalSyncRequest>()
                
                // Validate request
                val validationError = validateIncrementalSyncRequest(request)
                if (validationError != null) {
                    call.respond(HttpStatusCode.BadRequest, validationError)
                    return@post
                }
                
                // Generate incremental sync response
                val syncResponse = generateIncrementalSyncResponse(request)
                
                call.respond(HttpStatusCode.OK, syncResponse)
                
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        error = "internal_server_error",
                        message = "An error occurred during incremental sync"
                    )
                )
            }
        }
    }
}

/**
 * Validates the full sync request.
 * Returns an error response if validation fails, null if valid.
 */
private fun validateSyncRequest(request: SyncRequest): ErrorResponse? {
    // Validate device_id is valid UUID
    try {
        UUID.fromString(request.deviceId)
    } catch (e: IllegalArgumentException) {
        return ErrorResponse(
            error = "validation_error",
            message = "Invalid device_id format. Must be a valid UUID.",
            details = mapOf("field" to "device_id", "value" to request.deviceId)
        )
    }
    
    // Validate event_id is valid UUID
    try {
        UUID.fromString(request.eventId)
    } catch (e: IllegalArgumentException) {
        return ErrorResponse(
            error = "validation_error",
            message = "Invalid event_id format. Must be a valid UUID.",
            details = mapOf("field" to "event_id", "value" to request.eventId)
        )
    }
    
    // Validate max_storage_bytes is positive
    if (request.maxStorageBytes <= 0) {
        return ErrorResponse(
            error = "validation_error",
            message = "max_storage_bytes must be a positive value",
            details = mapOf("field" to "max_storage_bytes", "value" to request.maxStorageBytes.toString())
        )
    }
    
    return null
}

/**
 * Validates the incremental sync request.
 * Returns an error response if validation fails, null if valid.
 */
private fun validateIncrementalSyncRequest(request: IncrementalSyncRequest): ErrorResponse? {
    // Validate device_id is valid UUID
    try {
        UUID.fromString(request.deviceId)
    } catch (e: IllegalArgumentException) {
        return ErrorResponse(
            error = "validation_error",
            message = "Invalid device_id format. Must be a valid UUID.",
            details = mapOf("field" to "device_id", "value" to request.deviceId)
        )
    }
    
    // Validate event_id is valid UUID
    try {
        UUID.fromString(request.eventId)
    } catch (e: IllegalArgumentException) {
        return ErrorResponse(
            error = "validation_error",
            message = "Invalid event_id format. Must be a valid UUID.",
            details = mapOf("field" to "event_id", "value" to request.eventId)
        )
    }
    
    // Validate last_sync_timestamp is not negative
    if (request.lastSyncTimestamp < 0) {
        return ErrorResponse(
            error = "validation_error",
            message = "last_sync_timestamp must be non-negative",
            details = mapOf("field" to "last_sync_timestamp", "value" to request.lastSyncTimestamp.toString())
        )
    }
    
    return null
}

/**
 * Generates a full sync response with prioritized content packages.
 * Mock implementation for now - would query database in production.
 */
private fun generateFullSyncResponse(request: SyncRequest): SyncResponse {
    val syncId = UUID.randomUUID().toString()
    val packages = mutableListOf<ContentPackage>()
    var totalSize = 0L
    val maxSize = request.maxStorageBytes
    
    // Define available content packages with priorities
    val availablePackages = listOf(
        // Priority 1 - Safety (highest)
        ContentPackage(
            id = UUID.randomUUID().toString(),
            name = "safety",
            priority = 1,
            sizeBytes = 50_000_000L, // 50MB
            version = 1,
            expiryDate = null
        ),
        // Priority 2 - Maps
        ContentPackage(
            id = UUID.randomUUID().toString(),
            name = "maps",
            priority = 2,
            sizeBytes = 200_000_000L, // 200MB
            version = 2,
            expiryDate = null
        ),
        // Priority 3 - Static content
        ContentPackage(
            id = UUID.randomUUID().toString(),
            name = "static",
            priority = 3,
            sizeBytes = 100_000_000L, // 100MB
            version = 1,
            expiryDate = null
        ),
        // Priority 4 - Theme camps
        ContentPackage(
            id = UUID.randomUUID().toString(),
            name = "theme_camps",
            priority = 4,
            sizeBytes = 300_000_000L, // 300MB
            version = 3,
            expiryDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L) // 7 days
        ),
        // Priority 5 - Art installations
        ContentPackage(
            id = UUID.randomUUID().toString(),
            name = "art_installations",
            priority = 5,
            sizeBytes = 400_000_000L, // 400MB
            version = 2,
            expiryDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)
        ),
        // Priority 6 - Performances
        ContentPackage(
            id = UUID.randomUUID().toString(),
            name = "performances",
            priority = 6,
            sizeBytes = 150_000_000L, // 150MB
            version = 1,
            expiryDate = System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000L) // 3 days
        ),
        // Priority 7 - Media
        ContentPackage(
            id = UUID.randomUUID().toString(),
            name = "media",
            priority = 7,
            sizeBytes = 800_000_000L, // 800MB
            version = 1,
            expiryDate = System.currentTimeMillis() + (14 * 24 * 60 * 60 * 1000L) // 14 days
        )
    )
    
    // Sort packages by priority (ascending, 1 = highest priority)
    val sortedPackages = availablePackages.sortedBy { it.priority }
    
    // If priority packages are specified, reorder to put them first
    val prioritizedPackages = if (request.priorityPackages != null) {
        val prioritySet = request.priorityPackages.toSet()
        sortedPackages.sortedWith(compareBy(
            { it.name !in prioritySet }, // Priority packages first
            { it.priority } // Then by numeric priority
        ))
    } else {
        sortedPackages
    }
    
    // Add packages until we reach the storage limit
    for (pkg in prioritizedPackages) {
        if (totalSize + pkg.sizeBytes <= maxSize) {
            packages.add(pkg)
            totalSize += pkg.sizeBytes
        }
    }
    
    // Generate download URLs for included packages
    val downloadUrls = packages.associate { pkg ->
        pkg.id to "https://afrikaburn-companion.supabase.co/api/v1/download/${pkg.id}"
    }
    
    return SyncResponse(
        syncId = syncId,
        totalSizeBytes = totalSize,
        contentPackages = packages,
        downloadUrls = downloadUrls
    )
}

/**
 * Generates an incremental sync response with updates since last sync.
 * Mock implementation for now - would query database for changes in production.
 */
private fun generateIncrementalSyncResponse(request: IncrementalSyncRequest): SyncResponse {
    val syncId = UUID.randomUUID().toString()
    val packages = mutableListOf<ContentPackage>()
    var totalSize = 0L
    
    // Mock implementation: return updates based on timestamp
    // In production, would query for actual changes since last_sync_timestamp
    
    val currentTime = System.currentTimeMillis()
    val timeSinceLastSync = currentTime - request.lastSyncTimestamp
    
    // If last sync was more than 1 day ago, include some updates
    if (timeSinceLastSync > 24 * 60 * 60 * 1000L) {
        // Updated safety information
        packages.add(
            ContentPackage(
                id = UUID.randomUUID().toString(),
                name = "safety_updates",
                priority = 1,
                sizeBytes = 5_000_000L, // 5MB
                version = 2,
                expiryDate = null
            )
        )
        totalSize += 5_000_000L
        
        // New performances added
        packages.add(
            ContentPackage(
                id = UUID.randomUUID().toString(),
                name = "performances_delta",
                priority = 6,
                sizeBytes = 20_000_000L, // 20MB
                version = 2,
                expiryDate = currentTime + (3 * 24 * 60 * 60 * 1000L)
            )
        )
        totalSize += 20_000_000L
    }
    
    // If last sync was more than 3 days ago, include more updates
    if (timeSinceLastSync > 3 * 24 * 60 * 60 * 1000L) {
        // Theme camp updates
        packages.add(
            ContentPackage(
                id = UUID.randomUUID().toString(),
                name = "theme_camps_delta",
                priority = 4,
                sizeBytes = 30_000_000L, // 30MB
                version = 4,
                expiryDate = currentTime + (7 * 24 * 60 * 60 * 1000L)
            )
        )
        totalSize += 30_000_000L
    }
    
    // Generate download URLs for included packages
    val downloadUrls = if (packages.isNotEmpty()) {
        packages.associate { pkg ->
            pkg.id to "https://afrikaburn-companion.supabase.co/api/v1/download/${pkg.id}"
        }
    } else {
        null
    }
    
    return SyncResponse(
        syncId = syncId,
        totalSizeBytes = totalSize,
        contentPackages = packages,
        downloadUrls = downloadUrls
    )
}