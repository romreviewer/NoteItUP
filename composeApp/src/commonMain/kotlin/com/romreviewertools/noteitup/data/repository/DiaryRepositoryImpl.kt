package com.romreviewertools.noteitup.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.romreviewertools.noteitup.data.database.DiaryDatabase
import com.romreviewertools.noteitup.data.mapper.toDomain
import com.romreviewertools.noteitup.domain.model.DetailedStats
import com.romreviewertools.noteitup.domain.model.DiaryEntry
import com.romreviewertools.noteitup.domain.model.DiaryStats
import com.romreviewertools.noteitup.domain.model.Folder
import com.romreviewertools.noteitup.domain.model.ImageAttachment
import com.romreviewertools.noteitup.domain.model.MonthlyEntryCount
import com.romreviewertools.noteitup.domain.model.Mood
import com.romreviewertools.noteitup.domain.model.Tag
import com.romreviewertools.noteitup.domain.repository.BrainstormMessageData
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

class DiaryRepositoryImpl(
    private val database: DiaryDatabase
) : DiaryRepository {

    private val queries = database.diaryDatabaseQueries

    override fun getAllEntries(): Flow<List<DiaryEntry>> {
        return queries.getAllEntries()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    val tags = queries.getTagsForEntry(entity.id)
                        .executeAsList()
                        .map { it.toDomain() }
                    val images = queries.getImagesForEntry(entity.id)
                        .executeAsList()
                        .map { it.toDomain() }
                    entity.toDomain(tags, images)
                }
            }
    }

    override fun getRecentEntries(limit: Int): Flow<List<DiaryEntry>> {
        return queries.getRecentEntries(limit.toLong())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    val tags = queries.getTagsForEntry(entity.id)
                        .executeAsList()
                        .map { it.toDomain() }
                    val images = queries.getImagesForEntry(entity.id)
                        .executeAsList()
                        .map { it.toDomain() }
                    entity.toDomain(tags, images)
                }
            }
    }

    override suspend fun getEntryById(id: String): DiaryEntry? = withContext(Dispatchers.IO) {
        queries.getEntryById(id).executeAsOneOrNull()?.let { entity ->
            val tags = queries.getTagsForEntry(entity.id)
                .executeAsList()
                .map { it.toDomain() }
            val images = queries.getImagesForEntry(entity.id)
                .executeAsList()
                .map { it.toDomain() }
            entity.toDomain(tags, images)
        }
    }

    override suspend fun createEntry(entry: DiaryEntry): Result<DiaryEntry> = runCatching {
        withContext(Dispatchers.IO) {
            queries.insertEntry(
                id = entry.id,
                title = entry.title,
                content = entry.content,
                created_at = entry.createdAt.toEpochMilliseconds(),
                updated_at = entry.updatedAt.toEpochMilliseconds(),
                folder_id = entry.folderId,
                is_favorite = if (entry.isFavorite) 1L else 0L,
                mood = entry.mood?.name,
                latitude = entry.location?.latitude,
                longitude = entry.location?.longitude,
                location_address = entry.location?.address,
                location_name = entry.location?.placeName
            )
            entry
        }
    }

    override suspend fun updateEntry(entry: DiaryEntry): Result<DiaryEntry> = runCatching {
        withContext(Dispatchers.IO) {
            queries.updateEntry(
                title = entry.title,
                content = entry.content,
                updated_at = entry.updatedAt.toEpochMilliseconds(),
                folder_id = entry.folderId,
                is_favorite = if (entry.isFavorite) 1L else 0L,
                mood = entry.mood?.name,
                latitude = entry.location?.latitude,
                longitude = entry.location?.longitude,
                location_address = entry.location?.address,
                location_name = entry.location?.placeName,
                id = entry.id
            )
            entry
        }
    }

    override suspend fun deleteEntry(id: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.removeAllTagsFromEntry(id)
            queries.deleteEntry(id)
        }
    }

    override suspend fun toggleFavorite(id: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.toggleFavorite(
                updated_at = Clock.System.now().toEpochMilliseconds(),
                id = id
            )
        }
    }

    override fun searchEntries(query: String): Flow<List<DiaryEntry>> {
        return queries.searchEntries(query, query)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    val tags = queries.getTagsForEntry(entity.id)
                        .executeAsList()
                        .map { it.toDomain() }
                    val images = queries.getImagesForEntry(entity.id)
                        .executeAsList()
                        .map { it.toDomain() }
                    entity.toDomain(tags, images)
                }
            }
    }

    override fun getAllTags(): Flow<List<Tag>> {
        return queries.getAllTags()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun createTag(tag: Tag): Result<Tag> = runCatching {
        withContext(Dispatchers.IO) {
            queries.insertTag(tag.id, tag.name, tag.color)
            tag
        }
    }

    override suspend fun deleteTag(id: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.deleteTag(id)
        }
    }

    override suspend fun addTagToEntry(entryId: String, tagId: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.addTagToEntry(entryId, tagId)
        }
    }

    override suspend fun removeTagFromEntry(entryId: String, tagId: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.removeTagFromEntry(entryId, tagId)
        }
    }

    override fun getEntriesByDateRange(startDate: Instant, endDate: Instant): Flow<List<DiaryEntry>> {
        return queries.getEntriesByDateRange(
            startDate.toEpochMilliseconds(),
            endDate.toEpochMilliseconds()
        )
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    val tags = queries.getTagsForEntry(entity.id)
                        .executeAsList()
                        .map { it.toDomain() }
                    val images = queries.getImagesForEntry(entity.id)
                        .executeAsList()
                        .map { it.toDomain() }
                    entity.toDomain(tags, images)
                }
            }
    }

    override fun getAllFolders(): Flow<List<Folder>> {
        return queries.getAllFolders()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun createFolder(folder: Folder): Result<Folder> = runCatching {
        withContext(Dispatchers.IO) {
            queries.insertFolder(
                id = folder.id,
                name = folder.name,
                color = folder.color,
                icon = folder.icon,
                parent_id = folder.parentId,
                created_at = folder.createdAt.toEpochMilliseconds(),
                sort_order = folder.sortOrder.toLong()
            )
            folder
        }
    }

    override suspend fun deleteFolder(id: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.deleteFolder(id)
        }
    }

    override fun getEntriesByFolder(folderId: String): Flow<List<DiaryEntry>> {
        return queries.getEntriesByFolder(folderId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    val tags = queries.getTagsForEntry(entity.id)
                        .executeAsList()
                        .map { it.toDomain() }
                    val images = queries.getImagesForEntry(entity.id)
                        .executeAsList()
                        .map { it.toDomain() }
                    entity.toDomain(tags, images)
                }
            }
    }

    override fun getStats(): Flow<DiaryStats> {
        val totalEntriesFlow = queries.getTotalEntryCount()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toInt() ?: 0 }

        val favoriteCountFlow = queries.getFavoriteCount()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toInt() ?: 0 }

        val tagCountFlow = queries.getTagCount()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toInt() ?: 0 }

        // Calculate streak (entries in last 30 days with distinct days)
        val thirtyDaysAgo = Clock.System.now()
            .minus(30, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            .toEpochMilliseconds()

        val streakFlow = queries.getStreakDays(thirtyDaysAgo)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toInt() ?: 0 }

        return combine(
            totalEntriesFlow,
            favoriteCountFlow,
            tagCountFlow,
            streakFlow
        ) { total, favorites, tags, streak ->
            DiaryStats(
                totalEntries = total,
                streakDays = streak,
                totalTags = tags,
                favoriteCount = favorites
            )
        }
    }

    override suspend fun getDetailedStats(): DetailedStats = withContext(Dispatchers.IO) {
        val totalEntries = queries.getTotalEntryCount().executeAsOne().toInt()
        val favoriteCount = queries.getFavoriteCount().executeAsOne().toInt()
        val writingDays = queries.getWritingDaysCount().executeAsOne().toInt()

        // Get mood distribution
        val moodDistribution = queries.getMoodDistribution()
            .executeAsList()
            .mapNotNull { row ->
                val mood = row.mood?.let { moodName ->
                    Mood.entries.find { it.name == moodName }
                }
                mood?.let { it to row.count.toInt() }
            }
            .toMap()

        // Get entries by month
        val entriesByMonth = queries.getEntriesByMonth()
            .executeAsList()
            .map { row ->
                MonthlyEntryCount(
                    yearMonth = row.year_month ?: "",
                    count = row.count.toInt()
                )
            }

        // Calculate total words (approximate: content length / 5)
        val totalContentLength = queries.getTotalContentLength().executeAsOneOrNull()?.SUM ?: 0L
        val totalWords = (totalContentLength / 5).toInt()
        val averageWordsPerEntry = if (totalEntries > 0) totalWords / totalEntries else 0

        // Calculate current and longest streak
        val allDates = queries.getAllEntriesCreatedAt()
            .executeAsList()
            .map { timestamp ->
                kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
            }
            .distinct()
            .sortedDescending()

        val (currentStreak, longestStreak) = calculateStreaks(allDates)

        DetailedStats(
            totalEntries = totalEntries,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalWords = totalWords,
            averageWordsPerEntry = averageWordsPerEntry,
            moodDistribution = moodDistribution,
            entriesByMonth = entriesByMonth,
            writingDays = writingDays,
            favoriteCount = favoriteCount
        )
    }

    // Image operations
    override suspend fun getImagesForEntry(entryId: String): List<ImageAttachment> =
        withContext(Dispatchers.IO) {
            queries.getImagesForEntry(entryId)
                .executeAsList()
                .map { entity ->
                    ImageAttachment(
                        id = entity.id,
                        fileName = entity.file_name,
                        filePath = entity.file_path,
                        thumbnailPath = entity.thumbnail_path,
                        createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(entity.created_at)
                    )
                }
        }

    override suspend fun addImageToEntry(
        entryId: String,
        image: ImageAttachment
    ): Result<ImageAttachment> = runCatching {
        withContext(Dispatchers.IO) {
            queries.insertImage(
                id = image.id,
                entry_id = entryId,
                file_name = image.fileName,
                file_path = image.filePath,
                thumbnail_path = image.thumbnailPath,
                created_at = image.createdAt.toEpochMilliseconds()
            )
            image
        }
    }

    override suspend fun removeImageFromEntry(imageId: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.deleteImage(imageId)
        }
    }

    override suspend fun deleteAllImagesForEntry(entryId: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.deleteImagesForEntry(entryId)
        }
    }

    // Brainstorm message operations
    override fun getBrainstormMessages(): Flow<List<BrainstormMessageData>> =
        queries.getAllBrainstormMessages()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    BrainstormMessageData(
                        id = entity.id,
                        content = entity.content,
                        isUser = entity.is_user == 1L,
                        timestamp = entity.timestamp
                    )
                }
            }

    override suspend fun insertBrainstormMessage(
        id: String,
        content: String,
        isUser: Boolean,
        timestamp: Long
    ): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.insertBrainstormMessage(id, content, if (isUser) 1L else 0L, timestamp)
        }
    }

    override suspend fun deleteAllBrainstormMessages(): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.deleteAllBrainstormMessages()
        }
    }

    private fun calculateStreaks(sortedDates: List<kotlinx.datetime.LocalDate>): Pair<Int, Int> {
        if (sortedDates.isEmpty()) return 0 to 0

        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 1

        // Check if the streak is still active (entry today or yesterday)
        val isStreakActive = sortedDates.isNotEmpty() &&
            (sortedDates.first() == today ||
             sortedDates.first() == today.minus(1, DateTimeUnit.DAY))

        for (i in 0 until sortedDates.size - 1) {
            val current = sortedDates[i]
            val next = sortedDates[i + 1]
            val daysBetween = current.toEpochDays() - next.toEpochDays()

            if (daysBetween == 1L) {
                tempStreak++
            } else {
                if (i == 0 || (i > 0 && isStreakActive)) {
                    currentStreak = tempStreak
                }
                longestStreak = maxOf(longestStreak, tempStreak)
                tempStreak = 1
            }
        }

        // Handle the final streak
        longestStreak = maxOf(longestStreak, tempStreak)
        if (isStreakActive && currentStreak == 0) {
            currentStreak = tempStreak
        }

        return currentStreak to longestStreak
    }
}
