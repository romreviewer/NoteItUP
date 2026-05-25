package com.romreviewertools.noteitup.data.cloud

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.romreviewertools.noteitup.BuildConfig

/**
 * Android OAuth handler using system browser.
 * The app should handle the redirect URI via an intent filter in AndroidManifest.
 */
actual class OAuthHandler(private val context: Context) {

    companion object {
        private const val TAG = "OAuthHandler"
    }

    actual suspend fun openAuthUrl(authUrl: String) {
        Log.d(TAG, "openAuthUrl: ${authUrl.take(80)}...")
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

    actual suspend fun startNativeGoogleAuth(): NativeAuthResult {
        Log.d(TAG, "startNativeGoogleAuth: clientId=${BuildConfig.GOOGLE_CLIENT_ID.take(10)}...")
        if (BuildConfig.GOOGLE_CLIENT_ID.isBlank()) {
            Log.e(TAG, "GOOGLE_CLIENT_ID is blank — check local.properties")
            return NativeAuthResult.Failed("GOOGLE_CLIENT_ID is blank — set it in local.properties to the Web OAuth client ID.")
        }
        val result = GoogleDriveAuthHelper.authorize(BuildConfig.GOOGLE_CLIENT_ID)
        Log.d(TAG, "startNativeGoogleAuth result: $result")
        return result
    }

    actual suspend fun startNativeDropboxAuth(): NativeAuthResult {
        Log.d(TAG, "startNativeDropboxAuth: appKey=${BuildConfig.DROPBOX_APP_KEY.take(6)}...")
        if (BuildConfig.DROPBOX_APP_KEY.isBlank()) {
            Log.e(TAG, "DROPBOX_APP_KEY is blank — check local.properties")
            return NativeAuthResult.Failed("DROPBOX_APP_KEY is blank — set it in local.properties.")
        }
        val result = DropboxAuthHelper.authorize(BuildConfig.DROPBOX_APP_KEY)
        Log.d(TAG, "startNativeDropboxAuth result: $result")
        return result
    }
}
