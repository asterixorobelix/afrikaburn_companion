package io.asterixorobelix.afrikaburn.ui.schedule

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.asterixorobelix.afrikaburn.models.*
import io.asterixorobelix.afrikaburn.Dimens
import kotlinx.datetime.*

enum class ScheduleViewMode {
    DAY_VIEW,
    AGENDA_VIEW
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalScheduleScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEvent: (String) -> Unit,
    onNavigateToAddItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(ScheduleViewMode.DAY_VIEW) }
    var selectedDate by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date) }
    var showRemoveDialog by remember { mutableStateOf<PersonalScheduleItem?>(null) }
    
    // Mock data for preview
    val scheduleItems = remember { mockScheduleItems }
    val conflicts = remember { detectConflicts(scheduleItems) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Schedule") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        viewMode = if (viewMode == ScheduleViewMode.DAY_VIEW) 
                            ScheduleViewMode.AGENDA_VIEW 
                        else 
                            ScheduleViewMode.DAY_VIEW 
                    }) {
                        Icon(
                            if (viewMode == ScheduleViewMode.DAY_VIEW) 
                                Icons.Default.ViewAgenda 
                            else 
                                Icons.Default.CalendarToday,
                            contentDescription = "Toggle view"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddItem,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add to schedule")
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Date selector
            DateSelector(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                modifier = Modifier.fillMaxWidth()
            )
            
            Divider()
            
            // Schedule content
            AnimatedContent(
                targetState = viewMode,
                label = "Schedule view mode"
            ) { mode ->
                when (mode) {
                    ScheduleViewMode.DAY_VIEW -> DayView(
                        date = selectedDate,
                        scheduleItems = scheduleItems.filter { 
                            it.startTime.date == selectedDate 
                        },
                        conflicts = conflicts,
                        onItemClick = { item ->
                            when (item) {
                                is PersonalScheduleItem.EventItem -> onNavigateToEvent(item.eventId)
                                is PersonalScheduleItem.CustomItem -> showRemoveDialog = item
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    ScheduleViewMode.AGENDA_VIEW -> AgendaView(
                        scheduleItems = scheduleItems.filter { 
                            it.startTime.date >= selectedDate 
                        },
                        conflicts = conflicts,
                        onItemClick = { item ->
                            when (item) {
                                is PersonalScheduleItem.EventItem -> onNavigateToEvent(item.eventId)
                                is PersonalScheduleItem.CustomItem -> showRemoveDialog = item
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        
        // Remove item dialog
        showRemoveDialog?.let { item ->
            AlertDialog(
                onDismissRequest = { showRemoveDialog = null },
                title = { Text("Remove from Schedule") },
                text = { 
                    Text("Remove \"${item.title}\" from your personal schedule?") 
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Handle remove
                            showRemoveDialog = null
                        }
                    ) {
                        Text("Remove")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRemoveDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun DateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val eventDates = remember { 
        // Mock event dates - replace with actual event dates
        (0..6).map { selectedDate.plus(it, DateTimeUnit.DAY) }
    }
    
    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(vertical = Dimens.paddingMedium),
        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
    ) {
        Spacer(modifier = Modifier.width(Dimens.paddingMedium))
        
        eventDates.forEach { date ->
            DateChip(
                date = date,
                isSelected = date == selectedDate,
                onClick = { onDateSelected(date) }
            )
        }
        
        Spacer(modifier = Modifier.width(Dimens.paddingMedium))
    }
}

@Composable
private fun DateChip(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.surfaceVariant,
        label = "Date chip background"
    )
    
    val contentColor = if (isSelected) 
        MaterialTheme.colorScheme.onPrimary 
    else 
        MaterialTheme.colorScheme.onSurfaceVariant
    
    Surface(
        onClick = onClick,
        color = backgroundColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(Dimens.cornerRadiusMedium),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(
                horizontal = Dimens.paddingMedium,
                vertical = Dimens.paddingSmall
            )
        ) {
            Text(
                text = date.dayOfWeek.name.take(3),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DayView(
    date: LocalDate,
    scheduleItems: List<PersonalScheduleItem>,
    conflicts: Set<Conflict>,
    onItemClick: (PersonalScheduleItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val hours = (0..23).toList()
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(Dimens.paddingMedium)
    ) {
        items(hours) { hour ->
            HourRow(
                hour = hour,
                items = scheduleItems.filter { item ->
                    item.startTime.hour == hour
                },
                conflicts = conflicts,
                onItemClick = onItemClick
            )
            
            if (hour < 23) {
                Divider(
                    modifier = Modifier.padding(vertical = Dimens.paddingSmall),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun HourRow(
    hour: Int,
    items: List<PersonalScheduleItem>,
    conflicts: Set<Conflict>,
    onItemClick: (PersonalScheduleItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
    ) {
        // Hour label
        Text(
            text = "${hour.toString().padStart(2, '0')}:00",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(50.dp)
        )
        
        // Schedule items
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
        ) {
            items.forEach { item ->
                val hasConflict = conflicts.any { conflict ->
                    conflict.items.contains(item)
                }
                
                ScheduleItemCard(
                    item = item,
                    hasConflict = hasConflict,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Composable
private fun AgendaView(
    scheduleItems: List<PersonalScheduleItem>,
    conflicts: Set<Conflict>,
    onItemClick: (PersonalScheduleItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val groupedItems = scheduleItems.groupBy { it.startTime.date }
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(Dimens.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
    ) {
        groupedItems.forEach { (date, items) ->
            item {
                Text(
                    text = formatDate(date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = Dimens.paddingSmall)
                )
            }
            
            items(items.sortedBy { it.startTime }) { item ->
                val hasConflict = conflicts.any { conflict ->
                    conflict.items.contains(item)
                }
                
                ScheduleItemCard(
                    item = item,
                    hasConflict = hasConflict,
                    showTime = true,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleItemCard(
    item: PersonalScheduleItem,
    hasConflict: Boolean,
    showTime: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = if (hasConflict) 0.7f else 1f,
        label = "Item alpha"
    )
    
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha),
        colors = CardDefaults.cardColors(
            containerColor = when (item) {
                is PersonalScheduleItem.EventItem -> MaterialTheme.colorScheme.primaryContainer
                is PersonalScheduleItem.CustomItem -> MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingMedium),
            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                imageVector = when (item) {
                    is PersonalScheduleItem.EventItem -> Icons.Default.Event
                    is PersonalScheduleItem.CustomItem -> Icons.Default.Schedule
                },
                contentDescription = null,
                modifier = Modifier.size(Dimens.iconSizeSmall)
            )
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (showTime) {
                    Text(
                        text = "${formatTime(item.startTime)} - ${formatTime(item.endTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                item.location?.let { location ->
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Conflict indicator
            if (hasConflict) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Schedule conflict",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(Dimens.iconSizeSmall)
                )
            }
        }
    }
}

// Helper functions
private fun detectConflicts(items: List<PersonalScheduleItem>): Set<Conflict> {
    val conflicts = mutableSetOf<Conflict>()
    
    for (i in items.indices) {
        for (j in i + 1 until items.size) {
            val item1 = items[i]
            val item2 = items[j]
            
            // Check if times overlap
            if (item1.startTime < item2.endTime && item2.startTime < item1.endTime) {
                conflicts.add(Conflict(setOf(item1, item2)))
            }
        }
    }
    
    return conflicts
}

private fun formatDate(date: LocalDate): String {
    return "${date.dayOfWeek.name.take(3)}, ${date.month.name.take(3)} ${date.dayOfMonth}"
}

private fun formatTime(dateTime: LocalDateTime): String {
    return "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
}

// Data classes
sealed class PersonalScheduleItem {
    abstract val id: String
    abstract val title: String
    abstract val startTime: LocalDateTime
    abstract val endTime: LocalDateTime
    abstract val location: String?
    
    data class EventItem(
        override val id: String,
        override val title: String,
        override val startTime: LocalDateTime,
        override val endTime: LocalDateTime,
        override val location: String?,
        val eventId: String,
        val artistName: String
    ) : PersonalScheduleItem()
    
    data class CustomItem(
        override val id: String,
        override val title: String,
        override val startTime: LocalDateTime,
        override val endTime: LocalDateTime,
        override val location: String?,
        val notes: String? = null
    ) : PersonalScheduleItem()
}

data class Conflict(
    val items: Set<PersonalScheduleItem>
)

// Mock data for preview
private val mockScheduleItems = listOf(
    PersonalScheduleItem.EventItem(
        id = "1",
        title = "Desert Techno Sunrise Set",
        startTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).let {
            LocalDateTime(it.date, LocalTime(10, 0))
        },
        endTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).let {
            LocalDateTime(it.date, LocalTime(12, 0))
        },
        location = "Binnekring",
        eventId = "event1",
        artistName = "DJ Dust Storm"
    ),
    PersonalScheduleItem.CustomItem(
        id = "2",
        title = "Lunch with camp",
        startTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).let {
            LocalDateTime(it.date, LocalTime(12, 30))
        },
        endTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).let {
            LocalDateTime(it.date, LocalTime(13, 30))
        },
        location = "Camp Awesome",
        notes = "Bring the cooler box"
    ),
    PersonalScheduleItem.EventItem(
        id = "3",
        title = "Fire Performance Workshop",
        startTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).let {
            LocalDateTime(it.date, LocalTime(11, 30))
        },
        endTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).let {
            LocalDateTime(it.date, LocalTime(13, 0))
        },
        location = "Flame Village",
        eventId = "event2",
        artistName = "Pyro Collective"
    ),
    PersonalScheduleItem.EventItem(
        id = "4",
        title = "Sunset Drum Circle",
        startTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).let {
            LocalDateTime(it.date, LocalTime(18, 0))
        },
        endTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).let {
            LocalDateTime(it.date, LocalTime(20, 0))
        },
        location = "Main Circle",
        eventId = "event3",
        artistName = "Rhythm Tribe"
    ),
    PersonalScheduleItem.CustomItem(
        id = "5",
        title = "Temple visit",
        startTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).let {
            LocalDateTime(it.date.plus(1, DateTimeUnit.DAY), LocalTime(9, 0))
        },
        endTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).let {
            LocalDateTime(it.date.plus(1, DateTimeUnit.DAY), LocalTime(10, 0))
        },
        location = "Temple",
        notes = "Morning meditation"
    )
)

@Composable
fun PersonalScheduleScreenPreview() {
    MaterialTheme {
        PersonalScheduleScreen(
            onNavigateBack = {},
            onNavigateToEvent = {},
            onNavigateToAddItem = {}
        )
    }
}