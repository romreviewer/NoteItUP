package com.romreviewertools.noteitup.domain.model

enum class ExportFormat(val extension: String, val mimeType: String) {
    JSON("json", "application/json"),
    CSV("csv", "text/csv"),
    MARKDOWN("md", "text/markdown")
}

data class ExportOptions(
    val format: ExportFormat = ExportFormat.JSON,
    val includeEntries: Boolean = true,
    val includeFolders: Boolean = true,
    val includeTags: Boolean = true
)

data class ExportResult(
    val success: Boolean,
    val filePath: String? = null,
    val entryCount: Int = 0,
    val error: String? = null
)
