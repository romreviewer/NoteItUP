package com.romreviewertools.noteitup.domain.usecase

import com.romreviewertools.noteitup.data.ai.AIService
import com.romreviewertools.noteitup.data.ai.ImprovementType

/**
 * Use case for improving text using AI
 */
class ImproveTextUseCase(
    private val aiService: AIService
) {
    /**
     * Improve text with specified improvement type
     * @param text The text to improve
     * @param improvementType Type of improvement to apply
     * @param onModelLoading Called when the local model starts loading for first use
     * @return Result with improved text or error
     */
    suspend operator fun invoke(
        text: String,
        improvementType: ImprovementType,
        onModelLoading: (() -> Unit)? = null
    ): Result<String> {
        if (text.isBlank()) {
            return Result.failure(Exception("Text cannot be empty"))
        }

        return aiService.improveText(text, improvementType, onModelLoading)
    }
}
