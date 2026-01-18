package com.romreviewertools.noteitup.presentation.screens.aisettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.romreviewertools.noteitup.data.ai.AIService
import com.romreviewertools.noteitup.data.analytics.AnalyticsEvent
import com.romreviewertools.noteitup.data.analytics.AnalyticsService
import com.romreviewertools.noteitup.data.repository.AISettingsRepository
import com.romreviewertools.noteitup.domain.model.AIProvider
import com.romreviewertools.noteitup.util.UrlOpener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for AI Settings screen
 */
class AISettingsViewModel(
    private val aiSettingsRepository: AISettingsRepository,
    private val aiService: AIService,
    private val urlOpener: UrlOpener,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AISettingsUiState())
    val uiState: StateFlow<AISettingsUiState> = _uiState.asStateFlow()

    init {
        analyticsService.logEvent(AnalyticsEvent.ScreenViewAISettings)
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            aiSettingsRepository.aiSettings.collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
    }

    fun onIntent(intent: AISettingsIntent) {
        when (intent) {
            is AISettingsIntent.UpdateAIEnabled -> updateAIEnabled(intent.enabled)
            is AISettingsIntent.SelectProvider -> selectProvider(intent.provider)
            is AISettingsIntent.UpdateApiKey -> updateApiKey(intent.apiKey)
            is AISettingsIntent.UpdateSelectedModel -> updateSelectedModel(intent.model)
            is AISettingsIntent.UpdateStreamingEnabled -> updateStreamingEnabled(intent.enabled)
            is AISettingsIntent.TestConnection -> testConnection()
            is AISettingsIntent.ClearApiKey -> clearApiKey()
            is AISettingsIntent.OpenApiKeyUrl -> openApiKeyUrl()
            is AISettingsIntent.DismissError -> dismissError()
        }
    }

    private fun updateAIEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                aiSettingsRepository.updateAIEnabled(enabled)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to update AI settings: ${e.message}") }
            }
        }
    }

    private fun selectProvider(provider: AIProvider) {
        viewModelScope.launch {
            try {
                aiSettingsRepository.updateProvider(provider)
                // Clear model selection and API key when provider changes
                aiSettingsRepository.updateSelectedModel("")
                aiSettingsRepository.clearApiKey()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to select provider: ${e.message}") }
            }
        }
    }

    private fun updateApiKey(apiKey: String) {
        viewModelScope.launch {
            try {
                aiSettingsRepository.updateApiKey(apiKey)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to update API key: ${e.message}") }
            }
        }
    }

    private fun updateSelectedModel(model: String) {
        viewModelScope.launch {
            try {
                aiSettingsRepository.updateSelectedModel(model)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to update model: ${e.message}") }
            }
        }
    }

    private fun updateStreamingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                aiSettingsRepository.updateStreamingEnabled(enabled)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to update streaming: ${e.message}") }
            }
        }
    }

    private fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingConnection = true, testResult = null) }

            try {
                val result = aiService.testConnection()

                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        testResult = if (result.isSuccess) {
                            TestResult.Success
                        } else {
                            TestResult.Failure(result.exceptionOrNull()?.message ?: "Unknown error")
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        testResult = TestResult.Failure(e.message ?: "Unknown error")
                    )
                }
            }
        }
    }

    private fun clearApiKey() {
        viewModelScope.launch {
            try {
                aiSettingsRepository.clearApiKey()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to clear API key: ${e.message}") }
            }
        }
    }

    private fun openApiKeyUrl() {
        try {
            val provider = _uiState.value.settings.selectedProvider
            urlOpener.openUrl(provider.apiKeyUrl)
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to open URL: ${e.message}") }
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null, testResult = null) }
    }
}
