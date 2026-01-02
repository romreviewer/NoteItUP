package com.romreviewertools.noteitup.domain.usecase

import com.romreviewertools.noteitup.domain.model.DiaryStats
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow

class GetStatsUseCase(
    private val repository: DiaryRepository
) {
    operator fun invoke(): Flow<DiaryStats> = repository.getStats()
}
