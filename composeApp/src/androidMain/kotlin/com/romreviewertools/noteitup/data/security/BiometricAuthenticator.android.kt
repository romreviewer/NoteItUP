package com.romreviewertools.noteitup.data.security

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class BiometricAuthenticator(
    private val context: Context
) {
    private val biometricManager = BiometricManager.from(context)

    actual fun getBiometricStatus(): BiometricStatus {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.HARDWARE_NOT_PRESENT
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.NOT_AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
            else -> BiometricStatus.NOT_AVAILABLE
        }
    }

    actual fun getBiometricType(): BiometricType {
        if (getBiometricStatus() != BiometricStatus.AVAILABLE) {
            return BiometricType.NONE
        }

        // Check for face unlock (API 29+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val hasFace = context.packageManager.hasSystemFeature("android.hardware.biometrics.face")
            val hasFingerprint = context.packageManager.hasSystemFeature("android.hardware.fingerprint")
            val hasIris = context.packageManager.hasSystemFeature("android.hardware.biometrics.iris")

            return when {
                hasFace && hasFingerprint -> BiometricType.MULTIPLE
                hasFace -> BiometricType.FACE
                hasIris -> BiometricType.IRIS
                hasFingerprint -> BiometricType.FINGERPRINT
                else -> BiometricType.FINGERPRINT // Default fallback
            }
        }

        return BiometricType.FINGERPRINT
    }

    actual suspend fun authenticate(
        title: String,
        subtitle: String,
        negativeButtonText: String
    ): BiometricResult = suspendCancellableCoroutine { continuation ->
        val activity = context as? FragmentActivity
        if (activity == null) {
            continuation.resume(BiometricResult.Error("Activity not available"))
            return@suspendCancellableCoroutine
        }

        if (getBiometricStatus() != BiometricStatus.AVAILABLE) {
            continuation.resume(BiometricResult.NotAvailable)
            return@suspendCancellableCoroutine
        }

        val executor = ContextCompat.getMainExecutor(context)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                if (continuation.isActive) {
                    continuation.resume(BiometricResult.Success)
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (continuation.isActive) {
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_CANCELED -> {
                            continuation.resume(BiometricResult.Cancelled)
                        }
                        else -> {
                            continuation.resume(BiometricResult.Error(errString.toString()))
                        }
                    }
                }
            }

            override fun onAuthenticationFailed() {
                // Don't resume here - this is called for each failed attempt
                // The prompt will show an error and let user try again
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()

        biometricPrompt.authenticate(promptInfo)

        continuation.invokeOnCancellation {
            biometricPrompt.cancelAuthentication()
        }
    }
}
