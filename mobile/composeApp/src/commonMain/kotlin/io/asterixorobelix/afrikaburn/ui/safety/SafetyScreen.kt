package io.asterixorobelix.afrikaburn.ui.safety

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
import org.jetbrains.compose.ui.tooling.preview.Preview

// Data models
data class EmergencyContact(
    val name: String,
    val description: String,
    val phoneNumber: String? = null,
    val radioChannel: String? = null,
    val icon: ImageVector
)

data class ResourceLocation(
    val name: String,
    val type: ResourceType,
    val description: String,
    val coordinates: Pair<Double, Double>? = null,
    val landmarks: List<String> = emptyList()
)

enum class ResourceType(val icon: ImageVector, val displayName: String) {
    WATER(Icons.Default.WaterDrop, "Water"),
    ICE(Icons.Default.AcUnit, "Ice"),
    MEDICAL(Icons.Default.LocalHospital, "Medical"),
    HELP(Icons.Default.Help, "Help Point"),
    RANGERS(Icons.Default.Security, "Rangers")
}

data class SafetyTip(
    val title: String,
    val content: String,
    val priority: Priority = Priority.NORMAL,
    val icon: ImageVector = Icons.Default.Info
)

enum class Priority {
    HIGH, NORMAL, LOW
}

@Composable
fun SafetyScreen(
    userLocation: Pair<Double, Double>? = null,
    onEmergencyCall: (EmergencyContact) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        SafetyHeader()
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Emergency") },
                icon = { Icon(Icons.Default.Emergency, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Resources") },
                icon = { Icon(Icons.Default.Place, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Safety Tips") },
                icon = { Icon(Icons.Default.Lightbulb, contentDescription = null) }
            )
        }
        
        // Content
        when (selectedTab) {
            0 -> EmergencyContactsContent(onEmergencyCall)
            1 -> ResourceLocationsContent(userLocation)
            2 -> SafetyTipsContent()
        }
    }
}

@Composable
private fun SafetyHeader() {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Safety",
                modifier = Modifier.size(Dimens.iconSizeLarge),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
            Text(
                text = "Safety & Emergency",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Stay safe in the desert",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmergencyContactsContent(
    onEmergencyCall: (EmergencyContact) -> Unit
) {
    val emergencyContacts = remember { getEmergencyContacts() }
    
    LazyColumn(
        contentPadding = PaddingValues(Dimens.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
    ) {
        item {
            AlertCard()
            Spacer(modifier = Modifier.height(Dimens.paddingMedium))
        }
        
        items(emergencyContacts) { contact ->
            EmergencyContactCard(
                contact = contact,
                onCall = { onEmergencyCall(contact) }
            )
        }
    }
}

@Composable
private fun AlertCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(Dimens.paddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(Dimens.iconSizeMedium)
            )
            Spacer(modifier = Modifier.width(Dimens.paddingMedium))
            Column {
                Text(
                    text = "Emergency Procedures",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "In case of emergency, stay calm and contact the appropriate service below",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun EmergencyContactCard(
    contact: EmergencyContact,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCall() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(Dimens.paddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = contact.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimens.iconSizeLarge)
            )
            Spacer(modifier = Modifier.width(Dimens.paddingMedium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = contact.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                contact.phoneNumber?.let {
                    Text(
                        text = "Phone: $it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                contact.radioChannel?.let {
                    Text(
                        text = "Radio: $it",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Call",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ResourceLocationsContent(
    userLocation: Pair<Double, Double>?
) {
    val resources = remember { getResourceLocations() }
    val groupedResources = remember { resources.groupBy { it.type } }
    
    LazyColumn(
        contentPadding = PaddingValues(Dimens.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
    ) {
        item {
            if (userLocation != null) {
                NearestResourceCard(userLocation, resources)
                Spacer(modifier = Modifier.height(Dimens.paddingMedium))
            }
        }
        
        groupedResources.forEach { (type, locations) ->
            item {
                ResourceTypeHeader(type)
            }
            items(locations) { location ->
                ResourceLocationCard(location, userLocation)
            }
            item {
                Spacer(modifier = Modifier.height(Dimens.paddingSmall))
            }
        }
    }
}

@Composable
private fun NearestResourceCard(
    userLocation: Pair<Double, Double>,
    resources: List<ResourceLocation>
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingMedium)
        ) {
            Text(
                text = "Nearest Help Points",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
            // In a real app, calculate actual distances
            Text(
                text = "Medical: ~500m NE",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "Water: ~200m W",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun ResourceTypeHeader(type: ResourceType) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = Dimens.paddingSmall)
    ) {
        Icon(
            imageVector = type.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimens.iconSizeMedium)
        )
        Spacer(modifier = Modifier.width(Dimens.paddingSmall))
        Text(
            text = type.displayName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ResourceLocationCard(
    location: ResourceLocation,
    userLocation: Pair<Double, Double>?
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingMedium)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = location.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
            
            if (expanded && location.landmarks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                Divider()
                Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                Text(
                    text = "Landmarks:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                location.landmarks.forEach { landmark ->
                    Row(
                        modifier = Modifier.padding(start = Dimens.paddingMedium, top = Dimens.paddingExtraSmall)
                    ) {
                        Text("• ", style = MaterialTheme.typography.bodySmall)
                        Text(landmark, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun SafetyTipsContent() {
    val safetyTips = remember { getSafetyTips() }
    val groupedTips = remember { safetyTips.groupBy { it.priority } }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.paddingMedium)
    ) {
        // High priority tips first
        listOf(Priority.HIGH, Priority.NORMAL, Priority.LOW).forEach { priority ->
            groupedTips[priority]?.let { tips ->
                if (priority == Priority.HIGH) {
                    Text(
                        text = "Critical Safety Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = Dimens.paddingSmall)
                    )
                }
                
                tips.forEach { tip ->
                    SafetyTipCard(tip, priority)
                    Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                }
                
                if (priority == Priority.HIGH) {
                    Divider(modifier = Modifier.padding(vertical = Dimens.paddingMedium))
                    Text(
                        text = "General Safety Tips",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = Dimens.paddingSmall)
                    )
                }
            }
        }
    }
}

@Composable
private fun SafetyTipCard(
    tip: SafetyTip,
    priority: Priority
) {
    var expanded by remember { mutableStateOf(priority == Priority.HIGH) }
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (priority) {
                Priority.HIGH -> MaterialTheme.colorScheme.errorContainer
                Priority.NORMAL -> MaterialTheme.colorScheme.surface
                Priority.LOW -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingMedium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = tip.icon,
                    contentDescription = null,
                    tint = when (priority) {
                        Priority.HIGH -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(Dimens.iconSizeMedium)
                )
                Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                Text(
                    text = tip.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (priority) {
                        Priority.HIGH -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    }
                )
            }
        }
    }
}

// Sample data functions
private fun getEmergencyContacts(): List<EmergencyContact> = listOf(
    EmergencyContact(
        name = "Rangers",
        description = "Fire safety, lost persons, and general emergencies",
        phoneNumber = "+27 XX XXX XXXX",
        radioChannel = "Channel 5",
        icon = Icons.Default.Security
    ),
    EmergencyContact(
        name = "Medical",
        description = "Medical emergencies and health concerns",
        phoneNumber = "+27 XX XXX XXXX",
        radioChannel = "Channel 3",
        icon = Icons.Default.LocalHospital
    ),
    EmergencyContact(
        name = "Sanctuary",
        description = "Mental health support and crisis intervention",
        phoneNumber = "+27 XX XXX XXXX",
        radioChannel = "Channel 7",
        icon = Icons.Default.Favorite
    ),
    EmergencyContact(
        name = "Site Ops",
        description = "Infrastructure and site-related issues",
        radioChannel = "Channel 1",
        icon = Icons.Default.Engineering
    )
)

private fun getResourceLocations(): List<ResourceLocation> = listOf(
    ResourceLocation(
        name = "Center Camp Water",
        type = ResourceType.WATER,
        description = "Free water refill station",
        landmarks = listOf("Near the main stage", "Blue water tanks", "Open 24/7")
    ),
    ResourceLocation(
        name = "Binnekring Water Point",
        type = ResourceType.WATER,
        description = "Water refill station",
        landmarks = listOf("At 3:00 position", "Look for flags", "Solar powered pumps")
    ),
    ResourceLocation(
        name = "Ice Sales - Center Camp",
        type = ResourceType.ICE,
        description = "Ice available for purchase",
        landmarks = listOf("Near the info desk", "Morning and evening sales", "Bring a cooler")
    ),
    ResourceLocation(
        name = "Medical Station",
        type = ResourceType.MEDICAL,
        description = "24/7 medical assistance",
        landmarks = listOf("Red cross flags", "Near Center Camp", "Staffed by professionals")
    ),
    ResourceLocation(
        name = "Ranger Outpost - Binnekring",
        type = ResourceType.RANGERS,
        description = "Rangers on duty",
        landmarks = listOf("Green flags", "Radio available", "Fire safety equipment")
    )
)

private fun getSafetyTips(): List<SafetyTip> = listOf(
    SafetyTip(
        title = "Hydration is Critical",
        content = "Drink at least 4 liters of water per day. The desert environment is extremely dehydrating. Add electrolytes to your water and avoid excessive alcohol consumption during the day.",
        priority = Priority.HIGH,
        icon = Icons.Default.WaterDrop
    ),
    SafetyTip(
        title = "Sun Protection",
        content = "The desert sun is intense. Wear sunscreen (SPF 50+), a wide-brimmed hat, and protective clothing. Seek shade during peak hours (11am-3pm). Sunburn can happen in minutes.",
        priority = Priority.HIGH,
        icon = Icons.Default.WbSunny
    ),
    SafetyTip(
        title = "Dust Storms",
        content = "During dust storms: Seek shelter immediately, protect your eyes and breathing with goggles and dust masks, secure loose items, and wait for the storm to pass. Visibility can drop to zero.",
        priority = Priority.HIGH,
        icon = Icons.Default.Air
    ),
    SafetyTip(
        title = "Night Navigation",
        content = "Always carry a flashlight or headlamp. Mark your camp with unique lights or flags. The playa is disorienting at night. Consider using GPS waypoints for your camp location.",
        priority = Priority.NORMAL,
        icon = Icons.Default.FlashlightOn
    ),
    SafetyTip(
        title = "Fire Safety",
        content = "Keep fire extinguishers nearby when using flames. Never leave fires unattended. Check burn regulations daily. Report any uncontrolled fires immediately to Rangers.",
        priority = Priority.HIGH,
        icon = Icons.Default.LocalFireDepartment
    ),
    SafetyTip(
        title = "Food Safety",
        content = "Keep perishables in coolers with ice. The heat accelerates spoilage. When in doubt, throw it out. Food poisoning in the desert can be dangerous.",
        priority = Priority.NORMAL,
        icon = Icons.Default.Restaurant
    ),
    SafetyTip(
        title = "Vehicle Safety",
        content = "Drive slowly (5mph/8kph max). Watch for pedestrians and cyclists. Use lights at night. Never drive under the influence.",
        priority = Priority.NORMAL,
        icon = Icons.Default.DirectionsCar
    ),
    SafetyTip(
        title = "Personal Boundaries",
        content = "Consent is essential. Respect others' boundaries. If you feel unsafe, find Rangers or Sanctuary. Look out for each other.",
        priority = Priority.NORMAL,
        icon = Icons.Default.People
    )
)

@Preview
@Composable
private fun SafetyScreenPreview() {
    AppTheme {
        SafetyScreen()
    }
}

@Preview
@Composable
private fun SafetyScreenDarkPreview() {
    AppTheme(useDarkTheme = true) {
        SafetyScreen()
    }
}