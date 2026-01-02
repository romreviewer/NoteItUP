package com.romreviewertools.noteitup.data.cloud

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

/**
 * iOS OAuth handler using system browser.
 * The app should handle the redirect URI via URL scheme in Info.plist.
 */
actual class OAuthHandler {

    actual suspend fun openAuthUrl(authUrl: String) {
        val url = NSURL.URLWithString(authUrl) ?: return
        UIApplication.sharedApplication.openURL(url)
    }

    actual fun getRedirectUri(provider: CloudProviderType): String {
        return when (provider) {
            CloudProviderType.GOOGLE_DRIVE -> "com.romreviewertools.noteitup:/oauth2callback"
            CloudProviderType.DROPBOX -> "com.romreviewertools.noteitup:/oauth2callback/dropbox"
        }
    }
}
