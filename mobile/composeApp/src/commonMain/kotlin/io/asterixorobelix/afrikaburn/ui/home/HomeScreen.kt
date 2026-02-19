package io.asterixorobelix.afrikaburn.ui.home

import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.home_category_art
import afrikaburn.composeapp.generated.resources.home_category_camps
import afrikaburn.composeapp.generated.resources.home_category_events
import afrikaburn.composeapp.generated.resources.home_category_mobile_art
import afrikaburn.composeapp.generated.resources.home_category_survival
import afrikaburn.composeapp.generated.resources.home_category_vehicles
import afrikaburn.composeapp.generated.resources.home_section_categories
import afrikaburn.composeapp.generated.resources.home_section_happening_now
import afrikaburn.composeapp.generated.resources.home_title
import afrikaburn.composeapp.generated.resources.home_welcome_subtitle
import afrikaburn.composeapp.generated.resources.home_welcome_title
import afrikaburn.composeapp.generated.resources.home_year_badge
import afrikaburn.composeapp.generated.resources.cd_home_category
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import io.asterixorobelix.afrikaburn.Dimens
import io.asterixorobelix.afrikaburn.cardElevation
import io.asterixorobelix.afrikaburn.di.koinProjectTabViewModel
import io.asterixorobelix.afrikaburn.di.koinProjectsViewModel
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType
import io.asterixorobelix.afrikaburn.presentation.projects.ProjectsScreenUiState
import io.asterixorobelix.afrikaburn.presentation.projects.ProjectsUiState
import io.asterixorobelix.afrikaburn.presentation.projects.contentOrDefault
import io.asterixorobelix.afrikaburn.ui.components.bounceClick
import org.jetbrains.compose.resources.stringResource

private const val FEATURED_ITEMS_LIMIT = 5
private const val MAX_DESCRIPTION_LINES = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCategoryClick: (ProjectType) -> Unit,
    onSurvivalGuideClick: () -> Unit,
    onProjectClick: (ProjectItem) -> Unit
) {
    val viewModel = koinProjectsViewModel()
    val screenState by viewModel.screenUiState.collectAsState()
    val screenContent = when (screenState) {
        is ProjectsScreenUiState.Content ->
            screenState as ProjectsScreenUiState.Content
        is ProjectsScreenUiState.Empty ->
            (screenState as ProjectsScreenUiState.Empty).content
        is ProjectsScreenUiState.Error ->
            (screenState as ProjectsScreenUiState.Error).content
        ProjectsScreenUiState.Loading ->
            ProjectsScreenUiState.Content()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HomeTopBar()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = Dimens.paddingLarge)
        ) {
            WelcomeHero()

            Spacer(modifier = Modifier.height(Dimens.spacingExtraLarge))

            SectionHeader(text = stringResource(Res.string.home_section_categories))

            Spacer(modifier = Modifier.height(Dimens.spacingMedium))

            CategoryGrid(
                tabs = screenContent.tabs,
                onCategoryClick = onCategoryClick,
                onSurvivalGuideClick = onSurvivalGuideClick
            )

            Spacer(modifier = Modifier.height(Dimens.spacingExtraLarge))

            SectionHeader(text = stringResource(Res.string.home_section_happening_now))

            Spacer(modifier = Modifier.height(Dimens.spacingMedium))

            HappeningNowRow(
                onProjectClick = onProjectClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar() {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.home_title),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.width(Dimens.spacingSmall))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = stringResource(Res.string.home_year_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(
                            horizontal = Dimens.yearBadgePaddingHorizontal,
                            vertical = Dimens.yearBadgePaddingVertical
                        )
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    )
}

@Composable
private fun WelcomeHero() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimens.paddingMedium,
                vertical = Dimens.homeHeroVerticalPadding
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingLarge)
        ) {
            Text(
                text = stringResource(Res.string.home_welcome_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Dimens.spacingSmall))
            Text(
                text = stringResource(Res.string.home_welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = Dimens.paddingMedium)
    )
}

