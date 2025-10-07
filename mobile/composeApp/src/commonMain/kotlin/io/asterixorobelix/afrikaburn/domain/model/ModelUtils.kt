package io.asterixorobelix.afrikaburn.domain.model

import kotlinx.datetime.Clock

/**
 * Utility functions for domain models that handle Kotlin Multiplatform compatibility
 */

/**
 * Get current timestamp in milliseconds (KMP compatible)
 */
fun getCurrentTimestamp(): Long {
    return Clock.System.now().toEpochMilliseconds()
}

/**
 * Convert degrees to radians (KMP compatible)
 */
fun toRadians(degrees: Double): Double {
    return degrees * kotlin.math.PI / 180.0
}

/**
 * Format double to string with specified decimal places (KMP compatible)
 */
fun Double.formatToDecimalPlaces(decimalPlaces: Int): String {
    val multiplier = when (decimalPlaces) {
        0 -> 1.0
        1 -> 10.0
        2 -> 100.0
        3 -> 1000.0
        else -> {
            var result = 1.0
            repeat(decimalPlaces) { result *= 10.0 }
            result
        }
    }
    val rounded = kotlin.math.round(this * multiplier) / multiplier
    return rounded.toString()
}

/**
 * Calculate distance between two coordinates using Haversine formula (KMP compatible)
 */
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadiusKm = 6371.0
    
    val lat1Rad = toRadians(lat1)
    val lat2Rad = toRadians(lat2)
    val deltaLatRad = toRadians(lat2 - lat1)
    val deltaLonRad = toRadians(lon2 - lon1)
    
    val a = kotlin.math.sin(deltaLatRad / 2) * kotlin.math.sin(deltaLatRad / 2) +
            kotlin.math.cos(lat1Rad) * kotlin.math.cos(lat2Rad) *
            kotlin.math.sin(deltaLonRad / 2) * kotlin.math.sin(deltaLonRad / 2)
    
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    
    return earthRadiusKm * c
}

/**
 * Calculate bearing between two coordinates (KMP compatible)
 */
fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val lat1Rad = toRadians(lat1)
    val lat2Rad = toRadians(lat2)
    val deltaLonRad = toRadians(lon2 - lon1)
    
    val x = kotlin.math.sin(deltaLonRad) * kotlin.math.cos(lat2Rad)
    val y = kotlin.math.cos(lat1Rad) * kotlin.math.sin(lat2Rad) -
            kotlin.math.sin(lat1Rad) * kotlin.math.cos(lat2Rad) * kotlin.math.cos(deltaLonRad)
    
    val bearing = kotlin.math.atan2(x, y)
    
    // Convert from radians to degrees and normalize to 0-360
    return (toDegrees(bearing) + 360) % 360
}

/**
 * Convert radians to degrees (KMP compatible)
 */
fun toDegrees(radians: Double): Double {
    return radians * 180.0 / kotlin.math.PI
}

/**
 * Convert bearing to compass direction (KMP compatible)
 */
fun Double.toCompassDirection(): String {
    val directions = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
    val index = kotlin.math.round(this / 22.5).toInt() % 16
    return directions[index]
}