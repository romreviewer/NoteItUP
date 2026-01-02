package com.romreviewertools.noteitup.data.mapper

import com.romreviewertools.noteitup.data.database.DiaryEntryEntity
import com.romreviewertools.noteitup.data.database.FolderEntity
import com.romreviewertools.noteitup.data.database.TagEntity
import com.romreviewertools.noteitup.domain.model.DiaryEntry
import com.romreviewertools.noteitup.domain.model.Folder
import com.romreviewertools.noteitup.domain.model.Mood
import com.romreviewertools.noteitup.domain.model.Tag
import kotlinx.datetime.Instant

fun DiaryEntryEntity.toDomain(tags: List<Tag> = emptyList()): DiaryEntry {
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
        }
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
