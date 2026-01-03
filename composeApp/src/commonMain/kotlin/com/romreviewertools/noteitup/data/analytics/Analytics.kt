package com.romreviewertools.noteitup.data.analytics

/**
 * Cross-platform analytics interface.
 * Wraps Firebase Analytics for Android and provides stubs for other platforms.
 */
expect object Analytics {
    /**
     * Log a custom event.
     */
    fun logEvent(name: String, params: Map<String, Any>? = null)

    /**
     * Log screen view.
     */
    fun logScreenView(screenName: String, screenClass: String? = null)

    /**
     * Set user property.
     */
    fun setUserProperty(name: String, value: String?)

    /**
     * Set user ID for analytics.
     */
    fun setUserId(userId: String?)
}

/**
 * Cross-platform crash reporting interface.
 * Wraps Firebase Crashlytics for Android and provides stubs for other platforms.
 */
expect object CrashReporter {
    /**
     * Log a non-fatal exception.
     */
    fun logException(throwable: Throwable)

    /**
     * Log a message for crash context.
     */
    fun log(message: String)

    /**
     * Set a custom key-value pair for crash reports.
     */
    fun setCustomKey(key: String, value: String)

    /**
     * Set user identifier for crash reports.
     */
    fun setUserId(userId: String?)

    /**
     * Record a non-fatal exception with a message.
     */
    fun recordException(message: String, throwable: Throwable? = null)
}

/**
 * Common analytics event names.
 */
object AnalyticsEvents {
    const val ENTRY_CREATED = "entry_created"
    const val ENTRY_UPDATED = "entry_updated"
    const val ENTRY_DELETED = "entry_deleted"
    const val ENTRY_VIEWED = "entry_viewed"

    const val FOLDER_CREATED = "folder_created"
    const val FOLDER_DELETED = "folder_deleted"

    const val TAG_CREATED = "tag_created"
    const val TAG_DELETED = "tag_deleted"

    const val BACKUP_CREATED = "backup_created"
    const val BACKUP_RESTORED = "backup_restored"
    const val CLOUD_SYNC_STARTED = "cloud_sync_started"
    const val CLOUD_SYNC_COMPLETED = "cloud_sync_completed"
    const val CLOUD_PROVIDER_CONNECTED = "cloud_provider_connected"
    const val CLOUD_PROVIDER_DISCONNECTED = "cloud_provider_disconnected"

    const val IMAGE_ATTACHED = "image_attached"
    const val LOCATION_TAGGED = "location_tagged"

    const val SEARCH_PERFORMED = "search_performed"
    const val EXPORT_COMPLETED = "export_completed"
    const val IMPORT_COMPLETED = "import_completed"

    const val APP_LOCK_ENABLED = "app_lock_enabled"
    const val BIOMETRIC_ENABLED = "biometric_enabled"

    const val REMINDER_ENABLED = "reminder_enabled"
    const val REMINDER_DISABLED = "reminder_disabled"
}

/**
 * Common screen names for analytics.
 */
object AnalyticsScreens {
    const val HOME = "home"
    const val EDITOR = "editor"
    const val CALENDAR = "calendar"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val STATISTICS = "statistics"
    const val CLOUD_SYNC = "cloud_sync"
    const val FOLDERS = "folders"
    const val TAGS = "tags"
    const val EXPORT = "export"
    const val SECURITY = "security"
    const val LOCK_SCREEN = "lock_screen"
}
