package com.romreviewertools.noteitup.data.export

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual class FileImporter {
    actual suspend fun readFileContent(uri: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            File(uri).readText()
        }
    }
}
