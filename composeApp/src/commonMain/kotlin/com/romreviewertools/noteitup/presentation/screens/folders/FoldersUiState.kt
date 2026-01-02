package com.romreviewertools.noteitup.presentation.screens.folders

import com.romreviewertools.noteitup.domain.model.Folder

data class FoldersUiState(
    val folders: List<Folder> = emptyList(),
    val isLoading: Boolean = true,
    val showCreateDialog: Boolean = false,
    val newFolderName: String = "",
    val newFolderColor: Long? = null,
    val error: String? = null
)
