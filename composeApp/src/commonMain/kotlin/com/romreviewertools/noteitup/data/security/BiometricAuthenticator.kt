package com.romreviewertools.noteitup.data.security

enum class BiometricType {
    NONE,
    FINGERPRINT,
    FACE,
    IRIS,
    MULTIPLE
}

enum class BiometricStatus {
    AVAILABLE,
    NOT_AVAILABLE,
    NOT_ENROLLED,
    HARDWARE_NOT_PRESENT
}

sealed class BiometricResult {
    data object Success : BiometricResult()
    data object Cancelled : BiometricResult()
    data class Error(val message: String) : BiometricResult()
    data object NotAvailable : BiometricResult()
}

expect class BiometricAuthenticator {
    fun getBiometricStatus(): BiometricStatus
    fun getBiometricType(): BiometricType
    suspend fun authenticate(
        title: String,
        subtitle: String,
        negativeButtonText: String
    ): BiometricResult
}
