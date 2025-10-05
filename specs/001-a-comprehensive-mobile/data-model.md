# Data Model: AfrikaBurn Companion Mobile App

**Date**: 2025-09-29  
**Phase**: 1 - Data Architecture Design

## Entity Relationships Overview

```
Participant 1:1 UserPreferences
Participant 1:* PersonalScheduleItem
Participant 1:1 CampLocation (optional)

Event 1:* ThemeCamp
Event 1:* ArtInstallation
Event 1:* MutantVehicle
Event 1:* EventPerformance
Event 1:* EmergencyContact
Event 1:* ResourceLocation

Map 1:* MapPin
Map 1:* MapLayer

SyncManager 1:* ContentPackage
ContentPackage 1:* MediaAsset
```

## Core Entities

### Participant
**Purpose**: Event attendee with app preferences and personal data
```kotlin
data class Participant(
    val deviceId: String,                    // Primary key (UUID)
    val isDarkModeEnabled: Boolean = false,
    val preferredLanguage: String = "en",
    val hasCompletedOnboarding: Boolean = false,
    val lastSyncTimestamp: Long = 0,
    val batteryOptimizationEnabled: Boolean = true
)
```
**Validation**: 
- deviceId must be valid UUID format
- preferredLanguage must be supported locale code
- lastSyncTimestamp must be valid epoch time

### UserPreferences  
**Purpose**: User customization settings and privacy choices
```kotlin
data class UserPreferences(
    val deviceId: String,                    // Foreign key -> Participant
    val locationSharingEnabled: Boolean = false,
    val crashReportingEnabled: Boolean = true,
    val weatherAlertsEnabled: Boolean = true,
    val pushNotificationsEnabled: Boolean = true,
    val storageUsageLimit: Long = 2_000_000_000, // 2GB in bytes
    val autoSyncOnWifiOnly: Boolean = false
)
```

### CampLocation
**Purpose**: User's marked camp location on the map
```kotlin
data class CampLocation(
    val id: String,                          // Primary key (UUID)
    val deviceId: String,                    // Foreign key -> Participant
    val latitude: Double,
    val longitude: Double,
    val name: String?,                       // Optional camp name
    val notes: String?,                      // Optional user notes
    val markedTimestamp: Long,
    val isActive: Boolean = true
)
```
**Validation**:
- Latitude: -90.0 to 90.0
- Longitude: -180.0 to 180.0
- Must be within Tankwa Karoo geographic bounds

### Event
**Purpose**: Annual AfrikaBurn event metadata
```kotlin
data class Event(
    val id: String,                          // Primary key (UUID)
    val year: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val centerLatitude: Double,              // Event center coordinates
    val centerLongitude: Double,
    val radiusKm: Double = 5.0,              // Unlock radius
    val theme: String,
    val isCurrentYear: Boolean = false,
    val lastUpdated: Long
)
```

### ThemeCamp
**Purpose**: Organized camps with activities and amenities
```kotlin
data class ThemeCamp(
    val id: String,                          // Primary key (UUID)
    val eventId: String,                     // Foreign key -> Event
    val name: String,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val contactInfo: String?,
    val activities: List<String> = emptyList(),
    val amenities: List<String> = emptyList(),
    val qrCode: String?,
    val photoUrl: String?,
    val isHidden: Boolean = false,           // For time-released content
    val unlockTimestamp: Long?,              // When content becomes available
    val lastUpdated: Long
)
```

### ArtInstallation
**Purpose**: Artwork installations with artist details and photos  
```kotlin
data class ArtInstallation(
    val id: String,                          // Primary key (UUID)
    val eventId: String,                     // Foreign key -> Event
    val name: String,
    val artistName: String,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val photoUrls: List<String> = emptyList(),
    val artistBio: String?,
    val interactiveFeatures: List<String> = emptyList(),
    val isHidden: Boolean = false,           // For time-released content
    val unlockTimestamp: Long?,              // When content becomes available
    val qrCode: String?,
    val lastUpdated: Long
)
```

