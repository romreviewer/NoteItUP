package com.romreviewertools.noteitup.data.ai

import io.ktor.client.HttpClient
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * JVM (Desktop) implementation of ModelDownloadManager.
 *
 * Uses a [Job] cancellation handle with [withContext] instead of a standalone CoroutineScope.
 * The caller's coroutine scope (ViewModel) provides structured concurrency;
 * the Job is only used as an external cancellation signal.
 *
 * Stores models in ~/.noteitup/models/
 */
actual class ModelDownloadManager(
    private val httpClient: HttpClient
) {
    private val modelsDir = File(System.getProperty("user.home"), ".noteitup/models").apply { mkdirs() }
    private val _downloadState = MutableStateFlow<ModelDownloadState>(ModelDownloadState.NotDownloaded)
    private var cancellationJob: Job? = null
    private var modelSource: ModelSource = ModelSource.NONE

    init {
        val defaultModel = AvailableModels.GEMMA_4_E2B
        val modelFile = File(modelsDir, defaultModel.fileName)
        if (modelFile.exists() && modelFile.length() > 0) {
            _downloadState.value = ModelDownloadState.Downloaded
            modelSource = ModelSource.DOWNLOADED
        }
    }

    actual fun getDownloadState(): StateFlow<ModelDownloadState> = _downloadState.asStateFlow()

    actual suspend fun downloadModel(modelInfo: ModelInfo) {
        // Cancel any existing download
        cancellationJob?.cancel()

        val job = Job()
        cancellationJob = job

        val targetFile = File(modelsDir, modelInfo.fileName)
        val tempFile = File(modelsDir, "${modelInfo.fileName}.tmp")

        try {
            // withContext inherits the caller's scope for structured concurrency,
            // but the added Job allows external cancellation via cancelDownload()
            withContext(Dispatchers.IO + job) {
                _downloadState.value = ModelDownloadState.Downloading(0f)

                httpClient.prepareGet(modelInfo.downloadUrl).execute { response ->
                    val totalBytes = response.contentLength() ?: modelInfo.sizeBytes
                    val channel = response.bodyAsChannel()
                    val buffer = ByteArray(8192)
                    var bytesRead = 0L

                    tempFile.outputStream().buffered().use { output ->
                        while (true) {
                            ensureActive() // Throws CancellationException if job is cancelled
                            val read = channel.readAvailable(buffer)
                            if (read <= 0) break
                            output.write(buffer, 0, read)
                            bytesRead += read
                            val progress = (bytesRead.toFloat() / totalBytes).coerceIn(0f, 1f)
                            _downloadState.value = ModelDownloadState.Downloading(progress)
                        }
                    }
                }

                // Download completed successfully
                if (targetFile.exists()) targetFile.delete()
                tempFile.renameTo(targetFile)

                modelSource = ModelSource.DOWNLOADED
                _downloadState.value = ModelDownloadState.Downloaded
                println("ModelDownloadManager: Download complete: ${targetFile.absolutePath}")
            }
        } catch (e: CancellationException) {
            // Cancelled by cancelDownload() or parent scope -- clean up
            tempFile.delete()
            _downloadState.value = ModelDownloadState.NotDownloaded
        } catch (e: Exception) {
            tempFile.delete()
            println("ModelDownloadManager: Download failed: ${e.message}")
            _downloadState.value = ModelDownloadState.Error("Download failed: ${e.message}")
        } finally {
            if (cancellationJob == job) {
                cancellationJob = null
            }
        }
    }

    actual fun cancelDownload() {
        cancellationJob?.cancel()
        cancellationJob = null
        // Clean up temp files
        modelsDir.listFiles()?.filter { it.name.endsWith(".tmp") }?.forEach { it.delete() }
        _downloadState.value = ModelDownloadState.NotDownloaded
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

    actual suspend fun importModel(externalPath: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(externalPath)
            if (!sourceFile.exists()) {
                return@withContext Result.failure(Exception("File not found: $externalPath"))
            }

            if (!validateModelFile(externalPath)) {
                return@withContext Result.failure(Exception("Invalid model file. Expected a .litertlm file."))
            }

            _downloadState.value = ModelDownloadState.Downloading(0f)

            val targetFile = File(modelsDir, AvailableModels.GEMMA_4_E2B.fileName)
            if (targetFile.exists()) targetFile.delete()

            val totalBytes = sourceFile.length()
            var bytesCopied = 0L

            sourceFile.inputStream().buffered().use { input ->
                targetFile.outputStream().buffered().use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesCopied += read
                        val progress = (bytesCopied.toFloat() / totalBytes).coerceIn(0f, 1f)
                        _downloadState.value = ModelDownloadState.Downloading(progress)
                    }
                }
            }

            modelSource = ModelSource.IMPORTED
            _downloadState.value = ModelDownloadState.Downloaded
            println("ModelDownloadManager: Model imported from: $externalPath")
            Result.success(targetFile.absolutePath)

        } catch (e: Exception) {
            _downloadState.value = ModelDownloadState.Error("Import failed: ${e.message}")
            Result.failure(Exception("Failed to import model: ${e.message}"))
        }
    }

    actual fun validateModelFile(path: String): Boolean {
        val file = File(path)
        return file.exists() &&
                file.length() > 1_000_000 &&
                (file.name.endsWith(".litertlm") || file.name.endsWith(".bin") || file.name.endsWith(".task"))
    }

    actual fun getModelSource(): ModelSource = modelSource
}
