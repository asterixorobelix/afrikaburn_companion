package io.asterixorobelix.afrikaburn.ui.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.cd_artist_icon
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
import io.asterixorobelix.afrikaburn.models.Artist
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.ui.components.animatedScale
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Card component displaying project information.
 * Shows project name, artist, description, and status badge.
 * Tapping the card triggers the onClick callback for navigation to detail screen.
 */
@Composable
fun ProjectCard(
    project: ProjectItem,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animatedScale(isPressed = isPressed),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationMedium),
        onClick = { onClick?.invoke() },
        interactionSource = interactionSource
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingMedium)
        ) {
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
            contentDescription = stringResource(Res.string.cd_artist_icon),
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
        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
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
                )
            )
            ProjectCard(
                project = ProjectItem(
                    name = "Simple Camp",
                    description = "A minimal camp setup without artist or status.",
                    artist = Artist(name = ""),
                    status = ""
                )
            )
        }
    }
}
