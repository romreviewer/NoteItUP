package com.romreviewertools.noteitup.data.ai

import com.romreviewertools.noteitup.data.repository.AISettingsRepository
import com.romreviewertools.noteitup.domain.model.AIProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

/**
 * Service for interacting with AI providers using OpenAI-compatible APIs
 * and local on-device inference via LiteRT-LM.
 */
class AIService(
    private val httpClient: HttpClient,
    private val aiSettingsRepository: AISettingsRepository,
    private val localInferenceEngine: LocalInferenceEngine,
    private val modelDownloadManager: ModelDownloadManager
) {

    /**
     * Test connection to AI provider (or test local model)
     */
    suspend fun testConnection(): Result<String> {
        return try {
            val settings = aiSettingsRepository.aiSettings.firstOrNull()
                ?: return Result.failure(Exception("AI settings not found"))

            // Local provider: test on-device inference
            if (settings.selectedProvider == AIProvider.LOCAL_GEMMA) {
                return testLocalModel()
            }

            if (settings.apiKey.isBlank()) {
                return Result.failure(Exception("API key is required"))
            }

            // Send a minimal test request
            val request = ChatCompletionRequest(
                model = getDefaultModel(settings.selectedProvider),
                messages = listOf(
                    ChatMessage(role = "user", content = "Hello")
                ),
                maxTokens = 5
            )

            val response = makeRequest(
                provider = settings.selectedProvider,
                apiKey = settings.apiKey,
                request = request
            )

            Result.success("Connection successful!")
        } catch (e: Exception) {
            // Print detailed error for debugging
            println("AI Service Error: ${e::class.simpleName}: ${e.message}")
            e.printStackTrace()
            Result.failure(Exception("Connection failed: ${e.message ?: e::class.simpleName}"))
        }
    }

    /**
     * Test local model by running a minimal inference.
     * Auto-loads the model if downloaded but not in memory.
     */
    private suspend fun testLocalModel(): Result<String> {
        return try {
            if (!localInferenceEngine.isModelLoaded()) {
                val modelPath = modelDownloadManager.getModelPath()
                    ?: return Result.failure(Exception("Model not downloaded. Download it first."))
                localInferenceEngine.loadModel(modelPath)
            }
            val response = localInferenceEngine.generateResponse(
                systemPrompt = "",
                userMessage = "Hello",
                maxTokens = 10
            )
            Result.success("Local model working! Response: ${response.take(50)}")
        } catch (e: Exception) {
            Result.failure(Exception("Local model test failed: ${e.message}"))
        }
    }

    /**
     * Improve text using specified improvement type.
     * @param onModelLoading Called when the local model starts loading for the first time in this session.
     */
    suspend fun improveText(
        text: String,
        improvementType: ImprovementType,
        onModelLoading: (() -> Unit)? = null
    ): Result<String> {
        return try {
            val settings = aiSettingsRepository.aiSettings.firstOrNull()
                ?: return Result.failure(Exception("AI settings not configured"))

            if (!settings.enabled) {
                return Result.failure(Exception("AI features are disabled"))
            }

            // Local provider: use on-device inference
            if (settings.selectedProvider == AIProvider.LOCAL_GEMMA) {
                return makeLocalRequest(
                    systemPrompt = improvementType.systemPrompt,
                    userMessage = text,
                    onModelLoading = onModelLoading
                )
            }

            // Cloud providers: require API key
            if (settings.apiKey.isBlank()) {
                return Result.failure(Exception("API key not configured"))
            }

            val request = ChatCompletionRequest(
                model = settings.selectedModel.ifBlank {
                    getDefaultModel(settings.selectedProvider)
                },
                messages = listOf(
                    ChatMessage(
                        role = "system",
                        content = improvementType.systemPrompt
                    ),
                    ChatMessage(
                        role = "user",
                        content = text
                    )
                ),
                temperature = 0.7,
                stream = false
            )

            val response = makeRequest(
                provider = settings.selectedProvider,
                apiKey = settings.apiKey,
                request = request
            )

            val improvedText = response.choices.firstOrNull()?.message?.content
                ?: return Result.failure(Exception("No response from AI"))

            Result.success(improvedText.trim())
        } catch (e: Exception) {
            Result.failure(Exception("Failed to improve text: ${e.message}"))
        }
    }

    /**
     * Streaming text improvement for local on-device model.
     * Returns a Flow that emits partial text chunks as they are generated.
     */
    fun improveTextStream(
        text: String,
        improvementType: ImprovementType,
        onModelLoading: (() -> Unit)? = null
    ): Flow<String> = flow {
        val settings = aiSettingsRepository.aiSettings.firstOrNull()
            ?: throw Exception("AI settings not configured")

        if (!settings.enabled) {
            throw Exception("AI features are disabled")
        }

        if (settings.selectedProvider != AIProvider.LOCAL_GEMMA) {
            // For non-local providers, fall back to full response
            val result = improveText(text, improvementType, onModelLoading)
            result.fold(
                onSuccess = { emit(it) },
                onFailure = { throw it }
            )
            return@flow
        }

        // Ensure model is loaded
        if (!localInferenceEngine.isModelLoaded()) {
            val modelPath = modelDownloadManager.getModelPath()
            if (modelPath != null) {
                onModelLoading?.invoke()
                localInferenceEngine.loadModel(modelPath)
                println("AIService: Auto-loaded local model from: $modelPath")
            } else {
                throw Exception("AI model not downloaded. Go to AI Settings to download the Gemma 4 model.")
            }
        }

        localInferenceEngine.generateResponseStream(
            systemPrompt = improvementType.systemPrompt,
            userMessage = text
        ).collect { chunk ->
            emit(chunk)
        }
    }

    /**
     * Multi-turn chat conversation with AI.
     * @param onModelLoading Called when the local model starts loading for the first time in this session.
     */
    suspend fun chat(
        systemPrompt: String,
        messages: List<ChatMessage>,
        onModelLoading: (() -> Unit)? = null
    ): Result<String> {
        return try {
            val settings = aiSettingsRepository.aiSettings.firstOrNull()
                ?: return Result.failure(Exception("AI settings not configured"))

            if (!settings.enabled) {
                return Result.failure(Exception("AI features are disabled"))
            }

            // Local provider: use on-device inference
            if (settings.selectedProvider == AIProvider.LOCAL_GEMMA) {
                // For chat, combine conversation history into a single prompt
                val userMessage = messages.lastOrNull { it.role == "user" }?.content
                    ?: return Result.failure(Exception("No user message found"))
                return makeLocalRequest(
                    systemPrompt = systemPrompt,
                    userMessage = userMessage,
                    onModelLoading = onModelLoading
                )
            }

            // Cloud providers: require API key
            if (settings.apiKey.isBlank()) {
                return Result.failure(Exception("API key not configured"))
            }

            val allMessages = listOf(
                ChatMessage(role = "system", content = systemPrompt)
            ) + messages

            val request = ChatCompletionRequest(
                model = settings.selectedModel.ifBlank {
                    getDefaultModel(settings.selectedProvider)
                },
                messages = allMessages,
                temperature = 0.8,
                stream = false
            )

            val response = makeRequest(
                provider = settings.selectedProvider,
                apiKey = settings.apiKey,
                request = request
            )

            val responseText = response.choices.firstOrNull()?.message?.content
                ?: return Result.failure(Exception("No response from AI"))

            Result.success(responseText.trim())
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get response: ${e.message}"))
        }
    }

    /**
     * Streaming chat for local on-device model.
     * Returns a Flow that emits partial text chunks as they are generated.
     * @param onModelLoading Called when the local model starts loading for the first time in this session.
     */
    fun chatStream(
        systemPrompt: String,
        messages: List<ChatMessage>,
        onModelLoading: (() -> Unit)? = null
    ): Flow<String> = flow {
        val settings = aiSettingsRepository.aiSettings.firstOrNull()
            ?: throw Exception("AI settings not configured")

        if (!settings.enabled) {
            throw Exception("AI features are disabled")
        }

        if (settings.selectedProvider != AIProvider.LOCAL_GEMMA) {
            // For non-local providers, fall back to full response
            val result = chat(systemPrompt, messages, onModelLoading)
            result.fold(
                onSuccess = { emit(it) },
                onFailure = { throw it }
            )
            return@flow
        }

        val userMessage = messages.lastOrNull { it.role == "user" }?.content
            ?: throw Exception("No user message found")

        // Ensure model is loaded
        if (!localInferenceEngine.isModelLoaded()) {
            val modelPath = modelDownloadManager.getModelPath()
            if (modelPath != null) {
                onModelLoading?.invoke()
                localInferenceEngine.loadModel(modelPath)
                println("AIService: Auto-loaded local model from: $modelPath")
            } else {
                throw Exception("AI model not downloaded. Go to AI Settings to download the Gemma 4 model.")
            }
        }

        localInferenceEngine.generateResponseStream(
            systemPrompt = systemPrompt,
            userMessage = userMessage
        ).collect { chunk ->
            emit(chunk)
        }
    }

    /**
     * Make a local inference request using on-device model.
     *
     * If the model is downloaded but not loaded, it will be auto-loaded on first use.
     * The caller's loading indicator (spinner) will be visible during the load (~5-10s).
     *
     * @param onModelLoading Optional callback invoked when model loading starts,
     *        so the caller can show a "Loading AI model..." message.
     */
    private suspend fun makeLocalRequest(
        systemPrompt: String,
        userMessage: String,
        onModelLoading: (() -> Unit)? = null
    ): Result<String> {
        return try {
            // Lazy auto-load: if model is downloaded but not in memory, load it now
            if (!localInferenceEngine.isModelLoaded()) {
                val modelPath = modelDownloadManager.getModelPath()
                if (modelPath != null) {
                    // Model downloaded but not loaded -- auto-load now
                    onModelLoading?.invoke()
                    try {
                        localInferenceEngine.loadModel(modelPath)
                        println("AIService: Auto-loaded local model from: $modelPath")
                    } catch (e: Exception) {
                        return Result.failure(Exception("Failed to load AI model: ${e.message}"))
                    }
                } else {
                    // Model not downloaded at all
                    return Result.failure(
                        Exception("AI model not downloaded. Go to AI Settings to download the Gemma 4 model.")
                    )
                }
            }

            val response = localInferenceEngine.generateResponse(
                systemPrompt = systemPrompt,
                userMessage = userMessage
            )
            Result.success(response.trim())
        } catch (e: Exception) {
            Result.failure(Exception("Local AI error: ${e.message}"))
        }
    }

    /**
     * Make HTTP request to AI provider
     */
    private suspend fun makeRequest(
        provider: AIProvider,
        apiKey: String,
        request: ChatCompletionRequest
    ): ChatCompletionResponse {
        // Special handling for Gemini
        if (provider == AIProvider.GEMINI) {
            return makeGeminiRequest(apiKey, request)
        }

        val url = buildApiUrl(provider, apiKey)

        return httpClient.post(url) {
            contentType(ContentType.Application.Json)
            headers {
                when (provider) {
                    AIProvider.OPENAI,
                    AIProvider.GROQ,
                    AIProvider.TOGETHER,
                    AIProvider.OPENROUTER -> {
                        append(HttpHeaders.Authorization, "Bearer $apiKey")
                    }
                    AIProvider.ANTHROPIC -> {
                        append("x-api-key", apiKey)
                        append("anthropic-version", "2023-06-01")
                    }
                    AIProvider.GEMINI -> {
                        // Handled above
                    }
                    AIProvider.LOCAL_GEMMA -> {
                        // Should never reach here - local requests are handled separately
                    }
                }
            }
            setBody(request)
        }.body()
    }

    /**
     * Make request to Google Gemini API (different format)
     */
    private suspend fun makeGeminiRequest(
        apiKey: String,
        request: ChatCompletionRequest
    ): ChatCompletionResponse {
        return try {
            val model = request.model.ifBlank { "gemini-2.0-flash" }
            val url = "${AIProvider.GEMINI.baseUrl}/models/$model:generateContent?key=$apiKey"

            println("Gemini URL: $url")

            // Convert OpenAI format to Gemini format
            val geminiRequest = GeminiRequest(
                contents = request.messages
                    .filter { it.role != "system" } // Gemini doesn't have system role
                    .map { message ->
                        GeminiContent(
                            parts = listOf(GeminiPart(text = message.content)),
                            role = when (message.role) {
                                "assistant" -> "model"
                                else -> "user"
                            }
                        )
                    }
            )

            // Add system prompt as first user message if present
            val systemMessage = request.messages.firstOrNull { it.role == "system" }
            val finalRequest = if (systemMessage != null) {
                val combinedFirstMessage = request.messages
                    .firstOrNull { it.role == "user" }
                    ?.let { "${systemMessage.content}\n\n${it.content}" }
                    ?: systemMessage.content

                GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart(text = combinedFirstMessage)),
                            role = "user"
                        )
                    )
                )
            } else {
                geminiRequest
            }

            println("Gemini Request: $finalRequest")

            val response = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(finalRequest)
            }

            println("Gemini HTTP Status: ${response.status}")

            // Try to parse as error response first
            if (response.status.value !in 200..299) {
                try {
                    val errorResponse: GeminiErrorResponse = response.body()
                    throw Exception("Gemini API error: ${errorResponse.error.message}")
                } catch (e: Exception) {
                    throw Exception("HTTP ${response.status.value}: ${response.status.description}")
                }
            }

            val geminiResponse: GeminiResponse = response.body()

            println("Gemini Response: $geminiResponse")

            // Convert Gemini response to OpenAI format
            val responseText = geminiResponse.candidates
                .firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?: throw Exception("No response from Gemini")

            ChatCompletionResponse(
                id = null,
                choices = listOf(
                    ChatChoice(
                        index = 0,
                        message = ChatMessage(
                            role = "assistant",
                            content = responseText
                        ),
                        finishReason = geminiResponse.candidates.firstOrNull()?.finishReason
                    )
                )
            )
        } catch (e: Exception) {
            println("Gemini API Error: ${e.message}")
            e.printStackTrace()
            throw Exception("Gemini API error: ${e.message}")
        }
    }

    /**
     * Build API endpoint URL for provider
     */
    private fun buildApiUrl(provider: AIProvider, apiKey: String): String {
        return when (provider) {
            AIProvider.OPENAI -> "${provider.baseUrl}/chat/completions"
            AIProvider.ANTHROPIC -> "${provider.baseUrl}/messages"
            AIProvider.GEMINI -> "${provider.baseUrl}/models/gemini-2.0-flash:generateContent?key=$apiKey"
            AIProvider.GROQ -> "${provider.baseUrl}/chat/completions"
            AIProvider.OPENROUTER -> "${provider.baseUrl}/chat/completions"
            AIProvider.TOGETHER -> "${provider.baseUrl}/chat/completions"
            AIProvider.LOCAL_GEMMA -> "" // Not used - local requests bypass HTTP
        }
    }

    /**
     * Get default model for provider
     */
    private fun getDefaultModel(provider: AIProvider): String {
        return when (provider) {
            AIProvider.OPENAI -> "gpt-4o-mini"
            AIProvider.ANTHROPIC -> "claude-3-5-haiku-20241022"
            AIProvider.GEMINI -> "gemini-2.0-flash"
            AIProvider.GROQ -> "llama-3.3-70b-versatile"
            AIProvider.OPENROUTER -> "meta-llama/llama-3.2-3b-instruct:free"
            AIProvider.TOGETHER -> "meta-llama/Llama-3-8b-chat-hf"
            AIProvider.LOCAL_GEMMA -> "gemma-4-e2b" // Display name only, not used for API calls
        }
    }
}
