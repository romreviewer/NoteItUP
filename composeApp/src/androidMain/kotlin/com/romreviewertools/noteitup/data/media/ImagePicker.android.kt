package com.romreviewertools.noteitup.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.coroutines.resume

actual class ImagePicker(private val context: Context) {

    private var pendingGalleryCallback: ((ImagePickerResult) -> Unit)? = null
    private var pendingCameraCallback: ((ImagePickerResult) -> Unit)? = null
    private var pendingCameraUri: Uri? = null

    actual suspend fun pickImageFromGallery(): ImagePickerResult =
        suspendCancellableCoroutine { continuation ->
            pendingGalleryCallback = { result ->
                pendingGalleryCallback = null
                continuation.resume(result)
            }
            // The actual picking is triggered by the UI layer
            // This will be resolved when onGalleryResult is called
        }

    actual suspend fun takePhoto(): ImagePickerResult =
        suspendCancellableCoroutine { continuation ->
            pendingCameraCallback = { result ->
                pendingCameraCallback = null
                continuation.resume(result)
            }
            // The actual camera capture is triggered by the UI layer
        }

    fun getCameraUri(): Uri {
        val imagesDir = File(context.filesDir, "images")
        if (!imagesDir.exists()) imagesDir.mkdirs()

        val photoFile = File(imagesDir, "camera_${UUID.randomUUID()}.jpg")
        pendingCameraUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        return pendingCameraUri!!
    }

    fun onGalleryResult(uri: Uri?) {
        if (uri == null) {
            pendingGalleryCallback?.invoke(ImagePickerResult.Cancelled)
            return
        }

        try {
            val fileName = "gallery_${UUID.randomUUID()}.jpg"
            val result = copyFromUri(uri, fileName)
            result.fold(
                onSuccess = { path ->
                    pendingGalleryCallback?.invoke(ImagePickerResult.Success(path))
                },
                onFailure = { e ->
                    pendingGalleryCallback?.invoke(ImagePickerResult.Error(e.message ?: "Failed to copy image"))
                }
            )
        } catch (e: Exception) {
            pendingGalleryCallback?.invoke(ImagePickerResult.Error(e.message ?: "Failed to process image"))
        }
    }

    fun onCameraResult(success: Boolean) {
        if (!success) {
            pendingCameraCallback?.invoke(ImagePickerResult.Cancelled)
            return
        }

        val uri = pendingCameraUri
        if (uri == null) {
            pendingCameraCallback?.invoke(ImagePickerResult.Error("Camera URI not found"))
            return
        }

        try {
            val fileName = "photo_${UUID.randomUUID()}.jpg"
            val result = copyFromUri(uri, fileName)
            result.fold(
                onSuccess = { path ->
                    pendingCameraCallback?.invoke(ImagePickerResult.Success(path))
                },
                onFailure = { e ->
                    pendingCameraCallback?.invoke(ImagePickerResult.Error(e.message ?: "Failed to save photo"))
                }
            )
        } catch (e: Exception) {
            pendingCameraCallback?.invoke(ImagePickerResult.Error(e.message ?: "Failed to process photo"))
        }
    }

    private fun copyFromUri(uri: Uri, fileName: String): Result<String> = runCatching {
        val imagesDir = File(context.filesDir, "images")
        if (!imagesDir.exists()) imagesDir.mkdirs()

        val destFile = File(imagesDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw Exception("Failed to open input stream")

        destFile.absolutePath
    }

    actual fun copyToAppStorage(sourcePath: String, fileName: String): Result<String> = runCatching {
        val imagesDir = File(context.filesDir, "images")
        if (!imagesDir.exists()) imagesDir.mkdirs()

        val sourceFile = File(sourcePath)
        val destFile = File(imagesDir, fileName)
        sourceFile.copyTo(destFile, overwrite = true)
        destFile.absolutePath
    }

    actual fun createThumbnail(imagePath: String, maxSize: Int): Result<String> = runCatching {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(imagePath, options)

        val scale = maxOf(options.outWidth, options.outHeight) / maxSize.toFloat()
        val sampleSize = if (scale > 1) scale.toInt() else 1

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        val bitmap = BitmapFactory.decodeFile(imagePath, decodeOptions)
            ?: throw Exception("Failed to decode image")

        val scaledBitmap = if (bitmap.width > maxSize || bitmap.height > maxSize) {
            val ratio = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true).also {
                if (it != bitmap) bitmap.recycle()
            }
        } else {
            bitmap
        }

        val thumbDir = File(context.filesDir, "thumbnails")
        if (!thumbDir.exists()) thumbDir.mkdirs()

        val thumbFile = File(thumbDir, "thumb_${File(imagePath).name}")
        FileOutputStream(thumbFile).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }
        scaledBitmap.recycle()

        thumbFile.absolutePath
    }

    actual fun deleteImage(filePath: String): Result<Unit> = runCatching {
        val file = File(filePath)
        if (file.exists()) {
            file.delete()
        }

        // Also try to delete thumbnail
        val thumbPath = filePath.replace("/images/", "/thumbnails/").let {
            it.substringBeforeLast("/") + "/thumb_" + it.substringAfterLast("/")
        }
        val thumbFile = File(thumbPath)
        if (thumbFile.exists()) {
            thumbFile.delete()
        }
    }

    actual fun getImagesDirectory(): String {
        val imagesDir = File(context.filesDir, "images")
        if (!imagesDir.exists()) imagesDir.mkdirs()
        return imagesDir.absolutePath
    }
}
