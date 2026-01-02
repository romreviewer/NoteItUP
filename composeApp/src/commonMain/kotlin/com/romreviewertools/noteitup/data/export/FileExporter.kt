package com.romreviewertools.noteitup.data.export

expect class FileExporter {
    suspend fun exportToFile(
        fileName: String,
        content: String,
        mimeType: String
    ): Result<String>

    suspend fun shareFile(filePath: String, mimeType: String)
}
