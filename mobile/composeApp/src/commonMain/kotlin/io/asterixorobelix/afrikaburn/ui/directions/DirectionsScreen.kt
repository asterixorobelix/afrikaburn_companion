package io.asterixorobelix.afrikaburn.ui.directions

import afrikaburn.composeapp.generated.resources.New_Turnoff
import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.button_directions
import afrikaburn.composeapp.generated.resources.cd_about_page4_image
import afrikaburn.composeapp.generated.resources.direction_content
import afrikaburn.composeapp.generated.resources.direction_title
import afrikaburn.composeapp.generated.resources.direction_url
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.asterixorobelix.afrikaburn.Dimens
import io.asterixorobelix.afrikaburn.ui.about.AboutPageContent
import io.asterixorobelix.afrikaburn.ui.about.AboutPageData
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DirectionsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(Dimens.paddingMedium)
    ) {
        AboutPageContent(
            data = AboutPageData(
                title = stringResource(Res.string.direction_title),
                content = stringResource(Res.string.direction_content),
                buttonText = stringResource(Res.string.button_directions),
                url = stringResource(Res.string.direction_url),
                imagePainter = painterResource(Res.drawable.New_Turnoff),
                imageContentDescription = stringResource(Res.string.cd_about_page4_image)
            )
        )
    }
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun DirectionsScreenPreview() {
    DirectionsScreen()
}