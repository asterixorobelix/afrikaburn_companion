package io.asterixorobelix.afrikaburn.ui.discovery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
import io.asterixorobelix.afrikaburn.domain.model.ArtInstallation
import io.asterixorobelix.afrikaburn.domain.model.EventPerformance
import io.asterixorobelix.afrikaburn.domain.model.MutantVehicle
import io.asterixorobelix.afrikaburn.domain.model.ThemeCamp
import io.asterixorobelix.afrikaburn.presentation.discovery.EventDiscoveryViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

enum class DiscoveryTab {
    THEME_CAMPS,
    ART,
    MUTANT_VEHICLES,
    PERFORMANCES
}

@Composable
fun EventDiscoveryScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: EventDiscoveryViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(DiscoveryTab.THEME_CAMPS) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "Discover AfrikaBurn",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(Dimens.paddingMedium)
        )

        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            DiscoveryTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = when (tab) {
                                DiscoveryTab.THEME_CAMPS -> "Camps"
                                DiscoveryTab.ART -> "Art"
                                DiscoveryTab.MUTANT_VEHICLES -> "Vehicles"
                                DiscoveryTab.PERFORMANCES -> "Performances"
                            },
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                )
            }
        }

        when {
            uiState.isLoading -> LoadingContent()
            uiState.error != null -> ErrorContent(error = uiState.error!!)
            else -> {
                when (selectedTab) {
                    DiscoveryTab.THEME_CAMPS -> ThemeCampsContent(camps = uiState.themeCamps)
                    DiscoveryTab.ART -> ArtInstallationsContent(artInstallations = uiState.artInstallations)
                    DiscoveryTab.MUTANT_VEHICLES -> MutantVehiclesContent(vehicles = uiState.mutantVehicles)
                    DiscoveryTab.PERFORMANCES -> PerformancesContent(performances = uiState.performances)
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.paddingMedium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Loading experiences...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ErrorContent(error: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.paddingMedium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error: $error",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun ThemeCampsContent(camps: List<ThemeCamp>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(Dimens.paddingMedium)
    ) {
        items(camps) { camp ->
            ThemeCampCard(camp = camp)
        }
    }
}

@Composable
private fun ThemeCampCard(camp: ThemeCamp) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationSmall)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
        ) {
            Text(
                text = camp.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            camp.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (camp.activities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimens.paddingExtraSmall))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(Dimens.paddingExtraSmall))
                Text(
                    text = "Activities: ${camp.activities.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ArtInstallationsContent(artInstallations: List<ArtInstallation>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(Dimens.paddingMedium)
    ) {
        items(artInstallations) { art ->
            ArtInstallationCard(art = art)
        }
    }
}

@Composable
private fun ArtInstallationCard(art: ArtInstallation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationSmall)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
        ) {
            Text(
                text = art.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "by ${art.artistName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            art.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun MutantVehiclesContent(vehicles: List<MutantVehicle>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(Dimens.paddingMedium)
    ) {
        items(vehicles) { vehicle ->
            MutantVehicleCard(vehicle = vehicle)
        }
    }
}

@Composable
private fun MutantVehicleCard(vehicle: MutantVehicle) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationSmall)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
        ) {
            Text(
                text = vehicle.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            vehicle.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            vehicle.operatingHours?.let {
                Spacer(modifier = Modifier.height(Dimens.paddingExtraSmall))
                Text(
                    text = "Operating Hours: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PerformancesContent(performances: List<EventPerformance>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(Dimens.paddingMedium)
    ) {
        items(performances) { performance ->
            PerformanceCard(performance = performance)
        }
    }
}

@Composable
private fun PerformanceCard(performance: EventPerformance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationSmall)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
        ) {
            Text(
                text = performance.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (performance.performers.isNotEmpty()) {
                Text(
                    text = "by ${performance.performers.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = performance.performanceType.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            performance.venueName?.let {
                Text(
                    text = "At: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview
@Composable
private fun EventDiscoveryScreenPreview() {
    AppTheme {
        EventDiscoveryScreen()
    }
}