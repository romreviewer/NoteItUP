package com.romreviewertools.noteitup.data.cloud

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
     * Returns a server auth code on success, or null if unavailable/cancelled.
     * Non-Android platforms return null to fall through to browser flow.
     */
    suspend fun startNativeGoogleAuth(): String?
}
