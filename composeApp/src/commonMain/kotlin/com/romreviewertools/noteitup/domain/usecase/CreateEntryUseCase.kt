package com.romreviewertools.noteitup.domain.usecase

import com.benasher44.uuid.uuid4
import com.romreviewertools.noteitup.domain.model.DiaryEntry
import com.romreviewertools.noteitup.domain.model.Location
import com.romreviewertools.noteitup.domain.model.Mood
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class CreateEntryUseCase(
    private val repository: DiaryRepository
) {
    suspend operator fun invoke(
        content: String,
        folderId: String? = null,
        mood: Mood? = null,
        location: Location? = null
    ): Result<DiaryEntry> {
        val now = Clock.System.now()
        val entry = DiaryEntry(
            id = uuid4().toString(),
            title = extractTitleFromContent(content, now),
            content = content,
            createdAt = now,
            updatedAt = now,
            folderId = folderId,
            mood = mood,
            location = location
        )
        return repository.createEntry(entry)
    }

    private fun extractTitleFromContent(content: String, createdAt: kotlin.time.Instant): String {
        val lines = content.lines().filter { it.isNotBlank() }
        val first = lines.firstOrNull()
            ?: return "Entry - ${createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date}"
        val cleaned = if (first.trim().startsWith("#")) first.trim().removePrefix("#").trim() else first
        return cleaned.take(100).ifBlank {
            "Entry - ${createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date}"
        }
    }
}
