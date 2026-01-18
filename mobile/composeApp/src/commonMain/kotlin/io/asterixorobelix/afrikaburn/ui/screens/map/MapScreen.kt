package io.asterixorobelix.afrikaburn.ui.screens.map

import afrikaburn.composeapp.generated.resources.Res
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.asterixorobelix.afrikaburn.Dimens
import io.asterixorobelix.afrikaburn.di.koinMapViewModel
import io.asterixorobelix.afrikaburn.presentation.map.MapUiState
import io.github.dellisd.spatialk.geojson.Position
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.Feature
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.eq
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle

private const val MAP_STYLE_PATH = "files/maps/style.json"
private const val MOCK_LOCATIONS_PATH = "files/maps/mock-locations.geojson"

// Material Design 3 purple for camps
private val CAMP_MARKER_COLOR = Color(0xFFBB86FC)
// Material Design 3 teal for artworks
private val ARTWORK_MARKER_COLOR = Color(0xFF03DAC6)
private val MARKER_STROKE_COLOR = Color.White

private val CAMP_MARKER_RADIUS = 12.dp
private val ARTWORK_MARKER_RADIUS = 10.dp
private val MARKER_STROKE_WIDTH = 2.dp

/**
 * Main map screen composable.
 *
 * Displays an interactive offline map of the Tankwa Karoo region
 * centered on the AfrikaBurn event location.
 */
@Composable
fun MapScreen() {
    val viewModel = koinMapViewModel()
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is MapUiState.Loading -> LoadingContent()
        is MapUiState.Success -> MapContent(
            state = state,
            onCameraChanged = viewModel::onCameraPositionChanged
        )
        is MapUiState.Error -> ErrorContent(
            message = state.message,
            onRetry = viewModel::resetToDefaultPosition
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun MapContent(
    state: MapUiState.Success,
    onCameraChanged: (Double, Double, Double) -> Unit
) {
    val cameraState = rememberCameraState(
        CameraPosition(
            target = Position(state.centerLongitude, state.centerLatitude),
            zoom = state.zoomLevel
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            baseStyle = BaseStyle.Uri(Res.getUri(MAP_STYLE_PATH)),
            cameraState = cameraState
        ) {
            // Load mock locations GeoJSON - must be inside MaplibreMap scope
            val locationsSource = rememberGeoJsonSource(
                data = GeoJsonData.Uri(Res.getUri(MOCK_LOCATIONS_PATH))
            )

            // Camp markers (purple circles)
            CircleLayer(
                id = "camp-markers",
                source = locationsSource,
                filter = Feature["type"].asString() eq const("camp"),
                color = const(CAMP_MARKER_COLOR),
                radius = const(CAMP_MARKER_RADIUS),
                strokeColor = const(MARKER_STROKE_COLOR),
                strokeWidth = const(MARKER_STROKE_WIDTH)
            )

            // Artwork markers (teal circles)
            CircleLayer(
                id = "artwork-markers",
                source = locationsSource,
                filter = Feature["type"].asString() eq const("artwork"),
                color = const(ARTWORK_MARKER_COLOR),
                radius = const(ARTWORK_MARKER_RADIUS),
                strokeColor = const(MARKER_STROKE_COLOR),
                strokeWidth = const(MARKER_STROKE_WIDTH)
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(Dimens.iconSizeExtraLarge),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(Dimens.spacingLarge))
            Text(
                text = "Loading map...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(Dimens.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Map,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(Dimens.iconSizeHero)
        )

        Spacer(modifier = Modifier.height(Dimens.spacingLarge))

        Text(
            text = "Map Error",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimens.spacingSmall))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimens.spacingExtraLarge))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(Dimens.iconSizeSmall)
            )
            Spacer(modifier = Modifier.size(Dimens.spacingSmall))
            Text(
                text = "Retry",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
