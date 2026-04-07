package com.romreviewertools.noteitup.data.ai

import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

/**
 * JVM (Desktop) implementation of LocalInferenceEngine using LiteRT-LM with CPU backend.
 */
actual class LocalInferenceEngine {

    private var engine: Engine? = null

    actual suspend fun loadModel(modelPath: String) = withContext(Dispatchers.IO) {
        // Release any existing engine
        unloadModel()

        val cacheDir = File(System.getProperty("user.home"), ".noteitup/cache").apply { mkdirs() }

        val config = EngineConfig(
            modelPath = modelPath,
            backend = Backend.CPU(),
            cacheDir = cacheDir.absolutePath
        )

        try {
            engine = Engine(config).also { it.initialize() }
            println("LocalInferenceEngine: Model loaded with CPU backend")
        } catch (e: Exception) {
            println("LocalInferenceEngine: Failed to load model: ${e.message}")
            throw Exception("Failed to load model: ${e.message}")
        }
    }

    actual fun unloadModel() {
        try {
            engine?.close()
        } catch (e: Exception) {
            println("LocalInferenceEngine: Error closing engine: ${e.message}")
        }
        engine = null
    }

    actual fun isModelLoaded(): Boolean = engine != null

    actual suspend fun generateResponse(
        systemPrompt: String,
        userMessage: String,
        temperature: Float,
        maxTokens: Int
    ): String = withContext(Dispatchers.IO) {
        val currentEngine = engine ?: throw Exception("Model not loaded. Download and load the model first.")

        val conversationConfig = ConversationConfig(
            systemInstruction = if (systemPrompt.isNotBlank()) Contents.of(systemPrompt) else null,
            samplerConfig = SamplerConfig(
                temperature = temperature.toDouble(),
                topK = 40,
                topP = 0.95
            )
        )

        currentEngine.createConversation(conversationConfig).use { conversation ->
            conversation.sendMessage(userMessage).toString()
        }
    }

    actual fun generateResponseStream(
        systemPrompt: String,
        userMessage: String,
        temperature: Float,
        maxTokens: Int
    ): Flow<String> = flow {
        val currentEngine = engine ?: throw Exception("Model not loaded. Download and load the model first.")

        val conversationConfig = ConversationConfig(
            systemInstruction = if (systemPrompt.isNotBlank()) Contents.of(systemPrompt) else null,
            samplerConfig = SamplerConfig(
                temperature = temperature.toDouble(),
                topK = 40,
                topP = 0.95
            )
        )

        currentEngine.createConversation(conversationConfig).use { conversation ->
            conversation.sendMessageAsync(userMessage).collect { message ->
                emit(message.toString())
            }
        }
    }.flowOn(Dispatchers.IO)
}
