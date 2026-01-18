package io.asterixorobelix.afrikaburn.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.resume

private const val LOCATION_UPDATE_INTERVAL_MS = 5000L
private const val LOCATION_TAG = "AndroidLocationService"

/**
 * Android implementation of LocationService using FusedLocationProviderClient.
 *
 * Uses balanced power accuracy to conserve battery while providing
 * adequate accuracy for map navigation at AfrikaBurn.
 */
class AndroidLocationService : LocationService, KoinComponent {

    private val context: Context by inject()
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }
    private var locationCallback: LocationCallback? = null

    override suspend fun checkPermission(): PermissionState {
        return when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> PermissionState.GRANTED

            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> PermissionState.GRANTED

            else -> PermissionState.NOT_DETERMINED
        }
    }

    override suspend fun requestPermission(): PermissionState {
        // Note: Actual permission request must be handled at the Activity/Fragment level
        // This method returns the current state - the UI layer handles requesting
        return checkPermission()
    }

    @Suppress("MissingPermission")
    override fun startLocationUpdates(): Flow<LocationData> = callbackFlow {
        if (checkPermission() != PermissionState.GRANTED) {
            Log.w(LOCATION_TAG, "Location permission not granted")
            close()
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            LOCATION_UPDATE_INTERVAL_MS
        ).build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val locationData = LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        timestamp = location.time
                    )
                    trySend(locationData)
                }
            }
        }

        locationCallback = callback

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e(LOCATION_TAG, "Security exception requesting location updates", e)
            close(e)
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
            locationCallback = null
        }
    }

    override fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
    }

    @Suppress("MissingPermission")
    override suspend fun getCurrentLocation(): LocationData? {
        if (checkPermission() != PermissionState.GRANTED) {
            return null
        }

        return suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val locationData = LocationData(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                accuracy = location.accuracy,
                                timestamp = location.time
                            )
                            continuation.resume(locationData)
                        } else {
                            continuation.resume(null)
                        }
                    }
                    .addOnFailureListener {
                        Log.e(LOCATION_TAG, "Failed to get current location", it)
                        continuation.resume(null)
                    }
            } catch (e: SecurityException) {
                Log.e(LOCATION_TAG, "Security exception getting location", e)
                continuation.resume(null)
            }
        }
    }
}

/**
 * Create Android-specific location service.
 */
actual fun createLocationService(): LocationService = AndroidLocationService()
