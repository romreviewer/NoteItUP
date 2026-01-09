package com.romreviewertools.noteitup.domain.usecase

import com.romreviewertools.noteitup.data.import.TarExtractor
import com.romreviewertools.noteitup.data.import.joplin.JoplinParser
import com.romreviewertools.noteitup.data.media.ImagePicker
import com.romreviewertools.noteitup.domain.model.DiaryEntry
import com.romreviewertools.noteitup.domain.model.Folder
import com.romreviewertools.noteitup.domain.model.ImageAttachment
import com.romreviewertools.noteitup.domain.model.Mood
import com.romreviewertools.noteitup.domain.model.Tag
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import kotlinx.datetime.Instant

class ImportJoplinUseCase(
    private val diaryRepository: DiaryRepository,
    private val joplinParser: JoplinParser,
    private val tarExtractor: TarExtractor
) {
    /**
     * Imports entries from a Joplin JEX export.
     *
     * @param jexPath Path to Joplin JEX (TAR) export file
     * @param imagePicker Platform-specific image operations
     * @return ImportResult with counts and error details
     */
    suspend operator fun invoke(
        jexPath: String,
        imagePicker: ImagePicker
    ): ImportResult {
        return try {
            // 1. Extract TAR archive to temporary directory
            val tempDir = imagePicker.getImagesDirectory() + "/joplin_import_temp"
            val extractedFiles = tarExtractor.extractTar(jexPath, tempDir).getOrThrow()

            // 2. Read file contents from extracted files
            val fileContents = mutableMapOf<String, String>()
            extractedFiles.forEach { (filename, filePath) ->
                try {
                    // Only read JSON files
                    if (filename.endsWith(".json") || filename.endsWith(".md")) {
                        val content = readFileContent(filePath)
                        fileContents[filename] = content
                    }
                } catch (e: Exception) {
                    // Skip files we can't read
                    println("Failed to read file $filename: ${e.message}")
                }
            }

            // 3. Parse Joplin data
            val exportData = joplinParser.parseJoplinArchive(fileContents).getOrThrow()

            // 4. Import data using shared logic
            val imageFiles = extractedFiles.filterKeys { filename ->
                // Filter for image resources
                filename.endsWith(".jpg") || filename.endsWith(".jpeg") ||
                filename.endsWith(".png") || filename.endsWith(".gif") ||
                filename.endsWith(".webp")
            }
            val result = importExportData(exportData, imageFiles, imagePicker)

            // 5. Cleanup temp directory (optional - OS will clean up temp files)
            // cleanupTempDirectory(tempDir)

            result
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult(
                success = false,
                error = "Joplin import failed: ${e.message}"
            )
        }
    }

    /**
     * Reads file content as string.
     * Platform-specific implementation should handle file I/O.
     */
    private fun readFileContent(filePath: String): String {
        // This is a simplified implementation - in practice, you'd use platform-specific file reading
        return try {
            java.io.File(filePath).readText()
        } catch (e: Exception) {
            ""
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

        // Import folders (Joplin notebooks → NoteItUP folders)
        val folderMap = mutableMapOf<String, Folder>()
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
                folderMap[folder.id] = folder
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
                    ?: extractedImages.entries.firstOrNull {
                        it.key.endsWith(exportImage.fileName)
                    }?.value

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
                } else {
                    skippedItems.add(SkippedItem("Image", exportImage.fileName, "File not found in archive"))
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
