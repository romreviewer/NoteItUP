package com.romreviewertools.noteitup.domain.usecase

import com.romreviewertools.noteitup.domain.repository.DiaryRepository

class DeleteTagUseCase(
    private val repository: DiaryRepository
) {
    suspend operator fun invoke(tagId: String): Result<Unit> =
        repository.deleteTag(tagId)
}
