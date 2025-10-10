package io.asterixorobelix.afrikaburn.ui.discovery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
import io.asterixorobelix.afrikaburn.domain.model.ThemeCamp
import io.asterixorobelix.afrikaburn.presentation.discovery.ThemeCampsViewModel
import io.asterixorobelix.afrikaburn.presentation.discovery.ThemeCampsUiState
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
fun ThemeCampsScreen(
    onNavigateToCamp: (ThemeCamp) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ThemeCampsViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    
    ThemeCampsContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::updateSearchQuery,
        onToggleActivityFilter = viewModel::toggleActivityFilter,
        onToggleAmenityFilter = viewModel::toggleAmenityFilter,
        onToggleFavorite = viewModel::toggleFavorite,
        onNavigateToCamp = onNavigateToCamp,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ThemeCampsContent(
    uiState: ThemeCampsUiState,
    onSearchQueryChange: (String) -> Unit,
    onToggleActivityFilter: (String) -> Unit,
    onToggleAmenityFilter: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onNavigateToCamp: (ThemeCamp) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilters by remember { mutableStateOf(false) }
    var expandedCampId by remember { mutableStateOf<String?>(null) }
    val bottomSheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Bar
        SearchBar(
            query = uiState.searchQuery,
            onQueryChange = onSearchQueryChange,
            onSearch = { /* Search is handled automatically */ },
            active = false,
            onActiveChange = { /* Keep search bar static */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingMedium),
            placeholder = { Text("Search theme camps...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                Row {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                showFilters = true
                                bottomSheetState.show()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (uiState.hasActiveFilters) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        ) {}
        
        // Active filters display
        if (uiState.hasActiveFilters) {
            ActiveFiltersRow(
                activityFilters = uiState.selectedActivities,
                amenityFilters = uiState.selectedAmenities,
                onRemoveActivityFilter = onToggleActivityFilter,
                onRemoveAmenityFilter = onToggleAmenityFilter,
                modifier = Modifier.padding(horizontal = Dimens.paddingMedium)
            )
        }
        
        // Theme Camps List
        when {
            uiState.isLoading -> LoadingContent()
            uiState.error != null -> ErrorContent(error = uiState.error)
            uiState.filteredCamps.isEmpty() -> EmptyContent()
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                    contentPadding = PaddingValues(Dimens.paddingMedium)
                ) {
                    items(
                        items = uiState.filteredCamps,
                        key = { it.id }
                    ) { camp ->
                        ThemeCampCard(
                            camp = camp,
                            isFavorite = uiState.favoriteCampIds.contains(camp.id),
                            isExpanded = expandedCampId == camp.id,
                            onToggleFavorite = { onToggleFavorite(camp.id) },
                            onToggleExpanded = { 
                                expandedCampId = if (expandedCampId == camp.id) null else camp.id 
                            },
                            onNavigate = { onNavigateToCamp(camp) }
                        )
                    }
                }
            }
        }
    }
    
    // Filter Bottom Sheet
    if (showFilters) {
        ModalBottomSheet(
            onDismissRequest = {
                showFilters = false
            },
            sheetState = bottomSheetState
        ) {
            FilterContent(
                availableActivities = uiState.availableActivities,
                availableAmenities = uiState.availableAmenities,
                selectedActivities = uiState.selectedActivities,
                selectedAmenities = uiState.selectedAmenities,
                onToggleActivityFilter = onToggleActivityFilter,
                onToggleAmenityFilter = onToggleAmenityFilter,
                modifier = Modifier.padding(bottom = Dimens.paddingLarge)
            )
        }
    }
}

