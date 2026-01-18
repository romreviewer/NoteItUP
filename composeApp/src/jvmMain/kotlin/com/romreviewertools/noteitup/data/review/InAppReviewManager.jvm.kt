package com.romreviewertools.noteitup.data.review

/**
 * JVM/Desktop implementation of InAppReviewManager.
 * In-app review is not applicable for desktop applications.
 */
actual class InAppReviewManager {
    /**
     * Request to show the in-app review dialog.
     * Not applicable for desktop - does nothing.
     */
    actual fun requestReview() {
        // In-app review is not applicable for desktop
    }

    /**
     * In-app review is not available on desktop.
     */
    actual fun isAvailable(): Boolean = false
}