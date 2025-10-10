package io.asterixorobelix.afrikaburn.presentation.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.asterixorobelix.afrikaburn.domain.model.ContentPackage
import io.asterixorobelix.afrikaburn.domain.model.SyncManager
import io.asterixorobelix.afrikaburn.domain.repository.SyncRepository
import io.asterixorobelix.afrikaburn.domain.repository.NetworkType
import io.asterixorobelix.afrikaburn.domain.usecase.SyncContentUseCase
import io.asterixorobelix.afrikaburn.domain.usecase.SyncProgress
import io.asterixorobelix.afrikaburn.domain.model.getCurrentTimestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SyncUiState(
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val currentProgress: Float = 0f,
    val currentPackageName: String? = null,
    val totalSizeBytes: Long = 0,
    val downloadedSizeBytes: Long = 0,
    val availablePackages: List<ContentPackage> = emptyList(),
    val downloadedPackages: List<ContentPackage> = emptyList(),
    val lastSyncTime: Long? = null,
    val error: String? = null,
    val storageUsed: Long = 0,
    val storageLimit: Long = 2_000_000_000, // 2GB
    val isWifiConnected: Boolean = false,
    val selectedPackages: Set<String> = emptySet()
)

enum class SyncStatus {
    IDLE, CHECKING, DOWNLOADING, COMPLETE, ERROR
}

class SyncViewModel(
    private val syncContentUseCase: SyncContentUseCase,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    init {
        loadSyncStatus()
        observeNetworkStatus()
    }

    private fun loadSyncStatus() {
        viewModelScope.launch {
            syncRepository.observeSyncManager()
                .catch { e ->
                    _uiState.update { 
                        it.copy(error = "Failed to load sync status: ${e.message}")
                    }
                }
                .collect { syncManager ->
                    syncManager?.let { manager ->
                        _uiState.update { 
                            it.copy(
                                lastSyncTime = manager.lastFullSync,
                                storageUsed = manager.totalStorageUsed,
                                storageLimit = manager.maxStorageLimit,
                                error = manager.errorMessage
                            )
                        }
                    }
                }
        }
        
        loadContentPackages()
    }

    private fun loadContentPackages() {
        viewModelScope.launch {
            try {
                val packages = syncRepository.getAllContentPackages()
                _uiState.update { 
                    it.copy(
                        availablePackages = packages,
                        downloadedPackages = packages.filter { it.isDownloaded() }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to load content packages: ${e.message}")
                }
            }
        }
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            try {
                val networkType = syncRepository.getNetworkType()
                _uiState.update { 
                    it.copy(isWifiConnected = networkType == NetworkType.WIFI) 
                }
            } catch (e: Exception) {
                // Ignore network monitoring errors
            }
        }
    }

    fun startSync() {
        if (_uiState.value.syncStatus != SyncStatus.IDLE) return
        
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    syncStatus = SyncStatus.CHECKING,
                    error = null,
                    currentProgress = 0f
                )
            }
            
            try {
                _uiState.update { it.copy(syncStatus = SyncStatus.DOWNLOADING) }
                
                syncContentUseCase.execute(forceSync = false)
                    .collect { progress ->
                        when (progress) {
                            is SyncProgress.Syncing -> {
                                _uiState.update { 
                                    it.copy(
                                        currentProgress = progress.progress / 100f,
                                        currentPackageName = progress.contentType.displayName,
                                        error = null
                                    )
                                }
                            }
                            is SyncProgress.Complete -> {
                                _uiState.update { 
                                    it.copy(
                                        syncStatus = SyncStatus.COMPLETE,
                                        currentProgress = 1f,
                                        lastSyncTime = getCurrentTimestamp(),
                                        totalSizeBytes = progress.totalSizeBytes,
                                        error = null
                                    )
                                }
                            }
                            is SyncProgress.Failed -> {
                                _uiState.update { 
                                    it.copy(
                                        syncStatus = SyncStatus.ERROR,
                                        error = progress.error
                                    )
                                }
                            }
                            is SyncProgress.Offline -> {
                                _uiState.update { 
                                    it.copy(
                                        syncStatus = SyncStatus.ERROR,
                                        error = progress.message
                                    )
                                }
                            }
                            else -> {
                                // Handle other progress types if needed
                            }
                        }
                    }
                
                // Reload packages to update downloaded status
                loadContentPackages()
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        syncStatus = SyncStatus.ERROR,
                        error = "Sync failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun cancelSync() {
        viewModelScope.launch {
            syncContentUseCase.cancelSync()
            _uiState.update { 
                it.copy(
                    syncStatus = SyncStatus.IDLE,
                    currentProgress = 0f,
                    currentPackageName = null
                )
            }
        }
    }

    fun togglePackageSelection(packageId: String) {
        _uiState.update { state ->
            val newSelection = if (state.selectedPackages.contains(packageId)) {
                state.selectedPackages - packageId
            } else {
                state.selectedPackages + packageId
            }
            state.copy(selectedPackages = newSelection)
        }
    }

    fun selectAllPackages() {
        _uiState.update { state ->
            state.copy(selectedPackages = state.availablePackages.map { it.id }.toSet())
        }
    }

    fun clearPackageSelection() {
        _uiState.update { it.copy(selectedPackages = emptySet()) }
    }

    fun deletePackage(packageId: String) {
        viewModelScope.launch {
            try {
                syncRepository.deleteContentPackage(packageId)
                // Reload to update UI
                loadContentPackages()
                loadSyncStatus()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to delete package: ${e.message}")
                }
            }
        }
    }

    fun getStorageUsagePercent(): Float {
        val state = _uiState.value
        return if (state.storageLimit > 0) {
            (state.storageUsed.toFloat() / state.storageLimit.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    fun getEstimatedSyncTime(): String {
        val state = _uiState.value
        val remainingBytes = state.totalSizeBytes - state.downloadedSizeBytes
        
        // Estimate based on network speed
        val bytesPerSecond = if (state.isWifiConnected) {
            1_000_000 // 1MB/s on WiFi
        } else {
            100_000 // 100KB/s on cellular
        }
        
        val secondsRemaining = (remainingBytes / bytesPerSecond).toInt()
        
        return when {
            secondsRemaining < 60 -> "Less than 1 minute"
            secondsRemaining < 3600 -> "${secondsRemaining / 60} minutes"
            else -> "${secondsRemaining / 3600} hours"
        }
    }

    fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> {
                val gb = bytes / (1024.0 * 1024.0 * 1024.0)
                "${(gb * 100).toInt() / 100.0} GB"
            }
        }
    }
}