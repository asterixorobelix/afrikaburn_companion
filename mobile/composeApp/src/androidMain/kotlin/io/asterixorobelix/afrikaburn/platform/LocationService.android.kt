package io.asterixorobelix.afrikaburn.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.*

class AndroidLocationService(
    private val context: Context,
    private val locationConfig: LocationConfig = LocationConfig()
) : LocationService {
    
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback? = null
    
    override fun getLocationUpdates(): Flow<Pair<Double, Double>> = callbackFlow {
        if (!hasLocationPermission()) {
            close(SecurityException("Location permission not granted"))
            return@callbackFlow
        }
        
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(Pair(location.latitude, location.longitude))
                }
            }
        }
        
        locationCallback = callback
        
        val locationRequest = createLocationRequest()
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            close(e)
            return@callbackFlow
        }
        
        awaitClose {
            stopLocationUpdates()
        }
    }
    
    override suspend fun getCurrentLocation(): Pair<Double, Double>? {
        if (!hasLocationPermission()) {
            return null
        }
        
        return suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        location?.let {
                            continuation.resume(Pair(it.latitude, it.longitude))
                        } ?: continuation.resume(null)
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            } catch (e: SecurityException) {
                continuation.resume(null)
            }
        }
    }
    
    override fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    override suspend fun requestLocationPermission(): Boolean {
        // This needs to be handled by the Activity/Fragment
        // Return current permission status
        return hasLocationPermission()
    }
    
    override fun startLocationUpdates() {
        if (!hasLocationPermission()) return
        
        locationCallback?.let { callback ->
            val locationRequest = createLocationRequest()
            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    callback,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                // Handle permission denied
            }
        }
    }
    
    override fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
    }
    
    override fun calculateDistance(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double
    ): Double {
        val results = FloatArray(1)
        Location.distanceBetween(startLat, startLon, endLat, endLon, results)
        return results[0].toDouble()
    }
    
    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.Builder(
            when (locationConfig.highAccuracy) {
                true -> Priority.PRIORITY_HIGH_ACCURACY
                false -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
            },
            locationConfig.updateIntervalMillis
        ).apply {
            setMinUpdateIntervalMillis(locationConfig.fastestUpdateIntervalMillis)
            setMinUpdateDistanceMeters(locationConfig.minDisplacementMeters)
            setWaitForAccurateLocation(locationConfig.highAccuracy)
        }.build()
    }
}

actual fun createLocationService(): LocationService {
    throw IllegalStateException("LocationService must be provided via dependency injection with Android Context. Use AndroidLocationService(context) instead.")
}