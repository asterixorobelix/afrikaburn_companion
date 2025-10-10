package io.asterixorobelix.afrikaburn.models

data class MapLocation(
    val id: String,
    val name: String,
    val type: MapLocationType,
    val coordinates: MapCoordinates,
    val description: String? = null,
    val subType: String? = null,
    val tags: List<String> = emptyList()
)

enum class MapLocationType {
    CAMP,
    ART,
    FACILITY,
    EMERGENCY
}

data class MapCoordinates(
    val x: Float,
    val y: Float
)