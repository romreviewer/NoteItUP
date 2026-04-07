package com.romreviewertools.noteitup.util

/**
 * Platform-specific capabilities
 */
expect object PlatformCapabilities {
    /**
     * Returns true if the platform has camera support
     */
    fun hasCameraSupport(): Boolean

    /**
     * Returns true if the platform has GPS/location support
     */
    fun hasLocationSupport(): Boolean

    /**
     * Returns true if the platform supports local on-device AI inference (LiteRT-LM)
     */
    fun hasLocalAISupport(): Boolean
}
