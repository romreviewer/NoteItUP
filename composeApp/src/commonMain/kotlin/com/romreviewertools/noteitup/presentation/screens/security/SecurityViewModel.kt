package com.romreviewertools.noteitup.presentation.screens.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.romreviewertools.noteitup.data.analytics.AnalyticsEvent
import com.romreviewertools.noteitup.data.analytics.AnalyticsService
import com.romreviewertools.noteitup.data.security.BiometricAuthenticator
import com.romreviewertools.noteitup.data.security.BiometricResult
import com.romreviewertools.noteitup.data.security.BiometricStatus
import com.romreviewertools.noteitup.data.security.BiometricType
import com.romreviewertools.noteitup.domain.model.LockType
import com.romreviewertools.noteitup.domain.repository.SecurityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SecurityViewModel(
    private val securityRepository: SecurityRepository,
    private val biometricAuthenticator: BiometricAuthenticator,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    init {
        analyticsService.logEvent(AnalyticsEvent.ScreenViewSecurity)
        loadSecuritySettings()
        checkLockStatus()
        checkBiometricAvailability()
    }

    private fun checkBiometricAvailability() {
        val status = biometricAuthenticator.getBiometricStatus()
        val type = biometricAuthenticator.getBiometricType()
        _uiState.update {
            it.copy(
                biometricAvailable = status == BiometricStatus.AVAILABLE,
                biometricType = type
            )
        }
    }

    fun authenticateWithBiometric() {
        viewModelScope.launch {
            val result = biometricAuthenticator.authenticate(
                title = "Unlock NoteItUP",
                subtitle = "Use your biometric to unlock the app",
                negativeButtonText = "Use PIN"
            )
            when (result) {
                is BiometricResult.Success -> {
                    processIntent(SecurityIntent.BiometricSuccess)
                }
                is BiometricResult.Cancelled -> {
                    // User cancelled or chose to use PIN - do nothing
                }
                is BiometricResult.Error -> {
                    _uiState.update { it.copy(pinError = result.message) }
                }
                is BiometricResult.NotAvailable -> {
                    _uiState.update { it.copy(biometricAvailable = false) }
                }
            }
        }
    }

    fun processIntent(intent: SecurityIntent) {
        when (intent) {
            is SecurityIntent.EnterPinDigit -> enterPinDigit(intent.digit)
            SecurityIntent.DeletePinDigit -> deletePinDigit()
            SecurityIntent.ClearPin -> clearPin()
            SecurityIntent.SubmitPin -> submitPin()
            SecurityIntent.BiometricSuccess -> unlockApp()
            SecurityIntent.BiometricFailed -> _uiState.update { it.copy(pinError = "Biometric authentication failed") }
            is SecurityIntent.SetLockType -> setLockType(intent.lockType)
            is SecurityIntent.SetAutoLockTimeout -> setAutoLockTimeout(intent.timeout)
            SecurityIntent.StartPinSetup -> startPinSetup()
            SecurityIntent.CancelPinSetup -> cancelPinSetup()
            SecurityIntent.RemovePin -> removePin()
            is SecurityIntent.SetBiometricEnabled -> setBiometricEnabled(intent.enabled)
            SecurityIntent.CheckLockStatus -> checkLockStatus()
            SecurityIntent.DismissError -> _uiState.update { it.copy(pinError = null) }
        }
    }

    private fun loadSecuritySettings() {
        viewModelScope.launch {
            securityRepository.getSecuritySettings().collect { settings ->
                _uiState.update {
                    it.copy(
                        lockType = settings.lockType,
                        autoLockTimeout = settings.autoLockTimeout,
                        biometricEnabled = settings.biometricEnabled,
                        hasPinSet = settings.pinHash != null
                    )
                }
            }
        }
    }

    private fun checkLockStatus() {
        viewModelScope.launch {
            val shouldLock = securityRepository.shouldLock()
            _uiState.update { it.copy(isLocked = shouldLock) }
        }
    }

    private fun enterPinDigit(digit: String) {
        val currentState = _uiState.value

        if (currentState.isSettingUp) {
            when (currentState.setupStep) {
                SetupStep.ENTER_PIN -> {
                    if (currentState.setupPin.length < 6) {
                        val newPin = currentState.setupPin + digit
                        _uiState.update { it.copy(setupPin = newPin, pinError = null) }
                        if (newPin.length == 4 || newPin.length == 6) {
                            // Auto advance after 4 or 6 digits
                        }
                    }
                }
                SetupStep.CONFIRM_PIN -> {
                    if (currentState.confirmPin.length < 6) {
                        val newPin = currentState.confirmPin + digit
                        _uiState.update { it.copy(confirmPin = newPin, pinError = null) }
                    }
                }
            }
        } else {
            if (currentState.pinEntry.length < 6) {
                val newPin = currentState.pinEntry + digit
                _uiState.update { it.copy(pinEntry = newPin, pinError = null) }
            }
        }
    }

    private fun deletePinDigit() {
        val currentState = _uiState.value

        if (currentState.isSettingUp) {
            when (currentState.setupStep) {
                SetupStep.ENTER_PIN -> {
                    if (currentState.setupPin.isNotEmpty()) {
                        _uiState.update { it.copy(setupPin = it.setupPin.dropLast(1)) }
                    }
                }
                SetupStep.CONFIRM_PIN -> {
                    if (currentState.confirmPin.isNotEmpty()) {
                        _uiState.update { it.copy(confirmPin = it.confirmPin.dropLast(1)) }
                    }
                }
            }
        } else {
            if (currentState.pinEntry.isNotEmpty()) {
                _uiState.update { it.copy(pinEntry = it.pinEntry.dropLast(1)) }
            }
        }
    }

    private fun clearPin() {
        val currentState = _uiState.value

        if (currentState.isSettingUp) {
            when (currentState.setupStep) {
                SetupStep.ENTER_PIN -> _uiState.update { it.copy(setupPin = "") }
                SetupStep.CONFIRM_PIN -> _uiState.update { it.copy(confirmPin = "") }
            }
        } else {
            _uiState.update { it.copy(pinEntry = "") }
        }
    }

    private fun submitPin() {
        val currentState = _uiState.value

        if (currentState.isSettingUp) {
            when (currentState.setupStep) {
                SetupStep.ENTER_PIN -> {
                    if (currentState.setupPin.length >= 4) {
                        _uiState.update { it.copy(setupStep = SetupStep.CONFIRM_PIN) }
                    } else {
                        _uiState.update { it.copy(pinError = "PIN must be at least 4 digits") }
                    }
                }
                SetupStep.CONFIRM_PIN -> {
                    if (currentState.confirmPin == currentState.setupPin) {
                        viewModelScope.launch {
                            securityRepository.setPin(currentState.setupPin)
                            securityRepository.setLockType(LockType.PIN)
                            _uiState.update {
                                it.copy(
                                    isSettingUp = false,
                                    setupPin = "",
                                    confirmPin = "",
                                    setupStep = SetupStep.ENTER_PIN,
                                    hasPinSet = true,
                                    lockType = LockType.PIN
                                )
                            }
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                pinError = "PINs don't match",
                                confirmPin = ""
                            )
                        }
                    }
                }
            }
        } else {
            viewModelScope.launch {
                val isValid = securityRepository.verifyPin(currentState.pinEntry)
                if (isValid) {
                    unlockApp()
                } else {
                    _uiState.update {
                        it.copy(
                            pinError = "Incorrect PIN",
                            pinEntry = ""
                        )
                    }
                }
            }
        }
    }

    private fun unlockApp() {
        viewModelScope.launch {
            securityRepository.updateLastActiveTime()
            _uiState.update {
                it.copy(
                    isLocked = false,
                    pinEntry = "",
                    pinError = null
                )
            }
        }
    }

    private fun setLockType(lockType: LockType) {
        viewModelScope.launch {
            securityRepository.setLockType(lockType)
            _uiState.update { it.copy(lockType = lockType) }
        }
    }

    private fun setAutoLockTimeout(timeout: com.romreviewertools.noteitup.domain.model.AutoLockTimeout) {
        viewModelScope.launch {
            securityRepository.setAutoLockTimeout(timeout)
            _uiState.update { it.copy(autoLockTimeout = timeout) }
        }
    }

    private fun startPinSetup() {
        _uiState.update {
            it.copy(
                isSettingUp = true,
                setupPin = "",
                confirmPin = "",
                setupStep = SetupStep.ENTER_PIN,
                pinError = null
            )
        }
    }

    private fun cancelPinSetup() {
        _uiState.update {
            it.copy(
                isSettingUp = false,
                setupPin = "",
                confirmPin = "",
                setupStep = SetupStep.ENTER_PIN,
                pinError = null
            )
        }
    }

    private fun removePin() {
        viewModelScope.launch {
            securityRepository.clearPin()
            securityRepository.setLockType(LockType.NONE)
            _uiState.update {
                it.copy(
                    hasPinSet = false,
                    lockType = LockType.NONE
                )
            }
        }
    }

    private fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            securityRepository.setBiometricEnabled(enabled)
            if (enabled && _uiState.value.hasPinSet) {
                securityRepository.setLockType(LockType.PIN_AND_BIOMETRIC)
                _uiState.update {
                    it.copy(
                        biometricEnabled = true,
                        lockType = LockType.PIN_AND_BIOMETRIC
                    )
                }
            } else {
                _uiState.update { it.copy(biometricEnabled = enabled) }
            }
        }
    }

    fun updateLastActiveTime() {
        viewModelScope.launch {
            securityRepository.updateLastActiveTime()
        }
    }
}
