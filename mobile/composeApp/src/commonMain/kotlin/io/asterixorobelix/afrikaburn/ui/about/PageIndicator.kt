package io.asterixorobelix.afrikaburn.ui.about

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun PageIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.paddingSmall)
            .semantics {
                contentDescription = "Page ${currentPage + 1} of $totalPages"
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPages) { index ->
            val isSelected = index == currentPage
            val indicatorColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            }

            val horizontalPadding = if (index < totalPages - 1) {
                Dimens.paddingMedium
            } else {
                Dimens.paddingExtraSmall
            }
            
            Box(
                modifier = Modifier
                    .size(if (isSelected) 20.dp else 12.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
                    .padding(horizontal = horizontalPadding)
            )
        }
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
                .padding(Dimens.paddingMedium)
        ) {
            Text(
                text = "Page Indicator Examples",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = Dimens.paddingMedium)
            )

            PageIndicator(
                currentPage = 0,
                totalPages = 4,
                modifier = Modifier.padding(vertical = Dimens.paddingSmall)
            )

            PageIndicator(
                currentPage = 2,
                totalPages = 4,
                modifier = Modifier.padding(vertical = Dimens.paddingSmall)
            )
        }
    }
}