package com.romreviewertools.noteitup.presentation.screens.cloudsync

import com.romreviewertools.noteitup.data.cloud.BackupInfo
import com.romreviewertools.noteitup.data.cloud.CloudProviderType
import com.romreviewertools.noteitup.data.cloud.QuotaInfo
import com.romreviewertools.noteitup.data.cloud.SyncState
import com.romreviewertools.noteitup.domain.model.AutoSyncInterval

/**
 * UI State for CloudSync screen.
 */
data class CloudSyncUiState(
    // Provider connection status
    val googleDriveConnected: Boolean = false,
    val dropboxConnected: Boolean = false,

    // Authentication in progress
    val isAuthenticating: Boolean = false,
    val authenticatingProvider: CloudProviderType? = null,

    // Sync settings
    val autoSyncEnabled: Boolean = false,
    val autoSyncInterval: AutoSyncInterval = AutoSyncInterval.HOURLY,
    val syncOnWifiOnly: Boolean = true,
    val lastSyncTime: Long? = null,

    // Current operation status
    val syncState: SyncState = SyncState.IDLE,
    val syncProgress: Float = 0f,
    val currentOperation: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    // Backup list
    val backups: List<BackupInfo> = emptyList(),
    val isLoadingBackups: Boolean = false,
    val selectedProvider: CloudProviderType? = null,

    // Quota info
    val googleDriveQuota: QuotaInfo? = null,
    val dropboxQuota: QuotaInfo? = null,

    // Dialogs
    val showPasswordDialog: Boolean = false,
    val passwordDialogAction: PasswordDialogAction = PasswordDialogAction.BACKUP,
    val selectedBackupForRestore: BackupInfo? = null,
    val showBackupListDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val backupToDelete: BackupInfo? = null,
    val showDisconnectConfirmDialog: Boolean = false,
    val providerToDisconnect: CloudProviderType? = null
) {
    val isAnyProviderConnected: Boolean
        get() = googleDriveConnected || dropboxConnected

    val isSyncing: Boolean
        get() = syncState == SyncState.SYNCING
}

/**
 * Action to perform after password is entered.
 */
enum class PasswordDialogAction {
    BACKUP,
    RESTORE
}
