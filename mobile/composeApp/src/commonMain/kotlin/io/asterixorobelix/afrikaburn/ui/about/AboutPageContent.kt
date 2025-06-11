package io.asterixorobelix.afrikaburn.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

data class AboutPageData(
    val title: String,
    val content: String,
    val imagePainter: Painter? = null,
    val buttonText: String? = null,
    val url: String? = null,
    val imageContentDescription: String? = null
)

@Composable
fun AboutPageContent(data: AboutPageData) {
    val uriHandler = LocalUriHandler.current
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
    ) {
        data.imagePainter?.let { painter ->
            Image(
                painter = painter,
                contentDescription = data.imageContentDescription ?: "About page illustration",
                modifier = Modifier
                    .size(120.dp)
                    .clip(MaterialTheme.shapes.large)
            )
            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
        }
        
        Text(
            text = data.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = data.content,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        if (data.buttonText != null && data.url != null) {
            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
            Button(
                onClick = {
                    uriHandler.openUri(data.url)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = data.buttonText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun AboutPageContentPreview() {
    AppTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
        ) {
            // Without image and button
            AboutPageContent(
                data = AboutPageData(
                    title = "Welcome to AfrikaBurn",
                    content = "Your companion app for the AfrikaBurn experience in the Tankwa Karoo."
                )
            )
            
            // With button
            AboutPageContent(
                data = AboutPageData(
                    title = "About AfrikaBurn",
                    content = "Learn more about this amazing event in the Tankwa Karoo.",
                    buttonText = "Visit Website",
                    url = "https://afrikaburn.com"
                )
            )
        }
    }
}