package io.asterixorobelix.afrikaburn.ui.projects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
import io.asterixorobelix.afrikaburn.models.Artist
import io.asterixorobelix.afrikaburn.models.ProjectItem
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val MAX_ANIMATED_ITEMS = 10
private const val SLIDE_IN_OFFSET_DIVISOR = 4

/**
 * List component displaying project cards with staggered enter animations.
 * Provides visual feedback when content loads with smooth fade-in effects.
 */
@Composable
fun ProjectList(
    projects: List<ProjectItem>,
    modifier: Modifier = Modifier,
    onProjectClick: ((ProjectItem) -> Unit)? = null
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingLarge),
        contentPadding = PaddingValues(
            horizontal = Dimens.paddingMedium,
            vertical = Dimens.paddingMedium
        )
    ) {
        itemsIndexed(
            items = projects,
            key = { index, project -> "${project.name}_$index" }
        ) { index, project ->
            AnimatedProjectCard(
                project = project,
                index = index,
                onClick = onProjectClick?.let { { it(project) } }
            )
        }
    }
}

@Composable
private fun AnimatedProjectCard(
    project: ProjectItem,
    index: Int,
    onClick: (() -> Unit)? = null
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(project) {
        // Stagger delay based on index, capped for performance
        val delayMs = (index.coerceAtMost(MAX_ANIMATED_ITEMS) * Dimens.staggerDelayPerItem).toLong()
        delay(delayMs)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = Dimens.animationDurationMedium
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = Dimens.animationDurationMedium
            ),
            initialOffsetY = { fullHeight -> fullHeight / SLIDE_IN_OFFSET_DIVISOR }
        )
    ) {
        ProjectCard(project = project, onClick = onClick)
    }
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun ProjectListPreview() {
    val sampleProjects = listOf(
        ProjectItem(
            name = "Dust Storm Collective",
            description = "An immersive art installation.",
            artist = Artist(name = "Sarah Johnson"),
            status = "Day Time"
        ),
        ProjectItem(
            name = "Fire Temple",
            description = "A sacred space for contemplation.",
            artist = Artist(name = ""),
            status = "Night Time"
        ),
        ProjectItem(
            name = "Sound Wave Camp",
            description = "Music and vibes all day long.",
            artist = Artist(name = "DJ Collective"),
            status = "All Night | Fam(ish)"
        )
    )

    AppTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Dimens.paddingMedium)
        ) {
            ProjectList(projects = sampleProjects)
        }
    }
}
