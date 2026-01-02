package com.romreviewertools.noteitup.domain.usecase

import com.romreviewertools.noteitup.domain.model.DiaryEntry
import com.romreviewertools.noteitup.domain.repository.DiaryRepository

class GetEntryByIdUseCase(
    private val repository: DiaryRepository
) {
    suspend operator fun invoke(id: String): DiaryEntry? = repository.getEntryById(id)
}
