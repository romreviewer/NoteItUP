package com.romreviewertools.noteitup.data.export

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile

actual class FileExporter {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun exportToFile(
        fileName: String,
        content: String,
        mimeType: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val fileManager = NSFileManager.defaultManager
            val documentsUrl = fileManager.URLsForDirectory(
                NSDocumentDirectory,
                NSUserDomainMask
            ).firstOrNull() as? NSURL

            val exportDir = documentsUrl?.URLByAppendingPathComponent("exports")
            exportDir?.path?.let { path ->
                if (!fileManager.fileExistsAtPath(path)) {
                    fileManager.createDirectoryAtPath(path, withIntermediateDirectories = true, attributes = null, error = null)
                }
            }

            val fileUrl = exportDir?.URLByAppendingPathComponent(fileName)
            val filePath = fileUrl?.path ?: throw Exception("Could not create file path")

            val nsString = NSString.create(string = content)
            nsString.writeToFile(filePath, atomically = true, encoding = NSUTF8StringEncoding, error = null)

            filePath
        }
    }

    actual suspend fun shareFile(filePath: String, mimeType: String) {
        // iOS sharing is handled via SwiftUI/UIKit interop
        // This would require additional platform-specific UI code
        // For now, the file is saved and can be accessed from the Files app
    }
}
