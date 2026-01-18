package com.romreviewertools.noteitup.data.review

/**
 * iOS implementation of InAppReviewManager.
 * Currently a stub - could be implemented using StoreKit SKStoreReviewController.
 */
actual class InAppReviewManager {
    /**
     * Request to show the in-app review dialog.
     * iOS implementation is a stub for now.
     * TODO: Implement using StoreKit SKStoreReviewController
     */
    actual fun requestReview() {
        // Stub implementation
        // Could be implemented using:
        // SKStoreReviewController.requestReview()
    }

    /**
     * In-app review is not yet implemented on iOS.
     */
    actual fun isAvailable(): Boolean = false
}