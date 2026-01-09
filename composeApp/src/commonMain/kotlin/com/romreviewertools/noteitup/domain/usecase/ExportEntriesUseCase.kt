package com.romreviewertools.noteitup.domain.usecase

import com.romreviewertools.noteitup.domain.model.DiaryEntry
import com.romreviewertools.noteitup.domain.model.ExportFormat
import com.romreviewertools.noteitup.domain.model.ExportOptions
import com.romreviewertools.noteitup.domain.model.Folder
import com.romreviewertools.noteitup.domain.model.ImageAttachment
import com.romreviewertools.noteitup.domain.model.Location
import com.romreviewertools.noteitup.domain.model.Tag
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Statistics from an export operation.
 */
data class ExportStats(
    val entryCount: Int,
    val folderCount: Int,
    val tagCount: Int
)

class ExportEntriesUseCase(
    private val diaryRepository: DiaryRepository
) {
    private val json = Json { prettyPrint = true }

    /**
     * Exports data and returns both the JSON string and statistics.
     */
    suspend fun exportWithStats(options: ExportOptions): Pair<String, ExportStats> {
        val entries = if (options.includeEntries) {
            diaryRepository.getAllEntries().first()
        } else {
            emptyList()
        }

        val folders = if (options.includeFolders) {
            diaryRepository.getAllFolders().first()
        } else {
            emptyList()
        }

        val tags = if (options.includeTags) {
            diaryRepository.getAllTags().first()
        } else {
            emptyList()
        }

        val stats = ExportStats(
            entryCount = entries.size,
            folderCount = folders.size,
            tagCount = tags.size
        )

        val exportedData = when (options.format) {
            ExportFormat.JSON -> exportToJson(entries, folders, tags)
            ExportFormat.CSV -> exportToCsv(entries)
            ExportFormat.MARKDOWN -> exportToMarkdown(entries)
        }

        return exportedData to stats
    }

    suspend operator fun invoke(options: ExportOptions): String {
        val entries = if (options.includeEntries) {
            diaryRepository.getAllEntries().first()
        } else {
            emptyList()
        }

        val folders = if (options.includeFolders) {
            diaryRepository.getAllFolders().first()
        } else {
            emptyList()
        }

        val tags = if (options.includeTags) {
            diaryRepository.getAllTags().first()
        } else {
            emptyList()
        }

        return when (options.format) {
            ExportFormat.JSON -> exportToJson(entries, folders, tags)
            ExportFormat.CSV -> exportToCsv(entries)
            ExportFormat.MARKDOWN -> exportToMarkdown(entries)
        }
    }

    private fun exportToJson(
        entries: List<DiaryEntry>,
        folders: List<Folder>,
        tags: List<Tag>
    ): String {
        // Collect all images from all entries
        val allImages = entries.flatMap { entry ->
            entry.images.map { it.toExportImage() }
        }

        val exportData = ExportData(
            version = 1,
            entries = entries.map { it.toExportEntry() },
            folders = folders.map { it.toExportFolder() },
            tags = tags.map { it.toExportTag() },
            images = allImages
        )
        return json.encodeToString(exportData)
    }

    private fun exportToCsv(entries: List<DiaryEntry>): String {
        val header = "id,title,content,created_at,updated_at,is_favorite,mood,folder_id,tags"
        val rows = entries.map { entry ->
            listOf(
                entry.id,
                escapeCsv(entry.title),
                escapeCsv(entry.content),
                entry.createdAt.toEpochMilliseconds().toString(),
                entry.updatedAt.toEpochMilliseconds().toString(),
                entry.isFavorite.toString(),
                entry.mood?.name ?: "",
                entry.folderId ?: "",
                entry.tags.joinToString(";") { it.name }
            ).joinToString(",")
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    private fun exportToMarkdown(entries: List<DiaryEntry>): String {
        val sb = StringBuilder()
        sb.appendLine("# Diary Export")
        sb.appendLine()
        sb.appendLine("Exported ${entries.size} entries")
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()

        entries.forEach { entry ->
            sb.appendLine("## ${entry.title.ifBlank { "Untitled" }}")
            sb.appendLine()
            if (entry.mood != null) {
                sb.appendLine("**Mood:** ${entry.mood.emoji} ${entry.mood.label}")
            }
            sb.appendLine("**Date:** ${formatDate(entry.createdAt)}")
            if (entry.isFavorite) {
                sb.appendLine("**Favorite:** Yes")
            }
            if (entry.tags.isNotEmpty()) {
                sb.appendLine("**Tags:** ${entry.tags.joinToString(", ") { it.name }}")
            }
            sb.appendLine()
            sb.appendLine(entry.content)
            sb.appendLine()
            sb.appendLine("---")
            sb.appendLine()
        }

        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            "\"$escaped\""
        } else {
            escaped
        }
    }

    private fun formatDate(instant: kotlinx.datetime.Instant): String {
        val dateTime = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        return "${dateTime.year}-${dateTime.monthNumber.toString().padStart(2, '0')}-${dateTime.dayOfMonth.toString().padStart(2, '0')} " +
               "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
    }

    private fun DiaryEntry.toExportEntry() = ExportEntry(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt.toEpochMilliseconds(),
        updatedAt = updatedAt.toEpochMilliseconds(),
        isFavorite = isFavorite,
        mood = mood?.name,
        folderId = folderId,
        tagIds = tags.map { it.id },
        imageIds = images.map { it.id },
        location = location
    )

    private fun ImageAttachment.toExportImage() = ExportImage(
        id = id,
        fileName = fileName,
        createdAt = createdAt.toEpochMilliseconds()
    )

    private fun Folder.toExportFolder() = ExportFolder(
        id = id,
        name = name,
        color = color,
        icon = icon,
        parentId = parentId,
        createdAt = createdAt.toEpochMilliseconds(),
        sortOrder = sortOrder
    )

    private fun Tag.toExportTag() = ExportTag(
        id = id,
        name = name,
        color = color
    )
}

@Serializable
data class ExportData(
    val version: Int,
    val entries: List<ExportEntry>,
    val folders: List<ExportFolder>,
    val tags: List<ExportTag>,
    val images: List<ExportImage> = emptyList()
)

@Serializable
data class ExportEntry(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isFavorite: Boolean,
    val mood: String?,
    val folderId: String?,
    val tagIds: List<String>,
    val imageIds: List<String> = emptyList(),
    val location: Location? = null
)

@Serializable
data class ExportFolder(
    val id: String,
    val name: String,
    val color: Long?,
    val icon: String?,
    val parentId: String?,
    val createdAt: Long,
    val sortOrder: Int
)

@Serializable
data class ExportTag(
    val id: String,
    val name: String,
    val color: Long?
)

@Serializable
data class ExportImage(
    val id: String,
    val fileName: String,
    val createdAt: Long
)
