package io.asterixorobelix.afrikaburn.ui.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
import org.jetbrains.compose.ui.tooling.preview.Preview

// Material Design blue for user location (standard convention)
private val USER_LOCATION_COLOR = Color(0xFF2196F3)
private val USER_LOCATION_SIZE = 16.dp
private val USER_LOCATION_BORDER = 3.dp

/**
 * Blue dot marker showing user's current GPS location.
 *
 * Follows standard map conventions with blue fill and white border.
 * This composable is primarily for preview and reference.
 * Actual map display uses MapLibre's CircleLayer positioned at user coordinates.
 */
@Composable
fun UserLocationMarker(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(USER_LOCATION_SIZE)
            .shadow(
                elevation = Dimens.elevationSmall,
                shape = CircleShape
            )
            .background(
                color = USER_LOCATION_COLOR,
                shape = CircleShape
            )
            .border(
                width = USER_LOCATION_BORDER,
                color = Color.White,
                shape = CircleShape
            )
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun UserLocationMarkerPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            UserLocationMarker()
        }
    }
}
