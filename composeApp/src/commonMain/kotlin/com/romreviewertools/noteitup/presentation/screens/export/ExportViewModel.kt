package com.romreviewertools.noteitup.presentation.screens.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.romreviewertools.noteitup.data.export.FileExporter
import com.romreviewertools.noteitup.data.export.FileImporter
import com.romreviewertools.noteitup.domain.model.ExportFormat
import com.romreviewertools.noteitup.domain.model.ExportResult
import com.romreviewertools.noteitup.domain.usecase.ExportEntriesUseCase
import com.romreviewertools.noteitup.domain.usecase.ImportEntriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

class ExportViewModel(
    private val exportEntriesUseCase: ExportEntriesUseCase,
    private val importEntriesUseCase: ImportEntriesUseCase,
    private val fileExporter: FileExporter,
    private val fileImporter: FileImporter
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    fun processIntent(intent: ExportIntent) {
        when (intent) {
            is ExportIntent.SelectFormat -> selectFormat(intent.format)
            is ExportIntent.ToggleIncludeEntries -> toggleIncludeEntries(intent.include)
            is ExportIntent.ToggleIncludeFolders -> toggleIncludeFolders(intent.include)
            is ExportIntent.ToggleIncludeTags -> toggleIncludeTags(intent.include)
            ExportIntent.StartExport -> startExport()
            ExportIntent.ShareExport -> shareExport()
            ExportIntent.DismissError -> _uiState.update { it.copy(error = null) }
            ExportIntent.DismissResult -> _uiState.update { it.copy(exportResult = null) }
            is ExportIntent.ImportFromUri -> importFromUri(intent.uri)
            ExportIntent.DismissImportResult -> _uiState.update { it.copy(importResult = null) }
        }
    }

    private fun selectFormat(format: ExportFormat) {
        _uiState.update { state ->
            state.copy(options = state.options.copy(format = format))
        }
    }

    private fun toggleIncludeEntries(include: Boolean) {
        _uiState.update { state ->
            state.copy(options = state.options.copy(includeEntries = include))
        }
    }

    private fun toggleIncludeFolders(include: Boolean) {
        _uiState.update { state ->
            state.copy(options = state.options.copy(includeFolders = include))
        }
    }

    private fun toggleIncludeTags(include: Boolean) {
        _uiState.update { state ->
            state.copy(options = state.options.copy(includeTags = include))
        }
    }

    private fun startExport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, error = null) }

            try {
                val content = exportEntriesUseCase(_uiState.value.options)
                val timestamp = Clock.System.now().toEpochMilliseconds()
                val fileName = "diary_export_$timestamp.${_uiState.value.options.format.extension}"

                val result = fileExporter.exportToFile(
                    fileName = fileName,
                    content = content,
                    mimeType = _uiState.value.options.format.mimeType
                )

                result.fold(
                    onSuccess = { filePath ->
                        _uiState.update {
                            it.copy(
                                isExporting = false,
                                exportResult = ExportResult(
                                    success = true,
                                    filePath = filePath,
                                    entryCount = content.lines().count { line -> line.contains("\"id\"") }
                                )
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isExporting = false,
                                error = error.message ?: "Export failed"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        error = e.message ?: "Export failed"
                    )
                }
            }
        }
    }

    private fun shareExport() {
        val filePath = _uiState.value.exportResult?.filePath ?: return
        val mimeType = _uiState.value.options.format.mimeType

        viewModelScope.launch {
            try {
                fileExporter.shareFile(filePath, mimeType)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Share failed") }
            }
        }
    }

    private fun importFromUri(uri: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, error = null) }

            try {
                val contentResult = fileImporter.readFileContent(uri)
                contentResult.fold(
                    onSuccess = { content ->
                        val importResult = importEntriesUseCase(content)
                        _uiState.update {
                            it.copy(
                                isImporting = false,
                                importResult = importResult,
                                error = if (!importResult.success) importResult.error else null
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isImporting = false,
                                error = error.message ?: "Failed to read file"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        error = e.message ?: "Import failed"
                    )
                }
            }
        }
    }
}
