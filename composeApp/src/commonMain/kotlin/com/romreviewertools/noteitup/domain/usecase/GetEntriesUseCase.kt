package com.romreviewertools.noteitup.domain.usecase

import com.romreviewertools.noteitup.domain.model.DiaryEntry
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow

class GetEntriesUseCase(
    private val repository: DiaryRepository
) {
    operator fun invoke(): Flow<List<DiaryEntry>> = repository.getAllEntries()

    fun getRecent(limit: Int = 10): Flow<List<DiaryEntry>> = repository.getRecentEntries(limit)
}
