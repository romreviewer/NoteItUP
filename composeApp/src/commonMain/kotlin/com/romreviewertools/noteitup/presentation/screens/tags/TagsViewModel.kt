package com.romreviewertools.noteitup.presentation.screens.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.romreviewertools.noteitup.domain.usecase.CreateTagUseCase
import com.romreviewertools.noteitup.domain.usecase.DeleteTagUseCase
import com.romreviewertools.noteitup.domain.usecase.GetAllTagsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TagsViewModel(
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val createTagUseCase: CreateTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagsUiState())
    val uiState: StateFlow<TagsUiState> = _uiState.asStateFlow()

    init {
        loadTags()
    }

    private fun loadTags() {
        viewModelScope.launch {
            getAllTagsUseCase()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load tags"
                        )
                    }
                }
                .collect { tags ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            tags = tags
                        )
                    }
                }
        }
    }

    fun processIntent(intent: TagsIntent) {
        when (intent) {
            is TagsIntent.ShowCreateDialog -> showCreateDialog()
            is TagsIntent.DismissCreateDialog -> dismissCreateDialog()
            is TagsIntent.UpdateNewTagName -> updateNewTagName(intent.name)
            is TagsIntent.UpdateNewTagColor -> updateNewTagColor(intent.color)
            is TagsIntent.CreateTag -> createTag()
            is TagsIntent.DeleteTag -> deleteTag(intent.tagId)
            is TagsIntent.DismissError -> dismissError()
        }
    }

    private fun showCreateDialog() {
        _uiState.update {
            it.copy(
                showCreateDialog = true,
                newTagName = "",
                newTagColor = null
            )
        }
    }

    private fun dismissCreateDialog() {
        _uiState.update {
            it.copy(
                showCreateDialog = false,
                newTagName = "",
                newTagColor = null
            )
        }
    }

    private fun updateNewTagName(name: String) {
        _uiState.update { it.copy(newTagName = name) }
    }

    private fun updateNewTagColor(color: Long?) {
        _uiState.update { it.copy(newTagColor = color) }
    }

    private fun createTag() {
        val currentState = _uiState.value
        if (currentState.newTagName.isBlank()) {
            _uiState.update { it.copy(error = "Tag name cannot be empty") }
            return
        }

        viewModelScope.launch {
            createTagUseCase(currentState.newTagName, currentState.newTagColor)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            showCreateDialog = false,
                            newTagName = "",
                            newTagColor = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to create tag") }
                }
        }
    }

    private fun deleteTag(tagId: String) {
        viewModelScope.launch {
            deleteTagUseCase(tagId)
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to delete tag") }
                }
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
