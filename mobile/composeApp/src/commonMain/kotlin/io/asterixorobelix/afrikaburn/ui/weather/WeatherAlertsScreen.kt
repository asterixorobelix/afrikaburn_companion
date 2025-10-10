package io.asterixorobelix.afrikaburn.ui.weather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.asterixorobelix.afrikaburn.AppTheme
import io.asterixorobelix.afrikaburn.Dimens
import io.asterixorobelix.afrikaburn.domain.model.WeatherAlert
import io.asterixorobelix.afrikaburn.domain.model.WeatherAlertType
import io.asterixorobelix.afrikaburn.domain.model.WeatherSeverity
import io.asterixorobelix.afrikaburn.presentation.weather.WeatherAlertsViewModel
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherAlertsScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherAlertsViewModel? = null
) {
    val actualViewModel: WeatherAlertsViewModel = viewModel ?: koinInject()
    val uiState by actualViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Weather Alerts",
                        style = MaterialTheme.typography.headlineSmall
                    ) 
                },
                actions = {
                    IconButton(onClick = { actualViewModel.refreshAlerts() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh alerts"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Current conditions header
            CurrentConditionsCard(
                temperature = uiState.currentTemperature,
                windSpeed = uiState.currentWindSpeed,
                visibility = uiState.currentVisibility,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.paddingMedium)
            )

            // Update timer
            UpdateTimerCard(
                secondsUntilUpdate = uiState.secondsUntilNextUpdate,
                isUpdating = uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.paddingMedium)
            )

            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

            // Active alerts
            when {
                uiState.isLoading && uiState.alerts.isEmpty() -> {
                    LoadingContent()
                }
                uiState.error != null -> {
                    ErrorContent(error = uiState.error!!)
                }
                uiState.alerts.isEmpty() -> {
                    NoAlertsContent()
                }
                else -> {
                    AlertsList(
                        alerts = uiState.alerts,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentConditionsCard(
    temperature: Double?,
    windSpeed: Double?,
    visibility: Double?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationNormal)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingMedium),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ConditionItem(
                icon = Icons.Default.Thermostat,
                label = "Temperature",
                value = temperature?.let { "${it.toInt()}°C" } ?: "--"
            )
            ConditionItem(
                icon = Icons.Default.Air,
                label = "Wind",
                value = windSpeed?.let { "${it.toInt()} km/h" } ?: "--"
            )
            ConditionItem(
                icon = Icons.Default.Visibility,
                label = "Visibility",
                value = visibility?.let { "${it} km" } ?: "--"
            )
        }
    }
}

@Composable
private fun ConditionItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingExtraSmall)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(Dimens.iconSizeMedium),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun UpdateTimerCard(
    secondsUntilUpdate: Int,
    isUpdating: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Update,
                contentDescription = "Update timer",
                modifier = Modifier.size(Dimens.iconSizeSmall),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.padding(horizontal = Dimens.paddingExtraSmall))
            
            if (isUpdating) {
                Text(
                    text = "Updating...",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.padding(horizontal = Dimens.paddingSmall))
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimens.iconSizeSmall),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Next update in ${formatTime(secondsUntilUpdate)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun AlertsList(
    alerts: List<WeatherAlert>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(Dimens.paddingMedium)
    ) {
        // Show critical alerts first
        val criticalAlerts = alerts.filter { it.isCritical() }
        val normalAlerts = alerts.filterNot { it.isCritical() }
        
        if (criticalAlerts.isNotEmpty()) {
            item {
                Text(
                    text = "CRITICAL ALERTS",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = Dimens.paddingSmall)
                )
            }
            items(criticalAlerts) { alert ->
                WeatherAlertCard(
                    alert = alert,
                    isCritical = true
                )
            }
        }
        
        if (normalAlerts.isNotEmpty()) {
            item {
                Text(
                    text = "Active Alerts",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = Dimens.paddingSmall)
                )
            }
            items(normalAlerts) { alert ->
                WeatherAlertCard(
                    alert = alert,
                    isCritical = false
                )
            }
        }
    }
}

