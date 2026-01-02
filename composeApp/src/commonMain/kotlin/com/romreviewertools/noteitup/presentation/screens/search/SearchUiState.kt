package com.romreviewertools.noteitup.presentation.screens.search

import com.romreviewertools.noteitup.domain.model.DiaryEntry

data class SearchUiState(
    val query: String = "",
    val results: List<DiaryEntry> = emptyList(),
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val error: String? = null
)
