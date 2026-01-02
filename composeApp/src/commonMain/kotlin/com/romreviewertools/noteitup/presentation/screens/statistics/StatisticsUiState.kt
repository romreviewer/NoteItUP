package com.romreviewertools.noteitup.presentation.screens.statistics

import com.romreviewertools.noteitup.domain.model.DetailedStats

data class StatisticsUiState(
    val stats: DetailedStats = DetailedStats(),
    val isLoading: Boolean = true,
    val error: String? = null
)
