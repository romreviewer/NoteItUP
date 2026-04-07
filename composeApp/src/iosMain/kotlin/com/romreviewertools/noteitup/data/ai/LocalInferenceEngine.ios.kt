package com.romreviewertools.noteitup.data.ai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * iOS stub implementation of LocalInferenceEngine.
 * LiteRT-LM Swift API is not yet stable, so local AI is not available on iOS.
 */
actual class LocalInferenceEngine {

    actual suspend fun loadModel(modelPath: String) {
        throw UnsupportedOperationException("Local AI is not yet available on iOS. LiteRT-LM Swift API is coming soon.")
    }

    actual fun unloadModel() {
        // No-op on iOS
    }

    actual fun isModelLoaded(): Boolean = false

    actual suspend fun generateResponse(
        systemPrompt: String,
        userMessage: String,
        temperature: Float,
        maxTokens: Int
    ): String {
        throw UnsupportedOperationException("Local AI is not yet available on iOS.")
    }

    actual fun generateResponseStream(
        systemPrompt: String,
        userMessage: String,
        temperature: Float,
        maxTokens: Int
    ): Flow<String> = flow {
        throw UnsupportedOperationException("Local AI is not yet available on iOS.")
    }
}
