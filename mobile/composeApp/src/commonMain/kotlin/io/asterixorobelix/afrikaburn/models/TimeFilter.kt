package io.asterixorobelix.afrikaburn.models

/**
 * Enum representing different time-based filter options for camps
 */
enum class TimeFilter(val displayName: String) {
    ALL("All Times"),
    DAYTIME("Daytime"),
    NIGHTTIME("Nighttime")
}