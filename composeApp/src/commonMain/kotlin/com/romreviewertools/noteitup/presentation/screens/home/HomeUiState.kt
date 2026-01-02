package com.romreviewertools.noteitup.presentation.screens.home

import com.romreviewertools.noteitup.domain.model.DiaryEntry
import com.romreviewertools.noteitup.domain.model.DiaryStats

data class HomeUiState(
    val isLoading: Boolean = true,
    val entries: List<DiaryEntry> = emptyList(),
    val stats: DiaryStats = DiaryStats(0, 0, 0, 0),
    val error: String? = null,
    val userName: String? = null
)
