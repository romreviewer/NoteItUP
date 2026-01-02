package com.romreviewertools.noteitup.domain.model

import kotlinx.datetime.Instant

data class Folder(
    val id: String,
    val name: String,
    val color: Long? = null,
    val icon: String? = null,
    val parentId: String? = null,
    val createdAt: Instant,
    val sortOrder: Int = 0
)