@Composable
private fun ThemeCampCard(
    camp: ThemeCamp,
    isFavorite: Boolean,
    isExpanded: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleExpanded: () -> Unit,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggleExpanded() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationSmall)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingMedium)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = camp.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (camp.activities.isNotEmpty()) {
                        Text(
                            text = camp.activities.take(3).joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(Dimens.iconSizeLarge)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Expanded Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = Dimens.paddingMedium)
                ) {
                    // Description
                    camp.description?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = Dimens.paddingSmall)
                        )
                    }
                    
                    // Activities
                    if (camp.activities.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Dimens.paddingExtraSmall))
                        Text(
                            text = "Activities",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        ChipsRow(
                            items = camp.activities,
                            modifier = Modifier.padding(top = Dimens.paddingExtraSmall)
                        )
                    }
                    
                    // Amenities
                    if (camp.amenities.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                        Text(
                            text = "Amenities",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        ChipsRow(
                            items = camp.amenities,
                            modifier = Modifier.padding(top = Dimens.paddingExtraSmall)
                        )
                    }
                    
                    // Contact Info
                    camp.contactInfo?.let { contact ->
                        Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                        Text(
                            text = "Contact: $contact",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Navigate Button
                    Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate() }
                            .padding(vertical = Dimens.paddingSmall),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Navigate to camp",
                            modifier = Modifier.size(Dimens.iconSizeSmall),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Navigate to camp location",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = Dimens.paddingExtraSmall)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipsRow(
    items: List<String>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingExtraSmall),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingExtraSmall)
    ) {
        items.forEach { item ->
            AssistChip(
                onClick = { /* No action for display chips */ },
                label = { 
                    Text(
                        text = item,
                        style = MaterialTheme.typography.labelSmall
                    ) 
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveFiltersRow(
    activityFilters: Set<String>,
    amenityFilters: Set<String>,
    onRemoveActivityFilter: (String) -> Unit,
    onRemoveAmenityFilter: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.padding(vertical = Dimens.paddingSmall),
        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingExtraSmall),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingExtraSmall)
    ) {
        activityFilters.forEach { activity ->
            FilterChip(
                selected = true,
                onClick = { onRemoveActivityFilter(activity) },
                label = { Text(activity, style = MaterialTheme.typography.labelSmall) },
                trailingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove filter",
                        modifier = Modifier.size(Dimens.iconSizeSmall)
                    )
                }
            )
        }
        amenityFilters.forEach { amenity ->
            FilterChip(
                selected = true,
                onClick = { onRemoveAmenityFilter(amenity) },
                label = { Text(amenity, style = MaterialTheme.typography.labelSmall) },
                trailingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove filter",
                        modifier = Modifier.size(Dimens.iconSizeSmall)
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterContent(
    availableActivities: List<String>,
    availableAmenities: List<String>,
    selectedActivities: Set<String>,
    selectedAmenities: Set<String>,
    onToggleActivityFilter: (String) -> Unit,
    onToggleAmenityFilter: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingMedium)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Filter Theme Camps",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = Dimens.paddingMedium)
        )
        
        // Activities Section
        if (availableActivities.isNotEmpty()) {
            Text(
                text = "Activities",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = Dimens.paddingSmall)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingExtraSmall),
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingExtraSmall),
                modifier = Modifier.padding(bottom = Dimens.paddingMedium)
            ) {
                availableActivities.forEach { activity ->
                    FilterChip(
                        selected = selectedActivities.contains(activity),
                        onClick = { onToggleActivityFilter(activity) },
                        label = { Text(activity, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }
        }
        
        // Amenities Section
        if (availableAmenities.isNotEmpty()) {
            Text(
                text = "Amenities",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = Dimens.paddingSmall)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingExtraSmall),
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingExtraSmall)
            ) {
                availableAmenities.forEach { amenity ->
                    FilterChip(
                        selected = selectedAmenities.contains(amenity),
                        onClick = { onToggleAmenityFilter(amenity) },
                        label = { Text(amenity, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Loading theme camps...",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ErrorContent(error: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Error: $error",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No theme camps found",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ThemeCampsUiState is defined in the ViewModel file

@Preview
@Composable
private fun ThemeCampsScreenPreview() {
    AppTheme {
        val sampleCamps = listOf(
            ThemeCamp(
                id = "1",
                eventId = "event1",
                name = "Desert Oasis Camp",
                description = "A peaceful sanctuary in the heart of the playa offering workshops, healing sessions, and community gatherings.",
                latitude = -32.3921,
                longitude = 20.8559,
                contactInfo = "desert.oasis@camps.burn",
                activities = listOf("Workshops", "Healing", "Meditation", "Yoga"),
                amenities = listOf("Shade", "Seating", "Water", "First Aid"),
                qrCode = "DESERTOASIS2024",
                photoUrl = null,
                isHidden = false,
                unlockTimestamp = null,
                lastUpdated = System.currentTimeMillis()
            ),
            ThemeCamp(
                id = "2",
                eventId = "event1",
                name = "Fire Dancers Collective",
                description = "Home to the playa's premier fire performers. Join us for nightly fire spinning shows and workshops.",
                latitude = -32.3931,
                longitude = 20.8569,
                contactInfo = "@firedancers",
                activities = listOf("Fire Spinning", "Performances", "Dancing", "Workshops"),
                amenities = listOf("Stage", "Sound System", "Lighting", "Seating"),
                qrCode = "FIREDANCE2024",
                photoUrl = null,
                isHidden = false,
                unlockTimestamp = null,
                lastUpdated = System.currentTimeMillis()
            )
        )
        
        ThemeCampsContent(
            uiState = ThemeCampsUiState(
                allCamps = sampleCamps,
                filteredCamps = sampleCamps,
                availableActivities = ThemeCamp.COMMON_ACTIVITIES,
                availableAmenities = ThemeCamp.COMMON_AMENITIES
            ),
            onSearchQueryChange = {},
            onToggleActivityFilter = {},
            onToggleAmenityFilter = {},
            onToggleFavorite = {},
            onNavigateToCamp = {}
        )
    }
}