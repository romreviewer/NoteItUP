package com.romreviewertools.noteitup.data.cloud

import java.awt.Desktop
import java.net.URI

/**
 * JVM/Desktop OAuth handler using system browser.
 * Uses localhost redirect with a temporary server for callback.
 */
actual class OAuthHandler {

    actual suspend fun openAuthUrl(authUrl: String) {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI(authUrl))
        }
    }

    actual fun getRedirectUri(provider: CloudProviderType): String {
        // For desktop, use localhost redirect
        return when (provider) {
            CloudProviderType.GOOGLE_DRIVE -> "http://localhost:8080/oauth2callback"
            CloudProviderType.DROPBOX -> "http://localhost:8080/oauth2callback/dropbox"
        }
    }

    actual suspend fun startNativeGoogleAuth(): String? = null
}
