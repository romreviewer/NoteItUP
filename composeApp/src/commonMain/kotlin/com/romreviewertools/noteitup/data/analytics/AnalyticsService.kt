package com.romreviewertools.noteitup.data.analytics

/**
 * Platform-specific analytics service.
 * On Android and iOS, uses Firebase Analytics.
 * On JVM/Desktop, provides a stub implementation (no-op).
 */
expect class AnalyticsService {
    /**
     * Log an analytics event.
     */
    fun logEvent(event: AnalyticsEvent)

    /**
     * Set a user property for segmentation.
     */
    fun setUserProperty(name: String, value: String)

    /**
     * Check if analytics is available on this platform.
     */
    fun isAvailable(): Boolean
}