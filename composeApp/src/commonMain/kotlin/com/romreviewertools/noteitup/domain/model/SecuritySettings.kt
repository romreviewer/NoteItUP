package com.romreviewertools.noteitup.domain.model

enum class LockType {
    NONE,
    PIN,
    BIOMETRIC,
    PIN_AND_BIOMETRIC
}

enum class AutoLockTimeout(val seconds: Int, val label: String) {
    IMMEDIATE(0, "Immediately"),
    AFTER_1_MIN(60, "After 1 minute"),
    AFTER_5_MIN(300, "After 5 minutes"),
    AFTER_15_MIN(900, "After 15 minutes"),
    AFTER_30_MIN(1800, "After 30 minutes"),
    NEVER(-1, "Never")
}

data class SecuritySettings(
    val lockType: LockType = LockType.NONE,
    val autoLockTimeout: AutoLockTimeout = AutoLockTimeout.AFTER_1_MIN,
    val pinHash: String? = null,
    val biometricEnabled: Boolean = false
)
