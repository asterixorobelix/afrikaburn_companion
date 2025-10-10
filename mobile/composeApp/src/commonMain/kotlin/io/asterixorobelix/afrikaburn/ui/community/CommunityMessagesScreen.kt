package io.asterixorobelix.afrikaburn.ui.community

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SpeakerNotesOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import io.asterixorobelix.afrikaburn.Dimens
import io.asterixorobelix.afrikaburn.presentation.community.CommunityMessagesViewModel
import io.asterixorobelix.afrikaburn.presentation.community.CommunityMessage
import io.asterixorobelix.afrikaburn.presentation.community.MessageChannel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.screen_community_messages_title
import afrikaburn.composeapp.generated.resources.button_post_message
import afrikaburn.composeapp.generated.resources.cd_back_button
import io.asterixorobelix.afrikaburn.AppTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityMessagesScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel = koinInject<CommunityMessagesViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    
    var showPostDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = { 
                Text(
                    text = stringResource(Res.string.screen_community_messages_title),
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(Res.string.cd_back_button)
                    )
                }
            },
            actions = {
                IconButton(onClick = { showPostDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(Res.string.button_post_message)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // Channel Selection
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingMedium),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(Dimens.paddingMedium),
                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
            ) {
                items(MessageChannel.values().toList()) { channel ->
                    FilterChip(
                        selected = uiState.currentChannel == channel,
                        onClick = { viewModel.selectChannel(channel) },
                        label = { Text(getChannelName(channel)) }
                    )
                }
            }
        }
        
        // Messages List
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.messages.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
                ) {
                    Icon(
                        imageVector = Icons.Default.SpeakerNotesOff,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.iconSizeLarge),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "No messages in ${getChannelName(uiState.currentChannel)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(Dimens.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    MessageCard(
                        message = message,
                        onReact = { emoji -> viewModel.reactToMessage(message.id, emoji) },
                        onReply = { viewModel.startReply(message) }
                    )
                }
            }
        }
    }
    
    // Post Message Dialog
    if (showPostDialog) {
        PostMessageDialog(
            currentChannel = uiState.currentChannel,
            replyingTo = uiState.replyingTo,
            onDismiss = { 
                showPostDialog = false
                viewModel.cancelReply()
            },
            onPost = { content ->
                viewModel.postMessage(content)
                showPostDialog = false
            }
        )
    }
}

@Composable
private fun MessageCard(
    message: CommunityMessage,
    onReact: (String) -> Unit,
    onReply: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (message.isPinned) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationSmall)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingMedium)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
                    ) {
                        if (message.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                modifier = Modifier.size(Dimens.iconSizeSmall),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Text(
                            text = message.authorName,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        ElevatedFilterChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = getChannelName(message.channel),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            selected = true,
                            colors = FilterChipDefaults.elevatedFilterChipColors(
                                containerColor = getChannelColor(message.channel)
                            ),
                            modifier = Modifier.height(Dimens.chipHeight)
                        )
                    }
                    
                    if (message.location != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingExtraSmall)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.iconSizeSmall),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = message.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
            
            // Content
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Reactions and Actions
            if (message.reactions.isNotEmpty() || message.replyToId != null) {
                Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reactions
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingExtraSmall)
                    ) {
                        message.reactions.forEach { (emoji, count) ->
                            AssistChip(
                                onClick = { onReact(emoji) },
                                label = { Text("$emoji $count") },
                                modifier = Modifier.height(Dimens.chipHeight)
                            )
                        }
                    }
                    
                    // Reply button
                    TextButton(onClick = onReply) {
                        Icon(
                            imageVector = Icons.Default.Reply,
                            contentDescription = "Reply",
                            modifier = Modifier.size(Dimens.iconSizeSmall)
                        )
                        Spacer(modifier = Modifier.width(Dimens.paddingExtraSmall))
                        Text("Reply")
                    }
                }
            }
        }
    }
}

@Composable
private fun PostMessageDialog(
    currentChannel: MessageChannel,
    replyingTo: CommunityMessage?,
    onDismiss: () -> Unit,
    onPost: (String) -> Unit
) {
    var message by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dimens.cornerRadiusLarge)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.paddingLarge)
            ) {
                Text(
                    text = if (replyingTo != null) "Reply to ${replyingTo.authorName}" else "Post to ${getChannelName(currentChannel)}",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(Dimens.paddingMedium))
                
                if (replyingTo != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = replyingTo.content,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(Dimens.paddingSmall),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(Dimens.paddingMedium))
                }
                
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Your message") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                Spacer(modifier = Modifier.height(Dimens.paddingMedium))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                    
                    Button(
                        onClick = { onPost(message) },
                        enabled = message.isNotBlank()
                    ) {
                        Text("Post")
                    }
                }
            }
        }
    }
}

private fun getChannelName(channel: MessageChannel): String = when (channel) {
    MessageChannel.GENERAL -> "General"
    MessageChannel.SAFETY -> "Safety"
    MessageChannel.EVENTS -> "Events"
    MessageChannel.CAMPS -> "Camps"
    MessageChannel.LOST_FOUND -> "Lost & Found"
    MessageChannel.RIDES -> "Rides"
    MessageChannel.VOLUNTEER -> "Volunteer"
}

private fun getChannelColor(channel: MessageChannel): androidx.compose.ui.graphics.Color = when (channel) {
    MessageChannel.SAFETY -> androidx.compose.ui.graphics.Color(0xFFE53935)
    MessageChannel.EVENTS -> androidx.compose.ui.graphics.Color(0xFF43A047)
    MessageChannel.LOST_FOUND -> androidx.compose.ui.graphics.Color(0xFFFB8C00)
    MessageChannel.VOLUNTEER -> androidx.compose.ui.graphics.Color(0xFF1E88E5)
    else -> androidx.compose.ui.graphics.Color(0xFF757575)
}

private fun formatTimestamp(instant: Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
}

@Preview
@Composable
private fun CommunityMessagesScreenPreview() {
    AppTheme {
        CommunityMessagesScreen(
            onNavigateBack = {}
        )
    }
}

