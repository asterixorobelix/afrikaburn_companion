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
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val DEFAULT_SKELETON_COUNT = 4
private const val SLIDE_IN_OFFSET_DIVISOR = 4
private const val STAGGER_DELAY_MS = 75L

/**
 * Skeleton loading list component that displays multiple ProjectCardSkeleton items.
 * Uses staggered fade-in animations for visual interest during loading state.
 *
 * @param skeletonCount Number of skeleton cards to display (default: 4)
 * @param modifier Modifier for styling and layout
 */
@Composable
fun ProjectListSkeleton(
    modifier: Modifier = Modifier,
    skeletonCount: Int = DEFAULT_SKELETON_COUNT
) {
    val skeletonItems = remember(skeletonCount) {
        (0 until skeletonCount).toList()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingLarge),
        contentPadding = PaddingValues(
            horizontal = Dimens.paddingMedium,
            vertical = Dimens.paddingMedium
        )
    ) {
        itemsIndexed(
            items = skeletonItems,
            key = { index, _ -> "skeleton_$index" }
        ) { index, _ ->
            AnimatedSkeletonCard(index = index)
        }
    }
}

@Composable
private fun AnimatedSkeletonCard(index: Int) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(index) {
        // Stagger the appearance of skeleton cards for visual interest
        delay(index * STAGGER_DELAY_MS)
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
        ProjectCardSkeleton()
    }
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun ProjectListSkeletonPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Dimens.paddingMedium)
        ) {
            ProjectListSkeleton(skeletonCount = DEFAULT_SKELETON_COUNT)
        }
    }
}
