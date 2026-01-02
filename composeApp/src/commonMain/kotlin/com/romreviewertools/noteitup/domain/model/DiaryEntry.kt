package com.romreviewertools.noteitup.domain.model

import kotlinx.datetime.Instant

data class DiaryEntry(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val folderId: String? = null,
    val tags: List<Tag> = emptyList(),
    val isFavorite: Boolean = false,
    val mood: Mood? = null
)
