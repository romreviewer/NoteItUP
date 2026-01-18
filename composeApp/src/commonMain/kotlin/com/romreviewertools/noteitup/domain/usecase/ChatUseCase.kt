package com.romreviewertools.noteitup.domain.usecase

import com.romreviewertools.noteitup.data.ai.AIService
import com.romreviewertools.noteitup.data.ai.ChatMessage
import com.romreviewertools.noteitup.data.repository.AISettingsRepository
import kotlinx.coroutines.flow.firstOrNull

/**
 * Use case for brainstorming chat with AI assistant.
 * Supports journaling prompts, idea generation, and writing assistance.
 */
class ChatUseCase(
    private val aiService: AIService,
    private val aiSettingsRepository: AISettingsRepository
) {
    companion object {
        private val SYSTEM_PROMPT = """
            You are a helpful journaling assistant and brainstorming partner. Your role is to:

            1. Help users reflect on their thoughts and feelings
            2. Suggest journaling prompts and topics to explore
            3. Ask thoughtful questions to deepen self-reflection
            4. Help brainstorm ideas and work through problems
            5. Provide encouragement and support for their writing journey

            Keep responses conversational, warm, and supportive. Be concise but thoughtful.
            When suggesting prompts, provide 2-3 options to choose from.
            Ask follow-up questions to help users explore their thoughts deeper.

            Remember: You're a supportive writing companion, not a therapist.
            Focus on creative exploration and self-expression.
        """.trimIndent()

        val STARTER_PROMPTS = listOf(
            "What's on your mind today?",
            "Help me reflect on my day",
            "I need journaling prompts",
            "Help me process my feelings",
            "I want to set some goals",
            "Help me brainstorm ideas"
        )
    }

    /**
     * Send a message to the AI and get a response.
     * @param userMessage The user's message
     * @param conversationHistory Previous messages in the conversation
     * @return Result containing the AI's response
     */
    suspend fun sendMessage(
        userMessage: String,
        conversationHistory: List<ChatMessage>
    ): Result<String> {
        val settings = aiSettingsRepository.aiSettings.firstOrNull()
            ?: return Result.failure(Exception("AI settings not configured"))

        if (!settings.enabled) {
            return Result.failure(Exception("AI features are disabled. Enable them in AI Settings."))
        }

        if (settings.apiKey.isBlank()) {
            return Result.failure(Exception("API key not configured. Add your API key in AI Settings."))
        }

        return aiService.chat(
            systemPrompt = SYSTEM_PROMPT,
            messages = conversationHistory + ChatMessage(role = "user", content = userMessage)
        )
    }

    /**
     * Generate journaling prompts based on a topic.
     */
    suspend fun generatePrompts(topic: String? = null): Result<String> {
        val prompt = if (topic.isNullOrBlank()) {
            "Generate 5 thoughtful journaling prompts for today. Make them varied - include prompts about gratitude, reflection, goals, and creativity."
        } else {
            "Generate 5 thoughtful journaling prompts about: $topic. Make them specific and introspective."
        }

        return sendMessage(prompt, emptyList())
    }

    /**
     * Check if AI is properly configured.
     */
    suspend fun isConfigured(): Boolean {
        val settings = aiSettingsRepository.aiSettings.firstOrNull() ?: return false
        return settings.enabled && settings.apiKey.isNotBlank()
    }
}