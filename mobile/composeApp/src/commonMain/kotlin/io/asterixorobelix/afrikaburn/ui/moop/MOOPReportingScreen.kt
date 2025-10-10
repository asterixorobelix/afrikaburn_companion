package io.asterixorobelix.afrikaburn.ui.moop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.asterixorobelix.afrikaburn.Dimens
import io.asterixorobelix.afrikaburn.domain.model.MOOPSeverity
import io.asterixorobelix.afrikaburn.domain.model.MOOPType
import io.asterixorobelix.afrikaburn.domain.model.getCurrentTimestamp
import io.asterixorobelix.afrikaburn.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MOOPReportingScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMap: () -> Unit,
    viewModel: MOOPReportingViewModel = rememberMOOPReportingViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Handle report submission results
    LaunchedEffect(uiState.reportResult) {
        when (val result = uiState.reportResult) {
            is ReportResult.Success -> {
                val message = when (result.syncStatus) {
                    SyncStatus.SYNCED -> "MOOP report submitted successfully"
                    SyncStatus.QUEUED -> "MOOP report saved offline and will sync when network is available"
                    SyncStatus.FAILED -> "MOOP report saved but sync failed. Will retry automatically."
                }
                snackbarHostState.showSnackbar(message)
                viewModel.clearForm()
            }
            is ReportResult.Failed -> {
                snackbarHostState.showSnackbar("Error: ${result.error}")
            }
            is ReportResult.ValidationFailed -> {
                val errorMessage = result.errors.joinToString("\n") { it.message }
                snackbarHostState.showSnackbar(errorMessage)
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report MOOP") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleMapView() }) {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = "View map",
                            tint = if (uiState.showMapView) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.paddingMedium)
            ) {
                // Location Section
                LocationSection(
                    hasLocation = uiState.hasLocation,
                    latitude = uiState.latitude,
                    longitude = uiState.longitude,
                    onGetLocation = { viewModel.getCurrentLocation() }
                )
                
                Spacer(modifier = Modifier.height(Dimens.paddingMedium))
                
                // MOOP Type Selection
                MOOPTypeSection(
                    selectedType = uiState.moopType,
                    onTypeSelected = { viewModel.updateMoopType(it) }
                )
                
                Spacer(modifier = Modifier.height(Dimens.paddingMedium))
                
                // Description Input
                DescriptionSection(
                    description = uiState.description,
                    onDescriptionChanged = { viewModel.updateDescription(it) },
                    validationError = uiState.validationErrors.find { 
                        it == ValidationError.MISSING_DESCRIPTION ||
                        it == ValidationError.DESCRIPTION_TOO_SHORT ||
                        it == ValidationError.DESCRIPTION_TOO_LONG
                    }
                )
                
                Spacer(modifier = Modifier.height(Dimens.paddingMedium))
                
                // Severity Selection
                SeveritySection(
                    selectedSeverity = uiState.severity,
                    onSeveritySelected = { viewModel.updateSeverity(it) }
                )
                
                Spacer(modifier = Modifier.height(Dimens.paddingMedium))
                
                // Photo Attachment
                PhotoSection(
                    photoUrl = uiState.photoUrl,
                    onAddPhoto = { viewModel.selectPhoto() },
                    onRemovePhoto = { viewModel.removePhoto() }
                )
                
                Spacer(modifier = Modifier.height(Dimens.paddingMedium))
                
                // Additional Options
                AdditionalOptionsSection(
                    estimatedCleanupTime = uiState.estimatedCleanupTime,
                    requiresSpecialEquipment = uiState.requiresSpecialEquipment,
                    isHazardous = uiState.isHazardous,
                    notes = uiState.notes,
                    onEstimatedTimeChanged = { viewModel.updateEstimatedCleanupTime(it) },
                    onSpecialEquipmentChanged = { viewModel.updateRequiresSpecialEquipment(it) },
                    onHazardousChanged = { viewModel.updateIsHazardous(it) },
                    onNotesChanged = { viewModel.updateNotes(it) }
                )
                
                Spacer(modifier = Modifier.height(Dimens.paddingLarge))
                
                // Submit Button
                Button(
                    onClick = { viewModel.submitReport() },
                    enabled = uiState.canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimens.iconSizeMedium),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(Dimens.iconSizeMedium)
                        )
                        Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                        Text("Submit MOOP Report")
                    }
                }
                
                // Offline Queue Status
                if (uiState.pendingReportsCount > 0) {
                    Spacer(modifier = Modifier.height(Dimens.paddingMedium))
                    OfflineQueueCard(
                        pendingCount = uiState.pendingReportsCount,
                        onViewQueue = { /* Navigate to queue view */ }
                    )
                }
            }
            
            // Map overlay (if enabled)
            AnimatedVisibility(
                visible = uiState.showMapView,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                RecentReportsMapOverlay(
                    recentReports = uiState.recentReports,
                    currentLocation = uiState.latitude to uiState.longitude,
                    onClose = { viewModel.toggleMapView() }
                )
            }
        }
    }
}

