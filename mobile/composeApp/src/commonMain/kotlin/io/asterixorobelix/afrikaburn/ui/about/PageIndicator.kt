package io.asterixorobelix.afrikaburn.ui.about

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val UNSELECTED_ALPHA = 0.38f
private const val SELECTED_SCALE = 1f
private const val UNSELECTED_SCALE = 0.75f

/**
 * An animated page indicator that shows the current position in a pager.
 * Features smooth scale and color transitions when the page changes.
 *
 * @param currentPage The currently selected page index (0-based)
 * @param pageOffset The fractional offset of the current page during scroll (0f to 1f)
 * @param totalPages The total number of pages
 * @param modifier Optional modifier for the indicator row
 */
@Composable
fun PageIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier,
    pageOffset: Float = 0f
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.paddingSmall)
            .semantics {
                contentDescription = "Page ${currentPage + 1} of $totalPages"
            },
        horizontalArrangement = Arrangement.spacedBy(
            Dimens.indicatorSpacing,
            Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPages) { index ->
            AnimatedIndicatorDot(
                index = index,
                currentPage = currentPage,
                pageOffset = pageOffset,
                totalPages = totalPages
            )
        }
    }
}

@Composable
private fun AnimatedIndicatorDot(
    index: Int,
    currentPage: Int,
    pageOffset: Float,
    totalPages: Int
) {
    val isSelected = index == currentPage
    val isNextPage = index == currentPage + 1 && currentPage < totalPages - 1

    // Calculate interpolated values for smooth transitions during swipe
    val selectionProgress = when {
        isSelected -> 1f - pageOffset
        isNextPage -> pageOffset
        else -> 0f
    }

    val targetScale = UNSELECTED_SCALE + (SELECTED_SCALE - UNSELECTED_SCALE) * selectionProgress

    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "indicatorScale"
    )

    val size by animateDpAsState(
        targetValue = if (selectionProgress > 0.5f) {
            Dimens.indicatorDotSizeLarge
        } else {
            Dimens.indicatorDotSizeSmall
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "indicatorSize"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = UNSELECTED_ALPHA)

    val color by animateColorAsState(
        targetValue = if (selectionProgress > 0.5f) primaryColor else unselectedColor,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "indicatorColor"
    )

    Box(
        modifier = Modifier
            .size(Dimens.indicatorDotSizeLarge)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun PageIndicatorPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Dimens.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingLarge)
        ) {
            Text(
                text = "Page Indicator - First Page",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            PageIndicator(
                currentPage = 0,
                totalPages = 4
            )

            Text(
                text = "Page Indicator - Middle Page",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            PageIndicator(
                currentPage = 2,
                totalPages = 4
            )

            Text(
                text = "Page Indicator - With Offset (Swiping)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            PageIndicator(
                currentPage = 1,
                totalPages = 4,
                pageOffset = 0.5f
            )
        }
    }
}
