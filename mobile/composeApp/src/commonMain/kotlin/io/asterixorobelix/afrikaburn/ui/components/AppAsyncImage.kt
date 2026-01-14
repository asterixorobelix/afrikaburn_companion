package io.asterixorobelix.afrikaburn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Default crossfade duration for image loading animations.
 */
private const val DEFAULT_CROSSFADE_DURATION_MS = 300

/**
 * A reusable async image component that wraps Coil's AsyncImage with app-specific defaults.
 * Supports URL-based and resource-based images with placeholder, error, and loading states.
 *
 * Features:
 * - Automatic placeholder while loading
 * - Error state display on failure
 * - Crossfade animation on successful load
 * - Memory and disk caching handled automatically by Coil
 * - Consistent styling with Material Design 3 theme
 *
 * @param model The image source - can be a URL string, URI, or any data supported by Coil
 * @param contentDescription Accessibility description for the image
 * @param modifier Modifier for the image component
 * @param contentScale How the image should be scaled within its bounds
 * @param placeholderIcon Icon to display while loading (defaults to Image icon)
 * @param errorIcon Icon to display on load failure (defaults to BrokenImage icon)
 * @param crossfadeDurationMs Duration of the crossfade animation in milliseconds
 * @param colorFilter Optional color filter to apply to the image
 */
@Composable
fun AppAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderIcon: ImageVector = Icons.Default.Image,
    errorIcon: ImageVector = Icons.Default.BrokenImage,
    crossfadeDurationMs: Int = DEFAULT_CROSSFADE_DURATION_MS,
    colorFilter: ColorFilter? = null
) {
    val context = LocalPlatformContext.current

    val imageRequest = ImageRequest.Builder(context)
        .data(model)
        .crossfade(crossfadeDurationMs)
        .build()

    SubcomposeAsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        colorFilter = colorFilter,
        loading = {
            ImageLoadingPlaceholder(
                icon = placeholderIcon,
                modifier = Modifier.fillMaxSize()
            )
        },
        error = {
            ImageErrorPlaceholder(
                icon = errorIcon,
                modifier = Modifier.fillMaxSize()
            )
        }
    )
}

/**
 * A simpler async image component without subcomposition for better performance
 * when custom loading/error states are not needed.
 *
 * Uses placeholder and error painters for a more lightweight implementation.
 *
 * @param model The image source - can be a URL string, URI, or any data supported by Coil
 * @param contentDescription Accessibility description for the image
 * @param modifier Modifier for the image component
 * @param contentScale How the image should be scaled within its bounds
 * @param placeholder Painter to display while loading
 * @param error Painter to display on load failure
 * @param crossfadeDurationMs Duration of the crossfade animation in milliseconds
 * @param colorFilter Optional color filter to apply to the image
 */
@Composable
fun AppAsyncImageSimple(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: Painter? = null,
    error: Painter? = null,
    crossfadeDurationMs: Int = DEFAULT_CROSSFADE_DURATION_MS,
    colorFilter: ColorFilter? = null
) {
    val context = LocalPlatformContext.current

    val imageRequest = ImageRequest.Builder(context)
        .data(model)
        .crossfade(crossfadeDurationMs)
        .build()

    AsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        placeholder = placeholder,
        error = error,
        colorFilter = colorFilter
    )
}

/**
 * Async image component with state observation callback.
 *
 * Use this variant when you need to react to image loading state changes
 * (e.g., to show custom loading UI or handle errors).
 *
 * @param model The image source - can be a URL string, URI, or any data supported by Coil
 * @param contentDescription Accessibility description for the image
 * @param modifier Modifier for the image component
 * @param contentScale How the image should be scaled within its bounds
 * @param crossfadeDurationMs Duration of the crossfade animation in milliseconds
 * @param colorFilter Optional color filter to apply to the image
 * @param onState Callback to observe image loading state changes
 */
@Composable
fun AppAsyncImageWithState(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    crossfadeDurationMs: Int = DEFAULT_CROSSFADE_DURATION_MS,
    colorFilter: ColorFilter? = null,
    onState: ((AsyncImagePainter.State) -> Unit)? = null
) {
    val context = LocalPlatformContext.current

    val imageRequest = ImageRequest.Builder(context)
        .data(model)
        .crossfade(crossfadeDurationMs)
        .build()

    AsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        colorFilter = colorFilter,
        onState = onState
    )
}

/**
 * Placeholder composable displayed while an image is loading.
 * Shows a centered icon with a subtle background.
 *
 * @param icon The icon to display
 * @param modifier Modifier for the placeholder container
 */
@Composable
private fun ImageLoadingPlaceholder(
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(Dimens.iconSizeMedium),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = Dimens.cornerRadiusXSmall
        )
    }
}

/**
 * Placeholder composable displayed when image loading fails.
 * Shows an error icon with a subtle background.
 *
 * @param icon The error icon to display
 * @param modifier Modifier for the placeholder container
 */
@Composable
private fun ImageErrorPlaceholder(
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.errorContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Dimens.iconSizeLarge),
            tint = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun AppAsyncImagePreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Dimens.paddingMedium)
        ) {
            Text(
                text = "AppAsyncImage Examples",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = Dimens.paddingMedium)
            )

            Text(
                text = "Loading State:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = Dimens.paddingSmall)
            )
            Box(
                modifier = Modifier
                    .size(Dimens.iconSizeHero)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimens.iconSizeMedium),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = Dimens.cornerRadiusXSmall
                )
            }

            Text(
                text = "Error State:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    top = Dimens.paddingMedium,
                    bottom = Dimens.paddingSmall
                )
            )
            ImageErrorPlaceholder(
                icon = Icons.Default.BrokenImage,
                modifier = Modifier.size(Dimens.iconSizeHero)
            )
        }
    }
}
