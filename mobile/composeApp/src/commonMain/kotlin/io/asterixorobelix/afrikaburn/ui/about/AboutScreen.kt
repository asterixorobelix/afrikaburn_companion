package io.asterixorobelix.afrikaburn.ui.about

import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources._20531795
import afrikaburn.composeapp.generated.resources._600px_Quaggapedia_OOTB
import afrikaburn.composeapp.generated.resources.Laura_Niggeschmidt_Afrika_Burn_76
import afrikaburn.composeapp.generated.resources.about_page1_content
import afrikaburn.composeapp.generated.resources.about_page1_title
import afrikaburn.composeapp.generated.resources.about_page1_url
import afrikaburn.composeapp.generated.resources.about_page2_content
import afrikaburn.composeapp.generated.resources.about_page2_title
import afrikaburn.composeapp.generated.resources.about_page2_url
import afrikaburn.composeapp.generated.resources.about_page3_content
import afrikaburn.composeapp.generated.resources.about_page3_title
import afrikaburn.composeapp.generated.resources.about_page3_url
import afrikaburn.composeapp.generated.resources.about_page4_content
import afrikaburn.composeapp.generated.resources.about_page4_title
import afrikaburn.composeapp.generated.resources.about_page4_url
import afrikaburn.composeapp.generated.resources.about_swipe_hint
import afrikaburn.composeapp.generated.resources.about_title
import afrikaburn.composeapp.generated.resources.button_contact
import afrikaburn.composeapp.generated.resources.button_learn_more
import afrikaburn.composeapp.generated.resources.button_view_theme
import afrikaburn.composeapp.generated.resources.button_visit_website
import afrikaburn.composeapp.generated.resources.cd_about_page1_image
import afrikaburn.composeapp.generated.resources.cd_about_page2_image
import afrikaburn.composeapp.generated.resources.cd_about_page3_image
import afrikaburn.composeapp.generated.resources.cd_about_page4_image
import afrikaburn.composeapp.generated.resources.cd_swipe_hint
import afrikaburn.composeapp.generated.resources.compose_multiplatform
import afrikaburn.composeapp.generated.resources.images
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.util.lerp
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
import io.asterixorobelix.afrikaburn.cardElevation
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.absoluteValue

private const val TOTAL_PAGES = 4
private const val PAGE_CONTACT = 3
private const val MIN_SCALE = 0.85f
private const val MIN_ALPHA = 0.5f

@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
    ) {
        AboutScreenTitle()

        val pagerState = rememberPagerState(pageCount = { TOTAL_PAGES })

        // Calculate page offset for smooth indicator transitions
        val pageOffset by remember {
            derivedStateOf {
                val currentPageOffset = pagerState.currentPageOffsetFraction
                if (currentPageOffset >= 0f) currentPageOffset else 0f
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            AboutScreenPager(pagerState)
        }

        // Swipe hint - only visible on first page
        SwipeHint(
            isVisible = pagerState.currentPage == 0,
            modifier = Modifier.fillMaxWidth()
        )

        PageIndicator(
            currentPage = pagerState.currentPage,
            totalPages = TOTAL_PAGES,
            pageOffset = pageOffset,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.paddingMedium)
        )
    }
}

@Composable
private fun AboutScreenTitle() {
    Text(
        text = stringResource(Res.string.about_title),
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.paddingMedium)
    )
}

@Composable
private fun AboutScreenPager(pagerState: PagerState) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "About information carousel"
            },
        pageSpacing = Dimens.paddingSmall,
        beyondViewportPageCount = 1
    ) { page ->
        // Calculate page offset for scale/alpha animations
        val pageOffset = calculatePageOffset(pagerState, page)

        AboutPageCard(
            page = page,
            pageOffset = pageOffset
        )
    }
}

/**
 * Calculates the offset of a page relative to the current scroll position.
 * Returns 0 when the page is fully centered, and increases as it moves away.
 */
@Composable
private fun calculatePageOffset(pagerState: PagerState, page: Int): Float {
    return remember(pagerState.currentPage, pagerState.currentPageOffsetFraction) {
        ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
    }
}

@Composable
private fun AboutPageCard(
    page: Int,
    pageOffset: Float
) {
    // Animate scale and alpha based on page position
    val scale by animateFloatAsState(
        targetValue = lerp(
            start = MIN_SCALE,
            stop = 1f,
            fraction = 1f - pageOffset.coerceIn(0f, 1f)
        ),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )

    val alpha by animateFloatAsState(
        targetValue = lerp(
            start = MIN_ALPHA,
            stop = 1f,
            fraction = 1f - pageOffset.coerceIn(0f, 1f)
        ),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.paddingSmall)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(
            defaultElevation = cardElevation()
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = Dimens.cardContentPaddingVertical),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AboutPageContent(data = getAboutPageData(page))
        }
    }
}

/**
 * A subtle swipe hint that appears on the first page to guide users.
 * Animates in/out with a fade and slide transition.
 */
@Composable
private fun SwipeHint(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val contentDescription = stringResource(Res.string.cd_swipe_hint)

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingLarge)
                .semantics { this.contentDescription = contentDescription },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Dimens.swipeHintIconSize)
            )

            Text(
                text = stringResource(Res.string.about_swipe_hint),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Dimens.paddingSmall)
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Dimens.swipeHintIconSize)
            )
        }
    }
}

@Composable
private fun getAboutPageData(page: Int): AboutPageData {
    return when (page) {
        0 -> AboutPageData(
            title = stringResource(Res.string.about_page1_title),
            content = stringResource(Res.string.about_page1_content),
            buttonText = stringResource(Res.string.button_learn_more),
            url = stringResource(Res.string.about_page1_url),
            imagePainter = painterResource(Res.drawable.images),
            imageContentDescription = stringResource(Res.string.cd_about_page1_image)
        )
        1 -> AboutPageData(
            title = stringResource(Res.string.about_page2_title),
            content = stringResource(Res.string.about_page2_content),
            buttonText = stringResource(Res.string.button_visit_website),
            url = stringResource(Res.string.about_page2_url),
            imagePainter = painterResource(Res.drawable._600px_Quaggapedia_OOTB),
            imageContentDescription = stringResource(Res.string.cd_about_page2_image)
        )
        2 -> AboutPageData(
            title = stringResource(Res.string.about_page3_title),
            content = stringResource(Res.string.about_page3_content),
            buttonText = stringResource(Res.string.button_view_theme),
            url = stringResource(Res.string.about_page3_url),
            imagePainter = painterResource(Res.drawable.Laura_Niggeschmidt_Afrika_Burn_76),
            imageContentDescription = stringResource(Res.string.cd_about_page3_image)
        )
        PAGE_CONTACT -> AboutPageData(
            title = stringResource(Res.string.about_page4_title),
            content = stringResource(Res.string.about_page4_content),
            buttonText = stringResource(Res.string.button_contact),
            url = stringResource(Res.string.about_page4_url),
            imagePainter = painterResource(Res.drawable._20531795),
            imageContentDescription = stringResource(Res.string.cd_about_page4_image)
        )
        else -> AboutPageData(
            title = "Unknown Page",
            content = "This page is not available."
        )
    }
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun AboutScreenPreview() {
    AppTheme {
        AboutScreen()
    }
}
