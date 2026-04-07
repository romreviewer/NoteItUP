package com.romreviewertools.noteitup.data.ai

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.ktor.client.HttpClient
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import org.koin.android.ext.android.inject
import java.io.File

/**
 * Android ForegroundService for downloading the local AI model.
 *
 * Ensures download survives:
 * - Screen navigation (user leaves AI Settings)
 * - App going to background
 * - Configuration changes (rotation)
 *
 * Shows a persistent notification with progress bar and cancel action.
 */
class ModelDownloadService : Service() {

    companion object {
        const val CHANNEL_ID = "model_download"
        const val NOTIFICATION_ID = 4200
        const val ACTION_CANCEL = "com.romreviewertools.noteitup.CANCEL_MODEL_DOWNLOAD"

        const val EXTRA_MODEL_NAME = "model_name"
        const val EXTRA_MODEL_FILENAME = "model_filename"
        const val EXTRA_MODEL_URL = "model_url"
        const val EXTRA_MODEL_SIZE = "model_size"

        fun createIntent(context: Context, modelInfo: ModelInfo): Intent {
            return Intent(context, ModelDownloadService::class.java).apply {
                putExtra(EXTRA_MODEL_NAME, modelInfo.name)
                putExtra(EXTRA_MODEL_FILENAME, modelInfo.fileName)
                putExtra(EXTRA_MODEL_URL, modelInfo.downloadUrl)
                putExtra(EXTRA_MODEL_SIZE, modelInfo.sizeBytes)
            }
        }
    }

    private val httpClient: HttpClient by inject()
    private val modelDownloadManager: ModelDownloadManager by inject()

    // No persistent CoroutineScope -- we create a Job per download.
    // The Job is cancelled in cancelDownload() or onDestroy().
    // An inline CoroutineScope(job + Dispatchers.IO) is used to launch,
    // and is garbage collected when the job completes or is cancelled.
    private var downloadJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CANCEL) {
            cancelDownload()
            return START_NOT_STICKY
        }

        val modelName = intent?.getStringExtra(EXTRA_MODEL_NAME) ?: "AI Model"
        val fileName = intent?.getStringExtra(EXTRA_MODEL_FILENAME) ?: return START_NOT_STICKY
        val downloadUrl = intent.getStringExtra(EXTRA_MODEL_URL) ?: return START_NOT_STICKY
        val sizeBytes = intent.getLongExtra(EXTRA_MODEL_SIZE, 0L)

        // Start as foreground immediately
        val notification = buildProgressNotification(modelName, 0)
        startForeground(NOTIFICATION_ID, notification)

        // Start download -- create a Job and an inline scope tied to it.
        // The scope is not stored as a field; it gets GC'd when the job completes.
        val job = Job()
        downloadJob = job
        CoroutineScope(job + Dispatchers.IO).launch {
            performDownload(modelName, fileName, downloadUrl, sizeBytes)
        }

        return START_NOT_STICKY
    }

    private suspend fun performDownload(
        modelName: String,
        fileName: String,
        downloadUrl: String,
        expectedSize: Long
    ) {
        val modelsDir = File(filesDir, "models").apply { mkdirs() }
        val targetFile = File(modelsDir, fileName)
        val tempFile = File(modelsDir, "${fileName}.tmp")

        try {
            modelDownloadManager.updateDownloadState(ModelDownloadState.Downloading(0f))

            httpClient.prepareGet(downloadUrl).execute { response ->
                val totalBytes = response.contentLength() ?: expectedSize
                val channel = response.bodyAsChannel()
                val buffer = ByteArray(8192)
                var bytesRead = 0L
                var lastNotificationUpdate = 0L

                tempFile.outputStream().buffered().use { output ->
                    while (true) {
                        coroutineContext.ensureActive() // Throws CancellationException if job cancelled
                        val read = channel.readAvailable(buffer)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        bytesRead += read

                        val progress = (bytesRead.toFloat() / totalBytes).coerceIn(0f, 1f)
                        modelDownloadManager.updateDownloadState(
                            ModelDownloadState.Downloading(progress)
                        )

                        // Update notification at most every 500ms to avoid throttling
                        val now = System.currentTimeMillis()
                        if (now - lastNotificationUpdate > 500) {
                            val percent = (progress * 100).toInt()
                            updateNotification(modelName, percent)
                            lastNotificationUpdate = now
                        }
                    }
                }
            }

            // Move temp to final
            if (targetFile.exists()) targetFile.delete()
            tempFile.renameTo(targetFile)

            modelDownloadManager.onDownloadComplete()
            println("ModelDownloadService: Download complete: ${targetFile.absolutePath}")

        } catch (e: Exception) {
            tempFile.delete()
            println("ModelDownloadService: Download failed: ${e.message}")
            modelDownloadManager.updateDownloadState(
                ModelDownloadState.Error("Download failed: ${e.message}")
            )
        } finally {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null

        // Clean up temp files
        val modelsDir = File(filesDir, "models")
        modelsDir.listFiles()?.filter { it.name.endsWith(".tmp") }?.forEach { it.delete() }

        modelDownloadManager.updateDownloadState(ModelDownloadState.NotDownloaded)

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        downloadJob?.cancel()
        downloadJob = null
        super.onDestroy()
    }

    // ---- Notification ----

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Model Downloads",
                NotificationManager.IMPORTANCE_LOW // No sound, just progress
            ).apply {
                description = "Shows progress when downloading AI models"
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildProgressNotification(modelName: String, percent: Int): Notification {
        val cancelIntent = Intent(this, ModelDownloadService::class.java).apply {
            action = ACTION_CANCEL
        }
        val cancelPendingIntent = PendingIntent.getService(
            this, 0, cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Downloading $modelName")
            .setContentText("$percent% complete")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, percent, false)
            .setOngoing(true)
            .setSilent(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Cancel",
                cancelPendingIntent
            )
            .build()
    }

    private fun updateNotification(modelName: String, percent: Int) {
        val notification = buildProgressNotification(modelName, percent)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }
}
