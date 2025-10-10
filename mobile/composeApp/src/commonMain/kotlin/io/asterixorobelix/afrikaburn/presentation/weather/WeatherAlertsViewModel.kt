package io.asterixorobelix.afrikaburn.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.asterixorobelix.afrikaburn.domain.model.WeatherAlert
import io.asterixorobelix.afrikaburn.domain.usecase.GetWeatherAlertsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

data class WeatherAlertsUiState(
    val isLoading: Boolean = false,
    val alerts: List<WeatherAlert> = emptyList(),
    val currentTemperature: Double? = null,
    val currentWindSpeed: Double? = null,
    val currentVisibility: Double? = null,
    val secondsUntilNextUpdate: Int = 300, // 5 minutes default
    val error: String? = null
)

class WeatherAlertsViewModel(
    private val getWeatherAlertsUseCase: GetWeatherAlertsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WeatherAlertsUiState())
    val uiState: StateFlow<WeatherAlertsUiState> = _uiState.asStateFlow()
    
    private var updateTimerJob: Job? = null
    private val updateIntervalSeconds = 300 // 5 minutes

    init {
        loadWeatherAlerts()
        startUpdateTimer()
    }

    fun refreshAlerts() {
        loadWeatherAlerts()
        resetUpdateTimer()
    }

    private fun loadWeatherAlerts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                getWeatherAlertsUseCase()
                    .catch { error ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Failed to load weather alerts: ${error.message}"
                            ) 
                        }
                    }
                    .collect { alerts ->
                        // Extract current conditions from the most recent alert if available
                        val currentAlert = alerts.firstOrNull()
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                alerts = alerts
                                    .sortedByDescending { alert -> if (alert.isCritical()) 1 else 0 }
                                    .sortedByDescending { alert -> alert.severity.ordinal },
                                currentTemperature = currentAlert?.temperature,
                                currentWindSpeed = currentAlert?.windSpeed,
                                currentVisibility = currentAlert?.visibility,
                                error = null
                            ) 
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load weather alerts: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun startUpdateTimer() {
        updateTimerJob?.cancel()
        updateTimerJob = viewModelScope.launch {
            var secondsRemaining = updateIntervalSeconds
            
            while (secondsRemaining > 0) {
                _uiState.update { it.copy(secondsUntilNextUpdate = secondsRemaining) }
                delay(1000) // 1 second
                secondsRemaining--
            }
            
            // Auto-refresh when timer reaches zero
            loadWeatherAlerts()
            startUpdateTimer() // Restart timer
        }
    }

    private fun resetUpdateTimer() {
        updateTimerJob?.cancel()
        startUpdateTimer()
    }

    override fun onCleared() {
        super.onCleared()
        updateTimerJob?.cancel()
    }
}