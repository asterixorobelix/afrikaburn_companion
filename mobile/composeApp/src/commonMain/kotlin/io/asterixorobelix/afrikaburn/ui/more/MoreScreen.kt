package io.asterixorobelix.afrikaburn.ui.more

import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.cd_more_about
import afrikaburn.composeapp.generated.resources.cd_more_directions
import afrikaburn.composeapp.generated.resources.more_about_subtitle
import afrikaburn.composeapp.generated.resources.more_directions_subtitle
import afrikaburn.composeapp.generated.resources.more_section_about
import afrikaburn.composeapp.generated.resources.more_section_directions
import afrikaburn.composeapp.generated.resources.more_title
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import io.asterixorobelix.afrikaburn.Dimens
import io.asterixorobelix.afrikaburn.cardElevation
import io.asterixorobelix.afrikaburn.ui.components.pressableScale
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onDirectionsClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(Res.string.more_title),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.paddingMedium)
        ) {
            MoreMenuItem(
                title = stringResource(Res.string.more_section_directions),
                subtitle = stringResource(Res.string.more_directions_subtitle),
                icon = Icons.Outlined.LocationOn,
                iconTint = MaterialTheme.colorScheme.primary,
                contentDescription = stringResource(Res.string.cd_more_directions),
                onClick = onDirectionsClick
            )

            Spacer(modifier = Modifier.height(Dimens.spacingMedium))

            MoreMenuItem(
                title = stringResource(Res.string.more_section_about),
                subtitle = stringResource(Res.string.more_about_subtitle),
                icon = Icons.Outlined.Info,
                iconTint = MaterialTheme.colorScheme.secondary,
                contentDescription = stringResource(Res.string.cd_more_about),
                onClick = onAboutClick
            )
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun MoreMenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    contentDescription: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pressableScale(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(Dimens.moreItemIconSize)
            )

            Spacer(modifier = Modifier.width(Dimens.spacingLarge))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Dimens.spacingExtraSmall))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