@Suppress("LongMethod")
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryGrid(
    tabs: List<ProjectType>,
    onCategoryClick: (ProjectType) -> Unit,
    onSurvivalGuideClick: () -> Unit
) {
    data class CategoryDef(
        val label: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val tintColor: @Composable () -> androidx.compose.ui.graphics.Color,
        val count: Int,
        val onClick: () -> Unit
    )

    val categories = mutableListOf<CategoryDef>()

    val categoryConfigs = listOf(
        Triple(ProjectType.ART, Res.string.home_category_art, Icons.Outlined.Palette),
        Triple(ProjectType.EVENTS, Res.string.home_category_events, Icons.Outlined.Celebration),
        Triple(ProjectType.CAMPS, Res.string.home_category_camps, Icons.Outlined.Groups),
        Triple(ProjectType.VEHICLES, Res.string.home_category_vehicles, Icons.Outlined.DirectionsCar),
        Triple(ProjectType.MOBILE_ART, Res.string.home_category_mobile_art, Icons.Outlined.Brush),
    )

    for ((projectType, labelRes, icon) in categoryConfigs) {
        val tabVm = koinProjectTabViewModel(projectType)
        val tabState by tabVm.uiState.collectAsState()
        val contentState = tabState.contentOrDefault()
        val label = stringResource(labelRes)
        val tintColor = categoryTintColor(projectType)
        categories.add(
            CategoryDef(
                label = label,
                icon = icon,
                tintColor = { tintColor },
                count = contentState.totalProjectCount,
                onClick = { onCategoryClick(projectType) }
            )
        )
    }

    // Add Survival Guide as a special category
    val survivalLabel = stringResource(Res.string.home_category_survival)
    categories.add(
        CategoryDef(
            label = survivalLabel,
            icon = Icons.Outlined.Map,
            tintColor = { MaterialTheme.colorScheme.tertiary },
            count = 0,
            onClick = onSurvivalGuideClick
        )
    )

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingMedium),
        horizontalArrangement = Arrangement.spacedBy(Dimens.categoryGridSpacing),
        verticalArrangement = Arrangement.spacedBy(Dimens.categoryGridSpacing),
        maxItemsInEachRow = 3
    ) {
        categories.forEach { cat ->
            val cdString = stringResource(Res.string.cd_home_category, cat.label)
            CategoryCard(
                label = cat.label,
                icon = cat.icon,
                tintColor = cat.tintColor(),
                itemCount = cat.count,
                contentDescription = cdString,
                modifier = Modifier.weight(1f),
                onClick = cat.onClick
            )
        }
    }
}

@Composable
private fun categoryTintColor(projectType: ProjectType): androidx.compose.ui.graphics.Color {
    return when (projectType) {
        ProjectType.ART -> MaterialTheme.colorScheme.primary
        ProjectType.PERFORMANCES -> MaterialTheme.colorScheme.primary
        ProjectType.EVENTS -> MaterialTheme.colorScheme.secondary
        ProjectType.CAMPS -> MaterialTheme.colorScheme.tertiary
        ProjectType.VEHICLES -> MaterialTheme.colorScheme.secondary
        ProjectType.MOBILE_ART -> MaterialTheme.colorScheme.primary
    }
}

@Composable
private fun HappeningNowRow(
    onProjectClick: (ProjectItem) -> Unit
) {
    val allFeatured = mutableListOf<Pair<ProjectItem, ProjectType>>()

    for (projectType in ProjectType.entries) {
        val tabVm = koinProjectTabViewModel(projectType)
        val tabState by tabVm.uiState.collectAsState()
        val contentState = tabState.contentOrDefault()
        contentState.projects.take(2).forEach { project ->
            allFeatured.add(project to projectType)
        }
    }

    val items = allFeatured.take(FEATURED_ITEMS_LIMIT)

    if (items.isEmpty()) return

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = Dimens.paddingMedium
        )
    ) {
        items(items, key = { "${it.second.name}_${it.first.name}" }) { (project, projectType) ->
            FeaturedProjectCard(
                project = project,
                projectType = projectType,
                onClick = { onProjectClick(project) }
            )
        }
    }
}

@Composable
private fun FeaturedProjectCard(
    project: ProjectItem,
    projectType: ProjectType,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(Dimens.happeningNowCardWidth)
            .height(Dimens.happeningNowCardHeight)
            .bounceClick(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.paddingMedium)
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = categoryTintColor(projectType).copy(alpha = 0.15f)
            ) {
                Text(
                    text = projectType.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = categoryTintColor(projectType),
                    modifier = Modifier.padding(
                        horizontal = Dimens.paddingSmall,
                        vertical = Dimens.paddingExtraSmall
                    )
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spacingSmall))

            Text(
                text = project.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(Dimens.spacingExtraSmall))

            Text(
                text = project.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = MAX_DESCRIPTION_LINES,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
