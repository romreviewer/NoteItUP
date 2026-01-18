package com.romreviewertools.noteitup.data.analytics

/**
 * JVM/Desktop implementation of AnalyticsService.
 * Analytics is not applicable for desktop applications.
 * All methods are no-ops.
 */
actual class AnalyticsService {
    /**
     * Log an analytics event.
     * Desktop implementation is a no-op.
     */
    actual fun logEvent(event: AnalyticsEvent) {
        // No-op for desktop
        // Could log to console for debugging if needed
    }

    /**
     * Set a user property for segmentation.
     * Desktop implementation is a no-op.
     */
    actual fun setUserProperty(name: String, value: String) {
        // No-op for desktop
    }

    /**
     * Analytics is not available on desktop.
     */
    actual fun isAvailable(): Boolean = false
}