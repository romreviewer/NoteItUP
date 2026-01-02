package com.romreviewertools.noteitup.presentation.screens.security

import com.romreviewertools.noteitup.domain.model.AutoLockTimeout
import com.romreviewertools.noteitup.domain.model.LockType

sealed interface SecurityIntent {
    data class EnterPinDigit(val digit: String) : SecurityIntent
    data object DeletePinDigit : SecurityIntent
    data object ClearPin : SecurityIntent
    data object SubmitPin : SecurityIntent
    data object BiometricSuccess : SecurityIntent
    data object BiometricFailed : SecurityIntent
    data class SetLockType(val lockType: LockType) : SecurityIntent
    data class SetAutoLockTimeout(val timeout: AutoLockTimeout) : SecurityIntent
    data object StartPinSetup : SecurityIntent
    data object CancelPinSetup : SecurityIntent
    data object RemovePin : SecurityIntent
    data class SetBiometricEnabled(val enabled: Boolean) : SecurityIntent
    data object CheckLockStatus : SecurityIntent
    data object DismissError : SecurityIntent
}
