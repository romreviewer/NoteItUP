package com.romreviewertools.noteitup.data.analytics

/**
 * iOS implementation - stub for now.
 * TODO: Integrate Firebase iOS SDK when needed.
 */
actual object Analytics {
    actual fun logEvent(name: String, params: Map<String, Any>?) {
        // iOS Firebase integration would go here
        println("Analytics: $name - $params")
    }

    actual fun logScreenView(screenName: String, screenClass: String?) {
        println("Analytics Screen: $screenName")
    }

    actual fun setUserProperty(name: String, value: String?) {
        println("Analytics Property: $name = $value")
    }

    actual fun setUserId(userId: String?) {
        println("Analytics UserId: $userId")
    }
}

actual object CrashReporter {
    actual fun logException(throwable: Throwable) {
        println("Crash: ${throwable.message}")
    }

    actual fun log(message: String) {
        println("Crash Log: $message")
    }

    actual fun setCustomKey(key: String, value: String) {
        println("Crash Key: $key = $value")
    }

    actual fun setUserId(userId: String?) {
        println("Crash UserId: $userId")
    }

    actual fun recordException(message: String, throwable: Throwable?) {
        println("Crash Exception: $message - ${throwable?.message}")
    }
}
