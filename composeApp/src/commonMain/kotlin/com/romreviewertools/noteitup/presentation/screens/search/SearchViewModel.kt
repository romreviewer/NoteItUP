package com.romreviewertools.noteitup.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.romreviewertools.noteitup.data.analytics.AnalyticsEvent
import com.romreviewertools.noteitup.data.analytics.AnalyticsService
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import com.romreviewertools.noteitup.domain.usecase.SearchEntriesUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val searchEntriesUseCase: SearchEntriesUseCase,
    private val repository: DiaryRepository,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        analyticsService.logEvent(AnalyticsEvent.ScreenViewSearch)
        observeSearchQuery()
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            queryFlow
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        flowOf(emptyList())
                    } else {
                        _uiState.update { it.copy(isLoading = true) }
                        searchEntriesUseCase(query)
                    }
                }
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Search failed"
                        )
                    }
                }
                .collect { results ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            results = results,
                            hasSearched = it.query.isNotBlank()
                        )
                    }
                }
        }
    }

    fun processIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.UpdateQuery -> updateQuery(intent.query)
            is SearchIntent.ClearQuery -> clearQuery()
            is SearchIntent.DismissError -> dismissError()
        }
    }

    private fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
        queryFlow.value = query
    }

    private fun clearQuery() {
        _uiState.update {
            it.copy(
                query = "",
                results = emptyList(),
                hasSearched = false
            )
        }
        queryFlow.value = ""
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun toggleFavorite(entryId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(entryId)
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }
}
