package io.asterixorobelix.afrikaburn.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * iOS implementation of location permission launcher.
 *
 * Delegates to CLLocationManager.requestWhenInUseAuthorization() through
 * the LocationService interface.
 *
 * @param onResult Callback invoked when user responds to permission dialog
 * @return A function that triggers the iOS permission request
 */
@Composable
actual fun rememberLocationPermissionLauncher(
    onResult: (granted: Boolean) -> Unit
): () -> Unit {
    val locationService: LocationService = koinInject()
    val scope = rememberCoroutineScope()

    return {
        scope.launch {
            val result = locationService.requestPermission()
            onResult(result == PermissionState.GRANTED)
        }
    }
}
