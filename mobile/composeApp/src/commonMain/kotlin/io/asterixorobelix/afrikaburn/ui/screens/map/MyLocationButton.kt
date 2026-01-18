package io.asterixorobelix.afrikaburn.ui.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Floating action button to center map on user's location.
 *
 * Uses Material Design 3 styling with appropriate colors for
 * enabled and disabled states.
 *
 * @param onClick Called when button is tapped
 * @param enabled Whether the button is enabled (location available)
 * @param modifier Optional modifier
 */
@Composable
fun MyLocationButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = if (enabled) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = if (enabled) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = Dimens.elevationSmall
        )
    ) {
        Icon(
            imageVector = Icons.Default.MyLocation,
            contentDescription = "Center on my location"
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun MyLocationButtonPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
            ) {
                // Enabled state
                MyLocationButton(
                    onClick = {},
                    enabled = true
                )
                // Disabled state
                MyLocationButton(
                    onClick = {},
                    enabled = false
                )
            }
        }
    }
}
