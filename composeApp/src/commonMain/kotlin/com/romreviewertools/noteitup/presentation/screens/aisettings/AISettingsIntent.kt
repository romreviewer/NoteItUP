package com.romreviewertools.noteitup.presentation.screens.aisettings

import com.romreviewertools.noteitup.domain.model.AIProvider

/**
 * User intents for AI Settings screen
 */
sealed interface AISettingsIntent {
    data class UpdateAIEnabled(val enabled: Boolean) : AISettingsIntent
    data class SelectProvider(val provider: AIProvider) : AISettingsIntent
    data class UpdateApiKey(val apiKey: String) : AISettingsIntent
    data class UpdateSelectedModel(val model: String) : AISettingsIntent
    data class UpdateStreamingEnabled(val enabled: Boolean) : AISettingsIntent
    data object TestConnection : AISettingsIntent
    data object ClearApiKey : AISettingsIntent
    data object OpenApiKeyUrl : AISettingsIntent
    data object DismissError : AISettingsIntent
}
