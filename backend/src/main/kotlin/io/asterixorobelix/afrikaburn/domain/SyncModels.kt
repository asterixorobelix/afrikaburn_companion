package io.asterixorobelix.afrikaburn.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request model for full sync operation.
 * Follows the OpenAPI specification for /sync/full endpoint.
 */
@Serializable
data class SyncRequest(
    @SerialName("device_id")
    val deviceId: String,
    
    @SerialName("event_id")
    val eventId: String,
    
    @SerialName("max_storage_bytes")
    val maxStorageBytes: Long = 2_000_000_000L, // 2GB default
    
    @SerialName("priority_packages")
    val priorityPackages: List<String>? = null,
    
    @SerialName("last_sync_timestamp")
    val lastSyncTimestamp: Long = 0L
)

/**
 * Request model for incremental sync operation.
 * Follows the OpenAPI specification for /sync/incremental endpoint.
 */
@Serializable
data class IncrementalSyncRequest(
    @SerialName("device_id")
    val deviceId: String,
    
    @SerialName("event_id")
    val eventId: String,
    
    @SerialName("last_sync_timestamp")
    val lastSyncTimestamp: Long
)

/**
 * Response model for both full and incremental sync operations.
 * Follows the OpenAPI specification for SyncResponse.
 */
@Serializable
data class SyncResponse(
    @SerialName("sync_id")
    val syncId: String,
    
    @SerialName("total_size_bytes")
    val totalSizeBytes: Long,
    
    @SerialName("content_packages")
    val contentPackages: List<ContentPackage>,
    
    @SerialName("download_urls")
    val downloadUrls: Map<String, String>? = null
)

/**
 * Represents a content package for sync operations.
 * Follows the OpenAPI specification for ContentPackage.
 */
@Serializable
data class ContentPackage(
    val id: String,
    
    val name: String,
    
    val priority: Int,
    
    @SerialName("size_bytes")
    val sizeBytes: Long,
    
    val version: Int,
    
    @SerialName("expiry_date")
    val expiryDate: Long? = null
)