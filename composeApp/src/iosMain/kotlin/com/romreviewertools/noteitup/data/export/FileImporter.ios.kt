package com.romreviewertools.noteitup.data.export

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

actual class FileImporter {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun readFileContent(uri: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            NSString.stringWithContentsOfFile(uri, NSUTF8StringEncoding, null)
                ?: throw IllegalStateException("Could not read file")
        }
    }
}
