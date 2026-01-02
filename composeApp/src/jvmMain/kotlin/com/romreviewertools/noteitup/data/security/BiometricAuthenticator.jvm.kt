package com.romreviewertools.noteitup.data.security

actual class BiometricAuthenticator {
    actual fun getBiometricStatus(): BiometricStatus {
        // Desktop doesn't typically have biometric hardware
        return BiometricStatus.HARDWARE_NOT_PRESENT
    }

    actual fun getBiometricType(): BiometricType {
        return BiometricType.NONE
    }

    actual suspend fun authenticate(
        title: String,
        subtitle: String,
        negativeButtonText: String
    ): BiometricResult {
        return BiometricResult.NotAvailable
    }
}
