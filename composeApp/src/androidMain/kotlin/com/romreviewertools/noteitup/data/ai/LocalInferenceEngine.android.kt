package com.romreviewertools.noteitup.data.ai

import android.content.Context
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

/**
 * Android implementation of LocalInferenceEngine using LiteRT-LM with GPU acceleration.
 * Falls back to CPU if GPU is not available.
 */
actual class LocalInferenceEngine(private val context: Context) {

    private var engine: Engine? = null
    private var useGpu: Boolean = true

    actual suspend fun loadModel(modelPath: String) = withContext(Dispatchers.IO) {
        // Release any existing engine
        unloadModel()

        try {
            // Try GPU first for best performance
            val config = EngineConfig(
                modelPath = modelPath,
                backend = Backend.GPU(),
                cacheDir = context.cacheDir.path
            )
            engine = Engine(config).also { it.initialize() }
            useGpu = true
            println("LocalInferenceEngine: Model loaded with GPU backend")
        } catch (e: Exception) {
            println("LocalInferenceEngine: GPU failed (${e.message}), falling back to CPU")
            try {
                // Fallback to CPU
                val cpuConfig = EngineConfig(
                    modelPath = modelPath,
                    backend = Backend.CPU(),
                    cacheDir = context.cacheDir.path
                )
                engine = Engine(cpuConfig).also { it.initialize() }
                useGpu = false
                println("LocalInferenceEngine: Model loaded with CPU backend")
            } catch (cpuError: Exception) {
                println("LocalInferenceEngine: CPU also failed: ${cpuError.message}")
                throw Exception("Failed to load model: ${cpuError.message}")
            }
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
