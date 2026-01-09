package com.romreviewertools.noteitup.domain.usecase

import com.romreviewertools.noteitup.data.export.ZipExporter
import com.romreviewertools.noteitup.data.media.ImagePicker
import com.romreviewertools.noteitup.domain.model.DiaryEntry
import com.romreviewertools.noteitup.domain.model.Folder
import com.romreviewertools.noteitup.domain.model.ImageAttachment
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
    val imagesImported: Int = 0,
    val entriesSkipped: Int = 0,
    val skippedItems: List<SkippedItem> = emptyList(),
    val error: String? = null
)

data class SkippedItem(
    val type: String,    // "Entry", "Tag", "Folder", "Image"
    val name: String,
    val reason: String?
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

    /**
     * Imports diary data from a ZIP archive that includes both JSON data and image files.
     *
     * @param zipPath Path to the ZIP file to import
     * @param zipExporter Platform-specific ZIP extractor
     * @param imagePicker Platform-specific image operations for thumbnail generation
     * @return ImportResult with counts of imported items
     */
    suspend fun importFromZip(
        zipPath: String,
        zipExporter: ZipExporter,
        imagePicker: ImagePicker
    ): ImportResult {
        return try {
            // Extract ZIP archive
            val imagesDir = imagePicker.getImagesDirectory()
            val (jsonContent, extractedImages) = zipExporter.extractZip(zipPath, imagesDir).getOrThrow()

            // Parse JSON data
            val exportData = json.decodeFromString<ExportData>(jsonContent)

            var tagsImported = 0
            var foldersImported = 0
            var entriesImported = 0
            var imagesImported = 0

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

            // Import images and create thumbnails
            val imageMap = mutableMapOf<String, ImageAttachment>()
            exportData.images.forEach { exportImage ->
                val filePath = extractedImages[exportImage.fileName]
                if (filePath != null) {
                    // Create thumbnail for the imported image
                    val thumbnailPath = imagePicker.createThumbnail(filePath, 200).getOrNull()

                    val image = ImageAttachment(
                        id = exportImage.id,
                        fileName = exportImage.fileName,
                        filePath = filePath,
                        thumbnailPath = thumbnailPath,
                        createdAt = Instant.fromEpochMilliseconds(exportImage.createdAt)
                    )
                    imageMap[exportImage.id] = image
                    imagesImported++
                }
            }

            // Import entries with their tags and images
            exportData.entries.forEach { exportEntry ->
                val entryTags = exportEntry.tagIds.mapNotNull { tagId ->
                    tagMap[tagId]
                }

                val entryImages = exportEntry.imageIds.mapNotNull { imageId ->
                    imageMap[imageId]
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
                    tags = entryTags,
                    images = entryImages
                )
                diaryRepository.createEntry(entry)

                // Add tag associations
                entryTags.forEach { tag ->
                    diaryRepository.addTagToEntry(entry.id, tag.id)
                }

                // Add image associations
                entryImages.forEach { image ->
                    diaryRepository.addImageToEntry(entry.id, image)
                }

                entriesImported++
            }

            ImportResult(
                success = true,
                entriesImported = entriesImported,
                foldersImported = foldersImported,
                tagsImported = tagsImported,
                imagesImported = imagesImported
            )
        } catch (e: Exception) {
            ImportResult(
                success = false,
                error = e.message ?: "Unknown error during ZIP import"
            )
        }
    }
}
