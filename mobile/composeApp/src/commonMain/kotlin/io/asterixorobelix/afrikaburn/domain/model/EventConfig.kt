package io.asterixorobelix.afrikaburn.domain.model

import kotlinx.datetime.LocalDate

/**
 * Configuration data for the AfrikaBurn event.
 * Contains event dates, location coordinates, and geofence radius.
 *
 * @property eventStartDate The first day of the event (inclusive)
 * @property eventEndDate The last day of the event (inclusive)
 * @property eventLatitude GPS latitude of the event location (Tankwa Karoo)
 * @property eventLongitude GPS longitude of the event location (Tankwa Karoo)
 * @property geofenceRadiusKm Radius in kilometers for geofence detection
 */
data class EventConfig(
    val eventStartDate: LocalDate,
    val eventEndDate: LocalDate,
    val eventLatitude: Double,
    val eventLongitude: Double,
    val geofenceRadiusKm: Double
) {
    companion object {
        /**
         * AfrikaBurn 2026 event configuration.
         *
         * Event dates: April 27 - May 3, 2026
         * Location: Tankwa Karoo, South Africa
         * Coordinates: -32.3266, 19.7437
         * Geofence: 20 km radius as per requirements
         */
        val DEFAULT = EventConfig(
            eventStartDate = LocalDate(2026, 4, 27),
            eventEndDate = LocalDate(2026, 5, 3),
            eventLatitude = -32.3266,
            eventLongitude = 19.7437,
            geofenceRadiusKm = 20.0
        )
    }
}
