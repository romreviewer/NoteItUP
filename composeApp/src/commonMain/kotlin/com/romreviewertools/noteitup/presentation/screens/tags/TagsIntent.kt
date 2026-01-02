package com.romreviewertools.noteitup.presentation.screens.tags

sealed interface TagsIntent {
    data object ShowCreateDialog : TagsIntent
    data object DismissCreateDialog : TagsIntent
    data class UpdateNewTagName(val name: String) : TagsIntent
    data class UpdateNewTagColor(val color: Long?) : TagsIntent
    data object CreateTag : TagsIntent
    data class DeleteTag(val tagId: String) : TagsIntent
    data object DismissError : TagsIntent
}
