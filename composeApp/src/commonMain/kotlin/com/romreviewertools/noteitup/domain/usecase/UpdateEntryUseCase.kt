package com.romreviewertools.noteitup.domain.usecase

import com.romreviewertools.noteitup.domain.model.DiaryEntry
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import kotlin.time.Clock

class UpdateEntryUseCase(
    private val repository: DiaryRepository
) {
    suspend operator fun invoke(entry: DiaryEntry): Result<DiaryEntry> {
        val updatedEntry = entry.copy(updatedAt = Clock.System.now())
        return repository.updateEntry(updatedEntry)
    }
}
