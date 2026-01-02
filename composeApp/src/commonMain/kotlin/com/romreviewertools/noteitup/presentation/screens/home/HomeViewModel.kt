package com.romreviewertools.noteitup.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import com.romreviewertools.noteitup.domain.usecase.DeleteEntryUseCase
import com.romreviewertools.noteitup.domain.usecase.GetEntriesUseCase
import com.romreviewertools.noteitup.domain.usecase.GetStatsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getEntriesUseCase: GetEntriesUseCase,
    private val getStatsUseCase: GetStatsUseCase,
    private val deleteEntryUseCase: DeleteEntryUseCase,
    private val repository: DiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        processIntent(HomeIntent.LoadEntries)
    }

    fun processIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadEntries -> loadEntries()
            is HomeIntent.ToggleFavorite -> toggleFavorite(intent.entryId)
            is HomeIntent.DeleteEntry -> deleteEntry(intent.entryId)
            is HomeIntent.DismissError -> dismissError()
        }
    }

    private fun loadEntries() {
        viewModelScope.launch {
            combine(
                getEntriesUseCase.getRecent(20),
                getStatsUseCase()
            ) { entries, stats ->
                HomeUiState(
                    isLoading = false,
                    entries = entries,
                    stats = stats
                )
            }.catch { e ->
                emit(
                    _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "An error occurred"
                    )
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun toggleFavorite(entryId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(entryId)
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    private fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            deleteEntryUseCase(entryId)
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
