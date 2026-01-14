package io.asterixorobelix.afrikaburn.ui.projects

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val SHIMMER_ANIMATION_DURATION_MS = 1200
private const val SHIMMER_START_OFFSET = 0f
private const val SHIMMER_END_OFFSET = 1000f
private const val SHIMMER_GRADIENT_START = 0.0f
private const val SHIMMER_GRADIENT_CENTER = 0.5f
private const val SHIMMER_GRADIENT_END = 1.0f

/**
 * A composable that displays a shimmer loading effect.
 * Uses an animated gradient that sweeps from left to right to indicate loading.
 *
 * @param modifier Modifier for styling and layout
 */
@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.surfaceVariant
    )

    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = SHIMMER_START_OFFSET,
        targetValue = SHIMMER_END_OFFSET,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = SHIMMER_ANIMATION_DURATION_MS,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerAnimation"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(
            x = translateAnim + SHIMMER_END_OFFSET / 2,
            y = translateAnim + SHIMMER_END_OFFSET / 2
        )
    )

    Box(
        modifier = modifier.background(brush = brush)
    )
}

/**
 * Creates a shimmer brush for custom shimmer effects.
 * Returns a Brush that can be applied to any composable background.
 */
@Composable
fun rememberShimmerBrush(): Brush {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.surfaceVariant
    )

    val transition = rememberInfiniteTransition(label = "shimmerBrushTransition")
    val translateAnim by transition.animateFloat(
        initialValue = SHIMMER_START_OFFSET,
        targetValue = SHIMMER_END_OFFSET,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = SHIMMER_ANIMATION_DURATION_MS,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerBrushAnimation"
    )

    return Brush.linearGradient(
        colorStops = arrayOf(
            SHIMMER_GRADIENT_START to shimmerColors[0],
            SHIMMER_GRADIENT_CENTER to shimmerColors[1],
            SHIMMER_GRADIENT_END to shimmerColors[2]
        ),
        start = Offset(translateAnim, translateAnim),
        end = Offset(
            x = translateAnim + SHIMMER_END_OFFSET / 2,
            y = translateAnim + SHIMMER_END_OFFSET / 2
        )
    )
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun ShimmerBoxPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Dimens.paddingMedium)
        ) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.skeletonLineHeightLarge)
            )
            Spacer(modifier = Modifier.height(Dimens.spacingSmall))
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.6f)
                    .height(Dimens.skeletonLineHeightSmall)
            )
            Spacer(modifier = Modifier.height(Dimens.spacingSmall))
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.skeletonLineHeightSmall)
            )
        }
    }
}
