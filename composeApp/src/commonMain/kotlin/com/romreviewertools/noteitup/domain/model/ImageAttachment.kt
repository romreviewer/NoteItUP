package com.romreviewertools.noteitup.domain.model

import kotlinx.datetime.Instant

data class ImageAttachment(
    val id: String,
    val fileName: String,
    val filePath: String,
    val thumbnailPath: String? = null,
    val createdAt: Instant
)
