package com.romreviewertools.noteitup.data.repository

import com.romreviewertools.noteitup.data.preferences.PreferencesStorage
import com.romreviewertools.noteitup.domain.model.AIProvider
import com.romreviewertools.noteitup.domain.model.AISettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Repository for managing AI settings and API keys.
 * Uses secure storage for API keys (platform-specific).
 */
class AISettingsRepository(
    private val preferencesStorage: PreferencesStorage
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _aiSettings = MutableStateFlow(AISettings())
    val aiSettings: Flow<AISettings> = _aiSettings.asStateFlow()

    init {
        // Observe preferences and update settings
        scope.launch {
            combine(
                preferencesStorage.getString(KEY_AI_ENABLED, "false"),
                preferencesStorage.getString(KEY_AI_PROVIDER, AIProvider.GROQ.name),
                preferencesStorage.getString(KEY_API_KEY, ""),
                preferencesStorage.getString(KEY_SELECTED_MODEL, ""),
                preferencesStorage.getString(KEY_STREAMING_ENABLED, "true")
            ) { enabled, providerName, apiKey, model, streaming ->
                val provider = try {
                    AIProvider.valueOf(providerName)
                } catch (e: IllegalArgumentException) {
                    AIProvider.GROQ
                }
                AISettings(
                    enabled = enabled.toBoolean(),
                    selectedProvider = provider,
                    apiKey = apiKey,
                    selectedModel = model,
                    streamingEnabled = streaming.toBoolean()
                )
            }.collect { settings ->
                _aiSettings.value = settings
            }
        }
    }

    suspend fun updateAIEnabled(enabled: Boolean) {
        preferencesStorage.putString(KEY_AI_ENABLED, enabled.toString())
    }

    suspend fun updateProvider(provider: AIProvider) {
        preferencesStorage.putString(KEY_AI_PROVIDER, provider.name)
    }

    suspend fun updateApiKey(apiKey: String) {
        // API key is stored encrypted by PreferencesStorage (platform-specific)
        preferencesStorage.putString(KEY_API_KEY, apiKey)
    }

    suspend fun updateSelectedModel(model: String) {
        preferencesStorage.putString(KEY_SELECTED_MODEL, model)
    }

    suspend fun updateStreamingEnabled(enabled: Boolean) {
        preferencesStorage.putString(KEY_STREAMING_ENABLED, enabled.toString())
    }

    suspend fun clearApiKey() {
        preferencesStorage.putString(KEY_API_KEY, "")
    }

    companion object {
        private const val KEY_AI_ENABLED = "ai_enabled"
        private const val KEY_AI_PROVIDER = "ai_provider"
        private const val KEY_API_KEY = "ai_api_key"  // Stored encrypted
        private const val KEY_SELECTED_MODEL = "ai_selected_model"
        private const val KEY_STREAMING_ENABLED = "ai_streaming_enabled"
    }
}
