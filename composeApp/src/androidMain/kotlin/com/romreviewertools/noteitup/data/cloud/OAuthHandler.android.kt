package com.romreviewertools.noteitup.data.cloud

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Android OAuth handler using system browser.
 * The app should handle the redirect URI via an intent filter in AndroidManifest.
 */
actual class OAuthHandler(private val context: Context) {

    actual suspend fun openAuthUrl(authUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    actual fun getRedirectUri(provider: CloudProviderType): String {
        return when (provider) {
            CloudProviderType.GOOGLE_DRIVE -> "com.romreviewertools.noteitup:/oauth2callback"
            CloudProviderType.DROPBOX -> "com.romreviewertools.noteitup:/oauth2callback/dropbox"
        }
    }
}
