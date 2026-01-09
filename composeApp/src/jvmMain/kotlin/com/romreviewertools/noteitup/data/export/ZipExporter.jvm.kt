package com.romreviewertools.noteitup.data.export

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

actual class ZipExporter {
    actual suspend fun createZip(
        jsonContent: String,
        imageFiles: List<Pair<String, String>>,
        outputPath: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            // Ensure parent directory exists
            File(outputPath).parentFile?.mkdirs()

            ZipOutputStream(FileOutputStream(outputPath)).use { zip ->
                // Add JSON data file
                zip.putNextEntry(ZipEntry("data.json"))
                zip.write(jsonContent.toByteArray(Charsets.UTF_8))
                zip.closeEntry()

                // Add image files
                imageFiles.forEach { (fileName, filePath) ->
                    val file = File(filePath)
                    if (file.exists()) {
                        zip.putNextEntry(ZipEntry("images/$fileName"))
                        file.inputStream().use { input ->
                            input.copyTo(zip)
                        }
                        zip.closeEntry()
                    }
                }
            }
            outputPath
        }
    }

    actual suspend fun extractZip(
        zipPath: String,
        outputDir: String
    ): Result<Pair<String, Map<String, String>>> = withContext(Dispatchers.IO) {
        runCatching {
            var jsonContent = ""
            val imageMap = mutableMapOf<String, String>()

            // Ensure output directory exists
            File(outputDir).mkdirs()

            ZipInputStream(File(zipPath).inputStream()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    when {
                        entry.name == "data.json" -> {
                            jsonContent = zip.readBytes().toString(Charsets.UTF_8)
                        }
                        entry.name.startsWith("images/") && !entry.isDirectory -> {
                            val fileName = entry.name.substringAfter("images/")
                            val outputFile = File(outputDir, fileName)
                            outputFile.parentFile?.mkdirs()
                            FileOutputStream(outputFile).use { output ->
                                zip.copyTo(output)
                            }
                            imageMap[fileName] = outputFile.absolutePath
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }

            jsonContent to imageMap
        }
    }
}
