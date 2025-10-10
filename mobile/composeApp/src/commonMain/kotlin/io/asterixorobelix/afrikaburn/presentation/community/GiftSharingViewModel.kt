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
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

data class Gift(
    val id: String,
    val title: String,
    val description: String,
    val giverName: String,
    val category: GiftCategory,
    val imageUrl: String? = null,
    val location: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val availableFrom: Instant,
    val availableUntil: Instant? = null,
    val isClaimed: Boolean = false,
    val claimedBy: String? = null,
    val tags: List<String> = emptyList()
)

enum class GiftCategory {
    FOOD, DRINK, CRAFT, SERVICE, EXPERIENCE, OTHER
}

data class GiftRequest(
    val id: String,
    val requesterName: String,
    val description: String,
    val category: GiftCategory,
    val urgency: RequestUrgency,
    val location: String,
    val timestamp: Instant,
    val isFulfilled: Boolean = false
)

enum class RequestUrgency {
    LOW, MEDIUM, HIGH
}

data class GiftSharingUiState(
    val isLoading: Boolean = false,
    val availableGifts: List<Gift> = emptyList(),
    val myGifts: List<Gift> = emptyList(),
    val giftRequests: List<GiftRequest> = emptyList(),
    val selectedGift: Gift? = null,
    val selectedRequest: GiftRequest? = null,
    val currentTab: GiftTab = GiftTab.BROWSE,
    val categoryFilter: GiftCategory? = null,
    val error: String? = null
)

enum class GiftTab {
    BROWSE, MY_GIFTS, REQUESTS
}

class GiftSharingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GiftSharingUiState())
    val uiState: StateFlow<GiftSharingUiState> = _uiState.asStateFlow()

    init {
        loadMockGifts()
    }

    private fun loadMockGifts() {
        val mockGifts = listOf(
            Gift(
                id = "1",
                title = "Handmade Dream Catchers",
                description = "Beautiful dream catchers made with love. Perfect for your tent!",
                giverName = "Artisan Camp",
                category = GiftCategory.CRAFT,
                location = "Near the Temple",
                latitude = -32.395,
                longitude = 19.382,
                availableFrom = Clock.System.now(),
                tags = listOf("handmade", "decoration")
            ),
            Gift(
                id = "2",
                title = "Fresh Fruit Smoothies",
                description = "Come get a refreshing smoothie! Made with fresh fruit and love.",
                giverName = "Smoothie Pirates",
                category = GiftCategory.DRINK,
                location = "Center Camp",
                latitude = -32.394,
                longitude = 19.383,
                availableFrom = Clock.System.now(),
                availableUntil = Clock.System.now().plus(2.toDuration(DurationUnit.HOURS)),
                tags = listOf("refreshing", "healthy")
            ),
            Gift(
                id = "3",
                title = "Sunset Yoga Session",
                description = "Join us for a peaceful yoga session as the sun sets over the playa",
                giverName = "Zen Warriors",
                category = GiftCategory.EXPERIENCE,
                location = "Yoga Dome",
                latitude = -32.396,
                longitude = 19.381,
                availableFrom = Clock.System.now().plus(4.toDuration(DurationUnit.HOURS)),
                tags = listOf("wellness", "relaxation")
            )
        )

        val mockRequests = listOf(
            GiftRequest(
                id = "r1",
                requesterName = "Lost Burner",
                description = "Lost my water bottle, desperately need hydration!",
                category = GiftCategory.DRINK,
                urgency = RequestUrgency.HIGH,
                location = "Near the Man",
                timestamp = Clock.System.now()
            ),
            GiftRequest(
                id = "r2",
                requesterName = "Night Owl Camp",
                description = "Looking for LED lights or glow sticks for our night party",
                category = GiftCategory.OTHER,
                urgency = RequestUrgency.MEDIUM,
                location = "7:30 and C",
                timestamp = Clock.System.now()
            )
        )

        _uiState.update { 
            it.copy(
                availableGifts = mockGifts,
                giftRequests = mockRequests,
                isLoading = false
            ) 
        }
    }

    fun offerGift(
        title: String,
        description: String,
        category: GiftCategory,
        location: String,
        availableHours: Int? = null,
        tags: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            try {
                val newGift = Gift(
                    id = System.currentTimeMillis().toString(),
                    title = title,
                    description = description,
                    giverName = "Me",
                    category = category,
                    location = location,
                    availableFrom = Clock.System.now(),
                    availableUntil = availableHours?.let {
                        Clock.System.now().plus(it.toDuration(DurationUnit.HOURS))
                    },
                    tags = tags
                )

                _uiState.update { state ->
                    state.copy(
                        myGifts = state.myGifts + newGift,
                        availableGifts = state.availableGifts + newGift
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to offer gift: ${e.message}") 
                }
            }
        }
    }

    fun claimGift(giftId: String) {
        _uiState.update { state ->
            state.copy(
                availableGifts = state.availableGifts.map { gift ->
                    if (gift.id == giftId) {
                        gift.copy(isClaimed = true, claimedBy = "Me")
                    } else {
                        gift
                    }
                }
            )
        }
    }

    fun removeGift(giftId: String) {
        _uiState.update { state ->
            state.copy(
                myGifts = state.myGifts.filter { it.id != giftId },
                availableGifts = state.availableGifts.filter { it.id != giftId }
            )
        }
    }

    fun createRequest(
        description: String,
        category: GiftCategory,
        urgency: RequestUrgency,
        location: String
    ) {
        viewModelScope.launch {
            try {
                val newRequest = GiftRequest(
                    id = System.currentTimeMillis().toString(),
                    requesterName = "Me",
                    description = description,
                    category = category,
                    urgency = urgency,
                    location = location,
                    timestamp = Clock.System.now()
                )

                _uiState.update { state ->
                    state.copy(
                        giftRequests = state.giftRequests + newRequest
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to create request: ${e.message}") 
                }
            }
        }
    }

    fun fulfillRequest(requestId: String) {
        _uiState.update { state ->
            state.copy(
                giftRequests = state.giftRequests.map { request ->
                    if (request.id == requestId) {
                        request.copy(isFulfilled = true)
                    } else {
                        request
                    }
                }
            )
        }
    }

    fun selectTab(tab: GiftTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setCategoryFilter(category: GiftCategory?) {
        _uiState.update { it.copy(categoryFilter = category) }
    }

    fun selectGift(gift: Gift) {
        _uiState.update { it.copy(selectedGift = gift) }
    }

    fun selectRequest(request: GiftRequest) {
        _uiState.update { it.copy(selectedRequest = request) }
    }

    fun clearSelection() {
        _uiState.update { 
            it.copy(
                selectedGift = null,
                selectedRequest = null
            ) 
        }
    }

    fun getFilteredGifts(): List<Gift> {
        val gifts = _uiState.value.availableGifts
        val filter = _uiState.value.categoryFilter
        
        return if (filter != null) {
            gifts.filter { it.category == filter && !it.isClaimed }
        } else {
            gifts.filter { !it.isClaimed }
        }
    }

    fun getActiveRequests(): List<GiftRequest> {
        return _uiState.value.giftRequests.filter { !it.isFulfilled }
    }
}