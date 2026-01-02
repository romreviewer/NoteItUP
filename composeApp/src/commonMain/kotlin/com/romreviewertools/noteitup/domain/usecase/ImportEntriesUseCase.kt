package com.romreviewertools.noteitup.domain.usecase

import com.romreviewertools.noteitup.domain.model.DiaryEntry
import com.romreviewertools.noteitup.domain.model.Folder
import com.romreviewertools.noteitup.domain.model.Mood
import com.romreviewertools.noteitup.domain.model.Tag
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json

data class ImportResult(
    val success: Boolean,
    val entriesImported: Int = 0,
    val foldersImported: Int = 0,
    val tagsImported: Int = 0,
    val error: String? = null
)

class ImportEntriesUseCase(
    private val diaryRepository: DiaryRepository
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend operator fun invoke(jsonContent: String): ImportResult {
        return try {
            val exportData = json.decodeFromString<ExportData>(jsonContent)

            var tagsImported = 0
            var foldersImported = 0
            var entriesImported = 0

            // Import tags first (entries may reference them)
            val tagMap = mutableMapOf<String, Tag>()
            exportData.tags.forEach { exportTag ->
                val tag = Tag(
                    id = exportTag.id,
                    name = exportTag.name,
                    color = exportTag.color
                )
                diaryRepository.createTag(tag)
                tagMap[tag.id] = tag
                tagsImported++
            }

            // Import folders
            exportData.folders.forEach { exportFolder ->
                val folder = Folder(
                    id = exportFolder.id,
                    name = exportFolder.name,
                    color = exportFolder.color,
                    icon = exportFolder.icon,
                    parentId = exportFolder.parentId,
                    createdAt = Instant.fromEpochMilliseconds(exportFolder.createdAt),
                    sortOrder = exportFolder.sortOrder
                )
                diaryRepository.createFolder(folder)
                foldersImported++
            }

            // Import entries
            exportData.entries.forEach { exportEntry ->
                val entryTags = exportEntry.tagIds.mapNotNull { tagId ->
                    tagMap[tagId]
                }

                val entry = DiaryEntry(
                    id = exportEntry.id,
                    title = exportEntry.title,
                    content = exportEntry.content,
                    createdAt = Instant.fromEpochMilliseconds(exportEntry.createdAt),
                    updatedAt = Instant.fromEpochMilliseconds(exportEntry.updatedAt),
                    isFavorite = exportEntry.isFavorite,
                    mood = exportEntry.mood?.let { moodName ->
                        Mood.entries.find { it.name == moodName }
                    },
                    folderId = exportEntry.folderId,
                    tags = entryTags
                )
                diaryRepository.createEntry(entry)
                entriesImported++
            }

            ImportResult(
                success = true,
                entriesImported = entriesImported,
                foldersImported = foldersImported,
                tagsImported = tagsImported
            )
        } catch (e: Exception) {
            ImportResult(
                success = false,
                error = e.message ?: "Unknown error during import"
            )
        }
    }
}
