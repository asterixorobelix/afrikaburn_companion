package io.asterixorobelix.afrikaburn.ui.projects

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.filter_family_friendly_short
import afrikaburn.composeapp.generated.resources.filter_section_header
import afrikaburn.composeapp.generated.resources.filter_time_all
import afrikaburn.composeapp.generated.resources.filter_time_daytime
import afrikaburn.composeapp.generated.resources.filter_time_nighttime
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
import io.asterixorobelix.afrikaburn.models.TimeFilter
import io.asterixorobelix.afrikaburn.ui.components.animateSelectionScale
import io.asterixorobelix.afrikaburn.ui.components.shortDurationTween
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Filter chips section with header and visual separation.
 * Displays family-friendly filter and time-based filter options.
 */
@Composable
fun ProjectFilterChips(
    isFamilyFilterEnabled: Boolean,
    onToggleFamilyFilter: () -> Unit,
    timeFilter: TimeFilter,
    onTimeFilterChange: (TimeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Section divider above filters
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = Dimens.dividerThickness,
            modifier = Modifier.padding(horizontal = Dimens.paddingMedium)
        )

        Spacer(modifier = Modifier.height(Dimens.spacingMedium))

        // Section header
        Text(
            text = stringResource(Res.string.filter_section_header),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Dimens.paddingMedium)
        )

        Spacer(modifier = Modifier.height(Dimens.spacingSmall))

        // Filter chips row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingMedium),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Family filter chip
            item {
                FamilyFilterChip(
                    isSelected = isFamilyFilterEnabled,
                    onClick = onToggleFamilyFilter
                )
            }

            // Time filter chips
            items(TimeFilter.entries.toList()) { filter ->
                TimeFilterChip(
                    filter = filter,
                    isSelected = timeFilter == filter,
                    onClick = { onTimeFilterChange(filter) }
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spacingMedium))

        // Section divider below filters
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = Dimens.dividerThickness,
            modifier = Modifier.padding(horizontal = Dimens.paddingMedium)
        )
    }
}

@Composable
private fun FamilyFilterChip(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale = animateSelectionScale(isSelected = isSelected)

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = shortDurationTween(),
        label = "familyChipContainerColor"
    )

    val labelColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = shortDurationTween(),
        label = "familyChipLabelColor"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        FilterChip(
            onClick = onClick,
            label = {
                Text(
                    text = stringResource(Res.string.filter_family_friendly_short),
                    style = MaterialTheme.typography.labelMedium
                )
            },
            selected = isSelected,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = containerColor,
                selectedLabelColor = labelColor,
                containerColor = containerColor,
                labelColor = labelColor
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isSelected,
                borderColor = MaterialTheme.colorScheme.outline,
                selectedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun TimeFilterChip(
    filter: TimeFilter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale = animateSelectionScale(isSelected = isSelected)

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = shortDurationTween(),
        label = "timeChipContainerColor"
    )

    val labelColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = shortDurationTween(),
        label = "timeChipLabelColor"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        FilterChip(
            onClick = onClick,
            label = {
                Text(
                    text = when (filter) {
                        TimeFilter.ALL -> stringResource(Res.string.filter_time_all)
                        TimeFilter.DAYTIME -> stringResource(Res.string.filter_time_daytime)
                        TimeFilter.NIGHTTIME -> stringResource(Res.string.filter_time_nighttime)
                    },
                    style = MaterialTheme.typography.labelMedium
                )
            },
            selected = isSelected,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = containerColor,
                selectedLabelColor = labelColor,
                containerColor = containerColor,
                labelColor = labelColor
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isSelected,
                borderColor = MaterialTheme.colorScheme.outline,
                selectedBorderColor = MaterialTheme.colorScheme.secondary
            )
        )
    }
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun ProjectFilterChipsPreview() {
    AppTheme {
        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            ProjectFilterChips(
                isFamilyFilterEnabled = false,
                onToggleFamilyFilter = {},
                timeFilter = TimeFilter.ALL,
                onTimeFilterChange = {}
            )
            Spacer(modifier = Modifier.height(Dimens.paddingLarge))
            ProjectFilterChips(
                isFamilyFilterEnabled = true,
                onToggleFamilyFilter = {},
                timeFilter = TimeFilter.DAYTIME,
                onTimeFilterChange = {}
            )
        }
    }
}
