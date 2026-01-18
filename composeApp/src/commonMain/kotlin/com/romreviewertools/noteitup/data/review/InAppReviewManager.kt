package com.romreviewertools.noteitup.data.review

/**
 * Platform-specific in-app review manager.
 * On Android, uses Google Play In-App Review API.
 * On iOS and JVM, provides stub implementations.
 */
expect class InAppReviewManager {
    /**
     * Request to show the in-app review dialog.
     * The actual display is controlled by the platform (Google Play decides when to show).
     * This is a fire-and-forget operation - we cannot know if the user actually reviewed.
     */
    fun requestReview()

    /**
     * Check if in-app review is available on this platform.
     */
    fun isAvailable(): Boolean
}