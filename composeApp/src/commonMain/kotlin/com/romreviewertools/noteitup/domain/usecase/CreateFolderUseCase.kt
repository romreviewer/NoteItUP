package com.romreviewertools.noteitup.domain.usecase

import com.benasher44.uuid.uuid4
import com.romreviewertools.noteitup.domain.model.Folder
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import kotlin.time.Clock

class CreateFolderUseCase(
    private val repository: DiaryRepository
) {
    suspend operator fun invoke(
        name: String,
        color: Long? = null,
        icon: String? = null
    ): Result<Folder> {
        val folder = Folder(
            id = uuid4().toString(),
            name = name.trim(),
            color = color,
            icon = icon,
            parentId = null,
            createdAt = Clock.System.now(),
            sortOrder = 0
        )
        return repository.createFolder(folder)
    }
}
