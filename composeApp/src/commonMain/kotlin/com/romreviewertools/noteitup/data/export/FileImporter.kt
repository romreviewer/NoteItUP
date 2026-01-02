package com.romreviewertools.noteitup.data.export

expect class FileImporter {
    suspend fun readFileContent(uri: String): Result<String>
}
