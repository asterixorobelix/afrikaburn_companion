package io.asterixorobelix.afrikaburn.platform

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

/**
 * Android implementation of location permission launcher.
 *
 * Uses ActivityResultContracts.RequestMultiplePermissions to request
 * ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions.
 *
 * @param onResult Callback invoked when user responds to permission dialog
 * @return A function that launches the system permission dialog
 */
@Composable
actual fun rememberLocationPermissionLauncher(
    onResult: (granted: Boolean) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Permission is granted if either fine or coarse location is granted
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onResult(granted)
    }

    return {
        launcher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}
