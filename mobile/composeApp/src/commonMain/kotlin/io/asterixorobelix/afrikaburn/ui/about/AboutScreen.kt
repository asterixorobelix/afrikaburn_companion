package io.asterixorobelix.afrikaburn.ui.about

import afrikaburn.composeapp.generated.resources.Res
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
import afrikaburn.composeapp.generated.resources.about_title
import afrikaburn.composeapp.generated.resources.button_learn_more
import afrikaburn.composeapp.generated.resources.button_visit_website
import afrikaburn.composeapp.generated.resources.button_view_theme
import afrikaburn.composeapp.generated.resources.button_contact
import afrikaburn.composeapp.generated.resources.cd_about_page1_image
import afrikaburn.composeapp.generated.resources.cd_about_page2_image
import afrikaburn.composeapp.generated.resources.cd_about_page3_image
import afrikaburn.composeapp.generated.resources.cd_about_page4_image
import afrikaburn.composeapp.generated.resources.compose_multiplatform
import afrikaburn.composeapp.generated.resources.images
import afrikaburn.composeapp.generated.resources._600px_Quaggapedia_OOTB
import afrikaburn.composeapp.generated.resources.Laura_Niggeschmidt_Afrika_Burn_76
import afrikaburn.composeapp.generated.resources._20531795
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens

private const val TOTAL_PAGES = 4
private const val PAGE_CONTACT = 3
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

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
        
        AboutScreenPager(pagerState)
        
        PageIndicator(
            currentPage = pagerState.currentPage,
            totalPages = TOTAL_PAGES,
            modifier = Modifier.padding(vertical = Dimens.paddingMedium).fillMaxWidth()
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
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AboutScreenPager(pagerState: androidx.compose.foundation.pager.PagerState) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .weight(1f)
            .semantics {
                contentDescription = "About information carousel"
            }
    ) { page ->
        AboutPageCard(page)
    }
}

@Composable
private fun AboutPageCard(page: Int) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.paddingExtraSmall),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimens.elevationSmall
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = Dimens.paddingExtraSmall),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AboutPageContent(data = getAboutPageData(page))
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