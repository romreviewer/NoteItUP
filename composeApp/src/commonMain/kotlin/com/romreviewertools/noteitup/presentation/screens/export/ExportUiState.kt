package com.romreviewertools.noteitup.presentation.screens.export

import com.romreviewertools.noteitup.domain.model.ExportFormat
import com.romreviewertools.noteitup.domain.model.ExportOptions
import com.romreviewertools.noteitup.domain.model.ExportResult
import com.romreviewertools.noteitup.domain.usecase.ImportResult

data class ExportUiState(
    val options: ExportOptions = ExportOptions(),
    val isExporting: Boolean = false,
    val exportResult: ExportResult? = null,
    val error: String? = null,
    val isImporting: Boolean = false,
    val importResult: ImportResult? = null
)
