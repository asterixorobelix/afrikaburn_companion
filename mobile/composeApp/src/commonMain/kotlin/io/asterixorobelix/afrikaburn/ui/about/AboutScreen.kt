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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
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
        Text(
            text = stringResource(Res.string.about_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        val pagerState = rememberPagerState(pageCount = { 4 })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .semantics {
                    contentDescription = "About information carousel"
                }
        ) { page ->
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
                    when (page) {
                        0 -> AboutPageContent(
                            title = stringResource(Res.string.about_page1_title),
                            content = stringResource(Res.string.about_page1_content),
                            buttonText = stringResource(Res.string.button_learn_more),
                            url = stringResource(Res.string.about_page1_url)
                        )

                        1 -> AboutPageContent(
                            title = stringResource(Res.string.about_page2_title),
                            content = stringResource(Res.string.about_page2_content),
                            buttonText = stringResource(Res.string.button_visit_website),
                            url = stringResource(Res.string.about_page2_url)
                        )

                        2 -> AboutPageContent(
                            title = stringResource(Res.string.about_page3_title),
                            content = stringResource(Res.string.about_page3_content),
                            buttonText = stringResource(Res.string.button_view_theme),
                            url = stringResource(Res.string.about_page3_url)
                        )

                        3 -> AboutPageContent(
                            title = stringResource(Res.string.about_page4_title),
                            content = stringResource(Res.string.about_page4_content),
                            buttonText = stringResource(Res.string.button_contact),
                            url = stringResource(Res.string.about_page4_url)
                        )
                    }
                }
            }
        }

        PageIndicator(
            currentPage = pagerState.currentPage,
            totalPages = 4,
            modifier = Modifier.padding(vertical = Dimens.paddingMedium).fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun AboutScreenPreview() {
    AppTheme {
        AboutScreen()
    }
}