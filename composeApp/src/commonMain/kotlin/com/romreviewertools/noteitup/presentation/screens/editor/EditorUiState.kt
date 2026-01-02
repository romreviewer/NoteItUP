package com.romreviewertools.noteitup.presentation.screens.editor

import com.romreviewertools.noteitup.domain.model.Folder
import com.romreviewertools.noteitup.domain.model.ImageAttachment
import com.romreviewertools.noteitup.domain.model.Location
import com.romreviewertools.noteitup.domain.model.Mood
import com.romreviewertools.noteitup.domain.model.Tag

data class EditorUiState(
    val entryId: String? = null,
    val title: String = "",
    val content: String = "",
    val mood: Mood? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val isNewEntry: Boolean = true,
    val availableTags: List<Tag> = emptyList(),
    val selectedTagIds: Set<String> = emptySet(),
    val availableFolders: List<Folder> = emptyList(),
    val selectedFolderId: String? = null,
    // Image attachments
    val images: List<ImageAttachment> = emptyList(),
    val isAddingImage: Boolean = false,
    // Location
    val location: Location? = null,
    val isLoadingLocation: Boolean = false,
    val isLocationAvailable: Boolean = true
)
