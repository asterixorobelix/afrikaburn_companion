package io.asterixorobelix.afrikaburn.ui.about

import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.about_page1_content
import afrikaburn.composeapp.generated.resources.about_page1_title
import afrikaburn.composeapp.generated.resources.about_page2_content
import afrikaburn.composeapp.generated.resources.about_page2_title
import afrikaburn.composeapp.generated.resources.about_page3_content
import afrikaburn.composeapp.generated.resources.about_page3_title
import afrikaburn.composeapp.generated.resources.about_page4_content
import afrikaburn.composeapp.generated.resources.about_page4_title
import afrikaburn.composeapp.generated.resources.about_title
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
                    .padding(Dimens.paddingSmall),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(
                    defaultElevation = Dimens.elevationSmall
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimens.paddingLarge),
                    contentAlignment = Alignment.Center
                ) {
                    when (page) {
                        0 -> AboutPageContent(
                            title = stringResource(Res.string.about_page1_title),
                            content = stringResource(Res.string.about_page1_content)
                        )

                        1 -> AboutPageContent(
                            title = stringResource(Res.string.about_page2_title),
                            content = stringResource(Res.string.about_page2_content)
                        )

                        2 -> AboutPageContent(
                            title = stringResource(Res.string.about_page3_title),
                            content = stringResource(Res.string.about_page3_content)
                        )

                        3 -> AboutPageContent(
                            title = stringResource(Res.string.about_page4_title),
                            content = stringResource(Res.string.about_page4_content)
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