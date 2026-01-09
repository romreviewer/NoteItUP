package com.romreviewertools.noteitup.data.import

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

actual class TarExtractor {
    actual suspend fun extractTar(
        tarPath: String,
        outputDir: String
    ): Result<Map<String, String>> = withContext(Dispatchers.IO) {
        runCatching {
            val fileMap = mutableMapOf<String, String>()
            val outputDirectory = File(outputDir)
            outputDirectory.mkdirs()

            TarArchiveInputStream(FileInputStream(tarPath)).use { tar ->
                var entry = tar.nextTarEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val outputFile = File(outputDir, entry.name)
                        outputFile.parentFile?.mkdirs()

                        FileOutputStream(outputFile).use { output ->
                            tar.copyTo(output)
                        }

                        fileMap[entry.name] = outputFile.absolutePath
                    }
                    entry = tar.nextTarEntry
                }
            }

            fileMap
        }
    }
}
