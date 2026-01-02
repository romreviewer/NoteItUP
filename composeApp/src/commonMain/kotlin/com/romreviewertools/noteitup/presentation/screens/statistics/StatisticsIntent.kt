package com.romreviewertools.noteitup.presentation.screens.statistics

sealed interface StatisticsIntent {
    data object RefreshStats : StatisticsIntent
    data object DismissError : StatisticsIntent
}
