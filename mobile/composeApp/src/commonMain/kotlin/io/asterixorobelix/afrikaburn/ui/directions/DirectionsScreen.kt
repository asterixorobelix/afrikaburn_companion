package io.asterixorobelix.afrikaburn.ui.directions

import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.button_directions
import afrikaburn.composeapp.generated.resources.cd_directions_car_icon
import afrikaburn.composeapp.generated.resources.cd_directions_collapse
import afrikaburn.composeapp.generated.resources.cd_directions_expand
import afrikaburn.composeapp.generated.resources.cd_directions_gps_icon
import afrikaburn.composeapp.generated.resources.cd_directions_location_icon
import afrikaburn.composeapp.generated.resources.cd_directions_warning_icon
import afrikaburn.composeapp.generated.resources.direction_title
import afrikaburn.composeapp.generated.resources.direction_url
import afrikaburn.composeapp.generated.resources.directions_gps_latitude
import afrikaburn.composeapp.generated.resources.directions_gps_latitude_value
import afrikaburn.composeapp.generated.resources.directions_gps_longitude
import afrikaburn.composeapp.generated.resources.directions_gps_longitude_value
import afrikaburn.composeapp.generated.resources.directions_location_area
import afrikaburn.composeapp.generated.resources.directions_location_country
import afrikaburn.composeapp.generated.resources.directions_location_name
import afrikaburn.composeapp.generated.resources.directions_note_fuel
import afrikaburn.composeapp.generated.resources.directions_note_fuel_detail
import afrikaburn.composeapp.generated.resources.directions_note_no_signal
import afrikaburn.composeapp.generated.resources.directions_note_no_signal_detail
import afrikaburn.composeapp.generated.resources.directions_note_not_national_park
import afrikaburn.composeapp.generated.resources.directions_note_not_national_park_detail
import afrikaburn.composeapp.generated.resources.directions_open_maps
import afrikaburn.composeapp.generated.resources.directions_section_gps
import afrikaburn.composeapp.generated.resources.directions_section_important_notes
import afrikaburn.composeapp.generated.resources.directions_section_location
import afrikaburn.composeapp.generated.resources.directions_section_travel_times
import afrikaburn.composeapp.generated.resources.directions_travel_calvinia
import afrikaburn.composeapp.generated.resources.directions_travel_calvinia_time
import afrikaburn.composeapp.generated.resources.directions_travel_cape_town
import afrikaburn.composeapp.generated.resources.directions_travel_cape_town_time
import afrikaburn.composeapp.generated.resources.directions_travel_ceres
import afrikaburn.composeapp.generated.resources.directions_travel_ceres_time
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.asterixorobelix.afrikaburn.ui.components.ExternalLinkConfirmationDialog
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val ROTATION_EXPANDED = 180f
private const val ROTATION_COLLAPSED = 0f

@Composable
fun DirectionsScreen() {
    val uriHandler = LocalUriHandler.current
    val mapsUrl = stringResource(Res.string.direction_url)
    var pendingUrl by remember { mutableStateOf<String?>(null) }

    pendingUrl?.let { url ->
        ExternalLinkConfirmationDialog(
            onConfirm = {
                uriHandler.openUri(url)
                pendingUrl = null
            },
            onDismiss = { pendingUrl = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        DirectionsHeader()

        Column(
            modifier = Modifier.padding(horizontal = Dimens.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
        ) {
            LocationSection()

            TravelTimesSection()

            ImportantNotesSection()

            GPSCoordinatesSection()

            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

            OpenMapsButton(onClick = { pendingUrl = mapsUrl })

            Spacer(modifier = Modifier.height(Dimens.paddingLarge))
        }
    }
}

@Composable
private fun DirectionsHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeroLocationIcon()

        Spacer(modifier = Modifier.height(Dimens.paddingMedium))

        Text(
            text = stringResource(Res.string.direction_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(Res.string.directions_location_name),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Dimens.paddingExtraSmall)
        )
    }
}

@Composable
private fun HeroLocationIcon() {
    Box(
        modifier = Modifier
            .size(Dimens.directionsHeroContainerSize)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = stringResource(Res.string.cd_directions_location_icon),
            modifier = Modifier.size(Dimens.directionsHeroIconSize),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun LocationSection() {
    val locationContentDescription = stringResource(Res.string.cd_directions_location_icon)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationSmall)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingMedium)
        ) {
            SectionHeader(
                title = stringResource(Res.string.directions_section_location),
                icon = Icons.Filled.LocationOn,
                contentDescription = locationContentDescription
            )

            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

            LocationInfoRow(
                label = stringResource(Res.string.directions_location_name),
                isPrimary = true
            )
            LocationInfoRow(
                label = stringResource(Res.string.directions_location_area),
                isPrimary = false
            )
            LocationInfoRow(
                label = stringResource(Res.string.directions_location_country),
                isPrimary = false
            )
        }
    }
}

@Composable
private fun LocationInfoRow(
    label: String,
    isPrimary: Boolean
) {
    Text(
        text = label,
        style = if (isPrimary) {
            MaterialTheme.typography.titleMedium
        } else {
            MaterialTheme.typography.bodyMedium
        },
        color = if (isPrimary) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = Modifier.padding(vertical = Dimens.paddingExtraSmall)
    )
}