@Composable
private fun LocationSection(
    hasLocation: Boolean,
    latitude: Double,
    longitude: Double,
    onGetLocation: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (hasLocation) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (hasLocation) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                    Text(
                        text = if (hasLocation) "Location captured" else "Location required",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (hasLocation) {
                    Text(
                        text = "%.6f, %.6f".format(latitude, longitude),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 32.dp, top = Dimens.paddingExtraSmall)
                    )
                }
            }
            
            if (!hasLocation) {
                Button(
                    onClick = onGetLocation,
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Get Location")
                }
            } else {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Location captured",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun MOOPTypeSection(
    selectedType: MOOPType,
    onTypeSelected: (MOOPType) -> Unit
) {
    Column {
        Text(
            text = "Type of MOOP",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = Dimens.paddingSmall)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
        ) {
            items(MOOPType.values().toList()) { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    label = { 
                        Text(
                            type.name.lowercase().replace('_', ' ')
                                .replaceFirstChar { it.uppercase() }
                        )
                    },
                    leadingIcon = if (selectedType == type) {
                        {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.iconSizeSmall)
                            )
                        }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun DescriptionSection(
    description: String,
    onDescriptionChanged: (String) -> Unit,
    validationError: ValidationError?
) {
    Column {
        Text(
            text = "Description",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = Dimens.paddingSmall)
        )
        
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChanged,
            label = { Text("Describe the MOOP") },
            placeholder = { Text("What did you find? Be specific about location and nature of the matter.") },
            isError = validationError != null,
            supportingText = {
                if (validationError != null) {
                    Text(
                        text = validationError.message,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = "${description.length}/500 characters",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SeveritySection(
    selectedSeverity: MOOPSeverity?,
    onSeveritySelected: (MOOPSeverity) -> Unit
) {
    Column {
        Text(
            text = "Severity",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = Dimens.paddingSmall)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
        ) {
            MOOPSeverity.values().forEach { severity ->
                SeverityChip(
                    severity = severity,
                    selected = selectedSeverity == severity,
                    onSelected = { onSeveritySelected(severity) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SeverityChip(
    severity: MOOPSeverity,
    selected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (color, icon, description) = when (severity) {
        MOOPSeverity.LOW -> Triple(
            Color(0xFF4CAF50),
            Icons.Default.CheckCircle,
            "Minor cleanup"
        )
        MOOPSeverity.MEDIUM -> Triple(
            Color(0xFFFF9800),
            Icons.Default.Warning,
            "Moderate effort"
        )
        MOOPSeverity.HIGH -> Triple(
            Color(0xFFFF5252),
            Icons.Default.Error,
            "Urgent action"
        )
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelected() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ),
        border = if (selected) BorderStroke(2.dp, color) else null
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingSmall)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Dimens.iconSizeMedium)
            )
            Text(
                text = severity.name,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) color else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PhotoSection(
    photoUrl: String?,
    onAddPhoto: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    Column {
        Text(
            text = "Photo (Optional)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = Dimens.paddingSmall)
        )
        
        if (photoUrl == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clickable { onAddPhoto() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(Dimens.iconSizeLarge),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Add Photo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box {
                    // Photo preview placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(Dimens.iconSizeExtraLarge),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Remove button
                    IconButton(
                        onClick = onRemovePhoto,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(Dimens.paddingSmall)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove photo",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(Dimens.paddingExtraSmall)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdditionalOptionsSection(
    estimatedCleanupTime: Int?,
    requiresSpecialEquipment: Boolean,
    isHazardous: Boolean,
    notes: String,
    onEstimatedTimeChanged: (Int?) -> Unit,
    onSpecialEquipmentChanged: (Boolean) -> Unit,
    onHazardousChanged: (Boolean) -> Unit,
    onNotesChanged: (String) -> Unit
) {
    Column {
        Text(
            text = "Additional Information",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = Dimens.paddingSmall)
        )
        
        // Hazardous material toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isHazardous) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                Column {
                    Text("Hazardous Material")
                    Text(
                        text = "Chemical, sharp objects, biohazard",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = isHazardous,
                onCheckedChange = onHazardousChanged
            )
        }
        
        Divider(modifier = Modifier.padding(vertical = Dimens.paddingSmall))
        
        // Special equipment toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                Column {
                    Text("Special Equipment Needed")
                    Text(
                        text = "Tools, vehicles, protective gear",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = requiresSpecialEquipment,
                onCheckedChange = onSpecialEquipmentChanged
            )
        }
        
        Spacer(modifier = Modifier.height(Dimens.paddingMedium))
        
        // Additional notes
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChanged,
            label = { Text("Additional Notes (Optional)") },
            placeholder = { Text("Any other relevant information") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )
    }
}

@Composable
private fun OfflineQueueCard(
    pendingCount: Int,
    onViewQueue: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                Column {
                    Text(
                        text = "$pendingCount reports pending sync",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Will upload when network is available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            TextButton(onClick = onViewQueue) {
                Text("View")
            }
        }
    }
}

@Composable
private fun RecentReportsMapOverlay(
    recentReports: List<MOOPReportUi>,
    currentLocation: Pair<Double, Double>,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.paddingMedium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.paddingMedium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent MOOP Reports",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close map")
                }
            }
            
            // Map placeholder
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.iconSizeExtraLarge),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Map visualization would appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${recentReports.size} reports in last 24 hours",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.paddingMedium),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MOOPSeverity.values().forEach { severity ->
                    val color = when (severity) {
                        MOOPSeverity.LOW -> Color(0xFF4CAF50)
                        MOOPSeverity.MEDIUM -> Color(0xFFFF9800)
                        MOOPSeverity.HIGH -> Color(0xFFFF5252)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = severity.name,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

// Preview function
@Composable
fun MOOPReportingScreenPreview() {
    MaterialTheme {
        MOOPReportingScreen(
            onNavigateBack = {},
            onNavigateToMap = {},
            viewModel = PreviewMOOPReportingViewModel()
        )
    }
}

// Supporting classes for preview
@Composable
private fun rememberMOOPReportingViewModel(): MOOPReportingViewModel {
    return remember { MOOPReportingViewModel() }
}

private class PreviewMOOPReportingViewModel : MOOPReportingViewModel() {
    init {
        _uiState.value = MOOPReportingUiState(
            hasLocation = true,
            latitude = -32.397,
            longitude = 19.982,
            description = "Large pile of plastic bottles near the main road",
            severity = MOOPSeverity.MEDIUM,
            moopType = MOOPType.RECYCLING,
            pendingReportsCount = 3,
            recentReports = listOf(
                MOOPReportUi("1", -32.395, 19.980, MOOPSeverity.LOW),
                MOOPReportUi("2", -32.398, 19.983, MOOPSeverity.HIGH),
                MOOPReportUi("3", -32.396, 19.981, MOOPSeverity.MEDIUM)
            )
        )
    }
}

// ViewModel implementation
open class MOOPReportingViewModel(
    private val reportMOOPUseCase: ReportMOOPUseCase? = null,
    private val locationProvider: LocationProvider? = null,
    private val photoProvider: PhotoProvider? = null,
    private val moopRepository: MOOPRepository? = null
) {
    protected val _uiState = MutableStateFlow(MOOPReportingUiState())
    val uiState: StateFlow<MOOPReportingUiState> = _uiState.asStateFlow()
    
    private val coroutineScope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.Dispatchers.Default + kotlinx.coroutines.SupervisorJob()
    )
    
    init {
        loadPendingReportsCount()
        loadRecentReports()
    }
    
    fun getCurrentLocation() {
        coroutineScope.launch {
            _uiState.update { it.copy(isLoadingLocation = true) }
            
            try {
                val location = locationProvider?.getCurrentLocation()
                if (location != null) {
                    _uiState.update { 
                        it.copy(
                            hasLocation = true,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            isLoadingLocation = false
                        )
                    }
                } else {
                    // Mock location for preview/testing
                    _uiState.update { 
                        it.copy(
                            hasLocation = true,
                            latitude = -32.397,
                            longitude = 19.982,
                            isLoadingLocation = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoadingLocation = false,
                        error = "Failed to get location: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun updateDescription(description: String) {
        _uiState.update { 
            it.copy(
                description = description,
                validationErrors = it.validationErrors.filter { error ->
                    error != ValidationError.MISSING_DESCRIPTION &&
                    error != ValidationError.DESCRIPTION_TOO_SHORT &&
                    error != ValidationError.DESCRIPTION_TOO_LONG
                }
            )
        }
    }
    
    fun updateSeverity(severity: MOOPSeverity) {
        _uiState.update { it.copy(severity = severity) }
    }
    
    fun updateMoopType(type: MOOPType) {
        _uiState.update { it.copy(moopType = type) }
    }
    
    fun updateEstimatedCleanupTime(time: Int?) {
        _uiState.update { it.copy(estimatedCleanupTime = time) }
    }
    
    fun updateRequiresSpecialEquipment(requires: Boolean) {
        _uiState.update { it.copy(requiresSpecialEquipment = requires) }
    }
    
    fun updateIsHazardous(hazardous: Boolean) {
        _uiState.update { it.copy(isHazardous = hazardous) }
    }
    
    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }
    
    fun selectPhoto() {
        coroutineScope.launch {
            try {
                val photoUrl = photoProvider?.selectPhoto()
                _uiState.update { it.copy(photoUrl = photoUrl) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to select photo: ${e.message}")
                }
            }
        }
    }
    
    fun removePhoto() {
        _uiState.update { it.copy(photoUrl = null) }
    }
    
    fun toggleMapView() {
        _uiState.update { it.copy(showMapView = !it.showMapView) }
    }
    
    fun submitReport() {
        val state = _uiState.value
        
        if (!state.canSubmit) return
        
        coroutineScope.launch {
            _uiState.update { it.copy(isSubmitting = true, reportResult = null) }
            
            reportMOOPUseCase?.reportMOOP(
                latitude = state.latitude,
                longitude = state.longitude,
                description = state.description,
                severity = state.severity!!,
                moopType = state.moopType,
                photoUrl = state.photoUrl,
                estimatedCleanupTime = state.estimatedCleanupTime,
                requiresSpecialEquipment = state.requiresSpecialEquipment,
                isHazardous = state.isHazardous,
                notes = state.notes.takeIf { it.isNotBlank() }
            )?.collect { result ->
                _uiState.update { 
                    it.copy(
                        isSubmitting = result is ReportResult.Validating || 
                                      result is ReportResult.SavingLocally ||
                                      result is ReportResult.Syncing,
                        reportResult = result,
                        validationErrors = if (result is ReportResult.ValidationFailed) 
                            result.errors else emptyList()
                    )
                }
                
                if (result is ReportResult.Success) {
                    loadPendingReportsCount()
                    loadRecentReports()
                }
            } ?: run {
                // Mock successful submission for preview
                _uiState.update { 
                    it.copy(
                        isSubmitting = false,
                        reportResult = ReportResult.Success(
                            reportId = "mock-${System.currentTimeMillis()}",
                            syncStatus = SyncStatus.SYNCED,
                            message = "MOOP report submitted successfully"
                        )
                    )
                }
            }
        }
    }
    
    fun clearForm() {
        _uiState.update { 
            MOOPReportingUiState(
                pendingReportsCount = it.pendingReportsCount,
                recentReports = it.recentReports
            ) 
        }
    }
    
    private fun loadPendingReportsCount() {
        coroutineScope.launch {
            try {
                val count = moopRepository?.getPendingSyncReports()?.size ?: 0
                _uiState.update { it.copy(pendingReportsCount = count) }
            } catch (e: Exception) {
                // Ignore error for now
            }
        }
    }
    
    private fun loadRecentReports() {
        coroutineScope.launch {
            try {
                val reports = moopRepository?.getAllMOOPReports()
                    ?.filter { report ->
                        val twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
                        report.reportedTimestamp > twentyFourHoursAgo
                    }
                    ?.map { report ->
                        MOOPReportUi(
                            id = report.id,
                            latitude = report.latitude,
                            longitude = report.longitude,
                            severity = report.severity
                        )
                    } ?: emptyList()
                
                _uiState.update { it.copy(recentReports = reports) }
            } catch (e: Exception) {
                // Ignore error for now
            }
        }
    }
}

// UI State data class
data class MOOPReportingUiState(
    val isLoading: Boolean = false,
    val isLoadingLocation: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val hasLocation: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val description: String = "",
    val severity: MOOPSeverity? = null,
    val moopType: MOOPType = MOOPType.OTHER,
    val photoUrl: String? = null,
    val estimatedCleanupTime: Int? = null,
    val requiresSpecialEquipment: Boolean = false,
    val isHazardous: Boolean = false,
    val notes: String = "",
    val showMapView: Boolean = false,
    val pendingReportsCount: Int = 0,
    val recentReports: List<MOOPReportUi> = emptyList(),
    val reportResult: ReportResult? = null,
    val validationErrors: List<ValidationError> = emptyList()
) {
    val canSubmit: Boolean
        get() = hasLocation && 
                description.isNotBlank() && 
                severity != null && 
                !isSubmitting &&
                validationErrors.isEmpty()
}

// UI representation of MOOP report for map display
data class MOOPReportUi(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val severity: MOOPSeverity
)

// Interface abstractions for platform-specific implementations
interface LocationProvider {
    suspend fun getCurrentLocation(): LocationData?
}

interface PhotoProvider {
    suspend fun selectPhoto(): String?
}

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null
)