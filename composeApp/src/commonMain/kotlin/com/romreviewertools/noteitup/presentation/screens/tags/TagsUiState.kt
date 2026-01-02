package com.romreviewertools.noteitup.presentation.screens.tags

import com.romreviewertools.noteitup.domain.model.Tag

data class TagsUiState(
    val tags: List<Tag> = emptyList(),
    val isLoading: Boolean = true,
    val showCreateDialog: Boolean = false,
    val newTagName: String = "",
    val newTagColor: Long? = null,
    val error: String? = null
)
