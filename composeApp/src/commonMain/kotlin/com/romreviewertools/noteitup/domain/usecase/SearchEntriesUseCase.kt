package com.romreviewertools.noteitup.domain.usecase

import com.romreviewertools.noteitup.domain.model.DiaryEntry
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow

class SearchEntriesUseCase(
    private val repository: DiaryRepository
) {
    operator fun invoke(query: String): Flow<List<DiaryEntry>> =
        repository.searchEntries(query)
}
