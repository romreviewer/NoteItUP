package com.romreviewertools.noteitup.presentation.screens.calendar

import com.romreviewertools.noteitup.domain.model.DiaryEntry
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

data class CalendarUiState(
    val currentYear: Int = 2024,
    val currentMonth: Month = Month.JANUARY,
    val entriesByDay: Map<Int, List<DiaryEntry>> = emptyMap(),
    val selectedDate: LocalDate? = null,
    val entriesForSelectedDate: List<DiaryEntry> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
