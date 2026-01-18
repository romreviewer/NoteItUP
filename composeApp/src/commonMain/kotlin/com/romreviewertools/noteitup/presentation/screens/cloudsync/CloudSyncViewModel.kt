package com.romreviewertools.noteitup.presentation.screens.cloudsync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.romreviewertools.noteitup.data.analytics.AnalyticsEvent
import com.romreviewertools.noteitup.data.analytics.AnalyticsService
import com.romreviewertools.noteitup.data.cloud.CloudProviderType
import com.romreviewertools.noteitup.data.cloud.CloudResult
import com.romreviewertools.noteitup.data.cloud.CloudSyncManager
import com.romreviewertools.noteitup.data.cloud.OAuthHandler
import com.romreviewertools.noteitup.data.cloud.SyncState
import com.romreviewertools.noteitup.domain.repository.CloudSyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CloudSyncViewModel(
    private val cloudSyncManager: CloudSyncManager,
    private val cloudSyncRepository: CloudSyncRepository,
    private val oAuthHandler: OAuthHandler,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CloudSyncUiState())
    val uiState: StateFlow<CloudSyncUiState> = _uiState.asStateFlow()

    init {
        analyticsService.logEvent(AnalyticsEvent.ScreenViewCloudSync)
        observeAuthStatus()
        observeSyncSettings()
        observeSyncStatus()
    }

    private fun observeAuthStatus() {
        viewModelScope.launch {
            cloudSyncManager.getAuthenticationStatus().collect { authStatus ->
                _uiState.update { state ->
                    state.copy(
                        googleDriveConnected = authStatus[CloudProviderType.GOOGLE_DRIVE] ?: false,
                        dropboxConnected = authStatus[CloudProviderType.DROPBOX] ?: false
                    )
                }
            }
        }
    }

    private fun observeSyncSettings() {
        viewModelScope.launch {
            cloudSyncManager.getSyncSettings().collect { settings ->
                _uiState.update { state ->
                    state.copy(
                        autoSyncEnabled = settings.autoSyncEnabled,
                        autoSyncInterval = settings.autoSyncInterval,
                        syncOnWifiOnly = settings.syncOnWifiOnly,
                        lastSyncTime = settings.lastSyncTime
                    )
                }
            }
        }
    }

    private fun observeSyncStatus() {
        viewModelScope.launch {
            cloudSyncManager.syncStatus.collect { status ->
                _uiState.update { state ->
                    state.copy(
                        syncState = status.state,
                        syncProgress = status.progress,
                        currentOperation = status.currentOperation,
                        errorMessage = if (status.state == SyncState.ERROR) status.error else state.errorMessage,
                        successMessage = if (status.state == SyncState.SUCCESS) "Operation completed successfully" else null,
                        lastSyncTime = status.lastSyncTime ?: state.lastSyncTime
                    )
                }
            }
        }
    }

    fun handleIntent(intent: CloudSyncIntent) {
        when (intent) {
            is CloudSyncIntent.ConnectProvider -> connectProvider(intent.provider)
            is CloudSyncIntent.HandleOAuthCallback -> handleOAuthCallback(intent.provider, intent.code)
            is CloudSyncIntent.DisconnectProvider -> showDisconnectConfirm(intent.provider)
            is CloudSyncIntent.ConfirmDisconnect -> disconnectProvider(intent.provider)
            is CloudSyncIntent.CancelDisconnect -> hideDisconnectConfirm()

            is CloudSyncIntent.StartBackup -> startBackup(intent.provider)
            is CloudSyncIntent.ConfirmBackupWithPassword -> performBackup(intent.password)
            is CloudSyncIntent.ShowBackupList -> showBackupList(intent.provider)
            is CloudSyncIntent.HideBackupList -> hideBackupList()
            is CloudSyncIntent.SelectBackupForRestore -> selectBackupForRestore(intent.backup)
            is CloudSyncIntent.ConfirmRestoreWithPassword -> performRestore(intent.password)
            is CloudSyncIntent.DeleteBackup -> showDeleteConfirm(intent.backup)
            is CloudSyncIntent.ConfirmDeleteBackup -> deleteBackup(intent.backup)
            is CloudSyncIntent.CancelDeleteBackup -> hideDeleteConfirm()

            is CloudSyncIntent.SetAutoSyncEnabled -> setAutoSyncEnabled(intent.enabled)
            is CloudSyncIntent.SetAutoSyncInterval -> setAutoSyncInterval(intent.interval)
            is CloudSyncIntent.SetSyncOnWifiOnly -> setSyncOnWifiOnly(intent.enabled)

            is CloudSyncIntent.ShowPasswordDialog -> showPasswordDialog()
            is CloudSyncIntent.HidePasswordDialog -> hidePasswordDialog()
            is CloudSyncIntent.DismissError -> dismissError()
            is CloudSyncIntent.DismissSuccess -> dismissSuccess()

            is CloudSyncIntent.RefreshBackups -> refreshBackups()
            is CloudSyncIntent.RefreshQuota -> refreshQuota()
        }
    }

    private fun connectProvider(provider: CloudProviderType) {
        viewModelScope.launch {
            try {
                val authUrl = cloudSyncManager.startOAuthFlow(provider)
                oAuthHandler.openAuthUrl(authUrl)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to start authentication: ${e.message}") }
            }
        }
    }

    private fun handleOAuthCallback(provider: CloudProviderType, code: String) {
        viewModelScope.launch {
            // Show loading state
            _uiState.update { it.copy(
                isAuthenticating = true,
                authenticatingProvider = provider
            ) }

            try {
                when (val result = cloudSyncManager.handleOAuthCallback(provider, code)) {
                    is CloudResult.Success -> {
                        _uiState.update { it.copy(
                            isAuthenticating = false,
                            authenticatingProvider = null,
                            successMessage = "Connected to ${provider.name.replace("_", " ")}"
                        ) }
                        refreshQuota()
                    }
                    is CloudResult.Error -> {
                        _uiState.update { it.copy(
                            isAuthenticating = false,
                            authenticatingProvider = null,
                            errorMessage = result.message
                        ) }
                    }
                    is CloudResult.NotAuthenticated -> {
                        _uiState.update { it.copy(
                            isAuthenticating = false,
                            authenticatingProvider = null,
                            errorMessage = "Authentication failed"
                        ) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isAuthenticating = false,
                    authenticatingProvider = null,
                    errorMessage = "Connection failed: ${e.message}"
                ) }
            }
        }
    }

    private fun showDisconnectConfirm(provider: CloudProviderType) {
        _uiState.update { it.copy(
            showDisconnectConfirmDialog = true,
            providerToDisconnect = provider
        ) }
    }

    private fun hideDisconnectConfirm() {
        _uiState.update { it.copy(
            showDisconnectConfirmDialog = false,
            providerToDisconnect = null
        ) }
    }

    private fun disconnectProvider(provider: CloudProviderType) {
        viewModelScope.launch {
            cloudSyncManager.disconnectProvider(provider)
            _uiState.update { it.copy(
                showDisconnectConfirmDialog = false,
                providerToDisconnect = null,
                successMessage = "Disconnected from ${provider.name.replace("_", " ")}"
            ) }
        }
    }

    private fun startBackup(provider: CloudProviderType) {
        _uiState.update { it.copy(
            showPasswordDialog = true,
            passwordDialogAction = PasswordDialogAction.BACKUP,
            selectedProvider = provider
        ) }
    }

    private fun performBackup(password: String) {
        val provider = _uiState.value.selectedProvider ?: return
        hidePasswordDialog()

        viewModelScope.launch {
            when (val result = cloudSyncManager.createBackup(provider, password)) {
                is CloudResult.Success -> {
                    refreshBackups()
                }
                is CloudResult.Error -> {
                    // Error already handled by syncStatus observer
                }
                is CloudResult.NotAuthenticated -> {
                    _uiState.update { it.copy(errorMessage = "Not authenticated. Please reconnect.") }
                }
            }
        }
    }

    private fun showBackupList(provider: CloudProviderType) {
        _uiState.update { it.copy(
            showBackupListDialog = true,
            selectedProvider = provider,
            isLoadingBackups = true
        ) }
        loadBackups(provider)
    }

    private fun hideBackupList() {
        _uiState.update { it.copy(
            showBackupListDialog = false,
            backups = emptyList()
        ) }
    }

    private fun loadBackups(provider: CloudProviderType) {
        viewModelScope.launch {
            when (val result = cloudSyncManager.listBackups(provider)) {
                is CloudResult.Success -> {
                    _uiState.update { it.copy(
                        backups = result.data,
                        isLoadingBackups = false
                    ) }
                }
                is CloudResult.Error -> {
                    _uiState.update { it.copy(
                        isLoadingBackups = false,
                        errorMessage = result.message
                    ) }
                }
                is CloudResult.NotAuthenticated -> {
                    _uiState.update { it.copy(
                        isLoadingBackups = false,
                        errorMessage = "Not authenticated"
                    ) }
                }
            }
        }
    }

    private fun selectBackupForRestore(backup: com.romreviewertools.noteitup.data.cloud.BackupInfo) {
        _uiState.update { it.copy(
            showPasswordDialog = true,
            passwordDialogAction = PasswordDialogAction.RESTORE,
            selectedBackupForRestore = backup,
            showBackupListDialog = false
        ) }
    }

    private fun performRestore(password: String) {
        val backup = _uiState.value.selectedBackupForRestore ?: return
        hidePasswordDialog()

        viewModelScope.launch {
            when (val result = cloudSyncManager.restoreBackup(backup.provider, backup.fileId, password)) {
                is CloudResult.Success -> {
                    _uiState.update { it.copy(
                        successMessage = "Restored ${result.data.entriesImported} entries, " +
                                "${result.data.foldersImported} folders, " +
                                "${result.data.tagsImported} tags"
                    ) }
                }
                is CloudResult.Error -> {
                    // Error already handled by syncStatus observer
                }
                is CloudResult.NotAuthenticated -> {
                    _uiState.update { it.copy(errorMessage = "Not authenticated") }
                }
            }
        }
    }

    private fun showDeleteConfirm(backup: com.romreviewertools.noteitup.data.cloud.BackupInfo) {
        _uiState.update { it.copy(
            showDeleteConfirmDialog = true,
            backupToDelete = backup
        ) }
    }

    private fun hideDeleteConfirm() {
        _uiState.update { it.copy(
            showDeleteConfirmDialog = false,
            backupToDelete = null
        ) }
    }

    private fun deleteBackup(backup: com.romreviewertools.noteitup.data.cloud.BackupInfo) {
        viewModelScope.launch {
            when (val result = cloudSyncManager.deleteBackup(backup.provider, backup.fileId)) {
                is CloudResult.Success -> {
                    _uiState.update { it.copy(
                        showDeleteConfirmDialog = false,
                        backupToDelete = null,
                        successMessage = "Backup deleted"
                    ) }
                    refreshBackups()
                }
                is CloudResult.Error -> {
                    _uiState.update { it.copy(
                        showDeleteConfirmDialog = false,
                        backupToDelete = null,
                        errorMessage = result.message
                    ) }
                }
                is CloudResult.NotAuthenticated -> {
                    _uiState.update { it.copy(
                        showDeleteConfirmDialog = false,
                        errorMessage = "Not authenticated"
                    ) }
                }
            }
        }
    }

    private fun setAutoSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            cloudSyncRepository.setAutoSyncEnabled(enabled)
        }
    }

    private fun setAutoSyncInterval(interval: com.romreviewertools.noteitup.domain.model.AutoSyncInterval) {
        viewModelScope.launch {
            cloudSyncRepository.setAutoSyncInterval(interval)
        }
    }

    private fun setSyncOnWifiOnly(enabled: Boolean) {
        viewModelScope.launch {
            cloudSyncRepository.setSyncOnWifiOnly(enabled)
        }
    }

    private fun showPasswordDialog() {
        _uiState.update { it.copy(showPasswordDialog = true) }
    }

    private fun hidePasswordDialog() {
        _uiState.update { it.copy(
            showPasswordDialog = false,
            selectedBackupForRestore = null
        ) }
    }

    private fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
        cloudSyncManager.resetSyncStatus()
    }

    private fun dismissSuccess() {
        _uiState.update { it.copy(successMessage = null) }
        cloudSyncManager.resetSyncStatus()
    }

    private fun refreshBackups() {
        val provider = _uiState.value.selectedProvider ?: return
        loadBackups(provider)
    }

    private fun refreshQuota() {
        viewModelScope.launch {
            if (_uiState.value.googleDriveConnected) {
                when (val result = cloudSyncManager.getQuotaInfo(CloudProviderType.GOOGLE_DRIVE)) {
                    is CloudResult.Success -> {
                        _uiState.update { it.copy(googleDriveQuota = result.data) }
                    }
                    else -> { /* ignore */ }
                }
            }
            if (_uiState.value.dropboxConnected) {
                when (val result = cloudSyncManager.getQuotaInfo(CloudProviderType.DROPBOX)) {
                    is CloudResult.Success -> {
                        _uiState.update { it.copy(dropboxQuota = result.data) }
                    }
                    else -> { /* ignore */ }
                }
            }
        }
    }
}
