package com.romreviewertools.noteitup.domain.model

import com.romreviewertools.noteitup.data.cloud.CloudProviderType
import kotlin.time.Clock

/**
 * Settings for cloud sync functionality.
 */
data class CloudSyncSettings(
    val autoSyncEnabled: Boolean = false,
    val autoSyncInterval: AutoSyncInterval = AutoSyncInterval.HOURLY,
    val syncOnWifiOnly: Boolean = true,
    val lastSyncTime: Long? = null,
    val lastLocalModificationTime: Long? = null,
    val connectedProviders: Set<CloudProviderType> = emptySet()
)

/**
 * Available intervals for auto-sync.
 */
enum class AutoSyncInterval(val minutes: Int, val label: String) {
    EVERY_15_MIN(15, "Every 15 minutes"),
    EVERY_30_MIN(30, "Every 30 minutes"),
    HOURLY(60, "Every hour"),
    EVERY_6_HOURS(360, "Every 6 hours"),
    DAILY(1440, "Daily")
}

/**
 * OAuth token information for a cloud provider.
 */
data class CloudTokenInfo(
    val accessToken: String,
    val refreshToken: String?,
    val expiresAt: Long
) {
    val isExpired: Boolean
        get() = expiresAt < Clock.System.now().toEpochMilliseconds()
}
