package io.asterixorobelix.afrikaburn.ui.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val TITLE_WIDTH_FRACTION = 0.7f
private const val ARTIST_WIDTH_FRACTION = 0.4f
private const val DESCRIPTION_LINE_1_FRACTION = 1.0f
private const val DESCRIPTION_LINE_2_FRACTION = 0.9f
private const val DESCRIPTION_LINE_3_FRACTION = 0.6f

/**
 * Skeleton loading component that mirrors the ProjectCard layout.
 * Displays animated shimmer placeholders for title, artist, description, and status badge.
 */
@Composable
fun ProjectCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationMedium)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingMedium)
        ) {
            SkeletonHeader()

            Spacer(modifier = Modifier.height(Dimens.spacingMedium))

            SkeletonDescription()

            Spacer(modifier = Modifier.height(Dimens.spacingLarge))

            SkeletonStatusBadge()
        }
    }
}

@Composable
private fun SkeletonHeader() {
    Column {
        // Title placeholder
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(fraction = TITLE_WIDTH_FRACTION)
                .height(Dimens.skeletonLineHeightLarge)
                .clip(MaterialTheme.shapes.small)
        )

        Spacer(modifier = Modifier.height(Dimens.spacingSmall))

        // Artist info placeholder
        SkeletonArtistInfo()

        Spacer(modifier = Modifier.height(Dimens.spacingSmall))

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = Dimens.dividerThickness
        )
    }
}

@Composable
private fun SkeletonArtistInfo() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Icon placeholder
        ShimmerBox(
            modifier = Modifier
                .size(Dimens.iconSizeSmall)
                .clip(MaterialTheme.shapes.small)
        )

        Spacer(modifier = Modifier.width(Dimens.spacingSmall))

        // Artist name placeholder
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(fraction = ARTIST_WIDTH_FRACTION)
                .height(Dimens.skeletonLineHeightSmall)
                .clip(MaterialTheme.shapes.small)
        )
    }
}

@Composable
private fun SkeletonDescription() {
    Column {
        // Description line 1
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(fraction = DESCRIPTION_LINE_1_FRACTION)
                .height(Dimens.skeletonLineHeightSmall)
                .clip(MaterialTheme.shapes.small)
        )

        Spacer(modifier = Modifier.height(Dimens.spacingExtraSmall))

        // Description line 2
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(fraction = DESCRIPTION_LINE_2_FRACTION)
                .height(Dimens.skeletonLineHeightSmall)
                .clip(MaterialTheme.shapes.small)
        )

        Spacer(modifier = Modifier.height(Dimens.spacingExtraSmall))

        // Description line 3 (shorter)
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(fraction = DESCRIPTION_LINE_3_FRACTION)
                .height(Dimens.skeletonLineHeightSmall)
                .clip(MaterialTheme.shapes.small)
        )
    }
}

@Composable
private fun SkeletonStatusBadge() {
    Box(
        modifier = Modifier
            .width(Dimens.skeletonBadgeWidth)
            .height(Dimens.skeletonBadgeHeight)
            .clip(MaterialTheme.shapes.small)
    ) {
        ShimmerBox(modifier = Modifier.matchParentSize())
    }
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun ProjectCardSkeletonPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Dimens.paddingMedium)
        ) {
            ProjectCardSkeleton()
        }
    }
}
