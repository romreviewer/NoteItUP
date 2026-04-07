package com.romreviewertools.noteitup.presentation.screens.aisettings

import com.romreviewertools.noteitup.data.ai.ModelDownloadState
import com.romreviewertools.noteitup.data.ai.ModelSource
import com.romreviewertools.noteitup.domain.model.AISettings

/**
 * UI state for AI Settings screen
 */
data class AISettingsUiState(
    val settings: AISettings = AISettings(),
    val isLoading: Boolean = false,
    val isTestingConnection: Boolean = false,
    val testResult: TestResult? = null,
    val error: String? = null,
    // Local AI model state
    val modelDownloadState: ModelDownloadState = ModelDownloadState.NotDownloaded,
    val isModelLoaded: Boolean = false,
    val isModelLoading: Boolean = false,
    val modelSource: ModelSource = ModelSource.NONE
)

/**
 * Result of testing AI provider connection
 */
sealed interface TestResult {
    data object Success : TestResult
    data class Failure(val message: String) : TestResult
}
