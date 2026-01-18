package com.romreviewertools.noteitup.data.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Android implementation of AnalyticsService using Firebase Analytics.
 */
actual class AnalyticsService(
    context: Context
) {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    /**
     * Log an analytics event to Firebase.
     */
    actual fun logEvent(event: AnalyticsEvent) {
        val bundle = Bundle().apply {
            event.params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }
        firebaseAnalytics.logEvent(event.name, bundle)
    }

    /**
     * Set a user property for segmentation.
     */
    actual fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }

    /**
     * Analytics is available on Android via Firebase.
     */
    actual fun isAvailable(): Boolean = true
}