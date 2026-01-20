package io.asterixorobelix.afrikaburn.ui.screens.map

import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.map_legend_artworks
import afrikaburn.composeapp.generated.resources.map_legend_camps
import afrikaburn.composeapp.generated.resources.map_legend_hint
import afrikaburn.composeapp.generated.resources.map_legend_my_camp
import afrikaburn.composeapp.generated.resources.map_legend_you
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.asterixorobelix.afrikaburn.Dimens
import io.asterixorobelix.afrikaburn.di.koinMapViewModel
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.platform.PermissionState
import io.asterixorobelix.afrikaburn.platform.rememberLocationPermissionLauncher
import io.asterixorobelix.afrikaburn.presentation.map.CampPinDialogState
import io.asterixorobelix.afrikaburn.presentation.map.CampPinState
import io.asterixorobelix.afrikaburn.presentation.map.MapUiState
import io.asterixorobelix.afrikaburn.presentation.map.MapViewModel
import io.github.dellisd.spatialk.geojson.Feature as GeoJsonFeature
import io.github.dellisd.spatialk.geojson.Point
import io.github.dellisd.spatialk.geojson.Position
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds
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
import org.maplibre.compose.util.ClickResult

private const val MAP_STYLE_PATH = "files/maps/style.json"
private const val MOCK_LOCATIONS_PATH = "files/maps/mock-locations.geojson"

// Map marker colors - warm festival palette
@Suppress("MagicNumber")
private val CAMP_MARKER_COLOR = Color(0xFFBB86FC)  // Purple for camps
@Suppress("MagicNumber")
private val ARTWORK_MARKER_COLOR = Color(0xFF03DAC6)  // Teal for artworks
@Suppress("MagicNumber")
private val USER_LOCATION_COLOR = Color(0xFF64B5F6)  // Lighter blue for user
@Suppress("MagicNumber")
private val CAMP_PIN_COLOR = Color(0xFFFFAB40)  // Warm orange for user's camp
private val MARKER_STROKE_COLOR = Color.White

private val CAMP_MARKER_RADIUS = 12.dp
private val ARTWORK_MARKER_RADIUS = 10.dp
private val USER_LOCATION_RADIUS = 8.dp
private val CAMP_PIN_RADIUS = 14.dp
private val MARKER_STROKE_WIDTH = 2.dp
private val USER_LOCATION_STROKE_WIDTH = 3.dp
private val CAMP_PIN_STROKE_WIDTH = 3.dp

// Legend dimensions
private val LEGEND_DOT_SIZE = 10.dp
private val LEGEND_TOGGLE_SIZE = 36.dp

/**
 * Main map screen composable.
 *
 * Displays an interactive offline map of the Tankwa Karoo region
 * centered on the AfrikaBurn event location. Shows user GPS location
 * as a blue dot and provides a My Location FAB to center on position.
 *
 * @param onProjectClick Callback invoked when a marker is tapped with the matching ProjectItem
 */
