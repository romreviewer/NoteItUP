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
import kotlinx.coroutines.flow.firstOrNull

/**
 * Service for interacting with AI providers using OpenAI-compatible APIs
 */
class AIService(
    private val httpClient: HttpClient,
    private val aiSettingsRepository: AISettingsRepository
) {

    /**
     * Test connection to AI provider
     */
    suspend fun testConnection(): Result<String> {
        return try {
            val settings = aiSettingsRepository.aiSettings.firstOrNull()
                ?: return Result.failure(Exception("AI settings not found"))

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
     * Improve text using specified improvement type
     */
    suspend fun improveText(
        text: String,
        improvementType: ImprovementType
    ): Result<String> {
        return try {
            val settings = aiSettingsRepository.aiSettings.firstOrNull()
                ?: return Result.failure(Exception("AI settings not configured"))

            if (!settings.enabled) {
                return Result.failure(Exception("AI features are disabled"))
            }

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
        }
    }
}
