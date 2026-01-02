package com.romreviewertools.noteitup.presentation.screens.home

sealed interface HomeIntent {
    data object LoadEntries : HomeIntent
    data class ToggleFavorite(val entryId: String) : HomeIntent
    data class DeleteEntry(val entryId: String) : HomeIntent
    data object DismissError : HomeIntent
}
