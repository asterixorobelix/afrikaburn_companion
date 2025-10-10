package io.asterixorobelix.afrikaburn.ui.discovery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
import io.asterixorobelix.afrikaburn.domain.model.ArtInstallation
import io.asterixorobelix.afrikaburn.domain.model.ArtType
import io.asterixorobelix.afrikaburn.domain.model.ViewingTime
import io.asterixorobelix.afrikaburn.domain.model.getCurrentTimestamp
import io.asterixorobelix.afrikaburn.presentation.discovery.EventDiscoveryViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtInstallationsScreen(
    onBackClick: () -> Unit = {},
    onNavigateToArt: (ArtInstallation) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: EventDiscoveryViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedArtTypes by remember { mutableStateOf(setOf<ArtType>()) }
    var showInteractiveOnly by remember { mutableStateOf(false) }
    var showNightIlluminatedOnly by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    
    // Filter art installations based on search and filters
    val filteredArt = remember(searchQuery, selectedArtTypes, showInteractiveOnly, showNightIlluminatedOnly, uiState.artInstallations) {
        uiState.artInstallations.filter { art ->
            val matchesSearch = searchQuery.isBlank() || 
                art.name.contains(searchQuery, ignoreCase = true) ||
                art.artistName.contains(searchQuery, ignoreCase = true) ||
                art.description?.contains(searchQuery, ignoreCase = true) == true
            
            val matchesType = selectedArtTypes.isEmpty() || selectedArtTypes.contains(art.artType)
            val matchesInteractive = !showInteractiveOnly || art.isInteractive
            val matchesNightIlluminated = !showNightIlluminatedOnly || art.isNightIlluminated
            
            matchesSearch && matchesType && matchesInteractive && matchesNightIlluminated
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Art Installations") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Toggle filters",
                            tint = if (showFilters || selectedArtTypes.isNotEmpty() || showInteractiveOnly || showNightIlluminatedOnly) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.paddingMedium)
                    .padding(top = Dimens.paddingSmall)
            )
            
            // Filters
            if (showFilters) {
                FiltersSection(
                    selectedArtTypes = selectedArtTypes,
                    onArtTypeToggle = { artType ->
                        selectedArtTypes = if (selectedArtTypes.contains(artType)) {
                            selectedArtTypes - artType
                        } else {
                            selectedArtTypes + artType
                        }
                    },
                    showInteractiveOnly = showInteractiveOnly,
                    onInteractiveToggle = { showInteractiveOnly = !showInteractiveOnly },
                    showNightIlluminatedOnly = showNightIlluminatedOnly,
                    onNightIlluminatedToggle = { showNightIlluminatedOnly = !showNightIlluminatedOnly },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.paddingMedium)
                        .padding(vertical = Dimens.paddingSmall)
                )
            }
            
            // Results count
            if (filteredArt.size != uiState.artInstallations.size || searchQuery.isNotEmpty()) {
                Text(
                    text = "${filteredArt.size} art installations found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Dimens.paddingMedium, vertical = Dimens.paddingSmall)
                )
            }
            
            // Art installations list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = Dimens.paddingMedium,
                    vertical = Dimens.paddingSmall
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
            ) {
                items(filteredArt) { art ->
                    ArtInstallationCard(
                        art = art,
                        onClick = { onNavigateToArt(art) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        placeholder = { Text("Search art, artists, or descriptions") },
        singleLine = true,
        shape = RoundedCornerShape(Dimens.cornerRadiusLarge),
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FiltersSection(
    selectedArtTypes: Set<ArtType>,
    onArtTypeToggle: (ArtType) -> Unit,
    showInteractiveOnly: Boolean,
    onInteractiveToggle: () -> Unit,
    showNightIlluminatedOnly: Boolean,
    onNightIlluminatedToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.cornerRadiusMedium),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = Dimens.elevationSmall
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
        ) {
            // Quick filters
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
            ) {
                FilterChip(
                    selected = showInteractiveOnly,
                    onClick = onInteractiveToggle,
                    label = { Text("Interactive") }
                )
                FilterChip(
                    selected = showNightIlluminatedOnly,
                    onClick = onNightIlluminatedToggle,
                    label = { Text("Night Illuminated") }
                )
            }
            
            Divider()
            
            // Art type filters
            Text(
                "Art Types",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = Dimens.paddingSmall)
            )
            
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
            ) {
                ArtType.entries.forEach { artType ->
                    FilterChip(
                        selected = selectedArtTypes.contains(artType),
                        onClick = { onArtTypeToggle(artType) },
                        label = { Text(artType.displayName) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtInstallationCard(
    art: ArtInstallation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.cornerRadiusMedium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationSmall)
    ) {
        Column {
            // Image gallery
            if (art.photoUrls.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(topStart = Dimens.cornerRadiusMedium, topEnd = Dimens.cornerRadiusMedium))
                ) {
                    if (art.photoUrls.size > 1) {
                        LazyRow(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(0.dp),
                            horizontalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            items(art.photoUrls) { photoUrl ->
                                // TODO: Replace with actual image loading
                                Box(
                                    modifier = Modifier
                                        .height(200.dp)
                                        .aspectRatio(1.5f)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Text(
                                        text = "Image",
                                        modifier = Modifier.align(Alignment.Center),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        // TODO: Replace with actual image loading
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = "Image",
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Photo indicator
                    if (art.photoUrls.size > 1) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(Dimens.paddingSmall)
                                .background(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(Dimens.cornerRadiusSmall)
                                )
                                .padding(horizontal = Dimens.paddingSmall, vertical = Dimens.paddingExtraSmall)
                        ) {
                            Text(
                                text = "1/${art.photoUrls.size}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
            ) {
                // Title and artist
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = art.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "by ${art.artistName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Location icon
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.iconSizeMedium)
                    )
                }
                
                // Description
                art.description?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Features and info chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
                ) {
                    // Art type chip
                    SuggestionChip(
                        onClick = {},
                        label = { Text(art.artType.displayName) }
                    )
                    
                    // Interactive chip
                    if (art.isInteractive) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Interactive") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                    
                    // Night illuminated chip
                    if (art.isNightIlluminated) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Night Illuminated") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                                )
                            }
                        )
                    }
                }
                
                // Dimensions and materials
                if (art.dimensions != null || art.materials.isNotEmpty()) {
                    Divider(modifier = Modifier.padding(vertical = Dimens.paddingSmall))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        art.dimensions?.let {
                            Text(
                                text = "Size: $it",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        if (art.materials.isNotEmpty()) {
                            Text(
                                text = art.materials.take(3).joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                // Navigation button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Optimal viewing times
                    val viewingTimes = art.getOptimalViewingTimes()
                    if (viewingTimes.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingExtraSmall)
                        ) {
                            Text(
                                text = "Best viewed:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            viewingTimes.forEach { time ->
                                Text(
                                    text = time.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    // Navigate button
                    AssistChip(
                        onClick = onClick,
                        label = { Text("Navigate") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Navigation,
                                contentDescription = null,
                                modifier = Modifier.size(AssistChipDefaults.IconSize)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ArtInstallationsScreenPreview() {
    AppTheme {
        ArtInstallationsScreen()
    }
}

@Preview
@Composable
private fun ArtInstallationCardPreview() {
    val sampleArt = ArtInstallation(
        id = "1",
        eventId = "event1",
        name = "The Burning Embrace",
        artistName = "Sarah Johnson",
        description = "A 30-foot tall sculpture made from recycled metal and wood, representing the connection between humans and nature. Interactive elements allow visitors to create sounds by touching different parts.",
        latitude = -32.0,
        longitude = 24.0,
        artType = ArtType.SCULPTURE,
        materials = listOf("Recycled Metal", "Wood", "LED Lights", "Solar Panels"),
        dimensions = "30ft x 20ft x 15ft",
        isInteractive = true,
        isNightIlluminated = true,
        accessibilityNotes = "Wheelchair accessible viewing area. Interactive elements at various heights.",
        qrCode = "ABC12345",
        photoUrls = listOf("https://example.com/photo1.jpg", "https://example.com/photo2.jpg"),
        isHidden = false,
        unlockTimestamp = null,
        lastUpdated = getCurrentTimestamp()
    )
    
    AppTheme {
        ArtInstallationCard(
            art = sampleArt,
            onClick = {},
            modifier = Modifier.padding(Dimens.paddingMedium)
        )
    }
}