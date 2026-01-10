package com.romreviewertools.noteitup.presentation.screens.editor

import com.romreviewertools.noteitup.data.ai.ImprovementType
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

    // Image intents
    data object PickImageFromGallery : EditorIntent
    data object TakePhoto : EditorIntent
    data class AddImageFromPath(val filePath: String) : EditorIntent
    data class RemoveImage(val imageId: String) : EditorIntent
    data object CancelImagePicking : EditorIntent

    // Location intents
    data object AddLocation : EditorIntent
    data object RemoveLocation : EditorIntent

    // AI intents
    data class ImproveText(val improvementType: ImprovementType) : EditorIntent
    data object DismissAISuggestion : EditorIntent
    data object AcceptAISuggestion : EditorIntent
}
