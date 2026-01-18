package io.asterixorobelix.afrikaburn.platform

import androidx.compose.runtime.Composable

/**
 * Callback interface for location permission results.
 */
interface LocationPermissionCallback {
    /**
     * Called when permission result is determined.
     * @param granted True if location permission was granted
     */
    fun onPermissionResult(granted: Boolean)
}

/**
 * Remembers and provides a location permission handler.
 *
 * On Android, this uses ActivityResultContracts.RequestMultiplePermissions
 * to show the system permission dialog.
 *
 * On iOS, this delegates to CLLocationManager.requestWhenInUseAuthorization().
 *
 * @param onResult Callback invoked when permission is granted or denied
 * @return A function that triggers the permission request
 */
@Composable
expect fun rememberLocationPermissionLauncher(
    onResult: (granted: Boolean) -> Unit
): () -> Unit
