package com.romreviewertools.noteitup.domain.repository

import com.romreviewertools.noteitup.domain.model.AutoLockTimeout
import com.romreviewertools.noteitup.domain.model.LockType
import com.romreviewertools.noteitup.domain.model.SecuritySettings
import kotlinx.coroutines.flow.Flow

interface SecurityRepository {
    fun getSecuritySettings(): Flow<SecuritySettings>
    suspend fun setLockType(lockType: LockType)
    suspend fun setAutoLockTimeout(timeout: AutoLockTimeout)
    suspend fun setPin(pin: String)
    suspend fun verifyPin(pin: String): Boolean
    suspend fun clearPin()
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun updateLastActiveTime()
    suspend fun getLastActiveTime(): Long
    suspend fun shouldLock(): Boolean
}
