package com.romreviewertools.noteitup.domain.usecase

import com.romreviewertools.noteitup.domain.repository.DiaryRepository

class DeleteEntryUseCase(
    private val repository: DiaryRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.deleteEntry(id)
}
