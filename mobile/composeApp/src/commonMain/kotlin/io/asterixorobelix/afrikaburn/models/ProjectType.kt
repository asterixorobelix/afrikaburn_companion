package io.asterixorobelix.afrikaburn.models

enum class ProjectType(val fileName: String, val displayName: String) {
    ART("WTFArtworks.json", "Art"),
    PERFORMANCES("WTFPerformances.json", "Performances"),
    EVENTS("WTFEvents.json", "Events"),
    MOBILE_ART("WTFRovingArtworks.json", "Mobile Art"),
    VEHICLES("WTFMutantVehicles.json", "Vehicles"),
    CAMPS("WTFThemeCamps.json", "Camps")
}