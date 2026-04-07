package com.romreviewertools.noteitup.data.ai

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

/**
 * Android implementation of ModelDownloadManager.
 *
 * Downloads are handled by [ModelDownloadService] (ForegroundService) so they survive
 * screen navigation and app backgrounding. This class acts as the coordinator:
 * - Starts/stops the service
 * - Holds the shared [_downloadState] StateFlow that both the service and UI observe
 * - Manages the model file on disk
 *
 * Stores models in app internal storage: context.filesDir/models/
 */
actual class ModelDownloadManager(
    private val context: Context,
    private val httpClient: io.ktor.client.HttpClient
) {
    private val modelsDir = File(context.filesDir, "models").apply { mkdirs() }
    internal val _downloadState = MutableStateFlow<ModelDownloadState>(ModelDownloadState.NotDownloaded)
    private var modelSource: ModelSource = ModelSource.NONE

    init {
        // Check if model already exists on disk
        val defaultModel = AvailableModels.GEMMA_4_E2B
        val modelFile = File(modelsDir, defaultModel.fileName)
        if (modelFile.exists() && modelFile.length() > 0) {
            _downloadState.value = ModelDownloadState.Downloaded
            modelSource = ModelSource.DOWNLOADED
        }
    }

    actual fun getDownloadState(): StateFlow<ModelDownloadState> = _downloadState.asStateFlow()

    /**
     * Start downloading the model via the ForegroundService.
     * The service handles the actual HTTP download and updates [_downloadState].
     */
    actual suspend fun downloadModel(modelInfo: ModelInfo) {
        val intent = ModelDownloadService.createIntent(context, modelInfo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /**
     * Cancel an in-progress download by sending a cancel action to the service.
     */
    actual fun cancelDownload() {
        val cancelIntent = Intent(context, ModelDownloadService::class.java).apply {
            action = ModelDownloadService.ACTION_CANCEL
        }
        context.startService(cancelIntent)
    }

    actual suspend fun deleteModel() = withContext(Dispatchers.IO) {
        modelsDir.listFiles()?.forEach { it.delete() }
        modelSource = ModelSource.NONE
        _downloadState.value = ModelDownloadState.NotDownloaded
    }

    actual fun getModelPath(): String? {
        val defaultModel = AvailableModels.GEMMA_4_E2B
        val modelFile = File(modelsDir, defaultModel.fileName)
        return if (modelFile.exists() && modelFile.length() > 0) {
            modelFile.absolutePath
        } else null
    }

    actual fun getAvailableModels(): List<ModelInfo> = AvailableModels.ALL

    /**
     * Import a model from an external path or content URI.
     * On Android, the file picker returns a content:// URI, so we handle both:
     * - content:// URIs (from system file picker)
     * - File paths (from direct path input)
     */
    actual suspend fun importModel(externalPath: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            _downloadState.value = ModelDownloadState.Downloading(0f)

            val targetFile = File(modelsDir, AvailableModels.GEMMA_4_E2B.fileName)
            if (targetFile.exists()) targetFile.delete()

            // Open input stream from content URI or file path
            val (inputStream, totalBytes) = openModelSource(externalPath)
                ?: return@withContext Result.failure(Exception("Cannot open file. Please try a different file."))

            // Copy to model directory with progress tracking
            var bytesCopied = 0L
            inputStream.buffered().use { input ->
                targetFile.outputStream().buffered().use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesCopied += read
                        if (totalBytes > 0) {
                            val progress = (bytesCopied.toFloat() / totalBytes).coerceIn(0f, 1f)
                            _downloadState.value = ModelDownloadState.Downloading(progress)
                        }
                    }
                }
            }

            // Sanity check: file should be at least 1MB
            if (targetFile.length() < 1_000_000) {
                targetFile.delete()
                return@withContext Result.failure(Exception("File too small to be a valid model."))
            }

            modelSource = ModelSource.IMPORTED
            _downloadState.value = ModelDownloadState.Downloaded
            println("ModelDownloadManager: Model imported from: $externalPath (${targetFile.length()} bytes)")
            Result.success(targetFile.absolutePath)

        } catch (e: Exception) {
            _downloadState.value = ModelDownloadState.Error("Import failed: ${e.message}")
            Result.failure(Exception("Failed to import model: ${e.message}"))
        }
    }

    /**
     * Open an InputStream from either a content:// URI or a file path.
     * Returns the stream and the total size in bytes (or -1 if unknown).
     */
    private fun openModelSource(path: String): Pair<InputStream, Long>? {
        return if (path.startsWith("content://")) {
            // Content URI from Android file picker
            val uri = Uri.parse(path)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            // Try to get file size from content resolver
            val size = try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: -1L
            } catch (e: Exception) {
                -1L
            }
            Pair(inputStream, size)
        } else {
            // Direct file path
            val file = File(path)
            if (!file.exists()) return null
            Pair(file.inputStream(), file.length())
        }
    }

    actual fun validateModelFile(path: String): Boolean {
        return if (path.startsWith("content://")) {
            // For content URIs, we can't easily validate before import.
            // The import will do a size check after copying.
            true
        } else {
            val file = File(path)
            file.exists() &&
                    file.length() > 1_000_000 &&
                    (file.name.endsWith(".litertlm") || file.name.endsWith(".bin") || file.name.endsWith(".task"))
        }
    }

    actual fun getModelSource(): ModelSource = modelSource

    // ---- Internal API called by ModelDownloadService ----

    /**
     * Called by [ModelDownloadService] to update download progress.
     */
    internal fun updateDownloadState(state: ModelDownloadState) {
        _downloadState.value = state
    }

    /**
     * Called by [ModelDownloadService] when download completes successfully.
     */
    internal fun onDownloadComplete() {
        modelSource = ModelSource.DOWNLOADED
        _downloadState.value = ModelDownloadState.Downloaded
    }
}
