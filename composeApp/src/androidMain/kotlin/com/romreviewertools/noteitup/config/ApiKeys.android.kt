package com.romreviewertools.noteitup.config

import com.romreviewertools.noteitup.BuildConfig

/**
 * Android implementation - reads keys from BuildConfig
 * which are injected from local.properties at build time.
 */
actual object ApiKeys {
    actual val DROPBOX_APP_KEY: String = BuildConfig.DROPBOX_APP_KEY
    actual val DROPBOX_APP_SECRET: String = BuildConfig.DROPBOX_APP_SECRET
    actual val GOOGLE_CLIENT_ID: String = BuildConfig.GOOGLE_CLIENT_ID
    actual val GOOGLE_CLIENT_SECRET: String = BuildConfig.GOOGLE_CLIENT_SECRET
}
