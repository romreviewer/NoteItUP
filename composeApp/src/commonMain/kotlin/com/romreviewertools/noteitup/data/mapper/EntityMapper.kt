package com.romreviewertools.noteitup.data.mapper

import com.romreviewertools.noteitup.data.database.DiaryEntryEntity
import com.romreviewertools.noteitup.data.database.FolderEntity
import com.romreviewertools.noteitup.data.database.ImageAttachmentEntity
import com.romreviewertools.noteitup.data.database.TagEntity
import com.romreviewertools.noteitup.domain.model.DiaryEntry
import com.romreviewertools.noteitup.domain.model.Folder
import com.romreviewertools.noteitup.domain.model.ImageAttachment
import com.romreviewertools.noteitup.domain.model.Location
import com.romreviewertools.noteitup.domain.model.Mood
import com.romreviewertools.noteitup.domain.model.Tag
import kotlinx.datetime.Instant

fun DiaryEntryEntity.toDomain(
    tags: List<Tag> = emptyList(),
    images: List<ImageAttachment> = emptyList()
): DiaryEntry {
    val location = if (latitude != null && longitude != null) {
        Location(
            latitude = latitude,
            longitude = longitude,
            address = location_address,
            placeName = location_name
        )
    } else null

    return DiaryEntry(
        id = id,
        title = title,
        content = content,
        createdAt = Instant.fromEpochMilliseconds(created_at),
        updatedAt = Instant.fromEpochMilliseconds(updated_at),
        folderId = folder_id,
        tags = tags,
        isFavorite = is_favorite == 1L,
        mood = mood?.let { moodName ->
            Mood.entries.find { it.name == moodName }
        },
        images = images,
        location = location
    )
}

fun ImageAttachmentEntity.toDomain(): ImageAttachment {
    return ImageAttachment(
        id = id,
        fileName = file_name,
        filePath = file_path,
        thumbnailPath = thumbnail_path,
        createdAt = Instant.fromEpochMilliseconds(created_at)
    )
}

fun TagEntity.toDomain(): Tag {
    return Tag(
        id = id,
        name = name,
        color = color
    )
}

fun FolderEntity.toDomain(): Folder {
    return Folder(
        id = id,
        name = name,
        color = color,
        icon = icon,
        parentId = parent_id,
        createdAt = Instant.fromEpochMilliseconds(created_at),
        sortOrder = sort_order.toInt()
    )
}
