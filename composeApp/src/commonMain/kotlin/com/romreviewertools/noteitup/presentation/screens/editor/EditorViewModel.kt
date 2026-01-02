package com.romreviewertools.noteitup.presentation.screens.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.romreviewertools.noteitup.domain.model.DiaryEntry
import com.romreviewertools.noteitup.domain.model.Mood
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import com.romreviewertools.noteitup.domain.usecase.CreateEntryUseCase
import com.romreviewertools.noteitup.domain.usecase.GetAllFoldersUseCase
import com.romreviewertools.noteitup.domain.usecase.GetAllTagsUseCase
import com.romreviewertools.noteitup.domain.usecase.GetEntryByIdUseCase
import com.romreviewertools.noteitup.domain.usecase.UpdateEntryUseCase
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

class EditorViewModel(
    private val getEntryByIdUseCase: GetEntryByIdUseCase,
    private val createEntryUseCase: CreateEntryUseCase,
    private val updateEntryUseCase: UpdateEntryUseCase,
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val getAllFoldersUseCase: GetAllFoldersUseCase,
    private val repository: DiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var originalEntry: DiaryEntry? = null
    private var originalTagIds: Set<String> = emptySet()
    private var originalFolderId: String? = null

    init {
        loadAvailableTags()
        loadAvailableFolders()
    }

    private fun loadAvailableTags() {
        viewModelScope.launch {
            getAllTagsUseCase()
                .catch { /* ignore errors loading tags */ }
                .collect { tags ->
                    _uiState.update { it.copy(availableTags = tags) }
                }
        }
    }

    private fun loadAvailableFolders() {
        viewModelScope.launch {
            getAllFoldersUseCase()
                .catch { /* ignore errors loading folders */ }
                .collect { folders ->
                    _uiState.update { it.copy(availableFolders = folders) }
                }
        }
    }

    fun processIntent(intent: EditorIntent) {
        when (intent) {
            is EditorIntent.LoadEntry -> loadEntry(intent.entryId)
            is EditorIntent.UpdateTitle -> updateTitle(intent.title)
            is EditorIntent.UpdateContent -> updateContent(intent.content)
            is EditorIntent.UpdateMood -> updateMood(intent.mood)
            is EditorIntent.ToggleTag -> toggleTag(intent.tagId)
            is EditorIntent.SelectFolder -> selectFolder(intent.folderId)
            is EditorIntent.ToggleFavorite -> toggleFavorite()
            is EditorIntent.Save -> save()
            is EditorIntent.DismissError -> dismissError()
        }
    }

    private fun loadEntry(entryId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val entry = getEntryByIdUseCase(entryId)
                if (entry != null) {
                    originalEntry = entry
                    originalTagIds = entry.tags.map { it.id }.toSet()
                    originalFolderId = entry.folderId
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            entryId = entry.id,
                            title = entry.title,
                            content = entry.content,
                            mood = entry.mood,
                            isFavorite = entry.isFavorite,
                            isNewEntry = false,
                            selectedTagIds = originalTagIds,
                            selectedFolderId = originalFolderId
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Entry not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load entry"
                    )
                }
            }
        }
    }

    private fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    private fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    private fun updateMood(mood: Mood?) {
        _uiState.update { it.copy(mood = mood) }
    }

    private fun toggleTag(tagId: String) {
        _uiState.update { state ->
            val newSelectedTags = if (tagId in state.selectedTagIds) {
                state.selectedTagIds - tagId
            } else {
                state.selectedTagIds + tagId
            }
            state.copy(selectedTagIds = newSelectedTags)
        }
    }

    private fun selectFolder(folderId: String?) {
        _uiState.update { it.copy(selectedFolderId = folderId) }
    }

    private fun toggleFavorite() {
        _uiState.update { it.copy(isFavorite = !it.isFavorite) }
    }

    private fun save() {
        val currentState = _uiState.value
        if (currentState.title.isBlank() && currentState.content.isBlank()) {
            _uiState.update { it.copy(error = "Please add a title or content") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val result = if (currentState.isNewEntry) {
                createEntryUseCase(
                    title = currentState.title,
                    content = currentState.content,
                    folderId = currentState.selectedFolderId,
                    mood = currentState.mood
                )
            } else {
                val now = Clock.System.now()
                val entry = originalEntry?.copy(
                    title = currentState.title.ifBlank { "Untitled" },
                    content = currentState.content,
                    updatedAt = now,
                    folderId = currentState.selectedFolderId,
                    isFavorite = currentState.isFavorite,
                    mood = currentState.mood
                ) ?: DiaryEntry(
                    id = currentState.entryId ?: uuid4().toString(),
                    title = currentState.title.ifBlank { "Untitled" },
                    content = currentState.content,
                    createdAt = now,
                    updatedAt = now,
                    folderId = currentState.selectedFolderId,
                    isFavorite = currentState.isFavorite,
                    mood = currentState.mood
                )
                updateEntryUseCase(entry)
            }

            result
                .onSuccess { savedEntry ->
                    // Update tag associations
                    updateTagAssociations(savedEntry.id, currentState.selectedTagIds)

                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSaved = true
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = e.message ?: "Failed to save entry"
                        )
                    }
                }
        }
    }

    private suspend fun updateTagAssociations(entryId: String, selectedTagIds: Set<String>) {
        // Tags to add (in selectedTagIds but not in originalTagIds)
        val tagsToAdd = selectedTagIds - originalTagIds
        // Tags to remove (in originalTagIds but not in selectedTagIds)
        val tagsToRemove = originalTagIds - selectedTagIds

        tagsToAdd.forEach { tagId ->
            repository.addTagToEntry(entryId, tagId)
        }

        tagsToRemove.forEach { tagId ->
            repository.removeTagFromEntry(entryId, tagId)
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
