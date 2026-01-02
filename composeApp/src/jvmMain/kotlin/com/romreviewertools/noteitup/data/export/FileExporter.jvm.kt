package com.romreviewertools.noteitup.data.export

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File

actual class FileExporter {
    actual suspend fun exportToFile(
        fileName: String,
        content: String,
        mimeType: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val userHome = System.getProperty("user.home")
            val exportDir = File(userHome, ".noteitup/exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val file = File(exportDir, fileName)
            file.writeText(content)
            file.absolutePath
        }
    }

    actual suspend fun shareFile(filePath: String, mimeType: String) {
        withContext(Dispatchers.IO) {
            val file = File(filePath)
            if (file.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file.parentFile)
            }
        }
    }
}
