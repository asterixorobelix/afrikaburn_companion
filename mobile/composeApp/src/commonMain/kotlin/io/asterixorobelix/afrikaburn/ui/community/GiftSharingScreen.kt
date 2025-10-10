package io.asterixorobelix.afrikaburn.ui.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.asterixorobelix.afrikaburn.Dimens
import io.asterixorobelix.afrikaburn.presentation.community.GiftSharingViewModel
import io.asterixorobelix.afrikaburn.presentation.community.Gift
import io.asterixorobelix.afrikaburn.presentation.community.GiftCategory
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.screen_gift_sharing_title
import afrikaburn.composeapp.generated.resources.button_offer_gift
import afrikaburn.composeapp.generated.resources.cd_back_button
import io.asterixorobelix.afrikaburn.AppTheme
import kotlinx.datetime.Instant

@Composable
fun GiftSharingScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: GiftSharingViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    
    var showOfferDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.paddingMedium)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(Res.string.cd_back_button),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Text(
                text = stringResource(Res.string.screen_gift_sharing_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            IconButton(onClick = { showOfferDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.button_offer_gift),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(Dimens.paddingMedium))
        
        // Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
        ) {
            item {
                FilterChip(
                    selected = uiState.categoryFilter == null,
                    onClick = { viewModel.setCategoryFilter(null) },
                    label = { Text("All") }
                )
            }
            items(GiftCategory.entries.toTypedArray()) { category ->
                FilterChip(
                    selected = uiState.categoryFilter == category,
                    onClick = { viewModel.setCategoryFilter(category) },
                    label = { Text(category.name.replace("_", " ")) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(Dimens.paddingMedium))
        
        // Gifts List
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
            ) {
                items(uiState.availableGifts) { gift ->
                    GiftCard(
                        gift = gift,
                        onClaim = { viewModel.claimGift(gift.id) }
                    )
                }
            }
        }
    }
    
    // Offer Gift Dialog
    if (showOfferDialog) {
        OfferGiftDialog(
            onDismiss = { showOfferDialog = false },
            onOffer = { title, description, category, location ->
                viewModel.offerGift(title, description, category, location)
                showOfferDialog = false
            }
        )
    }
}

@Composable
private fun GiftCard(
    gift: Gift,
    onClaim: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationSmall)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = gift.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(Dimens.paddingExtraSmall))
                    
                    Text(
                        text = gift.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
                    ) {
                        AssistChip(
                            onClick = { },
                            label = { Text(gift.category.name.replace("_", " ")) },
                            leadingIcon = {
                                Icon(
                                    imageVector = getCategoryIcon(gift.category),
                                    contentDescription = null,
                                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                                )
                            }
                        )
                        
                        if (!gift.isClaimed && gift.location.isNotBlank()) {
                            AssistChip(
                                onClick = { },
                                label = { Text(gift.location) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(AssistChipDefaults.IconSize)
                                    )
                                }
                            )
                        }
                    }
                }
                
                if (!gift.isClaimed) {
                    TextButton(onClick = onClaim) {
                        Text("Claim")
                    }
                }
            }
            
            if (gift.isClaimed) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.paddingSmall),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Claimed",
                        modifier = Modifier.padding(Dimens.paddingSmall),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun OfferGiftDialog(
    onDismiss: () -> Unit,
    onOffer: (String, String, GiftCategory, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(GiftCategory.OTHER) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Offer a Gift") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Gift Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Category Selection
                Column {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelLarge
                    )
                    
                    Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                    
                    GiftCategory.entries.forEach { category ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category }
                            )
                            
                            Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                            
                            Text(
                                text = category.name.replace("_", " "),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank() && location.isNotBlank()) {
                        onOffer(title, description, selectedCategory, location)
                    }
                }
            ) {
                Text("Offer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getCategoryIcon(category: GiftCategory) = when (category) {
    GiftCategory.FOOD -> Icons.Default.Restaurant
    GiftCategory.DRINK -> Icons.Default.LocalDrink
    GiftCategory.CRAFT -> Icons.Default.Build
    GiftCategory.SERVICE -> Icons.Default.Handyman
    GiftCategory.EXPERIENCE -> Icons.Default.Stars
    GiftCategory.OTHER -> Icons.Default.Category
}

@Preview
@Composable
private fun GiftSharingScreenPreview() {
    AppTheme {
        GiftSharingScreen(onNavigateBack = {})
    }
}