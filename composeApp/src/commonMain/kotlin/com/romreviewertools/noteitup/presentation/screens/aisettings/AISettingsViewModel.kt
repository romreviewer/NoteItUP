package com.romreviewertools.noteitup.presentation.screens.aisettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.romreviewertools.noteitup.data.ai.AIService
import com.romreviewertools.noteitup.data.ai.AvailableModels
import com.romreviewertools.noteitup.data.ai.LocalInferenceEngine
import com.romreviewertools.noteitup.data.ai.ModelDownloadManager
import com.romreviewertools.noteitup.data.ai.ModelDownloadState
import com.romreviewertools.noteitup.data.ai.ModelSource
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
    private val analyticsService: AnalyticsService,
    private val modelDownloadManager: ModelDownloadManager,
    private val localInferenceEngine: LocalInferenceEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(AISettingsUiState())
    val uiState: StateFlow<AISettingsUiState> = _uiState.asStateFlow()

    init {
        analyticsService.logEvent(AnalyticsEvent.ScreenViewAISettings)
        loadSettings()
        observeModelDownloadState()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            aiSettingsRepository.aiSettings.collect { settings ->
                _uiState.update {
                    it.copy(
                        settings = settings,
                        isModelLoaded = localInferenceEngine.isModelLoaded()
                    )
                }
            }
        }
    }

    private fun observeModelDownloadState() {
        viewModelScope.launch {
            modelDownloadManager.getDownloadState().collect { downloadState ->
                _uiState.update {
                    it.copy(
                        modelDownloadState = downloadState,
                        modelSource = modelDownloadManager.getModelSource()
                    )
                }
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
            // Local model intents
            is AISettingsIntent.DownloadModel -> downloadModel()
            is AISettingsIntent.CancelDownload -> cancelDownload()
            is AISettingsIntent.DeleteModel -> deleteModel()
            is AISettingsIntent.LoadModel -> loadModel()
            is AISettingsIntent.UnloadModel -> unloadModel()
            is AISettingsIntent.ImportModelFile -> importModelFile(intent.path)
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
                // Clear model selection when provider changes
                aiSettingsRepository.updateSelectedModel("")
                // Only clear API key for cloud providers switching away
                if (provider.requiresApiKey) {
                    aiSettingsRepository.clearApiKey()
                }
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
            if (provider.apiKeyUrl.isNotBlank()) {
                urlOpener.openUrl(provider.apiKeyUrl)
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to open URL: ${e.message}") }
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null, testResult = null) }
    }

    // ---- Local Model Management ----

    private fun downloadModel() {
        viewModelScope.launch {
            try {
                val model = AvailableModels.GEMMA_4_E2B
                modelDownloadManager.downloadModel(model)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Download failed: ${e.message}") }
            }
        }
    }

    private fun cancelDownload() {
        modelDownloadManager.cancelDownload()
    }

    private fun deleteModel() {
        viewModelScope.launch {
            try {
                // Unload model first if loaded
                if (localInferenceEngine.isModelLoaded()) {
                    localInferenceEngine.unloadModel()
                }
                modelDownloadManager.deleteModel()
                _uiState.update { it.copy(isModelLoaded = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete model: ${e.message}") }
            }
        }
    }

    private fun loadModel() {
        viewModelScope.launch {
            val modelPath = modelDownloadManager.getModelPath()
            if (modelPath == null) {
                _uiState.update { it.copy(error = "No model file found. Download or import a model first.") }
                return@launch
            }

            _uiState.update { it.copy(isModelLoading = true) }

            try {
                localInferenceEngine.loadModel(modelPath)
                _uiState.update {
                    it.copy(
                        isModelLoaded = true,
                        isModelLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isModelLoading = false,
                        error = "Failed to load model: ${e.message}"
                    )
                }
            }
        }
    }

    private fun unloadModel() {
        localInferenceEngine.unloadModel()
        _uiState.update { it.copy(isModelLoaded = false) }
    }

    private fun importModelFile(path: String) {
        viewModelScope.launch {
            try {
                val result = modelDownloadManager.importModel(path)
                if (result.isFailure) {
                    _uiState.update {
                        it.copy(error = result.exceptionOrNull()?.message ?: "Import failed")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Import failed: ${e.message}") }
            }
        }
    }
}
