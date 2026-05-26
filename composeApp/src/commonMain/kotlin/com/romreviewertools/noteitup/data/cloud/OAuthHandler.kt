package com.romreviewertools.noteitup.data.cloud

/**
 * Result of a native (non-browser) cloud provider sign-in attempt.
 * Non-supporting platforms always return [Unsupported].
 */
sealed class NativeAuthResult {
    /** Returned an auth code that still needs to be exchanged for tokens. */
    data class Success(val code: String) : NativeAuthResult()

    /**
     * Returned tokens directly, skipping the auth-code exchange step.
     * Used by SDKs (e.g. Dropbox) that handle the OAuth handshake internally.
     */
    data class SuccessTokens(
        val accessToken: String,
        val refreshToken: String?,
        val expiresIn: Long
    ) : NativeAuthResult()

    object Cancelled : NativeAuthResult()
    data class Failed(val message: String) : NativeAuthResult()
    object Unsupported : NativeAuthResult()
}

/**
 * Platform-specific OAuth handler for cloud provider authentication.
 * Uses browser-based OAuth flow on each platform.
 */
expect class OAuthHandler {
    /**
     * Opens the OAuth authorization URL in the system browser/webview.
     * @param authUrl The OAuth authorization URL to open
     */
    suspend fun openAuthUrl(authUrl: String)

    /**
     * Creates a redirect URI for OAuth callback.
     * Returns a platform-appropriate redirect URI.
     */
    fun getRedirectUri(provider: CloudProviderType): String

    /**
     * Attempts native Google Sign-In (Android only).
     * Non-Android platforms return [NativeAuthResult.Unsupported] so the caller
     * can fall through to the browser flow.
     */
    suspend fun startNativeGoogleAuth(): NativeAuthResult

    /**
     * Starts the Dropbox SDK auth flow on Android (uses the Dropbox app if installed,
     * Chrome Custom Tabs otherwise). Non-Android platforms return [NativeAuthResult.Unsupported].
     */
    suspend fun startNativeDropboxAuth(): NativeAuthResult
}
