package com.romreviewertools.noteitup.data.cloud

import kotlinx.coroutines.flow.Flow

/**
 * Enum representing supported cloud storage providers.
 */
enum class CloudProviderType {
    GOOGLE_DRIVE,
    DROPBOX
}

/**
 * Represents a file stored in cloud storage.
 */
data class CloudFile(
    val id: String,
    val name: String,
    val modifiedAt: Long,
    val size: Long
)

/**
 * Represents cloud storage quota information.
 */
data class QuotaInfo(
    val used: Long,
    val total: Long
) {
    val available: Long get() = total - used
    val usagePercent: Float get() = if (total > 0) used.toFloat() / total else 0f
}

/**
 * Result wrapper for cloud operations.
 */
sealed class CloudResult<out T> {
    data class Success<T>(val data: T) : CloudResult<T>()
    data class Error(val message: String, val code: Int? = null) : CloudResult<Nothing>()
    data object NotAuthenticated : CloudResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
}

/**
 * Common interface for cloud storage providers.
 * Implemented by GoogleDriveProvider and DropboxProvider.
 */
interface CloudProvider {
    /**
     * The type of this cloud provider.
     */
    val type: CloudProviderType

    /**
     * Flow indicating whether the user is authenticated with this provider.
     */
    fun isAuthenticated(): Flow<Boolean>

    /**
     * Gets the OAuth authorization URL to start the authentication flow.
     */
    suspend fun getAuthUrl(): String

    /**
     * Handles the OAuth callback after user authorizes the app.
     *
     * @param code The authorization code from the OAuth callback
     * @param redirectUri Optional override for the redirect URI used in token exchange
     */
    suspend fun handleAuthCallback(code: String, redirectUri: String? = null): CloudResult<Unit>

    /**
     * Disconnects from the cloud provider and clears stored tokens.
     */
    suspend fun disconnect()

    /**
     * Refreshes the access token if it's expired.
     */
    suspend fun refreshTokenIfNeeded(): CloudResult<Unit>

    /**
     * Lists all backup files in the app's cloud folder.
     */
    suspend fun listBackups(): CloudResult<List<CloudFile>>

    /**
     * Uploads a backup file to the cloud.
     *
     * @param fileName Name of the file to create
     * @param data File contents as byte array
     */
    suspend fun uploadBackup(fileName: String, data: ByteArray): CloudResult<CloudFile>

    /**
     * Downloads a backup file from the cloud.
     *
     * @param fileId ID of the file to download
     */
    suspend fun downloadBackup(fileId: String): CloudResult<ByteArray>

    /**
     * Deletes a backup file from the cloud.
     *
     * @param fileId ID of the file to delete
     */
    suspend fun deleteBackup(fileId: String): CloudResult<Unit>

    /**
     * Gets the storage quota information.
     */
    suspend fun getQuotaInfo(): CloudResult<QuotaInfo>
}
