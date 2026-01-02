package com.romreviewertools.noteitup.presentation.screens.editor

import com.romreviewertools.noteitup.domain.model.Mood

sealed interface EditorIntent {
    data class LoadEntry(val entryId: String) : EditorIntent
    data class UpdateTitle(val title: String) : EditorIntent
    data class UpdateContent(val content: String) : EditorIntent
    data class UpdateMood(val mood: Mood?) : EditorIntent
    data class ToggleTag(val tagId: String) : EditorIntent
    data class SelectFolder(val folderId: String?) : EditorIntent
    data object ToggleFavorite : EditorIntent
    data object Save : EditorIntent
    data object DismissError : EditorIntent
}
