package com.romreviewertools.noteitup.util

actual object PlatformCapabilities {
    actual fun hasCameraSupport(): Boolean = true

    actual fun hasLocationSupport(): Boolean = true
}
