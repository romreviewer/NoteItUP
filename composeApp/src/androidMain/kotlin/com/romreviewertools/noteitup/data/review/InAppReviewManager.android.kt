package com.romreviewertools.noteitup.data.review

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory
import java.lang.ref.WeakReference

/**
 * Android implementation of InAppReviewManager using Google Play In-App Review API.
 */
actual class InAppReviewManager(
    context: Context
) {
    private val reviewManager = ReviewManagerFactory.create(context)
    private var activityRef: WeakReference<Activity>? = null

    /**
     * Set the current activity for launching the review flow.
     * Must be called before requestReview().
     */
    fun setActivity(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    /**
     * Clear the activity reference (call in onDestroy).
     */
    fun clearActivity() {
        activityRef = null
    }

    /**
     * Request to show the in-app review dialog.
     * Note: Google Play controls when the dialog is actually shown.
     * The dialog may not appear on every request due to quota limits.
     * Make sure to call setActivity() before calling this method.
     * This is fire-and-forget - we cannot know if the user actually reviewed.
     */
    actual fun requestReview() {
        val activity = activityRef?.get() ?: return
        if (activity.isFinishing) return

        val request = reviewManager.requestReviewFlow()
        request.addOnSuccessListener { reviewInfo ->
            // Re-check activity is still valid before launching
            val currentActivity = activityRef?.get()
            if (currentActivity != null && !currentActivity.isFinishing) {
                reviewManager.launchReviewFlow(currentActivity, reviewInfo)
            }
        }
        // No need to handle failure - it's fire-and-forget
    }

    /**
     * In-app review is available on Android.
     */
    actual fun isAvailable(): Boolean = true
}