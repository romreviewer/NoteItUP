package com.romreviewertools.noteitup.data.repository

import com.romreviewertools.noteitup.data.preferences.PreferencesKeys
import com.romreviewertools.noteitup.data.preferences.PreferencesStorage
import com.romreviewertools.noteitup.domain.model.AutoLockTimeout
import com.romreviewertools.noteitup.domain.model.LockType
import com.romreviewertools.noteitup.domain.model.SecuritySettings
import com.romreviewertools.noteitup.domain.repository.SecurityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlin.time.Clock

class SecurityRepositoryImpl(
    private val preferencesStorage: PreferencesStorage
) : SecurityRepository {

    override fun getSecuritySettings(): Flow<SecuritySettings> {
        return combine(
            preferencesStorage.getString(PreferencesKeys.LOCK_TYPE, LockType.NONE.name),
            preferencesStorage.getString(PreferencesKeys.AUTO_LOCK_TIMEOUT, AutoLockTimeout.AFTER_1_MIN.name),
            preferencesStorage.getString(PreferencesKeys.PIN_HASH, ""),
            preferencesStorage.getString(PreferencesKeys.BIOMETRIC_ENABLED, "false")
        ) { lockTypeStr, autoLockStr, pinHash, biometricStr ->
            SecuritySettings(
                lockType = LockType.entries.find { it.name == lockTypeStr } ?: LockType.NONE,
                autoLockTimeout = AutoLockTimeout.entries.find { it.name == autoLockStr } ?: AutoLockTimeout.AFTER_1_MIN,
                pinHash = pinHash.ifEmpty { null },
                biometricEnabled = biometricStr.toBoolean()
            )
        }
    }

    override suspend fun setLockType(lockType: LockType) {
        preferencesStorage.putString(PreferencesKeys.LOCK_TYPE, lockType.name)
    }

    override suspend fun setAutoLockTimeout(timeout: AutoLockTimeout) {
        preferencesStorage.putString(PreferencesKeys.AUTO_LOCK_TIMEOUT, timeout.name)
    }

    override suspend fun setPin(pin: String) {
        val hash = hashPin(pin)
        preferencesStorage.putString(PreferencesKeys.PIN_HASH, hash)
    }

    override suspend fun verifyPin(pin: String): Boolean {
        val storedHash = preferencesStorage.getString(PreferencesKeys.PIN_HASH, "").first()
        if (storedHash.isEmpty()) return false
        return hashPin(pin) == storedHash
    }

    override suspend fun clearPin() {
        preferencesStorage.putString(PreferencesKeys.PIN_HASH, "")
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        preferencesStorage.putString(PreferencesKeys.BIOMETRIC_ENABLED, enabled.toString())
    }

    override suspend fun updateLastActiveTime() {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        preferencesStorage.putString(PreferencesKeys.LAST_ACTIVE_TIME, currentTime.toString())
    }

    override suspend fun getLastActiveTime(): Long {
        val timeStr = preferencesStorage.getString(PreferencesKeys.LAST_ACTIVE_TIME, "0").first()
        return timeStr.toLongOrNull() ?: 0L
    }

    override suspend fun shouldLock(): Boolean {
        val settings = getSecuritySettings().first()

        // No lock if no PIN is set
        if (settings.pinHash == null) return false

        // No lock if lock type is NONE
        if (settings.lockType == LockType.NONE) return false

        // Always lock on first launch (lastActive == 0)
        val lastActive = getLastActiveTime()
        if (lastActive == 0L) return true

        // Never auto-lock if timeout is set to NEVER
        if (settings.autoLockTimeout == AutoLockTimeout.NEVER) return false

        // Check if enough time has passed since last active
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val elapsedSeconds = (currentTime - lastActive) / 1000

        // For IMMEDIATE timeout, always lock
        if (settings.autoLockTimeout == AutoLockTimeout.IMMEDIATE) return true

        return elapsedSeconds >= settings.autoLockTimeout.seconds
    }

    private fun hashPin(pin: String): String {
        // Simple hash for PIN - in production, use proper cryptographic hashing
        // This is a basic implementation using a simple algorithm
        var hash = 7L
        for (char in pin) {
            hash = hash * 31 + char.code
        }
        return hash.toString(16)
    }
}
