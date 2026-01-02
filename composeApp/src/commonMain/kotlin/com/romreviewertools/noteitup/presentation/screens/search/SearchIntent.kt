package com.romreviewertools.noteitup.presentation.screens.search

sealed interface SearchIntent {
    data class UpdateQuery(val query: String) : SearchIntent
    data object ClearQuery : SearchIntent
    data object DismissError : SearchIntent
}
