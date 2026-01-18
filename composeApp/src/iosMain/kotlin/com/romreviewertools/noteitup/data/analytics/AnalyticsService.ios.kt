package com.romreviewertools.noteitup.data.analytics

/**
 * iOS implementation of AnalyticsService.
 * Currently a stub - could be implemented using Firebase iOS SDK.
 */
actual class AnalyticsService {
    /**
     * Log an analytics event.
     * iOS implementation is a stub for now.
     * TODO: Implement using Firebase iOS SDK
     */
    actual fun logEvent(event: AnalyticsEvent) {
        // Stub implementation
        // Could be implemented using Firebase iOS SDK
        println("Analytics (iOS stub): ${event.name} - ${event.params}")
    }

    /**
     * Set a user property for segmentation.
     */
    actual fun setUserProperty(name: String, value: String) {
        // Stub implementation
        println("Analytics (iOS stub): setUserProperty($name, $value)")
    }

    /**
     * Analytics is not yet fully implemented on iOS.
     */
    actual fun isAvailable(): Boolean = false
}