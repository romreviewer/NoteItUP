package com.romreviewertools.noteitup.domain.repository

import com.romreviewertools.noteitup.data.cloud.CloudProviderType
import com.romreviewertools.noteitup.domain.model.AutoSyncInterval
import com.romreviewertools.noteitup.domain.model.CloudSyncSettings
import com.romreviewertools.noteitup.domain.model.CloudTokenInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing cloud sync settings and tokens.
 */
interface CloudSyncRepository {
    /**
     * Gets the current cloud sync settings as a Flow.
     */
    fun getSyncSettings(): Flow<CloudSyncSettings>

    /**
     * Updates the auto-sync enabled state.
     */
    suspend fun setAutoSyncEnabled(enabled: Boolean)

    /**
     * Updates the auto-sync interval.
     */
    suspend fun setAutoSyncInterval(interval: AutoSyncInterval)

    /**
     * Updates the WiFi-only sync preference.
     */
    suspend fun setSyncOnWifiOnly(wifiOnly: Boolean)

    /**
     * Updates the last sync timestamp.
     */
    suspend fun updateLastSyncTime(timestamp: Long)

    /**
     * Updates the last local modification timestamp.
     */
    suspend fun updateLastLocalModificationTime(timestamp: Long)

    /**
     * Gets the last local modification timestamp.
     */
    suspend fun getLastLocalModificationTime(): Long

    /**
     * Gets the last sync timestamp.
     */
    suspend fun getLastSyncTime(): Long

    /**
     * Saves OAuth tokens for a cloud provider.
     */
    suspend fun saveTokens(
        provider: CloudProviderType,
        accessToken: String,
        refreshToken: String?,
        expiresIn: Long
    )

    /**
     * Gets the stored token info for a provider.
     */
    suspend fun getTokenInfo(provider: CloudProviderType): CloudTokenInfo?

    /**
     * Gets the access token for a provider as a Flow.
     */
    fun getAccessToken(provider: CloudProviderType): Flow<String?>

    /**
     * Checks if a token is expired.
     */
    suspend fun isTokenExpired(provider: CloudProviderType): Boolean

    /**
     * Clears tokens for a provider (disconnect).
     */
    suspend fun clearTokens(provider: CloudProviderType)

    /**
     * Gets all connected providers.
     */
    fun getConnectedProviders(): Flow<Set<CloudProviderType>>

    /**
     * Saves the encryption password hash (for verification only).
     * The actual password is never stored.
     */
    suspend fun saveSyncPasswordHash(hash: String)

    /**
     * Verifies if a password matches the stored hash.
     */
    suspend fun verifySyncPassword(password: String): Boolean

    /**
     * Checks if a sync password has been set.
     */
    suspend fun hasSyncPassword(): Boolean

    /**
     * Clears the sync password hash.
     */
    suspend fun clearSyncPassword()
}
