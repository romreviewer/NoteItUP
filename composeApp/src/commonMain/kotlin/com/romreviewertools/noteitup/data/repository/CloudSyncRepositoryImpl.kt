package com.romreviewertools.noteitup.data.repository

import com.romreviewertools.noteitup.data.cloud.CloudProviderType
import com.romreviewertools.noteitup.data.preferences.PreferencesKeys
import com.romreviewertools.noteitup.data.preferences.PreferencesStorage
import com.romreviewertools.noteitup.domain.model.AutoSyncInterval
import com.romreviewertools.noteitup.domain.model.CloudSyncSettings
import com.romreviewertools.noteitup.domain.model.CloudTokenInfo
import com.romreviewertools.noteitup.domain.repository.CloudSyncRepository
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class CloudSyncRepositoryImpl(
    private val preferencesStorage: PreferencesStorage
) : CloudSyncRepository {

    override fun getSyncSettings(): Flow<CloudSyncSettings> {
        return combine(
            preferencesStorage.getString(PreferencesKeys.CLOUD_AUTO_SYNC_ENABLED, "false"),
            preferencesStorage.getString(PreferencesKeys.CLOUD_AUTO_SYNC_INTERVAL, AutoSyncInterval.HOURLY.name),
            preferencesStorage.getString(PreferencesKeys.CLOUD_SYNC_WIFI_ONLY, "true"),
            preferencesStorage.getString(PreferencesKeys.CLOUD_LAST_SYNC_TIME, "0"),
            getConnectedProviders()
        ) { autoSync, interval, wifiOnly, lastSync, providers ->
            CloudSyncSettings(
                autoSyncEnabled = autoSync.toBoolean(),
                autoSyncInterval = try {
                    AutoSyncInterval.valueOf(interval)
                } catch (e: Exception) {
                    AutoSyncInterval.HOURLY
                },
                syncOnWifiOnly = wifiOnly.toBoolean(),
                lastSyncTime = lastSync.toLongOrNull()?.takeIf { it > 0 },
                connectedProviders = providers
            )
        }
    }

    override suspend fun setAutoSyncEnabled(enabled: Boolean) {
        preferencesStorage.putString(PreferencesKeys.CLOUD_AUTO_SYNC_ENABLED, enabled.toString())
    }

    override suspend fun setAutoSyncInterval(interval: AutoSyncInterval) {
        preferencesStorage.putString(PreferencesKeys.CLOUD_AUTO_SYNC_INTERVAL, interval.name)
    }

    override suspend fun setSyncOnWifiOnly(wifiOnly: Boolean) {
        preferencesStorage.putString(PreferencesKeys.CLOUD_SYNC_WIFI_ONLY, wifiOnly.toString())
    }

    override suspend fun updateLastSyncTime(timestamp: Long) {
        preferencesStorage.putString(PreferencesKeys.CLOUD_LAST_SYNC_TIME, timestamp.toString())
    }

    override suspend fun updateLastLocalModificationTime(timestamp: Long) {
        preferencesStorage.putString(PreferencesKeys.CLOUD_LAST_LOCAL_MODIFICATION, timestamp.toString())
    }

    override suspend fun getLastLocalModificationTime(): Long {
        return preferencesStorage.getString(PreferencesKeys.CLOUD_LAST_LOCAL_MODIFICATION, "0")
            .first().toLongOrNull() ?: 0L
    }

    override suspend fun getLastSyncTime(): Long {
        return preferencesStorage.getString(PreferencesKeys.CLOUD_LAST_SYNC_TIME, "0")
            .first().toLongOrNull() ?: 0L
    }

    override suspend fun saveTokens(
        provider: CloudProviderType,
        accessToken: String,
        refreshToken: String?,
        expiresIn: Long
    ) {
        val prefix = getProviderPrefix(provider)
        val expiresAt = Clock.System.now().toEpochMilliseconds() + (expiresIn * 1000)

        preferencesStorage.putString("${prefix}_access_token", accessToken)
        refreshToken?.let {
            preferencesStorage.putString("${prefix}_refresh_token", it)
        }
        preferencesStorage.putString("${prefix}_expires_at", expiresAt.toString())
        preferencesStorage.putString("${prefix}_connected", "true")
    }

    override suspend fun getTokenInfo(provider: CloudProviderType): CloudTokenInfo? {
        val prefix = getProviderPrefix(provider)
        val accessToken = preferencesStorage.getString("${prefix}_access_token", "").first()

        if (accessToken.isEmpty()) return null

        val refreshToken = preferencesStorage.getString("${prefix}_refresh_token", "").first()
            .takeIf { it.isNotEmpty() }
        val expiresAt = preferencesStorage.getString("${prefix}_expires_at", "0").first()
            .toLongOrNull() ?: 0L

        return CloudTokenInfo(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = expiresAt
        )
    }

    override fun getAccessToken(provider: CloudProviderType): Flow<String?> {
        val prefix = getProviderPrefix(provider)
        return preferencesStorage.getString("${prefix}_access_token", "")
            .map { it.takeIf { token -> token.isNotEmpty() } }
    }

    override suspend fun isTokenExpired(provider: CloudProviderType): Boolean {
        val tokenInfo = getTokenInfo(provider) ?: return true
        return tokenInfo.isExpired
    }

    override suspend fun clearTokens(provider: CloudProviderType) {
        val prefix = getProviderPrefix(provider)
        preferencesStorage.putString("${prefix}_access_token", "")
        preferencesStorage.putString("${prefix}_refresh_token", "")
        preferencesStorage.putString("${prefix}_expires_at", "0")
        preferencesStorage.putString("${prefix}_connected", "false")
    }

    override fun getConnectedProviders(): Flow<Set<CloudProviderType>> {
        return combine(
            preferencesStorage.getString("${getProviderPrefix(CloudProviderType.GOOGLE_DRIVE)}_connected", "false"),
            preferencesStorage.getString("${getProviderPrefix(CloudProviderType.DROPBOX)}_connected", "false")
        ) { googleConnected, dropboxConnected ->
            buildSet {
                if (googleConnected.toBoolean()) add(CloudProviderType.GOOGLE_DRIVE)
                if (dropboxConnected.toBoolean()) add(CloudProviderType.DROPBOX)
            }
        }
    }

    override suspend fun saveSyncPasswordHash(hash: String) {
        preferencesStorage.putString(PreferencesKeys.CLOUD_SYNC_PASSWORD_HASH, hash)
    }

    override suspend fun verifySyncPassword(password: String): Boolean {
        val storedHash = preferencesStorage.getString(PreferencesKeys.CLOUD_SYNC_PASSWORD_HASH, "").first()
        if (storedHash.isEmpty()) return false

        // Simple hash comparison - in production, use proper password hashing
        val inputHash = hashPassword(password)
        return inputHash == storedHash
    }

    override suspend fun hasSyncPassword(): Boolean {
        val hash = preferencesStorage.getString(PreferencesKeys.CLOUD_SYNC_PASSWORD_HASH, "").first()
        return hash.isNotEmpty()
    }

    override suspend fun clearSyncPassword() {
        preferencesStorage.putString(PreferencesKeys.CLOUD_SYNC_PASSWORD_HASH, "")
    }

    private fun getProviderPrefix(provider: CloudProviderType): String {
        return when (provider) {
            CloudProviderType.GOOGLE_DRIVE -> "cloud_gdrive"
            CloudProviderType.DROPBOX -> "cloud_dropbox"
        }
    }

    private fun hashPassword(password: String): String {
        // Simple hash for demo - in production use PBKDF2 or similar
        var hash = 7L
        for (char in password) {
            hash = hash * 31 + char.code
        }
        return hash.toString(16)
    }
}
