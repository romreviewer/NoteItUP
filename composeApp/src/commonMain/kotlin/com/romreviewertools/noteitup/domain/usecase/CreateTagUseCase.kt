package com.romreviewertools.noteitup.domain.usecase

import com.benasher44.uuid.uuid4
import com.romreviewertools.noteitup.domain.model.Tag
import com.romreviewertools.noteitup.domain.repository.DiaryRepository

class CreateTagUseCase(
    private val repository: DiaryRepository
) {
    suspend operator fun invoke(name: String, color: Long? = null): Result<Tag> {
        val tag = Tag(
            id = uuid4().toString(),
            name = name.trim(),
            color = color
        )
        return repository.createTag(tag)
    }
}