### MutantVehicle  
**Purpose**: Mobile art cars with tracking and search capabilities
```kotlin
data class MutantVehicle(
    val id: String,                          // Primary key (UUID)
    val eventId: String,                     // Foreign key -> Event
    val name: String,
    val description: String,
    val ownerName: String?,
    val photoUrls: List<String> = emptyList(),
    val scheduleInfo: String?,               // When/where it typically operates
    val lastKnownLatitude: Double?,          // Optional tracking
    val lastKnownLongitude: Double?,
    val lastLocationUpdate: Long?,
    val searchTags: List<String> = emptyList(),
    val isActive: Boolean = true,
    val lastUpdated: Long
)
```

### EventPerformance
**Purpose**: Shows, workshops, and activities with timing and location
```kotlin
data class EventPerformance(
    val id: String,                          // Primary key (UUID)
    val eventId: String,                     // Foreign key -> Event
    val name: String,
    val description: String?,
    val performerName: String?,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val latitude: Double?,                   // Optional location
    val longitude: Double?,
    val venue: String?,                      // Alternative to coordinates
    val category: String,                    // Workshop, Music, Art, etc.
    val isHidden: Boolean = false,           // For time-released content
    val lastUpdated: Long
)
```

### PersonalScheduleItem
**Purpose**: User's personal schedule with conflict detection
```kotlin
data class PersonalScheduleItem(
    val id: String,                          // Primary key (UUID)
    val deviceId: String,                    // Foreign key -> Participant  
    val eventPerformanceId: String?,         // Optional link to EventPerformance
    val customTitle: String?,                // For custom events
    val customDescription: String?,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val hasConflict: Boolean = false,        // Computed field
    val isReminder: Boolean = true,
    val createdTimestamp: Long,
    val lastUpdated: Long
)
```

## Map and Location Entities

### OfflineMap
**Purpose**: Map tiles and geographic data for offline use
```kotlin
data class OfflineMap(
    val id: String,                          // Primary key (UUID)
    val eventId: String,                     // Foreign key -> Event
    val boundaryNorthLat: Double,
    val boundarySouthLat: Double,
    val boundaryEastLng: Double,
    val boundaryWestLng: Double,
    val zoomLevels: List<Int>,               // Available zoom levels
    val tileStoragePath: String,             // Local file system path
    val sizeBytes: Long,
    val lastUpdated: Long
)
```

### MapPin
**Purpose**: Pinned locations for all map features
```kotlin
data class MapPin(
    val id: String,                          // Primary key (UUID)
    val mapId: String,                       // Foreign key -> OfflineMap
    val contentType: String,                 // "camp", "art", "facility", "emergency"
    val contentId: String,                   // Foreign key to specific content
    val latitude: Double,
    val longitude: Double,
    val iconType: String,                    // Icon identifier
    val layerId: String,                     // Which layer this pin belongs to
    val isVisible: Boolean = true,
    val priority: Int = 0                    // Display priority (higher = more important)
)
```

## Smart Sync and Content Management

### SyncManager
**Purpose**: Manages offline content synchronization
```kotlin
data class SyncManager(
    val id: String,                          // Primary key (UUID)
    val lastFullSync: Long = 0,
    val lastIncrementalSync: Long = 0,
    val totalStorageUsed: Long = 0,
    val maxStorageLimit: Long = 2_000_000_000, // 2GB
    val syncStatus: String = "idle",         // idle, syncing, error
    val errorMessage: String? = null,
    val networkType: String? = null          // wifi, cellular, none
)
```

### ContentPackage
**Purpose**: Grouped content for efficient downloading and storage management
```kotlin
data class ContentPackage(
    val id: String,                          // Primary key (UUID)
    val name: String,                        // e.g., "Safety Info", "Maps", "Art Images"
    val priority: Int,                       // Storage priority (1=highest)
    val sizeBytes: Long,
    val version: Int,
    val isDownloaded: Boolean = false,
    val downloadProgress: Float = 0f,
    val lastDownloaded: Long = 0,
    val expiryDate: Long?                    // Optional expiry for time-sensitive content
)
```

