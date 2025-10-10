package io.asterixorobelix.afrikaburn.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import io.asterixorobelix.afrikaburn.models.*
import io.asterixorobelix.afrikaburn.Dimens
import kotlinx.coroutines.launch
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineMapScreen(
    onNavigateBack: () -> Unit,
    viewModel: OfflineMapViewModel = rememberOfflineMapViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Map") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Navigate back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.centerOnUserLocation() }) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = "Center on my location"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MapCanvas(
                uiState = uiState,
                onMapTransform = { scale, offset ->
                    viewModel.updateMapTransform(scale, offset)
                },
                onLocationTap = { location ->
                    viewModel.selectLocation(location)
                }
            )

            // Layer controls
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Dimens.paddingMedium),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.paddingSmall)
                ) {
                    Text(
                        text = "Layers",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = Dimens.paddingSmall)
                    )
                    
                    MapLayerChip(
                        label = "Camps",
                        icon = Icons.Default.Home,
                        selected = uiState.showCamps,
                        onClick = { viewModel.toggleLayer(MapLayer.CAMPS) }
                    )
                    
                    MapLayerChip(
                        label = "Art",
                        icon = Icons.Default.Palette,
                        selected = uiState.showArt,
                        onClick = { viewModel.toggleLayer(MapLayer.ART) }
                    )
                    
                    MapLayerChip(
                        label = "Facilities",
                        icon = Icons.Default.LocalDrink,
                        selected = uiState.showFacilities,
                        onClick = { viewModel.toggleLayer(MapLayer.FACILITIES) }
                    )
                    
                    MapLayerChip(
                        label = "Emergency",
                        icon = Icons.Default.LocalHospital,
                        selected = uiState.showEmergency,
                        onClick = { viewModel.toggleLayer(MapLayer.EMERGENCY) }
                    )
                }
            }

            // Selected location details
            uiState.selectedLocation?.let { location ->
                LocationDetailsCard(
                    location = location,
                    onDismiss = { viewModel.clearSelection() },
                    onMarkAsCamp = {
                        viewModel.markAsUserCamp(location)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Camp location saved")
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(Dimens.paddingMedium)
                )
            }

            // Zoom controls
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Dimens.paddingMedium)
            ) {
                FloatingActionButton(
                    onClick = { viewModel.zoomIn() },
                    modifier = Modifier.size(Dimens.buttonHeightSmall),
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom in")
                }
                
                Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                
                FloatingActionButton(
                    onClick = { viewModel.zoomOut() },
                    modifier = Modifier.size(Dimens.buttonHeightSmall),
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom out")
                }
            }
        }
    }
}

@Composable
private fun MapCanvas(
    uiState: OfflineMapUiState,
    onMapTransform: (scale: Float, offset: Offset) -> Unit,
    onLocationTap: (MapLocation) -> Unit
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    
    // Capture theme colors outside Canvas scope
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val errorColor = MaterialTheme.colorScheme.error
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val backgroundColor = MaterialTheme.colorScheme.background
    
    var scale by remember { mutableStateOf(uiState.scale) }
    var offset by remember { mutableStateOf(uiState.offset) }
    
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 5f)
        offset += panChange
        onMapTransform(scale, offset)
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .transformable(state = transformableState)
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    // Convert tap coordinates to map coordinates
                    val mapX = (tapOffset.x - offset.x) / scale
                    val mapY = (tapOffset.y - offset.y) / scale
                    
                    // Check if tap is near any location
                    val tappedLocation = uiState.allLocations.find { location ->
                        val distance = sqrt(
                            (location.coordinates.x - mapX).pow(2) + 
                            (location.coordinates.y - mapY).pow(2)
                        )
                        distance < 30 / scale // Adjust hit target based on zoom
                    }
                    
                    tappedLocation?.let { onLocationTap(it) }
                }
            }
    ) {
        translate(offset.x, offset.y) {
            scale(scale) {
                // Draw map background/grid
                drawMapBackground()
                
                // Draw locations by layer
                if (uiState.showCamps) {
                    uiState.camps.forEach { camp ->
                        drawLocation(
                            location = camp,
                            color = primaryColor,
                            icon = "🏕️",
                            isUserCamp = camp.id == uiState.userCampId
                        )
                    }
                }
                
                if (uiState.showArt) {
                    uiState.artInstallations.forEach { art ->
                        drawLocation(
                            location = art,
                            color = secondaryColor,
                            icon = "🎨"
                        )
                    }
                }
                
                if (uiState.showFacilities) {
                    uiState.facilities.forEach { facility ->
                        drawLocation(
                            location = facility,
                            color = tertiaryColor,
                            icon = when (facility.subType) {
                                "toilet" -> "🚻"
                                "water" -> "💧"
                                "food" -> "🍴"
                                else -> "🏢"
                            }
                        )
                    }
                }
                
                if (uiState.showEmergency) {
                    uiState.emergencyPoints.forEach { emergency ->
                        drawLocation(
                            location = emergency,
                            color = errorColor,
                            icon = "🚨"
                        )
                    }
                }
                
                // Draw user location
                uiState.userLocation?.let { userLoc ->
                    drawCircle(
                        color = primaryColor,
                        radius = 10f,
                        center = Offset(userLoc.x, userLoc.y)
                    )
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.3f),
                        radius = 20f,
                        center = Offset(userLoc.x, userLoc.y)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawMapBackground() {
    // Draw grid lines for orientation
    val gridSize = 100f
    val gridColor = Color.Gray.copy(alpha = 0.2f)
    
    for (x in 0..10) {
        drawLine(
            color = gridColor,
            start = Offset(x * gridSize, 0f),
            end = Offset(x * gridSize, size.height),
            strokeWidth = 1f
        )
    }
    
    for (y in 0..10) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y * gridSize),
            end = Offset(size.width, y * gridSize),
            strokeWidth = 1f
        )
    }
}

