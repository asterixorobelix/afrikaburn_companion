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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.button_clear_filters
import afrikaburn.composeapp.generated.resources.button_clear_search
import afrikaburn.composeapp.generated.resources.button_reset_all
import afrikaburn.composeapp.generated.resources.cd_empty_state_icon
import afrikaburn.composeapp.generated.resources.empty_state_filters_active
import afrikaburn.composeapp.generated.resources.empty_state_no_category_items
import afrikaburn.composeapp.generated.resources.empty_state_no_filtered_results
import afrikaburn.composeapp.generated.resources.empty_state_no_search_results
import afrikaburn.composeapp.generated.resources.empty_state_suggestion_art
import afrikaburn.composeapp.generated.resources.empty_state_suggestion_camps
import afrikaburn.composeapp.generated.resources.empty_state_suggestion_events
import afrikaburn.composeapp.generated.resources.empty_state_suggestion_mobile_art
import afrikaburn.composeapp.generated.resources.empty_state_suggestion_performances
import afrikaburn.composeapp.generated.resources.empty_state_suggestion_vehicles
import afrikaburn.composeapp.generated.resources.empty_state_try_different_search
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
import io.asterixorobelix.afrikaburn.models.ProjectType
import io.asterixorobelix.afrikaburn.models.TimeFilter
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Represents the type of empty state to display.
 */
enum class EmptyStateType {
    SEARCH_NO_RESULTS,
    FILTERED_NO_RESULTS,
    CATEGORY_EMPTY
}

/**
 * Enhanced empty state component with context-aware suggestions and call-to-action buttons.
 *
 * @param emptyStateType The type of empty state to display
 * @param projectType The current project type (for context-aware suggestions)
 * @param searchQuery The current search query (shown when search yields no results)
 * @param hasActiveFilters Whether any filters are currently active
 * @param onClearSearch Callback when user taps "Clear search" button
 * @param onClearFilters Callback when user taps "Clear filters" button
 */
@Suppress("LongParameterList")
@Composable
fun EmptyStateContent(
    emptyStateType: EmptyStateType,
    projectType: ProjectType,
    searchQuery: String,
    hasActiveFilters: Boolean,
    onClearSearch: () -> Unit,
    onClearFilters: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingLarge),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = CARD_BACKGROUND_ALPHA
                )
            ),
            shape = RoundedCornerShape(Dimens.cornerRadiusLarge)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.paddingExtraLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingLarge)
            ) {
                EmptyStateIcon()

                EmptyStateTitle(
                    emptyStateType = emptyStateType,
                    projectType = projectType,
                    searchQuery = searchQuery
                )

                EmptyStateSubtitle(
                    emptyStateType = emptyStateType,
                    hasActiveFilters = hasActiveFilters
                )

                EmptyStateSuggestion(projectType = projectType)

                EmptyStateActions(
                    searchQuery = searchQuery,
                    hasActiveFilters = hasActiveFilters,
                    onClearSearch = onClearSearch,
                    onClearFilters = onClearFilters
                )
            }
        }
    }
}

@Composable
private fun EmptyStateIcon() {
    Box(
        modifier = Modifier
            .size(Dimens.iconSizeHero)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(
                    alpha = ICON_BACKGROUND_ALPHA
                ),
                shape = RoundedCornerShape(Dimens.cornerRadiusLarge)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = stringResource(Res.string.cd_empty_state_icon),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimens.iconSizeExtraLarge)
        )
    }
}

