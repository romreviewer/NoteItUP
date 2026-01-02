package com.romreviewertools.noteitup.domain.usecase

import com.romreviewertools.noteitup.domain.repository.DiaryRepository

class DeleteFolderUseCase(
    private val repository: DiaryRepository
) {
    suspend operator fun invoke(folderId: String): Result<Unit> =
        repository.deleteFolder(folderId)
}