### MediaAsset
**Purpose**: Images and multimedia content for offline access
```kotlin
data class MediaAsset(
    val id: String,                          // Primary key (UUID)
    val contentPackageId: String,            // Foreign key -> ContentPackage
    val fileName: String,
    val localPath: String?,                  // Local file system path
    val remoteUrl: String,
    val mimeType: String,
    val sizeBytes: Long,
    val isDownloaded: Boolean = false,
    val compressionLevel: String = "medium", // low, medium, high
    val lastAccessed: Long = 0               // For LRU cache management
)
```

## Safety and Emergency

### EmergencyContact
**Purpose**: Rangers, medical facilities, and emergency services
```kotlin
data class EmergencyContact(
    val id: String,                          // Primary key (UUID)
    val eventId: String,                     // Foreign key -> Event
    val name: String,
    val contactType: String,                 // ranger, medical, emergency, admin
    val phoneNumber: String?,
    val radioChannel: String?,
    val latitude: Double?,                   // Optional location
    val longitude: Double?,
    val description: String?,
    val isAvailable24Hours: Boolean = false,
    val operatingHours: String?,
    val priority: Int = 0                    // Display priority
)
```

### ResourceLocation
**Purpose**: Water points, ice vendors, help stations
```kotlin
data class ResourceLocation(
    val id: String,                          // Primary key (UUID)
    val eventId: String,                     // Foreign key -> Event
    val name: String,
    val resourceType: String,                // water, ice, help, medical, food
    val latitude: Double,
    val longitude: Double,
    val availability: String = "unknown",    // available, limited, unavailable
    val lastStatusUpdate: Long = 0,
    val operatingHours: String?,
    val description: String?,
    val isVerified: Boolean = false
)
```

## Environmental and Community

### MOOPReport
**Purpose**: Matter Out of Place tracking and reporting
```kotlin
data class MOOPReport(
    val id: String,                          // Primary key (UUID)
    val deviceId: String,                    // Foreign key -> Participant
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val photoPath: String?,                  // Local photo path
    val severity: String,                    // low, medium, high
    val status: String = "reported",         // reported, in_progress, resolved
    val reportedTimestamp: Long,
    val isSynced: Boolean = false,           // Offline sync status
    val lastUpdated: Long
)
```

### WeatherAlert
**Purpose**: Weather information and alerts for desert conditions
```kotlin
data class WeatherAlert(
    val id: String,                          // Primary key (UUID)
    val alertType: String,                   // dust_storm, high_wind, extreme_heat
    val severity: String,                    // low, medium, high, extreme
    val title: String,
    val description: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime?,
    val isActive: Boolean = true,
    val lastUpdated: Long
)
```

## State Transitions and Lifecycle

### Content Unlock States
1. **Hidden**: Content exists but not visible to user
2. **Location Unlocked**: User is within event boundary, content immediately available
3. **Time Unlocked**: User outside boundary, content available based on schedule
4. **Fully Available**: No restrictions, content accessible

### Sync States  
1. **Idle**: No sync activity
2. **Pending**: Sync requested, waiting for network
3. **Active**: Currently syncing data
4. **Complete**: Sync finished successfully
5. **Error**: Sync failed, retry required

### Storage Management
1. **Monitor**: Track storage usage vs 2GB limit
2. **Prioritize**: Apply priority order when approaching limit
3. **Cleanup**: Remove low-priority cached content
4. **Compress**: Apply compression to media assets

## Data Volume Estimates

- **Total Events**: ~50 per year
- **Theme Camps**: ~200 per event  
- **Art Installations**: ~150 per event
- **Mutant Vehicles**: ~100 per event
- **Performances**: ~500 per event
- **Media Assets**: ~2000 images/videos (within 2GB limit)
- **Map Tiles**: ~500MB for complete offline coverage
- **Expected Users**: 5000+ participants

**Storage Allocation**:
- Maps & Navigation: 500MB (25%)
- Safety Information: 100MB (5%)  
- Static Content: 300MB (15%)
- Community Features: 600MB (30%)
- Event Schedule: 500MB (25%)

All percentages align with constitutional storage prioritization requirements.