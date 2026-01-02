package com.romreviewertools.noteitup.domain.usecase

import com.benasher44.uuid.uuid4
import com.romreviewertools.noteitup.domain.model.DiaryEntry
import com.romreviewertools.noteitup.domain.model.Location
import com.romreviewertools.noteitup.domain.model.Mood
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import kotlin.time.Clock

class CreateEntryUseCase(
    private val repository: DiaryRepository
) {
    suspend operator fun invoke(
        title: String,
        content: String,
        folderId: String? = null,
        mood: Mood? = null,
        location: Location? = null
    ): Result<DiaryEntry> {
        val now = Clock.System.now()
        val entry = DiaryEntry(
            id = uuid4().toString(),
            title = title.ifBlank { "Untitled" },
            content = content,
            createdAt = now,
            updatedAt = now,
            folderId = folderId,
            mood = mood,
            location = location
        )
        return repository.createEntry(entry)
    }
}
