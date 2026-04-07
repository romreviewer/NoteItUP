package com.romreviewertools.noteitup.data.ai

import kotlinx.coroutines.flow.Flow

/**
 * Platform-specific engine for on-device LLM inference using LiteRT-LM.
 *
 * - Android: Uses LiteRT-LM with GPU backend
 * - JVM (Desktop): Uses LiteRT-LM with CPU backend
 * - iOS: Stub (not yet supported)
 */
expect class LocalInferenceEngine {

    /**
     * Load a model from the given file path.
     * This is a heavy operation (~5-10 seconds) and should be called from a background thread.
     */
    suspend fun loadModel(modelPath: String)

    /**
     * Unload the current model and release all native resources.
     */
    fun unloadModel()

    /**
     * Check if a model is currently loaded and ready for inference.
     */
    fun isModelLoaded(): Boolean

    /**
     * Generate a complete response synchronously.
     *
     * @param systemPrompt Instructions for the model's behavior
     * @param userMessage The user's input text
     * @param temperature Sampling temperature (0.0 = deterministic, 1.0 = creative)
     * @param maxTokens Maximum number of tokens to generate
     * @return The generated text response
     */
    suspend fun generateResponse(
        systemPrompt: String,
        userMessage: String,
        temperature: Float = 0.7f,
        maxTokens: Int = 1024
    ): String

    /**
     * Generate a streaming response as a Flow of text chunks.
     *
     * @param systemPrompt Instructions for the model's behavior
     * @param userMessage The user's input text
     * @param temperature Sampling temperature (0.0 = deterministic, 1.0 = creative)
     * @param maxTokens Maximum number of tokens to generate
     * @return Flow emitting partial text as it's generated
     */
    fun generateResponseStream(
        systemPrompt: String,
        userMessage: String,
        temperature: Float = 0.7f,
        maxTokens: Int = 1024
    ): Flow<String>
}
