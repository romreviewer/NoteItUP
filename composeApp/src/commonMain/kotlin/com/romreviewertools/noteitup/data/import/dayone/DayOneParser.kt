package com.romreviewertools.noteitup.data.import.dayone

import com.romreviewertools.noteitup.domain.model.Location
import com.romreviewertools.noteitup.domain.usecase.ExportData
import com.romreviewertools.noteitup.domain.usecase.ExportEntry
import com.romreviewertools.noteitup.domain.usecase.ExportFolder
import com.romreviewertools.noteitup.domain.usecase.ExportImage
import com.romreviewertools.noteitup.domain.usecase.ExportTag
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class DayOneParser {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Parses Day One Journal.json content into ExportData format.
     *
     * @param jsonContent The JSON content from Journal.json
     * @param extractedImages Map of image filename to local file path
     * @return ExportData ready for import
     */
    @OptIn(ExperimentalUuidApi::class)
    fun parseDayOneJson(
        jsonContent: String,
        extractedImages: Map<String, String>
    ): Result<ExportData> = runCatching {
        val dayOneExport = json.decodeFromString<DayOneExport>(jsonContent)

        val allTags = mutableMapOf<String, ExportTag>()
        val exportImages = mutableListOf<ExportImage>()
        val exportEntries = mutableListOf<ExportEntry>()

        dayOneExport.entries.forEach { dayOneEntry ->
            // Extract title from content
            val title = extractTitle(dayOneEntry.text, dayOneEntry.creationDate)

            // Parse dates
            val createdAt = Instant.parse(dayOneEntry.creationDate).toEpochMilliseconds()
            val updatedAt = dayOneEntry.modifiedDate?.let {
                Instant.parse(it).toEpochMilliseconds()
            } ?: createdAt

            // Process tags (create if not exists)
            val tagIds = dayOneEntry.tags.map { tagName ->
                allTags.getOrPut(tagName) {
                    ExportTag(
                        id = Uuid.random().toString(),
                        name = tagName,
                        color = null
                    )
                }.id
            }

            // Process images
            val imageIds = dayOneEntry.photos.mapNotNull { photo ->
                val filename = photo.filename ?: "${photo.identifier}.${photo.type.lowercase()}"
                val localPath = extractedImages[filename] ?: extractedImages["photos/$filename"]

                if (localPath != null) {
                    val imageId = Uuid.random().toString()
                    exportImages.add(
                        ExportImage(
                            id = imageId,
                            fileName = filename,
                            createdAt = photo.creationDate?.let {
                                Instant.parse(it).toEpochMilliseconds()
                            } ?: createdAt
                        )
                    )
                    imageId
                } else {
                    null
                }
            }

            // Convert location
            val location = dayOneEntry.location?.let {
                Location(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    address = buildAddress(it),
                    placeName = it.placeName
                )
            }

            exportEntries.add(
                ExportEntry(
                    id = Uuid.random().toString(),
                    title = title,
                    content = dayOneEntry.text,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                    isFavorite = dayOneEntry.starred,
                    mood = null, // Day One doesn't have mood
                    folderId = null, // Day One doesn't have folders
                    tagIds = tagIds,
                    imageIds = imageIds,
                    location = location
                )
            )
        }

        ExportData(
            version = 1,
            entries = exportEntries,
            folders = emptyList(),
            tags = allTags.values.toList(),
            images = exportImages
        )
    }

    /**
     * Extracts title from Day One entry text.
     * Priority:
     * 1) First # heading in markdown
     * 2) First line if < 100 chars
     * 3) Date-based title "Entry - YYYY-MM-DD"
     */
    private fun extractTitle(text: String, creationDate: String): String {
        val lines = text.lines().filter { it.isNotBlank() }

        // Check for markdown heading
        val headingLine = lines.firstOrNull { it.trim().startsWith("#") }
        if (headingLine != null) {
            return headingLine.trim().removePrefix("#").trim()
        }

        // Use first line if short enough
        val firstLine = lines.firstOrNull()
        if (firstLine != null && firstLine.length <= 100) {
            return firstLine.take(100)
        }

        // Fallback to date
        val instant = Instant.parse(creationDate)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "Entry - ${dateTime.date}"
    }

    /**
     * Builds address string from Day One location components.
     */
    private fun buildAddress(location: DayOneLocation): String? {
        val parts = listOfNotNull(
            location.localityName,
            location.administrativeArea,
            location.country
        )
        return if (parts.isNotEmpty()) parts.joinToString(", ") else null
    }
}
