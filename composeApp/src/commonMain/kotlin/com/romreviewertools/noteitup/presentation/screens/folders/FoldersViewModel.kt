package com.romreviewertools.noteitup.presentation.screens.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.romreviewertools.noteitup.domain.usecase.CreateFolderUseCase
import com.romreviewertools.noteitup.domain.usecase.DeleteFolderUseCase
import com.romreviewertools.noteitup.domain.usecase.GetAllFoldersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FoldersViewModel(
    private val getAllFoldersUseCase: GetAllFoldersUseCase,
    private val createFolderUseCase: CreateFolderUseCase,
    private val deleteFolderUseCase: DeleteFolderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoldersUiState())
    val uiState: StateFlow<FoldersUiState> = _uiState.asStateFlow()

    init {
        loadFolders()
    }

    private fun loadFolders() {
        viewModelScope.launch {
            getAllFoldersUseCase()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load folders"
                        )
                    }
                }
                .collect { folders ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            folders = folders
                        )
                    }
                }
        }
    }

    fun processIntent(intent: FoldersIntent) {
        when (intent) {
            is FoldersIntent.ShowCreateDialog -> showCreateDialog()
            is FoldersIntent.DismissCreateDialog -> dismissCreateDialog()
            is FoldersIntent.UpdateNewFolderName -> updateNewFolderName(intent.name)
            is FoldersIntent.UpdateNewFolderColor -> updateNewFolderColor(intent.color)
            is FoldersIntent.CreateFolder -> createFolder()
            is FoldersIntent.DeleteFolder -> deleteFolder(intent.folderId)
            is FoldersIntent.DismissError -> dismissError()
        }
    }

    private fun showCreateDialog() {
        _uiState.update {
            it.copy(
                showCreateDialog = true,
                newFolderName = "",
                newFolderColor = null
            )
        }
    }

    private fun dismissCreateDialog() {
        _uiState.update {
            it.copy(
                showCreateDialog = false,
                newFolderName = "",
                newFolderColor = null
            )
        }
    }

    private fun updateNewFolderName(name: String) {
        _uiState.update { it.copy(newFolderName = name) }
    }

    private fun updateNewFolderColor(color: Long?) {
        _uiState.update { it.copy(newFolderColor = color) }
    }

    private fun createFolder() {
        val currentState = _uiState.value
        if (currentState.newFolderName.isBlank()) {
            _uiState.update { it.copy(error = "Folder name cannot be empty") }
            return
        }

        viewModelScope.launch {
            createFolderUseCase(currentState.newFolderName, currentState.newFolderColor)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            showCreateDialog = false,
                            newFolderName = "",
                            newFolderColor = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to create folder") }
                }
        }
    }

    private fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            deleteFolderUseCase(folderId)
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to delete folder") }
                }
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
