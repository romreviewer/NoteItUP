package com.romreviewertools.noteitup.data.ai

import kotlinx.coroutines.flow.StateFlow

/**
 * Information about an available local AI model.
 */
data class ModelInfo(
    val name: String,
    val fileName: String,
    val sizeBytes: Long,
    val downloadUrl: String,
    val description: String
)

/**
 * State of a model download or availability.
 */
sealed class ModelDownloadState {
    data object NotDownloaded : ModelDownloadState()
    data class Downloading(val progress: Float) : ModelDownloadState()  // 0.0 - 1.0
    data object Downloaded : ModelDownloadState()
    data class Error(val message: String) : ModelDownloadState()
}

/**
 * How the model was acquired.
 */
enum class ModelSource {
    NONE,
    DOWNLOADED,
    IMPORTED
}

/**
 * Platform-specific manager for downloading, importing, and managing local AI model files.
 *
 * - Android: Downloads to app internal storage, uses system file picker for import
 * - JVM: Downloads to ~/.noteitup/models/, uses JFileChooser for import
 * - iOS: Stub (local AI not supported)
 */
expect class ModelDownloadManager {

    /** Reactive state of the model download/availability. */
    fun getDownloadState(): StateFlow<ModelDownloadState>

    /** Start downloading the model from HuggingFace. Progress updates via getDownloadState(). */
    suspend fun downloadModel(modelInfo: ModelInfo)

    /** Cancel an in-progress download. */
    fun cancelDownload()

    /** Delete the downloaded/imported model file and reset state. */
    suspend fun deleteModel()

    /** Get the local file path of the model, or null if no model available. */
    fun getModelPath(): String?

    /** Get the list of available models that can be downloaded. */
    fun getAvailableModels(): List<ModelInfo>

    /**
     * Import a model file from an external path (user already has the .litertlm file).
     * Copies the file into app-managed storage.
     *
     * @param externalPath Absolute path to the .litertlm file
     * @return Result containing the managed model path on success
     */
    suspend fun importModel(externalPath: String): Result<String>

    /**
     * Validate that a file is a potentially valid .litertlm model.
     * Does a quick size/extension check (not a full load).
     */
    fun validateModelFile(path: String): Boolean

    /** Get how the current model was acquired. */
    fun getModelSource(): ModelSource
}

/**
 * Pre-defined model entries available for download.
 */
object AvailableModels {
    val GEMMA_4_E2B = ModelInfo(
        name = "Gemma 4 E2B",
        fileName = "gemma-4-E2B-it.litertlm",
        sizeBytes = 1_600_000_000L, // ~1.6 GB
        downloadUrl = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm",
        description = "2B params. Best for 8GB+ RAM devices. Fast, private text improvement."
    )

    val ALL = listOf(GEMMA_4_E2B)
}
