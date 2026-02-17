package io.asterixorobelix.afrikaburn.ui.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
import io.asterixorobelix.afrikaburn.cardElevation
import io.asterixorobelix.afrikaburn.models.Artist
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType
import io.asterixorobelix.afrikaburn.ui.components.animatedScale
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val MAX_DESCRIPTION_LINES = 2

@Composable
fun ProjectCard(
    project: ProjectItem,
    modifier: Modifier = Modifier,
    projectType: ProjectType? = null,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val categoryColor = projectType?.let { categoryIndicatorColor(it) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animatedScale(isPressed = isPressed),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation()),
        onClick = { onClick?.invoke() },
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            if (categoryColor != null) {
                Box(
                    modifier = Modifier
                        .width(Dimens.categoryIndicatorWidth)
                        .fillMaxHeight()
                        .background(categoryColor)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Dimens.cardContentPaddingHorizontal,
                        vertical = Dimens.cardContentPaddingVertical
                    )
            ) {
                if (projectType != null) {
                    ProjectTypeBadge(projectType = projectType, color = categoryColor!!)
                    Spacer(modifier = Modifier.height(Dimens.spacingSmall))
                }

                ProjectCardHeader(
                    name = project.name,
                    artistName = project.artist.name
                )

                Spacer(modifier = Modifier.height(Dimens.spacingMedium))

                ProjectCardDescription(description = project.description)

                if (project.status.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Dimens.spacingLarge))
                    ProjectCardStatusBadge(status = project.status)
                }
            }
        }
    }
}

@Composable
private fun categoryIndicatorColor(projectType: ProjectType): Color {
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
private fun ProjectTypeBadge(projectType: ProjectType, color: Color) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = projectType.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(
                horizontal = Dimens.paddingSmall,
                vertical = Dimens.paddingExtraSmall
            )
        )
    }
}

@Composable
private fun ProjectCardHeader(
    name: String,
    artistName: String
) {
    Column {
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (artistName.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Dimens.spacingSmall))

            ProjectCardArtistInfo(artistName = artistName)

            Spacer(modifier = Modifier.height(Dimens.spacingSmall))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = Dimens.dividerThickness
            )
        }
    }
}

@Composable
private fun ProjectCardArtistInfo(artistName: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimens.iconSizeSmall)
        )
        Spacer(modifier = Modifier.width(Dimens.spacingSmall))
        Text(
            text = artistName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProjectCardDescription(description: String) {
    Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
        maxLines = MAX_DESCRIPTION_LINES,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun ProjectCardStatusBadge(status: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(
                horizontal = Dimens.paddingSmall,
                vertical = Dimens.paddingExtraSmall
            )
        )
    }
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun ProjectCardPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Dimens.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingLarge)
        ) {
            ProjectCard(
                project = ProjectItem(
                    name = "Dust Storm Collective",
                    description = "An immersive art installation exploring the " +
                        "relationship between humans and the desert environment.",
                    artist = Artist(name = "Sarah Johnson"),
                    status = "Day Time | Fam(ish)"
                ),
                projectType = ProjectType.ART
            )
            ProjectCard(
                project = ProjectItem(
                    name = "Simple Camp",
                    description = "A minimal camp setup without artist or status.",
                    artist = Artist(name = ""),
                    status = ""
                ),
                projectType = ProjectType.CAMPS
            )
        }
    }
}
