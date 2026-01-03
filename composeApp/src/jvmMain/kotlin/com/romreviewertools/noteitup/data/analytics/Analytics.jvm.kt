package com.romreviewertools.noteitup.data.analytics

/**
 * JVM/Desktop implementation - logging only.
 * Firebase is not available on desktop.
 */
actual object Analytics {
    actual fun logEvent(name: String, params: Map<String, Any>?) {
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
        System.err.println("Crash: ${throwable.message}")
        throwable.printStackTrace()
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
        System.err.println("Crash Exception: $message")
        throwable?.printStackTrace()
    }
}