@Composable
fun MapScreen(
    onProjectClick: (ProjectItem) -> Unit = {}
) {
    val viewModel = koinMapViewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Platform-specific permission launcher for showing system dialog
    val requestPermission = rememberLocationPermissionLauncher { granted ->
        viewModel.onPermissionResult(granted)
    }

    // Check permission state and request if not determined
    LaunchedEffect(Unit) {
        viewModel.checkLocationPermission()
    }

    // Trigger permission request when state indicates NOT_DETERMINED
    val currentState = uiState
    LaunchedEffect(currentState) {
        if (currentState is MapUiState.Success &&
            currentState.locationPermissionState == PermissionState.NOT_DETERMINED
        ) {
            requestPermission()
        }
    }

    // Stop tracking when leaving screen to conserve battery
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopLocationTracking()
        }
    }

    when (val state = uiState) {
        is MapUiState.Loading -> LoadingContent()
        is MapUiState.Success -> MapContent(
            state = state,
            viewModel = viewModel,
            onCameraChanged = viewModel::onCameraPositionChanged,
            onProjectClick = onProjectClick
        )
        is MapUiState.Error -> ErrorContent(
            message = state.message,
            onRetry = viewModel::resetToDefaultPosition
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Suppress("LongMethod")
@Composable
private fun MapContent(
    state: MapUiState.Success,
    viewModel: MapViewModel,
    onCameraChanged: (Double, Double, Double) -> Unit,
    onProjectClick: (ProjectItem) -> Unit
) {
    val cameraState = rememberCameraState(
        CameraPosition(
            target = Position(state.centerLongitude, state.centerLatitude),
            zoom = state.zoomLevel
        )
    )

    // Legend expansion state - persists across recompositions
    var isLegendExpanded by rememberSaveable { mutableStateOf(true) }

    // Animate camera to user location when FAB is tapped
    LaunchedEffect(state.centerOnUserLocationRequest) {
        if (state.centerOnUserLocationRequest > 0 && state.hasUserLocation) {
            cameraState.animateTo(
                finalPosition = cameraState.position.copy(
                    target = Position(
                        longitude = state.userLongitude!!,
                        latitude = state.userLatitude!!
                    )
                ),
                duration = Dimens.animationDurationLong.milliseconds
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            baseStyle = BaseStyle.Uri(Res.getUri(MAP_STYLE_PATH)),
            cameraState = cameraState,
            onMapClick = { _, offset ->
                val features = cameraState.projection?.queryRenderedFeatures(offset)

                val markerFeature = features?.firstOrNull { feature ->
                    val type = feature.properties?.get("type")?.toString()?.removeSurrounding("\"")
                    type == "camp" || type == "artwork"
                }

                if (markerFeature != null) {
                    val code = markerFeature.properties
                        ?.get("code")
                        ?.toString()
                        ?.removeSurrounding("\"")

                    code?.let { viewModel.findProjectByCode(it) }?.let { project ->
                        onProjectClick(project)
                        return@MaplibreMap ClickResult.Consume
                    }
                }
                ClickResult.Pass
            },
            onMapLongClick = { position, _ ->
                viewModel.onMapLongPress(
                    latitude = position.latitude,
                    longitude = position.longitude
                )
                ClickResult.Consume
            }
        ) {
            val locationsSource = rememberGeoJsonSource(
                data = GeoJsonData.Uri(Res.getUri(MOCK_LOCATIONS_PATH))
            )

            // Camp markers (purple)
            CircleLayer(
                id = "camp-markers",
                source = locationsSource,
                filter = Feature["type"].asString() eq const("camp"),
                color = const(CAMP_MARKER_COLOR),
                radius = const(CAMP_MARKER_RADIUS),
                strokeColor = const(MARKER_STROKE_COLOR),
                strokeWidth = const(MARKER_STROKE_WIDTH)
            )

            // Artwork markers (teal)
            CircleLayer(
                id = "artwork-markers",
                source = locationsSource,
                filter = Feature["type"].asString() eq const("artwork"),
                color = const(ARTWORK_MARKER_COLOR),
                radius = const(ARTWORK_MARKER_RADIUS),
                strokeColor = const(MARKER_STROKE_COLOR),
                strokeWidth = const(MARKER_STROKE_WIDTH)
            )

            // User location (blue)
            if (state.hasUserLocation) {
                val userLocationFeature = GeoJsonFeature(
                    geometry = Point(
                        coordinates = Position(
                            longitude = state.userLongitude!!,
                            latitude = state.userLatitude!!
                        )
                    )
                )

                val userLocationSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(userLocationFeature)
                )

                CircleLayer(
                    id = "user-location",
                    source = userLocationSource,
                    color = const(USER_LOCATION_COLOR),
                    radius = const(USER_LOCATION_RADIUS),
                    strokeColor = const(MARKER_STROKE_COLOR),
                    strokeWidth = const(USER_LOCATION_STROKE_WIDTH)
                )
            }

            // User's camp pin (warm orange)
            val userCampPin = state.userCampPin
            if (userCampPin is CampPinState.Placed) {
                val campPinFeature = GeoJsonFeature(
                    geometry = Point(
                        coordinates = Position(
                            longitude = userCampPin.longitude,
                            latitude = userCampPin.latitude
                        )
                    )
                )

                val campPinSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(campPinFeature)
                )

                CircleLayer(
                    id = "camp-pin-layer",
                    source = campPinSource,
                    color = const(CAMP_PIN_COLOR),
                    radius = const(CAMP_PIN_RADIUS),
                    strokeColor = const(MARKER_STROKE_COLOR),
                    strokeWidth = const(CAMP_PIN_STROKE_WIDTH)
                )
            }
        }

        // Collapsible Map Legend - positioned to avoid scale bar
        MapLegend(
            isExpanded = isLegendExpanded,
            onToggle = { isLegendExpanded = !isLegendExpanded },
            showUserLocation = state.locationPermissionState == PermissionState.GRANTED,
            showCampPinHint = state.userCampPin is CampPinState.None,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = Dimens.paddingSmall,
                    top = Dimens.mapLegendTopPadding
                )
        )

        // My Location FAB - positioned to avoid MapLibre attribution
        if (state.locationPermissionState == PermissionState.GRANTED) {
            MyLocationButton(
                onClick = viewModel::centerOnUserLocation,
                enabled = state.hasUserLocation,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = Dimens.paddingMedium,
                        bottom = Dimens.mapFabBottomPadding
                    )
            )
        }
    }

    // Camp pin dialogs
    CampPinDialogs(
        dialogState = state.campPinDialogState,
        viewModel = viewModel
    )
}