private fun DrawScope.drawLocation(
    location: MapLocation,
    color: Color,
    icon: String,
    isUserCamp: Boolean = false
) {
    val center = Offset(location.coordinates.x, location.coordinates.y)
    
    // Draw pin
    drawCircle(
        color = if (isUserCamp) Color.Green else color,
        radius = if (isUserCamp) 15f else 10f,
        center = center
    )
    
    // Draw icon (simplified - in real app would use actual icons)
    drawIntoCanvas { canvas ->
        // This is a placeholder - actual implementation would draw proper icons
    }
}

@Composable
private fun MapLayerChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(Dimens.iconSizeSmall)
            )
        },
        modifier = Modifier
            .padding(vertical = 2.dp)
            .height(Dimens.buttonHeightSmall)
    )
}

@Composable
private fun LocationDetailsCard(
    location: MapLocation,
    onDismiss: () -> Unit,
    onMarkAsCamp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium
                )
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(Dimens.iconSizeMedium)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            location.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = Dimens.paddingSmall)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Type: ${location.type}",
                    style = MaterialTheme.typography.labelMedium
                )
                
                if (location.type == MapLocationType.CAMP) {
                    TextButton(
                        onClick = onMarkAsCamp
                    ) {
                        Icon(
                            Icons.Default.Flag,
                            contentDescription = null,
                            modifier = Modifier.size(Dimens.iconSizeSmall)
                        )
                        Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                        Text("Mark as my camp")
                    }
                }
            }
        }
    }
}

// Preview function
@Composable
fun OfflineMapScreenPreview() {
    MaterialTheme {
        OfflineMapScreen(
            onNavigateBack = {},
            viewModel = PreviewOfflineMapViewModel()
        )
    }
}

// Supporting classes for the preview
@Composable
private fun rememberOfflineMapViewModel(): OfflineMapViewModel {
    // This would be provided by DI in the real app
    return remember { OfflineMapViewModel() }
}

private class PreviewOfflineMapViewModel : OfflineMapViewModel() {
    init {
        // Initialize with preview data
        uiStateInternal.value = OfflineMapUiState(
            camps = listOf(
                MapLocation(
                    id = "1",
                    name = "Sunset Camp",
                    type = MapLocationType.CAMP,
                    coordinates = MapCoordinates(200f, 300f),
                    description = "A peaceful camp with amazing sunsets"
                ),
                MapLocation(
                    id = "2",
                    name = "Dance Camp",
                    type = MapLocationType.CAMP,
                    coordinates = MapCoordinates(400f, 400f),
                    description = "24/7 music and dancing"
                )
            ),
            artInstallations = listOf(
                MapLocation(
                    id = "3",
                    name = "The Burning Heart",
                    type = MapLocationType.ART,
                    coordinates = MapCoordinates(500f, 500f),
                    description = "A giant flaming heart sculpture"
                )
            ),
            showCamps = true,
            showArt = true
        )
    }
}