@Composable
private fun WeatherAlertCard(
    alert: WeatherAlert,
    isCritical: Boolean
) {
    var isExpanded by remember { mutableStateOf(isCritical) }
    val alpha by animateFloatAsState(
        targetValue = if (isCritical) 1f else 0.95f,
        animationSpec = tween(durationMillis = 300)
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha),
        colors = CardDefaults.cardColors(
            containerColor = if (isCritical) {
                when (alert.severity) {
                    WeatherSeverity.EXTREME -> MaterialTheme.colorScheme.errorContainer
                    WeatherSeverity.HIGH -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                    else -> MaterialTheme.colorScheme.surface
                }
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCritical) Dimens.elevationNormal else Dimens.elevationSmall
        ),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Alert icon
                Surface(
                    modifier = Modifier.size(Dimens.iconSizeLarge),
                    shape = CircleShape,
                    color = getAlertIconBackground(alert.severity)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getAlertIcon(alert.alertType),
                            contentDescription = alert.alertType.name,
                            modifier = Modifier.size(Dimens.iconSizeMedium),
                            tint = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.padding(horizontal = Dimens.paddingSmall))
                
                // Alert content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCritical) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(Dimens.paddingExtraSmall))
                    
                    Text(
                        text = alert.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCritical) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.padding(top = Dimens.paddingSmall)
                        ) {
                            // Safety instructions
                            if (alert.safetyInstructions.isNotEmpty()) {
                                Text(
                                    text = "Safety Instructions:",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCritical) {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.padding(vertical = Dimens.paddingExtraSmall)
                                )
                                alert.safetyInstructions.forEach { instruction ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "• ",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isCritical) {
                                                MaterialTheme.colorScheme.onErrorContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                        Text(
                                            text = instruction,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isCritical) {
                                                MaterialTheme.colorScheme.onErrorContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                }
                            }
                            
                            // Recommended actions
                            if (alert.recommendedActions.isNotEmpty()) {
                                Text(
                                    text = "Recommended Actions:",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCritical) {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.padding(vertical = Dimens.paddingExtraSmall)
                                )
                                alert.recommendedActions.forEach { action ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "• ",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isCritical) {
                                                MaterialTheme.colorScheme.onErrorContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                        Text(
                                            text = action,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isCritical) {
                                                MaterialTheme.colorScheme.onErrorContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.paddingMedium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(Dimens.paddingMedium))
        Text(
            text = "Fetching weather alerts...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ErrorContent(error: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.paddingMedium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudQueue,
            contentDescription = "Error",
            modifier = Modifier.size(Dimens.iconSizeExtraLarge),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(Dimens.paddingMedium))
        Text(
            text = "Unable to fetch weather alerts",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Dimens.paddingSmall)
        )
    }
}

@Composable
private fun NoAlertsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.paddingMedium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudQueue,
            contentDescription = "No alerts",
            modifier = Modifier.size(Dimens.iconSizeExtraLarge),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(Dimens.paddingMedium))
        Text(
            text = "No active weather alerts",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Enjoy the beautiful Tankwa weather!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Dimens.paddingSmall)
        )
    }
}

private fun getAlertIcon(alertType: WeatherAlertType): ImageVector {
    return when (alertType) {
        WeatherAlertType.DUST_STORM -> Icons.Default.Air
        WeatherAlertType.HIGH_WIND -> Icons.Default.Air
        WeatherAlertType.EXTREME_HEAT -> Icons.Default.Thermostat
        else -> Icons.Default.Warning
    }
}

private fun getAlertIconBackground(severity: WeatherSeverity): Color {
    return when (severity) {
        WeatherSeverity.EXTREME -> Color(0xFF8B0000)
        WeatherSeverity.HIGH -> Color(0xFFFF0000)
        WeatherSeverity.MEDIUM -> Color(0xFFFF6347)
        WeatherSeverity.LOW -> Color(0xFFFFA500)
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return if (minutes > 0) {
        "${minutes}m ${remainingSeconds}s"
    } else {
        "${remainingSeconds}s"
    }
}

@Preview
@Composable
private fun WeatherAlertsScreenPreview() {
    AppTheme {
        // Create a mock screen with sample data
        WeatherAlertsScreenPreviewContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeatherAlertsScreenPreviewContent() {
    val sampleAlerts = listOf(
        WeatherAlert(
            id = "1",
            alertType = WeatherAlertType.DUST_STORM,
            severity = WeatherSeverity.EXTREME,
            title = "Severe Dust Storm Warning",
            description = "Dangerous dust storm approaching from the west. Visibility near zero.",
            startTime = Clock.System.now().toEpochMilliseconds(),
            endTime = Clock.System.now().toEpochMilliseconds() + 2.hours,
            isActive = true,
            safetyInstructions = listOf(
                "Seek immediate shelter",
                "Secure all loose items",
                "Wear protective goggles and dust masks",
                "Avoid driving or cycling"
            ),
            temperature = 38.0,
            windSpeed = 65.0,
            visibility = 0.1,
            recommendedActions = listOf(
                "Close all tent flaps and windows",
                "Move bikes and equipment inside",
                "Check on neighbors"
            ),
            lastUpdated = Clock.System.now().toEpochMilliseconds(),
            createdAt = Clock.System.now().toEpochMilliseconds()
        ),
        WeatherAlert(
            id = "2",
            alertType = WeatherAlertType.EXTREME_HEAT,
            severity = WeatherSeverity.HIGH,
            title = "Extreme Heat Advisory",
            description = "Temperatures expected to reach 42°C. Stay hydrated!",
            startTime = Clock.System.now().toEpochMilliseconds(),
            endTime = Clock.System.now().toEpochMilliseconds() + 6.hours,
            isActive = true,
            safetyInstructions = listOf(
                "Drink water every 30 minutes",
                "Avoid strenuous activities during peak hours",
                "Wear light, loose clothing"
            ),
            temperature = 42.0,
            windSpeed = 15.0,
            visibility = 10.0,
            recommendedActions = listOf(
                "Take regular shade breaks",
                "Use sunscreen SPF 50+"
            ),
            lastUpdated = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis()
        )
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Weather Alerts",
                        style = MaterialTheme.typography.headlineSmall
                    ) 
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh alerts"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            CurrentConditionsCard(
                temperature = 38.0,
                windSpeed = 45.0,
                visibility = 2.0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.paddingMedium)
            )
            
            UpdateTimerCard(
                secondsUntilUpdate = 247,
                isUpdating = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.paddingMedium)
            )
            
            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
            
            AlertsList(
                alerts = sampleAlerts,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Extension property for Duration
private val Int.hours get() = (this * 60 * 60 * 1000).toLong()
private val Int.minutes get() = (this * 60 * 1000).toLong()