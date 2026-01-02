package com.romreviewertools.noteitup.presentation.screens.cloudsync

import com.romreviewertools.noteitup.data.cloud.BackupInfo
import com.romreviewertools.noteitup.data.cloud.CloudProviderType
import com.romreviewertools.noteitup.domain.model.AutoSyncInterval

/**
 * User intents/actions for CloudSync screen.
 */
sealed class CloudSyncIntent {
    // Provider connection
    data class ConnectProvider(val provider: CloudProviderType) : CloudSyncIntent()
    data class HandleOAuthCallback(val provider: CloudProviderType, val code: String) : CloudSyncIntent()
    data class DisconnectProvider(val provider: CloudProviderType) : CloudSyncIntent()
    data class ConfirmDisconnect(val provider: CloudProviderType) : CloudSyncIntent()
    data object CancelDisconnect : CloudSyncIntent()

    // Backup operations
    data class StartBackup(val provider: CloudProviderType) : CloudSyncIntent()
    data class ConfirmBackupWithPassword(val password: String) : CloudSyncIntent()
    data class ShowBackupList(val provider: CloudProviderType) : CloudSyncIntent()
    data object HideBackupList : CloudSyncIntent()
    data class SelectBackupForRestore(val backup: BackupInfo) : CloudSyncIntent()
    data class ConfirmRestoreWithPassword(val password: String) : CloudSyncIntent()
    data class DeleteBackup(val backup: BackupInfo) : CloudSyncIntent()
    data class ConfirmDeleteBackup(val backup: BackupInfo) : CloudSyncIntent()
    data object CancelDeleteBackup : CloudSyncIntent()

    // Sync settings
    data class SetAutoSyncEnabled(val enabled: Boolean) : CloudSyncIntent()
    data class SetAutoSyncInterval(val interval: AutoSyncInterval) : CloudSyncIntent()
    data class SetSyncOnWifiOnly(val enabled: Boolean) : CloudSyncIntent()

    // Dialog management
    data object ShowPasswordDialog : CloudSyncIntent()
    data object HidePasswordDialog : CloudSyncIntent()
    data object DismissError : CloudSyncIntent()
    data object DismissSuccess : CloudSyncIntent()

    // Refresh
    data object RefreshBackups : CloudSyncIntent()
    data object RefreshQuota : CloudSyncIntent()
}
