package com.romreviewertools.noteitup.domain.model

/**
 * Supported AI providers for text improvement and brainstorming.
 * Users bring their own API keys (BYOK model).
 */
enum class AIProvider(
    val displayName: String,
    val baseUrl: String,
    val hasFreeTier: Boolean,
    val description: String,
    val apiKeyUrl: String
) {
    OPENAI(
        displayName = "OpenAI",
        baseUrl = "https://api.openai.com/v1",
        hasFreeTier = false,
        description = "GPT-4o, GPT-4o-mini, GPT-3.5-turbo - High-quality text improvement",
        apiKeyUrl = "https://platform.openai.com/api-keys"
    ),
    ANTHROPIC(
        displayName = "Anthropic Claude",
        baseUrl = "https://api.anthropic.com/v1",
        hasFreeTier = false,
        description = "Claude 3.5 Sonnet, Haiku - Long-form writing assistance",
        apiKeyUrl = "https://console.anthropic.com/settings/keys"
    ),
    GEMINI(
        displayName = "Google Gemini",
        baseUrl = "https://generativelanguage.googleapis.com/v1beta",
        hasFreeTier = true,
        description = "Gemini 1.5 Flash, Pro - Cost-effective with generous free tier",
        apiKeyUrl = "https://aistudio.google.com/apikey"
    ),
    GROQ(
        displayName = "Groq",
        baseUrl = "https://api.groq.com/openai/v1",
        hasFreeTier = true,
        description = "Llama 3, Mixtral, Gemma - Fast inference speed with free tier",
        apiKeyUrl = "https://console.groq.com/keys"
    ),
    OPENROUTER(
        displayName = "OpenRouter",
        baseUrl = "https://openrouter.ai/api/v1",
        hasFreeTier = true,
        description = "100+ models - Access to many models, some free",
        apiKeyUrl = "https://openrouter.ai/keys"
    ),
    TOGETHER(
        displayName = "Together AI",
        baseUrl = "https://api.together.xyz/v1",
        hasFreeTier = true,
        description = "Qwen, Llama, Mixtral - Open-source models with $25 free credit",
        apiKeyUrl = "https://api.together.xyz/settings/api-keys"
    )
}

/**
 * AI model configuration for a specific provider
 */
data class AIModel(
    val id: String,
    val name: String,
    val provider: AIProvider,
    val contextWindow: Int,
    val supportsStreaming: Boolean = true
)

/**
 * AI settings stored in preferences
 */
data class AISettings(
    val enabled: Boolean = false,
    val selectedProvider: AIProvider = AIProvider.GROQ,
    val apiKey: String = "",
    val selectedModel: String = "",
    val streamingEnabled: Boolean = true
)
