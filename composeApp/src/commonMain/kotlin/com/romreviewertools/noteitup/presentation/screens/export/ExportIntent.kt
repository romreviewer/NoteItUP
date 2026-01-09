package com.romreviewertools.noteitup.presentation.screens.export

import com.romreviewertools.noteitup.domain.model.ExportFormat

sealed interface ExportIntent {
    data class SelectFormat(val format: ExportFormat) : ExportIntent
    data class ToggleIncludeEntries(val include: Boolean) : ExportIntent
    data class ToggleIncludeFolders(val include: Boolean) : ExportIntent
    data class ToggleIncludeTags(val include: Boolean) : ExportIntent
    data object StartExport : ExportIntent
    data object ShareExport : ExportIntent
    data object DismissError : ExportIntent
    data object DismissResult : ExportIntent
    data class ImportFromUri(val uri: String) : ExportIntent
    data class ImportDayOneFromUri(val uri: String) : ExportIntent
    data class ImportJoplinFromUri(val uri: String) : ExportIntent
    data object DismissImportResult : ExportIntent
}
