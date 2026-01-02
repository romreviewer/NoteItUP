package com.romreviewertools.noteitup.data.security

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LABiometryType
import platform.LocalAuthentication.LABiometryTypeFaceID
import platform.LocalAuthentication.LABiometryTypeTouchID
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import kotlin.coroutines.resume

actual class BiometricAuthenticator {
    private val context = LAContext()

    @OptIn(ExperimentalForeignApi::class)
    actual fun getBiometricStatus(): BiometricStatus {
        val canEvaluate = context.canEvaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            error = null
        )

        return if (canEvaluate) {
            BiometricStatus.AVAILABLE
        } else {
            BiometricStatus.NOT_AVAILABLE
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun getBiometricType(): BiometricType {
        // Need to call canEvaluatePolicy first to populate biometryType
        context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, error = null)

        return when (context.biometryType) {
            LABiometryTypeFaceID -> BiometricType.FACE
            LABiometryTypeTouchID -> BiometricType.FINGERPRINT
            else -> BiometricType.NONE
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun authenticate(
        title: String,
        subtitle: String,
        negativeButtonText: String
    ): BiometricResult = suspendCancellableCoroutine { continuation ->
        if (getBiometricStatus() != BiometricStatus.AVAILABLE) {
            continuation.resume(BiometricResult.NotAvailable)
            return@suspendCancellableCoroutine
        }

        val authContext = LAContext()
        authContext.localizedFallbackTitle = negativeButtonText

        authContext.evaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            localizedReason = subtitle
        ) { success, error ->
            if (continuation.isActive) {
                when {
                    success -> {
                        continuation.resume(BiometricResult.Success)
                    }
                    error != null -> {
                        val errorCode = error.code
                        // LAError codes: userCancel = -2, userFallback = -3, systemCancel = -4
                        when (errorCode) {
                            -2L, -3L, -4L -> {
                                continuation.resume(BiometricResult.Cancelled)
                            }
                            else -> {
                                continuation.resume(
                                    BiometricResult.Error(
                                        error.localizedDescription ?: "Authentication failed"
                                    )
                                )
                            }
                        }
                    }
                    else -> {
                        continuation.resume(BiometricResult.Error("Unknown error"))
                    }
                }
            }
        }
    }
}
