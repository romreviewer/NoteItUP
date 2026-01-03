package com.romreviewertools.noteitup.presentation.screens.cloudsync

import com.romreviewertools.noteitup.OAuthCallbackHolder
import com.romreviewertools.noteitup.data.cloud.CloudProviderType

actual fun consumePendingOAuthCallback(): OAuthCallback? {
    val callback = OAuthCallbackHolder.consumeCode() ?: return null
    val (code, isDropbox) = callback
    val provider = if (isDropbox) CloudProviderType.DROPBOX else CloudProviderType.GOOGLE_DRIVE
    return OAuthCallback(code = code, provider = provider)
}
