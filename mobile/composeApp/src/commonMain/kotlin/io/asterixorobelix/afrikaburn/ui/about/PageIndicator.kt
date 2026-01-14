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

private val INDICATOR_SIZE_SELECTED = 12.dp
private val INDICATOR_SIZE_UNSELECTED = 8.dp
private const val UNSELECTED_ALPHA = 0.3f

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
        horizontalArrangement = Arrangement.spacedBy(
            Dimens.paddingSmall,
            Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPages) { index ->
            val isSelected = index == currentPage
            val indicatorColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = UNSELECTED_ALPHA)
            }

            Box(
                modifier = Modifier
                    .size(if (isSelected) INDICATOR_SIZE_SELECTED else INDICATOR_SIZE_UNSELECTED)
                    .clip(CircleShape)
                    .background(indicatorColor)
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