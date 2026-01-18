package com.romreviewertools.noteitup.data.preferences

import kotlinx.coroutines.flow.Flow

expect class PreferencesStorage {
    fun getString(key: String, defaultValue: String): Flow<String>
    suspend fun putString(key: String, value: String)
}

object PreferencesKeys {
    const val THEME_MODE = "theme_mode"
    const val ACCENT_COLOR = "accent_color"
    const val FONT_SIZE = "font_size"
    const val REMINDER_ENABLED = "reminder_enabled"
    const val REMINDER_HOUR = "reminder_hour"
    const val REMINDER_MINUTE = "reminder_minute"
    const val REMINDER_DAYS = "reminder_days"
    const val LOCK_TYPE = "lock_type"
    const val AUTO_LOCK_TIMEOUT = "auto_lock_timeout"
    const val PIN_HASH = "pin_hash"
    const val BIOMETRIC_ENABLED = "biometric_enabled"
    const val LAST_ACTIVE_TIME = "last_active_time"

    // Cloud sync settings
    const val CLOUD_AUTO_SYNC_ENABLED = "cloud_auto_sync_enabled"
    const val CLOUD_AUTO_SYNC_INTERVAL = "cloud_auto_sync_interval"
    const val CLOUD_SYNC_WIFI_ONLY = "cloud_sync_wifi_only"
    const val CLOUD_LAST_SYNC_TIME = "cloud_last_sync_time"
    const val CLOUD_LAST_LOCAL_MODIFICATION = "cloud_last_local_modification"
    const val CLOUD_SYNC_PASSWORD_HASH = "cloud_sync_password_hash"

    // In-app review settings
    const val REVIEW_ENTRIES_SAVED_COUNT = "review_entries_saved_count"
    const val REVIEW_HAS_RATED = "review_has_rated"
    const val REVIEW_LAST_PROMPT_TIME = "review_last_prompt_time"
    const val REVIEW_DISMISS_COUNT = "review_dismiss_count"
    const val REVIEW_NEVER_ASK_AGAIN = "review_never_ask_again"
}
