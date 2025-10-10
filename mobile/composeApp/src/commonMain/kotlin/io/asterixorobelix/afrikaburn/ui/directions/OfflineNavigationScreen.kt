package io.asterixorobelix.afrikaburn.ui.directions

import afrikaburn.composeapp.generated.resources.New_Turnoff
import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.button_directions
import afrikaburn.composeapp.generated.resources.cd_about_page4_image
import afrikaburn.composeapp.generated.resources.direction_content
import afrikaburn.composeapp.generated.resources.direction_sub_title
import afrikaburn.composeapp.generated.resources.direction_title
import afrikaburn.composeapp.generated.resources.direction_url
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val GPS_LAT = -32.482474
private const val GPS_LON = 19.897824
private const val DISTANCE_FROM_CAPE_TOWN = "290 km"
private const val DISTANCE_FROM_CERES = "180 km"

@Composable
fun OfflineNavigationScreen() {
    var isOfflineMapDownloaded by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = stringResource(Res.string.direction_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingMedium)
        )

        // GPS Coordinates Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingMedium),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationSmall)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.paddingMedium),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = "GPS coordinates",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(Dimens.iconSizeMedium)
                )
                Text(
                    text = "GPS: $GPS_LAT, $GPS_LON",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(start = Dimens.paddingSmall)
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.paddingMedium))

        // Map Image
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingMedium),
            elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationSmall)
        ) {
            Box {
                Image(
                    painter = painterResource(Res.drawable.New_Turnoff),
                    contentDescription = stringResource(Res.string.cd_about_page4_image),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.paddingLarge * 10)
                        .clip(RoundedCornerShape(Dimens.cornerRadiusMedium)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.paddingMedium))

        // Offline Map Download Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingMedium),
            colors = CardDefaults.cardColors(
                containerColor = if (isOfflineMapDownloaded) {
                    MaterialTheme.colorScheme.tertiaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationSmall)
        ) {
            Column(
                modifier = Modifier.padding(Dimens.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download offline map",
                        tint = if (isOfflineMapDownloaded) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                    Text(
                        text = if (isOfflineMapDownloaded) {
                            "Offline Map Ready!"
                        } else {
                            "Download Offline Map"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = Dimens.paddingSmall)
                    )
                }

                Text(
                    text = "Download the route for offline navigation. Essential for when you lose cell signal!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!isOfflineMapDownloaded && downloadProgress > 0) {
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Button(
                    onClick = { 
                        if (!isOfflineMapDownloaded) {
                            // Simulate download
                            downloadProgress = 0.5f
                            isOfflineMapDownloaded = true
                        }
                    },
                    enabled = !isOfflineMapDownloaded,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isOfflineMapDownloaded) Icons.Default.Navigation else Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.iconSizeSmall)
                    )
                    Text(
                        text = if (isOfflineMapDownloaded) "Navigate Offline" else "Download Map (25MB)",
                        modifier = Modifier.padding(start = Dimens.paddingSmall)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.paddingMedium))

        // Distance Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingMedium),
            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
        ) {
            DistanceCard(
                modifier = Modifier.weight(1f),
                fromLocation = "Cape Town",
                distance = DISTANCE_FROM_CAPE_TOWN,
                duration = "3 hours"
            )
            DistanceCard(
                modifier = Modifier.weight(1f),
                fromLocation = "Ceres",
                distance = DISTANCE_FROM_CERES,
                duration = "2 hours"
            )
        }

        Spacer(modifier = Modifier.height(Dimens.paddingMedium))

        // Directions Content
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingMedium),
            elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationSmall)
        ) {
            Column(
                modifier = Modifier.padding(Dimens.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
            ) {
                Text(
                    text = stringResource(Res.string.direction_sub_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(Res.string.direction_content),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.paddingMedium))

        // Open in Maps Button
        FilledTonalButton(
            onClick = { /* Open in maps app */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingMedium),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(stringResource(Res.string.button_directions))
        }

        Spacer(modifier = Modifier.height(Dimens.paddingLarge))
    }
}

@Composable
private fun DistanceCard(
    modifier: Modifier = Modifier,
    fromLocation: String,
    distance: String,
    duration: String
) {
    OutlinedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = fromLocation,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = distance,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = duration,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun OfflineNavigationScreenPreview() {
    AppTheme {
        OfflineNavigationScreen()
    }
}