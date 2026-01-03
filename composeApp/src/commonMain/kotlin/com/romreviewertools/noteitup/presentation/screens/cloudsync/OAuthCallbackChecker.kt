package com.romreviewertools.noteitup.presentation.screens.cloudsync

import com.romreviewertools.noteitup.data.cloud.CloudProviderType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Platform-specific OAuth callback checker.
 * Returns the pending OAuth code and provider type if available.
 */
expect fun consumePendingOAuthCallback(): OAuthCallback?

data class OAuthCallback(
    val code: String,
    val provider: CloudProviderType
)

/**
 * Shared flow for OAuth callbacks - allows reactive handling when app resumes.
 */
object OAuthCallbackEmitter {
    private val _callbacks = MutableSharedFlow<OAuthCallback>(extraBufferCapacity = 1)
    val callbacks: SharedFlow<OAuthCallback> = _callbacks.asSharedFlow()

    fun emit(callback: OAuthCallback) {
        _callbacks.tryEmit(callback)
    }
}
