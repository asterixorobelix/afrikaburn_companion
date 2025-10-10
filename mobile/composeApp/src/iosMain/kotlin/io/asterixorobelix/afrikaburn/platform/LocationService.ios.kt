package io.asterixorobelix.afrikaburn.platform

import kotlinx.cinterop.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.*
import platform.Foundation.NSError
import kotlin.coroutines.resume
import kotlin.math.*

class IOSLocationService(
    private val locationConfig: LocationConfig = LocationConfig()
) : LocationService {
    
    private val locationManager = CLLocationManager()
    private val locationDelegate = LocationDelegate()
    
    init {
        locationManager.delegate = locationDelegate
        locationManager.desiredAccuracy = if (locationConfig.highAccuracy) {
            kCLLocationAccuracyBest
        } else {
            kCLLocationAccuracyHundredMeters
        }
        locationManager.distanceFilter = locationConfig.minDisplacementMeters.toDouble()
    }
    
    override fun getLocationUpdates(): Flow<Pair<Double, Double>> = callbackFlow {
        locationDelegate.onLocationUpdate = { location ->
            trySend(Pair(location.coordinate.latitude, location.coordinate.longitude))
        }
        
        locationDelegate.onError = { error ->
            close(Exception(error.localizedDescription))
        }
        
        if (hasLocationPermission()) {
            locationManager.startUpdatingLocation()
        } else {
            locationManager.requestWhenInUseAuthorization()
        }
        
        awaitClose {
            stopLocationUpdates()
        }
    }
    
    override suspend fun getCurrentLocation(): Pair<Double, Double>? {
        return suspendCancellableCoroutine { continuation ->
            locationDelegate.oneShotLocationUpdate = { location ->
                continuation.resume(Pair(location.coordinate.latitude, location.coordinate.longitude))
            }
            
            locationDelegate.oneShotError = {
                continuation.resume(null)
            }
            
            if (hasLocationPermission()) {
                locationManager.requestLocation()
            } else {
                continuation.resume(null)
            }
        }
    }
    
    override fun hasLocationPermission(): Boolean {
        return when (CLLocationManager.authorizationStatus()) {
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> true
            else -> false
        }
    }
    
    override suspend fun requestLocationPermission(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            locationDelegate.onAuthorizationChange = { status ->
                continuation.resume(
                    status == kCLAuthorizationStatusAuthorizedAlways ||
                    status == kCLAuthorizationStatusAuthorizedWhenInUse
                )
            }
            
            when (CLLocationManager.authorizationStatus()) {
                kCLAuthorizationStatusNotDetermined -> {
                    locationManager.requestWhenInUseAuthorization()
                }
                kCLAuthorizationStatusAuthorizedAlways,
                kCLAuthorizationStatusAuthorizedWhenInUse -> {
                    continuation.resume(true)
                }
                else -> {
                    continuation.resume(false)
                }
            }
        }
    }
    
    override fun startLocationUpdates() {
        if (hasLocationPermission()) {
            locationManager.startUpdatingLocation()
        }
    }
    
    override fun stopLocationUpdates() {
        locationManager.stopUpdatingLocation()
    }
    
    override fun calculateDistance(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double
    ): Double {
        val startLocation = CLLocation(
            latitude = startLat,
            longitude = startLon
        )
        val endLocation = CLLocation(
            latitude = endLat,
            longitude = endLon
        )
        
        return startLocation.distanceFromLocation(endLocation)
    }
    
    private class LocationDelegate : NSObject(), CLLocationManagerDelegateProtocol {
        var onLocationUpdate: ((CLLocation) -> Unit)? = null
        var onError: ((NSError) -> Unit)? = null
        var onAuthorizationChange: ((CLAuthorizationStatus) -> Unit)? = null
        var oneShotLocationUpdate: ((CLLocation) -> Unit)? = null
        var oneShotError: (() -> Unit)? = null
        
        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            @Suppress("UNCHECKED_CAST")
            val locations = didUpdateLocations as List<CLLocation>
            locations.lastOrNull()?.let { location ->
                onLocationUpdate?.invoke(location)
                oneShotLocationUpdate?.invoke(location)
                oneShotLocationUpdate = null
            }
        }
        
        override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
            onError?.invoke(didFailWithError)
            oneShotError?.invoke()
            oneShotError = null
        }
        
        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            val status = CLLocationManager.authorizationStatus()
            onAuthorizationChange?.invoke(status)
        }
    }
}

actual fun createLocationService(): LocationService {
    return IOSLocationService()
}