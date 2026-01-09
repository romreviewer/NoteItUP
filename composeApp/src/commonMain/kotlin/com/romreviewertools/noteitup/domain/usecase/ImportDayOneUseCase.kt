package com.romreviewertools.noteitup.domain.usecase

import com.romreviewertools.noteitup.data.export.ZipExporter
import com.romreviewertools.noteitup.data.import.dayone.DayOneParser
import com.romreviewertools.noteitup.data.media.ImagePicker
import com.romreviewertools.noteitup.domain.model.DiaryEntry
import com.romreviewertools.noteitup.domain.model.Folder
import com.romreviewertools.noteitup.domain.model.ImageAttachment
import com.romreviewertools.noteitup.domain.model.Mood
import com.romreviewertools.noteitup.domain.model.Tag
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import kotlinx.datetime.Instant

class ImportDayOneUseCase(
    private val diaryRepository: DiaryRepository,
    private val dayOneParser: DayOneParser
) {
    /**
     * Imports entries from a Day One ZIP export.
     *
     * @param zipPath Path to Day One ZIP export file
     * @param zipExporter Platform-specific ZIP extractor
     * @param imagePicker Platform-specific image operations
     * @return ImportResult with counts and error details
     */
    suspend operator fun invoke(
        zipPath: String,
        zipExporter: ZipExporter,
        imagePicker: ImagePicker
    ): ImportResult {
        return try {
            // 1. Extract ZIP to temporary directory
            val tempDir = imagePicker.getImagesDirectory() + "/dayone_import_temp"
            val (jsonContent, extractedImages) = zipExporter.extractZip(zipPath, tempDir).getOrThrow()

            // 2. Parse Day One JSON
            val exportData = dayOneParser.parseDayOneJson(jsonContent, extractedImages).getOrThrow()

            // 3. Import data using shared logic
            val result = importExportData(exportData, extractedImages, imagePicker)

            // 4. Cleanup temp directory (optional - OS will clean up temp files)
            // cleanupTempDirectory(tempDir)

            result
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult(
                success = false,
                error = "Day One import failed: ${e.message}"
            )
        }
    }

    private suspend fun importExportData(
        exportData: ExportData,
        extractedImages: Map<String, String>,
        imagePicker: ImagePicker
    ): ImportResult {
        var tagsImported = 0
        var foldersImported = 0
        var entriesImported = 0
        var imagesImported = 0
        val skippedItems = mutableListOf<SkippedItem>()

        // Import tags first (entries may reference them)
        val tagMap = mutableMapOf<String, Tag>()
        exportData.tags.forEach { exportTag ->
            runCatching {
                val tag = Tag(
                    id = exportTag.id,
                    name = exportTag.name,
                    color = exportTag.color
                )
                diaryRepository.createTag(tag).getOrThrow()
                tagMap[tag.id] = tag
                tagsImported++
            }.onFailure { e ->
                skippedItems.add(SkippedItem("Tag", exportTag.name, e.message))
            }
        }

        // Import folders (Day One doesn't have folders, but include for consistency)
        exportData.folders.forEach { exportFolder ->
            runCatching {
                val folder = Folder(
                    id = exportFolder.id,
                    name = exportFolder.name,
                    color = exportFolder.color,
                    icon = exportFolder.icon,
                    parentId = exportFolder.parentId,
                    createdAt = Instant.fromEpochMilliseconds(exportFolder.createdAt),
                    sortOrder = exportFolder.sortOrder
                )
                diaryRepository.createFolder(folder).getOrThrow()
                foldersImported++
            }.onFailure { e ->
                skippedItems.add(SkippedItem("Folder", exportFolder.name, e.message))
            }
        }

        // Import images and create thumbnails
        val imageMap = mutableMapOf<String, ImageAttachment>()
        exportData.images.forEach { exportImage ->
            runCatching {
                val filePath = extractedImages[exportImage.fileName]
                    ?: extractedImages["photos/${exportImage.fileName}"]

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
            }.onFailure { e ->
                skippedItems.add(SkippedItem("Image", exportImage.fileName, e.message))
            }
        }

        // Import entries with their tags and images
        exportData.entries.forEach { exportEntry ->
            runCatching {
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
                    images = entryImages,
                    location = exportEntry.location
                )

                diaryRepository.createEntry(entry).getOrThrow()

                // Add tag associations
                entryTags.forEach { tag ->
                    diaryRepository.addTagToEntry(entry.id, tag.id)
                }

                // Add image associations
                entryImages.forEach { image ->
                    diaryRepository.addImageToEntry(entry.id, image)
                }

                entriesImported++
            }.onFailure { e ->
                skippedItems.add(SkippedItem("Entry", exportEntry.title, e.message))
            }
        }

        return ImportResult(
            success = true,
            entriesImported = entriesImported,
            foldersImported = foldersImported,
            tagsImported = tagsImported,
            imagesImported = imagesImported,
            entriesSkipped = skippedItems.count { it.type == "Entry" },
            skippedItems = skippedItems
        )
    }
}
