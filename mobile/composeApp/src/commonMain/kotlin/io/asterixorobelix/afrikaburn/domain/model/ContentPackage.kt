package io.asterixorobelix.afrikaburn.domain.model

/**
 * Domain model for packaged content for offline access
 */
data class ContentPackage(
    val id: String,
    val name: String,
    val description: String?,
    val packageType: ContentPackageType,
    val version: String,
    val sizeBytes: Long,
    val priority: PackagePriority,
    val contentIds: List<String> = emptyList(),
    val downloadStatus: DownloadStatus = DownloadStatus.NOT_DOWNLOADED,
    val downloadProgress: Float = 0f,
    val isRequired: Boolean = false,
    val expirationTime: Long?,
    val lastDownloaded: Long?,
    val checksum: String?,
    val downloadUrl: String?,
    val unlockedAt: Long?,
    val unlockRequirements: List<String> = emptyList(),
    val dependencies: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val createdAt: Long,
    val lastUpdated: Long
) {
    companion object {
        const val MAX_PACKAGE_SIZE = 500_000_000L // 500MB per package
    }
    
    fun isValid(): Boolean {
        return id.isNotBlank() &&
               name.isNotBlank() &&
               version.isNotBlank() &&
               sizeBytes > 0 &&
               sizeBytes <= MAX_PACKAGE_SIZE
    }
    
    fun isDownloaded(): Boolean = downloadStatus == DownloadStatus.COMPLETED
    fun isExpired(): Boolean = expirationTime?.let { it < getCurrentTimestamp() } ?: false
    fun isUnlocked(): Boolean = unlockedAt != null
    fun canDownload(): Boolean = downloadStatus == DownloadStatus.NOT_DOWNLOADED && !isExpired()
    fun getDownloadProgressPercent(): Int = (downloadProgress * 100).toInt()
}

enum class ContentPackageType {
    ESSENTIAL_DATA, MAPS, MEDIA, ART_CONTENT, THEME_CAMP_INFO, 
    PERFORMANCE_SCHEDULES, SAFETY_INFO, OFFLINE_MAPS, OTHER
}

enum class PackagePriority { CRITICAL, HIGH, NORMAL, LOW }