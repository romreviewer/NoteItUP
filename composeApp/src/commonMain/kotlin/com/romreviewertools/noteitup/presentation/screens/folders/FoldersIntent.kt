package com.romreviewertools.noteitup.presentation.screens.folders

sealed interface FoldersIntent {
    data object ShowCreateDialog : FoldersIntent
    data object DismissCreateDialog : FoldersIntent
    data class UpdateNewFolderName(val name: String) : FoldersIntent
    data class UpdateNewFolderColor(val color: Long?) : FoldersIntent
    data object CreateFolder : FoldersIntent
    data class DeleteFolder(val folderId: String) : FoldersIntent
    data object DismissError : FoldersIntent
}
