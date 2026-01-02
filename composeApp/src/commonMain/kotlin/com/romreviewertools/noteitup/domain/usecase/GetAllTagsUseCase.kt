package com.romreviewertools.noteitup.domain.usecase

import com.romreviewertools.noteitup.domain.model.Tag
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow

class GetAllTagsUseCase(
    private val repository: DiaryRepository
) {
    operator fun invoke(): Flow<List<Tag>> = repository.getAllTags()
}
