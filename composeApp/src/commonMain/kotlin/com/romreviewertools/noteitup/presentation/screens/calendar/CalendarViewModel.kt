package com.romreviewertools.noteitup.presentation.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.romreviewertools.noteitup.data.analytics.AnalyticsEvent
import com.romreviewertools.noteitup.data.analytics.AnalyticsService
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class CalendarViewModel(
    private val repository: DiaryRepository,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        analyticsService.logEvent(AnalyticsEvent.ScreenViewCalendar)
        loadEntriesForMonth()
    }

    private fun createInitialState(): CalendarUiState {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return CalendarUiState(
            currentYear = now.year,
            currentMonth = now.month
        )
    }

    fun processIntent(intent: CalendarIntent) {
        when (intent) {
            is CalendarIntent.NextMonth -> navigateToNextMonth()
            is CalendarIntent.PreviousMonth -> navigateToPreviousMonth()
            is CalendarIntent.SelectDate -> selectDate(intent.date)
            is CalendarIntent.ClearSelection -> clearSelection()
            is CalendarIntent.DismissError -> dismissError()
        }
    }

    private fun navigateToNextMonth() {
        _uiState.update { state ->
            val (newYear, newMonth) = if (state.currentMonth == Month.DECEMBER) {
                Pair(state.currentYear + 1, Month.JANUARY)
            } else {
                Pair(state.currentYear, Month.entries[state.currentMonth.ordinal + 1])
            }
            state.copy(
                currentYear = newYear,
                currentMonth = newMonth,
                selectedDate = null,
                entriesForSelectedDate = emptyList()
            )
        }
        loadEntriesForMonth()
    }

    private fun navigateToPreviousMonth() {
        _uiState.update { state ->
            val (newYear, newMonth) = if (state.currentMonth == Month.JANUARY) {
                Pair(state.currentYear - 1, Month.DECEMBER)
            } else {
                Pair(state.currentYear, Month.entries[state.currentMonth.ordinal - 1])
            }
            state.copy(
                currentYear = newYear,
                currentMonth = newMonth,
                selectedDate = null,
                entriesForSelectedDate = emptyList()
            )
        }
        loadEntriesForMonth()
    }

    private fun selectDate(date: LocalDate) {
        val entries = _uiState.value.entriesByDay[date.dayOfMonth] ?: emptyList()
        _uiState.update { state ->
            state.copy(
                selectedDate = date,
                entriesForSelectedDate = entries
            )
        }
    }

    private fun clearSelection() {
        _uiState.update { it.copy(selectedDate = null, entriesForSelectedDate = emptyList()) }
    }

    private fun loadEntriesForMonth() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val state = _uiState.value
            val timeZone = TimeZone.currentSystemDefault()

            // First day of the month
            val startDate = LocalDate(state.currentYear, state.currentMonth, 1)
            val startInstant = startDate.atStartOfDayIn(timeZone)

            // First day of next month
            val (nextYear, nextMonth) = if (state.currentMonth == Month.DECEMBER) {
                Pair(state.currentYear + 1, Month.JANUARY)
            } else {
                Pair(state.currentYear, Month.entries[state.currentMonth.ordinal + 1])
            }
            val endDate = LocalDate(nextYear, nextMonth, 1)
            val endInstant = endDate.atStartOfDayIn(timeZone)

            repository.getEntriesByDateRange(startInstant, endInstant)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load entries"
                        )
                    }
                }
                .collect { entries ->
                    // Group entries by day of month
                    val entriesByDay = entries.groupBy { entry ->
                        entry.createdAt.toLocalDateTime(timeZone).dayOfMonth
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            entriesByDay = entriesByDay
                        )
                    }
                }
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
