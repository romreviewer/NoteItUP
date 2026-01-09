package com.romreviewertools.noteitup.data.import.joplin

import com.romreviewertools.noteitup.domain.model.Location
import com.romreviewertools.noteitup.domain.usecase.ExportData
import com.romreviewertools.noteitup.domain.usecase.ExportEntry
import com.romreviewertools.noteitup.domain.usecase.ExportFolder
import com.romreviewertools.noteitup.domain.usecase.ExportImage
import com.romreviewertools.noteitup.domain.usecase.ExportTag
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class JoplinParser {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Parses extracted Joplin JEX files into ExportData format.
     *
     * @param extractedFiles Map of filename -> file content (JSON strings)
     * @return ExportData ready for import
     */
    @OptIn(ExperimentalUuidApi::class)
    fun parseJoplinArchive(
        extractedFiles: Map<String, String>
    ): Result<ExportData> = runCatching {
        val notes = mutableListOf<JoplinNote>()
        val notebooks = mutableListOf<JoplinNotebook>()
        val tags = mutableListOf<JoplinTag>()
        val noteTags = mutableListOf<JoplinNoteTag>()
        val resources = mutableListOf<JoplinResource>()

        // Parse all JSON files based on type_ field
        extractedFiles.forEach { (_, content) ->
            // Skip non-JSON content
            if (!content.trim().startsWith("{")) return@forEach

            try {
                when {
                    content.contains("\"type_\":1") || content.contains("\"type_\": 1") -> {
                        runCatching { json.decodeFromString<JoplinNote>(content) }
                            .onSuccess { notes.add(it) }
                    }
                    content.contains("\"type_\":2") || content.contains("\"type_\": 2") -> {
                        runCatching { json.decodeFromString<JoplinNotebook>(content) }
                            .onSuccess { notebooks.add(it) }
                    }
                    content.contains("\"type_\":5") || content.contains("\"type_\": 5") -> {
                        runCatching { json.decodeFromString<JoplinTag>(content) }
                            .onSuccess { tags.add(it) }
                    }
                    content.contains("\"type_\":6") || content.contains("\"type_\": 6") -> {
                        runCatching { json.decodeFromString<JoplinNoteTag>(content) }
                            .onSuccess { noteTags.add(it) }
                    }
                    content.contains("\"type_\":4") || content.contains("\"type_\": 4") -> {
                        runCatching { json.decodeFromString<JoplinResource>(content) }
                            .onSuccess { resources.add(it) }
                    }
                }
            } catch (e: Exception) {
                // Skip malformed JSON files
                println("Failed to parse Joplin file: ${e.message}")
            }
        }

        // Transform to ExportData
        transformJoplinData(notes, notebooks, tags, noteTags, resources)
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun transformJoplinData(
        notes: List<JoplinNote>,
        notebooks: List<JoplinNotebook>,
        tags: List<JoplinTag>,
        noteTags: List<JoplinNoteTag>,
        resources: List<JoplinResource>
    ): ExportData {
        // Map old IDs to new UUIDs
        val notebookIdMap = notebooks.associate { it.id to Uuid.random().toString() }
        val tagIdMap = tags.associate { it.id to Uuid.random().toString() }
        val noteIdMap = notes.associate { it.id to Uuid.random().toString() }
        val resourceIdMap = resources.associate { it.id to Uuid.random().toString() }

        // Transform notebooks to folders
        val exportFolders = notebooks.map { notebook ->
            ExportFolder(
                id = notebookIdMap[notebook.id]!!,
                name = notebook.title,
                color = null,
                icon = "📓",
                parentId = notebook.parentId?.let { notebookIdMap[it] },
                createdAt = notebook.createdTime,
                sortOrder = 0
            )
        }

        // Transform tags
        val exportTags = tags.map { tag ->
            ExportTag(
                id = tagIdMap[tag.id]!!,
                name = tag.title,
                color = null
            )
        }

        // Build note-tag relationships
        val noteToTagsMap = noteTags.groupBy { it.noteId }
            .mapValues { (_, noteTags) ->
                noteTags.mapNotNull { nt -> tagIdMap[nt.tagId] }
            }

        // Transform resources to images (filter for image types)
        val imageResources = resources.filter { resource ->
            resource.mime?.startsWith("image/") == true
        }

        val exportImages = imageResources.map { resource ->
            ExportImage(
                id = resourceIdMap[resource.id]!!,
                fileName = resource.filename ?: "${resource.id}.jpg",
                createdAt = resource.createdTime
            )
        }

        // Transform notes to entries
        val exportEntries = notes.map { note ->
            val content = if (note.isTodo == 1) {
                transformTodoContent(note.body, note.todoCompleted == 1)
            } else {
                note.body
            }

            val location = if (note.latitude != null && note.longitude != null) {
                Location(
                    latitude = note.latitude,
                    longitude = note.longitude,
                    address = null,
                    placeName = null
                )
            } else {
                null
            }

            ExportEntry(
                id = noteIdMap[note.id]!!,
                title = note.title,
                content = content,
                createdAt = note.createdTime,
                updatedAt = note.updatedTime,
                isFavorite = false,
                mood = null,
                folderId = note.parentId?.let { notebookIdMap[it] },
                tagIds = noteToTagsMap[note.id] ?: emptyList(),
                imageIds = emptyList(), // TODO: Parse resource references from body
                location = location
            )
        }

        return ExportData(
            version = 1,
            entries = exportEntries,
            folders = exportFolders,
            tags = exportTags,
            images = exportImages
        )
    }

    /**
     * Transforms Joplin to-do into content with checkbox notation.
     */
    private fun transformTodoContent(body: String, completed: Boolean): String {
        val checkbox = if (completed) "- [x]" else "- [ ]"
        return "$checkbox TODO\n\n$body"
    }
}