/**
 * Collapsible map legend with smooth animations.
 * Shows marker colors and a long-press hint for first-time users.
 */
@Composable
private fun MapLegend(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    showUserLocation: Boolean,
    showCampPinHint: Boolean,
    modifier: Modifier = Modifier
) {
    // Animate rotation for the toggle icon
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 0f else 180f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "legend_rotation"
    )

    // Animate scale for press feedback
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "legend_scale"
    )

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = Dimens.elevationSmall,
                shape = RoundedCornerShape(Dimens.cornerRadiusMedium)
            ),
        shape = RoundedCornerShape(Dimens.cornerRadiusMedium),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = Dimens.elevationSmall
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingSmall)
        ) {
            // Toggle header - always visible
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(Dimens.cornerRadiusSmall))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            isPressed = true
                            onToggle()
                        }
                    )
                    .padding(Dimens.paddingExtraSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Layers,
                    contentDescription = "Toggle legend",
                    modifier = Modifier
                        .size(Dimens.iconSizeMedium)
                        .rotate(rotation),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Legend",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Reset pressed state
            LaunchedEffect(isPressed) {
                if (isPressed) {
                    kotlinx.coroutines.delay(100)
                    isPressed = false
                }
            }

            // Expandable content with smooth animation
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = Dimens.paddingSmall),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
                ) {
                    // Theme Camps - Purple
                    LegendItem(
                        color = CAMP_MARKER_COLOR,
                        label = stringResource(Res.string.map_legend_camps)
                    )

                    // Artworks - Teal
                    LegendItem(
                        color = ARTWORK_MARKER_COLOR,
                        label = stringResource(Res.string.map_legend_artworks)
                    )

                    // My Camp - Orange
                    LegendItem(
                        color = CAMP_PIN_COLOR,
                        label = stringResource(Res.string.map_legend_my_camp)
                    )

                    // You - Blue
                    if (showUserLocation) {
                        LegendItem(
                            color = USER_LOCATION_COLOR,
                            label = stringResource(Res.string.map_legend_you)
                        )
                    }

                    // Long-press hint with warm styling
                    if (showCampPinHint) {
                        Spacer(modifier = Modifier.height(Dimens.spacingExtraSmall))
                        Surface(
                            shape = RoundedCornerShape(Dimens.cornerRadiusSmall),
                            color = CAMP_PIN_COLOR.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = stringResource(Res.string.map_legend_hint),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(
                                    horizontal = Dimens.paddingSmall,
                                    vertical = Dimens.paddingExtraSmall
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
    ) {
        // Colored dot with white stroke for polish
        Box(
            modifier = Modifier
                .size(LEGEND_DOT_SIZE)
                .shadow(1.dp, CircleShape)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CampPinDialogs(
    dialogState: CampPinDialogState,
    viewModel: MapViewModel
) {
    when (dialogState) {
        is CampPinDialogState.Hidden -> { /* No dialog */ }

        is CampPinDialogState.ConfirmPlace -> {
            CampPinPlaceDialog(
                onConfirm = viewModel::confirmPlacePin,
                onDismiss = viewModel::dismissDialog
            )
        }

        is CampPinDialogState.PinOptions -> {
            CampPinOptionsDialog(
                onMoveRequest = viewModel::dismissDialog,
                onDeleteRequest = viewModel::showDeleteConfirmation,
                onDismiss = viewModel::dismissDialog
            )
        }

        is CampPinDialogState.ConfirmMove -> {
            CampPinMoveDialog(
                onConfirm = viewModel::confirmMovePin,
                onDismiss = viewModel::dismissDialog
            )
        }

        is CampPinDialogState.ConfirmDelete -> {
            CampPinDeleteDialog(
                onConfirm = viewModel::confirmDeletePin,
                onDismiss = viewModel::dismissDialog
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
