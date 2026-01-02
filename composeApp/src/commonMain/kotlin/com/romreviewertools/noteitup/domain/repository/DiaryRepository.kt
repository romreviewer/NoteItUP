package com.romreviewertools.noteitup.domain.repository

import com.romreviewertools.noteitup.domain.model.DetailedStats
import com.romreviewertools.noteitup.domain.model.DiaryEntry
import com.romreviewertools.noteitup.domain.model.DiaryStats
import com.romreviewertools.noteitup.domain.model.Folder
import com.romreviewertools.noteitup.domain.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

interface DiaryRepository {
    // Entry operations
    fun getAllEntries(): Flow<List<DiaryEntry>>
    fun getRecentEntries(limit: Int): Flow<List<DiaryEntry>>
    suspend fun getEntryById(id: String): DiaryEntry?
    suspend fun createEntry(entry: DiaryEntry): Result<DiaryEntry>
    suspend fun updateEntry(entry: DiaryEntry): Result<DiaryEntry>
    suspend fun deleteEntry(id: String): Result<Unit>
    suspend fun toggleFavorite(id: String): Result<Unit>
    fun searchEntries(query: String): Flow<List<DiaryEntry>>
    fun getEntriesByDateRange(startDate: Instant, endDate: Instant): Flow<List<DiaryEntry>>

    // Tag operations
    fun getAllTags(): Flow<List<Tag>>
    suspend fun createTag(tag: Tag): Result<Tag>
    suspend fun deleteTag(id: String): Result<Unit>
    suspend fun addTagToEntry(entryId: String, tagId: String): Result<Unit>
    suspend fun removeTagFromEntry(entryId: String, tagId: String): Result<Unit>

    // Folder operations
    fun getAllFolders(): Flow<List<Folder>>
    suspend fun createFolder(folder: Folder): Result<Folder>
    suspend fun deleteFolder(id: String): Result<Unit>
    fun getEntriesByFolder(folderId: String): Flow<List<DiaryEntry>>

    // Stats
    fun getStats(): Flow<DiaryStats>
    suspend fun getDetailedStats(): DetailedStats
}
