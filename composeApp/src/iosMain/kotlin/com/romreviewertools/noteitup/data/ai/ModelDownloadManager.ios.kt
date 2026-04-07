package com.romreviewertools.noteitup.data.ai

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * iOS stub implementation of ModelDownloadManager.
 * Local AI is not yet supported on iOS.
 */
actual class ModelDownloadManager {

    private val _downloadState = MutableStateFlow<ModelDownloadState>(ModelDownloadState.NotDownloaded)

    actual fun getDownloadState(): StateFlow<ModelDownloadState> = _downloadState.asStateFlow()

    actual suspend fun downloadModel(modelInfo: ModelInfo) {
        _downloadState.value = ModelDownloadState.Error("Local AI is not yet available on iOS.")
    }

    actual fun cancelDownload() {
        // No-op
    }

    actual suspend fun deleteModel() {
        // No-op
    }

    actual fun getModelPath(): String? = null

    actual fun getAvailableModels(): List<ModelInfo> = emptyList()

    actual suspend fun importModel(externalPath: String): Result<String> {
        return Result.failure(UnsupportedOperationException("Local AI is not yet available on iOS."))
    }

    actual fun validateModelFile(path: String): Boolean = false

    actual fun getModelSource(): ModelSource = ModelSource.NONE
}
