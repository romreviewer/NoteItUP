package com.romreviewertools.noteitup.presentation.screens.cloudsync

import com.romreviewertools.noteitup.OAuthCallbackHolder
import com.romreviewertools.noteitup.data.cloud.CloudProviderType

actual fun consumePendingOAuthCallback(): OAuthCallback? {
    val code = OAuthCallbackHolder.consumeCode() ?: return null
    return OAuthCallback(code = code, provider = CloudProviderType.GOOGLE_DRIVE)
}
