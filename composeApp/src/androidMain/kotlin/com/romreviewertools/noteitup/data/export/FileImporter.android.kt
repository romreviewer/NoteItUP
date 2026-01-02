package com.romreviewertools.noteitup.data.export

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class FileImporter(private val context: Context) {
    actual suspend fun readFileContent(uri: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val contentUri = Uri.parse(uri)
            context.contentResolver.openInputStream(contentUri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: throw IllegalStateException("Could not open file")
        }
    }
}
