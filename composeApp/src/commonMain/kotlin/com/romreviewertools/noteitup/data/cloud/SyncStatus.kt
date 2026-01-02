package com.romreviewertools.noteitup.data.cloud

/**
 * Enum representing the current state of a sync operation.
 */
enum class SyncState {
    /** No sync operation in progress */
    IDLE,
    /** Sync operation is in progress */
    SYNCING,
    /** Last sync completed successfully */
    SUCCESS,
    /** Last sync failed with an error */
    ERROR,
    /** Conflict detected between local and remote data */
    CONFLICT
}

/**
 * Represents the current sync status.
 */
data class SyncStatus(
    val state: SyncState = SyncState.IDLE,
    val lastSyncTime: Long? = null,
    val error: String? = null,
    val progress: Float = 0f,
    val currentOperation: String? = null
) {
    val isSyncing: Boolean get() = state == SyncState.SYNCING
    val isSuccess: Boolean get() = state == SyncState.SUCCESS
    val isError: Boolean get() = state == SyncState.ERROR
}

/**
 * Represents information about a backup file.
 */
data class BackupInfo(
    val provider: CloudProviderType,
    val fileId: String,
    val fileName: String,
    val createdAt: Long,
    val size: Long
) {
    val formattedSize: String get() {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }
    }
}

/**
 * Result of a conflict check between local and remote data.
 */
sealed class ConflictResult {
    /** No conflict - data is in sync */
    data object NoConflict : ConflictResult()

    /** Local data is newer than remote */
    data object LocalNewer : ConflictResult()

    /** Remote data is newer than local */
    data class RemoteNewer(val file: CloudFile) : ConflictResult()

    /** Both local and remote have changes since last sync */
    data class Conflict(
        val localModifiedAt: Long,
        val remoteModifiedAt: Long,
        val remoteFile: CloudFile
    ) : ConflictResult()
}

/**
 * User's choice for resolving a conflict.
 */
enum class ConflictResolution {
    /** Use local data and overwrite remote */
    USE_LOCAL,
    /** Use remote data and overwrite local */
    USE_REMOTE,
    /** Keep both versions (creates a new backup) */
    KEEP_BOTH,
    /** Cancel the sync operation */
    CANCEL
}

