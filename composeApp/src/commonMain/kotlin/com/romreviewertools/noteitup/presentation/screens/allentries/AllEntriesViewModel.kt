package com.romreviewertools.noteitup.presentation.screens.allentries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.romreviewertools.noteitup.domain.model.DiaryEntry
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import com.romreviewertools.noteitup.domain.usecase.GetEntriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AllEntriesUiState(
    val isLoading: Boolean = true,
    val entries: List<DiaryEntry> = emptyList(),
    val error: String? = null
)

class AllEntriesViewModel(
    private val getEntriesUseCase: GetEntriesUseCase,
    private val repository: DiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AllEntriesUiState())
    val uiState: StateFlow<AllEntriesUiState> = _uiState.asStateFlow()

    init {
        loadAllEntries()
    }

    private fun loadAllEntries() {
        viewModelScope.launch {
            getEntriesUseCase()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "An error occurred"
                        )
                    }
                }
                .collect { entries ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            entries = entries
                        )
                    }
                }
        }
    }

    fun toggleFavorite(entryId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(entryId)
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
