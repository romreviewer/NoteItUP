package com.romreviewertools.noteitup.data.analytics

/**
 * Defines all trackable analytics events in the app.
 * Events are tracked at use case level, not UI level.
 * No PII or diary content is sent to analytics.
 */
sealed class AnalyticsEvent(
    val name: String,
    val params: Map<String, Any> = emptyMap()
) {
    // ==================== Screen View Events ====================
    data object ScreenViewHome : AnalyticsEvent("screen_view_home")
    data object ScreenViewEditor : AnalyticsEvent("screen_view_editor")
    data object ScreenViewAllEntries : AnalyticsEvent("screen_view_all_entries")
    data object ScreenViewSearch : AnalyticsEvent("screen_view_search")
    data object ScreenViewTags : AnalyticsEvent("screen_view_tags")
    data object ScreenViewFolders : AnalyticsEvent("screen_view_folders")
    data object ScreenViewCalendar : AnalyticsEvent("screen_view_calendar")
    data object ScreenViewStatistics : AnalyticsEvent("screen_view_statistics")
    data object ScreenViewSettings : AnalyticsEvent("screen_view_settings")
    data object ScreenViewSecurity : AnalyticsEvent("screen_view_security")
    data object ScreenViewCloudSync : AnalyticsEvent("screen_view_cloud_sync")
    data object ScreenViewExport : AnalyticsEvent("screen_view_export")
    data object ScreenViewAISettings : AnalyticsEvent("screen_view_ai_settings")
    data object ScreenViewBrainstorm : AnalyticsEvent("screen_view_brainstorm")

    // ==================== Entry Events ====================
    data object EntryCreated : AnalyticsEvent("entry_created")
    data object EntrySaved : AnalyticsEvent("entry_saved")
    data object EntryDeleted : AnalyticsEvent("entry_deleted")
    data object EntryFavorited : AnalyticsEvent("entry_favorited")
    data object EntryUnfavorited : AnalyticsEvent("entry_unfavorited")

    // ==================== Mood Events ====================
    data class MoodSelected(val moodType: String) : AnalyticsEvent(
        name = "mood_selected",
        params = mapOf("mood_type" to moodType)
    )

    // ==================== AI Events ====================
    data class AIFeatureUsed(val improvementType: String) : AnalyticsEvent(
        name = "ai_feature_used",
        params = mapOf("improvement_type" to improvementType)
    )
    data object AISuggestionAccepted : AnalyticsEvent("ai_suggestion_accepted")
    data object AISuggestionDismissed : AnalyticsEvent("ai_suggestion_dismissed")
    data object BrainstormSessionStarted : AnalyticsEvent("brainstorm_session_started")
    data object BrainstormMessageSent : AnalyticsEvent("brainstorm_message_sent")

    // ==================== AI Settings Events ====================
    data class AIProviderSelected(val provider: String) : AnalyticsEvent(
        name = "ai_provider_selected",
        params = mapOf("provider" to provider)
    )
    data object AISettingsConfigured : AnalyticsEvent("ai_settings_configured")

    // ==================== Export/Import Events ====================
    data class ExportStarted(val format: String) : AnalyticsEvent(
        name = "export_started",
        params = mapOf("format" to format)
    )
    data class ExportCompleted(val format: String, val count: Int) : AnalyticsEvent(
        name = "export_completed",
        params = mapOf("format" to format, "count" to count)
    )
    data class ImportStarted(val source: String) : AnalyticsEvent(
        name = "import_started",
        params = mapOf("source" to source)
    )
    data class ImportCompleted(val source: String, val count: Int) : AnalyticsEvent(
        name = "import_completed",
        params = mapOf("source" to source, "count" to count)
    )

    // ==================== Cloud Sync Events ====================
    data class CloudProviderConnected(val provider: String) : AnalyticsEvent(
        name = "cloud_provider_connected",
        params = mapOf("provider" to provider)
    )
    data class CloudProviderDisconnected(val provider: String) : AnalyticsEvent(
        name = "cloud_provider_disconnected",
        params = mapOf("provider" to provider)
    )
    data class CloudSyncStarted(val provider: String) : AnalyticsEvent(
        name = "cloud_sync_started",
        params = mapOf("provider" to provider)
    )
    data class CloudSyncCompleted(val provider: String, val success: Boolean) : AnalyticsEvent(
        name = "cloud_sync_completed",
        params = mapOf("provider" to provider, "success" to success)
    )

    // ==================== Settings Events ====================
    data class ThemeChanged(val theme: String) : AnalyticsEvent(
        name = "theme_changed",
        params = mapOf("theme" to theme)
    )
    data class FontSizeChanged(val size: String) : AnalyticsEvent(
        name = "font_size_changed",
        params = mapOf("size" to size)
    )

    // ==================== Organization Events ====================
    data object FolderCreated : AnalyticsEvent("folder_created")
    data object FolderDeleted : AnalyticsEvent("folder_deleted")
    data object TagCreated : AnalyticsEvent("tag_created")
    data object TagDeleted : AnalyticsEvent("tag_deleted")
    data class TagAssigned(val entryCount: Int) : AnalyticsEvent(
        name = "tag_assigned",
        params = mapOf("entry_count" to entryCount)
    )
    data class FolderAssigned(val entryCount: Int) : AnalyticsEvent(
        name = "folder_assigned",
        params = mapOf("entry_count" to entryCount)
    )

    // ==================== Search Events ====================
    data object SearchPerformed : AnalyticsEvent("search_performed")
    data class SearchResultsFound(val count: Int) : AnalyticsEvent(
        name = "search_results_found",
        params = mapOf("count" to count)
    )

    // ==================== Security Events ====================
    data class SecurityEnabled(val type: String) : AnalyticsEvent(
        name = "security_enabled",
        params = mapOf("type" to type)
    )
    data class SecurityDisabled(val type: String) : AnalyticsEvent(
        name = "security_disabled",
        params = mapOf("type" to type)
    )
    data object AppUnlocked : AnalyticsEvent("app_unlocked")
    data object AppLockFailed : AnalyticsEvent("app_lock_failed")

    // ==================== Media Events ====================
    data object ImageAttached : AnalyticsEvent("image_attached")
    data object ImageRemoved : AnalyticsEvent("image_removed")
    data object LocationAttached : AnalyticsEvent("location_attached")
    data object LocationRemoved : AnalyticsEvent("location_removed")

    // ==================== Calendar Events ====================
    data class CalendarDateSelected(val hasEntries: Boolean) : AnalyticsEvent(
        name = "calendar_date_selected",
        params = mapOf("has_entries" to hasEntries)
    )
    data class CalendarMonthChanged(val direction: String) : AnalyticsEvent(
        name = "calendar_month_changed",
        params = mapOf("direction" to direction)
    )

    // ==================== Statistics Events ====================
    data object StatisticsViewed : AnalyticsEvent("statistics_viewed")

    // ==================== Review Events ====================
    data object ReviewPromptShown : AnalyticsEvent("review_prompt_shown")
    data object ReviewCompleted : AnalyticsEvent("review_completed")
    data object ReviewDismissed : AnalyticsEvent("review_dismissed")

    // ==================== Error Events ====================
    data class ErrorOccurred(val errorType: String, val screen: String) : AnalyticsEvent(
        name = "error_occurred",
        params = mapOf("error_type" to errorType, "screen" to screen)
    )
}