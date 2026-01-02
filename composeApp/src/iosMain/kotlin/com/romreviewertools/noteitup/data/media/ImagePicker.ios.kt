package com.romreviewertools.noteitup.data.media

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import platform.Foundation.create
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
actual class ImagePicker {

    private var pendingGalleryCallback: ((ImagePickerResult) -> Unit)? = null
    private var pendingCameraCallback: ((ImagePickerResult) -> Unit)? = null

    actual suspend fun pickImageFromGallery(): ImagePickerResult =
        suspendCancellableCoroutine { continuation ->
            pendingGalleryCallback = { result ->
                pendingGalleryCallback = null
                continuation.resume(result)
            }
            // The actual picking is triggered by the UI layer via SwiftUI interop
        }

    actual suspend fun takePhoto(): ImagePickerResult =
        suspendCancellableCoroutine { continuation ->
            pendingCameraCallback = { result ->
                pendingCameraCallback = null
                continuation.resume(result)
            }
            // The actual camera capture is triggered by the UI layer
        }

    fun onImagePicked(imagePath: String?) {
        if (imagePath == null) {
            pendingGalleryCallback?.invoke(ImagePickerResult.Cancelled)
            pendingCameraCallback?.invoke(ImagePickerResult.Cancelled)
            return
        }

        val result = ImagePickerResult.Success(imagePath)
        pendingGalleryCallback?.invoke(result)
        pendingCameraCallback?.invoke(result)
    }

    fun onImagePickError(message: String) {
        val result = ImagePickerResult.Error(message)
        pendingGalleryCallback?.invoke(result)
        pendingCameraCallback?.invoke(result)
    }

    actual fun copyToAppStorage(sourcePath: String, fileName: String): Result<String> = runCatching {
        val imagesDir = getImagesDirectory()
        val destPath = "$imagesDir/$fileName"

        val fileManager = NSFileManager.defaultManager
        val sourceUrl = NSURL.fileURLWithPath(sourcePath)
        val destUrl = NSURL.fileURLWithPath(destPath)

        // Remove existing file if present
        if (fileManager.fileExistsAtPath(destPath)) {
            fileManager.removeItemAtPath(destPath, null)
        }

        val success = fileManager.copyItemAtURL(sourceUrl, destUrl, null)
        if (!success) {
            throw Exception("Failed to copy image to app storage")
        }

        destPath
    }

    actual fun createThumbnail(imagePath: String, maxSize: Int): Result<String> = runCatching {
        val image = UIImage.imageWithContentsOfFile(imagePath)
            ?: throw Exception("Failed to load image")

        val originalWidth = image.size.width
        val originalHeight = image.size.height

        val ratio = minOf(maxSize.toDouble() / originalWidth, maxSize.toDouble() / originalHeight)
        val newWidth = originalWidth * ratio
        val newHeight = originalHeight * ratio

        UIGraphicsBeginImageContextWithOptions(CGSizeMake(newWidth, newHeight), false, 1.0)
        image.drawInRect(platform.CoreGraphics.CGRectMake(0.0, 0.0, newWidth, newHeight))
        val scaledImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        if (scaledImage == null) {
            throw Exception("Failed to create thumbnail")
        }

        val jpegData = UIImageJPEGRepresentation(scaledImage, 0.8)
            ?: throw Exception("Failed to convert thumbnail to JPEG")

        val thumbDir = getThumbsDirectory()
        val originalFileName = imagePath.substringAfterLast("/")
        val thumbPath = "$thumbDir/thumb_$originalFileName"

        val success = jpegData.writeToFile(thumbPath, true)
        if (!success) {
            throw Exception("Failed to save thumbnail")
        }

        thumbPath
    }

    actual fun deleteImage(filePath: String): Result<Unit> = runCatching {
        val fileManager = NSFileManager.defaultManager

        if (fileManager.fileExistsAtPath(filePath)) {
            fileManager.removeItemAtPath(filePath, null)
        }

        // Also try to delete thumbnail
        val thumbPath = filePath.replace("/images/", "/thumbnails/").let {
            it.substringBeforeLast("/") + "/thumb_" + it.substringAfterLast("/")
        }
        if (fileManager.fileExistsAtPath(thumbPath)) {
            fileManager.removeItemAtPath(thumbPath, null)
        }
    }

    actual fun getImagesDirectory(): String {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        )
        val documentsDir = paths.firstOrNull() as? String
            ?: throw Exception("Could not find documents directory")

        val imagesDir = "$documentsDir/images"
        val fileManager = NSFileManager.defaultManager

        if (!fileManager.fileExistsAtPath(imagesDir)) {
            fileManager.createDirectoryAtPath(imagesDir, true, null, null)
        }

        return imagesDir
    }

    private fun getThumbsDirectory(): String {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        )
        val documentsDir = paths.firstOrNull() as? String
            ?: throw Exception("Could not find documents directory")

        val thumbsDir = "$documentsDir/thumbnails"
        val fileManager = NSFileManager.defaultManager

        if (!fileManager.fileExistsAtPath(thumbsDir)) {
            fileManager.createDirectoryAtPath(thumbsDir, true, null, null)
        }

        return thumbsDir
    }
}