@Composable
private fun EmptyStateTitle(
    emptyStateType: EmptyStateType,
    projectType: ProjectType,
    searchQuery: String
) {
    val titleText = when (emptyStateType) {
        EmptyStateType.SEARCH_NO_RESULTS -> stringResource(
            Res.string.empty_state_no_search_results,
            searchQuery
        )
        EmptyStateType.FILTERED_NO_RESULTS -> stringResource(
            Res.string.empty_state_no_filtered_results,
            projectType.displayName.lowercase()
        )
        EmptyStateType.CATEGORY_EMPTY -> stringResource(
            Res.string.empty_state_no_category_items,
            projectType.displayName.lowercase()
        )
    }

    Text(
        text = titleText,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun EmptyStateSubtitle(
    emptyStateType: EmptyStateType,
    hasActiveFilters: Boolean
) {
    val subtitleResource = when {
        emptyStateType == EmptyStateType.SEARCH_NO_RESULTS && hasActiveFilters ->
            Res.string.empty_state_try_different_search
        emptyStateType == EmptyStateType.SEARCH_NO_RESULTS ->
            Res.string.empty_state_try_different_search
        hasActiveFilters ->
            Res.string.empty_state_filters_active
        else -> null
    }

    subtitleResource?.let { resource ->
        Text(
            text = stringResource(resource),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyStateSuggestion(projectType: ProjectType) {
    val suggestionResource = getSuggestionForProjectType(projectType)

    Spacer(modifier = Modifier.height(Dimens.spacingSmall))

    Text(
        text = stringResource(suggestionResource),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun EmptyStateActions(
    searchQuery: String,
    hasActiveFilters: Boolean,
    onClearSearch: () -> Unit,
    onClearFilters: () -> Unit
) {
    val hasSearchQuery = searchQuery.isNotEmpty()
    val showBothButtons = hasSearchQuery && hasActiveFilters

    if (!hasSearchQuery && !hasActiveFilters) {
        return
    }

    Spacer(modifier = Modifier.height(Dimens.spacingSmall))

    if (showBothButtons) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClearSearchButton(onClick = onClearSearch)
            ClearFiltersButton(onClick = onClearFilters)
        }
    } else if (hasSearchQuery) {
        ClearSearchButton(onClick = onClearSearch)
    } else {
        ClearFiltersButton(onClick = onClearFilters)
    }
}

@Composable
private fun ClearSearchButton(onClick: () -> Unit) {
    OutlinedButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = null,
            modifier = Modifier.size(Dimens.iconSizeSmall)
        )
        Spacer(modifier = Modifier.width(Dimens.spacingSmall))
        Text(
            text = stringResource(Res.string.button_clear_search),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun ClearFiltersButton(onClick: () -> Unit) {
    OutlinedButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Outlined.FilterList,
            contentDescription = null,
            modifier = Modifier.size(Dimens.iconSizeSmall)
        )
        Spacer(modifier = Modifier.width(Dimens.spacingSmall))
        Text(
            text = stringResource(Res.string.button_clear_filters),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

private fun getSuggestionForProjectType(projectType: ProjectType): StringResource {
    return when (projectType) {
        ProjectType.ART -> Res.string.empty_state_suggestion_art
        ProjectType.PERFORMANCES -> Res.string.empty_state_suggestion_performances
        ProjectType.EVENTS -> Res.string.empty_state_suggestion_events
        ProjectType.MOBILE_ART -> Res.string.empty_state_suggestion_mobile_art
        ProjectType.VEHICLES -> Res.string.empty_state_suggestion_vehicles
        ProjectType.CAMPS -> Res.string.empty_state_suggestion_camps
    }
}

/**
 * Helper function to determine the empty state type based on current state.
 */
fun determineEmptyStateType(
    searchQuery: String,
    isFamilyFilterEnabled: Boolean,
    timeFilter: TimeFilter,
    hasProjects: Boolean
): EmptyStateType {
    val hasActiveFilters = isFamilyFilterEnabled || timeFilter != TimeFilter.ALL
    val hasSearchQuery = searchQuery.isNotEmpty()

    return when {
        hasSearchQuery -> EmptyStateType.SEARCH_NO_RESULTS
        hasActiveFilters -> EmptyStateType.FILTERED_NO_RESULTS
        !hasProjects -> EmptyStateType.CATEGORY_EMPTY
        else -> EmptyStateType.CATEGORY_EMPTY
    }
}

/**
 * Helper function to determine if any filters are active.
 */
fun hasActiveFilters(
    isFamilyFilterEnabled: Boolean,
    timeFilter: TimeFilter
): Boolean = isFamilyFilterEnabled || timeFilter != TimeFilter.ALL

private const val CARD_BACKGROUND_ALPHA = 0.5f
private const val ICON_BACKGROUND_ALPHA = 0.3f

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun EmptyStateContentPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            EmptyStateContent(
                emptyStateType = EmptyStateType.SEARCH_NO_RESULTS,
                projectType = ProjectType.ART,
                searchQuery = "sculpture",
                hasActiveFilters = false,
                onClearSearch = {},
                onClearFilters = {}
            )
        }
    }
}
