package com.romreviewertools.noteitup.data.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

actual object Analytics {
    private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }

    actual fun logEvent(name: String, params: Map<String, Any>?) {
        val bundle = params?.toBundle()
        firebaseAnalytics.logEvent(name, bundle)
    }

    actual fun logScreenView(screenName: String, screenClass: String?) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            screenClass?.let { putString(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    actual fun setUserProperty(name: String, value: String?) {
        firebaseAnalytics.setUserProperty(name, value)
    }

    actual fun setUserId(userId: String?) {
        firebaseAnalytics.setUserId(userId)
    }

    private fun Map<String, Any>.toBundle(): Bundle {
        return Bundle().apply {
            forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Float -> putFloat(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }
    }
}

actual object CrashReporter {
    private val crashlytics by lazy { Firebase.crashlytics }

    actual fun logException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    actual fun log(message: String) {
        crashlytics.log(message)
    }

    actual fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    actual fun setUserId(userId: String?) {
        userId?.let { crashlytics.setUserId(it) }
    }

    actual fun recordException(message: String, throwable: Throwable?) {
        crashlytics.log(message)
        throwable?.let { crashlytics.recordException(it) }
            ?: crashlytics.recordException(Exception(message))
    }
}