@Composable
private fun TravelTimesSection() {
    val travelContentDescription = stringResource(Res.string.cd_directions_car_icon)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationSmall)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingMedium)
        ) {
            SectionHeader(
                title = stringResource(Res.string.directions_section_travel_times),
                icon = Icons.Filled.DirectionsCar,
                contentDescription = travelContentDescription
            )

            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TravelTimeCard(
                    origin = stringResource(Res.string.directions_travel_cape_town),
                    duration = stringResource(Res.string.directions_travel_cape_town_time),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                TravelTimeCard(
                    origin = stringResource(Res.string.directions_travel_ceres),
                    duration = stringResource(Res.string.directions_travel_ceres_time),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                TravelTimeCard(
                    origin = stringResource(Res.string.directions_travel_calvinia),
                    duration = stringResource(Res.string.directions_travel_calvinia_time),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TravelTimeCard(
    origin: String,
    duration: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingSmall),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = duration,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = origin,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ImportantNotesSection() {
    var isExpanded by remember { mutableStateOf(true) }
    val warningContentDescription = stringResource(Res.string.cd_directions_warning_icon)
    val expandContentDescription = if (isExpanded) {
        stringResource(Res.string.cd_directions_collapse)
    } else {
        stringResource(Res.string.cd_directions_expand)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationSmall)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ExpandableSectionHeader(
                title = stringResource(Res.string.directions_section_important_notes),
                icon = Icons.Filled.Warning,
                iconTint = MaterialTheme.colorScheme.error,
                contentDescription = warningContentDescription,
                isExpanded = isExpanded,
                expandContentDescription = expandContentDescription,
                onClick = { isExpanded = !isExpanded }
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = Dimens.paddingMedium,
                        end = Dimens.paddingMedium,
                        bottom = Dimens.paddingMedium
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
                ) {
                    ImportantNoteItem(
                        icon = Icons.Filled.Map,
                        title = stringResource(Res.string.directions_note_not_national_park),
                        description = stringResource(
                            Res.string.directions_note_not_national_park_detail
                        )
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = Dimens.dividerThickness
                    )

                    ImportantNoteItem(
                        icon = Icons.Filled.SignalCellularOff,
                        title = stringResource(Res.string.directions_note_no_signal),
                        description = stringResource(Res.string.directions_note_no_signal_detail)
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = Dimens.dividerThickness
                    )

                    ImportantNoteItem(
                        icon = Icons.Filled.LocalGasStation,
                        title = stringResource(Res.string.directions_note_fuel),
                        description = stringResource(Res.string.directions_note_fuel_detail)
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportantNoteItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(Dimens.noteCardIconSize),
            color = MaterialTheme.colorScheme.errorContainer,
            shape = CircleShape
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconSizeMedium),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.width(Dimens.paddingSmall))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GPSCoordinatesSection() {
    val gpsContentDescription = stringResource(Res.string.cd_directions_gps_icon)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationSmall)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingMedium)
        ) {
            SectionHeader(
                title = stringResource(Res.string.directions_section_gps),
                icon = Icons.Filled.GpsFixed,
                contentDescription = gpsContentDescription
            )

            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

            GPSCoordinateRow(
                label = stringResource(Res.string.directions_gps_latitude),
                value = stringResource(Res.string.directions_gps_latitude_value)
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Dimens.paddingExtraSmall),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = Dimens.dividerThickness
            )

            GPSCoordinateRow(
                label = stringResource(Res.string.directions_gps_longitude),
                value = stringResource(Res.string.directions_gps_longitude_value)
            )
        }
    }
}

@Composable
private fun GPSCoordinateRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.gpsRowHeight),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    contentDescription: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics {
            this.contentDescription = contentDescription
        }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Dimens.directionsSectionIconSize),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(Dimens.paddingSmall))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ExpandableSectionHeader(
    title: String,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    contentDescription: String,
    isExpanded: Boolean,
    expandContentDescription: String,
    onClick: () -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) ROTATION_EXPANDED else ROTATION_COLLAPSED,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "expandRotation"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(Dimens.paddingMedium)
            .semantics { this.contentDescription = contentDescription },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Dimens.directionsSectionIconSize),
            tint = iconTint
        )
        Spacer(modifier = Modifier.width(Dimens.paddingSmall))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = expandContentDescription,
            modifier = Modifier
                .size(Dimens.iconSizeMedium)
                .rotate(rotationAngle),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OpenMapsButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingMedium),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Icon(
            imageVector = Icons.Filled.Map,
            contentDescription = null,
            modifier = Modifier.size(Dimens.iconSizeMedium)
        )
        Spacer(modifier = Modifier.width(Dimens.paddingSmall))
        Text(
            text = stringResource(Res.string.directions_open_maps),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(vertical = Dimens.paddingSmall)
        )
    }
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun DirectionsScreenPreview() {
    AppTheme {
        DirectionsScreen()
    }
}
