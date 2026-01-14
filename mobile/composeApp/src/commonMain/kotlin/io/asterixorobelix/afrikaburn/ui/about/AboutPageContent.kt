package io.asterixorobelix.afrikaburn.ui.about

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Data class representing the content for a single About page.
 */
data class AboutPageData(
    val title: String,
    val content: String,
    val imagePainter: Painter? = null,
    val buttonText: String? = null,
    val url: String? = null,
    val imageContentDescription: String? = null
)

/**
 * Content layout for a single About page with enhanced visual hierarchy.
 * Features a prominent circular image, clear typography hierarchy, and
 * a styled call-to-action button.
 *
 * @param data The content data for this page
 * @param modifier Optional modifier for the content column
 */
@Composable
fun AboutPageContent(
    data: AboutPageData,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.cardContentPaddingHorizontal)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
    ) {
        // Image section with circular styling and shadow
        data.imagePainter?.let { painter ->
            AboutPageImage(
                painter = painter,
                contentDescription = data.imageContentDescription
            )
            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
        }

        // Title with prominent styling
        Text(
            text = data.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Content description with proper line height
        Text(
            text = data.content,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingSmall)
        )

        // Call-to-action button
        if (data.buttonText != null && data.url != null) {
            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
            AboutPageButton(
                text = data.buttonText,
                onClick = { uriHandler.openUri(data.url) }
            )
        }
    }
}

/**
 * Circular image component with shadow and gradient overlay for visual depth.
 */
@Composable
private fun AboutPageImage(
    painter: Painter,
    contentDescription: String?
) {
    Box(
        modifier = Modifier
            .size(Dimens.aboutImageSize)
            .shadow(
                elevation = Dimens.elevationMedium,
                shape = CircleShape,
                clip = false
            )
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription ?: "About page illustration",
            modifier = Modifier
                .size(Dimens.aboutImageSize)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        // Subtle gradient overlay for depth
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                        )
                    )
                )
        )
    }
}

/**
 * Styled call-to-action button with filled styling.
 */
@Composable
private fun AboutPageButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingMedium)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(vertical = Dimens.paddingExtraSmall)
        )
    }
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun AboutPageContentPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(Dimens.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingLarge)
        ) {
            // Without image
            AboutPageContent(
                data = AboutPageData(
                    title = "Welcome to AfrikaBurn",
                    content = "Your companion app for the AfrikaBurn experience " +
                        "in the Tankwa Karoo desert."
                )
            )

            // With button
            AboutPageContent(
                data = AboutPageData(
                    title = "About AfrikaBurn",
                    content = "Learn more about this amazing event " +
                        "in the Tankwa Karoo.",
                    buttonText = "Visit Website",
                    url = "https://afrikaburn.com"
                )
            )
        }
    }
}
