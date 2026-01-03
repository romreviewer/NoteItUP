package com.romreviewertools.noteitup.config

/**
 * JVM/Desktop implementation - keys can be read from
 * system properties or environment variables.
 *
 * Set via: -DDROPBOX_APP_KEY=xxx or environment variables
 */
actual object ApiKeys {
    actual val DROPBOX_APP_KEY: String = System.getProperty("DROPBOX_APP_KEY")
        ?: System.getenv("DROPBOX_APP_KEY")
        ?: ""
    actual val DROPBOX_APP_SECRET: String = System.getProperty("DROPBOX_APP_SECRET")
        ?: System.getenv("DROPBOX_APP_SECRET")
        ?: ""
    actual val GOOGLE_CLIENT_ID: String = System.getProperty("GOOGLE_CLIENT_ID")
        ?: System.getenv("GOOGLE_CLIENT_ID")
        ?: ""
    actual val GOOGLE_CLIENT_SECRET: String = System.getProperty("GOOGLE_CLIENT_SECRET")
        ?: System.getenv("GOOGLE_CLIENT_SECRET")
        ?: ""
}
