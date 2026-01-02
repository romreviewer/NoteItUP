package com.romreviewertools.noteitup.domain.usecase

import com.romreviewertools.noteitup.domain.model.Folder
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow

class GetAllFoldersUseCase(
    private val repository: DiaryRepository
) {
    operator fun invoke(): Flow<List<Folder>> = repository.getAllFolders()
}
