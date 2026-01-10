package com.romreviewertools.noteitup.util

actual object PlatformCapabilities {
    actual fun hasCameraSupport(): Boolean = false

    actual fun hasLocationSupport(): Boolean = false
}
