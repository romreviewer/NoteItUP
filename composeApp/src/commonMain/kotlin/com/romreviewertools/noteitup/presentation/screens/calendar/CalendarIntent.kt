package com.romreviewertools.noteitup.presentation.screens.calendar

import kotlinx.datetime.LocalDate

sealed interface CalendarIntent {
    data object NextMonth : CalendarIntent
    data object PreviousMonth : CalendarIntent
    data class SelectDate(val date: LocalDate) : CalendarIntent
    data object ClearSelection : CalendarIntent
    data object DismissError : CalendarIntent
}
