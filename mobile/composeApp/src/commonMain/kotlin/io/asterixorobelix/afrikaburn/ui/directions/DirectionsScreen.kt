package io.asterixorobelix.afrikaburn.ui.directions

import afrikaburn.composeapp.generated.resources.New_Turnoff
import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.about_title
import afrikaburn.composeapp.generated.resources.button_directions
import afrikaburn.composeapp.generated.resources.cd_about_page4_image
import afrikaburn.composeapp.generated.resources.direction_content
import afrikaburn.composeapp.generated.resources.direction_sub_title
import afrikaburn.composeapp.generated.resources.direction_title
import afrikaburn.composeapp.generated.resources.direction_url
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import io.asterixorobelix.afrikaburn.Dimens
import io.asterixorobelix.afrikaburn.ui.about.AboutPageContent
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
        Text(
            text = stringResource(Res.string.direction_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(Dimens.paddingMedium)
        )
        AboutPageContent(
            title = stringResource(Res.string.direction_sub_title),
            content = stringResource(Res.string.direction_content),
            buttonText = stringResource(Res.string.button_directions),
            url = stringResource(Res.string.direction_url),
            imagePainter = painterResource(Res.drawable.New_Turnoff),
            imageContentDescription = stringResource(Res.string.cd_about_page4_image)
        )
    }
}

@Preview
@Composable
private fun DirectionsScreenPreview() {
    DirectionsScreen()
}