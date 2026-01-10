package com.romreviewertools.noteitup.data.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Types of text improvements available
 */
enum class ImprovementType(
    val displayName: String,
    val systemPrompt: String
) {
    IMPROVE_FOR_JOURNAL(
        displayName = "Improve for Journal",
        systemPrompt = "You are a journal writing expert. Rewrite the text to be more personal, reflective, and emotionally authentic - perfect for a diary entry. Use first-person perspective, add introspection about feelings and thoughts, and make it flow naturally as a personal reflection. Keep the core message but enhance the journaling style. Return only the improved text without explanations."
    ),
    FIX_GRAMMAR(
        displayName = "Fix Grammar",
        systemPrompt = "You are a grammar expert. Fix any grammar, spelling, and punctuation errors in the text. Keep the original meaning and style. Return only the corrected text without explanations."
    ),
    IMPROVE_CLARITY(
        displayName = "Improve Clarity",
        systemPrompt = "You are a writing coach. Improve the clarity and readability of the text. Make it easier to understand while preserving the original meaning. Return only the improved text without explanations."
    ),
    MAKE_SHORTER(
        displayName = "Make Shorter",
        systemPrompt = "You are a concise writer. Make the text shorter while keeping the key points and meaning. Be succinct. Return only the shortened text without explanations."
    ),
    EXPAND(
        displayName = "Expand",
        systemPrompt = "You are a creative writer. Expand the text by adding more details, examples, and depth. Keep the original tone and style. Return only the expanded text without explanations."
    ),
    CHANGE_TONE_PROFESSIONAL(
        displayName = "Professional Tone",
        systemPrompt = "You are a professional writer. Rewrite the text with a professional and formal tone. Keep the meaning intact. Return only the rewritten text without explanations."
    ),
    CHANGE_TONE_CASUAL(
        displayName = "Casual Tone",
        systemPrompt = "You are a casual writer. Rewrite the text with a casual and friendly tone. Keep the meaning intact. Return only the rewritten text without explanations."
    ),
    /*BRAINSTORM(
        displayName = "Brainstorm Ideas",
        systemPrompt = "You are a creative brainstorming assistant. Based on the text, generate related ideas, questions, or topics to explore. Provide 3-5 concise suggestions."
    ),*/
    SUMMARIZE(
        displayName = "Summarize",
        systemPrompt = "You are a summarization expert. Create a concise summary of the text, capturing the main points. Return only the summary without explanations."
    )
}

/**
 * OpenAI-compatible chat completion request
 */
@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    val stream: Boolean = false,
    @SerialName("max_tokens")
    val maxTokens: Int? = null
)

/**
 * Chat message in conversation
 */
@Serializable
data class ChatMessage(
    val role: String, // "system", "user", or "assistant"
    val content: String
)

/**
 * OpenAI-compatible chat completion response
 */
@Serializable
data class ChatCompletionResponse(
    val id: String? = null,
    val choices: List<ChatChoice>,
    val usage: Usage? = null
)

/**
 * Individual choice in response
 */
@Serializable
data class ChatChoice(
    val index: Int,
    val message: ChatMessage,
    @SerialName("finish_reason")
    val finishReason: String? = null
)

/**
 * Token usage information
 */
@Serializable
data class Usage(
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int
)

/**
 * Streaming chunk for server-sent events
 */
@Serializable
data class ChatCompletionChunk(
    val id: String? = null,
    val choices: List<ChunkChoice>
)

/**
 * Individual choice in streaming chunk
 */
@Serializable
data class ChunkChoice(
    val index: Int,
    val delta: Delta,
    @SerialName("finish_reason")
    val finishReason: String? = null
)

/**
 * Delta content in streaming response
 */
@Serializable
data class Delta(
    val role: String? = null,
    val content: String? = null
)

/**
 * Result of AI text improvement
 */
sealed class AIResult<out T> {
    data class Success<T>(val data: T) : AIResult<T>()
    data class Error(val message: String) : AIResult<Nothing>()
    data object Loading : AIResult<Nothing>()
}

// Google Gemini API Models
@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? = null
)

@Serializable
data class GeminiPart(
    val text: String
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent,
    val finishReason: String? = null
)

@Serializable
data class GeminiErrorResponse(
    val error: GeminiError
)

@Serializable
data class GeminiError(
    val code: Int,
    val message: String,
    val status: String
)
