package com.romreviewertools.noteitup.data.cloud

import com.romreviewertools.noteitup.data.encryption.EncryptedBundleService
import com.romreviewertools.noteitup.domain.model.CloudSyncSettings
import com.romreviewertools.noteitup.domain.repository.CloudSyncRepository
import com.romreviewertools.noteitup.domain.usecase.ImportEntriesUseCase
import com.romreviewertools.noteitup.domain.usecase.ImportResult as DomainImportResult
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

/**
 * Manages cloud sync operations across all connected providers.
 * Handles backup creation, restore operations, and auto-sync scheduling.
 */
class CloudSyncManager(
    private val googleDriveProvider: CloudProvider,
    private val dropboxProvider: CloudProvider,
    private val cloudSyncRepository: CloudSyncRepository,
    private val encryptedBundleService: EncryptedBundleService,
    private val importEntriesUseCase: ImportEntriesUseCase
) {
    private val _syncStatus = MutableStateFlow(SyncStatus())
    val syncStatus: Flow<SyncStatus> = _syncStatus.asStateFlow()

    private val providers = mapOf(
        CloudProviderType.GOOGLE_DRIVE to googleDriveProvider,
        CloudProviderType.DROPBOX to dropboxProvider
    )

    /**
     * Get the provider for a specific type.
     */
    fun getProvider(type: CloudProviderType): CloudProvider {
        return providers[type] ?: throw IllegalArgumentException("Unknown provider: $type")
    }

    /**
     * Get sync settings as a flow.
     */
    fun getSyncSettings(): Flow<CloudSyncSettings> {
        return cloudSyncRepository.getSyncSettings()
    }

    /**
     * Get combined authentication status for all providers.
     */
    fun getAuthenticationStatus(): Flow<Map<CloudProviderType, Boolean>> {
        return combine(
            googleDriveProvider.isAuthenticated(),
            dropboxProvider.isAuthenticated()
        ) { googleAuth, dropboxAuth ->
            mapOf(
                CloudProviderType.GOOGLE_DRIVE to googleAuth,
                CloudProviderType.DROPBOX to dropboxAuth
            )
        }
    }

    /**
     * Initiate OAuth flow for a provider.
     */
    suspend fun startOAuthFlow(provider: CloudProviderType): String {
        return getProvider(provider).getAuthUrl()
    }

    /**
     * Handle OAuth callback after user authorization.
     */
    suspend fun handleOAuthCallback(provider: CloudProviderType, code: String): CloudResult<Unit> {
        return getProvider(provider).handleAuthCallback(code)
    }

    /**
     * Disconnect a provider.
     */
    suspend fun disconnectProvider(provider: CloudProviderType) {
        getProvider(provider).disconnect()
    }

    /**
     * Create a backup and upload to the specified provider.
     */
    suspend fun createBackup(
        provider: CloudProviderType,
        password: String
    ): CloudResult<BackupInfo> {
        _syncStatus.value = SyncStatus(
            state = SyncState.SYNCING,
            currentOperation = "Creating backup..."
        )

        return try {
            // Create encrypted bundle
            _syncStatus.value = _syncStatus.value.copy(
                progress = 0.2f,
                currentOperation = "Encrypting data..."
            )
            val bundleData = encryptedBundleService.createBundle(password)

            // Generate filename with timestamp
            val timestamp = Clock.System.now().toEpochMilliseconds()
            val fileName = "backup_$timestamp.noteitup"

            // Upload to provider
            _syncStatus.value = _syncStatus.value.copy(
                progress = 0.5f,
                currentOperation = "Uploading to ${provider.name}..."
            )
            val uploadResult = getProvider(provider).uploadBackup(fileName, bundleData)

            when (uploadResult) {
                is CloudResult.Success -> {
                    // Update last sync time
                    cloudSyncRepository.updateLastSyncTime(timestamp)

                    _syncStatus.value = SyncStatus(
                        state = SyncState.SUCCESS,
                        lastSyncTime = timestamp,
                        progress = 1f
                    )

                    CloudResult.Success(
                        BackupInfo(
                            provider = provider,
                            fileId = uploadResult.data.id,
                            fileName = fileName,
                            createdAt = timestamp,
                            size = bundleData.size.toLong()
                        )
                    )
                }
                is CloudResult.Error -> {
                    _syncStatus.value = SyncStatus(
                        state = SyncState.ERROR,
                        error = uploadResult.message
                    )
                    CloudResult.Error(uploadResult.message, uploadResult.code)
                }
                is CloudResult.NotAuthenticated -> {
                    _syncStatus.value = SyncStatus(
                        state = SyncState.ERROR,
                        error = "Not authenticated"
                    )
                    CloudResult.NotAuthenticated
                }
            }
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus(
                state = SyncState.ERROR,
                error = e.message ?: "Backup failed"
            )
            CloudResult.Error(e.message ?: "Backup failed")
        }
    }

    /**
     * List available backups from a provider.
     */
    suspend fun listBackups(provider: CloudProviderType): CloudResult<List<BackupInfo>> {
        return when (val result = getProvider(provider).listBackups()) {
            is CloudResult.Success -> {
                val backups = result.data.map { file ->
                    BackupInfo(
                        provider = provider,
                        fileId = file.id,
                        fileName = file.name,
                        createdAt = file.modifiedAt,
                        size = file.size
                    )
                }
                CloudResult.Success(backups)
            }
            is CloudResult.Error -> CloudResult.Error(result.message, result.code)
            is CloudResult.NotAuthenticated -> CloudResult.NotAuthenticated
        }
    }

    /**
     * Restore from a backup.
     */
    suspend fun restoreBackup(
        provider: CloudProviderType,
        fileId: String,
        password: String
    ): CloudResult<DomainImportResult> {
        _syncStatus.value = SyncStatus(
            state = SyncState.SYNCING,
            currentOperation = "Downloading backup..."
        )

        return try {
            // Download backup
            _syncStatus.value = _syncStatus.value.copy(progress = 0.3f)
            val downloadResult = getProvider(provider).downloadBackup(fileId)

            when (downloadResult) {
                is CloudResult.Success -> {
                    // Decrypt bundle
                    _syncStatus.value = _syncStatus.value.copy(
                        progress = 0.5f,
                        currentOperation = "Decrypting..."
                    )
                    val jsonData = try {
                        encryptedBundleService.extractBundle(downloadResult.data, password)
                    } catch (e: Exception) {
                        _syncStatus.value = SyncStatus(
                            state = SyncState.ERROR,
                            error = "Wrong password or corrupted backup"
                        )
                        return CloudResult.Error("Decryption failed: ${e.message}")
                    }

                    // Import data
                    _syncStatus.value = _syncStatus.value.copy(
                        progress = 0.7f,
                        currentOperation = "Importing entries..."
                    )
                    val importResult = importEntriesUseCase(jsonData)

                    if (importResult.success) {
                        val timestamp = Clock.System.now().toEpochMilliseconds()
                        cloudSyncRepository.updateLastSyncTime(timestamp)

                        _syncStatus.value = SyncStatus(
                            state = SyncState.SUCCESS,
                            lastSyncTime = timestamp,
                            progress = 1f
                        )

                        CloudResult.Success(importResult)
                    } else {
                        _syncStatus.value = SyncStatus(
                            state = SyncState.ERROR,
                            error = importResult.error ?: "Import failed"
                        )
                        CloudResult.Error(importResult.error ?: "Import failed")
                    }
                }
                is CloudResult.Error -> {
                    _syncStatus.value = SyncStatus(
                        state = SyncState.ERROR,
                        error = downloadResult.message
                    )
                    CloudResult.Error(downloadResult.message, downloadResult.code)
                }
                is CloudResult.NotAuthenticated -> {
                    _syncStatus.value = SyncStatus(
                        state = SyncState.ERROR,
                        error = "Not authenticated"
                    )
                    CloudResult.NotAuthenticated
                }
            }
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus(
                state = SyncState.ERROR,
                error = e.message ?: "Restore failed"
            )
            CloudResult.Error(e.message ?: "Restore failed")
        }
    }

    /**
     * Delete a backup from a provider.
     */
    suspend fun deleteBackup(provider: CloudProviderType, fileId: String): CloudResult<Unit> {
        return getProvider(provider).deleteBackup(fileId)
    }

    /**
     * Check if auto-sync should run and perform it if needed.
     * Call this periodically from the platform-specific background work.
     */
    suspend fun checkAndPerformAutoSync(password: String): CloudResult<Unit> {
        val settings = cloudSyncRepository.getSyncSettings().first()

        if (!settings.autoSyncEnabled) {
            return CloudResult.Success(Unit)
        }

        val lastSync = cloudSyncRepository.getLastSyncTime()
        val lastModification = cloudSyncRepository.getLastLocalModificationTime()
        val now = Clock.System.now().toEpochMilliseconds()

        // Check if interval has passed
        val intervalMs = settings.autoSyncInterval.minutes * 60 * 1000L
        if (now - lastSync < intervalMs) {
            return CloudResult.Success(Unit)
        }

        // Check if there are new changes since last sync
        if (lastModification <= lastSync) {
            // No new changes, skip sync
            return CloudResult.Success(Unit)
        }

        // Perform sync to connected providers
        val connectedProviders = settings.connectedProviders
        if (connectedProviders.isEmpty()) {
            return CloudResult.Success(Unit)
        }

        // Sync to first connected provider
        val provider = connectedProviders.first()
        return when (val result = createBackup(provider, password)) {
            is CloudResult.Success -> CloudResult.Success(Unit)
            is CloudResult.Error -> CloudResult.Error(result.message, result.code)
            is CloudResult.NotAuthenticated -> CloudResult.NotAuthenticated
        }
    }

    /**
     * Get quota info for a provider.
     */
    suspend fun getQuotaInfo(provider: CloudProviderType): CloudResult<QuotaInfo> {
        return getProvider(provider).getQuotaInfo()
    }

    /**
     * Get backup metadata without downloading the full file.
     */
    suspend fun getBackupMetadata(provider: CloudProviderType, fileId: String): CloudResult<BackupMetadata> {
        return when (val result = getProvider(provider).downloadBackup(fileId)) {
            is CloudResult.Success -> {
                try {
                    val bundleMetadata = encryptedBundleService.getMetadata(result.data)
                    CloudResult.Success(
                        BackupMetadata(
                            version = bundleMetadata.version,
                            createdAt = bundleMetadata.createdAt,
                            entryCount = bundleMetadata.entryCount,
                            folderCount = bundleMetadata.folderCount,
                            tagCount = bundleMetadata.tagCount
                        )
                    )
                } catch (e: Exception) {
                    CloudResult.Error("Failed to read backup metadata: ${e.message}")
                }
            }
            is CloudResult.Error -> CloudResult.Error(result.message, result.code)
            is CloudResult.NotAuthenticated -> CloudResult.NotAuthenticated
        }
    }

    /**
     * Reset sync status to idle.
     */
    fun resetSyncStatus() {
        _syncStatus.value = SyncStatus()
    }
}

/**
 * Basic backup metadata for display purposes.
 */
data class BackupMetadata(
    val version: Int,
    val createdAt: Long,
    val entryCount: Int,
    val folderCount: Int,
    val tagCount: Int
)
