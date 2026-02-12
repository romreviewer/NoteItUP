package com.romreviewertools.noteitup.data.cloud

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.romreviewertools.noteitup.BuildConfig

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
            CloudProviderType.GOOGLE_DRIVE -> "com.romreviewertools.noteitup://oauth2callback"
            // Dropbox OAuth - using db-APP_KEY format (key from BuildConfig)
            CloudProviderType.DROPBOX -> "db-${BuildConfig.DROPBOX_APP_KEY}://2/token"
        }
    }

    actual suspend fun startNativeGoogleAuth(): String? {
        return GoogleDriveAuthHelper.authorize(BuildConfig.GOOGLE_CLIENT_ID)
    }
}
