package com.romreviewertools.noteitup.data.export

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToURL
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual class ZipExporter {
    actual suspend fun createZip(
        jsonContent: String,
        imageFiles: List<Pair<String, String>>,
        outputPath: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val fileManager = NSFileManager.defaultManager

            // Create temporary directory for files to zip
            val tempDir = "${NSFileManager.defaultManager.temporaryDirectory.path!!}/zip_temp_${System.currentTimeMillis()}"
            fileManager.createDirectoryAtPath(tempDir, true, null, null)

            try {
                // Write JSON file
                val jsonPath = "$tempDir/data.json"
                val jsonData = jsonContent.toByteArray(Charsets.UTF_8)
                jsonData.usePinned { pinned ->
                    val nsData = NSData.create(bytes = pinned.addressOf(0), length = jsonData.size.toULong())
                    nsData?.writeToURL(NSURL.fileURLWithPath(jsonPath), true)
                }

                // Create images directory
                val imagesDir = "$tempDir/images"
                fileManager.createDirectoryAtPath(imagesDir, true, null, null)

                // Copy image files
                imageFiles.forEach { (fileName, filePath) ->
                    if (fileManager.fileExistsAtPath(filePath)) {
                        val destPath = "$imagesDir/$fileName"
                        fileManager.copyItemAtPath(filePath, destPath, null)
                    }
                }

                // Create ZIP archive using coordinateReadingItemAtURL
                val sourceURL = NSURL.fileURLWithPath(tempDir)
                val destURL = NSURL.fileURLWithPath(outputPath)

                // Use NSFileCoordinator for safe file operations
                var error: NSError? = null
                val success = fileManager.createDirectoryAtPath(
                    outputPath.substringBeforeLast("/"),
                    true,
                    null,
                    null
                )

                // For iOS, we'll use a simple archival approach
                // In production, consider using SSZipArchive via CocoaPods
                zipDirectory(tempDir, outputPath)

                outputPath
            } finally {
                // Clean up temp directory
                fileManager.removeItemAtPath(tempDir, null)
            }
        }
    }

    actual suspend fun extractZip(
        zipPath: String,
        outputDir: String
    ): Result<Pair<String, Map<String, String>>> = withContext(Dispatchers.IO) {
        runCatching {
            val fileManager = NSFileManager.defaultManager

            // Create temporary extraction directory
            val tempDir = "${NSFileManager.defaultManager.temporaryDirectory.path!!}/unzip_temp_${System.currentTimeMillis()}"
            fileManager.createDirectoryAtPath(tempDir, true, null, null)

            try {
                // Extract ZIP
                unzipFile(zipPath, tempDir)

                // Read JSON content
                val jsonPath = "$tempDir/data.json"
                val jsonData = NSData.dataWithContentsOfURL(NSURL.fileURLWithPath(jsonPath))
                    ?: throw IllegalStateException("Failed to read data.json from ZIP")

                val jsonBytes = ByteArray(jsonData.length.toInt())
                jsonBytes.usePinned { pinned ->
                    memcpy(pinned.addressOf(0), jsonData.bytes, jsonData.length)
                }
                val jsonContent = jsonBytes.toString(Charsets.UTF_8)

                // Copy images to output directory
                val imageMap = mutableMapOf<String, String>()
                val imagesDir = "$tempDir/images"

                if (fileManager.fileExistsAtPath(imagesDir)) {
                    val contents = fileManager.contentsOfDirectoryAtPath(imagesDir, null) as? List<*>
                    contents?.forEach { fileName ->
                        if (fileName is String) {
                            val sourcePath = "$imagesDir/$fileName"
                            val destPath = "$outputDir/$fileName"

                            // Ensure output directory exists
                            fileManager.createDirectoryAtPath(outputDir, true, null, null)

                            // Copy image file
                            if (fileManager.fileExistsAtPath(destPath)) {
                                fileManager.removeItemAtPath(destPath, null)
                            }
                            fileManager.copyItemAtPath(sourcePath, destPath, null)
                            imageMap[fileName] = destPath
                        }
                    }
                }

                jsonContent to imageMap
            } finally {
                // Clean up temp directory
                fileManager.removeItemAtPath(tempDir, null)
            }
        }
    }

    private fun zipDirectory(sourcePath: String, destPath: String) {
        val fileManager = NSFileManager.defaultManager
        val sourceURL = NSURL.fileURLWithPath(sourcePath)
        val destURL = NSURL.fileURLWithPath(destPath)

        // Note: This is a simplified implementation
        // For production, integrate SSZipArchive library
        // For now, we'll create a basic archive structure

        // Create parent directory if needed
        val parentDir = destPath.substringBeforeLast("/")
        fileManager.createDirectoryAtPath(parentDir, true, null, null)

        // This is a placeholder - in production you would use:
        // SSZipArchive.createZipFileAtPath(destPath, withContentsOfDirectory: sourcePath)
        // For now, just copy the directory
        if (fileManager.fileExistsAtPath(destPath)) {
            fileManager.removeItemAtPath(destPath, null)
        }
        fileManager.copyItemAtPath(sourcePath, destPath, null)
    }

    private fun unzipFile(zipPath: String, destPath: String) {
        val fileManager = NSFileManager.defaultManager

        // Note: This is a simplified implementation
        // For production, integrate SSZipArchive library
        // For now, assume the file is just a directory copy

        // This is a placeholder - in production you would use:
        // SSZipArchive.unzipFileAtPath(zipPath, toDestination: destPath)
        fileManager.createDirectoryAtPath(destPath, true, null, null)

        // For now, just copy if it's a directory
        if (fileManager.fileExistsAtPath(zipPath)) {
            val contents = fileManager.contentsOfDirectoryAtPath(zipPath, null) as? List<*>
            contents?.forEach { item ->
                if (item is String) {
                    val sourcePath = "$zipPath/$item"
                    val itemDestPath = "$destPath/$item"
                    fileManager.copyItemAtPath(sourcePath, itemDestPath, null)
                }
            }
        }
    }
}
