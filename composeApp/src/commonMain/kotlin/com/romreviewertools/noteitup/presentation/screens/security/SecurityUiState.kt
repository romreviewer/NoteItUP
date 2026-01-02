package com.romreviewertools.noteitup.presentation.screens.security

import com.romreviewertools.noteitup.data.security.BiometricType
import com.romreviewertools.noteitup.domain.model.AutoLockTimeout
import com.romreviewertools.noteitup.domain.model.LockType

data class SecurityUiState(
    val isLocked: Boolean = false,
    val lockType: LockType = LockType.NONE,
    val autoLockTimeout: AutoLockTimeout = AutoLockTimeout.AFTER_1_MIN,
    val biometricEnabled: Boolean = false,
    val hasPinSet: Boolean = false,
    val pinEntry: String = "",
    val pinError: String? = null,
    val isSettingUp: Boolean = false,
    val setupPin: String = "",
    val confirmPin: String = "",
    val setupStep: SetupStep = SetupStep.ENTER_PIN,
    val biometricAvailable: Boolean = false,
    val biometricType: BiometricType = BiometricType.NONE
)

enum class SetupStep {
    ENTER_PIN,
    CONFIRM_PIN
}
