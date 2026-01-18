@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.asterixorobelix.afrikaburn.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLLocationAccuracyHundredMeters
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.NSLog
import platform.Foundation.timeIntervalSince1970
import platform.darwin.NSObject
import kotlin.coroutines.resume

private const val MILLIS_PER_SECOND = 1000

/**
 * iOS implementation of LocationService using CLLocationManager.
 *
 * Uses kCLLocationAccuracyHundredMeters for battery-efficient tracking
 * suitable for map navigation at AfrikaBurn.
 */
class IOSLocationService : LocationService {

    private val locationManager = CLLocationManager()
    private var currentDelegate: NSObject? = null

    override suspend fun checkPermission(): PermissionState {
        return mapAuthorizationStatus(locationManager.authorizationStatus)
    }

    override suspend fun requestPermission(): PermissionState =
        suspendCancellableCoroutine { continuation ->
            val currentStatus = locationManager.authorizationStatus

            if (currentStatus != kCLAuthorizationStatusNotDetermined) {
                continuation.resume(mapAuthorizationStatus(currentStatus))
                return@suspendCancellableCoroutine
            }

            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                    val newStatus = manager.authorizationStatus
                    if (newStatus != kCLAuthorizationStatusNotDetermined) {
                        continuation.resume(mapAuthorizationStatus(newStatus))
                    }
                }

                @Suppress("CONFLICTING_OVERLOADS")
                override fun locationManager(
                    manager: CLLocationManager,
                    didChangeAuthorizationStatus: CLAuthorizationStatus
                ) {
                    if (didChangeAuthorizationStatus != kCLAuthorizationStatusNotDetermined) {
                        continuation.resume(mapAuthorizationStatus(didChangeAuthorizationStatus))
                    }
                }
            }

            locationManager.delegate = delegate
            locationManager.requestWhenInUseAuthorization()
        }

    override fun startLocationUpdates(): Flow<LocationData> = callbackFlow {
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(
                manager: CLLocationManager,
                didUpdateLocations: List<*>
            ) {
                val locations = didUpdateLocations.filterIsInstance<CLLocation>()
                locations.lastOrNull()?.let { location ->
                    val locationData = extractLocationData(location)
                    trySend(locationData)
                }
            }

            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                NSLog("Location error: ${didFailWithError.localizedDescription}")
            }
        }

        currentDelegate = delegate
        locationManager.delegate = delegate
        locationManager.desiredAccuracy = kCLLocationAccuracyHundredMeters
        locationManager.startUpdatingLocation()

        awaitClose {
            locationManager.stopUpdatingLocation()
            currentDelegate = null
        }
    }

    override fun stopLocationUpdates() {
        locationManager.stopUpdatingLocation()
        currentDelegate = null
    }

    override suspend fun getCurrentLocation(): LocationData? =
        suspendCancellableCoroutine { continuation ->
            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManager(
                    manager: CLLocationManager,
                    didUpdateLocations: List<*>
                ) {
                    val locations = didUpdateLocations.filterIsInstance<CLLocation>()
                    locations.lastOrNull()?.let { location ->
                        manager.stopUpdatingLocation()
                        val locationData = extractLocationData(location)
                        continuation.resume(locationData)
                    }
                }

                override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                    NSLog("Location error: ${didFailWithError.localizedDescription}")
                    continuation.resume(null)
                }
            }

            locationManager.delegate = delegate
            locationManager.desiredAccuracy = kCLLocationAccuracyHundredMeters
            locationManager.startUpdatingLocation()
        }

    private fun extractLocationData(location: CLLocation): LocationData {
        val coordinate = location.coordinate.useContents {
            Pair(latitude, longitude)
        }
        val timestampSeconds = location.timestamp.timeIntervalSince1970()
        return LocationData(
            latitude = coordinate.first,
            longitude = coordinate.second,
            accuracy = location.horizontalAccuracy.toFloat(),
            timestamp = (timestampSeconds * MILLIS_PER_SECOND).toLong()
        )
    }

    private fun mapAuthorizationStatus(status: CLAuthorizationStatus): PermissionState {
        return when (status) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> PermissionState.GRANTED
            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted -> PermissionState.DENIED
            kCLAuthorizationStatusNotDetermined -> PermissionState.NOT_DETERMINED
            else -> PermissionState.NOT_DETERMINED
        }
    }
}

/**
 * Create iOS-specific location service.
 */
actual fun createLocationService(): LocationService = IOSLocationService()
