package io.asterixorobelix.afrikaburn.ui.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import io.asterixorobelix.afrikaburn.Dimens
import io.asterixorobelix.afrikaburn.models.Artist
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.TabDataSource
import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.tab_art
import afrikaburn.composeapp.generated.resources.tab_camps
import afrikaburn.composeapp.generated.resources.tab_events
import afrikaburn.composeapp.generated.resources.tab_mobile_art
import afrikaburn.composeapp.generated.resources.tab_performances
import afrikaburn.composeapp.generated.resources.tab_vehicles

@Composable
fun ProjectsScreen() {
    val tabs = listOf(
        stringResource(Res.string.tab_art),
        stringResource(Res.string.tab_performances),
        stringResource(Res.string.tab_events),
        stringResource(Res.string.tab_mobile_art),
        stringResource(Res.string.tab_vehicles),
        stringResource(Res.string.tab_camps)
    )
    
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { tabs.size }
    )
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
            }
        }
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val tabDataSources = listOf(
                TabDataSource("WTFArtworks.json", "Art"),
                TabDataSource("WTFPerformances.json", "Performances"),
                TabDataSource("WTFEvents.json", "Events"),
                TabDataSource("WTFRovingArtworks.json", "Mobile Art"),
                TabDataSource("WTFMutantVehicles.json", "Vehicles"),
                TabDataSource("WTFThemeCamps.json", "Camps")
            )
            
            ProjectTabContent(tabDataSources[page])
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun ProjectTabContent(tabDataSource: TabDataSource) {
    var projects by remember { mutableStateOf<List<ProjectItem>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(tabDataSource.fileName) {
        try {
            isLoading = true
            error = null
            // For now, use placeholder data until proper resource loading is implemented
            val sampleData = when (tabDataSource.fileName) {
                "WTFArtworks.json" -> listOf(
                    ProjectItem("Bee Cool Build", "A beehive-labyrinth hybrid with a purpose.", Artist("Megan Cleary and team")),
                    ProjectItem("OBSCURA", "In the magical realm of the OBSCURA, light and imagination intertwine.", Artist("Tiaan van Deventer")),
                    ProjectItem("Twist", "What do seashells, hurricanes, a Slinky toy, DNA, chameleon tails, and galaxies have in common?", Artist("Mari Schroeder"))
                )
                "WTFPerformances.json" -> listOf(
                    ProjectItem("Underdog", "Exploring our inner dog, our motley crew will go for a walk at dusk daily.", Artist("Junkanew and The OV Arts Collective")),
                    ProjectItem("The Dance of 1000 Flames", "The biggest fire dancing jam in Africa!", Artist("All the fire dancers of AfrikaBurn")),
                    ProjectItem("Azania", "Highlighting the richness of African music, dance, and culture.", Artist("Zizipho Gcasamba"))
                )
                "WTFEvents.json" -> listOf(
                    ProjectItem("Run Into The Blue", "On Thursday, 1 May, we invite fellow blue planet residents to join us.", Artist("")),
                    ProjectItem("The Midnight Melt", "Sometimes, you just need a toasted cheese.", Artist("The Melters")),
                    ProjectItem("Critical Tits Parade", "A celebration of body freedom and radical self-expression!", Artist("Tenille Lindeque"))
                )
                "WTFRovingArtworks.json" -> listOf(
                    ProjectItem("Mobile Art 1", "Beautiful roving artwork that moves around the Binnekring.", Artist("Artist 1")),
                    ProjectItem("Mobile Art 2", "Interactive mobile installation.", Artist("Artist 2"))
                )
                "WTFMutantVehicles.json" -> listOf(
                    ProjectItem("Fire Truck", "A spectacular fire-breathing vehicle.", Artist("Vehicle Team 1")),
                    ProjectItem("Art Car", "Mobile dance floor and art installation.", Artist("Vehicle Team 2"))
                )
                "WTFThemeCamps.json" -> listOf(
                    ProjectItem("The Vagabonds", "A wellness and relaxation camp where everyone feels at home.", Artist(""), "", "Fam • Day Time"),
                    ProjectItem("ALEGRA SPACE STATION", "The Burn equivalent of the International Space Station.", Artist(""), "", "Fam • Other"),
                    ProjectItem("Cactus Rising", "Get ready for a prickly adventure!", Artist(""), "", "Fam(ish) • Day Time")
                )
                else -> emptyList()
            }
            projects = sampleData
        } catch (e: Exception) {
            error = "Failed to load ${tabDataSource.displayName}: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error!!,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        projects != null -> {
            // Filter projects based on search query
            val filteredProjects = projects!!.filter { project ->
                searchQuery.isEmpty() || 
                project.name.contains(searchQuery, ignoreCase = true) ||
                project.description.contains(searchQuery, ignoreCase = true) ||
                project.artist.name.contains(searchQuery, ignoreCase = true)
            }
            
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.paddingSmall),
                    placeholder = {
                        Text(
                            text = "Search ${tabDataSource.displayName.lowercase()}...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    shape = MaterialTheme.shapes.large,
                    singleLine = true
                )
                
                // Results
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.paddingMedium),
                    verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                    }
                    
                    if (filteredProjects.isEmpty() && searchQuery.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimens.paddingLarge),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No ${tabDataSource.displayName.lowercase()} found matching \"$searchQuery\"",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(filteredProjects) { project ->
                            ProjectCard(project = project)
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(Dimens.paddingMedium))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectCard(project: ProjectItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationSmall)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingLarge)
        ) {
            // Title
            Text(
                text = project.name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = Dimens.paddingSmall)
            )
            
            // Artist info with icon
            if (project.artist.name.isNotEmpty()) {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = Dimens.paddingSmall)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Artist",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.paddingMedium)
                    )
                    Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                    Text(
                        text = project.artist.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Divider for visual separation
            if (project.artist.name.isNotEmpty()) {
                Divider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(bottom = Dimens.paddingSmall)
                )
            }
            
            // Description
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
            
            // Status badge
            if (project.status.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimens.paddingMedium))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                ) {
                    Text(
                        text = project.status,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = Dimens.paddingSmall, vertical = Dimens.paddingExtraSmall)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ProjectsScreenPreview() {
    io.asterixorobelix.afrikaburn.AppTheme {
        ProjectsScreen()
    }
}