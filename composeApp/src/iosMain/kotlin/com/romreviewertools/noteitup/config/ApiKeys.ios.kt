package com.romreviewertools.noteitup.config

/**
 * iOS implementation - keys can be stored in Info.plist
 * or injected via build configuration.
 *
 * TODO: Read from Info.plist or secure storage
 */
actual object ApiKeys {
    actual val DROPBOX_APP_KEY: String = ""
    actual val DROPBOX_APP_SECRET: String = ""
    actual val GOOGLE_CLIENT_ID: String = ""
    actual val GOOGLE_CLIENT_SECRET: String = ""
}
