package com.romreviewertools.noteitup.data.ai

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

/**
 * Android implementation of ModelDownloadManager.
 *
 * Uses the system [DownloadManager] for downloading model files.
 * This avoids foreground service permissions entirely -- DownloadManager handles
 * notification, resume/retry, and background download natively.
 *
 * Stores models in app-specific external storage: context.getExternalFilesDir("models")
 * (doesn't require storage permission, auto-cleaned on uninstall)
 */
actual class ModelDownloadManager(
    private val context: Context,
    @Suppress("UNUSED_PARAMETER") private val httpClient: io.ktor.client.HttpClient
) {
    private val modelsDir: File = (context.getExternalFilesDir(null)?.let { File(it, "models") }
        ?: File(context.filesDir, "models")).apply { mkdirs() }

    private val _downloadState = MutableStateFlow<ModelDownloadState>(ModelDownloadState.NotDownloaded)
    private var modelSource: ModelSource = ModelSource.NONE

    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private var activeDownloadId: Long = -1L
    private var progressJob: Job? = null
    private var completionReceiver: BroadcastReceiver? = null

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
     * Start downloading the model via Android's system DownloadManager.
     * The system handles notification, progress, resume/retry automatically.
     */
    actual suspend fun downloadModel(modelInfo: ModelInfo) {
        // Cancel any existing download
        cancelDownload()

        val destinationFile = File(modelsDir, modelInfo.fileName)
        if (destinationFile.exists()) destinationFile.delete()

        val request = DownloadManager.Request(Uri.parse(modelInfo.downloadUrl)).apply {
            setTitle("Downloading ${modelInfo.name}")
            setDescription("${modelInfo.name} - AI model for NoteItUP")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            setDestinationUri(Uri.fromFile(destinationFile))
            setAllowedOverMetered(true)
            setAllowedOverRoaming(false)
        }

        activeDownloadId = downloadManager.enqueue(request)
        _downloadState.value = ModelDownloadState.Downloading(0f)

        // Register receiver for download completion
        registerCompletionReceiver(modelInfo)

        // Start polling for progress
        startProgressPolling()
    }

    /**
     * Cancel an in-progress download.
     */
    actual fun cancelDownload() {
        if (activeDownloadId != -1L) {
            downloadManager.remove(activeDownloadId)
            activeDownloadId = -1L
        }
        stopProgressPolling()
        unregisterCompletionReceiver()
        // Clean up partial files
        modelsDir.listFiles()?.filter { !it.name.endsWith(".litertlm") || it.length() == 0L }?.forEach { it.delete() }
        _downloadState.value = ModelDownloadState.NotDownloaded
    }

    actual suspend fun deleteModel() = withContext(Dispatchers.IO) {
        cancelDownload()
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

    actual fun validateModelFile(path: String): Boolean {
        return if (path.startsWith("content://")) {
            true // Validated after copy via size check
        } else {
            val file = File(path)
            file.exists() &&
                    file.length() > 1_000_000 &&
                    (file.name.endsWith(".litertlm") || file.name.endsWith(".bin") || file.name.endsWith(".task"))
        }
    }

    actual fun getModelSource(): ModelSource = modelSource

    // ---- Private helpers ----

    /**
     * Open an InputStream from either a content:// URI or a file path.
     */
    private fun openModelSource(path: String): Pair<InputStream, Long>? {
        return if (path.startsWith("content://")) {
            val uri = Uri.parse(path)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val size = try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: -1L
            } catch (e: Exception) { -1L }
            Pair(inputStream, size)
        } else {
            val file = File(path)
            if (!file.exists()) return null
            Pair(file.inputStream(), file.length())
        }
    }

    /**
     * Register a BroadcastReceiver to detect download completion.
     */
    private fun registerCompletionReceiver(modelInfo: ModelInfo) {
        unregisterCompletionReceiver()

        completionReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: return
                if (id != activeDownloadId) return

                stopProgressPolling()
                unregisterCompletionReceiver()

                // Check download status
                val query = DownloadManager.Query().setFilterById(id)
                val cursor: Cursor? = downloadManager.query(query)
                if (cursor != null && cursor.moveToFirst()) {
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status = if (statusIndex >= 0) cursor.getInt(statusIndex) else -1

                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            modelSource = ModelSource.DOWNLOADED
                            _downloadState.value = ModelDownloadState.Downloaded
                            activeDownloadId = -1L
                            println("ModelDownloadManager: Download complete")
                        }
                        DownloadManager.STATUS_FAILED -> {
                            val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                            val reason = if (reasonIndex >= 0) cursor.getInt(reasonIndex) else -1
                            _downloadState.value = ModelDownloadState.Error("Download failed (error code: $reason)")
                            activeDownloadId = -1L
                        }
                        else -> {
                            // Paused or other
                            _downloadState.value = ModelDownloadState.Error("Download interrupted")
                            activeDownloadId = -1L
                        }
                    }
                    cursor.close()
                } else {
                    _downloadState.value = ModelDownloadState.Error("Download status unknown")
                    activeDownloadId = -1L
                }
            }
        }

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(completionReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(completionReceiver, filter)
        }
    }

    private fun unregisterCompletionReceiver() {
        completionReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (e: Exception) {
                // Already unregistered
            }
        }
        completionReceiver = null
    }

    /**
     * Poll DownloadManager for progress updates.
     * Updates the StateFlow so the UI can show a progress bar.
     */
    private fun startProgressPolling() {
        stopProgressPolling()
        progressJob = CoroutineScope(Dispatchers.IO).launch {
            while (activeDownloadId != -1L) {
                queryProgress()
                delay(500) // Poll every 500ms
            }
        }
    }

    private fun stopProgressPolling() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun queryProgress() {
        if (activeDownloadId == -1L) return

        val query = DownloadManager.Query().setFilterById(activeDownloadId)
        val cursor: Cursor? = downloadManager.query(query)
        if (cursor != null && cursor.moveToFirst()) {
            val bytesIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val totalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

            val bytesDownloaded = if (bytesIndex >= 0) cursor.getLong(bytesIndex) else 0L
            val totalBytes = if (totalIndex >= 0) cursor.getLong(totalIndex) else -1L

            if (totalBytes > 0) {
                val progress = (bytesDownloaded.toFloat() / totalBytes).coerceIn(0f, 1f)
                _downloadState.value = ModelDownloadState.Downloading(progress)
            }
            cursor.close()
        }
    }
}
