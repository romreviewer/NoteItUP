package com.romreviewertools.noteitup.data.media

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.util.UUID
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.coroutines.resume

actual class ImagePicker {

    private var pendingCallback: ((ImagePickerResult) -> Unit)? = null

    actual suspend fun pickImageFromGallery(): ImagePickerResult = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            val fileChooser = JFileChooser().apply {
                dialogTitle = "Select Image"
                fileSelectionMode = JFileChooser.FILES_ONLY
                isAcceptAllFileFilterUsed = false
                addChoosableFileFilter(
                    FileNameExtensionFilter(
                        "Image files",
                        "jpg", "jpeg", "png", "gif", "bmp"
                    )
                )
            }

            val result = fileChooser.showOpenDialog(null)
            when (result) {
                JFileChooser.APPROVE_OPTION -> {
                    val selectedFile = fileChooser.selectedFile
                    if (selectedFile != null && selectedFile.exists()) {
                        val fileName = "gallery_${UUID.randomUUID()}.${selectedFile.extension}"
                        val copyResult = copyToAppStorage(selectedFile.absolutePath, fileName)
                        copyResult.fold(
                            onSuccess = { path ->
                                continuation.resume(ImagePickerResult.Success(path))
                            },
                            onFailure = { e ->
                                continuation.resume(ImagePickerResult.Error(e.message ?: "Failed to copy image"))
                            }
                        )
                    } else {
                        continuation.resume(ImagePickerResult.Error("Selected file does not exist"))
                    }
                }
                JFileChooser.CANCEL_OPTION -> {
                    continuation.resume(ImagePickerResult.Cancelled)
                }
                else -> {
                    continuation.resume(ImagePickerResult.Error("File chooser error"))
                }
            }
        }
    }

    actual suspend fun takePhoto(): ImagePickerResult {
        // Desktop doesn't have camera access
        return ImagePickerResult.Error("Camera not available on desktop")
    }

    actual fun copyToAppStorage(sourcePath: String, fileName: String): Result<String> = runCatching {
        val imagesDir = File(getImagesDirectory())
        if (!imagesDir.exists()) imagesDir.mkdirs()

        val sourceFile = File(sourcePath)
        val destFile = File(imagesDir, fileName)
        sourceFile.copyTo(destFile, overwrite = true)
        destFile.absolutePath
    }

    actual fun createThumbnail(imagePath: String, maxSize: Int): Result<String> = runCatching {
        val originalImage = ImageIO.read(File(imagePath))
            ?: throw Exception("Failed to read image")

        val originalWidth = originalImage.width
        val originalHeight = originalImage.height

        val ratio = minOf(maxSize.toFloat() / originalWidth, maxSize.toFloat() / originalHeight)
        val newWidth = (originalWidth * ratio).toInt()
        val newHeight = (originalHeight * ratio).toInt()

        val scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH)
        val bufferedImage = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB)
        val graphics = bufferedImage.createGraphics()
        graphics.drawImage(scaledImage, 0, 0, null)
        graphics.dispose()

        val thumbDir = File(getThumbsDirectory())
        if (!thumbDir.exists()) thumbDir.mkdirs()

        val originalFileName = File(imagePath).name
        val thumbFile = File(thumbDir, "thumb_$originalFileName")
        ImageIO.write(bufferedImage, "jpg", thumbFile)

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
        val userHome = System.getProperty("user.home")
        val appDir = File(userHome, ".noteitup")
        val imagesDir = File(appDir, "images")
        if (!imagesDir.exists()) imagesDir.mkdirs()
        return imagesDir.absolutePath
    }

    private fun getThumbsDirectory(): String {
        val userHome = System.getProperty("user.home")
        val appDir = File(userHome, ".noteitup")
        val thumbsDir = File(appDir, "thumbnails")
        if (!thumbsDir.exists()) thumbsDir.mkdirs()
        return thumbsDir.absolutePath
    }
}
