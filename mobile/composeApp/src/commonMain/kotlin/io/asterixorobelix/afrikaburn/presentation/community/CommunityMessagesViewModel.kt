package io.asterixorobelix.afrikaburn.presentation.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class CommunityMessage(
    val id: String,
    val authorName: String,
    val content: String,
    val timestamp: Instant,
    val channel: MessageChannel,
    val replyToId: String? = null,
    val reactions: Map<String, Int> = emptyMap(),
    val isPinned: Boolean = false,
    val attachmentUrl: String? = null,
    val location: String? = null
)

enum class MessageChannel {
    GENERAL, SAFETY, EVENTS, CAMPS, LOST_FOUND, RIDES, VOLUNTEER
}

data class CommunityMessagesUiState(
    val isLoading: Boolean = false,
    val messages: List<CommunityMessage> = emptyList(),
    val currentChannel: MessageChannel = MessageChannel.GENERAL,
    val unreadCounts: Map<MessageChannel, Int> = emptyMap(),
    val isComposing: Boolean = false,
    val replyingTo: CommunityMessage? = null,
    val searchQuery: String = "",
    val error: String? = null
)

class CommunityMessagesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityMessagesUiState())
    val uiState: StateFlow<CommunityMessagesUiState> = _uiState.asStateFlow()

    init {
        loadMockMessages()
    }

    fun selectChannel(channel: MessageChannel) {
        _uiState.update { it.copy(currentChannel = channel) }
        loadMessagesForChannel(channel)
    }
    
    fun postMessage(content: String) {
        if (content.isBlank()) return
        
        viewModelScope.launch {
            val newMessage = CommunityMessage(
                id = System.currentTimeMillis().toString(),
                authorName = "Current User", // In real app, get from user session
                content = content,
                timestamp = Clock.System.now(),
                channel = _uiState.value.currentChannel,
                replyToId = _uiState.value.replyingTo?.id
            )
            
            _uiState.update { state ->
                state.copy(
                    messages = listOf(newMessage) + state.messages,
                    replyingTo = null
                )
            }
        }
    }
    
    fun reactToMessage(messageId: String, emoji: String) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { message ->
                    if (message.id == messageId) {
                        val currentCount = message.reactions[emoji] ?: 0
                        message.copy(
                            reactions = message.reactions + (emoji to currentCount + 1)
                        )
                    } else {
                        message
                    }
                }
            )
        }
    }
    
    fun startReply(message: CommunityMessage) {
        _uiState.update { it.copy(replyingTo = message) }
    }
    
    fun cancelReply() {
        _uiState.update { it.copy(replyingTo = null) }
    }
    
    private fun loadMessagesForChannel(channel: MessageChannel) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.filter { it.channel == channel }
            )
        }
    }

    private fun loadMockMessages() {
        val mockMessages = listOf(
            CommunityMessage(
                id = "1",
                authorName = "Event Coordinator",
                content = "Welcome to AfrikaBurn 2025! The gates are now open. Drive safely and remember to stop at the greeters station!",
                timestamp = Clock.System.now(),
                channel = MessageChannel.GENERAL,
                isPinned = true,
                reactions = mapOf("🔥" to 42, "❤️" to 18)
            ),
            CommunityMessage(
                id = "2",
                authorName = "Rangers",
                content = "Dust storm warning for this afternoon. Please secure your camps and have masks ready.",
                timestamp = Clock.System.now(),
                channel = MessageChannel.SAFETY,
                isPinned = true,
                reactions = mapOf("👍" to 67)
            ),
            CommunityMessage(
                id = "3",
                authorName = "Temple Guardians",
                content = "Sunrise ceremony at the Temple tomorrow at 6 AM. All are welcome to join.",
                timestamp = Clock.System.now(),
                channel = MessageChannel.EVENTS,
                location = "Temple",
                reactions = mapOf("🌅" to 23, "🙏" to 15)
            ),
            CommunityMessage(
                id = "4",
                authorName = "Dusty Burner",
                content = "Found: Blue water bottle near Center Camp. Has 'Playa Love' sticker on it.",
                timestamp = Clock.System.now(),
                channel = MessageChannel.LOST_FOUND,
                location = "Center Camp"
            ),
            CommunityMessage(
                id = "5",
                authorName = "DPW Volunteer",
                content = "We need help setting up the sound stage! Come to 3:00 and Esplanade if you can lend a hand.",
                timestamp = Clock.System.now(),
                channel = MessageChannel.VOLUNTEER,
                location = "3:00 and Esplanade",
                reactions = mapOf("💪" to 8)
            )
        )

        _uiState.update { 
            it.copy(
                messages = mockMessages,
                unreadCounts = mapOf(
                    MessageChannel.SAFETY to 2,
                    MessageChannel.EVENTS to 5,
                    MessageChannel.LOST_FOUND to 3
                ),
                isLoading = false
            ) 
        }
    }

    fun sendMessage(
        content: String,
        channel: MessageChannel = _uiState.value.currentChannel,
        location: String? = null
    ) {
        viewModelScope.launch {
            try {
                val replyTo = _uiState.value.replyingTo
                
                val newMessage = CommunityMessage(
                    id = System.currentTimeMillis().toString(),
                    authorName = "Me",
                    content = content,
                    timestamp = Clock.System.now(),
                    channel = channel,
                    replyToId = replyTo?.id,
                    location = location
                )

                _uiState.update { state ->
                    state.copy(
                        messages = (state.messages + newMessage).sortedByDescending { it.timestamp },
                        replyingTo = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to send message: ${e.message}") 
                }
            }
        }
    }

    fun switchChannel(channel: MessageChannel) {
        _uiState.update { state ->
            state.copy(
                currentChannel = channel,
                // Clear unread count for this channel
                unreadCounts = state.unreadCounts - channel
            )
        }
    }


    fun addReaction(messageId: String, emoji: String) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { message ->
                    if (message.id == messageId) {
                        val currentCount = message.reactions[emoji] ?: 0
                        message.copy(
                            reactions = message.reactions + (emoji to currentCount + 1)
                        )
                    } else {
                        message
                    }
                }
            )
        }
    }

    fun searchMessages(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun getFilteredMessages(): List<CommunityMessage> {
        val state = _uiState.value
        val channelMessages = state.messages.filter { it.channel == state.currentChannel }
        
        return if (state.searchQuery.isBlank()) {
            channelMessages
        } else {
            channelMessages.filter { message ->
                message.content.contains(state.searchQuery, ignoreCase = true) ||
                message.authorName.contains(state.searchQuery, ignoreCase = true)
            }
        }
    }

    fun getPinnedMessages(): List<CommunityMessage> {
        return _uiState.value.messages
            .filter { it.channel == _uiState.value.currentChannel && it.isPinned }
            .sortedByDescending { it.timestamp }
    }

    fun getTotalUnreadCount(): Int {
        return _uiState.value.unreadCounts.values.sum()
    }

    fun markAllAsRead() {
        _uiState.update { it.copy(unreadCounts = emptyMap()) }
    }

    fun refresh() {
        loadMockMessages()
    }
}